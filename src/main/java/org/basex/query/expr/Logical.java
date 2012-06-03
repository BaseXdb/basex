package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Logical expression, extended by {@link And} and {@link Or}.
 *
 * @author BaseX Team 2005-12, BSD License
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
  public Expr compile(final QueryContext ctx) throws QueryException {
    super.compile(ctx);
    final boolean and = this instanceof And;
    for(int e = 0; e < expr.length; e++) {
      expr[e] = expr[e].compEbv(ctx);
      if(!expr[e].isValue()) continue;

      // atomic items can be pre-evaluated
      ctx.compInfo(OPTREMOVE, description(), expr[e]);
      if(expr[e].ebv(ctx, info).bool(info) ^ and) return Bln.get(!and);
      expr = Array.delete(expr, e--);
    }
    return expr.length == 0 ? Bln.get(and) : this;
  }

  /**
   * Flattens nested logical expressions.
   * @param ctx query context
   */
  final void compFlatten(final QueryContext ctx) {
    // flatten nested expressions
    final ArrayList<Expr> tmp = new ArrayList<Expr>(expr.length);
    for(final Expr ex : expr) {
      if(ex.getClass().isInstance(this)) {
        Collections.addAll(tmp, ((Logical) ex).expr);
        ctx.compInfo(OPTFLAT, ex);
      } else {
        tmp.add(ex);
      }
    }
    if(expr.length != tmp.size()) expr = tmp.toArray(new Expr[tmp.size()]);
  }
}
