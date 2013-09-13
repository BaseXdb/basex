package org.basex.query.value.item;

import static org.basex.query.util.Err.*;

import java.math.*;
import java.util.*;
import java.util.regex.*;

import javax.xml.datatype.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract super class for date items.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class ADate extends ADateDur {
  /** Maximum value for computations on year value based on long range. */
  static final long MAX_YEAR = (long) (Long.MAX_VALUE / 365.2425) - 2;
  /** Minimum year value. */
  static final long MIN_YEAR = -MAX_YEAR;
  /** Constant for counting negative years (divisible by 400). */
  private static final long ADD_NEG = (MAX_YEAR / 400 + 1) * 400;

  /** Pattern for two digits. */
  protected static final String DD = "(\\d{2})";
  /** Year pattern. */
  protected static final String YEAR =
      "(-?(000[1-9]|00[1-9]\\d|0[1-9]\\d{2}|[1-9]\\d{3,}))";
  /** Date pattern. */
  protected static final String ZONE = "((\\+|-)" + DD + ':' + DD + "|Z)?";
  /** Day per months. */
  protected static final byte[] DAYS = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
  /** Date pattern. */
  private static final Pattern DATE = Pattern.compile(YEAR + '-' + DD + '-' + DD + ZONE);
  /** Time pattern. */
  private static final Pattern TIME = Pattern.compile(
      DD + ':' + DD + ':' + "(\\d{2}(\\.\\d+)?)" + ZONE);

  /** Year.
   * <ul>
   *   <li> 1 - {@code Long#MAX_VALUE}-1: AD</li>
   *   <li> 0 - {@link Long#MIN_VALUE}: BC, +1 added</li>
   *   <li> {@link Long#MAX_VALUE}: undefined</li>
   * </ul> */
  protected long yea = Long.MAX_VALUE;
  /** Month ({@code 0-11}). {@code -1}: undefined. */
  protected byte mon = -1;
  /** Day ({@code 0-30}). {@code -1}: undefined. */
  protected byte day = -1;
  /** Hour ({@code 0-59}). {@code -1}: undefined. */
  protected byte hou = -1;
  /** Minute ({@code 0-59}). {@code -1}: undefined. */
  protected byte min = -1;
  /** Timezone in minutes ({@code -14*60-14*60}). {@link Short#MAX_VALUE}: undefined. */
  protected short zon = Short.MAX_VALUE;

  /** Data factory. */
  public static DatatypeFactory df;
  static {
    try {
      df = DatatypeFactory.newInstance();
    } catch(final Exception ex) {
      Util.notexpected(ex);
    }
  }

  /**
   * Constructor.
   * @param typ data type
   * @param d date reference
   */
  ADate(final Type typ, final ADate d) {
    super(typ);
    yea = d.yea;
    mon = d.mon;
    day = d.day;
    hou = d.hou;
    min = d.min;
    sec = d.sec;
    zon = d.zon;
  }

  /**
   * Constructor.
   * @param typ data type
   */
  ADate(final Type typ) {
    super(typ);
  }

  /**
   * Initializes the date format.
   * @param d input
   * @param e example format
   * @param ii input info
   * @throws QueryException query exception
   */
  final void date(final byte[] d, final String e, final InputInfo ii)
      throws QueryException {

    final Matcher mt = DATE.matcher(Token.string(d).trim());
    if(!mt.matches()) dateErr(d, e, ii);
    yea = toLong(mt.group(1), false, ii);
    // +1 is added to BC values to simplify computations
    if(yea < 0) yea++;
    mon = (byte) (Token.toInt(mt.group(3)) - 1);
    day = (byte) (Token.toInt(mt.group(4)) - 1);

    if(mon < 0 || mon >= 12 || day < 0 || day >= dpm(yea, mon)) dateErr(d, e, ii);
    if(yea <= MIN_YEAR || yea > MAX_YEAR) DATERANGE.thrw(ii, type, d);
    zone(mt, 5, d, ii);
  }

  /**
   * Initializes the time format.
   * @param d input format
   * @param e expected format
   * @param ii input info
   * @throws QueryException query exception
   */
  final void time(final byte[] d, final String e, final InputInfo ii)
      throws QueryException {

    final Matcher mt = TIME.matcher(Token.string(d).trim());
    if(!mt.matches()) dateErr(d, e, ii);

    hou = (byte) Token.toInt(mt.group(1));
    min = (byte) Token.toInt(mt.group(2));
    sec = toDecimal(mt.group(3), false, ii);
    if(min >= 60 || sec.compareTo(BD60) >= 0 || hou > 24 ||
       hou == 24 && (min > 0 || sec.compareTo(BigDecimal.ZERO) > 0)) dateErr(d, e, ii);
    zone(mt, 5, d, ii);
    if(hou == 24) {
      hou = 0;
      add(DAYSECONDS);
    }
  }

  /**
   * Initializes the timezone.
   * @param mt matcher
   * @param p first matching position
   * @param val value
   * @param ii input info
   * @throws QueryException query exception
   */
  final void zone(final Matcher mt, final int p, final byte[] val, final InputInfo ii)
      throws QueryException {

    final String tz = mt.group(p);
    if(tz == null) return;
    if(tz.equals("Z")) {
      zon = 0;
    } else {
      final int th = Token.toInt(mt.group(p + 2));
      final int tm = Token.toInt(mt.group(p + 3));
      if(th > 14 || tm > 59 || th == 14 && tm != 0) INVALIDZONE.thrw(ii, val);
      final int mn = th * 60 + tm;
      zon = (short) (mt.group(p + 1).equals("-") ? -mn : mn);
    }
  }

  /**
   * Adds/subtracts the specified dayTime duration.
   * @param dur duration
   * @param p plus/minus flag
   */
  protected final void calc(final DTDur dur, final boolean p) {
    add(p ? dur.sec : dur.sec.negate());
  }

  /**
   * Adds/subtracts the specified yearMonth duration.
   * @param dur duration
   * @param p plus/minus flag
   * @param ii input info
   * @throws QueryException query exception
   */
  final void calc(final YMDur dur, final boolean p, final InputInfo ii)
      throws QueryException {

    final long m = p ? dur.mon : -dur.mon;
    final long mn = mon + m;
    mon = (byte) mod(mn, 12);
    yea = yea + div(mn, 12);
    day = (byte) Math.min(dpm(yea, mon) - 1, day);

    if(yea <= MIN_YEAR || yea > MAX_YEAR) DATEADDRANGE.thrw(ii, this);
  }

  /**
   * Adds the specified dayTime duration.
   * @param add value to be added
   */
  private void add(final BigDecimal add) {
    // normalized modulo: sc % 60  vs.  (-sc + sc % 60 + 60 + sc) % 60
    final BigDecimal sc = sec().add(add);
    sec = sc.signum() >= 0 ? sc.remainder(BD60) :
      sc.negate().add(sc.remainder(BD60)).add(BD60).add(sc).remainder(BD60);

    final long mn = Math.max(min(), 0) + div(sc.longValue(), 60);
    min = (byte) mod(mn, 60);
    final long ho = Math.max(hou, 0) + div(mn, 60);
    hou = (byte) mod(ho, 24);
    final long da = div(ho, 24);

    final long[] ymd = ymd(days().add(BigDecimal.valueOf(da)));
    yea = ymd[0];
    mon = (byte) ymd[1];
    day = (byte) ymd[2];
  }

  /**
   * Returns a normalized module value for negative and positive values.
   * @param i input value
   * @param m modulo
   * @return result
   */
  private static long mod(final long i, final int m) {
    return i > 0 ? i % m : (Long.MAX_VALUE / m * m + i) % m;
  }

  /**
   * Returns a normalized division value for negative and positive values.
   * @param i input value
   * @param d divisor
   * @return result
   */
  private static long div(final long i, final int d) {
    return i < 0 ? (i + 1) / d - 1 : i / d;
  }

  /**
   * Adjusts the timezone.
   * @param tz timezone
   * @param spec indicates if zone has been specified (can be {@code null})
   * @param ii input info
   * @throws QueryException query exception
   */
  public abstract void timeZone(final DTDur tz, final boolean spec, final InputInfo ii)
      throws QueryException;

  /**
   * Adjusts the timezone.
   * @param tz timezone
   * @param spec indicates if zone has been specified (can be {@code null})
   * @param ii input info
   * @throws QueryException query exception
   */
  protected void tz(final DTDur tz, final boolean spec, final InputInfo ii)
      throws QueryException {

    final short t;
    if(spec && tz == null) {
      t = Short.MAX_VALUE;
    } else {
      if(tz == null) {
        final Calendar c = Calendar.getInstance();
        t = (short) ((c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET)) / 60000);
      } else {
        t = (short) (tz.min() + tz.hou() * 60);
        if(tz.sec().signum() != 0) ZONESEC.thrw(ii, tz);
        if(Math.abs(t) > 60 * 14 || tz.day() != 0) INVALZONE.thrw(ii, tz);
      }

      // change time if two competing time zones exist
      if(zon != Short.MAX_VALUE) add(BigDecimal.valueOf(60 * (t - zon)));
    }
    zon = t;
  }

  @Override
  public final long yea() {
    return yea > 0 ? yea : yea - 1;
  }

  @Override
  public final long mon() {
    return mon + 1;
  }

  @Override
  public final long day() {
    return day + 1;
  }

  @Override
  public final long hou() {
    return hou;
  }

  @Override
  public final long min() {
    return min;
  }

  @Override
  public final BigDecimal sec() {
    return sec == null ? BigDecimal.ZERO : sec;
  }

  /**
   * Returns the timezone in minutes.
   * @return time zone
   */
  public final int zon() {
    return zon;
  }

  @Override
  public byte[] string(final InputInfo ii) {
    final TokenBuilder tb = new TokenBuilder();
    final boolean ymd = yea != Long.MAX_VALUE;
    if(ymd) {
      if(yea <= 0) tb.add('-');
      prefix(tb, Math.abs(yea()), 4);
      tb.add('-');
      prefix(tb, mon(), 2);
      tb.add('-');
      prefix(tb, day(), 2);
    }
    if(hou >= 0) {
      if(ymd) tb.add('T');
      prefix(tb, hou(), 2);
      tb.add(':');
      prefix(tb, min(), 2);
      tb.add(':');
      if(sec.intValue() < 10) tb.add('0');
      tb.addExt(Token.chopNumber(Token.token(sec().abs().toPlainString())));
    }
    zone(tb);
    return tb.finish();
  }

  /**
   * Adds the time zone to the specified token builder.
   * @param tb token builder
   */
  protected void zone(final TokenBuilder tb) {
    if(zon == Short.MAX_VALUE) return;
    if(zon == 0) {
      tb.add('Z');
    } else {
      tb.add(zon > 0 ? '+' : '-');
      prefix(tb, Math.abs(zon) / 60, 2);
      tb.add(':');
      prefix(tb, Math.abs(zon) % 60, 2);
    }
  }

  /**
   * Prefixes the specified number of zero digits before a number.
   * @param tb token builder
   * @param n number to be printed
   * @param z maximum number of zero digits
   */
  protected static void prefix(final TokenBuilder tb, final long n, final int z) {
    final byte[] t = Token.token(n);
    for(int i = t.length; i < z; i++) tb.add('0');
    tb.add(t);
  }

  @Override
  public final boolean eq(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    final ADate d = (ADate) (it instanceof ADate ? it : type.cast(it, null, ii));
    final BigDecimal d1 = seconds().add(days().multiply(DAYSECONDS));
    final BigDecimal d2 = d.seconds().add(d.days().multiply(DAYSECONDS));
    return d1.compareTo(d2) == 0;
  }

  @Override
  public int hash(final InputInfo ii) throws QueryException {
    return seconds().add(days().multiply(DAYSECONDS)).intValue();
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    final ADate d = (ADate) (it instanceof ADate ? it : type.cast(it, null, ii));
    final BigDecimal d1 = seconds().add(days().multiply(DAYSECONDS));
    final BigDecimal d2 = d.seconds().add(d.days().multiply(DAYSECONDS));
    return d1.compareTo(d2);
  }

  @Override
  public final XMLGregorianCalendar toJava() {
    return df.newXMLGregorianCalendar(
      yea != Long.MAX_VALUE ? BigInteger.valueOf(yea > 0 ? yea : yea - 1) : null,
      mon >= 0 ? mon + 1 : Integer.MIN_VALUE,
      day >= 0 ? day + 1 : Integer.MIN_VALUE,
      hou >= 0 ? hou : Integer.MIN_VALUE,
      min >= 0 ? min : Integer.MIN_VALUE,
      sec != null ? sec.intValue() : Integer.MIN_VALUE,
      sec != null ? sec.remainder(BigDecimal.ONE) : null,
      zon != Short.MAX_VALUE ? zon : Integer.MIN_VALUE);
  }

  /**
   * Returns the date in seconds.
   * @return seconds
   */
  final BigDecimal seconds() {
    int z = zon;
    if(z == Short.MAX_VALUE) {
      // [CG] XQuery, DateTime: may be removed
      final long n = System.currentTimeMillis();
      z = Calendar.getInstance().getTimeZone().getOffset(n) / 60000;
    }
    return (sec == null ? BigDecimal.ZERO : sec).add(
        BigDecimal.valueOf(Math.max(0, hou) * 3600 + Math.max(0, min) * 60 - z * 60));
  }

  /**
   * Returns a day count.
   * @return days
   */
  final BigDecimal days() {
    final long y = yea == Long.MAX_VALUE ? 1 : yea;
    return days(y + ADD_NEG, Math.max(mon, 0), Math.max(day, 0));
  }

  /**
   * Returns a day count for the specified years, months and days.
   * All values must be specified in their internal representation
   * (undefined values are supported, too).
   * Algorithm is derived from J R Stockton (http://www.merlyn.demon.co.uk/daycount.htm).
   * @param yea year
   * @param mon month
   * @param day days
   * @return days
   */
  public static BigDecimal days(final long yea, final int mon, final int day) {
    final long y = yea - (mon < 2 ? 1 : 0);
    final int m = mon + (mon < 2 ? 13 : 1);
    final int d = day + 1;
    return BD365.multiply(BigDecimal.valueOf(y)).add(
        BigDecimal.valueOf(y / 4 - y / 100 + y / 400 - 92 + d + (153 * m - 2) / 5));
  }

  /**
   * Converts a day count into year, month and day components.
   * Algorithm is derived from J R Stockton (http://www.merlyn.demon.co.uk/daycount.htm).
   * @param days day count
   * @return result array
   */
  private static long[] ymd(final BigDecimal days) {
    BigDecimal d = days;
    BigDecimal t = d.add(BD36525).multiply(BD4).
        divideToIntegralValue(BD146097).subtract(BigDecimal.ONE);
    BigDecimal y = BD100.multiply(t);
    d = d.subtract(BD36524.multiply(t).add(t.divideToIntegralValue(BD4)));
    t = d.add(BD366).multiply(BD4).divideToIntegralValue(BD1461).subtract(BigDecimal.ONE);
    y = y.add(t);
    d = d.subtract(BD365.multiply(t).add(t.divideToIntegralValue(BD4)));
    final BigDecimal m = BD5.multiply(d).add(BD2).divideToIntegralValue(BD153);
    d = d.subtract(BD153.multiply(m).add(BD2).divideToIntegralValue(BD5));
    long mm = m.longValue();
    if(mm > 9) { mm -= 12; y = y.add(BigDecimal.ONE); }
    return new long[] { y.subtract(BigDecimal.valueOf(ADD_NEG)).longValue(),
        mm + 2, d.longValue() };
  }

  /**
   * Returns days per month, considering leap years.
   * @param yea year
   * @param mon month
   * @return days
   */
  public static int dpm(final long yea, final int mon) {
    final byte l = DAYS[mon];
    return mon == 1 && yea % 4 == 0 && (yea % 100 != 0 || yea % 400 == 0) ? l + 1 : l;
  }

  @Override
  public final String toString() {
    return Util.info("\"%\"", string(null));
  }
}
