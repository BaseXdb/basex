package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the date-greater-than() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpDateGreaterThan extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DateGT-1                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'gt' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateGT1() {
    final XQuery query = new XQuery(
      "xs:date(\"2004-07-13\") gt xs:date(\"2004-07-12\")",
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
   *  Test: K-DateGT-2                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'gt' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateGT2() {
    final XQuery query = new XQuery(
      "not(xs:date(\"2004-07-12\") gt xs:date(\"2004-07-12\"))",
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
   *  Test: K-DateGT-3                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'gt' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateGT3() {
    final XQuery query = new XQuery(
      "not(xs:date(\"2004-07-12\") gt xs:date(\"2004-07-13\"))",
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
   *  Test: K-DateGT-4                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ge' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateGT4() {
    final XQuery query = new XQuery(
      "xs:date(\"2004-07-12\") ge xs:date(\"2004-07-12\")",
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
   *  Test: K-DateGT-5                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ge' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateGT5() {
    final XQuery query = new XQuery(
      "xs:date(\"2004-07-13\") ge xs:date(\"2004-07-12\")",
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
   *  Test: K-DateGT-6                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ge' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateGT6() {
    final XQuery query = new XQuery(
      "not(xs:date(\"2004-07-11\") ge xs:date(\"2004-07-12\"))",
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
  public void cbclDateGreaterEqual001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string }; declare function local:date($year as xs:integer, $month as xs:integer, $day as xs:integer) { let $m := local:two-digit($month), $d := local:two-digit($day) return xs:date(concat($year, '-', $m, '-', $d)) }; \n" +
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
  public void cbclDateGreaterEqual002() {
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
  public void cbclDateGreaterEqual003() {
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
  public void cbclDateGreaterEqual004() {
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
  public void cbclDateGreaterEqual005() {
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
  public void cbclDateGreaterEqual006() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string }; \n" +
      "      \tdeclare function local:date($year as xs:integer, $month as xs:integer, $day as xs:integer) { let $m := local:two-digit($month), $d := local:two-digit($day) return xs:date(concat($year, '-', $m, '-', $d)) }; \n" +
      "      \tnot(local:date(2008, 05, 12) ge xs:date(\"1972-12-15\"))\n" +
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
  public void cbclDateGreaterEqual007() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-30\") ge xs:date(\"2008-01-31+09:00\")",
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
  public void cbclDateGreaterEqual008() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-31+09:00\") ge xs:date(\"2008-01-30\")",
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
  public void cbclDateGreaterEqual009() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-31\") ge xs:date(\"2008-01-31+09:00\")",
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
  public void cbclDateGreaterEqual010() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-01-31+09:00\") ge xs:date(\"2008-01-31\")",
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
  public void cbclDateGreaterThan011() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:date($date as xs:date, $null as xs:boolean) { if ($null) then () else $date }; \n" +
      "      \texists(local:date(xs:date(\"1972-12-15\"), fn:true()) gt xs:date(\"1972-12-15\"))\n" +
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
  public void cbclDateGreaterThan012() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:date($date as xs:date, $null as xs:boolean) { if ($null) then () else $date }; \n" +
      "      \tlocal:date(xs:date(\"1972-12-15\"), fn:false()) gt xs:date(\"1972-12-15\")\n" +
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
  public void cbclDateGreaterThan013() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:date($date as xs:date, $null as xs:boolean) { if ($null) then () else $date }; \n" +
      "      \texists(local:date(xs:date(\"1972-12-15\"), fn:true()) le xs:date(\"1972-12-15\"))\n" +
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
  public void cbclDateGreaterThan014() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:date($date as xs:date, $null as xs:boolean) { if ($null) then () else $date }; \n" +
      "      \tlocal:date(xs:date(\"1972-12-15\"), fn:false()) le xs:date(\"1972-12-15\")\n" +
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
  public void cbclDateGreaterThan015() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:date($date as xs:string, $timezone as xs:string) { xs:date( concat($date, $timezone) ) }; \n" +
      "      \tadjust-date-to-timezone(local:date(\"1972-12-14\", \"-12:00\")) gt adjust-date-to-timezone(xs:date(\"1972-12-15+12:00\"))\n" +
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
  public void cbclDateGreaterThan016() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:date($date as xs:string, $timezone as xs:string) { xs:date( concat($date, $timezone) ) }; \n" +
      "      \tadjust-date-to-timezone(local:date(\"1972-12-14\", \"-12:00\")) ge adjust-date-to-timezone(xs:date(\"1972-12-15+12:00\"))\n" +
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
  public void cbclDateGt001() {
    final XQuery query = new XQuery(
      "xs:date(\"25252734927766555-07-28\") > xs:date(\"-25252734927766555-06-07+02:00\")",
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
   *  Test that comparing large dates does not overflow. .
   */
  @org.junit.Test
  public void cbclDateLe001() {
    final XQuery query = new XQuery(
      "xs:date(\"-25252734927766555-06-07+02:00\") <= xs:date(\"25252734927766555-07-28\")",
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
   * Test: op-date-greater-than-1                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-greater-than" function    
   * As per example 1 (for this function)of the F&O specs   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan1() {
    final XQuery query = new XQuery(
      "(xs:date(\"2004-12-25Z\") gt xs:date(\"2004-12-25+07:00\"))",
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
   * Test: op-date-greater-than-10                          
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-greater-than" function used  
   * together with "or" expression (ge operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan10() {
    final XQuery query = new XQuery(
      "(xs:date(\"1976-10-25Z\") ge xs:date(\"1976-10-28Z\")) or (xs:date(\"1980-08-11Z\") ge xs:date(\"1980-08-10Z\"))",
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
   * Test: op-date-greater-than-11                          
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-greater-than" function used
   * together with "fn:true"/or expression (gt operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan11() {
    final XQuery query = new XQuery(
      "(xs:date(\"1980-05-18Z\") gt xs:date(\"1980-05-17Z\")) or (fn:true())",
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
   * Test: op-date-greater-than-12                          
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-greater-than" function used
   * together with "fn:true"/or expression (ge operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan12() {
    final XQuery query = new XQuery(
      "(xs:date(\"2000-10-25Z\") ge xs:date(\"2000-10-26Z\")) or (fn:true())",
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
   * Test: op-date-greater-than-13                          
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-greater-than" function used
   * together with "fn:false"/or expression (gt operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan13() {
    final XQuery query = new XQuery(
      "(xs:date(\"1980-01-01Z\") gt xs:date(\"1980-10-01Z\")) or (fn:false())",
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
   * Test: op-date-greater-than-14                          
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-greater-than" function used
   * together with "fn:false"/or expression (ge operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan14() {
    final XQuery query = new XQuery(
      "(xs:date(\"1980-10-25Z\") ge xs:date(\"1980-10-26Z\")) or (fn:false())",
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
   * Test: op-date-greater-than-2                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-greater-than" function    
   * As per example 2 (for this function) of the F&O  specs 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan2() {
    final XQuery query = new XQuery(
      "(xs:date(\"2004-12-25-12:00\") gt xs:date(\"2004-12-26+12:00\"))",
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
   * Test: op-date-greater-than-3                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-greater-than" function that
   * return true and used together with fn:not (gt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan3() {
    final XQuery query = new XQuery(
      "fn:not((xs:date(\"2005-14-26Z\") gt xs:date(\"2005-14-25Z\")))",
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
   * return true and used together with fn:not (ge operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan4() {
    final XQuery query = new XQuery(
      "fn:not(xs:date(\"2005-04-02Z\") ge xs:date(\"2005-04-02Z\"))",
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
   * Test: op-date-greater-than-5                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-greater-than" function that
   * return false and used together with fn:not (gt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan5() {
    final XQuery query = new XQuery(
      "fn:not(xs:date(\"2000-11-09Z\") gt xs:date(\"2000-11-10Z\"))",
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
   * Test: op-date-greater-than-6                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-greater-than" function that
   * return false and used together with fn:not(ge operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan6() {
    final XQuery query = new XQuery(
      "fn:not(xs:date(\"2005-10-23Z\") ge xs:date(\"2005-10-25Z\"))",
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
   * Test: op-date-greater-than-7                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-greater-than" function used
   * together with "and" expression (gt operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan7() {
    final XQuery query = new XQuery(
      "(xs:date(\"2000-01-01Z\") gt xs:date(\"2000-01-01Z\")) and (xs:date(\"2001-02-02Z\") gt xs:date(\"2001-03-02Z\"))",
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
   * Test: op-date-greater-than-8                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-greater-than" function used
   * together with "and" expression (ge operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan8() {
    final XQuery query = new XQuery(
      "(xs:date(\"2000-01-25Z\") ge xs:date(\"2000-10-26Z\")) and (xs:date(\"1975-10-26Z\") ge xs:date(\"1975-10-28Z\"))",
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
   * Test: op-date-greater-than-9                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-greater-than" function used
   * together with "or" expression (gt operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan9() {
    final XQuery query = new XQuery(
      "(xs:date(\"2000-10-26Z\") gt xs:date(\"2000-10-28Z\")) or (xs:date(\"1976-10-28Z\") gt xs:date(\"1976-10-28Z\"))",
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
   * Test: op-date-greater-than2args-1                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan2args1() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") gt xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-greater-than2args-10                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan2args10() {
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
   * Test: op-date-greater-than2args-2                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(mid range)                             
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan2args2() {
    final XQuery query = new XQuery(
      "xs:date(\"1983-11-17Z\") gt xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-greater-than2args-3                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(upper bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan2args3() {
    final XQuery query = new XQuery(
      "xs:date(\"2030-12-31Z\") gt xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-greater-than2args-4                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan2args4() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") gt xs:date(\"1983-11-17Z\")",
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
   * Test: op-date-greater-than2args-5                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan2args5() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") gt xs:date(\"2030-12-31Z\")",
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
   * Test: op-date-greater-than2args-6                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan2args6() {
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
   * Test: op-date-greater-than2args-7                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(mid range)                             
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan2args7() {
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
   * Test: op-date-greater-than2args-8                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(upper bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan2args8() {
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
   * Test: op-date-greater-than2args-9                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateGreaterThan2args9() {
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
}
