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

  /** flag is set to true if prediacte has last function. */
  boolean lastFlag = false;
  /** flag is set to true if prediacte has a numeric value. */
  boolean numFlag = false;
  
  /**
   * Constructor.
   * @param r Root Expression
   * @param e Expression List
   */
  public PredIter(final Expr r, final Expr[] e) {
    super(r, e);
  }
  
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
    this.lastFlag = last;
    this.numFlag = num;
  }
  
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    return new Iter() {

      Iter iter = ctx.iter(root);

      final Item ci = ctx.item;
      final int cp = ctx.pos;

      Item i;
      boolean firstTime = true;
      boolean returnNull = false;

      @Override
      public Item next() throws XQException {

        if (returnNull) {
          ctx.item = ci;
          ctx.pos = cp;
          return null;
        }
        
        if (firstTime) {
          firstTime = false;
          ctx.pos = 1;
        }
        
        while ((i = iter.next()) != null) {
          ctx.item = i;
          i = ctx.iter(expr[0]).ebv();
          if(i.n() ? i.dbl() == ctx.pos : i.bool()) {
            ctx.pos++;
            
            // returns only one numeric value.
            // next call of next() will return null.
            if (numFlag) {
              numFlag = false;
              returnNull = true;
              return ctx.item;
            }
            
            return ctx.item;
          }
          ctx.pos++;
        }
        
        // returns the last item.
        // next call of next() will return null.        
        if(lastFlag) {
          returnNull = true;
          return ctx.item;
        }

        ctx.item = ci;
        ctx.pos = cp;
        return null;
      }
    };
  }
}
