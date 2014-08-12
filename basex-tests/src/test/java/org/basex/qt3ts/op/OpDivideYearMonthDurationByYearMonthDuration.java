package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the divide-yearMonthDuration-by-yearMonthDuration() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpDivideYearMonthDurationByYearMonthDuration extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-YearMonthDurationDivideYMD-1                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of dividing a xs:yearMonthDuration with xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationDivideYMD1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y36M\") div xs:yearMonthDuration(\"P60Y\") eq 0.1",
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
   *  test division of xs:yearMonthDuration by 0 .
   */
  @org.junit.Test
  public void cbclDivideYearMonthDurationByYearMonthDuration001() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P2Y\") div xs:yearMonthDuration(\"P0Y\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0001")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-yearMonthDuration-by-YMD-1             
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration-by-YMD" operator 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYMD1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y4M\") div xs:yearMonthDuration(\"-P1Y4M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-2.5")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-yearMonthDuration-by-YMD-10            
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration-by-YMD" operator used  
   * together with an "or" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYMD10() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P05Y02M\") div xs:yearMonthDuration(\"P03Y04M\")) or (xs:yearMonthDuration(\"P05Y03M\") div xs:yearMonthDuration(\"P01Y03M\"))",
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
   * Test: op-divide-yearMonthDuration-by-YMD-11            
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration-by-YMD" operator used  
   * together with multiple "div" expressions.              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYMD11() {
    final XQuery query = new XQuery(
      "round-half-to-even( (xs:yearMonthDuration(\"P42Y10M\") div xs:yearMonthDuration(\"P20Y10M\")) div (xs:yearMonthDuration(\"P20Y11M\") div xs:yearMonthDuration(\"P18Y11M\")), 15)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1.859410358565737")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-yearMonthDuration-by-YMD-12            
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration-by-YMD" operator used 
   * with a boolean expression and the "fn:true" function.   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYMD12() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y11M\") div xs:yearMonthDuration(\"P05Y07M\")) and (fn:true())",
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
   * Test: op-divide-yearMonthDuration-by-YMD-13            
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration-by-YMD" operator used  
   * together with the numeric-equal- operator "eq".        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYMD13() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P23Y11M\") div xs:yearMonthDuration(\"P23Y11M\")) eq xs:decimal(2.0)",
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
   * Test: op-divide-yearMonthDuration-by-YMD-14            
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration-by-YMD" operator used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYMD14() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P21Y12M\") div xs:yearMonthDuration(\"P08Y05M\")) ne xs:decimal(2.0)",
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
   * Test: op-divide-yearMonthDuration-by-YMD-15            
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration-by-YMD" operator used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYMD15() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y01M\") div xs:yearMonthDuration(\"P17Y02M\")) le xs:decimal(2.0)",
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
   * Test: op-divide-yearMonthDuration-by-YMD-16            
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration-by-YMD" operator used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYMD16() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P13Y09M\") div xs:yearMonthDuration(\"P18Y02M\")) ge xs:decimal(2.0)",
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
   * Test: op-divide-yearMonthDuration-by-YMD-2             
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration-by-YMD" operator 
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYMD2() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P10Y11M\") div xs:yearMonthDuration(\"P12Y07M\") and fn:false()",
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
   * Test: op-divide-yearMonthDuration-by-YMD-3             
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration-by-YMD" operator as 
   * part of a boolean expression (or operator) and the "fn:boolean" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYMD3() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P20Y10M\") div xs:yearMonthDuration(\"P19Y10M\") or fn:false()",
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
   * Test: op-divide-yearMonthDuration-by-YMD-4             
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration-by-YMD" operator that  
   * return true and used together with fn:not.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYMD4() {
    final XQuery query = new XQuery(
      "fn:not(xs:yearMonthDuration(\"P11Y04M\") div xs:yearMonthDuration(\"P02Y11M\"))",
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
   * Test: op-divide-yearMonthDuration-by-YMD-5             
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration-by-YMD" operator that  
   * is used as an argument to the fn:boolean function.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYMD5() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:yearMonthDuration(\"P05Y08M\") div xs:yearMonthDuration(\"P03Y06M\"))",
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
   * Test: op-divide-yearMonthDuration-by-YMD-6             
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration-by-YMD" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYMD6() {
    final XQuery query = new XQuery(
      "fn:number(xs:yearMonthDuration(\"P02Y09M\") div xs:yearMonthDuration(\"P02Y09M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-yearMonthDuration-by-YMD-7             
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration-by-YMD" operator used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYMD7() {
    final XQuery query = new XQuery(
      "fn:string(xs:yearMonthDuration(\"P03Y08M\") div xs:yearMonthDuration(\"P03Y08M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"1\"")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-yearMonthDuration-by-YMD-8             
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration-by-YMD" operator that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYMD8() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y01M\") div xs:yearMonthDuration(\"-P10Y01M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-yearMonthDuration-by-YMD-9             
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-yearMonthDuration-by-YMD" operator used 
   * together with and "and" expression.                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYMD9() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P01Y01M\") div xs:yearMonthDuration(\"P02Y02M\")) and (xs:yearMonthDuration(\"P02Y03M\") div xs:yearMonthDuration(\"P04Y04M\"))",
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
   * Test: op-divide-yearMonthDuration-by-yearMonthDuration2args-1
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-yearMonthDuration-by-yearMonthDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYearMonthDuration2args1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") div xs:yearMonthDuration(\"P0Y1M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-yearMonthDuration-by-yearMonthDuration2args-2
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-yearMonthDuration-by-yearMonthDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(mid range)               
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYearMonthDuration2args2() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1000Y6M\") div xs:yearMonthDuration(\"P0Y1M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("12006")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-yearMonthDuration-by-yearMonthDuration2args-3
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-yearMonthDuration-by-yearMonthDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(upper bound)             
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYearMonthDuration2args3() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P2030Y12M\") div xs:yearMonthDuration(\"P0Y1M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("24372")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-yearMonthDuration-by-yearMonthDuration2args-4
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-yearMonthDuration-by-yearMonthDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(mid range)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYearMonthDuration2args4() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") div xs:yearMonthDuration(\"P1000Y6M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-yearMonthDuration-by-yearMonthDuration2args-5
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-yearMonthDuration-by-yearMonthDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(upper bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideYearMonthDurationByYearMonthDuration2args5() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") div xs:yearMonthDuration(\"P2030Y12M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }
}
