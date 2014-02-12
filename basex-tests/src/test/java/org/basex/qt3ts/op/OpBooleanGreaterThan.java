package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the boolean-greater-than() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpBooleanGreaterThan extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-BooleanGT-1                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Compare two xs:boolean values.               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kBooleanGT1() {
    final XQuery query = new XQuery(
      "true() gt false()",
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
   *  Test: K-BooleanGT-2                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Compare two xs:boolean values.               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kBooleanGT2() {
    final XQuery query = new XQuery(
      "true() ge false()",
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
   *  Test: K-BooleanGT-3                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Compare two xs:boolean values.               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kBooleanGT3() {
    final XQuery query = new XQuery(
      "true() ge true()",
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
  public void cbclBooleanGreaterThan001() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      not(local:is-even(15) ge local:is-even(17))",
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
  public void cbclBooleanGreaterThan002() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      fn:true() ge local:is-even(17)",
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
  public void cbclBooleanGreaterThan003() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      fn:false() ge local:is-even(17)",
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
  public void cbclBooleanGreaterThan004() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      not(local:is-even(15) gt local:is-even(17))",
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
  public void cbclBooleanGreaterThan005() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      fn:true() gt local:is-even(17)",
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
  public void cbclBooleanGreaterThan006() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      fn:false() gt local:is-even(17)",
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
  public void cbclBooleanGreaterThan007() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      local:is-even(17) ge fn:true()",
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
  public void cbclBooleanGreaterThan008() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      local:is-even(17) ge fn:false()",
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
  public void cbclBooleanGreaterThan009() {
    final XQuery query = new XQuery(
      "declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      local:is-even(17) gt fn:true()",
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
  public void cbclBooleanGreaterThan010() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      local:is-even(17) gt fn:false()",
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
   * Test: op-boolean-greater-than-1                        
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-greater-than" function 
   *  with operands set to "not(true)", "true" respectively.
   *  Use of gt operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan1() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"true\")) gt xs:boolean(\"true\")",
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
   * Test: op-boolean-greater-than-10                       
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-greater-than" function 
   *  with operands set to "(7 eq 7)", "true" respectively.
   *  Use of le operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan10() {
    final XQuery query = new XQuery(
      "(7 eq 7) le xs:boolean(\"true\")",
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
   * Test: op-boolean-greater-than-11                       
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-greater-than" function 
   *  with operands set to "(7 eq 7)", "false" respectively.
   *  Use of gt operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan11() {
    final XQuery query = new XQuery(
      "(7 eq 7) gt xs:boolean(\"false\")",
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
   * Test: op-boolean-greater-than-12                       
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-greater-than" function 
   *  with operands set to "(7 eq 7)", "false" respectively.
   *  Use of le operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan12() {
    final XQuery query = new XQuery(
      "(7 eq 7) le xs:boolean(\"false\")",
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
   * Test: op-boolean-greater-than-2                        
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-greater-than" function 
   *  with operands set to "not(true)", "true" respectively.
   *  Use of le operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan2() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"true\")) le xs:boolean(\"true\")",
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
   * Test: op-boolean-greater-than-3                        
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-greater-than" function 
   *  with operands set to "not(true)", "false" respectively.
   *  Use of gt operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan3() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"true\")) gt xs:boolean(\"false\")",
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
   * Test: op-boolean-greater-than-4                        
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-greater-than" function 
   *  with operands set to "not(true)", "false" respectively.
   *  Use of le operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan4() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"true\")) le xs:boolean(\"false\")",
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
   * Test: op-boolean-greater-than-5                        
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-greater-than" function 
   *  with operands set to "not(false)", "true" respectively.
   *  Use of gt operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan5() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"false\")) gt xs:boolean(\"true\")",
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
   * Test: op-boolean-greater-than-6                        
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-greater-than" function 
   *  with operands set to "not(false)", "true" respectively.
   *  Use of le operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan6() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"false\")) le xs:boolean(\"true\")",
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
   * Test: op-boolean-greater-than-7                        
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-greater-than" function 
   *  with operands set to "not(false)", "false" respectively.
   *  Use of gt operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan7() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"false\")) gt xs:boolean(\"false\")",
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
   * Test: op-boolean-greater-than-8                        
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-greater-than" function 
   *  with operands set to "not(false)", "false" respectively.
   *  Use of le operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan8() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"false\")) le xs:boolean(\"false\")",
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
   * Test: op-boolean-greater-than-9                        
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "boolean-greater-than" function 
   *  with operands set to "(7 eq 7)", "true" respectively.
   *  Use of gt operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan9() {
    final XQuery query = new XQuery(
      "(7 eq 7) gt xs:boolean(\"true\")",
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
   *  Test: op-boolean-greater-than-more-args-001.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:16:48 2005                        
   *  Purpose: To check if arg1: Boolean is greater than arg2:Boolean 
   * *****************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThanMoreArgs001() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") gt xs:boolean(\"true\")",
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
   *  Test: op-boolean-greater-than-more-args-002.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:16:48 2005                        
   *  Purpose: To check if arg1: Boolean is greater than arg2:Boolean 
   * *****************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThanMoreArgs002() {
    final XQuery query = new XQuery(
      "xs:boolean(\"1\") gt xs:boolean(\"true\")",
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
   *  Test: op-boolean-greater-than-more-args-003.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:16:48 2005                        
   *  Purpose: To check if arg1: Boolean is greater than arg2:Boolean 
   * *****************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThanMoreArgs003() {
    final XQuery query = new XQuery(
      "xs:boolean(\"0\") gt xs:boolean(\"true\")",
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
   *  Test: op-boolean-greater-than-more-args-004.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:16:48 2005                        
   *  Purpose: To check if arg1: Boolean is greater than arg2:Boolean 
   * *****************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThanMoreArgs004() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") gt xs:boolean(\"1\")",
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
   *  Test: op-boolean-greater-than-more-args-005.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:16:48 2005                        
   *  Purpose: To check if arg1: Boolean is greater than arg2:Boolean 
   * *****************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThanMoreArgs005() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") gt xs:boolean(\"0\")",
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
   *  Test: op-boolean-greater-than-more-args-006.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:16:48 2005                        
   *  Purpose: To check if arg1: Boolean is greater than arg2:Boolean 
   * *****************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThanMoreArgs006() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") gt xs:boolean(\"false\")",
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
   *  Test: op-boolean-greater-than-more-args-007.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:16:48 2005                        
   *  Purpose: To check if arg1: Boolean is greater than arg2:Boolean 
   * *****************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThanMoreArgs007() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") gt xs:boolean(\"true\")",
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
   *  Test: op-boolean-greater-than-more-args-008.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:16:48 2005                        
   *  Purpose: To check if arg1: Boolean is less or equal to arg2:Boolean 
   * *********************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThanMoreArgs008() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") le xs:boolean(\"true\")",
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
   *  Test: op-boolean-greater-than-more-args-009.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:16:48 2005                        
   *  Purpose: To check if arg1: Boolean is less or equal to arg2:Boolean 
   * *********************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThanMoreArgs009() {
    final XQuery query = new XQuery(
      "xs:boolean(\"1\") le xs:boolean(\"true\")",
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
   *  Test: op-boolean-greater-than-more-args-010.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:16:48 2005                        
   *  Purpose: To check if arg1: Boolean is less or equal to arg2:Boolean 
   * *********************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThanMoreArgs010() {
    final XQuery query = new XQuery(
      "xs:boolean(\"0\") le xs:boolean(\"true\")",
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
   *  Test: op-boolean-greater-than-more-args-011.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:16:48 2005                        
   *  Purpose: To check if arg1: Boolean is less or equal to arg2:Boolean 
   * *********************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThanMoreArgs011() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") le xs:boolean(\"1\")",
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
   *  Test: op-boolean-greater-than-more-args-012.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:16:48 2005                        
   *  Purpose: To check if arg1: Boolean is less or equal to arg2:Boolean 
   * *********************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThanMoreArgs012() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") le xs:boolean(\"0\")",
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
   *  Test: op-boolean-greater-than-more-args-013.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:16:48 2005                        
   *  Purpose: To check if arg1: Boolean is less or equal to arg2:Boolean 
   * *********************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThanMoreArgs013() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") le xs:boolean(\"false\")",
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
   *  Test: op-boolean-greater-than-more-args-014.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:16:48 2005                        
   *  Purpose: To check if arg1: Boolean is less or equal to arg2:Boolean 
   * *********************************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThanMoreArgs014() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") le xs:boolean(\"true\")",
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
   * Test: op-boolean-greater-than2args-1                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(lower bound)                        
   * $arg2 = xs:boolean(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan2args1() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") gt xs:boolean(\"false\")",
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
   * Test: op-boolean-greater-than2args-10                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(lower bound)                        
   * $arg2 = xs:boolean(upper bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan2args10() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") le xs:boolean(\"0\")",
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
   * Test: op-boolean-greater-than2args-2                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(mid range)                          
   * $arg2 = xs:boolean(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan2args2() {
    final XQuery query = new XQuery(
      "xs:boolean(\"1\") gt xs:boolean(\"false\")",
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
   * Test: op-boolean-greater-than2args-3                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(upper bound)                        
   * $arg2 = xs:boolean(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan2args3() {
    final XQuery query = new XQuery(
      "xs:boolean(\"0\") gt xs:boolean(\"false\")",
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
   * Test: op-boolean-greater-than2args-4                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(lower bound)                        
   * $arg2 = xs:boolean(mid range)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan2args4() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") gt xs:boolean(\"1\")",
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
   * Test: op-boolean-greater-than2args-5                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(lower bound)                        
   * $arg2 = xs:boolean(upper bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan2args5() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") gt xs:boolean(\"0\")",
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
   * Test: op-boolean-greater-than2args-6                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(lower bound)                        
   * $arg2 = xs:boolean(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan2args6() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") le xs:boolean(\"false\")",
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
   * Test: op-boolean-greater-than2args-7                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(mid range)                          
   * $arg2 = xs:boolean(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan2args7() {
    final XQuery query = new XQuery(
      "xs:boolean(\"1\") le xs:boolean(\"false\")",
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
   * Test: op-boolean-greater-than2args-8                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(upper bound)                        
   * $arg2 = xs:boolean(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan2args8() {
    final XQuery query = new XQuery(
      "xs:boolean(\"0\") le xs:boolean(\"false\")",
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
   * Test: op-boolean-greater-than2args-9                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:boolean(lower bound)                        
   * $arg2 = xs:boolean(mid range)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanGreaterThan2args9() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") le xs:boolean(\"1\")",
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
}
