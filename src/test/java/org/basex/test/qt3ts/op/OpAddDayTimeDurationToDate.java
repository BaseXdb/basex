package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the add-dayTimeDuration-to-date() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpAddDayTimeDurationToDate extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DateAddDTD-1                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple testing involving operator '+' between xs:date and xs:dayTimeDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateAddDTD1() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-08-12\") + xs:dayTimeDuration(\"P23DT09H32M59S\") eq xs:date(\"1999-09-04\")",
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
   *  Test: K-DateAddDTD-2                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple testing involving operator '+' between xs:dayTimeDuration and xs:date. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateAddDTD2() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P23DT09H32M59S\") + xs:date(\"1999-08-12\") eq xs:date(\"1999-09-04\")",
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
   * Test: op-add-dayTimeDuration-to-date-1                 
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-date" operator 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate1() {
    final XQuery query = new XQuery(
      "xs:date(\"2004-10-30Z\") + xs:dayTimeDuration(\"P2DT2H30M0S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2004-11-01Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDuration-to-date-10                
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value "add-dayTimeDuration-to-date" operator used  
   * together with an "or" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate10() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1985-07-05Z\") + xs:dayTimeDuration(\"P03DT01H04M\"))) or fn:string((xs:date(\"1985-07-05Z\") + xs:dayTimeDuration(\"P01DT01H03M\")))",
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
   * Test: op-add-dayTimeDuration-to-date-12                
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value of "add-dayTimeDuration-to-date" operator used 
   * with a boolean expression and the "fn:true" function.   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate12() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1980-03-02Z\") + xs:dayTimeDuration(\"P05DT08H11M\"))) and (fn:true())",
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
   * Test: op-add-dayTimeDuration-to-date-13                
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-date" operator used  
   * together with the numeric-equal-operator "eq".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate13() {
    final XQuery query = new XQuery(
      "(xs:date(\"1980-05-05Z\") + xs:dayTimeDuration(\"P23DT11H11M\")) eq xs:date(\"1980-05-05Z\")",
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
   * Test: op-add-dayTimeDuration-to-date-14                
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-date" operator used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate14() {
    final XQuery query = new XQuery(
      "(xs:date(\"1979-12-12Z\") + xs:dayTimeDuration(\"P08DT08H05M\")) ne xs:date(\"1979-12-12Z\")",
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
   * Test: op-add-dayTimeDuration-to-date-15                
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-date" operator used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate15() {
    final XQuery query = new XQuery(
      "(xs:date(\"1978-12-12Z\") + xs:dayTimeDuration(\"P17DT10H02M\")) le xs:date(\"1978-12-12Z\")",
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
   * Test: op-add-dayTimeDuration-to-date-16                
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-date" operator used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate16() {
    final XQuery query = new XQuery(
      "(xs:date(\"1977-12-12Z\") + xs:dayTimeDuration(\"P18DT02H02M\")) ge xs:date(\"1977-12-12Z\")",
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
   * Test: op-add-dayTimeDuration-to-date-3                 
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value "add-dayTimeDuration-to-date" operator as 
   * part of a boolean expression (or operator) and the "fn:boolean" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate3() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1999-10-23Z\") + xs:dayTimeDuration(\"P19DT13H10M\"))) or fn:false()",
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
   * Test: op-add-dayTimeDuration-to-date-4                 
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value"add-dayTimeDuration-to-date" operator that  
   * return true and used together with fn:not.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:date(\"1998-09-12Z\") + xs:dayTimeDuration(\"P02DT07H01M\")))",
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
   * Test: op-add-dayTimeDuration-to-date-5                 
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value  "add-dayTimeDuration-to-date" operator that  
   * is used as an argument to the fn:boolean function.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:date(\"1962-03-12Z\") + xs:dayTimeDuration(\"P03DT08H06M\")))",
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
   * Test: op-add-dayTimeDuration-to-date-6                 
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-date" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate6() {
    final XQuery query = new XQuery(
      "fn:number(xs:date(\"1988-01-28Z\") + xs:dayTimeDuration(\"P10DT08H01M\"))",
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
   * Test: op-add-dayTimeDuration-to-date-7                 
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-date" operator used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate7() {
    final XQuery query = new XQuery(
      "fn:string(xs:date(\"1989-07-05Z\") + xs:dayTimeDuration(\"P01DT09H02M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1989-07-06Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDuration-to-date-8                 
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The "add-dayTimeDuration-to-date" operator that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate8() {
    final XQuery query = new XQuery(
      "(xs:date(\"0001-01-01Z\") + xs:dayTimeDuration(\"-P11DT02H02M\"))",
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
   * Test: op-add-dayTimeDuration-to-date-9                 
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value "add-dayTimeDuration-to-date" operator used  
   * together with an "and" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate9() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1993-12-09Z\") + xs:dayTimeDuration(\"P03DT01H04M\"))) and fn:string((xs:date(\"1993-12-09Z\") + xs:dayTimeDuration(\"P01DT01H03M\")))",
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
   * Test: op-add-dayTimeDuration-to-date2args-1             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDuration-to-date" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate2args1() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") + xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-add-dayTimeDuration-to-date2args-2             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDuration-to-date" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(mid range)                             
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate2args2() {
    final XQuery query = new XQuery(
      "xs:date(\"1983-11-17Z\") + xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-add-dayTimeDuration-to-date2args-3             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDuration-to-date" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(upper bound)                           
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate2args3() {
    final XQuery query = new XQuery(
      "xs:date(\"2030-12-31Z\") + xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-add-dayTimeDuration-to-date2args-4             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDuration-to-date" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:dayTimeDuration(mid range)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate2args4() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") + xs:dayTimeDuration(\"P15DT11H59M59S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1970-01-16Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDuration-to-date2args-5             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-dayTimeDuration-to-date" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:dayTimeDuration(upper bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDate2args5() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") + xs:dayTimeDuration(\"P31DT23H59M59S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1970-02-01Z")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-dayTimeDuration-to-date-2                 
   * Written By: Carmelo Montanez                           
   * Date: July 1, 2005                                     
   * Purpose: Evaluates The string value "add-dayTimeDuration-to-date" operator 
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddDayTimeDurationToDatealt2() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"2000-12-12Z\") + xs:dayTimeDuration(\"P19DT13H10M\"))) and fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }
}
