package org.basex.util;

import java.time.*;
import java.time.format.*;

/**
 * This class contains static, thread-safe methods for parsing and formatting dates and times.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DateTime {
  /** Date and time format without milliseconds and timezone. */
  public static final DateTimeFormatter DATETIME =
      DateTimeFormatter.ofPattern("uuuu-MM-dd-HH-mm-ss");
  /** Date format. */
  public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd");
  /** Time format. */
  public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
  /** Timezone format. */
  public static final DateTimeFormatter ZONE = DateTimeFormatter.ofPattern("xxx");
  /** Full date and time format (UTC). */
  private static final DateTimeFormatter FULL =
      DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

  /** Hidden constructor. */
  private DateTime() { }

  /**
   * Parses a local date and time. The {@link #DATETIME} format is the only supported one.
   * @param date string representing a date
   * @return milliseconds since 1970-01-01T00:00:00Z, or {@code 0} if the input cannot be parsed
   */
  public static long parse(final String date) {
    try {
      return LocalDateTime.parse(date, DATETIME).atZone(ZoneId.systemDefault()).
          toInstant().toEpochMilli();
    } catch(final DateTimeParseException ex) {
      Util.errln(ex);
      return 0;
    }
  }

  /**
   * Creates a full UTC string representation for the specified time.
   * @param ms milliseconds since 1970-01-01T00:00:00Z
   * @return string with the formatted date
   */
  public static String format(final long ms) {
    return FULL.format(Instant.ofEpochMilli(ms));
  }

  /**
   * Returns the offset of the system timezone at the specified time.
   * The zone is resolved on each call, as it can be changed at runtime.
   * @param ms milliseconds since 1970-01-01T00:00:00Z
   * @return offset in seconds
   */
  public static int offset(final long ms) {
    return ZoneId.systemDefault().getRules().getOffset(Instant.ofEpochMilli(ms)).getTotalSeconds();
  }
}
