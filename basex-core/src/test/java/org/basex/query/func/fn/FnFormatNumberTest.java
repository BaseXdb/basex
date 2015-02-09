package org.basex.query.func.fn;

import org.basex.query.*;

/**
 * XQuery functions tests.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnFormatNumberTest extends QueryTest {
  /** Constructor. */
  static {
    queries = new Object[][] {
      { "DecFormat 1", integers(1),
        "declare default decimal-format NaN='x'; 1" },
      { "DecFormat 2", integers(1),
        "declare default decimal-format NaN='x' digit='y'; 1" },
      { "DecFormat 3", integers(1),
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

      { "formnum  10", strings("0"), "format-number(0, '0')" },
      { "formnum  20", strings("00"), "format-number(0, '10')" },
      { "formnum  30", strings("1"), "format-number(1, '0')" },
      { "formnum  35", strings("0.1"), "format-number(0.1, '.0')" },
      { "formnum  40", strings("1.0"), "format-number(1, '1.0')" },
      { "formnum  50", strings("1"), "format-number(1.1, '1')" },
      { "formnum  60", strings("1.1"), "format-number(1.1, '1.0')" },
      { "formnum  70", strings("-1"), "format-number(-1, '1')" },
      { "formnum  80", strings("-1.1"), "format-number(-1.1, '1.1')" },
      { "formnum  90", strings("11"), "format-number(11, '00')" },
      { "formnum 100", strings("011"), "format-number(11, '000')" },
      { "formnum 110", strings("11"), "format-number(11, '###')" },
      { "formnum 120", strings("1,111"), "format-number(1111, '#,###')" },
      { "formnum 130", strings("1,111"), "format-number(1111, '0,000')" },
      { "formnum 140", strings("1.11"), "format-number(1.11, '#,#.#,#')" },
      { "formnum 150", strings("100%"), "format-number(1, '0.%')" },
      { "formnum 160", strings("-100.3%"), "format-number(-1.003, '0.0%')" },
      { "formnum 170", strings("1000\u2030"), "format-number(1, '0.\u2030')" },
      { "formnum 180", strings("Infinity"), "format-number(1 div 0.0e0, '0')" },
      { "formnum 190", strings("NaN"), "format-number(0 div 0.0e0, '0')" },
      { "formnum 200", strings("NaN"), "format-number(xs:double('NaN'), '00')" },
      { "formnum 210", strings("-1"), "format-number(-1, '0;-0')" },
      { "formnum 220", strings("100.0%"), "format-number(-1, '0.0%;0.0%')" },
      { "formnum 230", strings("1"), "format-number(-1, '-0;0')" },
      { "formnum 240", strings("1,2,3,4"), "format-number(1234, '#,#')" },
      { "formnum 250", strings("1,234,567"), "format-number(1234567, '#,###')" },
      { "formnum 260", strings("1,234,567.76"),
        "format-number(1234567.765, '#,###.##')" },
      { "formnum 270", strings("1.57"), "format-number(1.567, '#.#,#')" },
      { "formnum 280", strings("123,45,6"), "format-number(123456, '#,##,#')" },
      { "formnum 290", strings("1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1"),
        "format-number(xs:decimal('11111111111111111111'), '#,#')" },
      { "formnum 300", strings("12.346e2"), "format-number(1234.5678, '00.000e0')" },

      // http://www.w3schools.com/XSL/func_formatnumber.asp
      { "formnum w3-10", strings("500100"), "format-number(500100, '#')" },
      { "formnum w3-20", strings("500100"), "format-number(500100, '0')" },
      { "formnum w3-30", strings("500100.00"), "format-number(500100, '#.00')" },
      { "formnum w3-40", strings("500100.0"), "format-number(500100, '#.0')" },
      { "formnum w3-50", strings("500,100.0"), "format-number(500100, '#,###.0')" },
      { "formnum w3-60", strings("23%"), "format-number(0.23456, '#%')" },

      // http://www.devguru.com/technologies/xslt/quickref/
      //   xslt_example_formatnumber.xml
      { "formnum w3-10", strings("123456789.000000000"),
        "format-number(123456789, '#.000000000')" },
      { "formnum w3-20", strings("123456789.0"),
        "format-number(123456789, '#.0')" },
      { "formnum w3-3", strings("12%"),
        "format-number(0.123456789, '##%')" },
      { "formnum w3-40", strings("123456789"),
        "format-number(123456789, '################')" },
    };
  }
}
