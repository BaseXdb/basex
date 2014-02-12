package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the subtract-dayTimeDuration-from-time() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpSubtractDayTimeDurationFromTime extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-TimeSubtractDTD-1                             
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple testing involving operator '-' between xs:time and xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeSubtractDTD1() {
    final XQuery query = new XQuery(
      "xs:time(\"08:12:32\") - xs:dayTimeDuration(\"P23DT09H32M59S\") eq xs:time(\"22:39:33\")",
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
   *  Test: K-TimeSubtractDTD-2                             
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The '+' operator is not available between xs:yearMonthDuration and xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeSubtractDTD2() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y\") + xs:time(\"08:01:23\")",
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
   *  Test: K-TimeSubtractDTD-3                             
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The '+' operator is not available between xs:time and xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeSubtractDTD3() {
    final XQuery query = new XQuery(
      "xs:time(\"08:01:23\") + xs:yearMonthDuration(\"P1Y\")",
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
   *  Test: K-TimeSubtractDTD-4                             
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The '-' operator is not available between xs:yearMonthDuration and xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeSubtractDTD4() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y\") - xs:time(\"08:01:23\")",
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
   *  Test: K-TimeSubtractDTD-5                             
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The '-' operator is not available between xs:time and xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeSubtractDTD5() {
    final XQuery query = new XQuery(
      "xs:time(\"08:01:23\") - xs:yearMonthDuration(\"P1Y\")",
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
   *  test subtraction of zero duration from time .
   */
  @org.junit.Test
  public void cbclSubtractDayTimeDurationFromTime001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string };\n" +
      "        declare function local:time($hour as xs:integer, $mins as xs:integer) { let $h := local:two-digit($hour), $m := local:two-digit($mins) return xs:time(concat($h, ':', $m, ':00')) };\n" +
      "        local:time(12, 59) - xs:dayTimeDuration(\"P0D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "12:59:00")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDuration-from-time-1          
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "subtract-dayTimeDuration-from-time" operator 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime1() {
    final XQuery query = new XQuery(
      "xs:time(\"11:12:00\") - xs:dayTimeDuration(\"P3DT1H15M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "09:57:00")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDuration-from-time-10         
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of "subtract-dayTimeDuration-from-time" operator used  
   * together with an "or" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime10() {
    final XQuery query = new XQuery(
      "fn:string((xs:time(\"23:45:50Z\") - xs:dayTimeDuration(\"P03DT01H04M\"))) or fn:string((xs:time(\"23:45:50Z\") + xs:dayTimeDuration(\"P01DT01H03M\")))",
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
   * Test: op-subtract-dayTimeDuration-from-time-12         
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of "subtract-dayTimeDuration-from-time" operator used 
   * with a boolean expression and the "fn:true" function.   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime12() {
    final XQuery query = new XQuery(
      "fn:string((xs:time(\"02:02:02Z\") - xs:dayTimeDuration(\"P05DT08H11M\"))) and (fn:true())",
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
   * Test: op-subtract-dayTimeDuration-from-time-13         
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "subtract-dayTimeDuration-from-time" operator used  
   * together with the numeric-equal-operator "eq".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime13() {
    final XQuery query = new XQuery(
      "(xs:time(\"01:03:03Z\") - xs:dayTimeDuration(\"P23DT11H11M\")) eq xs:time(\"04:03:05Z\")",
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
   * Test: op-subtract-dayTimeDuration-from-time-14         
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "subtract-dayTimeDuration-from-time" operator used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime14() {
    final XQuery query = new XQuery(
      "(xs:time(\"04:04:05Z\") - xs:dayTimeDuration(\"P08DT08H05M\")) ne xs:time(\"05:08:02Z\")",
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
   * Test: op-subtract-dayTimeDuration-from-time-15         
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of "subtract-dayTimeDuration-from-time" operator used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime15() {
    final XQuery query = new XQuery(
      "fn:string((xs:time(\"08:09:09Z\") - xs:dayTimeDuration(\"P17DT10H02M\"))) le fn:string(xs:time(\"09:08:10Z\"))",
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
   * Test: op-subtract-dayTimeDuration-from-time-16         
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of the "subtract-dayTimeDuration-from-time" operator used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime16() {
    final XQuery query = new XQuery(
      "fn:string((xs:time(\"09:06:07Z\") - xs:dayTimeDuration(\"P18DT02H02M\"))) ge fn:string(xs:time(\"01:01:01Z\"))",
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
   * Test: op-subtract-dayTimeDuration-from-time-17         
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "subtract-dayTimeDuration-from-time" operator 
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   *  Should raise a type error.                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime17() {
    final XQuery query = new XQuery(
      "fn:string(xs:time(\"12:07:08Z\") - xs:dayTimeDuration(\"P12DT10H07M\")) and fn:false()",
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
   * Test: op-subtract-dayTimeDuration-from-time-2          
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "subtract-dayTimeDuration-from-time" operator 
   * as per example 2 (for this function) from the F&O specs. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime2() {
    final XQuery query = new XQuery(
      "xs:time(\"08:20:00-05:00\") - xs:dayTimeDuration(\"P23DT10H10M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "22:10:00-05:00")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDuration-from-time-3          
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value "subtract-dayTimeDuration-from-time" operator as 
   * part of a boolean expression (or operator) and the "fn:boolean" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime3() {
    final XQuery query = new XQuery(
      "fn:string((xs:time(\"12:12:01Z\") - xs:dayTimeDuration(\"P19DT13H10M\"))) or fn:false()",
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
   * Test: op-subtract-dayTimeDuration-from-time-4          
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of "subtract-dayTimeDuration-from-time" operator that  
   * return true and used together with fn:not.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:time(\"20:50:50Z\") - xs:dayTimeDuration(\"P02DT07H01M\")))",
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
   * Test: op-subtract-dayTimeDuration-from-time-5          
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of "subtract-dayTimeDuration-from-time" operator that  
   * is used as an argument to the fn:boolean function.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:time(\"23:55:55Z\") - xs:dayTimeDuration(\"P03DT08H06M\")))",
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
   * Test: op-subtract-dayTimeDuration-from-time-6          
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "subtract-dayTimeDuration-from-time" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime6() {
    final XQuery query = new XQuery(
      "fn:number(xs:time(\"10:11:45Z\") - xs:dayTimeDuration(\"P10DT08H01M\"))",
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
   * Test: op-subtract-dayTimeDuration-from-time-7          
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "subtract-dayTimeDuration-from-time" operator used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime7() {
    final XQuery query = new XQuery(
      "fn:string(xs:time(\"19:45:55Z\") - xs:dayTimeDuration(\"P01DT09H02M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10:43:55Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDuration-from-time-8          
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "subtract-dayTimeDuration-from-time" operator that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime8() {
    final XQuery query = new XQuery(
      "(xs:time(\"01:01:01\") - xs:dayTimeDuration(\"-P11DT02H02M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "03:03:01")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDuration-from-time-9          
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                    
   * Purpose: Evaluates The string value "subtract-dayTimeDuration-from-time" subtract used 
   * together with and "and" expression.                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime9() {
    final XQuery query = new XQuery(
      "fn:string((xs:time(\"10:10:10Z\") - xs:dayTimeDuration(\"P02DT09H02M\"))) and fn:string((xs:time(\"09:02:02Z\") - xs:dayTimeDuration(\"P04DT04H04M\")))",
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
   * Test: op-subtract-dayTimeDuration-from-time2args-1      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-dayTimeDuration-from-time" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime2args1() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") - xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "00:00:00Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDuration-from-time2args-2      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-dayTimeDuration-from-time" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(mid range)                             
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime2args2() {
    final XQuery query = new XQuery(
      "xs:time(\"08:03:35Z\") - xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "08:03:35Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDuration-from-time2args-3      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-dayTimeDuration-from-time" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(upper bound)                           
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime2args3() {
    final XQuery query = new XQuery(
      "xs:time(\"23:59:59Z\") - xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "23:59:59Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDuration-from-time2args-4      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-dayTimeDuration-from-time" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:dayTimeDuration(mid range)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime2args4() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") - xs:dayTimeDuration(\"P15DT11H59M59S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "12:00:01Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDuration-from-time2args-5      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-dayTimeDuration-from-time" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:dayTimeDuration(upper bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromTime2args5() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") - xs:dayTimeDuration(\"P31DT23H59M59S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "00:00:01Z")
    );
  }
}
