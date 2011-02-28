package org.basex.query.util.format;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import java.util.Calendar;
import javax.xml.datatype.XMLGregorianCalendar;
import org.basex.query.QueryException;
import org.basex.query.item.Date;
import org.basex.query.item.Type;
import org.basex.query.util.format.FormatParser.Case;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Date formatter.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DateFormatter {
  /** Private constructor. */
  private DateFormatter() { }

  /**
   * Formats the specified date.
   * @param date date to be formatted
   * @param pic picture
   * @param lng language
   * @param cal calendar
   * @param plc place
   * @param ii input info
   * @return formatted string
   * @throws QueryException query exception
   */
  public static byte[] format(final Date date, final String pic,
      final byte[] lng, final byte[] cal, final byte[] plc, final InputInfo ii)
      throws QueryException {

    // [CG] XQuery/Formatter: currently, calendars and places are ignored
    if(cal != null || plc != null);

    final Formatter form = Formatter.get(string(lng));

    final TokenBuilder tb = new TokenBuilder();
    final DateParser fp = new DateParser(ii, pic);
    while(fp.more()) {
      final char ch = fp.next();
      if(ch != 0) {
        // print literal
        tb.add(ch);
      } else {
        String m = fp.marker();
        if(m.isEmpty()) PICDATE.thrw(ii, pic);
        final int spec = cp(m, 0);
        m = m.substring(1);
        String pres = "1";
        long num = 0;

        final boolean dat = date.type == Type.DAT;
        final boolean tim = date.type == Type.TIM;
        final XMLGregorianCalendar gc = date.xc;
        boolean err = false;
        switch(spec) {
          case 'Y':
            num = gc.getYear();
            err = tim;
            break;
          case 'M':
            num = gc.getMonth();
            err = tim;
            break;
          case 'D':
            num = gc.getDay();
            err = tim;
            break;
          case 'd':
            num = Date.days(0, gc.getMonth(), gc.getDay());
            err = tim;
            break;
          case 'F':
            num = gc.toGregorianCalendar().get(Calendar.DAY_OF_WEEK) - 1;
            pres = "n";
            err = tim;
            break;
          case 'W':
            num = gc.toGregorianCalendar().get(Calendar.WEEK_OF_YEAR);
            err = tim;
            break;
          case 'w':
            num = gc.toGregorianCalendar().get(Calendar.WEEK_OF_MONTH);
            err = tim;
            break;
          case 'H':
            num = gc.getHour();
            err = dat;
            break;
          case 'h':
            num = gc.getHour() % 12;
            err = dat;
            break;
          case 'P':
            num = gc.getHour() / 12;
            pres = "n";
            err = dat;
            break;
          case 'm':
            num = gc.getMinute();
            pres = "01";
            err = dat;
            break;
          case 's':
            num = gc.getSecond();
            pres = "01";
            err = dat;
            break;
          case 'f':
            num = gc.getMillisecond();
            pres = "1";
            err = dat;
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
            err = true;
            break;
        }
        if(err) PICCOMP.thrw(ii, pic);

        final FormatParser mp = new FormatParser(m, pres, true);
        if(mp.error) PICDATE.thrw(ii, pic);

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
