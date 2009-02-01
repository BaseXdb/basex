package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

/**
 * Predicate expression. Mustn't be called with more than one predicate.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Dennis Stratmann
 */
public final class IterPred extends Pred {
  /** Flag is set to true if predicate has last function. */
  final boolean last;
  /** Optional position predicate. */
  final Pos pos;

  /**
   * Constructor.
   * @param r root expression
   * @param p predicates
   * @param ps position predicate; may equal the first predicate
   * @param l true if predicate has a last function
   */
  public IterPred(final Expr r, final Expr[] p, final Pos ps, final boolean l) {
    super(r, p);
    last = l;
    pos = ps;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      boolean more = true;
      boolean fast;
      Iter iter;
      long p;

      @Override
      public Item next() throws QueryException {
        // first call - initialize iterator
        if(iter == null) {
          iter = ctx.iter(root);
          p = 1;
         
          // iterator size is known - items can be directly accessed
          if(iter.size() != -1) {
            if(last) {
              p = iter.size();
              fast = true;
            } else if(pos != null) {
              p = pos.min;
              fast = true;
            }
          }
        }

        // cache context
        final Item ci = ctx.item;
        final long cp = ctx.pos;
        Item it = null;

        if(more) {
          if(fast) {
            // directly access relevant items
            it = iter.get(p++ - 1);
            ctx.pos = p;
          } else {
            // loop through all items
            while((it = iter.next()) != null) {
              // set context item and position
              ctx.item = it;
              ctx.pos = p++;
              final Item i = pred[0].test(ctx);
              if(i != null) {
                // item accepted.. adopt scoring value
                it.score(i.score());
                break;
              }
            }
            // returns the last item
            if(last) it = ctx.item;
          }
        }

        // check if more items are to be expected
        more &= !last && (pos == null || pos.more(ctx));

        // reset context
        ctx.item = ci;
        ctx.pos = cp;
        return it;
      }
    };
  }
}
