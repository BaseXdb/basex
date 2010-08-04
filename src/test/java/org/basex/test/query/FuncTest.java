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
      { "count 1", "count()" },
      { "count 2", "count(1, 1)" },
      { "contains 1", "contains(.)" },
      { "contains 2", "contains(. .)" },
      { "number 1", dbl(1), "number(true())" },
      { "number 2", dbl(0), "number(false())" },
      { "number 3", dbl(Double.NaN), "number(xs:gYear('2005'))" },

      { "formnum  10", str("0"), "format-number(0, '0')" },
      { "formnum  20", str("0"), "format-number(0, '1')" },
      { "formnum  30", str("1"), "format-number(1, '0')" },
      { "formnum  40", str("1.0"), "format-number(1, '1.0')" },
      { "formnum  50", str("1"), "format-number(1.1, '1')" },
      { "formnum  60", str("1.1"), "format-number(1.1, '1.0')" },
      { "formnum  70", str("-1"), "format-number(-1, '-1')" },
      { "formnum  80", str("-1.1"), "format-number(-1.1, '-1.1')" },
      { "formnum  90", str("11"), "format-number(11, '00')" },
      { "formnum 100", str("011"), "format-number(11, '000')" },
      { "formnum 110", str("11"), "format-number(11, '###')" },
      { "formnum 120", str("1,111"), "format-number(1111, '#,###')" },
      { "formnum 130", str("1,111"), "format-number(1111, '0,000')" },
      { "formnum 140", str("1.1,1"), "format-number(1.11, '#,#.#,#')" },
      { "formnum 150", str("0,1.1,1"), "format-number(1.11, '0,0.0,0')" },
      { "formnum 160", str("100%"), "format-number(1, '0.%')" },
      { "formnum 170", str("-100.3%"), "format-number(-1.003, '0.0%')" },
      { "formnum 180", str("1000\u2030"), "format-number(1, '0.\u2030')" },
      { "formnum 190", str("Infinity"), "format-number(1 div 0.0e0, '0')" },
      { "formnum 200", str("NaN"), "format-number(0 div 0.0e0, '0')" },
      { "formnum 210", str("NaN"), "format-number(xs:double('NaN'), '00')" },
      { "formnum 220", str("--1"), "format-number(-1, '0;--0')" },
      { "formnum 230", str("-100.0%"), "format-number(-1, '0.0%;-0.0%')" },
      { "formnum 240", str("1"), "format-number(-1, '-0;0')" },

      // http://www.w3schools.com/XSL/func_formatnumber.asp
      { "formnum w3-10", str("500100"), "format-number(500100, '#')" },
      { "formnum w3-20", str("500100"), "format-number(500100, '0')" },
      { "formnum w3-30", str("500100.00"), "format-number(500100, '#.00')" },
      { "formnum w3-40", str("500100.0"), "format-number(500100, '#.0')" },
      { "formnum w3-50", str("500,100.0"), "format-number(500100, '#,###.0')" },
      { "formnum w3-60", str("23%"), "format-number(0.23456, '#%')" },

      // http://www.devguru.com/technologies/xslt/quickref/
      //   xslt_example_formatnumber.xml
      { "formnum w3-10", str("123456789.000000000"),
        "format-number(123456789, '#.000000000')" },
      { "formnum w3-20", str("123456789.0"),
        "format-number(123456789, '#.0')" },
      { "formnum w3-3", str("12%"),
        "format-number(0.123456789, '##%')" },
      { "formnum w3-40", str("123456789"),
        "format-number(123456789, '################')" },

      { "formint  10", str("1"), "format-integer(1, '0')" },
      { "formint  20", str("01"), "format-integer(1, '00')" },
      { "formint  30", str("Eleventh"), "format-integer(11, 'Wwo')" },
      { "formint  40", str("az"), "format-integer(52, 'a')" },
      { "formint  50", str("LII"), "format-integer(52, 'I')" },
      { "formint  60", str("MCMLXXXIV"), "format-integer(1984, 'I')" },
      { "formint  70", str("12345"), "format-integer(12345, 'I')" },
      { "formint  80", str("12345"), "format-integer(12345, 'I')" },
      { "formint  90", str("one hundred and twenty-three"),
        "format-integer(123, 'w')" },
      { "formint 100", str("One Hundredth and Twenty-Third"),
        "format-integer(123, 'Wwo')" },

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
        "format-date(xs:date('2002-12-31'), " +
        "'[D1o] [MNn], [Y]', 'en', (), ()) " },
      { "formdate  80", str("31 DEC 2002"),
        "format-date(xs:date('2002-12-31'), " +
        "'[D01] [MN,*-3] [Y0001]', 'en', (), ()) " },
      { "formdate  90", str("December 31, 2002"),
        "format-date(xs:date('2002-12-31'), '[MNn] [D], [Y]', 'en', (), ()) " },
      { "formdate 100", str("31 December, 2002"),
        "format-date(xs:date('2002-12-31'), '[D] [MNn], [Y]', 'en', (), ()) " },
      { "formdate 110", str("[2002-12-31]"),
        "format-date(xs:date('2002-12-31'), '[[[Y0001]-[M01]-[D01]]]') " },
      { "formdate 120", str("Two Thousand and Two"),
        "format-date(xs:date('2002-12-31'), '[YWw]', 'en', (), ()) " },
      { "formdate 130", str("thirty-first December"),
        "format-date(xs:date('2002-12-31'), '[Dwo] [MNn]', 'de', (), ()) " },
      { "formdate 140", str("3:58 PM"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[h]:[m01] [PN]', 'en', (), ()) " },
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
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[H01]:[m01]:[s01].[f001]') " },
      /*
      { "formdate 200", str("15:58:45 GMT+02:00"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[H01]:[m01]:[s01] [z,6-6]', 'en', (), ()) " },
      { "formdate 210", str("15.58 Uhr GMT+2"),
        "format-time(xs:time('15:58:45.762+02:00'), " +
        "'[H01]:[m01] Uhr [z]', 'de', (), ()) " },
      */
      { "formdate 220", str("3.58pm on Tuesday, 31st December"),
        "format-dateTime(xs:dateTime('2002-12-31T15:58:45.762+02:00'), " +
        "'[h].[m01][Pn] on [FNn], [D1o] [MNn]') " },
      { "formdate 230", str("12/31/2002 at 15:58:45"),
        "format-dateTime(xs:dateTime('2002-12-31T15:58:45.762+02:00'), " +
        "'[M01]/[D01]/[Y0001] at [H01]:[m01]:[s01]')" },
    };
  }

  /* TABLE REPRESENTATION
  PRE PAR  TYPE  CONTENT
    0  -1  DOC   test.xml
    1   0  ELEM  html
  */
}
