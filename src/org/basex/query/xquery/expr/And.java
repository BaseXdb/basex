package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Scoring;
import org.basex.util.Array;

/**
 * And expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class And extends Arr {
  /**
   * Constructor.
   * @param e expression list
   */
  public And(final Expr[] e) {
    super(e);
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    for(int e = 0; e < expr.length; e++) {
      expr[e] = ctx.comp(expr[e]);
      if(!expr[e].i()) continue;
      if(!((Item) expr[e]).bool()) {
        // atomic items can be pre-evaluated
        ctx.compInfo(OPTFALSE, expr[e]);
        return Bln.FALSE;
      }
      ctx.compInfo(OPTTRUE, expr[e]);
      expr = Array.delete(expr, e--);
    }
    return expr.length == 0 ? Bln.TRUE : this;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    double d = 0;
    for(final Expr e : expr) {
      final Item it = ctx.iter(e).ebv();
      if(!it.bool()) return Bln.FALSE.iter();
      d = Scoring.and(d, it.score());
    }
    return (d == 0 ? Bln.TRUE : Bln.get(d)).iter();
  }

  @Override
  public String toString() {
    return toString(" and ");
  }
}
