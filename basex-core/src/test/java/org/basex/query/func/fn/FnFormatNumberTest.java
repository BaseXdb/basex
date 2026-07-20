package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * Tests for fn:format-number and decimal-format declarations.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnFormatNumberTest extends SandboxTest {
  /** Decimal-format declarations. */
  @Test public void decimalFormat() {
    query("declare default decimal-format NaN='x'; 1", 1);
    query("declare default decimal-format NaN='x' digit='y'; 1", 1);
    query("declare decimal-format hans NaN='x'; 1", 1);

    // duplicate decimal-format property
    error("declare default decimal-format NaN='x' NaN='y'; 1", DECDUPLPROP_X);
    // duplicate decimal-format declaration
    error("declare default decimal-format NaN='x'; "
        + "declare default decimal-format NaN='x'; 1", DECDUPL);
    // character specified twice
    error("declare default decimal-format percent=','; 1", DUPLDECFORM_X);
    // unknown decimal-format property
    error("declare default decimal-format xxx='x'; 1", FORMPROP_X);
    // invalid decimal-format property
    error("declare default decimal-format zero-digit='x'; 1", INVDECFORM_X_X);
    error("declare default decimal-format percent='xxx'; 1", INVDECFORM_X_X);
  }

  /** fn:format-number. */
  @Test public void formatNumber() {
    query("format-number(0, '0')", "0");
    query("format-number(0, '10')", "00");
    query("format-number(1, '0')", "1");
    query("format-number(0.1, '.0')", ".1");
    query("format-number(1, '1.0')", "1.0");
    query("format-number(1.1, '1')", "1");
    query("format-number(1.1, '1.0')", "1.1");
    query("format-number(-1, '1')", "-1");
    query("format-number(-1.1, '1.1')", "-1.1");
    query("format-number(11, '00')", "11");
    query("format-number(11, '000')", "011");
    query("format-number(11, '###')", "11");
    query("format-number(1111, '#,###')", "1,111");
    query("format-number(1111, '0,000')", "1,111");
    query("format-number(1.11, '#,#.#,#')", "1.1,1");
    query("format-number(1, '0.%')", "100%");
    query("format-number(-1.003, '0.0%')", "-100.3%");
    query("format-number(1, '0.‰')", "1000‰");
    query("format-number(1 div 0.0e0, '0')", "Infinity");
    query("format-number(0 div 0.0e0, '0')", "NaN");
    query("format-number(xs:double('NaN'), '00')", "NaN");
    query("format-number(-1, '0;-0')", "-1");
    query("format-number(-1, '0.0%;0.0%')", "100.0%");
    query("format-number(-1, '-0;0')", "1");
    query("format-number(1234, '#,#')", "1,2,3,4");
    query("format-number(1234567, '#,###')", "1,234,567");
    query("format-number(1234567.765, '#,###.##')", "1,234,567.76");
    query("format-number(1.567, '#.#,#')", "1.5,7");
    query("format-number(123456, '#,##,#')", "123,45,6");
    query("format-number(xs:decimal('11111111111111111111'), '#,#')",
        "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1");
    query("format-number(1234.5678, '00.000e0')", "12.346e2");
    query("format-number(-9999, '#,###')", "-9,999");

    query("declare default decimal-format zero-digit = '&#xA66;';"
        + "format-number(0, '#,i')", "੦,i");
    query("declare default decimal-format zero-digit = '&#xA66;';"
        + "format-number(1, '#੦')", "੧");

    // http://www.w3schools.com/XSL/func_formatnumber.asp
    query("format-number(500100, '#')", "500100");
    query("format-number(500100, '0')", "500100");
    query("format-number(500100, '#.00')", "500100.00");
    query("format-number(500100, '#.0')", "500100.0");
    query("format-number(500100, '#,###.0')", "500,100.0");
    query("format-number(0.23456, '#%')", "23%");

    // http://www.devguru.com/technologies/xslt/quickref/xslt_example_formatnumber.xml
    query("format-number(123456789, '#.000000000')", "123456789.000000000");
    query("format-number(123456789, '#.0')", "123456789.0");
    query("format-number(0.123456789, '##%')", "12%");
    query("format-number(123456789, '################')", "123456789");
  }
}
