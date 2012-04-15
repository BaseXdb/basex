package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the date-equal() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpDateEqual extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DateEQ-1                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:date, returning positive. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateEQ1() {
    final XQuery query = new XQuery(
      "xs:date(\"2004-08-12\") eq xs:date(\"2004-08-12\")",
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
   *  Test: K-DateEQ-2                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateEQ2() {
    final XQuery query = new XQuery(
      "not(xs:date(\"2004-08-12\") eq xs:date(\"2003-08-12\"))",
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
   *  Test: K-DateEQ-3                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateEQ3() {
    final XQuery query = new XQuery(
      "xs:date(\"2004-08-12\") ne xs:date(\"2004-07-12\")",
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
   *  Test: K-DateEQ-4                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateEQ4() {
    final XQuery query = new XQuery(
      "not(xs:date(\"2004-07-12\") ne xs:date(\"2004-07-12\"))",
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
   *  Test: K-DateEQ-5                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to Z, in xs:date. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateEQ5() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04-00:00\") eq xs:date(\"1999-12-04Z\")",
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
   *  Test: K-DateEQ-6                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset +00:00 is equal to Z, in xs:date. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateEQ6() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04+00:00\") eq xs:date(\"1999-12-04Z\")",
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
   *  Test: K-DateEQ-7                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset Z is equal to Z, in xs:date. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateEQ7() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04Z\") eq xs:date(\"1999-12-04Z\")",
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
   *  Test: K-DateEQ-8                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to +00:00, in xs:date. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateEQ8() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04-00:00\") eq xs:date(\"1999-12-04+00:00\")",
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
   *  Test: K2-DateEQ-1                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Basic negative equalness test for xs:date.   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void k2DateEQ1() {
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
   * Test: op-date-equal2args-1                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args1() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") eq xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-10                             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args10() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") ne xs:date(\"2030-12-31Z\")",
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
   * Test: op-date-equal2args-11                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:date-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args11() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") le xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-12                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:date-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(mid range)                             
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args12() {
    final XQuery query = new XQuery(
      "xs:date(\"1983-11-17Z\") le xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-13                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:date-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(upper bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args13() {
    final XQuery query = new XQuery(
      "xs:date(\"2030-12-31Z\") le xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-14                            
   * Written By: Carmelo Montanez                           
   * Date: June 14, 2005                                    
   * Purpose: Evaluates The "op:date-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args14() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") le xs:date(\"1983-11-17Z\")",
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
   * Test: op-date-equal2args-15                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:date-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args15() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") le xs:date(\"2030-12-31Z\")",
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
   * Test: op-date-equal2args-16                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:date-equal" operator (ge)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args16() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") ge xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-17                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:date-equal" operator (ge)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(mid range)                             
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args17() {
    final XQuery query = new XQuery(
      "xs:date(\"1983-11-17Z\") ge xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-18                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:date-equal" operator (ge)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(upper bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args18() {
    final XQuery query = new XQuery(
      "xs:date(\"2030-12-31Z\") ge xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-19                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:date-equal" operator (ge)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args19() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") ge xs:date(\"1983-11-17Z\")",
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
   * Test: op-date-equal2args-2                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(mid range)                             
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args2() {
    final XQuery query = new XQuery(
      "xs:date(\"1983-11-17Z\") eq xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-20                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:date-equal" operator (ge)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args20() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") ge xs:date(\"2030-12-31Z\")",
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
   * Test: op-date-equal2args-3                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(upper bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args3() {
    final XQuery query = new XQuery(
      "xs:date(\"2030-12-31Z\") eq xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-4                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args4() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") eq xs:date(\"1983-11-17Z\")",
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
   * Test: op-date-equal2args-5                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args5() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") eq xs:date(\"2030-12-31Z\")",
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
   * Test: op-date-equal2args-6                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args6() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") ne xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-7                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(mid range)                             
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args7() {
    final XQuery query = new XQuery(
      "xs:date(\"1983-11-17Z\") ne xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-8                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(upper bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args8() {
    final XQuery query = new XQuery(
      "xs:date(\"2030-12-31Z\") ne xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-equal2args-9                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateEqual2args9() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") ne xs:date(\"1983-11-17Z\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }
}
