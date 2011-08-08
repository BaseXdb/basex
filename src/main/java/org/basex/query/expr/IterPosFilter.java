package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;

/**
 * Iterative filter expression with position predicates.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class IterPosFilter extends Filter {
  /** Offset flag. */
  boolean off;

  /**
   * Constructor.
   * @param f original filter
   * @param o offset flag
   */
  IterPosFilter(final Filter f, final boolean o) {
    super(f.input, f.root, f.preds);
    type = f.type;
    last = f.last;
    size = f.size;
    pos = f.pos;
    off = o;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      /** Indicates if more items can be expected. */
      boolean skip;
      /** Fast evaluation. */
      boolean direct;
      /** Iterator. */
      Iter iter;
      /** Current position. */
      long cpos;

      @Override
      public Item next() throws QueryException {
        if(skip) return null;

        // first call - initialize iterator
        if(iter == null) {
          if(off) {
            // evaluate offset and create position expression
            final Item it = preds[0].ebv(ctx, input);
            final long l = it.itr(input);
            final Expr e = Pos.get(l, l, input);
            // don't accept fractional numbers
            if(l != it.dbl(input) || !(e instanceof Pos)) return null;
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
              direct = true;
            }
          }
        }

        // cache context
        final Value cv = ctx.value;
        final long cp = ctx.pos;

        Item item = null;
        if(direct) {
          // directly access relevant items
          item = iter.size() < cpos ? null : iter.get(cpos - 1);
          ctx.pos = cpos++;
        } else {
          // loop through all items
          Item old = null;
          while((item = iter.next()) != null) {
            ctx.checkStop();
            // set context position
            ctx.pos = cpos++;

            if(preds(item, ctx)) break;
            // remember last node
            old = item;
          }
          // returns the last item
          if(last) item = old;
        }

        // check if more items can be expected
        skip = last || pos != null && pos.skip(ctx);
        if(skip && direct) iter.reset();

        // reset context and return result
        ctx.value = cv;
        ctx.pos = cp;
        return item;
      }
    };
  }
}
