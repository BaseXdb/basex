package org.basex.query.value.node;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.lang.invoke.*;
import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.path.*;
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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FElem extends FNode {
  /** Handle for materializing a collapsed text child. */
  private static final VarHandle CONTENT;

  static {
    try {
      CONTENT = MethodHandles.lookup().findVarHandle(FElem.class, "content", Object.class);
    } catch(final ReflectiveOperationException ex) {
      throw Util.notExpected(ex);
    }
  }

  /** Element name. */
  private QNm name;
  /** Base URI. */
  private final byte[] uri;
  /** Namespaces. */
  private Atts namespaces;
  /** Namespaces inherited from enclosing constructors (can be {@code null}). */
  private Atts nsInherited;
  /** Attributes. */
  private GNode[] attributes;
  /** Children: node array, or the value of a single collapsed text child. */
  private volatile Object content;

  /**
   * Constructor.
   * @param name element name
   * @param uri base URI
   */
  private FElem(final QNm name, final byte[] uri) {
    super(NodeType.ELEMENT, ids(2));
    this.name = name;
    this.uri = uri;
  }

  /**
   * Convenience constructor for creating an element.
   * @param name element name
   * @return element builder
   */
  public static FBuilder build(final QNm name) {
    return build(name, Token.EMPTY);
  }

  /**
   * Convenience constructor for creating an element.
   * @param name element name
   * @param uri base URI
   * @return element builder
   */
  public static FBuilder build(final QNm name, final byte[] uri) {
    return new FBuilder(new FElem(name, uri));
  }

  /**
   * Constructor for creating an element from a DOM node.
   * Originally provided by Erdal Karaca.
   * @param elem DOM node
   * @param nsMap namespaces in scope
   * @return element builder
   */
  public static FBuilder build(final Element elem, final TokenObjectMap<byte[]> nsMap) {
    final String nsUri = elem.getNamespaceURI();
    final QNm name = new QNm(elem.getNodeName(), nsUri == null ? Token.EMPTY : token(nsUri));

    final FBuilder builder = build(name);
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
        builder.node(new FAttr(attr));
      }
    }

    // add new namespaces
    final int ns = nspaces.size();
    for(int n = 0; n < ns; n++) nsMap.put(nspaces.name(n), nspaces.value(n));

    // no parent, so we have to add all namespaces in scope
    TokenObjectMap<byte[]> namespaces = nsMap;
    if(namespaces == null) {
      namespaces = new TokenObjectMap<>();
      nsScope(elem.getParentNode(), namespaces);
      for(final byte[] prefix : namespaces) {
        if(!nspaces.contains(prefix)) nspaces.add(prefix, namespaces.get(prefix));
      }
    }
    final byte[] prefix = name.prefix(), uri = name.uri(), old = namespaces.get(prefix);
    if(old == null || !Token.eq(uri, old)) {
      nspaces.add(prefix, uri);
      nsMap.put(prefix, uri);
    }
    if(!nspaces.isEmpty()) builder.namespaces = nspaces;
    children(elem, builder, new TokenObjectMap<>());
    return builder;
  }

  /**
   * Renames the element (namespace conflict).
   * @param nm new name
   */
  void rename(final QNm nm) {
    name = nm;
  }

  /**
   * Finalizes the node.
   * @param ns namespaces
   * @param inherited namespaces inherited from enclosing constructors (can be {@code null})
   * @param at attributes
   * @param ch children
   * @return self reference
   */
  FElem finish(final Atts ns, final Atts inherited, final GNode[] at, final GNode[] ch) {
    namespaces = ns;
    nsInherited = inherited;
    attributes = at;
    content = ch.length == 1 && ch[0] instanceof final FTxt txt ? txt.string() : ch;
    return this;
  }

  @Override
  public byte[] textValue() {
    final Object cont = content;
    if(cont instanceof final byte[] value) return value;

    final GNode[] nodes = (GNode[]) cont;
    return nodes.length == 1 && nodes[0] instanceof final FTxt txt ? txt.string() : null;
  }

  /**
   * Returns the children, materializing a collapsed text child.
   * @return children
   */
  private GNode[] children() {
    final Object cont = content;
    if(cont instanceof final GNode[] nodes) return nodes;

    final GNode[] nodes = { new FTxt((byte[]) cont, this, id + 1) };
    final Object witness = CONTENT.compareAndExchange(this, cont, nodes);
    return witness == cont ? nodes : (GNode[]) witness;
  }

  /**
   * Gathers all defined namespaces in the scope of the given DOM element.
   * @param elem DOM element
   * @param nsMap map
   */
  private static void nsScope(final Node elem, final TokenObjectMap<byte[]> nsMap) {
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
  public Atts nsInherited() {
    return nsInherited;
  }

  /**
   * Assigns namespaces inherited from enclosing constructors.
   * @param inherited inherited namespaces (can be {@code null})
   */
  public void nsInherited(final Atts inherited) {
    nsInherited = inherited;
  }

  /**
   * Removes namespaces that are used neither by the element nor by its attributes
   * (copy-namespaces {@code no-preserve} mode).
   */
  public void noPreserve() {
    final byte[] prefix = name.prefix();
    for(int n = namespaces.size() - 1; n >= 0; n--) {
      final byte[] pref = namespaces.name(n);
      boolean used = eq(pref, prefix);
      for(int a = 0; !used && a < attributes.length; a++) {
        used = eq(attributes[a].qname().prefix(), pref);
      }
      if(!used) namespaces.remove(n);
    }
  }

  @Override
  public byte[] string() {
    final Object cont = content;
    return cont instanceof final byte[] value ? value : string((GNode[]) cont);
  }

  @Override
  public byte[] baseURI() {
    final byte[] base = attribute(XML_BASE);
    // nested elements inherit the base URI from their parents
    return base != null ? base : parent() == null ? uri : Token.EMPTY;
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
    return GNodeList.iter(attributes);
  }

  @Override
  public BasicNodeIter childIter(final Test test, final boolean descendant) {
    return GNodeList.iter(children());
  }

  @Override
  public boolean hasChildren() {
    return !(content instanceof final GNode[] nodes) || nodes.length != 0;
  }

  @Override
  public boolean hasAttributes() {
    return attributes.length != 0;
  }

  @Override
  public FNode materialize(final Predicate<Data> test, final boolean funcs, final InputInfo ii,
      final QueryContext qc) throws QueryException {

    if(materialized(test, funcs, ii)) return this;

    final FBuilder elem = build(name);
    final int ns = namespaces.size();
    for(int n = 0; n < ns; n++) elem.ns(namespaces.name(n), namespaces.value(n));
    for(final GNode attribute : attributes) {
      elem.node((GNode) attribute.materialize(test, funcs, ii, qc));
    }
    final Object cont = content;
    if(cont instanceof final byte[] value) {
      elem.node(new FTxt(value));
    } else {
      for(final GNode child : (GNode[]) cont) {
        elem.node((GNode) child.materialize(test, funcs, ii, qc));
      }
    }
    return elem.finish();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final FElem f && name.eq(f.name) &&
        sameContent(f) && Arrays.equals(attributes, f.attributes) &&
        Objects.equals(namespaces, f.namespaces) && super.equals(obj);
  }

  /**
   * Compares the children with those of another element.
   * @param elem element to be compared
   * @return result of check
   */
  private boolean sameContent(final FElem elem) {
    final Object cont = content, cont2 = elem.content;
    return !(cont instanceof byte[]) && !(cont2 instanceof byte[]) &&
      Arrays.equals((GNode[]) cont, (GNode[]) cont2);
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
    for(final GNode attr : attributes) tb.add(' ').add(attr);
    if(!hasChildren()) {
      tb.add("/>");
    } else {
      final byte[] value = textValue();
      tb.add('>').add(value != null ? QueryString.toValue(value) : DOTS).
        add("</").add(nm).add('>');
    }
    qs.token(tb.finish());
  }
}
