package org.basex.query.xquery.expr;

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
   * @param l expression list
   */
  public And(final Expr[] l) {
    super(l);
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    int el = expr.length;
    for(int e = 0; e < el; e++) {
      expr[e] = ctx.comp(expr[e]);
      if(!expr[e].i()) continue;
      if(!((Item) expr[e]).bool()) return Bln.FALSE;
      Array.move(expr, e + 1, -1, --el - e);
      --e;
    }
    return el == expr.length ? this : el == 0 ?  Bln.TRUE :
      new And(Array.finish(expr, el));
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    double d = 0;
    for(final Expr e : expr) {
      final Item it = ctx.iter(e).ebv();
      if(!it.bool()) return Bln.FALSE.iter();
      d = Scoring.and(d, it.score());
    }
    return (d == 0 ? Bln.TRUE : new Bln(true, d)).iter();
  }

  @Override
  public String toString() {
    return toString(" and ");
  }
}
