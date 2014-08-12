package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the prefix-from-QName() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnPrefixFromQName extends QT3TestSet {

  /**
   *  A test whose essence is: `prefix-from-QName()`. .
   */
  @org.junit.Test
  public void kPrefixFromQName1() {
    final XQuery query = new XQuery(
      "prefix-from-QName()",
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
   *  A test whose essence is: `prefix-from-QName(1, 2)`. .
   */
  @org.junit.Test
  public void kPrefixFromQName2() {
    final XQuery query = new XQuery(
      "prefix-from-QName(1, 2)",
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
   *  A test whose essence is: `empty(prefix-from-QName( () ))`. .
   */
  @org.junit.Test
  public void kPrefixFromQName3() {
    final XQuery query = new XQuery(
      "empty(prefix-from-QName( () ))",
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
   *  A test whose essence is: `prefix-from-QName( QName("example.com/", "pre:lname")) eq "pre"`. .
   */
  @org.junit.Test
  public void kPrefixFromQName4() {
    final XQuery query = new XQuery(
      "prefix-from-QName( QName(\"example.com/\", \"pre:lname\")) eq \"pre\"",
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
   *  Evaluation of fn-prefix-fromQName function with wrong arity. .
   */
  @org.junit.Test
  public void fnPrefixFromQname1() {
    final XQuery query = new XQuery(
      "fn:prefix-from-QName(\"arg1\",\"arg2\")",
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
   *  Evaluation of fn-prefix-fromQName function as argument to fn:lower-case. .
   */
  @org.junit.Test
  public void fnPrefixFromQname10() {
    final XQuery query = new XQuery(
      "fn:lower-case(fn:prefix-from-QName(xs:QName(\"FOO:bar\")))",
      ctx);
    try {
      query.namespace("FOO", "http://example.org");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "foo")
    );
  }

  /**
   *  Evaluation of fn-prefix-fromQName function as argument to fn:lower-case. .
   */
  @org.junit.Test
  public void fnPrefixFromQname11() {
    final XQuery query = new XQuery(
      "fn:concat(fn:prefix-from-QName(xs:QName(\"foo:bar\")),\":bar\")",
      ctx);
    try {
      query.namespace("foo", "http://example.org");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "foo:bar")
    );
  }

  /**
   *  Evaluation of fn-prefix-fromQName function as argument to fn:substring-before. .
   */
  @org.junit.Test
  public void fnPrefixFromQname12() {
    final XQuery query = new XQuery(
      "fn:substring-before(fn:prefix-from-QName(xs:QName(\"foo:bar\")),\"oo\")",
      ctx);
    try {
      query.namespace("foo", "http://example.org");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "f")
    );
  }

  /**
   *  Evaluation of fn-prefix-fromQName function as argument to fn:substring-after. .
   */
  @org.junit.Test
  public void fnPrefixFromQname13() {
    final XQuery query = new XQuery(
      "fn:substring-after(fn:prefix-from-QName(xs:QName(\"foo:bar\")),\"f\")",
      ctx);
    try {
      query.namespace("foo", "http://example.org");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "oo")
    );
  }

  /**
   *  Evaluation of fn-prefix-fromQName function as argument to fn:string-length. .
   */
  @org.junit.Test
  public void fnPrefixFromQname14() {
    final XQuery query = new XQuery(
      "fn:string-length(fn:prefix-from-QName(xs:QName(\"foo:bar\")))",
      ctx);
    try {
      query.namespace("foo", "http://example.org");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   *  Evaluation of fn-prefix-fromQName function as argument to fn:substring. .
   */
  @org.junit.Test
  public void fnPrefixFromQname15() {
    final XQuery query = new XQuery(
      "fn:substring(fn:prefix-from-QName(xs:QName(\"foo:bar\")),2)",
      ctx);
    try {
      query.namespace("foo", "http://example.org");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "oo")
    );
  }

  /**
   *  Evaluation of fn-prefix-fromQName function as argument to fn:string-join. .
   */
  @org.junit.Test
  public void fnPrefixFromQname16() {
    final XQuery query = new XQuery(
      "fn:string-join((fn:prefix-from-QName(xs:QName(\"foo:bar\")),\":bar\"),\"\")",
      ctx);
    try {
      query.namespace("foo", "http://example.org");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "foo:bar")
    );
  }

  /**
   *  Evaluation of fn-prefix-fromQName function as argument to fn:starts-with. .
   */
  @org.junit.Test
  public void fnPrefixFromQname17() {
    final XQuery query = new XQuery(
      "fn:starts-with(fn:prefix-from-QName(xs:QName(\"foo:bar\")),\"f\")",
      ctx);
    try {
      query.namespace("foo", "http://example.org");
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
   *  Evaluation of fn-prefix-fromQName function as argument to fn:ends-with. .
   */
  @org.junit.Test
  public void fnPrefixFromQname18() {
    final XQuery query = new XQuery(
      "fn:ends-with(fn:prefix-from-QName(xs:QName(\"foo:bar\")),\"f\")",
      ctx);
    try {
      query.namespace("foo", "http://example.org");
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
   *  Evaluation of fn-prefix-fromQName function as argument to fn:contains. .
   */
  @org.junit.Test
  public void fnPrefixFromQname19() {
    final XQuery query = new XQuery(
      "fn:contains(fn:prefix-from-QName(xs:QName(\"foo:bar\")),\"f\")",
      ctx);
    try {
      query.namespace("foo", "http://example.org");
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
   *  Evaluation of fn-prefix-fromQName function with wrong argument type. .
   */
  @org.junit.Test
  public void fnPrefixFromQname2() {
    final XQuery query = new XQuery(
      "fn:prefix-from-QName(xs:integer(1))",
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
   *  Evaluation of fn-prefix-fromQName function with two namespaces declared with same namespace (different case). .
   */
  @org.junit.Test
  public void fnPrefixFromQname20() {
    final XQuery query = new XQuery(
      "fn:prefix-from-QName(xs:QName(\"foo:bar\"))",
      ctx);
    try {
      query.namespace("foo", "http://example.org");
      query.namespace("FOO", "http://example.org");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "foo")
    );
  }

  /**
   *  Evaluation of fn-prefix-fromQName function as argument to fn:string function and no prefix. Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnPrefixFromQname3() {
    final XQuery query = new XQuery(
      "fn:count(fn:prefix-from-QName(xs:QName(\"name\")))",
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
   *  Evaluation of fn-prefix-fromQName function as argument to xs:string function and no prefix Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnPrefixFromQname4() {
    final XQuery query = new XQuery(
      "fn:string-length(xs:string(fn:prefix-from-QName(xs:QName(\"name\"))))",
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
   *  Evaluation of fn-prefix-fromQName function as argument to xs:string function and with prefix .
   */
  @org.junit.Test
  public void fnPrefixFromQname5() {
    final XQuery query = new XQuery(
      "xs:string(fn:prefix-from-QName(xs:QName(\"foo:name\")))",
      ctx);
    try {
      query.namespace("foo", "http://example.org");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "foo")
    );
  }

  /**
   *  Evaluation of fn-prefix-fromQName function as argument to fn:string function and with prefix. .
   */
  @org.junit.Test
  public void fnPrefixFromQname6() {
    final XQuery query = new XQuery(
      "fn:string(fn:prefix-from-QName(xs:QName(\"foo:name\")))",
      ctx);
    try {
      query.namespace("foo", "http://example.org");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "foo")
    );
  }

  /**
   *  Evaluation of fn-prefix-fromQName function with argument set to empty sequence. Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void fnPrefixFromQname7() {
    final XQuery query = new XQuery(
      "fn:count(fn:prefix-from-QName(()))",
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
   *  Evaluation of fn-prefix-fromQName function with a prefix that is not defined. .
   */
  @org.junit.Test
  public void fnPrefixFromQname8() {
    final XQuery query = new XQuery(
      "fn:prefix-from-QName(xs:QName(\"foo:bar\"))",
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
   *  Evaluation of fn-prefix-fromQName function as argument to fn:upper-case. .
   */
  @org.junit.Test
  public void fnPrefixFromQname9() {
    final XQuery query = new XQuery(
      "fn:upper-case(fn:prefix-from-QName(xs:QName(\"foo:bar\")))",
      ctx);
    try {
      query.namespace("foo", "http://example.org");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "FOO")
    );
  }
}
