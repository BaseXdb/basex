package org.basex.query.func;

import static org.basex.util.Token.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.basex.io.IO;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.DTd;
import org.basex.query.item.Dat;
import org.basex.query.item.Dtm;
import org.basex.query.item.Item;
import org.basex.query.item.Tim;
import org.basex.query.item.Uri;
import org.basex.util.InputInfo;

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
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

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
        final IO base = ctx.sc.baseIO();
        return base == null ? null : Uri.uri(token(ctx.sc.baseIO().url()));
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
    final Date d = Calendar.getInstance().getTime();
    final String zon = new SimpleDateFormat("Z").format(d);
    final String ymd = new SimpleDateFormat("yyyy-MM-dd").format(d);
    final String hms = new SimpleDateFormat("HH:mm:ss.S").format(d);
    final String zone = zon.substring(0, 3) + ':' + zon.substring(3);
    ctx.date = new Dat(token(ymd + zone), input);
    ctx.time = new Tim(token(hms + zone), input);
    ctx.dtm = new Dtm(token(ymd + 'T' + hms + zone), input);
  }

  /**
   * Returns the current DateTime.
   * @return current date
   */
  private static Item implZone() {
    final Date d = Calendar.getInstance().getTime();
    final String zone = new SimpleDateFormat("Z").format(d);
    final byte[] z = token(zone);
    final int cshift = toInt(substring(z, 0, 3)) * 60 + toInt(substring(z, 3));
    return new DTd(cshift);
  }
}
