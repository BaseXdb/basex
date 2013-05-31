package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Context functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNContext extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNContext(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case CURRENT_DATE:      return ctx.initDateTime(info).date;
      case CURRENT_DATETIME:  return ctx.initDateTime(info).dtm;
      case CURRENT_TIME:      return ctx.initDateTime(info).time;
      case IMPLICIT_TIMEZONE: return ctx.initDateTime(info).zone;
      case DEFAULT_COLLATION: return ctx.sc.baseURI().resolve(ctx.sc.collation, info);
      case STATIC_BASE_URI:
        final Uri uri = ctx.sc.baseURI();
        return uri == Uri.EMPTY ? null : uri;
      default: return super.item(ctx, ii);
    }
  }
}
