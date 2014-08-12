package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the add-dayTimeDurations() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpAddDayTimeDurations extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationAdd-1                          
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of adding an xs:dayTimeDuration with PT0S. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationAdd1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT4H3M3.100S\") + xs:dayTimeDuration(\"PT0S\") eq xs:dayTimeDuration(\"P3DT4H3M3.100S\")",
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
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationAdd-2                          
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of substraction PT0S with an xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationAdd2() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"PT0S\") + xs:dayTimeDuration(\"P3DT4H3M3.100S\") eq xs:dayTimeDuration(\"P3DT4H3M3.100S\")",
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
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationAdd-3                          
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of adding two arbitrary xs:yearMonthDurations. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationAdd3() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT4H3M3.100S\") + xs:dayTimeDuration(\"P3DT12H31M56.303S\") eq xs:dayTimeDuration(\"P6DT16H34M59.403S\")",
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
   *  test addition of zero duration to dayTimeDuration .
   */
  @org.junit.Test
  public void cbclAddDayTimeDurations001() {
    final XQuery query = new XQuery(
      "declare function local:dayTimeDuration($days as xs:integer, $hours as xs:integer ) { xs:dayTimeDuration(concat('P', $days, 'DT', $hours, 'H')) }; local:dayTimeDuration(1, 1) + xs:dayTimeDuration(\"P0D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1DT1H")
    );
  }

  /**
   *  test addition of zero duration to dayTimeDuration .
   */
  @org.junit.Test
  public void cbclAddDayTimeDurations002() {
    final XQuery query = new XQuery(
      "declare function local:dayTimeDuration($days as xs:integer, $hours as xs:integer ) { xs:dayTimeDuration(concat('P', $days, 'DT', $hours, 'H')) }; xs:dayTimeDuration(\"P0D\") + local:dayTimeDuration(1, 1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1DT1H")
    );
  }

  /**
   *  test addition of dayTimeDurations .
   */
  @org.junit.Test
  public void cbclAddDayTimeDurations003() {
    final XQuery query = new XQuery(
      "declare function local:dayTimeDuration($days as xs:integer, $hours as xs:integer ) { xs:dayTimeDuration(concat('P', $days, 'DT', $hours, 'H')) }; local:dayTimeDuration(1, 1) + local:dayTimeDuration(1, 1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P2DT2H")
    );
  }

  /**
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") };\n" +
      "        xs:date(\"1997-01-01\") + local:f(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1997-01-02")
    );
  }

  /**
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus002() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") };\n" +
      "        xs:date(\"1997-01-01\") + local:f(true())",
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
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus003() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        xs:date(\"1997-01-01\") + local:f(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1997-02-01")
    );
  }

  /**
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus004() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        xs:date(\"1997-01-01\") + local:f(true())",
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
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus005() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") };\n" +
      "        xs:dateTime(\"1997-01-01T12:00:00\") + local:f(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1997-01-02T12:00:00")
    );
  }

  /**
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus006() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") };\n" +
      "        xs:dateTime(\"1997-01-01T12:00:00\") + local:f(true())",
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
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus007() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        xs:dateTime(\"1997-01-01T12:00:00\") + local:f(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1997-02-01T12:00:00")
    );
  }

  /**
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus008() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        xs:dateTime(\"1997-01-01T12:00:00\") + local:f(true())",
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
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus009() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"PT1H\") };\n" +
      "        xs:time(\"12:00:00\") + local:f(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "13:00:00")
    );
  }

  /**
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus010() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1H\") };\n" +
      "        xs:time(\"12:00:00\") + local:f(true())",
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
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus011() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") };\n" +
      "        xs:dayTimeDuration(\"PT1H\") + local:f(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1DT1H")
    );
  }

  /**
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus012() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") };\n" +
      "        xs:dayTimeDuration(\"PT1H\") + local:f(true())",
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
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus013() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        xs:yearMonthDuration(\"P1Y\") + local:f(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1Y1M")
    );
  }

  /**
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus014() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        xs:yearMonthDuration(\"P1Y\") + local:f(true())",
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
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus015() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") };\n" +
      "        local:f(false()) + xs:date(\"1997-01-01\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1997-01-02")
    );
  }

  /**
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus016() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") };\n" +
      "        local:f(true()) + xs:date(\"1997-01-01\")",
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
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus017() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        local:f(false()) + xs:date(\"1997-01-01\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1997-02-01")
    );
  }

  /**
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus018() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        local:f(true()) + xs:date(\"1997-01-01\")",
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
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus019() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") };\n" +
      "        local:f(false()) + xs:dateTime(\"1997-01-01T12:00:00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1997-01-02T12:00:00")
    );
  }

  /**
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus020() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") };\n" +
      "        local:f(true()) + xs:dateTime(\"1997-01-01T12:00:00\")",
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
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus021() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        local:f(false()) + xs:dateTime(\"1997-01-01T12:00:00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1997-02-01T12:00:00")
    );
  }

  /**
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus022() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        local:f(true()) + xs:dateTime(\"1997-01-01T12:00:00\")",
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
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus023() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"PT1H\") };\n" +
      "        local:f(false()) + xs:time(\"12:00:00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "13:00:00")
    );
  }

  /**
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus024() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"PT1H\") };\n" +
      "        local:f(true()) + xs:time(\"12:00:00\")",
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
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus025() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") };\n" +
      "        local:f(false()) + xs:dayTimeDuration(\"PT1H\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1DT1H")
    );
  }

  /**
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus026() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") };\n" +
      "        local:f(true()) + xs:dayTimeDuration(\"PT1H\")",
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
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus027() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        local:f(false()) + xs:yearMonthDuration(\"P1Y\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1Y1M")
    );
  }

  /**
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus028() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        local:f(true()) + xs:yearMonthDuration(\"P1Y\")",
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
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus029() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        local:f(false()) + local:f(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P2M")
    );
  }

  /**
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus030() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        local:f(true()) + local:f(true())",
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
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus031() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") };\n" +
      "        local:f(false()) + local:f(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P2D")
    );
  }

  /**
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus032() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") };\n" +
      "        local:f(true()) + local:f(true())",
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
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus033() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") };\n" +
      "        declare function local:g($x) { if ($x) then xs:duration(\"P1M\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        local:f(false()) + local:g(false())",
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
   *  Test behaviour of plus operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclPlus034() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") };\n" +
      "        declare function local:g($x) { if ($x) then xs:duration(\"P1M\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        local:f(true()) + local:g(true())",
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
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations-1                        
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P2DT12H5M\") + xs:dayTimeDuration(\"P5DT12H\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P8DT5M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations-10                       
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function used  
   * together with an "or" expression.                      
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations10() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P05DT09H02M\") + xs:dayTimeDuration(\"P03DT01H04M\"))) or fn:string((xs:dayTimeDuration(\"P05DT05H03M\") + xs:dayTimeDuration(\"P01DT01H03M\")))",
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
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations-11                       
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function used  
   * together with a "div" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations11() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P42DT10H10M\") + xs:dayTimeDuration(\"P10DT10H10M\")) div (xs:dayTimeDuration(\"P42DT10H10M\") + xs:dayTimeDuration(\"P10DT10H10M\"))",
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
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations-12                       
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" operators used 
   * with a boolean expression and the "fn:true" function.   
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations12() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P10DT08H11M\") + xs:dayTimeDuration(\"P05DT08H11M\"))) and (fn:true())",
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
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations-13                       
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function used  
   * together with the numeric-equal-operator "eq".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations13() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P23DT11H11M\") + xs:dayTimeDuration(\"P23DT11H11M\")) eq xs:dayTimeDuration(\"P23DT11H11M\")",
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
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations-14                       
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations14() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P21DT08H12M\") + xs:dayTimeDuration(\"P08DT08H05M\")) ne xs:dayTimeDuration(\"P08DT08H05M\")",
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
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations-15                       
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations15() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT10H01M\") + xs:dayTimeDuration(\"P17DT10H02M\")) le xs:dayTimeDuration(\"P17DT10H02M\")",
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
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations-16                       
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations16() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P13DT09H09M\") + xs:dayTimeDuration(\"P18DT02H02M\")) ge xs:dayTimeDuration(\"P18DT02H02M\")",
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
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations-2                        
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function 
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   *  Uses "fn:string" to account for new EBV rules.        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations2() {
    final XQuery query = new XQuery(
      "fn:string(xs:dayTimeDuration(\"P10DT10H11M\") + xs:dayTimeDuration(\"P12DT10H07M\")) and fn:false()",
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
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations-3                        
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function as 
   * part of a boolean expression (or operator) and the "fn:boolean" function. 
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations3() {
    final XQuery query = new XQuery(
      "fn:string(xs:dayTimeDuration(\"P20DT20H10M\") + xs:dayTimeDuration(\"P19DT13H10M\")) or fn:false()",
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
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations-4                        
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function that  
   * return true and used together with fn:not.             
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:dayTimeDuration(\"P11DT12H04M\") + xs:dayTimeDuration(\"P02DT07H01M\")))",
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
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations-5                        
   * Written By: Carmelo Montanez                           
   * Date: June 29 2005                                     
   * Purpose: Evaluates The "add-dayTimeDurations" function that  
   * is used as an argument to the fn:boolean function.     
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:dayTimeDuration(\"P05DT09H08M\") + xs:dayTimeDuration(\"P03DT08H06M\")))",
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
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations-6                        
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations6() {
    final XQuery query = new XQuery(
      "fn:number(xs:dayTimeDuration(\"P02DT06H09M\") + xs:dayTimeDuration(\"P10DT08H01M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations-7                        
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations7() {
    final XQuery query = new XQuery(
      "fn:string(xs:dayTimeDuration(\"P03DT04H08M\") + xs:dayTimeDuration(\"P01DT09H02M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P4DT13H10M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations-8                        
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations8() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT01H01M\") + xs:dayTimeDuration(\"-P11DT02H02M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-P1DT1H1M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations-9                        
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function used 
   * together with and "and" expression.                    
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations9() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P01DT02H01M\") + xs:dayTimeDuration(\"P02DT09H02M\"))) and fn:string((xs:dayTimeDuration(\"P02DT03H03M\") + xs:dayTimeDuration(\"P04DT04H04M\")))",
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
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations2args-1                    
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDurations" operator
   *  with the arguments set as follows:                    
   * Sarg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations2args1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") + xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations2args-2                    
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDurations" operator
   *  with the arguments set as follows:                    
   * Sarg1 = xs:dayTimeDuration(mid range)                 
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations2args2() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P15DT11H59M59S\") + xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P15DT11H59M59S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations2args-3                    
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDurations" operator
   *  with the arguments set as follows:                    
   * Sarg1 = xs:dayTimeDuration(upper bound)               
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations2args3() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P31DT23H59M59S\") + xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P31DT23H59M59S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations2args-4                    
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDurations" operator
   *  with the arguments set as follows:                    
   * Sarg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(mid range)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations2args4() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") + xs:dayTimeDuration(\"P15DT11H59M59S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P15DT11H59M59S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations2args-5                    
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDurations" operator
   *  with the arguments set as follows:                    
   * Sarg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(upper bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations2args5() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") + xs:dayTimeDuration(\"P31DT23H59M59S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P31DT23H59M59S")
    );
  }
}
