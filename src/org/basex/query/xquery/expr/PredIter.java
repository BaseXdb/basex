package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;

/**
 * Predicate expression if the position()-function or last()-function is used.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Dennis Stratmann
 */
public final class PredIter extends Pred {

  /**
   * Constructor.
   * @param r Root Expression
   * @param e Expression List
   */
  public PredIter(final Expr r, final Expr[] e) {
    super(r, e);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    return new Iter() {

      Iter iter = ctx.iter(root);

      final Item ci = ctx.item;
      final int cs = ctx.size;
      final int cp = ctx.pos;
      
      boolean firstTime = true;
      int predCount = 0;
      int predCountTemp = predCount - 1;
      
      int itemPosition = 0;
      int newItemPosition = 0;
      
      final SeqIter sb = new SeqIter();
      Item i;
      
      @Override
      public Item next() throws XQException {

        // cache results to support last() function
        if (firstTime) {
          while((i = iter.next()) != null) sb.add(i);
          firstTime = false;
        }

        // evaluates predicates
        while (predCount < expr.length) {
          
          // looks if it's a new predicate
          if ((predCount - 1) == predCountTemp) {
            predCountTemp++;
            ctx.size = sb.size;
            ctx.pos = 1;
          }

          while (itemPosition < sb.size) {
            ctx.item = sb.item[itemPosition];
            i = ctx.iter(expr[predCount]).ebv();
            if(i.n() ? i.dbl() == ctx.pos : i.bool()) {
              sb.item[newItemPosition] = sb.item[itemPosition];
              ctx.pos++;
              itemPosition++;
              return sb.item[newItemPosition++];
            }
            ctx.pos++;
            itemPosition++;
          }
          predCount++;
          itemPosition = 0;
          sb.size = newItemPosition;
        }

        ctx.item = ci;
        ctx.size = cs;
        ctx.pos = cp;
        return null;
      }
    };
  }
}
