package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the add-dayTimeDuration-to-dateTime() function.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpAddDayTimeDurationToDateTime extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DateTimeAddDTD-1                              
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The '+' operator is not available between xs:date and xs:dateTime. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimeAddDTD1() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-10-12\") + xs:dateTime(\"1999-10-12T08:12:12\")",
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
   *  Test: K-DateTimeAddDTD-2                              
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The '+' operator is not available between xs:dateTime and xs:date. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimeAddDTD2() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-10-12T08:12:12\") + xs:date(\"1999-10-12\")",
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
   *  Test: K-DateTimeAddDTD-3                              
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The '+' operator is not available between xs:time and xs:dateTime. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimeAddDTD3() {
    final XQuery query = new XQuery(
      "xs:time(\"08:12:12\") + xs:dateTime(\"1999-10-12T08:12:12\")",
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
   *  Test: K-DateTimeAddDTD-4                              
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The '+' operator is not available between xs:dateTime and xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimeAddDTD4() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-10-12T08:12:12\") + xs:time(\"08:12:12\")",
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
   *  test addition of zero duration to dateTime .
   */
  @org.junit.Test
  public void cbclAddDayTimeDurationToDateTime001() {
    final XQuery query = new XQuery(
      "declare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string }; declare function local:dateTime($year as xs:integer, $month as xs:integer, $day as xs:integer, $hour as xs:integer, $mins as xs:integer) { let $m := local:two-digit($month), $d := local:two-digit($day), $h := local:two-digit($hour), $n := local:two-digit($mins) return xs:dateTime(concat($year, '-', $m, '-', $d, 'T', $h, ':', $n, ':00')) }; xs:dayTimeDuration(\"P0D\") + local:dateTime(2008, 05, 12, 12, 59)",
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
   *  test addition of zero duration to dateTime .
   */
  @org.junit.Test
  public void cbclAddDayTimeDurationToDateTime002() {
    final XQuery query = new XQuery(
      "declare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string }; declare function local:dateTime($year as xs:integer, $month as xs:integer, $day as xs:integer, $hour as xs:integer, $mins as xs:integer) { let $m := local:two-digit($month), $d := local:two-digit($day), $h := local:two-digit($hour), $n := local:two-digit($mins) return xs:dateTime(concat($year, '-', $m, '-', $d, 'T', $h, ':', $n, ':00')) }; local:dateTime(2008, 05, 12, 12, 59) + xs:dayTimeDuration(\"P0D\")",
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
   *  test addition of large duration to date .
   */
  @org.junit.Test
  public void cbclAddDayTimeDurationToDateTime003() {
    final XQuery query = new XQuery(
      "declare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string }; declare function local:dateTime($year as xs:integer, $month as xs:integer, $day as xs:integer, $hour as xs:integer, $mins as xs:integer) { let $m := local:two-digit($month), $d := local:two-digit($day), $h := local:two-digit($hour), $n := local:two-digit($mins) return xs:dateTime(concat($year, '-', $m, '-', $d, 'T', $h, ':', $n, ':00')) }; local:dateTime(25252734927766555, 05, 12, 12, 59) + xs:dayTimeDuration(\"P40267296D\")",
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
   * Test: op-add-dayTimeDuration-to-dateTime-1             
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-dateTime" operator 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime1() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2000-10-30T11:12:00\") + xs:dayTimeDuration(\"P3DT1H15M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2000-11-02T12:27:00")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDuration-to-dateTime-10            
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of the "add-dayTimeDuration-to-dateTime" 
   * operator used together with an "or" expression.        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime10() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1985-07-05T14:14:14Z\") + xs:dayTimeDuration(\"P03DT01H04M\"))) or fn:string((xs:dateTime(\"1985-07-05T15:15:15Z\") + xs:dayTimeDuration(\"P01DT01H03M\")))",
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
   * Test: op-add-dayTimeDuration-to-dateTime-12            
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of the "add-dayTimeDuration-to-dateTime" 
   * operator used with a boolean expression and the "fn:true" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime12() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1980-03-02T16:12:10Z\") + xs:dayTimeDuration(\"P05DT08H11M\"))) and (fn:true())",
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
   * Test: op-add-dayTimeDuration-to-dateTime-13            
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-dateTime" operator used  
   * together with the numeric-equal-operator "eq".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime13() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1980-05-05T17:17:17Z\") + xs:dayTimeDuration(\"P23DT11H11M\")) eq xs:dateTime(\"1980-05-05T17:17:17Z\")",
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
   * Test: op-add-dayTimeDuration-to-dateTime-14            
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-dateTime" operator used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime14() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1979-12-12T18:18:18Z\") + xs:dayTimeDuration(\"P08DT08H05M\")) ne xs:dateTime(\"1979-12-12T16:15:14Z\")",
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
   * Test: op-add-dayTimeDuration-to-dateTime-15            
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-dateTime" operator used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime15() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1978-12-12T12:45:12Z\") + xs:dayTimeDuration(\"P17DT10H02M\")) le xs:dateTime(\"1978-12-12T16:34:23Z\")",
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
   * Test: op-add-dayTimeDuration-to-dateTime-16            
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-dateTime" operator used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime16() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1977-12-12T13:12:15Z\") + xs:dayTimeDuration(\"P18DT02H02M\")) ge xs:dateTime(\"1977-12-12T15:56:10Z\")",
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
   * Test: op-add-dayTimeDuration-to-dateTime-2             
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of "add-dayTimeDuration-to-dateTime" operator 
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime2() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"2000-12-12T11:10:03Z\") + xs:dayTimeDuration(\"P12DT10H07M\"))) and fn:false()",
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
   * Test: op-add-dayTimeDuration-to-dateTime-3             
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of the "add-dayTimeDuration-to-dateTime" 
   * operator as part of a boolean expression (or operator) and the "fn:boolean" 
   * function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime3() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1999-10-23T03:12:23Z\") + xs:dayTimeDuration(\"P19DT13H10M\"))) or fn:false()",
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
   * Test: op-add-dayTimeDuration-to-dateTime-4             
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of the "add-dayTimeDuration-to-dateTime" 
   * operator that return true and used together with fn:not.
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:dateTime(\"1998-09-12T13:23:23Z\") + xs:dayTimeDuration(\"P02DT07H01M\")))",
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
   * Test: op-add-dayTimeDuration-to-dateTime-5             
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of the "add-dayTimeDuration-to-dateTime" 
   * operator that is used as an argument to the fn:boolean function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:dateTime(\"1962-03-12T12:34:09Z\") + xs:dayTimeDuration(\"P03DT08H06M\")))",
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
   * Test: op-add-dayTimeDuration-to-dateTime-6             
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-dateTime" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime6() {
    final XQuery query = new XQuery(
      "fn:number(xs:dateTime(\"1988-01-28T12:34:12Z\") + xs:dayTimeDuration(\"P10DT08H01M\"))",
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
   * Test: op-add-dayTimeDuration-to-dateTime-7             
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-dateTime" operator used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime7() {
    final XQuery query = new XQuery(
      "fn:string(xs:dateTime(\"1989-07-05T10:10:10Z\") + xs:dayTimeDuration(\"P01DT09H02M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1989-07-06T19:12:10Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDuration-to-dateTime-8             
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-dateTime" operator that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime8() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"0001-01-01T11:11:11Z\") + xs:dayTimeDuration(\"-P11DT02H02M\"))",
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
        assertStringValue(false, "-0001-12-21T09:09:11Z")
      ||
        assertStringValue(false, "0000-12-21T09:09:11Z")
      )
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDuration-to-dateTime-9             
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of the "add-dayTimeDuration-to-dateTime" 
   * operator used together with an "and" expression.       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime9() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1993-12-09T13:13:13Z\") + xs:dayTimeDuration(\"P03DT01H04M\"))) and fn:string((xs:dateTime(\"1993-12-09T13:13:13Z\") + xs:dayTimeDuration(\"P01DT01H03M\")))",
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
   * Test: op-add-dayTimeDuration-to-dateTime2args-1         
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDuration-to-dateTime" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(lower bound)                       
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime2args1() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") + xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-add-dayTimeDuration-to-dateTime2args-2         
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDuration-to-dateTime" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(mid range)                         
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime2args2() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1996-04-07T01:40:52Z\") + xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-add-dayTimeDuration-to-dateTime2args-3         
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDuration-to-dateTime" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(upper bound)                       
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime2args3() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2030-12-31T23:59:59Z\") + xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-add-dayTimeDuration-to-dateTime2args-4         
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDuration-to-dateTime" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(lower bound)                       
   * $arg2 = xs:dayTimeDuration(mid range)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime2args4() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") + xs:dayTimeDuration(\"P15DT11H59M59S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1970-01-16T11:59:59Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDuration-to-dateTime2args-5         
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDuration-to-dateTime" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(lower bound)                       
   * $arg2 = xs:dayTimeDuration(upper bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDateTime2args5() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") + xs:dayTimeDuration(\"P31DT23H59M59S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1970-02-01T23:59:59Z")
    );
  }
}
