package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.constr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnUpdate extends StandardFunc {
  /** Action: new value. */
  private static final Str VALUE = Str.get("value");
  /** Action: new name. */
  private static final Str RENAME = Str.get("rename");
  /** Action: replacing nodes. */
  private static final Str REPLACE = Str.get("replace");
  /** Action: nodes inserted before. */
  private static final Str BEFORE = Str.get("before");
  /** Action: nodes inserted after. */
  private static final Str AFTER = Str.get("after");
  /** Default actions: the selected nodes are deleted. */
  private static final XQMap DELETE = XQMap.get(REPLACE, Empty.VALUE);

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item value = arg(0).item(qc, info);
    final FItem select = toFunction(arg(1), 1, qc);
    final XQMap actions = defined(2) ?
      (XQMap) Types.UPDATE_ACTIONS_O.coerce(arg(2).value(qc), qc, info) : DELETE;
    if(actions.contains(REPLACE) && (actions.contains(VALUE) || actions.contains(RENAME)))
      throw UPACTIONS.get(info);

    // effective value: the supplied value, wrapped in a root JNode if it is a map or an array
    final GNode node;
    if(value instanceof final GNode gnode) node = gnode;
    else if(value instanceof XQStruct) node = new JNode(value);
    else throw typeError(value, Types.NODE_OR_MAP_OR_ARRAY, info);
    final TokenSet targets = new TokenSet(), ancestors = new TokenSet();
    for(final Item item : select.invoke(qc, info, node)) {
      if(!(item instanceof final GNode target)) throw typeError(item, NodeType.NODE, info);
      // the effective value cannot be selected; namespace nodes and item nodes have a parent,
      // but are no attributes or children
      if(target.is(node) || target.kind() == Kind.NAMESPACE ||
          target instanceof final JNode jn && jn.isItem()) throw UPSELECT_X.get(info, target);
      // the ancestors are remembered: subtrees without targets are copied instead of rebuilt
      boolean belongs = false;
      for(final GNode ancestor : target.ancestorIter(true)) {
        ancestors.add(ancestor.id());
        belongs = ancestor.is(node);
        if(belongs) break;
      }
      if(!belongs) throw UPSELECT_X.get(info, target);
      targets.add(target.id());
    }
    // a supplied map or array is returned as a value
    final Value result = transform(node, targets, ancestors, actions, true, qc);
    return node == value ? result : ((JNode) result).value;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // supplied nodes are passed on to the select function
    final SeqType st = arg(0).seqType();
    if(st.type instanceof NodeType) arg(1, arg -> arg.refineFunc(cc, st.with(Occ.EXACTLY_ONE)));
    return this;
  }

  /**
   * Returns the nodes that result from updating a node.
   * @param node node
   * @param targets ids of the selected nodes
   * @param ancestors ids of the selected nodes and their ancestors
   * @param actions actions
   * @param root root of the update
   * @param qc query context
   * @return resulting nodes
   * @throws QueryException query exception
   */
  private Value transform(final GNode node, final TokenSet targets, final TokenSet ancestors,
      final XQMap actions, final boolean root, final QueryContext qc) throws QueryException {

    // no node in this subtree is selected: copy it in one go. JNodes are views on values;
    // they are consumed by the rebuilt parent and need no copy
    if(!root && !ancestors.contains(node.id()))
      return node instanceof JNode ? node : copy(node, qc);
    // nodes that are deleted by default are not rebuilt
    if(actions == DELETE && targets.contains(node.id())) return Empty.VALUE;

    // the builder is created first, so that node IDs are assigned in document order
    final FBuilder builder = builder(node, root);
    // content first: a node reaches the actions with its descendants already updated
    final ValueBuilder attributes = new ValueBuilder(qc), children = new ValueBuilder(qc);
    for(final GNode attribute : node.attributeIter()) {
      attributes.add(transform(attribute, targets, ancestors, actions, false, qc));
    }
    for(final GNode child : node.childIter()) {
      children.add(transform(child, targets, ancestors, actions, false, qc));
    }
    final GNode copy = rebuild(node, builder, attributes.value(), children.value(), qc);
    if(!targets.contains(node.id())) return copy;
    // the copy has no parent, so the containing structure is taken from the original node
    final XQStruct container = node instanceof final JNode jnode ? jnode.container() : null;
    return apply(copy, container, actions, qc);
  }

  /**
   * Returns the nodes that result from applying the actions to a node.
   * @param node node with updated descendants
   * @param container map or array that contains the original node (can be {@code null})
   * @param actions actions
   * @param qc query context
   * @return resulting nodes
   * @throws QueryException query exception
   */
  private Value apply(final GNode node, final XQStruct container, final XQMap actions,
      final QueryContext qc) throws QueryException {

    // all actions are evaluated before the content of the node is moved to its successor
    final ValueBuilder vb = new ValueBuilder(qc), after = new ValueBuilder(qc);
    supply(action(actions, BEFORE, node, qc), vb, qc);
    final Value replace = action(actions, REPLACE, node, qc);
    final Value value = action(actions, VALUE, node, qc), name = action(actions, RENAME, node, qc);
    supply(action(actions, AFTER, node, qc), after, qc);
    if(replace != null) supply(replace, vb, qc);
    else vb.add(change(node, container, value, name, qc));
    vb.add(after.value());
    return vb.value(this);
  }

  /**
   * Returns the value of an action.
   * @param actions actions
   * @param key key of the action
   * @param node node with updated descendants
   * @param qc query context
   * @return value or {@code null}
   * @throws QueryException query exception
   */
  private Value action(final XQMap actions, final Str key, final GNode node,
      final QueryContext qc) throws QueryException {
    // a single function item is called with the node
    final Value action = actions.getOrNull(key);
    return action instanceof final FItem func && !(func instanceof XQStruct) ?
      checkArity(func, 1, false).invoke(qc, info, node) : action;
  }

  /**
   * Adds the nodes that are supplied by a value.
   * @param value value (can be {@code null})
   * @param vb value builder
   * @param qc query context
   * @throws QueryException query exception
   */
  private void supply(final Value value, final ValueBuilder vb, final QueryContext qc)
      throws QueryException {
    if(value == null) return;
    for(final Item item : value) {
      if(item instanceof final GNode gnode) {
        // a root node contributes its children
        final boolean children = gnode.kind() == Kind.DOCUMENT ||
            gnode instanceof final JNode jnode && jnode.isRoot();
        // supplied XNodes may be attached to another tree, so they are copied
        for(final Item nd : children ? gnode.childIter().value(qc, null) : gnode) {
          vb.add(nd instanceof XNode ? copy((GNode) nd, qc) : nd);
        }
      } else if(item instanceof final XQStruct struct) {
        // a map or an array contributes its entries or members
        vb.add(new JNode(struct).childIter().value(qc, null));
      } else {
        throw UPREPLACE_X_X.get(info, "Node, map or array", item.seqType());
      }
    }
  }

  /**
   * Returns a builder for a node with content.
   * @param node node
   * @param root root of the update
   * @return builder (can be {@code null})
   */
  private static FBuilder builder(final GNode node, final boolean root) {
    return switch(node.kind()) {
      // the root of the update retains the namespaces inherited from its ancestors
      case ELEMENT -> {
        final XNode xnode = (XNode) node;
        yield FElem.build(xnode.qname()).ns(root ? xnode.nsScope(null) : xnode.namespaces());
      }
      case DOCUMENT -> FDoc.build();
      default -> null;
    };
  }

  /**
   * Returns a node of the same kind and name or key as the specified node.
   * @param node node
   * @param builder builder (can be {@code null})
   * @param attributes updated attributes
   * @param children updated children
   * @param qc query context
   * @return resulting node
   * @throws QueryException query exception
   */
  private GNode rebuild(final GNode node, final FBuilder builder, final Value attributes,
      final Value children, final QueryContext qc) throws QueryException {

    if(node instanceof final JNode jnode) return new JNode(jnode.key, jvalue(jnode, children, qc));
    if(builder == null) return copy(node, qc);

    final QNmSet names = new QNmSet();
    for(final Item item : attributes) {
      final XNode attribute = toXNode(item, true);
      if(!names.add(attribute.qname())) throw UPREPLACEATT_X.get(info, attribute.qname());
      builder.node(attribute);
    }
    // attributes may be supplied from other trees: bind the prefixes of their names
    Constr.attributeNS(builder, qc);
    // the children of a node must not contain two consecutive text nodes
    final TokenBuilder text = new TokenBuilder();
    for(final Item item : children) {
      final XNode child = toXNode(item, false);
      if(child.kind() == Kind.TEXT) text.add(child.string());
      else builder.text(text.next()).node(child);
    }
    return builder.text(text.finish()).finish();
  }

  /**
   * Checks that a replacement can be inserted at the position of an attribute or child node.
   * @param item item
   * @param attribute attribute position
   * @return node
   * @throws QueryException query exception
   */
  private XNode toXNode(final Item item, final boolean attribute) throws QueryException {
    if(item instanceof final XNode xnode) {
      final Kind kind = xnode.kind();
      if(attribute ? kind == Kind.ATTRIBUTE : kind != Kind.ATTRIBUTE && kind != Kind.NAMESPACE)
        return xnode;
    }
    throw UPREPLACE_X_X.get(info, attribute ? "Attribute" : "Child node", item.seqType());
  }

  /**
   * Returns a node of the same kind as the specified node with a new value and name or key.
   * @param node node with updated descendants
   * @param container map or array that contains the original node (can be {@code null})
   * @param value new value (can be {@code null})
   * @param name new name or key (can be {@code null})
   * @param qc query context
   * @return resulting node
   * @throws QueryException query exception
   */
  private GNode change(final GNode node, final XQStruct container, final Value value,
      final Value name, final QueryContext qc) throws QueryException {

    if(value == null && name == null) return node;
    if(node instanceof final JNode jnode) {
      Item key = jnode.key;
      if(name != null) {
        if(!(container instanceof XQMap)) throw UPRENAME_X.get(info, node);
        if(!(name instanceof final Item item) || !item.type.instanceOf(BasicType.ANY_ATOMIC_TYPE))
          throw typeError(name, BasicType.ANY_ATOMIC_TYPE, info);
        key = item;
      }
      return new JNode(key, value != null ? value : jnode.value);
    }

    final XNode xnode = (XNode) node;
    final Kind kind = node.kind();
    final QNm qname = name != null ? name(xnode, name, qc) : xnode.qname();
    final byte[] string = value != null ? string(value, qc) :
      kind != Kind.ELEMENT ? xnode.string() : null;
    switch(kind) {
      case ELEMENT:
        final FBuilder builder = FElem.build(qname).ns(name != null ?
          namespaces(xnode, qname, qc) : xnode.namespaces());
        // the node is not exposed anymore, so its attributes and children are moved
        for(final GNode attribute : node.attributeIter()) builder.node(attribute);
        if(string != null) builder.text(string);
        else for(final GNode child : node.childIter()) builder.node(child);
        // attributes whose prefixes are bound to another URI are renamed
        if(name != null) Constr.attributeNS(builder, qc);
        return builder.finish();
      case ATTRIBUTE:
        // xml:id processing, as in attribute constructors
        final boolean id = eq(qname.uri(), XML_URI) && qname.eqLocal(ID);
        return new FAttr(qname, id ? normalize(string) : string);
      case TEXT: return new FTxt(string);
      case COMMENT:
        if(!FComm.valid(string)) throw UPVALUE_X_X.get(info, kind, string);
        return new FComm(string);
      case PROCESSING_INSTRUCTION:
        if(!FPI.valid(string)) throw UPVALUE_X_X.get(info, kind, string);
        return new FPI(qname, string);
      // namespace nodes are not visited by the recursion, documents are never selected
      default: throw Util.notExpected(node);
    }
  }

  /**
   * Returns the atomized items of a value, joined with spaces.
   * @param value value
   * @param qc query context
   * @return string
   * @throws QueryException query exception
   */
  private byte[] string(final Value value, final QueryContext qc) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    boolean more = false;
    final Iter iter = value.atomIter(qc, info);
    for(Item item; (item = qc.next(iter)) != null;) {
      if(more) tb.add(' ');
      tb.add(item.string(info));
      more = true;
    }
    return tb.finish();
  }

  /**
   * Checks and returns a new name for an XNode.
   * @param node node
   * @param name name
   * @param qc query context
   * @return name, with a generated prefix for attributes in a namespace
   * @throws QueryException query exception
   */
  private QNm name(final XNode node, final Value name, final QueryContext qc)
      throws QueryException {

    final Kind kind = node.kind();
    if(kind != Kind.ELEMENT && kind != Kind.ATTRIBUTE && kind != Kind.PROCESSING_INSTRUCTION)
      throw UPRENAME_X.get(info, node);
    if(!(name instanceof final QNm qnm)) throw typeError(name, BasicType.QNAME, info);

    final byte[] prefix = qnm.prefix(), uri = qnm.uri();
    if(kind == Kind.PROCESSING_INSTRUCTION) {
      if(uri.length != 0 || eq(lc(qnm.local()), XML)) throw UPNAME_X_X.get(info, kind, qnm);
      return qnm;
    }
    final boolean attribute = kind == Kind.ATTRIBUTE;
    if(eq(prefix, XML) != eq(uri, XML_URI) || eq(prefix, XMLNS) || eq(uri, XMLNS_URI) ||
        attribute && prefix.length == 0 && eq(qnm.local(), XMLNS))
      throw UPNAME_X_X.get(info, kind, qnm);
    // generate a prefix for attribute names in a namespace
    return attribute && prefix.length == 0 && uri.length != 0 ?
      qc.shared.qName(concat(qc.shared.prefix(uri), cpToken(':'), qnm.local()), uri) : qnm;
  }

  /**
   * Returns the namespaces of an element, with a binding for its (new) name.
   * @param node element
   * @param name name of the element
   * @param qc query context
   * @return namespaces
   */
  private static Atts namespaces(final XNode node, final QNm name, final QueryContext qc) {
    final Atts ns = new Atts(node.namespaces());
    final byte[] prefix = name.prefix(), uri = name.uri();
    final int i = ns.get(prefix);
    if(uri.length == 0) {
      // no namespace: an inherited default namespace is undeclared
      if(i == -1) ns.add(EMPTY, EMPTY);
      else ns.value(i, EMPTY);
    } else if(!eq(prefix, XML)) {
      // the binding of the prefix is added or replaced
      if(i == -1) ns.add(qc.shared.token(prefix), qc.shared.token(uri));
      else ns.value(i, qc.shared.token(uri));
    }
    return ns;
  }

  /**
   * Checks that a node can be a child of a map or an array.
   * @param item item
   * @param map indicates if a child of a map is expected
   * @param name name of the expected child
   * @return node
   * @throws QueryException query exception
   */
  private JNode toJNode(final Item item, final boolean map, final String name)
      throws QueryException {
    // a node that was taken from another tree must stem from a structure of the same kind
    if(item instanceof final JNode jnode && (jnode.parent() == null ||
        (map ? jnode.container() instanceof XQMap : jnode.container() instanceof XQArray)))
      return jnode;
    throw UPREPLACE_X_X.get(info, name, item.seqType());
  }

  /**
   * Returns the value of a JNode, rebuilt from its updated children.
   * @param node node
   * @param children updated children
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Value jvalue(final JNode node, final Value children, final QueryContext qc)
      throws QueryException {

    if(node.value instanceof XQMap) {
      final MapBuilder mb = new MapBuilder(children.size());
      for(final Item item : children) {
        final JNode jnode = toJNode(item, true, "Map entry");
        if(mb.contains(jnode.key)) throw UPREPLACEKEY_X.get(info, jnode.key);
        mb.put(jnode.key, jnode.value);
      }
      return mb.map();
    }
    if(node.value instanceof XQArray) {
      final ArrayBuilder ab = new ArrayBuilder(qc, children.size());
      for(final Item item : children) {
        ab.add(toJNode(item, false, "Array member").value);
      }
      return ab.array();
    }
    if(node.value.size() > 1) {
      // sequence node: the values of the updated items; map entries would lose their keys
      final ValueBuilder vb = new ValueBuilder(qc);
      for(final Item item : children) {
        if(!(item instanceof final JNode jnode) || jnode.container() instanceof XQMap)
          throw UPREPLACE_X_X.get(info, "Sequence item", item.seqType());
        vb.add(jnode.value);
      }
      return vb.value();
    }
    return node.value;
  }

  /**
   * Returns a copy of the specified node.
   * @param node node
   * @param qc query context
   * @return copy
   * @throws QueryException query exception
   */
  private GNode copy(final GNode node, final QueryContext qc) throws QueryException {
    return (GNode) node.materialize(d -> false, false, info, qc);
  }
}
