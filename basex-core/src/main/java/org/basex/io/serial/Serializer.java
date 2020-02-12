package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.data.*;
import org.basex.io.serial.csv.*;
import org.basex.io.serial.json.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.ft.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This is an interface for serializing XQuery values.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public abstract class Serializer implements Closeable {
  /** Stack with names of opened elements. */
  protected final Stack<QNm> elems = new Stack<>();
  /** Current level. */
  protected int level;
  /** Current element name. */
  protected QNm elem;
  /** Indentation flag. */
  protected boolean indent;

  /** Stack with currently available namespaces. */
  private final Atts nspaces = new Atts(XML, QueryText.XML_URI).add(EMPTY, EMPTY);
  /** Stack with namespace size pointers. */
  private final IntList nstack = new IntList();

  /** Static context. */
  protected StaticContext sc;
  /** Indicates if at least one item was already serialized. */
  protected boolean more;
  /** Indicates if an element is currently being opened. */
  private boolean opening;

  /**
   * Returns a default serializer.
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

    // choose serializer
    final SerializerOptions so = sopts == null ? SerializerMode.DEFAULT.get() : sopts;
    switch(so.get(SerializerOptions.METHOD)) {
      case XHTML: return new XHTMLSerializer(os, so);
      case HTML:  return new HTMLSerializer(os, so);
      case TEXT:  return new TextSerializer(os, so);
      case CSV:
        final CsvOptions copts = so.get(SerializerOptions.CSV);
        return copts.get(CsvOptions.FORMAT) == CsvFormat.XQUERY
               ? new CsvXQuerySerializer(os, so)
               : new CsvDirectSerializer(os, so);
      case JSON:
        final JsonSerialOptions jopts = so.get(SerializerOptions.JSON);
        final JsonFormat jformat = jopts.get(JsonOptions.FORMAT);
        return jformat == JsonFormat.JSONML ? new JsonMLSerializer(os, so) :
               jformat == JsonFormat.BASIC  ? new JsonBasicSerializer(os, so) :
               new JsonNodeSerializer(os, so);
      case XML:
        return new XMLSerializer(os, so);
      case ADAPTIVE:
        return new AdaptiveSerializer(os, so);
      default:
        return new BaseXSerializer(os, so);
    }
  }

  // PUBLIC METHODS ===============================================================================

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

  /**
   * Assigns the static context.
   * @param sctx static context
   * @return self-reference
   */
  public Serializer sc(final StaticContext sctx) {
    sc = sctx;
    return this;
  }

  // PROTECTED METHODS ============================================================================

  /**
   * Serializes the specified node.
   * @param node node to be serialized
   * @throws IOException I/O exception
   */
  protected void node(final ANode node) throws IOException {
    if(ignore(node)) return;
    if(node instanceof DBNode) {
      node((DBNode) node);
    } else {
      node((FNode) node);
    }
  }

  /**
   * Opens an element.
   * @param name element name
   * @throws IOException I/O exception
   */
  protected final void openElement(final QNm name) throws IOException {
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
      elem = elems.peek();
      level--;
      finishClose();
      elems.pop();
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
   * @param uri namespace URI
   * @param standalone standalone flag
   * @throws IOException I/O exception
   */
  protected void namespace(final byte[] prefix, final byte[] uri, final boolean standalone)
      throws IOException {

    final byte[] ancUri = nsUri(prefix);
    if(ancUri == null || !eq(ancUri, uri)) {
      attribute(prefix.length == 0 ? XMLNS : concat(XMLNSC, prefix), uri, standalone);
      nspaces.add(prefix, uri);
    }
  }

  /**
   * Returns the namespace URI currently bound by the given prefix.
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
   * Checks if an element should be ignored.
   * @param node node to be serialized
   * @return result of check
   */
  @SuppressWarnings("unused")
  protected boolean ignore(final ANode node) {
    return false;
  }

  /**
   * Serializes an attribute.
   * @param name name
   * @param value value
   * @param standalone standalone flag
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void attribute(final byte[] name, final byte[] value, final boolean standalone)
      throws IOException { }

  /**
   * Starts an element.
   * @param name element name
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void startOpen(final QNm name) throws IOException { }

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

  /**
   * Serializes a node of the specified data reference.
   * @param node database node
   * @throws IOException I/O exception
   */
  protected final void node(final DBNode node) throws IOException {
    final FTPosData ft = node instanceof FTPosNode ? ((FTPosNode) node).ftpos : null;
    final Data data = node.data();
    int pre = node.pre(), kind = data.kind(pre);

    // document node: output all children
    final int size = pre + data.size(pre, kind);
    if(kind == Data.DOC) {
      openDoc(data.text(pre++, true));
      while(pre < size && !finished()) {
        node((ANode) new DBNode(data, pre));
        pre += data.size(pre, data.kind(pre));
      }
      closeDoc();
      return;
    }

    final boolean nsExist = !data.nspaces.isEmpty();
    final TokenSet nsSet = nsExist ? new TokenSet() : null;
    final IntList parentStack = new IntList();
    final BoolList indentStack = new BoolList();

    // loop through all table entries
    while(pre < size && !finished()) {
      kind = data.kind(pre);
      final int par = data.parent(pre, kind);

      // close opened elements...
      while(!parentStack.isEmpty() && parentStack.peek() >= par) {
        closeElement();
        indent = indentStack.pop();
        parentStack.pop();
      }

      if(kind == Data.TEXT) {
        prepareText(data.text(pre, true), ft != null ? ft.get(data, pre) : null);
        pre++;
      } else if(kind == Data.COMM) {
        prepareComment(data.text(pre++, true));
      } else if(kind == Data.PI) {
        preparePi(data.name(pre, Data.PI), data.atom(pre++));
      } else {
        // element node:
        final byte[] name = data.name(pre, kind);
        byte[] nsPrefix = EMPTY, nsUri = null;
        if(nsExist) {
          nsPrefix = prefix(name);
          nsUri = data.nspaces.uri(data.uriId(pre, kind));
        }
        // open element, serialize namespace declaration if it's new
        openElement(new QNm(name, nsUri));
        if(nsUri == null) nsUri = EMPTY;
        namespace(nsPrefix, nsUri, false);

        // database contains namespaces: add declarations
        if(nsExist) {
          nsSet.add(nsUri);
          int p = pre;
          do {
            final Atts ns = data.namespaces(p);
            final int nl = ns.size();
            for(int n = 0; n < nl; n++) {
              nsPrefix = ns.name(n);
              if(nsSet.add(nsPrefix)) namespace(nsPrefix, ns.value(n), false);
            }
            // check ancestors only on top level
            if(level != 0) break;

            p = data.parent(p, data.kind(p));
          } while(p >= 0 && data.kind(p) == Data.ELEM);

          // reset namespace cache
          nsSet.clear();
        }

        // serialize attributes
        indentStack.push(indent);
        final int as = pre + data.attSize(pre, kind);
        while(++pre != as) {
          final byte[] n = data.name(pre, Data.ATTR);
          final byte[] v = data.text(pre, false);
          attribute(n, v, false);
          if(eq(n, XML_SPACE) && indent) indent = !eq(v, PRESERVE);
        }
        parentStack.push(par);
      }
    }

    // process remaining elements...
    while(!parentStack.isEmpty()) {
      closeElement();
      indent = indentStack.pop();
      parentStack.pop();
    }
  }

  /**
   * Serializes a node fragment.
   * @param node database node
   * @throws IOException I/O exception
   */
  protected final void node(final FNode node) throws IOException {
    final Type type = node.type;
    if(type == NodeType.COM) {
      prepareComment(node.string());
    } else if(type == NodeType.TXT) {
      prepareText(node.string(), null);
    } else if(type == NodeType.PI) {
      preparePi(node.name(), node.string());
    } else if(type == NodeType.ATT) {
      attribute(node.name(), node.string(), true);
    } else if(type == NodeType.NSP) {
      namespace(node.name(), node.string(), true);
    } else if(type == NodeType.DOC) {
      openDoc(node.baseURI());
      for(final ANode n : node.childIter()) node(n);
      closeDoc();
    } else {
      // serialize elements (code will never be called for attributes)
      final QNm name = node.qname();
      openElement(name);

      // serialize declared namespaces
      final Atts nsp = node.namespaces();
      for(int p = nsp.size() - 1; p >= 0; p--) namespace(nsp.name(p), nsp.value(p), false);
      // add new or updated namespace
      namespace(name.prefix(), name.uri(), false);

      // serialize attributes
      final boolean i = indent;
      BasicNodeIter iter = node.attributeIter();
      for(ANode nd; (nd = iter.next()) != null;) {
        final byte[] n = nd.name();
        final byte[] v = nd.string();
        attribute(n, v, false);
        if(eq(n, XML_SPACE) && indent) indent = !eq(v, PRESERVE);
      }

      // serialize children
      iter = node.childIter();
      for(ANode n; (n = iter.next()) != null;) node(n);
      closeElement();
      indent = i;
    }
  }

  // PRIVATE METHODS ==============================================================================

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
