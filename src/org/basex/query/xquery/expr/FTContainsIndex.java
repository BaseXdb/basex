package org.basex.query.xquery.expr;

import org.basex.index.FTTokenizer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.NodeIter;

/**
 * FTContains expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTContainsIndex extends Arr {
  /** Fulltext parser. */
  public final FTTokenizer ft = new FTTokenizer();

  /**
   * Constructor.
   * @param ex contains, select and optional ignore expression
   */
  public FTContainsIndex(final Expr... ex) {
    super(ex);
  }

  @Override
  public Iter iter(final XQContext ctx) {
    /*final FTTokenizer tmp = ctx.ftitem;
    ctx.ftitem = ft;
    final Iter iter = ctx.iter(expr[1]);
    
    boolean found = false;;
    double d = 0;
    Item i;
    while((i = iter.next()) != null) {
      final Iter iter2 = ctx.iter(expr[0]);
      //System.out.println(iter2.next());
      //final Item it = ctx.iter(expr[0]).ebv();
      //if(it.bool()) {
      d = Scoring.or(d, i.dbl());
      //d = Scoring.or(d, Scoring.and(i.dbl(), s));
        found = true;
      //}
    }
    ctx.ftitem = tmp;
    //return (d == 0 ? Bln.get(found) : new Bln(true, d)).iter();
    
    */
    return new NodeIter() {
      FTTokenizer tmp;
      Iter iter1;
    
      @Override
      public Nod next() throws XQException {
        while(true) {
          if(iter1 == null) {
            tmp = ctx.ftitem;
            ctx.ftitem = ft;
            iter1 = ctx.iter(expr[1]);  
          }
          final Item n = iter1.next();
          
          if (n != null && n.bool()) {
            return (Nod) ctx.item;
          } else {
            ctx.ftitem = tmp;
            return null;
          }
         }
        }
      };
    
  }

  @Override
  public String toString() {
    return toString(" ftcontainsIndex ");
  }

  @Override
  public Type returned() {
    return Type.BLN;
  }
}
