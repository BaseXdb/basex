package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnCodepointsToString extends QT3TestSet {

  /**
   *  A test whose essence is: `codepoints-to-string()`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc1() {
    final XQuery query = new XQuery(
      "codepoints-to-string()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(10) eq "&#xA;"`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc10() {
    final XQuery query = new XQuery(
      "codepoints-to-string(10) eq \"\n" +
      "\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Codepoint 11 is invalid in XML 1.0 but valid in XML 1.1. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc11() {
    final XQuery query = new XQuery(
      "codepoints-to-string(11)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  Codepoint 12 is invalid in XML 1.0 but valid in XML 1.1. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc12() {
    final XQuery query = new XQuery(
      "codepoints-to-string(12)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(13) eq "&#xd;"`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc13() {
    final XQuery query = new XQuery(
      "codepoints-to-string(13) eq \"&#xD;\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Codepoint 14 is invalid in XML 1.0 but valid in XML 1.1. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc14() {
    final XQuery query = new XQuery(
      "string-to-codepoints(codepoints-to-string(14))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("14")
      ||
        error("FOCH0001")
      )
    );
  }

  /**
   *  Codepoint 31 is invalid in XML 1.0 but valid in XML 1.1. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc15() {
    final XQuery query = new XQuery(
      "string-to-codepoints(codepoints-to-string(31))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("31")
      ||
        error("FOCH0001")
      )
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(32) eq "&#x20;"`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc16() {
    final XQuery query = new XQuery(
      "codepoints-to-string(32) eq \" \"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(27637) eq "&#x6bf5;"`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc17() {
    final XQuery query = new XQuery(
      "codepoints-to-string(27637) eq \"毵\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(55295) eq "&#xD7FF;"`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc18() {
    final XQuery query = new XQuery(
      "codepoints-to-string(55295) eq \"\ud7ff\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(55296)`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc19() {
    final XQuery query = new XQuery(
      "codepoints-to-string(55296)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string((84, 104), "INVALID")`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc2() {
    final XQuery query = new XQuery(
      "codepoints-to-string((84, 104), \"INVALID\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(57343)`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc20() {
    final XQuery query = new XQuery(
      "codepoints-to-string(57343)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(57344) eq "&#xE000;"`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc21() {
    final XQuery query = new XQuery(
      "codepoints-to-string(57344) eq \"\ue000\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(61438) eq "&#xEFFE;"`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc22() {
    final XQuery query = new XQuery(
      "codepoints-to-string(61438) eq \"\ueffe\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(65533) eq "&#xFFFD;"`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc23() {
    final XQuery query = new XQuery(
      "codepoints-to-string(65533) eq \"�\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(65534)`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc24() {
    final XQuery query = new XQuery(
      "codepoints-to-string(65534)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(65535)`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc25() {
    final XQuery query = new XQuery(
      "codepoints-to-string(65535)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(65536) eq "&#x10000;"`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc26() {
    final XQuery query = new XQuery(
      "codepoints-to-string(65536) eq \"𐀀\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(589823) eq "&#x8FFFF;"`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc27() {
    final XQuery query = new XQuery(
      "codepoints-to-string(589823) eq \"򏿿\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(1114111) eq "&#x10FFFF;"`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc28() {
    final XQuery query = new XQuery(
      "codepoints-to-string(1114111) eq \"􏿿\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(1114112)`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc29() {
    final XQuery query = new XQuery(
      "codepoints-to-string(1114112)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(()) eq ""`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc3() {
    final XQuery query = new XQuery(
      "codepoints-to-string(()) eq \"\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string((87, 36, 56, 87, 102, 96)) eq "W$8Wf`"`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc4() {
    final XQuery query = new XQuery(
      "codepoints-to-string((87, 36, 56, 87, 102, 96)) eq \"W$8Wf`\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(57343)`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc5() {
    final XQuery query = new XQuery(
      "codepoints-to-string(57343)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(-500)`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc6() {
    final XQuery query = new XQuery(
      "codepoints-to-string(-500)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(0)`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc7() {
    final XQuery query = new XQuery(
      "codepoints-to-string(0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  Codepoint 8 is invalid in XML 1.0 but valid in XML 1.1. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc8() {
    final XQuery query = new XQuery(
      "codepoints-to-string(8)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "&#x8;")
      ||
        error("FOCH0001")
      )
    );
  }

  /**
   *  A test whose essence is: `codepoints-to-string(9) eq "&#x9;"`. .
   */
  @org.junit.Test
  public void kCodepointToStringFunc9() {
    final XQuery query = new XQuery(
      "codepoints-to-string(9) eq \"\t\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:codepoints-to-string with invalid control character .
   */
  @org.junit.Test
  public void cbclCodepointsToString001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:test($test as xs:integer) as xs:integer? { \n" +
      "          if ($test = 1) then ( 0 ) else if ($test = 2) then ( 9 ) else if ($test = 3) then ( 13 ) else if ($test = 4) then ( 16 ) else () \n" +
      "        }; \n" +
      "        fn:codepoints-to-string( local:test(1) to 32 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  test fn:codepoints-to-string with invalid control character .
   */
  @org.junit.Test
  public void cbclCodepointsToString002() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:test($test as xs:integer) as xs:integer? { \n" +
      "          if ($test = 1) then ( 0 ) else if ($test = 2) then ( 9 ) else if ($test = 3) then ( 13 ) else if ($test = 4) then ( 16 ) else () \n" +
      "        }; \n" +
      "        fn:codepoints-to-string( local:test(2) to 32 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  test fn:codepoints-to-string with invalid control character .
   */
  @org.junit.Test
  public void cbclCodepointsToString003() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:test($test as xs:integer) as xs:integer? { \n" +
      "        if ($test = 1) then ( 0 ) else if ($test = 2) then ( 9 ) else if ($test = 3) then ( 13 )else if ($test = 4) then ( 16 ) else () \n" +
      "      }; \n" +
      "      fn:codepoints-to-string( local:test(3) to 32 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  test fn:codepoints-to-string with invalid control character .
   */
  @org.junit.Test
  public void cbclCodepointsToString004() {
    final XQuery query = new XQuery(
      "declare function local:test($test as xs:integer) as xs:integer? { \n" +
      "        if ($test = 1) then ( 0 ) else if ($test = 2) then ( 9 ) else if ($test = 3) then ( 13 ) else if ($test = 4) then ( 16 ) else () \n" +
      "      }; \n" +
      "      fn:codepoints-to-string( local:test(4) to 32 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  test fn:codepoints-to-string with codepoints above 0x10FFFF .
   */
  @org.junit.Test
  public void cbclCodepointsToString005() {
    final XQuery query = new XQuery(
      "fn:codepoints-to-string( 65536 to 1114112 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  test fn:codepoints-to-string spanning surrogate characters .
   */
  @org.junit.Test
  public void cbclCodepointsToString006() {
    final XQuery query = new XQuery(
      "fn:codepoints-to-string( 55295 to 55297 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  test fn:codepoints-to-string spanning surrogate characters .
   */
  @org.junit.Test
  public void cbclCodepointsToString007() {
    final XQuery query = new XQuery(
      "fn:codepoints-to-string( 55296 to 57343 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  test fn:codepoints-to-string spanning surrogate characters .
   */
  @org.junit.Test
  public void cbclCodepointsToString008() {
    final XQuery query = new XQuery(
      "fn:codepoints-to-string( 65535 to 70000 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  test fn:codepoints-to-string spanning surrogate characters .
   */
  @org.junit.Test
  public void cbclCodepointsToString009() {
    final XQuery query = new XQuery(
      "fn:codepoints-to-string( 65530 to 70000 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  test fn:boolean on fn:codepoints-to-string .
   */
  @org.junit.Test
  public void cbclCodepointsToString010() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:codepoints-to-string( 65 to 76 ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:boolean on fn:codepoints-to-string .
   */
  @org.junit.Test
  public void cbclCodepointsToString011() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:codepoints-to-string( 0 ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  test fn:boolean on fn:codepoints-to-string .
   */
  @org.junit.Test
  public void cbclCodepointsToString012() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:codepoints-to-string( 999999999 ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  test fn:boolean on fn:codepoints-to-string .
   */
  @org.junit.Test
  public void cbclCodepointsToString013() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:codepoints-to-string( 65 ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test fn:boolean on fn:codepoints-to-string .
   */
  @org.junit.Test
  public void cbclCodepointsToString014() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:codepoints-to-string( () ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  fn:codepoints-to-string on multibyte ranges .
   */
  @org.junit.Test
  public void cbclCodepointsToString015() {
    final XQuery query = new XQuery(
      "deep-equal( fn:string-to-codepoints(fn:codepoints-to-string(65536 to 66000)), 65536 to 66000 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:codepoints-to-string on multibyte ranges .
   */
  @org.junit.Test
  public void cbclCodepointsToString016() {
    final XQuery query = new XQuery(
      "deep-equal( fn:string-to-codepoints(fn:codepoints-to-string(65536 to 100000)), 65536 to 100000 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test codepoints to string on a range, returning a boolean .
   */
  @org.junit.Test
  public void cbclCodepointsToString017() {
    final XQuery query = new XQuery(
      "for $x in 32 to 64 return boolean(codepoints-to-string($x to $x + 10))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true true true true true true true true true true true true true true true true true true true true true true true true true true true true true true true true true")
    );
  }

  /**
   *  Tries to force evaluate to item on codepoint-to-string .
   */
  @org.junit.Test
  public void cbclCodepointsToString018() {
    final XQuery query = new XQuery(
      "if(5 < exactly-one((1 to 10)[. div 2 = 5])) then codepoints-to-string(32 to exactly-one((1 to 100)[. div 2 = 40])) else ()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOP")
    );
  }

  /**
   *  Tests for calling EvaluateToOptionalItem .
   */
  @org.junit.Test
  public void cbclCodepointsToString019() {
    final XQuery query = new XQuery(
      "for $x in 65 to 75 return string-length(codepoints-to-string($x to $x+10))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "11 11 11 11 11 11 11 11 11 11 11")
    );
  }

  /**
   *  Tests false returns from TryArguments .
   */
  @org.junit.Test
  public void cbclCodepointsToString020() {
    final XQuery query = new XQuery(
      "for $x in 65 to 75 return boolean(codepoints-to-string($x[. mod 2 = 0] to ($x+9)[. mod 2 = 0]))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "false false false false false false false false false false false")
    );
  }

  /**
   *  Tests overflows .
   */
  @org.junit.Test
  public void cbclCodepointsToString021() {
    final XQuery query = new XQuery(
      "let $y := 65536*65536 return for $x in $y to $y+10 return codepoints-to-string(65 to $x)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  Tests overflows .
   */
  @org.junit.Test
  public void cbclCodepointsToString022() {
    final XQuery query = new XQuery(
      "let $y := 65536*65536 return for $x in $y to $y+10 return codepoints-to-string($x to $x+10)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  Tests invalid codepoint 0xB .
   */
  @org.junit.Test
  public void cbclCodepointsToString023() {
    final XQuery query = new XQuery(
      "for $x in 9 to 15 return codepoints-to-string($x to $x)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  Tests invalid codepoint 0xE .
   */
  @org.junit.Test
  public void cbclCodepointsToString024() {
    final XQuery query = new XQuery(
      "for $x in 13 to 15 return codepoints-to-string($x to $x)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  Tests valid codepoints 0x9, 0xA .
   */
  @org.junit.Test
  public void cbclCodepointsToString025() {
    final XQuery query = new XQuery(
      "for $x in 9 to 9 return codepoints-to-string($x to $x+1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("'\t\n'")
    );
  }

  /**
   *  Tests valid codepoint 0xD .
   */
  @org.junit.Test
  public void cbclCodepointsToString026() {
    final XQuery query = new XQuery(
      "for $x in 13 to 13 return codepoints-to-string($x to $x)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "\r")
    );
  }

  /**
   *  Tests last lower codepoint case .
   */
  @org.junit.Test
  public void cbclCodepointsToString027() {
    final XQuery query = new XQuery(
      "for $x in (13), $y in (13,9,10) return codepoints-to-string($x to $y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "\r  ")
    );
  }

  /**
   *  Invalid XML character codepoint as part of "code-points-to-string" function. .
   */
  @org.junit.Test
  public void fnCodepointsToString1() {
    final XQuery query = new XQuery(
      "fn:codepoints-to-string(0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  Evaluation of an "fn:codepoints-to-string" used as argument to the "fn:lower-case" function and use 
   *         codepoints 97,32,115,116,114,105,110,103 (String "a string"). .
   */
  @org.junit.Test
  public void fnCodepointsToString10() {
    final XQuery query = new XQuery(
      "fn:lower-case(fn:codepoints-to-string((97,32,115,116,114,105,110,103)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a string")
    );
  }

  /**
   *  Evaluation of an "fn:codepoints-to-string" that uses xs:integer as argument and codepoints 97. .
   */
  @org.junit.Test
  public void fnCodepointsToString11() {
    final XQuery query = new XQuery(
      "fn:codepoints-to-string(xs:integer(97))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a")
    );
  }

  /**
   *  Evaluation of an "fn:codepoints-to-string" that uses fn:avg/xs:integer function and codepoints 65,32,83,116,114,105,110,103. .
   */
  @org.junit.Test
  public void fnCodepointsToString12() {
    final XQuery query = new XQuery(
      "fn:codepoints-to-string(xs:integer(fn:avg((65,32,83,116,114,105,110,103))))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "[")
    );
  }

  /**
   *  Evaluation of an "fn:codepoints-to-string" that is used as argument to fn:concat function. .
   */
  @org.junit.Test
  public void fnCodepointsToString13() {
    final XQuery query = new XQuery(
      "fn:concat(fn:codepoints-to-string((49,97)),\"1a\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1a1a")
    );
  }

  /**
   *  Evaluation of an "fn:codepoints-to-string" that is used as argument to fn:string-to-codepoints function. .
   */
  @org.junit.Test
  public void fnCodepointsToString14() {
    final XQuery query = new XQuery(
      "fn:string-to-codepoints(fn:codepoints-to-string((49,97)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("49, 97")
    );
  }

  /**
   *  Evaluation of an "fn:codepoints-to-string" that is used as argument to fn:string-length function. .
   */
  @org.junit.Test
  public void fnCodepointsToString15() {
    final XQuery query = new XQuery(
      "fn:string-length(fn:codepoints-to-string((49,97)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
   *  Evaluation of an "fn:codepoints-to-string" that is used as argument to fn:string-join function. .
   */
  @org.junit.Test
  public void fnCodepointsToString16() {
    final XQuery query = new XQuery(
      "fn:string-join((fn:codepoints-to-string((49,97)),'ab'),'')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1aab")
    );
  }

  /**
   *  Invalid XML character codepoint as part of "code-points-to-string" function. .
   */
  @org.junit.Test
  public void fnCodepointsToString2() {
    final XQuery query = new XQuery(
      "fn:codepoints-to-string(10000000)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  Evaluation of an "fn:codepoints-to-string" with argument set to codepoint 49 (single character ('1')). .
   */
  @org.junit.Test
  public void fnCodepointsToString3() {
    final XQuery query = new XQuery(
      "fn:codepoints-to-string(49)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Evaluation of an "fn-codepoints-to-string" with argument set to codepoint 97 (a single character 'a'). .
   */
  @org.junit.Test
  public void fnCodepointsToString4() {
    final XQuery query = new XQuery(
      "fn:codepoints-to-string(97)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a")
    );
  }

  /**
   *  Evaluation of an "fn:codepoints-to-string" with argument set to codepoints 49, 97 (combination of number/character '1a'). .
   */
  @org.junit.Test
  public void fnCodepointsToString5() {
    final XQuery query = new XQuery(
      "fn:codepoints-to-string((49,97))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1a")
    );
  }

  /**
   *  Evaluation of an "fn:codepoints-to-string" with argument set to the codepoints 35, 42, 94 36 (characters "#*^$"). .
   */
  @org.junit.Test
  public void fnCodepointsToString6() {
    final XQuery query = new XQuery(
      "fn:codepoints-to-string((35, 42, 94, 36))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "#*^$")
    );
  }

  /**
   *  Evaluation of an "fn:codepoints-to-string" with argument set to codepoints 
   *         99 111 100 101 112 111 105 110 116 115 45 116 111 45 115 116 114 105 110 103 (string "codepoints-to-string"). .
   */
  @org.junit.Test
  public void fnCodepointsToString7() {
    final XQuery query = new XQuery(
      "fn:codepoints-to-string((99,111,100,101,112,111,105,110,116,115,45,116,111,45,115,116,114,105,110,103))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "codepoints-to-string")
    );
  }

  /**
   *  Evaluation of an "fn:codepoints-to-string" used as argument to "xs:string()" function and uses 
   *         codepoints 65, 32, 83 116, 114, 105, 110, 103 ("A String") . .
   */
  @org.junit.Test
  public void fnCodepointsToString8() {
    final XQuery query = new XQuery(
      "xs:string(fn:codepoints-to-string((65,32,83,116,114,105,110,103)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "A String")
    );
  }

  /**
   *  Evaluation of an "fn:codepoints-to-string" used as argument to the "fn:upper-case" function and use 
   *         codepoints 65,32,83,84,82,73,78,71 (string "A STRING"). .
   */
  @org.junit.Test
  public void fnCodepointsToString9() {
    final XQuery query = new XQuery(
      "fn:upper-case(fn:codepoints-to-string((65,32,83,84,82,73,78,71)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "A STRING")
    );
  }

  /**
   * Test codepoints-to-string with variety of characters .
   */
  @org.junit.Test
  public void fnCodepointsToString1args1() {
    final XQuery query = new XQuery(
      "codepoints-to-string((98,223,1682,12365,63744))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "bßڒき豈")
    );
  }

  /**
   * Test codepoints-to-string with an empty sequence argument .
   */
  @org.junit.Test
  public void fnCodepointsToString1args2() {
    final XQuery query = new XQuery(
      "codepoints-to-string(())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
   * Test invalid type in argument for codepoints-to-string .
   */
  @org.junit.Test
  public void fnCodepointsToString1args3() {
    final XQuery query = new XQuery(
      "codepoints-to-string('hello')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * Test incorrect arity for codepoints-to-string .
   */
  @org.junit.Test
  public void fnCodepointsToString1args4() {
    final XQuery query = new XQuery(
      "codepoints-to-string((),())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }
}
