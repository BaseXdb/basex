package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the hexBinary-equal() function.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpHexBinaryEqual extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-HexBinaryEQ-1                                 
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Compare two hexBinary values.                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kHexBinaryEQ1() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"FF\") eq xs:hexBinary(\"ff\")",
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
   *  Test: K-HexBinaryEQ-2                                 
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Compare two hexBinary values.                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kHexBinaryEQ2() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"FF\") ne xs:hexBinary(\"aa\")",
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
   *  Test: K-HexBinaryEQ-3                                 
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Compare two hexBinary values.                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kHexBinaryEQ3() {
    final XQuery query = new XQuery(
      "not(xs:hexBinary(\"FF\") eq xs:hexBinary(\"aa\"))",
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
   *  Test: K-HexBinaryEQ-4                                 
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Complex comparison test of wrapped hexBinary/base64Binary constructor functions. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kHexBinaryEQ4() {
    final XQuery query = new XQuery(
      "xs:hexBinary(xs:base64Binary(xs:hexBinary(\"03\"))) eq xs:hexBinary(\"03\")",
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
   *  Test: K-HexBinaryEQ-5                                 
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Complex comparison test of wrapped hexBinary/base64Binary constructor functions. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kHexBinaryEQ5() {
    final XQuery query = new XQuery(
      "xs:hexBinary(xs:base64Binary(xs:hexBinary(\"03\"))) ne xs:hexBinary(\"13\")",
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
   *  test equality of xs:hexBinary .
   */
  @org.junit.Test
  public void cbclHexBinaryEqual001() {
    final XQuery query = new XQuery(
      "declare function local:hexBinary-value($arg as xs:boolean) as xs:hexBinary { if ($arg) then xs:hexBinary('68656c6c6f') else xs:hexBinary('676f6f64627965') }; local:hexBinary-value(true()) eq local:hexBinary-value(false())",
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
   *  test equality of xs:hexBinary .
   */
  @org.junit.Test
  public void cbclHexBinaryEqual002() {
    final XQuery query = new XQuery(
      "declare function local:hexBinary-value($arg as xs:boolean) as xs:hexBinary { if ($arg) then xs:hexBinary('68656c6c6f') else xs:hexBinary('676f6f64627965') }; not(local:hexBinary-value(true()) eq local:hexBinary-value(false()))",
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
   *  test equality of xs:hexBinary .
   */
  @org.junit.Test
  public void cbclHexBinaryEqual003() {
    final XQuery query = new XQuery(
      "declare function local:hexBinary-value($arg as xs:boolean) as xs:hexBinary { if ($arg) then xs:hexBinary('68656c6c6f') else xs:hexBinary('676f6f64627965') }; not(local:hexBinary-value(true()) ne local:hexBinary-value(false()))",
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
   * Test: hexBinary-1                                       
   * Description: Simple Binary hex operation as part of a   
   * logical expression.  Use "and" and "eq" operators.      
   * .
   */
  @org.junit.Test
  public void hexBinary1() {
    final XQuery query = new XQuery(
      "(xs:hexBinary(\"786174616d61616772\") eq xs:hexBinary(\"767479716c6a647663\")) and (xs:hexBinary(\"786174616d61616772\") eq xs:hexBinary(\"767479716c6a647663\"))",
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
   * Test: hexBinary-10                                       
   * Description: Simple Binary hex opeartion as part of argument to fn:not function. 
   * .
   */
  @org.junit.Test
  public void hexBinary10() {
    final XQuery query = new XQuery(
      "fn:not((xs:hexBinary(\"786174616d61616772\") ne xs:hexBinary(\"786174616d61616772\")))",
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
   * Test: hexBinary-11                                       
   * Description: Simple Binary hex operation as part of a   
   * logical expression.  Use "and" and "ne" operators with "fn:true" function.      
   * .
   */
  @org.junit.Test
  public void hexBinary11() {
    final XQuery query = new XQuery(
      "(xs:hexBinary(\"786174616d61616772\") ne xs:hexBinary(\"767479716c6a647663\")) and fn:true()",
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
   * Test: hexBinary-12                                       
   * Description: Simple Binary hex operation as part of a   
   * logical expression.  Use "and" and "ne" operators with "fn:false" function.      
   * .
   */
  @org.junit.Test
  public void hexBinary12() {
    final XQuery query = new XQuery(
      "(xs:hexBinary(\"786174616d61616772\") ne xs:hexBinary(\"767479716c6a647663\")) and fn:false()",
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
   * Test: hexBinary-13                                      
   * Description: Simple Binary hex operation as part of a   
   * logical expression.  Use "or" and "ne" operators and "fn:true" function.      
   * .
   */
  @org.junit.Test
  public void hexBinary13() {
    final XQuery query = new XQuery(
      "(xs:hexBinary(\"786174616d61616772\") ne xs:hexBinary(\"767479716c6a647663\")) or fn:true()",
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
   * Test: hexBinary-14                                     
   * Description: Simple Binary hex operation as part of a   
   * logical expression.  Use "or" and "ne" operators and "fn:false" function.      
   * .
   */
  @org.junit.Test
  public void hexBinary14() {
    final XQuery query = new XQuery(
      "(xs:hexBinary(\"786174616d61616772\") ne xs:hexBinary(\"767479716c6a647663\")) or fn:false()",
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
   * Test: hexBinary-2                                       
   * Description: Simple Binary hex operation as part of a   
   * logical expression.  Use "and" and "eq" operators.      
   * .
   */
  @org.junit.Test
  public void hexBinary2() {
    final XQuery query = new XQuery(
      "(xs:hexBinary(\"786174616d61616772\") eq xs:hexBinary(\"767479716c6a647663\")) or (xs:hexBinary(\"786174616d61616772\") eq xs:hexBinary(\"767479716c6a647663\"))",
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
   * Test: hexBinary-3                                       
   * Description: Simple Binary hex opeartion as part of argument to fn:not function. 
   * .
   */
  @org.junit.Test
  public void hexBinary3() {
    final XQuery query = new XQuery(
      "fn:not((xs:hexBinary(\"786174616d61616772\") eq xs:hexBinary(\"786174616d61616772\")))",
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
   * Test: hexBinary-4                                       
   * Description: Simple Binary hex operation as part of a   
   * logical expression.  Use "and" and "eq" operators with "fn:true" function.      
   * .
   */
  @org.junit.Test
  public void hexBinary4() {
    final XQuery query = new XQuery(
      "(xs:hexBinary(\"786174616d61616772\") eq xs:hexBinary(\"767479716c6a647663\")) and fn:true()",
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
   * Test: hexBinary-5                                       
   * Description: Simple Binary hex operation as part of a   
   * logical expression.  Use "and" and "eq" operators with "fn:false" function.      
   * .
   */
  @org.junit.Test
  public void hexBinary5() {
    final XQuery query = new XQuery(
      "(xs:hexBinary(\"786174616d61616772\") eq xs:hexBinary(\"767479716c6a647663\")) and fn:false()",
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
   * Test: hexBinary-6                                       
   * Description: Simple Binary hex operation as part of a   
   * logical expression.  Use "or" and "eq" operators and "fn:true" function.      
   * .
   */
  @org.junit.Test
  public void hexBinary6() {
    final XQuery query = new XQuery(
      "(xs:hexBinary(\"786174616d61616772\") eq xs:hexBinary(\"767479716c6a647663\")) or fn:true()",
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
   * Test: hexBinary-7                                      
   * Description: Simple Binary hex operation as part of a   
   * logical expression.  Use "or" and "eq" operators and "fn:false" function.      
   * .
   */
  @org.junit.Test
  public void hexBinary7() {
    final XQuery query = new XQuery(
      "(xs:hexBinary(\"786174616d61616772\") eq xs:hexBinary(\"767479716c6a647663\")) or fn:false()",
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
   * Test: hexBinary-8                                       
   * Description: Simple Binary hex operation as part of a   
   * logical expression.  Use "and" and "ne" operators.      
   * .
   */
  @org.junit.Test
  public void hexBinary8() {
    final XQuery query = new XQuery(
      "(xs:hexBinary(\"786174616d61616772\") ne xs:hexBinary(\"767479716c6a647663\")) and (xs:hexBinary(\"786174616d61616772\") ne xs:hexBinary(\"767479716c6a647663\"))",
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
   * Test: hexBinary-9                                       
   * Description: Simple Binary hex operation as part of a   
   * logical expression.  Use "and" and "eq" operators.      
   * .
   */
  @org.junit.Test
  public void hexBinary9() {
    final XQuery query = new XQuery(
      "(xs:hexBinary(\"786174616d61616772\") ne xs:hexBinary(\"767479716c6a647663\")) or (xs:hexBinary(\"786174616d61616772\") ne xs:hexBinary(\"767479716c6a647663\"))",
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
   * Test: op-hexBinary-equal2args-1                         
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:18 GMT-05:00 2004                
   * Purpose: Evaluates The "op:hexBinary-equal" operator   
   *  with the arguments set as follows:                    
   * $value1 = xs:hexBinary(lower bound)                    
   * $value2 = xs:hexBinary(lower bound)                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opHexBinaryEqual2args1() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"767479716c6a647663\") eq xs:hexBinary(\"767479716c6a647663\")",
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
   * Test: op-hexBinary-equal2args-10                        
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:18 GMT-05:00 2004                
   * Purpose: Evaluates The "op:hexBinary-equal" operator   
   *  with the arguments set as follows:                    
   * $value1 = xs:hexBinary(lower bound)                    
   * $value2 = xs:hexBinary(upper bound)                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opHexBinaryEqual2args10() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"767479716c6a647663\") ne xs:hexBinary(\"786174616d61616772\")",
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
   * Test: op-hexBinary-equal2args-2                         
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:18 GMT-05:00 2004                
   * Purpose: Evaluates The "op:hexBinary-equal" operator   
   *  with the arguments set as follows:                    
   * $value1 = xs:hexBinary(mid range)                      
   * $value2 = xs:hexBinary(lower bound)                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opHexBinaryEqual2args2() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"6e7875626264756366\") eq xs:hexBinary(\"767479716c6a647663\")",
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
   * Test: op-hexBinary-equal2args-3                         
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:18 GMT-05:00 2004                
   * Purpose: Evaluates The "op:hexBinary-equal" operator   
   *  with the arguments set as follows:                    
   * $value1 = xs:hexBinary(upper bound)                    
   * $value2 = xs:hexBinary(lower bound)                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opHexBinaryEqual2args3() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"786174616d61616772\") eq xs:hexBinary(\"767479716c6a647663\")",
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
   * Test: op-hexBinary-equal2args-4                         
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:18 GMT-05:00 2004                
   * Purpose: Evaluates The "op:hexBinary-equal" operator   
   *  with the arguments set as follows:                    
   * $value1 = xs:hexBinary(lower bound)                    
   * $value2 = xs:hexBinary(mid range)                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opHexBinaryEqual2args4() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"767479716c6a647663\") eq xs:hexBinary(\"6e7875626264756366\")",
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
   * Test: op-hexBinary-equal2args-5                         
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:18 GMT-05:00 2004                
   * Purpose: Evaluates The "op:hexBinary-equal" operator   
   *  with the arguments set as follows:                    
   * $value1 = xs:hexBinary(lower bound)                    
   * $value2 = xs:hexBinary(upper bound)                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opHexBinaryEqual2args5() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"767479716c6a647663\") eq xs:hexBinary(\"786174616d61616772\")",
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
   * Test: op-hexBinary-equal2args-6                         
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:18 GMT-05:00 2004                
   * Purpose: Evaluates The "op:hexBinary-equal" operator   
   *  with the arguments set as follows:                    
   * $value1 = xs:hexBinary(lower bound)                    
   * $value2 = xs:hexBinary(lower bound)                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opHexBinaryEqual2args6() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"767479716c6a647663\") ne xs:hexBinary(\"767479716c6a647663\")",
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
   * Test: op-hexBinary-equal2args-7                         
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:18 GMT-05:00 2004                
   * Purpose: Evaluates The "op:hexBinary-equal" operator   
   *  with the arguments set as follows:                    
   * $value1 = xs:hexBinary(mid range)                      
   * $value2 = xs:hexBinary(lower bound)                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opHexBinaryEqual2args7() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"6e7875626264756366\") ne xs:hexBinary(\"767479716c6a647663\")",
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
   * Test: op-hexBinary-equal2args-8                         
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:18 GMT-05:00 2004                
   * Purpose: Evaluates The "op:hexBinary-equal" operator   
   *  with the arguments set as follows:                    
   * $value1 = xs:hexBinary(upper bound)                    
   * $value2 = xs:hexBinary(lower bound)                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opHexBinaryEqual2args8() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"786174616d61616772\") ne xs:hexBinary(\"767479716c6a647663\")",
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
   * Test: op-hexBinary-equal2args-9                         
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:18 GMT-05:00 2004                
   * Purpose: Evaluates The "op:hexBinary-equal" operator   
   *  with the arguments set as follows:                    
   * $value1 = xs:hexBinary(lower bound)                    
   * $value2 = xs:hexBinary(mid range)                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opHexBinaryEqual2args9() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"767479716c6a647663\") ne xs:hexBinary(\"6e7875626264756366\")",
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
