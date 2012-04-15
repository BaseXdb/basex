package org.basex.test.qt3ts.misc;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the CombinedErrorCodes operator.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MiscCombinedErrorCodes extends QT3TestSet {

  /**
   *  Schema import binding to no namespace, and no location hint. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes1() {
    final XQuery query = new XQuery(
      "import schema \"http://example.com/NSNOTRECOGNIZED\"; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XQST0009")
      ||
        error("XQST0059")
      )
    );
  }

  /**
   *  A 'validate' expression with an invalid operand. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes10() {
    final XQuery query = new XQuery(
      "validate { () }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XQTY0030")
      ||
        error("XQST0075")
      )
    );
  }

  /**
   *  A 'validate' expression with an invalid operand. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes11() {
    final XQuery query = new XQuery(
      "validate lax { 1 }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XQTY0030")
      ||
        error("XQST0075")
      )
    );
  }

  /**
   *  A 'validate' expression with an invalid operand. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes12() {
    final XQuery query = new XQuery(
      "validate strict { 1 }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XQTY0030")
      ||
        error("XQST0075")
      )
    );
  }

  /**
   *  A 'validate' expression with an invalid operand. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes13() {
    final XQuery query = new XQuery(
      "validate lax { }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XPST0003")
      ||
        error("XQST0075")
      )
    );
  }

  /**
   *  A 'validate' expression with an invalid operand. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes14() {
    final XQuery query = new XQuery(
      "validate strict { }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XPST0003")
      ||
        error("XQST0075")
      )
    );
  }

  /**
   *  A 'validate' expression with an invalid operand. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes15() {
    final XQuery query = new XQuery(
      "validate { }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XPST0003")
      ||
        error("XQST0075")
      )
    );
  }

  /**
   *  Schema import binding to no namespace, but has a location hint. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes2() {
    final XQuery query = new XQuery(
      "import schema \"http://example.com/NSNOTRECOGNIZED\" at \"http://example.com/DOESNOTEXIST\"; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XQST0009")
      ||
        error("XQST0059")
      )
    );
  }

  /**
   *  Schema import binding to no namespace, but has three location hints. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes3() {
    final XQuery query = new XQuery(
      "import schema \"http://example.com/NSNOTRECOGNIZED\" at \"http://example.com/DOESNOTEXIST\", \"http://example.com/2\", \"http://example.com/3\"; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XQST0009")
      ||
        error("XQST0059")
      )
    );
  }

  /**
   *  Schema import binding to no namespace, but has three location hints. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes4() {
    final XQuery query = new XQuery(
      "import schema \"http://example.com/NSNOTRECOGNIZED\" at \"http://example.com/DOESNOTEXIST\", \"http://example.com/2\", \"http://example.com/3\"; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XQST0009")
      ||
        error("XQST0059")
      )
    );
  }

  /**
   *  Schema import binding to a namespace, and has three location hints. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes5() {
    final XQuery query = new XQuery(
      "import schema namespace prefix = \"http://example.com/NSNOTRECOGNIZED\" at \"http://example.com/DOESNOTEXIST\", \"http://example.com/2DOESNOTEXIST\", \"http://example.com/3DOESNOTEXIST\"; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XQST0009")
      ||
        error("XQST0059")
      )
    );
  }

  /**
   *  Schema import binding to the default element namespace, and has three location hints. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes6() {
    final XQuery query = new XQuery(
      "import schema default element namespace \"http://example.com/NSNOTRECOGNIZED\" at \"http://example.com/DOESNOTEXIST\", \"http://example.com/2DOESNOTEXIST\", \"http://example.com/3DOESNOTEXIST\"; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XQST0009")
      ||
        error("XQST0059")
      )
    );
  }

  /**
   *  ':=' cannot be used to assing namespaces in 'import schema'. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes7() {
    final XQuery query = new XQuery(
      "import schema namespace NCName := \"http://example.com/Dummy\"; 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  A 'validate' expression with an invalid operand. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes9() {
    final XQuery query = new XQuery(
      "validate { 1 }",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XQTY0030")
      ||
        error("XQST0075")
      )
    );
  }

  /**
   *  Evaluates simple module import to generate error code if feature not supported. .
   */
  @org.junit.Test
  public void combinedErrors1() {
    final XQuery query = new XQuery(
      "import module namespace defs=\"http://www.w3.org/TestModules/defs\"; \"ABC\"",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("misc/CombinedErrorCodes/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/defs", file("misc/CombinedErrorCodes/moduleDefs-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "ABC")
      ||
        error("XQST0016")
      )
    );
  }

  /**
   *  Evaluates simple full axis feature (preceding axis) to generate error code if feature not supported. .
   */
  @org.junit.Test
  public void combinedErrors4() {
    final XQuery query = new XQuery(
      "/works[1]/employee[2]/preceding::employee",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<employee name=\"Jane Doe 1\" gender=\"female\">\n   <empnum>E1</empnum>\n   <pnum>P1</pnum>\n   <hours>40</hours>\n  </employee>", false)
      ||
        error("XPST0010")
      )
    );
  }
}
