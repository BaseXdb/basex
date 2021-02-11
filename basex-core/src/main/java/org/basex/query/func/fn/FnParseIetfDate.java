package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnParseIetfDate extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = exprs[0].atomItem(qc, info);
    return item == Empty.VALUE ? Empty.VALUE : new DateParser(toToken(item), info).parse();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }

  /** Date parser. */
  private static final class DateParser extends InputParser {
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
      "utc", "ut", "gmt", "est", "edt", "cst", "cdt", "mst", "mdt", "pst", "pdt"
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
    private BigDecimal seconds = BigDecimal.ZERO;
    /** Timezone. */
    private Integer zone;

    /**
     * Constructor.
     * @param input input
     * @param info input info
     */
    private DateParser(final byte[] input, final InputInfo info) {
      super(string(lc(input)).trim());
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
      final int sec = seconds.intValue();
      addNumber(tb, sec, 2);
      if(seconds.scale() > 0) {
        tb.add(seconds.subtract(BigDecimal.valueOf(sec)).toString().substring(1));
      }
      final int z = zone == null ? 0 : zone;
      if(z == 0) {
        tb.add('Z');
      } else {
        tb.add(z < 0 ? '-' : '+');
        addNumber(tb, Math.abs(z / 60), 2).add(':');
        addNumber(tb, Math.abs(z % 60), 2);
      }
      try {
        return new Dtm(tb.finish(), info);
      } catch(final QueryException ex) {
        Util.debug(ex);
        throw QueryError.IETF_INV_X.get(info, original);
      }
    }

    /**
     * Parses the input.
     * @throws QueryException query exception
     */
    private void input() throws QueryException {
      skipWs();
      boolean f = false;
      for(final String d : DAYS) {
        if(consume(d)) {
          f = true;
          break;
        }
      }
      consume(',');
      if(f && !skipWs()) throw error("whitespace");
      if(datespec()) {
        if(!skipWs()) throw error("whitespace");
        if(!time()) throw error("time");
      } else if(!asctime()) {
        throw error("day, or name of month");
      }
      skipWs();
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
      final int ip = pos;
      skipWs();
      if(!timezone()) pos = ip;
      return true;
    }

    /**
     * Parses a day.
     * @return success flag
     */
    private boolean daynum() {
      day = oneOrTwoDigits();
      return day != -1;
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
      hours = oneOrTwoDigits();
      return hours != -1;
    }

    /**
     * Parses minutes.
     * @return success flag
     */
    private boolean minutes() {
      minutes = twoDigits();
      return minutes != -1;
    }

    /**
     * Parses seconds.
     * @return success flag
     * @throws QueryException query exception
     */
    private boolean seconds() throws QueryException {
      final int d = twoDigits();
      if(d == -1) return false;
      BigDecimal bd = BigDecimal.valueOf(d);
      if(consume('.')) {
        if(!digit(curr())) throw error("digit");
        BigDecimal f = BigDecimal.TEN;
        while(digit(curr())) {
          bd = bd.add(BigDecimal.valueOf(number()).divide(f, MathContext.DECIMAL64));
          f = f.multiply(BigDecimal.TEN);
        }
      }
      seconds = bd;
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
      final int ip = pos;
      skipWs();
      if(consume('(')) {
        skipWs();
        if(!tzname()) throw error("timezone");
        skipWs();
        if(!consume(')')) throw error("')'");
      } else {
        pos = ip;
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
      if(zone == null) zone = TIMES[t];
      return true;
    }

    /**
     * Parses a timezone offset.
     * @return success flag
     * @throws QueryException query exception
     */
    private boolean tzoffset() throws QueryException {
      final int s = consume('-') ? -1 : 1;
      if(s == 1 && !consume('+')) return false;
      if(!digit(curr())) throw error("timezone digits");

      int h = number(), m = 0;
      if(consume(':')) {
        final int n = twoDigits();
        if(n != -1) m = n;
      } else if(digit(curr())) {
        final int n = number();
        if(digit(curr())) {
          final int n2 = number();
          if(digit(curr())) {
            h = h * 10 + n;
            m = n2 * 10 + number();
          } else {
            m = n * 10 + n2;
          }
        } else {
          h = h * 10 + n;
          if(consume(':')) {
            final int n2 = twoDigits();
            if(n2 != -1) m = n2;
          }
        }
      }
      if(m >= 60) throw QueryError.IETF_INV_X.get(info, m);

      zone = s * (h * 60 + m);
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
     * Parses one or two digits.
     * @return number or {@code -1}
     */
    private int oneOrTwoDigits() {
      if(!digit(curr())) return -1;
      final int d = number();
      return digit(curr()) ? d * 10 + number() : d;
    }

    /**
     * Consumes a whitespace character.
     * @return success flag
     */
    private boolean skipWs() {
      if(!more() || !ws(curr())) return false;
      do consume(); while(more() && ws(curr()));
      return true;
    }

    /**
     * Adds a number to a string.
     * @param tb token builder
     * @param num number
     * @param digits minimum number of digits to add
     * @return token builder
     */
    private static TokenBuilder addNumber(final TokenBuilder tb, final int num, final int digits) {
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
      return QueryError.IETF_PARSE_X_X_X.get(info, msg, curr(), original);
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
