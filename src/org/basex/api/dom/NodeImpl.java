package org.basex.api.dom;

import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.query.xpath.values.NodeBuilder;
import org.basex.util.Token;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

/**
 * DOM - Node Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class NodeImpl implements Node {
  /** Node type mapping (see {@link Data} interface). */
  private static final short[] TYPES = {
    Node.DOCUMENT_NODE, Node.ELEMENT_NODE,
    Node.TEXT_NODE, Node.ATTRIBUTE_NODE,
    Node.COMMENT_NODE, Node.PROCESSING_INSTRUCTION_NODE
  };
  /** Node name mapping (see {@link Data} interface). */
  private static final String[] NAMES = {
    "#document", null, "#text", null, "#comment", null
  };
  
  /** Data reference. */
  protected final Data data;
  /** Node kind. */
  protected final int kind;
  /** Node kind. */
  protected final int pre;
  
  /**
   * Constructor.
   * @param d data reference
   * @param p pre value
   */
  public NodeImpl(final Data d, final int p) {
    this(d, p, d.kind(p));
  }
  
  /**
   * Constructor.
   * @param d data reference
   * @param p pre value
   * @param k node kind
   */
  public NodeImpl(final Data d, final int p, final int k) {
    data = d;
    pre = p;
    kind = k;
  }

  public String getNodeName() {
    return NAMES[kind];
  }

  public short getNodeType() {
    return TYPES[kind];
  }

  public String getNodeValue() {
    return null;
  }

  public String getLocalName() {
    return null;
  }

  public Node cloneNode(final boolean deep) {
    return get(data, pre);
  }

  public short compareDocumentPosition(final Node other) {
    int p = ((NodeImpl) other).pre;
    return (short) (pre < p ? -1 : pre > p ? 1 : 0);
  }

  public NamedNodeMap getAttributes() {
    final NodeBuilder nb = new NodeBuilder();
    final int s = pre + data.attSize(pre, kind);
    int p = pre;
    while(++p != s) nb.add(p);
    return new NamedNodeImpl(data, nb.finish(), nb.size);
  }

  public String getBaseURI() {
    return url(data.meta.file.path());
  }

  public NodeList getChildNodes() {
    final NodeBuilder nb = new NodeBuilder();
    final int s = pre + data.size(pre, kind);
    int p = pre + data.attSize(pre, kind);
    while(p != s) {
      nb.add(p);
      p += data.size(p, kind);
    }
    return new NodeListImpl(data, nb.finish(), nb.size);
  }

  public Node getFirstChild() {
    final int s = pre + data.attSize(pre, kind);
    return s < data.size && data.parent(s, data.kind(s)) == pre ?
        get(data, s) : null;
  }

  public Node getLastChild() {
    int s = pre + data.size(pre, kind);
    while(--s != pre) {
      int k = data.kind(s);
      if(k == Data.ATTR) continue;
      if(data.parent(s, data.kind(s)) == pre) return get(data, s);
    }
    return null;
  }

  public String getNamespaceURI() {
    return null;
  }

  public Node getNextSibling() {
    int s = pre + data.size(pre, kind);
    int p = data.parent(pre, kind);
    return s < data.size && data.parent(s, data.kind(s)) == p ?
        get(data, s) : null;
  }

  public Node getPreviousSibling() {
    int par = data.parent(pre, kind);
    int p = pre;
    while(--p != par) {
      if(data.parent(p, data.kind(p)) == par) return get(data, p);
    }
    return null;
  }

  public Node getParentNode() {
    return get(data, data.parent(pre, kind));
  }

  public boolean hasChildNodes() {
    final int s = pre + data.size(pre, kind);
    int p = pre + data.attSize(pre, kind);
    return p != s;
  }

  public boolean isSameNode(final Node other) {
    return this == other;
  }

  public Document getOwnerDocument() {
    return (Document) get(data, 0);
  }

  public boolean hasAttributes() {
    return data.attSize(pre, kind) > 1;
  }

  public Object getFeature(final String feature, final String version) {
    return null;
  }

  public String getPrefix() {
    return null;
  }

  public String getTextContent() {
    return Token.string(data.atom(pre));
  }

  public Node appendChild(final Node newChild) {
    BaseX.noupdates();
    return null;
  }

  public Object getUserData(final String key) {
    return null;
  }

  public boolean isSupported(final String feature, final String version) {
    return false;
  }

  public Node insertBefore(final Node newChild, final Node refChild) {
    BaseX.noupdates();
    return null;
  }

  public boolean isDefaultNamespace(final String namespaceURI) {
    BaseX.notimplemented();
    return false;
  }

  public boolean isEqualNode(final Node arg) {
    BaseX.notimplemented();
    return false;
  }

  public String lookupNamespaceURI(final String prefix) {
    BaseX.notimplemented();
    return null;
  }

  public String lookupPrefix(final String namespaceURI) {
    BaseX.notimplemented();
    return null;
  }

  public void normalize() {
    BaseX.noupdates();
  }

  public Node removeChild(final Node oldChild) {
    BaseX.noupdates();
    return null;
  }

  public Node replaceChild(final Node newChild, final Node oldChild) {
    BaseX.noupdates();
    return null;
  }

  public void setNodeValue(final String nodeValue) {
    BaseX.noupdates();
  }

  public void setPrefix(final String prefix) {
    BaseX.noupdates();
  }

  public void setTextContent(final String textContent) {
    BaseX.noupdates();
  }

  public Object setUserData(final String key, final Object dat,
      final UserDataHandler handler) {
    BaseX.noupdates();
    return null;
  }
  
  @Override
  public String toString() {
    return "[" + getNodeName() + ": " + getNodeValue() + "]";
  }
  
  /**
   * Returns a new node reference.
   * @param data data reference
   * @param pre pre value
   * @return node
   */
  public static Node get(final Data data, final int pre) {
    if(pre == -1) return null;
    
    final int kind = data.kind(pre);
    switch(kind) {
      case Data.DOC:  return new DocImpl(data);
      case Data.ELEM: return new ElementImpl(data, pre);
      case Data.TEXT: return new TextImpl(data, pre);
      case Data.ATTR: return new AttrImpl(data, pre);
      case Data.COMM: return new CommentImpl(data, pre);
      case Data.PI  : return new PIImpl(data, pre);
    }
    return null;
  }
  
  /**
   * Returns all nodes with the given tag name.
   * @param tag tag name
   * @return nodes
   */
  protected NodeList getElements(final String tag) {
    final int t = tag.equals("*") ? -1 : data.tags.id(Token.token(tag));
    final NodeBuilder nb = new NodeBuilder();
    final int s = pre + data.size(pre, kind);
    int p = pre + data.attSize(pre, kind);
    while(p != s) {
      if(data.kind(p) == Data.ELEM) {
        if(t == -1 || data.tagID(p) == t) nb.add(p);
        p += data.attSize(p, kind);
      }
    }
    return new NodeListImpl(data, nb.finish(), nb.size);
  }

  /**
   * Creates a URL from the specified path.
   * @param path path to be converted
   * @return URL
   */
  public static String url(final String path) {
    String pre = "file://";
    if(!path.startsWith("/")) {
      pre += "/";
      if(path.length() < 2 || path.charAt(1) != ':') {
        pre += "/" + Prop.WORK.replace('\\', '/');
        if(!pre.endsWith("/")) pre += "/";
      }
    }
    return pre + path.replace('\\', '/');
  }
}
