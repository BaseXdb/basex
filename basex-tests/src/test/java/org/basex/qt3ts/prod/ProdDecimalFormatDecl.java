package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the XQuery 3.0 decimal format declaration, controlling the format-number() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdDecimalFormatDecl extends QT3TestSet {

  /**
   * 
   *         Purpose: Test of format-number with 2 arguments, showing zeroes. .
   */
  @org.junit.Test
  public void decimalFormat01() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default decimal-format zero-digit=\"0\" grouping-separator=\",\" decimal-separator=\".\";\n" +
      "      \tformat-number(2392.14*36.58,'000,000.000000')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "087,504.481200")
    );
  }

  /**
   * 
   *         Purpose: Test of format-number and # and 0 in format string. .
   */
  @org.junit.Test
  public void decimalFormat02() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default decimal-format digit=\"#\" grouping-separator=\",\" decimal-separator=\".\";\n" +
      "      \tformat-number(12792.14*96.58,'##,###,000.000###')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1,235,464.8812")
    );
  }

  /**
   * 
   *         Purpose: Test of format-number on a negative number. .
   */
  @org.junit.Test
  public void decimalFormat03() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default decimal-format minus-sign=\"-\" grouping-separator=\",\" decimal-separator=\".\";\n" +
      "      \tformat-number(2792.14*(-36.58),'000,000.000###')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-102,136.4812")
    );
  }

  /**
   * 
   *         Purpose: Test of format-number on a negative number; should choose second pattern. .
   */
  @org.junit.Test
  public void decimalFormat04() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default decimal-format minus-sign=\"-\" pattern-separator=\";\" grouping-separator=\",\" decimal-separator=\".\";\n" +
      "      \tformat-number(2392.14*(-36.58),'000,000.000###;###,###.000###')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "87,504.4812")
    );
  }

  /**
   * 
   *         Purpose: Test of format-number percentage format. .
   */
  @org.junit.Test
  public void decimalFormat05() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare decimal-format local:df minus-sign=\"-\" percent=\"%\" decimal-separator=\".\";\n" +
      "      \tformat-number(0.4857,'###.###%', 'local:df')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "48.57%")
    );
  }

  /**
   * 
   *         Purpose: Test of format-number per-mille format. .
   */
  @org.junit.Test
  public void decimalFormat06() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare decimal-format local:df minus-sign=\"-\" per-mille=\"\u2030\" decimal-separator=\".\";\n" +
      "      \tformat-number(0.4857,'###.###\u2030')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "485.7\u2030")
    );
  }

  /**
   * 
   *         Purpose: Test of format-number currency symbol, which is not supposed to be there. .
   */
  @org.junit.Test
  public void decimalFormat07() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare decimal-format local:df minus-sign=\"-\" currency-symbol=\"¤\" decimal-separator=\".\";\n" +
      "      \tformat-number(95.4857,'¤###.####', \"local:df\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   * 
   *         Purpose: Test non-default decimal-format on separator characters, changing both. .
   */
  @org.junit.Test
  public void decimalFormat09() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare default decimal-format decimal-separator=\"|\" grouping-separator=\".\"; \n" +
      "         format-number(931.4857,'000.000|###')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "000.931|486")
    );
  }

  /**
   * 
   *         Purpose: Test default decimal-format on pattern-only characters, positive number. .
   */
  @org.junit.Test
  public void decimalFormat11() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default decimal-format digit=\"!\" pattern-separator=\"\\\";\n" +
      "        format-number(26931.4,'+!!!,!!!.!!!\\-!!,!!!.!!!')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "+26,931.4")
    );
  }

  /**
   * 
   *         Purpose: Test default decimal-format on pattern-only characters, negative number. .
   */
  @org.junit.Test
  public void decimalFormat12() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default decimal-format digit=\"!\" pattern-separator=\"\\\";\n" +
      "        format-number(-26931.4,'+!!,!!!.!!!\\-!!!,!!!.!!!')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-26,931.4")
    );
  }

  /**
   * 
   *         Purpose: Test default decimal-format on pattern-only characters, negative number and one pattern. .
   */
  @org.junit.Test
  public void decimalFormat13() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default decimal-format digit=\"!\" pattern-separator=\"\\\";\n" +
      "        format-number(-26931.4,'!!!,!!!.!!!')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-26,931.4")
    );
  }

  /**
   * 
   *         Purpose: Test specified result pattern for infinity. .
   */
  @org.junit.Test
  public void decimalFormat14() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare decimal-format local:df2 infinity=\"off-the-scale\";\n" +
      "        format-number(1 div 0e0,'###############################', 'local:df2')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "off-the-scale")
    );
  }

  /**
   * 
   *         Purpose: Test specified result pattern for not-a-number. .
   */
  @org.junit.Test
  public void decimalFormat15() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare decimal-format local:df2 NaN=\"non-numeric\";\n" +
      "        format-number(number('none'), '#############', 'local:df2')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "non-numeric")
    );
  }

  /**
   * 
   *         Purpose: Test of decimal-format per-mille format with character being changed. .
   */
  @org.junit.Test
  public void decimalFormat16() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default decimal-format  per-mille=\"m\";\n" +
      "        format-number(0.4857,'###.###m')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "485.7m")
    );
  }

  /**
   * 
   *         Purpose: Test decimal-format output character for negative, 2 patterns. .
   */
  @org.junit.Test
  public void decimalFormat17() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default decimal-format  minus-sign=\"_\";\n" +
      "        format-number(-26931.4,'+###,###.###;-###,###.###')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-26,931.4")
    );
  }

  /**
   * 
   *         Purpose: Test decimal-format output character for negative, one pattern. .
   */
  @org.junit.Test
  public void decimalFormat18() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default decimal-format minus-sign=\"_\";\n" +
      "        format-number(-26931.4,'###,###.###')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "_26,931.4")
    );
  }

  /**
   * 
   *         Purpose: Test decimal-format declaration with an unprefixed name. .
   */
  @org.junit.Test
  public void decimalFormat19() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare decimal-format myminus minus-sign=\"_\"; \n" +
      "        concat(format-number(-26931.4,'###,###.###','myminus'), '/',\n" +
      "        format-number(-42857.1,'###,###.###'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "_26,931.4/-42,857.1")
    );
  }

  /**
   * 
   *         Purpose: Test of decimal-format with qualified name. Unqualified name provided as a trap. .
   */
  @org.junit.Test
  public void decimalFormat20() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace foo=\"http://foo.ns\";\n" +
      "        declare decimal-format foo:decimal1  decimal-separator=\"!\" grouping-separator=\"*\";\n" +
      "        declare decimal-format decimal1  decimal-separator=\"*\" grouping-separator=\"!\";\n" +
      "        format-number(1234.567,'#*###*###!###','foo:decimal1')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1*234!567")
    );
  }

  /**
   * 
   *         Purpose: Decimal formats apply only within their own module. .
   */
  @org.junit.Test
  public void decimalFormat21() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace m=\"http://www.w3.org/TestModules/dfd-module-001\";\n" +
      "        declare decimal-format df001 grouping-separator=\"!\";\n" +
      "        format-number(123456.789,'#!###!###.###','df001')||'-'||m:do()\n" +
      "      ",
      ctx);
    try {
      query.addModule("http://www.w3.org/TestModules/dfd-module-001", file("prod/DecimalFormatDecl/dfd-module-001.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "123!456.789-123'456.789")
    );
  }

  /**
   * 
   *         Purpose: Create a conflict in the use of the '!' character. 
   *       .
   */
  @org.junit.Test
  public void decimalFormat901err() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default decimal-format decimal-separator=\"!\" grouping-separator=\"!\";\n" +
      "        format-number(931.4857,'###!###!###')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
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
  public void decimalFormat902err() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default decimal-format digit='$';\n" +
      "        format-number(931.4857,'000.$$0')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODF1310")
    );
  }

  /**
   * 
   *         Purpose: Two default decimal declarations. .
   */
  @org.junit.Test
  public void decimalFormat903err() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default decimal-format digit='$';\n" +
      "        declare default decimal-format minus-sign='_';\n" +
      "        format-number(931.4857,'000.$$0')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0111")
    );
  }

  /**
   * 
   *         Purpose: Two decimal format declarations with same name. .
   */
  @org.junit.Test
  public void decimalFormat904err() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace a=\"http://a.com/\";\n" +
      "        declare namespace b=\"http://a.com/\";\n" +
      "        declare decimal-format a:one digit='$';\n" +
      "        declare decimal-format two digit='$';\n" +
      "        declare decimal-format three digit='$';\n" +
      "        declare decimal-format four digit='$';\n" +
      "        declare decimal-format five digit='$';\n" +
      "        declare decimal-format b:one minus-sign=\"_\";\n" +
      "        declare default decimal-format minus-sign='_';\n" +
      "        format-number(931.4857,'000.$$0')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0111")
    );
  }

  /**
   * 
   *         Purpose: test error condition: no digit or zero-digit in picture. .
   */
  @org.junit.Test
  public void decimalFormat905err() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare decimal-format q decimal-separator=\".\" grouping-separator=\",\";\n" +
      "        format-number(931.4857,'fred.ginger', 'q')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODF1310")
    );
  }

  /**
   * 
   *         Purpose: Test raising error FODF1280. .
   */
  @org.junit.Test
  public void decimalFormat906err() {
    final XQuery query = new XQuery(
      "format-number(931.45, '000.##0', 'foo:bar')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODF1280")
    );
  }

  /**
   * 
   *         Purpose: Decimal format declaration declares the same property twice. .
   */
  @org.junit.Test
  public void decimalFormat907err() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace a=\"http://a.com/\";\n" +
      "        declare namespace b=\"http://a.com/\";\n" +
      "        declare decimal-format a:one digit='$' zero-digit=\"0\" minus-sign=\"_\" digit=\"#\";\n" +
      "        format-number(931.4857,'000.$$0', 'a:one')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0114")
    );
  }

  /**
   * 
   *         Purpose: Decimal format declaration declares invalid property value. .
   */
  @org.junit.Test
  public void decimalFormat908err() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default decimal-format digit=\"one\";\n" +
      "        format-number(931.4857,'000.$$0')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0097")
    );
  }

  /**
   * 
   *         Purpose: Decimal format declaration declares invalid property value. .
   */
  @org.junit.Test
  public void decimalFormat909err() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default decimal-format zero-digit=\"1\";\n" +
      "        format-number(931.4857,'000.$$0')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0097")
    );
  }

  /**
   * 
   *         Purpose: Decimal format declaration declares invalid property value. .
   */
  @org.junit.Test
  public void decimalFormat910err() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default decimal-format zero-digit=\"a\";\n" +
      "        format-number(931.4857,'aaa.$$a')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0097")
    );
  }

  /**
   * 
   *         Purpose: Decimal format declaration declares invalid property value. .
   */
  @org.junit.Test
  public void decimalFormat911err() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default decimal-format minus-sign=\"--\";\n" +
      "        format-number(931.4857,'000.$$0')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0097")
    );
  }
}
