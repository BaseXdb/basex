package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the dateTime-greater-than() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpDateTimeGreaterThan extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DateTimeGT-1                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'gt' for xs:dateTime.         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimeGT1() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2004-07-13T23:01:04.12\") gt xs:dateTime(\"2004-07-12T23:01:04.12\")",
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
   *  Test: K-DateTimeGT-2                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'gt' for xs:dateTime.         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimeGT2() {
    final XQuery query = new XQuery(
      "not(xs:dateTime(\"2004-07-12T23:01:04.12\") gt xs:dateTime(\"2004-07-12T23:01:04.12\"))",
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
   *  Test: K-DateTimeGT-3                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'gt' for xs:dateTime.         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimeGT3() {
    final XQuery query = new XQuery(
      "not(xs:dateTime(\"2004-07-12T23:01:04.12\") gt xs:dateTime(\"2004-07-13T23:01:04.12\"))",
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
   *  Test: K-DateTimeGT-4                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ge' for xs:dateTime.         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimeGT4() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2004-07-12T23:01:04.12\") ge xs:dateTime(\"2004-07-12T23:01:04.12\")",
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
   *  Test: K-DateTimeGT-5                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ge' for xs:dateTime.         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimeGT5() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2004-07-13T23:01:04.12\") ge xs:dateTime(\"2004-07-12T23:01:04.12\")",
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
   *  Test: K-DateTimeGT-6                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ge' for xs:dateTime.         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimeGT6() {
    final XQuery query = new XQuery(
      "not(xs:dateTime(\"2004-07-11T23:01:04.12\") ge xs:dateTime(\"2004-07-12T23:01:04.12\"))",
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
   *  test comparison of dateTime .
   */
  @org.junit.Test
  public void cbclDateTimeGreaterThan001() {
    final XQuery query = new XQuery(
      "declare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string }; declare function local:dateTime($year as xs:integer, $month as xs:integer, $day as xs:integer) { let $m := local:two-digit($month), $d := local:two-digit($day) return xs:dateTime(concat($year, '-', $m, '-', $d, \"T12:00:00\")) }; not(local:dateTime(2008, 05, 12) gt xs:dateTime(\"1972-12-15T12:00:00\"))",
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
   *  test comparison of dateTime .
   */
  @org.junit.Test
  public void cbclDateTimeGreaterThan002() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2008-01-30T00:01:00\") gt xs:dateTime(\"2008-01-31T01:00:00+09:00\")",
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
   *  test comparison of dateTime .
   */
  @org.junit.Test
  public void cbclDateTimeGreaterThan003() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2008-01-31T00:01:00+09:00\") gt xs:dateTime(\"2008-01-30T00:01:00\")",
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
   *  test comparison of dateTime .
   */
  @org.junit.Test
  public void cbclDateTimeGreaterThan004() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2008-01-31T00:01:00\") gt xs:dateTime(\"2008-01-31T00:01:00+09:00\")",
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
   *  test comparison of dateTime .
   */
  @org.junit.Test
  public void cbclDateTimeGreaterThan005() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2008-01-31T00:01:00+09:00\") gt xs:dateTime(\"2008-01-31T00:01:00\")",
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
   *  test comparison of dateTime .
   */
  @org.junit.Test
  public void cbclDateTimeGreaterThan006() {
    final XQuery query = new XQuery(
      "declare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string }; declare function local:dateTime($year as xs:integer, $month as xs:integer, $day as xs:integer) { let $m := local:two-digit($month), $d := local:two-digit($day) return xs:dateTime(concat($year, '-', $m, '-', $d, \"T12:00:00\")) }; not(local:dateTime(2008, 05, 12) ge xs:dateTime(\"1972-12-15T12:00:00\"))",
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
   *  test comparison of dateTime .
   */
  @org.junit.Test
  public void cbclDateTimeGreaterThan007() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2008-01-30T00:01:00\") ge xs:dateTime(\"2008-01-31T00:01:00+09:00\")",
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
   *  test comparison of dateTime .
   */
  @org.junit.Test
  public void cbclDateTimeGreaterThan008() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2008-01-31T00:01:00+09:00\") ge xs:dateTime(\"2008-01-30T00:01:00\")",
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
   *  test comparison of dateTime .
   */
  @org.junit.Test
  public void cbclDateTimeGreaterThan009() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2008-01-31T00:01:00\") ge xs:dateTime(\"2008-01-31T00:01:00+09:00\")",
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
   *  test comparison of dateTime .
   */
  @org.junit.Test
  public void cbclDateTimeGreaterThan010() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2008-01-31T00:01:00+09:00\") ge xs:dateTime(\"2008-01-31T00:01:00\")",
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
   *  test comparison of dateTime .
   */
  @org.junit.Test
  public void cbclDateTimeGreaterThan011() {
    final XQuery query = new XQuery(
      "declare function local:dateTime($dateTime as xs:dateTime, $null as xs:boolean) { if ($null) then () else $dateTime }; exists(local:dateTime(xs:dateTime(\"1972-12-15T12:00:00\"), fn:true()) gt xs:dateTime(\"1972-12-15T12:00:00\"))",
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
   *  test comparison of dateTime .
   */
  @org.junit.Test
  public void cbclDateTimeGreaterThan012() {
    final XQuery query = new XQuery(
      "declare function local:dateTime($dateTime as xs:dateTime, $null as xs:boolean) { if ($null) then () else $dateTime }; local:dateTime(xs:dateTime(\"1972-12-15T12:00:00\"), fn:false()) gt xs:dateTime(\"1972-12-15T12:00:00\")",
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
   *  test comparison of dateTime .
   */
  @org.junit.Test
  public void cbclDateTimeGreaterThan013() {
    final XQuery query = new XQuery(
      "declare function local:dateTime($dateTime as xs:dateTime, $null as xs:boolean) { if ($null) then () else $dateTime }; exists(local:dateTime(xs:dateTime(\"1972-12-15T12:00:00\"), fn:true()) le xs:dateTime(\"1972-12-15T12:00:00\"))",
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
   *  test comparison of dateTime .
   */
  @org.junit.Test
  public void cbclDateTimeGreaterThan014() {
    final XQuery query = new XQuery(
      "declare function local:dateTime($dateTime as xs:dateTime, $null as xs:boolean) { if ($null) then () else $dateTime }; local:dateTime(xs:dateTime(\"1972-12-15T12:00:00\"), fn:false()) le xs:dateTime(\"1972-12-15T12:00:00\")",
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
   *  test comparison of dateTime .
   */
  @org.junit.Test
  public void cbclDateTimeGreaterThan015() {
    final XQuery query = new XQuery(
      "declare function local:dateTime($dateTime as xs:string, $timezone as xs:string) { xs:dateTime( concat($dateTime, $timezone) ) }; adjust-dateTime-to-timezone(local:dateTime(\"1972-12-14T00:00:00\", \"-12:00\")) gt adjust-dateTime-to-timezone(xs:dateTime(\"1972-12-15T00:00:00+12:00\"))",
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
   *  test comparison of dateTime .
   */
  @org.junit.Test
  public void cbclDateTimeGreaterThan016() {
    final XQuery query = new XQuery(
      "declare function local:dateTime($dateTime as xs:string, $timezone as xs:string) { xs:dateTime( concat($dateTime, $timezone) ) }; adjust-dateTime-to-timezone(local:dateTime(\"1972-12-14T00:00:00\", \"-12:00\")) ge adjust-dateTime-to-timezone(xs:dateTime(\"1972-12-15T00:00:00+12:00\"))",
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
   * Test: op-dateTime-greater-than-10                      
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-greater-than" function used  
   * together with "or" expression (ge operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan10() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"2002-04-03T12:00:10Z\") ge xs:dateTime(\"1990-04-02T12:10:00Z\")) or (xs:dateTime(\"1975-04-03T12:10:00Z\") ge xs:dateTime(\"2000-02-02T12:00:09Z\"))",
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
   * Test: op-dateTime-greater-than-11                      
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-greater-than" function used  
   * together with "fn:true"/or expression (gt operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan11() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1990-04-02T12:00:10Z\") gt xs:dateTime(\"2006-06-02T12:10:00Z\")) or (fn:true())",
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
   * Test: op-dateTime-greater-than-12                      
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-greater-than" function used  
   * together with "fn:true"/or expression (ge operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan12() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1970-04-02T12:00:20Z\") ge xs:dateTime(\"1980-04-02T12:00:20Z\")) or (fn:true())",
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
   * Test: op-dateTime-greater-than-13                      
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-greater-than" function used  
   * together with "fn:false"/or expression (gt operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan13() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1981-04-02T12:00:00Z\") gt xs:dateTime(\"2003-04-02T12:10:00Z\")) or (fn:false())",
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
   * Test: op-dateTime-greater-than-14                      
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-greater-than" function used  
   * together with "fn:false"/or expression (ge operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan14() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1976-04-03T12:00:00Z\") ge xs:dateTime(\"2002-07-02T12:00:30Z\")) or (fn:false())",
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
   * Test: op-dateTime-greater-than-3                       
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-greater-than" function that 
   * return true and used together with fn:not (gt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan3() {
    final XQuery query = new XQuery(
      "fn:not((xs:dateTime(\"2004-04-02T12:00:00Z\") gt xs:dateTime(\"2003-04-02T12:00:00Z\")))",
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
   * Test: op-dateTime-greater-than-4                       
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-greater-than" function that  
   * return true and used together with fn:not (ge operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan4() {
    final XQuery query = new XQuery(
      "fn:not(xs:dateTime(\"2002-04-02T12:00:00Z\") ge xs:dateTime(\"2002-04-02T12:00:00Z\"))",
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
   * Test: op-dateTime-greater-than-5                       
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-greater-than" function that  
   * return false and used together with fn:not (gt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan5() {
    final XQuery query = new XQuery(
      "fn:not(xs:dateTime(\"2002-04-02T12:00:00Z\") gt xs:dateTime(\"2002-05-02T12:00:00Z\"))",
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
   * Test: op-dateTime-greater-than-6                       
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-greater-than" function that  
   * return false and used together with fn:not(ge operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan6() {
    final XQuery query = new XQuery(
      "fn:not(xs:dateTime(\"2002-04-02T12:00:00Z\") ge xs:dateTime(\"2008-04-02T12:00:00Z\"))",
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
   * Test: op-dateTime-greater-than-7                       
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-greater-than" function used  
   * together with "and" expression (gt operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan7() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"2002-04-02T12:00:00Z\") gt xs:dateTime(\"2002-04-02T12:01:00Z\")) and (xs:dateTime(\"2003-04-02T12:00:00Z\") gt xs:dateTime(\"2002-04-02T12:00:00Z\"))",
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
   * Test: op-dateTime-greater-than-8                        
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-greater-than" function used  
   * together with "and" expression (ge operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan8() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"2002-04-02T12:00:00Z\") ge xs:dateTime(\"2005-04-02T12:00:20Z\")) and (xs:dateTime(\"2002-04-02T12:10:00Z\") ge xs:dateTime(\"2002-04-03T12:00:00Z\"))",
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
   * Test: op-dateTime-greater-than-9                       
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-greater-than" function used
   * together with "or" expression (gt operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan9() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"2002-06-02T12:00:10Z\") gt xs:dateTime(\"2000-04-04T12:00:00Z\")) or (xs:dateTime(\"2002-04-02T13:00:10Z\") gt xs:dateTime(\"2001-04-02T10:00:00Z\"))",
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
   * Test: op-dateTime-greater-than2args-1                   
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(lower bound)                       
   * $arg2 = xs:dateTime(lower bound)                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan2args1() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") gt xs:dateTime(\"1970-01-01T00:00:00Z\")",
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
   * Test: op-dateTime-greater-than2args-10                  
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(lower bound)                       
   * $arg2 = xs:dateTime(upper bound)                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan2args10() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") le xs:dateTime(\"2030-12-31T23:59:59Z\")",
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
   * Test: op-dateTime-greater-than2args-2                   
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(mid range)                         
   * $arg2 = xs:dateTime(lower bound)                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan2args2() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1996-04-07T01:40:52Z\") gt xs:dateTime(\"1970-01-01T00:00:00Z\")",
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
   * Test: op-dateTime-greater-than2args-3                   
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(upper bound)                       
   * $arg2 = xs:dateTime(lower bound)                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan2args3() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2030-12-31T23:59:59Z\") gt xs:dateTime(\"1970-01-01T00:00:00Z\")",
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
   * Test: op-dateTime-greater-than2args-4                   
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(lower bound)                       
   * $arg2 = xs:dateTime(mid range)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan2args4() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") gt xs:dateTime(\"1996-04-07T01:40:52Z\")",
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
   * Test: op-dateTime-greater-than2args-5                   
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(lower bound)                       
   * $arg2 = xs:dateTime(upper bound)                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan2args5() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") gt xs:dateTime(\"2030-12-31T23:59:59Z\")",
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
   * Test: op-dateTime-greater-than2args-6                   
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(lower bound)                       
   * $arg2 = xs:dateTime(lower bound)                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan2args6() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") le xs:dateTime(\"1970-01-01T00:00:00Z\")",
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
   * Test: op-dateTime-greater-than2args-7                   
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(mid range)                         
   * $arg2 = xs:dateTime(lower bound)                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan2args7() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1996-04-07T01:40:52Z\") le xs:dateTime(\"1970-01-01T00:00:00Z\")",
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
   * Test: op-dateTime-greater-than2args-8                   
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(upper bound)                       
   * $arg2 = xs:dateTime(lower bound)                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan2args8() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2030-12-31T23:59:59Z\") le xs:dateTime(\"1970-01-01T00:00:00Z\")",
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
   * Test: op-dateTime-greater-than2args-9                   
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(lower bound)                       
   * $arg2 = xs:dateTime(mid range)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeGreaterThan2args9() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") le xs:dateTime(\"1996-04-07T01:40:52Z\")",
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
