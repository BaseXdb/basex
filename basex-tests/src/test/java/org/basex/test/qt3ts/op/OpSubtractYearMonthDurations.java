package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the subtract-yearMonthDurations() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpSubtractYearMonthDurations extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-YearMonthDurationSubtract-1                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of substraction an xs:yearMonthDuration with P0M. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationSubtract1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y3M\") - xs:yearMonthDuration(\"P0M\") eq xs:yearMonthDuration(\"P3Y3M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   *  Test: K-YearMonthDurationSubtract-2                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of substraction P0M with an xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationSubtract2() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0M\") - xs:yearMonthDuration(\"P3Y3M\") eq xs:yearMonthDuration(\"-P3Y3M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   *  Test: K-YearMonthDurationSubtract-3                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of substraction two arbitrary xs:yearMonthDurations. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationSubtract3() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P32Y43M\") - xs:yearMonthDuration(\"P12Y13M\") eq xs:yearMonthDuration(\"P22Y6M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   *  Test: K-YearMonthDurationSubtract-4                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The addition operator is not available between xs:dayTimeDuration and xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationSubtract4() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3D\") + xs:yearMonthDuration(\"P3Y3M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-YearMonthDurationSubtract-5                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The addition operator is not available between xs:yearMonthDuration and xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationSubtract5() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y3M\") + xs:dayTimeDuration(\"P3D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-YearMonthDurationSubtract-6                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The addition operator is not available between xs:duration and xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationSubtract6() {
    final XQuery query = new XQuery(
      "xs:duration(\"P3D\") + xs:yearMonthDuration(\"P3Y3M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-YearMonthDurationSubtract-7                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The addition operator is not available between xs:yearMonthDuration and xs:duration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationSubtract7() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y3M\") + xs:duration(\"P3D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-YearMonthDurationSubtract-8                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The addition operator is not available between xs:dayTimeDuration and xs:duration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationSubtract8() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3D\") + xs:duration(\"P3Y3M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-YearMonthDurationSubtract-9                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The addition operator is not available between xs:duration and xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationSubtract9() {
    final XQuery query = new XQuery(
      "xs:duration(\"P3Y3M\") + xs:dayTimeDuration(\"P3D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test subtraction of zero duration from yearMonthDurations .
   */
  @org.junit.Test
  public void cbclSubtractYearMonthDurations001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($years as xs:integer, $months as xs:integer ) { xs:yearMonthDuration(concat('P', $years, 'Y', $months, 'M')) };\n" +
      "        local:yearMonthDuration(1, 1) - xs:yearMonthDuration(\"P0Y\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1Y1M")
    );
  }

  /**
   *  test subtraction of yearMonthDurations .
   */
  @org.junit.Test
  public void cbclSubtractYearMonthDurations002() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($years as xs:integer, $months as xs:integer ) { xs:yearMonthDuration(concat('P', $years, 'Y', $months, 'M')) };\n" +
      "        local:yearMonthDuration(1, 1) - local:yearMonthDuration(1, 1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P0M")
    );
  }

  /**
   *  test subtraction of large yearMonthDuration .
   */
  @org.junit.Test
  public void cbclSubtractYearMonthDurations003() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($years as xs:integer, $months as xs:integer ) { xs:yearMonthDuration(concat('P', $years, 'Y', $months, 'M')) };\n" +
      "        xs:yearMonthDuration(\"-P768614336404564650Y\") - local:yearMonthDuration(768614336404564650, 1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0002")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDurations-1                 
   * Written By: Carmelo Montanez                           
   * Date: June 28, 2005                                    
   * Purpose: Evaluates The "subtract-yearMonthDurations" function 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations1() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P2Y11M\") - xs:yearMonthDuration(\"P3Y3M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-P4M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDurations-10                
   * Written By: Carmelo Montanez                           
   * Date: June 28, 2005                                    
   * Purpose: Evaluates The "subtract-yearMonthDurations" function used  
   * together with an "or" expression.                      
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations10() {
    final XQuery query = new XQuery(
      "fn:string((xs:yearMonthDuration(\"P05Y02M\") - xs:yearMonthDuration(\"P03Y04M\"))) or fn:string((xs:yearMonthDuration(\"P05Y03M\") - xs:yearMonthDuration(\"P01Y03M\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   * Test: op-subtract-yearMonthDurations-11                
   * Written By: Carmelo Montanez                           
   * Date: June 28, 2005                                    
   * Purpose: Evaluates The "subtract-yearMonthDurations" function used  
   * together with a "div" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations11() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P42Y10M\") - xs:yearMonthDuration(\"P20Y10M\")) div (xs:yearMonthDuration(\"P20Y11M\") - xs:yearMonthDuration(\"P18Y11M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "11")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDurations-12                
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-yearMonthDurations" operators used 
   * with a boolean expression and the "fn:true" function.   
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations12() {
    final XQuery query = new XQuery(
      "fn:string((xs:yearMonthDuration(\"P10Y11M\") - xs:yearMonthDuration(\"P05Y07M\"))) and (fn:true())",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   * Test: op-subtract-yearMonthDurations-13                
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-yearMonthDurations" function used  
   * together with the numeric-equal- operator "eq".        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations13() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P23Y11M\") - xs:yearMonthDuration(\"P23Y11M\")) eq xs:yearMonthDuration(\"P23Y11M\")",
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
   * Test: op-subtract-yearMonthDurations-14                
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-yearMonthDurations" function used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations14() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P21Y12M\") - xs:yearMonthDuration(\"P08Y05M\")) ne xs:yearMonthDuration(\"P08Y05M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   * Test: op-subtract-yearMonthDurations-15                
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-yearMonthDurations" function used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations15() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y01M\") - xs:yearMonthDuration(\"P17Y02M\")) le xs:yearMonthDuration(\"P17Y02M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   * Test: op-subtract-yearMonthDurations-16                
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-yearMonthDurations" function used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations16() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P13Y09M\") - xs:yearMonthDuration(\"P18Y02M\")) ge xs:yearMonthDuration(\"P18Y02M\")",
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
   * Test: op-subtract-yearMonthDurations-2                 
   * Written By: Carmelo Montanez                           
   * Date: June 28, 2005                                    
   * Purpose: Evaluates The "subtract-yearMonthDurations" function 
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations2() {
    final XQuery query = new XQuery(
      "fn:string(xs:yearMonthDuration(\"P10Y11M\") - xs:yearMonthDuration(\"P12Y07M\")) and fn:false()",
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
   * Test: op-subtract-yearMonthDurations-3                 
   * Written By: Carmelo Montanez                           
   * Date: June 28, 2005                                    
   * Purpose: Evaluates The "subtract-yearMonthDurations" function as 
   * part of a boolean expression (or operator) and the "fn:boolean" function. 
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations3() {
    final XQuery query = new XQuery(
      "fn:string(xs:yearMonthDuration(\"P20Y10M\") - xs:yearMonthDuration(\"P19Y10M\")) or fn:false()",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   * Test: op-subtract-yearMonthDurations-4                 
   * Written By: Carmelo Montanez                           
   * Date: June 28, 2005                                    
   * Purpose: Evaluates The "subtract-yearMonthDurations" function that  
   * return true and used together with fn:not.             
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:yearMonthDuration(\"P11Y04M\") - xs:yearMonthDuration(\"P02Y11M\")))",
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
   * Test: op-subtract-yearMonthDurations-5                 
   * Written By: Carmelo Montanez                           
   * Date: June 28 2005                                     
   * Purpose: Evaluates The "subtract-yearMonthDurations" function that  
   * is used as an argument to the fn:boolean function.     
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:yearMonthDuration(\"P05Y08M\") - xs:yearMonthDuration(\"P03Y06M\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   * Test: op-subtract-yearMonthDurations-6                 
   * Written By: Carmelo Montanez                           
   * Date: June 28, 2005                                    
   * Purpose: Evaluates The "subtract-yearMonthDurations" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations6() {
    final XQuery query = new XQuery(
      "fn:number(xs:yearMonthDuration(\"P02Y09M\") - xs:yearMonthDuration(\"P10Y01M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDurations-7                 
   * Written By: Carmelo Montanez                           
   * Date: June 28, 2005                                    
   * Purpose: Evaluates The "subtract-yearMonthDurations" function used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations7() {
    final XQuery query = new XQuery(
      "fn:string(xs:yearMonthDuration(\"P03Y08M\") - xs:yearMonthDuration(\"P01Y02M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P2Y6M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDurations-8                 
   * Written By: Carmelo Montanez                           
   * Date: June 28, 2005                                    
   * Purpose: Evaluates The "subtract-yearMonthDurations" function that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations8() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y01M\") - xs:yearMonthDuration(\"P11Y02M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-P1Y1M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDurations-9                 
   * Written By: Carmelo Montanez                           
   * Date: June 28, 2005                                    
   * Purpose: Evaluates The "subtract-yearMonthDurations" function used 
   * together with and "and" expression.                    
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations9() {
    final XQuery query = new XQuery(
      "fn:string((xs:yearMonthDuration(\"P01Y01M\") - xs:yearMonthDuration(\"P02Y02M\"))) and fn:string((xs:yearMonthDuration(\"P02Y03M\") - xs:yearMonthDuration(\"P04Y04M\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
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
   * Test: op-subtract-yearMonthDurations2args-1             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-yearMonthDurations" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations2args1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") - xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P0M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDurations2args-2             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-yearMonthDurations" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(mid range)               
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations2args2() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1000Y6M\") - xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1000Y6M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDurations2args-3             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-yearMonthDurations" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(upper bound)             
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations2args3() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P2030Y12M\") - xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P2031Y")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDurations2args-4             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-yearMonthDurations" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(mid range)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations2args4() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") - xs:yearMonthDuration(\"P1000Y6M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-P1000Y6M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-yearMonthDurations2args-5             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-yearMonthDurations" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(upper bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractYearMonthDurations2args5() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") - xs:yearMonthDuration(\"P2030Y12M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-P2031Y")
    );
  }
}
