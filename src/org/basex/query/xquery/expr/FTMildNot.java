package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xpath.expr.FTMildNotXP;
import org.basex.util.IntList;

/**
 * FTMildnot expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTMildNot extends Arr {
  /**
   * Constructor.
   * @param l expression list
   */
  public FTMildNot(final Expr[] l) {
    super(l);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Item it = ctx.iter(expr[0]).next();
    if(!it.bool()) return Dbl.iter(0);
    boolean r = false;
    for (int i = 1; i < expr.length; i++) {
      Item it1 = ctx.iter(expr[i]).next();
      r |= it1.bool();   
    }

    IntList[] pos = ctx.ftpos.getPos();
    if (!r || pos.length == 1 || FTMildNotXP.evalMildNot(pos)) return it.iter();
    return Dbl.iter(0);
  }

  @Override
  public String toString() {
    return toString(" not in ");
  }
}
