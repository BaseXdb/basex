package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.iter.Iter;

/**
 * If expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class If extends Arr {
  /**
   * Constructor.
   * @param e expression
   * @param t then clause
   * @param s else clause
   */
  public If(final Expr e, final Expr t, final Expr s) {
    super(e, t, s);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].comp(ctx);
    List.updating(ctx, new Expr[] { expr[1], expr[2] });

    Expr e = this;
    if(checkUp(expr[0], ctx).i()) {
      // static result: return then or else branch
      e = expr[((Item) expr[0]).bool() ? 1 : 2];
    } else if(expr[1].e() && expr[2].e()) {
      // both branches are empty
      e = Seq.EMPTY;
    }
    if(e != this) ctx.compInfo(OPTPRE, this);
    return e;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return ctx.iter(expr[expr[0].ebv(ctx).bool() ? 1 : 2]);
  }

  @Override
  public Return returned(final QueryContext ctx) {
    final Return ret = expr[1].returned(ctx);
    return ret == expr[2].returned(ctx) ? ret : Return.SEQ;
  }

  @Override
  public String toString() {
    return IF + " " + expr[0] + " " + THEN + " " + expr[1] + " " +
      ELSE + " " + expr[2];
  }
}
