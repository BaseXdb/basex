package org.basex.util;

import java.time.*;
import java.util.*;

import org.basex.core.*;

/**
 * Cron expression: parses a schedule pattern and computes subsequent execution times.
 * All computations are based on local wall-clock time; time zones are applied by the caller.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Cron {
  /** Month names. */
  private static final String[] MONTHS = {
    "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec" };
  /** Weekday names (Sunday first). */
  private static final String[] WEEKDAYS = {
    "sun", "mon", "tue", "wed", "thu", "fri", "sat" };
  /** Years to scan before an expression is treated as unsatisfiable. */
  private static final int MAX_YEARS = 8;

  /** Original expression. */
  private final String expression;
  /** Matching seconds (0-59). */
  private final long seconds;
  /** Matching minutes (0-59). */
  private final long minutes;
  /** Matching hours (0-23). */
  private final long hours;
  /** Matching days of the month (1-31). */
  private final long days;
  /** Matching months (1-12). */
  private final long months;
  /** Matching weekdays (0-6, Sunday first). */
  private final long weekdays;
  /** Days and weekdays are both restricted: a match of either of them is sufficient. */
  private final boolean either;

  /**
   * Constructor.
   * @param expression cron expression with 5 fields, or 6 fields if seconds are supplied
   * @throws BaseXException expression is invalid
   */
  public Cron(final String expression) throws BaseXException {
    final String[] fields = expression.trim().split("\\s+");
    final int fl = fields.length;
    if(fl != 5 && fl != 6) throw new BaseXException("5 or 6 fields expected, found %", fl);
    this.expression = String.join(" ", fields);

    final int f = fl - 5;
    // without a seconds field, only second 0 is matched
    seconds = f == 1 ? field(fields[0], 0, 59, null) : 1;
    minutes = field(fields[f], 0, 59, null);
    hours = field(fields[f + 1], 0, 23, null);
    days = field(fields[f + 2], 1, 31, null);
    months = field(fields[f + 3], 1, 12, MONTHS);
    weekdays = field(fields[f + 4], 0, 7, WEEKDAYS);
    // if both day fields are restricted, a match of either of them is sufficient
    either = !all(days, 1, 31) && !all(weekdays, 0, 6);
  }

  /**
   * Returns the next execution time after the specified one.
   * @param from point in time (exclusive)
   * @return next execution time, or {@code null} if the expression will never match again
   */
  public LocalDateTime next(final LocalDateTime from) {
    // inclusive limit: a repeated job steps on from its own occurrence, and the longest gap
    // between two executions ('0 0 29 2 *' across 2100) is exactly MAX_YEARS
    final LocalDateTime limit = from.plusYears(MAX_YEARS);
    LocalDateTime dt = from.withNano(0).plusSeconds(1);
    while(!dt.isAfter(limit)) {
      if(!matches(months, dt.getMonthValue())) {
        dt = dt.plusMonths(1).withDayOfMonth(1).toLocalDate().atStartOfDay();
      } else if(!day(dt)) {
        dt = dt.plusDays(1).toLocalDate().atStartOfDay();
      } else if(!matches(hours, dt.getHour())) {
        final int s = skip(hours, dt.getHour());
        dt = s == 0 ? dt.plusDays(1).toLocalDate().atStartOfDay() :
          dt.plusHours(s).withMinute(0).withSecond(0);
      } else if(!matches(minutes, dt.getMinute())) {
        final int s = skip(minutes, dt.getMinute());
        dt = s == 0 ? dt.plusHours(1).withMinute(0).withSecond(0) :
          dt.plusMinutes(s).withSecond(0);
      } else if(!matches(seconds, dt.getSecond())) {
        final int s = skip(seconds, dt.getSecond());
        dt = s == 0 ? dt.plusMinutes(1).withSecond(0) : dt.plusSeconds(s);
      } else {
        return dt;
      }
    }
    return null;
  }

  /**
   * Checks if the day of the specified date is matched.
   * @param dt date
   * @return result of check
   */
  private boolean day(final LocalDateTime dt) {
    final boolean d = matches(days, dt.getDayOfMonth());
    final boolean w = matches(weekdays, dt.getDayOfWeek().getValue() % 7);
    return either ? d || w : d && w;
  }

  /**
   * Checks if a value is contained in a bit set.
   * @param set bit set
   * @param value value
   * @return result of check
   */
  private static boolean matches(final long set, final int value) {
    return (set >> value & 1) != 0;
  }

  /**
   * Returns the distance to the next larger value of a bit set.
   * @param set bit set
   * @param value current value
   * @return distance, or {@code 0} if the set has no larger value
   */
  private static int skip(final long set, final int value) {
    final long rest = set >>> value + 1;
    return rest == 0 ? 0 : Long.numberOfTrailingZeros(rest) + 1;
  }

  /**
   * Checks if a bit set contains all values of a range.
   * @param set bit set
   * @param min minimum value
   * @param max maximum value
   * @return result of check
   */
  private static boolean all(final long set, final int min, final int max) {
    final long range = (1L << max - min + 1) - 1 << min;
    return (set & range) == range;
  }

  /**
   * Checks if a field denotes all values.
   * @param field field
   * @return result of check
   */
  private static boolean wildcard(final String field) {
    return Strings.eq(field, "*", "?");
  }

  /**
   * Parses a single field and returns its values as a bit set.
   * @param field field
   * @param min minimum value
   * @param max maximum value
   * @param names value names (can be {@code null})
   * @return bit set
   * @throws BaseXException field is invalid
   */
  private static long field(final String field, final int min, final int max, final String[] names)
      throws BaseXException {

    long set = 0;
    // empty components are preserved, and rejected as invalid values
    for(final String part : Strings.split(field, ',')) {
      // step: last component of a part
      final int s = part.indexOf('/');
      int step = 1;
      if(s != -1) {
        step = number(part.substring(s + 1), field);
        if(step < 1) throw new BaseXException("Invalid step in '%'", field);
      }
      // range: single value, wildcard, or first component of a part
      final String range = s == -1 ? part : part.substring(0, s);
      final int d = range.indexOf('-', 1);
      int from, to;
      if(wildcard(range)) {
        from = min;
        to = max;
      } else if(d != -1) {
        from = value(range.substring(0, d), min, max, names, field);
        to = value(range.substring(d + 1), min, max, names, field);
      } else {
        from = value(range, min, max, names, field);
        // 'a/n' extends to the maximum, 'a' without step denotes a single value
        to = s != -1 ? max : from;
      }
      // ranges may wrap around (e.g. '22-2')
      final int size = max - min + 1;
      if(to < from) to += size;
      for(int v = from; v <= to; v += step) set |= 1L << min + (v - min) % size;
    }
    // both 0 and 7 denote Sunday
    return names == WEEKDAYS && matches(set, 7) ? set & ~(1L << 7) | 1 : set;
  }

  /**
   * Parses a single value of a field.
   * @param value value
   * @param min minimum value
   * @param max maximum value
   * @param names value names (can be {@code null})
   * @param field field (for error messages)
   * @return numeric value
   * @throws BaseXException value is invalid
   */
  private static int value(final String value, final int min, final int max, final String[] names,
      final String field) throws BaseXException {

    int v = -1;
    if(names != null) {
      final String name = value.toLowerCase(Locale.ENGLISH);
      final int nl = names.length;
      for(int n = 0; n < nl && v == -1; n++) {
        if(names[n].equals(name)) v = n + min;
      }
    }
    if(v == -1) v = number(value, field);
    if(v < min || v > max) throw new BaseXException("Value out of range in '%': %", field, value);
    return v;
  }

  /**
   * Parses a number.
   * @param value value
   * @param field field (for error messages)
   * @return number
   * @throws BaseXException value is no valid number
   */
  private static int number(final String value, final String field) throws BaseXException {
    final int v = Strings.toInt(value);
    if(v == Integer.MIN_VALUE) throw new BaseXException("Invalid value in '%': %", field, value);
    return v;
  }

  @Override
  public String toString() {
    return expression;
  }
}
