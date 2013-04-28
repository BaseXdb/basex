package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the require-feature and prohibit-feature options..
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdRequireProhibitFeature extends QT3TestSet {

  /**
   * An error must be thrown if the feature is prohibited and used. .
   */
  @org.junit.Test
  public void prohibitAllOptionalFeatures1() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option prohibit-feature \"all-optional-features\";\n" +
      "      1 instance of function(*)\n" +
      "    ",
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
        error("XQST0129")
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * The require-feature and prohibit-feature options may be lists of QNames..
   */
  @org.junit.Test
  public void prohibitFeatureList1() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option prohibit-feature \"all-extensions static-typing\";\n" +
      "      ()\n" +
      "    ",
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
        assertEmpty()
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * The require-feature and prohibit-feature options may be lists of QNames..
   */
  @org.junit.Test
  public void prohibitFeatureList2() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option prohibit-feature \"static-typing all-extensions\";\n" +
      "      ()\n" +
      "    ",
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
        assertEmpty()
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * An error must be thrown if the feature is prohibited and used. .
   */
  @org.junit.Test
  public void prohibitHigherOrderFunction1Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option prohibit-feature \"higher-order-function\";\n" +
      "      1 instance of function(*)\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0129")
    );
  }

  /**
   * An error must be thrown if the feature is prohibited and used. .
   */
  @org.junit.Test
  public void prohibitHigherOrderFunction1S() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option prohibit-feature \"higher-order-function\";\n" +
      "      1 instance of function(*)\n" +
      "    ",
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
        error("XQST0129")
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * An error must be thrown if the feature is prohibited and used. .
   */
  @org.junit.Test
  public void prohibitHigherOrderFunction2Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option prohibit-feature \"higher-order-function\";\n" +
      "      for $f in () return $f(1)\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0129")
    );
  }

  /**
   * An error must be thrown if the feature is prohibited and used. .
   */
  @org.junit.Test
  public void prohibitHigherOrderFunction2S() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option prohibit-feature \"higher-order-function\";\n" +
      "      for $f in () return $f(1)\n" +
      "    ",
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
        error("XQST0129")
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * An error must be thrown if the feature is prohibited and used..
   */
  @org.junit.Test
  public void prohibitHigherOrderFunction3Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option prohibit-feature \"higher-order-function\";\n" +
      "      let $f := function($x) { $x + 1 } return 0\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0129")
    );
  }

  /**
   * An error must be thrown if the feature is prohibited and used..
   */
  @org.junit.Test
  public void prohibitHigherOrderFunction3S() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option prohibit-feature \"higher-order-function\";\n" +
      "      let $f := function($x) { $x + 1 } return 0\n" +
      "    ",
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
        error("XQST0129")
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * An error must be thrown if the feature is prohibited and used..
   */
  @org.junit.Test
  public void prohibitHigherOrderFunction4Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option prohibit-feature \"higher-order-function\";\n" +
      "      let $f := sum#1 return 0\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0129")
    );
  }

  /**
   * An error must be thrown if the feature is prohibited and used..
   */
  @org.junit.Test
  public void prohibitHigherOrderFunction4S() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option prohibit-feature \"higher-order-function\";\n" +
      "      let $f := sum#1 return 0\n" +
      "    ",
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
        error("XQST0129")
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * An error must be thrown if the feature is prohibited and used..
   */
  @org.junit.Test
  public void prohibitHigherOrderFunction5Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option prohibit-feature \"higher-order-function\";\n" +
      "      for $f in () return $f(1, ?)\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0129")
    );
  }

  /**
   * An error must be thrown if the feature is prohibited and used..
   */
  @org.junit.Test
  public void prohibitHigherOrderFunction5S() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option prohibit-feature \"higher-order-function\";\n" +
      "      for $f in () return $f(1, ?)\n" +
      "    ",
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
        error("XQST0129")
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * An error must be thrown if the feature is prohibited and used..
   */
  @org.junit.Test
  public void prohibitModule1Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace m = \"http://www.example.com/dummy-module\";\n" +
      "      declare option prohibit-feature \"module\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/dummy.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0016")
    );
  }

  /**
   * An error must be thrown if the feature is prohibited and used..
   */
  @org.junit.Test
  public void prohibitModule1S() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace m = \"http://www.example.com/dummy-module\";\n" +
      "      declare option prohibit-feature \"module\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/dummy.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XQST0016")
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * All extensions features cannot be activated..
   */
  @org.junit.Test
  public void requireAllExtensions1() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"all-extensions\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0126")
    );
  }

  /**
   * An error must be thrown if the feature is prohibited and used. .
   */
  @org.junit.Test
  public void requireAllOptionalFeatures2Ns3() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"all-optional-features\";\n" +
      "      declare option prohibit-feature \"higher-order-function\";\n" +
      "      1 instance of function(*)\n" +
      "    ",
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
        error("XQST0129")
      ||
        error("XQST0128")
      ||
        error("XQST0120")
      )
    );
  }

  /**
   * An error must be thrown if the feature is prohibited and used. .
   */
  @org.junit.Test
  public void requireAllOptionalFeatures3Ns1() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option prohibit-feature \"higher-order-function\";\n" +
      "      declare option require-feature \"all-optional-features\";\n" +
      "      1 instance of function(*)\n" +
      "    ",
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
        error("XQST0129")
      ||
        error("XQST0128")
      ||
        error("XQST0120")
      )
    );
  }

  /**
   * Test should succeed if feature is supported by implementation, otherwise error. .
   */
  @org.junit.Test
  public void requireAllOptionalFeatures5Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option prohibit-feature \"all-optional-features\";\n" +
      "      declare option require-feature \"higher-order-function\";\n" +
      "      1 instance of function(*)\n" +
      "    ",
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
        assertBoolean(false)
      ||
        error("XQST0120")
      )
    );
  }

  /**
   * Test should succeed if feature is supported by implementation, otherwise error. .
   */
  @org.junit.Test
  public void requireAllOptionalFeatures5S() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option prohibit-feature \"all-optional-features\";\n" +
      "      declare option require-feature \"higher-order-function\";\n" +
      "      1 instance of function(*)\n" +
      "    ",
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
        assertBoolean(false)
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * An error must be thrown if the feature is prohibited and used. .
   */
  @org.junit.Test
  public void requireAllOptionalFeatures7Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option prohibit-feature \"all-optional-features\";\n" +
      "      declare option require-feature \"module\";\n" +
      "      1 instance of function(*)\n" +
      "    ",
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
        error("XQST0129")
      ||
        error("XQST0120")
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * An error must be thrown if the feature is prohibited and used. .
   */
  @org.junit.Test
  public void requireAllOptionalFeatures7S() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option prohibit-feature \"all-optional-features\";\n" +
      "      declare option require-feature \"module\";\n" +
      "      1 instance of function(*)\n" +
      "    ",
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
        error("XQST0129")
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * The require-feature and prohibit-feature options may be lists of QNames..
   */
  @org.junit.Test
  public void requireFeatureList1Ns1() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      declare option require-feature \"module higher-order-function\";\n" +
      "      let $f := function() { () } return $f()\n" +
      "    ",
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
        assertEmpty()
      ||
        error("XQST0120")
      )
    );
  }

  /**
   * The require-feature and prohibit-feature options may be lists of QNames..
   */
  @org.junit.Test
  public void requireFeatureList1Ns2() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      declare option require-feature \"module higher-order-function\";\n" +
      "      let $f := function() { () } return $f()\n" +
      "    ",
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
        assertEmpty()
      ||
        error("XQST0120")
      )
    );
  }

  /**
   * The require-feature and prohibit-feature options may be lists of QNames..
   */
  @org.junit.Test
  public void requireFeatureList1S() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      declare option require-feature \"module higher-order-function\";\n" +
      "      let $f := function() { () } return $f()\n" +
      "    ",
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
        assertEmpty()
      )
    );
  }

  /**
   * The require-feature and prohibit-feature options may be lists of QNames..
   */
  @org.junit.Test
  public void requireFeatureList2Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"unknown higher-order-function\";\n" +
      "      let $f := function() { () } return $f()\n" +
      "    ",
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
        error("XQST0123")
      ||
        error("XQST0120")
      )
    );
  }

  /**
   * The require-feature and prohibit-feature options may be lists of QNames..
   */
  @org.junit.Test
  public void requireFeatureList2S() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"unknown higher-order-function\";\n" +
      "      let $f := function() { () } return $f()\n" +
      "    ",
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
        error("XQST0123")
      )
    );
  }

  /**
   * The require-feature and prohibit-feature options may be lists of QNames..
   */
  @org.junit.Test
  public void requireFeatureList3Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"higher-order-function unknown\";\n" +
      "      let $f := function() { () } return $f()\n" +
      "    ",
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
        error("XQST0123")
      )
    );
  }

  /**
   * The require-feature and prohibit-feature options may be lists of QNames..
   */
  @org.junit.Test
  public void requireFeatureList3S() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"unknown higher-order-function\";\n" +
      "      let $f := function() { () } return $f()\n" +
      "    ",
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
        error("XQST0123")
      )
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction1Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"higher-order-function\";\n" +
      "      1 instance of function(*)\n" +
      "    ",
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
        assertBoolean(false)
      ||
        error("XQST0120")
      )
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction1S() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"higher-order-function\";\n" +
      "      1 instance of function(*)\n" +
      "    ",
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
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction10Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      for $f in () return $f(1, ?)\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0129")
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction10S() {
    final XQuery query = new XQuery(
      "\n" +
      "      for $f in () return $f(1, ?)\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction2Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"higher-order-function\";\n" +
      "      for $f in () return $f(1)\n" +
      "    ",
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
        assertEmpty()
      ||
        error("XQST0120")
      )
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction2S() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"higher-order-function\";\n" +
      "      for $f in () return $f(1)\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction3Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"higher-order-function\";\n" +
      "      let $f := function($x) { $x + 1 } return ()\n" +
      "    ",
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
        assertEmpty()
      ||
        error("XQST0120")
      )
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction3S() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"higher-order-function\";\n" +
      "      let $f := function($x) { $x + 1 } return ()\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction4Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"higher-order-function\";\n" +
      "      let $f := sum#1 return 0\n" +
      "    ",
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
        assertEq("0")
      ||
        error("XQST0120")
      )
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction4S() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"higher-order-function\";\n" +
      "      let $f := sum#1 return 0\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction5Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"higher-order-function\";\n" +
      "      for $f in () return $f(1, ?)\n" +
      "    ",
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
        assertEmpty()
      ||
        error("XQST0120")
      )
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction5S() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"higher-order-function\";\n" +
      "      for $f in () return $f(1, ?)\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction6Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      1 instance of function(*)\n" +
      "    ",
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
        assertBoolean(false)
      ||
        error("XQST0129")
      )
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction6S() {
    final XQuery query = new XQuery(
      "\n" +
      "      1 instance of function(*)\n" +
      "    ",
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
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction7Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      for $f in () return $f(1)\n" +
      "    ",
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
        assertEmpty()
      ||
        error("XQST0129")
      )
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction7S() {
    final XQuery query = new XQuery(
      "\n" +
      "      for $f in () return $f(1)\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction8Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      let $f := function($x) { $x + 1 } return 0\n" +
      "    ",
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
        assertEq("0")
      ||
        error("XQST0129")
      )
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction8S() {
    final XQuery query = new XQuery(
      "\n" +
      "      let $f := function($x) { $x + 1 } return 0\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction9Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      let $f := sum#1 return 0\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0129")
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireHigherOrderFunction9S() {
    final XQuery query = new XQuery(
      "\n" +
      "      let $f := sum#1 return 0\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireModule1Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace m = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      declare option require-feature \"module\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/dummy.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEmpty()
      ||
        error("XQST0120")
      )
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireModule1S() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace m = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      declare option require-feature \"module\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/dummy.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireModule2Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace m = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/dummy.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0016")
    );
  }

  /**
   * An error must be thrown if the feature is not supported..
   */
  @org.junit.Test
  public void requireModule2S() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace m = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/dummy.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * A feature cannot be both required and prohibited..
   */
  @org.junit.Test
  public void requireProhibit1() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"schema-aware\";\n" +
      "      declare option prohibit-feature \"schema-aware\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0127")
    );
  }

  /**
   * A feature cannot be both required and prohibited..
   */
  @org.junit.Test
  public void requireProhibit2() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"module\";\n" +
      "      declare option prohibit-feature \"module\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0127")
    );
  }

  /**
   * A feature cannot be both required and prohibited..
   */
  @org.junit.Test
  public void requireProhibit4() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"higher-order-function\";\n" +
      "      declare option prohibit-feature \"higher-order-function\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0127")
    );
  }

  /**
   * A feature cannot be both required and prohibited..
   */
  @org.junit.Test
  public void requireProhibit5() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"static-typing\";\n" +
      "      declare option prohibit-feature \"static-typing\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0127")
    );
  }

  /**
   * An error must be thrown if the feature is not recognized..
   */
  @org.junit.Test
  public void requireSerialization1() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"serialization\";\n" +
      "      ()\n" +
      "    ",
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
        error("XQST0123")
      ||
        assertEmpty()
      )
    );
  }

  /**
   * Prohibiting or requiring a feature only affects the module..
   */
  @org.junit.Test
  public void separateModules1() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      declare option prohibit-feature \"higher-order-function\";\n" +
      "      let $f := module:one() return 1\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/uses.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("1")
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * Prohibiting or requiring a feature only affects the module..
   */
  @org.junit.Test
  public void separateModules2Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      declare option prohibit-feature \"all-optional-features\";\n" +
      "      declare option require-feature \"module\";\n" +
      "      for $f in module:one() return 1\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/uses.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XQST0129")
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * Prohibiting or requiring a feature only affects the module..
   */
  @org.junit.Test
  public void separateModules2S() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      declare option prohibit-feature \"all-optional-features\";\n" +
      "      declare option require-feature \"module\";\n" +
      "      for $f in module:one() return 1\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/uses.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("1")
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * Prohibiting or requiring a feature only affects the module..
   */
  @org.junit.Test
  public void separateModules3Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      declare option prohibit-feature \"higher-order-function\";\n" +
      "      for $f in module:one() return 1\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/requires.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("1")
      ||
        error("XQST0120")
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * Prohibiting or requiring a feature only affects the module..
   */
  @org.junit.Test
  public void separateModules3S() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      declare option prohibit-feature \"higher-order-function\";\n" +
      "      for $f in module:one() return 1\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/requires.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("1")
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * Prohibiting or requiring a feature only affects the module..
   */
  @org.junit.Test
  public void separateModules4Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      module:one()()\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/requires.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XQST0120")
      ||
        error("XQST0129")
      )
    );
  }

  /**
   * Prohibiting or requiring a feature only affects the module..
   */
  @org.junit.Test
  public void separateModules4S() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      module:one()()\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/requires.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   * Prohibiting or requiring a feature only affects the module..
   */
  @org.junit.Test
  public void separateModules5Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      declare option prohibit-feature \"higher-order-function\";\n" +
      "      module:one()()\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/requires.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XQST0129")
      ||
        error("XQST0120")
      )
    );
  }

  /**
   * Prohibiting or requiring a feature only affects the module..
   */
  @org.junit.Test
  public void separateModules5S() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      declare option prohibit-feature \"higher-order-function\";\n" +
      "      module:one()()\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/requires.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XQST0129")
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * Prohibiting or requiring a feature only affects the module..
   */
  @org.junit.Test
  public void separateModules6Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/prohibits-uses.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XQST0129")
      )
    );
  }

  /**
   * Prohibiting or requiring a feature only affects the module..
   */
  @org.junit.Test
  public void separateModules6S() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/prohibits-uses.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XQST0129")
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * Prohibiting or requiring a feature only affects the module..
   */
  @org.junit.Test
  public void separateModules7Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      declare option require-feature \"higher-order-function\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/prohibits-uses.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XQST0129")
      ||
        error("XQST0120")
      )
    );
  }

  /**
   * Prohibiting or requiring a feature only affects the module..
   */
  @org.junit.Test
  public void separateModules7S() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      declare option require-feature \"higher-order-function\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/prohibits-uses.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XQST0129")
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * Prohibiting or requiring a feature only affects the module..
   */
  @org.junit.Test
  public void separateModules8Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      let $f := function() { () } return $f()\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/prohibits.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0129")
    );
  }

  /**
   * Prohibiting or requiring a feature only affects the module..
   */
  @org.junit.Test
  public void separateModules8S() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      let $f := function() { () } return $f()\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/prohibits.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEmpty()
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * Prohibiting or requiring a feature only affects the module..
   */
  @org.junit.Test
  public void separateModules9Ns() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      declare option require-feature \"higher-order-function\";\n" +
      "      let $f := function() { () } return $f()\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/prohibits.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEmpty()
      ||
        error("XQST0120")
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * Prohibiting or requiring a feature only affects the module..
   */
  @org.junit.Test
  public void separateModules9S() {
    final XQuery query = new XQuery(
      "\n" +
      "      import module namespace module = \"http://www.w3.org/XQueryTest/RequireProhibitFeature\";\n" +
      "      declare option require-feature \"higher-order-function\";\n" +
      "      let $f := function() { () } return $f()\n" +
      "    ",
      ctx);
    try {
      query.addModule("http://www.w3.org/XQueryTest/RequireProhibitFeature", file("prod/RequireProhibitFeature/prohibits.xquery"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEmpty()
      ||
        error("XQST0128")
      )
    );
  }

  /**
   * An error must be thrown if the feature is not recognized..
   */
  @org.junit.Test
  public void unknownFeature() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare namespace example = \"http://www.example.com/does/not/exist\";\n" +
      "      declare option require-feature \"example:does-not-exist\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0123")
    );
  }

  /**
   * Features must be lexical QNames..
   */
  @org.junit.Test
  public void wellFormedFeature1() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"not:a:qname\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0122")
    );
  }

  /**
   * Features must be lexical QNames..
   */
  @org.junit.Test
  public void wellFormedFeature2() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"qname not:a:qname\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0122")
    );
  }

  /**
   * Features must be lexical QNames..
   */
  @org.junit.Test
  public void wellFormedFeature3() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"123\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0122")
    );
  }

  /**
   * The prefix of a feature must be bound..
   */
  @org.junit.Test
  public void wellFormedFeature4() {
    final XQuery query = new XQuery(
      "\n" +
      "      declare option require-feature \"unkonwn:prefix\";\n" +
      "      ()\n" +
      "    ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0081")
    );
  }
}
