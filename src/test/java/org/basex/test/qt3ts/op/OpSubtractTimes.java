package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the subtract-times() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpSubtractTimes extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-TimeSubtract-1                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple testing involving operator '-' between xs:time and xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeSubtract1() {
    final XQuery query = new XQuery(
      "xs:time(\"08:12:32\") - xs:time(\"18:12:32\") eq xs:dayTimeDuration(\"-PT10H\")",
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
   *  Test: K-TimeSubtract-2                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple testing involving operator '-' between xs:time and xs:time, that evaluates to zero. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeSubtract2() {
    final XQuery query = new XQuery(
      "xs:time(\"08:12:32\") - xs:time(\"08:12:32\") eq xs:dayTimeDuration(\"PT0S\")",
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
   *  Test: K-TimeSubtract-3                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The '-' operator is not available between xs:yearMonthDuration and xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeSubtract3() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3D\") - xs:time(\"08:01:23\")",
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
   *  Test: K-TimeSubtract-4                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The '-' operator is not available between xs:yearMonthDuration and xs:date. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeSubtract4() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3D\") - xs:date(\"1999-08-12\")",
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
   *  Test: K-TimeSubtract-5                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The '+' operator is not available between xs:time and xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeSubtract5() {
    final XQuery query = new XQuery(
      "xs:time(\"08:01:23\") + xs:time(\"08:01:23\")",
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
   *  Test: K-TimeSubtract-6                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The '+' operator is not available between xs:time and xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeSubtract6() {
    final XQuery query = new XQuery(
      "xs:time(\"08:01:23\") * xs:time(\"08:01:23\")",
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
   *  Test: K-TimeSubtract-7                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The '-' operator is not available between xs:time and xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeSubtract7() {
    final XQuery query = new XQuery(
      "xs:time(\"08:01:23\") div xs:time(\"08:01:23\")",
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
   *  Test: K-TimeSubtract-8                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The 'mod' operator is not available between xs:time and xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeSubtract8() {
    final XQuery query = new XQuery(
      "xs:time(\"08:01:23\") mod xs:time(\"08:01:23\")",
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
   *  test subtraction of times .
   */
  @org.junit.Test
  public void cbclSubtractTimes001() {
    final XQuery query = new XQuery(
      "xs:time(\"12:00:00+01:00\") - xs:time(\"12:00:00\") - implicit-timezone()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-PT1H")
    );
  }

  /**
   *  test subtraction of times .
   */
  @org.junit.Test
  public void cbclSubtractTimes002() {
    final XQuery query = new XQuery(
      "xs:time(\"12:00:00\") - xs:time(\"12:00:00+01:00\") + implicit-timezone()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT1H")
    );
  }

  /**
   *  test subtraction of times .
   */
  @org.junit.Test
  public void cbclSubtractTimes003() {
    final XQuery query = new XQuery(
      "\n" +
      "      fn:adjust-time-to-timezone(xs:time(\"12:00:00\")) - fn:adjust-time-to-timezone(xs:time(\"08:00:00+05:00\"), xs:dayTimeDuration(\"PT1H\")) + implicit-timezone()\n" +
      "   ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT9H")
    );
  }

  /**
   *  test subtraction of times .
   */
  @org.junit.Test
  public void cbclSubtractTimes004() {
    final XQuery query = new XQuery(
      "\n" +
      "      fn:adjust-time-to-timezone(xs:time(\"08:00:00+05:00\"), xs:dayTimeDuration(\"PT1H\")) - fn:adjust-time-to-timezone(xs:time(\"12:00:00\")) - implicit-timezone()\n" +
      "   ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-PT9H")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-times-1                              
   * Written By: Carmelo Montanez                           
   * Date: June 26, 2005                                    
   * Purpose: Evaluates The "subtract-times" function       
   * As per example 1 (for this function)of the F&O specs   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes1() {
    final XQuery query = new XQuery(
      "xs:time(\"11:12:00Z\") - xs:time(\"04:00:00Z\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT7H12M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-times-10                             
   * Written By: Carmelo Montanez                           
   * Date: June 27, 2005                                    
   * Purpose: Evaluates The "subtract-times" function used  
   * together with an "or" expression.                      
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes10() {
    final XQuery query = new XQuery(
      "fn:string((xs:time(\"13:00:00Z\") - xs:time(\"17:00:00Z\"))) or fn:string((xs:time(\"13:00:00Z\") - xs:time(\"17:00:00Z\")))",
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
   * Test: op-subtract-times-11                             
   * Written By: Carmelo Montanez                           
   * Date: June 27, 2005                                    
   * Purpose: Evaluates The "subtract-times" function used  
   * together with a "div" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes11() {
    final XQuery query = new XQuery(
      "(xs:time(\"23:00:00Z\") - xs:time(\"17:00:00Z\")) div (xs:time(\"13:00:00Z\") - xs:time(\"10:00:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-times-12                             
   * Written By: Carmelo Montanez                           
   * Date: June 28, 2005                                    
   * Purpose: Evaluates The "subtract-times" operators used 
   * with a boolean expression and the "fn:true" function.   
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes12() {
    final XQuery query = new XQuery(
      "fn:string((xs:time(\"17:00:00Z\") - xs:time(\"13:00:00Z\"))) and (fn:true())",
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
   * Test: op-subtract-times-13                             
   * Written By: Carmelo Montanez                           
   * Date: June 28, 2005                                    
   * Purpose: Evaluates The "subtract-times" function used  
   * together with the numeric-equal- operator "eq".        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes13() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") - xs:time(\"17:00:00Z\")) eq xs:dayTimeDuration(\"P20DT01H02M\")",
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
   * Test: op-subtract-times-14                             
   * Written By: Carmelo Montanez                           
   * Date: June 28, 2005                                    
   * Purpose: Evaluates The "subtract-times" function used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes14() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") - xs:time(\"17:00:00Z\")) ne xs:dayTimeDuration(\"P10DT01H01M\")",
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
   * Test: op-subtract-times-15                             
   * Written By: Carmelo Montanez                           
   * Date: June 28, 2005                                    
   * Purpose: Evaluates The "subtract-times" function used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes15() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") - xs:time(\"17:00:00Z\")) le xs:dayTimeDuration(\"P10DT02H10M\")",
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
   * Test: op-subtract-times-16                             
   * Written By: Carmelo Montanez                           
   * Date: June 28, 2005                                    
   * Purpose: Evaluates The "subtract-times" function used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes16() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") - xs:time(\"17:00:00Z\")) ge xs:dayTimeDuration(\"P17DT10H02M\")",
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
   * Test: op-subtract-times-2                              
   * Written By: Carmelo Montanez                           
   * Date: June 27, 2005                                    
   * Purpose: Evaluates The "subtract-times" function       
   * As per example 2 (for this function) of the F&O  specs 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes2() {
    final XQuery query = new XQuery(
      "xs:time(\"11:00:00-05:00\") - xs:time(\"21:30:00+05:30\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-times-3                              
   * Written By: Carmelo Montanez                           
   * Date: June 27, 2005                                    
   * Purpose: Evaluates The "subtract-times" function as    
   * per example 3 (for this function) from the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes3() {
    final XQuery query = new XQuery(
      "xs:time(\"17:00:00-06:00\") - xs:time(\"08:00:00+09:00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1D")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-times-4                              
   * Written By: Carmelo Montanez                           
   * Date: June 27, 2005                                    
   * Purpose: Evaluates The "subtract-times" function that  
   * return true and used together with fn:not.             
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:time(\"13:00:00Z\") - xs:time(\"14:00:00Z\")))",
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
   * Test: op-subtract-times-5                              
   * Written By: Carmelo Montanez                           
   * Date: June 27 2005                                     
   * Purpose: Evaluates The "subtract-times" function that  
   * is used as an argument to the fn:boolean function.     
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:time(\"13:00:00Z\") - xs:time(\"10:00:00Z\")))",
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
   * Test: op-subtract-times-6                              
   * Written By: Carmelo Montanez                           
   * Date: June 27, 2005                                    
   * Purpose: Evaluates The "subtract-times" operator that  
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes6() {
    final XQuery query = new XQuery(
      "fn:number(xs:time(\"13:00:00Z\") - xs:time(\"12:00:00Z\"))",
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
   * Test: op-subtract-times-7                              
   * Written By: Carmelo Montanez                           
   * Date: June 27, 2005                                    
   * Purpose: Evaluates The "subtract-times" function used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes7() {
    final XQuery query = new XQuery(
      "fn:string(xs:time(\"13:00:00Z\") - xs:time(\"17:00:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-PT4H")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-times-8                              
   * Written By: Carmelo Montanez                           
   * Date: June 27, 2005                                    
   * Purpose: Evaluates The "subtract-times" function that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes8() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") - xs:time(\"17:00:00Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-PT4H")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-times-9                              
   * Written By: Carmelo Montanez                           
   * Date: June 27, 2005                                    
   * Purpose: Evaluates The "subtract-times" function used  
   * together with and "and" expression.                    
   *  Uses the "fn:string" function to account for new EBV rules. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes9() {
    final XQuery query = new XQuery(
      "fn:string((xs:time(\"13:00:00Z\") - xs:time(\"12:00:00Z\"))) and fn:string((xs:time(\"13:00:00Z\") - xs:time(\"10:00:00Z\")))",
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
   * Test: op-subtract-times2args-1                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-times" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes2args1() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") - xs:time(\"00:00:00Z\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-times2args-2                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-times" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(mid range)                             
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes2args2() {
    final XQuery query = new XQuery(
      "xs:time(\"08:03:35Z\") - xs:time(\"00:00:00Z\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT8H3M35S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-times2args-3                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-times" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(upper bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes2args3() {
    final XQuery query = new XQuery(
      "xs:time(\"23:59:59Z\") - xs:time(\"00:00:00Z\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT23H59M59S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-times2args-4                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-times" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes2args4() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") - xs:time(\"08:03:35Z\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-PT8H3M35S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-times2args-5                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:subtract-times" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractTimes2args5() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") - xs:time(\"23:59:59Z\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-PT23H59M59S")
    );
  }
}
