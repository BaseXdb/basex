package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the date-less-than() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpDateLessThan extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DateLT-1                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'lt' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateLT1() {
    final XQuery query = new XQuery(
      "xs:date(\"2004-07-12\") lt xs:date(\"2004-07-13\")",
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
   *  Test: K-DateLT-2                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'lt' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateLT2() {
    final XQuery query = new XQuery(
      "not(xs:date(\"2004-07-13\") lt xs:date(\"2004-07-12\"))",
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
   *  Test: K-DateLT-3                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'lt' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateLT3() {
    final XQuery query = new XQuery(
      "not(xs:date(\"2004-07-13\") lt xs:date(\"2004-07-13\"))",
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
   *  Test: K-DateLT-4                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'le' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateLT4() {
    final XQuery query = new XQuery(
      "xs:date(\"2004-07-12\") le xs:date(\"2004-07-12\")",
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
   *  Test: K-DateLT-5                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'le' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateLT5() {
    final XQuery query = new XQuery(
      "xs:date(\"2004-07-12\") le xs:date(\"2004-07-12\")",
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
   *  Test: K-DateLT-6                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'le' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateLT6() {
    final XQuery query = new XQuery(
      "not(xs:date(\"2004-07-13\") le xs:date(\"2004-07-12\"))",
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
  public void cbclDateGe001() {
    final XQuery query = new XQuery(
      "xs:date(\"25252734927766555-07-28\") >= xs:date(\"-25252734927766555-06-07+02:00\")",
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
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateLessThan001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:two-digit($number as xs:integer) { \n" +
      "      \t\tlet $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string \n" +
      "      \t}; \n" +
      "      \tdeclare function local:date($year as xs:integer, $month as xs:integer, $day as xs:integer) { \n" +
      "      \t\tlet $m := local:two-digit($month), $d := local:two-digit($day) return xs:date(concat($year, '-', $m, '-', $d)) \n" +
      "      \t}; \n" +
      "      \tnot(local:date(2008, 05, 12) lt xs:date(\"1972-12-15\"))\n" +
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
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateLessThan002() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-30\") lt xs:date(\"2008-01-31+09:00\")",
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
  public void cbclDateLessThan003() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-31+09:00\") lt xs:date(\"2008-01-30\")",
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
  public void cbclDateLessThan004() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-31\") lt xs:date(\"2008-01-31+09:00\")",
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
  public void cbclDateLessThan005() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-31+09:00\") lt xs:date(\"2008-01-31\")",
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
  public void cbclDateLessThan006() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string }; \n" +
      "      \tdeclare function local:date($year as xs:integer, $month as xs:integer, $day as xs:integer) { let $m := local:two-digit($month), $d := local:two-digit($day) return xs:date(concat($year, '-', $m, '-', $d)) }; \n" +
      "      \tnot(local:date(2008, 05, 12) le xs:date(\"1972-12-15\"))\n" +
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
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateLessThan007() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-30\") le xs:date(\"2008-01-31+09:00\")",
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
  public void cbclDateLessThan008() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-31+09:00\") le xs:date(\"2008-01-30\")",
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
  public void cbclDateLessThan009() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-31\") le xs:date(\"2008-01-31+09:00\")",
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
  public void cbclDateLessThan010() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-31+09:00\") le xs:date(\"2008-01-31\")",
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
  public void cbclDateLessThan011() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:date($date as xs:date, $null as xs:boolean) { if ($null) then () else $date }; \n" +
      "      \texists(local:date(xs:date(\"1972-12-15\"), fn:true()) lt xs:date(\"1972-12-15\"))\n" +
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
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateLessThan012() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:date($date as xs:date, $null as xs:boolean) { if ($null) then () else $date }; \n" +
      "      \tlocal:date(xs:date(\"1972-12-15\"), fn:false()) lt xs:date(\"1972-12-15\")\n" +
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
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateLessThan013() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:date($date as xs:date, $null as xs:boolean) { if ($null) then () else $date }; \n" +
      "      \texists(local:date(xs:date(\"1972-12-15\"), fn:true()) ge xs:date(\"1972-12-15\"))\n" +
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
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateLessThan014() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:date($date as xs:date, $null as xs:boolean) { if ($null) then () else $date }; \n" +
      "      \tlocal:date(xs:date(\"1972-12-15\"), fn:false()) ge xs:date(\"1972-12-15\")\n" +
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
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateLessThan015() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:date($date as xs:string, $timezone as xs:string) { xs:date( concat($date, $timezone) ) }; \n" +
      "      \tadjust-date-to-timezone(local:date(\"1972-12-14\", \"-12:00\")) lt adjust-date-to-timezone(xs:date(\"1972-12-15+12:00\"))\n" +
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
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of date .
   */
  @org.junit.Test
  public void cbclDateLessThan016() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:date($date as xs:string, $timezone as xs:string) { xs:date( concat($date, $timezone) ) }; \n" +
      "      \tadjust-date-to-timezone(local:date(\"1972-12-14\", \"-12:00\")) le adjust-date-to-timezone(xs:date(\"1972-12-15+12:00\"))\n" +
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
      assertBoolean(true)
    );
  }

  /**
   *  Test that comparing large dates does not overflow. .
   */
  @org.junit.Test
  public void cbclDateLt001() {
    final XQuery query = new XQuery(
      "xs:date(\"-25252734927766555-06-07+02:00\") < xs:date(\"25252734927766555-07-28\")",
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
   * Test: op-date-less-than-1                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function       
   * As per example 1 (for this function)of the F&O specs   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan1() {
    final XQuery query = new XQuery(
      "(xs:date(\"2004-12-25Z\") lt xs:date(\"2004-12-25-05:00\"))",
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
   * Test: op-date-less-than-10                             
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function used  
   * together with "or" expression (le operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan10() {
    final XQuery query = new XQuery(
      "(xs:date(\"1976-10-25Z\") le xs:date(\"1976-10-28Z\")) or (xs:date(\"1980-08-11Z\") le xs:date(\"1980-08-10Z\"))",
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
   * Test: op-date-less-than-11                             
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function used  
   * together with "fn:true"/or expression (lt operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan11() {
    final XQuery query = new XQuery(
      "(xs:date(\"1980-05-18Z\") lt xs:date(\"1980-05-17Z\")) or (fn:true())",
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
   * Test: op-date-less-than-12                             
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function used  
   * together with "fn:true"/or expression (le operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan12() {
    final XQuery query = new XQuery(
      "(xs:date(\"2000-10-25Z\") le xs:date(\"2000-10-26Z\")) or (fn:true())",
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
   * Test: op-date-less-than-13                             
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function used  
   * together with "fn:false"/or expression (lt operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan13() {
    final XQuery query = new XQuery(
      "(xs:date(\"1980-01-01Z\") lt xs:date(\"1980-10-01Z\")) or (fn:false())",
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
   * Test: op-date-less-than-14                             
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function used  
   * together with "fn:false"/or expression (le operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan14() {
    final XQuery query = new XQuery(
      "(xs:date(\"1980-10-25Z\") le xs:date(\"1980-10-26Z\")) or (fn:false())",
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
   * Test: op-date-less-than-2                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function       
   * As per example 2 (for this function) of the F&O  specs 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2() {
    final XQuery query = new XQuery(
      "(xs:date(\"2004-12-25-12:00\") le xs:date(\"2004-12-26+12:00\"))",
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
   * Test: op-date-less-than-3                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function that  
   * return true and used together with fn:not (lt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan3() {
    final XQuery query = new XQuery(
      "fn:not((xs:date(\"2005-14-25Z\") lt xs:date(\"2005-14-26Z\")))",
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
   * Test: op-date-less-than-4                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function that  
   * return true and used together with fn:not (le operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan4() {
    final XQuery query = new XQuery(
      "fn:not(xs:date(\"2005-04-02Z\") le xs:date(\"2005-04-02Z\"))",
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
   * Test: op-date-less-than-5                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function that  
   * return false and used together with fn:not (lt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan5() {
    final XQuery query = new XQuery(
      "fn:not(xs:date(\"2000-12-25Z\") lt xs:date(\"2000-11-25Z\"))",
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
   * Test: op-date-less-than-6                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function that  
   * return false and used together with fn:not(le operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan6() {
    final XQuery query = new XQuery(
      "fn:not(xs:date(\"2005-10-25Z\") le xs:date(\"2005-10-23Z\"))",
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
   * Test: op-date-less-than-7                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function used  
   * together with "and" expression (lt operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan7() {
    final XQuery query = new XQuery(
      "(xs:date(\"2000-01-01Z\") lt xs:date(\"2000-01-01Z\")) and (xs:date(\"2001-02-02Z\") lt xs:date(\"2001-03-02Z\"))",
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
   * Test: op-date-less-than-8                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function used  
   * together with "and" expression (le operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan8() {
    final XQuery query = new XQuery(
      "(xs:date(\"2000-01-25Z\") le xs:date(\"2000-10-26Z\")) and (xs:date(\"1975-10-26Z\") le xs:date(\"1975-10-28Z\"))",
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
   * Test: op-date-less-than-9                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function used  
   * together with "or" expression (lt operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan9() {
    final XQuery query = new XQuery(
      "(xs:date(\"2000-10-26Z\") lt xs:date(\"2000-10-28Z\")) or (xs:date(\"1976-10-28Z\") lt xs:date(\"1976-10-28Z\"))",
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
   * Test: op-date-less-than2args-1                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args1() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") lt xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-less-than2args-10                         
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args10() {
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
   * Test: op-date-less-than2args-2                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(mid range)                             
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args2() {
    final XQuery query = new XQuery(
      "xs:date(\"1983-11-17Z\") lt xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-less-than2args-3                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(upper bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args3() {
    final XQuery query = new XQuery(
      "xs:date(\"2030-12-31Z\") lt xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-less-than2args-4                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args4() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") lt xs:date(\"1983-11-17Z\")",
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
   * Test: op-date-less-than2args-5                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args5() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") lt xs:date(\"2030-12-31Z\")",
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
   * Test: op-date-less-than2args-6                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args6() {
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
   * Test: op-date-less-than2args-7                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(mid range)                             
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args7() {
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
   * Test: op-date-less-than2args-8                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(upper bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args8() {
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
   * Test: op-date-less-than2args-9                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args9() {
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
}
