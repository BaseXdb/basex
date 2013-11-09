package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the boolean-equal() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpBooleanEqual extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-BooleanEqual-1                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Compare two xs:boolean values.               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kBooleanEqual1() {
    final XQuery query = new XQuery(
      "false() eq false()",
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
   *  Test: K-BooleanEqual-2                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Compare two xs:boolean values.               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kBooleanEqual2() {
    final XQuery query = new XQuery(
      "true() eq true()",
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
   *  Test: K-BooleanEqual-3                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Compare two xs:boolean values.               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kBooleanEqual3() {
    final XQuery query = new XQuery(
      "false() ne true()",
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
   *  Test: K-BooleanEqual-4                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Compare two xs:boolean values.               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kBooleanEqual4() {
    final XQuery query = new XQuery(
      "true() ne false()",
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
   *  Test: K-BooleanEqual-5                                
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A complex value-comparison involving xs:boolean. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kBooleanEqual5() {
    final XQuery query = new XQuery(
      "((((((((((((false() eq false()) eq false()) eq false()) eq false()) eq false()) eq false()) eq false()) eq false()) eq false()) eq false()) eq false()) eq false()) eq false()",
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
   *  Test: K2-BooleanEqual-1                               
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Cannot compare xs:boolean and xs:untypedAtomic. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void k2BooleanEqual1() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") eq true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K2-BooleanEqual-2                               
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Cannot compare xs:boolean and xs:untypedAtomic. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void k2BooleanEqual2() {
    final XQuery query = new XQuery(
      "<name>true</name> eq true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K2-BooleanEqual-3                               
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Cannot compare xs:boolean and xs:untypedAtomic(#2). 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void k2BooleanEqual3() {
    final XQuery query = new XQuery(
      "true() eq <name>true</name>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   *  Name: cbcl-boolean-equal-001 
   *  Description: test equality of xs:boolean 
   *  Author: Tim Mills 
   *  Date: 2008-05-14 
   * .
   */
  @org.junit.Test
  public void cbclBooleanEqual001() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      fn:false() eq local:is-even(17)",
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
   *  Name: cbcl-boolean-equal-002 
   *  Description: test equality of xs:boolean 
   *  Author: Tim Mills 
   *  Date: 2008-05-14 
   * .
   */
  @org.junit.Test
  public void cbclBooleanEqual002() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      not(local:is-even(13) eq local:is-even(17))",
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
   *  Name: cbcl-boolean-equal-003 
   *  Description: test equality of xs:boolean 
   *  Author: Tim Mills 
   *  Date: 2008-05-14 
   * .
   */
  @org.junit.Test
  public void cbclBooleanEqual003() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      fn:true() ne local:is-even(17)",
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
   *  Name: cbcl-boolean-equal-004 
   *  Description: test equality of xs:boolean 
   *  Author: Tim Mills 
   *  Date: 2008-05-14 
   * .
   */
  @org.junit.Test
  public void cbclBooleanEqual004() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      local:is-even(17) ne fn:true()",
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
   * test equality of xs:boolean.
   */
  @org.junit.Test
  public void cbclBooleanEqual005() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      local:is-even(17) ne fn:false()",
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
   * test equal to of xs:boolean.
   */
  @org.junit.Test
  public void cbclBooleanEqual006() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:is-even($arg as xs:integer) as xs:boolean { (($arg mod 2) eq 0) }; \n" +
      "      not(not(local:is-even(17) eq local:is-even(16)))",
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
   * Test: op-boolean-equal-1                               
   * Written By: Carmelo Montanez                           
   * Date: June 24, 2005                                    
   * Purpose: Evaluates The "boolean-equal" function        
   *  with operands set to "not(true)", "true" respectively.
   *  Use of eq operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual1() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"true\")) eq xs:boolean(\"true\")",
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
   * Test: op-boolean-equal-10                              
   * Written By: Carmelo Montanez                           
   * Date: June 24, 2005                                    
   * Purpose: Evaluates The "boolean-equal" function        
   *  with operands set to "(7 eq 7)" and "false" respectively. 
   *  Use of ge operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual10() {
    final XQuery query = new XQuery(
      "(7 eq 7) eq xs:boolean(\"false\")",
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
   * Test: op-boolean-equal-11                              
   * Written By: Carmelo Montanez                           
   * Date: June 24, 2005                                    
   * Purpose: Evaluates The "boolean-equal" function        
   *  with operands set to "not(7 lt 7)", "true" respectively.
   *  Use of lt operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual11() {
    final XQuery query = new XQuery(
      "fn:not(7 lt 7) eq xs:boolean(\"true\")",
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
   * Test: op-boolean-equal-12                              
   * Written By: Carmelo Montanez                           
   * Date: June 24, 2005                                    
   * Purpose: Evaluates The "boolean-equal" function        
   *  with operands set to "not(7 lt 7)" and "false" respectively.
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual12() {
    final XQuery query = new XQuery(
      "fn:not(7 lt 7) and xs:boolean(\"false\")",
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
   * Test: op-boolean-equal-13                              
   * Written By: Carmelo Montanez                           
   * Date: June 24, 2005                                    
   * Purpose: Evaluates The "boolean-equal" function        
   *  with both operands set to expressions unsing multiple 
   *  "eq" operators connected by an "and" expression.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual13() {
    final XQuery query = new XQuery(
      "((7 eq 7) eq xs:boolean(\"true\")) and (xs:boolean(\"false\") eq xs:boolean(\"true\"))",
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
   * Test: op-boolean-equal-14                              
   * Written By: Carmelo Montanez                           
   * Date: June 24, 2005                                    
   * Purpose: Evaluates The "boolean-equal" function        
   *  with both operands set to expressions unsing multiple 
   *  "eq" operators connected by an "or" expression.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual14() {
    final XQuery query = new XQuery(
      "((7 eq 7) eq xs:boolean(\"true\")) or (xs:boolean(\"false\") eq xs:boolean(\"true\"))",
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
   * Test: op-boolean-equal-15                              
   * Written By: Carmelo Montanez                           
   * Date: June 24, 2005                                    
   * Purpose: Evaluates The "boolean-equal" function        
   *  using the "starts-with" function.                     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual15() {
    final XQuery query = new XQuery(
      "fn:starts-with(\"Query\",\"Que\") eq xs:boolean(\"false\")",
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
   * Test: op-boolean-equal-16                              
   * Written By: Carmelo Montanez                           
   * Date: June 24, 2005                                    
   * Purpose: Evaluates The "boolean-equal" function        
   *  using the "ends-with" function.                       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual16() {
    final XQuery query = new XQuery(
      "fn:ends-with(\"Query\",\"ry\") eq xs:boolean(\"false\")",
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
   * Test: op-boolean-equal-17                              
   * Written By: Carmelo Montanez                           
   * Date: March 26, 2006                                   
   * Purpose: Evaluates cardinality violation on a sequence 
   *  of two or more items.                                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual17() {
    final XQuery query = new XQuery(
      "let $e := (0,1) return $e eq 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-boolean-equal-2                               
   * Written By: Carmelo Montanez                           
   * Date: June 24, 2005                                    
   * Purpose: Evaluates The "boolean-equal" function        
   *  with operands set to "not(true)", "false" respectively.
   *  Use of eq operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual2() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"true\")) eq xs:boolean(\"false\")",
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
   * Test: op-boolean-equal-3                               
   * Written By: Carmelo Montanez                           
   * Date: June 24, 2005                                    
   * Purpose: Evaluates The "boolean-equal" function        
   *  with operands set to "not(false)" ad "false" respectively.
   *  Use of eq operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual3() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"false\")) eq xs:boolean(\"false\")",
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
   * Test: op-boolean-equal-4                               
   * Written By: Carmelo Montanez                           
   * Date: June 24, 2005                                    
   * Purpose: Evaluates The "boolean-equal" function        
   *  with both operands set to "and" expressions respectively.
   *  Use of eq operator.                                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual4() {
    final XQuery query = new XQuery(
      "(xs:boolean(\"true\") and xs:boolean(\"true\")) eq (xs:boolean(\"false\") and xs:boolean(\"false\"))",
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
   * Test: op-boolean-equal-5                               
   * Written By: Carmelo Montanez                           
   * Date: June 24, 2005                                    
   * Purpose: Evaluates The "boolean-equal" function        
   *  with left operand set to and "and" expressions with the 
   *  "not" function and right operand set to "true".       
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual5() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"true\") and xs:boolean(\"true\")) eq xs:boolean(\"true\")",
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
   * Test: op-boolean-equal-6                               
   * Written By: Carmelo Montanez                           
   * Date: June 24, 2005                                    
   * Purpose: Evaluates The "boolean-equal" function        
   *  with left operand set to and "and" expressions with the 
   *  "not" function and right operand set to "false".  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual6() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"false\") and xs:boolean(\"false\")) eq xs:boolean(\"false\")",
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
   * Test: op-boolean-equal-7                               
   * Written By: Carmelo Montanez                           
   * Date: June 24, 2005                                    
   * Purpose: Evaluates The "boolean-equal" function        
   *  with operands set to "fn:not(false)" and "fn:not(false)" 
   *  respectively.                                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual7() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"false\")) eq fn:not(xs:boolean(\"false\"))",
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
   * Test: op-boolean-equal-8                               
   * Written By: Carmelo Montanez                           
   * Date: June 24, 2005                                    
   * Purpose: Evaluates The "boolean-equal" function  with  
   *  with operands set to "not(false and true)" and        
   *  "not(false and true)" respectively.                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual8() {
    final XQuery query = new XQuery(
      "fn:not(xs:boolean(\"false\") and xs:boolean(\"true\")) eq fn:not(xs:boolean(\"false\") and xs:boolean(\"true\"))",
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
   * Test: op-boolean-equal-9                               
   * Written By: Carmelo Montanez                           
   * Date: June 24, 2005                                    
   * Purpose: Evaluates The "boolean-equal" function        
   *  with operands set to "(7 lt 7)" and "true" respectively. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual9() {
    final XQuery query = new XQuery(
      "(7 lt 7) eq xs:boolean(\"true\")",
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
   *  Test: op-boolean-equal-more-args-001.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs001() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") eq xs:boolean(\"true\")",
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
   *  Test: op-boolean-equal-more-args-002.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs002() {
    final XQuery query = new XQuery(
      "xs:boolean('false') eq xs:boolean('1')",
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
   *  Test: op-boolean-equal-more-args-003.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs003() {
    final XQuery query = new XQuery(
      "xs:boolean('false') eq xs:boolean('0')",
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
   *  Test: op-boolean-equal-more-args-004.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs004() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") eq xs:boolean('0')",
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
   *  Test: op-boolean-equal-more-args-005.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs005() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") eq xs:boolean(\"1\")",
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
   *  Test: op-boolean-equal-more-args-006.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs006() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") eq true()",
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
   *  Test: op-boolean-equal-more-args-007.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs007() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") eq false()",
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
   *  Test: op-boolean-equal-more-args-008.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs008() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") eq xs:boolean(\"true\")",
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
   *  Test: op-boolean-equal-more-args-009.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs009() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") eq xs:boolean(\"false\")",
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
   *  Test: op-boolean-equal-more-args-010.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs010() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") eq xs:boolean(\"1\")",
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
   *  Test: op-boolean-equal-more-args-011.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs011() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") eq xs:boolean(\"0\")",
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
   *  Test: op-boolean-equal-more-args-012.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs012() {
    final XQuery query = new XQuery(
      "xs:boolean(\"1\") eq xs:boolean(\"true\")",
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
   *  Test: op-boolean-equal-more-args-013.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs013() {
    final XQuery query = new XQuery(
      "xs:boolean(\"0\") eq xs:boolean(\"true\")",
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
   *  Test: op-boolean-equal-more-args-014.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs014() {
    final XQuery query = new XQuery(
      "xs:boolean('true') eq xs:boolean('1')",
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
   *  Test: op-boolean-equal-more-args-015.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs015() {
    final XQuery query = new XQuery(
      "xs:boolean('true') eq xs:boolean('0')",
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
   *  Test: op-boolean-equal-more-args-016.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs016() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") eq xs:boolean('1')",
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
   *  Test: op-boolean-equal-more-args-017.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs017() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") eq xs:boolean(\"1\")",
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
   *  Test: op-boolean-equal-more-args-018.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs018() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") eq true()",
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
   *  Test: op-boolean-equal-more-args-019.xq          
   *  Written By: Pulkita Tyagi                             
   *  Date: Thu Jun  2 00:10:02 2005                        
   *  Purpose: To check if args of Boolean type are equal   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqualMoreArgs019() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") eq false()",
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
   * Test: op-boolean-equal2args-1                           
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-equal" operator     
   *  with the arguments set as follows:                    
   * $value1 = xs:boolean(lower bound)                      
   * $value2 = xs:boolean(lower bound)                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual2args1() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") eq xs:boolean(\"false\")",
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
   * Test: op-boolean-equal2args-2                           
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-equal" operator     
   *  with the arguments set as follows:                    
   * $value1 = xs:boolean(mid range)                        
   * $value2 = xs:boolean(lower bound)                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual2args2() {
    final XQuery query = new XQuery(
      "xs:boolean(\"1\") eq xs:boolean(\"false\")",
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
   * Test: op-boolean-equal2args-3                           
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-equal" operator     
   *  with the arguments set as follows:                    
   * $value1 = xs:boolean(upper bound)                      
   * $value2 = xs:boolean(lower bound)                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual2args3() {
    final XQuery query = new XQuery(
      "xs:boolean(\"0\") eq xs:boolean(\"false\")",
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
   * Test: op-boolean-equal2args-4                           
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-equal" operator     
   *  with the arguments set as follows:                    
   * $value1 = xs:boolean(lower bound)                      
   * $value2 = xs:boolean(mid range)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual2args4() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") eq xs:boolean(\"1\")",
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
   * Test: op-boolean-equal2args-5                           
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:17 GMT-05:00 2004                
   * Purpose: Evaluates The "op:boolean-equal" operator     
   *  with the arguments set as follows:                    
   * $value1 = xs:boolean(lower bound)                      
   * $value2 = xs:boolean(upper bound)                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBooleanEqual2args5() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") eq xs:boolean(\"0\")",
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
