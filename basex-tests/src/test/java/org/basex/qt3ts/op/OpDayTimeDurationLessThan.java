package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the dayTimeDuration-less-than() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpDayTimeDurationLessThan extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationLT-1                           
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'lt' for xs:dayTimeDuration.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationLT1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT08H34M12.142S\") lt xs:dayTimeDuration(\"P3DT08H34M12.143S\")",
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
   *  Test: K-DayTimeDurationLT-2                           
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'lt' for xs:dayTimeDuration, evaluating to false. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationLT2() {
    final XQuery query = new XQuery(
      "not(xs:dayTimeDuration(\"P3DT08H34M12.143S\") lt xs:dayTimeDuration(\"P3DT08H34M12.143S\"))",
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
   *  Test: K-DayTimeDurationLT-3                           
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'lt' for xs:dayTimeDuration, evaluating to false. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationLT3() {
    final XQuery query = new XQuery(
      "not(xs:dayTimeDuration(\"P3DT08H34M12.144S\") lt xs:dayTimeDuration(\"P3DT08H34M12.143S\"))",
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
   *  Test: K-DayTimeDurationLT-4                           
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'le' for xs:dayTimeDuration.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationLT4() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT08H34M12.143S\") le xs:dayTimeDuration(\"P3DT08H34M12.143S\")",
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
   *  Test: K-DayTimeDurationLT-5                           
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'le' for xs:dayTimeDuration.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationLT5() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT08H34M12.143S\") le xs:dayTimeDuration(\"P3DT08H34M12.143S\")",
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
   *  Test: K-DayTimeDurationLT-6                           
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'le' for xs:dayTimeDuration, evaluating to false. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationLT6() {
    final XQuery query = new XQuery(
      "not(xs:dayTimeDuration(\"P3DT08H34M12.143S\") le xs:dayTimeDuration(\"P3DT08H34M12.142S\"))",
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
   *  test comparison of dayTimeDurations .
   */
  @org.junit.Test
  public void cbclDayTimeDurationLessThan001() {
    final XQuery query = new XQuery(
      "declare function local:dayTimeDuration($days as xs:integer, $hours as xs:integer ) { xs:dayTimeDuration(concat('P', $days, 'DT', $hours, 'H')) }; not(local:dayTimeDuration(1, 1) lt xs:dayTimeDuration(\"P0D\"))",
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
   *  test comparison of dayTimeDurations .
   */
  @org.junit.Test
  public void cbclDayTimeDurationLessThan002() {
    final XQuery query = new XQuery(
      "declare function local:dayTimeDuration($days as xs:integer, $hours as xs:integer ) { xs:dayTimeDuration(concat('P', $days, 'DT', $hours, 'H')) }; not(local:dayTimeDuration(1, 1) le xs:dayTimeDuration(\"P0D\"))",
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
   *  test comparison of xs:dayTimeDuration .
   */
  @org.junit.Test
  public void cbclDayTimeDurationLessThan003() {
    final XQuery query = new XQuery(
      "declare function local:dayTimeDuration($dayTimeDuration as xs:dayTimeDuration, $null as xs:boolean) { if ($null) then () else $dayTimeDuration }; exists(local:dayTimeDuration(xs:dayTimeDuration(\"P0D\"), fn:true()) lt xs:dayTimeDuration(\"P0D\"))",
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
   *  test comparison of xs:dayTimeDuration .
   */
  @org.junit.Test
  public void cbclDayTimeDurationLessThan004() {
    final XQuery query = new XQuery(
      "declare function local:dayTimeDuration($dayTimeDuration as xs:dayTimeDuration, $null as xs:boolean) { if ($null) then () else $dayTimeDuration }; local:dayTimeDuration(xs:dayTimeDuration(\"P0D\"), fn:false()) lt xs:dayTimeDuration(\"P0D\")",
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
   *  test comparison of xs:dayTimeDuration .
   */
  @org.junit.Test
  public void cbclDayTimeDurationLessThan005() {
    final XQuery query = new XQuery(
      "declare function local:dayTimeDuration($dayTimeDuration as xs:dayTimeDuration, $null as xs:boolean) { if ($null) then () else $dayTimeDuration }; exists(local:dayTimeDuration(xs:dayTimeDuration(\"P0D\"), fn:true()) le xs:dayTimeDuration(\"P0D\"))",
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
   *  test comparison of xs:dayTimeDuration .
   */
  @org.junit.Test
  public void cbclDayTimeDurationLessThan006() {
    final XQuery query = new XQuery(
      "declare function local:dayTimeDuration($dayTimeDuration as xs:dayTimeDuration, $null as xs:boolean) { if ($null) then () else $dayTimeDuration }; local:dayTimeDuration(xs:dayTimeDuration(\"P0D\"), fn:false()) le xs:dayTimeDuration(\"P0D\")",
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
   *  Test behaviour of the overloaded less equal value comparison operator when presented with an expression with static type xs:duration. .
   */
  @org.junit.Test
  public void cbclValueLessEqual001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        local:f(false()) le xs:yearMonthDuration(\"P1Y\")",
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
   *  Test behaviour of the overloaded less equal value comparison operator when presented with an expression with static type xs:duration. .
   */
  @org.junit.Test
  public void cbclValueLessEqual002() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        local:f(true()) le xs:yearMonthDuration(\"P1Y\")",
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
   *  Test behaviour of the overloaded less equal value comparison operator when presented with an expression with static type xs:duration. .
   */
  @org.junit.Test
  public void cbclValueLessEqual003() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:dayTimeDuration(\"PT1H\") };\n" +
      "        local:f(false()) le xs:dayTimeDuration(\"P1D\")",
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
   *  Test behaviour of the overloaded less equal value comparison operator when presented with an expression with static type xs:duration. .
   */
  @org.junit.Test
  public void cbclValueLessEqual004() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:dayTimeDuration(\"PT1H\") };\n" +
      "        local:f(true()) le xs:dayTimeDuration(\"P1D\")",
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
   *  Test behaviour of the overloaded less equal value comparison operator when presented with an expression with static type xs:duration. .
   */
  @org.junit.Test
  public void cbclValueLessEqual005() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        xs:yearMonthDuration(\"P1Y\") le local:f(false())",
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
   *  Test behaviour of the overloaded less equal value comparison operator when presented with an expression with static type xs:duration. .
   */
  @org.junit.Test
  public void cbclValueLessEqual006() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        xs:yearMonthDuration(\"P1Y\") le local:f(true())",
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
   *  Test behaviour of the overloaded less equal value comparison operator when presented with an expression with static type xs:duration. .
   */
  @org.junit.Test
  public void cbclValueLessEqual007() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:dayTimeDuration(\"PT1H\") };\n" +
      "        xs:dayTimeDuration(\"P1D\") le local:f(false())",
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
   *  Test behaviour of the overloaded less equal value comparison operator when presented with an expression with static type xs:duration. .
   */
  @org.junit.Test
  public void cbclValueLessEqual008() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:dayTimeDuration(\"PT1H\") };\n" +
      "        xs:dayTimeDuration(\"P1D\") le local:f(true())",
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
   *  Test behaviour of the overloaded less equal value comparison operator when presented with an expression with static type xs:duration. .
   */
  @org.junit.Test
  public void cbclValueLessEqual009() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        local:f(false()) le local:f(false())",
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
   *  Test behaviour of the overloaded less equal value comparison operator when presented with an expression with static type xs:duration. .
   */
  @org.junit.Test
  public void cbclValueLessEqual010() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:yearMonthDuration(\"P1M\") };\n" +
      "        local:f(true()) le local:f(false())",
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
   *  Test behaviour of the overloaded less equal value comparison operator when presented with an expression with static type xs:duration. .
   */
  @org.junit.Test
  public void cbclValueLessEqual011() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:dayTimeDuration(\"PT1H\") };\n" +
      "        local:f(false()) le local:f(false())",
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
   *  Test behaviour of the overloaded less equal value comparison operator when presented with an expression with static type xs:duration. .
   */
  @org.junit.Test
  public void cbclValueLessEqual012() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x) then xs:duration(\"P1M\") else xs:dayTimeDuration(\"PT1H\") };\n" +
      "        local:f(false()) le local:f(true())",
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
   * Test: op-dayTimeDuration-less-than-10                  
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-less-than" function used  
   * together with "or" expression (le operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan10() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT01H\") le xs:dayTimeDuration(\"P09DT06H\")) or (xs:dayTimeDuration(\"P15DT01H\") le xs:dayTimeDuration(\"P02DT04H\"))",
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
   * Test: op-dayTimeDuration-less-than-11                  
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-less-than" function used  
   * together with "fn:true"/or expression (lt operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan11() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT02H\") lt xs:dayTimeDuration(\"P01DT10H\")) or (fn:true())",
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
   * Test: op-dayTimeDuration-less-than-12                  
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-less-than" function used  
   * together with "fn:true"/or expression (le operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan12() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT01H\") le xs:dayTimeDuration(\"P09DT05H\")) or (fn:true())",
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
   * Test: op-dayTimeDuration-less-than-13                  
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-less-than" function used  
   * together with "fn:false"/or expression (lt operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan13() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P30DT10H\") lt xs:dayTimeDuration(\"P01DT02H\")) or (fn:false())",
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
   * Test: op-dayTimeDuration-less-than-14                  
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-less-than" function used  
   * together with "fn:false"/or expression (le operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan14() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT05H\") le xs:dayTimeDuration(\"P20DT10H\")) or (fn:false())",
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
   * Test: op-dayTimeDuration-less-than-3                   
   * Written By: Carmelo Montanez                           
   * Date: June 17, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-less-than" function that  
   * return true and used together with fn:not (lt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan3() {
    final XQuery query = new XQuery(
      "fn:not((xs:dayTimeDuration(\"P13DT12H\") lt xs:dayTimeDuration(\"P14DT11H\")))",
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
   * Test: op-dayTimeDuration-less-than-4                   
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-less-than" function that  
   * return true and used together with fn:not (le operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan4() {
    final XQuery query = new XQuery(
      "fn:not(xs:dayTimeDuration(\"P10DT110H\") le xs:dayTimeDuration(\"P10DT11H\"))",
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
   * Test: op-dayTimeDuration-less-than-5                   
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-less-than" function that  
   * return false and used together with fn:not (lt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan5() {
    final XQuery query = new XQuery(
      "fn:not(xs:dayTimeDuration(\"P10DT10H\") lt xs:dayTimeDuration(\"P9DT09H\"))",
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
   * Test: op-dayTimeDuration-less-than-6                   
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-less-than" function that  
   * return false and used together with fn:not(le operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan6() {
    final XQuery query = new XQuery(
      "fn:not(xs:dayTimeDuration(\"P10DT09H\") le xs:dayTimeDuration(\"P09DT09H\"))",
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
   * Test: op-dayTimeDuration-less-than-7                   
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-less-than" function used  
   * together with "and" expression (lt operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan7() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT09H\") lt xs:dayTimeDuration(\"P09DT10H\")) and (xs:dayTimeDuration(\"P10DT01H\") lt xs:dayTimeDuration(\"P08DT06H\"))",
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
   * Test: op-dayTimeDuration-less-than-8                   
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-less-than" function used  
   * together with "and" expression (le operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan8() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT09H\") le xs:dayTimeDuration(\"P10DT01H\")) and (xs:dayTimeDuration(\"P02DT04H\") le xs:dayTimeDuration(\"P09DT07H\"))",
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
   * Test: op-dayTimeDuration-less-than-9                   
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-less-than" function used
   * together with "or" expression (lt operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan9() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT08H\") lt xs:dayTimeDuration(\"P10DT07H\")) or (xs:dayTimeDuration(\"P10DT09H\") lt xs:dayTimeDuration(\"P10DT09H\"))",
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
   * Test: op-dayTimeDuration-less-than2args-1               
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan2args1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") lt xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-dayTimeDuration-less-than2args-10              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(upper bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan2args10() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") ge xs:dayTimeDuration(\"P31DT23H59M59S\")",
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
   * Test: op-dayTimeDuration-less-than2args-2               
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(mid range)                 
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan2args2() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P15DT11H59M59S\") lt xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-dayTimeDuration-less-than2args-3               
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(upper bound)               
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan2args3() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P31DT23H59M59S\") lt xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-dayTimeDuration-less-than2args-4               
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(mid range)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan2args4() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") lt xs:dayTimeDuration(\"P15DT11H59M59S\")",
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
   * Test: op-dayTimeDuration-less-than2args-5               
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(upper bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan2args5() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") lt xs:dayTimeDuration(\"P31DT23H59M59S\")",
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
   * Test: op-dayTimeDuration-less-than2args-6               
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan2args6() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") ge xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-dayTimeDuration-less-than2args-7               
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(mid range)                 
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan2args7() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P15DT11H59M59S\") ge xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-dayTimeDuration-less-than2args-8               
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(upper bound)               
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan2args8() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P31DT23H59M59S\") ge xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-dayTimeDuration-less-than2args-9               
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(mid range)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationLessThan2args9() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") ge xs:dayTimeDuration(\"P15DT11H59M59S\")",
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
}
