package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the dateTime-less-than() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpDateTimeLessThan extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DateTimeLT-1                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'lt' for xs:dateTime.         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimeLT1() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2004-07-12T23:01:04.12\") lt xs:dateTime(\"2004-07-13T23:01:04.12\")",
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
   *  Test: K-DateTimeLT-2                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'lt' for xs:dateTime.         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimeLT2() {
    final XQuery query = new XQuery(
      "not(xs:dateTime(\"2004-07-13T23:01:04.12\") lt xs:dateTime(\"2004-07-12T23:01:04.12\"))",
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
   *  Test: K-DateTimeLT-3                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'lt' for xs:dateTime.         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimeLT3() {
    final XQuery query = new XQuery(
      "not(xs:dateTime(\"2004-07-13T23:01:04.12\") lt xs:dateTime(\"2004-07-13T23:01:04.12\"))",
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
   *  Test: K-DateTimeLT-4                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'le' for xs:dateTime.         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimeLT4() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2004-07-12T23:01:04.12\") le xs:dateTime(\"2004-07-12T23:01:04.12\")",
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
   *  Test: K-DateTimeLT-5                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'le' for xs:dateTime.         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimeLT5() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2004-07-12T23:01:04.12\") le xs:dateTime(\"2004-07-12T23:01:04.12\")",
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
   *  Test: K-DateTimeLT-6                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'le' for xs:dateTime.         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimeLT6() {
    final XQuery query = new XQuery(
      "not(xs:dateTime(\"2004-07-13T23:01:04.12\") le xs:dateTime(\"2004-07-12T23:01:04.12\"))",
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
  public void cbclDateTimeLessThan001() {
    final XQuery query = new XQuery(
      "declare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string }; declare function local:dateTime($year as xs:integer, $month as xs:integer, $day as xs:integer) { let $m := local:two-digit($month), $d := local:two-digit($day) return xs:dateTime(concat($year, '-', $m, '-', $d, \"T12:00:00\")) }; not(local:dateTime(2008, 05, 12) lt xs:dateTime(\"1972-12-15T12:00:00\"))",
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
  public void cbclDateTimeLessThan002() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2008-01-30T00:01:00\") lt xs:dateTime(\"2008-01-31T01:00:00+09:00\")",
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
  public void cbclDateTimeLessThan003() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2008-01-31T00:01:00+09:00\") lt xs:dateTime(\"2008-01-30T00:01:00\")",
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
  public void cbclDateTimeLessThan004() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2008-01-31T00:01:00\") lt xs:dateTime(\"2008-01-31T00:01:00+09:00\")",
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
  public void cbclDateTimeLessThan005() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2008-01-31T00:01:00+09:00\") lt xs:dateTime(\"2008-01-31T00:01:00\")",
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
  public void cbclDateTimeLessThan006() {
    final XQuery query = new XQuery(
      "declare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string }; declare function local:dateTime($year as xs:integer, $month as xs:integer, $day as xs:integer) { let $m := local:two-digit($month), $d := local:two-digit($day) return xs:dateTime(concat($year, '-', $m, '-', $d, \"T12:00:00\")) }; not(local:dateTime(2008, 05, 12) le xs:dateTime(\"1972-12-15T12:00:00\"))",
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
  public void cbclDateTimeLessThan007() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2008-01-30T00:01:00\") le xs:dateTime(\"2008-01-31T00:01:00+09:00\")",
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
  public void cbclDateTimeLessThan008() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2008-01-31T00:01:00+09:00\") le xs:dateTime(\"2008-01-30T00:01:00\")",
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
  public void cbclDateTimeLessThan009() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2008-01-31T00:01:00\") le xs:dateTime(\"2008-01-31T00:01:00+09:00\")",
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
  public void cbclDateTimeLessThan010() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2008-01-31T00:01:00+09:00\") le xs:dateTime(\"2008-01-31T00:01:00\")",
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
  public void cbclDateTimeLessThan011() {
    final XQuery query = new XQuery(
      "declare function local:dateTime($dateTime as xs:dateTime, $null as xs:boolean) { if ($null) then () else $dateTime }; exists(local:dateTime(xs:dateTime(\"1972-12-15T12:00:00\"), fn:true()) lt xs:dateTime(\"1972-12-15T12:00:00\"))",
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
  public void cbclDateTimeLessThan012() {
    final XQuery query = new XQuery(
      "declare function local:dateTime($dateTime as xs:dateTime, $null as xs:boolean) { if ($null) then () else $dateTime }; local:dateTime(xs:dateTime(\"1972-12-15T12:00:00\"), fn:false()) lt xs:dateTime(\"1972-12-15T12:00:00\")",
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
  public void cbclDateTimeLessThan013() {
    final XQuery query = new XQuery(
      "declare function local:dateTime($dateTime as xs:dateTime, $null as xs:boolean) { if ($null) then () else $dateTime }; exists(local:dateTime(xs:dateTime(\"1972-12-15T12:00:00\"), fn:true()) ge xs:dateTime(\"1972-12-15T12:00:00\"))",
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
  public void cbclDateTimeLessThan014() {
    final XQuery query = new XQuery(
      "declare function local:dateTime($dateTime as xs:dateTime, $null as xs:boolean) { if ($null) then () else $dateTime }; local:dateTime(xs:dateTime(\"1972-12-15T12:00:00\"), fn:false()) ge xs:dateTime(\"1972-12-15T12:00:00\")",
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
  public void cbclDateTimeLessThan015() {
    final XQuery query = new XQuery(
      "declare function local:dateTime($dateTime as xs:string, $timezone as xs:string) { xs:dateTime( concat($dateTime, $timezone) ) }; adjust-dateTime-to-timezone(local:dateTime(\"1972-12-14T00:00:00\", \"-12:00\")) lt adjust-dateTime-to-timezone(xs:dateTime(\"1972-12-15T00:00:00+12:00\"))",
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
  public void cbclDateTimeLessThan016() {
    final XQuery query = new XQuery(
      "declare function local:dateTime($dateTime as xs:string, $timezone as xs:string) { xs:dateTime( concat($dateTime, $timezone) ) }; adjust-dateTime-to-timezone(local:dateTime(\"1972-12-14T00:00:00\", \"-12:00\")) le adjust-dateTime-to-timezone(xs:dateTime(\"1972-12-15T00:00:00+12:00\"))",
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
   * Test: op-dateTime-less-than-10                         
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-less-than" function used  
   * together with "or" expression (le operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan10() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"2002-04-03T12:00:10Z\") le xs:dateTime(\"1990-04-02T12:10:00Z\")) or (xs:dateTime(\"1975-04-03T12:10:00Z\") le xs:dateTime(\"2000-02-02T12:00:09Z\"))",
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
   * Test: op-dateTime-less-than-11                         
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-less-than" function used  
   * together with "fn:true"/or expression (lt operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan11() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1990-04-02T12:00:10Z\") lt xs:dateTime(\"2006-06-02T12:10:00Z\")) or (fn:true())",
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
   * Test: op-dateTime-less-than-12                         
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-less-than" function used  
   * together with "fn:true"/or expression (le operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan12() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1970-04-02T12:00:20Z\") le xs:dateTime(\"1980-04-02T12:00:20Z\")) or (fn:true())",
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
   * Test: op-dateTime-less-than-13                         
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-less-than" function used  
   * together with "fn:false"/or expression (lt operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan13() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1981-04-02T12:00:00Z\") lt xs:dateTime(\"2003-04-02T12:10:00Z\")) or (fn:false())",
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
   * Test: op-dateTime-less-than-14                         
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-less-than" function used  
   * together with "fn:false"/or expression (le operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan14() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1976-04-03T12:00:00Z\") le xs:dateTime(\"2002-07-02T12:00:30Z\")) or (fn:false())",
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
   * Test: op-dateTime-less-than-3                          
   * Written By: Carmelo Montanez                           
   * Date: June 17, 2005                                    
   * Purpose: Evaluates The "dateTime-less-than" function that  
   * return true and used together with fn:not (lt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan3() {
    final XQuery query = new XQuery(
      "fn:not((xs:dateTime(\"2002-04-02T12:00:00Z\") lt xs:dateTime(\"2003-04-02T12:00:00Z\")))",
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
   * Test: op-dateTime-less-than-4                          
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-less-than" function that  
   * return true and used together with fn:not (le operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan4() {
    final XQuery query = new XQuery(
      "fn:not(xs:dateTime(\"2002-04-02T12:00:00Z\") le xs:dateTime(\"2002-04-02T12:00:00Z\"))",
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
   * Test: op-dateTime-less-than-5                          
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-less-than" function that  
   * return false and used together with fn:not (lt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan5() {
    final XQuery query = new XQuery(
      "fn:not(xs:dateTime(\"2002-05-02T12:00:00Z\") lt xs:dateTime(\"2002-04-02T12:00:00Z\"))",
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
   * Test: op-dateTime-less-than-6                          
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-less-than" function that  
   * return false and used together with fn:not(le operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan6() {
    final XQuery query = new XQuery(
      "fn:not(xs:dateTime(\"2004-04-02T12:00:00Z\") le xs:dateTime(\"2002-04-02T12:00:00Z\"))",
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
   * Test: op-dateTime-less-than-7                          
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-less-than" function used  
   * together with "and" expression (lt operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan7() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"2002-04-02T12:00:00Z\") lt xs:dateTime(\"2002-04-02T12:01:00Z\")) and (xs:dateTime(\"2003-04-02T12:00:00Z\") lt xs:dateTime(\"2002-04-02T12:00:00Z\"))",
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
   * Test: op-dateTime-less-than-8                          
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-less-than" function used  
   * together with "and" expression (le operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan8() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"2002-04-02T12:00:00Z\") le xs:dateTime(\"2005-04-02T12:00:20Z\")) and (xs:dateTime(\"2002-04-02T12:10:00Z\") le xs:dateTime(\"2002-04-03T12:00:00Z\"))",
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
   * Test: op-dateTime-less-than-9                          
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dateTime-less-than" function used
   * together with "or" expression (lt operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan9() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"2002-06-02T12:00:10Z\") lt xs:dateTime(\"2000-04-04T12:00:00Z\")) or (xs:dateTime(\"2002-04-02T13:00:10Z\") lt xs:dateTime(\"2001-04-02T10:00:00Z\"))",
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
   * Test: op-dateTime-less-than2args-1                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(lower bound)                       
   * $arg2 = xs:dateTime(lower bound)                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan2args1() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") lt xs:dateTime(\"1970-01-01T00:00:00Z\")",
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
   * Test: op-dateTime-less-than2args-10                     
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(lower bound)                       
   * $arg2 = xs:dateTime(upper bound)                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan2args10() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") ge xs:dateTime(\"2030-12-31T23:59:59Z\")",
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
   * Test: op-dateTime-less-than2args-2                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(mid range)                         
   * $arg2 = xs:dateTime(lower bound)                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan2args2() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1996-04-07T01:40:52Z\") lt xs:dateTime(\"1970-01-01T00:00:00Z\")",
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
   * Test: op-dateTime-less-than2args-3                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(upper bound)                       
   * $arg2 = xs:dateTime(lower bound)                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan2args3() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2030-12-31T23:59:59Z\") lt xs:dateTime(\"1970-01-01T00:00:00Z\")",
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
   * Test: op-dateTime-less-than2args-4                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(lower bound)                       
   * $arg2 = xs:dateTime(mid range)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan2args4() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") lt xs:dateTime(\"1996-04-07T01:40:52Z\")",
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
   * Test: op-dateTime-less-than2args-5                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(lower bound)                       
   * $arg2 = xs:dateTime(upper bound)                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan2args5() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") lt xs:dateTime(\"2030-12-31T23:59:59Z\")",
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
   * Test: op-dateTime-less-than2args-6                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(lower bound)                       
   * $arg2 = xs:dateTime(lower bound)                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan2args6() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") ge xs:dateTime(\"1970-01-01T00:00:00Z\")",
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
   * Test: op-dateTime-less-than2args-7                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(mid range)                         
   * $arg2 = xs:dateTime(lower bound)                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan2args7() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1996-04-07T01:40:52Z\") ge xs:dateTime(\"1970-01-01T00:00:00Z\")",
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
   * Test: op-dateTime-less-than2args-8                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(upper bound)                       
   * $arg2 = xs:dateTime(lower bound)                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan2args8() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2030-12-31T23:59:59Z\") ge xs:dateTime(\"1970-01-01T00:00:00Z\")",
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
   * Test: op-dateTime-less-than2args-9                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dateTime-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dateTime(lower bound)                       
   * $arg2 = xs:dateTime(mid range)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateTimeLessThan2args9() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1970-01-01T00:00:00Z\") ge xs:dateTime(\"1996-04-07T01:40:52Z\")",
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
