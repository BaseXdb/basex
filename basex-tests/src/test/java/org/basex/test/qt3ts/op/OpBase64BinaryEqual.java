package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the base64Binary-equal() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpBase64BinaryEqual extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-Base64BinaryEQ-1                              
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:base64Binary(xs:hexBinary("03")) eq xs:base64Binary(xs:hexBinary("03"))`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kBase64BinaryEQ1() {
    final XQuery query = new XQuery(
      "xs:base64Binary(xs:hexBinary(\"03\")) eq xs:base64Binary(xs:hexBinary(\"03\"))",
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
   *  Test: K-Base64BinaryEQ-2                              
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:base64Binary(xs:hexBinary("03")) ne xs:base64Binary(xs:hexBinary("13"))`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kBase64BinaryEQ2() {
    final XQuery query = new XQuery(
      "xs:base64Binary(xs:hexBinary(\"03\")) ne xs:base64Binary(xs:hexBinary(\"13\"))",
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
   *  Test: K-Base64BinaryEQ-3                              
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:hexBinary(xs:base64Binary("/w==")) eq xs:hexBinary("FF")`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kBase64BinaryEQ3() {
    final XQuery query = new XQuery(
      "xs:hexBinary(xs:base64Binary(\"/w==\")) eq xs:hexBinary(\"FF\")",
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
   * Test: base64BinaryEqual-1                                   
   * Description: Simple base 64 binary operation as part of a   
   * logical expression.  Use "and" and "eq" operators.          
   * .
   */
  @org.junit.Test
  public void base64BinaryEqual1() {
    final XQuery query = new XQuery(
      "(xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") eq xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\")) and (xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") eq xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\"))",
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
   * Test: base64BinaryEqual-10                                       
   * Description: Simple opeartion involving xs:base64Binary values as part of argument to "fn:not" function. 
   * .
   */
  @org.junit.Test
  public void base64BinaryEqual10() {
    final XQuery query = new XQuery(
      "fn:not(xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") ne xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\"))",
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
   * Test: base64BinaryEqual-11                                       
   * Description: Simple operation involving xs:base64Binary values as part of a   
   * logical expression.  Use "and" and "ne" operators with "fn:true" function.      
   * .
   */
  @org.junit.Test
  public void base64BinaryEqual11() {
    final XQuery query = new XQuery(
      "(xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") ne xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\")) and fn:true()",
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
   * Test: base64BinaryEqual-12                                       
   * Description: Simple operation involving base64Binary values as part of a   
   * logical expression.  Use "and" and "ne" operators with "fn:false" function.      
   * .
   */
  @org.junit.Test
  public void base64BinaryEqual12() {
    final XQuery query = new XQuery(
      "(xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") ne xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\")) and fn:false()",
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
   * Test: base64BinaryEqual-13                                   
   * Description: Simple operation involving xs:base64Binary values used as part of a   
   * logical expression.  Use "or" and "ne" operators and "fn:true" function.      
   * .
   */
  @org.junit.Test
  public void base64BinaryEqual13() {
    final XQuery query = new XQuery(
      "(xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") ne xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\")) or fn:true()",
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
   * Test: base64BinaryEqual-14                                     
   * Description: Simple operation involving xs:base64Binary values used as part of a   
   * logical expression.  Use "or" and "ne" operators and "fn:false" function.      
   * .
   */
  @org.junit.Test
  public void base64BinaryEqual14() {
    final XQuery query = new XQuery(
      "(xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") ne xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\")) or fn:false()",
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
   * Test: base64BinaryEqual-2                               
   * Description: Simple base 64 Binary operation as part of a   
   * logical expression.  Use "or" and "eq" operators.      
   * .
   */
  @org.junit.Test
  public void base64BinaryEqual2() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") eq xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\") or xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") eq xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\")",
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
   * Test: base54BinaryEqual-3                                       
   * Description: Simple base 64 binary operation as part of argument to fn:not function. 
   * .
   */
  @org.junit.Test
  public void base64BinaryEqual3() {
    final XQuery query = new XQuery(
      "fn:not(xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") eq xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\"))",
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
   * Test: base64BinaryEqual-4                                       
   * Description: Simple operation with xs:base64binary values as part of a   
   * logical expression.  Use "and" and "eq" operators with "fn:true" function.
   * .
   */
  @org.junit.Test
  public void base64BinaryEqual4() {
    final XQuery query = new XQuery(
      "(xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") eq xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\")) and fn:true()",
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
   * Test: base64BinaryEqual-6                                       
   * Description: Simple operation using xs:base64Binary values as part of a   
   * logical expression.  Use "or" and "eq" operators and "fn:true" function.      
   * .
   */
  @org.junit.Test
  public void base64BinaryEqual6() {
    final XQuery query = new XQuery(
      "(xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") eq xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\")) or fn:true()",
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
   * Test: base64BinaryEqual-7                                      
   * Description: Simple operation using xs:base64Binary values as part of a   
   * logical expression.  Use "or" and "eq" operators and "fn:false" function. 
   * .
   */
  @org.junit.Test
  public void base64BinaryEqual7() {
    final XQuery query = new XQuery(
      "(xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") eq xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\")) or fn:false()",
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
   * Test: base64BinaryEqual-8                               
   * Description: Simple operation involving xs:base64Binary values as part of a   
   * logical expression.  Use "and" and "ne" operators.      
   * .
   */
  @org.junit.Test
  public void base64BinaryEqual8() {
    final XQuery query = new XQuery(
      "(xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") eq xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\")) and (xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") eq xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\"))",
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
   * Test: base64BinaryEqual-9                                       
   * Description: Simple operation involving xs:base64Binary values as part of a   
   * logical expression.  Use "and" and "ne" operators.      
   * .
   */
  @org.junit.Test
  public void base64BinaryEqual9() {
    final XQuery query = new XQuery(
      "(xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") ne xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\")) or (xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") ne xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\"))",
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
   * Test: base64BinaryEqual-5                                       
   * Description: Simple operation involving xs:base64Binary values as part of a   
   * logical expression.  Use "and" and "eq" operators with "fn:false" function.  
   * .
   */
  @org.junit.Test
  public void base64binaryequal5() {
    final XQuery query = new XQuery(
      "(xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") eq xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\")) and fn:false()",
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
   *  Name: cbcl-base64Binary-equal-001 
   *  Description: test equality of xs:base64Binary 
   *  Author: Tim Mills 
   *  Date: 2008-05-14 
   * .
   */
  @org.junit.Test
  public void cbclBase64BinaryEqual001() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare function local:base64Binary-value($arg as xs:boolean) as xs:base64Binary { \n" +
      "      \tif ($arg) then xs:base64Binary('aGVsbG8=') else xs:base64Binary('Z29vZGJ5ZQ==') \n" +
      "      }; \n" +
      "      local:base64Binary-value(true()) eq local:base64Binary-value(false())",
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
   *  Name: cbcl-base64Binary-equal-002 
   *  Description: test equality of xs:base64Binary 
   *  Author: Tim Mills 
   *  Date: 2008-05-14 
   * .
   */
  @org.junit.Test
  public void cbclBase64BinaryEqual002() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:base64Binary-value($arg as xs:boolean) as xs:base64Binary { \n" +
      "      \t\tif ($arg) then xs:base64Binary('aGVsbG8=') else xs:base64Binary('Z29vZGJ5ZQ==') \n" +
      "      \t}; \n" +
      "      \tnot(local:base64Binary-value(true()) eq local:base64Binary-value(false()))",
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
   *  Name: cbcl-base64Binary-equal-003 
   *  Description: test equality of xs:base64Binary 
   *  Author: Tim Mills 
   *  Date: 2008-05-14 
   * .
   */
  @org.junit.Test
  public void cbclBase64BinaryEqual003() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:base64Binary-value($arg as xs:boolean) as xs:base64Binary { \n" +
      "      \t\tif ($arg) then xs:base64Binary('aGVsbG8=') else xs:base64Binary('Z29vZGJ5ZQ==') \n" +
      "      \t}; \n" +
      "      \tnot(local:base64Binary-value(true()) ne local:base64Binary-value(false()))",
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
   * Test: op-base64Binary-equal2args-1                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Mar 22 11:23:46 GMT-05:00 2005                
   * Purpose: Evaluates The "op:base64Binary-equal" operator
   *  with the arguments set as follows:                    
   * $value1 = xs:base64Binary(lower bound)                 
   * $value2 = xs:base64Binary(lower bound)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBase64BinaryEqual2args1() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") eq xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\")",
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
   * Test: op-base64Binary-equal2args-10                     
   * Written By: Carmelo Montanez                            
   * Date: Tue Mar 22 11:23:46 GMT-05:00 2005                
   * Purpose: Evaluates The "op:base64Binary-equal" operator
   *  with the arguments set as follows:                    
   * $value1 = xs:base64Binary(lower bound)                 
   * $value2 = xs:base64Binary(upper bound)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBase64BinaryEqual2args10() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") ne xs:base64Binary(\"cW9kanZzY3ZlaWthYXVreGxibm11dW91ZmllZGplbXZza2FqcGlwdWlxcG5xbHR4dmFjcWFjbGN1Z3BqYmVuZWhsdHhzeHZs\")",
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
   * Test: op-base64Binary-equal2args-2                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Mar 22 11:23:46 GMT-05:00 2005                
   * Purpose: Evaluates The "op:base64Binary-equal" operator
   *  with the arguments set as follows:                    
   * $value1 = xs:base64Binary(mid range)                   
   * $value2 = xs:base64Binary(lower bound)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBase64BinaryEqual2args2() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\") eq xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\")",
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
   * Test: op-base64Binary-equal2args-3                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Mar 22 11:23:46 GMT-05:00 2005                
   * Purpose: Evaluates The "op:base64Binary-equal" operator
   *  with the arguments set as follows:                    
   * $value1 = xs:base64Binary(upper bound)                 
   * $value2 = xs:base64Binary(lower bound)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBase64BinaryEqual2args3() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"cW9kanZzY3ZlaWthYXVreGxibm11dW91ZmllZGplbXZza2FqcGlwdWlxcG5xbHR4dmFjcWFjbGN1Z3BqYmVuZWhsdHhzeHZs\") eq xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\")",
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
   * Test: op-base64Binary-equal2args-4                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Mar 22 11:23:46 GMT-05:00 2005                
   * Purpose: Evaluates The "op:base64Binary-equal" operator
   *  with the arguments set as follows:                    
   * $value1 = xs:base64Binary(lower bound)                 
   * $value2 = xs:base64Binary(mid range)                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBase64BinaryEqual2args4() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") eq xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\")",
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
   * Test: op-base64Binary-equal2args-5                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Mar 22 11:23:46 GMT-05:00 2005                
   * Purpose: Evaluates The "op:base64Binary-equal" operator
   *  with the arguments set as follows:                    
   * $value1 = xs:base64Binary(lower bound)                 
   * $value2 = xs:base64Binary(upper bound)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBase64BinaryEqual2args5() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") eq xs:base64Binary(\"cW9kanZzY3ZlaWthYXVreGxibm11dW91ZmllZGplbXZza2FqcGlwdWlxcG5xbHR4dmFjcWFjbGN1Z3BqYmVuZWhsdHhzeHZs\")",
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
   * Test: op-base64Binary-equal2args-6                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Mar 22 11:23:46 GMT-05:00 2005                
   * Purpose: Evaluates The "op:base64Binary-equal" operator
   *  with the arguments set as follows:                    
   * $value1 = xs:base64Binary(lower bound)                 
   * $value2 = xs:base64Binary(lower bound)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBase64BinaryEqual2args6() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") ne xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\")",
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
   * Test: op-base64Binary-equal2args-7                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Mar 22 11:23:46 GMT-05:00 2005                
   * Purpose: Evaluates The "op:base64Binary-equal" operator
   *  with the arguments set as follows:                    
   * $value1 = xs:base64Binary(mid range)                   
   * $value2 = xs:base64Binary(lower bound)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBase64BinaryEqual2args7() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\") ne xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\")",
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
   * Test: op-base64Binary-equal2args-8                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Mar 22 11:23:46 GMT-05:00 2005                
   * Purpose: Evaluates The "op:base64Binary-equal" operator
   *  with the arguments set as follows:                    
   * $value1 = xs:base64Binary(upper bound)                 
   * $value2 = xs:base64Binary(lower bound)                 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBase64BinaryEqual2args8() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"cW9kanZzY3ZlaWthYXVreGxibm11dW91ZmllZGplbXZza2FqcGlwdWlxcG5xbHR4dmFjcWFjbGN1Z3BqYmVuZWhsdHhzeHZs\") ne xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\")",
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
   * Test: op-base64Binary-equal2args-9                      
   * Written By: Carmelo Montanez                            
   * Date: Tue Mar 22 11:23:46 GMT-05:00 2005                
   * Purpose: Evaluates The "op:base64Binary-equal" operator
   *  with the arguments set as follows:                    
   * $value1 = xs:base64Binary(lower bound)                 
   * $value2 = xs:base64Binary(mid range)                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opBase64BinaryEqual2args9() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"cmxjZ3R4c3JidnllcmVuZG91aWpsbXV5Z2NhamxpcmJkaWFhbmFob2VsYXVwZmJ1Z2dmanl2eHlzYmhheXFtZXR0anV2dG1q\") ne xs:base64Binary(\"d2J1bnB0Y3lucWtvYXdpb2xoZWNwZXlkdG90eHB3ZXJqcnliZXFubmJjZXBmbGx3aGN3cmNndG9xb2hvdHdlY2pzZ3h5bnlp\")",
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
