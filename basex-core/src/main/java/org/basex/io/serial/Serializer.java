package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.build.CsvOptions.CsvFormat;
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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Serializer {
  /** Stack with names of opened elements. */
  protected final TokenList elems = new TokenList();
  /** Current level. */
  protected int lvl;
  /** Current element name. */
  protected byte[] elem;
  /** Undeclare prefixes. */
  boolean undecl;
  /** Indentation flag. */
  protected boolean indent;

  /** Stack with currently available namespaces. */
  private final Atts nspaces = new Atts(XML, XML_URI).add(EMPTY, EMPTY);
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
    return new XMLSerializer(os, SerializerOptions.get(true));
  }

  /**
   * Returns a specific serializer.
   * @param os output stream reference
   * @param sopts serialization parameters (may be {@code null})
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
      case CSV:
        final CsvOptions copts = sopts.get(SerializerOptions.CSV);
        final CsvFormat cform = copts.get(CsvOptions.FORMAT);
        return cform == CsvFormat.MAP ? new CsvMapSerializer(os, sopts) :
               new CsvDirectSerializer(os, sopts);
      case JSON:
        final JsonSerialOptions jopts = sopts.get(SerializerOptions.JSON);
        final JsonFormat jform = jopts.get(JsonOptions.FORMAT);
        return jform == JsonFormat.JSONML ? new JsonMLSerializer(os, sopts) :
               jform == JsonFormat.MAP ? new JsonMapSerializer(os, sopts) :
               new JsonNodeSerializer(os, sopts);
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
    serialize(item, false);
  }

  /**
   * Serializes the specified item, which may be a node or an atomic value.
   * @param item item to be serialized
   * @param atts also serialize attributes and namespaces
   * @throws IOException I/O exception
   */
  public void serialize(final Item item, final boolean atts) throws IOException {
    serialize(item, atts, false);
  }

  /**
   * Serializes the specified item, which may be a node or an atomic value.
   * @param item item to be serialized
   * @param atts also serialize attributes and namespaces
   * @param iter iterative evaluation
   * @throws IOException I/O exception
   */
  public void serialize(final Item item, final boolean atts, final boolean iter)
      throws IOException {

    openResult();
    if(item instanceof ANode) {
      final Type type = item.type;
      if(!atts) {
        if(type == NodeType.ATT) throw SERATTR_X.getIO(item);
        if(type == NodeType.NSP) throw SERNS_X.getIO(item);
      }
      serialize((ANode) item);
    } else if(item instanceof FItem) {
      throw SERFUNC_X.getIO(item.seqType());
    } else {
      prepare();
      atomic(item, iter);
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
  final void openElement(final byte[] name) throws IOException {
    prepare();
    nstack.push(nspaces.size());
    opening = true;
    elem = name;
    startOpen(name);
  }

  /**
   * Closes an element.
   * @throws IOException I/O exception
   */
  final void closeElement() throws IOException {
    nspaces.size(nstack.pop());
    if(opening) {
      finishEmpty();
      opening = false;
    } else {
      elem = elems.pop();
      lvl--;
      finishClose();
    }
  }

  /**
   * Gets the namespace URI currently bound by the given prefix.
   * @param prefix namespace prefix
   * @return URI if found, {@code null} otherwise
   */
  final byte[] nsUri(final byte[] prefix) {
    for(int n = nspaces.size() - 1; n >= 0; n--) {
      if(eq(nspaces.name(n), prefix)) return nspaces.value(n);
    }
    return null;
  }

  /**
   * Serializes a namespace if it has not been serialized by an ancestor yet.
   * @param prefix prefix
   * @param uri namespace URI
   * @throws IOException I/O exception
   */
  protected void namespace(final byte[] prefix, final byte[] uri) throws IOException {
    if(!undecl && prefix.length != 0 && uri.length == 0) return;
    final byte[] u = nsUri(prefix);
    if(u == null || !eq(u, uri)) {
      attribute(prefix.length == 0 ? XMLNS : concat(XMLNSC, prefix), uri);
      nspaces.add(prefix, uri);
    }
  }

  /**
   * Starts a result.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  void openResult() throws IOException { }

  /**
   * Closes a result.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  void closeResult() throws IOException { }

  /**
   * Opens a document.
   * @param name name
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  void openDoc(final byte[] name) throws IOException { }

  /**
   * Closes a document.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  void closeDoc() throws IOException { }

  /**
   * Serializes an attribute.
   * @param name name
   * @param value value
   * @throws IOException I/O exception
   */
  protected abstract void attribute(final byte[] name, final byte[] value) throws IOException;

  /**
   * Starts an element.
   * @param name element name
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
   * @param ftp full-text positions, used for visualization highlighting
   * @throws IOException I/O exception
   */
  protected abstract void text(final byte[] value, final FTPos ftp) throws IOException;

  /**
   * Serializes a comment.
   * @param value value
   * @throws IOException I/O exception
   */
  protected abstract void comment(final byte[] value) throws IOException;

  /**
   * Serializes a processing instruction.
   * @param name name
   * @param value value
   * @throws IOException I/O exception
   */
  protected abstract void pi(final byte[] name, final byte[] value) throws IOException;

  /**
   * Serializes an atomic value.
   * @param item item
   * @param iter iterative evaluation
   * @throws IOException I/O exception
   */
  protected abstract void atomic(final Item item, final boolean iter) throws IOException;

  // PRIVATE METHODS ==========================================================

  /**
   * Serializes the specified node.
   * @param node node to be serialized
   * @throws IOException I/O exception
   */
  protected void serialize(final ANode node) throws IOException {
    if(node instanceof DBNode) {
      serialize((DBNode) node);
    } else {
      final Type type = node.type;
      if(type == NodeType.COM) {
        prepareComment(node.string());
      } else if(type == NodeType.TXT) {
        prepareText(node.string(), null);
      } else if(type == NodeType.PI) {
        preparePi(node.name(), node.string());
      } else if(type == NodeType.ATT) {
        attribute(node.name(), node.string());
      } else if(type == NodeType.NSP) {
        namespace(node.name(), node.string());
      } else if(type == NodeType.DOC) {
        openDoc(node.baseURI());
        for(final ANode n : node.children()) serialize(n);
        closeDoc();
      } else {
        // serialize elements (code will never be called for attributes)
        final QNm name = node.qname();
        openElement(name.string());

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
          if(eq(n, XML_SPACE) && indent) indent = eq(v, DataText.DEFAULT);
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
    final FTPosData ft = node instanceof FTPosNode ? ((FTPosNode) node).ftpos : null;
    final Data data = node.data;
    int p = node.pre;
    int k = data.kind(p);
    if(k == Data.ATTR) throw SERATTR_X.getIO(node);

    boolean doc = false;
    final TokenSet nsp = data.nspaces.size() == 0 ? null : new TokenSet();
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
        prepareText(data.text(p, true), ft != null ? ft.get(data, p) : null);
        p++;
      } else if(k == Data.COMM) {
        prepareComment(data.text(p++, true));
      } else {
        if(k == Data.PI) {
          preparePi(data.name(p, Data.PI), data.atom(p++));
        } else {
          // add element node
          final byte[] name = data.name(p, k);
          openElement(name);

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
              final int nl = ns.size();
              for(int n = 0; n < nl; n++) {
                final byte[] pref = ns.name(n);
                if(nsp.add(pref)) namespace(pref, ns.value(n));
              }
              // check ancestors only on top level
              if(lvl != 0) break;

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
            if(eq(n, XML_SPACE) && indent) indent = eq(v, DataText.DEFAULT);
          }
          pars.push(r);
        }
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
   * Serializes a comment.
   * @param value value
   * @throws IOException I/O exception
   */
  private void prepareComment(final byte[] value) throws IOException {
    prepare();
    comment(value);
  }

  /**
   * Serializes a text.
   * @param value text bytes
   * @param ftp full-text positions, used for visualization highlighting
   * @throws IOException I/O exception
   */
  private void prepareText(final byte[] value, final FTPos ftp) throws IOException {
    prepare();
    text(value, ftp);
  }

  /**
   * Serializes a processing instruction.
   * @param name name
   * @param value value
   * @throws IOException I/O exception
   */
  private void preparePi(final byte[] name, final byte[] value) throws IOException {
    prepare();
    pi(name, value);
  }

  /**
   * Finishes an opening element node if necessary.
   * @throws IOException I/O exception
   */
  private void prepare() throws IOException {
    if(!opening) return;
    opening = false;
    finishOpen();
    elems.push(elem);
    lvl++;
  }
}
