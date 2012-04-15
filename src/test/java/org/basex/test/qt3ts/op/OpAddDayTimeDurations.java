package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the add-dayTimeDurations() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpAddDayTimeDurations extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationAdd-1                          
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of adding an xs:dayTimeDuration with PT0S. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationAdd1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT4H3M3.100S\") + xs:dayTimeDuration(\"PT0S\") eq xs:dayTimeDuration(\"P3DT4H3M3.100S\")",
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
   *  Test: K-DayTimeDurationAdd-2                          
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of substraction PT0S with an xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationAdd2() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"PT0S\") + xs:dayTimeDuration(\"P3DT4H3M3.100S\") eq xs:dayTimeDuration(\"P3DT4H3M3.100S\")",
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
   *  Test: K-DayTimeDurationAdd-3                          
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of adding two arbitrary xs:yearMonthDurations. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationAdd3() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT4H3M3.100S\") + xs:dayTimeDuration(\"P3DT12H31M56.303S\") eq xs:dayTimeDuration(\"P6DT16H34M59.403S\")",
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
   * Test: op-add-dayTimeDurations-1                        
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P2DT12H5M\") + xs:dayTimeDuration(\"P5DT12H\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "P8DT5M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations-10                       
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function used  
   * together with an "or" expression.                      
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations10() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P05DT09H02M\") + xs:dayTimeDuration(\"P03DT01H04M\"))) or fn:string((xs:dayTimeDuration(\"P05DT05H03M\") + xs:dayTimeDuration(\"P01DT01H03M\")))",
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
   * Test: op-add-dayTimeDurations-11                       
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function used  
   * together with a "div" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations11() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P42DT10H10M\") + xs:dayTimeDuration(\"P10DT10H10M\")) div (xs:dayTimeDuration(\"P42DT10H10M\") + xs:dayTimeDuration(\"P10DT10H10M\"))",
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
   * Test: op-add-dayTimeDurations-12                       
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" operators used 
   * with a boolean expression and the "fn:true" function.   
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations12() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P10DT08H11M\") + xs:dayTimeDuration(\"P05DT08H11M\"))) and (fn:true())",
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
   * Test: op-add-dayTimeDurations-13                       
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function used  
   * together with the numeric-equal-operator "eq".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations13() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P23DT11H11M\") + xs:dayTimeDuration(\"P23DT11H11M\")) eq xs:dayTimeDuration(\"P23DT11H11M\")",
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
   * Test: op-add-dayTimeDurations-14                       
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations14() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P21DT08H12M\") + xs:dayTimeDuration(\"P08DT08H05M\")) ne xs:dayTimeDuration(\"P08DT08H05M\")",
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
   * Test: op-add-dayTimeDurations-15                       
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations15() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT10H01M\") + xs:dayTimeDuration(\"P17DT10H02M\")) le xs:dayTimeDuration(\"P17DT10H02M\")",
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
   * Test: op-add-dayTimeDurations-16                       
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations16() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P13DT09H09M\") + xs:dayTimeDuration(\"P18DT02H02M\")) ge xs:dayTimeDuration(\"P18DT02H02M\")",
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
   * Test: op-add-dayTimeDurations-2                        
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function 
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   *  Uses "fn:string" to account for new EBV rules.        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations2() {
    final XQuery query = new XQuery(
      "fn:string(xs:dayTimeDuration(\"P10DT10H11M\") + xs:dayTimeDuration(\"P12DT10H07M\")) and fn:false()",
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
   * Test: op-add-dayTimeDurations-3                        
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function as 
   * part of a boolean expression (or operator) and the "fn:boolean" function. 
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations3() {
    final XQuery query = new XQuery(
      "fn:string(xs:dayTimeDuration(\"P20DT20H10M\") + xs:dayTimeDuration(\"P19DT13H10M\")) or fn:false()",
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
   * Test: op-add-dayTimeDurations-4                        
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function that  
   * return true and used together with fn:not.             
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:dayTimeDuration(\"P11DT12H04M\") + xs:dayTimeDuration(\"P02DT07H01M\")))",
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
   * Test: op-add-dayTimeDurations-5                        
   * Written By: Carmelo Montanez                           
   * Date: June 29 2005                                     
   * Purpose: Evaluates The "add-dayTimeDurations" function that  
   * is used as an argument to the fn:boolean function.     
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:dayTimeDuration(\"P05DT09H08M\") + xs:dayTimeDuration(\"P03DT08H06M\")))",
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
   * Test: op-add-dayTimeDurations-6                        
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations6() {
    final XQuery query = new XQuery(
      "fn:number(xs:dayTimeDuration(\"P02DT06H09M\") + xs:dayTimeDuration(\"P10DT08H01M\"))",
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
   * Test: op-add-dayTimeDurations-7                        
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations7() {
    final XQuery query = new XQuery(
      "fn:string(xs:dayTimeDuration(\"P03DT04H08M\") + xs:dayTimeDuration(\"P01DT09H02M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "P4DT13H10M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDurations-8                        
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations8() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT01H01M\") + xs:dayTimeDuration(\"-P11DT02H02M\"))",
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
   * Test: op-add-dayTimeDurations-9                        
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-dayTimeDurations" function used 
   * together with and "and" expression.                    
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations9() {
    final XQuery query = new XQuery(
      "fn:string((xs:dayTimeDuration(\"P01DT02H01M\") + xs:dayTimeDuration(\"P02DT09H02M\"))) and fn:string((xs:dayTimeDuration(\"P02DT03H03M\") + xs:dayTimeDuration(\"P04DT04H04M\")))",
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
   * Test: op-add-dayTimeDurations2args-1                    
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDurations" operator
   *  with the arguments set as follows:                    
   * Sarg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations2args1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") + xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-add-dayTimeDurations2args-2                    
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDurations" operator
   *  with the arguments set as follows:                    
   * Sarg1 = xs:dayTimeDuration(mid range)                 
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations2args2() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P15DT11H59M59S\") + xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-add-dayTimeDurations2args-3                    
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDurations" operator
   *  with the arguments set as follows:                    
   * Sarg1 = xs:dayTimeDuration(upper bound)               
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations2args3() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P31DT23H59M59S\") + xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-add-dayTimeDurations2args-4                    
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDurations" operator
   *  with the arguments set as follows:                    
   * Sarg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(mid range)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations2args4() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") + xs:dayTimeDuration(\"P15DT11H59M59S\")",
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
   * Test: op-add-dayTimeDurations2args-5                    
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDurations" operator
   *  with the arguments set as follows:                    
   * Sarg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(upper bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurations2args5() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") + xs:dayTimeDuration(\"P31DT23H59M59S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "P31DT23H59M59S")
    );
  }
}
