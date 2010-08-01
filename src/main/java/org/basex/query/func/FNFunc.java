package org.basex.query.func;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryInfo;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

/**
 * Functions on functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNFunc extends Fun {
  /**
   * Constructor.
   * @param i query info
   * @param f function definition
   * @param e arguments
   */
  protected FNFunc(final QueryInfo i, final FunDef f, final Expr... e) {
    super(i, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case FILTER:
      case MAP:
      case MAPPAIRS:
      case FOLDLEFT:
      case FOLDRIGHT:
        error(NOTIMPL, func.desc);
        return null;
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    switch(func) {
      case FUNCNAME:
      case FUNCARITY:
        error(NOTIMPL, func.desc);
        return null;
      default:
        return super.atomic(ctx);
    }
  }
}
