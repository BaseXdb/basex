package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.item.Dec.*;

import java.math.*;
import java.util.*;
import java.util.regex.*;

import javax.xml.datatype.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract super class for date items.
 *
 * @author BaseX Team 2005-24, BSD License
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
  long year = Long.MAX_VALUE;
  /** Month ({@code 0-11}). {@code -1}: undefined. */
  byte month = -1;
  /** Day ({@code 0-30}). {@code -1}: undefined. */
  byte day = -1;
  /** Hour ({@code 0-59}). {@code -1}: undefined. */
  byte hour = -1;
  /** Minute ({@code 0-59}). {@code -1}: undefined. */
  byte minute = -1;
  /** Seconds and milliseconds. {@code null}: undefined. */
  BigDecimal seconds;
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
    year = date.year;
    month = date.month;
    day = date.day;
    hour = date.hour;
    minute = date.minute;
    seconds = date.seconds;
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
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  final void date(final byte[] date, final String exp, final InputInfo info) throws QueryException {
    final Matcher mt = DATE.matcher(Token.string(date).trim());
    if(!mt.matches()) throw dateError(date, exp, info);
    year = toLong(mt.group(1), false, info);
    // +1 is added to BC values to simplify computations
    if(year < 0) year++;
    month = (byte) (Strings.toInt(mt.group(3)) - 1);
    day = (byte) (Strings.toInt(mt.group(4)) - 1);

    if(month < 0 || month >= 12 || day < 0 || day >= daysOfMonth(year, month)) {
      throw dateError(date, exp, info);
    }
    if(year <= MIN_YEAR || year > MAX_YEAR) throw DATERANGE_X_X.get(info, type, date);
    zone(mt, 5, date, info);
  }

  /**
   * Initializes the time format.
   * @param time input format
   * @param exp expected format
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  final void time(final byte[] time, final String exp, final InputInfo info) throws QueryException {
    final Matcher mt = TIME.matcher(Token.string(time).trim());
    if(!mt.matches()) throw dateError(time, exp, info);

    hour = (byte) Strings.toInt(mt.group(1));
    minute = (byte) Strings.toInt(mt.group(2));
    seconds = toDecimal(mt.group(3), false, info);
    if(minute >= 60 || seconds.compareTo(BD_60) >= 0 || hour > 24 ||
       hour == 24 && (minute > 0 || seconds.compareTo(BigDecimal.ZERO) > 0)) {
      throw dateError(time, exp, info);
    }
    zone(mt, 5, time, info);
    if(hour == 24) {
      hour = 0;
      add(BD_864000);
    }
  }

  /**
   * Initializes the timezone.
   * @param matcher matcher
   * @param pos first matching position
   * @param value value
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  final void zone(final Matcher matcher, final int pos, final byte[] value, final InputInfo info)
      throws QueryException {

    final String z = matcher.group(pos);
    if(z == null) return;
    if("Z".equals(z)) {
      tz = 0;
    } else {
      final int th = Strings.toInt(matcher.group(pos + 2));
      final int tm = Strings.toInt(matcher.group(pos + 3));
      if(th > 14 || tm > 59 || th == 14 && tm != 0) throw INVALIDZONE_X.get(info, value);
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
    add(plus ? dur.seconds : dur.seconds.negate());
  }

  /**
   * Adds/subtracts the specified yearMonth duration.
   * @param dur duration
   * @param plus plus/minus flag
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  final void calc(final YMDur dur, final boolean plus, final InputInfo info) throws QueryException {
    final long m = plus ? dur.months : -dur.months;
    final long mn = month + m;
    month = (byte) mod(mn, 12);
    year += div(mn, 12);
    day = (byte) Math.min(daysOfMonth(year, month) - 1, day);

    if(year <= MIN_YEAR || year > MAX_YEAR) throw YEARRANGE_X.get(info, year);
  }

  /**
   * Adds the specified dayTime duration.
   * @param add value to be added
   */
  private void add(final BigDecimal add) {
    // normalized modulo: sc % 60  vs.  (-sc + sc % 60 + 60 + sc) % 60
    final BigDecimal sc = sec().add(add);
    seconds = sc.signum() >= 0 ? sc.remainder(BD_60) :
      sc.negate().add(sc.remainder(BD_60)).add(BD_60).add(sc).remainder(BD_60);

    final long mn = Math.max(minute(), 0) + div(
        sc.setScale(0, RoundingMode.FLOOR).longValue(), 60);
    minute = (byte) mod(mn, 60);

    final long ho = Math.max(hour, 0) + div(mn, 60);
    hour = (byte) mod(ho, 24);
    final long da = div(ho, 24);

    final long[] ymd = ymd(days().add(BigDecimal.valueOf(da)));
    year = ymd[0];
    month = (byte) ymd[1];
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
   * Creates a new item with an adjusted timezone.
   * @param dur duration to add to the timezone (if {@code null}, assign implicit timezone)
   * @param undefined invalidate timezone
   * @param info input info (can be {@code null})
   * @return new item
   * @throws QueryException query exception
   */
  public abstract ADate timeZone(DTDur dur, boolean undefined, InputInfo info)
      throws QueryException;

  /**
   * Adjusts the timezone.
   * @param dur duration to add to the timezone (if {@code null}, assign implicit timezone)
   * @param undefined invalidate timezone
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  protected void tz(final DTDur dur, final boolean undefined, final InputInfo info)
      throws QueryException {
    final short t;
    if(undefined) {
      t = Short.MAX_VALUE;
    } else {
      if(dur == null) {
        final Calendar c = Calendar.getInstance();
        t = (short) ((c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET)) / 60000);
      } else {
        t = (short) (dur.minute() + dur.hour() * 60);
        if(dur.sec().signum() != 0) throw ZONESEC_X.get(info, dur);
        if(Math.abs(t) > 60 * 14 || dur.day() != 0) throw INVALZONE_X.get(info, dur);
      }

      // change time if two competing time zones exist
      if(tz != Short.MAX_VALUE) add(BigDecimal.valueOf(60L * (t - tz)));
    }
    tz = t;
  }

  @Override
  public final long yea() {
    return year > 0 ? year : year - 1;
  }

  @Override
  public final long mon() {
    return month + 1;
  }

  @Override
  public final long day() {
    return day + 1;
  }

  @Override
  public final long hour() {
    return hour;
  }

  @Override
  public final long minute() {
    return minute;
  }

  @Override
  public final BigDecimal sec() {
    return seconds == null ? BigDecimal.ZERO : seconds;
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
    final boolean ymd = year != Long.MAX_VALUE;
    if(ymd) {
      if(year <= 0) tb.add('-');
      prefix(tb, Math.abs(yea()), 4);
      tb.add('-');
      prefix(tb, mon(), 2);
      tb.add('-');
      prefix(tb, day(), 2);
    }
    if(hour >= 0) {
      if(ymd) tb.add('T');
      prefix(tb, hour(), 2);
      tb.add(':');
      prefix(tb, minute(), 2);
      tb.add(':');
      if(seconds.intValue() < 10) tb.add('0');
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
  public final boolean equal(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return compare(item, ii) == 0;
  }

  @Override
  public final boolean deepEqual(final Item item, final DeepEqual deep) throws QueryException {
    return type == item.type && compare(item, deep.info) == 0 &&
      (!deep.options.get(DeepEqualOptions.TIMEZONES) || tz == ((ADate) item).tz);
  }

  @Override
  public final boolean atomicEqual(final Item item) throws QueryException {
    return type == item.type && compare(item, null) == 0 && hasTz() == ((ADate) item).hasTz();
  }

  @Override
  public final int hash() {
    return seconds().intValue();
  }

  /**
   * {@inheritDoc}
   * Overwritten by {@link GDt}.
   */
  @Override
  public int compare(final Item item, final Collation coll, final boolean transitive,
      final InputInfo ii) throws QueryException {
    return compare(item, ii);
  }

  /**
   * Compares the current and the specified item.
   * See {@link Item#compare(Item, Collation, boolean, InputInfo)}.
   * @param item item to be compared
   * @param info input info (can be {@code null})
   * @return result of comparison (-1, 0, 1)
   * @throws QueryException query exception
   */
  private int compare(final Item item, final InputInfo info) throws QueryException {
    final ADate dat = (ADate) (item instanceof ADate ? item : type.cast(item, null, null, info));
    return seconds().compareTo(dat.seconds());
  }

  @Override
  public final XMLGregorianCalendar toJava() {
    return DF.newXMLGregorianCalendar(
      year == Long.MAX_VALUE ? null : BigInteger.valueOf(year > 0 ? year : year - 1),
      month >= 0 ? month + 1 : Integer.MIN_VALUE,
      day >= 0 ? day + 1 : Integer.MIN_VALUE,
      hour >= 0 ? hour : Integer.MIN_VALUE,
      minute >= 0 ? minute : Integer.MIN_VALUE,
      seconds != null ? seconds.intValue() : Integer.MIN_VALUE,
      seconds != null ? seconds.remainder(BigDecimal.ONE) : null,
      tz == Short.MAX_VALUE ? Integer.MIN_VALUE : tz);
  }

  /**
   * Returns the date in seconds.
   * @return seconds
   */
  final BigDecimal seconds() {
    return daySeconds().add(days().multiply(BD_864000));
  }

  /**
   * Returns the seconds of one day.
   * @return seconds
   */
  public final BigDecimal daySeconds() {
    int z = tz;
    if(z == Short.MAX_VALUE) {
      final long n = System.currentTimeMillis();
      z = Calendar.getInstance().getTimeZone().getOffset(n) / 60000;
    }
    return (seconds == null ? BigDecimal.ZERO : seconds).add(
        BigDecimal.valueOf(Math.max(0, hour) * 3600L + Math.max(0, minute) * 60L - z * 60L));
  }

  /**
   * Returns a day count.
   * @return days
   */
  final BigDecimal days() {
    final long y = year == Long.MAX_VALUE ? 1 : year;
    return days(y + ADD_NEG, Math.max(month, 0), Math.max(day, 0));
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
    BigDecimal d = days, t = d.add(BD_36525).multiply(BD_4).
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
   * Returns the number of days of the specified month, considering leap years.
   * @param year year
   * @param month month
   * @return days
   */
  public static int daysOfMonth(final long year, final int month) {
    final byte days = DAYS[month];
    return month == 1 && year % 4 == 0 && (year % 100 != 0 || year % 400 == 0) ? days + 1 : days;
  }

  @Override
  public final boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof ADate)) return false;
    final ADate dat = (ADate) obj;
    return type.eq(dat.type) && year == dat.year && month == dat.month && day == dat.day &&
        hour == dat.hour && minute == dat.minute && tz == dat.tz &&
        Objects.equals(seconds, dat.seconds);
  }

  @Override
  public final void toString(final QueryString qs) {
    qs.quoted(string(null));
  }
}
