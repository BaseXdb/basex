package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the subtract-dayTimeDurations() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpSubtractDayTimeDurations extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationSubtract-1                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of substraction an xs:dayTimeDuration with PT0S. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationSubtract1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT4H3M3.100S\") - xs:dayTimeDuration(\"PT0S\") eq xs:dayTimeDuration(\"P3DT4H3M3.100S\")",
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
   *  Test: K-DayTimeDurationSubtract-2                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of substraction PT0S with an xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationSubtract2() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"PT0S\") - xs:dayTimeDuration(\"P3DT4H3M3.100S\") eq xs:dayTimeDuration(\"-P3DT4H3M3.100S\")",
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
   *  Test: K-DayTimeDurationSubtract-3                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of substraction two arbitrary xs:dayTimeDurations. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationSubtract3() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"PT0S\") - xs:dayTimeDuration(\"P3DT4H3M3.100S\") eq xs:dayTimeDuration(\"-P3DT4H3M3.100S\")",
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
   *  Test: K-DayTimeDurationSubtract-4                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The substraction operator is not available between xs:dayTimeDuration and xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationSubtract4() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3D\") - xs:yearMonthDuration(\"P3Y3M\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationSubtract-5                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The substraction operator is not available between xs:yearMonthDuration and xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationSubtract5() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y3M\") - xs:dayTimeDuration(\"P3D\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationSubtract-6                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The substraction operator is not available between xs:duration and xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationSubtract6() {
    final XQuery query = new XQuery(
      "xs:duration(\"P3D\") - xs:yearMonthDuration(\"P3Y3M\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationSubtract-7                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The substraction operator is not available between xs:yearMonthDuration and xs:duration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationSubtract7() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y3M\") - xs:duration(\"P3D\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationSubtract-8                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The substraction operator is not available between xs:dayTimeDuration and xs:duration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationSubtract8() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3D\") - xs:duration(\"P3Y3M\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationSubtract-9                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The substraction operator is not available between xs:duration and xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationSubtract9() {
    final XQuery query = new XQuery(
      "xs:duration(\"P3Y3M\") - xs:dayTimeDuration(\"P3D\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K2-DayTimeDurationSubtract-1                    
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Ensure that a value from current-time() can be extracted. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void k2DayTimeDurationSubtract1() {
    final XQuery query = new XQuery(
      "(current-time() - xs:dayTimeDuration(\"PT3H\")) ne current-time()",
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
   *  Test: K2-DayTimeDurationSubtract-2                    
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Ensure that a value from current-dateTime() can be extracted. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void k2DayTimeDurationSubtract2() {
    final XQuery query = new XQuery(
      "(current-dateTime() - xs:dayTimeDuration(\"PT3H\")) lt current-dateTime()",
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
   * Test: op-subtract-dayTimeDurations-1                   
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-dayTimeDurations" function 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P2DT12H\") - xs:dayTimeDuration(\"P1DT10H30M\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "P1DT1H30M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDurations-10                  
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-dayTimeDurations" function used  
   * together with an "or" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations10() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P05DT09H02M\") - xs:dayTimeDuration(\"P03DT01H04M\"))) or fn:string((xs:dayTimeDuration(\"P05DT05H03M\") - xs:dayTimeDuration(\"P01DT01H03M\")))",
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
   * Test: op-subtract-dayTimeDurations-11                  
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-dayTimeDurations" function used  
   * together with a "div" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations11() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P42DT10H10M\") - xs:dayTimeDuration(\"P10DT10H10M\")) div (xs:dayTimeDuration(\"P20DT10H10M\") - xs:dayTimeDuration(\"P18DT10H10M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "16")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDurations-12                  
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-dayTimeDurations" operators used 
   * with a boolean expression and the "fn:true" function.   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations12() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P10DT08H11M\") - xs:dayTimeDuration(\"P05DT08H11M\"))) and (fn:true())",
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
   * Test: op-subtract-dayTimeDurations-13                  
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-dayTimeDurations" function used  
   * together with the numeric-equal-operator "eq".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations13() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P23DT11H11M\") - xs:dayTimeDuration(\"P23DT11H11M\")) eq xs:dayTimeDuration(\"P23DT11H11M\")",
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
   * Test: op-subtract-dayTimeDurations-14                  
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-dayTimeDurations" function used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations14() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P21DT08H12M\") - xs:dayTimeDuration(\"P08DT08H05M\")) ne xs:dayTimeDuration(\"P08DT08H05M\")",
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
   * Test: op-subtract-dayTimeDurations-15                  
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-dayTimeDurations" function used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations15() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT10H01M\") - xs:dayTimeDuration(\"P17DT10H02M\")) le xs:dayTimeDuration(\"P17DT10H02M\")",
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
   * Test: op-subtract-dayTimeDurations-16                  
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-dayTimeDurations" function used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations16() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P13DT09H09M\") - xs:dayTimeDuration(\"P18DT02H02M\")) ge xs:dayTimeDuration(\"P18DT02H02M\")",
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
   * Test: op-subtract-dayTimeDurations-2                   
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The string value of "subtract-dayTimeDurations" function 
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations2() {
    final XQuery query = new XQuery(
      "fn:string(xs:dayTimeDuration(\"P10DT10H11M\") - xs:dayTimeDuration(\"P12DT10H07M\")) and fn:false()",
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
   * Test: op-subtract-dayTimeDurations-3                   
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-dayTimeDurations" function as 
   * part of a boolean expression (or operator) and the "fn:boolean" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations3() {
    final XQuery query = new XQuery(
      "fn:string(xs:dayTimeDuration(\"P20DT20H10M\") - xs:dayTimeDuration(\"P19DT13H10M\")) or fn:false()",
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
   * Test: op-subtract-dayTimeDurations-4                   
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-dayTimeDurations" function that  
   * return true and used together with fn:not.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:dayTimeDuration(\"P11DT12H04M\") - xs:dayTimeDuration(\"P02DT07H01M\")))",
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
   * Test: op-subtract-dayTimeDurations-5                   
   * Written By: Carmelo Montanez                           
   * Date: June 29 2005                                     
   * Purpose: Evaluates The "subtract-dayTimeDurations" function that  
   * is used as an argument to the fn:boolean function.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:dayTimeDuration(\"P05DT09H08M\") - xs:dayTimeDuration(\"P03DT08H06M\")))",
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
   * Test: op-subtract-dayTimeDurations-6                   
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-dayTimeDurations" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations6() {
    final XQuery query = new XQuery(
      "fn:number(xs:dayTimeDuration(\"P02DT06H09M\") - xs:dayTimeDuration(\"P10DT08H01M\"))",
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
   * Test: op-subtract-dayTimeDurations-7                   
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-dayTimeDurations" function used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations7() {
    final XQuery query = new XQuery(
      "fn:string(xs:dayTimeDuration(\"P03DT04H08M\") - xs:dayTimeDuration(\"P01DT09H02M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "P1DT19H6M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDurations-8                   
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-dayTimeDurations" function that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations8() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT01H01M\") - xs:dayTimeDuration(\"P11DT02H02M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-P1DT1H1M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDurations-9                   
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "subtract-dayTimeDurations" function used 
   * together with and "and" expression.                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations9() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P01DT02H01M\") - xs:dayTimeDuration(\"P02DT09H02M\"))) and fn:string((xs:dayTimeDuration(\"P02DT03H03M\") - xs:dayTimeDuration(\"P04DT04H04M\")))",
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
   * Test: op-subtract-dayTimeDurations2args-1               
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-dayTimeDurations" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations2args1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") - xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-subtract-dayTimeDurations2args-2               
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-dayTimeDurations" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(mid range)                 
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations2args2() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P15DT11H59M59S\") - xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "P15DT11H59M59S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDurations2args-3               
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-dayTimeDurations" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(upper bound)               
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations2args3() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P31DT23H59M59S\") - xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "P31DT23H59M59S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDurations2args-4               
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-dayTimeDurations" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(mid range)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations2args4() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") - xs:dayTimeDuration(\"P15DT11H59M59S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-P15DT11H59M59S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDurations2args-5               
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-dayTimeDurations" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(upper bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurations2args5() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") - xs:dayTimeDuration(\"P31DT23H59M59S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-P31DT23H59M59S")
    );
  }
}
