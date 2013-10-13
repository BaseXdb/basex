package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the resolve-QName() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnResolveQName extends QT3TestSet {

  /**
   *  A test whose essence is: `resolve-QName()`. .
   */
  @org.junit.Test
  public void kResolveQNameConstructFunc1() {
    final XQuery query = new XQuery(
      "resolve-QName()",
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
   *  A test whose essence is: `resolve-QName("wrongparam")`. .
   */
  @org.junit.Test
  public void kResolveQNameConstructFunc2() {
    final XQuery query = new XQuery(
      "resolve-QName(\"wrongparam\")",
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
   *  A test whose essence is: `resolve-QName("wrongparam", "takes a node", "wrong")`. .
   */
  @org.junit.Test
  public void kResolveQNameConstructFunc3() {
    final XQuery query = new XQuery(
      "resolve-QName(\"wrongparam\", \"takes a node\", \"wrong\")",
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
   *  Although the second argument contains a type error(because an element is expected), it is also valid to return the empty sequence because one must not evaluate all arguments. .
   */
  @org.junit.Test
  public void kResolveQNameConstructFunc4() {
    final XQuery query = new XQuery(
      "empty(resolve-QName((), \"a string\"))",
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
        assertBoolean(true)
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Tests fn:resolve-QName on an empty prefix an a node without a default namespace .
   */
  @org.junit.Test
  public void cbclFnResolveQname001() {
    final XQuery query = new XQuery(
      "resolve-QName(\"blah\",<foo:a xmlns:foo=\"http://test/\"/>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "blah")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the give qname is not lexically correct. .
   */
  @org.junit.Test
  public void fnResolveQname1() {
    final XQuery query = new XQuery(
      "fn:resolve-QName(\"aName::\", <anElement>Some content</anElement>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the given qname has no prefix and there is a namespace binding with prefix for the element. uses fn:namespace-uri-from-qname to get namespace uri. Element is given as direct element use fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnResolveQname10() {
    final XQuery query = new XQuery(
      "fn:count(fn:namespace-uri-from-QName(fn:resolve-QName(\"anElement\", <anElement xmlns:p1=\"http://example.com/examples\">Some content</anElement>)))",
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
   *  Evaluation of fn:resolve-qname function for which the given qname has a prefix used together with xs:string and there is a namespace binding with prefix for the element. uses fn:local-name-from-qname to get local part. Element is given as direct element .
   */
  @org.junit.Test
  public void fnResolveQname11() {
    final XQuery query = new XQuery(
      "fn:string(fn:local-name-from-QName(fn:resolve-QName(xs:string(\"p1:anElement\"), <anElement xmlns:p1=\"http://example.com/examples\">Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "anElement")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the given qname has a prefix used together with xs:string and there is a namespace binding with prefix for the element. uses fn:namespace-uri-from-qname to get namespace-uri part. Element is given as direct element .
   */
  @org.junit.Test
  public void fnResolveQname12() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-from-QName(fn:resolve-QName(xs:string(\"p1:anElement\"), <anElement xmlns:p1=\"http://example.com/examples\">Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.com/examples")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the given qname has a prefix used together with fn:concat and there is a namespace binding with prefix for the element. uses fn:local-name-from-qname to get local part. Element is given as direct element .
   */
  @org.junit.Test
  public void fnResolveQname13() {
    final XQuery query = new XQuery(
      "fn:string(fn:local-name-from-QName(fn:resolve-QName(fn:concat(\"p1:\",\"anElement\"), <anElement xmlns:p1=\"http://example.com/examples\">Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "anElement")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the given qname has a prefix used together with fn:concat and there is a namespace binding with prefix for the element. uses fn:namespace-uri-from-qname to get namespace-uri part. Element is given as direct element .
   */
  @org.junit.Test
  public void fnResolveQname14() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-from-QName(fn:resolve-QName(fn:concat(\"p1:\",\"anElement\"), <anElement xmlns:p1=\"http://example.com/examples\">Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.com/examples")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the given qname has a prefix used together with fn:string-join and there is a namespace binding with prefix for the element. uses fn:local-name-from-qname to get local part. Element is given as direct element .
   */
  @org.junit.Test
  public void fnResolveQname15() {
    final XQuery query = new XQuery(
      "fn:string(fn:local-name-from-QName(fn:resolve-QName(fn:string-join((\"p1:\",\"anElement\"),\"\"), <anElement xmlns:p1=\"http://example.com/examples\">Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "anElement")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the given qname has a prefix used together with fn:string-join and there is a namespace binding with prefix for the element. uses fn:namespace-uri-from-qname to get namespace-uri part. Element is given as direct element .
   */
  @org.junit.Test
  public void fnResolveQname16() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-from-QName(fn:resolve-QName(fn:string-join(('p1:','anElement'),''), <anElement xmlns:p1=\"http://example.com/examples\">Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.com/examples")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the given qname has a prefix used together with fn:substring-before and there is a namespace binding with prefix for the element. uses fn:local-name-from-qname to get local part. Element is given as direct element .
   */
  @org.junit.Test
  public void fnResolveQname17() {
    final XQuery query = new XQuery(
      "fn:string(fn:local-name-from-QName(fn:resolve-QName(fn:substring-before(\"p1:anElementabc\",\"abc\"), <anElement xmlns:p1=\"http://example.com/examples\">Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "anElement")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the given qname has a prefix used together with fn:substring-before and there is a namespace binding with prefix for the element. uses fn:namespace-uri-from-qname to get namespace-uri part. Element is given as direct element .
   */
  @org.junit.Test
  public void fnResolveQname18() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-from-QName(fn:resolve-QName(fn:substring-before(\"p1:anElementabc\",\"abc\"), <anElement xmlns:p1=\"http://example.com/examples\">Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.com/examples")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the given qname has a prefix used together with fn:substring-after and there is a namespace binding with prefix for the element. uses fn:local-name-from-qname to get local part. Element is given as direct element .
   */
  @org.junit.Test
  public void fnResolveQname19() {
    final XQuery query = new XQuery(
      "fn:string(fn:local-name-from-QName(fn:resolve-QName(fn:substring-after(\"abcp1:anElement\",\"abc\"), <anElement xmlns:p1=\"http://example.com/examples\">Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "anElement")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the give qname has a prefix but element does not have binding namespace. Element is give as a directly constructed element .
   */
  @org.junit.Test
  public void fnResolveQname2() {
    final XQuery query = new XQuery(
      "fn:resolve-QName(\"p1:anElement\", <anElement>Some content</anElement>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FONS0004")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the given qname has a prefix used together with fn:substring-after and there is a namespace binding with prefix for the element. uses fn:namespace-uri-from-qname to get namespace-uri part. Element is given as direct element .
   */
  @org.junit.Test
  public void fnResolveQname20() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-from-QName(fn:resolve-QName(fn:substring-after(\"abcp1:anElement\",\"abc\"), <anElement xmlns:p1=\"http://example.com/examples\">Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.com/examples")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the given qname has a prefix used together with fn:substring and there is a namespace binding with prefix for the element. uses fn:local-name-from-qname to get local part. Element is given as direct element .
   */
  @org.junit.Test
  public void fnResolveQname21() {
    final XQuery query = new XQuery(
      "fn:string(fn:local-name-from-QName(fn:resolve-QName(fn:substring(\"abcp1:anElement\",4), <anElement xmlns:p1=\"http://example.com/examples\">Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "anElement")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the given qname has a prefix used together with fn:substring and there is a namespace binding with prefix for the element. uses fn:namespace-uri-from-qname to get namespace-uri part. Element is given as direct element .
   */
  @org.junit.Test
  public void fnResolveQname22() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-from-QName(fn:resolve-QName(fn:substring(\"abcp1:anElement\",4), <anElement xmlns:p1=\"http://example.com/examples\">Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.com/examples")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the give qname is the empty sequence uses fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnResolveQname3() {
    final XQuery query = new XQuery(
      "fn:count(fn:resolve-QName((), <anElement>Some content</anElement>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the give qname has a prefix and there is a namespace binding for the element. uses fn:local-name-from-QName to get local part. Element is given as direct element .
   */
  @org.junit.Test
  public void fnResolveQname4() {
    final XQuery query = new XQuery(
      "fn:string(fn:local-name-from-QName(fn:resolve-QName(\"p1:name\", <anElement xmlns:p1=\"http://example.com/examples\">Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "name")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the give qname has a prefix and there is a namespace binding for the element. uses fn:namespace-uri-from-QName to get namespace part. Element is given as direct element .
   */
  @org.junit.Test
  public void fnResolveQname5() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-from-QName(fn:resolve-QName(\"p1:name\", <anElement xmlns:p1=\"http://example.com/examples\">Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.com/examples")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the give qname has a prefix and there is a namespace binding for the element. Element has multiple namespapce bindings. Uses fn:local-name-from-QName to get local part. Element is given as direct element .
   */
  @org.junit.Test
  public void fnResolveQname6() {
    final XQuery query = new XQuery(
      "fn:string(fn:local-name-from-QName(fn:resolve-QName(\"p1:name\", <anElement xmlns:p1=\"http://example.com/examples\" xmlns:P1=\"http://example.com/examples\">Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "name")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the give qname has a prefix and there is a namespace binding for the element. uses fn:namespace-uri-from-QName to get local part. Element is given as direct element .
   */
  @org.junit.Test
  public void fnResolveQname7() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-from-QName(fn:resolve-QName(\"P1:name\", <anElement xmlns:p1=\"http://example.com/examples\" xmlns:P1=\"http://someothernamespace.com/examples\">Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://someothernamespace.com/examples")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the given qname has no prefix and there is a namespace binding for the element. uses fn:local-part-from-QName to get local part. Element is given as direct element .
   */
  @org.junit.Test
  public void fnResolveQname8() {
    final XQuery query = new XQuery(
      "fn:string(fn:local-name-from-QName(fn:resolve-QName(\"anElement\", <anElement xmlns:p1=\"http://example.com/examples\">Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "anElement")
    );
  }

  /**
   *  Evaluation of fn:resolve-qname function for which the given qname has no prefix and there is a namespace binding for the element. uses fn:namespace-uri-from-qname to get namespace uri. Element is given as direct element .
   */
  @org.junit.Test
  public void fnResolveQname9() {
    final XQuery query = new XQuery(
      "fn:string(fn:namespace-uri-from-QName(fn:resolve-QName(\"anElement\", <anElement xmlns=\"http://example.com/examples\">Some content</anElement>)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.com/examples")
    );
  }
}
