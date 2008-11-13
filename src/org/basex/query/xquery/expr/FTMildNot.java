package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;

/**
 * FTMildnot expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTMildNot extends FTExpr {
  /**
   * Constructor.
   * @param l expression list
   */
  public FTMildNot(final FTExpr... l) {
    super(l);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Item it = ctx.iter(expr[0]).next();
    if(!it.bool()) return Dbl.iter(0);
    
    boolean f = false;
    for(int i = 1; i < expr.length; i++) f |= ctx.iter(expr[i]).next().bool();
    return !f || ctx.ftpos.mildNot() ? it.iter() : Dbl.iter(0);
  }

  @Override
  public String toString() {
    return toString(" not in ");
  }
}
