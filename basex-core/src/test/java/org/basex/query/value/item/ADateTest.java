package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * Date item tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ADateTest extends SandboxTest {
  /** Smallest and largest supported years. */
  @Test public void yearRange() {
    query("string(xs:date('999999999-12-31'))", "999999999-12-31");
    query("string(xs:date('-999999999-01-01'))", "-999999999-01-01");
    query("string(xs:gYear('-999999999'))", "-999999999");
    query("string(xs:dateTime('999999999-12-31T23:59:59'))", "999999999-12-31T23:59:59");

    error("xs:date('1000000000-01-01')", DATERANGE_X_X);
    error("xs:date('-1000000000-01-01')", DATERANGE_X_X);
    error("xs:gYear('1000000000')", DATERANGE_X_X);
    error("xs:date('999999999-12-31') + xs:dayTimeDuration('P1D')", YEARRANGE_X);
    error("xs:date('-999999999-01-01') - xs:dayTimeDuration('P1D')", YEARRANGE_X);
    error("xs:date('999999999-12-31') + xs:yearMonthDuration('P1M')", YEARRANGE_X);
  }

  /** The year range must be identical for casts and fn:build-dateTime. */
  @Test public void buildYearRange() {
    query("string(build-dateTime({ 'year': -999999999, 'month': 1, 'day': 1 }))",
        "-999999999-01-01");
    query("string(build-dateTime({ 'year': 999999999, 'month': 12, 'day': 31 }))",
        "999999999-12-31");

    error("build-dateTime({ 'year': -1000000000, 'month': 1, 'day': 1 })", INVALIDVALUE_X_X);
    error("build-dateTime({ 'year': 1000000000, 'month': 1, 'day': 1 })", INVALIDVALUE_X_X);
  }

  /** Values without a year component are anchored in a leap year. */
  @Test public void noYear() {
    query("xs:gMonthDay('--02-28') lt xs:gMonthDay('--02-29')", true);
    query("xs:gMonthDay('--02-29') lt xs:gMonthDay('--03-01')", true);
    query("count(distinct-values((xs:gMonthDay('--02-29'), xs:gMonthDay('--03-01'))))", 2);
    query("xs:gDay('---01') lt xs:gDay('---31')", true);
    query("xs:gMonth('--01') lt xs:gMonth('--12')", true);
  }

  /** Every accessor returns the empty sequence for a component the type does not have. */
  @Test public void accessors() {
    // one row per type: the components it does NOT have must all be empty
    query("xs:time('23:59:59') ! (year-from-dateTime(.), month-from-dateTime(.), " +
        "day-from-dateTime(.)) => empty()", true);
    query("xs:gYear('2024') ! (month-from-dateTime(.), day-from-dateTime(.), " +
        "hours-from-dateTime(.), minutes-from-dateTime(.), seconds-from-dateTime(.)) => empty()",
        true);
    query("xs:gYearMonth('2024-10') ! (day-from-dateTime(.), hours-from-dateTime(.)) => empty()",
        true);
    query("xs:gMonth('--10') ! (year-from-dateTime(.), day-from-dateTime(.)) => empty()", true);
    query("xs:gMonthDay('--10-08') ! (year-from-dateTime(.), hours-from-dateTime(.)) => empty()",
        true);
    query("xs:gDay('---08') ! (year-from-dateTime(.), month-from-dateTime(.)) => empty()", true);
    query("xs:date('2024-10-08') ! (hours-from-dateTime(.), minutes-from-dateTime(.), " +
        "seconds-from-dateTime(.)) => empty()", true);

    // the components a type does have are returned, including the lowest legal values
    query("xs:gMonth('--01') => month-from-dateTime()", 1);
    query("xs:gDay('---01') => day-from-dateTime()", 1);
    query("xs:dateTime('2024-10-08T00:00:00') ! (hours-from-dateTime(.), " +
        "minutes-from-dateTime(.), seconds-from-dateTime(.))", "0\n0\n0");
    query("parts-of-dateTime(xs:time('23:59:59'))?day => empty()", true);
    query("parts-of-dateTime(xs:gDay('---08'))?day", 8);
  }

  /** Components are reported as present or absent, never as a sentinel value. */
  @Test public void components() {
    // absent timezone must be distinguishable from a zero timezone
    query("empty(timezone-from-date(xs:date('2026-07-22')))", true);
    query("timezone-from-date(xs:date('2026-07-22Z'))", "PT0S");
    query("empty(xs:time('12:00:00') => timezone-from-time())", true);
    // casting drops exactly the components the target type does not have
    query("string(xs:gYearMonth(xs:date('2026-07-22Z')))", "2026-07Z");
    query("string(xs:gDay(xs:date('2026-07-22')))", "---22");
    query("string(xs:time(xs:dateTime('2026-07-22T12:00:00Z')))", "12:00:00Z");
    // arithmetic on a date must not resurrect time components
    query("string(xs:date('2026-07-22') + xs:dayTimeDuration('PT25H'))", "2026-07-23");
    // adjusting a timezone can add, change and remove it
    query("string(adjust-date-to-timezone(xs:date('2026-07-22'), xs:dayTimeDuration('PT2H')))",
        "2026-07-22+02:00");
    query("string(adjust-date-to-timezone(xs:date('2026-07-22Z'), ()))", "2026-07-22");
    // fn:dateTime reconciles the timezones of both arguments
    query("string(dateTime(xs:date('2026-07-22Z'), xs:time('12:00:00')))", "2026-07-22T12:00:00Z");
    error("dateTime(xs:date('2026-07-22Z'), xs:time('12:00:00+02:00'))", FUNZONE_X_X);
  }

  /** Hashing stays consistent with equality, with and without timezones. */
  @Test public void hashing() {
    // equal instants written in different timezones must be deduplicated
    query("count(distinct-values((xs:dateTime('2026-07-22T12:00:00Z'), " +
        "xs:dateTime('2026-07-22T14:00:00+02:00'))))", 1);
    query("count(distinct-values((xs:date('2026-07-22Z'), xs:date('2026-07-22+00:00'))))", 1);
    // a value with a timezone is deduplicated with one without only if both compare as equal,
    // which depends on the implicit timezone
    query("let $a := xs:dateTime('2026-07-22T12:00:00Z'), " +
        "$b := xs:dateTime('2026-07-22T12:00:00') " +
        "return count(distinct-values(($a, $b))) = (if($a eq $b) then 1 else 2)", true);
    // values without a timezone are compared in the same implicit timezone
    query("count(distinct-values((xs:dateTime('2026-07-22T12:00:00'), " +
        "xs:dateTime('2026-07-22T12:00:00'))))", 1);
    query("xs:dateTime('2026-07-22T12:00:00') lt xs:dateTime('2026-07-22T13:00:00')", true);
  }

  /** Time arithmetic is unaffected by the date range. */
  @Test public void timeArithmetic() {
    query("string(xs:time('12:00:00') + xs:dayTimeDuration('P100000000000000D'))", "12:00:00");
    query("string(xs:time('23:30:00') + xs:dayTimeDuration('P1DT1H'))", "00:30:00");
    query("string(xs:time('24:00:00'))", "00:00:00");
  }
}
