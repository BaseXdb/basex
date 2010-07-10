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
    };
  }

  /* TABLE REPRESENTATION
  PRE DIS  TYPE  CONTENT
    0  -1  DOC   test.xml
    1   0  ELEM  html
  */
}
