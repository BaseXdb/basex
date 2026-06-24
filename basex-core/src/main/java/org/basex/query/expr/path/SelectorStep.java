package org.basex.query.expr.path;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
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
   * @param test kind test (wildcard)
   * @param selector selector
   * @param preds predicates
   */
  public SelectorStep(final InputInfo info, final Axis axis, final Test test, final Expr selector,
      final Expr... preds) {
    super(info, axis, test, preds);
    this.selector = selector;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // evaluate name keys once, with the context node as focus
    final Value keys = selector.atomValue(qc, info);

    // collect axis nodes that match one of the keys
    final GNodeList list = new GNodeList();
    for(final GNode node : iterator(qc)) {
      if(matches(keys, node)) list.add(node);
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
            key.type.isStringOrUntyped() && Token.eq(key.string(info), qname.local()))
            return true;
        }
      }
    }
    return false;
  }

  @Override
  Expr optimize(final Expr root, final CompileContext cc) throws QueryException {
    selector = selector.optimize(cc);

    // no keys: no results
    if(selector.seqType().zero()) return cc.emptySeq(this);

    // constant QName keys: rewrite to a static name test
    if(selector instanceof final Value value) {
      final ArrayList<Test> list = new ArrayList<>();
      boolean qnames = true;
      for(final Item item : value) {
        if(item instanceof final QNm qnm) {
          list.add(Test.get(axis == Axis.ATTRIBUTE ? Kind.ATTRIBUTE : null, qnm, null, null));
        } else {
          qnames = false;
          break;
        }
      }
      final Test t = qnames ? Test.get(list) : null;
      if(t != null) {
        final Expr step = Step.get(cc, root, info, axis, t, exprs);
        cc.info(QueryText.OPTREWRITE_X_X, this, step);
        return step;
      }
    }

    // dynamic name test: assign type, optimize predicates
    assignType(root != null ? root : cc.qc.focus.value);
    return optimize(cc, this) ? cc.emptySeq(this) : this;
  }

  @Override
  public Step copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new SelectorStep(info, axis, test.copy(), selector.copy(cc, vm),
        copyAll(cc, vm, exprs)));
  }
}
