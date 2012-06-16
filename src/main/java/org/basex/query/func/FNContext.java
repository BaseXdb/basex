package org.basex.query.func;

import static org.basex.util.Token.*;

import java.text.*;
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
        return implZone();
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
   * Returns the current DateTime.
   * @param ctx query context
   * @return current date
   * @throws QueryException query exception
   */
  private Item currDTM(final QueryContext ctx) throws QueryException {
    if(ctx.dtm == null) initDateTime(ctx);
    return ctx.dtm;
  }

  /**
   * Returns the current DateTime.
   * @param ctx query context
   * @return current date
   * @throws QueryException query exception
   */
  private Item currTIM(final QueryContext ctx) throws QueryException {
    if(ctx.time == null) initDateTime(ctx);
    return ctx.time;
  }


  /**
   * Initializes the static date and time context of a query.
   * @param ctx query context
   * @throws QueryException query exception
   */
  private void initDateTime(final QueryContext ctx) throws QueryException {
    final java.util.Date d = Calendar.getInstance().getTime();
    final String zon = new SimpleDateFormat("Z").format(d);
    final String ymd = new SimpleDateFormat("yyyy-MM-dd").format(d);
    final String hms = new SimpleDateFormat("HH:mm:ss.S").format(d);
    final String zone = zon.substring(0, 3) + ':' + zon.substring(3);
    ctx.date = new Dat(token(ymd + zone), info);
    ctx.time = new Tim(token(hms + zone), info);
    ctx.dtm = new Dtm(token(ymd + 'T' + hms + zone), info);
  }

  /**
   * Returns the current DateTime.
   * @return current date
   */
  private static Item implZone() {
    final java.util.Date d = Calendar.getInstance().getTime();
    final String zone = new SimpleDateFormat("Z").format(d);
    final byte[] z = token(zone);
    final int cshift = toInt(substring(z, 0, 3)) * 60 + toInt(substring(z, 3));
    return new DTd(cshift);
  }
}
