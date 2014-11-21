package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative filter expression without numeric predicates.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class IterFilter extends Filter {
  /**
   * Constructor.
   * @param info input info
   * @param root root expression
   * @param preds predicates
   */
  IterFilter(final InputInfo info, final Expr root, final Expr... preds) {
    super(info, root, preds);
  }

  @Override
  public Iter iter(final QueryContext qc) {
    final boolean scoring = qc.scoring;
    return new Iter() {
      /** Iterator. */
      Iter iter;

      @Override
      public Item next() throws QueryException {
        // first call - initialize iterator
        if(iter == null) iter = qc.iter(root);
        // filter sequence
        for(Item it; (it = iter.next()) != null;) {
          qc.checkStop();
          if(preds(it, qc, scoring)) return it;
        }
        return null;
      }
    };
  }

  @Override
  public IterFilter copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return copy(new IterFilter(info, root.copy(qc, scp, vs), Arr.copyAll(qc, scp, vs, preds)));
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem();
    addPlan(plan, el, root);
    super.plan(el);
  }
}
