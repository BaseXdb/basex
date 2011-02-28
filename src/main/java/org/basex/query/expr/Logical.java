package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.func.FunDef;
import org.basex.query.item.Bln;
import org.basex.query.item.SeqType;
import org.basex.util.Array;
import org.basex.util.InputInfo;

/**
 * Logical expression.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class Logical extends Arr {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression list
   */
  Logical(final InputInfo ii, final Expr[] e) {
    super(ii, e);
    type = SeqType.BLN;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);

    final boolean and = this instanceof And;
    for(int e = 0; e < expr.length; ++e) {
      expr[e] = expr[e].compEbv(ctx);
      if(!expr[e].value()) continue;

      // atomic items can be pre-evaluated
      ctx.compInfo(OPTREMOVE, desc(), expr[e]);
      if(expr[e].ebv(ctx, input).bool(input) ^ and) return Bln.get(!and);
      expr = Array.delete(expr, e--);
    }
    return expr.length == 0 ? Bln.get(and) : this;
  }

  /**
   * Returns an equivalent for the logical expression, assuming that only
   * one operand exists.
   * @return resulting expression
   */
  protected final Expr single() {
    return expr[0].type().eq(SeqType.BLN) ? expr[0] :
      FunDef.BOOLEAN.get(input, expr[0]);
  }
}
