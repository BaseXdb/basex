package org.basex.query.expr;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Literal function item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Leonard Woerteler
 */
public class LitFunc extends Single {

  /** Variables. */
  private final Var[] vars;

  /**
   * Constructor.
   * @param ii input info
   * @param e function expression
   * @param arg arguments
   */
  public LitFunc(final InputInfo ii, final Expr e, final Var[] arg) {
    super(ii, e);
    vars = arg;
  }

  @Override
  public String toString() {
    final String str = expr.toString();
    final int par = str.indexOf('(');
    return (par > -1 ? str.substring(0, par) : str) + "#" + vars.length;
  }

}
