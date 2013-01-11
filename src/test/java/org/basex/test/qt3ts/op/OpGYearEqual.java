package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the gYear-equal() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpGYearEqual extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-gYearEQ-1                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:gYear, returning positive. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGYearEQ1() {
    final XQuery query = new XQuery(
      "xs:gYear(\" 1956 \") eq xs:gYear(\"1956\")",
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
   *  Test: K-gYearEQ-2                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:gYear.            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGYearEQ2() {
    final XQuery query = new XQuery(
      "not(xs:gYear(\"1956\") eq xs:gYear(\"1958\"))",
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
   *  Test: K-gYearEQ-3                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:gYear.            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGYearEQ3() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1956\") ne xs:gYear(\"1958\")",
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
   *  Test: K-gYearEQ-4                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:gYear.            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGYearEQ4() {
    final XQuery query = new XQuery(
      "not(xs:gYear(\"1956\") ne xs:gYear(\"1956\"))",
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
   *  Test: K-gYearEQ-5                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to Z, in xs:gYear. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGYearEQ5() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1956-00:00\") eq xs:gYear(\"1956Z\")",
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
   *  Test: K-gYearEQ-6                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset +00:00 is equal to Z, in xs:gYear. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGYearEQ6() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1956+00:00\") eq xs:gYear(\"1956Z\")",
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
   *  Test: K-gYearEQ-7                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset Z is equal to Z, in xs:gYear. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGYearEQ7() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1956Z\") eq xs:gYear(\"1956Z\")",
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
   *  Test: K-gYearEQ-8                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to +00:00, in xs:gYear. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGYearEQ8() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1956-00:00\") eq xs:gYear(\"1956+00:00\")",
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
   *  test comparison of gYear .
   */
  @org.junit.Test
  public void cbclGYearEqual001() {
    final XQuery query = new XQuery(
      "declare function local:gYear($year as xs:integer) { xs:gYear(string(2000 + $year)) }; not(local:gYear(7) eq xs:gYear(\"2008\"))",
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
   *  test comparison of gYear .
   */
  @org.junit.Test
  public void cbclGYearEqual002() {
    final XQuery query = new XQuery(
      "xs:gYear(\"2008\") eq xs:gYear(\"2008+09:00\")",
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
   *  test comparison of gYear .
   */
  @org.junit.Test
  public void cbclGYearEqual003() {
    final XQuery query = new XQuery(
      "xs:gYear(\"2008+09:00\") eq xs:gYear(\"2008\")",
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
   *  test comparison of gYear .
   */
  @org.junit.Test
  public void cbclGYearEqual004() {
    final XQuery query = new XQuery(
      "xs:gYear(\"2008\") eq xs:gYear(\"2008+09:00\")",
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
   *  test comparison of gYear .
   */
  @org.junit.Test
  public void cbclGYearEqual005() {
    final XQuery query = new XQuery(
      "xs:gYear(\"2008+09:00\") eq xs:gYear(\"2008\")",
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
   *  test comparison of gYear .
   */
  @org.junit.Test
  public void cbclGYearEqual006() {
    final XQuery query = new XQuery(
      "declare function local:gYear($year as xs:integer) { xs:gYear(string(2000 + $year)) }; not(local:gYear(7) ne xs:gYear(\"2008\"))",
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
   *  test comparison of gYear .
   */
  @org.junit.Test
  public void cbclGYearEqual007() {
    final XQuery query = new XQuery(
      "xs:gYear(\"2008\") ne xs:gYear(\"2008+09:00\")",
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
   *  test comparison of gYear .
   */
  @org.junit.Test
  public void cbclGYearEqual008() {
    final XQuery query = new XQuery(
      "xs:gYear(\"2008+09:00\") ne xs:gYear(\"2008\")",
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
   *  test comparison of gYear .
   */
  @org.junit.Test
  public void cbclGYearEqual009() {
    final XQuery query = new XQuery(
      "xs:gYear(\"2008\") ne xs:gYear(\"2008+09:00\")",
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
   *  test comparison of gYear .
   */
  @org.junit.Test
  public void cbclGYearEqual010() {
    final XQuery query = new XQuery(
      "xs:gYear(\"2008+09:00\") ne xs:gYear(\"2008\")",
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
   *  test comparison of gYear .
   */
  @org.junit.Test
  public void cbclGYearEqual011() {
    final XQuery query = new XQuery(
      "declare function local:gYear($gYear as xs:gYear, $null as xs:boolean) { if ($null) then () else $gYear }; exists(local:gYear(xs:gYear(\"1972\"), fn:true()) eq xs:gYear(\"1972\"))",
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
   *  test comparison of gYear .
   */
  @org.junit.Test
  public void cbclGYearEqual012() {
    final XQuery query = new XQuery(
      "declare function local:gYear($gYear as xs:gYear, $null as xs:boolean) { if ($null) then () else $gYear }; local:gYear(xs:gYear(\"1972\"), fn:false()) ne xs:gYear(\"1972\")",
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
   *  test comparison of gYear .
   */
  @org.junit.Test
  public void cbclGYearEqual013() {
    final XQuery query = new XQuery(
      "declare function local:gYear($gYear as xs:gYear, $null as xs:boolean) { if ($null) then () else $gYear }; exists(local:gYear(xs:gYear(\"1972\"), fn:true()) ne xs:gYear(\"1972\"))",
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
   *  test comparison of gYear .
   */
  @org.junit.Test
  public void cbclGYearEqual014() {
    final XQuery query = new XQuery(
      "declare function local:gYear($gYear as xs:gYear, $null as xs:boolean) { if ($null) then () else $gYear }; local:gYear(xs:gYear(\"1972\"), fn:false()) ne xs:gYear(\"1972\")",
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
   * Test: op-gYear-equal-1                                 
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYear-equal" function          
   * As per example 1 (for this function)of the F&O specs   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqual1() {
    final XQuery query = new XQuery(
      "(xs:gYear(\"2005-12:00\") eq xs:gYear(\"2005+12:00\"))",
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
   * Test: op-gYear-equal-10                                
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYear-equal" function used     
   * together with "or" expression (ne operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqual10() {
    final XQuery query = new XQuery(
      "(xs:gYear(\"1976Z\") ne xs:gYear(\"1976Z\")) or (xs:gYear(\"1980Z\") ne xs:gYear(\"1980Z\"))",
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
   * Test: op-gYear-equal-11                                
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYear-equal" function used     
   * together with "fn:true"/or expression (eq operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqual11() {
    final XQuery query = new XQuery(
      "(xs:gYear(\"1980Z\") eq xs:gYear(\"1980Z\")) or (fn:true())",
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
   * Test: op-gYear-equal-13                                
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYear-equal" function used     
   * together with "fn:false"/or expression (eq operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqual13() {
    final XQuery query = new XQuery(
      "(xs:gYear(\"1980Z\") eq xs:gYear(\"1980Z\")) or (fn:false())",
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
   * Test: op-gYear-equal-14                                
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYear-equal" function used     
   * together with "fn:false"/or expression (ne operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqual14() {
    final XQuery query = new XQuery(
      "(xs:gYear(\"1980Z\") ne xs:gYear(\"1980Z\")) or (fn:false())",
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
   * Test: op-gYear-equal-2                                 
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYear-equal" function          
   * As per example 2 (for this function) of the F&O  specs 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqual2() {
    final XQuery query = new XQuery(
      "(xs:gYear(\"1976-05:00\") eq xs:gYear(\"1976-05:00\"))",
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
   * Test: op-gYear-equal-5                                 
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYear-equal" function that     
   * return false and used together with fn:not (eq operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqual5() {
    final XQuery query = new XQuery(
      "fn:not(xs:gYear(\"2000Z\") eq xs:gYear(\"2001Z\"))",
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
   * Test: op-gYear-equal-8                                 
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYear-equal" function used     
   * together with "and" expression (ne operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqual8() {
    final XQuery query = new XQuery(
      "(xs:gYear(\"2000Z\") ne xs:gYear(\"2000Z\")) and (xs:gYear(\"1975Z\") ne xs:gYear(\"1975Z\"))",
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
   * Test: op-gYear-equal2args-1                             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYear-equal" operator       
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYear(lower bound)                          
   * $arg2 = xs:gYear(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqual2args1() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1970Z\") eq xs:gYear(\"1970Z\")",
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
   * Test: op-gYear-equal2args-10                            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYear-equal" operator       
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYear(lower bound)                          
   * $arg2 = xs:gYear(upper bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqual2args10() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1970Z\") ne xs:gYear(\"2030Z\")",
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
   * Test: op-gYear-equal2args-2                             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYear-equal" operator       
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYear(mid range)                            
   * $arg2 = xs:gYear(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqual2args2() {
    final XQuery query = new XQuery(
      "xs:gYear(\"2012Z\") eq xs:gYear(\"1970Z\")",
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
   * Test: op-gYear-equal2args-3                             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYear-equal" operator       
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYear(upper bound)                          
   * $arg2 = xs:gYear(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqual2args3() {
    final XQuery query = new XQuery(
      "xs:gYear(\"2030Z\") eq xs:gYear(\"1970Z\")",
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
   * Test: op-gYear-equal2args-4                             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYear-equal" operator       
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYear(lower bound)                          
   * $arg2 = xs:gYear(mid range)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqual2args4() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1970Z\") eq xs:gYear(\"2012Z\")",
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
   * Test: op-gYear-equal2args-5                             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYear-equal" operator       
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYear(lower bound)                          
   * $arg2 = xs:gYear(upper bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqual2args5() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1970Z\") eq xs:gYear(\"2030Z\")",
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
   * Test: op-gYear-equal2args-6                             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYear-equal" operator       
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYear(lower bound)                          
   * $arg2 = xs:gYear(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqual2args6() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1970Z\") ne xs:gYear(\"1970Z\")",
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
   * Test: op-gYear-equal2args-7                             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYear-equal" operator       
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYear(mid range)                            
   * $arg2 = xs:gYear(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqual2args7() {
    final XQuery query = new XQuery(
      "xs:gYear(\"2012Z\") ne xs:gYear(\"1970Z\")",
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
   * Test: op-gYear-equal2args-8                             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYear-equal" operator       
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYear(upper bound)                          
   * $arg2 = xs:gYear(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqual2args8() {
    final XQuery query = new XQuery(
      "xs:gYear(\"2030Z\") ne xs:gYear(\"1970Z\")",
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
   * Test: op-gYear-equal2args-9                             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYear-equal" operator       
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYear(lower bound)                          
   * $arg2 = xs:gYear(mid range)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqual2args9() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1970Z\") ne xs:gYear(\"2012Z\")",
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
   * Test: op-gYear-equal-12                                
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYear-equal" function used     
   * together with "fn:true"/or expression (ne operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqualNew12() {
    final XQuery query = new XQuery(
      "(xs:gYear(\"2000Z\") ne xs:gYear(\"2000Z\")) or (fn:true())",
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
   * Test: op-gYear-equal-3                                 
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYear-equal" function that     
   * return true and used together with fn:not (eq operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqualNew3() {
    final XQuery query = new XQuery(
      "fn:not((xs:gYear(\"1995Z\") eq xs:gYear(\"1995Z\")))",
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
   * Test: op-gYear-equal-4                                 
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYear-equal" function that     
   * return true and used together with fn:not (ne operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqualNew4() {
    final XQuery query = new XQuery(
      "fn:not(xs:gYear(\"2005Z\") ne xs:gYear(\"2006Z\"))",
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
   * Test: op-gYear-equal-6                                 
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYear-equal" function that     
   * return false and used together with fn:not(ne operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqualNew6() {
    final XQuery query = new XQuery(
      "fn:not(xs:gYear(\"2005Z\") ne xs:gYear(\"2005Z\"))",
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
   * Test: op-gYear-equal-7                                 
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYear-equal" function used     
   * together with "and" expression (eq operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqualNew7() {
    final XQuery query = new XQuery(
      "(xs:gYear(\"2000Z\") eq xs:gYear(\"2000Z\")) and (xs:gYear(\"2001Z\") eq xs:gYear(\"2001Z\"))",
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
   * Test: op-gYear-equal-9                                 
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYear-equal" function used     
   * together with "or" expression (eq operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearEqualNew9() {
    final XQuery query = new XQuery(
      "(xs:gYear(\"2000Z\") eq xs:gYear(\"2000Z\")) or (xs:gYear(\"1976Z\") eq xs:gYear(\"1976Z\"))",
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
