package org.basex.query.func;

import static org.basex.util.Token.*;

import java.util.*;

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
      case CURRENT_DATE:
        return currDate(ctx);
      case CURRENT_DATETIME:
        return currDTM(ctx);
      case CURRENT_TIME:
        return currTIM(ctx);
      case IMPLICIT_TIMEZONE:
        return currZone(ctx);
      case DEFAULT_COLLATION:
        return ctx.sc.baseURI().resolve(ctx.sc.collation);
      case STATIC_BASE_URI:
        final Uri uri = ctx.sc.baseURI();
        return uri == Uri.EMPTY ? null : uri;
      default:
        return super.item(ctx, ii);
    }
  }

  /**
   * Returns the current date.
   * @param ctx query context
   * @return current date
   * @throws QueryException query exception
   */
  private Item currDate(final QueryContext ctx) throws QueryException {
    if(ctx.date == null) initDateTime(ctx);
    return ctx.date;
  }

  /**
   * Returns the current dateTime.
   * @param ctx query context
   * @return current date
   * @throws QueryException query exception
   */
  private Item currDTM(final QueryContext ctx) throws QueryException {
    if(ctx.dtm == null) initDateTime(ctx);
    return ctx.dtm;
  }

  /**
   * Returns the current time.
   * @param ctx query context
   * @return current dateTime
   * @throws QueryException query exception
   */
  private Item currTIM(final QueryContext ctx) throws QueryException {
    if(ctx.time == null) initDateTime(ctx);
    return ctx.time;
  }

  /**
   * Returns the current timezone.
   * @param ctx query context
   * @return current timezone
   * @throws QueryException query exception
   */
  private Item currZone(final QueryContext ctx) throws QueryException {
    if(ctx.zone == null) initDateTime(ctx);
    return ctx.zone;
  }

  /**
   * Initializes the static date and time context of a query.
   * @param ctx query context
   * @throws QueryException query exception
   */
  private void initDateTime(final QueryContext ctx) throws QueryException {
    final Date d = Calendar.getInstance().getTime();
    final String zon = DateTime.format(d, DateTime.ZONE);
    final String ymd = DateTime.format(d, DateTime.DATE);
    final String hms = DateTime.format(d, DateTime.TIME);
    final String zone = zon.substring(0, 3) + ':' + zon.substring(3);
    ctx.time = new Tim(Token.token(hms + zone), info);
    ctx.date = new Dat(Token.token(ymd + zone), info);
    ctx.dtm = new Dtm(Token.token(ymd + 'T' + hms + zone), info);
    ctx.zone = new DTDur(toInt(zon.substring(0, 3)) * 60 + toInt(zon.substring(3)));
  }
}
