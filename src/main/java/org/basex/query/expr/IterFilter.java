package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;

/**
 * Iterative predicate expression. Supports one predicate.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class IterFilter extends Filter {
  /** Flag is set to true if predicate has last function. */
  final boolean last;
  /** Optional position predicate. */
  Pos pos;

  /**
   * Constructor.
   * @param f original filter
   * @param ps position predicate; may equal the first predicate
   * @param l true if predicate has a last function
   */
  IterFilter(final Filter f, final Pos ps, final boolean l) {
    super(f.input, f.root, f.pred);
    type = f.type;
    last = l;
    pos = ps;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      boolean finish;
      boolean direct;
      Iter iter;
      long p;

      @Override
      public Item next() throws QueryException {
        if(finish) return null;

        // first call - initialize iterator
        if(iter == null) {
          iter = ctx.iter(root);
          p = 1;

          if(pos != null || last) {
            // runtime optimization:
            // items can be directly accessed if iterator size is known
            final long s = iter.size();
            if(s == 0) return null;
            if(s != -1) {
              p = last ? s : pos.min;
              if(p > s) return null;
              direct = true;
            }
          }
        }

        // cache context
        final Value cv = ctx.value;
        final long cp = ctx.pos;
        Item it = null;

        if(direct) {
          // directly access relevant items
          it = iter.size() < p ? null : iter.get(p - 1);
          ctx.pos = p++;
        } else {
          // loop through all items
          Item old = null;
          while((it = iter.next()) != null) {
            // set context item and position
            ctx.value = it;
            ctx.pos = p++;
            old = it;
            final Item i = pred[0].test(ctx, input);
            if(i != null) {
              // item accepted.. adopt scoring value
              it.score(i.score());
              break;
            }
          }
          // returns the last item
          if(last) it = old;
        }

        // check if more items are to be expected
        finish = last || pos != null && pos.last(ctx);
        if(finish && direct) iter.reset();

        // reset context
        ctx.value = cv;
        ctx.pos = cp;
        return it;
      }
    };
  }
}
