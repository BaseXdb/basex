package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

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
import org.basex.util.options.Options.*;

/**
 * This is an interface for serializing XQuery values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class Serializer implements Closeable {
  /** New line token to be emitted during canonicalization. */
  private static final byte[] NL = { '\n' };
  /** Stack with names of opened elements. */
  protected final Stack<QNm> opened = new Stack<>();
  /** Current level. */
  protected int level;
  /** Current element name. */
  protected QNm elem;
  /** Most recent tag, in case it was a closing tag. */
  protected QNm closed = QNm.EMPTY;
  /** Indentation flag. */
  protected boolean indent;
  /** Canocical serialization flag. */
  protected boolean canonical;

  /** Stack with currently available namespaces. */
  private final Atts nspaces = new Atts(2).add(XML, QueryText.XML_URI).add(EMPTY, EMPTY);
  /** Stack with namespace size pointers. */
  private final IntList nstack = new IntList();

  /** Static context. */
  protected StaticContext sc;
  /** Indicates if at least one item was already serialized. */
  protected boolean more;
  /** Flag for skipping elements. */
  protected int skip;
  /** Indicates if an element is currently being opened. */
  protected boolean opening;

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
   * @param sopts serialization parameters (can be {@code null})
   * @return serializer
   * @throws IOException I/O exception
   */
  public static Serializer get(final OutputStream os, final SerializerOptions sopts)
      throws IOException {

    // choose serializer
    final SerializerOptions so = sopts == null ? SerializerMode.DEFAULT.get() : sopts;
    return switch(so.get(SerializerOptions.METHOD)) {
      case XHTML    -> new XHTMLSerializer(os, restrictIfCanonical(so));
      case HTML     -> new HTMLSerializer(os, ignoreCanonical(so));
      case TEXT     -> new TextSerializer(os, ignoreCanonical(so));
      case CSV      -> CsvSerializer.get(os, ignoreCanonical(so));
      case JSON     -> JsonSerializer.get(os, restrictIfCanonical(so));
      case XML      -> new XMLSerializer(os, restrictIfCanonical(so));
      case ADAPTIVE -> new AdaptiveSerializer(os, ignoreCanonical(so));
      default       -> new BaseXSerializer(os, so);
    };
  }

  // PUBLIC METHODS ===============================================================================

  /**
   * Serializes the specified item, which may be a node or an atomic item.
   * @param item item to be serialized
   * @throws IOException I/O exception
   */
  public void serialize(final Item item) throws IOException {
    if(item instanceof final ANode node) {
      node(node);
    } else if(item instanceof final FItem fitem) {
      function(fitem);
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
   * Resets the serializer (indentation, etc.).
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
    if(node instanceof final DBNode dbnode) {
      node(dbnode);
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
    closed = QNm.EMPTY;
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
      closed = elem;
    } else {
      elem = opened.peek();
      level--;
      finishClose();
      closed = opened.pop();
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
      if(canonical && uri.length > 0 && !Uri.get(uri).isAbsolute()) {
        throw SERRELURI.getIO(uri);
      }
      attribute(prefix.length == 0 ? XMLNS : concat(XMLNS_COLON, prefix), uri, standalone);
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
   * Checks if an element should be skipped.
   * @param node node to be serialized
   * @return result of check
   */
  @SuppressWarnings("unused")
  protected boolean skipElement(final ANode node) {
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
   * @param ftp full-text positions, used for visualization highlighting (can be {@code null})
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
   * Serializes an atomic item.
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

  // PRIVATE CLASSES AND METHODS ===============================================================

  /** Collector for canonical attribute serialization. */
  private final class CanonicalAtts {
    /**
     * Attribute item (URI, name, value) with canonical sort order.
     * @param uri namespace uri (can be {@code null})
     * @param name attribute name including prefix, if any
     * @param value attribute value
     */
    private record Att(byte[] uri, byte[] name, byte[] value) {
      /** Constructor. */
      Att {
        if(uri == null) uri = EMPTY;
      }
    }
    /** Collected attributes. */
    private final ArrayList<Att> list = new ArrayList<>();

    /**
     * Add an attribute.
     * @param uri namespace uri (can be {@code null})
     * @param name attribute name including prefix, if any
     * @param value attribute value
     */
    void add(final byte[] uri, final byte[] name, final byte[] value) {
      list.add(new Att(uri, name, value));
    }

    /**
     * Emit attributes in canonical sorting order.
     * @throws IOException I/O exception.
     */
    void emit() throws IOException {
      list.sort((a, b) -> {
        final int d = compare(a.uri, b.uri);
        return d != 0 ? d : compare(a.name, indexOf(a.name, ':') + 1, a.name.length,
                                    b.name, indexOf(b.name, ':') + 1, b.name.length);
      });
      for(final Att a : list) attribute(a.name(), a.value(), false);
    }
  }

  /** Collector for canonical namespace serialization. */
  private final class CanonicalNS {
    /**
     * Namespace item.
     * @param prefix namespace prefix
     * @param uri namespace URI
     */
    private record NS(byte[] prefix, byte[] uri) { }
    /** Collected namespaces. */
    private final ArrayList<NS> list = new ArrayList<>();

    /**
     * Add a namespace.
     * @param prefix namespace prefix
     * @param uri namespace URI
     */
    void add(final byte[] prefix, final byte[] uri) {
      list.add(new NS(prefix, uri));
    }

    /**
     * Emit Namespaces in canonical sorting order.
     * @throws IOException I/O exception.
     */
    void emit() throws IOException {
      // canonical: order namespace nodes by prefix
      list.sort((a, b) -> compare(a.prefix, b.prefix));
      for(final NS ns : list) namespace(ns.prefix, ns.uri, false);
    }
  }

  /**
   * Serializes a node of the specified data reference.
   * @param node database node
   * @throws IOException I/O exception
   */
  private void node(final DBNode node) throws IOException {
    final Data data = node.data();
    int pre = node.pre(), kind = data.kind(pre);

    // document node: output all children
    final int size = pre + data.size(pre, kind);
    if(kind == Data.DOC) {
      openDoc(data.text(pre++, true));
      byte[] nl = null;
      while(pre < size && !finished()) {
        if(nl != null) {
          text(nl, null);
        } else if(canonical) {
          nl = NL;
        }
        node((ANode) new DBNode(data, pre));
        pre += data.size(pre, data.kind(pre));
      }
      closeDoc();
      return;
    }

    final FTPosData ftData = node instanceof final FTPosNode ft ? ft.ftpos : null;
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
        prepareText(data.text(pre, true), ftData != null ? ftData.get(data, pre) : null);
        pre++;
      } else if(kind == Data.COMM) {
        prepareComment(data.text(pre++, true));
      } else if(kind == Data.PI) {
        preparePi(data.name(pre, Data.PI), data.atom(pre++));
      } else if(skip > 0 && skipElement(new DBNode(data, pre, kind))) {
        // ignore specific elements
        pre += data.size(pre, kind);
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
          final CanonicalNS decls = canonical ? new CanonicalNS() : null;
          int p = pre;
          do {
            final Atts ns = data.namespaces(p);
            final int nl = ns.size();
            for(int n = 0; n < nl; n++) {
              nsPrefix = ns.name(n);
              if(nsSet.add(nsPrefix)) {
                final byte[] uri = ns.value(n);
                if(decls == null) {
                  namespace(nsPrefix, uri, false);
                }
                else {
                  decls.add(nsPrefix, uri);
                }
              }
            }
            // check ancestors only on top level
            if(level != 0) break;

            p = data.parent(p, data.kind(p));
          } while(p >= 0 && data.kind(p) == Data.ELEM);
          if(decls != null) decls.emit();
          // reset namespace cache
          nsSet.clear();
        }

        // serialize attributes
        indentStack.push(indent);
        final int as = pre + data.attSize(pre, kind);
        final CanonicalAtts atts = canonical ? new CanonicalAtts() : null;
        while(++pre != as) {
          final byte[] n = data.name(pre, Data.ATTR), v = data.text(pre, false);
          if(atts == null) {
            attribute(n, v, false);
            if(eq(n, XML_SPACE) && indent) indent = !eq(v, PRESERVE);
          } else {
            atts.add(data.nspaces.uri(data.uriId(pre, Data.ATTR)), n, v);
          }
        }
        if(atts != null) atts.emit();
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
  private void node(final FNode node) throws IOException {
    final Type type = node.type;
    if(type == NodeType.COMMENT) {
      prepareComment(node.string());
    } else if(type == NodeType.TEXT) {
      prepareText(node.string(), null);
    } else if(type == NodeType.PROCESSING_INSTRUCTION) {
      preparePi(node.name(), node.string());
    } else if(type == NodeType.ATTRIBUTE) {
      attribute(node.name(), node.string(), true);
    } else if(type == NodeType.NAMESPACE_NODE) {
      namespace(node.name(), node.string(), true);
    } else if(type == NodeType.DOCUMENT_NODE) {
      openDoc(node.baseURI());
      if(!canonical) {
        for(final ANode nd : node.childIter()) node(nd);
      } else {
        int roots = 0;
        byte[] nl = null;
        for(final ANode nd : node.childIter()) {
          if(nd.type != NodeType.TEXT) {
            if(nl != null) {
              text(nl, null);
            } else if(canonical) {
              nl = NL;
            }
            node(nd);
            if(nd.type == NodeType.ELEMENT) ++roots;
          }
        }
        if(roots != 1) throw SERWELLFORM_X.getIO(node);
      }
      closeDoc();
    } else if(skip == 0 || !skipElement(node)) {
      // serialize elements (code will never be called for attributes)
      final QNm name = node.qname();
      openElement(name);

      // serialize declared namespaces
      final Atts nsp = node.namespaces();
      final int ps = nsp.size();
      final CanonicalNS decls = canonical ? new CanonicalNS() : null;
      for(int p = 0; p < ps; p++) {
        final byte[] pfx = nsp.name(p), uri = nsp.value(p);
        if(decls == null) {
          namespace(pfx, uri, false);
        } else {
          decls.add(pfx, uri);
        }
      }
      // add new or updated namespace
      final byte[] pfx = name.prefix(), uri = name.uri();
      if(decls == null) {
        namespace(pfx, uri, false);
      } else {
        decls.add(pfx, uri);
        decls.emit();
      }

      // serialize attributes
      final boolean i = indent;
      BasicNodeIter iter = node.attributeIter();
      final CanonicalAtts atts = canonical ? new CanonicalAtts() : null;
      for(ANode nd; (nd = iter.next()) != null;) {
        final byte[] n = nd.name(), v = nd.string();
        if(atts == null) {
          attribute(n, v, false);
          if(eq(n, XML_SPACE) && indent) indent = !eq(v, PRESERVE);
        } else {
          atts.add(nsUri(prefix(n)), n, v);
        }
      }
      if(atts != null) atts.emit();

      // serialize children
      iter = node.childIter();
      for(ANode n; (n = iter.next()) != null;) {
        node(n);
      }
      closeElement();
      indent = i;
    }
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
    opened.push(elem);
    level++;
  }

  // STATIC METHODS ===============================================================================

  /**
   * Serializes the specified value.
   * @param value value
   * @param chop chop large tokens
   * @param quote character for quoting the value; ignored if {@code 0}
   * @return value
   */
  public static byte[] value(final byte[] value, final char quote, final boolean chop) {
    final boolean quoting = quote != 0;
    final TokenBuilder tb = new TokenBuilder();
    if(quoting) tb.add(quote);

    int c = 0;
    for(final TokenParser tp = new TokenParser(value); tp.more(); c++) {
      if(chop && c == 200) {
        tb.add(QueryText.DOTS);
        break;
      }
      final int cp = tp.next();
      if(cp == '&') tb.add(E_AMP);
      else if(cp == '\r') tb.add(E_CR);
      else if(cp == '\n') tb.add(E_NL);
      else if(cp == quote) tb.add(quote).add(quote);
      else tb.add(cp);
    }

    if(quoting) tb.add(quote);
    return tb.finish();
  }

  /**
   * Returns the given serializer options, or a restricted copy if canonical serialization is
   * enabled.
   * @param so serializer options
   * @return original or restricted serializer options
   */
  private static SerializerOptions restrictIfCanonical(final SerializerOptions so) {
    if(!so.yes(SerializerOptions.CANONICAL)) return so;
    final SerializerOptions opts = new SerializerOptions();
    opts.set(SerializerOptions.CANONICAL, YesNo.YES);
    opts.set(SerializerOptions.NORMALIZATION_FORM, opts.get(SerializerOptions.NORMALIZATION_FORM));
    opts.set(SerializerOptions.MEDIA_TYPE, opts.get(SerializerOptions.MEDIA_TYPE));
    opts.set(SerializerOptions.HTML_VERSION, opts.get(SerializerOptions.HTML_VERSION));
    opts.set(SerializerOptions.INCLUDE_CONTENT_TYPE,
        opts.get(SerializerOptions.INCLUDE_CONTENT_TYPE));
    opts.set(SerializerOptions.JSON_LINES, opts.get(SerializerOptions.JSON_LINES));
    opts.set(SerializerOptions.JSON_NODE_OUTPUT_METHOD,
        opts.get(SerializerOptions.JSON_NODE_OUTPUT_METHOD));
    return opts;
  }

  /**
   * Returns the given serializer options, or a copy with canonical set to 'no' in case it was set
   * to 'yes'.
   * @param so serializer options
   * @return original or restricted serializer options
   */
  private static SerializerOptions ignoreCanonical(final SerializerOptions so) {
    if(!so.yes(SerializerOptions.CANONICAL)) return so;
    final SerializerOptions opts = new SerializerOptions(so);
    opts.set(SerializerOptions.CANONICAL, YesNo.NO);
    return opts;
  }
}
