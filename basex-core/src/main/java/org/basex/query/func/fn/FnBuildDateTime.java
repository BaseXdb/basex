package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.BasicType.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class FnBuildDateTime extends DateTimeFn {
  /** Field name: year.     */
  private static final Str YEAR     = Str.get("year");
  /** Field name: month.    */
  private static final Str MONTH    = Str.get("month");
  /** Field name: day.      */
  private static final Str DAY      = Str.get("day");
  /** Field name: hours.    */
  private static final Str HOURS    = Str.get("hours");
  /** Field name: minutes.  */
  private static final Str MINUTES  = Str.get("minutes");
  /** Field name: seconds.  */
  private static final Str SECONDS  = Str.get("seconds");
  /** Field name: timezone. */
  private static final Str TIMEZONE = Str.get("timezone");

  /** Mask value: year.    */
  private static final int Y  = 1;
  /** Mask value: month.   */
  private static final int MO = 2;
  /** Mask value: day.     */
  private static final int D  = 4;
  /** Mask value: hours.   */
  private static final int H  = 8;
  /** Mask value: minutes. */
  private static final int MI = 16;
  /** Mask value: seconds. */
  private static final int S  = 32;

  /** Constant for 60, used in time validation. */
  private static final BigDecimal BD_60 = BigDecimal.valueOf(60);

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = arg(0).item(qc, info);
    if(value.isEmpty()) return Empty.VALUE;

    final XQMap map = toRecord(value, Records.DATETIME.get(), qc);
    final Long       year    = getLong(map, YEAR);
    final Long       month   = getLong(map, MONTH);
    final Long       day     = getLong(map, DAY);
    final Long       hours   = getLong(map, HOURS);
    final Long       minutes = getLong(map, MINUTES);
    final BigDecimal seconds = getDecimal(map, SECONDS);
    final DTDur      tz      = getDuration(map, TIMEZONE);
    checkTz(tz, info);

    final int mask =
        (year    != null ? Y  : 0) |
        (month   != null ? MO : 0) |
        (day     != null ? D  : 0) |
        (hours   != null ? H  : 0) |
        (minutes != null ? MI : 0) |
        (seconds != null ? S  : 0);

    final BasicType type = switch(mask) {
      case Y | MO | D | H | MI | S -> {
        checkDate(year, month, day, info);
        checkTime(hours, minutes, seconds, info);
        yield tz != null ? DATE_TIME_STAMP : DATE_TIME;
      }
      case Y | MO | D -> {
        checkDate(year, month, day, info);
        yield DATE;
      }
      case Y -> {
        checkYear(year, info);
        yield G_YEAR;
      }
      case Y | MO -> {
        checkYear(year, info);
        checkMonth(month, info);
        yield G_YEAR_MONTH;
      }
      case MO -> {
        checkMonth(month, info);
        yield G_MONTH;
      }
      case MO | D -> {
        checkMonthDay(month, day, info);
        yield G_MONTH_DAY;
      }
      case D -> {
        checkDayOnly(day, info);
        yield G_DAY;
      }
      case H | MI | S -> {
        checkTime(hours, minutes, seconds, info);
        yield TIME;
      }
      default -> throw INVDATETIMEFIELDS_X.get(info, map);
    };
    return Dtm.buildUnchecked(type, year, month, day, hours, minutes, seconds, tz, info);
  }

  /**
   * Retrieves an item from the map for the specified key, returning {@code null} if the key is not
   * present or if the value is empty.
   * @param map the map to retrieve the item from
   * @param key the key to look up in the map
   * @return the item associated with the key, or {@code null} if the key is not present or if the
   *         value is empty
   * @throws QueryException if an error occurs while retrieving the item from the map
   */
  private static Item getItem(final XQMap map, final Str key) throws QueryException {
    final Value v = map.getOrNull(key);
    return v == null || v.isEmpty() ? null : v.itemAt(0);
  }

  /**
   * Retrieves a long value from the map for the specified key, returning {@code null} if the key is
   * not present or if the value is empty.
   * @param map the map to retrieve the long value from
   * @param key the key to look up in the map
   * @return the long value associated with the key, or {@code null} if the key is not present or if
   *         the value is empty
   * @throws QueryException if an error occurs while retrieving the long value from the map
   */
  private static Long getLong(final XQMap map, final Str key) throws QueryException {
    final Item item = getItem(map, key);
    return item == null ? null : ((ANum) item).itr();
  }

  /**
   * Retrieves a BigDecimal value from the map for the specified key, returning {@code null} if the
   * key is not present or if the value is empty.
   * @param map the map to retrieve the BigDecimal value from
   * @param key the key to look up in the map
   * @return the BigDecimal value associated with the key, or {@code null} if the key is not present
   *         or if the value is empty
   * @throws QueryException if an error occurs while retrieving the BigDecimal value from the map
   */
  private static BigDecimal getDecimal(final XQMap map, final Str key) throws QueryException {
    final Item item = getItem(map, key);
    return item == null ? null : item.dec(null);
  }

  /**
   * Retrieves a DTDur value from the map for the specified key, returning {@code null} if the key
   * is not present or if the value is empty.
   * @param map the map to retrieve the DTDur value from
   * @param key the key to look up in the map
   * @return the DTDur value associated with the key, or {@code null} if the key is not present or
   *         if the value is empty
   * @throws QueryException if an error occurs while retrieving the DTDur value from the map
   */
  private static DTDur getDuration(final XQMap map, final Str key) throws QueryException {
    final Item item = getItem(map, key);
    return item == null ? null : (DTDur) item;
  }

  /**
   * Checks the year component for validity.
   * @param year the year to check
   * @param info input info for error reporting
   * @throws QueryException if the year is out of range
   */
  private static void checkYear(final long year, final InputInfo info) throws QueryException {
    if(year <= ADate.MIN_YEAR || year > ADate.MAX_YEAR)
      throw INVDATETIMEVALUE_X_X.get(info, YEAR, year);
  }

  /**
   * Checks the month component for validity.
   * @param month the  month to check
   * @param info input info for error reporting
   * @throws QueryException if the month is out of range (not between 1 and 12)
   */
  private static void checkMonth(final long month, final InputInfo info) throws QueryException {
    if(month < 1 || month > 12) throw INVDATETIMEVALUE_X_X.get(info, MONTH, month);
  }

  /**
   * Checks the day component for validity, without considering month/year context.
   * @param day the day to check
   * @param info input info for error reporting
   * @throws QueryException if the day is out of range (not between 1 and 31)
   */
  private static void checkDayOnly(final long day, final InputInfo info) throws QueryException {
    if(day < 1 || day > 31)  throw INVDATETIMEVALUE_X_X.get(info, DAY, day);
  }

  /**
   * Checks the month and day components for validity in the context of a month (but without year
   * context).
   * @param month the month to check
   * @param day the day to check
   * @param info input info for error reporting
   * @throws QueryException if the month is out of range or if the day is out of range for the given
   *           month, allowing February 29
   */
  private static void checkMonthDay(final long month, final long day, final InputInfo info)
      throws QueryException {
    checkMonth(month, info);
    checkDayOnly(day, info);
    if(day < 1 || day > ADate.DAYS[(int) month - 1] + (month == 2 ? 1 : 0))
      throw INVDATETIMEVALUE_X_X.get(info, DAY, day);
  }

  /**
   * Checks the year, month, and day components for validity in the context of a complete date.
   * @param year the year to check
   * @param month the month to check
   * @param day the day to check
   * @param info input info for error reporting
   * @throws QueryException if the year, month, or day is out of range, or if the day is out of
   *           range for the given month and year
   */
  private static void checkDate(final long year, final long month, final long day,
      final InputInfo info) throws QueryException {
    checkYear(year, info);
    checkMonth(month, info);
    checkDayOnly(day, info);
    final int dom = ADate.daysOfMonth(year, (int) month - 1);
    if(day > dom) throw INVDATETIMEVALUE_X_X.get(info, DAY, day);
  }

  /**
   * Checks the hours, minutes, and seconds components for validity in the context of a complete
   * time.
   * @param hours the hours to check
   * @param minutes the minutes to check
   * @param seconds the seconds to check
   * @param info input info for error reporting
   * @throws QueryException if the hours, minutes, or seconds are out of range (hours not between 0
   *           and 23, minutes not between 0 and 59, seconds not between 0 and 60)
   */
  private static void checkTime(final long hours, final long minutes, final BigDecimal seconds,
      final InputInfo info) throws QueryException {
    if(hours < 0 || hours > 23) throw INVDATETIMEVALUE_X_X.get(info, HOURS, hours);
    if(minutes < 0 || minutes > 59) throw INVDATETIMEVALUE_X_X.get(info, MINUTES, minutes);
    if(seconds.compareTo(BigDecimal.ZERO) < 0 || seconds.compareTo(BD_60) >= 0)
      throw INVDATETIMEVALUE_X_X.get(info, SECONDS, seconds);
  }

  /**
   * Checks the timezone component for validity.
   * @param tz the timezone to check
   * @param info input info for error reporting
   * @throws QueryException if the timezone is out of range (not between -14:00 and +14:00, or if it
   *           has non-zero day or second components)
   */
  private static void checkTz(final DTDur tz, final InputInfo info) throws QueryException {
    if(tz != null && (tz.day() != 0 || tz.seconds().signum() != 0
        || Math.abs(tz.minute() + tz.hour() * 60) > 14 * 60)) {
      throw INVDATETIMEVALUE_X_X.get(info, TIMEZONE, tz);
    }
  }
}
