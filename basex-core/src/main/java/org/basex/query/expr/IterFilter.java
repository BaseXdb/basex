package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
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
   * @param f original filter
   */
  IterFilter(final Filter f) {
    super(f.info, f.root, f.preds);
    type = f.type;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      /** Iterator. */
      Iter iter;

      @Override
      public Item next() throws QueryException {
        // first call - initialize iterator
        if(iter == null) iter = ctx.iter(root);
        // filter sequence
        for(Item it; (it = iter.next()) != null;) {
          ctx.checkStop();
          if(preds(it, ctx)) return it;
        }
        return null;
      }
    };
  }

  @Override
  public Filter copy(final QueryContext ctx, final VarScope scp,
      final IntObjMap<Var> vs) {
    final Filter f = new CachedFilter(info, root == null ? null : root.copy(ctx, scp, vs),
        Arr.copyAll(ctx, scp, vs, preds));
    return copy(new IterFilter(f));
  }

  @Override
  public Filter addPred(final QueryContext ctx, final VarScope scp, final Expr p)
      throws QueryException {
    // [LW] should be fixed
    return ((Filter) new CachedFilter(info, root, preds).copy(ctx, scp)
        ).addPred(ctx, scp, p);
  }
}
