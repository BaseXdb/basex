package org.basex.test.query.func;

import org.basex.test.query.*;

/**
 * XQuery functions tests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNFormatDateTest extends QueryTest {
  /** Constructor. */
  static {
    doc = "<dummy/>";

    queries = new Object[][] {
      { "formdate  10", str("2002-12-31"),
        "format-date(xs:date('2002-12-31'), '[Y0001]-[M01]-[D01]')" },
      { "formdate  20", str("12-31-2002"),
        "format-date(xs:date('2002-12-31'), '[M]-[D]-[Y]')" },
      { "formdate  30", str("2002-12-31"),
        "format-date(xs:date('2002-12-31'), '[Y0001]-[M01]-[D01]') " },
      { "formdate  40", str("12-31-2002"),
        "format-date(xs:date('2002-12-31'), '[M]-[D]-[Y]') " },
      { "formdate  50", str("31-12-2002"),
        "format-date(xs:date('2002-12-31'), '[D]-[M]-[Y]') " },
      { "formdate  60", str("31 XII 2002"),
        "format-date(xs:date('2002-12-31'), '[D1] [MI] [Y]') " },
      { "formdate  70", str("31st December, 2002"),
        "format-date(xs:date('2002-12-31'), '[D1o] [MNn], [Y]', 'en', (), ()) " },
      { "formdate  80", str("31 DEC 2002"),
        "format-date(xs:date('2002-12-31'), '[D01] [MN,*-3] [Y0001]', 'en', (), ()) " },
      { "formdate  90", str("December 31, 2002"),
        "format-date(xs:date('2002-12-31'), '[MNn] [D], [Y]', 'en', (), ()) " },
      { "formdate 100", str("31 December, 2002"),
        "format-date(xs:date('2002-12-31'), '[D] [MNn], [Y]', 'en', (), ()) " },
      { "formdate 110", str("[2002-12-31]"),
        "format-date(xs:date('2002-12-31'), '[[[Y0001]-[M01]-[D01]]]') " },
      { "formdate 120", str("Two Thousand and Two"),
        "format-date(xs:date('2002-12-31'), '[YWw]', 'en', (), ()) " },
      { "formdate 130", str("einunddrei\u00dfigste Dezember"),
        "format-date(xs:date('2002-12-31'), '[Dwo] [MNn]', 'de', (), ()) " },
      { "formdate 140", str("3:58 PM"),
        "format-time(xs:time('15:58:45.762+02:00'), '[h]:[m01] [PN]', 'en', (), ()) " },
      { "formdate 150", str("3:58:45 pm"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[h]:[m01]:[s01] [Pn]', 'en', (), ()) " },
      /*
      { "formdate 160", str("3:58:45 PM PDT"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[h]:[m01]:[s01] [PN] [ZN,*-3]', 'en', (), ()) " },
      { "formdate 170", str("3:58:45 o'clock PM PDT"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[h]:[m01]:[s01] o''clock [PN] [ZN,*-3]', 'en', (), ()) " },
      */
      { "formdate 180", str("15:58"),
        "format-time(xs:time('15:58:45.762+02:00'),'[H01]:[m01]') " },
      { "formdate 190", str("15:58:45.762"),
        "format-time(xs:time('15:58:45.762+02:00'), '[H01]:[m01]:[s01].[f001]') " },
      { "formdate 200", str("15:58:45 GMT+02:00"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[H01]:[m01]:[s01] [z,6-6]', 'en', (), ()) " },
      { "formdate 210", str("15:58 Uhr GMT+2"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[H01]:[m01] Uhr [z0]', 'de', (), ()) " },
      { "formdate 220", str("3.58pm on Tuesday, 31st December"),
        "format-dateTime(xs:dateTime('2002-12-31T15:58:45.762+02:00'), " +
        "'[h].[m01][Pn] on [FNn], [D1o] [MNn]') " },
      { "formdate 230", str("12/31/2002 at 15:58:45"),
        "format-dateTime(xs:dateTime('2002-12-31T15:58:45.762+02:00'), " +
        "'[M01]/[D01]/[Y0001] at [H01]:[m01]:[s01]')" },

      { "formdate 250", str("J"), "format-time(xs:time('12:12:12'), '[ZZ]')" },
      { "formdate 251", str(""),  "format-time(xs:time('12:12:12'), '[zZ]')" },
      { "formdate 252", str(""),  "format-time(xs:time('12:12:12'), '[Zz]')" },
    };
  }
}
