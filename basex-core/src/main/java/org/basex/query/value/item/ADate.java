package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.item.Dec.*;

import java.math.*;
import java.time.*;
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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class ADate extends ADateDur {
  /** Minimum day count. */
  private static final long MIN_DAY = LocalDate.MIN.toEpochDay();
  /** Maximum day count. */
  private static final long MAX_DAY = LocalDate.MAX.toEpochDay();

  /** Pattern for two digits. */
  static final String DD = "(\\d{2})";
  /** Year pattern. */
  static final String YEAR = "(-?([1-9]\\d{3,}|0\\d{3}))";
  /** Date pattern. */
  static final String ZONE = "(([-+])" + DD + ':' + DD + "|Z)?";
  /** Date pattern. */
  public static final Pattern DATE = Pattern.compile(YEAR + '-' + DD + '-' + DD + ZONE);
  /** Time pattern. */
  public static final Pattern TIME = Pattern.compile(
      DD + ':' + DD + ':' + "(\\d{2}(\\.\\d+)?)" + ZONE);

  /** Component flag: year. */
  static final int YEA = 1;
  /** Component flag: month. */
  static final int MON = 2;
  /** Component flag: day. */
  static final int DAY = 4;
  /** Component flag: hours. */
  static final int HRS = 8;
  /** Component flag: minutes. */
  static final int MIN = 16;
  /** Component flag: seconds. */
  static final int SEC = 32;
  /** Component flag: timezone. */
  static final int ZON = 64;

  /** Components that have been assigned. Undefined components keep their initial value. */
  int defined;

  /** Year ({@code 1 - Long#MAX_VALUE-1}: AD, {@code 0 - Long#MIN_VALUE}: BC). */
  long year = Long.MAX_VALUE;
  /** Month ({@code 1-12}). */
  byte month = -1;
  /** Day ({@code 1-31}). */
  byte day = -1;
  /** Hour ({@code 0-59}). */
  byte hour = -1;
  /** Minute ({@code 0-59}). */
  byte minute = -1;
  /** Seconds and milliseconds. */
  BigDecimal seconds;
  /** Timezone in minutes ({@code -14*60-14*60}). */
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
    defined = date.defined;
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
    month = (byte) Strings.toInt(mt.group(3));
    day = (byte) Strings.toInt(mt.group(4));
    defined |= YEA | MON | DAY;

    if(month < 1 || month > 12 || day < 1 || day > daysOfMonth(year, month)) {
      throw dateError(date, exp, info);
    }
    if(year < Year.MIN_VALUE || year > Year.MAX_VALUE) throw DATERANGE_X_X.get(info, type, date);
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
    defined |= HRS | MIN | SEC;
    if(minute >= 60 || seconds.compareTo(BD_60) >= 0 || hour > 24 ||
       hour == 24 && (minute > 0 || seconds.compareTo(BigDecimal.ZERO) > 0)) {
      throw dateError(time, exp, info);
    }
    zone(mt, 5, time, info);
    if(hour == 24) {
      hour = 0;
      add(BD_864000, info);
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
    defined |= ZON;
    if("Z".equals(z)) {
      tz = 0;
    } else {
      final int th = Strings.toInt(matcher.group(pos + 2));
      final int tm = Strings.toInt(matcher.group(pos + 3));
      if(th > 14 || tm > 59 || th == 14 && tm != 0) {
        throw INVALIDVALUE_X_X.get(info, "timezone", value);
      }
      final int mn = th * 60 + tm;
      tz = (short) ("-".equals(matcher.group(pos + 1)) ? -mn : mn);
    }
  }

  /**
   * Adds/subtracts the specified dayTime duration.
   * @param dur duration
   * @param plus plus/minus flag
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  final void calc(final DTDur dur, final boolean plus, final InputInfo info)
      throws QueryException {
    add(plus ? dur.seconds : dur.seconds.negate(), info);
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
    final long mn = month - 1 + m;
    month = (byte) (Math.floorMod(mn, 12) + 1);
    year += Math.floorDiv(mn, 12);
    day = (byte) Math.min(daysOfMonth(year, month), day);

    if(year < Year.MIN_VALUE || year > Year.MAX_VALUE) throw YEARRANGE_X.get(info, year);
  }

  /**
   * Adds the specified dayTime duration.
   * @param add value to be added
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  private void add(final BigDecimal add, final InputInfo info) throws QueryException {
    // normalized modulo: sc % 60  vs.  (-sc + sc % 60 + 60 + sc) % 60
    final BigDecimal sc = seconds().add(add);
    seconds = sc.signum() >= 0 ? sc.remainder(BD_60) :
      sc.negate().add(sc.remainder(BD_60)).add(BD_60).add(sc).remainder(BD_60);

    final long mn = (has(MIN) ? minute : 0) + Math.floorDiv(
        sc.setScale(0, RoundingMode.FLOOR).longValue(), 60);
    minute = (byte) Math.floorMod(mn, 60);

    final long ho = (has(HRS) ? hour : 0) + Math.floorDiv(mn, 60);
    hour = (byte) Math.floorMod(ho, 24);
    defined |= HRS | MIN | SEC;

    // xs:time: the date is undefined and will be discarded
    if(!has(YEA)) return;

    final long dys = days() + Math.floorDiv(ho, 24);
    // 400 years span exactly 146097 days: sufficient for an approximate error message
    if(dys < MIN_DAY || dys > MAX_DAY) throw YEARRANGE_X.get(info, dys * 400 / 146097 + 1970);

    final LocalDate ld = LocalDate.ofEpochDay(dys);
    year = ld.getYear();
    month = (byte) ld.getMonthValue();
    day = (byte) ld.getDayOfMonth();
  }

  /**
   * Creates a new item with an adjusted timezone.
   * @param dur duration to add to the timezone (ignored if {@code undefined} is set)
   * @param undefined invalidate timezone
   * @param info input info (can be {@code null})
   * @return new item
   * @throws QueryException query exception
   */
  public abstract ADate timeZone(DTDur dur, boolean undefined, InputInfo info)
      throws QueryException;

  /**
   * Adjusts the timezone.
   * @param dur duration to add to the timezone (ignored if {@code undefined} is set)
   * @param undefined invalidate timezone
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  protected void tz(final DTDur dur, final boolean undefined, final InputInfo info)
      throws QueryException {
    final short t;
    if(undefined) {
      t = Short.MAX_VALUE;
      defined &= ~ZON;
    } else {
      t = (short) (dur.minute() + dur.hour() * 60);
      if(dur.seconds().signum() != 0) throw ZONESEC_X.get(info, dur);
      if(Math.abs(t) > 60 * 14 || dur.day() != 0) throw INVALIDZONE_X.get(info, dur);

      // change time if two competing time zones exist
      if(has(ZON)) add(BigDecimal.valueOf(60L * (t - tz)), info);
      defined |= ZON;
    }
    tz = t;
  }

  @Override
  public final long yea() {
    return year;
  }

  @Override
  public final long mon() {
    return month;
  }

  @Override
  public final long day() {
    return day;
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
  public final BigDecimal seconds() {
    return has(SEC) ? seconds : BigDecimal.ZERO;
  }

  /**
   * Returns the timezone in minutes.
   * @return time zone
   */
  public final int tz() {
    return tz;
  }

  /**
   * Returns if all specified components are defined.
   * @param components component flags
   * @return result of check
   */
  final boolean has(final int components) {
    return (defined & components) == components;
  }

  /**
   * Returns if the year is defined.
   * @return result of check
   */
  public final boolean hasYear() {
    return has(YEA);
  }

  /**
   * Returns if the month is defined.
   * @return result of check
   */
  public final boolean hasMonth() {
    return has(MON);
  }

  /**
   * Returns if the day is defined.
   * @return result of check
   */
  public final boolean hasDay() {
    return has(DAY);
  }

  /**
   * Returns if the hours are defined.
   * @return result of check
   */
  public final boolean hasHours() {
    return has(HRS);
  }

  /**
   * Returns if the minutes are defined.
   * @return result of check
   */
  public final boolean hasMinutes() {
    return has(MIN);
  }

  /**
   * Returns if the seconds are defined.
   * @return result of check
   */
  public final boolean hasSeconds() {
    return has(SEC);
  }

  /**
   * Returns if the timezone is defined.
   * @return time zone
   */
  public final boolean hasTz() {
    return has(ZON);
  }

  @Override
  public byte[] string(final InputInfo ii) {
    final TokenBuilder tb = new TokenBuilder();
    final boolean ymd = has(YEA);
    if(ymd) {
      if(year < 0) tb.add('-');
      prefix(tb, Math.abs(yea()), 4);
      tb.add('-');
      prefix(tb, mon(), 2);
      tb.add('-');
      prefix(tb, day(), 2);
    }
    if(has(HRS)) {
      if(ymd) tb.add('T');
      prefix(tb, hour(), 2);
      tb.add(':');
      prefix(tb, minute(), 2);
      tb.add(':');
      if(seconds.intValue() < 10) tb.add('0');
      tb.add(Token.chopNumber(Token.token(seconds().abs().toPlainString())));
    }
    zone(tb);
    return tb.finish();
  }

  /**
   * Adds the time zone to the specified token builder.
   * @param tb token builder
   */
  void zone(final TokenBuilder tb) {
    // the offset ID is the XSD lexical form: 'Z', or +/-HH:MM
    if(has(ZON)) tb.add(ZoneOffset.ofTotalSeconds(tz * 60).getId());
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
  public final boolean deepEqual(final Item item, final DeepEqual deep) throws QueryException {
    return (type.instanceOf(item.type) || item.type.instanceOf(type))
        && compare(item, deep.qc, deep.info) == 0
        && (!deep.options.get(DeepEqualOptions.TIMEZONES) || tz == ((ADate) item).tz);
  }

  @Override
  public final boolean atomicEqual(final Item item) throws QueryException {
    // the timezones are compared first: the implicit timezone is never required afterwards
    return this == item || (type.instanceOf(item.type) || item.type.instanceOf(type))
        && hasTz() == ((ADate) item).hasTz() && compare(item, null, null) == 0;
  }

  @Override
  public final int hashCode() {
    // the implicit timezone is irrelevant here: items are only equal if both have or lack a
    // timezone, and if both lack one, it cancels out in the comparison
    return toSeconds(0).intValue();
  }

  @Override
  public final int compare(final Item item, final Collation coll, final boolean transitive,
      final QueryContext qc, final InputInfo ii) throws QueryException {
    return compare(item, qc, ii);
  }

  /**
   * Compares the current and the specified item.
   * See {@link Item#compare(Item, Collation, boolean, QueryContext, InputInfo)}.
   * @param item item to be compared
   * @param qc query context (can be {@code null})
   * @param info input info (can be {@code null})
   * @return result of comparison (-1, 0, 1)
   * @throws QueryException query exception
   */
  private int compare(final Item item, final QueryContext qc, final InputInfo info)
      throws QueryException {
    final ADate dat = (ADate) (item instanceof ADate ? item : type.cast(item, qc, info));
    // resolve the implicit timezone only if exactly one operand has a timezone: if both have or
    // lack one, it is either unused or cancels out in the comparison
    final int implicit = has(ZON) == dat.has(ZON) ? 0 : implicitTz(qc);
    return toSeconds(implicit).compareTo(dat.toSeconds(implicit));
  }

  @Override
  public final XMLGregorianCalendar toJava() {
    return DF.newXMLGregorianCalendar(
      has(YEA) ? BigInteger.valueOf(year) : null,
      has(MON) ? month : Integer.MIN_VALUE,
      has(DAY) ? day : Integer.MIN_VALUE,
      has(HRS) ? hour : Integer.MIN_VALUE,
      has(MIN) ? minute : Integer.MIN_VALUE,
      has(SEC) ? seconds.intValue() : Integer.MIN_VALUE,
      has(SEC) ? seconds.remainder(BigDecimal.ONE) : null,
      has(ZON) ? tz : Integer.MIN_VALUE);
  }

  /**
   * Returns the date in seconds.
   * @param implicit implicit timezone in minutes, applied if the item has none
   * @return seconds
   */
  final BigDecimal toSeconds(final int implicit) {
    return daySeconds(implicit).add(BigDecimal.valueOf(days()).multiply(BD_864000));
  }

  /**
   * Returns the seconds of one day.
   * @param implicit implicit timezone in minutes, applied if the item has none
   * @return seconds
   */
  public final BigDecimal daySeconds(final int implicit) {
    final int z = has(ZON) ? tz : implicit;
    return seconds().add(BigDecimal.valueOf(
        (has(HRS) ? hour : 0) * 3600L + (has(MIN) ? minute : 0) * 60L - z * 60L));
  }

  /**
   * Returns the implicit timezone in minutes.
   * Without a query context, the offset of the system timezone is resolved per call, and can
   * differ from {@code fn:implicit-timezone} if it changes while a query is running.
   * @param qc query context (can be {@code null})
   * @return offset
   * @throws QueryException query exception
   */
  static int implicitTz(final QueryContext qc) throws QueryException {
    return qc != null ? qc.dateTime().zone : DateTime.offset(System.currentTimeMillis()) / 60;
  }

  /**
   * Returns the day count since 1970-01-01.
   * @return days
   */
  final long days() {
    // Reference year for values without year (leap year)
    final int y = has(YEA) ? (int) year : 2000;
    return LocalDate.of(y, has(MON) ? month : 1, 1).toEpochDay() + (has(DAY) ? day - 1 : 0);
  }

  /**
   * Returns the number of days of the specified month, considering leap years.
   * @param year year
   * @param month month ({@code 1-12})
   * @return days
   */
  public static int daysOfMonth(final long year, final int month) {
    return Month.of(month).length(Year.isLeap(year));
  }

  /**
   * Returns the maximum number of days of the specified month, irrespective of the year.
   * February has 29 days, as required by {@code xs:gMonthDay} and {@code xs:gDay}.
   * @param month month ({@code 1-12})
   * @return days
   */
  public static int maxDaysOfMonth(final int month) {
    return Month.of(month).maxLength();
  }

  /**
   * Returns the date components as a local date. See {@link #days()} for undefined components.
   * @return local date
   */
  public final LocalDate toLocalDate() {
    return LocalDate.ofEpochDay(days());
  }

  /**
   * Returns the date and time components as a local date and time.
   * Undefined components are replaced with their minimum, fractional seconds are truncated.
   * @return local date and time
   */
  public final LocalDateTime toLocalDateTime() {
    final BigDecimal sc = seconds();
    return toLocalDate().atTime(has(HRS) ? hour : 0, has(MIN) ? minute : 0, sc.intValue(),
        sc.remainder(BigDecimal.ONE).movePointRight(9).intValue());
  }

  @Override
  public final boolean equals(final Object obj) {
    return this == obj || obj instanceof final ADate dat && type.eq(dat.type) &&
        year == dat.year && month == dat.month && day == dat.day &&
        hour == dat.hour && minute == dat.minute && tz == dat.tz &&
        Objects.equals(seconds, dat.seconds);
  }

  @Override
  public final void toString(final QueryString qs) {
    qs.quoted(string(null));
  }
}
