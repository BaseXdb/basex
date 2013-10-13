package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the boolean-less-than() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpBooleanLessThan extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-BooleanLT-1                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Compare two xs:boolean values.               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kBooleanLT1() {
    final XQuery query = new XQuery(
      "false() lt true()",
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
   *  Test: K-BooleanLT-2                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Compare two xs:boolean values.               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kBooleanLT2() {
    final XQuery query = new XQuery(
      "false() le true()",
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
   *  Test: K-BooleanLT-3                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Compare two xs:boolean values.               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kBooleanLT3() {
    final XQuery query = new XQuery(
      "true() le true()",
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
.
   */
  @org.junit.Test
  public void cbclBooleanLessThan001() {
    final XQuery query = new XQuery(
      "declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      not(local:is-even(15) le local:is-even(17))",
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
.
   */
  @org.junit.Test
  public void cbclBooleanLessThan002() {
    final XQuery query = new XQuery(
      "declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      fn:true() le local:is-even(17)",
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
.
   */
  @org.junit.Test
  public void cbclBooleanLessThan003() {
    final XQuery query = new XQuery(
      "declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      fn:false() le local:is-even(17)",
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
.
   */
  @org.junit.Test
  public void cbclBooleanLessThan004() {
    final XQuery query = new XQuery(
      "declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      not(local:is-even(15) lt local:is-even(17))",
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
.
   */
  @org.junit.Test
  public void cbclBooleanLessThan005() {
    final XQuery query = new XQuery(
      "declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      fn:true() lt local:is-even(17)",
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
.
   */
  @org.junit.Test
  public void cbclBooleanLessThan006() {
    final XQuery query = new XQuery(
      "declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      fn:false() lt local:is-even(17)",
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
.
   */
  @org.junit.Test
  public void cbclBooleanLessThan007() {
    final XQuery query = new XQuery(
      "declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      local:is-even(17) le fn:true()",
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
.
   */
  @org.junit.Test
  public void cbclBooleanLessThan008() {
    final XQuery query = new XQuery(
      "declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      local:is-even(17) le fn:false()",
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
.
   */
  @org.junit.Test
  public void cbclBooleanLessThan009() {
    final XQuery query = new XQuery(
      "declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      local:is-even(17) lt fn:true()",
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
.
   */
  @org.junit.Test
  public void cbclBooleanLessThan010() {
    final XQuery query = new XQuery(
      "declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      local:is-even(17) lt fn:false()",
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
   * Test: op-boolean-less-than-1                           
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-less-than" function    
   *  with operands set to "not(true)", "true" respectively.
   *  Use of lt operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan1() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"true\")) lt xs:boolean(\"true\")",
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
   * Test: op-boolean-less-than-10                           
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-less-than" function    
   *  with operands set to "(7 eq 7)", "true" respectively.
   *  Use of ge operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan10() {
    final XQuery query = new XQuery(
      "(7 eq 7) ge xs:boolean(\"true\")",
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
   * Test: op-boolean-less-than-11                           
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-less-than" function    
   *  with operands set to "(7 eq 7)", "false" respectively.
   *  Use of lt operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan11() {
    final XQuery query = new XQuery(
      "(7 eq 7) lt xs:boolean(\"false\")",
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
   * Test: op-boolean-less-than-12                           
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-less-than" function    
   *  with operands set to "(7 eq 7)", "false" respectively.
   *  Use of ge operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan12() {
    final XQuery query = new XQuery(
      "(7 eq 7) ge xs:boolean(\"false\")",
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
   * Test: op-boolean-less-than-2                           
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-less-than" function    
   *  with operands set to "not(true)", "true" respectively.
   *  Use of ge operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan2() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"true\")) ge xs:boolean(\"true\")",
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
   * Test: op-boolean-less-than-3                           
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-less-than" function    
   *  with operands set to "not(true)", "false" respectively.
   *  Use of lt operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan3() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"true\")) lt xs:boolean(\"false\")",
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
   * Test: op-boolean-less-than-4                           
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-less-than" function    
   *  with operands set to "not(true)", "false" respectively.
   *  Use of ge operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan4() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"true\")) ge xs:boolean(\"false\")",
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
   * Test: op-boolean-less-than-5                           
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-less-than" function    
   *  with operands set to "not(false)", "true" respectively.
   *  Use of lt operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan5() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"false\")) lt xs:boolean(\"true\")",
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
   * Test: op-boolean-less-than-6                           
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-less-than" function    
   *  with operands set to "not(false)", "true" respectively.
   *  Use of ge operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan6() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"false\")) ge xs:boolean(\"true\")",
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
   * Test: op-boolean-less-than-7                           
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-less-than" function    
   *  with operands set to "not(false)", "false" respectively.
   *  Use of lt operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan7() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"false\")) lt xs:boolean(\"false\")",
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
   * Test: op-boolean-less-than-8                           
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-less-than" function    
   *  with operands set to "not(false)", "false" respectively.
   *  Use of ge operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan8() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"false\")) ge xs:boolean(\"false\")",
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
   * Test: op-boolean-less-than-9                           
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-less-than" function    
   *  with operands set to "(7 eq 7)", "true" respectively.
   *  Use of lt operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan9() {
    final XQuery query = new XQuery(
      "(7 eq 7) lt xs:boolean(\"true\")",
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
   *  Test: op-boolean-less-than-more-args-001.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:19:25 2005                        
   *  Purpose: To check if arg1: Boolean is less than arg2:Boolean 
   * **************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThanMoreArgs001() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") lt xs:boolean(\"true\")",
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
   *  Test: op-boolean-less-than-more-args-002.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:19:25 2005                        
   *  Purpose: To check if arg1: Boolean is less than arg2:Boolean 
   * **************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThanMoreArgs002() {
    final XQuery query = new XQuery(
      "xs:boolean(\"1\") lt xs:boolean(\"true\")",
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
   *  Test: op-boolean-less-than-more-args-003.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:19:25 2005                        
   *  Purpose: To check if arg1: Boolean is less than arg2:Boolean 
   * **************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThanMoreArgs003() {
    final XQuery query = new XQuery(
      "xs:boolean(\"0\") lt xs:boolean(\"true\")",
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
   *  Test: op-boolean-less-than-more-args-004.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:19:25 2005                        
   *  Purpose: To check if arg1: Boolean is less than arg2:Boolean 
   * **************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThanMoreArgs004() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") lt xs:boolean(\"1\")",
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
   *  Test: op-boolean-less-than-more-args-005.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:19:25 2005                        
   *  Purpose: To check if arg1: Boolean is less than arg2:Boolean 
   * **************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThanMoreArgs005() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") lt xs:boolean(\"0\")",
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
   *  Test: op-boolean-less-than-more-args-006.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:19:25 2005                        
   *  Purpose: To check if arg1: Boolean is less than arg2:Boolean 
   * **************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThanMoreArgs006() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") lt xs:boolean(\"false\")",
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
   *  Test: op-boolean-less-than-more-args-007.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:19:25 2005                        
   *  Purpose: To check if arg1: Boolean is less than arg2:Boolean 
   * **************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThanMoreArgs007() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") lt xs:boolean(\"true\")",
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
   *  Test: op-boolean-less-than-more-args-008.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:19:25 2005                        
   *  Purpose: To check if arg1: Boolean is less than arg2:Boolean 
   * **************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThanMoreArgs008() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") ge xs:boolean(\"true\")",
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
   *  Test: op-boolean-less-than-more-args-009.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:19:25 2005                        
   *  Purpose: To check if arg1: Boolean is less than arg2:Boolean 
   * **************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThanMoreArgs009() {
    final XQuery query = new XQuery(
      "xs:boolean(\"1\") ge xs:boolean(\"true\")",
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
   *  Test: op-boolean-less-than-more-args-010.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:19:25 2005                        
   *  Purpose: To check if arg1: Boolean is less than arg2:Boolean 
   * **************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThanMoreArgs010() {
    final XQuery query = new XQuery(
      "xs:boolean(\"0\") ge xs:boolean(\"true\")",
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
   *  Test: op-boolean-less-than-more-args-011.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:19:25 2005                        
   *  Purpose: To check if arg1: Boolean is less than arg2:Boolean 
   * **************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThanMoreArgs011() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") ge xs:boolean(\"1\")",
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
   *  Test: op-boolean-less-than-more-args-012.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:19:25 2005                        
   *  Purpose: To check if arg1: Boolean is less than arg2:Boolean 
   * **************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThanMoreArgs012() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") ge xs:boolean(\"0\")",
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
   *  Test: op-boolean-less-than-more-args-013.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:19:25 2005                        
   *  Purpose: To check if arg1: Boolean is less than arg2:Boolean 
   * **************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThanMoreArgs013() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") ge xs:boolean(\"false\")",
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
   *  Test: op-boolean-less-than-more-args-014.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:19:25 2005                        
   *  Purpose: To check if arg1: Boolean is less than arg2:Boolean 
   * **************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThanMoreArgs014() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") ge xs:boolean(\"true\")",
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
   * Test: op-boolean-less-than2args-1                       
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(lower bound)                        
   * $arg2 = xs:boolean(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan2args1() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") lt xs:boolean(\"false\")",
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
   * Test: op-boolean-less-than2args-10                      
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(lower bound)                        
   * $arg2 = xs:boolean(upper bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan2args10() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") ge xs:boolean(\"0\")",
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
   * Test: op-boolean-less-than2args-2                       
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(mid range)                          
   * $arg2 = xs:boolean(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan2args2() {
    final XQuery query = new XQuery(
      "xs:boolean(\"1\") lt xs:boolean(\"false\")",
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
   * Test: op-boolean-less-than2args-3                       
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(upper bound)                        
   * $arg2 = xs:boolean(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan2args3() {
    final XQuery query = new XQuery(
      "xs:boolean(\"0\") lt xs:boolean(\"false\")",
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
   * Test: op-boolean-less-than2args-4                       
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(lower bound)                        
   * $arg2 = xs:boolean(mid range)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan2args4() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") lt xs:boolean(\"1\")",
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
   * Test: op-boolean-less-than2args-5                       
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(lower bound)                        
   * $arg2 = xs:boolean(upper bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan2args5() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") lt xs:boolean(\"0\")",
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
   * Test: op-boolean-less-than2args-6                       
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(lower bound)                        
   * $arg2 = xs:boolean(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan2args6() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") ge xs:boolean(\"false\")",
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
   * Test: op-boolean-less-than2args-7                       
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(mid range)                          
   * $arg2 = xs:boolean(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan2args7() {
    final XQuery query = new XQuery(
      "xs:boolean(\"1\") ge xs:boolean(\"false\")",
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
   * Test: op-boolean-less-than2args-8                       
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(upper bound)                        
   * $arg2 = xs:boolean(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan2args8() {
    final XQuery query = new XQuery(
      "xs:boolean(\"0\") ge xs:boolean(\"false\")",
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
   * Test: op-boolean-less-than2args-9                       
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(lower bound)                        
   * $arg2 = xs:boolean(mid range)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanLessThan2args9() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") ge xs:boolean(\"1\")",
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
}
