package org.basex.test.query;

/**
 * XQuery functions tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FunTest extends QueryTest {
  /** Constructor. */
  static {
    doc = "<desclist xml:lang='en'><desc xml:lang='en-US'>" +
        "<line>A line of text.</line></desc><desc xml:lang='fr'>" +
        "<line>Une ligne de texte.</line></desc></desclist>";

    queries = new Object[][] {
      { "false 1", bool(false), "false()" },
      { "true 2", bool(true), "true()" },

      { "count 1", "count()" },
      { "count 2", "count(1, 1)" },
      { "count 3", bool(false), "count(1[.]) eq 0" },
      { "count 4", bool(false), "count(1[.]) <= 0" },
      { "count 5", bool(false), "count(1[.]) < 0" },
      { "count 6", bool(true), "count(1[.]) != 0" },
      { "count 7", bool(true), "count(1[.]) >= 0" },
      { "count 8", bool(true), "count(1[.]) > 0" },
      { "count 9", bool(true), "count(1[.]) = 1" },
      { "count 10", bool(true), "count(1[.]) le 1" },
      { "count 11", bool(false), "count(1[.]) < 1" },
      { "count 12", bool(false), "count(1[.]) != 1" },
      { "count 13", bool(true), "count(1[.]) >= 1" },
      { "count 14", bool(false), "count(1[.]) > 1" },
      { "count 15", bool(false), "count(1[.]) = 2" },
      { "count 16", bool(true), "count(1[.]) <= 2" },
      { "count 17", bool(true), "count(1[.]) lt 2" },
      { "count 18", bool(true), "count(1[.]) != 2" },
      { "count 19", bool(false), "count(1[.]) >= 2" },
      { "count 20", bool(false), "count(1[.]) > 2" },
      { "count 21", bool(false), "count(1[.]) = 1.1" },
      { "count 22", bool(true), "count(1[.]) <= 1.1" },
      { "count 23", bool(true), "count(1[.]) < 1.1" },
      { "count 24", bool(true), "count(1[.]) ne 1.1" },
      { "count 25", bool(false), "count(1[.]) >= 1.1" },
      { "count 26", bool(false), "count(1[.]) > 1.1" },
      { "count 27", bool(false), "count(1[.]) = -1.1" },
      { "count 28", bool(false), "count(1[.]) <= -1.1" },
      { "count 29", bool(false), "count(1[.]) < -1.1" },
      { "count 30", bool(true), "count(1[.]) != -1.1" },
      { "count 31", bool(true), "count(1[.]) ge -1.1" },
      { "count 32", bool(true), "count(1[.]) gt -1.1" },
      { "count 33", itr(10000000),
        "count(for $i in 1 to 10000000 return $i)" },
      { "count 34", itr(100000),
        "count(for $i in 1 to 100000 return $i * $i)" },
      { "count 35", itr(1000000000000l),
        "count(for $i in 1 to 10000000 for $i in 1 to 100000 return $i * $i)" },
      { "count 36", itr(2),
        "count((for $a in (1,2) for $b in <b/> return $b)/.)" },
      { "count 37", itr(2),
        "count((for $a in (1,2) let $b := <b/> return $b)/.)" },

      { "contains 1", "contains(.)" },
      { "contains 2", "contains(. .)" },

      { "deep-equal  1", bool(true),  "deep-equal(1, 1)" },
      { "deep-equal  2", bool(false), "deep-equal(1, 2)" },
      { "deep-equal  3", bool(true),  "deep-equal('a', 'a')" },
      { "deep-equal  4", bool(false), "deep-equal('a', 'b')" },
      { "deep-equal  5", bool(true),  "deep-equal(1.0, 1)" },
      { "deep-equal  6", bool(false), "deep-equal('1', 1)" },
      { "deep-equal  7", bool(true),  "deep-equal((), ())" },
      { "deep-equal  8", bool(false), "deep-equal(<a>1</a>, 1)" },
      { "deep-equal  9", bool(true),  "deep-equal(text{'a'}, text{'a'})" },
      { "deep-equal 10", bool(false), "deep-equal(text{'a'}, text{'b'})" },
      { "deep-equal 11", bool(true),
        "deep-equal(comment{'a'}, comment{'a'})" },
      { "deep-equal 12", bool(false),
        "deep-equal(comment{'a'}, comment{'b'})" },
      { "deep-equal 13", bool(false), "deep-equal(text{'a'}, comment{'a'})" },
      { "deep-equal 14", bool(false),
        "deep-equal(comment{ 'a' }, processing-instruction{ 'a' } { 'a' })" },

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

      { "formint   0", str(""), "format-integer((), 'a')" },
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
      { "formint  91", str("one hundred and one"),
        "format-integer(101, 'w')" },
      { "formint  95", str("one hundred"),
        "format-integer(100, 'w')" },
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

      // http://www.xqueryfunctions.com/xq/fn_lang.html
      { "lang 1", nod(3), "//desc[lang('en')]" },
      { "lang 2", nod(3), "//desc[lang('en-US')]" },
      { "lang 3", nod(7), "//desc[lang('fr')]" },
      { "lang 4", nod(5), "//desc/line[lang('en')]" },
      { "lang 5", empty(),  "/.[lang('en-US')]" },
      { "lang 6", nod(7), "//desc[lang('FR')]" },
    };
  }

  /* TABLE REPRESENTATION
  PRE  DIS  SIZ  ATS  NS  KIND  CONTENT
  -------------------------------------------------
    0    1   11    1  +0  DOC   test.xml
    1    1   10    2   0  ELEM  desclist
    2    1    1    1   0  ATTR  xml:lang="en"
    3    2    4    2   0  ELEM  desc
    4    1    1    1   0  ATTR  xml:lang="en-US"
    5    2    2    1   0  ELEM  line
    6    1    1    1   0  TEXT  A line of text.
    7    6    4    2   0  ELEM  desc
    8    1    1    1   0  ATTR  xml:lang="fr"
    9    2    2    1   0  ELEM  line
   10    1    1    1   0  TEXT  Une ligne de texte.
  */
}
