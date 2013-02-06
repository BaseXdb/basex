package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * Iterative filter expression without numeric predicates.
 *
 * @author BaseX Team 2005-12, BSD License
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
  public Filter copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    return copy(new IterFilter(super.copy(ctx, scp, vs)));
  }
}
