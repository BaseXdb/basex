package org.basex.query.util.format;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.util.Calendar;
import javax.xml.datatype.XMLGregorianCalendar;
import org.basex.query.QueryException;
import org.basex.query.expr.ParseExpr;
import org.basex.query.item.Date;
import org.basex.query.item.Type;
import org.basex.query.util.format.FormatParser.Case;
import org.basex.util.TokenBuilder;
import org.basex.util.locale.Formatter;

/**
 * Date formatter.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class DateFormatter {
  /** Private constructor. */
  private DateFormatter() { }

  /**
   * Formats the specified date.
   * @param e calling expression
   * @param date date to be formatted
   * @param pic picture
   * @param lng language
   * @param cal calendar
   * @param plc place
   * @return formatted string
   * @throws QueryException query exception
   */
  public static byte[] format(final ParseExpr e, final Date date,
      final String pic, final byte[] lng, final byte[] cal, final byte[] plc)
      throws QueryException {

    // ignore calendars and places
    if(cal != null || plc != null);

    final Formatter form = Formatter.get(string(lng));

    final TokenBuilder tb = new TokenBuilder();
    final DateParser fp = new DateParser(e, pic);
    while(fp.more()) {
      final char ch = fp.next();
      if(ch != 0) {
        // print literal
        tb.add(ch);
      } else {
        String m = fp.marker();
        if(m.length() == 0) e.error(PICDATE, pic);
        final int spec = cp(m, 0);
        m = m.substring(1);
        String pres = "1";
        long num = 0;

        final boolean dat = date.type == Type.DAT;
        final boolean tim = date.type == Type.TIM;
        final XMLGregorianCalendar gc = date.xc;
        switch(spec) {
          case 'Y':
            if(tim) e.error(PICCOMP, pic);
            num = gc.getYear();
            break;
          case 'M':
            if(tim) e.error(PICCOMP, pic);
            num = gc.getMonth();
            break;
          case 'D':
            if(tim) e.error(PICCOMP, pic);
            num = gc.getDay();
            break;
          case 'd':
            if(tim) e.error(PICCOMP, pic);
            num = Date.days(0, gc.getMonth(), gc.getDay());
            break;
          case 'F':
            if(tim) e.error(PICCOMP, pic);
            num = gc.toGregorianCalendar().get(Calendar.DAY_OF_WEEK) - 1;
            pres = "n";
            break;
          case 'W':
            num = gc.toGregorianCalendar().get(Calendar.WEEK_OF_YEAR);
            if(tim) e.error(PICCOMP, pic);
            break;
          case 'w':
            num = gc.toGregorianCalendar().get(Calendar.WEEK_OF_MONTH);
            if(tim) e.error(PICCOMP, pic);
            break;
          case 'H':
            if(dat) e.error(PICCOMP, pic);
            num = gc.getHour();
            break;
          case 'h':
            num = gc.getHour() % 12;
            if(dat) e.error(PICCOMP, pic);
            break;
          case 'P':
            if(dat) e.error(PICCOMP, pic);
            num = gc.getHour() / 12;
            pres = "n";
            break;
          case 'm':
            if(dat) e.error(PICCOMP, pic);
            num = gc.getMinute();
            pres = "01";
            break;
          case 's':
            if(dat) e.error(PICCOMP, pic);
            num = gc.getSecond();
            pres = "01";
            break;
          case 'f':
            if(dat) e.error(PICCOMP, pic);
            num = gc.getMillisecond();
            pres = "1";
            break;
          case 'Z':
            num = gc.getTimezone();
            pres = "01:01";
            break;
          case 'z':
            num = gc.getTimezone();
            pres = "01:01";
            break;
          case 'C':
            pres = "n";
            break;
          case 'E':
            num = gc.getYear();
            pres = "n";
            break;
          default:
            e.error(PICDATE, pic);
            break;
        }

        final FormatParser mp = new FormatParser(m, pres, true);
        if(mp.error) e.error(PICDATE, pic);

        if(mp.pres.startsWith("n")) {
          byte[] in = EMPTY;
          if(spec == 'M') {
            in = form.month((int) num - 1, mp.min, mp.max);
          } else if(spec == 'F') {
            in = form.day((int) num, mp.min, mp.max);
          } else if(spec == 'P') {
            in = form.ampm(num == 0);
          } else if(spec == 'C') {
            in = form.calendar();
          } else if(spec == 'E') {
            in = form.era((int) num);
          }
          if(mp.cs == Case.LOWER) in = lc(in);
          if(mp.cs == Case.UPPER) in = uc(in);
          tb.add(in);
        } else {
          tb.add(IntFormatter.format(num, mp, form));
        }
      }
    }
    return tb.finish();
  }
}
