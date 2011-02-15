package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;

/**
 * Switch expression.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Switch extends Arr {
  /**
   * Constructor.
   * @param ii input info
   * @param e expressions
   */
  public Switch(final InputInfo ii, final Expr[] e) {
    super(ii, e);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);

    Expr e = this;
    if(expr[0].value()) {
      final Item it = expr[0].item(ctx, input);
      final int el = expr.length;
      boolean vals = true;
      for(int i = 1; i < el - 1; i += 2) {
        vals &= expr[i].value();
        if(!vals) break;
        final Item cs = expr[i].item(ctx, input);
        if(it == cs || cs != null && it != null && it.equiv(input, cs)) {
          e = expr[i + 1];
          break;
        }
      }
      if(vals && e == this) e = expr[el - 1];
    }

    if(e == this) {
      final int el = expr.length;
      type = expr[el - 1].type();
      for(int i = 1; i < el - 1; i += 2) type = type.intersect(expr[i].type());
    }
    return optPre(e, ctx);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Item it = expr[0].item(ctx, input);
    final int el = expr.length;
    for(int i = 1; i < el - 1; i += 2) {
      final Item cs = expr[i].item(ctx, input);
      // includes check for empty sequence (null reference)
      if(it == cs || it != null && cs != null && it.equiv(input, cs))
        return ctx.iter(expr[i + 1]);
    }
    // choose default expression
    return ctx.iter(expr[el - 1]);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(SWITCH + PAR1 + expr[0] + PAR2);
    final int el = expr.length;
    for(int i = 1; i < el; ++i) {
      sb.append(" " + (i + 1 < el ? CASE + ' ' + expr[i++] : DEFAULT));
      sb.append(" " + RETURN + " " + expr[i]);
    }
    return sb.toString();
  }
}
