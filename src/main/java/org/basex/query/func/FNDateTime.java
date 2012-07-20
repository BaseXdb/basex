package org.basex.query.func;

import static org.basex.util.Token.*;

import java.text.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * DateTime functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNDateTime extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNDateTime(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _DATETIME_CURRENT_TIME:     return dateTime(info)[0];
      case _DATETIME_CURRENT_DATE:     return dateTime(info)[1];
      case _DATETIME_CURRENT_DATETIME: return dateTime(info)[2];
      case _DATETIME_TIMESTAMP:        return Int.get(System.currentTimeMillis());
      default:                         return super.item(ctx, ii);
    }
  }

  /**
   * Returns the current dates and times.
   * @param info input info
   * @return dates and times
   * @throws QueryException query exception
   */
  static Item[] dateTime(final InputInfo info) throws QueryException {
    final java.util.Date d = Calendar.getInstance().getTime();
    final String zon = new SimpleDateFormat("Z").format(d);
    final String ymd = new SimpleDateFormat("yyyy-MM-dd").format(d);
    final String hms = new SimpleDateFormat("HH:mm:ss.S").format(d);
    final String zone = zon.substring(0, 3) + ':' + zon.substring(3);
    return new Item[] {
        new Tim(token(hms + zone), info),
        new Dat(token(ymd + zone), info),
        new Dtm(token(ymd + 'T' + hms + zone), info)
    };
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.NDT || super.uses(u);
  }
}
