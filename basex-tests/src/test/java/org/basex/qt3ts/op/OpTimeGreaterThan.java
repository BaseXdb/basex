package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the time-greater-than() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpTimeGreaterThan extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-TimeGT-1                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'lt' for xs:time.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeGT1() {
    final XQuery query = new XQuery(
      "xs:time(\"23:01:03.12\") lt xs:time(\"23:01:04.12\")",
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
   *  Test: K-TimeGT-2                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'lt' for xs:time.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeGT2() {
    final XQuery query = new XQuery(
      "not(xs:time(\"23:01:04.12\") lt xs:time(\"23:01:04.12\"))",
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
   *  Test: K-TimeGT-3                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'lt' for xs:time.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeGT3() {
    final XQuery query = new XQuery(
      "not(xs:time(\"23:01:05.12\") lt xs:time(\"23:01:04.12\"))",
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
   *  Test: K-TimeGT-4                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'le' for xs:time.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeGT4() {
    final XQuery query = new XQuery(
      "xs:time(\"23:01:04.12\") le xs:time(\"23:01:04.12\")",
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
   *  Test: K-TimeGT-5                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'le' for xs:time.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeGT5() {
    final XQuery query = new XQuery(
      "xs:time(\"23:01:03.12\") le xs:time(\"23:01:04.12\")",
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
   *  Test: K-TimeGT-6                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'le' for xs:time.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeGT6() {
    final XQuery query = new XQuery(
      "not(xs:time(\"23:01:05.12\") le xs:time(\"23:01:04.12\"))",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeGreaterThan001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string };\n" +
      "        declare function local:time($hours as xs:integer, $mins as xs:integer, $seconds as xs:decimal) { let $h := local:two-digit($hours), $m := local:two-digit($mins) return xs:time(concat($h, ':', $m, ':', $seconds)) };\n" +
      "        not(local:time(12, 59, 30) gt xs:time(\"12:32:05\"))",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeGreaterThan002() {
    final XQuery query = new XQuery(
      "xs:time(\"14:00:00-12:00\") gt xs:time(\"02:00:00\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeGreaterThan003() {
    final XQuery query = new XQuery(
      "xs:time(\"02:00:00\") gt xs:time(\"14:00:00-12:00\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeGreaterThan004() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() gt xs:dayTimeDuration('PT1H'))\n" +
      "            then xs:time(\"00:00:00\") gt xs:time(\"00:00:00+01:00\")\n" +
      "            else xs:time(\"00:00:00+01:01\") gt xs:time(\"00:00:00\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeGreaterThan005() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() gt xs:dayTimeDuration('PT1H'))\n" +
      "            then xs:time(\"00:00:01+01:00\") gt xs:time(\"00:00:00\")\n" +
      "            else xs:time(\"00:00:00\") gt xs:time(\"00:00:00+01:01\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeGreaterThan006() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string };\n" +
      "        declare function local:time($hours as xs:integer, $mins as xs:integer, $seconds as xs:decimal) { let $h := local:two-digit($hours), $m := local:two-digit($mins) return xs:time(concat($h, ':', $m, ':', $seconds)) };\n" +
      "        not(local:time(12, 59, 30) le xs:time(\"12:32:05\"))",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeGreaterThan007() {
    final XQuery query = new XQuery(
      "xs:time(\"14:00:00-12:00\") le xs:time(\"02:00:00\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeGreaterThan008() {
    final XQuery query = new XQuery(
      "xs:time(\"02:00:00\") le xs:time(\"14:00:00-12:00\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeGreaterThan009() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() gt xs:dayTimeDuration('PT1H'))\n" +
      "            then xs:time(\"00:00:00\") le xs:time(\"00:00:00+01:00\")\n" +
      "            else xs:time(\"00:00:00+01:01\") le xs:time(\"00:00:00\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeGreaterThan010() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() gt xs:dayTimeDuration('PT1H'))\n" +
      "            then xs:time(\"00:00:01+01:00\") le xs:time(\"00:00:00\")\n" +
      "            else xs:time(\"00:00:00\") le xs:time(\"00:00:00+01:01\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeGreaterThan011() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:time($time as xs:time, $null as xs:boolean) { if ($null) then () else $time };\n" +
      "        exists(local:time(xs:time(\"23:58:00\"), fn:true()) gt xs:time(\"23:58:00\"))",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeGreaterThan012() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:time($time as xs:time, $null as xs:boolean) { if ($null) then () else $time };\n" +
      "        local:time(xs:time(\"23:58:00\"), fn:false()) gt xs:time(\"23:58:00\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeGreaterThan013() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:time($time as xs:time, $null as xs:boolean) { if ($null) then () else $time };\n" +
      "        exists(local:time(xs:time(\"23:58:00\"), fn:true()) le xs:time(\"23:58:00\"))",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeGreaterThan014() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:time($time as xs:time, $null as xs:boolean) { if ($null) then () else $time };\n" +
      "        local:time(xs:time(\"23:58:00\"), fn:false()) le xs:time(\"23:58:00\")",
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
   * Test: op-time-greater-than-1                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-greater-than" function    
   * As per example 1 (for this function)of the F&O specs   
   * (gt operator).                                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan1() {
    final XQuery query = new XQuery(
      "(xs:time(\"08:00:00+09:00\") gt xs:time(\"17:00:00-06:00\"))",
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
   * Test: op-time-greater-than-10                          
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-greater-than" function used
   * together with "or" expression (ge operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan10() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") ge xs:time(\"17:00:00Z\")) or (xs:time(\"13:00:00Z\") ge xs:time(\"17:00:00Z\"))",
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
   * Test: op-time-greater-than-11                          
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-greater-than" function used
   * together with "fn:true"/or expression (gt operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan11() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") gt xs:time(\"17:00:00Z\")) or (fn:true())",
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
   * Test: op-time-greater-than-12                          
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-greater-than" function used
   * together with "fn:true"/or expression (ge operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan12() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") ge xs:time(\"17:00:00Z\")) or (fn:true())",
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
   * Test: op-time-greater-than-13                          
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-greater-than" function used
   * together with "fn:false"/or expression (gt operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan13() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") gt xs:time(\"17:00:00Z\")) or (fn:false())",
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
   * Test: op-time-greater-than-14                          
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-greater-than" function used
   * together with "fn:false"/or expression (ge operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan14() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") ge xs:time(\"17:00:00Z\")) or (fn:false())",
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
   * Test:  op-time-greater-than-2                          
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-greater-than" function    
   * As per example 1 (for this function)of the F&O specs   
   * (ge operator).                                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan2() {
    final XQuery query = new XQuery(
      "(xs:time(\"08:00:00+09:00\") ge xs:time(\"17:00:00-06:00\"))",
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
   * Test: op-time-greater-than-3                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-greater-than" function that
   * return true and used together with fn:not (gt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan3() {
    final XQuery query = new XQuery(
      "fn:not((xs:time(\"14:00:00Z\") gt xs:time(\"13:00:00Z\")))",
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
   * Test: op-time-greater-than-4                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-greater-than" function that
   * return true and used together with fn:not (ge operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan4() {
    final XQuery query = new XQuery(
      "fn:not(xs:time(\"13:00:00Z\") ge xs:time(\"10:00:00Z\"))",
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
   * Test: op-time-greater-than-5                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-greater-than" function that
   * return false and used together with fn:not (gt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan5() {
    final XQuery query = new XQuery(
      "fn:not(xs:time(\"13:00:00Z\") gt xs:time(\"14:00:00Z\"))",
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
   * Test: op-time-greater-than-6                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-greater-than" function that
   * return false and used together with fn:not(ge operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan6() {
    final XQuery query = new XQuery(
      "fn:not(xs:time(\"13:00:00Z\") ge xs:time(\"17:00:00Z\"))",
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
   * Test: op-time-greater-than-7                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-greater-than" function used
   * together with "and" expression (gt operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan7() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") gt xs:time(\"17:00:00Z\")) and (xs:time(\"13:00:00Z\") gt xs:time(\"17:00:00Z\"))",
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
   * Test: op-time-greater-than-8                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-greater-than" function used
   * together with "and" expression (ge operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan8() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") ge xs:time(\"17:00:00Z\")) and (xs:time(\"13:00:00Z\") ge xs:time(\"17:00:00Z\"))",
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
   * Test: op-time-greater-than-9                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-greater-than" function used
   * together with "or" expression (gt operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan9() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") gt xs:time(\"17:00:00Z\")) or (xs:time(\"13:00:00Z\") gt xs:time(\"17:00:00Z\"))",
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
   * Test: op-time-greater-than2args-1                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan2args1() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") gt xs:time(\"00:00:00Z\")",
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
   * Test: op-time-greater-than2args-10                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan2args10() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") le xs:time(\"23:59:59Z\")",
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
   * Test: op-time-greater-than2args-2                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(mid range)                             
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan2args2() {
    final XQuery query = new XQuery(
      "xs:time(\"08:03:35Z\") gt xs:time(\"00:00:00Z\")",
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
   * Test: op-time-greater-than2args-3                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(upper bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan2args3() {
    final XQuery query = new XQuery(
      "xs:time(\"23:59:59Z\") gt xs:time(\"00:00:00Z\")",
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
   * Test: op-time-greater-than2args-4                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan2args4() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") gt xs:time(\"08:03:35Z\")",
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
   * Test: op-time-greater-than2args-5                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan2args5() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") gt xs:time(\"23:59:59Z\")",
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
   * Test: op-time-greater-than2args-6                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan2args6() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") le xs:time(\"00:00:00Z\")",
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
   * Test: op-time-greater-than2args-7                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(mid range)                             
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan2args7() {
    final XQuery query = new XQuery(
      "xs:time(\"08:03:35Z\") le xs:time(\"00:00:00Z\")",
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
   * Test: op-time-greater-than2args-8                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(upper bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan2args8() {
    final XQuery query = new XQuery(
      "xs:time(\"23:59:59Z\") le xs:time(\"00:00:00Z\")",
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
   * Test: op-time-greater-than2args-9                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-greater-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeGreaterThan2args9() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") le xs:time(\"08:03:35Z\")",
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
