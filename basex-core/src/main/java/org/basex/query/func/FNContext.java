package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Context functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNContext extends StandardFunc {
  /**
   * Constructor.
   * @param sctx static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNContext(final StaticContext sctx, final InputInfo info, final Function func,
      final Expr... args) {
    super(sctx, info, func, args);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(func) {
      case CURRENT_DATE:      return ctx.initDateTime().date;
      case CURRENT_DATETIME:  return ctx.initDateTime().dtm;
      case CURRENT_TIME:      return ctx.initDateTime().time;
      case IMPLICIT_TIMEZONE: return ctx.initDateTime().zone;
      case DEFAULT_COLLATION:
        final Collation coll = sc.collation;
        return Uri.uri(coll == null ? QueryText.COLLATIONURI : coll.uri());
      case STATIC_BASE_URI:
        final Uri uri = sc.baseURI();
        return uri == Uri.EMPTY ? null : uri;
      default: return super.item(ctx, ii);
    }
  }
}
