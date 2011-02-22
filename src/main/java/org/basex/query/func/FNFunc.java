package org.basex.query.func;

import static org.basex.query.util.Err.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;

/**
 * Functions on functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class FNFunc extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNFunc(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(def) {
      case FILTER:
      case MAP:
      case MAPPAIRS:
      case FOLDLEFT:
      case FOLDRIGHT:
        NOTIMPL.thrw(input, def.desc);
        return null;
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case FUNCNAME:
      case FUNCARITY:
      case PARTAPP:
        NOTIMPL.thrw(input, def.desc);
        return null;
      default:
        return super.item(ctx, ii);
    }
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 || super.uses(u);
  }
}
