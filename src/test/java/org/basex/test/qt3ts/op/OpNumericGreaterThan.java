package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the numeric-greater-than() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpNumericGreaterThan extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-NumericGT-1                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:integer(1) gt xs:integer(-1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT1() {
    final XQuery query = new XQuery(
      "xs:integer(1) gt xs:integer(-1)",
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
   *  Test: K-NumericGT-10                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:float("INF") gt 0`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT10() {
    final XQuery query = new XQuery(
      "xs:float(\"INF\") gt 0",
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
   *  Test: K-NumericGT-11                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:float(1) gt xs:float(-1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT11() {
    final XQuery query = new XQuery(
      "xs:float(1) gt xs:float(-1)",
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
   *  Test: K-NumericGT-12                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:float(1) ge xs:float(1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT12() {
    final XQuery query = new XQuery(
      "xs:float(1) ge xs:float(1)",
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
   *  Test: K-NumericGT-13                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `not(xs:double("NaN") gt 1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT13() {
    final XQuery query = new XQuery(
      "not(xs:double(\"NaN\") gt 1)",
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
   *  Test: K-NumericGT-14                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `not(xs:float("NaN") gt 1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT14() {
    final XQuery query = new XQuery(
      "not(xs:float(\"NaN\") gt 1)",
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
   *  Test: K-NumericGT-15                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `not(xs:double("NaN") ge 1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT15() {
    final XQuery query = new XQuery(
      "not(xs:double(\"NaN\") ge 1)",
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
   *  Test: K-NumericGT-16                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `not(xs:float("NaN") ge 1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT16() {
    final XQuery query = new XQuery(
      "not(xs:float(\"NaN\") ge 1)",
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
   *  Test: K-NumericGT-17                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `not(1 gt xs:double("NaN"))`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT17() {
    final XQuery query = new XQuery(
      "not(1 gt xs:double(\"NaN\"))",
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
   *  Test: K-NumericGT-18                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `not(1 gt xs:float("NaN"))`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT18() {
    final XQuery query = new XQuery(
      "not(1 gt xs:float(\"NaN\"))",
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
   *  Test: K-NumericGT-19                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `not(1 ge xs:double("NaN"))`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT19() {
    final XQuery query = new XQuery(
      "not(1 ge xs:double(\"NaN\"))",
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
   *  Test: K-NumericGT-2                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:integer(1) ge xs:integer(1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT2() {
    final XQuery query = new XQuery(
      "xs:integer(1) ge xs:integer(1)",
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
   *  Test: K-NumericGT-20                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `not(1 ge xs:float("NaN"))`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT20() {
    final XQuery query = new XQuery(
      "not(1 ge xs:float(\"NaN\"))",
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
   *  Test: K-NumericGT-21                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: le combined with count().                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT21() {
    final XQuery query = new XQuery(
      "1 le count((1, 2, 3, timezone-from-time(current-time()), 4))",
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
   *  Test: K-NumericGT-22                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: ge combined with count().                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT22() {
    final XQuery query = new XQuery(
      "count((1, 2, 3, timezone-from-time(current-time()), 4)) ge 1",
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
   *  Test: K-NumericGT-23                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: gt combined with count().                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT23() {
    final XQuery query = new XQuery(
      "count((1, 2, 3, timezone-from-time(current-time()), 4)) gt 0",
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
   *  Test: K-NumericGT-3                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:decimal(1) gt xs:decimal(-1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT3() {
    final XQuery query = new XQuery(
      "xs:decimal(1) gt xs:decimal(-1)",
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
   *  Test: K-NumericGT-4                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:decimal(1) ge xs:decimal(1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT4() {
    final XQuery query = new XQuery(
      "xs:decimal(1) ge xs:decimal(1)",
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
   *  Test: K-NumericGT-5                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `0 gt xs:double("-INF")`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT5() {
    final XQuery query = new XQuery(
      "0 gt xs:double(\"-INF\")",
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
   *  Test: K-NumericGT-6                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:double("INF") gt 0`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT6() {
    final XQuery query = new XQuery(
      "xs:double(\"INF\") gt 0",
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
   *  Test: K-NumericGT-7                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:double(1) gt xs:double(-1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT7() {
    final XQuery query = new XQuery(
      "xs:double(1) gt xs:double(-1)",
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
   *  Test: K-NumericGT-8                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:double(1) ge xs:double(1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT8() {
    final XQuery query = new XQuery(
      "xs:double(1) ge xs:double(1)",
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
   *  Test: K-NumericGT-9                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `0 gt xs:float("-INF")`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericGT9() {
    final XQuery query = new XQuery(
      "0 gt xs:float(\"-INF\")",
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
   *  Test: K2-NumericGT-1                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Compare two xs:unsignedLong values.          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void k2NumericGT1() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"100\") gt xs:unsignedLong(\"18446744073709551615\")",
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
   *  Test: K2-NumericGT-2                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Compare two xs:unsignedLong values.          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void k2NumericGT2() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"18446744073709551615\") gt xs:unsignedLong(\"100\")",
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
   * Test: op-numeric-greater-thandbl2args-1                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(lower bound)                         
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThandbl2args1() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") gt xs:double(\"-1.7976931348623157E308\")",
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
   * Test: op-numeric-greater-thandbl2args-2                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(mid range)                           
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThandbl2args2() {
    final XQuery query = new XQuery(
      "xs:double(\"0\") gt xs:double(\"-1.7976931348623157E308\")",
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
   * Test: op-numeric-greater-thandbl2args-3                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(upper bound)                         
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThandbl2args3() {
    final XQuery query = new XQuery(
      "xs:double(\"1.7976931348623157E308\") gt xs:double(\"-1.7976931348623157E308\")",
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
   * Test: op-numeric-greater-thandbl2args-4                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(lower bound)                         
   * $arg2 = xs:double(mid range)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThandbl2args4() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") gt xs:double(\"0\")",
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
   * Test: op-numeric-greater-thandbl2args-5                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(lower bound)                         
   * $arg2 = xs:double(upper bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThandbl2args5() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") gt xs:double(\"1.7976931348623157E308\")",
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
   * Test: op-numeric-greater-thandec2args-1                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(lower bound)                        
   * $arg2 = xs:decimal(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThandec2args1() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") gt xs:decimal(\"-999999999999999999\")",
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
   * Test: op-numeric-greater-thandec2args-2                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(mid range)                          
   * $arg2 = xs:decimal(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThandec2args2() {
    final XQuery query = new XQuery(
      "xs:decimal(\"617375191608514839\") gt xs:decimal(\"-999999999999999999\")",
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
   * Test: op-numeric-greater-thandec2args-3                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(upper bound)                        
   * $arg2 = xs:decimal(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThandec2args3() {
    final XQuery query = new XQuery(
      "xs:decimal(\"999999999999999999\") gt xs:decimal(\"-999999999999999999\")",
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
   * Test: op-numeric-greater-thandec2args-4                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(lower bound)                        
   * $arg2 = xs:decimal(mid range)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThandec2args4() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") gt xs:decimal(\"617375191608514839\")",
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
   * Test: op-numeric-greater-thandec2args-5                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(lower bound)                        
   * $arg2 = xs:decimal(upper bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThandec2args5() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") gt xs:decimal(\"999999999999999999\")",
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
   * Test: op-numeric-greater-thanflt2args-1                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(lower bound)                          
   * $arg2 = xs:float(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanflt2args1() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") gt xs:float(\"-3.4028235E38\")",
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
   * Test: op-numeric-greater-thanflt2args-2                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(mid range)                            
   * $arg2 = xs:float(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanflt2args2() {
    final XQuery query = new XQuery(
      "xs:float(\"0\") gt xs:float(\"-3.4028235E38\")",
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
   * Test: op-numeric-greater-thanflt2args-3                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(upper bound)                          
   * $arg2 = xs:float(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanflt2args3() {
    final XQuery query = new XQuery(
      "xs:float(\"3.4028235E38\") gt xs:float(\"-3.4028235E38\")",
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
   * Test: op-numeric-greater-thanflt2args-4                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(lower bound)                          
   * $arg2 = xs:float(mid range)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanflt2args4() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") gt xs:float(\"0\")",
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
   * Test: op-numeric-greater-thanflt2args-5                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(lower bound)                          
   * $arg2 = xs:float(upper bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanflt2args5() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") gt xs:float(\"3.4028235E38\")",
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
   * Test: op-numeric-greater-thanint2args-1                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(lower bound)                            
   * $arg2 = xs:int(lower bound)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanint2args1() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") gt xs:int(\"-2147483648\")",
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
   * Test: op-numeric-greater-thanint2args-2                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(mid range)                              
   * $arg2 = xs:int(lower bound)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanint2args2() {
    final XQuery query = new XQuery(
      "xs:int(\"-1873914410\") gt xs:int(\"-2147483648\")",
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
   * Test: op-numeric-greater-thanint2args-3                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(upper bound)                            
   * $arg2 = xs:int(lower bound)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanint2args3() {
    final XQuery query = new XQuery(
      "xs:int(\"2147483647\") gt xs:int(\"-2147483648\")",
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
   * Test: op-numeric-greater-thanint2args-4                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(lower bound)                            
   * $arg2 = xs:int(mid range)                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanint2args4() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") gt xs:int(\"-1873914410\")",
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
   * Test: op-numeric-greater-thanint2args-5                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(lower bound)                            
   * $arg2 = xs:int(upper bound)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanint2args5() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") gt xs:int(\"2147483647\")",
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
   * Test: op-numeric-greater-thanintg2args-1                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(lower bound)                        
   * $arg2 = xs:integer(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanintg2args1() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") gt xs:integer(\"-999999999999999999\")",
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
   * Test: op-numeric-greater-thanintg2args-2                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(mid range)                          
   * $arg2 = xs:integer(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanintg2args2() {
    final XQuery query = new XQuery(
      "xs:integer(\"830993497117024304\") gt xs:integer(\"-999999999999999999\")",
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
   * Test: op-numeric-greater-thanintg2args-3                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(upper bound)                        
   * $arg2 = xs:integer(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanintg2args3() {
    final XQuery query = new XQuery(
      "xs:integer(\"999999999999999999\") gt xs:integer(\"-999999999999999999\")",
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
   * Test: op-numeric-greater-thanintg2args-4                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(lower bound)                        
   * $arg2 = xs:integer(mid range)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanintg2args4() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") gt xs:integer(\"830993497117024304\")",
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
   * Test: op-numeric-greater-thanintg2args-5                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(lower bound)                        
   * $arg2 = xs:integer(upper bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanintg2args5() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") gt xs:integer(\"999999999999999999\")",
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
   * Test: op-numeric-greater-thanlng2args-1                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(lower bound)                           
   * $arg2 = xs:long(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanlng2args1() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") gt xs:long(\"-92233720368547758\")",
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
   * Test: op-numeric-greater-thanlng2args-2                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(mid range)                             
   * $arg2 = xs:long(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanlng2args2() {
    final XQuery query = new XQuery(
      "xs:long(\"-47175562203048468\") gt xs:long(\"-92233720368547758\")",
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
   * Test: op-numeric-greater-thanlng2args-3                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(upper bound)                           
   * $arg2 = xs:long(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanlng2args3() {
    final XQuery query = new XQuery(
      "xs:long(\"92233720368547758\") gt xs:long(\"-92233720368547758\")",
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
   * Test: op-numeric-greater-thanlng2args-4                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(lower bound)                           
   * $arg2 = xs:long(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanlng2args4() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") gt xs:long(\"-47175562203048468\")",
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
   * Test: op-numeric-greater-thanlng2args-5                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(lower bound)                           
   * $arg2 = xs:long(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanlng2args5() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") gt xs:long(\"92233720368547758\")",
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
   * Test: op-numeric-greater-thannint2args-1                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(lower bound)                
   * $arg2 = xs:negativeInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThannint2args1() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") gt xs:negativeInteger(\"-999999999999999999\")",
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
   * Test: op-numeric-greater-thannint2args-2                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(mid range)                  
   * $arg2 = xs:negativeInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThannint2args2() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-297014075999096793\") gt xs:negativeInteger(\"-999999999999999999\")",
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
   * Test: op-numeric-greater-thannint2args-3                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(upper bound)                
   * $arg2 = xs:negativeInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThannint2args3() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-1\") gt xs:negativeInteger(\"-999999999999999999\")",
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
   * Test: op-numeric-greater-thannint2args-4                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(lower bound)                
   * $arg2 = xs:negativeInteger(mid range)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThannint2args4() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") gt xs:negativeInteger(\"-297014075999096793\")",
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
   * Test: op-numeric-greater-thannint2args-5                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(lower bound)                
   * $arg2 = xs:negativeInteger(upper bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThannint2args5() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") gt xs:negativeInteger(\"-1\")",
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
   * Test: op-numeric-greater-thannni2args-1                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(lower bound)             
   * $arg2 = xs:nonNegativeInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThannni2args1() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") gt xs:nonNegativeInteger(\"0\")",
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
   * Test: op-numeric-greater-thannni2args-2                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(mid range)               
   * $arg2 = xs:nonNegativeInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThannni2args2() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"303884545991464527\") gt xs:nonNegativeInteger(\"0\")",
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
   * Test: op-numeric-greater-thannni2args-3                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(upper bound)             
   * $arg2 = xs:nonNegativeInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThannni2args3() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"999999999999999999\") gt xs:nonNegativeInteger(\"0\")",
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
   * Test: op-numeric-greater-thannni2args-4                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(lower bound)             
   * $arg2 = xs:nonNegativeInteger(mid range)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThannni2args4() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") gt xs:nonNegativeInteger(\"303884545991464527\")",
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
   * Test: op-numeric-greater-thannni2args-5                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(lower bound)             
   * $arg2 = xs:nonNegativeInteger(upper bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThannni2args5() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") gt xs:nonNegativeInteger(\"999999999999999999\")",
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
   * Test: op-numeric-greater-thannpi2args-1                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(lower bound)             
   * $arg2 = xs:nonPositiveInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThannpi2args1() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") gt xs:nonPositiveInteger(\"-999999999999999999\")",
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
   * Test: op-numeric-greater-thannpi2args-2                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(mid range)               
   * $arg2 = xs:nonPositiveInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThannpi2args2() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-475688437271870490\") gt xs:nonPositiveInteger(\"-999999999999999999\")",
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
   * Test: op-numeric-greater-thannpi2args-3                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(upper bound)             
   * $arg2 = xs:nonPositiveInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThannpi2args3() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"0\") gt xs:nonPositiveInteger(\"-999999999999999999\")",
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
   * Test: op-numeric-greater-thannpi2args-4                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(lower bound)             
   * $arg2 = xs:nonPositiveInteger(mid range)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThannpi2args4() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") gt xs:nonPositiveInteger(\"-475688437271870490\")",
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
   * Test: op-numeric-greater-thannpi2args-5                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(lower bound)             
   * $arg2 = xs:nonPositiveInteger(upper bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThannpi2args5() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") gt xs:nonPositiveInteger(\"0\")",
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
   * Test: op-numeric-greater-thanpint2args-1                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(lower bound)                
   * $arg2 = xs:positiveInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanpint2args1() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") gt xs:positiveInteger(\"1\")",
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
   * Test: op-numeric-greater-thanpint2args-2                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(mid range)                  
   * $arg2 = xs:positiveInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanpint2args2() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"52704602390610033\") gt xs:positiveInteger(\"1\")",
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
   * Test: op-numeric-greater-thanpint2args-3                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(upper bound)                
   * $arg2 = xs:positiveInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanpint2args3() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"999999999999999999\") gt xs:positiveInteger(\"1\")",
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
   * Test: op-numeric-greater-thanpint2args-4                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(lower bound)                
   * $arg2 = xs:positiveInteger(mid range)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanpint2args4() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") gt xs:positiveInteger(\"52704602390610033\")",
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
   * Test: op-numeric-greater-thanpint2args-5                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(lower bound)                
   * $arg2 = xs:positiveInteger(upper bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanpint2args5() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") gt xs:positiveInteger(\"999999999999999999\")",
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
   * Test: op-numeric-greater-thansht2args-1                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(lower bound)                          
   * $arg2 = xs:short(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThansht2args1() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") gt xs:short(\"-32768\")",
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
   * Test: op-numeric-greater-thansht2args-2                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(mid range)                            
   * $arg2 = xs:short(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThansht2args2() {
    final XQuery query = new XQuery(
      "xs:short(\"-5324\") gt xs:short(\"-32768\")",
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
   * Test: op-numeric-greater-thansht2args-3                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(upper bound)                          
   * $arg2 = xs:short(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThansht2args3() {
    final XQuery query = new XQuery(
      "xs:short(\"32767\") gt xs:short(\"-32768\")",
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
   * Test: op-numeric-greater-thansht2args-4                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(lower bound)                          
   * $arg2 = xs:short(mid range)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThansht2args4() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") gt xs:short(\"-5324\")",
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
   * Test: op-numeric-greater-thansht2args-5                 
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(lower bound)                          
   * $arg2 = xs:short(upper bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThansht2args5() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") gt xs:short(\"32767\")",
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
   * Test: op-numeric-greater-thanulng2args-1                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(lower bound)                   
   * $arg2 = xs:unsignedLong(lower bound)                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanulng2args1() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") gt xs:unsignedLong(\"0\")",
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
   * Test: op-numeric-greater-thanulng2args-2                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(mid range)                     
   * $arg2 = xs:unsignedLong(lower bound)                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanulng2args2() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"130747108607674654\") gt xs:unsignedLong(\"0\")",
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
   * Test: op-numeric-greater-thanulng2args-3                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(upper bound)                   
   * $arg2 = xs:unsignedLong(lower bound)                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanulng2args3() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"184467440737095516\") gt xs:unsignedLong(\"0\")",
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
   * Test: op-numeric-greater-thanulng2args-4                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(lower bound)                   
   * $arg2 = xs:unsignedLong(mid range)                     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanulng2args4() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") gt xs:unsignedLong(\"130747108607674654\")",
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
   * Test: op-numeric-greater-thanulng2args-5                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(lower bound)                   
   * $arg2 = xs:unsignedLong(upper bound)                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanulng2args5() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") gt xs:unsignedLong(\"184467440737095516\")",
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
   * Test: op-numeric-greater-thanusht2args-1                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(lower bound)                  
   * $arg2 = xs:unsignedShort(lower bound)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanusht2args1() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") gt xs:unsignedShort(\"0\")",
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
   * Test: op-numeric-greater-thanusht2args-2                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(mid range)                    
   * $arg2 = xs:unsignedShort(lower bound)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanusht2args2() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"44633\") gt xs:unsignedShort(\"0\")",
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
   * Test: op-numeric-greater-thanusht2args-3                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(upper bound)                  
   * $arg2 = xs:unsignedShort(lower bound)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanusht2args3() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"65535\") gt xs:unsignedShort(\"0\")",
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
   * Test: op-numeric-greater-thanusht2args-4                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(lower bound)                  
   * $arg2 = xs:unsignedShort(mid range)                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanusht2args4() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") gt xs:unsignedShort(\"44633\")",
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
   * Test: op-numeric-greater-thanusht2args-5                
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(lower bound)                  
   * $arg2 = xs:unsignedShort(upper bound)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericGreaterThanusht2args5() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") gt xs:unsignedShort(\"65535\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }
}
