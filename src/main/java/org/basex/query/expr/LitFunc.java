package org.basex.query.expr;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FunItem;
import org.basex.query.item.FunType;
import org.basex.query.item.QNm;
import org.basex.query.util.TypedFunc;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Literal function item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Leonard Woerteler
 */
public class LitFunc extends Func {

  /** Function name. */
  private final QNm name;

  /**
   * Constructor.
   * @param ii input info
   * @param n function name
   * @param f function expression
   * @param arg arguments
   */
  public LitFunc(final InputInfo ii, final QNm n, final TypedFunc f,
      final Var[] arg) {
    super(ii, new Var(ii, new QNm(), f.ret()), f.type.type(arg), true);

    expr = f.fun;
    name = n;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);

    return new FunItem(name, args, expr, FunType.get(this));
  }

  @Override
  public String toString() {
    final String str = expr.toString();
    final int par = str.indexOf('(');
    return (par > -1 ? str.substring(0, par) : str) + "#" + args.length;
  }

}
