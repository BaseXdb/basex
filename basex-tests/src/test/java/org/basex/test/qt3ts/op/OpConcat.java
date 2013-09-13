package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the "||" concatenation operator (new in XPath 3.0). 
 *     Tests adapted from the fn:concat() tests by Michael Kay.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpConcat extends QT3TestSet {

  /**
   * 
   *  Purpose: A test whose essence is: `("ab" ||  "c") eq "abc"`. 
   * .
   */
  @org.junit.Test
  public void kConcatOp3() {
    final XQuery query = new XQuery(
      "(\"ab\" ||  \"c\") eq \"abc\"",
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
   *  Purpose: A test whose essence is: `concat("ab" ||  "c") instance of xs:string`. 
   * .
   */
  @org.junit.Test
  public void kConcatOp4() {
    final XQuery query = new XQuery(
      "(\"ab\" ||  \"c\") instance of xs:string",
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
   *  Purpose: A test whose essence is: `concat(() ||  ()) instance of xs:string`. 
   * .
   */
  @org.junit.Test
  public void kConcatOp5() {
    final XQuery query = new XQuery(
      "(() ||  ()) instance of xs:string",
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
   *  Purpose: A test whose essence is: `concat(() ||  ()) eq ""`. 
   * .
   */
  @org.junit.Test
  public void kConcatOp6() {
    final XQuery query = new XQuery(
      "(() ||  ()) eq \"\"",
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
   *  Purpose: A test whose essence is: `('a' ||  'b' ||  'c' ||  () ||  'd' ||  'e' ||  'f' ||  'g' ||  'h' ||  ' ' ||  'i' ||  'j' ||  'k l') eq "abcdefgh ijk l"`. 
   * .
   */
  @org.junit.Test
  public void kConcatOp7() {
    final XQuery query = new XQuery(
      "('a' ||  'b' ||  'c' ||  () ||  'd' ||  'e' ||  'f' ||  'g' ||  'h' ||  ' ' ||  'i' ||  'j' ||  'k l') eq \"abcdefgh ijk l\"",
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
   *  Purpose: A test whose essence is: `(1 ||  2 ||  3) eq "123"`. 
   * .
   */
  @org.junit.Test
  public void kConcatOp8() {
    final XQuery query = new XQuery(
      "(1 ||  2 ||  3) eq \"123\"",
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
   *  Purpose: A test whose essence is: `(1 ||  "2" ||  3) eq "123"`. 
   * .
   */
  @org.junit.Test
  public void kConcatOp9() {
    final XQuery query = new XQuery(
      "(1 ||  \"2\" ||  3) eq \"123\"",
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
   * Evaluation of concat function as per example 1 (for this function) 
   *  from the F&O specs.   
   * .
   */
  @org.junit.Test
  public void opConcat1() {
    final XQuery query = new XQuery(
      "('un' ||  'grateful')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ungrateful")
    );
  }

  /**
   * Evaluation of concat function with argument set to "*****"
   * .
   */
  @org.junit.Test
  public void opConcat10() {
    final XQuery query = new XQuery(
      "(\"**\" || \"***\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "*****")
    );
  }

  /**
   * Evaluation of concat function with argument that uses another concat function
   * .
   */
  @org.junit.Test
  public void opConcat11() {
    final XQuery query = new XQuery(
      "((\"zzz\" || \"zz\") || \"123\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "zzzzz123")
    );
  }

  /**
   * Evaluation of concat function as an argument to the "fn:boolean" function
   * .
   */
  @org.junit.Test
  public void opConcat12() {
    final XQuery query = new XQuery(
      "fn:boolean((\"ab\" || \"cde\"))",
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
   * Evaluation of concat function as an argument to the "fn:string" function
   * .
   */
  @org.junit.Test
  public void opConcat13() {
    final XQuery query = new XQuery(
      "fn:string((\"abc\" || \"de\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abcde")
    );
  }

  /**
   * Evaluation of concat function as an argument to the "fn:not" function
   * .
   */
  @org.junit.Test
  public void opConcat14() {
    final XQuery query = new XQuery(
      "fn:not((\"ab\" || \"cde\"))",
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
   * Evaluation of concat function with argument set to "%$" || #@!"
   * .
   */
  @org.junit.Test
  public void opConcat15() {
    final XQuery query = new XQuery(
      "(\"%$\" || \"#@!\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "%$#@!")
    );
  }

  /**
   * Evaluation of concat function with argument set to "concat" || "concat"
   * .
   */
  @org.junit.Test
  public void opConcat16() {
    final XQuery query = new XQuery(
      "(\"concat\" || \"concat\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "concatconcat")
    );
  }

  /**
   * Evaluation of concat function as part of a boolean expression.
   */
  @org.junit.Test
  public void opConcat17() {
    final XQuery query = new XQuery(
      "(\"abc\" || \"abc\") and (\"abc\" || \"abc\")",
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
   * Can't have a function item as an argument to concat.
   */
  @org.junit.Test
  public void opConcat18() {
    final XQuery query = new XQuery(
      "(\"abc\" || \"abc\" ||  fn:concat#3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOTY0013")
    );
  }

  /**
   * Concat operator has lower precedence than plus/minus.
   */
  @org.junit.Test
  public void opConcat19() {
    final XQuery query = new XQuery(
      "12 || 34 - 50",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("\"12-16\"")
      &&
        assertType("xs:string")
      )
    );
  }

  /**
   * Evaluation of concat function as per example 2 (for this function) 
   *  from the F&O specs.   
   * .
   */
  @org.junit.Test
  public void opConcat2() {
    final XQuery query = new XQuery(
      "('Thy ' ||  () ||  'old ' ||  \"groans\" ||  \"\" ||  ' ring' ||  ' yet' ||  ' in' ||  ' my' ||  ' ancient' || ' ears.')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Thy old groans ring yet in my ancient ears.")
    );
  }

  /**
   * Concat operator has higher precedence than eq.
   */
  @org.junit.Test
  public void opConcat20() {
    final XQuery query = new XQuery(
      "\"1234\" eq 12 || 34",
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
   * Evaluation of concat function as per example 3 (for this function) 
   *  from the F&O specs.   
   * .
   */
  @org.junit.Test
  public void opConcat3() {
    final XQuery query = new XQuery(
      "('Ciao!' || ())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Ciao!")
    );
  }

  /**
   * Evaluation of concat function as per example 4 (for this function) 
   *  from the F&O specs.   
   * .
   */
  @org.junit.Test
  public void opConcat4() {
    final XQuery query = new XQuery(
      "('Ingratitude, ' ||  'thou ' ||  'marble-hearted' ||  ' fiend!')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Ingratitude, thou marble-hearted fiend!")
    );
  }

  /**
   * Evaluation of concat function that uses only upper case letters as part of argument
   * .
   */
  @org.junit.Test
  public void opConcat5() {
    final XQuery query = new XQuery(
      "(\"AB\" || \"CD\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ABCD")
    );
  }

  /**
   * Evaluation of concat function that uses only lower case letters as part of argument
   * .
   */
  @org.junit.Test
  public void opConcat6() {
    final XQuery query = new XQuery(
      "(\"abc\" || \"de\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "abcde")
    );
  }

  /**
   * Evaluation of concat function that uses both upper and lower case letters as part of argument
   * .
   */
  @org.junit.Test
  public void opConcat7() {
    final XQuery query = new XQuery(
      "(\"ABCDE\" || \"abcde\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ABCDEabcde")
    );
  }

  /**
   * Evaluation of concat function that uses the empty string as part of argument
   *  Uses "fn:count" to avoid the empty file  
   * .
   */
  @org.junit.Test
  public void opConcat8() {
    final XQuery query = new XQuery(
      "fn:count((\"\" || \"\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Evaluation of concat function that uses the "upper-case" function as part of argument
   * .
   */
  @org.junit.Test
  public void opConcat9() {
    final XQuery query = new XQuery(
      "(fn:upper-case(\"Abc\") || fn:upper-case(\"DH\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ABCDH")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatdbl2args-1                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(lower bound)                         
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatdbl2args1() {
    final XQuery query = new XQuery(
      "(xs:double(\"-1.7976931348623157E308\")||xs:double(\"-1.7976931348623157E308\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1.7976931348623157E308-1.7976931348623157E308")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatdbl2args-2                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(mid range)                           
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatdbl2args2() {
    final XQuery query = new XQuery(
      "(xs:double(\"0\")||xs:double(\"-1.7976931348623157E308\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0-1.7976931348623157E308")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatdbl2args-3                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(upper bound)                         
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatdbl2args3() {
    final XQuery query = new XQuery(
      "(xs:double(\"1.7976931348623157E308\")||xs:double(\"-1.7976931348623157E308\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.7976931348623157E308-1.7976931348623157E308")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatdbl2args-4                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(lower bound)                         
   * $arg2 = xs:double(mid range)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatdbl2args4() {
    final XQuery query = new XQuery(
      "(xs:double(\"-1.7976931348623157E308\") || xs:double(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1.7976931348623157E3080")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatdbl2args-5                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(lower bound)                         
   * $arg2 = xs:double(upper bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatdbl2args5() {
    final XQuery query = new XQuery(
      "(xs:double(\"-1.7976931348623157E308\") || xs:double(\"1.7976931348623157E308\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1.7976931348623157E3081.7976931348623157E308")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatdec2args-1                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(lower bound)                        
   * $arg2 = xs:decimal(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatdec2args1() {
    final XQuery query = new XQuery(
      "(xs:decimal(\"-999999999999999999\")||xs:decimal(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999-999999999999999999")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatdec2args-2                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(mid range)                          
   * $arg2 = xs:decimal(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatdec2args2() {
    final XQuery query = new XQuery(
      "(xs:decimal(\"617375191608514839\")||xs:decimal(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "617375191608514839-999999999999999999")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatdec2args-3                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(upper bound)                        
   * $arg2 = xs:decimal(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatdec2args3() {
    final XQuery query = new XQuery(
      "(xs:decimal(\"999999999999999999\")||xs:decimal(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "999999999999999999-999999999999999999")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatdec2args-4                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(lower bound)                        
   * $arg2 = xs:decimal(mid range)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatdec2args4() {
    final XQuery query = new XQuery(
      "(xs:decimal(\"-999999999999999999\")||xs:decimal(\"617375191608514839\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999617375191608514839")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatdec2args-5                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(lower bound)                        
   * $arg2 = xs:decimal(upper bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatdec2args5() {
    final XQuery query = new XQuery(
      "(xs:decimal(\"-999999999999999999\")||xs:decimal(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999999999999999999999")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatflt2args-1                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(lower bound)                          
   * $arg2 = xs:float(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatflt2args1() {
    final XQuery query = new XQuery(
      "(xs:float(\"-3.4028235E38\") || xs:float(\"-3.4028235E38\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-3.4028235E38-3.4028235E38")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatflt2args-2                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(mid range)                            
   * $arg2 = xs:float(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatflt2args2() {
    final XQuery query = new XQuery(
      "(xs:float(\"0\") || xs:float(\"-3.4028235E38\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0-3.4028235E38")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatflt2args-3                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(upper bound)                          
   * $arg2 = xs:float(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatflt2args3() {
    final XQuery query = new XQuery(
      "(xs:float(\"3.4028235E38\") || xs:float(\"-3.4028235E38\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3.4028235E38-3.4028235E38")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatflt2args-4                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(lower bound)                          
   * $arg2 = xs:float(mid range)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatflt2args4() {
    final XQuery query = new XQuery(
      "(xs:float(\"-3.4028235E38\") || xs:float(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-3.4028235E380")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatflt2args-5                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(lower bound)                          
   * $arg2 = xs:float(upper bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatflt2args5() {
    final XQuery query = new XQuery(
      "(xs:float(\"-3.4028235E38\") || xs:float(\"3.4028235E38\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-3.4028235E383.4028235E38")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatint2args-1                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(lower bound)                            
   * $arg2 = xs:int(lower bound)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatint2args1() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\")||xs:int(\"-2147483648\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-2147483648-2147483648")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatint2args-2                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(mid range)                              
   * $arg2 = xs:int(lower bound)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatint2args2() {
    final XQuery query = new XQuery(
      "xs:int(\"-1873914410\")||xs:int(\"-2147483648\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1873914410-2147483648")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatint2args-3                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(upper bound)                            
   * $arg2 = xs:int(lower bound)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatint2args3() {
    final XQuery query = new XQuery(
      "xs:int(\"2147483647\")||xs:int(\"-2147483648\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2147483647-2147483648")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatint2args-4                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(lower bound)                            
   * $arg2 = xs:int(mid range)                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatint2args4() {
    final XQuery query = new XQuery(
      "(xs:int(\"-2147483648\")||xs:int(\"-1873914410\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-2147483648-1873914410")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatint2args-5                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(lower bound)                            
   * $arg2 = xs:int(upper bound)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatint2args5() {
    final XQuery query = new XQuery(
      "(xs:int(\"-2147483648\")||xs:int(\"2147483647\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-21474836482147483647")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatintg2args-1                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(lower bound)                        
   * $arg2 = xs:integer(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatintg2args1() {
    final XQuery query = new XQuery(
      "(xs:integer(\"-999999999999999999\")||xs:integer(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999-999999999999999999")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatintg2args-2                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(mid range)                          
   * $arg2 = xs:integer(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatintg2args2() {
    final XQuery query = new XQuery(
      "(xs:integer(\"830993497117024304\")||xs:integer(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "830993497117024304-999999999999999999")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatintg2args-3                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(upper bound)                        
   * $arg2 = xs:integer(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatintg2args3() {
    final XQuery query = new XQuery(
      "(xs:integer(\"999999999999999999\")||xs:integer(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "999999999999999999-999999999999999999")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatintg2args-4                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(lower bound)                        
   * $arg2 = xs:integer(mid range)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatintg2args4() {
    final XQuery query = new XQuery(
      "(xs:integer(\"-999999999999999999\")||xs:integer(\"830993497117024304\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999830993497117024304")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatintg2args-5                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(lower bound)                        
   * $arg2 = xs:integer(upper bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatintg2args5() {
    final XQuery query = new XQuery(
      "(xs:integer(\"-999999999999999999\")||xs:integer(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999999999999999999999")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatlng2args-1                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(lower bound)                           
   * $arg2 = xs:long(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatlng2args1() {
    final XQuery query = new XQuery(
      "(xs:long(\"-92233720368547758\") || xs:long(\"-92233720368547758\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-92233720368547758-92233720368547758")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatlng2args-2                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(mid range)                             
   * $arg2 = xs:long(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatlng2args2() {
    final XQuery query = new XQuery(
      "(xs:long(\"-47175562203048468\") || xs:long(\"-92233720368547758\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-47175562203048468-92233720368547758")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatlng2args-3                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(upper bound)                           
   * $arg2 = xs:long(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatlng2args3() {
    final XQuery query = new XQuery(
      "(xs:long(\"92233720368547758\") || xs:long(\"-92233720368547758\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "92233720368547758-92233720368547758")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatlng2args-4                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(lower bound)                           
   * $arg2 = xs:long(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatlng2args4() {
    final XQuery query = new XQuery(
      "(xs:long(\"-92233720368547758\") || xs:long(\"-47175562203048468\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-92233720368547758-47175562203048468")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatlng2args-5                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(lower bound)                           
   * $arg2 = xs:long(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatlng2args5() {
    final XQuery query = new XQuery(
      "(xs:long(\"-92233720368547758\") || xs:long(\"92233720368547758\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-9223372036854775892233720368547758")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatnint2args-1                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(lower bound)                
   * $arg2 = xs:negativeInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatnint2args1() {
    final XQuery query = new XQuery(
      "(xs:negativeInteger(\"-999999999999999999\") || xs:negativeInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999-999999999999999999")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatnint2args-2                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(mid range)                  
   * $arg2 = xs:negativeInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatnint2args2() {
    final XQuery query = new XQuery(
      "(xs:negativeInteger(\"-297014075999096793\") || xs:negativeInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-297014075999096793-999999999999999999")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatnint2args-3                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(upper bound)                
   * $arg2 = xs:negativeInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatnint2args3() {
    final XQuery query = new XQuery(
      "(xs:negativeInteger(\"-1\") || xs:negativeInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1-999999999999999999")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatnint2args-4                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(lower bound)                
   * $arg2 = xs:negativeInteger(mid range)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatnint2args4() {
    final XQuery query = new XQuery(
      "(xs:negativeInteger(\"-999999999999999999\") || xs:negativeInteger(\"-297014075999096793\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999-297014075999096793")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatnint2args-5                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(lower bound)                
   * $arg2 = xs:negativeInteger(upper bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatnint2args5() {
    final XQuery query = new XQuery(
      "(xs:negativeInteger(\"-999999999999999999\") || xs:negativeInteger(\"-1\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999-1")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatnni2args-1                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(lower bound)             
   * $arg2 = xs:nonNegativeInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatnni2args1() {
    final XQuery query = new XQuery(
      "(xs:nonNegativeInteger(\"0\") || xs:nonNegativeInteger(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "00")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatnni2args-2                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(mid range)               
   * $arg2 = xs:nonNegativeInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatnni2args2() {
    final XQuery query = new XQuery(
      "(xs:nonNegativeInteger(\"303884545991464527\") || xs:nonNegativeInteger(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3038845459914645270")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatnni2args-3                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(upper bound)             
   * $arg2 = xs:nonNegativeInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatnni2args3() {
    final XQuery query = new XQuery(
      "(xs:nonNegativeInteger(\"999999999999999999\") || xs:nonNegativeInteger(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9999999999999999990")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatnni2args-4                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(lower bound)             
   * $arg2 = xs:nonNegativeInteger(mid range)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatnni2args4() {
    final XQuery query = new XQuery(
      "(xs:nonNegativeInteger(\"0\") || xs:nonNegativeInteger(\"303884545991464527\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0303884545991464527")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatnni2args-5                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(lower bound)             
   * $arg2 = xs:nonNegativeInteger(upper bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatnni2args5() {
    final XQuery query = new XQuery(
      "(xs:nonNegativeInteger(\"0\") || xs:nonNegativeInteger(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0999999999999999999")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatnpi2args-1                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(lower bound)             
   * $arg2 = xs:nonPositiveInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatnpi2args1() {
    final XQuery query = new XQuery(
      "(xs:nonPositiveInteger(\"-999999999999999999\") || xs:nonPositiveInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999-999999999999999999")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatnpi2args-2                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(mid range)               
   * $arg2 = xs:nonPositiveInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatnpi2args2() {
    final XQuery query = new XQuery(
      "(xs:nonPositiveInteger(\"-475688437271870490\") || xs:nonPositiveInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-475688437271870490-999999999999999999")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatnpi2args-3                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(upper bound)             
   * $arg2 = xs:nonPositiveInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatnpi2args3() {
    final XQuery query = new XQuery(
      "(xs:nonPositiveInteger(\"0\") || xs:nonPositiveInteger(\"-999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0-999999999999999999")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatnpi2args-4                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(lower bound)             
   * $arg2 = xs:nonPositiveInteger(mid range)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatnpi2args4() {
    final XQuery query = new XQuery(
      "(xs:nonPositiveInteger(\"-999999999999999999\") || xs:nonPositiveInteger(\"-475688437271870490\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-999999999999999999-475688437271870490")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatnpi2args-5                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(lower bound)             
   * $arg2 = xs:nonPositiveInteger(upper bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatnpi2args5() {
    final XQuery query = new XQuery(
      "(xs:nonPositiveInteger(\"-999999999999999999\") || xs:nonPositiveInteger(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-9999999999999999990")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatpint2args-1                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(lower bound)                
   * $arg2 = xs:positiveInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatpint2args1() {
    final XQuery query = new XQuery(
      "(xs:positiveInteger(\"1\") || xs:positiveInteger(\"1\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "11")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatpint2args-2                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(mid range)                  
   * $arg2 = xs:positiveInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatpint2args2() {
    final XQuery query = new XQuery(
      "(xs:positiveInteger(\"52704602390610033\") || xs:positiveInteger(\"1\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "527046023906100331")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatpint2args-3                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(upper bound)                
   * $arg2 = xs:positiveInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatpint2args3() {
    final XQuery query = new XQuery(
      "(xs:positiveInteger(\"999999999999999999\") || xs:positiveInteger(\"1\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9999999999999999991")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatpint2args-4                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(lower bound)                
   * $arg2 = xs:positiveInteger(mid range)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatpint2args4() {
    final XQuery query = new XQuery(
      "(xs:positiveInteger(\"1\") || xs:positiveInteger(\"52704602390610033\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "152704602390610033")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatpint2args-5                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(lower bound)                
   * $arg2 = xs:positiveInteger(upper bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatpint2args5() {
    final XQuery query = new XQuery(
      "(xs:positiveInteger(\"1\") || xs:positiveInteger(\"999999999999999999\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1999999999999999999")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatsht2args-1                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(lower bound)                          
   * $arg2 = xs:short(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatsht2args1() {
    final XQuery query = new XQuery(
      "(xs:short(\"-32768\") || xs:short(\"-32768\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-32768-32768")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatsht2args-2                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(mid range)                            
   * $arg2 = xs:short(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatsht2args2() {
    final XQuery query = new XQuery(
      "(xs:short(\"-5324\") || xs:short(\"-32768\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-5324-32768")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatsht2args-3                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(upper bound)                          
   * $arg2 = xs:short(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatsht2args3() {
    final XQuery query = new XQuery(
      "(xs:short(\"32767\") || xs:short(\"-32768\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "32767-32768")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatsht2args-4                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(lower bound)                          
   * $arg2 = xs:short(mid range)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatsht2args4() {
    final XQuery query = new XQuery(
      "(xs:short(\"-32768\") || xs:short(\"-5324\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-32768-5324")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatsht2args-5                                  
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(lower bound)                          
   * $arg2 = xs:short(upper bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatsht2args5() {
    final XQuery query = new XQuery(
      "(xs:short(\"-32768\") || xs:short(\"32767\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-3276832767")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatulng2args-1                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(lower bound)                   
   * $arg2 = xs:unsignedLong(lower bound)                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatulng2args1() {
    final XQuery query = new XQuery(
      "(xs:unsignedLong(\"0\") || xs:unsignedLong(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "00")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatulng2args-2                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(mid range)                     
   * $arg2 = xs:unsignedLong(lower bound)                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatulng2args2() {
    final XQuery query = new XQuery(
      "(xs:unsignedLong(\"130747108607674654\") || xs:unsignedLong(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1307471086076746540")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatulng2args-3                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(upper bound)                   
   * $arg2 = xs:unsignedLong(lower bound)                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatulng2args3() {
    final XQuery query = new XQuery(
      "(xs:unsignedLong(\"184467440737095516\") || xs:unsignedLong(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1844674407370955160")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatulng2args-4                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(lower bound)                   
   * $arg2 = xs:unsignedLong(mid range)                     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatulng2args4() {
    final XQuery query = new XQuery(
      "(xs:unsignedLong(\"0\") || xs:unsignedLong(\"130747108607674654\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0130747108607674654")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatulng2args-5                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(lower bound)                   
   * $arg2 = xs:unsignedLong(upper bound)                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatulng2args5() {
    final XQuery query = new XQuery(
      "(xs:unsignedLong(\"0\") || xs:unsignedLong(\"184467440737095516\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0184467440737095516")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatusht2args-1                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(lower bound)                  
   * $arg2 = xs:unsignedShort(lower bound)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatusht2args1() {
    final XQuery query = new XQuery(
      "(xs:unsignedShort(\"0\") || xs:unsignedShort(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "00")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatusht2args-2                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(mid range)                    
   * $arg2 = xs:unsignedShort(lower bound)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatusht2args2() {
    final XQuery query = new XQuery(
      "(xs:unsignedShort(\"44633\") || xs:unsignedShort(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "446330")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatusht2args-3                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(upper bound)                  
   * $arg2 = xs:unsignedShort(lower bound)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatusht2args3() {
    final XQuery query = new XQuery(
      "(xs:unsignedShort(\"65535\") || xs:unsignedShort(\"0\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "655350")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatusht2args-4                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(lower bound)                  
   * $arg2 = xs:unsignedShort(mid range)                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatusht2args4() {
    final XQuery query = new XQuery(
      "(xs:unsignedShort(\"0\") || xs:unsignedShort(\"44633\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "044633")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: concatusht2args-5                                 
   * Written By: Carmelo Montanez                            
   * Date: Wed Dec 15 15:41:48 GMT-05:00 2004                
   * Purpose: Evaluates The "concat" function               
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(lower bound)                  
   * $arg2 = xs:unsignedShort(upper bound)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opConcatusht2args5() {
    final XQuery query = new XQuery(
      "(xs:unsignedShort(\"0\") || xs:unsignedShort(\"65535\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "065535")
    );
  }
}
