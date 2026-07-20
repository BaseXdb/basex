package org.basex.query.func.fn;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * Tests for fn:format-date, fn:format-time and fn:format-dateTime.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnFormatDateTest extends SandboxTest {
  /** fn:format-date. */
  @Test public void formatDate() {
    query("format-date(xs:date('2002-12-31'), '[Y0001]-[M01]-[D01]')", "2002-12-31");
    query("format-date(xs:date('2002-12-31'), '[M]-[D]-[Y]')", "12-31-2002");
    query("format-date(xs:date('2002-12-31'), '[D]-[M]-[Y]')", "31-12-2002");
    query("format-date(xs:date('2002-12-31'), '[D1] [MI] [Y]')", "31 XII 2002");
    query("format-date(xs:date('2002-12-31'), '[D1o] [MNn], [Y]', 'en', (), ())",
        "31st December, 2002");
    query("format-date(xs:date('2002-12-31'), '[D01] [MN,*-3] [Y0001]', 'en', (), ())",
        "31 DEC 2002");
    query("format-date(xs:date('2002-12-31'), '[MNn] [D], [Y]', 'en', (), ())",
        "December 31, 2002");
    query("format-date(xs:date('2002-12-31'), '[D] [MNn], [Y]', 'en', (), ())",
        "31 December, 2002");
    query("format-date(xs:date('2002-12-31'), '[[[Y0001]-[M01]-[D01]]]')", "[2002-12-31]");
    query("format-date(xs:date('2002-12-31'), '[YWw]', 'en', (), ())", "Two Thousand Two");
    query("format-date(xs:date('2002-12-31'), '[Dwo] [MNn]', 'de', (), ())",
        "einunddreißigste Dezember");
  }

  /** fn:format-time. */
  @Test public void formatTime() {
    query("format-time(xs:time('15:58:45.762+02:00'), '[h]:[m01] [PN]', 'en', (), ())", "3:58 PM");
    query("format-time(xs:time('15:58:45.762+02:00'), '[h]:[m01]:[s01] [Pn]', 'en', (), ())",
        "3:58:45 pm");
    query("format-time(xs:time('15:58:45.762+02:00'), '[H01]:[m01]')", "15:58");
    query("format-time(xs:time('15:58:45.762+02:00'), '[H01]:[m01]:[s01].[f001]')", "15:58:45.762");
    query("format-time(xs:time('15:58:45.762+02:00'), '[H01]:[m01]:[s01] [z,6-6]', 'en', (), ())",
        "15:58:45 GMT+02:00");
    query("format-time(xs:time('15:58:45.762+02:00'), '[H01]:[m01] Uhr [z0]', 'de', (), ())",
        "15:58 Uhr GMT+2");
    query("format-time(xs:time('12:12:12'), '[ZZ]')", "J");
    query("format-time(xs:time('12:12:12'), '[zZ]')", "");
    query("format-time(xs:time('12:12:12'), '[Zz]')", "");
  }

  /** fn:format-dateTime. */
  @Test public void formatDateTime() {
    query("format-dateTime(xs:dateTime('2002-12-31T15:58:45.762+02:00'), "
        + "'[h].[m01][Pn] on [FNn], [D1o] [MNn]')", "3.58pm on Tuesday, 31st December");
    query("format-dateTime(xs:dateTime('2002-12-31T15:58:45.762+02:00'), "
        + "'[M01]/[D01]/[Y0001] at [H01]:[m01]:[s01]')", "12/31/2002 at 15:58:45");
  }
}
