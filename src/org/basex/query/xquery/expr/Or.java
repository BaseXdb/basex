package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQExprList;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Scoring;

/**
 * Or expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Or extends Arr {
  /**
   * Constructor.
   * @param e expression list
   */
  public Or(final Expr[] e) {
    super(e);
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    //int el = expr.length;
    XQExprList exli = new XQExprList(expr.length);
    for(int e = 0; e < expr.length; e++) {
      expr[e] = ctx.comp(expr[e]);
      if(!expr[e].i()) {
        exli.add(expr[e], false, ctx);
        continue;
      }
      if(((Item) expr[e]).bool()) return Bln.TRUE;
      //Array.move(expr, e + 1, -1, --el - e);
      //--e;
    }
    
    return exli.size == expr.length ? this : exli.size == 0 ?  Bln.FALSE :
      exli.singel() ? exli.list[0] : new Or(exli.finishXQ());
  }
  
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    double d = 0;
    boolean found = false;
    for(final Expr e : expr) {
      final Item it = ctx.iter(e).ebv();
      if(it.bool()) {
        final double s = it.score();
        if(s == 0) return Bln.TRUE.iter();
        d = Scoring.or(d, s);
        found = true;
      }
    }
    return (d == 0 ? Bln.get(found) : new Bln(true, d)).iter();
  }

  @Override
  public String toString() {
    return toString(" or ");
  }
}
