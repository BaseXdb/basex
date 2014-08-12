package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the gMonthDay-equal() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpGMonthDayEqual extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-gMonthDayEQ-1                                 
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:gMonthDay, returning positive. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGMonthDayEQ1() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--04-01 \") eq xs:gMonthDay(\"--04-01\")",
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
   *  Test: K-gMonthDayEQ-2                                 
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:gMonthDay.        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGMonthDayEQ2() {
    final XQuery query = new XQuery(
      "not(xs:gMonthDay(\"--03-03\") eq xs:gMonthDay(\"--04-03\"))",
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
   *  Test: K-gMonthDayEQ-3                                 
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:gMonthDay.        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGMonthDayEQ3() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--04-12\") ne xs:gMonthDay(\"--04-11\")",
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
   *  Test: K-gMonthDayEQ-4                                 
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:gMonthDay.        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGMonthDayEQ4() {
    final XQuery query = new XQuery(
      "not(xs:gMonthDay(\"--04-11\") ne xs:gMonthDay(\"--04-11\"))",
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
   *  Test: K-gMonthDayEQ-5                                 
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to Z, in xs:gMonthDay. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGMonthDayEQ5() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--03-01-00:00\") eq xs:gMonthDay(\"--03-01Z\")",
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
   *  Test: K-gMonthDayEQ-6                                 
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset +00:00 is equal to Z, in xs:gMonthDay. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGMonthDayEQ6() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--03-01+00:00\") eq xs:gMonthDay(\"--03-01Z\")",
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
   *  Test: K-gMonthDayEQ-7                                 
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset Z is equal to Z, in xs:gMonthDay. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGMonthDayEQ7() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--03-01Z\") eq xs:gMonthDay(\"--03-01Z\")",
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
   *  Test: K-gMonthDayEQ-8                                 
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to +00:00, in xs:gMonthDay. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGMonthDayEQ8() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--03-01-00:00\") eq xs:gMonthDay(\"--03-01+00:00\")",
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
   *  test comparison of gMonthDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual001() {
    final XQuery query = new XQuery(
      "declare function local:gMonthDay($day as xs:integer) { if ($day lt 10) then xs:gMonthDay(concat(\"--12-0\", $day)) else xs:gMonthDay(concat(\"--12-\", $day)) }; not(local:gMonthDay(1) eq xs:gMonthDay(\"--12-31\"))",
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
   *  test comparison of gMonthDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual002() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT9M'))\n" +
      "            then xs:gMonthDay(\"--12-30\") eq xs:gMonthDay(\"--12-31+09:01\")\n" +
      "            else xs:gMonthDay(\"--12-30\") eq xs:gMonthDay(\"--12-31+09:00\")",
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
   *  test comparison of gMonthDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual003() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT9M'))\n" +
      "            then xs:gMonthDay(\"--12-31+09:01\") eq xs:gMonthDay(\"--12-30\")\n" +
      "            else xs:gMonthDay(\"--12-31+09:00\") eq xs:gMonthDay(\"--12-30\")",
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
   *  test comparison of gMonthDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual004() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('-PT9M'))\n" +
      "            then xs:gMonthDay(\"--12-30\") eq xs:gMonthDay(\"--12-31-09:01\")\n" +
      "            else xs:gMonthDay(\"--12-30\") eq xs:gMonthDay(\"--12-31-09:00\")",
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
   *  test comparison of gMonthDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual005() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('-PT9M'))\n" +
      "            then xs:gMonthDay(\"--12-31-09:01\") eq xs:gMonthDay(\"--12-30\")\n" +
      "            else xs:gMonthDay(\"--12-31-09:00\") eq xs:gMonthDay(\"--12-30\")",
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
   *  test comparison of gMonthDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual006() {
    final XQuery query = new XQuery(
      "declare function local:gMonthDay($day as xs:integer) { if ($day lt 10) then xs:gMonthDay(concat(\"--12-0\", $day)) else xs:gMonthDay(concat(\"--12-\", $day)) }; not(local:gMonthDay(1) ne xs:gMonthDay(\"--12-31\"))",
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
   *  test comparison of gMonthDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual007() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT9M'))\n" +
      "            then xs:gMonthDay(\"--12-30\") ne xs:gMonthDay(\"--12-31+09:01\")\n" +
      "            else xs:gMonthDay(\"--12-30\") ne xs:gMonthDay(\"--12-31+09:00\")",
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
   *  test comparison of gMonthDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual008() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT9M'))\n" +
      "            then xs:gMonthDay(\"--12-31+09:01\") ne xs:gMonthDay(\"--12-30\")\n" +
      "            else xs:gMonthDay(\"--12-31+09:00\") ne xs:gMonthDay(\"--12-30\")",
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
   *  test comparison of gMonthDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual009() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('-PT9M'))\n" +
      "            then xs:gMonthDay(\"--12-30\") ne xs:gMonthDay(\"--12-31-09:01\")\n" +
      "            else xs:gMonthDay(\"--12-30\") ne xs:gMonthDay(\"--12-31-09:00\")",
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
   *  test comparison of gMonthDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual010() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('-PT9M'))\n" +
      "            then xs:gMonthDay(\"--12-31-09:01\") ne xs:gMonthDay(\"--12-30\")\n" +
      "            else xs:gMonthDay(\"--12-31-09:00\") ne xs:gMonthDay(\"--12-30\")",
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
   *  test comparison of gMonthDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual011() {
    final XQuery query = new XQuery(
      "declare function local:gMonthDay($gMonthDay as xs:gMonthDay, $null as xs:boolean) { if ($null) then () else $gMonthDay }; exists(local:gMonthDay(xs:gMonthDay(\"--12-31\"), fn:true()) eq xs:gMonthDay(\"--12-31\"))",
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
   *  test comparison of gMonthDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual012() {
    final XQuery query = new XQuery(
      "declare function local:gMonthDay($gMonthDay as xs:gMonthDay, $null as xs:boolean) { if ($null) then () else $gMonthDay }; local:gMonthDay(xs:gMonthDay(\"--12-31\"), fn:false()) eq xs:gMonthDay(\"--12-31\")",
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
   *  test comparison of gMonthDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual013() {
    final XQuery query = new XQuery(
      "declare function local:gMonthDay($gMonthDay as xs:gMonthDay, $null as xs:boolean) { if ($null) then () else $gMonthDay }; exists(local:gMonthDay(xs:gMonthDay(\"--12-31\"), fn:true()) ne xs:gMonthDay(\"--12-31\"))",
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
   *  test comparison of gMonthDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual014() {
    final XQuery query = new XQuery(
      "declare function local:gMonthDay($gMonthDay as xs:gMonthDay, $null as xs:boolean) { if ($null) then () else $gMonthDay }; local:gMonthDay(xs:gMonthDay(\"--12-31\"), fn:false()) ne xs:gMonthDay(\"--12-31\")",
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
   *  test comparison of gDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual015() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--01-30-12:00\") eq xs:gMonthDay(\"--01-31+12:00\")",
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
   *  test comparison of gDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual016() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--01-31+12:00\") eq xs:gMonthDay(\"--01-30-12:00\")",
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
   *  test comparison of gDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual017() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--01-30-12:00\") ne xs:gMonthDay(\"--01-31+12:00\")",
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
   *  test comparison of gDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual018() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--01-31+12:00\") ne xs:gMonthDay(\"--01-30-12:00\")",
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
   *  test comparison of gMonthDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual019() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT1M'))\n" +
      "            then xs:gMonthDay(\"--12-30\") eq xs:gMonthDay(\"--12-31+01:01\")\n" +
      "            else xs:gMonthDay(\"--12-30\") eq xs:gMonthDay(\"--12-31+01:00\")",
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
   *  test comparison of gMonthDay .
   */
  @org.junit.Test
  public void cbclGMonthDayEqual020() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT1M'))\n" +
      "            then xs:gMonthDay(\"--12-31+01:01\") eq xs:gMonthDay(\"--12-30\")\n" +
      "            else xs:gMonthDay(\"--12-31+01:00\") eq xs:gMonthDay(\"--12-30\")",
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
   * Test: op-gMonthDay-equal-10                            
   * Written By: Carmelo Montanez                           
   * Date: June 22, 2005                                    
   * Purpose: Evaluates The "gMonthDay-equal" function used 
   * together with "or" expression (ne operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual10() {
    final XQuery query = new XQuery(
      "(xs:gMonthDay(\"--06-02Z\") ne xs:gMonthDay(\"--06-09Z\")) or (xs:gMonthDay(\"--08-06Z\") ne xs:gMonthDay(\"--09-01Z\"))",
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
   * Test: op-gMonthDay-equal-11                            
   * Written By: Carmelo Montanez                           
   * Date: June 22, 2005                                    
   * Purpose: Evaluates The "gMonthDay-equal" function used 
   * together with "fn:true"/or expression (eq operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual11() {
    final XQuery query = new XQuery(
      "(xs:gMonthDay(\"--03-04Z\") eq xs:gMonthDay(\"--01-07Z\")) or (fn:true())",
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
   * Test: op-gMonthDay-equal-12                            
   * Written By: Carmelo Montanez                           
   * Date: June 22, 2005                                    
   * Purpose: Evaluates The "gMonthDay-equal" function used 
   * together with "fn:true"/or expression (ne operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual12() {
    final XQuery query = new XQuery(
      "(xs:gMonthDay(\"--08-04Z\") ne xs:gMonthDay(\"--07-12Z\")) or (fn:true())",
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
   * Test: op-gMonthDay-equal-13                            
   * Written By: Carmelo Montanez                           
   * Date: June 22, 2005                                    
   * Purpose: Evaluates The "gMonthDay-equal" function used 
   * together with "fn:false"/or expression (eq operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual13() {
    final XQuery query = new XQuery(
      "(xs:gMonthDay(\"--05-05Z\") eq xs:gMonthDay(\"--05-05Z\")) or (fn:false())",
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
   * Test: op-gMonthDay-equal-14                            
   * Written By: Carmelo Montanez                           
   * Date: June 22, 2005                                    
   * Purpose: Evaluates The "gMonthDay-equal" function used 
   * together with "fn:false"/or expression (ne operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual14() {
    final XQuery query = new XQuery(
      "(xs:gMonthDay(\"--09-09Z\") ne xs:gMonthDay(\"--09-10Z\")) or (fn:false())",
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
   * Test: op-gMonthDay-equal-2                             
   * Date: June 22, 2005                                    
   * Purpose: Evaluates The "gMonthDay-equal" function      
   * As per example 2 (for this function) of the F&O  specs 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual2() {
    final XQuery query = new XQuery(
      "(xs:gMonthDay(\"--12-25Z\") eq xs:gMonthDay(\"--12-26Z\"))",
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
   * Test: op-gMonthDay-equal-3                             
   * Written By: Carmelo Montanez                           
   * Date: June 22, 2005                                    
   * Purpose: Evaluates The "gMonthDay-equal" function that 
   * return true and used together with fn:not (eq operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual3() {
    final XQuery query = new XQuery(
      "fn:not((xs:gMonthDay(\"--12-10Z\") eq xs:gMonthDay(\"--12-10Z\")))",
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
   * Test: op-gMonthDay-equal-4                             
   * Written By: Carmelo Montanez                           
   * Date: June 22, 2005                                    
   * Purpose: Evaluates The "gMonthDay-equal" function that 
   * return true and used together with fn:not (ne operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual4() {
    final XQuery query = new XQuery(
      "fn:not(xs:gMonthDay(\"--05-01Z\") ne xs:gMonthDay(\"--06-12Z\"))",
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
   * Test: op-gMonthDay-equal-5                             
   * Written By: Carmelo Montanez                           
   * Date: June 22, 2005                                    
   * Purpose: Evaluates The "gMonthDay-equal" function that 
   * return false and used together with fn:not (eq operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual5() {
    final XQuery query = new XQuery(
      "fn:not(xs:gMonthDay(\"--11-10Z\") eq xs:gMonthDay(\"--10-02Z\"))",
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
   * Test: op-gMonthDay-equal-6                             
   * Written By: Carmelo Montanez                           
   * Date: June 22, 2005                                    
   * Purpose: Evaluates The "gMonthDay-equal" function that 
   * return false and used together with fn:not(ne operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual6() {
    final XQuery query = new XQuery(
      "fn:not(xs:gMonthDay(\"--05-10Z\") ne xs:gMonthDay(\"--05-10Z\"))",
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
   * Test: op-gMonthDay-equal-7                             
   * Written By: Carmelo Montanez                           
   * Date: June 22, 2005                                    
   * Purpose: Evaluates The "gMonthDay-equal" function used 
   * together with "and" expression (eq operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual7() {
    final XQuery query = new XQuery(
      "(xs:gMonthDay(\"--04-02Z\") eq xs:gMonthDay(\"--02-03Z\")) and (xs:gMonthDay(\"--01-07Z\") eq xs:gMonthDay(\"--12-10Z\"))",
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
   * Test: op-gMonthDay-equal-8                             
   * Written By: Carmelo Montanez                           
   * Date: June 22, 2005                                    
   * Purpose: Evaluates The "gMonthDay-equal" function used 
   * together with "and" expression (ne operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual8() {
    final XQuery query = new XQuery(
      "(xs:gMonthDay(\"--12-10Z\") ne xs:gMonthDay(\"--03-11Z\")) and (xs:gMonthDay(\"--05-12Z\") ne xs:gMonthDay(\"--08-08Z\"))",
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
   * Test: op-gMonthDay-equal-9                             
   * Written By: Carmelo Montanez                           
   * Date: June 22, 2005                                    
   * Purpose: Evaluates The "gMonthDay-equal" function used 
   * together with "or" expression (eq operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual9() {
    final XQuery query = new XQuery(
      "(xs:gMonthDay(\"--02-01Z\") eq xs:gMonthDay(\"--02-10Z\")) or (xs:gMonthDay(\"--06-02Z\") eq xs:gMonthDay(\"--06-03Z\"))",
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
   * Test: op-gMonthDay-equal2args-1                         
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonthDay-equal" operator   
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonthDay(lower bound)                      
   * $arg2 = xs:gMonthDay(lower bound)                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual2args1() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--01-01Z\") eq xs:gMonthDay(\"--01-01Z\")",
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
   * Test: op-gMonthDay-equal2args-10                        
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonthDay-equal" operator   
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonthDay(lower bound)                      
   * $arg2 = xs:gMonthDay(upper bound)                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual2args10() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--01-01Z\") ne xs:gMonthDay(\"--12-31Z\")",
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
   * Test: op-gMonthDay-equal2args-2                         
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonthDay-equal" operator   
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonthDay(mid range)                        
   * $arg2 = xs:gMonthDay(lower bound)                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual2args2() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--04-10Z\") eq xs:gMonthDay(\"--01-01Z\")",
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
   * Test: op-gMonthDay-equal2args-3                         
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonthDay-equal" operator   
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonthDay(upper bound)                      
   * $arg2 = xs:gMonthDay(lower bound)                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual2args3() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--12-31Z\") eq xs:gMonthDay(\"--01-01Z\")",
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
   * Test: op-gMonthDay-equal2args-4                         
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonthDay-equal" operator   
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonthDay(lower bound)                      
   * $arg2 = xs:gMonthDay(mid range)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual2args4() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--01-01Z\") eq xs:gMonthDay(\"--04-10Z\")",
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
   * Test: op-gMonthDay-equal2args-5                         
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonthDay-equal" operator   
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonthDay(lower bound)                      
   * $arg2 = xs:gMonthDay(upper bound)                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual2args5() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--01-01Z\") eq xs:gMonthDay(\"--12-31Z\")",
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
   * Test: op-gMonthDay-equal2args-6                         
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonthDay-equal" operator   
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonthDay(lower bound)                      
   * $arg2 = xs:gMonthDay(lower bound)                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual2args6() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--01-01Z\") ne xs:gMonthDay(\"--01-01Z\")",
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
   * Test: op-gMonthDay-equal2args-7                         
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonthDay-equal" operator   
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonthDay(mid range)                        
   * $arg2 = xs:gMonthDay(lower bound)                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual2args7() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--04-10Z\") ne xs:gMonthDay(\"--01-01Z\")",
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
   * Test: op-gMonthDay-equal2args-8                         
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonthDay-equal" operator   
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonthDay(upper bound)                      
   * $arg2 = xs:gMonthDay(lower bound)                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual2args8() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--12-31Z\") ne xs:gMonthDay(\"--01-01Z\")",
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
   * Test: op-gMonthDay-equal2args-9                         
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonthDay-equal" operator   
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonthDay(lower bound)                      
   * $arg2 = xs:gMonthDay(mid range)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthDayEqual2args9() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--01-01Z\") ne xs:gMonthDay(\"--04-10Z\")",
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
