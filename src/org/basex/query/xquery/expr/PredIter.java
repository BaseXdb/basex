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
public final class PredIter extends Pred {
  /** Flag is set to true if predicate has last function. */
  boolean lastFlag;
  /** Flag is set to true if predicate has a numeric value. */
  boolean numFlag;

  /**
   * Constructor.
   * @param r Root Expression
   * @param e Expression List
   * @param last lastFlag is true if predicate has a last function
   * @param num numberFlag is true if predicate has a numeric value
   */
  public PredIter(final Expr r, final Expr[] e,
      final boolean last, final boolean num) {

    super(r, e);
    lastFlag = last;
    numFlag = num;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    return new Iter() {
      final Iter iter = ctx.iter(root);
      final Item ci = ctx.item;
      final int cp = ctx.pos;

      Item i;
      boolean first = true;
      boolean finished = false;

      @Override
      public Item next() throws XQException {
        if(finished) {
          ctx.item = ci;
          ctx.pos = cp;
          return null;
        }

        if(first) {
          first = false;
          ctx.pos = 1;
        }

        while((i = iter.next()) != null) {
          ctx.item = i;
          i = ctx.iter(expr[0]).ebv();

          final boolean found = i.n() ? i.dbl() == ctx.pos : i.bool();
          ctx.pos++;
          
          if(found) {
            // if item is numeric it will be returned and the rest of expr
            // will be skipped. next call of next() will return null.
            ctx.item.score(i.score());
            if(numFlag) finished = true;
            return ctx.item;
          }
        }

        // returns the last item.
        // next call of next() will return null.
        if(lastFlag) {
          finished = true;
          return ctx.item;
        }

        ctx.item = ci;
        ctx.pos = cp;
        return null;
      }
    };
  }
}
