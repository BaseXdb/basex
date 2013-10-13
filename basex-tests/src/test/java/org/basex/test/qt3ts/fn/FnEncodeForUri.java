package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the encode-for-uri() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnEncodeForUri extends QT3TestSet {

  /**
   *  A test whose essence is: `encode-for-uri()`. .
   */
  @org.junit.Test
  public void kEncodeURIfunc1() {
    final XQuery query = new XQuery(
      "encode-for-uri()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `encode-for-uri("http://example.com/", "wrong param")`. .
   */
  @org.junit.Test
  public void kEncodeURIfunc2() {
    final XQuery query = new XQuery(
      "encode-for-uri(\"http://example.com/\", \"wrong param\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `encode-for-uri(()) eq ""`. .
   */
  @org.junit.Test
  public void kEncodeURIfunc3() {
    final XQuery query = new XQuery(
      "encode-for-uri(()) eq \"\"",
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
   *  Combine fn:concat and fn:encode-for-uri. .
   */
  @org.junit.Test
  public void kEncodeURIfunc4() {
    final XQuery query = new XQuery(
      "concat(\"http://www.example.com/\", encode-for-uri(\"~bébé\")) eq \"http://www.example.com/~b%C3%A9b%C3%A9\"",
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
   *  Combine fn:concat and fn:encode-for-uri. .
   */
  @org.junit.Test
  public void kEncodeURIfunc5() {
    final XQuery query = new XQuery(
      "concat(\"http://www.example.com/\", encode-for-uri(\"100% organic\")) eq \"http://www.example.com/100%25%20organic\"",
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
   *  Invoke fn:normalize-space() on the return value of fn:encode-for-uri(). .
   */
  @org.junit.Test
  public void kEncodeURIfunc6() {
    final XQuery query = new XQuery(
      "normalize-space(encode-for-uri((\"some string\", current-time())[1] treat as xs:string))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "some%20string")
    );
  }

  /**
   *  Evaluation of fn-encode-for-uri function with argument thast ha nothing to encode. .
   */
  @org.junit.Test
  public void fnEncodeForUri1() {
    final XQuery query = new XQuery(
      "(fn:encode-for-uri(\"example\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "example")
    );
  }

  /**
   *  Examines that fn-encode-for-uri function does escape the "(" symbol. .
   */
  @org.junit.Test
  public void fnEncodeForUri10() {
    final XQuery query = new XQuery(
      "(fn:encode-for-uri(\"examples(example\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "examples%28example")
    );
  }

  /**
   *  Examines that fn-encode-for-uri function does escape the ")" symbol. .
   */
  @org.junit.Test
  public void fnEncodeForUri11() {
    final XQuery query = new XQuery(
      "(fn:encode-for-uri(\"examples)example\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "examples%29example")
    );
  }

  /**
   *  Examines that fn-encode-for-uri function does not escape numbers. .
   */
  @org.junit.Test
  public void fnEncodeForUri12() {
    final XQuery query = new XQuery(
      "(fn:encode-for-uri(\"examples0123456789example\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "examples0123456789example")
    );
  }

  /**
   *  Examines that fn-encode-for-uri function escapes the space. .
   */
  @org.junit.Test
  public void fnEncodeForUri13() {
    final XQuery query = new XQuery(
      "(fn:encode-for-uri(\"examples example\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "examples%20example")
    );
  }

  /**
   *  Examines that fn-encode-for-uri function escapes the "/" character. .
   */
  @org.junit.Test
  public void fnEncodeForUri14() {
    final XQuery query = new XQuery(
      "(fn:encode-for-uri(\"examples/example\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "examples%2Fexample")
    );
  }

  /**
   *  Examines that fn-encode-for-uri function escapes the ":" character. .
   */
  @org.junit.Test
  public void fnEncodeForUri15() {
    final XQuery query = new XQuery(
      "(fn:encode-for-uri(\"http:examples\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http%3Aexamples")
    );
  }

  /**
   *  Examines that fn-encode-for-uri function escapes the "%" character. .
   */
  @org.junit.Test
  public void fnEncodeForUri16() {
    final XQuery query = new XQuery(
      "(fn:encode-for-uri(\"http%20examples\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http%2520examples")
    );
  }

  /**
   *  Examines that fn-encode-for-uri function does escape the "#" symbol. .
   */
  @org.junit.Test
  public void fnEncodeForUri2() {
    final XQuery query = new XQuery(
      "(fn:encode-for-uri(\"examples#example\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "examples%23example")
    );
  }

  /**
   *  Examines that fn-encode-for-uri function does not escape the "-" symbol. .
   */
  @org.junit.Test
  public void fnEncodeForUri3() {
    final XQuery query = new XQuery(
      "(fn:encode-for-uri(\"examples-example\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "examples-example")
    );
  }

  /**
   *  Examines that fn-encode-for-uri function does not escape the "_" symbol. .
   */
  @org.junit.Test
  public void fnEncodeForUri4() {
    final XQuery query = new XQuery(
      "(fn:encode-for-uri(\"examples_example\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "examples_example")
    );
  }

  /**
   *  Examines that fn-encode-for-uri function does not escape the "." symbol. .
   */
  @org.junit.Test
  public void fnEncodeForUri5() {
    final XQuery query = new XQuery(
      "(fn:encode-for-uri(\"examples.example\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "examples.example")
    );
  }

  /**
   *  Examines that fn-encode-for-uri function does escapes the "!" symbol. .
   */
  @org.junit.Test
  public void fnEncodeForUri6() {
    final XQuery query = new XQuery(
      "(fn:encode-for-uri(\"examples!example\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "examples%21example")
    );
  }

  /**
   *  Examines that fn-encode-for-uri function does not escape the "~" symbol. .
   */
  @org.junit.Test
  public void fnEncodeForUri7() {
    final XQuery query = new XQuery(
      "(fn:encode-for-uri(\"examples~example\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "examples~example")
    );
  }

  /**
   *  Examines that fn-encode-for-uri function does escape the "*" symbol. .
   */
  @org.junit.Test
  public void fnEncodeForUri8() {
    final XQuery query = new XQuery(
      "(fn:encode-for-uri(\"examples*example\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "examples%2Aexample")
    );
  }

  /**
   *  Examines that fn-encode-for-uri function does escape the "'" symbol. .
   */
  @org.junit.Test
  public void fnEncodeForUri9() {
    final XQuery query = new XQuery(
      "(fn:encode-for-uri(\"examples'example\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "examples%27example")
    );
  }

  /**
   * Test encode-for-uri from example defined in functions and operators specification .
   */
  @org.junit.Test
  public void fnEncodeForUri1args1() {
    final XQuery query = new XQuery(
      "fn:encode-for-uri (\"http://www.example.com/00/Weather/CA/Los%20Angeles#ocean\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http%3A%2F%2Fwww.example.com%2F00%2FWeather%2FCA%2FLos%2520Angeles%23ocean")
    );
  }

  /**
   * Test encode-for-uri from example defined in functions and operators specification .
   */
  @org.junit.Test
  public void fnEncodeForUri1args2() {
    final XQuery query = new XQuery(
      "encode-for-uri(\"~bébé\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "~b%C3%A9b%C3%A9")
    );
  }

  /**
   * Test encode-for-uri from example defined in functions and operators specification .
   */
  @org.junit.Test
  public void fnEncodeForUri1args3() {
    final XQuery query = new XQuery(
      "encode-for-uri(\"100% organic\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "100%25%20organic")
    );
  }

  /**
   * Test encode-for-uri with zero-length string argument .
   */
  @org.junit.Test
  public void fnEncodeForUri1args4() {
    final XQuery query = new XQuery(
      "encode-for-uri('')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
   * Test encode-for-uri with empty sequence argument .
   */
  @org.junit.Test
  public void fnEncodeForUri1args5() {
    final XQuery query = new XQuery(
      "encode-for-uri(())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
   * Test encode-for-uri with invalid argument type .
   */
  @org.junit.Test
  public void fnEncodeForUri1args6() {
    final XQuery query = new XQuery(
      "encode-for-uri(12)",
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
   * Test encode-for-uri with incorrect arity .
   */
  @org.junit.Test
  public void fnEncodeForUri1args7() {
    final XQuery query = new XQuery(
      "encode-for-uri('',())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }
}
