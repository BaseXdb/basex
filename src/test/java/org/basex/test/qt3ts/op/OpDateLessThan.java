package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the date-less-than() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpDateLessThan extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DateLT-1                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'lt' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateLT1() {
    final XQuery query = new XQuery(
      "xs:date(\"2004-07-12\") lt xs:date(\"2004-07-13\")",
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
   *  Test: K-DateLT-2                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'lt' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateLT2() {
    final XQuery query = new XQuery(
      "not(xs:date(\"2004-07-13\") lt xs:date(\"2004-07-12\"))",
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
   *  Test: K-DateLT-3                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'lt' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateLT3() {
    final XQuery query = new XQuery(
      "not(xs:date(\"2004-07-13\") lt xs:date(\"2004-07-13\"))",
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
   *  Test: K-DateLT-4                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'le' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateLT4() {
    final XQuery query = new XQuery(
      "xs:date(\"2004-07-12\") le xs:date(\"2004-07-12\")",
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
   *  Test: K-DateLT-5                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'le' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateLT5() {
    final XQuery query = new XQuery(
      "xs:date(\"2004-07-12\") le xs:date(\"2004-07-12\")",
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
   *  Test: K-DateLT-6                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'le' for xs:date.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateLT6() {
    final XQuery query = new XQuery(
      "not(xs:date(\"2004-07-13\") le xs:date(\"2004-07-12\"))",
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
   * Test: op-date-less-than-1                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function       
   * As per example 1 (for this function)of the F&O specs   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan1() {
    final XQuery query = new XQuery(
      "(xs:date(\"2004-12-25Z\") lt xs:date(\"2004-12-25-05:00\"))",
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
   * Test: op-date-less-than-10                             
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function used  
   * together with "or" expression (le operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan10() {
    final XQuery query = new XQuery(
      "(xs:date(\"1976-10-25Z\") le xs:date(\"1976-10-28Z\")) or (xs:date(\"1980-08-11Z\") le xs:date(\"1980-08-10Z\"))",
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
   * Test: op-date-less-than-11                             
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function used  
   * together with "fn:true"/or expression (lt operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan11() {
    final XQuery query = new XQuery(
      "(xs:date(\"1980-05-18Z\") lt xs:date(\"1980-05-17Z\")) or (fn:true())",
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
   * Test: op-date-less-than-12                             
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function used  
   * together with "fn:true"/or expression (le operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan12() {
    final XQuery query = new XQuery(
      "(xs:date(\"2000-10-25Z\") le xs:date(\"2000-10-26Z\")) or (fn:true())",
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
   * Test: op-date-less-than-13                             
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function used  
   * together with "fn:false"/or expression (lt operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan13() {
    final XQuery query = new XQuery(
      "(xs:date(\"1980-01-01Z\") lt xs:date(\"1980-10-01Z\")) or (fn:false())",
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
   * Test: op-date-less-than-14                             
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function used  
   * together with "fn:false"/or expression (le operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan14() {
    final XQuery query = new XQuery(
      "(xs:date(\"1980-10-25Z\") le xs:date(\"1980-10-26Z\")) or (fn:false())",
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
   * Test: op-date-less-than-2                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function       
   * As per example 2 (for this function) of the F&O  specs 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2() {
    final XQuery query = new XQuery(
      "(xs:date(\"2004-12-25-12:00\") le xs:date(\"2004-12-26+12:00\"))",
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
   * Test: op-date-less-than-3                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function that  
   * return true and used together with fn:not (lt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan3() {
    final XQuery query = new XQuery(
      "fn:not((xs:date(\"2005-12-25Z\") lt xs:date(\"2005-12-26Z\")))",
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
   * Test: op-date-less-than-4                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function that  
   * return true and used together with fn:not (le operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan4() {
    final XQuery query = new XQuery(
      "fn:not(xs:date(\"2005-04-02Z\") le xs:date(\"2005-04-02Z\"))",
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
   * Test: op-date-less-than-5                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function that  
   * return false and used together with fn:not (lt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan5() {
    final XQuery query = new XQuery(
      "fn:not(xs:date(\"2000-12-25Z\") lt xs:date(\"2000-11-25Z\"))",
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
   * Test: op-date-less-than-6                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function that  
   * return false and used together with fn:not(le operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan6() {
    final XQuery query = new XQuery(
      "fn:not(xs:date(\"2005-10-25Z\") le xs:date(\"2005-10-23Z\"))",
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
   * Test: op-date-less-than-7                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function used  
   * together with "and" expression (lt operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan7() {
    final XQuery query = new XQuery(
      "(xs:date(\"2000-01-01Z\") lt xs:date(\"2000-01-01Z\")) and (xs:date(\"2001-02-02Z\") lt xs:date(\"2001-03-02Z\"))",
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
   * Test: op-date-less-than-8                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function used  
   * together with "and" expression (le operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan8() {
    final XQuery query = new XQuery(
      "(xs:date(\"2000-01-25Z\") le xs:date(\"2000-10-26Z\")) and (xs:date(\"1975-10-26Z\") le xs:date(\"1975-10-28Z\"))",
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
   * Test: op-date-less-than-9                              
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "date-less-than" function used  
   * together with "or" expression (lt operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan9() {
    final XQuery query = new XQuery(
      "(xs:date(\"2000-10-26Z\") lt xs:date(\"2000-10-28Z\")) or (xs:date(\"1976-10-28Z\") lt xs:date(\"1976-10-28Z\"))",
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
   * Test: op-date-less-than2args-1                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args1() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") lt xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-less-than2args-10                         
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args10() {
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
   * Test: op-date-less-than2args-2                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(mid range)                             
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args2() {
    final XQuery query = new XQuery(
      "xs:date(\"1983-11-17Z\") lt xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-less-than2args-3                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(upper bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args3() {
    final XQuery query = new XQuery(
      "xs:date(\"2030-12-31Z\") lt xs:date(\"1970-01-01Z\")",
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
   * Test: op-date-less-than2args-4                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args4() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") lt xs:date(\"1983-11-17Z\")",
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
   * Test: op-date-less-than2args-5                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args5() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") lt xs:date(\"2030-12-31Z\")",
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
   * Test: op-date-less-than2args-6                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args6() {
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
   * Test: op-date-less-than2args-7                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(mid range)                             
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args7() {
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
   * Test: op-date-less-than2args-8                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(upper bound)                           
   * $arg2 = xs:date(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args8() {
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
   * Test: op-date-less-than2args-9                          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:date-less-than" operator    
   *  with the arguments set as follows:                    
   * $arg1 = xs:date(lower bound)                           
   * $arg2 = xs:date(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDateLessThan2args9() {
    final XQuery query = new XQuery(
      "xs:date(\"1970-01-01Z\") ge xs:date(\"1983-11-17Z\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }
}
