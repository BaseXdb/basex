package org.basex.query.value.node;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.constr.*;
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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FElem extends FNode {
  /** Element name. */
  private final QNm name;

  /** Namespaces. */
  private Atts namespaces = Atts.EMPTY;
  /** Attributes. */
  private ANode[] attributes = EMPTY;
  /** Child nodes. */
  private ANode[] children = EMPTY;

  /**
   * Constructor.
   * @param name element name
   */
  public FElem(final QNm name) {
    super(NodeType.ELEMENT);
    this.name = name;
  }

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
   * Constructor for creating an element from a DOM node.
   * Originally provided by Erdal Karaca.
   * @param elem DOM node
   * @param parent parent reference (can be {@code null})
   * @param nsMap namespaces in scope
   */
  public FElem(final Element elem, final FNode parent, final TokenMap nsMap) {
    super(NodeType.ELEMENT);

    this.parent = parent;
    final String nsUri = elem.getNamespaceURI();
    name = new QNm(elem.getNodeName(), nsUri == null ? Token.EMPTY : token(nsUri));

    final FBuilder builder = new FBuilder();
    final Atts nspaces = new Atts();

    // attributes and namespaces
    final NamedNodeMap at = elem.getAttributes();
    final int al = at.getLength();

    for(int a = 0; a < al; ++a) {
      final Attr attr = (Attr) at.item(a);
      final byte[] nm = token(attr.getName()), uri = token(attr.getValue());
      if(Token.eq(nm, XMLNS)) {
        nspaces.add(Token.EMPTY, uri);
      } else if(startsWith(nm, XMLNS_COLON)) {
        nspaces.add(local(nm), uri);
      } else {
        builder.add(new FAttr(attr));
      }
    }

    // add new namespaces
    final int ns = nspaces.size();
    for(int n = 0; n < ns; n++) nsMap.put(nspaces.name(n), nspaces.value(n));

    // no parent, so we have to add all namespaces in scope
    if(parent == null) {
      nsScope(elem.getParentNode(), nsMap);
      for(final byte[] prefix : nsMap) {
        if(!nspaces.contains(prefix)) nspaces.add(prefix, nsMap.get(prefix));
      }
    }

    final byte[] prefix = name.prefix(), uri = name.uri(), old = nsMap.get(prefix);
    if(old == null || !Token.eq(uri, old)) {
      nspaces.add(prefix, uri);
      nsMap.put(prefix, uri);
    }

    if(!nspaces.isEmpty()) namespaces = nspaces;
    children(elem, builder, new TokenMap());
    finish(builder);
  }

  /**
   * Assigns nodes.
   * @param builder node builder
   * @return self reference
   */
  public FElem finish(final FBuilder builder) {
    namespaces = builder.namespaces == null ? Atts.EMPTY : builder.namespaces.finish();
    attributes = builder.attributes == null ? FNode.EMPTY : builder.attributes.finish();
    children = builder.children == null ? FNode.EMPTY : builder.children.finish();
    for(final ANode child : children) child.parent(this);
    for(final ANode attribute : attributes) attribute.parent(this);
    return this;
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
      final String prefix = n.getPrefix();
      if(prefix != null) {
        final byte[] pref = token(prefix);
        if(nsMap.get(pref) != null) nsMap.put(pref, token(n.getNamespaceURI()));
      }
      final int len = atts.getLength();
      for(int i = 0; i < len; ++i) {
        final Attr a = (Attr) atts.item(i);
        final byte[] name = token(a.getName()), val = token(a.getValue());
        if(Token.eq(name, XMLNS)) {
          // default namespace
          if(nsMap.get(Token.EMPTY) == null) nsMap.put(Token.EMPTY, val);
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
  public Atts namespaces() {
    return namespaces;
  }

  @Override
  public byte[] string() {
    return string(children);
  }

  @Override
  public byte[] baseURI() {
    final byte[] base = attribute(QNm.XML_BASE);
    return base != null ? base : Token.EMPTY;
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
    return ANodeList.iter(attributes);
  }

  @Override
  public BasicNodeIter childIter() {
    return ANodeList.iter(children);
  }

  @Override
  public boolean hasChildren() {
    return children.length != 0;
  }

  @Override
  public FNode materialize(final Predicate<Data> test, final InputInfo ii, final QueryContext qc)
      throws QueryException {

    if(materialized(test, ii)) return this;

    final FBuilder elem = new FBuilder(new FElem(name));
    final int ns = namespaces.size();
    for(int n = 0; n < ns; n++) elem.addNS(namespaces.name(n), namespaces.value(n));
    for(final ANode attribute : attributes) elem.add(attribute.materialize(test, ii, qc));
    for(final ANode child : children) elem.add(child.materialize(test, ii, qc));
    return elem.finish();
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
    final int ns = namespaces.size();
    for(int n = 0; n < ns; n++) {
      tb.add(' ').add(new FNSpace(namespaces.name(n), namespaces.value(n)));
    }
    for(final ANode attr : attributes) tb.add(' ').add(attr);
    if(hasChildren()) {
      tb.add('>');
      final ANode child = children[0];
      if(child.type == NodeType.TEXT && children.length == 1) {
        tb.add(QueryString.toValue(child.string()));
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
