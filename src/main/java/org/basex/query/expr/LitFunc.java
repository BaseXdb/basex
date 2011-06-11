package org.basex.query.expr;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FuncItem;
import org.basex.query.item.FuncType;
import org.basex.query.item.QNm;
import org.basex.query.util.TypedFunc;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Literal function item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public final class LitFunc extends UserFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param n function name
   * @param f function expression
   * @param arg arguments
   */
  public LitFunc(final InputInfo ii, final QNm n, final TypedFunc f,
      final Var[] arg) {
    super(ii, n, f.type.type(arg), f.ret(), true);
    expr = f.fun;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);

    final FuncType ft = FuncType.get(this);
    for(final Var v : args) v.type = null;
    return new FuncItem(name, args, expr, ft, false);
  }

  @Override
  public String toString() {
    final String str = expr.toString();
    final int par = str.indexOf('(');
    return (par > -1 ? str.substring(0, par) : str) + "#" + args.length;
  }
}
