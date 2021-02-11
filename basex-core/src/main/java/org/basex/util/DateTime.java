package org.basex.util;

import java.text.*;
import java.util.*;

/**
 * This class contains static, thread-safe methods for parsing and formatting dates and times.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DateTime {
  /** Date format without milliseconds and timestamp (uses UTC time zone). */
  public static final SimpleDateFormat DATETIME = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
  /** Full date format. */
  public static final SimpleDateFormat FULL = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
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
   * Parses a string and produces a date object.
   * Returns the standard base time if it cannot be converted.
   * @param date string representing a date
   * @return parsed date
   */
  public static synchronized Date parse(final String date) {
    try {
      return (date.contains(":") ? FULL : DATETIME).parse(date);
    } catch(final ParseException ex) {
      Util.errln(ex);
      return new Date(0);
    }
  }

  /**
   * Creates a full string representation for the specified date.
   * @param date date
   * @return string with the formatted date
   */
  public static synchronized String format(final Date date) {
    return format(date, FULL);
  }

  /**
   * Creates a string representation for a date in the specified format.
   * @param format date format
   * @param date date
   * @return string with the formatted date
   */
  public static synchronized String format(final Date date, final DateFormat format) {
    return format.format(date);
  }
}
