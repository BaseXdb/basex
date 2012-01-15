package org.basex.test.query.func;

import org.basex.test.query.QueryTest;

/**
 * XQuery functions tests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNFormatNumTest extends QueryTest {
  /** Constructor. */
  static {
    doc = "<dummy/>";

    queries = new Object[][] {
      { "DecFormat 1", itr(1),
        "declare default decimal-format NaN='x'; 1" },
      { "DecFormat 2", itr(1),
        "declare default decimal-format NaN='x' digit='y'; 1" },
      { "DecFormat 3", itr(1),
        "declare decimal-format hans NaN='x'; 1" },

      // XQST0114: duplicate decimal-format property
      { "DecFormatErr 1",
        "declare default decimal-format NaN='x' NaN='y'; 1" },
      // XQST0111: duplicate decimal-format declaration
      { "DecFormatErr 2",
        "declare default decimal-format NaN='x'; " +
        "declare default decimal-format NaN='x'; 1" },
      // XQST0098: character specified twice
      { "DecFormatErr 3",
        "declare default decimal-format percent=','; 1" },
      // XQST0003: unknown decimal-format property
      { "DecFormatErr 4",
        "declare default decimal-format xxx='x'; 1" },
      // XQST0097: invalid decimal-format property
      { "DecFormatErr 6",
        "declare default decimal-format zero-digit='x'; 1" },
      // XQST0097: invalid decimal-format property
      { "DecFormatErr 7",
        "declare default decimal-format percent='xxx'; 1" },

      { "formnum  10", str("0"), "format-number(0, '0')" },
      { "formnum  20", str("00"), "format-number(0, '10')" },
      { "formnum  30", str("1"), "format-number(1, '0')" },
      { "formnum  35", str(".1"), "format-number(0.1, '.0')" },
      { "formnum  40", str("1.0"), "format-number(1, '1.0')" },
      { "formnum  50", str("1"), "format-number(1.1, '1')" },
      { "formnum  60", str("1.1"), "format-number(1.1, '1.0')" },
      { "formnum  70", str("-1"), "format-number(-1, '1')" },
      { "formnum  80", str("-1.1"), "format-number(-1.1, '1.1')" },
      { "formnum  90", str("11"), "format-number(11, '00')" },
      { "formnum 100", str("011"), "format-number(11, '000')" },
      { "formnum 110", str("11"), "format-number(11, '###')" },
      { "formnum 120", str("1,111"), "format-number(1111, '#,###')" },
      { "formnum 130", str("1,111"), "format-number(1111, '0,000')" },
      { "formnum 140", str("1.11"), "format-number(1.11, '#,#.#,#')" },
      { "formnum 150", str("100%"), "format-number(1, '0.%')" },
      { "formnum 160", str("-100.3%"), "format-number(-1.003, '0.0%')" },
      { "formnum 170", str("1000\u2030"), "format-number(1, '0.\u2030')" },
      { "formnum 180", str("Infinity"), "format-number(1 div 0.0e0, '0')" },
      { "formnum 190", str("NaN"), "format-number(0 div 0.0e0, '0')" },
      { "formnum 200", str("NaN"), "format-number(xs:double('NaN'), '00')" },
      { "formnum 210", str("--1"), "format-number(-1, '0;-0')" },
      { "formnum 220", str("-100.0%"), "format-number(-1, '0.0%;0.0%')" },
      { "formnum 230", str("-1"), "format-number(-1, '-0;0')" },
      { "formnum 240", str("1,2,3,4"), "format-number(1234, '#,#')" },
      { "formnum 250", str("1,234,567"), "format-number(1234567, '#,###')" },
      { "formnum 260", str("1,234,567.76"),
        "format-number(1234567.765, '#,###.##')" },
      { "formnum 270", str("1.57"), "format-number(1.567, '#.#,#')" },
      { "formnum 280", str("123,45,6"), "format-number(123456, '#,##,#')" },
      { "formnum 290", str("1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1"),
        "format-number(xs:decimal('11111111111111111111'), '#,#')" },

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
    };
  }
}
