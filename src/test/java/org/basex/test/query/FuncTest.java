package org.basex.test.query;

/**
 * XQuery functions tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FuncTest extends QueryTest {
  /** Constructor. */
  static {
    doc = "<html/>";

    queries = new Object[][] {
      { "formnum  1", str("0"), "format-number(0, '0')" },
      { "formnum  2", str("0"), "format-number(0, '1')" },
      { "formnum  3", str("1"), "format-number(1, '0')" },
      { "formnum  4", str("1.0"), "format-number(1, '1.0')" },
      { "formnum  5", str("1"), "format-number(1.1, '1')" },
      { "formnum  6", str("1.1"), "format-number(1.1, '1.0')" },
      { "formnum  7", str("-1"), "format-number(-1, '-1')" },
      { "formnum  8", str("-1.1"), "format-number(-1.1, '-1.1')" },
      { "formnum  9", str("11"), "format-number(11, '00')" },
      { "formnum 10", str("011"), "format-number(11, '000')" },
      { "formnum 11", str("11"), "format-number(11, '###')" },
      { "formnum 12", str("1,111"), "format-number(1111, '#,###')" },
      { "formnum 13", str("1,111"), "format-number(1111, '0,000')" },
      { "formnum 14", str("1.1,1"), "format-number(1.11, '#,#.#,#')" },
      { "formnum 15", str("0,1.1,1"), "format-number(1.11, '0,0.0,0')" },
      { "formnum 16", str("100"), "format-number(1, '0%')" },
      { "formnum 17", str("100%"), "format-number(1, '0.%')" },
      { "formnum 18", str("-100.3%"), "format-number(-1.003, '0.0%')" },
      { "formnum 19", str("1000\u2030"), "format-number(1, '0.\u2030')" },
      { "formnum 20", str("Infinity"), "format-number(1 div 0.0e0, '0')" },
      { "formnum 21", str("NaN"), "format-number(0 div 0.0e0, '0')" },
      { "formnum 22", str("NaN"), "format-number(xs:double('NaN'), '00')" },
      { "formnum 23", str("--1"), "format-number(-1, '0;--0')" },
      { "formnum 24", str("-100.0%"), "format-number(-1, '0.0%;-0.0%')" },
      { "formnum 25", str("1"), "format-number(-1, '-0;0')" },

      { "formint  1", str("1"), "format-integer(1, '0')" },
      { "formint  2", str("01"), "format-integer(1, '00')" },
      { "formint  3", str("Eleventh"), "format-integer(11, 'Wwo')" },
      { "formint  4", str("az"), "format-integer(52, 'a')" },
      { "formint  5", str("LII"), "format-integer(52, 'I')" },
      { "formint  6", str("MCMLXXXIV"), "format-integer(1984, 'I')" },
      { "formint  7", str("12345"), "format-integer(12345, 'I')" },
      { "formint  8", str("12345"), "format-integer(12345, 'I')" },
      { "formint  9", str("one hundred and twenty-three"),
        "format-integer(123, 'w')" },
      { "formint 10", str("One Hundredth and Twenty-Third"),
        "format-integer(123, 'Wwo')" },
        
      { "formdate  1", str("2002-12-31"),
        "format-date(xs:date('2002-12-31'), '[Y0001]-[M01]-[D01]')" },
      { "formdate  2", str("12-31-2002"),
        "format-date(xs:date('2002-12-31'), '[M]-[D]-[Y]')" },
      { "formdate  3", str("2002-12-31"),
        "format-date(xs:date('2002-12-31'), '[Y0001]-[M01]-[D01]') " },
      { "formdate  4", str("12-31-2002"),
        "format-date(xs:date('2002-12-31'), '[M]-[D]-[Y]') " },
      { "formdate  5", str("31-12-2002"),
        "format-date(xs:date('2002-12-31'), '[D]-[M]-[Y]') " },
      { "formdate  6", str("31 XII 2002"),
        "format-date(xs:date('2002-12-31'), '[D1] [MI] [Y]') " },
      { "formdate  7", str("31st December, 2002"),
        "format-date(xs:date('2002-12-31'), " +
        "'[D1o] [MNn], [Y]', 'en', (), ()) " },
      { "formdate  8", str("31 DEC 2002"),
        "format-date(xs:date('2002-12-31'), " +
        "'[D01] [MN,*-3] [Y0001]', 'en', (), ()) " },
      { "formdate  9", str("December 31, 2002"),
        "format-date(xs:date('2002-12-31'), '[MNn] [D], [Y]', 'en', (), ()) " },
      { "formdate 10", str("31 December, 2002"),
        "format-date(xs:date('2002-12-31'), '[D] [MNn], [Y]', 'en', (), ()) " },
      { "formdate 11", str("[2002-12-31]"),
        "format-date(xs:date('2002-12-31'), '[[[Y0001]-[M01]-[D01]]]') " },
      { "formdate 12", str("Two Thousand and Two"),
        "format-date(xs:date('2002-12-31'), '[YWw]', 'en', (), ()) " },
      { "formdate 13", str("thirty-first December"),
        "format-date(xs:date('2002-12-31'), '[Dwo] [MNn]', 'de', (), ()) " },
      { "formdate 14", str("3:58 PM"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[h]:[m01] [PN]', 'en', (), ()) " },
      { "formdate 15", str("3:58:45 pm"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[h]:[m01]:[s01] [Pn]', 'en', (), ()) " },
      /*
      { "formdate 16", str("3:58:45 PM PDT"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[h]:[m01]:[s01] [PN] [ZN,*-3]', 'en', (), ()) " },
      { "formdate 17", str("3:58:45 o'clock PM PDT"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[h]:[m01]:[s01] o''clock [PN] [ZN,*-3]', 'en', (), ()) " },
      */
      { "formdate 18", str("15:58"),
        "format-time(xs:time('15:58:45.762+02:00'),'[H01]:[m01]') " },
      { "formdate 19", str("15:58:45.762"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[H01]:[m01]:[s01].[f001]') " },
      /*
      { "formdate 20", str("15:58:45 GMT+02:00"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[H01]:[m01]:[s01] [z,6-6]', 'en', (), ()) " },
      { "formdate 21", str("15.58 Uhr GMT+2"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[H01]:[m01] Uhr [z]', 'de', (), ()) " },
      */
      { "formdate 22", str("3.58pm on Tuesday, 31st December"),
        "format-dateTime(xs:dateTime('2002-12-31T15:58:45.762+02:00'), " +
        "'[h].[m01][Pn] on [FNn], [D1o] [MNn]') " },
      { "formdate 23", str("12/31/2002 at 15:58:45"),
        "format-dateTime(xs:dateTime('2002-12-31T15:58:45.762+02:00'), " +
        "'[M01]/[D01]/[Y0001] at [H01]:[m01]:[s01]')" },
    };
  }

  /* TABLE REPRESENTATION
  PRE DIS  TYPE  CONTENT
    0  -1  DOC   test.xml
    1   0  ELEM  html
  */
}
