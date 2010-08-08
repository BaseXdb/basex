package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;

/**
 * If expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class If extends Arr {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param t then clause
   * @param s else clause
   */
  public If(final InputInfo ii, final Expr e, final Expr t, final Expr s) {
    super(ii, e, t, s);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].comp(ctx);
    checkUp(ctx, expr[1], expr[2]);

    Expr e = this;
    if(checkUp(expr[0], ctx).value()) {
      // static result: return then or else branch
      e = expr[((Item) expr[0]).ebv(ctx, input).bool(input) ? 1 : 2];
    } else if(expr[1].empty() && expr[2].empty()) {
      // both branches are empty
      e = Seq.EMPTY;
    }
    return optPre(e, ctx);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return ctx.iter(expr[expr[0].ebv(ctx, input).bool(input) ? 1 : 2]);
  }
  
  @Override
  public SeqType returned(final QueryContext ctx) {
    final SeqType ret = expr[1].returned(ctx);
    return ret.eq(expr[2].returned(ctx)) ? ret : SeqType.ITEM_ZM;
  }

  @Override
  public String toString() {
    return IF + '(' + expr[0] + ") " + THEN + ' ' + expr[1] + ' ' +
      ELSE + ' ' + expr[2];
  }
}
