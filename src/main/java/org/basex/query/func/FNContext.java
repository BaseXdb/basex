package org.basex.query.func;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.DTd;
import org.basex.query.item.Dat;
import org.basex.query.item.Dtm;
import org.basex.query.item.Item;
import org.basex.query.item.Tim;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Context functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNContext extends FuncCall {
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
    final Iter[] arg = new Iter[expr.length];
    for(int a = 0; a < expr.length; ++a) arg[a] = ctx.iter(expr[a]);

    switch(def) {
      case CURRDATE:  return currDate(ctx);
      case CURRDTM:   return currDTM(ctx);
      case CURRTIME:  return currTIM(ctx);
      case IMPLZONE:  return implZone();
      case COLLAT:    return ctx.baseURI.resolve(ctx.collation);
      case STBASEURI: return ctx.baseURI != Uri.EMPTY ? ctx.baseURI : null;
      default: return super.item(ctx, ii);
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
    final String zone = zon.substring(0, 3) + ":" + zon.substring(3);
    ctx.date = new Dat(Token.token(ymd + zone), input);
    ctx.time = new Tim(Token.token(hms + zone), input);
    ctx.dtm = new Dtm(Token.token(ymd + "T" + hms + zone), input);
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
