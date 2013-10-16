package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.build.JsonOptions.JsonFormat;
import org.basex.data.*;
import org.basex.io.serial.csv.*;
import org.basex.io.serial.json.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This is an interface for serializing XQuery values.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class Serializer {
  /** Default serialization parameters. */
  public static final SerializerOptions OPTIONS = new SerializerOptions();

  /** Stack with names of opened elements. */
  protected final TokenList tags = new TokenList();
  /** Current level. */
  protected int level;
  /** Current element name. */
  protected byte[] tag;
  /** Undeclare prefixes. */
  protected boolean undecl;
  /** Indentation flag. */
  protected boolean indent;

  /** Stack with currently available namespaces. */
  private final Atts nspaces = new Atts(XML, XMLURI).add(EMPTY, EMPTY);
  /** Stack with namespace size pointers. */
  private final IntList nstack = new IntList();

  /** Indicates if an element is currently being opened. */
  private boolean opening;

  /**
   * Returns an XML serializer.
   * @param os output stream reference
   * @return serializer
   * @throws IOException I/O exception
   */
  public static XMLSerializer get(final OutputStream os) throws IOException {
    return new XMLSerializer(os, OPTIONS);
  }

  /**
   * Returns a specific serializer.
   * @param os output stream reference
   * @param sopts serialization parameters (can be {@code null})
   * @return serializer
   * @throws IOException I/O exception
   */
  public static Serializer get(final OutputStream os, final SerializerOptions sopts)
      throws IOException {

    // no parameters given: serialize as XML
    if(sopts == null) return get(os);

    // standard types: XHTML, HTML, text
    switch(sopts.get(SerializerOptions.METHOD)) {
      case XHTML: return new XHTMLSerializer(os, sopts);
      case HTML:  return new HTMLSerializer(os, sopts);
      case TEXT:  return new TextSerializer(os, sopts);
      case RAW:   return new RawSerializer(os, sopts);
      case CSV:   return new CsvSerializer(os, sopts);
      case JSON:
        final JsonSerialOptions jopts = sopts.get(SerializerOptions.JSON);
        final JsonFormat format = jopts.get(JsonOptions.FORMAT);
        return format == JsonFormat.JSONML ? new JsonMLSerializer(os, sopts) :
               format == JsonFormat.MAP ? new JsonMapSerializer(os, sopts) :
               new JsonDirectSerializer(os, sopts);
      default: return new XMLSerializer(os, sopts);
    }
  }

  // PUBLIC METHODS =====================================================================

  /**
   * Serializes the specified item, which may be a node or an atomic value.
   * @param item item to be serialized
   * @throws IOException I/O exception
   */
  public void serialize(final Item item) throws IOException {
    openResult();
    if(item instanceof ANode) {
      final Type type = item.type;
      if(type == NodeType.ATT) SERATTR.thrwIO(item);
      if(type == NodeType.NSP) SERNS.thrwIO(item);
      serialize((ANode) item);
    } else if(item instanceof FItem) {
      SERFUNC.thrwIO(item.description());
    } else {
      finishElement();
      atomic(item);
    }
    closeResult();
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
    tag = name;
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
      tag = tags.pop();
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
  protected abstract void attribute(final byte[] name, final byte[] value) throws IOException;

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
  protected abstract void finishPi(final byte[] name, final byte[] value) throws IOException;

  /**
   * Serializes an atomic value.
   * @param item item
   * @throws IOException I/O exception
   */
  protected abstract void atomic(final Item item) throws IOException;

  // PRIVATE METHODS ==========================================================

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
        // serialize elements (code will never be called for attributes)
        final QNm name = node.qname();
        startElement(name.string());

        // serialize declared namespaces
        final Atts nsp = node.namespaces();
        for(int p = nsp.size() - 1; p >= 0; p--) {
          namespace(nsp.name(p), nsp.value(p));
        }
        // add new or updated namespace
        namespace(name.prefix(), name.uri());

        // serialize attributes
        final boolean i = indent;
        AxisMoreIter ai = node.attributes();
        for(ANode nd; (nd = ai.next()) != null;) {
          final byte[] n = nd.name();
          final byte[] v = nd.string();
          attribute(n, v);
          if(eq(n, XML_SPACE)) indent &= eq(v, DataText.DEFAULT);
        }
        // serialize children
        ai = node.children();
        for(ANode n; (n = ai.next()) != null;) serialize(n);
        indent = i;
        closeElement();
      }
    }
  }

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
    if(k == Data.ATTR) SERATTR.thrwIO(node);

    boolean doc = false;
    final TokenSet nsp = data.nspaces.size() != 0 ? new TokenSet() : null;
    final IntList pars = new IntList();
    final BoolList indt = new BoolList();

    // loop through all table entries
    final int s = p + data.size(p, k);
    while(p < s && !finished()) {
      k = data.kind(p);
      final int r = data.parent(p, k);

      // close opened elements...
      while(!pars.isEmpty() && pars.peek() >= r) {
        closeElement();
        indent = indt.pop();
        pars.pop();
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
          final byte[] u = data.nspaces.uri(data.uri(p, k));
          namespace(prefix(name), u == null ? EMPTY : u);

          do {
            final Atts ns = data.ns(pp);
            for(int n = 0; n < ns.size(); ++n) {
              final byte[] pref = ns.name(n);
              if(nsp.add(pref)) namespace(pref, ns.value(n));
            }
            // check ancestors only on top level
            if(level != 0) break;

            pp = data.parent(pp, data.kind(pp));
          } while(pp >= 0 && data.kind(pp) == Data.ELEM);
        }

        // serialize attributes
        indt.push(indent);
        final int as = p + data.attSize(p, k);
        while(++p != as) {
          final byte[] n = data.name(p, Data.ATTR);
          final byte[] v = data.text(p, false);
          attribute(n, v);
          if(eq(n, XML_SPACE)) indent &= eq(v, DataText.DEFAULT);
        }
        pars.push(r);
      }
    }

    // process remaining elements...
    while(!pars.isEmpty()) {
      closeElement();
      indent = indt.pop();
      pars.pop();
    }
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
    tags.push(tag);
    level++;
  }
}
