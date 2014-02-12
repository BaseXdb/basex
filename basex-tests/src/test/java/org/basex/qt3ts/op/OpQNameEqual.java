package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the QName-equal() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpQNameEqual extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-QNameEQ-1                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `QName("example.com/", "p:ncname") eq QName("example.com/", "p:ncname")`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kQNameEQ1() {
    final XQuery query = new XQuery(
      "QName(\"example.com/\", \"p:ncname\") eq QName(\"example.com/\", \"p:ncname\")",
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
   *  Test: K-QNameEQ-10                                    
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Operator 'ge' is not available between values of type xs:QName. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kQNameEQ10() {
    final XQuery query = new XQuery(
      "QName(\"example.com/\", \"p:ncname\") ge QName(\"example.com/\", \"p:ncname\")",
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
   *  Test: K-QNameEQ-2                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `QName("example.com/", "p:ncname") eq QName("example.com/", "pdifferent:ncname")`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kQNameEQ2() {
    final XQuery query = new XQuery(
      "QName(\"example.com/\", \"p:ncname\") eq QName(\"example.com/\", \"pdifferent:ncname\")",
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
   *  Test: K-QNameEQ-3                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `QName("example.com/", "p:ncname") ne QName("example.com/Nope", "p:ncname")`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kQNameEQ3() {
    final XQuery query = new XQuery(
      "QName(\"example.com/\", \"p:ncname\") ne QName(\"example.com/Nope\", \"p:ncname\")",
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
   *  Test: K-QNameEQ-4                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `QName("example.com/", "p:ncname") ne QName("example.com/", "p:ncnameNope")`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kQNameEQ4() {
    final XQuery query = new XQuery(
      "QName(\"example.com/\", \"p:ncname\") ne QName(\"example.com/\", \"p:ncnameNope\")",
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
   *  Test: K-QNameEQ-5                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Operator 'lt' is not available between xs:QName and xs:integer. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kQNameEQ5() {
    final XQuery query = new XQuery(
      "QName(\"example.com/\", \"p:ncname\") lt 1",
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
   *  Test: K-QNameEQ-6                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Operator 'eq' is not available between xs:anyURI and xs:QName. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kQNameEQ6() {
    final XQuery query = new XQuery(
      "QName(\"example.com/\", \"p:ncname\") eq xs:anyURI(\"org\")",
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
   *  Test: K-QNameEQ-7                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Operator 'lt' is not available between values of type xs:QName. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kQNameEQ7() {
    final XQuery query = new XQuery(
      "QName(\"example.com/\", \"p:ncname\") lt QName(\"example.com/\", \"p:ncname\")",
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
   *  Test: K-QNameEQ-8                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Operator 'le' is not available between values of type xs:QName. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kQNameEQ8() {
    final XQuery query = new XQuery(
      "QName(\"example.com/\", \"p:ncname\") le QName(\"example.com/\", \"p:ncname\")",
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
   *  Test: K-QNameEQ-9                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Operator 'gt' is not available between values of type xs:QName. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kQNameEQ9() {
    final XQuery query = new XQuery(
      "QName(\"example.com/\", \"p:ncname\") gt QName(\"example.com/\", \"p:ncname\")",
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
   *  test equality of xs:QName .
   */
  @org.junit.Test
  public void cbclQNameEqual001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:QName-value($arg as xs:boolean) as xs:QName { if ($arg) then QName(\"example.com/\", \"p:ncname\") else QName(\"example.com/\", \"q:ncname\") };\n" +
      "        not(local:QName-value(true()) eq local:QName-value(false()))",
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
   *  test equality of xs:QName .
   */
  @org.junit.Test
  public void cbclQNameEqual002() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:QName-value($arg as xs:boolean) as xs:QName { if ($arg) then QName(\"example.com/\", \"p:ncname\") else QName(\"example.com/\", \"q:ncname\") };\n" +
      "        not(local:QName-value(true()) ne local:QName-value(false()))",
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
   *  Name: op-qName-equal-1 
   *  Description: Evaluation of op-QName-equal operator with two identical qName values. Uses the "eq" operator. 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual1() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", \"person\") eq fn:QName(\"http://www.example.com/example\", \"person\")",
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
   *  Name: op-qName-equal-10 
   *  Description: Evaluation of op-QName-equal operator with two same namespace uri, same local part, different prefix. Uses the "ne" operator. 
   *  Should ignore the prefix 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual10() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", \"px1:person\") ne fn:QName(\"http://www.example.com/example\",\"px2:person\")",
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
   *  Name: op-qName-equal-11 
   *  Description: Evaluation of op-QName-equal operator with two same namespace uri, different local part, different prefix. Uses the "eq" operator. 
   *  Should ignore the prefix 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual11() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", \"px1:person1\") eq fn:QName(\"http://www.example.com/example\",\"px2:person2\")",
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
   *  Name: op-qName-equal-12 
   *  Description: Evaluation of op-QName-equal operator with two same namespace uri, different local part, different prefix. Uses the "ne" operator. 
   *  Should ignore the prefix 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual12() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", \"px1:person1\") ne fn:QName(\"http://www.example.com/example\",\"px2:person2\")",
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
   *  Name: op-qName-equal-13 
   *  Description: Evaluation of op-QName-equal operator with two different namespace uri, different local part, different prefix. Uses the "eq" operator. 
   *  Should ignore the prefix 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual13() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example1\", \"px1:person1\") eq fn:QName(\"http://www.example.com/example2\",\"px2:person2\")",
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
   *  Name: op-qName-equal-14 
   *  Description: Evaluation of op-QName-equal operator with two different namespace uri, different local part, different prefix. Uses the "ne" operator. 
   *  Should ignore the prefix 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual14() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example1\", \"px1:person1\") ne fn:QName(\"http://www.example.com/example2\",\"px2:person2\")",
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
   *  Name: op-qName-equal-15 
   *  Description: Evaluation of op-QName-equal operator with first namespace uri set to the empty string, same local part and no prefix. Uses the "eq" operator. 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual15() {
    final XQuery query = new XQuery(
      "fn:QName(\"\", \"person\") eq fn:QName(\"http://www.example.com/example\",\"person\")",
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
   *  Name: op-qName-equal-16 
   *  Description: Evaluation of op-QName-equal operator with first namespace uri set to the empty string, same local part and no prefix. Uses the "ne" operator. 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual16() {
    final XQuery query = new XQuery(
      "fn:QName(\"\", \"person\") ne fn:QName(\"http://www.example.com/example\",\"person\")",
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
   *  Name: op-qName-equal-17 
   *  Description: Evaluation of op-QName-equal operator with second namespace uri set to the empty string, same local part and no prefix. Uses the "eq" operator. 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual17() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", \"person\") eq fn:QName(\"\",\"person\")",
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
   *  Name: op-qName-equal-18 
   *  Description: Evaluation of op-QName-equal operator with second namespace uri set to the empty string, same local part and no prefix. Uses the "ne" operator. 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual18() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", \"person\") ne fn:QName(\"\",\"person\")",
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
   *  Name: op-qName-equal-19 
   *  Description: Evaluation of op-QName-equal operator with both namespace uri set to the empty string, same local part and no prefix. Uses the "eq" operator. 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual19() {
    final XQuery query = new XQuery(
      "fn:QName(\"\", \"person\") eq fn:QName(\"\",\"person\")",
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
   *  Name: op-qName-equal-2 
   *  Description: Evaluation of op-QName-equal operator with two identical qName values. Uses the "ne" operator. 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual2() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", \"person\") ne fn:QName(\"http://www.example.com/example\", \"person\")",
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
   *  Name: op-qName-equal-20 
   *  Description: Evaluation of op-QName-equal operator with both namespace uri set to the empty string, same local part and no prefix. Uses the "ne" operator. 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual20() {
    final XQuery query = new XQuery(
      "fn:QName(\"\", \"person\") ne fn:QName(\"\",\"person\")",
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
   *  Name: op-qName-equal-21 
   *  Description: Evaluation of op-QName-equal operator as an argument to the fn:not function.  Uses "eq" operator 
   *  Should ignore the prefix 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual21() {
    final XQuery query = new XQuery(
      "fn:not(fn:QName(\"http://www.example.com/example\", \"px1:person1\") eq fn:QName(\"http://www.example.com/example\",\"px2:person2\"))",
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
   *  Name: op-qName-equal-22 
   *  Description: Evaluation of op-QName-equal operator as an argument to the fn:not function.  Uses "ne" operator 
   *  Should ignore the prefix 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual22() {
    final XQuery query = new XQuery(
      "fn:not(fn:QName(\"http://www.example.com/example\", \"px1:person1\") ne fn:QName(\"http://www.example.com/example\",\"px2:person2\"))",
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
   *  Name: op-qName-equal-23 
   *  Description: Evaluation of op-QName-equal operator as part of a boolean expression.  Uses "eq"  and "and" operators 
   *  Should ignore the prefix 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual23() {
    final XQuery query = new XQuery(
      "(fn:QName(\"http://www.example.com/example\", \"px:person\") eq fn:QName(\"http://www.example.com/example\",\"px:person\")) and fn:true()",
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
   *  Name: op-qName-equal-24 
   *  Description: Evaluation of op-QName-equal operator as part of a boolean expression.  Uses "ne"  and "and" operators 
   *  Should ignore the prefix 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual24() {
    final XQuery query = new XQuery(
      "(fn:QName(\"http://www.example.com/example\", \"px:person\") ne fn:QName(\"http://www.example.com/example\",\"px:person\")) and fn:true()",
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
   *  Name: op-qName-equal-3 
   *  Description: Evaluation of op-QName-equal operator with two two different qName values (different namespace uri values, same local part). Uses the "eq" operator. 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual3() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example1\", \"person\") eq fn:QName(\"http://www.example.com/example2\", \"person\")",
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
   *  Name: op-qName-equal-4 
   *  Description: Evaluation of op-QName-equal operator with two two different qName values. Uses the "ne" operator. 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual4() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example1\", \"person\") ne fn:QName(\"http://www.example.com/example2\", \"person\")",
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
   *  Name: op-qName-equal-5 
   *  Description: Evaluation of op-QName-equal operator with two two different qName values (same namespace uri values, different local part). Uses the "eq" operator. 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual5() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", \"person1\") eq fn:QName(\"http://www.example.com/example\",\"person2\")",
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
   *  Name: op-qName-equal-6 
   *  Description: Evaluation of op-QName-equal operator with two two different qName values (same namespace uri, different local part). Uses the "ne" operator. 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual6() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", \"person1\") ne fn:QName(\"http://www.example.com/example\",\"person2\")",
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
   *  Name: op-qName-equal-7 
   *  Description: Evaluation of op-QName-equal operator with two same namespace uri, same local part, same prefix. Uses the "eq" operator. 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual7() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", \"px1:person\") eq fn:QName(\"http://www.example.com/example\",\"px1:person\")",
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
   *  Name: op-qName-equal-8 
   *  Description: Evaluation of op-QName-equal operator with two same namespace uri, same local part, same prefix. Uses the "ne" operator. 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual8() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", \"px1:person\") ne fn:QName(\"http://www.example.com/example\",\"px1:person\")",
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
   *  Name: op-qName-equal-9 
   *  Description: Evaluation of op-QName-equal operator with two same namespace uri, same local part, different prefix. Uses the "eq" operator. 
   *  Should ignore the prefix 
   *  .
   */
  @org.junit.Test
  public void opQnameEqual9() {
    final XQuery query = new XQuery(
      "fn:QName(\"http://www.example.com/example\", \"px1:person\") eq fn:QName(\"http://www.example.com/example\",\"px2:person\")",
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
   *  name : value-comparison-1 
   *  description : Evaluation of xs:string compare to xs:anyURI.
   *  Uses "eq" operator. 
   *  .
   */
  @org.junit.Test
  public void valueComparison1() {
    final XQuery query = new XQuery(
      "xs:string(\"example.org/\") eq xs:anyURI(\"example.org/\")",
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
   *  name : value-comparison-2 
   *  description : Evaluation of xs:string compare to xs:anyURI.
   *  Uses "ne" operator. 
   *  .
   */
  @org.junit.Test
  public void valueComparison2() {
    final XQuery query = new XQuery(
      "xs:string(\"example.org/\") ne xs:anyURI(\"example.org/\")",
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
