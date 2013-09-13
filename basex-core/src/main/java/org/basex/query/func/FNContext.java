package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
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
      case CURRENT_DATE:      return ctx.initDateTime().date;
      case CURRENT_DATETIME:  return ctx.initDateTime().dtm;
      case CURRENT_TIME:      return ctx.initDateTime().time;
      case IMPLICIT_TIMEZONE: return ctx.initDateTime().zone;
      case DEFAULT_COLLATION:
        final Collation coll = ctx.sc.collation;
        return Uri.uri(coll == null ? QueryText.URLCOLL : coll.uri());
      case STATIC_BASE_URI:
        final Uri uri = ctx.sc.baseURI();
        return uri == Uri.EMPTY ? null : uri;
      default: return super.item(ctx, ii);
    }
  }
}
