package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.CsvFormat;
import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.JsonFormat;
import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.io.serial.csv.*;
import org.basex.io.serial.json.*;
import org.basex.query.*;
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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class Serializer implements AutoCloseable {
  /** Stack with names of opened elements. */
  protected final TokenList elems = new TokenList();
  /** Current level. */
  protected int level;
  /** Current element name. */
  protected byte[] elem;
  /** Indentation flag. */
  protected boolean indent;

  /** Stack with currently available namespaces. */
  private final Atts nspaces = new Atts(XML, QueryText.XML_URI).add(EMPTY, EMPTY);
  /** Stack with namespace size pointers. */
  private final IntList nstack = new IntList();

  /** Indicates if more than one item was serialized. */
  protected boolean more;
  /** Indicates if an element is currently being opened. */
  private boolean opening;

  /**
   * Returns an adaptive serializer.
   * @param os output stream reference
   * @return serializer
   * @throws IOException I/O exception
   */
  public static Serializer get(final OutputStream os) throws IOException {
    return get(os, null);
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

    // create print output
    final SerializerOptions so = sopts == null ? SerializerOptions.get(true) : sopts;
    final String enc = Strings.normEncoding(so.get(SerializerOptions.ENCODING), true);
    final PrintOutput po;
    if(enc == Strings.UTF8) {
      po = PrintOutput.get(os);
    } else {
      try {
        po = new EncoderOutput(os, Charset.forName(enc));
      } catch(final Exception ex) {
        throw SERENCODING_X.getIO(enc);
      }
    }
    po.setLimit(so.get(SerializerOptions.LIMIT));

    // no parameters given: serialize adaptively
    switch(so.get(SerializerOptions.METHOD)) {
      case XHTML: return new XHTMLSerializer(po, so);
      case HTML:  return new HTMLSerializer(po, so);
      case TEXT:  return new TextSerializer(po, so);
      case RAW:   return new RawSerializer(po, so);
      case CSV:
        final CsvOptions copts = so.get(SerializerOptions.CSV);
        return copts.get(CsvOptions.FORMAT) == CsvFormat.MAP
               ? new CsvMapSerializer(po, so)
               : new CsvDirectSerializer(po, so);
      case JSON:
        final JsonSerialOptions jopts = so.get(SerializerOptions.JSON);
        return jopts.get(JsonOptions.FORMAT) == JsonFormat.JSONML
               ? new JsonMLSerializer(po, so)
               : new JsonNodeSerializer(po, so);
      case XML:
        return new XMLSerializer(po, so);
      default:
        return new AdaptiveSerializer(po, so);
    }
  }

  // PUBLIC METHODS =====================================================================

  /**
   * Serializes the specified item, which may be a node or an atomic value.
   * @param item item to be serialized
   * @throws IOException I/O exception
   */
  public void serialize(final Item item) throws IOException {
    if(item instanceof ANode) {
      node((ANode) item);
    } else if(item instanceof FItem) {
      function((FItem) item);
    } else {
      atomic(item);
    }
    more = true;
  }

  /**
   * Closes the serializer.
   * @throws IOException I/O exception
   */
  @Override
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
   * Serializes the specified node.
   * @param node node to be serialized
   * @throws IOException I/O exception
   */
  protected void node(final ANode node) throws IOException {
    if(node instanceof DBNode) {
      node((DBNode) node);
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
        for(final ANode n : node.children()) node(n);
        closeDoc();
      } else {
        // serialize elements (code will never be called for attributes)
        final QNm name = node.qname();
        openElement(name.string());

        // serialize declared namespaces
        final Atts nsp = node.namespaces();
        for(int p = nsp.size() - 1; p >= 0; p--) namespace(nsp.name(p), nsp.value(p));
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
        for(ANode n; (n = ai.next()) != null;) node(n);
        indent = i;
        closeElement();
      }
    }
  }

  /**
   * Opens an element.
   * @param name element name
   * @throws IOException I/O exception
   */
  protected final void openElement(final byte[] name) throws IOException {
    prepare();
    opening = true;
    elem = name;
    startOpen(name);
    nstack.push(nspaces.size());
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
      elem = elems.pop();
      level--;
      finishClose();
    }
  }

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
   * Serializes a namespace.
   * @param prefix prefix
   * @param uri uri
   * @throws IOException I/O exception
   */
  protected void namespace(final byte[] prefix, final byte[] uri) throws IOException {
    final byte[] u = nsUri(prefix);
    if(u == null || !eq(u, uri)) {
      attribute(prefix.length == 0 ? XMLNS : concat(XMLNSC, prefix), uri);
      nspaces.add(prefix, uri);
    }
  }

  /**
   * Gets the namespace URI currently bound by the given prefix.
   * @param prefix namespace prefix
   * @return URI if found, {@code null} otherwise
   */
  protected final byte[] nsUri(final byte[] prefix) {
    for(int n = nspaces.size() - 1; n >= 0; n--) {
      if(eq(nspaces.name(n), prefix)) return nspaces.value(n);
    }
    return null;
  }

  /**
   * Serializes an attribute.
   * @param name name
   * @param value value
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void attribute(final byte[] name, final byte[] value) throws IOException { }

  /**
   * Starts an element.
   * @param name element name
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void startOpen(final byte[] name) throws IOException { }

  /**
   * Finishes an opening element node.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void finishOpen() throws IOException { }

  /**
   * Closes an empty element.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void finishEmpty() throws IOException { }

  /**
   * Closes an element.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void finishClose() throws IOException { }

  /**
   * Serializes a text.
   * @param value value
   * @param ftp full-text positions, used for visualization highlighting
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void text(final byte[] value, final FTPos ftp) throws IOException { }

  /**
   * Serializes a comment.
   * @param value value
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void comment(final byte[] value) throws IOException { }

  /**
   * Serializes a processing instruction.
   * @param name name
   * @param value value
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void pi(final byte[] name, final byte[] value) throws IOException { }

  /**
   * Serializes an atomic value.
   * @param item item
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void atomic(final Item item) throws IOException { }

  /**
   * Serializes a function item.
   * @param item item
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void function(final FItem item) throws IOException { }

  // PRIVATE METHODS ==========================================================

  /**
   * Serializes a node of the specified data reference.
   * @param node database node
   * @throws IOException I/O exception
   */
  private void node(final DBNode node) throws IOException {
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
    level++;
  }
}
