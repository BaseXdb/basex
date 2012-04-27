package org.basex.query.item;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.iter.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.w3c.dom.*;

/**
 * Element node fragment.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FElem extends FNode {
  /** Element name. */
  private final QNm name;

  /** Child nodes. */
  private NodeCache children;
  /** Attributes. */
  private NodeCache atts;
  /** Namespaces. */
  private Atts ns;

  /**
   * Convenience constructor.
   * @param n element name
   */
  public FElem(final byte[] n) {
    this(new QNm(n));
  }

  /**
   * Constructor.
   * @param n element name
   */
  public FElem(final QNm n) {
    this(n, null);
  }

  /**
   * Convenience constructor.
   * @param n element name
   * @param nsp namespaces
   */
  public FElem(final byte[] n, final Atts nsp) {
    this(new QNm(n), nsp);
  }

  /**
   * Constructor.
   * @param n element name
   * @param nsp namespaces
   */
  public FElem(final QNm n, final Atts nsp) {
    this(n, null, null, nsp);
  }

  /**
   * Constructor.
   * @param nm element name
   * @param ch children; can be {@code null}
   * @param at attributes; can be {@code null}
   * @param nsp namespaces; can be {@code null}
   */
  public FElem(final QNm nm, final NodeCache ch, final NodeCache at, final Atts nsp) {
    super(NodeType.ELM);
    name = nm;
    children = ch;
    atts = at;
    ns = nsp;

    // update parent references
    if(ch != null) {
      final long nl = (int) ch.size();
      for(int n = 0; n < nl; ++n) ch.get(n).parent(this);
    }
    if(at != null) {
      final long al = (int) at.size();
      for(int a = 0; a < al; ++a) at.get(a).parent(this);
    }
  }

  /**
   * Constructor for DOM nodes.
   * Originally provided by Erdal Karaca.
   * @param elem DOM node
   * @param p parent reference
   * @param nss namespaces in scope
   */
  public FElem(final Element elem, final ANode p, final TokenMap nss) {
    super(NodeType.ELM);

    // general stuff
    final String nu = elem.getNamespaceURI();
    name = new QNm(elem.getNodeName(), nu == null ? EMPTY : token(nu));
    par = p;
    ns = new Atts();

    // attributes and namespaces
    final NamedNodeMap at = elem.getAttributes();
    final int as = at.getLength();

    for(int i = 0; i < as; ++i) {
      final Attr att = (Attr) at.item(i);
      final byte[] nm = token(att.getName()), uri = token(att.getValue());
      if(Token.eq(nm, XMLNS)) {
        ns.add(EMPTY, uri);
      } else if(startsWith(nm, XMLNSC)) {
        ns.add(local(nm), uri);
      } else {
        add(new FAttr(att));
      }
    }

    // add all new namespaces
    for(int i = 0; i < ns.size(); ++i) nss.add(ns.name(i), ns.string(i));

    // no parent, so we have to add all namespaces in scope
    if(p == null) {
      nsScope(elem.getParentNode(), nss);
      for(final byte[] pref : nss.keys()) {
        if(!ns.contains(pref)) ns.add(pref, nss.get(pref));
      }
    }

    final byte[] pref = name.prefix();
    final byte[] uri = name.uri();
    final byte[] old = nss.get(pref);
    if(old == null || !Token.eq(uri, old)) {
      ns.add(pref, uri);
      nss.add(pref, uri);
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
          add(new FElem((Element) child, this, nss));
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
   * @param nss map
   */
  private static void nsScope(final Node elem, final TokenMap nss) {
    Node n = elem;
    // only elements can declare namespaces
    while(n != null && n instanceof Element) {
      final NamedNodeMap atts = n.getAttributes();
      final byte[] pref = token(n.getPrefix());
      if(nss.get(pref) != null) nss.add(pref, token(n.getNamespaceURI()));
      for(int i = 0, len = atts.getLength(); i < len; ++i) {
        final Attr a = (Attr) atts.item(i);
        final byte[] name = token(a.getName()), val = token(a.getValue());
        if(Token.eq(name, XMLNS)) {
          // default namespace
          if(nss.get(EMPTY) == null) nss.add(EMPTY, val);
        } else if(startsWith(name, XMLNS)) {
          // prefixed namespace
          final byte[] ln = local(name);
          if(nss.get(ln) == null) nss.add(ln, val);
        }
      }
      n = n.getParentNode();
    }
  }

  @Override
  public FElem optimize() {
    if(children != null && children.size() == 0) children = null;
    if(atts != null && atts.size() == 0) atts = null;
    if(ns != null && ns.size() == 0) ns = null;
    return this;
  }

  /**
   * Adds a node and updates its parent reference.
   * @param node node to be added
   * @return self reference
   */
  public FElem add(final ANode node) {
    final NodeCache nc;
    if(node.type == NodeType.ATT) {
      if(atts == null) atts = new NodeCache();
      nc = atts;
    } else {
      if(children == null) children = new NodeCache();
      nc = children;
    }
    nc.add(node);
    node.parent(this);
    return this;
  }

  @Override
  public Atts namespaces() {
    if(ns == null) ns = new Atts();
    return ns;
  }

  @Override
  public byte[] string() {
    return children == null ? EMPTY : string(children);
  }

  @Override
  public byte[] baseURI() {
    final byte[] b = attribute(new QNm(BASE, XMLURI));
    return b != null ? b : EMPTY;
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
  public AxisMoreIter attributes() {
    return atts != null ? iter(atts) : super.attributes();
  }

  @Override
  public AxisMoreIter children() {
    return children != null ? iter(children) : super.children();
  }

  @Override
  public boolean hasChildren() {
    return children != null && children.size() != 0;
  }

  @Override
  public FNode copy() {
    final FElem node = new FElem(name);
    if(ns != null) {
      node.ns = new Atts();
      for(int n = 0; n < ns.size(); ++n) node.ns.add(ns.name(n), ns.string(n));
    }
    if(atts != null) {
      for(int a = 0; a < atts.size(); ++a) node.add(atts.get(a).copy());
    }
    if(children != null) {
      for(int c = 0; c < children.size(); ++c) node.add(children.get(c).copy());
    }
    return node.parent(par);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, name.string()));
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder().add('<').add(name.string());
    if(ns != null) {
      for(int n = 0; n < ns.size(); n++) {
        tb.add(new FNames(ns.name(n), ns.string(n)).toString());
      }
    }
    if(atts != null) {
      for(int a = 0; a < atts.size(); a++) tb.add(atts.get(a).toString());
    }
    if(hasChildren()) tb.add(">...</").add(name.string());
    else tb.add("/");
    return tb.add(">").toString();
  }
}
