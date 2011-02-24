package org.basex.query.expr;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FunItem;
import org.basex.query.item.FunType;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Literal function item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Leonard Woerteler
 */
public class LitFunc extends Func {

  /** Variables. */
  private final Var[] vars;
  /** Function name. */
  private final QNm name;

  /**
   * Constructor.
   * @param ii input info
   * @param n function name
   * @param e function expression
   * @param arg arguments
   */
  public LitFunc(final InputInfo ii, final QNm n, final Expr e,
      final Var[] arg) {
    super(ii, new Var(ii, new QNm(), e.type()), arg, true);
    vars = arg;
    expr = e;
    name = n;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);

    final SeqType[] at = new SeqType[args.length];
    for(int i = 0; i < at.length; i++)
      at[i] = args[i].type == null ? SeqType.ITEM_ZM : args[i].type;

    return new FunItem(name, args, expr, FunType.get(at, var.type()));
  }

  @Override
  public String toString() {
    final String str = expr.toString();
    final int par = str.indexOf('(');
    return (par > -1 ? str.substring(0, par) : str) + "#" + vars.length;
  }

}
