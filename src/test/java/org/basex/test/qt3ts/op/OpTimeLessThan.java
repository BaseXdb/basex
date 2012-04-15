package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the time-less-than() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpTimeLessThan extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-TimeLT-1                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'gt' for xs:time.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeLT1() {
    final XQuery query = new XQuery(
      "xs:time(\"23:01:05.12\") gt xs:time(\"23:01:04.12\")",
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
   *  Test: K-TimeLT-2                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'gt' for xs:time.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeLT2() {
    final XQuery query = new XQuery(
      "not(xs:time(\"23:01:03.12\") gt xs:time(\"23:01:04.12\"))",
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
   *  Test: K-TimeLT-3                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'gt' for xs:time.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeLT3() {
    final XQuery query = new XQuery(
      "not(xs:time(\"23:01:04.12\") gt xs:time(\"23:01:04.12\"))",
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
   *  Test: K-TimeLT-4                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ge' for xs:time.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeLT4() {
    final XQuery query = new XQuery(
      "xs:time(\"23:01:04.12\") ge xs:time(\"23:01:04.12\")",
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
   *  Test: K-TimeLT-5                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ge' for xs:time.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeLT5() {
    final XQuery query = new XQuery(
      "xs:time(\"23:01:05.12\") ge xs:time(\"23:01:04.12\")",
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
   *  Test: K-TimeLT-6                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ge' for xs:time.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeLT6() {
    final XQuery query = new XQuery(
      "not(xs:time(\"23:01:03.12\") ge xs:time(\"23:01:04.12\"))",
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
   * Test: op-time-less-than-1                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-less-than" function       
   * As per example 1 (for this function)of the F&O specs   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan1() {
    final XQuery query = new XQuery(
      "(xs:time(\"12:00:00-05:00\") lt xs:time(\"23:00:00+06:00\"))",
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
   * Test: op-time-less-than-10                             
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-less-than" function used  
   * together with "or" expression (le operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan10() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") le xs:time(\"17:00:00Z\")) or (xs:time(\"13:00:00Z\") le xs:time(\"17:00:00Z\"))",
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
   * Test: op-time-less-than-11                             
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-less-than" function used  
   * together with "fn:true"/or expression (lt operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan11() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") lt xs:time(\"17:00:00Z\")) or (fn:true())",
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
   * Test: op-time-less-than-12                             
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-less-than" function used  
   * together with "fn:true"/or expression (le operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan12() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") le xs:time(\"17:00:00Z\")) or (fn:true())",
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
   * Test: op-time-less-than-13                             
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-less-than" function used  
   * together with "fn:false"/or expression (lt operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan13() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") lt xs:time(\"17:00:00Z\")) or (fn:false())",
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
   * Test: op-time-less-than-14                             
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-less-than" function used  
   * together with "fn:false"/or expression (le operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan14() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") le xs:time(\"17:00:00Z\")) or (fn:false())",
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
   * Test: op-time-less-than-2                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-less-than" function       
   * As per example 2 (for this function) of the F&O  specs 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan2() {
    final XQuery query = new XQuery(
      "xs:time(\"11:00:00-05:00\") lt xs:time(\"17:00:00Z\")",
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
   * Test: op-time-less-than-3                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-less-than" function that  
   * return true and used together with fn:not (lt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan3() {
    final XQuery query = new XQuery(
      "fn:not((xs:time(\"13:00:00Z\") lt xs:time(\"14:00:00Z\")))",
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
   * Test: op-time-less-than-4                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-less-than" function that  
   * return true and used together with fn:not (le operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan4() {
    final XQuery query = new XQuery(
      "fn:not(xs:time(\"13:00:00Z\") le xs:time(\"14:00:00Z\"))",
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
   * Test: op-time-less-than-5                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-less-than" function that  
   * return false and used together with fn:not (lt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan5() {
    final XQuery query = new XQuery(
      "fn:not(xs:time(\"13:00:00Z\") lt xs:time(\"10:00:00Z\"))",
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
   * Test: op-time-less-than-6                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-less-than" function that  
   * return false and used together with fn:not(le operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan6() {
    final XQuery query = new XQuery(
      "fn:not(xs:time(\"13:00:00Z\") le xs:time(\"12:00:00Z\"))",
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
   * Test: op-time-less-than-7                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-less-than" function used  
   * together with "and" expression (lt operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan7() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") lt xs:time(\"17:00:00Z\")) and (xs:time(\"13:00:00Z\") lt xs:time(\"17:00:00Z\"))",
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
   * Test: op-time-less-than-8                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-less-than" function used  
   * together with "and" expression (le operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan8() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") le xs:time(\"17:00:00Z\")) and (xs:time(\"13:00:00Z\") le xs:time(\"17:00:00Z\"))",
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
   * Test: op-time-less-than-9                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "time-less-than" function used  
   * together with "or" expression (lt operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan9() {
    final XQuery query = new XQuery(
      "(xs:time(\"13:00:00Z\") lt xs:time(\"17:00:00Z\")) or (xs:time(\"13:00:00Z\") lt xs:time(\"17:00:00Z\"))",
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
   * Test: op-time-less-than2args-1                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan2args1() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") lt xs:time(\"00:00:00Z\")",
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
   * Test: op-time-less-than2args-10                         
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan2args10() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") ge xs:time(\"23:59:59Z\")",
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
   * Test: op-time-less-than2args-2                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(mid range)                             
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan2args2() {
    final XQuery query = new XQuery(
      "xs:time(\"08:03:35Z\") lt xs:time(\"00:00:00Z\")",
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
   * Test: op-time-less-than2args-3                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(upper bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan2args3() {
    final XQuery query = new XQuery(
      "xs:time(\"23:59:59Z\") lt xs:time(\"00:00:00Z\")",
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
   * Test: op-time-less-than2args-4                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan2args4() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") lt xs:time(\"08:03:35Z\")",
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
   * Test: op-time-less-than2args-5                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan2args5() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") lt xs:time(\"23:59:59Z\")",
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
   * Test: op-time-less-than2args-6                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan2args6() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") ge xs:time(\"00:00:00Z\")",
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
   * Test: op-time-less-than2args-7                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(mid range)                             
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan2args7() {
    final XQuery query = new XQuery(
      "xs:time(\"08:03:35Z\") ge xs:time(\"00:00:00Z\")",
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
   * Test: op-time-less-than2args-8                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(upper bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan2args8() {
    final XQuery query = new XQuery(
      "xs:time(\"23:59:59Z\") ge xs:time(\"00:00:00Z\")",
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
   * Test: op-time-less-than2args-9                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeLessThan2args9() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") ge xs:time(\"08:03:35Z\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }
}
