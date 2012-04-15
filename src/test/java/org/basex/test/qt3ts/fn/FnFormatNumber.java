package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * 
 *       Tests for the format-number() function transferred from XSLT 1.0/2.0 to XPath 3.0/XQuery 3.0
 *    .
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnFormatNumber extends QT3TestSet {

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test of format-number with 2 arguments, showing zeroes. .
   */
  @org.junit.Test
  public void numberformat01() {
    final XQuery query = new XQuery(
      "format-number(2392.14*36.58,'000,000.000000')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "087,504.481200")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test of format-number and # and 0 in format string. .
   */
  @org.junit.Test
  public void numberformat02() {
    final XQuery query = new XQuery(
      "format-number(12792.14*96.58,'##,###,000.000###')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1,235,464.8812")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test of format-number on a negative number. .
   */
  @org.junit.Test
  public void numberformat03() {
    final XQuery query = new XQuery(
      "format-number(2792.14*(-36.58),'000,000.000###')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-102,136.4812")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test of format-number on a negative number; should choose second pattern. .
   */
  @org.junit.Test
  public void numberformat04() {
    final XQuery query = new XQuery(
      "format-number(2392.14*(-36.58),'000,000.000###;###,###.000###')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "87,504.4812")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test of format-number percentage format. .
   */
  @org.junit.Test
  public void numberformat05() {
    final XQuery query = new XQuery(
      "format-number(0.4857,'###.###%')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "48.57%")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test of format-number per-mille format. .
   */
  @org.junit.Test
  public void numberformat06() {
    final XQuery query = new XQuery(
      "format-number(0.4857,'###.###\u2030')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "485.7\u2030")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test of format-number currency symbol, which is not supposed to be there. .
   */
  @org.junit.Test
  public void numberformat07() {
    final XQuery query = new XQuery(
      "format-number(95.4857,'¤###.####')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "¤95.4857")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test of format-number with prefix and suffix in format string. .
   */
  @org.junit.Test
  public void numberformat08() {
    final XQuery query = new XQuery(
      "format-number(2.14*86.58,'PREFIX##00.000###SUFFIX')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "PREFIX185.2812SUFFIX")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test default decimal-format on separator characters, changing both. .
   */
  @org.junit.Test
  public void numberformat09() {
    final XQuery query = new XQuery(
      "format-number(931.4857,'000.000|###')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "000.931|486")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test default decimal-format on pattern-only characters, positive number. .
   */
  @org.junit.Test
  public void numberformat11() {
    final XQuery query = new XQuery(
      "format-number(26931.4,'+!!!,!!!.!!!\\-!!,!!!.!!!')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "+26,931.4")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test default decimal-format on pattern-only characters, negative number. .
   */
  @org.junit.Test
  public void numberformat12() {
    final XQuery query = new XQuery(
      "format-number(-26931.4,'+!!,!!!.!!!\\-!!!,!!!.!!!')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-26,931.4")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test default decimal-format on pattern-only characters, negative number and one pattern. .
   */
  @org.junit.Test
  public void numberformat13() {
    final XQuery query = new XQuery(
      "format-number(-26931.4,'!!!,!!!.!!!')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-26,931.4")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test specified result pattern for infinity. .
   */
  @org.junit.Test
  public void numberformat14() {
    final XQuery query = new XQuery(
      "format-number(1 div 0e0,'###############################')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "off-the-scale")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test of decimal-format per-mille format with character being changed. .
   */
  @org.junit.Test
  public void numberformat16() {
    final XQuery query = new XQuery(
      "format-number(0.4857,'###.###m')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "485.7m")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test decimal-format output character for negative, 2 patterns. .
   */
  @org.junit.Test
  public void numberformat17() {
    final XQuery query = new XQuery(
      "format-number(-26931.4,'+###,###.###;-###,###.###')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-26,931.4")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test decimal-format output character for negative, one pattern. .
   */
  @org.junit.Test
  public void numberformat18() {
    final XQuery query = new XQuery(
      "format-number(-26931.4,'###,###.###')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "_26,931.4")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test decimal-format declaration with a name. .
   */
  @org.junit.Test
  public void numberformat19() {
    final XQuery query = new XQuery(
      "concat(format-number(-26931.4,'###,###.###','myminus'), '/',\n" +
      "            format-number(-42857.1,'###,###.###'))",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "_26,931.4/-42,857.1")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test of decimal-format with qualified name. Unqualified name provided as a trap. .
   */
  @org.junit.Test
  public void numberformat20() {
    final XQuery query = new XQuery(
      "format-number(1234.567,'#*###*###!###','foo:decimal1')",
      ctx);
    query.namespace("foo", "http://foo.ns");
    // decimal format
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1*234!567")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Designate a space as the grouping separator. .
   */
  @org.junit.Test
  public void numberformat26() {
    final XQuery query = new XQuery(
      "format-number(7654321.4857,'### ### ###,#####')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "7 654 321,4857")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test of format-number minus-sign behavior on positive numbers. .
   */
  @org.junit.Test
  public void numberformat27() {
    final XQuery query = new XQuery(
      "string-join((format-number(2392.14*36.58,'000,000.000000','myminus'),\n" +
      "                        format-number(2392.14*36.58,'000,000.000000;###,###.000###'),\n" +
      "                        format-number(2392.14*36.58,'000,000.000000;###,###.000###','myminus')), ' ')\n" +
      "      ",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "087,504.481200 087,504.481200 087,504.481200")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test what happens to minus sign embedded in second pattern. .
   */
  @org.junit.Test
  public void numberformat28() {
    final XQuery query = new XQuery(
      "format-number(2392.14*(-36.58),'000,000.000###;-###,###.000###')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-87,504.4812")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test decimal-format output character does not influence input. .
   */
  @org.junit.Test
  public void numberformat29() {
    final XQuery query = new XQuery(
      "format-number(-26931.4,'+###,###.###;_###,###.###')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "_26,931.4")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test effects of minus-sign in one pattern. .
   */
  @org.junit.Test
  public void numberformat30() {
    final XQuery query = new XQuery(
      "string-join((\n" +
      "                format-number(-26931.4,'-###,###.###'),\n" +
      "                format-number(-26931.4,'zzz-###,###.###','myminus'),\n" +
      "                format-number(-26931.4,'_###,###.###','myminus')), ' ')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "--26,931.4 _zzz-26,931.4 __26,931.4")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test output of altered minus, 2 patterns but no sign marker in pattern. .
   */
  @org.junit.Test
  public void numberformat31() {
    final XQuery query = new XQuery(
      "format-number(-26931.4,'###,###.###;###,###.###')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "26,931.4")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test of format-number percent format with character being changed. .
   */
  @org.junit.Test
  public void numberformat32() {
    final XQuery query = new XQuery(
      "format-number(0.4857,'###.###c')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "48.57c")
    );
  }

  /**
   * 
   *         Creator: David Marston (modified by MHK because there are now rules on choosing a zero-digit) 
   *         Purpose: Test changing both digit and zero-digit in format string. .
   */
  @org.junit.Test
  public void numberformat34() {
    final XQuery query = new XQuery(
      "format-number(4030201.0506,'#!!!,!!!,٠٠٠.٠٠٠٠٠٠0')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "#٤,٠٣٠,٢٠١.٠٥٠٦٠٠0")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test of unequal spacing of grouping-separator. .
   */
  @org.junit.Test
  public void numberformat35() {
    final XQuery query = new XQuery(
      "format-number(987654321,'###,##0,00.00')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "9876,543,21.00")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test what happens when we overflow available digits on the left. .
   */
  @org.junit.Test
  public void numberformat36() {
    final XQuery query = new XQuery(
      "format-number(239236.588,'00000.00')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "239236.59")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test result pattern for infinity, unchanged. .
   */
  @org.junit.Test
  public void numberformat37() {
    final XQuery query = new XQuery(
      "format-number(1 div 0e0,'###############################')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Infinity")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test result pattern for negative infinity, unchanged. .
   */
  @org.junit.Test
  public void numberformat39() {
    final XQuery query = new XQuery(
      "format-number(-1 div 0e0,'###############################')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-Infinity")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Test specification of result pattern for infinity.
   */
  @org.junit.Test
  public void numberformat40() {
    final XQuery query = new XQuery(
      "format-number(-1 div 0e0,'###############################')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-huge")
    );
  }

  /**
   * 
   *         Test format-number() applied to large numbers (test how good the rounding is)
   *         Bug report from Pedro Christian against Saxon 7.8
   *       .
   */
  @org.junit.Test
  public void numberformat60() {
    final XQuery query = new XQuery(
      "string-join((format-number(1E25,'#,######'),\n" +
      "                            format-number(1E10,'#####################'),\n" +
      "                            format-number(1E11,'#####################'),\n" +
      "                            format-number(1E12,'#####################'),\n" +
      "                            format-number(1E13,'#####################'),\n" +
      "                            format-number(1E14,'#####################'),\n" +
      "                            format-number(1E15,'#####################'),\n" +
      "                            format-number(1E16,'#####################'),\n" +
      "                            format-number(1E17,'#####################'),\n" +
      "                            format-number(1E18,'#####################'),\n" +
      "                            format-number(1E19,'#####################'),\n" +
      "                            format-number(1E20,'#####################'),\n" +
      "                            format-number(1E21,'#####################'),\n" +
      "                            format-number(1E22,'#####################'),\n" +
      "                            format-number(1E23,'#####################'),\n" +
      "                            format-number(1E24,'#####################'),\n" +
      "                            format-number(1E25,'#####################'),\n" +
      "                            format-number(1E30,'#####################'),\n" +
      "                            format-number(1E35,'#####################'),\n" +
      "                            format-number(1E100,'#####################'),\n" +
      "                            format-number(1E100 div 3,'#####################')), ';\n" +
      "')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "10,000000,000000,000000,000000;\n10000000000;\n100000000000;\n1000000000000;\n10000000000000;\n100000000000000;\n1000000000000000;\n10000000000000000;\n100000000000000000;\n1000000000000000000;\n10000000000000000000;\n100000000000000000000;\n1000000000000000000000;\n10000000000000000000000;\n100000000000000000000000;\n1000000000000000000000000;\n10000000000000000000000000;\n1000000000000000000000000000000;\n100000000000000000000000000000000000;\n10000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000;\n3333333333333333224453896013722304246165110619355184909726539264904319486405759542029132894851563520")
    );
  }

  /**
   * 
   *         format-number() applied to an empty sequence
   *       .
   */
  @org.junit.Test
  public void numberformat61() {
    final XQuery query = new XQuery(
      "format-number((),'###.###')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * 
   *         Purpose: Test formatting of a high-precision decimal value. .
   */
  @org.junit.Test
  public void numberformat63() {
    final XQuery query = new XQuery(
      "format-number(000123456789012345678901234567890.123456789012345678900000,     '##0.0####################################################')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "123456789012345678901234567890.1234567890123456789")
    );
  }

  /**
   * 
   *         Purpose: Test formatting of a high-precision integer value. .
   */
  @org.junit.Test
  public void numberformat64() {
    final XQuery query = new XQuery(
      "format-number(000123456789012345678901234567890123456789012345678900000,     '# #0.0####################################################')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "12 34 56 78 90 12 34 56 78 90 12 34 56 78 90 12 34 56 78 90 12 34 56 78 90 00 00.0")
    );
  }

  /**
   * 
   *         Purpose: Test that a trailing decimalpoint is removed. 
   *       .
   */
  @org.junit.Test
  public void numberformat65() {
    final XQuery query = new XQuery(
      "concat(format-number(1234e0, '0000.####'), '|',\n" +
      "                    format-number(1234.00, '0000.####'))",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1234|1234")
    );
  }

  /**
   * 
   *         Decimal separator and grouping separator in the astral planes
   *       .
   */
  @org.junit.Test
  public void numberformat70() {
    final XQuery query = new XQuery(
      "format-number(1234567890.123456,'\ud82b\uddb1000\ud82b\uddb0000')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1\ud82b\uddb1234\ud82b\uddb1567\ud82b\uddb1890\ud82b\uddb0123")
    );
  }

  /**
   * 
   *         Use Osmanya digits (non-BMP) in formatted output
   *       .
   */
  @org.junit.Test
  public void numberformat71() {
    final XQuery query = new XQuery(
      "format-number(1234567890.123456,'##########𐒠.𐒠#####')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "𐒡𐒢𐒣𐒤𐒥𐒦𐒧𐒨𐒩𐒠.𐒡𐒢𐒣𐒤𐒥𐒦")
    );
  }

  /**
   * 
   *         Check that overflow isn't an error
   *       .
   */
  @org.junit.Test
  public void numberformat72() {
    final XQuery query = new XQuery(
      "format-number(1234567890.123456,'000.000')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1234567890.123")
    );
  }

  /**
   * 
   *         PURPOSE: test format-number() with two arguments
   *       .
   */
  @org.junit.Test
  public void numberformat80() {
    final XQuery query = new XQuery(
      "format-number(12.34, '##.##')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "12.34")
    );
  }

  /**
   * 
   *         PURPOSE: test format-number() with three arguments
   *       .
   */
  @org.junit.Test
  public void numberformat81() {
    final XQuery query = new XQuery(
      "format-number(12.34, '0.000,00', 'b:test')",
      ctx);
    query.namespace("a", "http://a.ns/");
    query.namespace("b", "http://a.ns/");
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0.012,34")
    );
  }

  /**
   * 
   *         PURPOSE: test format-number() with grouping separator
   *       .
   */
  @org.junit.Test
  public void numberformat84() {
    final XQuery query = new XQuery(
      "format-number(123456789.34, '#,###.##')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "123,456,789.34")
    );
  }

  /**
   * 
   *         PURPOSE: test special case of format-number() imposing minWholePartSize = 1 with different data types
   *       .
   */
  @org.junit.Test
  public void numberformat85() {
    final XQuery query = new XQuery(
      "string-join((\n" +
      "                format-number(0, '#'),\n" +
      "                format-number(0.0, '#'),\n" +
      "                format-number(0.0e0, '#'),\n" +
      "                format-number(xs:float(0), '#')), '|')\n" +
      "        \n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0|0|0|0")
    );
  }

  /**
   * In 3.0, () allowed as third argument (bug 14931).
   */
  @org.junit.Test
  public void numberformat86() {
    final XQuery query = new XQuery(
      "format-number(0.4857,'###.###%', ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "48.57%")
    );
  }

  /**
   * 
   *         Creator: David Marston 
   *         Purpose: Create a conflict in the use of the '!' character. 
   *       .
   */
  @org.junit.Test
  public void numberformat901err() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default decimal-format decimal-separator=\"!\" grouping-separator=\"!\";\n" +
      "        format-number(931.4857,'###!###!###')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0098")
    );
  }

  /**
   * 
   *         Creator: Michael Kay (bug report from doerschlein) 
   *         Purpose: Test use of an illegal picture. .
   */
  @org.junit.Test
  public void numberformat902err() {
    final XQuery query = new XQuery(
      "format-number(931.4857,'000.##0')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XTDE1310")
    );
  }

  /**
   * 
   *         Purpose: test error condition: no digit or zero-digit in picture. .
   */
  @org.junit.Test
  public void numberformat905err() {
    final XQuery query = new XQuery(
      "format-number(931.4857,'fred.ginger', 'q')",
      ctx);
    // decimal format

    final QT3Result res = result(query);
    result = res;
    test(
      error("XTDE1310")
    );
  }

  /**
   * 
   *         Creator: Zhen Hua Liu
   *         Purpose: Test raising error FODF1280. .
   */
  @org.junit.Test
  public void numberformatFODF1280() {
    final XQuery query = new XQuery(
      "format-number(931.45, '000.##0', 'foo:bar')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODF1280")
    );
  }

  /**
   * 
   *         Creator: Zhen Hua Liu
   *         Purpose: Test wrong arg datatype inputs for format-number. .
   */
  @org.junit.Test
  public void numberformatInputErr() {
    final XQuery query = new XQuery(
      "format-number(931.45, 931.45)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   *         Creator: Zhen Hua Liu
   *         Purpose: NaN input from number("abc"). .
   */
  @org.junit.Test
  public void numberformatNaN() {
    final XQuery query = new XQuery(
      "format-number(number(\"abc\"),'#############')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }
}
