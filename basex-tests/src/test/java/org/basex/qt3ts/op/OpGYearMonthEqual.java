package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the gYearMonth-equal() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpGYearMonthEqual extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-gYearMonthEQ-1                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:gYearMonth, returning positive. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGYearMonthEQ1() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"2001-01 \") eq xs:gYearMonth(\"2001-01\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   *  Test: K-gYearMonthEQ-2                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:gYearMonth.       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGYearMonthEQ2() {
    final XQuery query = new XQuery(
      "not(xs:gYearMonth(\"2001-03\") eq xs:gYearMonth(\"2000-03\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   *  Test: K-gYearMonthEQ-3                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:gYearMonth.       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGYearMonthEQ3() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"2001-12\") ne xs:gYearMonth(\"2001-11\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   *  Test: K-gYearMonthEQ-4                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:gYearMonth.       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGYearMonthEQ4() {
    final XQuery query = new XQuery(
      "not(xs:gYearMonth(\"1995-11\") ne xs:gYearMonth(\"1995-11\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   *  Test: K-gYearMonthEQ-5                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to Z, in xs:gYearMonth. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGYearMonthEQ5() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-01-00:00\") eq xs:gYearMonth(\"1999-01Z\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   *  Test: K-gYearMonthEQ-6                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset +00:00 is equal to Z, in xs:gYearMonth. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGYearMonthEQ6() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-01+00:00\") eq xs:gYearMonth(\"1999-01Z\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   *  Test: K-gYearMonthEQ-7                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset Z is equal to Z, in xs:gYearMonth. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGYearMonthEQ7() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-01Z\") eq xs:gYearMonth(\"1999-01Z\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   *  Test: K-gYearMonthEQ-8                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to +00:00, in xs:gYearMonth. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGYearMonthEQ8() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-01-00:00\") eq xs:gYearMonth(\"1999-01+00:00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   *  test comparison of gYearMonth .
   */
  @org.junit.Test
  public void cbclGYearMonthEqual001() {
    final XQuery query = new XQuery(
      "declare function local:gYearMonth($year as xs:integer) { xs:gYearMonth(concat(string(2000 + $year), \"-01\")) }; not(local:gYearMonth(7) eq xs:gYearMonth(\"2008-01\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   *  test comparison of gYearMonth .
   */
  @org.junit.Test
  public void cbclGYearMonthEqual002() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT9M'))\n" +
      "            then xs:gYearMonth(\"2008-01\") eq xs:gYearMonth(\"2008-01+09:01\")\n" +
      "            else xs:gYearMonth(\"2008-01\") eq xs:gYearMonth(\"2008-01+09:00\")",
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
   *  test comparison of gYearMonth .
   */
  @org.junit.Test
  public void cbclGYearMonthEqual003() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT9M'))\n" +
      "            then xs:gYearMonth(\"2008-01+09:01\") eq xs:gYearMonth(\"2008-01\")\n" +
      "            else xs:gYearMonth(\"2008-01+09:00\") eq xs:gYearMonth(\"2008-01\")",
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
   *  test comparison of gYearMonth .
   */
  @org.junit.Test
  public void cbclGYearMonthEqual004() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('-PT9M'))\n" +
      "            then xs:gYearMonth(\"2008-01\") eq xs:gYearMonth(\"2008-01-09:01\")\n" +
      "            else xs:gYearMonth(\"2008-01\") eq xs:gYearMonth(\"2008-01-09:00\")",
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
   *  test comparison of gYearMonth .
   */
  @org.junit.Test
  public void cbclGYearMonthEqual005() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('-PT9M'))\n" +
      "            then xs:gYearMonth(\"2008-01-09:01\") eq xs:gYearMonth(\"2008-01\")\n" +
      "            else xs:gYearMonth(\"2008-01-09:00\") eq xs:gYearMonth(\"2008-01\")",
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
   *  test comparison of gYearMonth .
   */
  @org.junit.Test
  public void cbclGYearMonthEqual006() {
    final XQuery query = new XQuery(
      "declare function local:gYearMonth($year as xs:integer) { xs:gYearMonth(concat(string(2000 + $year), \"-01\")) }; not(local:gYearMonth(7) ne xs:gYearMonth(\"2008-01\"))",
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
   *  test comparison of gYearMonth .
   */
  @org.junit.Test
  public void cbclGYearMonthEqual007() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT9M'))\n" +
      "            then xs:gYearMonth(\"2008-01\") ne xs:gYearMonth(\"2008-01+09:01\")\n" +
      "            else xs:gYearMonth(\"2008-01\") ne xs:gYearMonth(\"2008-01+09:00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   *  test comparison of gYearMonth .
   */
  @org.junit.Test
  public void cbclGYearMonthEqual008() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT9M'))\n" +
      "            then xs:gYearMonth(\"2008-01+09:01\") ne xs:gYearMonth(\"2008-01\")\n" +
      "            else xs:gYearMonth(\"2008-01+09:00\") ne xs:gYearMonth(\"2008-01\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   *  test comparison of gYearMonth .
   */
  @org.junit.Test
  public void cbclGYearMonthEqual009() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('-PT9M'))\n" +
      "            then xs:gYearMonth(\"2008-01\") ne xs:gYearMonth(\"2008-01-09:01\")\n" +
      "            else xs:gYearMonth(\"2008-01\") ne xs:gYearMonth(\"2008-01-09:00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   *  test comparison of gYearMonth .
   */
  @org.junit.Test
  public void cbclGYearMonthEqual010() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('-PT9M'))\n" +
      "            then xs:gYearMonth(\"2008-01-09:01\") ne xs:gYearMonth(\"2008-01\")\n" +
      "            else xs:gYearMonth(\"2008-01-09:00\") ne xs:gYearMonth(\"2008-01\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   *  test comparison of gYearMonth .
   */
  @org.junit.Test
  public void cbclGYearMonthEqual011() {
    final XQuery query = new XQuery(
      "declare function local:gYearMonth($gYearMonth as xs:gYearMonth, $null as xs:boolean) { if ($null) then () else $gYearMonth }; exists(local:gYearMonth(xs:gYearMonth(\"1972-12\"), fn:true()) eq xs:gYearMonth(\"1972-12\"))",
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
   *  test comparison of gYearMonth .
   */
  @org.junit.Test
  public void cbclGYearMonthEqual012() {
    final XQuery query = new XQuery(
      "declare function local:gYearMonth($gYearMonth as xs:gYearMonth, $null as xs:boolean) { if ($null) then () else $gYearMonth }; local:gYearMonth(xs:gYearMonth(\"1972-12\"), fn:false()) eq xs:gYearMonth(\"1972-12\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   *  test comparison of gYearMonth .
   */
  @org.junit.Test
  public void cbclGYearMonthEqual013() {
    final XQuery query = new XQuery(
      "declare function local:gYearMonth($gYearMonth as xs:gYearMonth, $null as xs:boolean) { if ($null) then () else $gYearMonth }; exists(local:gYearMonth(xs:gYearMonth(\"1972-12\"), fn:true()) ne xs:gYearMonth(\"1972-12\"))",
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
   *  test comparison of gYearMonth .
   */
  @org.junit.Test
  public void cbclGYearMonthEqual014() {
    final XQuery query = new XQuery(
      "declare function local:gYearMonth($gYearMonth as xs:gYearMonth, $null as xs:boolean) { if ($null) then () else $gYearMonth }; local:gYearMonth(xs:gYearMonth(\"1972-12\"), fn:false()) ne xs:gYearMonth(\"1972-12\")",
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
   * Test: op-gYearMonth-equal2args-1                        
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYearMonth-equal" operator  
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYearMonth(lower bound)                     
   * $arg2 = xs:gYearMonth(lower bound)                     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqual2args1() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1970-01Z\") eq xs:gYearMonth(\"1970-01Z\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   * Test: op-gYearMonth-equal2args-10                       
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYearMonth-equal" operator  
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYearMonth(lower bound)                     
   * $arg2 = xs:gYearMonth(upper bound)                     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqual2args10() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1970-01Z\") ne xs:gYearMonth(\"2030-12Z\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   * Test: op-gYearMonth-equal2args-2                        
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYearMonth-equal" operator  
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYearMonth(mid range)                       
   * $arg2 = xs:gYearMonth(lower bound)                     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqual2args2() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1984-12Z\") eq xs:gYearMonth(\"1970-01Z\")",
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
   * Test: op-gYearMonth-equal2args-3                        
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYearMonth-equal" operator  
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYearMonth(upper bound)                     
   * $arg2 = xs:gYearMonth(lower bound)                     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqual2args3() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"2030-12Z\") eq xs:gYearMonth(\"1970-01Z\")",
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
   * Test: op-gYearMonth-equal2args-4                        
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYearMonth-equal" operator  
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYearMonth(lower bound)                     
   * $arg2 = xs:gYearMonth(mid range)                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqual2args4() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1970-01Z\") eq xs:gYearMonth(\"1984-12Z\")",
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
   * Test: op-gYearMonth-equal2args-5                        
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYearMonth-equal" operator  
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYearMonth(lower bound)                     
   * $arg2 = xs:gYearMonth(upper bound)                     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqual2args5() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1970-01Z\") eq xs:gYearMonth(\"2030-12Z\")",
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
   * Test: op-gYearMonth-equal2args-6                        
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYearMonth-equal" operator  
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYearMonth(lower bound)                     
   * $arg2 = xs:gYearMonth(lower bound)                     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqual2args6() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1970-01Z\") ne xs:gYearMonth(\"1970-01Z\")",
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
   * Test: op-gYearMonth-equal2args-7                        
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYearMonth-equal" operator  
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYearMonth(mid range)                       
   * $arg2 = xs:gYearMonth(lower bound)                     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqual2args7() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1984-12Z\") ne xs:gYearMonth(\"1970-01Z\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   * Test: op-gYearMonth-equal2args-8                        
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYearMonth-equal" operator  
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYearMonth(upper bound)                     
   * $arg2 = xs:gYearMonth(lower bound)                     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqual2args8() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"2030-12Z\") ne xs:gYearMonth(\"1970-01Z\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   * Test: op-gYearMonth-equal2args-9                        
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gYearMonth-equal" operator  
   *  with the arguments set as follows:                    
   * $arg1 = xs:gYearMonth(lower bound)                     
   * $arg2 = xs:gYearMonth(mid range)                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqual2args9() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1970-01Z\") ne xs:gYearMonth(\"1984-12Z\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   * Test: op-gYearMonth-equal-1                            
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYearMonth-equal" function     
   * As per example 1 (for this function)of the F&O specs   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqualNew1() {
    final XQuery query = new XQuery(
      "(xs:gYearMonth(\"1976-02-05:00\") eq xs:gYearMonth(\"1976-03Z\"))",
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
   * Test: op-gYearMonth-equal-10                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYearMonth-equal" function used
   * together with "or" expression (ne operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqualNew10() {
    final XQuery query = new XQuery(
      "(xs:gYearMonth(\"1976-01Z\") ne xs:gYearMonth(\"1976-02Z\")) or (xs:gYearMonth(\"1980-03Z\") ne xs:gYearMonth(\"1980-04Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   * Test: op-gYearMonth-equal-11                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYearMonth-equal" function used
   * together with "fn:true"/or expression (eq operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqualNew11() {
    final XQuery query = new XQuery(
      "(xs:gYearMonth(\"1980-05Z\") eq xs:gYearMonth(\"1980-05Z\")) or (fn:true())",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   * Test: op-gYearMonth-equal-12                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYearMonth-equal" function used
   * together with "fn:true"/or expression (ne operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqualNew12() {
    final XQuery query = new XQuery(
      "(xs:gYearMonth(\"2000-06Z\") ne xs:gYearMonth(\"2000-07Z\")) or (fn:true())",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   * Test: op-gYearMonth-equal-13                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYearMonth-equal" function used
   * together with "fn:false"/or expression (eq operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqualNew13() {
    final XQuery query = new XQuery(
      "(xs:gYearMonth(\"1980-09Z\") eq xs:gYearMonth(\"1980-09Z\")) or (fn:false())",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   * Test: op-gYearMonth-equal-14                           
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYearMonth-equal" function used
   * together with "fn:false"/or expression (ne operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqualNew14() {
    final XQuery query = new XQuery(
      "(xs:gYearMonth(\"1980-03Z\") ne xs:gYearMonth(\"1980-03Z\")) or (fn:false())",
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
   * Test: op-gYearMonth-equal-2                            
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYearMonth-equal" function     
   * As per example 2 (for this function) of the F&O  specs 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqualNew2() {
    final XQuery query = new XQuery(
      "(xs:gYearMonth(\"1976-03-05:00\") eq xs:gYearMonth(\"1976-03Z\"))",
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
   * Test: op-gYearMonth-equal-3                            
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYearMonth-equal" function that
   * return true and used together with fn:not (eq operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqualNew3() {
    final XQuery query = new XQuery(
      "fn:not((xs:gYearMonth(\"1995-02Z\") eq xs:gYearMonth(\"1995-02Z\")))",
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
   * Test: op-gYearMonth-equal-4                            
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYearMonth-equal" function that
   * return true and used together with fn:not (le operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqualNew4() {
    final XQuery query = new XQuery(
      "fn:not(xs:gYearMonth(\"2005-02Z\") ne xs:gYearMonth(\"2006-03Z\"))",
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
   * Test: op-gYearMonth-equal-5                            
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYearMonth-equal" function that
   * return false and used together with fn:not (eq operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqualNew5() {
    final XQuery query = new XQuery(
      "fn:not(xs:gYearMonth(\"2000-01Z\") eq xs:gYearMonth(\"2001-04Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   * Test: op-gYearMonth-equal-6                            
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYearMonth-equal" function that
   * return false and used together with fn:not(ne operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqualNew6() {
    final XQuery query = new XQuery(
      "fn:not(xs:gYearMonth(\"2005-01Z\") ne xs:gYearMonth(\"2005-01Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   * Test: op-gYearMonth-equal-7                            
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYearMonth-equal" function used
   * together with "and" expression (eq operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqualNew7() {
    final XQuery query = new XQuery(
      "(xs:gYearMonth(\"2000-02Z\") eq xs:gYearMonth(\"2000-03Z\")) and (xs:gYearMonth(\"2001-01Z\") eq xs:gYearMonth(\"2001-01Z\"))",
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
   * Test: op-gYearMonth-equal-8                            
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYearMonth-equal" function used
   * together with "and" expression (ne operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqualNew8() {
    final XQuery query = new XQuery(
      "(xs:gYearMonth(\"2000-01Z\") ne xs:gYearMonth(\"2000-01Z\")) and (xs:gYearMonth(\"1975-01Z\") ne xs:gYearMonth(\"1975-03Z\"))",
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
   * Test: op-gYearMonth-equal-9                            
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "gYearMonth-equal" function used
   * together with "or" expression (eq operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGYearMonthEqualNew9() {
    final XQuery query = new XQuery(
      "(xs:gYearMonth(\"2000-01Z\") eq xs:gYearMonth(\"2000-03Z\")) or (xs:gYearMonth(\"1976-06Z\") eq xs:gYearMonth(\"1976-06Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
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
