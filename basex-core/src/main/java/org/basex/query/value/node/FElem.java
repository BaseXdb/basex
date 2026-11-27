package org.basex.query.value.node;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.lang.invoke.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.path.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
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
  /** Namespaces (can be {@code null}). */
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
   * @param scope namespaces in scope of the parent ({@code null} for a top-level element)
   * @return element builder
   */
  public static FBuilder build(final Element elem, final Atts scope) {
    final String nsUri = elem.getNamespaceURI();
    final QNm name = new QNm(elem.getNodeName(), nsUri == null ? Token.EMPTY : token(nsUri));

    final FBuilder builder = build(name);
    final Atts nspaces = new Atts();

    // attributes and namespaces
    final NamedNodeMap at = elem.getAttributes();
    final int al = at.getLength();

    for(int a = 0; a < al; a++) {
      final Attr attr = (Attr) at.item(a);
      final byte[] pref = nsPrefix(attr);
      if(pref != null) nspaces.add(pref, token(attr.getValue()));
      else builder.node(new FAttr(attr));
    }

    // top-level element: namespaces in scope of the DOM ancestors
    final Atts outer = scope != null ? scope : nsScope(elem.getParentNode());
    // declare the namespace of the name unless it is declared or inherited
    final byte[] prefix = name.prefix(), uri = name.uri();
    final byte[] declared = nspaces.value(prefix);
    final byte[] bound = declared != null ? declared : outer.value(prefix);
    if(bound == null || !Token.eq(uri, bound)) nspaces.add(prefix, uri);

    // top-level element: declare inherited namespaces, which are then in scope of the children
    Atts inScope = nspaces;
    if(scope == null) {
      addMissing(nspaces, outer);
    } else if(nspaces.isEmpty()) {
      inScope = scope;
    } else {
      inScope = new Atts(nspaces);
      addMissing(inScope, scope);
    }
    if(!nspaces.isEmpty()) builder.namespaces = nspaces;
    children(elem, builder, inScope);
    return builder;
  }

  /**
   * Adds namespaces whose prefixes are not contained in the target.
   * @param target target namespaces
   * @param source source namespaces
   */
  private static void addMissing(final Atts target, final Atts source) {
    final int ss = source.size();
    for(int s = 0; s < ss; s++) {
      if(!target.contains(source.name(s))) target.add(source.name(s), source.value(s));
    }
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
  @Override
  GNode[] children() {
    final Object cont = content;
    if(cont instanceof final GNode[] nodes) return nodes;

    final GNode[] nodes = { new FTxt((byte[]) cont, this, id + 1) };
    final Object witness = CONTENT.compareAndExchange(this, cont, nodes);
    return witness == cont ? nodes : (GNode[]) witness;
  }

  /**
   * Returns the namespaces in scope of the given DOM node.
   * @param node DOM node (can be {@code null})
   * @return namespaces
   */
  private static Atts nsScope(final Node node) {
    final Atts ns = new Atts();
    // only elements can declare namespaces
    for(Node n = node; n instanceof Element; n = n.getParentNode()) {
      final String prefix = n.getPrefix();
      if(prefix != null && !ns.contains(token(prefix))) {
        ns.add(token(prefix), token(n.getNamespaceURI()));
      }
      final NamedNodeMap atts = n.getAttributes();
      final int al = atts.getLength();
      for(int a = 0; a < al; a++) {
        final Attr attr = (Attr) atts.item(a);
        final byte[] pref = nsPrefix(attr);
        if(pref != null && !ns.contains(pref)) ns.add(pref, token(attr.getValue()));
      }
    }
    return ns;
  }

  /**
   * Returns the prefix declared by a DOM namespace attribute.
   * @param attr DOM attribute
   * @return prefix, or {@code null} if the attribute is no namespace declaration
   */
  private static byte[] nsPrefix(final Attr attr) {
    final byte[] name = token(attr.getName());
    return Token.eq(name, XMLNS) ? Token.EMPTY : startsWith(name, XMLNS_COLON) ? local(name) : null;
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
   * Adds in-scope namespaces of the original element that the copy does not declare
   * (copy-namespaces {@code preserve} mode).
   * @param orig original element
   */
  public void preserve(final XNode orig) {
    if(!orig.inherits()) return;
    final Atts scope = orig.nsScope(null);
    Atts ns = null;
    final int ss = scope.size();
    for(int s = 0; s < ss; s++) {
      final byte[] prefix = scope.name(s);
      if(!namespaces.contains(prefix)) {
        if(ns == null) ns = new Atts(namespaces);
        ns.add(prefix, scope.value(s));
      }
    }
    if(ns != null) namespaces = ns.optimize();
  }

  /**
   * Adds a namespace binding to an unexposed element unless it declares the prefix itself.
   * @param node node
   * @param prefix prefix
   * @param uri namespace URI
   * @return node with the binding
   */
  public static GNode declare(final XNode node, final byte[] prefix, final byte[] uri) {
    if(node.kind() != Kind.ELEMENT || node.namespaces().contains(prefix)) return node;
    final FElem elem = (FElem) (node instanceof FElem ? node : fragment(node));
    elem.namespaces = new Atts(elem.namespaces).add(prefix, uri).optimize();
    return elem;
  }

  /**
   * Undeclares the default namespace of the new parent in an unexposed element in no namespace.
   * @param node node
   * @param dflt default namespace of the new parent (can be {@code null})
   * @return node with the undeclaration
   */
  public static GNode undeclare(final XNode node, final byte[] dflt) {
    if(dflt == null || dflt.length == 0 || node.kind() != Kind.ELEMENT) return node;
    final QNm qname = node.qname();
    return qname.hasPrefix() || qname.uri().length != 0 ? node : declare(node, EMPTY, EMPTY);
  }

  /**
   * Removes namespaces that are used neither by the element nor by its attributes
   * (copy-namespaces {@code no-preserve} mode).
   */
  public void noPreserve() {
    for(int n = namespaces.size() - 1; n >= 0; n--) {
      if(!usesPrefix(namespaces.name(n))) namespaces.remove(n);
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
  public BasicNodeIter attributeIter(final Test test) {
    return iter(attributes, test);
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

    final FBuilder elem = build(name).ns(namespaces);
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
      tb.add('>').add(value != null ? QueryString.toContent(value, false) : DOTS).
        add("</").add(nm).add('>');
    }
    qs.token(tb.finish());
  }
}
