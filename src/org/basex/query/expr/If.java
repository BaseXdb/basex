package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

/**
 * If expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
  public Iter iter(final QueryContext ctx) throws QueryException {
    return ctx.iter(expr[expr[0].ebv(ctx).bool() ? 1 : 2]);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    if(!expr[0].i()) return this;
    // static result: return then or else branch
    final Expr e = expr[((Item) expr[0]).bool() ? 1 : 2];
    ctx.compInfo(OPTSIMPLE, this, e);
    return e;
  }

  @Override
  public Return returned(final QueryContext ctx) {
    final Return ret = expr[1].returned(ctx);
    return ret == expr[2].returned(ctx) ? ret : Return.SEQ;
  }

  @Override
  public String toString() {
    return "if " + expr[0] + " then " + expr[1] + " else " + expr[2];
  }
}
