package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the yearMonthDuration-less-than() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpYearMonthDurationLessThan extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-YearMonthDurationLT-1                         
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'lt' for xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationLT1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y9M\") lt xs:yearMonthDuration(\"P1999Y10M\")",
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
   *  Test: K-YearMonthDurationLT-2                         
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'lt' for xs:yearMonthDuration, evaluating to false. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationLT2() {
    final XQuery query = new XQuery(
      "not(xs:yearMonthDuration(\"P1999Y10M\") lt xs:yearMonthDuration(\"P1999Y10M\"))",
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
   *  Test: K-YearMonthDurationLT-3                         
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'lt' for xs:yearMonthDuration, evaluating to false. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationLT3() {
    final XQuery query = new XQuery(
      "not(xs:yearMonthDuration(\"P1999Y10M\") lt xs:yearMonthDuration(\"P1999Y9M\"))",
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
   *  Test: K-YearMonthDurationLT-4                         
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'le' for xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationLT4() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y10M\") le xs:yearMonthDuration(\"P1999Y10M\")",
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
   *  Test: K-YearMonthDurationLT-5                         
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'le' for xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationLT5() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y9M\") le xs:yearMonthDuration(\"P1999Y10M\")",
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
   *  Test: K-YearMonthDurationLT-6                         
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'le' for xs:yearMonthDuration, evaluating to false. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationLT6() {
    final XQuery query = new XQuery(
      "not(xs:yearMonthDuration(\"P1999Y10M\") le xs:yearMonthDuration(\"P1999Y9M\"))",
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
   * Test: op-yearMonthDuration-less-than-10                
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-less-than" function used  
   * together with "or" expression (le operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan10() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y01M\") le xs:yearMonthDuration(\"P09Y06M\")) or (xs:yearMonthDuration(\"P15Y01M\") le xs:yearMonthDuration(\"P02Y04M\"))",
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
   * Test: op-yearMonthDuration-less-than-11                
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-less-than" function used  
   * together with "fn:true"/or expression (lt operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan11() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y02M\") lt xs:yearMonthDuration(\"P01Y10M\")) or (fn:true())",
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
   * Test: op-yearMonthDuration-less-than-12                
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-less-than" function used  
   * together with "fn:true"/or expression (le operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan12() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y01M\") le xs:yearMonthDuration(\"P09Y05M\")) or (fn:true())",
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
   * Test: op-yearMonthDuration-less-than-13                
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-less-than" function used  
   * together with "fn:false"/or expression (lt operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan13() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P30Y10M\") lt xs:yearMonthDuration(\"P01Y02M\")) or (fn:false())",
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
   * Test: op-yearMonthDuration-less-than-14                
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-less-than" function used  
   * together with "fn:false"/or expression (le operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan14() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y05M\") le xs:yearMonthDuration(\"P20Y10M\")) or (fn:false())",
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
   * Test: op-yearMonthDuration-less-than-3                 
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-less-than" function that  
   * return true and used together with fn:not (lt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan3() {
    final XQuery query = new XQuery(
      "fn:not((xs:yearMonthDuration(\"P20Y10M\") lt xs:yearMonthDuration(\"P20Y11M\")))",
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
   * Test: op-yearMonthDuration-less-than-4                 
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-less-than" function that  
   * return true and used together with fn:not (le operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan4() {
    final XQuery query = new XQuery(
      "fn:not(xs:yearMonthDuration(\"P20Y10M\") le xs:yearMonthDuration(\"P20Y10M\"))",
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
   * Test: op-yearMonthDuration-less-than-5                 
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-less-than" function that  
   * return false and used together with fn:not (lt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan5() {
    final XQuery query = new XQuery(
      "fn:not(xs:yearMonthDuration(\"P20Y10M\") lt xs:yearMonthDuration(\"P20Y09M\"))",
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
   * Test: op-yearMonthDuration-less-than-6                 
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-less-than" function that  
   * return false and used together with fn:not(le operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan6() {
    final XQuery query = new XQuery(
      "fn:not(xs:yearMonthDuration(\"P10Y09M\") le xs:yearMonthDuration(\"P10Y07M\"))",
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
   * Test: op-yearMonthDuration-less-than-8                 
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-less-than" function used  
   * together with "and" expression (le operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan8() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y09M\") le xs:yearMonthDuration(\"P10Y01M\")) and (xs:yearMonthDuration(\"P02Y04M\") le xs:yearMonthDuration(\"P09Y07M\"))",
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
   * Test: op-yearMonthDuration-less-than-9                 
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-less-than" function used
   * together with "or" expression (lt operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan9() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y08M\") lt xs:yearMonthDuration(\"P10Y07M\")) or (xs:yearMonthDuration(\"P10Y09M\") lt xs:yearMonthDuration(\"P10Y09M\"))",
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
   * Test: op-yearMonthDuration-less-than2args-1             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan2args1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") lt xs:yearMonthDuration(\"P0Y0M\")",
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
   * Test: op-yearMonthDuration-less-than2args-10            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(upper bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan2args10() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") ge xs:yearMonthDuration(\"P2030Y12M\")",
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
   * Test: op-yearMonthDuration-less-than2args-2             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(mid range)               
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan2args2() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1000Y6M\") lt xs:yearMonthDuration(\"P0Y0M\")",
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
   * Test: op-yearMonthDuration-less-than2args-3             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(upper bound)             
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan2args3() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P2030Y12M\") lt xs:yearMonthDuration(\"P0Y0M\")",
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
   * Test: op-yearMonthDuration-less-than2args-4             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(mid range)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan2args4() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") lt xs:yearMonthDuration(\"P1000Y6M\")",
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
   * Test: op-yearMonthDuration-less-than2args-5             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(upper bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan2args5() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") lt xs:yearMonthDuration(\"P2030Y12M\")",
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
   * Test: op-yearMonthDuration-less-than2args-6             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan2args6() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") ge xs:yearMonthDuration(\"P0Y0M\")",
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
   * Test: op-yearMonthDuration-less-than2args-8             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(upper bound)             
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan2args8() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P2030Y12M\") ge xs:yearMonthDuration(\"P0Y0M\")",
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
   * Test: op-yearMonthDuration-less-than2args-9             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(mid range)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan2args9() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") ge xs:yearMonthDuration(\"P1000Y6M\")",
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
   * Test: op-yearMonthDuration-less-than2args-7             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-less-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(mid range)               
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThan2argsNew7() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1000Y6M\") ge xs:yearMonthDuration(\"P0Y0M\")",
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
   * Test: op-yearMonthDuration-less-than-7                 
   * Written By: Carmelo Montanez                           
   * Date: June 13, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-less-than" function used  
   * together with "and" expression (lt operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationLessThanNew7() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y09M\") lt xs:yearMonthDuration(\"P09Y10M\")) and (xs:yearMonthDuration(\"P10Y01M\") lt xs:yearMonthDuration(\"P08Y06M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }
}
