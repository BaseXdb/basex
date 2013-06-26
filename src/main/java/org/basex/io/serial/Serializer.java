package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.io.serial.SerializerProp.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This is an interface for serializing trees.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class Serializer {
  /** Default serialization parameters. */
  public static final SerializerProp PROPS = new SerializerProp();
  /** Stack of opened tags. */
  protected final TokenList tags = new TokenList();
  /** Current level. */
  protected int level;
  /** Current tag. */
  protected byte[] elem;
  /** Declare namespaces flag. */
  protected boolean undecl;
  /** Currently available namespaces. */
  protected final Atts nspaces = new Atts(XML, XMLURI).add(EMPTY, EMPTY);
  /** Namespace stack. */
  private final IntList nstack = new IntList();

  /** Indicates if an element has not been completely opened yet. */
  private boolean opening;

  /**
   * Returns an XML serializer.
   * @param os output stream reference
   * @return serializer
   * @throws IOException I/O exception
   */
  public static XMLSerializer get(final OutputStream os) throws IOException {
    return new XMLSerializer(os, PROPS);
  }

  /**
   * Returns a specific serializer.
   * @param os output stream reference
   * @param props serialization properties (can be {@code null})
   * @return serializer
   * @throws IOException I/O exception
   */
  public static Serializer get(final OutputStream os, final SerializerProp props)
      throws IOException {

    if(props == null) return get(os);
    final String m = props.check(S_METHOD, METHODS);
    if(M_XHTML.equals(m))  return new XHTMLSerializer(os, props);
    if(M_HTML.equals(m))   return new HTMLSerializer(os, props);
    if(M_TEXT.equals(m))   return new TextSerializer(os, props);
    if(M_JSON.equals(m))   return new JSONSerializer(os, props);
    if(M_JSONML.equals(m)) return new JsonMLSerializer(os, props);
    if(M_RAW.equals(m))    return new RawSerializer(os, props);
    return new XMLSerializer(os, props);
  }

  // PUBLIC METHODS =====================================================================

  /**
   * Serializes the specified item, which may be a node or an atomic value.
   * @param item item to be serialized
   * @throws IOException I/O exception
   */
  public final void serialize(final Item item) throws IOException {
    openResult();
    if(item instanceof ANode) {
      final Type type = item.type;
      if(type == NodeType.ATT) SERATTR.thrwSerial(item);
      if(type == NodeType.NSP) SERNS.thrwSerial(item);
      serialize((ANode) item);
    } else if(item instanceof FItem) {
      SERFUNC.thrwSerial(item.description());
    } else {
      finishElement();
      atomic(item);
    }
    closeResult();
  }

  /**
   * Serializes the specified node.
   * @param node node to be serialized
   * @throws IOException I/O exception
   */
  private void serialize(final ANode node) throws IOException {
    if(node instanceof DBNode) {
      serialize((DBNode) node);
    } else {
      final Type type = node.type;
      if(type == NodeType.COM) {
        comment(node.string());
      } else if(type == NodeType.ATT) {
        attribute(node.name(), node.string());
      } else if(type == NodeType.TXT) {
        text(node.string());
      } else if(type == NodeType.PI) {
        pi(node.name(), node.string());
      } else if(type == NodeType.NSP) {
        namespace(node.name(), node.string());
      } else if(type == NodeType.DOC) {
        openDoc(node.baseURI());
        for(final ANode n : node.children()) serialize(n);
        closeDoc();
      } else {
        final QNm name = node.qname();
        startElement(name.string());

        // serialize declared namespaces
        final Atts nsp = node.namespaces();
        for(int p = nsp.size() - 1; p >= 0; p--) {
          namespace(nsp.name(p), nsp.value(p));
        }

        // add new or updated namespace
        final byte[] pref = name.prefix();
        final byte[] uri = name.uri();
        final byte[] old = nsUri(pref);
        if(old == null || !eq(old, uri)) namespace(pref, uri);

        // serialize attributes
        AxisMoreIter ai = node.attributes();
        for(ANode n; (n = ai.next()) != null;) attribute(n.name(), n.string());
        // serialize children
        ai = node.children();
        for(ANode n; (n = ai.next()) != null;) serialize(n);
        closeElement();
      }
    }
  }

  /**
   * Closes the serializer.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  public void close() throws IOException { }

  /**
   * Tests if the serialization was interrupted.
   * @return result of check
   */
  public boolean finished() {
    return false;
  }

  /**
   * Resets the serializer (indentation, etc).
   */
  public void reset() { }

  // PROTECTED METHODS ==================================================================

  /**
   * Opens an element.
   * @param name element name
   * @throws IOException I/O exception
   */
  protected final void startElement(final byte[] name) throws IOException {
    finishElement();
    nstack.push(nspaces.size());
    opening = true;
    elem = name;
    startOpen(name);
  }

  /**
   * Closes an element.
   * @throws IOException I/O exception
   */
  protected final void closeElement() throws IOException {
    nspaces.size(nstack.pop());
    if(opening) {
      finishEmpty();
      opening = false;
    } else {
      elem = tags.pop();
      level--;
      finishClose();
    }
  }

  /**
   * Serializes a text.
   * @param value value
   * @param ftp full-text positions, used for visualization highlighting
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void finishText(final byte[] value, final FTPos ftp) throws IOException {
    text(value);
  }
  /**
   * Gets the namespace URI currently bound by the given prefix.
   * @param pref namespace prefix
   * @return URI if found, {@code null} otherwise
   */
  protected final byte[] nsUri(final byte[] pref) {
    for(int n = nspaces.size() - 1; n >= 0; n--) {
      if(eq(nspaces.name(n), pref)) return nspaces.value(n);
    }
    return null;
  }


  /**
   * Serializes a namespace if it has not been serialized by an ancestor yet.
   * @param pref prefix
   * @param uri URI
   * @throws IOException I/O exception
   */
  protected void namespace(final byte[] pref, final byte[] uri) throws IOException {
    if(!undecl && pref.length != 0 && uri.length == 0) return;
    final byte[] u = nsUri(pref);
    if(u == null || !eq(u, uri)) {
      attribute(pref.length == 0 ? XMLNS : concat(XMLNSC, pref), uri);
      nspaces.add(pref, uri);
    }
  }

  /**
   * Starts a result.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void openResult() throws IOException { }

  /**
   * Closes a result.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void closeResult() throws IOException { }

  /**
   * Opens a document.
   * @param name name
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void openDoc(final byte[] name) throws IOException { }

  /**
   * Closes a document.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void closeDoc() throws IOException { }

  /**
   * Serializes an attribute.
   * @param name name
   * @param value value
   * @throws IOException I/O exception
   */
  protected abstract void attribute(final byte[] name, final byte[] value)
      throws IOException;

  /**
   * Starts an element.
   * @param name tag name
   * @throws IOException I/O exception
   */
  protected abstract void startOpen(final byte[] name) throws IOException;

  /**
   * Finishes an opening element node.
   * @throws IOException I/O exception
   */
  protected abstract void finishOpen() throws IOException;

  /**
   * Closes an empty element.
   * @throws IOException I/O exception
   */
  protected abstract void finishEmpty() throws IOException;

  /**
   * Closes an element.
   * @throws IOException I/O exception
   */
  protected abstract void finishClose() throws IOException;

  /**
   * Serializes a text.
   * @param value value
   * @throws IOException I/O exception
   */
  protected abstract void finishText(final byte[] value) throws IOException;

  /**
   * Serializes a comment.
   * @param value value
   * @throws IOException I/O exception
   */
  protected abstract void finishComment(final byte[] value) throws IOException;

  /**
   * Serializes a processing instruction.
   * @param name name
   * @param value value
   * @throws IOException I/O exception
   */
  protected abstract void finishPi(final byte[] name, final byte[] value)
      throws IOException;

  /**
   * Serializes an atomic value.
   * @param item item
   * @throws IOException I/O exception
   */
  protected abstract void atomic(final Item item) throws IOException;

  // PRIVATE METHODS ==========================================================

  /**
   * Serializes a node of the specified data reference.
   * @param node database node
   * @throws IOException I/O exception
   */
  private void serialize(final DBNode node) throws IOException {
    final FTPosData ft = node instanceof FTPosNode ? ((FTPosNode) node).ft : null;
    final Data data = node.data;
    int p = node.pre;
    int k = data.kind(p);
    if(k == Data.ATTR) SERATTR.thrwSerial(node);

    boolean doc = false;
    final TokenSet nsp = data.nspaces.size() != 0 ? new TokenSet() : null;
    final IntList pars = new IntList();
    int l = 0;

    // loop through all table entries
    final int s = p + data.size(p, k);
    while(p < s && !finished()) {
      k = data.kind(p);
      final int r = data.parent(p, k);

      // close opened elements...
      while(l > 0 && pars.get(l - 1) >= r) {
        closeElement();
        --l;
      }

      if(k == Data.DOC) {
        if(doc) closeDoc();
        openDoc(data.text(p++, true));
        doc = true;
      } else if(k == Data.TEXT) {
        final FTPos ftd = ft != null ? ft.get(data, p) : null;
        if(ftd != null) text(data.text(p++, true), ftd);
        else text(data.text(p++, true));
      } else if(k == Data.COMM) {
        comment(data.text(p++, true));
      } else if(k == Data.ATTR) {
        attribute(data.name(p, k), data.text(p++, false));
      } else if(k == Data.PI) {
        pi(data.name(p, k), data.atom(p++));
      } else {
        // add element node
        final byte[] name = data.name(p, k);
        startElement(name);

        // add namespace definitions
        if(nsp != null) {
          // add namespaces from database
          nsp.clear();
          int pp = p;

          // check namespace of current element
          byte[] pref = prefix(name);
          byte[] uri = data.nspaces.uri(data.uri(p, k));
          if(uri == null) uri = EMPTY;
          final byte[] old = nsUri(pref);
          if(old == null || !eq(old, uri)) namespace(pref, uri);

          do {
            final Atts ns = data.ns(pp);
            for(int n = 0; n < ns.size(); ++n) {
              pref = ns.name(n);
              uri = ns.value(n);
              if(nsp.add(pref)) namespace(pref, uri);
            }
            // check ancestors only on top level
            if(level != 0 || l != 0) break;

            pp = data.parent(pp, data.kind(pp));
          } while(pp >= 0 && data.kind(pp) == Data.ELEM);
        }

        // serialize attributes
        final int as = p + data.attSize(p, k);
        while(++p != as) attribute(data.name(p, Data.ATTR), data.text(p, false));
        pars.set(l++, r);
      }
    }

    // process remaining elements...
    while(--l >= 0) closeElement();
    if(doc) closeDoc();
  }

  /**
   * Serializes a text.
   * @param v text bytes
   * @param ftp full-text positions, used for visualization highlighting
   * @throws IOException I/O exception
   */
  private void text(final byte[] v, final FTPos ftp) throws IOException {
    finishElement();
    finishText(v, ftp);
  }

  /**
   * Serializes a comment.
   * @param value value
   * @throws IOException I/O exception
   */
  private void comment(final byte[] value) throws IOException {
    finishElement();
    finishComment(value);
  }

  /**
   * Serializes a text.
   * @param value text bytes
   * @throws IOException I/O exception
   */
  private void text(final byte[] value) throws IOException {
    finishElement();
    finishText(value);
  }

  /**
   * Serializes a processing instruction.
   * @param name name
   * @param value value
   * @throws IOException I/O exception
   */
  private void pi(final byte[] name, final byte[] value) throws IOException {
    finishElement();
    finishPi(name, value);
  }

  /**
   * Finishes an opening element node if necessary.
   * @throws IOException I/O exception
   */
  private void finishElement() throws IOException {
    if(!opening) return;
    opening = false;
    finishOpen();
    tags.push(elem);
    level++;
  }
}
