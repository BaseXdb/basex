package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.constr.*;
import org.basex.query.func.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
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
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item value = arg(0).item(qc, info);
    final FItem select = toFunction(arg(1), 1, qc);
    final Value replacement = arg(2).value(qc);

    // effective value: the supplied value, wrapped in a root JNode if it is a map or an array
    final GNode node;
    if(value instanceof final GNode gnode) node = gnode;
    else if(value instanceof XQStruct) node = new JNode(value);
    else throw typeError(value, Types.NODE_OR_MAP_OR_ARRAY, info);
    final TokenSet targets = new TokenSet(), ancestors = new TokenSet();
    for(final Item item : select.invoke(qc, info, node)) {
      if(!(item instanceof final GNode target)) throw typeError(item, NodeType.NODE, info);
      // the effective value cannot be replaced; namespace nodes and item nodes have a parent,
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
    final Value result = transform(node, targets, ancestors, replacement, true, qc);
    return node == value ? result : ((JNode) result).value;
  }

  /**
   * Returns the nodes that a node is replaced by.
   * @param node node
   * @param targets ids of the nodes to be replaced
   * @param ancestors ids of the nodes to be replaced and their ancestors
   * @param replacement replacement
   * @param root root of the update
   * @param qc query context
   * @return resulting nodes
   * @throws QueryException query exception
   */
  private Value transform(final GNode node, final TokenSet targets, final TokenSet ancestors,
      final Value replacement, final boolean root, final QueryContext qc) throws QueryException {

    // no node in this subtree is replaced: copy it in one go. JNodes are views on values;
    // they are consumed by the rebuilt parent and need no copy
    if(!root && !ancestors.contains(node.id()))
      return node instanceof JNode ? node : copy(node, qc);

    // the builder is created first, so that node IDs are assigned in document order
    final FBuilder builder = builder(node, root);
    // content first: a node reaches the replacement with its descendants already updated
    final ValueBuilder attributes = new ValueBuilder(qc), children = new ValueBuilder(qc);
    for(final GNode attribute : node.attributeIter()) {
      attributes.add(transform(attribute, targets, ancestors, replacement, false, qc));
    }
    for(final GNode child : node.childIter()) {
      children.add(transform(child, targets, ancestors, replacement, false, qc));
    }
    final GNode copy = rebuild(node, builder, attributes.value(), children.value(), qc);
    if(!targets.contains(node.id())) return copy;
    // the copy has no parent, so the containing structure is taken from the original node
    final XQStruct container = node instanceof final JNode jnode ? jnode.container() : null;
    return substitute(copy, container, replacement, qc);
  }

  /**
   * Returns the nodes that take the place of a node.
   * @param node node with updated descendants
   * @param container map or array that contains the replaced node (can be {@code null})
   * @param replacement replacement
   * @param qc query context
   * @return resulting nodes
   * @throws QueryException query exception
   */
  private Value substitute(final GNode node, final XQStruct container, final Value replacement,
      final QueryContext qc) throws QueryException {

    // a single function item is a callback, anything else a literal replacement
    final Value items = replacement instanceof final FItem func && !(func instanceof XQStruct) ?
      checkArity(func, 1, false).invoke(qc, info, node) : replacement;

    final ValueBuilder vb = new ValueBuilder(qc);
    boolean value = false;
    for(final Item item : items) {
      if(item instanceof final GNode gnode) {
        // a root node contributes its children
        final boolean children = gnode.kind() == Kind.DOCUMENT ||
            gnode instanceof final JNode jnode && jnode.isRoot();
        // supplied XNodes may be attached to another tree, so they are copied
        for(final Item nd : children ? gnode.childIter().value(qc, null) : gnode) {
          vb.add(nd instanceof XNode ? copy((GNode) nd, qc) : nd);
        }
      } else if(container != null && item instanceof final XQStruct struct &&
          container instanceof XQMap == struct instanceof XQMap) {
        // a map within a map, or an array within an array, contributes its children
        vb.add(new JNode(struct).childIter().value(qc, null));
      } else {
        // the item replaces the value of the node, so at most one is allowed
        if(item instanceof FItem && !(node instanceof JNode))
          throw UPREPLACE_X_X.get(info, "Node", item.seqType());
        if(value) throw UPREPLACEVALUE_X.get(info, item);
        value = true;
        vb.add(withValue(node, item, qc));
      }
    }
    return vb.value(this);
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
   * Returns a node of the same kind and name or key as the specified node with a new value.
   * @param node node
   * @param value value (a map or an array only if the node is a JNode)
   * @param qc query context
   * @return resulting node
   * @throws QueryException query exception
   */
  private GNode withValue(final GNode node, final Item value, final QueryContext qc)
      throws QueryException {

    if(node instanceof final JNode jnode) return new JNode(jnode.key, value);

    final byte[] string = value.string(info);
    final XNode xnode = (XNode) node;
    switch(node.kind()) {
      case ELEMENT:
        final FBuilder builder = FElem.build(xnode.qname()).ns(xnode.namespaces());
        for(final GNode attribute : node.attributeIter()) builder.node(copy(attribute, qc));
        return builder.text(string).finish();
      case ATTRIBUTE: return new FAttr(xnode.qname(), string);
      case TEXT: return new FTxt(string);
      case COMMENT: return new FComm(FComm.parse(string, info));
      case PROCESSING_INSTRUCTION: return new FPI(xnode.qname(), FPI.content(string, info));
      // namespace nodes are not visited by the recursion, documents are never selected
      default: throw Util.notExpected(node);
    }
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
      // sequence node: the values of the updated items
      final ValueBuilder vb = new ValueBuilder(qc);
      for(final Item item : children) {
        if(!(item instanceof final JNode jnode))
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
