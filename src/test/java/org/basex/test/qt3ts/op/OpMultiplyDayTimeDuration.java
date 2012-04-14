package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the multiply-dayTimeDuration() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpMultiplyDayTimeDuration extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationMultiply-1                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of multiplying a xs:dayTimeDuration with 3. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationMultiply1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT4H3M3.100S\") * 3 eq xs:dayTimeDuration(\"P9DT12H9M9.3S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationMultiply-2                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of multiplying 3 with xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationMultiply2() {
    final XQuery query = new XQuery(
      "3 * xs:dayTimeDuration(\"P3DT4H3M3.100S\") eq xs:dayTimeDuration(\"P9DT12H9M9.3S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationMultiply-3                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of multiplying a xs:dayTimeDuration with 0. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationMultiply3() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT4H3M3.100S\") * 0 eq xs:dayTimeDuration(\"PT0S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationMultiply-4                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of multiplying 0 with xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationMultiply4() {
    final XQuery query = new XQuery(
      "0 * xs:dayTimeDuration(\"P3DT4H3M3.100S\") eq xs:dayTimeDuration(\"PT0S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationMultiply-5                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of multiplying a xs:dayTimeDuration with -0. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationMultiply5() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT4H3M3.100S\") * xs:double(\"-0\") eq xs:dayTimeDuration(\"PT0S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationMultiply-6                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of multiplying -0 with xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationMultiply6() {
    final XQuery query = new XQuery(
      "xs:double(\"-0\") * xs:dayTimeDuration(\"P3DT4H3M3.100S\") eq xs:dayTimeDuration(\"PT0S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationMultiply-7                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of multiplying a xs:dayTimeDuration with INF. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationMultiply7() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT4H3M3.100S\") * xs:double(\"INF\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODT0002")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationMultiply-8                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of multiplying a xs:dayTimeDuration with -INF. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationMultiply8() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT4H3M3.100S\") * xs:double(\"-INF\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODT0002")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationMultiply-9                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of multiplying a xs:dayTimeDuration with NaN. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationMultiply9() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT4H3M3.100S\") * xs:double(\"NaN\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOCA0005")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration-1                    
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "multiply-dayTimeDuration" function 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"PT2H10M\") * 2.1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "PT4H33M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration-10                   
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "multiply-dayTimeDuration" function used  
   * together with an "or" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration10() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P05DT09H02M\") * 2.0)) or fn:string((xs:dayTimeDuration(\"P05DT05H03M\") * 2.0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration-11                   
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "multiply-dayTimeDuration" function used  
   * together with a "div" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration11() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P42DT10H10M\") * 2.0) div (xs:dayTimeDuration(\"P42DT10H10M\") * 2.0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration-12                   
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "multiply-dayTimeDuration" operators used 
   * with a boolean expression and the "fn:true" function.   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration12() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P10DT08H11M\") * 2.0)) and (fn:true())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration-13                   
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "multiply-dayTimeDuration" function used  
   * together with the numeric-equal-operator "eq".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration13() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P23DT11H11M\") * 2.0) eq xs:dayTimeDuration(\"P23DT11H11M\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration-14                   
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "multiply-dayTimeDuration" function used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration14() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P21DT08H12M\") * 2.0) ne xs:dayTimeDuration(\"P08DT08H05M\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration-15                   
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "multiply-dayTimeDuration" function used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration15() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT10H01M\") * 2.0) le xs:dayTimeDuration(\"P17DT10H02M\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration-16                   
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "multiply-dayTimeDuration" function used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration16() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P13DT09H09M\") * 2.0) ge xs:dayTimeDuration(\"P18DT02H02M\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration-17                   
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "multiply-dayTimeDuration" function 
   * multiplied by -0. Use fn:count to avoid empty file.    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration17() {
    final XQuery query = new XQuery(
      "fn:count(xs:dayTimeDuration(\"P13DT09H09M\") * -0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration-18                   
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "multiply-dayTimeDuration" function 
   * multiplied by +0. Use fn:count to avoid empty file.    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration18() {
    final XQuery query = new XQuery(
      "fn:count(xs:dayTimeDuration(\"P13DT09H09M\") * +0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration-2                    
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "multiply-dayTimeDuration" function 
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration2() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P10DT10H11M\")) * 2.0) and fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration-3                    
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "multiply-dayTimeDuration" function as 
   * part of a boolean expression (or operator) and the "fn:boolean" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration3() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P20DT20H10M\") * 2.0)) or fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration-4                    
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "multiply-dayTimeDuration" function that  
   * return true and used together with fn:not.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:dayTimeDuration(\"P11DT12H04M\") * 2.0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration-5                    
   * Written By: Carmelo Montanez                           
   * Date: June 29 2005                                     
   * Purpose: Evaluates The "multiply-dayTimeDuration" function that  
   * is used as an argument to the fn:boolean function.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:dayTimeDuration(\"P05DT09H08M\") *2.0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration-6                    
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "multiply-dayTimeDuration" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration6() {
    final XQuery query = new XQuery(
      "fn:number(xs:dayTimeDuration(\"P02DT06H09M\") *2.0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration-7                    
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "multiply-dayTimeDuration" function used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration7() {
    final XQuery query = new XQuery(
      "fn:string(xs:dayTimeDuration(\"P03DT04H08M\") * 2.0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "P6DT8H16M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration-8                    
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "multiply-dayTimeDuration" function that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration8() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT01H01M\") * -2.0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-P20DT2H2M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration-9                    
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "multiply-dayTimeDuration" function used 
   * together with and "and" expression.                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration9() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P01DT02H01M\") * 2.0)) and fn:string((xs:dayTimeDuration(\"P02DT03H03M\") * 2.0 ))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration2args-1                
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:multiply-dayTimeDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration2args1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") * xs:double(\"-1.7976931348623157E308\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration2args-2                
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:multiply-dayTimeDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(mid range)                 
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration2args2() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P15DT11H59M59S\") * xs:double(\"0\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration2args-3                
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:multiply-dayTimeDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(upper bound)               
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration2args3() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P31DT23H59M59S\") * xs:double(\"0\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration2args-4                
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:multiply-dayTimeDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:double(mid range)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration2args4() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") * xs:double(\"0\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-multiply-dayTimeDuration2args-5                
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:multiply-dayTimeDuration" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:double(upper bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opMultiplyDayTimeDuration2args5() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") * xs:double(\"1.7976931348623157E308\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "PT0S")
    );
  }
}
