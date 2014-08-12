package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the multiply-yearMonthDuration() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpMultiplyYearMonthDuration extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-YearMonthDurationMultiply-1                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of multiplying a xs:yearMonthDuration with 3. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationMultiply1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y36M\") * 3 eq xs:yearMonthDuration(\"P18Y\")",
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
   *  Test: K-YearMonthDurationMultiply-10                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The multiplication operator is not available between xs:dayTimeDuration and xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationMultiply10() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3D\") * xs:yearMonthDuration(\"P3Y3M\")",
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
   *  Test: K-YearMonthDurationMultiply-11                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The multiplication operator is not available between xs:yearMonthDuration and xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationMultiply11() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y3M\") * xs:dayTimeDuration(\"P3D\")",
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
   *  Test: K-YearMonthDurationMultiply-12                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The multiplication operator is not available between xs:yearMonthDuration and xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationMultiply12() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y3M\") * xs:yearMonthDuration(\"P3Y3M\")",
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
   *  Test: K-YearMonthDurationMultiply-13                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The multiplication operator is not available between xs:dayTimeDuration and xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationMultiply13() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3D\") * xs:dayTimeDuration(\"P3D\")",
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
   *  Test: K-YearMonthDurationMultiply-2                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of multiplying 3 with xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationMultiply2() {
    final XQuery query = new XQuery(
      "3 * xs:yearMonthDuration(\"P3Y36M\") eq xs:yearMonthDuration(\"P18Y\")",
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
   *  Test: K-YearMonthDurationMultiply-3                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of multiplying 0 with xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationMultiply3() {
    final XQuery query = new XQuery(
      "0 * xs:yearMonthDuration(\"P3Y36M\") eq xs:yearMonthDuration(\"P0M\")",
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
   *  Test: K-YearMonthDurationMultiply-4                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of multiplying a xs:yearMonthDuration with 0. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationMultiply4() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y36M\") * 0 eq xs:yearMonthDuration(\"P0M\")",
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
   *  Test: K-YearMonthDurationMultiply-5                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of multiplying a xs:yearMonthDuration with INF. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationMultiply5() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y3M\") * xs:double(\"INF\")",
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
   *  Test: K-YearMonthDurationMultiply-6                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of multiplying a xs:yearMonthDuration with -INF. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationMultiply6() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y3M\") * xs:double(\"-INF\")",
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
   *  Test: K-YearMonthDurationMultiply-7                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of multiplying a xs:yearMonthDuration with NaN. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationMultiply7() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y3M\") * xs:double(\"NaN\")",
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
   *  Test: K-YearMonthDurationMultiply-8                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The multiplication operator is not available between xs:duration and xs:integer. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationMultiply8() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y3M\") * 3",
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
   *  Test: K-YearMonthDurationMultiply-9                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The multiplication operator is not available between xs:integer and xs:duration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationMultiply9() {
    final XQuery query = new XQuery(
      "3 * xs:duration(\"P1Y3M\")",
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
   *  test multiplication of xs:yearMonthDuration by 0 .
   */
  @org.junit.Test
  public void cbclMultiplyYearMonthDuration001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($years as xs:integer) as xs:yearMonthDuration { xs:yearMonthDuration(concat(\"P\", $years, \"Y\")) };\n" +
      "        local:yearMonthDuration(2) * 0",
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
   *  test multiplication of xs:yearMonthDuration by 1 .
   */
  @org.junit.Test
  public void cbclMultiplyYearMonthDuration002() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($years as xs:integer) as xs:yearMonthDuration { xs:yearMonthDuration(concat(\"P\", $years, \"Y\")) };\n" +
      "        local:yearMonthDuration(2) * 1",
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
   *  test multiplication of xs:yearMonthDuration by NaN .
   */
  @org.junit.Test
  public void cbclMultiplyYearMonthDuration003() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P2Y\") * xs:double('NaN')",
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
   *  test multiplication of xs:yearMonthDuration by 0 .
   */
  @org.junit.Test
  public void cbclMultiplyYearMonthDuration004() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($years as xs:integer) as xs:yearMonthDuration { xs:yearMonthDuration(concat(\"P\", $years, \"Y\")) };\n" +
      "        0 * local:yearMonthDuration(2)",
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
   *  test multiplication of xs:yearMonthDuration by 1 .
   */
  @org.junit.Test
  public void cbclMultiplyYearMonthDuration005() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($years as xs:integer) as xs:yearMonthDuration { xs:yearMonthDuration(concat(\"P\", $years, \"Y\")) };\n" +
      "        1 * local:yearMonthDuration(2)",
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
   *  test multiplication of xs:yearMonthDuration by NaN .
   */
  @org.junit.Test
  public void cbclMultiplyYearMonthDuration006() {
    final XQuery query = new XQuery(
      "xs:double('NaN') * xs:yearMonthDuration(\"P2Y\")",
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
   * Test: op-multiply-yearMonthDuration-1                  
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "multiply-yearMonthDuration" function 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P2Y11M\") * 2.3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P6Y9M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-yearMonthDuration-10                 
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "multiply-yearMonthDuration" function used  
   * together with an "or" expression.                      
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration10() {
    final XQuery query = new XQuery(
      "fn:string((xs:yearMonthDuration(\"P05Y02M\") * 2.0)) or fn:string((xs:yearMonthDuration(\"P05Y03M\") * 2.0))",
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
   * Test: op-multiply-yearMonthDuration-11                 
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "multiply-yearMonthDuration" function used  
   * together with a "div" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration11() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P20Y11M\") * 2.0) div (xs:yearMonthDuration(\"P20Y11M\") * 2.0)",
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
   * Test: op-multiply-yearMonthDuration-12                 
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "multiply-yearMonthDuration" operators used 
   * with a boolean expression and the "fn:true" function.   
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration12() {
    final XQuery query = new XQuery(
      "fn:string((xs:yearMonthDuration(\"P10Y11M\") * 2.0)) and (fn:true())",
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
   * Test: op-multiply-yearMonthDuration-13                 
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "multiply-yearMonthDuration" function used  
   * together with the numeric-equal- operator "eq".        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration13() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P23Y11M\") * 2.0) eq xs:yearMonthDuration(\"P23Y11M\")",
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
   * Test: op-multiply-yearMonthDuration-14                 
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "multiply-yearMonthDuration" function used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration14() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P21Y12M\") * 2.0) ne xs:yearMonthDuration(\"P08Y05M\")",
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
   * Test: op-multiply-yearMonthDuration-15                 
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "multiply-yearMonthDuration" function used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration15() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y01M\") * 2.0) le xs:yearMonthDuration(\"P17Y02M\")",
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
   * Test: op-subtract-yearMonthDurations-16                
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-yearMonthDurations" function used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration16() {
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
   * Test: op-multiply-yearMonthDuration-16                 
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "multiply-yearMonthDuration" operator
   * multiplied by +0.  Use of fn:count to avoid empty file. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration17() {
    final XQuery query = new XQuery(
      "fn:count((xs:yearMonthDuration(\"P13Y09M\") *+0))",
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
   * Test: op-multiply-yearMonthDuration-18                 
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "multiply-yearMonthDuration" operator
   * multiplied by -0.  Use of fn:count to avoid empty file. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration18() {
    final XQuery query = new XQuery(
      "fn:count((xs:yearMonthDuration(\"P13Y09M\") *-0))",
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
   * Test: op-multiply-yearMonthDuration-19                 
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "multiply-yearMonthDuration" operator
   * with arg2 set to NaN.  Should raise an error           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration19() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P13Y09M\") * fn:number(())",
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
   * Test: op-multiply-yearMonthDuration-2                  
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "multiply-yearMonthDuration" function 
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration2() {
    final XQuery query = new XQuery(
      "fn:string((xs:yearMonthDuration(\"P10Y11M\")) * 2.0) and fn:false()",
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
   * Test: op-multiply-yearMonthDuration-20                 
   * Written By: Michael Kay                                
   * Date: 5 Feb 2009                                       
   * Purpose: Test rounding behaviour, FO.E12.              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration20() {
    final XQuery query = new XQuery(
      "for $i in (-3.9, -3.5, -3.1, -0.9, -0.5, -0.1, +0.1, +0.5, +0.9, +3.1, +3.5, +3.9) return xs:yearMonthDuration(\"P1M\") * $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-P4M -P3M -P3M -P1M P0M P0M P0M P1M P1M P3M P4M P4M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-yearMonthDuration-3                  
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "multiply-yearMonthDuration" function as 
   * part of a boolean expression (or operator) and the "fn:boolean" function. 
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration3() {
    final XQuery query = new XQuery(
      "fn:string((xs:yearMonthDuration(\"P20Y10M\") * 2.0)) or fn:false()",
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
   * Test: op-multiply-yearMonthDuration-4                  
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "multiply-yearMonthDuration" function that  
   * return true and used together with fn:not.             
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:yearMonthDuration(\"P11Y04M\") * 2.0))",
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
   * Test: op-multiply-yearMonthDuration-5                  
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "multiply-yearMonthDuration" function that  
   * is used as an argument to the fn:boolean function.     
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:yearMonthDuration(\"P05Y08M\") * 2.0))",
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
   * Test: op-multiply-yearMonthDuration-6                  
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "multiply-yearMonthDuration" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration6() {
    final XQuery query = new XQuery(
      "fn:number(xs:yearMonthDuration(\"P02Y09M\") * 2.0)",
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
   * Test: op-multiply-yearMonthDuration-7                  
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "multiply-yearMonthDuration" function used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration7() {
    final XQuery query = new XQuery(
      "fn:string(xs:yearMonthDuration(\"P03Y08M\") * 2.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P7Y4M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-yearMonthDuration-8                  
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "multiply-yearMonthDuration" function that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration8() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y01M\") * -2.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-P20Y2M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-yearMonthDuration-9                  
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "multiply-yearMonthDuration" function used 
   * together with and "and" expression.                    
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration9() {
    final XQuery query = new XQuery(
      "fn:string((xs:yearMonthDuration(\"P01Y01M\") * 2.0)) and fn:string((xs:yearMonthDuration(\"P02Y03M\") * 2.0))",
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
   * Test: op-multiply-yearMonthDuration2args-1              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:multiply-yearMonthDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration2args1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") * xs:double(\"-1.7976931348623157E308\")",
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
   * Test: op-multiply-yearMonthDuration2args-2              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:multiply-yearMonthDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(mid range)               
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration2args2() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1000Y6M\") * xs:double(\"-0\")",
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
   * Test: op-multiply-yearMonthDuration2args-3              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:multiply-yearMonthDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(upper bound)             
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration2args3() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P2030Y12M\") * xs:double(\"-0\")",
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
   * Test: op-multiply-yearMonthDuration2args-4              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:multiply-yearMonthDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:double(mid range)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration2args4() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") * xs:double(\"0\")",
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
   * Test: op-multiply-yearMonthDuration2args-5              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:multiply-yearMonthDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:double(upper bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyYearMonthDuration2args5() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") * xs:double(\"1.7976931348623157E308\")",
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
