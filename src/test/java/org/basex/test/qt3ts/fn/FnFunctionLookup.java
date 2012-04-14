package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the fn:function-lookup() function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnFunctionLookup extends QT3TestSet {

  /**
   * Attempts to look up function fn:node-name..
   */
  @org.junit.Test
  public void fnFunctionLookup001() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'node-name'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:node-name..
   */
  @org.junit.Test
  public void fnFunctionLookup002() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'node-name'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:node-name..
   */
  @org.junit.Test
  public void fnFunctionLookup003() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'node-name'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:node-name..
   */
  @org.junit.Test
  public void fnFunctionLookup004() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'node-name'), 1)(/root)",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "root")
    );
  }

  /**
   * Attempts to look up function fn:nilled..
   */
  @org.junit.Test
  public void fnFunctionLookup005() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'nilled'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:nilled..
   */
  @org.junit.Test
  public void fnFunctionLookup006() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'nilled'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:nilled..
   */
  @org.junit.Test
  public void fnFunctionLookup007() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'nilled'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:nilled..
   */
  @org.junit.Test
  public void fnFunctionLookup008() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'nilled'), 1)(/root)",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "false")
    );
  }

  /**
   * Attempts to look up function fn:string..
   */
  @org.junit.Test
  public void fnFunctionLookup009() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'string'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to evaluate the "function-lookup" function with no arguments..
   */
  @org.junit.Test
  public void fnFunctionLookup01() {
    final XQuery query = new XQuery(
      "fn:function-lookup()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to invoke function fn:string..
   */
  @org.junit.Test
  public void fnFunctionLookup010() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'string'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:string..
   */
  @org.junit.Test
  public void fnFunctionLookup011() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'string'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:string..
   */
  @org.junit.Test
  public void fnFunctionLookup012() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'string'), 1)(1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:data..
   */
  @org.junit.Test
  public void fnFunctionLookup013() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'data'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:data..
   */
  @org.junit.Test
  public void fnFunctionLookup014() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'data'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:data..
   */
  @org.junit.Test
  public void fnFunctionLookup015() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'data'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:data..
   */
  @org.junit.Test
  public void fnFunctionLookup016() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'data'), 1)(/root/child[1])",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:base-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup017() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'base-uri'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:base-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup018() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'base-uri'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:base-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup019() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'base-uri'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to reference the "function-lookup" function with arity zero..
   */
  @org.junit.Test
  public void fnFunctionLookup02() {
    final XQuery query = new XQuery(
      "fn:function-lookup#0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to invoke function fn:base-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup020() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'base-uri'), 1)(/)",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.w3.org/fots/fn/function-lookup/function-lookup.xml")
    );
  }

  /**
   * Attempts to look up function fn:document-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup021() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'document-uri'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:document-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup022() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'document-uri'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:document-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup023() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'document-uri'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:document-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup024() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'document-uri'), 1)(/)",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.w3.org/fots/fn/function-lookup/function-lookup.xml")
    );
  }

  /**
   * Attempts to look up function fn:error..
   */
  @org.junit.Test
  public void fnFunctionLookup025() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'error'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:error..
   */
  @org.junit.Test
  public void fnFunctionLookup026() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'error'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOER0000")
    );
  }

  /**
   * Attempts to look up function fn:error..
   */
  @org.junit.Test
  public void fnFunctionLookup027() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'error'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:error..
   */
  @org.junit.Test
  public void fnFunctionLookup028() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'error'), 1)(fn:QName('http://www.w3.org/2005/xqt-errors', 'foo:XXXX0000'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XXXX0000")
    );
  }

  /**
   * Attempts to look up function fn:error..
   */
  @org.junit.Test
  public void fnFunctionLookup029() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'error'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to evaluate the "function-lookup" function with one argument..
   */
  @org.junit.Test
  public void fnFunctionLookup03() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'node-name'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to invoke function fn:error..
   */
  @org.junit.Test
  public void fnFunctionLookup030() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'error'), 2)(fn:QName('http://www.w3.org/2005/xqt-errors', 'foo:XXXX0000'), 'string')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XXXX0000")
    );
  }

  /**
   * Attempts to look up function fn:error..
   */
  @org.junit.Test
  public void fnFunctionLookup031() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'error'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:error..
   */
  @org.junit.Test
  public void fnFunctionLookup032() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'error'), 3)(fn:QName('http://www.w3.org/2005/xqt-errors', 'foo:XXXX0000'), 'string', (1, true()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XXXX0000")
    );
  }

  /**
   * Attempts to look up function fn:trace..
   */
  @org.junit.Test
  public void fnFunctionLookup033() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'trace'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:trace..
   */
  @org.junit.Test
  public void fnFunctionLookup034() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'trace'), 2)(1, 'label')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:abs..
   */
  @org.junit.Test
  public void fnFunctionLookup035() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'abs'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:abs..
   */
  @org.junit.Test
  public void fnFunctionLookup036() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'abs'), 1)(-1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:ceiling..
   */
  @org.junit.Test
  public void fnFunctionLookup037() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'ceiling'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:ceiling..
   */
  @org.junit.Test
  public void fnFunctionLookup038() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'ceiling'), 1)(0.9)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:floor..
   */
  @org.junit.Test
  public void fnFunctionLookup039() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'floor'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to reference the "function-lookup" function with arity one..
   */
  @org.junit.Test
  public void fnFunctionLookup04() {
    final XQuery query = new XQuery(
      "fn:function-lookup#1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to invoke function fn:floor..
   */
  @org.junit.Test
  public void fnFunctionLookup040() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'floor'), 1)(1.1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:round..
   */
  @org.junit.Test
  public void fnFunctionLookup041() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'round'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:round..
   */
  @org.junit.Test
  public void fnFunctionLookup042() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'round'), 1)(1.1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:round..
   */
  @org.junit.Test
  public void fnFunctionLookup043() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'round'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:round..
   */
  @org.junit.Test
  public void fnFunctionLookup044() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'round'), 2)(1.1, 0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:round-half-to-even..
   */
  @org.junit.Test
  public void fnFunctionLookup045() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'round-half-to-even'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:round-half-to-even..
   */
  @org.junit.Test
  public void fnFunctionLookup046() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'round-half-to-even'), 1)(1.1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:round-half-to-even..
   */
  @org.junit.Test
  public void fnFunctionLookup047() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'round-half-to-even'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:round-half-to-even..
   */
  @org.junit.Test
  public void fnFunctionLookup048() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'round-half-to-even'), 2)(1.1, 0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:number..
   */
  @org.junit.Test
  public void fnFunctionLookup049() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'number'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to evaluate the "function-lookup" function with three argument..
   */
  @org.junit.Test
  public void fnFunctionLookup05() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'node-name'), 1, ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to invoke function fn:number..
   */
  @org.junit.Test
  public void fnFunctionLookup050() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'number'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:number..
   */
  @org.junit.Test
  public void fnFunctionLookup051() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'number'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:number..
   */
  @org.junit.Test
  public void fnFunctionLookup052() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'number'), 1)('1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:format-integer..
   */
  @org.junit.Test
  public void fnFunctionLookup053() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-integer'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:format-integer..
   */
  @org.junit.Test
  public void fnFunctionLookup054() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-integer'), 2)(1, '0')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:format-integer..
   */
  @org.junit.Test
  public void fnFunctionLookup055() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-integer'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:format-integer..
   */
  @org.junit.Test
  public void fnFunctionLookup056() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-integer'), 3)(1, '0', 'en')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:format-number..
   */
  @org.junit.Test
  public void fnFunctionLookup057() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-number'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:format-number..
   */
  @org.junit.Test
  public void fnFunctionLookup058() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-number'), 2)(1, '0')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:format-number..
   */
  @org.junit.Test
  public void fnFunctionLookup059() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-number'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to reference the "function-lookup" function with arity three..
   */
  @org.junit.Test
  public void fnFunctionLookup06() {
    final XQuery query = new XQuery(
      "fn:function-lookup#3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to invoke function fn:format-number..
   */
  @org.junit.Test
  public void fnFunctionLookup060() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-number'), 3)(1, '0', ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function math:pi..
   */
  @org.junit.Test
  public void fnFunctionLookup061() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'pi'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function math:pi..
   */
  @org.junit.Test
  public void fnFunctionLookup062() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'pi'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "3.141592653589793")
    );
  }

  /**
   * Attempts to look up function math:exp..
   */
  @org.junit.Test
  public void fnFunctionLookup063() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'exp'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function math:exp..
   */
  @org.junit.Test
  public void fnFunctionLookup064() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'exp'), 1)(1e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2.718281828459045")
    );
  }

  /**
   * Attempts to look up function math:exp10..
   */
  @org.junit.Test
  public void fnFunctionLookup065() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'exp10'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function math:exp10..
   */
  @org.junit.Test
  public void fnFunctionLookup066() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'exp10'), 1)(1e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "10")
    );
  }

  /**
   * Attempts to look up function math:log..
   */
  @org.junit.Test
  public void fnFunctionLookup067() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'log'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function math:log..
   */
  @org.junit.Test
  public void fnFunctionLookup068() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'log'), 1)(1e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Attempts to look up function math:log10..
   */
  @org.junit.Test
  public void fnFunctionLookup069() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'log10'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Evaluates the "function-lookup" function with the argument set as follows: $name = () .
   */
  @org.junit.Test
  public void fnFunctionLookup07() {
    final XQuery query = new XQuery(
      "fn:function-lookup( (), 1 )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Attempts to invoke function math:log10..
   */
  @org.junit.Test
  public void fnFunctionLookup070() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'log10'), 1)(1e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Attempts to look up function math:pow..
   */
  @org.junit.Test
  public void fnFunctionLookup071() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'pow'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function math:pow..
   */
  @org.junit.Test
  public void fnFunctionLookup072() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'pow'), 2)(1e0, 1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function math:sqrt..
   */
  @org.junit.Test
  public void fnFunctionLookup073() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'sqrt'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function math:sqrt..
   */
  @org.junit.Test
  public void fnFunctionLookup074() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'sqrt'), 1)(1e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function math:sin..
   */
  @org.junit.Test
  public void fnFunctionLookup075() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'sin'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function math:sin..
   */
  @org.junit.Test
  public void fnFunctionLookup076() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'sin'), 1)(1e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0.8414709848078965")
    );
  }

  /**
   * Attempts to look up function math:cos..
   */
  @org.junit.Test
  public void fnFunctionLookup077() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'cos'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function math:cos..
   */
  @org.junit.Test
  public void fnFunctionLookup078() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'cos'), 1)(1e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0.5403023058681398")
    );
  }

  /**
   * Attempts to look up function math:tan..
   */
  @org.junit.Test
  public void fnFunctionLookup079() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'tan'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Evaluates the "function-lookup" function with the argument set as follows: $arity = () .
   */
  @org.junit.Test
  public void fnFunctionLookup08() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'node-name'), ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Attempts to invoke function math:tan..
   */
  @org.junit.Test
  public void fnFunctionLookup080() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'tan'), 1)(1e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1.5574077246549023")
    );
  }

  /**
   * Attempts to look up function math:asin..
   */
  @org.junit.Test
  public void fnFunctionLookup081() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'asin'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function math:asin..
   */
  @org.junit.Test
  public void fnFunctionLookup082() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'asin'), 1)(1e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1.5707963267948966")
    );
  }

  /**
   * Attempts to look up function math:acos..
   */
  @org.junit.Test
  public void fnFunctionLookup083() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'acos'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function math:acos..
   */
  @org.junit.Test
  public void fnFunctionLookup084() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'acos'), 1)(1e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Attempts to look up function math:atan..
   */
  @org.junit.Test
  public void fnFunctionLookup085() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'atan'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function math:atan..
   */
  @org.junit.Test
  public void fnFunctionLookup086() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'atan'), 1)(1e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0.7853981633974483")
    );
  }

  /**
   * Attempts to look up function math:atan2..
   */
  @org.junit.Test
  public void fnFunctionLookup087() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'atan2'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function math:atan2..
   */
  @org.junit.Test
  public void fnFunctionLookup088() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions/math', 'atan2'), 2)(1e0, 1e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0.7853981633974483")
    );
  }

  /**
   * Attempts to look up function fn:codepoints-to-string..
   */
  @org.junit.Test
  public void fnFunctionLookup089() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'codepoints-to-string'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to evaluate the "function-lookup" function with invalid arguments..
   */
  @org.junit.Test
  public void fnFunctionLookup09() {
    final XQuery query = new XQuery(
      "function-lookup((fn:QName('http://www.w3.org/2005/xpath-functions', 'node-name'), fn:QName('http://www.w3.org/2005/xpath-functions', 'node-name')), 1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Attempts to invoke function fn:codepoints-to-string..
   */
  @org.junit.Test
  public void fnFunctionLookup090() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'codepoints-to-string'), 1)((65, 66))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "AB")
    );
  }

  /**
   * Attempts to look up function fn:string-to-codepoints..
   */
  @org.junit.Test
  public void fnFunctionLookup091() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'string-to-codepoints'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:string-to-codepoints..
   */
  @org.junit.Test
  public void fnFunctionLookup092() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'string-to-codepoints'), 1)('A')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "65")
    );
  }

  /**
   * Attempts to look up function fn:compare..
   */
  @org.junit.Test
  public void fnFunctionLookup093() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'compare'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:compare..
   */
  @org.junit.Test
  public void fnFunctionLookup094() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'compare'), 2)('string', 'string')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Attempts to look up function fn:compare..
   */
  @org.junit.Test
  public void fnFunctionLookup095() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'compare'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:compare..
   */
  @org.junit.Test
  public void fnFunctionLookup096() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'compare'), 3)('string', 'string', 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Attempts to look up function fn:codepoint-equal..
   */
  @org.junit.Test
  public void fnFunctionLookup097() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'codepoint-equal'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:codepoint-equal..
   */
  @org.junit.Test
  public void fnFunctionLookup098() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'codepoint-equal'), 2)('string', 'string')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to look up function fn:concat..
   */
  @org.junit.Test
  public void fnFunctionLookup099() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'concat'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to evaluate the "function-lookup" function with invalid arguments..
   */
  @org.junit.Test
  public void fnFunctionLookup10() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'node-name'), (1, 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Attempts to invoke function fn:concat..
   */
  @org.junit.Test
  public void fnFunctionLookup100() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'concat'), 3)('a', 'bc', 'def')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "abcdef")
    );
  }

  /**
   * Attempts to look up function fn:string-join..
   */
  @org.junit.Test
  public void fnFunctionLookup101() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'string-join'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:string-join..
   */
  @org.junit.Test
  public void fnFunctionLookup102() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'string-join'), 1)(('abc', 'def'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "abcdef")
    );
  }

  /**
   * Attempts to look up function fn:string-join..
   */
  @org.junit.Test
  public void fnFunctionLookup103() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'string-join'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:string-join..
   */
  @org.junit.Test
  public void fnFunctionLookup104() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'string-join'), 2)(('abc', 'def'), '-')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "abc-def")
    );
  }

  /**
   * Attempts to look up function fn:substring..
   */
  @org.junit.Test
  public void fnFunctionLookup105() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'substring'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:substring..
   */
  @org.junit.Test
  public void fnFunctionLookup106() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'substring'), 2)('string', 2e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "tring")
    );
  }

  /**
   * Attempts to look up function fn:substring..
   */
  @org.junit.Test
  public void fnFunctionLookup107() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'substring'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:substring..
   */
  @org.junit.Test
  public void fnFunctionLookup108() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'substring'), 3)('string', 1e0, 1e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "s")
    );
  }

  /**
   * Attempts to look up function fn:string-length..
   */
  @org.junit.Test
  public void fnFunctionLookup109() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'string-length'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:string-length..
   */
  @org.junit.Test
  public void fnFunctionLookup110() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'string-length'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:string-length..
   */
  @org.junit.Test
  public void fnFunctionLookup111() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'string-length'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:string-length..
   */
  @org.junit.Test
  public void fnFunctionLookup112() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'string-length'), 1)('string')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "6")
    );
  }

  /**
   * Attempts to look up function fn:normalize-space..
   */
  @org.junit.Test
  public void fnFunctionLookup113() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'normalize-space'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:normalize-space..
   */
  @org.junit.Test
  public void fnFunctionLookup114() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'normalize-space'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:normalize-space..
   */
  @org.junit.Test
  public void fnFunctionLookup115() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'normalize-space'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:normalize-space..
   */
  @org.junit.Test
  public void fnFunctionLookup116() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'normalize-space'), 1)(' string ')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "string")
    );
  }

  /**
   * Attempts to look up function fn:normalize-unicode..
   */
  @org.junit.Test
  public void fnFunctionLookup117() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'normalize-unicode'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:normalize-unicode..
   */
  @org.junit.Test
  public void fnFunctionLookup118() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'normalize-unicode'), 1)('string')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "string")
    );
  }

  /**
   * Attempts to look up function fn:normalize-unicode..
   */
  @org.junit.Test
  public void fnFunctionLookup119() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'normalize-unicode'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Tests the type checking of the argument..
   */
  @org.junit.Test
  public void fnFunctionLookup12() {
    final XQuery query = new XQuery(
      "( fn:function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'node-name'),\n" +
      "                                 if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                 then ()\n" +
      "                                 else 1 ),\n" +
      "              fn:function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'node-name'),\n" +
      "                                 if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                 then 1\n" +
      "                                 else () ) )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Attempts to invoke function fn:normalize-unicode..
   */
  @org.junit.Test
  public void fnFunctionLookup120() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'normalize-unicode'), 2)('string', 'NFC')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "string")
    );
  }

  /**
   * Attempts to look up function fn:upper-case..
   */
  @org.junit.Test
  public void fnFunctionLookup121() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'upper-case'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:upper-case..
   */
  @org.junit.Test
  public void fnFunctionLookup122() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'upper-case'), 1)('string')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "STRING")
    );
  }

  /**
   * Attempts to look up function fn:lower-case..
   */
  @org.junit.Test
  public void fnFunctionLookup123() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'lower-case'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:lower-case..
   */
  @org.junit.Test
  public void fnFunctionLookup124() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'lower-case'), 1)('STRING')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "string")
    );
  }

  /**
   * Attempts to look up function fn:translate..
   */
  @org.junit.Test
  public void fnFunctionLookup125() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'translate'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:translate..
   */
  @org.junit.Test
  public void fnFunctionLookup126() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'translate'), 3)('string', 'i', 'o')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "strong")
    );
  }

  /**
   * Attempts to look up function fn:contains..
   */
  @org.junit.Test
  public void fnFunctionLookup127() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'contains'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:contains..
   */
  @org.junit.Test
  public void fnFunctionLookup128() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'contains'), 2)('string', 'rin')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to look up function fn:contains..
   */
  @org.junit.Test
  public void fnFunctionLookup129() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'contains'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:contains..
   */
  @org.junit.Test
  public void fnFunctionLookup130() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'contains'), 3)('string', 'RIN', 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Attempts to look up function fn:starts-with..
   */
  @org.junit.Test
  public void fnFunctionLookup131() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'starts-with'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:starts-with..
   */
  @org.junit.Test
  public void fnFunctionLookup132() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'starts-with'), 2)('string', 'str')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to look up function fn:starts-with..
   */
  @org.junit.Test
  public void fnFunctionLookup133() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'starts-with'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:starts-with..
   */
  @org.junit.Test
  public void fnFunctionLookup134() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'starts-with'), 3)('string', 'ing', 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Attempts to look up function fn:ends-with..
   */
  @org.junit.Test
  public void fnFunctionLookup135() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'ends-with'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:ends-with..
   */
  @org.junit.Test
  public void fnFunctionLookup136() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'ends-with'), 2)('string', 'ing')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to look up function fn:ends-with..
   */
  @org.junit.Test
  public void fnFunctionLookup137() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'ends-with'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:ends-with..
   */
  @org.junit.Test
  public void fnFunctionLookup138() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'ends-with'), 3)('string', 'str', 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Attempts to look up function fn:substring-before..
   */
  @org.junit.Test
  public void fnFunctionLookup139() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'substring-before'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Tests the type checking of the argument..
   */
  @org.junit.Test
  public void fnFunctionLookup14() {
    final XQuery query = new XQuery(
      "( fn:function-lookup((if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                  then fn:QName('http://www.w3.org/2005/xpath-functions', 'node-name')\n" +
      "                                  else ()), 1),\n" +
      "              fn:function-lookup((if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                  then ()\n" +
      "                                  else fn:QName('http://www.w3.org/2005/xpath-functions', 'node-name')), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Attempts to invoke function fn:substring-before..
   */
  @org.junit.Test
  public void fnFunctionLookup140() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'substring-before'), 2)('string', 'ing')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "str")
    );
  }

  /**
   * Attempts to look up function fn:substring-before..
   */
  @org.junit.Test
  public void fnFunctionLookup141() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'substring-before'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:substring-before..
   */
  @org.junit.Test
  public void fnFunctionLookup142() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'substring-before'), 3)('string', 'ing', 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "str")
    );
  }

  /**
   * Attempts to look up function fn:substring-after..
   */
  @org.junit.Test
  public void fnFunctionLookup143() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'substring-after'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:substring-after..
   */
  @org.junit.Test
  public void fnFunctionLookup144() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'substring-after'), 2)('string', 'str')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ing")
    );
  }

  /**
   * Attempts to look up function fn:substring-after..
   */
  @org.junit.Test
  public void fnFunctionLookup145() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'substring-after'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:substring-after..
   */
  @org.junit.Test
  public void fnFunctionLookup146() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'substring-after'), 3)('string', 'str', 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ing")
    );
  }

  /**
   * Attempts to look up function fn:matches..
   */
  @org.junit.Test
  public void fnFunctionLookup147() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'matches'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:matches..
   */
  @org.junit.Test
  public void fnFunctionLookup148() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'matches'), 2)('string', 'string')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to look up function fn:matches..
   */
  @org.junit.Test
  public void fnFunctionLookup149() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'matches'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Tests the return type of the "function-lookup" function..
   */
  @org.junit.Test
  public void fnFunctionLookup15() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'node-name'), 1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertType("function(*)")
    );
  }

  /**
   * Attempts to invoke function fn:matches..
   */
  @org.junit.Test
  public void fnFunctionLookup150() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'matches'), 3)('string', 'STRING', 'i')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to look up function fn:replace..
   */
  @org.junit.Test
  public void fnFunctionLookup151() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'replace'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:replace..
   */
  @org.junit.Test
  public void fnFunctionLookup152() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'replace'), 3)('string', 'i', 'o')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "strong")
    );
  }

  /**
   * Attempts to look up function fn:replace..
   */
  @org.junit.Test
  public void fnFunctionLookup153() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'replace'), 4))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:replace..
   */
  @org.junit.Test
  public void fnFunctionLookup154() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'replace'), 4)('string', 'I', 'o', 'i')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "strong")
    );
  }

  /**
   * Attempts to look up function fn:tokenize..
   */
  @org.junit.Test
  public void fnFunctionLookup155() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'tokenize'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:tokenize..
   */
  @org.junit.Test
  public void fnFunctionLookup156() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'tokenize'), 2)('string', 'i')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "str ng")
    );
  }

  /**
   * Attempts to look up function fn:tokenize..
   */
  @org.junit.Test
  public void fnFunctionLookup157() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'tokenize'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:tokenize..
   */
  @org.junit.Test
  public void fnFunctionLookup158() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'tokenize'), 3)('string', 'i', 'i')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "str ng")
    );
  }

  /**
   * Attempts to look up function fn:analyze-string..
   */
  @org.junit.Test
  public void fnFunctionLookup159() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'analyze-string'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:analyze-string..
   */
  @org.junit.Test
  public void fnFunctionLookup160() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'analyze-string'), 2)('', 'abc')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"/>", true)
    );
  }

  /**
   * Attempts to look up function fn:analyze-string..
   */
  @org.junit.Test
  public void fnFunctionLookup161() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'analyze-string'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:analyze-string..
   */
  @org.junit.Test
  public void fnFunctionLookup162() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'analyze-string'), 3)('', 'abc', 'i')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"/>", true)
    );
  }

  /**
   * Attempts to look up function fn:resolve-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup163() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'resolve-uri'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:resolve-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup164() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'resolve-uri'), 1)('http://www.w3.org/2005/xpath-functions')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:resolve-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup165() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'resolve-uri'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:resolve-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup166() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'resolve-uri'), 2)('/2005/xpath-functions', 'http://www.w3.org/')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.w3.org/2005/xpath-functions")
    );
  }

  /**
   * Attempts to look up function fn:encode-for-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup167() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'encode-for-uri'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:encode-for-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup168() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'encode-for-uri'), 1)(' ')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "%20")
    );
  }

  /**
   * Attempts to look up function fn:iri-to-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup169() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'iri-to-uri'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:iri-to-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup170() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'iri-to-uri'), 1)('http://www.example.com/')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/")
    );
  }

  /**
   * Attempts to look up function fn:escape-html-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup171() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'escape-html-uri'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:escape-html-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup172() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'escape-html-uri'), 1)('http://www.example.com/')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.com/")
    );
  }

  /**
   * Attempts to look up function fn:true..
   */
  @org.junit.Test
  public void fnFunctionLookup173() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'true'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:true..
   */
  @org.junit.Test
  public void fnFunctionLookup174() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'true'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to look up function fn:false..
   */
  @org.junit.Test
  public void fnFunctionLookup175() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'false'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:false..
   */
  @org.junit.Test
  public void fnFunctionLookup176() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'false'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Attempts to look up function fn:boolean..
   */
  @org.junit.Test
  public void fnFunctionLookup177() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'boolean'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:boolean..
   */
  @org.junit.Test
  public void fnFunctionLookup178() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'boolean'), 1)(\"string\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to look up function fn:not..
   */
  @org.junit.Test
  public void fnFunctionLookup179() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'not'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:not..
   */
  @org.junit.Test
  public void fnFunctionLookup180() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'not'), 1)(\"string\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Attempts to look up function fn:years-from-duration..
   */
  @org.junit.Test
  public void fnFunctionLookup181() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'years-from-duration'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:years-from-duration..
   */
  @org.junit.Test
  public void fnFunctionLookup182() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'years-from-duration'), 1)(xs:yearMonthDuration(\"P20Y15M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "21")
    );
  }

  /**
   * Attempts to look up function fn:months-from-duration..
   */
  @org.junit.Test
  public void fnFunctionLookup183() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'months-from-duration'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:months-from-duration..
   */
  @org.junit.Test
  public void fnFunctionLookup184() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'months-from-duration'), 1)(xs:yearMonthDuration(\"P20Y15M\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   * Attempts to look up function fn:days-from-duration..
   */
  @org.junit.Test
  public void fnFunctionLookup185() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'days-from-duration'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:days-from-duration..
   */
  @org.junit.Test
  public void fnFunctionLookup186() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'days-from-duration'), 1)(xs:dayTimeDuration(\"P3DT10H\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   * Attempts to look up function fn:hours-from-duration..
   */
  @org.junit.Test
  public void fnFunctionLookup187() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'hours-from-duration'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:hours-from-duration..
   */
  @org.junit.Test
  public void fnFunctionLookup188() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'hours-from-duration'), 1)(xs:dayTimeDuration(\"P3DT10H\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "10")
    );
  }

  /**
   * Attempts to look up function fn:minutes-from-duration..
   */
  @org.junit.Test
  public void fnFunctionLookup189() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'minutes-from-duration'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:minutes-from-duration..
   */
  @org.junit.Test
  public void fnFunctionLookup190() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'minutes-from-duration'), 1)(xs:dayTimeDuration(\"P3DT10H\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Attempts to look up function fn:seconds-from-duration..
   */
  @org.junit.Test
  public void fnFunctionLookup191() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'seconds-from-duration'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:seconds-from-duration..
   */
  @org.junit.Test
  public void fnFunctionLookup192() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'seconds-from-duration'), 1)(xs:dayTimeDuration(\"P3DT10H12.5S\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "12.5")
    );
  }

  /**
   * Attempts to look up function fn:dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup193() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'dateTime'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup194() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'dateTime'), 2)(xs:date('2012-01-01Z'), xs:time('00:00:00Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2012-01-01T00:00:00Z")
    );
  }

  /**
   * Attempts to look up function fn:year-from-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup195() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'year-from-dateTime'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:year-from-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup196() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'year-from-dateTime'), 1)(xs:dateTime('2012-04-03T02:01:00Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2012")
    );
  }

  /**
   * Attempts to look up function fn:month-from-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup197() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'month-from-dateTime'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:month-from-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup198() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'month-from-dateTime'), 1)(xs:dateTime('2012-04-03T02:01:00Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   * Attempts to look up function fn:day-from-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup199() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'day-from-dateTime'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:day-from-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup200() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'day-from-dateTime'), 1)(xs:dateTime('2012-04-03T02:01:00Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   * Attempts to look up function fn:hours-from-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup201() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'hours-from-dateTime'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:hours-from-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup202() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'hours-from-dateTime'), 1)(xs:dateTime('2012-04-03T02:01:00Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   * Attempts to look up function fn:minutes-from-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup203() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'minutes-from-dateTime'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:minutes-from-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup204() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'minutes-from-dateTime'), 1)(xs:dateTime('2012-04-03T02:01:00Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:seconds-from-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup205() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'seconds-from-dateTime'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:seconds-from-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup206() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'seconds-from-dateTime'), 1)(xs:dateTime('2012-04-03T02:01:00Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Attempts to look up function fn:timezone-from-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup207() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'timezone-from-dateTime'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:timezone-from-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup208() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'timezone-from-dateTime'), 1)(xs:dateTime('2012-01-01T00:00:00Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   * Attempts to look up function fn:year-from-date..
   */
  @org.junit.Test
  public void fnFunctionLookup209() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'year-from-date'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:year-from-date..
   */
  @org.junit.Test
  public void fnFunctionLookup210() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'year-from-date'), 1)(xs:date('2012-02-01Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2012")
    );
  }

  /**
   * Attempts to look up function fn:month-from-date..
   */
  @org.junit.Test
  public void fnFunctionLookup211() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'month-from-date'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:month-from-date..
   */
  @org.junit.Test
  public void fnFunctionLookup212() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'month-from-date'), 1)(xs:date('2012-02-01Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   * Attempts to look up function fn:day-from-date..
   */
  @org.junit.Test
  public void fnFunctionLookup213() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'day-from-date'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:day-from-date..
   */
  @org.junit.Test
  public void fnFunctionLookup214() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'day-from-date'), 1)(xs:date('2012-02-01Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:timezone-from-date..
   */
  @org.junit.Test
  public void fnFunctionLookup215() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'timezone-from-date'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:timezone-from-date..
   */
  @org.junit.Test
  public void fnFunctionLookup216() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'timezone-from-date'), 1)(xs:date('2012-01-01Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   * Attempts to look up function fn:hours-from-time..
   */
  @org.junit.Test
  public void fnFunctionLookup217() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'hours-from-time'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:hours-from-time..
   */
  @org.junit.Test
  public void fnFunctionLookup218() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'hours-from-time'), 1)(xs:time('02:01:00Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   * Attempts to look up function fn:minutes-from-time..
   */
  @org.junit.Test
  public void fnFunctionLookup219() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'minutes-from-time'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:minutes-from-time..
   */
  @org.junit.Test
  public void fnFunctionLookup220() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'minutes-from-time'), 1)(xs:time('02:01:00Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:seconds-from-time..
   */
  @org.junit.Test
  public void fnFunctionLookup221() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'seconds-from-time'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:seconds-from-time..
   */
  @org.junit.Test
  public void fnFunctionLookup222() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'seconds-from-time'), 1)(xs:time('02:01:00Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * Attempts to look up function fn:timezone-from-time..
   */
  @org.junit.Test
  public void fnFunctionLookup223() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'timezone-from-time'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:timezone-from-time..
   */
  @org.junit.Test
  public void fnFunctionLookup224() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'timezone-from-time'), 1)(xs:time('02:01:00Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   * Attempts to look up function fn:adjust-dateTime-to-timezone..
   */
  @org.junit.Test
  public void fnFunctionLookup225() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'adjust-dateTime-to-timezone'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:adjust-dateTime-to-timezone..
   */
  @org.junit.Test
  public void fnFunctionLookup226() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'adjust-dateTime-to-timezone'), 1)(xs:dateTime('2012-01-01T00:00:00Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:adjust-dateTime-to-timezone..
   */
  @org.junit.Test
  public void fnFunctionLookup227() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'adjust-dateTime-to-timezone'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:adjust-dateTime-to-timezone..
   */
  @org.junit.Test
  public void fnFunctionLookup228() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'adjust-dateTime-to-timezone'), 2)(xs:dateTime(\"1970-01-01T00:00:00Z\"),xs:dayTimeDuration(\"-PT10H\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1969-12-31T14:00:00-10:00")
    );
  }

  /**
   * Attempts to look up function fn:adjust-date-to-timezone..
   */
  @org.junit.Test
  public void fnFunctionLookup229() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'adjust-date-to-timezone'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:adjust-date-to-timezone..
   */
  @org.junit.Test
  public void fnFunctionLookup230() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'adjust-date-to-timezone'), 1)(xs:date('2012-01-01Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:adjust-date-to-timezone..
   */
  @org.junit.Test
  public void fnFunctionLookup231() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'adjust-date-to-timezone'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:adjust-date-to-timezone..
   */
  @org.junit.Test
  public void fnFunctionLookup232() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'adjust-date-to-timezone'), 2)(xs:date(\"1970-01-01Z\"),xs:dayTimeDuration(\"-PT10H\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1969-12-31-10:00")
    );
  }

  /**
   * Attempts to look up function fn:adjust-time-to-timezone..
   */
  @org.junit.Test
  public void fnFunctionLookup233() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'adjust-time-to-timezone'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:adjust-time-to-timezone..
   */
  @org.junit.Test
  public void fnFunctionLookup234() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'adjust-time-to-timezone'), 1)(xs:time('00:00:00Z'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:adjust-time-to-timezone..
   */
  @org.junit.Test
  public void fnFunctionLookup235() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'adjust-time-to-timezone'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:adjust-time-to-timezone..
   */
  @org.junit.Test
  public void fnFunctionLookup236() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'adjust-time-to-timezone'), 2)(xs:time(\"00:00:00Z\"),xs:dayTimeDuration(\"-PT10H\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "14:00:00-10:00")
    );
  }

  /**
   * Attempts to look up function fn:format-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup237() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-dateTime'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:format-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup238() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-dateTime'), 2)(xs:dateTime('2012-01-01T00:00:00Z'), '[Y]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:format-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup239() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-dateTime'), 5))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:format-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup240() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-dateTime'), 5)(xs:dateTime('2012-01-01T00:00:00Z'), '[Y]', 'en', (), ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2012")
    );
  }

  /**
   * Attempts to look up function fn:format-date..
   */
  @org.junit.Test
  public void fnFunctionLookup241() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-date'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:format-date..
   */
  @org.junit.Test
  public void fnFunctionLookup242() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-date'), 2)(xs:date('2012-01-01Z'), '[Y]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:format-date..
   */
  @org.junit.Test
  public void fnFunctionLookup243() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-date'), 5))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:format-date..
   */
  @org.junit.Test
  public void fnFunctionLookup244() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-date'), 5)(xs:date('2012-01-01Z'), '[Y]', 'en', (), ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2012")
    );
  }

  /**
   * Attempts to look up function fn:format-time..
   */
  @org.junit.Test
  public void fnFunctionLookup245() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-time'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:format-time..
   */
  @org.junit.Test
  public void fnFunctionLookup246() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-time'), 2)(xs:time('00:00:00Z'), '[H01]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:format-time..
   */
  @org.junit.Test
  public void fnFunctionLookup247() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-time'), 5))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:format-time..
   */
  @org.junit.Test
  public void fnFunctionLookup248() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'format-time'), 5)(xs:time('00:00:00Z'), '[H01]', 'en', (), ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "00")
    );
  }

  /**
   * Attempts to look up function fn:resolve-QName..
   */
  @org.junit.Test
  public void fnFunctionLookup249() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'resolve-QName'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:resolve-QName..
   */
  @org.junit.Test
  public void fnFunctionLookup250() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'resolve-QName'), 2)('ns:local', /root/*[2])",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ns:local")
    );
  }

  /**
   * Attempts to look up function fn:QName..
   */
  @org.junit.Test
  public void fnFunctionLookup251() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'QName'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:QName..
   */
  @org.junit.Test
  public void fnFunctionLookup252() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'QName'), 2)('http://www.example.org/', 'ns:local')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ns:local")
    );
  }

  /**
   * Attempts to look up function fn:prefix-from-QName..
   */
  @org.junit.Test
  public void fnFunctionLookup253() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'prefix-from-QName'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:prefix-from-QName..
   */
  @org.junit.Test
  public void fnFunctionLookup254() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'prefix-from-QName'), 1)(fn:QName('http://www.example.org', 'foo:bar'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "foo")
    );
  }

  /**
   * Attempts to look up function fn:local-name-from-QName..
   */
  @org.junit.Test
  public void fnFunctionLookup255() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'local-name-from-QName'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:local-name-from-QName..
   */
  @org.junit.Test
  public void fnFunctionLookup256() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'local-name-from-QName'), 1)(fn:QName('http://www.example.org', 'foo:bar'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "bar")
    );
  }

  /**
   * Attempts to look up function fn:namespace-uri-from-QName..
   */
  @org.junit.Test
  public void fnFunctionLookup257() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'namespace-uri-from-QName'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:namespace-uri-from-QName..
   */
  @org.junit.Test
  public void fnFunctionLookup258() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'namespace-uri-from-QName'), 1)(fn:QName('http://www.example.org', 'foo:bar'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.org")
    );
  }

  /**
   * Attempts to look up function fn:namespace-uri-for-prefix..
   */
  @org.junit.Test
  public void fnFunctionLookup259() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'namespace-uri-for-prefix'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:namespace-uri-for-prefix..
   */
  @org.junit.Test
  public void fnFunctionLookup260() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'namespace-uri-for-prefix'), 2)('ns', /root/*[2])",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.org/")
    );
  }

  /**
   * Attempts to look up function fn:in-scope-prefixes..
   */
  @org.junit.Test
  public void fnFunctionLookup261() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'in-scope-prefixes'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:in-scope-prefixes..
   */
  @org.junit.Test
  public void fnFunctionLookup262() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'in-scope-prefixes'), 1)(/root)",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "xml")
    );
  }

  /**
   * Attempts to look up function fn:name..
   */
  @org.junit.Test
  public void fnFunctionLookup263() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'name'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:name..
   */
  @org.junit.Test
  public void fnFunctionLookup264() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'name'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:name..
   */
  @org.junit.Test
  public void fnFunctionLookup265() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'name'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:name..
   */
  @org.junit.Test
  public void fnFunctionLookup266() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'name'), 1)(/root)",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "root")
    );
  }

  /**
   * Attempts to look up function fn:local-name..
   */
  @org.junit.Test
  public void fnFunctionLookup267() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'local-name'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:local-name..
   */
  @org.junit.Test
  public void fnFunctionLookup268() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'local-name'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:local-name..
   */
  @org.junit.Test
  public void fnFunctionLookup269() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'local-name'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:local-name..
   */
  @org.junit.Test
  public void fnFunctionLookup270() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'local-name'), 1)(/root)",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "root")
    );
  }

  /**
   * Attempts to look up function fn:namespace-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup271() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'namespace-uri'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:namespace-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup272() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'namespace-uri'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:namespace-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup273() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'namespace-uri'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:namespace-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup274() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'namespace-uri'), 1)(/root/*[2])",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.org/")
    );
  }

  /**
   * Attempts to look up function fn:lang..
   */
  @org.junit.Test
  public void fnFunctionLookup275() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'lang'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:lang..
   */
  @org.junit.Test
  public void fnFunctionLookup276() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'lang'), 1)('en')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:lang..
   */
  @org.junit.Test
  public void fnFunctionLookup277() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'lang'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:lang..
   */
  @org.junit.Test
  public void fnFunctionLookup278() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'lang'), 2)('en', /root)",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Attempts to look up function fn:root..
   */
  @org.junit.Test
  public void fnFunctionLookup279() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'root'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:root..
   */
  @org.junit.Test
  public void fnFunctionLookup280() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'root'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:root..
   */
  @org.junit.Test
  public void fnFunctionLookup281() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'root'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:root..
   */
  @org.junit.Test
  public void fnFunctionLookup282() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'root'), 1)(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Attempts to look up function fn:path..
   */
  @org.junit.Test
  public void fnFunctionLookup283() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'path'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:path..
   */
  @org.junit.Test
  public void fnFunctionLookup284() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'path'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:path..
   */
  @org.junit.Test
  public void fnFunctionLookup285() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'path'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:path..
   */
  @org.junit.Test
  public void fnFunctionLookup286() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'path'), 1)(/)",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "/")
    );
  }

  /**
   * Attempts to look up function fn:has-children..
   */
  @org.junit.Test
  public void fnFunctionLookup287() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'has-children'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:has-children..
   */
  @org.junit.Test
  public void fnFunctionLookup288() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'has-children'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:has-children..
   */
  @org.junit.Test
  public void fnFunctionLookup289() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'has-children'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:has-children..
   */
  @org.junit.Test
  public void fnFunctionLookup290() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'has-children'), 1)(/)",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to look up function fn:innermost..
   */
  @org.junit.Test
  public void fnFunctionLookup291() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'innermost'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:innermost..
   */
  @org.junit.Test
  public void fnFunctionLookup292() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'innermost'), 1)(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Attempts to look up function fn:outermost..
   */
  @org.junit.Test
  public void fnFunctionLookup293() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'outermost'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:outermost..
   */
  @org.junit.Test
  public void fnFunctionLookup294() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'outermost'), 1)(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Attempts to look up function fn:empty..
   */
  @org.junit.Test
  public void fnFunctionLookup295() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'empty'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:empty..
   */
  @org.junit.Test
  public void fnFunctionLookup296() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'empty'), 1)((1, true()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Attempts to look up function fn:exists..
   */
  @org.junit.Test
  public void fnFunctionLookup297() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'exists'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:exists..
   */
  @org.junit.Test
  public void fnFunctionLookup298() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'exists'), 1)((1, true()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to look up function fn:head..
   */
  @org.junit.Test
  public void fnFunctionLookup299() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'head'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:head..
   */
  @org.junit.Test
  public void fnFunctionLookup300() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'head'), 1)((1, true()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:tail..
   */
  @org.junit.Test
  public void fnFunctionLookup301() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'tail'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:tail..
   */
  @org.junit.Test
  public void fnFunctionLookup302() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'tail'), 1)((1, true()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "true")
    );
  }

  /**
   * Attempts to look up function fn:insert-before..
   */
  @org.junit.Test
  public void fnFunctionLookup303() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'insert-before'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:insert-before..
   */
  @org.junit.Test
  public void fnFunctionLookup304() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'insert-before'), 3)((1, 2, 3), 2, ('a', 'b', 'c'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1 a b c 2 3")
    );
  }

  /**
   * Attempts to look up function fn:remove..
   */
  @org.junit.Test
  public void fnFunctionLookup305() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'remove'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:remove..
   */
  @org.junit.Test
  public void fnFunctionLookup306() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'remove'), 2)(('a', 'b', 'c'), 2)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "a c")
    );
  }

  /**
   * Attempts to look up function fn:reverse..
   */
  @org.junit.Test
  public void fnFunctionLookup307() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'reverse'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:reverse..
   */
  @org.junit.Test
  public void fnFunctionLookup308() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'reverse'), 1)(1 to 3)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "3 2 1")
    );
  }

  /**
   * Attempts to look up function fn:subsequence..
   */
  @org.junit.Test
  public void fnFunctionLookup309() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'subsequence'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:subsequence..
   */
  @org.junit.Test
  public void fnFunctionLookup310() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'subsequence'), 2)((1, true()), 2e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "true")
    );
  }

  /**
   * Attempts to look up function fn:subsequence..
   */
  @org.junit.Test
  public void fnFunctionLookup311() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'subsequence'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:subsequence..
   */
  @org.junit.Test
  public void fnFunctionLookup312() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'subsequence'), 3)((1, true()), 1e0, 1e0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:unordered..
   */
  @org.junit.Test
  public void fnFunctionLookup313() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'unordered'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:unordered..
   */
  @org.junit.Test
  public void fnFunctionLookup314() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'unordered'), 1)(1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:distinct-values..
   */
  @org.junit.Test
  public void fnFunctionLookup315() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'distinct-values'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:distinct-values..
   */
  @org.junit.Test
  public void fnFunctionLookup316() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'distinct-values'), 1)((1, 'string'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:distinct-values..
   */
  @org.junit.Test
  public void fnFunctionLookup317() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'distinct-values'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:distinct-values..
   */
  @org.junit.Test
  public void fnFunctionLookup318() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'distinct-values'), 2)((1, 'string'), 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:index-of..
   */
  @org.junit.Test
  public void fnFunctionLookup319() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'index-of'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:index-of..
   */
  @org.junit.Test
  public void fnFunctionLookup320() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'index-of'), 2)((1, 'string'), 'string')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:index-of..
   */
  @org.junit.Test
  public void fnFunctionLookup321() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'index-of'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:index-of..
   */
  @org.junit.Test
  public void fnFunctionLookup322() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'index-of'), 3)((1, 'string'), 'string', 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:deep-equal..
   */
  @org.junit.Test
  public void fnFunctionLookup323() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'deep-equal'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:deep-equal..
   */
  @org.junit.Test
  public void fnFunctionLookup324() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'deep-equal'), 2)((1, true()), (1, true()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:deep-equal..
   */
  @org.junit.Test
  public void fnFunctionLookup325() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'deep-equal'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:deep-equal..
   */
  @org.junit.Test
  public void fnFunctionLookup326() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'deep-equal'), 3)((1, true()), (1, true()), 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:zero-or-one..
   */
  @org.junit.Test
  public void fnFunctionLookup327() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'zero-or-one'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:zero-or-one..
   */
  @org.junit.Test
  public void fnFunctionLookup328() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'zero-or-one'), 1)(1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:one-or-more..
   */
  @org.junit.Test
  public void fnFunctionLookup329() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'one-or-more'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:one-or-more..
   */
  @org.junit.Test
  public void fnFunctionLookup330() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'one-or-more'), 1)(1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:exactly-one..
   */
  @org.junit.Test
  public void fnFunctionLookup331() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'exactly-one'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:exactly-one..
   */
  @org.junit.Test
  public void fnFunctionLookup332() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'exactly-one'), 1)(1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:count..
   */
  @org.junit.Test
  public void fnFunctionLookup333() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'count'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:count..
   */
  @org.junit.Test
  public void fnFunctionLookup334() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'count'), 1)((1, true()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   * Attempts to look up function fn:avg..
   */
  @org.junit.Test
  public void fnFunctionLookup335() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'avg'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:avg..
   */
  @org.junit.Test
  public void fnFunctionLookup336() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'avg'), 1)((1, 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   * Attempts to look up function fn:max..
   */
  @org.junit.Test
  public void fnFunctionLookup337() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'max'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:max..
   */
  @org.junit.Test
  public void fnFunctionLookup338() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'max'), 1)((1, 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:max..
   */
  @org.junit.Test
  public void fnFunctionLookup339() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'max'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:max..
   */
  @org.junit.Test
  public void fnFunctionLookup340() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'max'), 2)((1, 3), 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:min..
   */
  @org.junit.Test
  public void fnFunctionLookup341() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'min'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:min..
   */
  @org.junit.Test
  public void fnFunctionLookup342() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'min'), 1)((1, 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:min..
   */
  @org.junit.Test
  public void fnFunctionLookup343() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'min'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:min..
   */
  @org.junit.Test
  public void fnFunctionLookup344() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'min'), 2)((1, 3), 'http://www.w3.org/2005/xpath-functions/collation/codepoint')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:sum..
   */
  @org.junit.Test
  public void fnFunctionLookup345() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'sum'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:sum..
   */
  @org.junit.Test
  public void fnFunctionLookup346() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'sum'), 1)((1, 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   * Attempts to look up function fn:sum..
   */
  @org.junit.Test
  public void fnFunctionLookup347() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'sum'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:sum..
   */
  @org.junit.Test
  public void fnFunctionLookup348() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'sum'), 2)((1, 2), 0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   * Attempts to look up function fn:id..
   */
  @org.junit.Test
  public void fnFunctionLookup349() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'id'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:id..
   */
  @org.junit.Test
  public void fnFunctionLookup350() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'id'), 1)(('id1', 'id2'))",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:id..
   */
  @org.junit.Test
  public void fnFunctionLookup351() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'id'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:id..
   */
  @org.junit.Test
  public void fnFunctionLookup352() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'id'), 2)(('id1', 'id2'), /)",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Attempts to look up function fn:element-with-id..
   */
  @org.junit.Test
  public void fnFunctionLookup353() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'element-with-id'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:element-with-id..
   */
  @org.junit.Test
  public void fnFunctionLookup354() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'element-with-id'), 1)(('id1', 'id2'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:element-with-id..
   */
  @org.junit.Test
  public void fnFunctionLookup355() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'element-with-id'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:element-with-id..
   */
  @org.junit.Test
  public void fnFunctionLookup356() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'element-with-id'), 2)(('id1', 'id2'), /)",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Attempts to look up function fn:idref..
   */
  @org.junit.Test
  public void fnFunctionLookup357() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'idref'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:idref..
   */
  @org.junit.Test
  public void fnFunctionLookup358() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'idref'), 1)(('id1', 'id2'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:idref..
   */
  @org.junit.Test
  public void fnFunctionLookup359() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'idref'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:idref..
   */
  @org.junit.Test
  public void fnFunctionLookup360() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'idref'), 2)(('id1', 'id2'), /)",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Attempts to look up function fn:generate-id..
   */
  @org.junit.Test
  public void fnFunctionLookup361() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'generate-id'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:generate-id..
   */
  @org.junit.Test
  public void fnFunctionLookup362() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'generate-id'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:generate-id..
   */
  @org.junit.Test
  public void fnFunctionLookup363() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'generate-id'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:generate-id..
   */
  @org.junit.Test
  public void fnFunctionLookup364() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'generate-id'), 1)(())",
      ctx);
    query.context(node(file("fn/function-lookup/function-lookup.xml")));
    // resource: http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertType("xs:string")
      &&
        assertEq("\"\"")
      )
    );
  }

  /**
   * Attempts to look up function fn:doc..
   */
  @org.junit.Test
  public void fnFunctionLookup365() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'doc'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:doc..
   */
  @org.junit.Test
  public void fnFunctionLookup366() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'doc'), 1)('string')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:doc-available..
   */
  @org.junit.Test
  public void fnFunctionLookup367() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'doc-available'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:doc-available..
   */
  @org.junit.Test
  public void fnFunctionLookup368() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'doc-available'), 1)('string')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:collection..
   */
  @org.junit.Test
  public void fnFunctionLookup369() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'collection'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:collection..
   */
  @org.junit.Test
  public void fnFunctionLookup370() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'collection'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:collection..
   */
  @org.junit.Test
  public void fnFunctionLookup371() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'collection'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:collection..
   */
  @org.junit.Test
  public void fnFunctionLookup372() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'collection'), 1)('string')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:uri-collection..
   */
  @org.junit.Test
  public void fnFunctionLookup373() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'uri-collection'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:uri-collection..
   */
  @org.junit.Test
  public void fnFunctionLookup374() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'uri-collection'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:uri-collection..
   */
  @org.junit.Test
  public void fnFunctionLookup375() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'uri-collection'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:uri-collection..
   */
  @org.junit.Test
  public void fnFunctionLookup376() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'uri-collection'), 1)('string')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:unparsed-text..
   */
  @org.junit.Test
  public void fnFunctionLookup377() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'unparsed-text'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:unparsed-text..
   */
  @org.junit.Test
  public void fnFunctionLookup378() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'unparsed-text'), 1)('http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:unparsed-text..
   */
  @org.junit.Test
  public void fnFunctionLookup379() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'unparsed-text'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:unparsed-text..
   */
  @org.junit.Test
  public void fnFunctionLookup380() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'unparsed-text'), 2)('http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt', 'utf-8')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:unparsed-text-lines..
   */
  @org.junit.Test
  public void fnFunctionLookup381() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'unparsed-text-lines'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:unparsed-text-lines..
   */
  @org.junit.Test
  public void fnFunctionLookup382() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'unparsed-text-lines'), 1)('http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:unparsed-text-lines..
   */
  @org.junit.Test
  public void fnFunctionLookup383() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'unparsed-text-lines'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:unparsed-text-lines..
   */
  @org.junit.Test
  public void fnFunctionLookup384() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'unparsed-text-lines'), 2)('http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt', 'utf-8')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:unparsed-text-available..
   */
  @org.junit.Test
  public void fnFunctionLookup385() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'unparsed-text-available'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:unparsed-text-available..
   */
  @org.junit.Test
  public void fnFunctionLookup386() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'unparsed-text-available'), 1)('http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:unparsed-text-available..
   */
  @org.junit.Test
  public void fnFunctionLookup387() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'unparsed-text-available'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:unparsed-text-available..
   */
  @org.junit.Test
  public void fnFunctionLookup388() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'unparsed-text-available'), 2)('http://www.w3.org/fots/unparsed-text/text-plain-utf-8.txt', 'utf-8')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:environment-variable..
   */
  @org.junit.Test
  public void fnFunctionLookup389() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'environment-variable'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:environment-variable..
   */
  @org.junit.Test
  public void fnFunctionLookup390() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'environment-variable'), 1)('string')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:available-environment-variables..
   */
  @org.junit.Test
  public void fnFunctionLookup391() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'available-environment-variables'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:available-environment-variables..
   */
  @org.junit.Test
  public void fnFunctionLookup392() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'available-environment-variables'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:parse-xml..
   */
  @org.junit.Test
  public void fnFunctionLookup393() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'parse-xml'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:parse-xml..
   */
  @org.junit.Test
  public void fnFunctionLookup394() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'parse-xml'), 1)('<doc />')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:parse-xml-fragment..
   */
  @org.junit.Test
  public void fnFunctionLookup395() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'parse-xml-fragment'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:parse-xml-fragment..
   */
  @org.junit.Test
  public void fnFunctionLookup396() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'parse-xml-fragment'), 1)('<doc />')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:serialize..
   */
  @org.junit.Test
  public void fnFunctionLookup397() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'serialize'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:serialize..
   */
  @org.junit.Test
  public void fnFunctionLookup398() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'serialize'), 1)((1, true()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1 true")
    );
  }

  /**
   * Attempts to look up function fn:serialize..
   */
  @org.junit.Test
  public void fnFunctionLookup399() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'serialize'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:serialize..
   */
  @org.junit.Test
  public void fnFunctionLookup400() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'serialize'), 2)((1, false()), ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1 false")
    );
  }

  /**
   * Attempts to look up function fn:position..
   */
  @org.junit.Test
  public void fnFunctionLookup401() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'position'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:position..
   */
  @org.junit.Test
  public void fnFunctionLookup402() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'position'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:last..
   */
  @org.junit.Test
  public void fnFunctionLookup403() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'last'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:last..
   */
  @org.junit.Test
  public void fnFunctionLookup404() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'last'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:current-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup405() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'current-dateTime'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:current-dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup406() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'current-dateTime'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:current-date..
   */
  @org.junit.Test
  public void fnFunctionLookup407() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'current-date'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:current-date..
   */
  @org.junit.Test
  public void fnFunctionLookup408() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'current-date'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:current-time..
   */
  @org.junit.Test
  public void fnFunctionLookup409() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'current-time'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:current-time..
   */
  @org.junit.Test
  public void fnFunctionLookup410() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'current-time'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:implicit-timezone..
   */
  @org.junit.Test
  public void fnFunctionLookup411() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'implicit-timezone'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:implicit-timezone..
   */
  @org.junit.Test
  public void fnFunctionLookup412() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'implicit-timezone'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:default-collation..
   */
  @org.junit.Test
  public void fnFunctionLookup413() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'default-collation'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:default-collation..
   */
  @org.junit.Test
  public void fnFunctionLookup414() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'default-collation'), 0)()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.w3.org/2005/xpath-functions/collation/codepoint")
    );
  }

  /**
   * Attempts to look up function fn:static-base-uri..
   */
  @org.junit.Test
  public void fnFunctionLookup415() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'static-base-uri'), 0))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:static-base-uri.  Note that this actually returns a property of the dynamic context!.
   */
  @org.junit.Test
  public void fnFunctionLookup416() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'static-base-uri'), 0)()",
      ctx);
    query.baseURI("http://www.example.com");

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:function-lookup..
   */
  @org.junit.Test
  public void fnFunctionLookup417() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'function-lookup'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:function-lookup..
   */
  @org.junit.Test
  public void fnFunctionLookup418() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'function-lookup'), 2)(fn:QName('http://www.example.org', 'foo:bar'), 1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0112")
    );
  }

  /**
   * Attempts to look up function fn:function-name..
   */
  @org.junit.Test
  public void fnFunctionLookup419() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'function-name'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:function-name..
   */
  @org.junit.Test
  public void fnFunctionLookup420() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'function-name'), 1)(fn:abs#1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("fn:QName('http://www.w3.org/2005/xpath-functions', 'fn:abs')")
    );
  }

  /**
   * Attempts to look up function fn:function-arity..
   */
  @org.junit.Test
  public void fnFunctionLookup421() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'function-arity'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:function-arity..
   */
  @org.junit.Test
  public void fnFunctionLookup422() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'function-arity'), 1)(fn:abs#1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function fn:map..
   */
  @org.junit.Test
  public void fnFunctionLookup423() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'map'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:map..
   */
  @org.junit.Test
  public void fnFunctionLookup424() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'map'), 2)(xs:int#1, (\"23\", \"29\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("(23, 29)")
    );
  }

  /**
   * Attempts to look up function fn:filter..
   */
  @org.junit.Test
  public void fnFunctionLookup425() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'filter'), 2))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:filter..
   */
  @org.junit.Test
  public void fnFunctionLookup426() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'filter'), 2)(function($a) {$a mod 2 = 0}, 1 to 10)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("(2, 4, 6, 8, 10)")
    );
  }

  /**
   * Attempts to look up function fn:fold-left..
   */
  @org.junit.Test
  public void fnFunctionLookup427() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'fold-left'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:fold-left..
   */
  @org.junit.Test
  public void fnFunctionLookup428() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'fold-left'), 3)(fn:concat(?, \".\", ?), \"\", 1 to 5)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, ".1.2.3.4.5")
    );
  }

  /**
   * Attempts to look up function fn:fold-right..
   */
  @org.junit.Test
  public void fnFunctionLookup429() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'fold-right'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:fold-right..
   */
  @org.junit.Test
  public void fnFunctionLookup430() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'fold-right'), 3)(fn:concat(?, \".\", ?), \"\", 1 to 5)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1.2.3.4.5.")
    );
  }

  /**
   * Attempts to look up function fn:map-pairs..
   */
  @org.junit.Test
  public void fnFunctionLookup431() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'map-pairs'), 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function fn:map-pairs..
   */
  @org.junit.Test
  public void fnFunctionLookup432() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xpath-functions', 'map-pairs'), 3)(concat#2, (\"a\", \"b\", \"c\"), (\"x\", \"y\", \"z\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("(\"ax\", \"by\", \"cz\")")
    );
  }

  /**
   * Attempts to look up function xs:untypedAtomic..
   */
  @org.junit.Test
  public void fnFunctionLookup433() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'untypedAtomic'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:untypedAtomic..
   */
  @org.junit.Test
  public void fnFunctionLookup434() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'untypedAtomic'), 1)('string')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "string")
    );
  }

  /**
   * Attempts to look up function xs:dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup435() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'dateTime'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:dateTime..
   */
  @org.junit.Test
  public void fnFunctionLookup436() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'dateTime'), 1)('1970-01-02T04:05:06Z')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1970-01-02T04:05:06Z")
    );
  }

  /**
   * Attempts to look up function xs:date..
   */
  @org.junit.Test
  public void fnFunctionLookup437() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'date'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:date..
   */
  @org.junit.Test
  public void fnFunctionLookup438() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'date'), 1)('1970-01-02Z')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1970-01-02Z")
    );
  }

  /**
   * Attempts to look up function xs:time..
   */
  @org.junit.Test
  public void fnFunctionLookup439() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'time'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:time..
   */
  @org.junit.Test
  public void fnFunctionLookup440() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'time'), 1)('01:02:03Z')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "01:02:03Z")
    );
  }

  /**
   * Attempts to look up function xs:duration..
   */
  @org.junit.Test
  public void fnFunctionLookup441() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'duration'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:duration..
   */
  @org.junit.Test
  public void fnFunctionLookup442() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'duration'), 1)('P5Y2M10DT15H')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "P5Y2M10DT15H")
    );
  }

  /**
   * Attempts to look up function xs:yearMonthDuration..
   */
  @org.junit.Test
  public void fnFunctionLookup443() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'yearMonthDuration'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:yearMonthDuration..
   */
  @org.junit.Test
  public void fnFunctionLookup444() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'yearMonthDuration'), 1)('P1Y')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "P1Y")
    );
  }

  /**
   * Attempts to look up function xs:dayTimeDuration..
   */
  @org.junit.Test
  public void fnFunctionLookup445() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'dayTimeDuration'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:dayTimeDuration..
   */
  @org.junit.Test
  public void fnFunctionLookup446() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'dayTimeDuration'), 1)('PT15H')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "PT15H")
    );
  }

  /**
   * Attempts to look up function xs:float..
   */
  @org.junit.Test
  public void fnFunctionLookup447() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'float'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:float..
   */
  @org.junit.Test
  public void fnFunctionLookup448() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'float'), 1)('1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:double..
   */
  @org.junit.Test
  public void fnFunctionLookup449() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'double'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:double..
   */
  @org.junit.Test
  public void fnFunctionLookup450() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'double'), 1)('1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:decimal..
   */
  @org.junit.Test
  public void fnFunctionLookup451() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'decimal'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:decimal..
   */
  @org.junit.Test
  public void fnFunctionLookup452() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'decimal'), 1)('1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:integer..
   */
  @org.junit.Test
  public void fnFunctionLookup453() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'integer'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:integer..
   */
  @org.junit.Test
  public void fnFunctionLookup454() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'integer'), 1)('1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:nonPositiveInteger..
   */
  @org.junit.Test
  public void fnFunctionLookup455() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'nonPositiveInteger'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:nonPositiveInteger..
   */
  @org.junit.Test
  public void fnFunctionLookup456() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'nonPositiveInteger'), 1)('-1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-1")
    );
  }

  /**
   * Attempts to look up function xs:negativeInteger..
   */
  @org.junit.Test
  public void fnFunctionLookup457() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'negativeInteger'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:negativeInteger..
   */
  @org.junit.Test
  public void fnFunctionLookup458() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'negativeInteger'), 1)('-1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-1")
    );
  }

  /**
   * Attempts to look up function xs:long..
   */
  @org.junit.Test
  public void fnFunctionLookup459() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'long'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:long..
   */
  @org.junit.Test
  public void fnFunctionLookup460() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'long'), 1)('1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:int..
   */
  @org.junit.Test
  public void fnFunctionLookup461() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'int'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:int..
   */
  @org.junit.Test
  public void fnFunctionLookup462() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'int'), 1)('1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:short..
   */
  @org.junit.Test
  public void fnFunctionLookup463() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'short'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:short..
   */
  @org.junit.Test
  public void fnFunctionLookup464() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'short'), 1)('1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:byte..
   */
  @org.junit.Test
  public void fnFunctionLookup465() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'byte'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:byte..
   */
  @org.junit.Test
  public void fnFunctionLookup466() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'byte'), 1)('1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:nonNegativeInteger..
   */
  @org.junit.Test
  public void fnFunctionLookup467() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'nonNegativeInteger'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:nonNegativeInteger..
   */
  @org.junit.Test
  public void fnFunctionLookup468() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'nonNegativeInteger'), 1)('1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:unsignedLong..
   */
  @org.junit.Test
  public void fnFunctionLookup469() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'unsignedLong'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:unsignedLong..
   */
  @org.junit.Test
  public void fnFunctionLookup470() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'unsignedLong'), 1)('1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:unsignedInt..
   */
  @org.junit.Test
  public void fnFunctionLookup471() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'unsignedInt'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:unsignedInt..
   */
  @org.junit.Test
  public void fnFunctionLookup472() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'unsignedInt'), 1)('1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:unsignedShort..
   */
  @org.junit.Test
  public void fnFunctionLookup473() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'unsignedShort'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:unsignedShort..
   */
  @org.junit.Test
  public void fnFunctionLookup474() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'unsignedShort'), 1)('1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:unsignedByte..
   */
  @org.junit.Test
  public void fnFunctionLookup475() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'unsignedByte'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:unsignedByte..
   */
  @org.junit.Test
  public void fnFunctionLookup476() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'unsignedByte'), 1)('1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:nonNegativeInteger..
   */
  @org.junit.Test
  public void fnFunctionLookup477() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'nonNegativeInteger'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:nonNegativeInteger..
   */
  @org.junit.Test
  public void fnFunctionLookup478() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'nonNegativeInteger'), 1)('1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:positiveInteger..
   */
  @org.junit.Test
  public void fnFunctionLookup479() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'positiveInteger'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:positiveInteger..
   */
  @org.junit.Test
  public void fnFunctionLookup480() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'positiveInteger'), 1)('1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Attempts to look up function xs:gYearMonth..
   */
  @org.junit.Test
  public void fnFunctionLookup481() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'gYearMonth'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:gYearMonth..
   */
  @org.junit.Test
  public void fnFunctionLookup482() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'gYearMonth'), 1)('2001-10Z')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2001-10Z")
    );
  }

  /**
   * Attempts to look up function xs:gYear..
   */
  @org.junit.Test
  public void fnFunctionLookup483() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'gYear'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:gYear..
   */
  @org.junit.Test
  public void fnFunctionLookup484() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'gYear'), 1)('2012Z')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2012Z")
    );
  }

  /**
   * Attempts to look up function xs:gMonthDay..
   */
  @org.junit.Test
  public void fnFunctionLookup485() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'gMonthDay'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:gMonthDay..
   */
  @org.junit.Test
  public void fnFunctionLookup486() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'gMonthDay'), 1)('--11-01Z')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "--11-01Z")
    );
  }

  /**
   * Attempts to look up function xs:gDay..
   */
  @org.junit.Test
  public void fnFunctionLookup487() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'gDay'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:gDay..
   */
  @org.junit.Test
  public void fnFunctionLookup488() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'gDay'), 1)('---01Z')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "---01Z")
    );
  }

  /**
   * Attempts to look up function xs:gMonth..
   */
  @org.junit.Test
  public void fnFunctionLookup489() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'gMonth'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:gMonth..
   */
  @org.junit.Test
  public void fnFunctionLookup490() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'gMonth'), 1)('--11Z')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "--11Z")
    );
  }

  /**
   * Attempts to look up function xs:string..
   */
  @org.junit.Test
  public void fnFunctionLookup491() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'string'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:string..
   */
  @org.junit.Test
  public void fnFunctionLookup492() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'string'), 1)('string')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "string")
    );
  }

  /**
   * Attempts to look up function xs:normalizeString..
   */
  @org.junit.Test
  public void fnFunctionLookup493() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'normalizedString'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:normalizeString..
   */
  @org.junit.Test
  public void fnFunctionLookup494() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'normalizedString'), 1)('normalized\n" +
      "string')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "normalized string")
    );
  }

  /**
   * Attempts to look up function xs:token..
   */
  @org.junit.Test
  public void fnFunctionLookup495() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'token'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:token..
   */
  @org.junit.Test
  public void fnFunctionLookup496() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'token'), 1)('token')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "token")
    );
  }

  /**
   * Attempts to look up function xs:language..
   */
  @org.junit.Test
  public void fnFunctionLookup497() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'language'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:language..
   */
  @org.junit.Test
  public void fnFunctionLookup498() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'language'), 1)('en')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "en")
    );
  }

  /**
   * Attempts to look up function xs:NMTOKEN..
   */
  @org.junit.Test
  public void fnFunctionLookup499() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'NMTOKEN'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:NMTOKEN..
   */
  @org.junit.Test
  public void fnFunctionLookup500() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'NMTOKEN'), 1)('NMTOKEN')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NMTOKEN")
    );
  }

  /**
   * Attempts to look up function xs:Name..
   */
  @org.junit.Test
  public void fnFunctionLookup501() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'Name'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:Name..
   */
  @org.junit.Test
  public void fnFunctionLookup502() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'Name'), 1)('Name')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Name")
    );
  }

  /**
   * Attempts to look up function xs:NCName..
   */
  @org.junit.Test
  public void fnFunctionLookup503() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'NCName'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:NCName..
   */
  @org.junit.Test
  public void fnFunctionLookup504() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'NCName'), 1)('NCName')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NCName")
    );
  }

  /**
   * Attempts to look up function xs:ID..
   */
  @org.junit.Test
  public void fnFunctionLookup505() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'ID'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:ID..
   */
  @org.junit.Test
  public void fnFunctionLookup506() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'ID'), 1)('ID')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ID")
    );
  }

  /**
   * Attempts to look up function xs:IDREF..
   */
  @org.junit.Test
  public void fnFunctionLookup507() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'IDREF'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:IDREF..
   */
  @org.junit.Test
  public void fnFunctionLookup508() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'IDREF'), 1)('IDREF')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "IDREF")
    );
  }

  /**
   * Attempts to look up function xs:ENTITY..
   */
  @org.junit.Test
  public void fnFunctionLookup509() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'ENTITY'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:ENTITY..
   */
  @org.junit.Test
  public void fnFunctionLookup510() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'ENTITY'), 1)('ENTITY')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ENTITY")
    );
  }

  /**
   * Attempts to look up function xs:boolean..
   */
  @org.junit.Test
  public void fnFunctionLookup511() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'boolean'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:boolean..
   */
  @org.junit.Test
  public void fnFunctionLookup512() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'boolean'), 1)('1')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "true")
    );
  }

  /**
   * Attempts to look up function xs:base64Binary..
   */
  @org.junit.Test
  public void fnFunctionLookup513() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'base64Binary'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:base64Binary..
   */
  @org.junit.Test
  public void fnFunctionLookup514() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'base64Binary'), 1)('D74D35D35D35')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "D74D35D35D35")
    );
  }

  /**
   * Attempts to look up function xs:hexBinary..
   */
  @org.junit.Test
  public void fnFunctionLookup515() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'hexBinary'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:hexBinary..
   */
  @org.junit.Test
  public void fnFunctionLookup516() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'hexBinary'), 1)('0fb7')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "0FB7")
    );
  }

  /**
   * Attempts to look up function xs:anyURI..
   */
  @org.junit.Test
  public void fnFunctionLookup517() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'anyURI'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:anyURI..
   */
  @org.junit.Test
  public void fnFunctionLookup518() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'anyURI'), 1)('http://www.example.org/')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.example.org/")
    );
  }

  /**
   * Attempts to look up function xs:QName..
   */
  @org.junit.Test
  public void fnFunctionLookup519() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'QName'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:QName..
   */
  @org.junit.Test
  public void fnFunctionLookup520() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'QName'), 1)('fn:QName')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "fn:QName")
    );
  }

  /**
   * Attempts to look up function xs:IDREFS..
   */
  @org.junit.Test
  public void fnFunctionLookup523() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'IDREFS'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:IDREFS..
   */
  @org.junit.Test
  public void fnFunctionLookup524() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'IDREFS'), 1)('ID1 ID2 ID3')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ID1 ID2 ID3")
    );
  }

  /**
   * Attempts to look up function xs:NMTOKENS..
   */
  @org.junit.Test
  public void fnFunctionLookup525() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'NMTOKENS'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:NMTOKENS..
   */
  @org.junit.Test
  public void fnFunctionLookup526() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'NMTOKENS'), 1)('NMTOKEN1 NMTOKEN2 NMTOKEN3')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NMTOKEN1 NMTOKEN2 NMTOKEN3")
    );
  }

  /**
   * Attempts to look up function xs:ENTITES..
   */
  @org.junit.Test
  public void fnFunctionLookup527() {
    final XQuery query = new XQuery(
      "exists(function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'ENTITIES'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke function xs:ENTITIES..
   */
  @org.junit.Test
  public void fnFunctionLookup528() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2001/XMLSchema', 'ENTITIES'), 1)('ENTITY1 ENTITY2 ENTITY3')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ENTITY1 ENTITY2 ENTITY3")
    );
  }

  /**
   * Attempts to look up non-existant function local:missing..
   */
  @org.junit.Test
  public void fnFunctionLookup529() {
    final XQuery query = new XQuery(
      "empty(function-lookup(fn:QName('http://www.w3.org/2005/xquery-local-functions', 'missing'), 1))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Attempts to invoke the non-existant function local:missing..
   */
  @org.junit.Test
  public void fnFunctionLookup530() {
    final XQuery query = new XQuery(
      "function-lookup(fn:QName('http://www.w3.org/2005/xquery-local-functions', 'missing'), 1)(\"arg\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Applied to a built-in function.
   */
  @org.junit.Test
  public void functionLookup001() {
    final XQuery query = new XQuery(
      "function-lookup(QName(\"http://www.w3.org/2005/xpath-functions\", \"abs\"), 1)(-3)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("3")
    );
  }

  /**
   * Applied to a built-in function with variable arity.
   */
  @org.junit.Test
  public void functionLookup002() {
    final XQuery query = new XQuery(
      "function-lookup(QName(\"http://www.w3.org/2005/xpath-functions\", \"concat\"), 3)(\"a\", \"b\", \"c\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "abc")
    );
  }

  /**
   * Applied to a built-in function with zero arity.
   */
  @org.junit.Test
  public void functionLookup003() {
    final XQuery query = new XQuery(
      "function-lookup(QName(\"http://www.w3.org/2005/xpath-functions/math\", \"pi\"), 0)() idiv 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("3")
    );
  }

  /**
   * Applied to a constructor function.
   */
  @org.junit.Test
  public void functionLookup004() {
    final XQuery query = new XQuery(
      "function-lookup(QName(\"http://www.w3.org/2001/XMLSchema\", \"time\"), 1)(\"12:30:00Z\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("xs:time(\"12:30:00Z\")")
    );
  }

  /**
   * Applied to a user-defined function.
   */
  @org.junit.Test
  public void functionLookup005() {
    final XQuery query = new XQuery(
      "\n" +
      "        xquery version \"3.0\";\n" +
      "      \tdeclare function local:square($i as xs:integer) as xs:integer { $i*$i };\n" +
      "        function-lookup(QName(\"http://www.w3.org/2005/xquery-local-functions\", \"square\"), 1)(13)\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("169")
    );
  }

  /**
   * Applied to a private user-defined function in the same module.
   */
  @org.junit.Test
  public void functionLookup006() {
    final XQuery query = new XQuery(
      "\n" +
      "        xquery version \"3.0\";\n" +
      "      \tdeclare %private function local:square($i as xs:integer) as xs:integer { $i*$i };\n" +
      "        function-lookup(QName(\"http://www.w3.org/2005/xquery-local-functions\", \"square\"), 1)(13)\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("169")
    );
  }

  /**
   * Applied to a non-existent function.
   */
  @org.junit.Test
  public void functionLookup007() {
    final XQuery query = new XQuery(
      "\n" +
      "        function-lookup(QName(\"http://www.w3.org/2005/xquery-local-functions\", \"cube\"), 1)\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Applied to a non-existent function (no namespace URI).
   */
  @org.junit.Test
  public void functionLookup008() {
    final XQuery query = new XQuery(
      "\n" +
      "        function-lookup(QName(\"\", \"round\"), 2)\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Get the name and arity of the result.
   */
  @org.junit.Test
  public void functionLookup009() {
    final XQuery query = new XQuery(
      "\n" +
      "        function-lookup(QName(\"http://www.w3.org/2005/xpath-functions\", \"round\"), 2) ! \n" +
      "                   (function-name(.) ! (namespace-uri-from-QName(.), local-name-from-QName(.)), function-arity(.))\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://www.w3.org/2005/xpath-functions round 2")
    );
  }

  /**
   * Call the function with the wrong number of arguments.
   */
  @org.junit.Test
  public void functionLookup010() {
    final XQuery query = new XQuery(
      "\n" +
      "        xquery version \"3.0\";\n" +
      "      \tdeclare %private function local:square($i as xs:integer) as xs:integer { $i*$i };\n" +
      "        function-lookup(QName(\"http://www.w3.org/2005/xquery-local-functions\", \"square\"), 1)(13, 12)\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Call the function with the wrong type of argument.
   */
  @org.junit.Test
  public void functionLookup011() {
    final XQuery query = new XQuery(
      "\n" +
      "        xquery version \"3.0\";\n" +
      "      \tdeclare %private function local:square($i as xs:integer) as xs:integer { $i*$i };\n" +
      "        function-lookup(QName(\"http://www.w3.org/2005/xquery-local-functions\", \"square\"), 1)(\"banana\")\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Curry the resulting function.
   */
  @org.junit.Test
  public void functionLookup012() {
    final XQuery query = new XQuery(
      "function-lookup(QName(\"http://www.w3.org/2005/xpath-functions\", \"round\"), 2)(?, 3)(1.2345678)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1.235")
    );
  }

  /**
   * Function lookup creating a circular dependency (see bug 15791 - provisional outcome).
   */
  @org.junit.Test
  public void functionLookup013() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $n external := xs:QName('local:return-v');\n" +
      "        declare variable $v := function-lookup($n, 0)();\n" +
      "        declare function local:return-v() {$v + 1};\n" +
      "        $v\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0054")
    );
  }

  /**
   * Function lookup accessing an EXSLT extension function, with two possible results.
   */
  @org.junit.Test
  public void functionLookup014() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $f := function-lookup(QName(\"http://exslt.org/dates-and-times\", \"month-abbreviation\"), 1)\n" +
      "        return if (exists($f)) then $f(\"2012-02-28\") else \"not-available\"\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "not-available")
      ||
        assertStringValue(false, "Feb")
      )
    );
  }

  /**
   * Function lookup accessing an eXist extension function, with two possible results.
   */
  @org.junit.Test
  public void functionLookup015() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $f := function-lookup(QName(\"http://exist-db.org/xquery/datetime\", \"days-in-month\"), 1)\n" +
      "        return if (exists($f)) then $f(xs:date(\"2012-02-28\")) else \"not-available\"\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "not-available")
      ||
        assertStringValue(false, "29")
      )
    );
  }
}
