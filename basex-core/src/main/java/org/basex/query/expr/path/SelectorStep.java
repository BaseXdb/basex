package org.basex.query.expr.path;

import java.util.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Axis step with a dynamic selector expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SelectorStep extends Step {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param axis axis
   * @param selector selector
   * @param preds predicates
   */
  public SelectorStep(final InputInfo info, final Axis axis, final Expr selector,
      final Expr... preds) {
    this(info, axis, Test.get(axis == Axis.ATTRIBUTE ? Kind.ATTRIBUTE : null, null,
      NameTest.Scope.ALL, null), selector, preds);
  }
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param axis axis
   * @param test kind test (wildcard)
   * @param selector selector
   * @param preds predicates
   */
  SelectorStep(final InputInfo info, final Axis axis, final Test test, final Expr selector,
      final Expr... preds) {
    super(info, axis, test, preds);
    this.selector = selector;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // evaluate name keys with an absent focus once
    final QueryFocus focus = qc.focus;
    Value keys;
    qc.focus = new QueryFocus();
    try {
      keys = selector.atomValue(qc, info);
    } catch(final QueryException ex) {
      // focus-dependent step on JNodes: suggest simple map operator
      if(ex.error() == QueryError.NOCTX_X && selector.has(Flag.CTX) &&
          (focus.value instanceof JNode || focus.value instanceof XQStruct)) {
        throw QueryError.SELECTORCTX_X.get(ex.info(), selector);
      }
      throw ex;
    } finally {
      qc.focus = focus;
    }
    // duplicate keys are irrelevant: replicate(1, $n) → 1
    if(keys instanceof final SingletonSeq ss && ss.singleItem()) keys = ss.itemAt(0);

    final GNodeList list = new GNodeList();
    final Value value = focus.value;
    if(axis == Axis.CHILD && keys instanceof final Item key &&
        toContextNode(value) instanceof final JNode jnode) {
      // child axis, single key: direct key lookup
      final JNode child = jnode.child(key);
      if(child != null) list.add(child);
    } else {
      // collect axis nodes that match one of the keys
      for(final GNode node : iterator(qc)) {
        if(matches(keys, node)) list.add(node);
      }
    }
    // evaluate predicates
    return preds(list, qc);
  }

  /**
   * Checks if a node matches one of the keys.
   * @param keys atomized name keys
   * @param node node to be checked
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean matches(final Value keys, final GNode node) throws QueryException {
    // range sequence: compare numeric jkey property with bounds (XNode names never match)
    if(keys instanceof final RangeSeq rs) {
      return node instanceof final JNode jnode && jnode.key instanceof final ANum num &&
          CmpG.compare(num, rs, CmpOp.EQ);
    }
    if(node instanceof final JNode jnode) {
      // JNode: compare jkey property with atomic values
      final Item jkey = jnode.key;
      if(jkey != null) {
        for(final Item key : keys) {
          if(key.atomicEqual(jkey)) return true;
        }
      }
    } else {
      // XNode: compare node name with QName values, local name with string values
      final QNm qname = node.qname();
      if(qname != null) {
        for(final Item key : keys) {
          if(key instanceof final QNm qnm ? qnm.eq(qname) :
            key.type.isStringOrUntyped() && qname.eqLocal(key.string(info)))
            return true;
        }
      }
    }
    return false;
  }

  @Override
  Expr optimize(final Expr root, final CompileContext cc) throws QueryException {
    // duplicates and order of keys are irrelevant: child::{ replicate(1, 3) } → child::{ 1 }
    selector = cc.get(null, false, () -> selector.optimize(cc).simplifyFor(Simplify.SET, cc));

    // no keys: child::{ () } → ()
    if(selector.seqType().zero()) return cc.emptySeq(this);

    // constant keys: $node/child::{ #a } → $node/child::a (range sequences are matched by bounds)
    if(selector instanceof final Value value && !(value instanceof RangeSeq)) {
      final Expr in = root != null ? root : cc.qc.focus.value;
      final Type it = in != null ? in.seqType().type : null;
      final boolean jnode = it != null &&
          (it.instanceOf(NodeType.JNODE) || it.instanceOf(Types.MAP_OR_ARRAY));

      final ArrayList<Test> list = new ArrayList<>();
      boolean fold = true;
      for(final Item item : value) {
        if(jnode && item.type.instanceOf(BasicType.ANY_ATOMIC_TYPE)) {
          list.add(JNodeTest.get(item, null));
        } else if(item instanceof final QNm qnm) {
          list.add(Test.get(axis == Axis.ATTRIBUTE ? Kind.ATTRIBUTE : null, qnm, null, null));
        } else {
          fold = false;
          break;
        }
      }
      final Test t = fold ? Test.get(list) : null;
      if(t != null) {
        final Expr step = Step.get(cc, root, info, axis, t, exprs);
        cc.info(QueryText.OPTREWRITE_X_X, this, step);
        return step;
      }
    }
    return super.optimize(root, cc);
  }

  @Override
  public Step copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new SelectorStep(info, axis, test.copy(), selector.copy(cc, vm),
        copyAll(cc, vm, exprs)));
  }
}
