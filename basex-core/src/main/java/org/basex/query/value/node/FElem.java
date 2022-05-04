package org.basex.query.value.node;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.w3c.dom.*;

/**
 * Element node fragment.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FElem extends FNode {
  /** Element name. */
  private final QNm name;

  /** Namespaces (can be {@code null}). */
  private Atts namespaces;
  /** Attributes (can be {@code null}). */
  private ANodeList attributes;
  /** Child nodes (can be {@code null}). */
  private ANodeList children;

  /**
   * Convenience constructor for creating an element.
   * @param name element name
   */
  public FElem(final String name) {
    this(token(name));
  }

  /**
   * Convenience constructor for creating an element.
   * @param name element name
   */
  public FElem(final byte[] name) {
    this(new QNm(name));
  }

  /**
   * Convenience constructor for creating an element with a new namespace.
   * @param local local name
   * @param uri namespace uri
   */
  public FElem(final byte[] local, final byte[] uri) {
    this(EMPTY, local, uri);
  }

  /**
   * Convenience constructor for creating an element with a new namespace.
   * @param local local name
   * @param uri namespace uri
   */
  public FElem(final String local, final String uri) {
    this(EMPTY, token(local), token(uri));
  }

  /**
   * Convenience constructor for creating an element with a new namespace.
   * @param prefix prefix (a default namespace will be created if the string is empty)
   * @param local local name
   * @param uri namespace uri
   */
  public FElem(final String prefix, final String local, final String uri) {
    this(token(prefix), token(local), token(uri));
  }

  /**
   * Convenience constructor for creating an element with a new namespace.
   * @param prefix prefix (a default namespace will be created if the string is empty)
   * @param local local name
   * @param uri namespace uri
   */
  public FElem(final byte[] prefix, final byte[] local, final byte[] uri) {
    this(new QNm(prefix, local, uri));
  }

  /**
   * Constructor for creating an element.
   * @param name element name
   */
  public FElem(final QNm name) {
    this(name, null, null, null);
  }

  /**
   * Constructor for creating an element with nodes, attributes and
   * namespace declarations.
   * @param name element name
   * @param namespaces namespaces, can be {@code null}
   * @param attributes attributes, can be {@code null}
   * @param children children, can be {@code null}
   */
  public FElem(final QNm name, final Atts namespaces, final ANodeList attributes,
      final ANodeList children) {
    super(NodeType.ELEMENT);
    this.name = name;
    this.namespaces = namespaces;
    this.attributes = attributes;
    this.children = children;
  }

  /**
   * Constructor for creating an element from a DOM node.
   * Originally provided by Erdal Karaca.
   * @param elem DOM node
   * @param parent parent reference (can be {@code null})
   * @param nsMap namespaces in scope
   */
  public FElem(final Element elem, final FNode parent, final TokenMap nsMap) {
    super(NodeType.ELEMENT);

    this.parent = parent;
    final String nu = elem.getNamespaceURI();
    name = new QNm(elem.getNodeName(), nu == null ? EMPTY : token(nu));
    namespaces = new Atts();

    // attributes and namespaces
    final NamedNodeMap at = elem.getAttributes();
    final int as = at.getLength();

    for(int i = 0; i < as; ++i) {
      final Attr att = (Attr) at.item(i);
      final byte[] nm = token(att.getName()), uri = token(att.getValue());
      if(Token.eq(nm, XMLNS)) {
        namespaces.add(EMPTY, uri);
      } else if(startsWith(nm, XMLNS_COLON)) {
        namespaces.add(local(nm), uri);
      } else {
        add(new FAttr(att));
      }
    }

    // add all new namespaces
    final int nl = namespaces.size();
    for(int n = 0; n < nl; n++) nsMap.put(namespaces.name(n), namespaces.value(n));

    // no parent, so we have to add all namespaces in scope
    if(parent == null) {
      nsScope(elem.getParentNode(), nsMap);
      for(final byte[] pref : nsMap) {
        if(!namespaces.contains(pref)) namespaces.add(pref, nsMap.get(pref));
      }
    }

    final byte[] pref = name.prefix(), uri = name.uri(), old = nsMap.get(pref);
    if(old == null || !Token.eq(uri, old)) {
      namespaces.add(pref, uri);
      nsMap.put(pref, uri);
    }

    // children
    final NodeList ch = elem.getChildNodes();
    for(int i = 0; i < ch.getLength(); ++i) {
      final Node child = ch.item(i);

      switch(child.getNodeType()) {
        case Node.TEXT_NODE:
          add(new FTxt((Text) child));
          break;
        case Node.COMMENT_NODE:
          add(new FComm((Comment) child));
          break;
        case Node.PROCESSING_INSTRUCTION_NODE:
          add(new FPI((ProcessingInstruction) child));
          break;
        case Node.ELEMENT_NODE:
          add(new FElem((Element) child, this, nsMap));
          break;
        default:
          break;
      }
    }
    optimize();
  }

  /**
   * Gathers all defined namespaces in the scope of the given DOM element.
   * @param elem DOM element
   * @param nsMap map
   */
  private static void nsScope(final Node elem, final TokenMap nsMap) {
    Node n = elem;
    // only elements can declare namespaces
    while(n instanceof Element) {
      final NamedNodeMap atts = n.getAttributes();
      final byte[] pref = token(n.getPrefix());
      if(nsMap.get(pref) != null) nsMap.put(pref, token(n.getNamespaceURI()));
      final int len = atts.getLength();
      for(int i = 0; i < len; ++i) {
        final Attr a = (Attr) atts.item(i);
        final byte[] name = token(a.getName()), val = token(a.getValue());
        if(Token.eq(name, XMLNS)) {
          // default namespace
          if(nsMap.get(EMPTY) == null) nsMap.put(EMPTY, val);
        } else if(startsWith(name, XMLNS)) {
          // prefixed namespace
          final byte[] ln = local(name);
          if(nsMap.get(ln) == null) nsMap.put(ln, val);
        }
      }
      n = n.getParentNode();
    }
  }

  @Override
  public FElem optimize() {
    // update parent references and invalidate empty arrays
    if(namespaces != null && namespaces.isEmpty()) {
      namespaces = null;
    }
    if(attributes != null) {
      for(final ANode node : attributes) node.parent(this);
      if(attributes.isEmpty()) attributes = null;
    }
    if(children != null) {
      for(final ANode node : children) node.parent(this);
      if(children.isEmpty()) children = null;
    }
    return this;
  }

  /**
   * Adds a namespace declaration for the QName of this element.
   * @return self reference
   */
  public FElem declareNS() {
    namespaces().add(name.prefix(), name.uri());
    return this;
  }

  /**
   * Adds a node and updates its parent reference.
   * @param node node to be added
   * @return self reference
   */
  public FElem add(final ANode node) {
    if(node.type == NodeType.ATTRIBUTE) {
      if(attributes == null) attributes = new ANodeList();
      attributes.add(node);
    } else {
      if(children == null) children = new ANodeList();
      children.add(node);
    }
    node.parent(this);
    return this;
  }

  /**
   * Adds an attribute and updates its parent reference.
   * @param nm attribute name
   * @param val attribute value
   * @return self reference
   */
  public FElem add(final String nm, final String val) {
    return add(token(nm), token(val));
  }

  /**
   * Adds an attribute and updates its parent reference.
   * @param nm attribute name
   * @param val attribute value
   * @return self reference
   */
  public FElem add(final byte[] nm, final String val) {
    return add(nm, token(val));
  }

  /**
   * Adds an attribute and updates its parent reference.
   * @param nm attribute name
   * @param val attribute value
   * @return self reference
   */
  public FElem add(final String nm, final byte[] val) {
    return add(token(nm), val);
  }

  /**
   * Adds an attribute and updates its parent reference.
   * @param nm attribute name
   * @param val attribute value
   * @return self reference
   */
  public FElem add(final byte[] nm, final byte[] val) {
    return add(new FAttr(nm, val));
  }

  /**
   * Adds an attribute and updates its parent reference.
   * @param nm attribute name
   * @param val attribute value
   * @return self reference
   */
  public FElem add(final QNm nm, final String val) {
    return add(nm, token(val));
  }

  /**
   * Adds an attribute and updates its parent reference.
   * @param nm attribute name
   * @param val attribute value
   * @return self reference
   */
  public FElem add(final QNm nm, final byte[] val) {
    return add(new FAttr(nm, val));
  }

  /**
   * Creates and adds a text node if the specified value is not empty.
   * Converts the specified string to a token and calls {@link #add(byte[])}.
   * @param text value of text node
   * @return self reference
   */
  public FElem add(final String text) {
    return add(token(text));
  }

  /**
   * Creates and adds a text node if the specified value is not empty.
   * @param text value of text node
   * @return self reference
   */
  public FElem add(final byte[] text) {
    if(text.length != 0) {
      final FTxt txt = new FTxt(text);
      if(children == null) children = new ANodeList();
      children.add(txt);
      txt.parent(this);
    }
    return this;
  }

  @Override
  public Atts namespaces() {
    if(namespaces == null) namespaces = new Atts();
    return namespaces;
  }

  @Override
  public byte[] string() {
    return children == null ? EMPTY : string(children);
  }

  @Override
  public byte[] baseURI() {
    final byte[] base = attribute(QNm.XML_BASE);
    return base != null ? base : EMPTY;
  }

  @Override
  public QNm qname() {
    return name;
  }

  @Override
  public byte[] name() {
    return name.string();
  }

  @Override
  public BasicNodeIter attributeIter() {
    return attributes != null ? attributes.iter() : BasicNodeIter.EMPTY;
  }

  @Override
  public BasicNodeIter childIter() {
    return children != null ? children.iter() : BasicNodeIter.EMPTY;
  }

  @Override
  public boolean hasChildren() {
    return children != null && !children.isEmpty();
  }

  @Override
  public FElem materialize(final QueryContext qc, final Predicate<ANode> test, final InputInfo ii)
      throws QueryException {

    if(test.test(this)) return this;

    // nodes must be added after root constructor in order to ensure ascending node ids
    final Atts nsp = namespaces != null ? new Atts(namespaces) : null;
    final ANodeList at = attributes != null ? new ANodeList(attributes.size()) : null;
    final ANodeList ch = children != null ? new ANodeList(children.size()) : null;
    final FElem node = new FElem(name, nsp, at, ch);
    if(at != null) {
      for(final ANode attr : attributes) at.add(attr.materialize(qc, test, ii));
    }
    if(ch != null) {
      for(final ANode child : children) ch.add(child.materialize(qc, test, ii));
    }
    return node.optimize();
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof FElem)) return false;
    final FElem f = (FElem) obj;
    return name.eq(f.name) && Objects.equals(children, f.children) &&
        Objects.equals(attributes, f.attributes) && Objects.equals(namespaces, f.namespaces) &&
        super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, NAME, name.string()));
  }

  @Override
  public void toString(final QueryString qs) {
    final byte[] nm = name.string();
    final TokenBuilder tb = new TokenBuilder().add('<').add(nm);
    if(namespaces != null) {
      final int nl = namespaces.size();
      for(int n = 0; n < nl; n++) {
        tb.add(' ').add(new FNSpace(namespaces.name(n), namespaces.value(n)));
      }
    }
    if(attributes != null) {
      for(final ANode att : attributes) tb.add(' ').add(att);
    }
    if(hasChildren()) {
      tb.add('>');
      final ANode child = children.get(0);
      if(child.type == NodeType.TEXT && children.size() == 1) {
        tb.add(QueryString.toValue(child.value));
      } else {
        tb.add(DOTS);
      }
      tb.add("</").add(nm).add('>');
    } else {
      tb.add("/>");
    }
    qs.token(tb.finish());
  }
}
