package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;

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
   * @param ps position predicate
   * @param l true if predicate has a last function
   */
  public IterPred(final Expr r, final Expr[] p, final Pos ps, final boolean l) {
    super(r, p);
    last = l;
    pos = ps;
  }

  @Override
  public Iter iter(final XQContext ctx) {
    return new Iter() {
      boolean more = true;
      Iter iter;
      Item ci;
      int cp;

      @Override
      public Item next() throws XQException {
        if(more) {
          if(iter == null) {
            iter = ctx.iter(root);
            ci = ctx.item;
            cp = ctx.pos;
            ctx.pos = 1;
          }
  
          Item i;
          while((i = iter.next()) != null) {
            ctx.item = i;
            i = ctx.iter(pred[0]).test(ctx);
            ctx.pos++;
            
            if(i != null) {
              // if item is numeric, the rest of expr will be skipped
              ctx.item.score(i.score());
              return ctx.item;
            }
            // no more results are to be expected
            if(pos != null && !pos.more(ctx)) break;
          }
  
          // returns the last item.
          // next call of next() will return null.
          if(last) {
            more = false;
            return ctx.item;
          }
        }

        ctx.item = ci;
        ctx.pos = cp;
        return null;
      }
    };
  }
}
