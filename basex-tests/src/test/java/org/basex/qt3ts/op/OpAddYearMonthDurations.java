package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the add-yearMonthDurations() function.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpAddYearMonthDurations extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-YearMonthDurationAdd-1                        
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of adding an xs:yearMonthDuration with P0M. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationAdd1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P3Y3M\") + xs:yearMonthDuration(\"P0M\") eq xs:yearMonthDuration(\"P3Y3M\")",
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
   *  Test: K-YearMonthDurationAdd-2                        
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of adding P0M with an xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationAdd2() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0M\") + xs:yearMonthDuration(\"P3Y3M\") eq xs:yearMonthDuration(\"P3Y3M\")",
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
   *  Test: K-YearMonthDurationAdd-3                        
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of adding two arbitrary xs:yearMonthDurations. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationAdd3() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P32Y43M\") + xs:yearMonthDuration(\"P12Y13M\") eq xs:yearMonthDuration(\"P48Y8M\")",
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
   *  test addition of zero duration to yearMonthDuration .
   */
  @org.junit.Test
  public void cbclAddYearMonthDurations001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($years as xs:integer, $months as xs:integer ) { xs:yearMonthDuration(concat('P', $years, 'Y', $months, 'M')) };\n" +
      "        local:yearMonthDuration(1, 1) + xs:yearMonthDuration(\"P0Y\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1Y1M")
    );
  }

  /**
   *  test addition of zero duration to yearMonthDuration .
   */
  @org.junit.Test
  public void cbclAddYearMonthDurations002() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($years as xs:integer, $months as xs:integer ) { xs:yearMonthDuration(concat('P', $years, 'Y', $months, 'M')) };\n" +
      "        xs:yearMonthDuration(\"P0Y\") + local:yearMonthDuration(1, 1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1Y1M")
    );
  }

  /**
   *  test addition of zero duration to yearMonthDuration .
   */
  @org.junit.Test
  public void cbclAddYearMonthDurations003() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($years as xs:integer, $months as xs:integer ) { xs:yearMonthDuration(concat('P', $years, 'Y', $months, 'M')) };\n" +
      "        local:yearMonthDuration(1, 1) + local:yearMonthDuration(1, 1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P2Y2M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-yearMonthDurations-1                      
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-yearMonthDurations" function 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P2Y11M\") + xs:yearMonthDuration(\"P3Y3M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P6Y2M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-yearMonthDurations-10                     
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-yearMonthDurations" function used  
   * together with an "or" expression.                      
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations10() {
    final XQuery query = new XQuery(
      "fn:string((xs:yearMonthDuration(\"P05Y02M\") + xs:yearMonthDuration(\"P03Y04M\"))) or fn:string((xs:yearMonthDuration(\"P05Y03M\") + xs:yearMonthDuration(\"P01Y03M\")))",
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
   * Test: op-add-yearMonthDurations-11                     
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-yearMonthDurations" function used  
   * together with a "div" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations11() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P42Y10M\") + xs:yearMonthDuration(\"P28Y10M\")) div (xs:yearMonthDuration(\"P10Y10M\") + xs:yearMonthDuration(\"P60Y10M\"))",
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
   * Test: op-add-yearMonthDurations-12                     
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-yearMonthDurations" operators used 
   * with a boolean expression and the "fn:true" function.   
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations12() {
    final XQuery query = new XQuery(
      "fn:string((xs:yearMonthDuration(\"P10Y11M\") + xs:yearMonthDuration(\"P05Y07M\"))) and (fn:true())",
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
   * Test: op-add-yearMonthDurations-13                     
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-yearMonthDurations" function used  
   * together with the numeric-equal- operator "eq".        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations13() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P23Y11M\") + xs:yearMonthDuration(\"P23Y11M\")) eq xs:yearMonthDuration(\"P23Y11M\")",
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
   * Test: op-add-yearMonthDurations-14                     
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-yearMonthDurations" function used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations14() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P21Y12M\") + xs:yearMonthDuration(\"P08Y05M\")) ne xs:yearMonthDuration(\"P08Y05M\")",
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
   * Test: op-add-yearMonthDurations-15                     
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-yearMonthDurations" function used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations15() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y01M\") + xs:yearMonthDuration(\"P17Y02M\")) le xs:yearMonthDuration(\"P17Y02M\")",
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
   * Test: op-add-yearMonthDurations-16                     
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-yearMonthDurations" function used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations16() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P13Y09M\") + xs:yearMonthDuration(\"P18Y02M\")) ge xs:yearMonthDuration(\"P18Y02M\")",
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
   * Test: op-add-yearMonthDurations-2                      
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-yearMonthDurations" function 
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations2() {
    final XQuery query = new XQuery(
      "fn:string(xs:yearMonthDuration(\"P10Y11M\") + xs:yearMonthDuration(\"P12Y07M\")) and fn:false()",
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
   * Test: op-add-yearMonthDurations-3                      
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-yearMonthDurations" function as 
   * part of a boolean expression (or operator) and the "fn:boolean" function. 
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations3() {
    final XQuery query = new XQuery(
      "fn:string(xs:yearMonthDuration(\"P20Y10M\") + xs:yearMonthDuration(\"P19Y10M\")) or fn:false()",
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
   * Test: op-add-yearMonthDurations-4                      
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-yearMonthDurations" function that  
   * return true and used together with fn:not.             
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:yearMonthDuration(\"P11Y04M\") + xs:yearMonthDuration(\"P02Y11M\")))",
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
   * Test: op-add-yearMonthDurations-5                      
   * Written By: Carmelo Montanez                           
   * Date: June 29 2005                                     
   * Purpose: Evaluates The "add-yearMonthDurations" function that  
   * is used as an argument to the fn:boolean function.     
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:yearMonthDuration(\"P05Y08M\") + xs:yearMonthDuration(\"P03Y06M\")))",
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
   * Test: op-add-yearMonthDurations-6                      
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-yearMonthDurations" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations6() {
    final XQuery query = new XQuery(
      "fn:number(xs:yearMonthDuration(\"P02Y09M\") + xs:yearMonthDuration(\"P10Y01M\"))",
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
   * Test: op-add-yearMonthDurations-7                      
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-yearMonthDurations" function used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations7() {
    final XQuery query = new XQuery(
      "fn:string(xs:yearMonthDuration(\"P03Y08M\") + xs:yearMonthDuration(\"P01Y02M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P4Y10M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-yearMonthDurations-8                      
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-yearMonthDurations" function that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations8() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y01M\") + xs:yearMonthDuration(\"-P11Y02M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-P1Y1M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-yearMonthDurations-9                      
   * Written By: Carmelo Montanez                           
   * Date: June 29, 2005                                    
   * Purpose: Evaluates The "add-yearMonthDurations" function used 
   * together with and "and" expression.                    
   *  Apply "fn:string" function to account for new EBV.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations9() {
    final XQuery query = new XQuery(
      "fn:string((xs:yearMonthDuration(\"P01Y01M\") + xs:yearMonthDuration(\"P02Y02M\"))) and fn:string((xs:yearMonthDuration(\"P02Y03M\") + xs:yearMonthDuration(\"P04Y04M\")))",
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
   * Test: op-add-yearMonthDurations2args-1                  
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-yearMonthDurations" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations2args1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") + xs:yearMonthDuration(\"P0Y0M\")",
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
   * Test: op-add-yearMonthDurations2args-2                  
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-yearMonthDurations" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(mid range)               
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations2args2() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1000Y6M\") + xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1000Y6M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-yearMonthDurations2args-3                  
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-yearMonthDurations" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(upper bound)             
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations2args3() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P2030Y12M\") + xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P2031Y")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-yearMonthDurations2args-4                  
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-yearMonthDurations" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(mid range)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations2args4() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") + xs:yearMonthDuration(\"P1000Y6M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1000Y6M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-add-yearMonthDurations2args-5                  
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:add-yearMonthDurations" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(upper bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opAddYearMonthDurations2args5() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") + xs:yearMonthDuration(\"P2030Y12M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P2031Y")
    );
  }
}
