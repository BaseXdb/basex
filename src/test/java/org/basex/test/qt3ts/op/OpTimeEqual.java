package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the time-equal() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpTimeEqual extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-TimeEQ-1                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:time, returning positive. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ1() {
    final XQuery query = new XQuery(
      "xs:time(\"23:01:04.12\") eq xs:time(\"23:01:04.12\")",
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
   *  Test: K-TimeEQ-10                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'ne' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ10() {
    final XQuery query = new XQuery(
      "xs:time(\"12:12:23\") ne xs:date(\"1999-12-04\")",
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
   *  Test: K-TimeEQ-11                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'le' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ11() {
    final XQuery query = new XQuery(
      "xs:time(\"12:12:23\") le xs:date(\"1999-12-04\")",
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
   *  Test: K-TimeEQ-12                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'lt' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ12() {
    final XQuery query = new XQuery(
      "xs:time(\"12:12:23\") lt xs:date(\"1999-12-04\")",
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
   *  Test: K-TimeEQ-13                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'ge' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ13() {
    final XQuery query = new XQuery(
      "xs:time(\"12:12:23\") ge xs:date(\"1999-12-04\")",
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
   *  Test: K-TimeEQ-14                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'gt' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ14() {
    final XQuery query = new XQuery(
      "xs:time(\"12:12:23\") gt xs:date(\"1999-12-04\")",
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
   *  Test: K-TimeEQ-15                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'eq' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ15() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04\") eq xs:time(\"12:12:23\")",
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
   *  Test: K-TimeEQ-16                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'ne' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ16() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04\") ne xs:time(\"12:12:23\")",
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
   *  Test: K-TimeEQ-17                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'le' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ17() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04\") le xs:time(\"12:12:23\")",
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
   *  Test: K-TimeEQ-18                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'lt' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ18() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04\") lt xs:time(\"12:12:23\")",
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
   *  Test: K-TimeEQ-19                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'ge' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ19() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04\") ge xs:time(\"12:12:23\")",
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
   *  Test: K-TimeEQ-2                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:time.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ2() {
    final XQuery query = new XQuery(
      "not(xs:time(\"23:01:04.12\") eq xs:time(\"23:01:04.13\"))",
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
   *  Test: K-TimeEQ-20                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'gt' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ20() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04\") gt xs:time(\"12:12:23\")",
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
   *  Test: K-TimeEQ-3                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:time.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ3() {
    final XQuery query = new XQuery(
      "xs:time(\"23:01:05.12\") ne xs:time(\"23:01:04.12\")",
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
   *  Test: K-TimeEQ-4                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:time.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ4() {
    final XQuery query = new XQuery(
      "not(xs:time(\"23:01:04.12\") ne xs:time(\"23:01:04.12\"))",
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
   *  Test: K-TimeEQ-5                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to Z, in xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ5() {
    final XQuery query = new XQuery(
      "xs:time(\"16:00:12.345-00:00\") eq xs:time(\"16:00:12.345Z\")",
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
   *  Test: K-TimeEQ-6                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset +00:00 is equal to Z, in xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ6() {
    final XQuery query = new XQuery(
      "xs:time(\"16:00:12.345+00:00\") eq xs:time(\"16:00:12.345Z\")",
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
   *  Test: K-TimeEQ-7                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset Z is equal to Z, in xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ7() {
    final XQuery query = new XQuery(
      "xs:time(\"16:00:12.345Z\") eq xs:time(\"16:00:12.345Z\")",
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
   *  Test: K-TimeEQ-8                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to +00:00, in xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ8() {
    final XQuery query = new XQuery(
      "xs:time(\"16:00:12.345-00:00\") eq xs:time(\"16:00:12.345+00:00\")",
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
   *  Test: K-TimeEQ-9                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'eq' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ9() {
    final XQuery query = new XQuery(
      "xs:time(\"12:12:23\") eq xs:date(\"1999-12-04\")",
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
   *  Test: K2-TimeEQ-1                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Basic negative equalness test for xs:time.   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void k2TimeEQ1() {
    final XQuery query = new XQuery(
      "xs:time(\"01:01:01-03:00\") ne xs:time(\"01:01:01+03:00\")",
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
   * Test: op-time-equal2args-1                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args1() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") eq xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-10                             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args10() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") ne xs:time(\"23:59:59Z\")",
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
   * Test: op-time-equal2args-11                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:time-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args11() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") le xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-12                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:time-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(mid range)                             
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args12() {
    final XQuery query = new XQuery(
      "xs:time(\"08:03:35Z\") le xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-13                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:time-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(upper bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args13() {
    final XQuery query = new XQuery(
      "xs:time(\"23:59:59Z\") le xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-14                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:time-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args14() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") le xs:time(\"08:03:35Z\")",
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
   * Test: op-time-equal2args-15                            
   * Written By: Carmelo Montanez                           
   * Date: June 3 2005                                      
   * Purpose: Evaluates The "op:time-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args15() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") le xs:time(\"23:59:59Z\")",
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
   * Test: op-time-equal2args-16                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:time-equal" operator (ge)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args16() {
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
   * Test: op-time-equal2args-17                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:time-equal" operator  (ge)  
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(mid range)                             
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args17() {
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
   * Test: op-time-equal2args-q8                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:time-equal" operator (ge)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(upper bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args18() {
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
   * Test: op-time-equal2args-19                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:time-equal" operator (ge)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args19() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") ge xs:time(\"08:03:35Z\")",
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
   * Test: op-time-equal2args-2                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(mid range)                             
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args2() {
    final XQuery query = new XQuery(
      "xs:time(\"08:03:35Z\") eq xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-20                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:time-equal" operator (ge)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args20() {
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
   * Test: op-time-equal2args-3                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(upper bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args3() {
    final XQuery query = new XQuery(
      "xs:time(\"23:59:59Z\") eq xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-4                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args4() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") eq xs:time(\"08:03:35Z\")",
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
   * Test: op-time-equal2args-5                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args5() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") eq xs:time(\"23:59:59Z\")",
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
   * Test: op-time-equal2args-6                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args6() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") ne xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-7                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(mid range)                             
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args7() {
    final XQuery query = new XQuery(
      "xs:time(\"08:03:35Z\") ne xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-8                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(upper bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args8() {
    final XQuery query = new XQuery(
      "xs:time(\"23:59:59Z\") ne xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-9                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args9() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") ne xs:time(\"08:03:35Z\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }
}
