package org.basex.query.func;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.util.json.JSONParser;
import org.basex.util.InputInfo;

/**
 * Project specific functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNJson extends FuncCall {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNJson(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case JPARSE:
        return new JSONParser(checkStr(expr[0], ctx), input).parse();
      case JSERIALIZE:
        checkNode(expr[0].item(ctx, input));
        return null;
      default:
        return super.item(ctx, ii);
    }
  }
}
