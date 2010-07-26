package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;

/**
 * Switch expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Switch extends Arr {
  /**
   * Constructor.
   * @param e expressions
   */
  public Switch(final Expr[] e) {
    super(e);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);

    Expr e = this;
    if(expr[0].item()) {
      final Item it = (Item) expr[0];
      final int el = expr.length;
      boolean items = true;
      for(int i = 1; i < el - 1; i += 2) {
        items &= expr[i].item();
        if(items && it.equiv((Item) expr[i])) {
          e = expr[i + 1];
          break;
        }
      }
      if(items && e == this) e = expr[el - 1];
    }
    if(e != this) ctx.compInfo(OPTPRE, SWITCH + "(" + expr[0] + ")");
    return e;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Item op = expr[0].atomic(ctx);
    final int el = expr.length;
    for(int i = 1; i < el - 1; i += 2) {
      final Item cs = expr[i].atomic(ctx);
      // includes check for empty sequence (null reference)
      if(op == cs || op.equiv(cs)) return ctx.iter(expr[i + 1]);
    }
    // choose default expression
    return ctx.iter(expr[el - 1]);
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    final int el = expr.length;
    final SeqType ret = expr[el - 1].returned(ctx);
    for(int i = 1; i < el - 1; i += 2) {
      if(!ret.eq(expr[i].returned(ctx))) return SeqType.ITEM_ZM;
    }
    return ret;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(SWITCH + "(" + expr[0] + ")");
    final int el = expr.length;
    for(int i = 1; i < el; i++) {
      sb.append(" " + (i + 1 < el ? CASE + ' ' + expr[i++] : DEFAULT));
      sb.append(" " + RETURN + " " + expr[i]);
    }
    return sb.toString();
  }
}
