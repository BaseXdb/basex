package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the divide-dayTimeDuration() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpDivideDayTimeDuration extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationDivide-1                       
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of dividing a xs:dayTimeDuration with 4. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationDivide1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P8DT4H4M4.400S\") div 4 eq xs:dayTimeDuration(\"P2DT1H1M1.1S\")",
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
   *  Test: K-DayTimeDurationDivide-10                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The division operator is not available between xs:integer and xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationDivide10() {
    final XQuery query = new XQuery(
      "3 div xs:dayTimeDuration(\"P3D\")",
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
   *  Test: K-DayTimeDurationDivide-11                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The division operator is not available between xs:dayTimeDuration and xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationDivide11() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3D\") div xs:yearMonthDuration(\"P3Y3M\")",
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
   *  Test: K-DayTimeDurationDivide-12                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The division operator is not available between xs:yearMonthDuration and xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationDivide12() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y3M\") div xs:dayTimeDuration(\"P3D\")",
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
   *  Test: K-DayTimeDurationDivide-13                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The division operator is not available between xs:duration and xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationDivide13() {
    final XQuery query = new XQuery(
      "xs:duration(\"P3D\") div xs:yearMonthDuration(\"P3Y3M\")",
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
   *  Test: K-DayTimeDurationDivide-14                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The division operator is not available between xs:yearMonthDuration and xs:duration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationDivide14() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y3M\") div xs:duration(\"P3D\")",
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
   *  Test: K-DayTimeDurationDivide-15                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The division operator is not available between xs:dayTimeDuration and xs:duration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationDivide15() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3D\") div xs:duration(\"P3Y3M\")",
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
   *  Test: K-DayTimeDurationDivide-16                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The division operator is not available between xs:duration and xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationDivide16() {
    final XQuery query = new XQuery(
      "xs:duration(\"P3Y3M\") div xs:dayTimeDuration(\"P3D\")",
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
   *  Test: K-DayTimeDurationDivide-2                       
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of dividing a xs:dayTimeDuration with xs:double('-INF'). 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationDivide2() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3D\") div xs:double(\"-INF\") eq xs:dayTimeDuration(\"PT0S\")",
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
   *  Test: K-DayTimeDurationDivide-3                       
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of dividing a xs:dayTimeDuration with xs:double('INF'). 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationDivide3() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3D\") div xs:double(\"INF\") eq xs:dayTimeDuration(\"PT0S\")",
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
   *  Test: K-DayTimeDurationDivide-4                       
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of dividing a xs:dayTimeDuration with 0. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationDivide4() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT4H3M3.100S\") div 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0002")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationDivide-5                       
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of dividing a xs:dayTimeDuration with NaN. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationDivide5() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT4H3M3.100S\") div xs:double(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0005")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationDivide-6                       
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of dividing a xs:dayTimeDuration with xs:double('-0'). 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationDivide6() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3D\") div xs:double(\"-0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0002")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationDivide-7                       
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The division operator is not available between xs:duration and xs:integer. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationDivide7() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y3M\") div 3",
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
   *  Test: K-DayTimeDurationDivide-8                       
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The division operator is not available between xs:integer and xs:duration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationDivide8() {
    final XQuery query = new XQuery(
      "3 div xs:duration(\"P1Y3M\")",
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
   *  Test: K-DayTimeDurationDivide-9                       
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The division operator is not available between xs:integer and xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationDivide9() {
    final XQuery query = new XQuery(
      "3 div xs:yearMonthDuration(\"P1Y3M\")",
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
   *  Test behaviour of division operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclDiv001() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then xs:duration(\"P1Y\") else xs:yearMonthDuration(\"P1Y\") }; local:f(false()) div xs:yearMonthDuration(\"P1M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "12")
    );
  }

  /**
   *  Test behaviour of division operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclDiv002() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then xs:duration(\"P1Y\") else xs:yearMonthDuration(\"P1Y\") }; local:f(true()) div xs:yearMonthDuration(\"P1M\")",
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
   *  Test behaviour of division operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclDiv003() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") }; local:f(false()) div xs:dayTimeDuration(\"PT1H\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "24")
    );
  }

  /**
   *  Test behaviour of division operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclDiv004() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") }; local:f(true()) div xs:dayTimeDuration(\"PT1H\")",
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
   *  Test behaviour of division operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclDiv005() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:yearMonthDuration(\"P1M\") }; xs:yearMonthDuration(\"P1Y\") div local:f(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "12")
    );
  }

  /**
   *  Test behaviour of division operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclDiv006() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:yearMonthDuration(\"P1M\") }; xs:yearMonthDuration(\"P1Y\") div local:f(true())",
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
   *  Test behaviour of division operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclDiv007() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:dayTimeDuration(\"PT1H\") }; xs:dayTimeDuration(\"P1D\") div local:f(false())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "24")
    );
  }

  /**
   *  Test behaviour of division operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclDiv008() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:dayTimeDuration(\"PT1H\") }; xs:dayTimeDuration(\"P1D\") div local:f(true())",
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
   *  Test behaviour of division operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclDiv009() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:yearMonthDuration(\"P1M\") }; local:f(false()) div local:f(false())",
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
   *  Test behaviour of division operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclDiv010() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:yearMonthDuration(\"P1M\") }; local:f(true()) div local:f(false())",
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
   *  Test behaviour of division operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclDiv011() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:dayTimeDuration(\"PT1H\") }; local:f(false()) div local:f(false())",
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
   *  Test behaviour of division operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclDiv012() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:dayTimeDuration(\"PT1H\") }; local:f(false()) div local:f(true())",
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
   *  Test behaviour of division operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclDiv013() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then xs:duration(\"P2M\") else xs:yearMonthDuration(\"P2M\") }; local:f(false()) div 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1M")
    );
  }

  /**
   *  Test behaviour of division operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclDiv014() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then xs:duration(\"P2M\") else xs:yearMonthDuration(\"P2M\") }; local:f(true()) div 2",
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
   *  Test behaviour of division operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclDiv015() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then xs:duration(\"P1D\") else xs:dayTimeDuration(\"P1D\") }; local:f(false()) div 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT12H")
    );
  }

  /**
   *  Test behaviour of division operator when presented with an expression with static type duration .
   */
  @org.junit.Test
  public void cbclDiv016() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:dayTimeDuration(\"P1D\") }; local:f(true()) div 2",
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
   *  test division of xs:dayTimeDuration by 0 .
   */
  @org.junit.Test
  public void cbclDivideDayTimeDuration001() {
    final XQuery query = new XQuery(
      "declare function local:dayTimeDuration($days as xs:integer) as xs:dayTimeDuration { xs:dayTimeDuration(concat(\"P\", $days, \"D\")) }; local:dayTimeDuration(2) div 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0002")
    );
  }

  /**
   *  test division of xs:dayTimeDuration by 1 .
   */
  @org.junit.Test
  public void cbclDivideDayTimeDuration002() {
    final XQuery query = new XQuery(
      "declare function local:dayTimeDuration($days as xs:integer) as xs:dayTimeDuration { xs:dayTimeDuration(concat(\"P\", $days, \"D\")) }; local:dayTimeDuration(2) div 1",
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
   *  test possible overflow in divison of xs:dayTimeDuration by .
   */
  @org.junit.Test
  public void cbclDivideDayTimeDuration003() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P9223372036854775807D\") div 0.5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0002")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-dayTimeDuration-10                     
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration" function used  
   * together with an "or" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration10() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P05DT09H02M\") div 2.0)) or fn:string((xs:dayTimeDuration(\"P05DT05H03M\") div 2.0))",
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
   * Test: op-divide-dayTimeDuration-11                     
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration" function used  
   * together with multiple "div" expressions.              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration11() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P42DT10H10M\") div 2.0) div (xs:dayTimeDuration(\"P42DT10H10M\") div 2.0)",
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
   * Test: op-divide-dayTimeDuration-12                     
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration" operators used 
   * with a boolean expression and the "fn:true" function.   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration12() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P10DT08H11M\") div 2.0)) and (fn:true())",
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
   * Test: op-divide-dayTimeDuration-13                     
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration" function used  
   * together with the numeric-equal-operator "eq".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration13() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P23DT11H11M\") div 2.0) eq xs:dayTimeDuration(\"P23DT11H11M\")",
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
   * Test: op-divide-dayTimeDuration-14                     
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration" function used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration14() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P21DT08H12M\") div 2.0) ne xs:dayTimeDuration(\"P08DT08H05M\")",
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
   * Test: op-divide-dayTimeDuration-15                     
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration" function used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration15() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT10H01M\") div 2.0) le xs:dayTimeDuration(\"P17DT10H02M\")",
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
   * Test: op-divide-dayTimeDuration-16                     
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration" function used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration16() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P13DT09H09M\") div 2.0) ge xs:dayTimeDuration(\"P18DT02H02M\")",
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
   * Test: op-divide-dayTimeDuration-2                      
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration" function 
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration2() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P10DT10H11M\")) div 2.0) and fn:false()",
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
   * Test: op-divide-dayTimeDuration-3                      
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration" function as 
   * part of a boolean expression (or operator) and the "fn:boolean" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration3() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P20DT20H10M\") div 2.0)) or fn:false()",
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
   * Test: op-divide-dayTimeDuration-4                      
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration" function that  
   * return true and used together with fn:not.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:dayTimeDuration(\"P11DT12H04M\") div 2.0))",
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
   * Test: op-divide-dayTimeDuration-5                      
   * Written By: Carmelo Montanez                           
   * Date: June 29 2005                                     
   * Purpose: Evaluates The "divide-dayTimeDuration" function that  
   * is used as an argument to the fn:boolean function.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:dayTimeDuration(\"P05DT09H08M\") div 2.0))",
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
   * Test: op-divide-dayTimeDuration-6                      
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration6() {
    final XQuery query = new XQuery(
      "fn:number(xs:dayTimeDuration(\"P02DT06H09M\") div 2.0)",
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
   * Test: op-divide-dayTimeDuration-7                      
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration" function used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration7() {
    final XQuery query = new XQuery(
      "fn:string(xs:dayTimeDuration(\"P03DT04H08M\") div 2.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1DT14H4M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-dayTimeDuration-8                      
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration" function that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration8() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT01H01M\") div -2.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-P5DT30M30S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-dayTimeDuration-9                      
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration" function used 
   * together with and "and" expression.                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration9() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P01DT02H01M\") div 2.0)) and fn:string((xs:dayTimeDuration(\"P02DT03H03M\") div 2.0 ))",
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
   * Test: op-divide-dayTimeDuration2args-1                  
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-dayTimeDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration2args1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") div xs:double(\"-1.7976931348623157E308\")",
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
   * Test: op-divide-dayTimeDuration2args-2                  
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-dayTimeDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(mid range)                 
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration2args2() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P15DT11H59M59S\") div xs:double(\"-1.7976931348623157E308\")",
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
   * Test: op-divide-dayTimeDuration2args-3                  
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-dayTimeDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(upper bound)               
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration2args3() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P31DT23H59M59S\") div xs:double(\"-1.7976931348623157E308\")",
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
   * Test: op-divide-dayTimeDuration2args-4                  
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-dayTimeDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:double(mid range)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration2args4() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") div xs:double(\"0.1\")",
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
   * Test: op-divide-dayTimeDuration2args-5                  
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-dayTimeDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:double(upper bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDuration2args5() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") div xs:double(\"1.7976931348623157E308\")",
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
}
