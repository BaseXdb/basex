package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the subtract-yearMonthDuration-from-date() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpSubtractYearMonthDurationFromDate extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DateSubtractYMD-1                             
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple testing involving operator '-' between xs:date and xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateSubtractYMD1() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-08-12\") - xs:yearMonthDuration(\"P3Y7M\") eq xs:date(\"1996-01-12\")",
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
   *  test subtraction of zero duration to date .
   */
  @org.junit.Test
  public void cbclSubtractYearMonthDurationFromDate001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:two-digit($number as xs:integer) { \n" +
      "      \t\tlet $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string \n" +
      "      \t}; \n" +
      "      \tdeclare function local:date($year as xs:integer, $month as xs:integer, $day as xs:integer) { \n" +
      "      \t\tlet $m := local:two-digit($month), $d := local:two-digit($day) return xs:date(concat($year, '-', $m, '-', $d)) \n" +
      "      \t}; \n" +
      "      \tlocal:date(2008, 05, 12) - xs:dayTimeDuration(\"P0D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2008-05-12")
    );
  }

  /**
   *  test subtraction of large duration from date .
   */
  @org.junit.Test
  public void cbclSubtractYearMonthDurationFromDate002() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:two-digit($number as xs:integer) { \n" +
      "      \t\tlet $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string \n" +
      "      \t}; \n" +
      "      \tdeclare function local:date($year as xs:integer, $month as xs:integer, $day as xs:integer) { \n" +
      "      \t\tlet $m := local:two-digit($month), $d := local:two-digit($day) return xs:date(concat($year, '-', $m, '-', $d)) \n" +
      "      \t}; \n" +
      "      \tlocal:date(-25252734927766554, 05, 12) - xs:yearMonthDuration(\"-P3214267297Y\")\n" +
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
      (
        error("FODT0001")
      ||
        assertStringValue(false, "-25252731713499257-05-12")
      )
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDuration-from-date-1        
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDuration-from-date" operator 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate1() {
    final XQuery query = new XQuery(
      "xs:date(\"2000-10-30\") - xs:yearMonthDuration(\"P1Y2M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1999-08-30")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDuration-from-date-10       
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The string value of "subtract-yearMonthDuration-from-date" operator used
   * together with an "or" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate10() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1985-07-05Z\") - xs:yearMonthDuration(\"P02Y02M\"))) or fn:string((xs:date(\"1985-07-05Z\") - xs:yearMonthDuration(\"P02Y02M\")))",
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
   * Test: op-subtract-yearMonthDuration-from-date-12       
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The string value of "subtract-yearMonthDuration-from-date" operator used 
   * with a boolean expression and the "fn:true" function.   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate12() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1980-03-02Z\") - xs:yearMonthDuration(\"P05Y05M\"))) and (fn:true())",
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
   * Test: op-subtract-yearMonthDuration-from-date-13       
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDuration-from-date" operator used  
   * together with the numeric-equal-operator "eq".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate13() {
    final XQuery query = new XQuery(
      "(xs:date(\"1980-05-05Z\") - xs:yearMonthDuration(\"P23Y11M\")) eq xs:date(\"1980-05-05Z\")",
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
   * Test: op-subtract-yearMonthDuration-from-date-14       
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDuration-from-date" operator used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate14() {
    final XQuery query = new XQuery(
      "(xs:date(\"1979-12-12Z\") - xs:yearMonthDuration(\"P08Y08M\")) ne xs:date(\"1979-12-12Z\")",
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
   * Test: op-subtract-yearMonthDuration-from-date-15       
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDuration-from-date" operator used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate15() {
    final XQuery query = new XQuery(
      "(xs:date(\"1978-12-12Z\") - xs:yearMonthDuration(\"P17Y12M\")) le xs:date(\"1978-12-12Z\")",
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
   * Test: op-subtract-yearMonthDuration-from-date-16       
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDuration-from-date" operator used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate16() {
    final XQuery query = new XQuery(
      "(xs:date(\"1977-12-12Z\") - xs:yearMonthDuration(\"P18Y02M\")) ge xs:date(\"1977-12-12Z\")",
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
   * Test: op-subtract-yearMonthDuration-from-date-17       
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The string value of "subtract-yearMonthDuration-from-date" operator 
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate17() {
    final XQuery query = new XQuery(
      "fn:string(xs:date(\"2000-12-12Z\") - xs:yearMonthDuration(\"P18Y11M\")) and fn:false()",
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
   * Test: op-subtract-yearMonthDuration-from-date-18       
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The string value of "subtract-yearMonthDuration-from-date" operator as 
   * part of a boolean expression (or operator) and the "fn:boolean" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate18() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1999-10-23Z\") - xs:yearMonthDuration(\"P19Y12M\"))) or fn:false()",
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
   * Test: op-subtract-yearMonthDuration-from-date-2        
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDuration-from-date" operator 
   * As per example 2 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate2() {
    final XQuery query = new XQuery(
      "xs:date(\"2000-02-29Z\") - xs:yearMonthDuration(\"P1Y\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1999-02-28Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDuration-from-date-3        
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDuration-from-date" operator 
   * As per example 3 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate3() {
    final XQuery query = new XQuery(
      "xs:date(\"2000-10-31-05:00\") - xs:yearMonthDuration(\"P1Y1M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1999-09-30-05:00")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDuration-from-date-4        
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The string value of "subtract-yearMonthDuration-from-date" operator that
   * return true and used together with fn:not.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:date(\"1998-09-12Z\") - xs:yearMonthDuration(\"P20Y03M\")))",
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
   * Test: op-subtract-yearMonthDuration-from-date-5        
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The string value of "subtract-yearMonthDuration-from-date" operator that  
   * is used as an argument to the fn:boolean function.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:date(\"1962-03-12Z\") - xs:yearMonthDuration(\"P10Y01M\")))",
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
   * Test: op-subtract-yearMonthDuration-from-date-6        
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDuration-from-date" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate6() {
    final XQuery query = new XQuery(
      "fn:number(xs:date(\"1988-01-28Z\") - xs:yearMonthDuration(\"P09Y02M\"))",
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
   * Test: op-subtract-yearMonthDuration-from-date-7        
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDuration-from-date" operator used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate7() {
    final XQuery query = new XQuery(
      "fn:string(xs:date(\"1989-07-05Z\") - xs:yearMonthDuration(\"P08Y04M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1981-03-05Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDuration-from-date-8        
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDuration-from-date" operator that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate8() {
    final XQuery query = new XQuery(
      "(xs:date(\"0001-01-01Z\") - xs:yearMonthDuration(\"-P20Y07M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0021-08-01Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDuration-from-date-9        
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The string value of "subtract-yearMonthDuration-from-date" operator used  
   * together with an "and" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate9() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1993-12-09Z\") - xs:yearMonthDuration(\"P03Y03M\"))) and fn:string((xs:date(\"1993-12-09Z\") - xs:yearMonthDuration(\"P03Y03M\")))",
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
   * Test: op-subtract-yearMonthDuration-from-date2args-1    
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-yearMonthDuration-from-date" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate2args1() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") - xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1970-01-01Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDuration-from-date2args-2    
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-yearMonthDuration-from-date" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(mid range)                             
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate2args2() {
    final XQuery query = new XQuery(
      "xs:date(\"1983-11-17Z\") - xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1983-11-17Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDuration-from-date2args-3    
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-yearMonthDuration-from-date" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(upper bound)                           
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate2args3() {
    final XQuery query = new XQuery(
      "xs:date(\"2030-12-31Z\") - xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2030-12-31Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDuration-from-date2args-4    
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-yearMonthDuration-from-date" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:yearMonthDuration(mid range)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate2args4() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") - xs:yearMonthDuration(\"P1000Y6M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0969-07-01Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDuration-from-date2args-5    
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-yearMonthDuration-from-date" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:yearMonthDuration(upper bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurationFromDate2args5() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") - xs:yearMonthDuration(\"P2030Y12M\")",
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
        assertStringValue(false, "-0062-01-01Z")
      ||
        assertStringValue(false, "-0061-01-01Z")
      )
    );
  }
}
