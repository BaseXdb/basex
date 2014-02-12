package org.basex.util;

import java.text.*;
import java.util.*;

/**
 * This class contains static, thread-safe methods for parsing and formatting
 * dates and times.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class DateTime {
  /** Date pattern. */
  public static final String PATTERN = "-\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}";
  /** Full date format. */
  public static final SimpleDateFormat FULL =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  /** Date format without milliseconds and timestamp. */
  public static final SimpleDateFormat DATETIME = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
  /** Date format. */
  public static final SimpleDateFormat DATE = new SimpleDateFormat("yyyy-MM-dd");
  /** Time format. */
  public static final SimpleDateFormat TIME = new SimpleDateFormat("HH:mm:ss.SSS");
  /** Time zone. */
  public static final SimpleDateFormat ZONE = new SimpleDateFormat("Z");

  static { FULL.setTimeZone(TimeZone.getTimeZone("UTC")); }

  /** Hidden constructor. */
  private DateTime() { }

  /**
   * Parses the specified date and returns its time in milliseconds.
   * Returns {@code null} if it cannot be converted.
   * @param date date to be parsed
   * @return time in milliseconds
   */
  public static long parse(final String date) {
    try {
      return parse(date, FULL).getTime();
    } catch(final ParseException ex) {
      Util.errln(ex);
      return 0;
    }
  }

  /**
   * Thread-safe method to create a string from a given date in a given format.
   * @param format date format
   * @param date date
   * @return string with the formatted date
   */
  public static synchronized String format(final Date date, final DateFormat format) {
    return format.format(date);
  }

  /**
   * Thread-safe method to parse a date from a string in a given format.
   * @param date string representing a date
   * @param format date format
   * @return parsed date
   * @throws ParseException if the string cannot be parsed
   */
  public static synchronized Date parse(final String date, final DateFormat format)
      throws ParseException {
    return format.parse(date);
  }
}
