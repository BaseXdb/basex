package org.basex.query.func.fn;

import org.basex.query.*;

/**
 * XQuery functions tests.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnFormatDateTest extends QueryTest {
  /** Constructor. */
  static {
    queries = new Object[][] {
      { "formdate  10", strings("2002-12-31"),
        "format-date(xs:date('2002-12-31'), '[Y0001]-[M01]-[D01]')" },
      { "formdate  20", strings("12-31-2002"),
        "format-date(xs:date('2002-12-31'), '[M]-[D]-[Y]')" },
      { "formdate  30", strings("2002-12-31"),
        "format-date(xs:date('2002-12-31'), '[Y0001]-[M01]-[D01]') " },
      { "formdate  40", strings("12-31-2002"),
        "format-date(xs:date('2002-12-31'), '[M]-[D]-[Y]') " },
      { "formdate  50", strings("31-12-2002"),
        "format-date(xs:date('2002-12-31'), '[D]-[M]-[Y]') " },
      { "formdate  60", strings("31 XII 2002"),
        "format-date(xs:date('2002-12-31'), '[D1] [MI] [Y]') " },
      { "formdate  70", strings("31st December, 2002"),
        "format-date(xs:date('2002-12-31'), '[D1o] [MNn], [Y]', 'en', (), ()) " },
      { "formdate  80", strings("31 DEC 2002"),
        "format-date(xs:date('2002-12-31'), '[D01] [MN,*-3] [Y0001]', 'en', (), ()) " },
      { "formdate  90", strings("December 31, 2002"),
        "format-date(xs:date('2002-12-31'), '[MNn] [D], [Y]', 'en', (), ()) " },
      { "formdate 100", strings("31 December, 2002"),
        "format-date(xs:date('2002-12-31'), '[D] [MNn], [Y]', 'en', (), ()) " },
      { "formdate 110", strings("[2002-12-31]"),
        "format-date(xs:date('2002-12-31'), '[[[Y0001]-[M01]-[D01]]]') " },
      { "formdate 120", strings("Two Thousand and Two"),
        "format-date(xs:date('2002-12-31'), '[YWw]', 'en', (), ()) " },
      { "formdate 130", strings("einunddrei\u00dfigste Dezember"),
        "format-date(xs:date('2002-12-31'), '[Dwo] [MNn]', 'de', (), ()) " },
      { "formdate 140", strings("3:58 PM"),
        "format-time(xs:time('15:58:45.762+02:00'), '[h]:[m01] [PN]', 'en', (), ()) " },
      { "formdate 150", strings("3:58:45 pm"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[h]:[m01]:[s01] [Pn]', 'en', (), ()) " },
      /*
      { "formdate 160", strings("3:58:45 PM PDT"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[h]:[m01]:[s01] [PN] [ZN,*-3]', 'en', (), ()) " },
      { "formdate 170", strings("3:58:45 o'clock PM PDT"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[h]:[m01]:[s01] o''clock [PN] [ZN,*-3]', 'en', (), ()) " },
      */
      { "formdate 180", strings("15:58"),
        "format-time(xs:time('15:58:45.762+02:00'),'[H01]:[m01]') " },
      { "formdate 190", strings("15:58:45.762"),
        "format-time(xs:time('15:58:45.762+02:00'), '[H01]:[m01]:[s01].[f001]') " },
      { "formdate 200", strings("15:58:45 GMT+02:00"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[H01]:[m01]:[s01] [z,6-6]', 'en', (), ()) " },
      { "formdate 210", strings("15:58 Uhr GMT+2"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[H01]:[m01] Uhr [z0]', 'de', (), ()) " },
      { "formdate 220", strings("3.58pm on Tuesday, 31st December"),
        "format-dateTime(xs:dateTime('2002-12-31T15:58:45.762+02:00'), " +
        "'[h].[m01][Pn] on [FNn], [D1o] [MNn]') " },
      { "formdate 230", strings("12/31/2002 at 15:58:45"),
        "format-dateTime(xs:dateTime('2002-12-31T15:58:45.762+02:00'), " +
        "'[M01]/[D01]/[Y0001] at [H01]:[m01]:[s01]')" },

      { "formdate 250", strings("J"), "format-time(xs:time('12:12:12'), '[ZZ]')" },
      { "formdate 251", strings(""),  "format-time(xs:time('12:12:12'), '[zZ]')" },
      { "formdate 252", strings(""),  "format-time(xs:time('12:12:12'), '[Zz]')" },
    };
  }
}
