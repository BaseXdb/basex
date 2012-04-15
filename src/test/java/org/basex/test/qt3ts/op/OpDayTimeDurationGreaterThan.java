package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the dayTimeDuration-greater-than() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpDayTimeDurationGreaterThan extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DayTimeDurationGT-1                           
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'gt' for xs:dayTimeDuration.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationGT1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT08H34M12.144S\") gt xs:dayTimeDuration(\"P3DT08H34M12.143S\")",
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
   *  Test: K-DayTimeDurationGT-2                           
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'gt' for xs:dayTimeDuration, evaluating to false. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationGT2() {
    final XQuery query = new XQuery(
      "not(xs:dayTimeDuration(\"P3DT08H34M12.144S\") gt xs:dayTimeDuration(\"P3DT08H34M12.144S\"))",
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
   *  Test: K-DayTimeDurationGT-3                           
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'gt' for xs:dayTimeDuration, evaluating to false. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationGT3() {
    final XQuery query = new XQuery(
      "not(xs:dayTimeDuration(\"P3DT08H34M12.144S\") gt xs:dayTimeDuration(\"P3DT08H34M12.145S\"))",
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
   *  Test: K-DayTimeDurationGT-4                           
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ge' for xs:dayTimeDuration.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationGT4() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT08H34M12.143S\") ge xs:dayTimeDuration(\"P3DT08H34M12.143S\")",
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
   *  Test: K-DayTimeDurationGT-5                           
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ge' for xs:dayTimeDuration.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationGT5() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT08H34M12.144S\") ge xs:dayTimeDuration(\"P3DT08H34M12.143S\")",
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
   *  Test: K-DayTimeDurationGT-6                           
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ge' for xs:dayTimeDuration, evaluating to false. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDayTimeDurationGT6() {
    final XQuery query = new XQuery(
      "not(xs:dayTimeDuration(\"P3DT08H34M12.143S\") ge xs:dayTimeDuration(\"P3DT08H34M12.144S\"))",
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
   * Test: op-dayTimeDuration-greater-than-10               
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-greater-than" function used 
   * together with "or" expression (ge operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan10() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT01H\") ge xs:dayTimeDuration(\"P09DT06H\")) or (xs:dayTimeDuration(\"P15DT01H\") ge xs:dayTimeDuration(\"P02DT04H\"))",
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
   * Test: op-dayTimeDuration-greater-than-11               
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-greater-than" function used  
   * together with "fn:true"/or expression (gt operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan11() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT02H\") gt xs:dayTimeDuration(\"P01DT10H\")) or (fn:true())",
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
   * Test: op-dayTimeDuration-greater-than-12               
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-greater-than" function used 
   * together with "fn:true"/or expression (ge operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan12() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT01H\") ge xs:dayTimeDuration(\"P09DT05H\")) or (fn:true())",
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
   * Test: op-dayTimeDuration-greater-than-13               
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-greater-than" function used 
   * together with "fn:false"/or expression (gt operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan13() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P30DT10H\") gt xs:dayTimeDuration(\"P01DT02H\")) or (fn:false())",
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
   * Test: op-dayTimeDuration-greater-than-14               
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-greater-than" function used 
   * together with "fn:false"/or expression (ge operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan14() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT05H\") ge xs:dayTimeDuration(\"P20DT10H\")) or (fn:false())",
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
   * Test: op-dayTimeDuration-greater-than-3                
   * Written By: Carmelo Montanez                           
   * Date: June 17, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-greater-than" function that
   * return true and used together with fn:not (gt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan3() {
    final XQuery query = new XQuery(
      "fn:not((xs:dayTimeDuration(\"P15DT12H\") gt xs:dayTimeDuration(\"P14DT11H\")))",
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
   * Test: op-dayTimeDuration-greater-than-4                
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-greater-than" function that
   * return true and used together with fn:not (ge operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan4() {
    final XQuery query = new XQuery(
      "fn:not(xs:dayTimeDuration(\"P10DT11H\") ge xs:dayTimeDuration(\"P10DT10H\"))",
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
   * Test: op-dayTimeDuration-greater-than-5                
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-greater-than" function that 
   * return false and used together with fn:not (gt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan5() {
    final XQuery query = new XQuery(
      "fn:not(xs:dayTimeDuration(\"P08DT10H\") gt xs:dayTimeDuration(\"P9DT09H\"))",
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
   * Test: op-dayTimeDuration-greater-than-6                
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-greater-than" function that 
   * return false and used together with fn:not(ge operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan6() {
    final XQuery query = new XQuery(
      "fn:not(xs:dayTimeDuration(\"P07DT09H\") ge xs:dayTimeDuration(\"P09DT09H\"))",
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
   * Test: op-dayTimeDuration-greater-than-7                
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-greater-than" function used 
   * together with "and" expression (gt operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan7() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT09H\") gt xs:dayTimeDuration(\"P09DT10H\")) and (xs:dayTimeDuration(\"P10DT01H\") gt xs:dayTimeDuration(\"P08DT06H\"))",
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
   * Test: op-dayTimeDuration-greater-than-8                
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-greater-than" function used 
   * together with "and" expression (ge operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan8() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT09H\") ge xs:dayTimeDuration(\"P10DT01H\")) and (xs:dayTimeDuration(\"P02DT04H\") ge xs:dayTimeDuration(\"P09DT07H\"))",
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
   * Test: op-dayTimeDuration-greater-than-9                
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "dayTimeDuration-greater-than" function used
   * together with "or" expression (gt operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan9() {
    final XQuery query = new XQuery(
      "(xs:dayTimeDuration(\"P10DT08H\") gt xs:dayTimeDuration(\"P10DT07H\")) or (xs:dayTimeDuration(\"P10DT09H\") gt xs:dayTimeDuration(\"P10DT09H\"))",
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
   * Test: op-dayTimeDuration-greater-than2args-1            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan2args1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") gt xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-dayTimeDuration-greater-than2args-10           
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(upper bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan2args10() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") le xs:dayTimeDuration(\"P31DT23H59M59S\")",
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
   * Test: op-dayTimeDuration-greater-than2args-2            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(mid range)                 
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan2args2() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P15DT11H59M59S\") gt xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-dayTimeDuration-greater-than2args-3            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(upper bound)               
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan2args3() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P31DT23H59M59S\") gt xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-dayTimeDuration-greater-than2args-4            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(mid range)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan2args4() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") gt xs:dayTimeDuration(\"P15DT11H59M59S\")",
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
   * Test: op-dayTimeDuration-greater-than2args-5            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(upper bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan2args5() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") gt xs:dayTimeDuration(\"P31DT23H59M59S\")",
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
   * Test: op-dayTimeDuration-greater-than2args-6            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan2args6() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") le xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-dayTimeDuration-greater-than2args-7            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(mid range)                 
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan2args7() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P15DT11H59M59S\") le xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-dayTimeDuration-greater-than2args-8            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(upper bound)               
   * $arg2 = xs:dayTimeDuration(lower bound)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan2args8() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P31DT23H59M59S\") le xs:dayTimeDuration(\"P0DT0H0M0S\")",
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
   * Test: op-dayTimeDuration-greater-than2args-9            
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:dayTimeDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:dayTimeDuration(lower bound)               
   * $arg2 = xs:dayTimeDuration(mid range)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opDayTimeDurationGreaterThan2args9() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") le xs:dayTimeDuration(\"P15DT11H59M59S\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }
}
