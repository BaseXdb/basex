package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the gDay-equal() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpGDayEqual extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-gDayEQ-1                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:gDay, returning positive. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGDayEQ1() {
    final XQuery query = new XQuery(
      "xs:gDay(\" ---31 \") eq xs:gDay(\"---31\")",
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
   *  Test: K-gDayEQ-2                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:gDay.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGDayEQ2() {
    final XQuery query = new XQuery(
      "not(xs:gDay(\"---31\") eq xs:gDay(\"---01\"))",
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
   *  Test: K-gDayEQ-3                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:gDay.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGDayEQ3() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01\") ne xs:gDay(\"---10\")",
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
   *  Test: K-gDayEQ-4                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:gDay.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGDayEQ4() {
    final XQuery query = new XQuery(
      "not(xs:gDay(\"---01\") ne xs:gDay(\"---01\"))",
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
   *  Test: K-gDayEQ-5                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to Z, in xs:gDay. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGDayEQ5() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01-00:00\") eq xs:gDay(\"---01Z\")",
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
   *  Test: K-gDayEQ-6                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset +00:00 is equal to Z, in xs:gDay. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGDayEQ6() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01+00:00\") eq xs:gDay(\"---01Z\")",
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
   *  Test: K-gDayEQ-7                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset Z is equal to Z, in xs:gDay. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGDayEQ7() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01Z\") eq xs:gDay(\"---01Z\")",
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
   *  Test: K-gDayEQ-8                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to +00:00, in xs:gDay. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kGDayEQ8() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01-00:00\") eq xs:gDay(\"---01+00:00\")",
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
   * Test: op-gDay-equal-10                                 
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function used      
   * together with "or" expression (ne operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual10() {
    final XQuery query = new XQuery(
      "(xs:gDay(\"---06Z\") ne xs:gDay(\"---06Z\")) or (xs:gDay(\"---08Z\") ne xs:gDay(\"---09Z\"))",
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
   * Test: op-gDay-equal-11                                 
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function used      
   * together with "fn:true"/or expression (eq operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual11() {
    final XQuery query = new XQuery(
      "(xs:gDay(\"---03Z\") eq xs:gDay(\"---01Z\")) or (fn:true())",
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
   * Test: op-gDay-equal-12                                 
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function used      
   * together with "fn:true"/or expression (ne operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual12() {
    final XQuery query = new XQuery(
      "(xs:gDay(\"---08Z\") ne xs:gDay(\"---07Z\")) or (fn:true())",
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
   * Test: op-gDay-equal-13                                 
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function used      
   * together with "fn:false"/or expression (eq operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual13() {
    final XQuery query = new XQuery(
      "(xs:gDay(\"---05Z\") eq xs:gDay(\"---05Z\")) or (fn:false())",
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
   * Test: op-gDay-equal-14                                 
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function used      
   * together with "fn:false"/or expression (ne operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual14() {
    final XQuery query = new XQuery(
      "(xs:gDay(\"---09Z\") ne xs:gDay(\"---09Z\")) or (fn:false())",
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
   * Test: op-gDay-equal-2                                  
   * Written By: Carmelo Montanez                           
   * Date: June 16, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function           
   * As per example 2 (for this function) of the F&O  specs 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2() {
    final XQuery query = new XQuery(
      "(xs:gDay(\"---12-05:00\") eq xs:gDay(\"---12Z\"))",
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
   * Test: op-gDay-equal-3                                  
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function that      
   * return true and used together with fn:not (eq operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual3() {
    final XQuery query = new XQuery(
      "fn:not((xs:gDay(\"---12Z\") eq xs:gDay(\"---12Z\")))",
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
   * Test: op-gDay-equal-4                                  
   * Written By: Carmelo Montanez                           
   * Date: June 16, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function that      
   * return true and used together with fn:not (ne operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual4() {
    final XQuery query = new XQuery(
      "fn:not(xs:gDay(\"---05Z\") ne xs:gDay(\"---06Z\"))",
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
   * Test: op-gDay-equal-5                                  
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function that      
   * return false and used together with fn:not (eq operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual5() {
    final XQuery query = new XQuery(
      "fn:not(xs:gDay(\"---11Z\") eq xs:gDay(\"---10Z\"))",
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
   * Test: op-gDay-equal-6                                  
   * Written By: Carmelo Montanez                           
   * Date: June 16, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function that      
   * return false and used together with fn:not(ne operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual6() {
    final XQuery query = new XQuery(
      "fn:not(xs:gDay(\"---05Z\") ne xs:gDay(\"---05Z\"))",
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
   * Test: op-gDay-equal-7                                  
   * Written By: Carmelo Montanez                           
   * Date: June 16, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function used      
   * together with "and" expression (eq operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual7() {
    final XQuery query = new XQuery(
      "(xs:gDay(\"---04Z\") eq xs:gDay(\"---02Z\")) and (xs:gDay(\"---01Z\") eq xs:gDay(\"---12Z\"))",
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
   * Test: op-gDay-equal-8                                  
   * Written By: Carmelo Montanez                           
   * Date: June 16, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function used      
   * together with "and" expression (ne operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual8() {
    final XQuery query = new XQuery(
      "(xs:gDay(\"---12Z\") ne xs:gDay(\"---03Z\")) and (xs:gDay(\"---05Z\") ne xs:gDay(\"---08Z\"))",
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
   * Test: op-gDay-equal-9                                  
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "gDay-equal" function used      
   * together with "or" expression (eq operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual9() {
    final XQuery query = new XQuery(
      "(xs:gDay(\"---02Z\") eq xs:gDay(\"---02Z\")) or (xs:gDay(\"---06Z\") eq xs:gDay(\"---06Z\"))",
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
   * Test: op-gDay-equal2args-1                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(lower bound)                           
   * $arg2 = xs:gDay(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args1() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01Z\") eq xs:gDay(\"---01Z\")",
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
   * Test: op-gDay-equal2args-10                             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(lower bound)                           
   * $arg2 = xs:gDay(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args10() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01Z\") ne xs:gDay(\"---31Z\")",
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
   * Test: op-gDay-equal2args-2                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(mid range)                             
   * $arg2 = xs:gDay(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args2() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---14Z\") eq xs:gDay(\"---01Z\")",
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
   * Test: op-gDay-equal2args-3                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(upper bound)                           
   * $arg2 = xs:gDay(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args3() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31Z\") eq xs:gDay(\"---01Z\")",
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
   * Test: op-gDay-equal2args-4                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(lower bound)                           
   * $arg2 = xs:gDay(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args4() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01Z\") eq xs:gDay(\"---14Z\")",
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
   * Test: op-gDay-equal2args-5                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(lower bound)                           
   * $arg2 = xs:gDay(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args5() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01Z\") eq xs:gDay(\"---31Z\")",
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
   * Test: op-gDay-equal2args-6                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(lower bound)                           
   * $arg2 = xs:gDay(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args6() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01Z\") ne xs:gDay(\"---01Z\")",
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
   * Test: op-gDay-equal2args-7                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(mid range)                             
   * $arg2 = xs:gDay(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args7() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---14Z\") ne xs:gDay(\"---01Z\")",
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
   * Test: op-gDay-equal2args-8                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(upper bound)                           
   * $arg2 = xs:gDay(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args8() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31Z\") ne xs:gDay(\"---01Z\")",
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
   * Test: op-gDay-equal2args-9                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:08 GMT-05:00 2005                
   * Purpose: Evaluates The "op:gDay-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:gDay(lower bound)                           
   * $arg2 = xs:gDay(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opGDayEqual2args9() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---01Z\") ne xs:gDay(\"---14Z\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }
}
