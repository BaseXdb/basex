package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.item.Dec.*;

import java.math.*;
import java.util.*;
import java.util.regex.*;

import javax.xml.datatype.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract super class for date items.
 *
 * @author BaseX Team 2005-21, BSD License
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
  static final String DD = "(\\d{2})";
  /** Year pattern. */
  static final String YEAR = "(-?(000[1-9]|00[1-9]\\d|0[1-9]\\d{2}|[1-9]\\d{3,}))";
  /** Date pattern. */
  static final String ZONE = "(([-+])" + DD + ':' + DD + "|Z)?";
  /** Day per months. */
  static final byte[] DAYS = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

  /** Date pattern. */
  public static final Pattern DATE = Pattern.compile(YEAR + '-' + DD + '-' + DD + ZONE);
  /** Time pattern. */
  public static final Pattern TIME = Pattern.compile(
      DD + ':' + DD + ':' + "(\\d{2}(\\.\\d+)?)" + ZONE);

  /** Year.
   * <ul>
   *   <li> 1 - {@code Long#MAX_VALUE}-1: AD</li>
   *   <li> 0 - {@link Long#MIN_VALUE}: BC, +1 added</li>
   *   <li> {@link Long#MAX_VALUE}: undefined</li>
   * </ul> */
  long yea = Long.MAX_VALUE;
  /** Month ({@code 0-11}). {@code -1}: undefined. */
  byte mon = -1;
  /** Day ({@code 0-30}). {@code -1}: undefined. */
  byte day = -1;
  /** Hour ({@code 0-59}). {@code -1}: undefined. */
  byte hou = -1;
  /** Minute ({@code 0-59}). {@code -1}: undefined. */
  byte min = -1;
  /** Timezone in minutes ({@code -14*60-14*60}). {@link Short#MAX_VALUE}: undefined. */
  short tz = Short.MAX_VALUE;

  /** Data factory. */
  static final DatatypeFactory DF;
  static {
    try {
      DF = DatatypeFactory.newInstance();
    } catch(final Exception ex) {
      throw Util.notExpected(ex);
    }
  }

  /**
   * Constructor.
   * @param type item type
   * @param date date reference
   */
  ADate(final Type type, final ADate date) {
    super(type);
    yea = date.yea;
    mon = date.mon;
    day = date.day;
    hou = date.hou;
    min = date.min;
    sec = date.sec;
    tz = date.tz;
  }

  /**
   * Constructor.
   * @param type item type
   */
  ADate(final Type type) {
    super(type);
  }

  /**
   * Initializes the date format.
   * @param date input
   * @param exp example format
   * @param ii input info
   * @throws QueryException query exception
   */
  final void date(final byte[] date, final String exp, final InputInfo ii) throws QueryException {
    final Matcher mt = DATE.matcher(Token.string(date).trim());
    if(!mt.matches()) throw dateError(date, exp, ii);
    yea = toLong(mt.group(1), false, ii);
    // +1 is added to BC values to simplify computations
    if(yea < 0) yea++;
    mon = (byte) (Strings.toInt(mt.group(3)) - 1);
    day = (byte) (Strings.toInt(mt.group(4)) - 1);

    if(mon < 0 || mon >= 12 || day < 0 || day >= dpm(yea, mon)) throw dateError(date, exp, ii);
    if(yea <= MIN_YEAR || yea > MAX_YEAR)
      throw DATERANGE_X_X.get(ii, type, normalize(date, ii));
    zone(mt, 5, date, ii);
  }

  /**
   * Initializes the time format.
   * @param time input format
   * @param exp expected format
   * @param ii input info
   * @throws QueryException query exception
   */
  final void time(final byte[] time, final String exp, final InputInfo ii) throws QueryException {
    final Matcher mt = TIME.matcher(Token.string(time).trim());
    if(!mt.matches()) throw dateError(time, exp, ii);

    hou = (byte) Strings.toInt(mt.group(1));
    min = (byte) Strings.toInt(mt.group(2));
    sec = toDecimal(mt.group(3), false, ii);
    if(min >= 60 || sec.compareTo(BD_60) >= 0 || hou > 24 ||
       hou == 24 && (min > 0 || sec.compareTo(BigDecimal.ZERO) > 0)) throw dateError(time, exp, ii);
    zone(mt, 5, time, ii);
    if(hou == 24) {
      hou = 0;
      add(BD_864000);
    }
  }

  /**
   * Initializes the timezone.
   * @param matcher matcher
   * @param pos first matching position
   * @param value value
   * @param ii input info
   * @throws QueryException query exception
   */
  final void zone(final Matcher matcher, final int pos, final byte[] value, final InputInfo ii)
      throws QueryException {

    final String z = matcher.group(pos);
    if(z == null) return;
    if("Z".equals(z)) {
      tz = 0;
    } else {
      final int th = Strings.toInt(matcher.group(pos + 2));
      final int tm = Strings.toInt(matcher.group(pos + 3));
      if(th > 14 || tm > 59 || th == 14 && tm != 0) throw INVALIDZONE_X.get(ii, value);
      final int mn = th * 60 + tm;
      tz = (short) ("-".equals(matcher.group(pos + 1)) ? -mn : mn);
    }
  }

  /**
   * Adds/subtracts the specified dayTime duration.
   * @param dur duration
   * @param plus plus/minus flag
   */
  final void calc(final DTDur dur, final boolean plus) {
    add(plus ? dur.sec : dur.sec.negate());
  }

  /**
   * Adds/subtracts the specified yearMonth duration.
   * @param dur duration
   * @param plus plus/minus flag
   * @param ii input info
   * @throws QueryException query exception
   */
  final void calc(final YMDur dur, final boolean plus, final InputInfo ii) throws QueryException {
    final long m = plus ? dur.mon : -dur.mon;
    final long mn = mon + m;
    mon = (byte) mod(mn, 12);
    yea += div(mn, 12);
    day = (byte) Math.min(dpm(yea, mon) - 1, day);

    if(yea <= MIN_YEAR || yea > MAX_YEAR) throw YEARRANGE_X.get(ii, yea);
  }

  /**
   * Adds the specified dayTime duration.
   * @param add value to be added
   */
  private void add(final BigDecimal add) {
    // normalized modulo: sc % 60  vs.  (-sc + sc % 60 + 60 + sc) % 60
    final BigDecimal sc = sec().add(add);
    sec = sc.signum() >= 0 ? sc.remainder(BD_60) :
      sc.negate().add(sc.remainder(BD_60)).add(BD_60).add(sc).remainder(BD_60);

    final long mn = Math.max(minute(), 0) + div(
        sc.setScale(0, RoundingMode.FLOOR).longValue(), 60);
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
   * @param value input value
   * @param mod modulo
   * @return result
   */
  private static long mod(final long value, final int mod) {
    return (value > 0 ? value : Long.MAX_VALUE / mod * mod + value) % mod;
  }

  /**
   * Returns a normalized division value for negative and positive values.
   * @param value input value
   * @param div divisor
   * @return result
   */
  private static long div(final long value, final int div) {
    return value < 0 ? (value + 1) / div - 1 : value / div;
  }

  /**
   * Adjusts the timezone.
   * @param zone timezone (may be {@code null})
   * @param spec indicates if zone has been specified
   * @param ii input info
   * @throws QueryException query exception
   */
  public abstract void timeZone(DTDur zone, boolean spec, InputInfo ii) throws QueryException;

  /**
   * Adjusts the timezone.
   * @param zone timezone
   * @param spec indicates if zone has been specified (may be {@code null})
   * @param ii input info
   * @throws QueryException query exception
   */
  final void tz(final DTDur zone, final boolean spec, final InputInfo ii) throws QueryException {
    final short t;
    if(spec && zone == null) {
      t = Short.MAX_VALUE;
    } else {
      if(zone == null) {
        final Calendar c = Calendar.getInstance();
        t = (short) ((c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET)) / 60000);
      } else {
        t = (short) (zone.minute() + zone.hour() * 60);
        if(zone.sec().signum() != 0) throw ZONESEC_X.get(ii, zone);
        if(Math.abs(t) > 60 * 14 || zone.day() != 0) throw INVALZONE_X.get(ii, zone);
      }

      // change time if two competing time zones exist
      if(tz != Short.MAX_VALUE) add(BigDecimal.valueOf(60L * (t - tz)));
    }
    tz = t;
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
  public final long hour() {
    return hou;
  }

  @Override
  public final long minute() {
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
  public final int tz() {
    return tz;
  }

  /**
   * Returns if the timezone is defined.
   * @return time zone
   */
  public final boolean hasTz() {
    return tz != Short.MAX_VALUE;
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
      prefix(tb, hour(), 2);
      tb.add(':');
      prefix(tb, minute(), 2);
      tb.add(':');
      if(sec.intValue() < 10) tb.add('0');
      tb.add(Token.chopNumber(Token.token(sec().abs().toPlainString())));
    }
    zone(tb);
    return tb.finish();
  }

  /**
   * Adds the time zone to the specified token builder.
   * @param tb token builder
   */
  void zone(final TokenBuilder tb) {
    if(tz == Short.MAX_VALUE) return;
    if(tz == 0) {
      tb.add('Z');
    } else {
      tb.add(tz > 0 ? '+' : '-');
      prefix(tb, Math.abs(tz) / 60, 2);
      tb.add(':');
      prefix(tb, Math.abs(tz) % 60, 2);
    }
  }

  /**
   * Prefixes the specified number of zero digits before a number.
   * @param tb token builder
   * @param number number to be printed
   * @param zero maximum number of zero digits
   */
  static void prefix(final TokenBuilder tb, final long number, final int zero) {
    final byte[] t = Token.token(number);
    for(int i = t.length; i < zero; i++) tb.add('0');
    tb.add(t);
  }

  @Override
  public final boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return df(item, ii) == 0;
  }

  /**
   * Returns the difference between the current and the specified item.
   * See {@link Item#diff(Item, Collation, InputInfo)}.
   * @param item item to be compared
   * @param ii input info (can be {@code null})
   * @return difference
   * @throws QueryException query exception
   */
  private int df(final Item item, final InputInfo ii) throws QueryException {
    final ADate d = (ADate) (item instanceof ADate ? item : type.cast(item, null, null, ii));
    final BigDecimal d1 = seconds().add(days().multiply(BD_864000));
    final BigDecimal d2 = d.seconds().add(d.days().multiply(BD_864000));
    return d1.compareTo(d2);
  }

  @Override
  public final boolean sameKey(final Item item, final InputInfo ii) throws QueryException {
    return item instanceof ADate && hasTz() == ((ADate) item).hasTz() && eq(item, null, null, ii);
  }

  @Override
  public final int hash(final InputInfo ii) {
    return seconds().add(days().multiply(BD_864000)).intValue();
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo ii) throws QueryException {
    return df(item, ii);
  }

  @Override
  public final XMLGregorianCalendar toJava() {
    return DF.newXMLGregorianCalendar(
      yea == Long.MAX_VALUE ? null : BigInteger.valueOf(yea > 0 ? yea : yea - 1),
      mon >= 0 ? mon + 1 : Integer.MIN_VALUE,
      day >= 0 ? day + 1 : Integer.MIN_VALUE,
      hou >= 0 ? hou : Integer.MIN_VALUE,
      min >= 0 ? min : Integer.MIN_VALUE,
      sec != null ? sec.intValue() : Integer.MIN_VALUE,
      sec != null ? sec.remainder(BigDecimal.ONE) : null,
      tz == Short.MAX_VALUE ? Integer.MIN_VALUE : tz);
  }

  /**
   * Returns the date in seconds.
   * @return seconds
   */
  public final BigDecimal seconds() {
    int z = tz;
    if(z == Short.MAX_VALUE) {
      // [CG] could be eliminated (XQuery, DateTime)
      final long n = System.currentTimeMillis();
      z = Calendar.getInstance().getTimeZone().getOffset(n) / 60000;
    }
    return (sec == null ? BigDecimal.ZERO : sec).add(
        BigDecimal.valueOf(Math.max(0, hou) * 3600L + Math.max(0, min) * 60L - z * 60L));
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
   * @param year year
   * @param month month
   * @param day days
   * @return days
   */
  private static BigDecimal days(final long year, final int month, final int day) {
    final long y = year - (month < 2 ? 1 : 0), m = month + (month < 2 ? 13 : 1), d = day + 1;
    return BD_365.multiply(BigDecimal.valueOf(y)).add(
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
    BigDecimal t = d.add(BD_36525).multiply(BD_4).
        divideToIntegralValue(BD_146097).subtract(BigDecimal.ONE);
    BigDecimal y = BD_100.multiply(t);
    d = d.subtract(BD_36524.multiply(t).add(t.divideToIntegralValue(BD_4)));
    t = d.add(BD_366).multiply(BD_4).divideToIntegralValue(BD_1461).subtract(BigDecimal.ONE);
    y = y.add(t);
    d = d.subtract(BD_365.multiply(t).add(t.divideToIntegralValue(BD_4)));
    final BigDecimal m = BD_5.multiply(d).add(BD_2).divideToIntegralValue(BD_153);
    d = d.subtract(BD_153.multiply(m).add(BD_2).divideToIntegralValue(BD_5));
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
  public final boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof ADate)) return false;
    final ADate a = (ADate) obj;
    return type.eq(a.type) && yea == a.yea && mon == a.mon && day == a.day &&
        hou == a.hou && min == a.min && tz == a.tz &&
        (sec == null ? a.sec == null : sec.compareTo(a.sec) == 0);
  }

  @Override
  public final void plan(final QueryString qs) {
    qs.quoted(string(null));
  }
}
