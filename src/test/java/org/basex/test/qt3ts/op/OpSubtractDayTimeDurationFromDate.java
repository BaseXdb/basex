package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the subtract-dayTimeDuration-from-date() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpSubtractDayTimeDurationFromDate extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DateSubtractDTD-1                             
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple testing involving operator '-' between xs:date and xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateSubtractDTD1() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-08-12\") - xs:dayTimeDuration(\"P23DT09H32M59S\") eq xs:date(\"1999-07-19\")",
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
   * Test: op-subtract-dayTimeDuration-from-date-1          
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "subtract-dayTimeDuration-from-date" operator 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate1() {
    final XQuery query = new XQuery(
      "xs:date(\"2000-10-30\") - xs:dayTimeDuration(\"P3DT1H15M\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2000-10-26")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDuration-from-date-10         
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of "subtract-dayTimeDuration-from-date" operator used  
   * together with an "or" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate10() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1985-07-05Z\") - xs:dayTimeDuration(\"P03DT01H04M\"))) or fn:string((xs:date(\"1985-07-05Z\") - xs:dayTimeDuration(\"P01DT01H03M\")))",
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
   * Test: op-subtract-dayTimeDuration-from-date-12         
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of "subtract-dayTimeDuration-from-date" operator used 
   * with a boolean expression and the "fn:true" function.   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate12() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1980-03-02Z\") - xs:dayTimeDuration(\"P05DT08H11M\"))) and (fn:true())",
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
   * Test: op-subtract-dayTimeDuration-from-date-13         
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "subtract-dayTimeDuration-from-date" operator used  
   * together with the numeric-equal-operator "eq".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate13() {
    final XQuery query = new XQuery(
      "(xs:date(\"1980-05-05Z\") - xs:dayTimeDuration(\"P23DT11H11M\")) eq xs:date(\"1980-05-05Z\")",
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
   * Test: op-subtract-dayTimeDuration-from-date-14         
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "subtract-dayTimeDuration-from-date" operator used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate14() {
    final XQuery query = new XQuery(
      "(xs:date(\"1979-12-12Z\") - xs:dayTimeDuration(\"P08DT08H05M\")) ne xs:date(\"1979-12-12Z\")",
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
   * Test: op-subtract-dayTimeDuration-from-date-15         
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "subtract-dayTimeDuration-from-date" operator used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate15() {
    final XQuery query = new XQuery(
      "(xs:date(\"1978-12-12Z\") - xs:dayTimeDuration(\"P17DT10H02M\")) le xs:date(\"1978-12-12Z\")",
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
   * Test: op-subtract-dayTimeDuration-from-date-16         
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "subtract-dayTimeDuration-from-date" operator used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate16() {
    final XQuery query = new XQuery(
      "(xs:date(\"1977-12-12Z\") - xs:dayTimeDuration(\"P18DT02H02M\")) ge xs:date(\"1977-12-12Z\")",
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
   * Test: op-subtract-dayTimeDuration-from-date-2          
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of "subtract-dayTimeDuration-from-date" operator 
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate2() {
    final XQuery query = new XQuery(
      "fn:string(xs:date(\"2000-12-12Z\") - xs:dayTimeDuration(\"P12DT10H07M\")) and fn:false()",
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
   * Test: op-subtract-dayTimeDuration-from-date-3          
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of "subtract-dayTimeDuration-from-date" operator as 
   * part of a boolean expression (or operator) and the "fn:boolean" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate3() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1999-10-23Z\") - xs:dayTimeDuration(\"P19DT13H10M\"))) or fn:false()",
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
   * Test: op-subtract-dayTimeDuration-from-date-4          
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of "subtract-dayTimeDuration-from-date" operator that  
   * return true and used together with fn:not.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:date(\"1998-09-12Z\") - xs:dayTimeDuration(\"P02DT07H01M\")))",
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
   * Test: op-subtract-dayTimeDuration-from-date-5          
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of "subtract-dayTimeDuration-from-date" operator that  
   * is used as an argument to the fn:boolean function.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:date(\"1962-03-12Z\") - xs:dayTimeDuration(\"P03DT08H06M\")))",
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
   * Test: op-subtract-dayTimeDuration-from-date-6          
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "subtract-dayTimeDuration-from-date" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate6() {
    final XQuery query = new XQuery(
      "fn:number(xs:date(\"1988-01-28Z\") - xs:dayTimeDuration(\"P10DT08H01M\"))",
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
   * Test: op-subtract-dayTimeDuration-from-date-7          
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "subtract-dayTimeDuration-from-date" operator used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate7() {
    final XQuery query = new XQuery(
      "fn:string(xs:date(\"1989-07-05Z\") - xs:dayTimeDuration(\"P01DT09H02M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1989-07-03Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDuration-from-date-8          
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "subtract-dayTimeDuration-from-date" operator that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate8() {
    final XQuery query = new XQuery(
      "(xs:date(\"0001-01-01Z\") - xs:dayTimeDuration(\"P11DT02H02M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "-0001-12-20Z")
      ||
        assertStringValue(false, "0000-12-20Z")
      )
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDuration-from-date-9          
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of "subtract-dayTimeDuration-from-date" operator used  
   * together with an "and" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate9() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1993-12-09Z\") - xs:dayTimeDuration(\"P03DT01H04M\"))) and fn:string((xs:date(\"1993-12-09Z\") - xs:dayTimeDuration(\"P01DT01H03M\")))",
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
   * Test: op-subtract-dayTimeDuration-from-date2args-1      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-dayTimeDuration-from-date" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate2args1() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") - xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1970-01-01Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDuration-from-date2args-2      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-dayTimeDuration-from-date" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(mid range)                             
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate2args2() {
    final XQuery query = new XQuery(
      "xs:date(\"1983-11-17Z\") - xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1983-11-17Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDuration-from-date2args-3      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-dayTimeDuration-from-date" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(upper bound)                           
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate2args3() {
    final XQuery query = new XQuery(
      "xs:date(\"2030-12-31Z\") - xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2030-12-31Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDuration-from-date2args-4      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-dayTimeDuration-from-date" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:dayTimeDuration(mid range)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate2args4() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") - xs:dayTimeDuration(\"P15DT11H59M59S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1969-12-16Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dayTimeDuration-from-date2args-5      
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-dayTimeDuration-from-date" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:dayTimeDuration(upper bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDayTimeDurationFromDate2args5() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") - xs:dayTimeDuration(\"P31DT23H59M59S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1969-11-30Z")
    );
  }
}
