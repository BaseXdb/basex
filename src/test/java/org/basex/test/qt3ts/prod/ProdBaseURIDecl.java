package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the BaseURIDecl production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdBaseURIDecl extends QT3TestSet {

  /**
   *  Test 'declare base-uri' with fn:static-base-uri(). .
   */
  @org.junit.Test
  public void kBaseURIProlog1() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare(::)base-uri(::)\"http://example.com/declareBaseURITest\"; \n" +
      "        static-base-uri() eq 'http://example.com/declareBaseURITest'\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Two 'declare base-uri' expressions, where the URIs differs. .
   */
  @org.junit.Test
  public void kBaseURIProlog2() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare(::)base-uri(::)\"http://example.com/declareBaseURITest\"; \n" +
      "        declare(::)base-uri(::)\"http://example.com/declareBaseURITest2\"; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0032")
    );
  }

  /**
   *  Two 'declare base-uri' expressions, where the URIs are equal. .
   */
  @org.junit.Test
  public void kBaseURIProlog3() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare(::)base-uri(::)\"http://example.com/declareBaseURITest\"(::); \n" +
      "        declare(::)base-uri(::)\"http://example.com/declareBaseURITest\"(::); 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0032")
    );
  }

  /**
   *  Specify an invalid URI. .
   */
  @org.junit.Test
  public void k2BaseURIProlog1() {
    final XQuery query = new XQuery(
      "declare base-uri \"http:\\\\invalid>URI\\someURI\"; 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("1")
      ||
        error("XQST0046")
      )
    );
  }

  /**
   *  A base-uri declaration with a Windows file path which is invalid. .
   */
  @org.junit.Test
  public void k2BaseURIProlog2() {
    final XQuery query = new XQuery(
      "declare base-uri \"c:\\windows\"; 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("1")
      ||
        error("XQST0046")
      )
    );
  }

  /**
   *  A base-uri declaration with a Windows file path which is invalid, and which is being used by a call to fn:doc(). .
   */
  @org.junit.Test
  public void k2BaseURIProlog3() {
    final XQuery query = new XQuery(
      "declare base-uri \"c:\\windows\"; fn:doc(\"example.com.xml\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XQST0046")
      ||
        error("FODC0005")
      ||
        error("FODC0002")
      )
    );
  }

  /**
   *  The static base-uri must be absolute. Since the declaration supplies a relative URI, 
   *         an implementation may fail with computing an absolute URI, hence XPST0001 is allowed. 
   *         The test checks that the static base-uri is absolute. .
   */
  @org.junit.Test
  public void k2BaseURIProlog4() {
    final XQuery query = new XQuery(
      "declare base-uri \"abc\"; declare function local:isAbsolute($uri as xs:string?) as xs:boolean { fn:matches($uri, \"[a-zA-Z0-9\\-.]*:/\") }; local:isAbsolute(fn:static-base-uri())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        error("XPST0001")
      )
    );
  }

  /**
   *  Use an empty use as base-uri. Since the implementation may fail with determining the base-uri, XPST0001 is allowed. .
   */
  @org.junit.Test
  public void k2BaseURIProlog5() {
    final XQuery query = new XQuery(
      "declare base-uri \"\"; ends-with(fn:static-base-uri(), \"prod/BaseURIDecl.xml\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        error("XPST0001")
      )
    );
  }

  /**
   *  The static base-uri is not affected by xml:base declarations on direct element constructors. .
   */
  @org.junit.Test
  public void k2BaseURIProlog6() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.com/BASEURI\"; <e xml:base=\"../\"> {fn:static-base-uri()} </e>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<e xml:base=\"../\">http://example.com/BASEURI</e>", false)
    );
  }

  /**
   *  Ensure the 'base-uri' keyword is parsed correctly. .
   */
  @org.junit.Test
  public void k2BaseURIProlog7() {
    final XQuery query = new XQuery(
      "base-uri lt base-uri",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Test for declaration of base-uri twice. .
   */
  @org.junit.Test
  public void baseURI1() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.org\"; declare base-uri \"http://example.org\"; \"aaa\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0032")
    );
  }

  /**
   *  Evaluates base-uri with the fn:static-base-uri function. base-uri not defined Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void baseURI10() {
    final XQuery query = new XQuery(
      "fn:count(fn:static-base-uri())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("0")
      ||
        assertEq("1")
      )
    );
  }

  /**
   *  Evaluates base-uri property can contain numbers. Used with static-base--uri function. .
   */
  @org.junit.Test
  public void baseURI11() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com/abc123\"; fn:string(fn:static-base-uri())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/abc123")
    );
  }

  /**
   *  Evaluates base-uri property can contain an escape quote. Used with static-base--uri function. .
   */
  @org.junit.Test
  public void baseURI12() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com/abc\"\"\"; fn:string(fn:static-base-uri())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "http://www.example.com/abc\"")
      ||
        error("XQST0046")
      )
    );
  }

  /**
   *  Evaluates base-uri property can contain an escape apostrophe. Used with static-base--uri function. .
   */
  @org.junit.Test
  public void baseURI13() {
    final XQuery query = new XQuery(
      "declare base-uri 'http://www.example.com/abc'''; fn:string(fn:static-base-uri())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/abc'")
    );
  }

  /**
   *  Evaluates base-uri property can contain "##0;". Used with static-base--uri function. .
   */
  @org.junit.Test
  public void baseURI14() {
    final XQuery query = new XQuery(
      "declare base-uri 'http://www.example.com/abc##0;'; fn:string(fn:static-base-uri())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "http://www.example.com/abc##0;")
      ||
        error("XQST0046")
      )
    );
  }

  /**
   *  Evaluates base-uri property can contain a single character (after the "http://"section). Used with static-base-uri function. .
   */
  @org.junit.Test
  public void baseURI15() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://A\"; fn:string(fn:static-base-uri())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://A")
    );
  }

  /**
   *  Evaluates base-uri property can contain the string "&#xa; (newline)". Used with static-base--uri function. .
   */
  @org.junit.Test
  public void baseURI18() {
    final XQuery query = new XQuery(
      "declare base-uri \"http:/www.abc&#xa;.com\"; fn:string(fn:static-base-uri())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "http:/www.abc .com")
      ||
        error("XQST0046")
      )
    );
  }

  /**
   *  Evaluates base-uri property can contain "declarebase-uri". Used with static-base--uri function. .
   */
  @org.junit.Test
  public void baseURI19() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://declarebase-uri.com\"; fn:string(fn:static-base-uri())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "http://declarebase-uri.com")
      ||
        error("XQST0046")
      )
    );
  }

  /**
   *  Verify that a base uri declaration containing "abc&lt;" is a valid base-uri declaration. .
   */
  @org.junit.Test
  public void baseURI2() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com/abc&lt;\"; \"aaa\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "aaa")
      ||
        error("XQST0046")
      )
    );
  }

  /**
   *  Evaluates that base-uri property can contain "base-uri". Used with static-base--uri function. .
   */
  @org.junit.Test
  public void baseURI20() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.base-uri.com\"; fn:string(fn:static-base-uri())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.base-uri.com")
    );
  }

  /**
   *  Evaluates that base-uri property can contain "BASE-URI". Used with static-base-uri function. .
   */
  @org.junit.Test
  public void baseURI21() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.BASE-URI.com\"; fn:string(fn:static-base-uri())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.BASE-URI.com")
    );
  }

  /**
   * Verify normalization of xs:anyURI (leading spaces). Used with static-base--uri function. .
   */
  @org.junit.Test
  public void baseURI22() {
    final XQuery query = new XQuery(
      "declare base-uri \" http://www.example.org/examples\"; fn:string(fn:static-base-uri())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.org/examples")
    );
  }

  /**
   * Verify normalization of xs:anyURI (trailing spaces). Used with static-base--uri function. .
   */
  @org.junit.Test
  public void baseURI23() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.org/examples \"; fn:string(fn:static-base-uri())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.org/examples")
    );
  }

  /**
   * Verify normalization of xs:anyURI (whitespaces in the middle). Used with static-base--uri function. .
   */
  @org.junit.Test
  public void baseURI24() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.org/ examples\"; fn:string(fn:static-base-uri())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "http://www.example.org/ examples")
      ||
        error("XQST0046")
      )
    );
  }

  /**
   *  Verify that a base-uri declaration containing "abc&gt;" is a valid base-uri declaration. .
   */
  @org.junit.Test
  public void baseURI3() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com/abc&gt;\"; \"aaa\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "aaa")
      ||
        error("XQST0046")
      )
    );
  }

  /**
   *  Verify that a base-uri declaration containing "abc&amp;" is a valid base-uri declaration. .
   */
  @org.junit.Test
  public void baseURI4() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com/abc&amp;\"; \"aaa\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "aaa")
    );
  }

  /**
   *  Verify that a base-uri declaration containing "abc&quot;" is a valid base-uri declaration. .
   */
  @org.junit.Test
  public void baseURI5() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com/abc&quot;\"; \"aaa\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "aaa")
      ||
        error("XQST0046")
      )
    );
  }

  /**
   *  Verify that a base-uri declaration containing "abc&apos;" is a valid base-uri declaration. .
   */
  @org.junit.Test
  public void baseURI6() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com/abc&apos;\"; \"aaa\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "aaa")
    );
  }

  /**
   *  Evaluates base-uri with the fn:resolve-uri function. Typical usage .
   */
  @org.junit.Test
  public void baseURI7() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com/\"; fn:string(fn:resolve-uri(\"examples\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  Evaluates base-uri with the fn:resolve-uri function. Base URI not initialized .
   */
  @org.junit.Test
  public void baseURI8() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace eg = \"http://example.org\"; \n" +
      "        declare function eg:noContextFunction() { \n" +
      "            if (fn:static-base-uri() eq fn:resolve-uri(\"examples\")) then \"true\" else \"true\" \n" +
      "        }; \n" +
      "        eg:noContextFunction()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "true")
      ||
        error("FONS0005")
      )
    );
  }

  /**
   *  Evaluates base-uri with the fn:static-base-uri function. Typical usage .
   */
  @org.junit.Test
  public void baseURI9() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://example.org\"; fn:string(fn:static-base-uri())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://example.org")
    );
  }

  /**
   * Verify normalization of xs:anyURI (encoded whitespaces in the middle). Used with static-base--uri function. .
   */
  @org.junit.Test
  public void baseUri25() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.org/%20%20examples\"; fn:string(fn:static-base-uri())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.org/%20%20examples")
    );
  }

  /**
   *  Evaluation of base-uri set to a relative value. .
   */
  @org.junit.Test
  public void baseUri26() {
    final XQuery query = new XQuery(
      "declare base-uri \"abc\"; fn:ends-with(fn:string(fn:static-base-uri()),\"abc\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  relative base-uri .
   */
  @org.junit.Test
  public void baseUri27() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com/\"; fn:base-uri(<elem xml:base=\"fluster\"></elem>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/fluster")
    );
  }

  /**
   *  relative base-uri through parent .
   */
  @org.junit.Test
  public void baseUri28() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com/\"; fn:base-uri(exactly-one((<elem xml:base=\"fluster\"><a/></elem>)/a))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/fluster")
    );
  }

  /**
   *  relative base-uri through parent .
   */
  @org.junit.Test
  public void baseUri29() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.com/\"; fn:base-uri(exactly-one((<elem xml:base=\"fluster/\"><a xml:base=\"now\"/></elem>)/a))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/fluster/now")
    );
  }
}
