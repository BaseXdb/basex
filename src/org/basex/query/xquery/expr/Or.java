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
    final XQExprList exli = new XQExprList(expr.length);
    for(final Expr e : expr) {
      final Expr ex = ctx.comp(e);
      if(!ex.i()) {
        exli.add(ex);
      } else if(((Item) ex).bool()) {
        // atomic items can be pre-evaluated
        return Bln.TRUE;
      }
    }
    return exli.size == 0 ? Bln.FALSE : new Or(exli.finish());
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
    return (d == 0 ? Bln.get(found) : Bln.get(d)).iter();
  }

  @Override
  public String toString() {
    return toString(" or ");
  }
}
