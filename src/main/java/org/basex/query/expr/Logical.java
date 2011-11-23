package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.SeqType;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.list.ObjList;

/**
 * Logical expression, extended by {@link And} and {@link Or}.
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
    for(final Expr e : expr) checkUp(e, ctx);

    final boolean and = this instanceof And;
    for(int e = 0; e < expr.length; ++e) {
      expr[e] = expr[e].comp(ctx).compEbv(ctx);
      if(!expr[e].isValue()) continue;

      // atomic items can be pre-evaluated
      ctx.compInfo(OPTREMOVE, desc(), expr[e]);
      if(expr[e].ebv(ctx, input).bool(input) ^ and) return Bln.get(!and);
      expr = Array.delete(expr, e--);
    }
    return expr.length == 0 ? Bln.get(and) : this;
  }

  /**
   * Flattens nested logical expressions.
   * @param ctx query context
   */
  protected final void compFlatten(final QueryContext ctx) {
    // flatten nested expressions
    final ObjList<Expr> tmp = new ObjList<Expr>(expr.length);
    for(int p = 0; p < expr.length; ++p) {
      if(expr[p].getClass().isInstance(this)) {
        for(final Expr e : ((Logical) expr[p]).expr) tmp.add(e);
        ctx.compInfo(OPTFLAT, expr[p]);
      } else {
        tmp.add(expr[p]);
      }
    }
    if(expr.length != tmp.size()) expr = tmp.toArray(new Expr[tmp.size()]);
  }
}
