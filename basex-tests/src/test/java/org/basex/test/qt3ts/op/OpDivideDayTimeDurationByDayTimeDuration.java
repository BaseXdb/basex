package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the divide-dayTimeDuration-by-dayTimeDuration() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpDivideDayTimeDurationByDayTimeDuration extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationDivideDTD-1                    
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of dividing a xs:dayTimeDuration with xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationDivideDTD1() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"PT8M\") div xs:dayTimeDuration(\"PT2M\")) eq 4",
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
   *  test division of xs:dayTimeDuration by zero .
   */
  @org.junit.Test
  public void cbclDivideDayTimeDurationByDayTimeDuration001() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P9223372036854775807D\") div xs:dayTimeDuration(\"P0D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("FOAR0001")
      ||
        error("FODT0002")
      )
    );
  }

  /**
   *  test division of xs:dayTimeDuration by a small duration .
   */
  @org.junit.Test
  public void cbclDivideDayTimeDurationByDayTimeDuration002() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P9223372036854775807D\") div xs:dayTimeDuration(\"P0DT0H0M0.000000001S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("FOAR0002")
      ||
        error("FODT0002")
      )
    );
  }

  /**
   *  test division of xs:dayTimeDuration by a small duration .
   */
  @org.junit.Test
  public void cbclDivideDayTimeDurationByDayTimeDuration003() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P9223372036854775806D\") div xs:dayTimeDuration(\"P4611686018427387903D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("2")
      ||
        error("FODT0002")
      )
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-dayTimeDuration-by-dTD-1             
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration-by-DTD" operator 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDTD1() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even((xs:dayTimeDuration(\"P2DT53M11S\") div xs:dayTimeDuration(\"P1DT10H\")),15)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("1.437834967320261")
      ||
        assertEq("1.4378349673")
      )
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-dayTimeDuration-by-dTD-10              
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration-by-dTD" operator used  
   * together with an "or" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDTD10() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P05DT09H02M\") div xs:dayTimeDuration(\"P03DT01H04M\")) or (xs:dayTimeDuration(\"P05DT05H03M\") div xs:dayTimeDuration(\"P01DT01H03M\"))",
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
   * Test: op-divide-dayTimeDuration-by-dTD-11              
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration-by-dTD" operator used  
   * together with multiple "div" expressions.              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDTD11() {
    final XQuery query = new XQuery(
      "fn:round-half-to-even( (xs:dayTimeDuration(\"P42DT10H10M\") div xs:dayTimeDuration(\"P10DT10H10M\")) div (xs:dayTimeDuration(\"P20DT10H10M\") div xs:dayTimeDuration(\"P18DT10H10M\")) ,15)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3.671399617754547")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-dayTimeDuration-by-dTD-12              
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration-by-dTD" operators used 
   * with a boolean expression and the "fn:true" function.   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDTD12() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT08H11M\") div xs:dayTimeDuration(\"P05DT08H11M\")) and (fn:true())",
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
   * Test: op-divide-dayTimeDuration-by-dTD-13              
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration-by-dTD" operator used  
   * together with the numeric-equal-operator "eq".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDTD13() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P23DT11H11M\") div xs:dayTimeDuration(\"P23DT11H11M\")) eq xs:decimal(2.0)",
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
   * Test: op-divide-dayTimeDuration-by-dTD-14              
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration-by-dTD" operator used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDTD14() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P21DT08H12M\") div xs:dayTimeDuration(\"P08DT08H05M\")) ne xs:decimal(2.0)",
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
   * Test: op-divide-dayTimeDuration-by-dTD-15              
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration-by-DTD" function used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDTD15() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT10H01M\") div xs:dayTimeDuration(\"P17DT10H02M\")) le xs:decimal(2.0)",
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
   * Test: op-divide-dayTimeDuration-by-dTD-16              
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration-by-dTD" operator used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDTD16() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P13DT09H09M\") div xs:dayTimeDuration(\"P18DT02H02M\")) ge xs:decimal(2.0)",
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
   * Test: op-divide-dayTimeDuration-by-dTD-2               
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration-by-dTD" operator 
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDTD2() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P10DT10H11M\") div xs:dayTimeDuration(\"P12DT10H07M\") and fn:false()",
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
   * Test: op-divide-dayTimeDuration-by-dTD-3               
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration-by-dTD" operator as 
   * part of a boolean expression (or operator) and the "fn:boolean" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDTD3() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P20DT20H10M\") div xs:dayTimeDuration(\"P19DT13H10M\") or fn:false()",
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
   * Test: op-divide-dayTimeDuration-by-dTD-4               
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration-by-dTD" operator that  
   * return true and used together with fn:not.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDTD4() {
    final XQuery query = new XQuery(
      "fn:not(xs:dayTimeDuration(\"P11DT12H04M\") div xs:dayTimeDuration(\"P02DT07H01M\"))",
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
   * Test: op-divide-dayTimeDuration-by-dTD-5               
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                     
   * Purpose: Evaluates The "divide-dayTimeDuration-by-dTD" operator that  
   * is used as an argument to the fn:boolean function.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDTD5() {
    final XQuery query = new XQuery(
      "fn:boolean(xs:dayTimeDuration(\"P05DT09H08M\") div xs:dayTimeDuration(\"P03DT08H06M\"))",
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
   * Test: op-divide-dayTimeDuration-by-dTD-6               
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration-by-dTD" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDTD6() {
    final XQuery query = new XQuery(
      "fn:number(xs:dayTimeDuration(\"P02DT06H09M\") div xs:dayTimeDuration(\"P02DT06H09M\"))",
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
   * Test: op-divide-dayTimeDuration-by-dTD-7               
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration-by-dTD" operator used  
   * as an argument to the "fn:string" function.            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDTD7() {
    final XQuery query = new XQuery(
      "fn:string(xs:dayTimeDuration(\"P08DT06H08M\") div xs:dayTimeDuration(\"P08DT06H08M\"))",
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
   * Test: op-divide-dayTimeDuration-by-dTD-8               
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration-by-dTD" operator that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDTD8() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT01H01M\") div xs:dayTimeDuration(\"-P10DT01H01M\"))",
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
   * Test: op-divide-dayTimeDuration-by-dTD-9               
   * Written By: Carmelo Montanez                           
   * Date: June 30, 2005                                    
   * Purpose: Evaluates The "divide-dayTimeDuration-by-dTD" duration used 
   * together with and "and" expression.                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDTD9() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P01DT02H01M\") div xs:dayTimeDuration(\"P02DT09H02M\")) and (xs:dayTimeDuration(\"P01DT02H01M\") div xs:dayTimeDuration(\"P02DT09H02M\"))",
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
   * Test: op-divide-dayTimeDuration-by-dayTimeDuration2args-1
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-dayTimeDuration-by-dayTimeDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDayTimeDuration2args1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") div xs:dayTimeDuration(\"P0DT0H0M01S\")",
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
   * Test: op-divide-dayTimeDuration-by-dayTimeDuration2args-2
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-dayTimeDuration-by-dayTimeDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(mid range)                 
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDayTimeDuration2args2() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P15DT11H59M59S\") div xs:dayTimeDuration(\"P0DT0H0M01S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1339199")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-dayTimeDuration-by-dayTimeDuration2args-3
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-dayTimeDuration-by-dayTimeDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(upper bound)               
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDayTimeDuration2args3() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P31DT23H59M59S\") div xs:dayTimeDuration(\"P0DT0H0M01S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2764799")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-divide-dayTimeDuration-by-dayTimeDuration2args-4
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-dayTimeDuration-by-dayTimeDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(mid range)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDayTimeDuration2args4() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") div xs:dayTimeDuration(\"P15DT11H59M59S\")",
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
   * Test: op-divide-dayTimeDuration-by-dayTimeDuration2args-5
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:divide-dayTimeDuration-by-dayTimeDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(upper bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDivideDayTimeDurationByDayTimeDuration2args5() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") div xs:dayTimeDuration(\"P31DT23H59M59S\")",
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
