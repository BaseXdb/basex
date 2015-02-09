package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the add-dayTimeDuration-to-time() function.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpAddDayTimeDurationToTime extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-TimeAddDTD-1                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple testing involving operator '+' between xs:time and xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeAddDTD1() {
    final XQuery query = new XQuery(
      "xs:time(\"08:12:32\") + xs:dayTimeDuration(\"P23DT09H32M59S\") eq xs:time(\"17:45:31\")",
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
   *  Test: K-TimeAddDTD-2                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple testing involving operator '+' between xs:dayTimeDuration and xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeAddDTD2() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P23DT09H32M59S\") + xs:time(\"08:12:32\") eq xs:time(\"17:45:31\")",
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
   *  Test: K2-TimeAddDTD-1                                 
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: No '+' operator is available between xs:time and xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void k2TimeAddDTD1() {
    final XQuery query = new XQuery(
      "xs:time(\"10:10:10\") + xs:time(\"23:10:10\")",
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
   *  test addition of zero duration to time .
   */
  @org.junit.Test
  public void cbclAddDayTimeDurationToTime001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string };\n" +
      "        declare function local:time($hour as xs:integer, $mins as xs:integer) { let $h := local:two-digit($hour), $m := local:two-digit($mins) return xs:time(concat( $h, ':', $m, ':00')) };\n" +
      "        local:time(12, 59) + xs:dayTimeDuration(\"P0D\")\n" +
      "      ",
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
   *  test addition of zero duration to time .
   */
  @org.junit.Test
  public void cbclAddDayTimeDurationToTime002() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string };\n" +
      "        declare function local:time($hour as xs:integer, $mins as xs:integer) { let $h := local:two-digit($hour), $m := local:two-digit($mins) return xs:time(concat( $h, ':', $m, ':00')) };\n" +
      "        xs:dayTimeDuration(\"P0D\") + local:time(12, 59)\n" +
      "      ",
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
   * Test: op-add-dayTimeDuration-to-time-1                 
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-time" function 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime1() {
    final XQuery query = new XQuery(
      "xs:time(\"11:12:00\") + xs:dayTimeDuration(\"P3DT1H15M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "12:27:00")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDuration-to-time-10                
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-time" function used  
   * together with an "or" expression.                      
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime10() {
    final XQuery query = new XQuery(
      "fn:string((xs:time(\"02:03:01Z\") + xs:dayTimeDuration(\"P03DT01H04M\"))) or fn:string((xs:time(\"02:03:01Z\") + xs:dayTimeDuration(\"P01DT01H03M\")))",
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
   * Test: op-add-dayTimeDuration-to-time-13                
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-time" function used  
   * together with the numeric-equal-operator "eq".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime13() {
    final XQuery query = new XQuery(
      "(xs:time(\"01:03:03Z\") + xs:dayTimeDuration(\"P23DT11H11M\")) eq xs:time(\"04:03:05Z\")",
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
   * Test: op-add-dayTimeDuration-to-time-14                
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-time" function used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime14() {
    final XQuery query = new XQuery(
      "(xs:time(\"04:04:05Z\") + xs:dayTimeDuration(\"P08DT08H05M\")) ne xs:time(\"05:08:02Z\")",
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
   * Test: op-add-dayTimeDuration-to-time-15                
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-time" function used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime15() {
    final XQuery query = new XQuery(
      "(xs:time(\"08:09:09Z\") + xs:dayTimeDuration(\"P17DT10H02M\")) le xs:time(\"09:08:10Z\")",
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
   * Test: op-add-dayTimeDuration-to-time-16                
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-time" function used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime16() {
    final XQuery query = new XQuery(
      "(xs:time(\"09:06:07Z\") + xs:dayTimeDuration(\"P18DT02H02M\")) ge xs:time(\"01:01:01Z\")",
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
   * Test: op-add-dayTimeDuration-to-time-17                
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-time" function 
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime17() {
    final XQuery query = new XQuery(
      "fn:string(xs:time(\"12:07:08Z\") + xs:dayTimeDuration(\"P12DT10H07M\")) and fn:false()",
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
   * Test: op-add-dayTimeDuration-to-time-2                 
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-time" function 
   * as per example 2 (for this function) from the F&O specs. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime2() {
    final XQuery query = new XQuery(
      "xs:time(\"23:12:00+03:00\") + xs:dayTimeDuration(\"P1DT3H15M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "02:27:00+03:00")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDuration-to-time-3                 
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-time" function as 
   * part of a boolean expression (or operator) and the "fn:boolean" function. 
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime3() {
    final XQuery query = new XQuery(
      "fn:string((xs:time(\"12:12:01Z\") + xs:dayTimeDuration(\"P19DT13H10M\"))) or fn:false()",
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
   * Test: op-add-dayTimeDuration-to-time-4                 
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-time" function that  
   * return true and used together with fn:not.             
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:time(\"13:12:00Z\") + xs:dayTimeDuration(\"P02DT07H01M\")))",
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
   * Test: op-add-dayTimeDuration-to-time-5                 
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-time" function that  
   * is used as an argument to the fn:boolean function.     
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:time(\"02:02:02Z\") + xs:dayTimeDuration(\"P03DT08H06M\")))",
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
   * Test: op-add-dayTimeDuration-to-time-6                 
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-time" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime6() {
    final XQuery query = new XQuery(
      "fn:number(xs:time(\"01:01:01Z\") + xs:dayTimeDuration(\"P10DT08H01M\"))",
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
   * Test: op-add-dayTimeDuration-to-time-7                 
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-time" function used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime7() {
    final XQuery query = new XQuery(
      "fn:string(xs:time(\"10:02:03Z\") + xs:dayTimeDuration(\"P01DT09H02M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "19:04:03Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDuration-to-time-8                 
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-time" function that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime8() {
    final XQuery query = new XQuery(
      "(xs:time(\"08:02:06\") + xs:dayTimeDuration(\"-P11DT02H02M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "06:00:06")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDuration-to-time-9                 
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDuration-to-time" function used 
   * together with and "and" expression.                    
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime9() {
    final XQuery query = new XQuery(
      "fn:string((xs:time(\"10:10:10Z\") + xs:dayTimeDuration(\"P02DT09H02M\"))) and fn:string((xs:time(\"09:02:02Z\") + xs:dayTimeDuration(\"P04DT04H04M\")))",
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
   * Test: op-add-dayTimeDuration-to-time2args-1             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDuration-to-time" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime2args1() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") + xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-add-dayTimeDuration-to-time2args-2             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDuration-to-time" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(mid range)                             
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime2args2() {
    final XQuery query = new XQuery(
      "xs:time(\"08:03:35Z\") + xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-add-dayTimeDuration-to-time2args-3             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDuration-to-time" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(upper bound)                           
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime2args3() {
    final XQuery query = new XQuery(
      "xs:time(\"23:59:59Z\") + xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-add-dayTimeDuration-to-time2args-4             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDuration-to-time" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:dayTimeDuration(mid range)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime2args4() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") + xs:dayTimeDuration(\"P15DT11H59M59S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "11:59:59Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDuration-to-time2args-5             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDuration-to-time" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:dayTimeDuration(upper bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTime2args5() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") + xs:dayTimeDuration(\"P31DT23H59M59S\")",
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
   * Test: op-add-dayTimeDuration-to-time-12                
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDuration-to-time" operators used 
   * with a boolean expression and the "fn:true" function.   
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToTimealt12() {
    final XQuery query = new XQuery(
      "fn:string((xs:time(\"02:02:02Z\") + xs:dayTimeDuration(\"P05DT08H11M\"))) and (fn:true())",
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
}
