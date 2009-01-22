package org.basex.query.func;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.basex.BaseX;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.DTd;
import org.basex.query.item.Dat;
import org.basex.query.item.Dtm;
import org.basex.query.item.Item;
import org.basex.query.item.Tim;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.util.Token;

/**
 * Context functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNContext extends Fun {
  @Override
  public Iter iter(final QueryContext ctx, final Iter[] arg)
      throws QueryException {
    switch(func) {
      case CURRDATE:  return currDate(ctx).iter();
      case CURRDTM:   return currDTM(ctx).iter();
      case CURRTIME:  return currTIM(ctx).iter();
      case IMPLZONE:  return implZone().iter();
      case COLLAT:    return ctx.baseURI.resolve(ctx.collation).iter();
      case STBASEURI: return ctx.baseURI != Uri.EMPTY ? ctx.baseURI.iter() :
        Iter.EMPTY;
      default: BaseX.notexpected(func); return null;
    }
  }

  /**
   * Returns the current Date.
   * @param ctx xquery context
   * @return current date
   * @throws QueryException xquery exception
   */
  private Item currDate(final QueryContext ctx) throws QueryException {
    if(ctx.date != null) return ctx.date;
    final Date d = Calendar.getInstance().getTime();
    final String zone = new SimpleDateFormat("Z").format(d);
    final String form = new SimpleDateFormat("yyyy-MM-dd").format(d);
    ctx.date = new Dat(Token.token(form + zone.substring(0, 3) + ":" +
        zone.substring(3)));
    return ctx.date;
  }

  /**
   * Returns the current DateTime.
   * @param ctx xquery context
   * @return current date
   * @throws QueryException xquery exception
   */
  private Item currDTM(final QueryContext ctx) throws QueryException {
    if(ctx.dtm != null) return ctx.dtm;
    final Date d = Calendar.getInstance().getTime();
    final String zone = new SimpleDateFormat("Z").format(d);
    final String df1 = new SimpleDateFormat("yyyy-MM-dd").format(d);
    final String df2 = new SimpleDateFormat("HH:mm:ss.S").format(d);
    ctx.dtm = new Dtm(Token.token(df1 + "T" + df2 + zone.substring(0, 3) +
        ":" + zone.substring(3)));
    return ctx.dtm;
  }

  /**
   * Returns the current DateTime.
   * @param ctx xquery context
   * @return current date
   * @throws QueryException xquery exception
   */
  private Item currTIM(final QueryContext ctx) throws QueryException {
    if(ctx.time != null) return ctx.time;
    final Date dat = Calendar.getInstance().getTime();
    final String zone = new SimpleDateFormat("Z").format(dat);
    final String form = new SimpleDateFormat("HH:mm:ss.S").format(dat);
    ctx.time = new Tim(Token.token(form + zone.substring(0, 3) + ":" +
        zone.substring(3)));
    return ctx.time;
  }

  /**
   * Returns the current DateTime.
   * @return current date
   */
  private Item implZone() {
    final Date d = Calendar.getInstance().getTime();
    final String zone = new SimpleDateFormat("Z").format(d);
    final byte[] z = Token.token(zone);
    final int cshift = Token.toInt(Token.substring(z, 0, 3)) * 60 +
      Token.toInt(Token.substring(z, 3));
    return new DTd(cshift);
  }
}
