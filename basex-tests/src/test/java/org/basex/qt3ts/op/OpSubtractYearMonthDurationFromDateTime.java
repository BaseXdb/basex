package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the subtract-yearMonthDuration-from-dateTime() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpSubtractYearMonthDurationFromDateTime extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DateTimeSubtractYMD-1                         
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple testing involving operator '-' between xs:dateTime and xs:yearMonthDuration that evaluates to zero. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimeSubtractYMD1() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-07-19T08:23:01.765\") - xs:yearMonthDuration(\"P3Y35M\") eq xs:dateTime(\"1993-08-19T08:23:01.765\")",
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
   *  test subtraction of zero duration to dateTime .
   */
  @org.junit.Test
  public void cbclSubtractYearMonthDurationFromDateTime001() {
    final XQuery query = new XQuery(
      "declare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string }; declare function local:dateTime($year as xs:integer, $month as xs:integer, $day as xs:integer, $hour as xs:integer, $mins as xs:integer) { let $m := local:two-digit($month), $d := local:two-digit($day), $h := local:two-digit($hour), $n := local:two-digit($mins) return xs:dateTime(concat($year, '-', $m, '-', $d, 'T', $h, ':', $n, ':00')) }; local:dateTime(2008, 05, 12, 12, 59) - xs:yearMonthDuration(\"P0Y\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2008-05-12T12:59:00")
    );
  }

  /**
   *  test subtraction of large duration from dateTime .
   */
  @org.junit.Test
  public void cbclSubtractYearMonthDurationFromDateTime002() {
    final XQuery query = new XQuery(
      "declare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string }; declare function local:dateTime($year as xs:integer, $month as xs:integer, $day as xs:integer, $hour as xs:integer, $mins as xs:integer) { let $m := local:two-digit($month), $d := local:two-digit($day), $h := local:two-digit($hour), $n := local:two-digit($mins) return xs:dateTime(concat($year, '-', $m, '-', $d, 'T', $h, ':', $n, ':00')) }; local:dateTime(-25252734927766554, 05, 12, 12, 59) + xs:yearMonthDuration(\"-P3214267297Y\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDuration-from-dateTime-1    
   * Written By: Carmelo Montanez                           
   * date: July 5, 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDuration-from-dateTime" operator 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime1() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2000-10-30T11:12:00\") - xs:yearMonthDuration(\"P1Y2M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1999-08-30T11:12:00")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDuration-from-dateTime-10   
   * Written By: Carmelo Montanez                           
   * date: July 5, 2005                                     
   * Purpose: Evaluates The string value of "subtract-yearMonthDuration-from-dateTime" 
   *  operator used together with an "or" expression.       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime10() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1985-07-05T09:09:09Z\") - xs:yearMonthDuration(\"P02Y02M\"))) or fn:string((xs:dateTime(\"1985-07-05T09:09:09Z\") - xs:yearMonthDuration(\"P02Y02M\")))",
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
   * Test: op-subtract-yearMonthDuration-from-dateTime-12   
   * Written By: Carmelo Montanez                           
   * date: July 5, 2005                                     
   * Purpose: Evaluates The string value of "subtract-yearMonthDuration-from-dateTime" 
   * operator used with a boolean expression and the "fn:true" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime12() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1980-03-02T02:02:02Z\") - xs:yearMonthDuration(\"P05Y05M\"))) and (fn:true())",
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
   * Test: op-subtract-yearMonthDuration-from-dateTime-13   
   * Written By: Carmelo Montanez                           
   * date: July 5, 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDuration-from-dateTime" operator used  
   * together with the numeric-equal-operator "eq".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime13() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1980-05-05T05:05:05Z\") - xs:yearMonthDuration(\"P23Y11M\")) eq xs:dateTime(\"1980-05-05T05:05:05Z\")",
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
   * Test: op-subtract-yearMonthDuration-from-dateTime-14   
   * Written By: Carmelo Montanez                           
   * date: July 5, 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDuration-from-dateTime" operator used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime14() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1979-12-12T09:09:09Z\") - xs:yearMonthDuration(\"P08Y08M\")) ne xs:dateTime(\"1979-12-12T09:09:09Z\")",
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
   * Test: op-subtract-yearMonthDuration-from-dateTime-15   
   * Written By: Carmelo Montanez                           
   * date: July 5, 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDuration-from-dateTime" operator used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime15() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1978-12-12T07:07:07Z\") - xs:yearMonthDuration(\"P17Y12M\")) le xs:dateTime(\"1978-12-12T07:07:07Z\")",
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
   * Test: op-subtract-yearMonthDuration-from-dateTime-16   
   * Written By: Carmelo Montanez                           
   * Date: July 5, 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDuration-from-dateTime" operator used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime16() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1977-12-12T03:03:03Z\") - xs:yearMonthDuration(\"P18Y02M\")) ge xs:dateTime(\"1977-12-12T03:03:03Z\")",
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
   * Test: op-subtract-yearMonthDuration-from-dateTime-2    
   * Written By: Carmelo Montanez                           
   * date: July 5, 2005                                     
   * Purpose: Evaluates The string value of "subtract-yearMonthDuration-from-dateTime" 
   *  operator used as part of a boolean expression (and operator) and the "fn:false" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime2() {
    final XQuery query = new XQuery(
      "fn:string(xs:dateTime(\"2000-12-12T12:12:12Z\") - xs:dayTimeDuration(\"P12DT10H07M\")) and fn:false()",
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
   * Test: op-subtract-yearMonthDuration-from-dateTime-3    
   * date: July 5, 2005                                     
   * Purpose: Evaluates The string value of "subtract-yearMonthDuration-from-dateTime" 
   * operator as part of a boolean expression (or operator) and the "fn:boolean" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime3() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1999-10-23T13:45:45Z\") - xs:yearMonthDuration(\"P19Y12M\"))) or fn:false()",
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
   * Test: op-subtract-yearMonthDuration-from-dateTime-4    
   * Written By: Carmelo Montanez                           
   * date: July 5, 2005                                     
   * Purpose: Evaluates The string value of "subtract-yearMonthDuration-from-dateTime" 
   *  operator that return true and used together with fn:not.
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:dateTime(\"1998-09-12T13:56:12Z\") - xs:yearMonthDuration(\"P20Y03M\")))",
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
   * Test: op-subtract-yearMonthDuration-from-dateTime-5    
   * Written By: Carmelo Montanez                           
   * date: July 5, 2005                                     
   * Purpose: Evaluates The string value of "subtract-yearMonthDuration-from-dateTime" 
   *  operator that is used as an argument to the fn:boolean function.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:dateTime(\"1962-03-12T10:12:34Z\") - xs:yearMonthDuration(\"P10Y01M\")))",
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
   * Test: op-subtract-yearMonthDuration-from-dateTime-6    
   * Written By: Carmelo Montanez                           
   * date: July 5, 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDuration-from-dateTime" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime6() {
    final XQuery query = new XQuery(
      "fn:number(xs:dateTime(\"1988-01-28T13:45:23Z\") - xs:yearMonthDuration(\"P09Y02M\"))",
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
   * Test: op-subtract-yearMonthDuration-from-dateTime-7    
   * Written By: Carmelo Montanez                           
   * date: July 5, 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDuration-from-dateTime" operator used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime7() {
    final XQuery query = new XQuery(
      "fn:string(xs:dateTime(\"1989-07-05T14:34:36Z\") - xs:yearMonthDuration(\"P08Y04M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1981-03-05T14:34:36Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDuration-from-dateTime-8    
   * Written By: Carmelo Montanez                           
   * date: July 8, 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDuration-from-dateTime" operator that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime8() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"0001-01-01T01:01:01Z\") - xs:yearMonthDuration(\"-P20Y07M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0021-08-01T01:01:01Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDuration-from-dateTime-9    
   * Written By: Carmelo Montanez                           
   * date: July 5, 2005                                     
   * Purpose: Evaluates string value of the "subtract-yearMonthDuration-from-dateTime" 
   * operator used together with an "and" expression.       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime9() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1993-12-09T10:10:10Z\") - xs:yearMonthDuration(\"P03Y03M\"))) and fn:string((xs:dateTime(\"1993-12-09T10:10:10Z\") - xs:yearMonthDuration(\"P03Y03M\")))",
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
   * Test: op-subtract-yearMonthDuration-from-dateTime2args-1
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-yearMonthDuration-from-dateTime" operator
   *  with the arguments set as follows:                    
   * arg1 = xs:dateTime(lower bound)                        
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime2args1() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") - xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1970-01-01T00:00:00Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDuration-from-dateTime2args-2
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-yearMonthDuration-from-dateTime" operator
   *  with the arguments set as follows:                    
   * arg1 = xs:dateTime(mid range)                          
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime2args2() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1996-04-07T01:40:52Z\") - xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1996-04-07T01:40:52Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDuration-from-dateTime2args-3
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-yearMonthDuration-from-dateTime" operator
   *  with the arguments set as follows:                    
   * arg1 = xs:dateTime(upper bound)                        
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime2args3() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2030-12-31T23:59:59Z\") - xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2030-12-31T23:59:59Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDuration-from-dateTime2args-4
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-yearMonthDuration-from-dateTime" operator
   *  with the arguments set as follows:                    
   * arg1 = xs:dateTime(lower bound)                        
   * $arg2 = xs:yearMonthDuration(mid range)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime2args4() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") - xs:yearMonthDuration(\"P1000Y6M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0969-07-01T00:00:00Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDuration-from-dateTime2args-5
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-yearMonthDuration-from-dateTime" operator
   *  with the arguments set as follows:                    
   * arg1 = xs:dateTime(lower bound)                        
   * $arg2 = xs:yearMonthDuration(upper bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDateTime2args5() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") - xs:yearMonthDuration(\"P2030Y12M\")",
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
        assertStringValue(false, "-0062-01-01T00:00:00Z")
      ||
        assertStringValue(false, "-0061-01-01T00:00:00Z")
      )
    );
  }
}
