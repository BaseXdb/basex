package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQText.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.datatype.DatatypeFactory;
import org.basex.BaseX;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Date container.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Date extends Item {
  /** Data factory. */
  static DatatypeFactory df;

  static {
    try {
      df = DatatypeFactory.newInstance();
    } catch(final Exception e) {
      BaseX.notexpected();
    }
  }
  
  /** Seconds per day. */
  protected static final int DAYSECONDS = 86400;
  /** Day of months. */
  protected static final byte[] DAYS = {
    30, 27, 30, 29, 30, 29, 30, 30, 29, 30, 29, 30 };
  /** Date pattern. */
  protected static final String ZONE = "((\\+|-)([0-9]{2}):([0-9]{2})|Z)?";
  /** Date pattern. */
  private static final Pattern DATE = Pattern.compile(
      "(-?)([0-9]{4})-([0-9]{2})-([0-9]{2})" + ZONE);
  /** Time pattern. */
  private static final Pattern TIME = Pattern.compile(
      "([0-9]{2}):([0-9]{2}):([0-9]{2})(\\.([0-9]+))?" + ZONE);

  /** Year and Month. */
  public int mon;
  /** Day and Time. */
  public long sec;
  /** Milliseconds. */
  public double mil;
  /** Minus Flag. */
  public boolean minus;
  /** Zone Flag. */
  public boolean zone;
  /** Zone Time. */
  public int zshift;

  /**
   * Constructor.
   * @param typ data type
   */
  protected Date(final Type typ) {
    super(typ);
  }

  /**
   * Interprets a date.
   * @param val date/time value
   * @param ex example format
   * @throws XQException evaluation exception
   */
  protected final void date(final byte[] val, final String ex)
      throws XQException {
    final Matcher mt = DATE.matcher(Token.string(val).trim());
    if(!mt.matches()) Err.date(type, ex);

    final int y = Token.toInt(mt.group(2));
    final int m = Token.toInt(mt.group(3)) - 1;
    final long d = Token.toInt(mt.group(4)) - 1;

    if(y == 0 || m < 0 || m > 11 || d < 0 || d > dpm(y, m))
      Err.range(type, val);

    mon = y * 12 + m;
    sec = d * DAYSECONDS;
    minus = mt.group(1).length() != 0 && (mon != 0 || sec != 0);
    zone(mt, 5, val);
  }

  /**
   * Add/subtract the specified duration.
   * @param a duration
   * @param p plus/minus flag
   */
  protected final void calc(final Dur a, final boolean p) {
    final int m = a.mon;
    final long s = a.sec;
    final boolean min = p ? a.minus : !a.minus;

    if(minus) {
      mon = -mon + m;
      sec += s;
      mil += a.mil;
    } else if(min) {
      mon -= m;
      sec -= s;
      mil -= a.mil;
    } else {
      mon += m;
      sec += s;
      mil += a.mil;
    }
    if(mil < 0) { mil++; sec--; } else if(mil >= 1) { mil--; sec++; }

    // correct date overflow
    while(mon >= 0 && sec / DAYSECONDS > Date.dpm(mon / 12, mon % 12)) {
      if(min || m % 12 == 0 && s == 0) {
        sec -= DAYSECONDS;
      } else {
        sec -= DAYSECONDS * (Date.dpm(mon / 12, mon % 12) + 1);
        mon++;
      }
    }
    while(sec < 0) {
      sec += DAYSECONDS;
      if(--mon < 0) mon = 12 - mon;
      if(sec < DAYSECONDS) sec += Date.dpm(mon / 12, mon % 12) * DAYSECONDS;
    }

    minus = mon < 0;
    mon = Math.abs(mon);
  }

  /***
   * Returns days per month.
   * @param y year
   * @param m month
   * @return days
   */
  public static long dpm(final int y, final int m) {
    return DAYS[m] + (m == 1 && y % 4 == 0 &&
        (y % 100 != 0 || y % 400 == 0) ? 1 : 0);
  }

  /**
   * Interprets a time.
   * @param val date/time value
   * @param ex example format
   * @throws XQException evaluation exception
   */
  protected final void time(final byte[] val, final String ex)
      throws XQException {
    final Matcher mt = TIME.matcher(Token.string(val).trim());
    if(!mt.matches()) Err.date(type, ex);

    final int h = Token.toInt(mt.group(1));
    final int m = Token.toInt(mt.group(2));
    final int s = Token.toInt(mt.group(3));
    if(h > 24 || m > 59 || s > 59 || h == 24 && (m > 0 || s > 0))
      Err.range(type, val);

    if(mt.group(4) != null) {
      mil = Double.parseDouble(mt.group(4));
      if(h == 24 && mil > 0) Err.date(type, ex);
    }
    sec += h * 3600 + m * 60 + s;
    minus &= sec != 0 || mil != 0;

    // correct day overflow
    if(sec / DAYSECONDS > dpm(mon / 12, mon % 12)) {
      sec %= DAYSECONDS;
      mon++;
    }

    zone(mt, 6, val);
  }

  /**
   * Evaluates the timezone.
   * @param mt matcher
   * @param p matching position
   * @param val value
   * @throws XQException evaluation exception
   */
  protected final void zone(final Matcher mt, final int p, final byte[] val)
    throws XQException {

    if(mt.group(p) != null) {
      zone = mt.group(p).equals("Z");
      if(!zone) {
        final int th = Token.toInt(mt.group(p + 2));
        final int tm = Token.toInt(mt.group(p + 3));
        if(th > 14 || tm > 59 || th == 14 && tm != 0) Err.or(INVALIDZONE, val);
        zshift = (short) (th * 60 + tm);
        if(mt.group(p + 1).equals("-")) zshift = -zshift;
        zone = true;
      }
    }
  }

  /**
   * Adds a date to the output.
   * @param tb token builder
   */
  protected final void date(final TokenBuilder tb) {
    if(minus) tb.add('-');
    format(mon / 12, 4, '-', tb);
    format(mon % 12 + 1, 2, '-', tb);
    format((int) (sec / DAYSECONDS) + 1, 2, (char) 0, tb);
  }

  /**
   * Adds a time  to the output.
   * @param tb token builder
   */
  protected final void time(final TokenBuilder tb) {
    final int t = (int) (sec % DAYSECONDS);
    format(t / 3600, 2, ':', tb);
    format(t % 3600 / 60, 2, ':', tb);
    format(t % 60, 2, (char) 0, tb);
    if(mil != 0) tb.add(Token.substring(Token.token(mil), 1));
  }

  /**
   * Adds a timezone to the output.
   * @param tb token builder
   */
  protected final void zone(final TokenBuilder tb) {
    if(zone) {
      if(zshift == 0) {
        tb.add('Z');
      } else {
        tb.add(zshift < 0 ? '-' : '+');
        final int s = Math.abs(zshift);
        format(s / 60, 2, ':', tb);
        format(s % 60, 2, (char) 0, tb);
      }
    }
  }

  /**
   * Formats a digit.
   * @param v value to be added
   * @param n number of preceding zeroes
   * @param o optional character
   * @param tb token builder
   */
  protected final void format(final int v, final int n, final char o,
      final TokenBuilder tb) {
    final byte[] val = Token.token(v);
    for(int c = val.length; c < n; c++) tb.add('0');
    tb.add(val);
    if(o != 0) tb.add(o);
  }

  @Override
  public final boolean eq(final Item it) {
    final Date d = (Date) it;
    return minus == d.minus && mon == d.mon && sec - zshift * 60L ==
      d.sec - d.zshift * 60L && mil == d.mil;
  }

  @Override
  @SuppressWarnings("unused")
  public int diff(final Item it) throws XQException {
    return df(it);
  }

  /**
   * Compares two dates.
   * @param dt date to be compared
   * @return difference
   */
  protected final int df(final Item dt) {
    final Date d = (Date) dt;
    final int m1 = minus ? -mon : mon;
    final int m2 = d.minus ? -d.mon : d.mon;
    if(m1 != m2) return m1 < m2 ? -1 : 1;
    final long s1 = sec - zshift * 60L;
    final long s2 = d.sec - d.zshift * 60L;
    if(s1 != s2) return s1 < s2 ? -1 : 1;
    if(mil != d.mil) return mil < d.mil ? -1 : 1;
    return 0;
  }

  @Override
  public final String toString() {
    return "\"" + Token.string(str()) + "\"";
  }
}
