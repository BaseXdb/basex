package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * Iterative filter expression with numeric predicates.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class IterPosFilter extends Filter {
  /** Offset flag. */
  private final boolean off;

  /**
   * Constructor.
   * @param f original filter
   * @param o offset flag
   */
  IterPosFilter(final Filter f, final boolean o) {
    super(f.info, f.root, f.preds);
    off = o;
    type = f.type;
    last = f.last;
    size = f.size;
    pos = f.pos;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      boolean skip, direct;
      Iter iter;
      long cpos;

      @Override
      public Item next() throws QueryException {
        if(skip) return null;

        // first call - initialize iterator
        if(iter == null) {
          if(off) {
            // evaluate offset and create position expression
            final Item it = preds[0].ebv(ctx, info);
            final long l = it.itr(info);
            final Expr e = Pos.get(l, l, info);
            // don't accept fractional numbers
            if(l != it.dbl(info) || !(e instanceof Pos)) return null;
            pos = (Pos) e;
          }

          iter = ctx.iter(root);
          cpos = 1;

          if(pos != null || last) {
            // runtime optimization:
            // items can be directly accessed if the iterator size is known
            final long s = iter.size();
            if(s == 0) return null;
            if(s != -1) {
              cpos = last ? s : pos.min;
              if(cpos > s) return null;
              direct = preds.length == 1;
            }
          }
        }

        // cache context
        final long cp = ctx.pos;
        final long cs = ctx.size;
        try {
          Item item;
          if(direct) {
            // directly access relevant items
            item = iter.size() < cpos ? null : iter.get(cpos - 1);
            ctx.pos = cpos++;
          } else {
            // loop through all items
            Item lnode = null;
            while((item = iter.next()) != null) {
              // evaluate predicates
              ctx.checkStop();
              ctx.size = 0;
              ctx.pos = cpos++;
              if(preds(item, ctx)) break;
              // remember last node
              lnode = item;
              ctx.pos = cp;
              ctx.size = cs;
            }
            // returns the last item
            if(last) item = lnode;
          }

          // check if more items can be expected
          skip = last || pos != null && pos.skip(ctx);
          if(skip && direct) iter.reset();
          return item;
        } finally {
          // reset context and return result
          ctx.pos = cp;
          ctx.size = cs;
        }
      }
    };
  }

  @Override
  public Filter copy(final QueryContext ctx, final VarScope scp,
      final IntObjMap<Var> vs) {
    final Filter f = new CachedFilter(info, root == null ? null : root.copy(ctx, scp, vs),
        Arr.copyAll(ctx, scp, vs, preds));
    return copy(new IterPosFilter(f, off));
  }

  @Override
  public Filter addPred(final QueryContext ctx, final VarScope scp, final Expr p)
      throws QueryException {
    // [LW] should be fixed
    return ((Filter) new CachedFilter(info, root, preds).copy(ctx, scp)
        ).addPred(ctx, scp, p);
  }
}
