package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the divide-yearMonthDuration() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpDivideYearMonthDuration extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-YearMonthDurationDivide-1                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of dividing a xs:yearMonthDuration with 3. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationDivide1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y36M\") div 3 eq xs:yearMonthDuration(\"P2Y\")",
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
   *  Test: K-YearMonthDurationDivide-2                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of dividing a xs:yearMonthDuration with xs:double('-INF'). 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationDivide2() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y36M\") div xs:double(\"-INF\") eq xs:yearMonthDuration(\"P0M\")",
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
   *  Test: K-YearMonthDurationDivide-3                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of dividing a xs:yearMonthDuration with xs:double('INF'). 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationDivide3() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y36M\") div xs:double(\"INF\") eq xs:yearMonthDuration(\"P0M\")",
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
   *  Test: K-YearMonthDurationDivide-4                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of dividing a xs:yearMonthDuration with 0. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationDivide4() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y36M\") div 0",
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
   *  Test: K-YearMonthDurationDivide-5                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of dividing a xs:yearMonthDuration with NaN. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationDivide5() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y36M\") div xs:double(\"NaN\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0005")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-YearMonthDurationDivide-6                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of dividing a xs:yearMonthDuration with xs:double('-0'). 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationDivide6() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y36M\") div xs:double(\"-0\")",
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
   *  test division of xs:yearMonthDuration by 0 .
   */
  @org.junit.Test
  public void cbclDivideYearMonthDuration001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($years as xs:integer) as xs:yearMonthDuration { xs:yearMonthDuration(concat(\"P\", $years, \"Y\")) };\n" +
      "        local:yearMonthDuration(2) div 0",
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
   *  test division of xs:yearMonthDuration by 1 .
   */
  @org.junit.Test
  public void cbclDivideYearMonthDuration002() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($years as xs:integer) as xs:yearMonthDuration { xs:yearMonthDuration(concat(\"P\", $years, \"Y\")) };\n" +
      "        local:yearMonthDuration(2) div 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P2Y")
    );
  }

  /**
   *  test possible overflow in division of xs:yearMonthDuration .
   */
  @org.junit.Test
  public void cbclDivideYearMonthDuration003() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($years as xs:integer) as xs:yearMonthDuration { xs:yearMonthDuration(concat(\"P\", $years, \"Y\")) };\n" +
      "        local:yearMonthDuration(768614336404564650) div 0.5",
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
   * Test: op-divide-yearMonthDuration-1                    
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration" operator 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration1() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P2Y11M\") div 1.5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1Y11M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-yearMonthDuration-10                   
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration" opeartor used  
   * together with an "or" expression.                      
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration10() {
    final XQuery query = new XQuery(
      "fn:string((xs:yearMonthDuration(\"P05Y02M\") div 2.0)) or fn:string((xs:yearMonthDuration(\"P05Y03M\") div 2.0))",
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
   * Test: op-divide-yearMonthDuration-11                   
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration" operator used  
   * together with a multiple "div" expressions.            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration11() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P20Y11M\") div 2.0) div (xs:yearMonthDuration(\"P20Y11M\") div 2.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-yearMonthDuration-12                   
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration" operators used 
   * with a boolean expression and the "fn:true" function.   
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration12() {
    final XQuery query = new XQuery(
      "fn:string((xs:yearMonthDuration(\"P10Y11M\") div 2.0)) and (fn:true())",
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
   * Test: op-divide-yearMonthDuration-13                   
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration" operator used  
   * together with the numeric-equal- operator "eq".        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration13() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P23Y11M\") div 2.0) eq xs:yearMonthDuration(\"P23Y11M\")",
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
   * Test: op-divide-yearMonthDuration-14                   
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration" operator used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration14() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P21Y12M\") div 2.0) ne xs:yearMonthDuration(\"P08Y05M\")",
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
   * Test: op-divide-yearMonthDuration-15                   
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration" operator used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration15() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y01M\") div 2.0) le xs:yearMonthDuration(\"P17Y02M\")",
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
   * Test: op-divide-yearMonthDuration-16                   
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration" operator used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration16() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P13Y09M\") div 2.0) ge xs:yearMonthDuration(\"P18Y02M\")",
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
   * Test: op-divide-yearMonthDuration-17                   
   * Written By: Michael Kay                                
   * Date: 5 March 2009                                     
   * Purpose: Tests rounding performed by the "divide-yearMonthDuration" operator  
   * See Erratum FO.E12                           .         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration17() {
    final XQuery query = new XQuery(
      "for $i in (-2, -4, -10, -50, +50, +10, +4, +2) return (xs:yearMonthDuration(\"P5M\") div $i)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-P2M -P1M P0M P0M P0M P1M P1M P3M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-yearMonthDuration-2                    
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration" operator
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration2() {
    final XQuery query = new XQuery(
      "fn:string((xs:yearMonthDuration(\"P10Y11M\") div 2.0)) and fn:false()",
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
   * Test: op-divide-yearMonthDuration-3                    
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration" operator as 
   * part of a boolean expression (or operator) and the "fn:boolean" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration3() {
    final XQuery query = new XQuery(
      "fn:string((xs:yearMonthDuration(\"P20Y10M\") div 2.0)) or fn:false()",
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
   * Test: op-divide-yearMonthDuration-4                    
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration" operator that  
   * return true and used together with fn:not.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:yearMonthDuration(\"P11Y04M\") div 2.0))",
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
   * Test: op-divide-yearMonthDuration-5                    
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration" operator that  
   * is used as an argument to the fn:boolean function.     
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:yearMonthDuration(\"P05Y08M\") div 2.0))",
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
   * Test: op-divide-yearMonthDuration-6                    
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration6() {
    final XQuery query = new XQuery(
      "fn:number(xs:yearMonthDuration(\"P02Y09M\") div 2.0)",
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
   * Test: op-divide-yearMonthDuration-7                    
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration" operator used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration7() {
    final XQuery query = new XQuery(
      "fn:string(xs:yearMonthDuration(\"P03Y08M\") div 2.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1Y10M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-yearMonthDuration-8                    
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration" operator that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration8() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y01M\") div -2.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-P5Y")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-yearMonthDuration-9                    
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration" operator used 
   * together with and "and" expression.                    
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration9() {
    final XQuery query = new XQuery(
      "fn:string((xs:yearMonthDuration(\"P01Y01M\") div 2.0)) and fn:string((xs:yearMonthDuration(\"P02Y03M\") div 2.0))",
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
   * Test: op-divide-yearMonthDuration2args-1                
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-yearMonthDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration2args1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") div xs:double(\"-1.7976931348623157E308\")",
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
   * Test: op-divide-yearMonthDuration2args-2                
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-yearMonthDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(mid range)               
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration2args2() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1000Y6M\") div xs:double(\"-1.7976931348623157E308\")",
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
   * Test: op-divide-yearMonthDuration2args-3                
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-yearMonthDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(upper bound)             
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration2args3() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P2030Y12M\") div xs:double(\"-1.7976931348623157E308\")",
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
   * Test: op-divide-yearMonthDuration2args-4                
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-yearMonthDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:double(mid range)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration2args4() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") div xs:double(\"0.1\")",
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
   * Test: op-divide-yearMonthDuration2args-5                
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-yearMonthDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:double(upper bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDuration2args5() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") div xs:double(\"1.7976931348623157E308\")",
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
}
