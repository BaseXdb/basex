package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the date-equal() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpDateEqual extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DateEQ-1                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:date, returning positive. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateEQ1() {
    final XQuery query = new XQuery(
      "xs:date(\"2004-08-12\") eq xs:date(\"2004-08-12\")",
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
   *  Test: K-DateEQ-2                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateEQ2() {
    final XQuery query = new XQuery(
      "not(xs:date(\"2004-08-12\") eq xs:date(\"2003-08-12\"))",
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
   *  Test: K-DateEQ-3                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateEQ3() {
    final XQuery query = new XQuery(
      "xs:date(\"2004-08-12\") ne xs:date(\"2004-07-12\")",
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
   *  Test: K-DateEQ-4                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateEQ4() {
    final XQuery query = new XQuery(
      "not(xs:date(\"2004-07-12\") ne xs:date(\"2004-07-12\"))",
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
   *  Test: K-DateEQ-5                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to Z, in xs:date. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateEQ5() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04-00:00\") eq xs:date(\"1999-12-04Z\")",
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
   *  Test: K-DateEQ-6                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset +00:00 is equal to Z, in xs:date. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateEQ6() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04+00:00\") eq xs:date(\"1999-12-04Z\")",
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
   *  Test: K-DateEQ-7                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset Z is equal to Z, in xs:date. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateEQ7() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04Z\") eq xs:date(\"1999-12-04Z\")",
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
   *  Test: K-DateEQ-8                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to +00:00, in xs:date. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateEQ8() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04-00:00\") eq xs:date(\"1999-12-04+00:00\")",
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
   *  Test: K2-DateEQ-1                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Basic negative equalness test for xs:date.   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void k2DateEQ1() {
    final XQuery query = new XQuery(
      "xs:time(\"01:01:01-03:00\") ne xs:time(\"01:01:01+03:00\")",
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
   *  Test that comparing large dates does not overflow. .
   */
  @org.junit.Test
  public void cbclDateEq001() {
    final XQuery query = new XQuery(
      "xs:date(\"-25252734927766555-06-07+02:00\") = xs:date(\"25252734927766555-07-28\")",
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
        assertBoolean(false)
      )
    );
  }

  /**
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateEqual001() {
    final XQuery query = new XQuery(
      "declare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string }; declare function local:date($year as xs:integer, $month as xs:integer, $day as xs:integer) { let $m := local:two-digit($month), $d := local:two-digit($day) return xs:date(concat($year, '-', $m, '-', $d)) }; not(local:date(2008, 05, 12) eq xs:date(\"1972-12-15\"))",
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
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateEqual002() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-30\") eq xs:date(\"2008-01-31+09:00\")",
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
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateEqual003() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-31+09:00\") eq xs:date(\"2008-01-30\")",
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
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateEqual004() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-31\") eq xs:date(\"2008-01-31+09:00\")",
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
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateEqual005() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-31+09:00\") eq xs:date(\"2008-01-31\")",
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
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateEqual006() {
    final XQuery query = new XQuery(
      "declare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string }; declare function local:date($year as xs:integer, $month as xs:integer, $day as xs:integer) { let $m := local:two-digit($month), $d := local:two-digit($day) return xs:date(concat($year, '-', $m, '-', $d)) }; not(local:date(2008, 05, 12) ne xs:date(\"1972-12-15\"))",
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
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateEqual007() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-30\") ne xs:date(\"2008-01-31+09:00\")",
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
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateEqual008() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-31+09:00\") ne xs:date(\"2008-01-30\")",
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
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateEqual009() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-31\") ne xs:date(\"2008-01-31+09:00\")",
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
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateEqual010() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-31+09:00\") ne xs:date(\"2008-01-31\")",
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
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateEqual011() {
    final XQuery query = new XQuery(
      "declare function local:date($date as xs:date, $null as xs:boolean) { if ($null) then () else $date }; exists(local:date(xs:date(\"1972-12-15\"), fn:true()) eq xs:date(\"1972-12-15\"))",
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
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateEqual012() {
    final XQuery query = new XQuery(
      "declare function local:date($date as xs:date, $null as xs:boolean) { if ($null) then () else $date }; local:date(xs:date(\"1972-12-15\"), fn:false()) eq xs:date(\"1972-12-15\")",
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
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateEqual013() {
    final XQuery query = new XQuery(
      "declare function local:date($date as xs:date, $null as xs:boolean) { if ($null) then () else $date }; exists(local:date(xs:date(\"1972-12-15\"), fn:true()) ne xs:date(\"1972-12-15\"))",
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
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateEqual014() {
    final XQuery query = new XQuery(
      "declare function local:date($date as xs:date, $null as xs:boolean) { if ($null) then () else $date }; local:date(xs:date(\"1972-12-15\"), fn:false()) ne xs:date(\"1972-12-15\")",
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
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateEqual015() {
    final XQuery query = new XQuery(
      "declare function local:date($date as xs:string, $timezone as xs:string) { xs:date( concat($date, $timezone) ) }; adjust-date-to-timezone(local:date(\"1972-12-14\", \"-12:00\")) eq adjust-date-to-timezone(xs:date(\"1972-12-15+12:00\"))",
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
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateEqual016() {
    final XQuery query = new XQuery(
      "declare function local:date($date as xs:string, $timezone as xs:string) { xs:date( concat($date, $timezone) ) }; adjust-date-to-timezone(local:date(\"1972-12-14\", \"-12:00\")) ne adjust-date-to-timezone(xs:date(\"1972-12-15+12:00\"))",
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
   *  Test that comparing large dates does not overflow. .
   */
  @org.junit.Test
  public void cbclDateNe001() {
    final XQuery query = new XQuery(
      "xs:date(\"25252734927766555-07-28\") != xs:date(\"-25252734927766555-06-07+02:00\")",
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
        assertBoolean(true)
      )
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-date-equal2args-1                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args1() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") eq xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-10                             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args10() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") ne xs:date(\"2030-12-31Z\")",
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
   * Test: op-date-equal2args-11                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:date-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args11() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") le xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-12                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:date-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(mid range)                             
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args12() {
    final XQuery query = new XQuery(
      "xs:date(\"1983-11-17Z\") le xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-13                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:date-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(upper bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args13() {
    final XQuery query = new XQuery(
      "xs:date(\"2030-12-31Z\") le xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-14                            
   * Written By: Carmelo Montanez                           
   * Date: June 14, 2005                                    
   * Purpose: Evaluates The "op:date-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args14() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") le xs:date(\"1983-11-17Z\")",
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
   * Test: op-date-equal2args-15                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:date-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args15() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") le xs:date(\"2030-12-31Z\")",
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
   * Test: op-date-equal2args-16                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:date-equal" operator (ge)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args16() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") ge xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-17                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:date-equal" operator (ge)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(mid range)                             
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args17() {
    final XQuery query = new XQuery(
      "xs:date(\"1983-11-17Z\") ge xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-18                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:date-equal" operator (ge)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(upper bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args18() {
    final XQuery query = new XQuery(
      "xs:date(\"2030-12-31Z\") ge xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-19                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:date-equal" operator (ge)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args19() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") ge xs:date(\"1983-11-17Z\")",
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
   * Test: op-date-equal2args-2                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(mid range)                             
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args2() {
    final XQuery query = new XQuery(
      "xs:date(\"1983-11-17Z\") eq xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-20                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:date-equal" operator (ge)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args20() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") ge xs:date(\"2030-12-31Z\")",
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
   * Test: op-date-equal2args-3                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(upper bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args3() {
    final XQuery query = new XQuery(
      "xs:date(\"2030-12-31Z\") eq xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-4                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args4() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") eq xs:date(\"1983-11-17Z\")",
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
   * Test: op-date-equal2args-5                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args5() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") eq xs:date(\"2030-12-31Z\")",
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
   * Test: op-date-equal2args-6                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args6() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") ne xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-7                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(mid range)                             
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args7() {
    final XQuery query = new XQuery(
      "xs:date(\"1983-11-17Z\") ne xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-8                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(upper bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args8() {
    final XQuery query = new XQuery(
      "xs:date(\"2030-12-31Z\") ne xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-9                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args9() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") ne xs:date(\"1983-11-17Z\")",
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
