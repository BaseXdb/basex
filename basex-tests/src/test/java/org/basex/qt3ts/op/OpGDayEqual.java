package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the gDay-equal() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpGDayEqual extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-gDayEQ-1                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:gDay, returning positive. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGDayEQ1() {
    final XQuery query = new XQuery(
      "xs:gDay(\" ---31 \") eq xs:gDay(\"---31\")",
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
   *  Test: K-gDayEQ-2                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:gDay.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGDayEQ2() {
    final XQuery query = new XQuery(
      "not(xs:gDay(\"---31\") eq xs:gDay(\"---01\"))",
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
   *  Test: K-gDayEQ-3                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:gDay.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGDayEQ3() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01\") ne xs:gDay(\"---10\")",
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
   *  Test: K-gDayEQ-4                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:gDay.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGDayEQ4() {
    final XQuery query = new XQuery(
      "not(xs:gDay(\"---01\") ne xs:gDay(\"---01\"))",
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
   *  Test: K-gDayEQ-5                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to Z, in xs:gDay. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGDayEQ5() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01-00:00\") eq xs:gDay(\"---01Z\")",
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
   *  Test: K-gDayEQ-6                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset +00:00 is equal to Z, in xs:gDay. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGDayEQ6() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01+00:00\") eq xs:gDay(\"---01Z\")",
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
   *  Test: K-gDayEQ-7                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset Z is equal to Z, in xs:gDay. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGDayEQ7() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01Z\") eq xs:gDay(\"---01Z\")",
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
   *  Test: K-gDayEQ-8                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to +00:00, in xs:gDay. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGDayEQ8() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01-00:00\") eq xs:gDay(\"---01+00:00\")",
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
  public void cbclGDayEqual001() {
    final XQuery query = new XQuery(
      "declare function local:gDay($day as xs:integer) { if ($day lt 10) then xs:gDay(concat(\"---0\", $day)) else xs:gDay(concat(\"---\", $day)) }; not(local:gDay(1) eq xs:gDay(\"---31\"))",
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
  public void cbclGDayEqual002() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT9M'))\n" +
      "            then xs:gDay(\"---30\") eq xs:gDay(\"---31+09:01\")\n" +
      "            else xs:gDay(\"---30\") eq xs:gDay(\"---31+09:00\")",
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
  public void cbclGDayEqual003() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT9M'))\n" +
      "            then xs:gDay(\"---31+09:01\") eq xs:gDay(\"---30\")\n" +
      "            else xs:gDay(\"---31+09:00\") eq xs:gDay(\"---30\")",
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
  public void cbclGDayEqual004() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('-PT9M'))\n" +
      "            then xs:gDay(\"---30\") eq xs:gDay(\"---31-09:01\")\n" +
      "            else xs:gDay(\"---30\") eq xs:gDay(\"---31-09:00\")",
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
  public void cbclGDayEqual005() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('-PT9M'))\n" +
      "            then xs:gDay(\"---31-09:01\") eq xs:gDay(\"---30\")\n" +
      "            else xs:gDay(\"---31-09:00\") eq xs:gDay(\"---30\")",
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
  public void cbclGDayEqual006() {
    final XQuery query = new XQuery(
      "declare function local:gDay($day as xs:integer) { if ($day lt 10) then xs:gDay(concat(\"---0\", $day)) else xs:gDay(concat(\"---\", $day)) }; not(local:gDay(1) ne xs:gDay(\"---31\"))",
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
  public void cbclGDayEqual007() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT9M'))\n" +
      "            then xs:gDay(\"---30\") ne xs:gDay(\"---31+09:01\")\n" +
      "            else xs:gDay(\"---30\") ne xs:gDay(\"---31+09:00\")",
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
  public void cbclGDayEqual008() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT9M'))\n" +
      "            then xs:gDay(\"---31+09:01\") ne xs:gDay(\"---30\")\n" +
      "            else xs:gDay(\"---31+09:00\") ne xs:gDay(\"---30\")",
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
  public void cbclGDayEqual009() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('-PT9M'))\n" +
      "            then xs:gDay(\"---30\") ne xs:gDay(\"---31-09:01\")\n" +
      "            else xs:gDay(\"---30\") ne xs:gDay(\"---31-09:00\")",
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
  public void cbclGDayEqual010() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('-PT9M'))\n" +
      "            then xs:gDay(\"---31-09:01\") ne xs:gDay(\"---30\")\n" +
      "            else xs:gDay(\"---31-09:00\") ne xs:gDay(\"---30\")",
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
  public void cbclGDayEqual011() {
    final XQuery query = new XQuery(
      "declare function local:gDay($gDay as xs:gDay, $null as xs:boolean) { if ($null) then () else $gDay }; exists(local:gDay(xs:gDay(\"---31\"), fn:true()) eq xs:gDay(\"---31\"))",
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
  public void cbclGDayEqual012() {
    final XQuery query = new XQuery(
      "declare function local:gDay($gDay as xs:gDay, $null as xs:boolean) { if ($null) then () else $gDay }; local:gDay(xs:gDay(\"---31\"), fn:false()) eq xs:gDay(\"---31\")",
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
  public void cbclGDayEqual013() {
    final XQuery query = new XQuery(
      "declare function local:gDay($gDay as xs:gDay, $null as xs:boolean) { if ($null) then () else $gDay }; exists(local:gDay(xs:gDay(\"---31\"), fn:true()) ne xs:gDay(\"---31\"))",
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
  public void cbclGDayEqual014() {
    final XQuery query = new XQuery(
      "declare function local:gDay($gDay as xs:gDay, $null as xs:boolean) { if ($null) then () else $gDay }; local:gDay(xs:gDay(\"---31\"), fn:false()) ne xs:gDay(\"---31\")",
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
  public void cbclGDayEqual015() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---30-12:00\") eq xs:gDay(\"---31+12:00\")",
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
  public void cbclGDayEqual016() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31+12:00\") eq xs:gDay(\"---30-12:00\")",
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
  public void cbclGDayEqual017() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---30-12:00\") ne xs:gDay(\"---31+12:00\")",
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
  public void cbclGDayEqual018() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31+12:00\") ne xs:gDay(\"---30-12:00\")",
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
  public void cbclGDayEqual019() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT1H'))\n" +
      "            then xs:gDay(\"---31+01:01\") eq xs:gDay(\"---31\")\n" +
      "            else xs:gDay(\"---31+01:00\") eq xs:gDay(\"---31\")",
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
  public void cbclGDayEqual020() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT1H'))\n" +
      "            then xs:gDay(\"---31\") eq xs:gDay(\"---31+01:01\")\n" +
      "            else xs:gDay(\"---31\") eq xs:gDay(\"---31+01:00\")",
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
   * Test: op-gDay-equal-10                                 
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function used      
   * together with "or" expression (ne operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual10() {
    final XQuery query = new XQuery(
      "(xs:gDay(\"---06Z\") ne xs:gDay(\"---06Z\")) or (xs:gDay(\"---08Z\") ne xs:gDay(\"---09Z\"))",
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
   * Test: op-gDay-equal-11                                 
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function used      
   * together with "fn:true"/or expression (eq operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual11() {
    final XQuery query = new XQuery(
      "(xs:gDay(\"---03Z\") eq xs:gDay(\"---01Z\")) or (fn:true())",
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
   * Test: op-gDay-equal-12                                 
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function used      
   * together with "fn:true"/or expression (ne operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual12() {
    final XQuery query = new XQuery(
      "(xs:gDay(\"---08Z\") ne xs:gDay(\"---07Z\")) or (fn:true())",
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
   * Test: op-gDay-equal-13                                 
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function used      
   * together with "fn:false"/or expression (eq operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual13() {
    final XQuery query = new XQuery(
      "(xs:gDay(\"---05Z\") eq xs:gDay(\"---05Z\")) or (fn:false())",
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
   * Test: op-gDay-equal-14                                 
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function used      
   * together with "fn:false"/or expression (ne operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual14() {
    final XQuery query = new XQuery(
      "(xs:gDay(\"---09Z\") ne xs:gDay(\"---09Z\")) or (fn:false())",
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
   * Test: op-gDay-equal-2                                  
   * Written By: Carmelo Montanez                           
   * Date: June 16, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function           
   * As per example 2 (for this function) of the F&O  specs 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2() {
    final XQuery query = new XQuery(
      "(xs:gDay(\"---12-05:00\") eq xs:gDay(\"---12Z\"))",
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
   * Test: op-gDay-equal-3                                  
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function that      
   * return true and used together with fn:not (eq operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual3() {
    final XQuery query = new XQuery(
      "fn:not((xs:gDay(\"---12Z\") eq xs:gDay(\"---12Z\")))",
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
   * Test: op-gDay-equal-4                                  
   * Written By: Carmelo Montanez                           
   * Date: June 16, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function that      
   * return true and used together with fn:not (ne operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual4() {
    final XQuery query = new XQuery(
      "fn:not(xs:gDay(\"---05Z\") ne xs:gDay(\"---06Z\"))",
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
   * Test: op-gDay-equal-5                                  
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function that      
   * return false and used together with fn:not (eq operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual5() {
    final XQuery query = new XQuery(
      "fn:not(xs:gDay(\"---11Z\") eq xs:gDay(\"---10Z\"))",
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
   * Test: op-gDay-equal-6                                  
   * Written By: Carmelo Montanez                           
   * Date: June 16, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function that      
   * return false and used together with fn:not(ne operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual6() {
    final XQuery query = new XQuery(
      "fn:not(xs:gDay(\"---05Z\") ne xs:gDay(\"---05Z\"))",
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
   * Test: op-gDay-equal-7                                  
   * Written By: Carmelo Montanez                           
   * Date: June 16, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function used      
   * together with "and" expression (eq operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual7() {
    final XQuery query = new XQuery(
      "(xs:gDay(\"---04Z\") eq xs:gDay(\"---02Z\")) and (xs:gDay(\"---01Z\") eq xs:gDay(\"---12Z\"))",
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
   * Test: op-gDay-equal-8                                  
   * Written By: Carmelo Montanez                           
   * Date: June 16, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function used      
   * together with "and" expression (ne operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual8() {
    final XQuery query = new XQuery(
      "(xs:gDay(\"---12Z\") ne xs:gDay(\"---03Z\")) and (xs:gDay(\"---05Z\") ne xs:gDay(\"---08Z\"))",
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
   * Test: op-gDay-equal-9                                  
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function used      
   * together with "or" expression (eq operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual9() {
    final XQuery query = new XQuery(
      "(xs:gDay(\"---02Z\") eq xs:gDay(\"---02Z\")) or (xs:gDay(\"---06Z\") eq xs:gDay(\"---06Z\"))",
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
   * Test: op-gDay-equal2args-1                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(lower bound)                           
   * $arg2 = xs:gDay(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args1() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01Z\") eq xs:gDay(\"---01Z\")",
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
   * Test: op-gDay-equal2args-10                             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(lower bound)                           
   * $arg2 = xs:gDay(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args10() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01Z\") ne xs:gDay(\"---31Z\")",
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
   * Test: op-gDay-equal2args-2                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(mid range)                             
   * $arg2 = xs:gDay(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args2() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---14Z\") eq xs:gDay(\"---01Z\")",
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
   * Test: op-gDay-equal2args-3                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(upper bound)                           
   * $arg2 = xs:gDay(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args3() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31Z\") eq xs:gDay(\"---01Z\")",
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
   * Test: op-gDay-equal2args-4                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(lower bound)                           
   * $arg2 = xs:gDay(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args4() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01Z\") eq xs:gDay(\"---14Z\")",
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
   * Test: op-gDay-equal2args-5                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(lower bound)                           
   * $arg2 = xs:gDay(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args5() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01Z\") eq xs:gDay(\"---31Z\")",
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
   * Test: op-gDay-equal2args-6                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(lower bound)                           
   * $arg2 = xs:gDay(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args6() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01Z\") ne xs:gDay(\"---01Z\")",
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
   * Test: op-gDay-equal2args-7                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(mid range)                             
   * $arg2 = xs:gDay(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args7() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---14Z\") ne xs:gDay(\"---01Z\")",
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
   * Test: op-gDay-equal2args-8                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(upper bound)                           
   * $arg2 = xs:gDay(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args8() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31Z\") ne xs:gDay(\"---01Z\")",
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
   * Test: op-gDay-equal2args-9                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(lower bound)                           
   * $arg2 = xs:gDay(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args9() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01Z\") ne xs:gDay(\"---14Z\")",
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
