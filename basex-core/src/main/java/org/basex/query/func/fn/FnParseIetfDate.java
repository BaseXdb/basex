package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnParseIetfDate extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final DateParser parser = new DateParser(toEmptyToken(exprs[0], qc), info);
    return parser.parse();
  }

  /** Date parser. */
  static class DateParser extends InputParser {
    /** Days. */
    private static final String[] DAYS = {
      "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday",
      "mon", "tue", "wed", "thu", "fri", "sat", "sun"
    };
    /** Months. */
    private static final String[] MONTHS = {
      "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"
    };
    /** Timezones. */
    private static final String[] TZNAMES = {
      "ut", "utc", "gmt", "est", "edt", "cst", "cdt", "mst", "mdt", "pst", "pdt"
    };
    /** Associated times. */
    private static final int[] TIMES = {
      0, 0, 0, -300, -240, -360, -300, -420, -360, -480, -420
    };

    /** Original string. */
    private final byte[] original;
    /** Input info. */
    private final InputInfo info;

    /** Day. */
    private int day;
    /** Month. */
    private int month;
    /** Year. */
    private int year;
    /** Hours. */
    private int hours;
    /** Minutes. */
    private int minutes;
    /** Seconds. */
    private double seconds;
    /** Timezone. */
    private int zone;

    /**
     * Constructor.
     * @param input input
     * @param info input info
     */
    DateParser(final byte[] input, final InputInfo info) {
      super(Token.string(lc(input)).trim());
      original = input;
      this.info = info;
    }

    /**
     * Parses the input and returns a dateTime item.
     * @return dateTime item
     * @throws QueryException query exception
     */
    private Dtm parse() throws QueryException {
      input();
      final TokenBuilder tb = new TokenBuilder();
      addNumber(tb, year, 4).add('-');
      addNumber(tb, month, 2).add('-');
      addNumber(tb, day, 2).add('T');
      addNumber(tb, hours, 2).add(':');
      addNumber(tb, minutes, 2).add(':');
      final int sec = (int) seconds;
      addNumber(tb, sec, 2);
      if(sec != seconds) tb.add('.').addExt(seconds - sec);
      if(zone != 0) {
        tb.add(zone < 0 ? '-' : '+');
        addNumber(tb, Math.abs(zone / 60), 2).add(':');
        addNumber(tb, Math.abs(zone % 60), 2);
      } else {
        tb.add('Z');
      }
      return new Dtm(tb.finish(), info);
    }

    /**
     * Parses the input.
     * @throws QueryException query exception
     */
    private void input() throws QueryException {
      for(final String d : DAYS) if(consume(d)) break;
      consume(',');
      if(!skipWs()) throw error("whitespace");
      if(datespec()) {
        if(!skipWs()) throw error("whitespace");
        if(!time()) throw error("time");
      } else if(!asctime()) {
        throw error("day, or name of month");
      }
      if(more()) throw error("end of input");
    }

    /**
     * Parses a date.
     * @return success flag
     * @throws QueryException query exception
     */
    private boolean datespec() throws QueryException {
      if(!daynum()) return false;
      if(!dsep()) throw error("'-'");
      if(!monthname()) throw error("name of month");
      if(!dsep()) throw error("'-'");
      if(!year()) throw error("year");
      return true;
    }

    /**
     * Parses a time and date.
     * @return success flag
     * @throws QueryException query exception
     */
    private boolean asctime() throws QueryException {
      if(!monthname()) return false;
      if(!dsep()) throw error("'-'");
      if(!daynum()) throw error("day");
      if(!skipWs()) throw error("whitespace");
      if(!time()) throw error("time");
      if(!skipWs()) throw error("whitespace");
      if(!year()) throw error("year");
      return true;
    }

    /**
     * Parses a time.
     * @return success flag
     * @throws QueryException query exception
     */
    private boolean time() throws QueryException {
      if(!hours()) return false;
      if(!consume(':') || !minutes()) throw error("minutes");
      if(consume(':') && !seconds()) throw error("seconds");
      skipWs();
      timezone();
      return true;
    }

    /**
     * Parses a day.
     * @return success flag
     */
    private boolean daynum() {
      if(!digit(curr())) return false;
      final int d = number();
      day = digit(curr()) ? d * 10 + number() : d;
      return true;
    }

    /**
     * Parses a separator.
     * @return success flag
     */
    private boolean dsep() {
      final boolean s = skipWs();
      if(consume('-')) {
        skipWs();
        return true;
      }
      return s;
    }

    /**
     * Parses a month.
     * @return success flag
     */
    private boolean monthname() {
      final int ml = MONTHS.length;
      int m = -1;
      while(++m < ml && !consume(MONTHS[m]));
      if(m == ml) return false;
      month = m + 1;
      return true;
    }

    /**
     * Parses a year.
     * @return success flag
     */
    private boolean year() {
      final int d1 = twoDigits();
      if(d1 == -1) return false;
      final int d2 = twoDigits();
      year = d2 == -1 ? d1 + 1900 : d1 * 100 + d2;
      return true;
    }

    /**
     * Parses hours.
     * @return success flag
     */
    private boolean hours() {
      final int d = twoDigits();
      if(d == -1) return false;
      hours = d;
      return true;
    }

    /**
     * Parses minutes.
     * @return success flag
     */
    private boolean minutes() {
      final int d = twoDigits();
      if(d == -1) return false;
      minutes = d;
      return true;
    }

    /**
     * Parses seconds.
     * @return success flag
     * @throws QueryException query exception
     */
    private boolean seconds() throws QueryException {
      double d = twoDigits();
      if(d == -1) return false;
      if(consume('.')) {
        if(!digit(curr())) throw error("digit");
        long f = 10;
        while(digit(curr())) {
          d += number() / f;
          f *= 10;
        }
      }
      seconds = d;
      return true;
    }

    /**
     * Parses a timezone.
     * @return success flag
     * @throws QueryException query exception
     */
    private boolean timezone() throws QueryException {
      if(tzname()) return true;
      if(!tzoffset()) return false;
      skipWs();
      if(consume('(')) {
        skipWs();
        if(!tzname()) throw error("timezone");
        skipWs();
        if(!consume(')')) throw error("')'");
      }
      return true;
    }

    /**
     * Parses a timezone name.
     * @return success flag
     */
    private boolean tzname() {
      final int tl = TZNAMES.length;
      int t = -1;
      while(++t < tl && !consume(TZNAMES[t]));
      if(t == tl) return false;
      zone = TIMES[t];
      return true;
    }

    /**
     * Parses a timezone offset.
     * @return success flag
     * @throws QueryException query exception
     */
    private boolean tzoffset() throws QueryException {
      final int m = consume('-') ? -1 : 1;
      if(m == 1 && !consume('+')) throw error("'+' or '-'");
      final int d = twoDigits();
      if(d == -1) throw error("timezone digits");
      zone = m * d * 60;
      consume(':');
      final int n = twoDigits();
      if(n != -1) zone += m * n;
      return true;
    }

    /**
     * Parses two digits.
     * @return number or {@code -1}
     */
    private int twoDigits() {
      return digit(curr()) && digit(next()) ? number() * 10 + number() : -1;
    }

    /**
     * Consumes a whitespace character.
     * @return success flag
     */
    private boolean skipWs() {
      if(!more() || !ws(curr())) return false;
      consume();
      return true;
    }

    /**
     * Adds a number to a string.
     * @param tb token builder
     * @param num number
     * @param digits minimum number of digits to add
     * @return token builder
     */
    private TokenBuilder addNumber(final TokenBuilder tb, final int num, final int digits) {
      final byte[] tmp = token(num);
      for(int n = tmp.length; n < digits; n++) tb.add('0');
      return tb.add(tmp);
    }

    /**
     * Returns an error.
     * @param msg message
     * @return error
     */
    private QueryException error(final String msg) {
      return Err.IETF_X_X_X.get(info, msg, curr(), original);
    }

    /**
     * Consumes a digit and returns the numeric representation.
     * @return number
     */
    private int number() {
      return consume() - '0';
    }
  }
}
