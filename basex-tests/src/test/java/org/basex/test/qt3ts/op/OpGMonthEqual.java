package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the gMonth-equal() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpGMonthEqual extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-gMonthEQ-1                                    
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:gMonth, returning positive. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGMonthEQ1() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--11 \") eq xs:gMonth(\"--11\")",
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
   *  Test: K-gMonthEQ-2                                    
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:gMonth.           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGMonthEQ2() {
    final XQuery query = new XQuery(
      "not(xs:gMonth(\"--11\") eq xs:gMonth(\"--01\"))",
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
   *  Test: K-gMonthEQ-3                                    
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:gMonth.           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGMonthEQ3() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--12\") ne xs:gMonth(\"--10\")",
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
   *  Test: K-gMonthEQ-4                                    
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:gMonth.           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGMonthEQ4() {
    final XQuery query = new XQuery(
      "not(xs:gMonth(\"--03\") ne xs:gMonth(\"--03\"))",
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
   *  Test: K-gMonthEQ-5                                    
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to Z, in xs:gMonth. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGMonthEQ5() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--01-00:00\") eq xs:gMonth(\"--01Z\")",
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
   *  Test: K-gMonthEQ-6                                    
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset +00:00 is equal to Z, in xs:gMonth. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGMonthEQ6() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--01+00:00\") eq xs:gMonth(\"--01Z\")",
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
   *  Test: K-gMonthEQ-7                                    
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset Z is equal to Z, in xs:gMonth. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGMonthEQ7() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--01Z\") eq xs:gMonth(\"--01Z\")",
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
   *  Test: K-gMonthEQ-8                                    
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to +00:00, in xs:gMonth. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGMonthEQ8() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--01-00:00\") eq xs:gMonth(\"--01+00:00\")",
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
   *  test comparison of gMonth .
   */
  @org.junit.Test
  public void cbclGMonthEqual001() {
    final XQuery query = new XQuery(
      "declare function local:gMonth($month as xs:integer) { if ($month lt 10) then xs:gMonth(concat(\"--0\", $month)) else xs:gMonth(concat(\"--\", $month)) }; not(local:gMonth(1) eq xs:gMonth(\"--06\"))",
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
   *  test comparison of gMonth .
   */
  @org.junit.Test
  public void cbclGMonthEqual002() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT9M'))\n" +
      "            then xs:gMonth(\"--06\") eq xs:gMonth(\"--06+09:01\")\n" +
      "            else xs:gMonth(\"--06\") eq xs:gMonth(\"--06+09:00\")",
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
   *  test comparison of gMonth .
   */
  @org.junit.Test
  public void cbclGMonthEqual003() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT9M'))\n" +
      "            then xs:gMonth(\"--06+09:01\") eq xs:gMonth(\"--06\")\n" +
      "            else xs:gMonth(\"--06+09:00\") eq xs:gMonth(\"--06\")",
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
   *  test comparison of gMonth .
   */
  @org.junit.Test
  public void cbclGMonthEqual004() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('-PT9M'))\n" +
      "            then xs:gMonth(\"--06\") eq xs:gMonth(\"--06-09:01\")\n" +
      "            else xs:gMonth(\"--06\") eq xs:gMonth(\"--06-09:00\")",
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
   *  test comparison of gMonth .
   */
  @org.junit.Test
  public void cbclGMonthEqual005() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('-PT9M'))\n" +
      "            then xs:gMonth(\"--06-09:01\") eq xs:gMonth(\"--06\")\n" +
      "            else xs:gMonth(\"--06-09:00\") eq xs:gMonth(\"--06\")",
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
   *  test comparison of gMonth .
   */
  @org.junit.Test
  public void cbclGMonthEqual006() {
    final XQuery query = new XQuery(
      "declare function local:gMonth($month as xs:integer) { if ($month lt 10) then xs:gMonth(concat(\"--0\", $month)) else xs:gMonth(concat(\"--\", $month)) }; not(local:gMonth(1) ne xs:gMonth(\"--06\"))",
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
   *  test comparison of gMonth .
   */
  @org.junit.Test
  public void cbclGMonthEqual007() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT9M'))\n" +
      "            then xs:gMonth(\"--06\") ne xs:gMonth(\"--06+09:01\")\n" +
      "            else xs:gMonth(\"--06\") ne xs:gMonth(\"--06+09:00\")",
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
   *  test comparison of gMonth .
   */
  @org.junit.Test
  public void cbclGMonthEqual008() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT9M'))\n" +
      "            then xs:gMonth(\"--06+09:01\") ne xs:gMonth(\"--06\")\n" +
      "            else xs:gMonth(\"--06+09:00\") ne xs:gMonth(\"--06\")",
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
   *  test comparison of gMonth .
   */
  @org.junit.Test
  public void cbclGMonthEqual009() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('-PT9M'))\n" +
      "            then xs:gMonth(\"--06\") ne xs:gMonth(\"--06-09:01\")\n" +
      "            else xs:gMonth(\"--06\") ne xs:gMonth(\"--06-09:00\")",
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
   *  test comparison of gMonth .
   */
  @org.junit.Test
  public void cbclGMonthEqual010() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('-PT9M'))\n" +
      "            then xs:gMonth(\"--06-09:01\") ne xs:gMonth(\"--06\")\n" +
      "            else xs:gMonth(\"--06-09:00\") ne xs:gMonth(\"--06\")",
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
   *  test comparison of gMonth .
   */
  @org.junit.Test
  public void cbclGMonthEqual011() {
    final XQuery query = new XQuery(
      "declare function local:gMonth($gMonth as xs:gMonth, $null as xs:boolean) { if ($null) then () else $gMonth }; exists(local:gMonth(xs:gMonth(\"--12\"), fn:true()) eq xs:gMonth(\"--12\"))",
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
   *  test comparison of gMonth .
   */
  @org.junit.Test
  public void cbclGMonthEqual012() {
    final XQuery query = new XQuery(
      "declare function local:gMonth($gMonth as xs:gMonth, $null as xs:boolean) { if ($null) then () else $gMonth }; local:gMonth(xs:gMonth(\"--12\"), fn:false()) eq xs:gMonth(\"--12\")",
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
   *  test comparison of gMonth .
   */
  @org.junit.Test
  public void cbclGMonthEqual013() {
    final XQuery query = new XQuery(
      "declare function local:gMonth($gMonth as xs:gMonth, $null as xs:boolean) { if ($null) then () else $gMonth }; exists(local:gMonth(xs:gMonth(\"--12\"), fn:true()) ne xs:gMonth(\"--12\"))",
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
   *  test comparison of gMonth .
   */
  @org.junit.Test
  public void cbclGMonthEqual014() {
    final XQuery query = new XQuery(
      "declare function local:gMonth($gMonth as xs:gMonth, $null as xs:boolean) { if ($null) then () else $gMonth }; local:gMonth(xs:gMonth(\"--12\"), fn:false()) ne xs:gMonth(\"--12\")",
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
   * Test: op-gMonth-equal-10                               
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gMonth-equal" function used    
   * together with "or" expression (ne operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual10() {
    final XQuery query = new XQuery(
      "(xs:gMonth(\"--06Z\") ne xs:gMonth(\"--06Z\")) or (xs:gMonth(\"--08Z\") ne xs:gMonth(\"--09Z\"))",
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
   * Test: op-gMonth-equal-11                               
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gMonth-equal" function used    
   * together with "fn:true"/or expression (eq operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual11() {
    final XQuery query = new XQuery(
      "(xs:gMonth(\"--03Z\") eq xs:gMonth(\"--01Z\")) or (fn:true())",
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
   * Test: op-gMonth-equal-12                               
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gMonth-equal" function used    
   * together with "fn:true"/or expression (ne operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual12() {
    final XQuery query = new XQuery(
      "(xs:gMonth(\"--08Z\") ne xs:gMonth(\"--07Z\")) or (fn:true())",
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
   * Test: op-gMonth-equal-13                               
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gMonth-equal" function used    
   * together with "fn:false"/or expression (eq operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual13() {
    final XQuery query = new XQuery(
      "(xs:gMonth(\"--05Z\") eq xs:gMonth(\"--05Z\")) or (fn:false())",
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
   * Test: op-gMonth-equal-14                               
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gMonth-equal" function used    
   * together with "fn:false"/or expression (ne operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual14() {
    final XQuery query = new XQuery(
      "(xs:gMonth(\"--09Z\") ne xs:gMonth(\"--09Z\")) or (fn:false())",
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
   * Test: op-gMonth-equal-2                                
   * Date: June 16, 2005                                    
   * Purpose: Evaluates The "gMonth-equal" function         
   * As per example 2 (for this function) of the F&O  specs 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual2() {
    final XQuery query = new XQuery(
      "(xs:gMonth(\"--12-05:00\") eq xs:gMonth(\"--12Z\"))",
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
   * Test: op-gMonth-equal-3                                
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gMonth-equal" function that    
   * return true and used together with fn:not (eq operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual3() {
    final XQuery query = new XQuery(
      "fn:not((xs:gMonth(\"--12Z\") eq xs:gMonth(\"--12Z\")))",
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
   * Test: op-gMonth-equal-4                                
   * Written By: Carmelo Montanez                           
   * Date: June 16, 2005                                    
   * Purpose: Evaluates The "gMonth-equal" function that    
   * return true and used together with fn:not (ne operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual4() {
    final XQuery query = new XQuery(
      "fn:not(xs:gMonth(\"--05Z\") ne xs:gMonth(\"--06Z\"))",
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
   * Test: op-gMonth-equal-5                                
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gMonth-equal" function that    
   * return false and used together with fn:not (eq operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual5() {
    final XQuery query = new XQuery(
      "fn:not(xs:gMonth(\"--11Z\") eq xs:gMonth(\"--10Z\"))",
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
   * Test: op-gMonth-equal-6                                
   * Written By: Carmelo Montanez                           
   * Date: June 16, 2005                                    
   * Purpose: Evaluates The "gMonth-equal" function that    
   * return false and used together with fn:not(ne operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual6() {
    final XQuery query = new XQuery(
      "fn:not(xs:gMonth(\"--05Z\") ne xs:gMonth(\"--05Z\"))",
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
   * Test: op-gMonth-equal-7                                
   * Written By: Carmelo Montanez                           
   * Date: June 16, 2005                                    
   * Purpose: Evaluates The "gMonth-equal" function used    
   * together with "and" expression (eq operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual7() {
    final XQuery query = new XQuery(
      "(xs:gMonth(\"--04Z\") eq xs:gMonth(\"--02Z\")) and (xs:gMonth(\"--01Z\") eq xs:gMonth(\"--12Z\"))",
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
   * Test: op-gMonth-equal-8                                
   * Written By: Carmelo Montanez                           
   * Date: June 16, 2005                                    
   * Purpose: Evaluates The "gMonth-equal" function used    
   * together with "and" expression (ne operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual8() {
    final XQuery query = new XQuery(
      "(xs:gMonth(\"--12Z\") ne xs:gMonth(\"--03Z\")) and (xs:gMonth(\"--05Z\") ne xs:gMonth(\"--08Z\"))",
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
   * Test: op-gMonth-equal-9                                
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gMonth-equal" function used    
   * together with "or" expression (eq operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual9() {
    final XQuery query = new XQuery(
      "(xs:gMonth(\"--02Z\") eq xs:gMonth(\"--02Z\")) or (xs:gMonth(\"--06Z\") eq xs:gMonth(\"--06Z\"))",
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
   * Test: op-gMonth-equal2args-1                            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonth-equal" operator      
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonth(lower bound)                         
   * $arg2 = xs:gMonth(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual2args1() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--01Z\") eq xs:gMonth(\"--01Z\")",
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
   * Test: op-gMonth-equal2args-10                           
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonth-equal" operator      
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonth(lower bound)                         
   * $arg2 = xs:gMonth(upper bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual2args10() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--01Z\") ne xs:gMonth(\"--12Z\")",
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
   * Test: op-gMonth-equal2args-2                            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonth-equal" operator      
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonth(mid range)                           
   * $arg2 = xs:gMonth(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual2args2() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--07Z\") eq xs:gMonth(\"--01Z\")",
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
   * Test: op-gMonth-equal2args-3                            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonth-equal" operator      
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonth(upper bound)                         
   * $arg2 = xs:gMonth(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual2args3() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--12Z\") eq xs:gMonth(\"--01Z\")",
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
   * Test: op-gMonth-equal2args-4                            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonth-equal" operator      
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonth(lower bound)                         
   * $arg2 = xs:gMonth(mid range)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual2args4() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--01Z\") eq xs:gMonth(\"--07Z\")",
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
   * Test: op-gMonth-equal2args-5                            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonth-equal" operator      
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonth(lower bound)                         
   * $arg2 = xs:gMonth(upper bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual2args5() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--01Z\") eq xs:gMonth(\"--12Z\")",
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
   * Test: op-gMonth-equal2args-6                            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonth-equal" operator      
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonth(lower bound)                         
   * $arg2 = xs:gMonth(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual2args6() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--01Z\") ne xs:gMonth(\"--01Z\")",
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
   * Test: op-gMonth-equal2args-7                            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonth-equal" operator      
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonth(mid range)                           
   * $arg2 = xs:gMonth(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual2args7() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--07Z\") ne xs:gMonth(\"--01Z\")",
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
   * Test: op-gMonth-equal2args-8                            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonth-equal" operator      
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonth(upper bound)                         
   * $arg2 = xs:gMonth(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual2args8() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--12Z\") ne xs:gMonth(\"--01Z\")",
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
   * Test: op-gMonth-equal2args-9                            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gMonth-equal" operator      
   *  with the arguments set as follows:                    
   * $arg1 = xs:gMonth(lower bound)                         
   * $arg2 = xs:gMonth(mid range)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGMonthEqual2args9() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--01Z\") ne xs:gMonth(\"--07Z\")",
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
