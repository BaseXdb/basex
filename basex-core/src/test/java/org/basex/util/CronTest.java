package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;

import org.basex.core.*;
import org.junit.jupiter.api.*;

/**
 * Cron expression tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CronTest {
  /**
   * Tests the fields of an expression.
   * @throws BaseXException unexpected error
   */
  @Test public void fields() throws BaseXException {
    // minute, hour, day of month, month, day of week
    next("* * * * *", "2026-07-22T09:41:12", "2026-07-22T09:42:00");
    next("15 * * * *", "2026-07-22T09:41:12", "2026-07-22T10:15:00");
    next("15 * * * *", "2026-07-22T09:14:59", "2026-07-22T09:15:00");
    next("0 3 * * *", "2026-07-22T09:41:12", "2026-07-23T03:00:00");
    next("0 3 1 * *", "2026-07-22T09:41:12", "2026-08-01T03:00:00");
    next("0 3 * 1 *", "2026-07-22T09:41:12", "2027-01-01T03:00:00");
    // optional seconds field
    next("30 * * * * *", "2026-07-22T09:41:12", "2026-07-22T09:41:30");
    next("30 * * * * *", "2026-07-22T09:41:30", "2026-07-22T09:42:30");
    next("*/20 * * * * *", "2026-07-22T09:41:12", "2026-07-22T09:41:20");
  }

  /**
   * Tests lists, ranges, steps and names.
   * @throws BaseXException unexpected error
   */
  @Test public void syntax() throws BaseXException {
    next("0,30 * * * *", "2026-07-22T09:41:12", "2026-07-22T10:00:00");
    next("0,30 * * * *", "2026-07-22T09:11:12", "2026-07-22T09:30:00");
    next("*/15 * * * *", "2026-07-22T09:41:12", "2026-07-22T09:45:00");
    next("0 9-17 * * *", "2026-07-22T18:41:12", "2026-07-23T09:00:00");
    next("0 9-17/4 * * *", "2026-07-22T09:41:12", "2026-07-22T13:00:00");
    // 'a/n' extends to the maximum
    next("0 10/6 * * *", "2026-07-22T09:41:12", "2026-07-22T10:00:00");
    next("0 10/6 * * *", "2026-07-22T11:41:12", "2026-07-22T16:00:00");
    // ranges may wrap around
    next("0 22-2 * * *", "2026-07-22T09:41:12", "2026-07-22T22:00:00");
    next("0 22-2 * * *", "2026-07-22T23:41:12", "2026-07-23T00:00:00");
    // month and weekday names, case-insensitive
    next("0 0 1 JAN *", "2026-07-22T09:41:12", "2027-01-01T00:00:00");
    next("0 0 * * mon", "2026-07-22T09:41:12", "2026-07-27T00:00:00");
    next("0 0 * * Mon-Fri", "2026-07-25T09:41:12", "2026-07-27T00:00:00");
    // both 0 and 7 denote Sunday
    next("0 0 * * 0", "2026-07-22T09:41:12", "2026-07-26T00:00:00");
    next("0 0 * * 7", "2026-07-22T09:41:12", "2026-07-26T00:00:00");
  }

  /**
   * Tests the interaction of day of month and day of week.
   * @throws BaseXException unexpected error
   */
  @Test public void days() throws BaseXException {
    // only one field is restricted: conjunction
    next("0 0 13 * *", "2026-07-22T09:41:12", "2026-08-13T00:00:00");
    next("0 0 * * fri", "2026-07-22T09:41:12", "2026-07-24T00:00:00");
    // both fields are restricted: disjunction (the 13th, and every Friday)
    next("0 0 13 * fri", "2026-07-22T09:41:12", "2026-07-24T00:00:00");
    next("0 0 13 * fri", "2026-07-31T09:41:12", "2026-08-07T00:00:00");
    // '?' is a synonym for '*'
    next("0 0 13 * ?", "2026-07-22T09:41:12", "2026-08-13T00:00:00");
    // a field that matches all values is not a restriction, however it is written
    next("0 0 13 * */1", "2026-07-22T09:41:12", "2026-08-13T00:00:00");
    next("0 0 13 * 0-6", "2026-07-22T09:41:12", "2026-08-13T00:00:00");
    next("0 0 1-31 * fri", "2026-07-22T09:41:12", "2026-07-24T00:00:00");
  }

  /**
   * Tests expressions with long gaps between two executions.
   * @throws BaseXException unexpected error
   */
  @Test public void leapYears() throws BaseXException {
    next("0 0 29 2 *", "2026-07-22T09:41:12", "2028-02-29T00:00:00");
    // 2100 is no leap year: the gap between two executions is eight years
    next("0 0 29 2 *", "2096-03-01T00:00:00", "2104-02-29T00:00:00");
    // a running job steps on from the exact occurrence: the gap is eight years to the second
    next("0 0 29 2 *", "2096-02-29T00:00:00", "2104-02-29T00:00:00");
  }

  /**
   * Tests how occurrences are projected onto a time zone with daylight saving.
   * @throws BaseXException unexpected error
   */
  @Test public void daylightSaving() throws BaseXException {
    // 2027-03-28: 02:00 is followed by 03:00; 2026-10-25: 02:00 occurs twice
    final ZoneId zone = ZoneId.of("Europe/Berlin");
    final Cron cron = new Cron("30 2 * * *");

    // the occurrence is skipped by the clock: it is triggered at the end of the gap
    final LocalDateTime gap = cron.next(LocalDateTime.parse("2027-03-27T12:00:00"));
    assertEquals(LocalDateTime.parse("2027-03-28T02:30:00"), gap);
    assertEquals("2027-03-28T03:30+02:00", gap.atZone(zone).toOffsetDateTime().toString());

    // the occurrence happens twice: it is triggered once, at the first of the two
    final LocalDateTime overlap = cron.next(LocalDateTime.parse("2026-10-24T12:00:00"));
    assertEquals(LocalDateTime.parse("2026-10-25T02:30:00"), overlap);
    assertEquals("2026-10-25T02:30+02:00", overlap.atZone(zone).toOffsetDateTime().toString());

    // the wall-clock time is unchanged on both days
    assertEquals(LocalDateTime.parse("2027-03-29T02:30:00"), cron.next(gap));
    assertEquals(LocalDateTime.parse("2026-10-26T02:30:00"), cron.next(overlap));
  }

  /** Whitespace is normalized. */
  @Test public void normalized() {
    assertEquals("0 8 * * MON-FRI", cron("  0   8 *\t* MON-FRI ").toString());
  }

  /** Expressions that never match. */
  @Test public void unsatisfiable() {
    // February 30
    assertNull(cron("0 0 30 2 *").next(LocalDateTime.parse("2026-07-22T09:41:12")));
  }

  /** Invalid expressions. */
  @Test public void errors() {
    error("");
    error("* * * *");
    error("* * * * * * *");
    error("60 * * * *");
    error("* 24 * * *");
    error("* * 0 * *");
    error("* * 32 * *");
    error("* * * 13 *");
    error("* * * * 8");
    error("* * * xyz *");
    error("*/0 * * * *");
    error("*/x * * * *");
    // empty components
    error("1,,2 * * * *");
    error("1, * * * *");
    error(",1 * * * *");
  }

  /**
   * Checks the next execution time of an expression.
   * @param expression cron expression
   * @param from point in time
   * @param expected expected result
   * @throws BaseXException unexpected error
   */
  private static void next(final String expression, final String from, final String expected)
      throws BaseXException {
    final LocalDateTime result = new Cron(expression).next(LocalDateTime.parse(from));
    assertEquals(LocalDateTime.parse(expected), result, expression + ", from " + from);
  }

  /**
   * Returns a cron instance.
   * @param expression cron expression
   * @return instance
   */
  private static Cron cron(final String expression) {
    return assertDoesNotThrow(() -> new Cron(expression));
  }

  /**
   * Checks that an expression is rejected.
   * @param expression cron expression
   */
  private static void error(final String expression) {
    assertThrows(BaseXException.class, () -> new Cron(expression), expression);
  }
}
