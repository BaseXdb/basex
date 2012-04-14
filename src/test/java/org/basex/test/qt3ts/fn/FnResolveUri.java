package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the resolve-uri function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnResolveUri extends QT3TestSet {

  /**
   *  A test whose essence is: `resolve-uri()`. .
   */
  @org.junit.Test
  public void kResolveURIFunc1() {
    final XQuery query = new XQuery(
      "resolve-uri()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `resolve-uri("http://www.example.com/", "relative/uri.ext", "wrong param")`. .
   */
  @org.junit.Test
  public void kResolveURIFunc2() {
    final XQuery query = new XQuery(
      "resolve-uri(\"http://www.example.com/\", \"relative/uri.ext\", \"wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `resolve-uri("relative/uri.ext", "http://www.example.com/") eq xs:anyURI("http://www.example.com/relative/uri.ext")`. .
   */
  @org.junit.Test
  public void kResolveURIFunc3() {
    final XQuery query = new XQuery(
      "resolve-uri(\"relative/uri.ext\", \"http://www.example.com/\") eq xs:anyURI(\"http://www.example.com/relative/uri.ext\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `resolve-uri("", "http://www.example.com/") eq xs:anyURI("http://www.example.com/")`. .
   */
  @org.junit.Test
  public void kResolveURIFunc4() {
    final XQuery query = new XQuery(
      "resolve-uri(\"\", \"http://www.example.com/\") eq xs:anyURI(\"http://www.example.com/\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(resolve-uri((), "http://www.example.com/"))`. .
   */
  @org.junit.Test
  public void kResolveURIFunc5() {
    final XQuery query = new XQuery(
      "empty(resolve-uri((), \"http://www.example.com/\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `resolve-uri("http://www.example.com/absolute", "http://www.example.com/") eq xs:anyURI("http://www.example.com/absolute")`. .
   */
  @org.junit.Test
  public void kResolveURIFunc6() {
    final XQuery query = new XQuery(
      "resolve-uri(\"http://www.example.com/absolute\", \"http://www.example.com/\") eq xs:anyURI(\"http://www.example.com/absolute\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to empty sequence Use fn:count to avoid empty file .
   */
  @org.junit.Test
  public void fnResolveUri1() {
    final XQuery query = new XQuery(
      "fn:count(fn:resolve-uri((),\"BaseValue\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI and given as a an argument to fn:lower-case function (Two argument version of function). Use the fn-string function. .
   */
  @org.junit.Test
  public void fnResolveUri10() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(fn:lower-case(\"EXAMPLES\"),fn:lower-case(\"HTTP://www.examples.com/\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.examples.com/examples")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI and given as a an argument to fn:substring function (Two argument version of function). Use the fn-string function. .
   */
  @org.junit.Test
  public void fnResolveUri11() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(\"examples\",fn:substring(\"1234http://www.examples.com/\",5)))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.examples.com/examples")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI and given as a an argument to fn:string-join function (Two argument version of function). Use the fn-string function. .
   */
  @org.junit.Test
  public void fnResolveUri12() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(\"examples\",fn:string-join(('http://www.example','.com/'),'')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI and given as a an argument to fn:concat function (Two argument version of function). Use the fn-string function. .
   */
  @org.junit.Test
  public void fnResolveUri13() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(\"examples\",fn:concat(\"http://www.example\",\".com/\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI and given as a an argument to fn:substring-before function (Two argument version of function). Use the fn:substring-before function. .
   */
  @org.junit.Test
  public void fnResolveUri14() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(\"examples\",fn:substring-before(\"http://www.example.com/123\",\"123\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI and given as a an argument to fn:substring-after function (Two argument version of function). Use the fn:substring-after function. .
   */
  @org.junit.Test
  public void fnResolveUri15() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(\"examples\",fn:substring-after(\"123http://www.example.com/\",\"123\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/examples")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI and given as an argument to fn:string function (two argument version of function). Use the fn-string function. .
   */
  @org.junit.Test
  public void fnResolveUri16() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(fn:string(\"http://www.examples.com/\"),\"\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.examples.com/")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI and given as an argument to fn:upper-case function (two argument version of function). Use the fn-string function. .
   */
  @org.junit.Test
  public void fnResolveUri17() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(fn:upper-case(\"http://www.examples.com\"),\"\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "HTTP://WWW.EXAMPLES.COM")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI and given as an argument to fn:lower-case function (two argument version of function). Use the fn-string function. .
   */
  @org.junit.Test
  public void fnResolveUri18() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(fn:lower-case(\"http://www.examples.com\"),\"\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.examples.com")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI and given as an argument to fn:substring function (two argument version of function). Use the fn-string function. .
   */
  @org.junit.Test
  public void fnResolveUri19() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(fn:substring(\"123http://www.examples.com\",4),\"\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.examples.com")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to zero length string. Use the base-uri property that is set. Use the fn-string function .
   */
  @org.junit.Test
  public void fnResolveUri2() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example/\"; fn:string(fn:resolve-uri(\"\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example/")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI and given as an argument to fn:string-join function (two argument version of function). Use the fn-string function. .
   */
  @org.junit.Test
  public void fnResolveUri20() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(fn:string-join((\"http://www.examples\",\".com\"),''),\"\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.examples.com")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI and given as an argument to fn:concat function (two argument version of function). Use the fn-string function. .
   */
  @org.junit.Test
  public void fnResolveUri21() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(fn:concat(\"http://www.examples\",\".com\"),\"\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.examples.com")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI and given as an argument to fn:substring-before function (two argument version of function). Use the fn-string function. .
   */
  @org.junit.Test
  public void fnResolveUri22() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(fn:substring-before(\"http://www.example.com123\",\"123\"),\"\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI and given as an argument to fn:substring-after function (two argument version of function). Use the fn-string function. .
   */
  @org.junit.Test
  public void fnResolveUri23() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(fn:substring-after(\"123http://www.example.com\",\"123\"),\"\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com")
    );
  }

  /**
   *  Evaluation of resolve-uri function with a relative URI reference for second argument. Expects error FORG0002: see erratum FO.E1 .
   */
  @org.junit.Test
  public void fnResolveUri24() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(\"a.html\",\"b.html\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0002")
    );
  }

  /**
   *  Evaluation of resolve-uri function with an absolute URI for the first argument and a relative URI reference for second argument. Should return first argument unchanged .
   */
  @org.junit.Test
  public void fnResolveUri25() {
    final XQuery query = new XQuery(
      "(: Description: Evaluation of resolve-uri function with an absolute URI for the first argument and a relative URI reference for second argument.:) string(resolve-uri(\"http://www.example.com/a.html\",\"b.html\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/a.html")
    );
  }

  /**
   *  Evaluation of resolve-uri function with an invalid URI value for first argument. Use the fn-string function .
   */
  @org.junit.Test
  public void fnResolveUri3() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(\":\",\"http://www.example.com/\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0002")
    );
  }

  /**
   *  Evaluation of resolve-uri function with an invalid URI value for second argument. Use the fn-string function .
   */
  @org.junit.Test
  public void fnResolveUri4() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(\"examples\",\"http:%%\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0002")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI. Use the fn-string function .
   */
  @org.junit.Test
  public void fnResolveUri5() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(\"http://www.examples.com\",\"\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.examples.com")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI (Two argument version of function). Use the fn-string function. .
   */
  @org.junit.Test
  public void fnResolveUri6() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(\"examples\",\"http://www.examples.com/\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.examples.com/examples")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI and given as a an argument to xs:string function (Two argument version of function). Use the fn-string function. .
   */
  @org.junit.Test
  public void fnResolveUri7() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(\"examples\",xs:string(\"http://www.examples.com/\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.examples.com/examples")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI and given as a an argument to fn:string function (Two argument version of function). Use the fn-string function. .
   */
  @org.junit.Test
  public void fnResolveUri8() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(fn:string(\"examples\"),fn:string(\"http://www.examples.com/\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.examples.com/examples")
    );
  }

  /**
   *  Evaluation of resolve-uri function with relative argument set to an absolute URI and given as a an argument to fn:upper-case function (Two argument version of function). Use the fn-string function. .
   */
  @org.junit.Test
  public void fnResolveUri9() {
    final XQuery query = new XQuery(
      "fn:string(fn:resolve-uri(fn:upper-case(\"examples\"),fn:upper-case(\"http://www.examples.com/\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "HTTP://WWW.EXAMPLES.COM/EXAMPLES")
    );
  }
}
