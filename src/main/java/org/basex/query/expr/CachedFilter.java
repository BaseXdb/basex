package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Filter expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class CachedFilter extends Filter {

  /**
   * Constructor.
   * @param ii input info
   * @param r expression
   * @param p predicates
   */
  public CachedFilter(final InputInfo ii, final Expr r, final Expr... p) {
    super(ii, r, p);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    Value val = root.value(ctx);
    final Value cv = ctx.value;
    final long cs = ctx.size;
    final long cp = ctx.pos;

    try {
      // evaluate first predicate, based on incoming value
      final ValueBuilder vb = new ValueBuilder();
      Expr p = preds[0];
      long is = val.size();
      ctx.size = is;
      ctx.pos = 1;
      for(int s = 0; s < is; ++s) {
        final Item it = val.itemAt(s);
        ctx.value = it;
        if(p.test(ctx, info) != null) vb.add(it);
        ctx.pos++;
      }
      // save memory
      val = null;

      // evaluate remaining predicates, based on value builder
      final int pl = preds.length;
      for(int i = 1; i < pl; i++) {
        is = vb.size();
        p = preds[i];
        ctx.size = is;
        ctx.pos = 1;
        int c = 0;
        for(int s = 0; s < is; ++s) {
          final Item it = vb.get(s);
          ctx.value = it;
          if(p.test(ctx, info) != null) vb.set(it, c++);
          ctx.pos++;
        }
        vb.size(c);
      }

      // return resulting values
      return vb;
    } finally {
      ctx.value = cv;
      ctx.size = cs;
      ctx.pos = cp;
    }
  }

  @Override
  public Filter addPred(final QueryContext ctx, final VarScope scp, final Expr p)
      throws QueryException {
    preds = Array.add(preds, p);
    return this;
  }

  @Override
  public Filter copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    final Filter f = new CachedFilter(info, root == null ? null : root.copy(ctx, scp, vs),
        Arr.copyAll(ctx, scp, vs, preds));
    f.pos = pos;
    f.last = last;
    return f;
  }
}
