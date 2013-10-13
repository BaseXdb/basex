package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the IfExpr production.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdIfExpr extends QT3TestSet {

  /**
   *  Simple if expression where boolean is a constant true .
   */
  @org.junit.Test
  public void condExpr002() {
    final XQuery query = new XQuery(
      "if (fn:true()) then <elem1/> else <elem2/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem1/>", false)
    );
  }

  /**
   *  Simple if expression where boolean is a constant false .
   */
  @org.junit.Test
  public void condExpr003() {
    final XQuery query = new XQuery(
      "if (fn:false()) then <elem1/> else <elem2/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem2/>", false)
    );
  }

  /**
   *  Node set from a path expression as test condition .
   */
  @org.junit.Test
  public void condExpr004() {
    final XQuery query = new XQuery(
      "if (//CompanyName) then <elem1/> else <elem2/>",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/nw_Customers.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem1/>", false)
    );
  }

  /**
   *  Empty node set from a path expression as test condition .
   */
  @org.junit.Test
  public void condExpr005() {
    final XQuery query = new XQuery(
      "if (//NodeDoesNotExist) then <elem1/> else <elem2/>",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/nw_Customers.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem2/>", false)
    );
  }

  /**
   * FileName: CondExpr006  then-expression has another if expression .
   */
  @org.junit.Test
  public void condExpr006() {
    final XQuery query = new XQuery(
      "<out>{ if (1 != 0) then if (4 != 5) then 1 else 2 else 3 }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>1</out>", false)
    );
  }

  /**
   *  else-expr has another if expression .
   */
  @org.junit.Test
  public void condExpr007() {
    final XQuery query = new XQuery(
      "if (//MissingNode) then <elem3/> else if (/Root/Customers[@CustomerID='ALFKI']//Country = \"Germany\") then <elem1/> else <elem2/>",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/nw_Customers.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem1/>", false)
    );
  }

  /**
   *  and-expression in test expression .
   */
  @org.junit.Test
  public void condExpr008() {
    final XQuery query = new XQuery(
      "if ( /Root/Customers[1]/@CustomerID = 'ALFKI' and /Root/Customers[1]/FullAddress/City = 'Berlin') then \"pass\" else \"fail\"",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/nw_Customers.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "pass")
    );
  }

  /**
   *  or-expression in test expression .
   */
  @org.junit.Test
  public void condExpr009() {
    final XQuery query = new XQuery(
      "if (/Root/Customers[1]/@CustomerID = 'ALFKI' or /Root/Customers[1]/FullAddress/City = 'Non-Existent') then \"pass\" else \"fail\"",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/nw_Customers.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "pass")
    );
  }

  /**
   * FileName: CondExpr010  a true expression in if expression .
   */
  @org.junit.Test
  public void condExpr010() {
    final XQuery query = new XQuery(
      " if (2 != 4) then 1 else 0 ",
      ctx);
    try {
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
   * FileName: CondExpr011  If expression as argument to a function .
   */
  @org.junit.Test
  public void condExpr011() {
    final XQuery query = new XQuery(
      "fn:string-length(if (2 != 3) then 'foo' else 'expanded-foo')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
   * FileName: CondExpr012  Adapted from example in spec; test expression is a relational expression .
   */
  @org.junit.Test
  public void condExpr012() {
    final XQuery query = new XQuery(
      "if (/doc/widget1/@unit-cost = /doc/widget2/@unit-cost) then /doc/widget1/@name else /doc/widget2/@name",
      ctx);
    try {
      query.context(node(file("prod/IfExpr/xq311A.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "axolotl")
    );
  }

  /**
   * FileName: CondExpr013  test expression has another if expression .
   */
  @org.junit.Test
  public void condExpr013() {
    final XQuery query = new XQuery(
      " if (if (5 != 3) then fn:true() else fn:empty(/doc/widget1)) then \"search\" else \"assume\" ",
      ctx);
    try {
      query.context(node(file("prod/IfExpr/xq311A.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "search")
    );
  }

  /**
   * FileName: CondExpr014  Two if expressions at same depth in larger expression .
   */
  @org.junit.Test
  public void condExpr014() {
    final XQuery query = new XQuery(
      " ( if (3 != 2) then 16 else 0 ) + ( if (8 = 7) then 4 else 1 ) ",
      ctx);
    try {
      query.context(node(file("prod/IfExpr/xq311A.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("17")
    );
  }

  /**
   * FileName: CondExpr015  Two if expressions at same depth in larger expression, each parenthesized .
   */
  @org.junit.Test
  public void condExpr015() {
    final XQuery query = new XQuery(
      " (if (3 != 2) then 16 else 0) + (if (8 = 7) then 4 else 1) ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("17")
    );
  }

  /**
   * FileName: CondExpr016  if where branches return different valid types .
   */
  @org.junit.Test
  public void condExpr016() {
    final XQuery query = new XQuery(
      "(//Folder)[1]/File[ if ( ./@name='File00000000000' ) then 2 else true() ]/FileName",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<FileName>File00000000001</FileName><FileName>File00000000002</FileName><FileName>File00000000003</FileName><FileName>File00000000004</FileName><FileName>File00000000005</FileName><FileName>File00000000006</FileName><FileName>File00000000007</FileName><FileName>File00000000008</FileName><FileName>File00000000009</FileName><FileName>File00000000010</FileName><FileName>File00000000011</FileName><FileName>File00000000012</FileName><FileName>File00000000013</FileName><FileName>File00000000014</FileName><FileName>File00000000015</FileName><FileName>File00000000016</FileName><FileName>File00000000017</FileName><FileName>File00000000018</FileName><FileName>File00000000019</FileName><FileName>File00000000020</FileName><FileName>File00000000021</FileName><FileName>File00000000022</FileName><FileName>File00000000023</FileName><FileName>File00000000024</FileName><FileName>File00000000025</FileName><FileName>File00000000026</FileName><FileName>File00000000027</FileName><FileName>File00000000028</FileName><FileName>File00000000029</FileName><FileName>File00000000030</FileName>", false)
    );
  }

  /**
   * FileName: CondExpr017  if where branches return different valid types .
   */
  @org.junit.Test
  public void condExpr017() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File return if( $file/FileName='File00000000000' ) then $file/FileName else data( $file/FileName )",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<FileName>File00000000000</FileName>File00000000001 File00000000002 File00000000003 File00000000004 File00000000005 File00000000006 File00000000007 File00000000008 File00000000009 File00000000010 File00000000011 File00000000012 File00000000013 File00000000014 File00000000015 File00000000016 File00000000017 File00000000018 File00000000019 File00000000020 File00000000021 File00000000022 File00000000023 File00000000024 File00000000025 File00000000026 File00000000027 File00000000028 File00000000029 File00000000030", false)
    );
  }

  /**
   * FileName: CondExpr018  if where branches return different valid types .
   */
  @org.junit.Test
  public void condExpr018() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File return if( $file/FileName='File00000000004' ) then 1 else data( $file/FileName )",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "File00000000000 File00000000001 File00000000002 File00000000003 1 File00000000005 File00000000006 File00000000007 File00000000008 File00000000009 File00000000010 File00000000011 File00000000012 File00000000013 File00000000014 File00000000015 File00000000016 File00000000017 File00000000018 File00000000019 File00000000020 File00000000021 File00000000022 File00000000023 File00000000024 File00000000025 File00000000026 File00000000027 File00000000028 File00000000029 File00000000030")
    );
  }

  /**
   * FileName: CondExpr019  Test with test expression not contained in ( ... ) .
   */
  @org.junit.Test
  public void condExpr019() {
    final XQuery query = new XQuery(
      "if //File[1] then \"true\" else \"false\"",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Test case where then-expression raises a dynamic error, but test-expression selects else-expression so no error is raised .
   */
  @org.junit.Test
  public void condExpr022() {
    final XQuery query = new XQuery(
      "if (doc-available('nodocument.xml')) then doc('nodocument.xml') else 10 cast as xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("10")
    );
  }

  /**
   * FileName: CondExpr020  Test case where then-expression raises a dynamic error, but test-expression selects else-expression so no error is raised .
   */
  @org.junit.Test
  public void condExpr20() {
    final XQuery query = new XQuery(
      "if( false() ) then /Folder[1] cast as xs:double ? else 10 cast as xs:double ?",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("10")
    );
  }

  /**
   * FileName: CondExpr021  Test case where else-expression raises a dynamic error, but test-expression selects then-expression so no error is raised .
   */
  @org.junit.Test
  public void condExpr21() {
    final XQuery query = new XQuery(
      "if( true() ) then 10 cast as xs:double ? else /Folder[1] cast as xs:double ?",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("10")
    );
  }

  /**
   *  A test whose essence is: `(if(2) then 1 else 0) eq 1`. .
   */
  @org.junit.Test
  public void kCondExpr1() {
    final XQuery query = new XQuery(
      "(if(2) then 1 else 0) eq 1",
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
   *  An if-test applied on fn:count(). .
   */
  @org.junit.Test
  public void kCondExpr10() {
    final XQuery query = new XQuery(
      "if(count((1, 2, 3, current-time(), 4))) then true() else 4",
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
   *  An if-test applied on fn:count() combined with fn:not(). .
   */
  @org.junit.Test
  public void kCondExpr11() {
    final XQuery query = new XQuery(
      "if(not(count(remove((1, 2, 3, current-time()), 1)))) then 3 else true()",
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
   *  if-then clause combined with fn:boolean(). .
   */
  @org.junit.Test
  public void kCondExpr12() {
    final XQuery query = new XQuery(
      "if(boolean((1, 2, 3, current-time())[1] treat as xs:integer)) then true() else 4",
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
   *  A test whose essence is: `if(()) then false() else true()`. .
   */
  @org.junit.Test
  public void kCondExpr2() {
    final XQuery query = new XQuery(
      "if(()) then false() else true()",
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
   *  An expression involving the if expression that trigger certain optimization paths in some implementations. .
   */
  @org.junit.Test
  public void kCondExpr3() {
    final XQuery query = new XQuery(
      "(if((1, current-time())[1] treat as xs:integer) then true() else false()) eq true()",
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
   *  An expression involving the if expression that trigger certain optimization paths in some implementations(#2). .
   */
  @org.junit.Test
  public void kCondExpr4() {
    final XQuery query = new XQuery(
      "(if(boolean((1, current-time())[1] treat as xs:integer)) then true() else false()) eq true()",
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
   *  An expression involving the if expression that trigger certain optimization paths in some implementations(#2). .
   */
  @org.junit.Test
  public void kCondExpr5() {
    final XQuery query = new XQuery(
      "if((1, 2, 3, hours-from-time(current-time()))[1]) then true() else false()",
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
   *  An expression involving the if expression that trigger certain optimization paths in some implementations(#3). .
   */
  @org.junit.Test
  public void kCondExpr6() {
    final XQuery query = new XQuery(
      "string(if(boolean((1, current-time())[1] treat as xs:integer)) then true() else false()) eq \"true\"",
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
   *  An if-test which EBV cannot be extracted from. .
   */
  @org.junit.Test
  public void kCondExpr7() {
    final XQuery query = new XQuery(
      "(if(current-time()) then 1 else 0) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  An if-test which EBV cannot be extracted from. .
   */
  @org.junit.Test
  public void kCondExpr8() {
    final XQuery query = new XQuery(
      "1 eq (if(xs:anyURI(\"example.com/\")) then 1 else 0)",
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
   *  An if-test which EBV cannot be extracted from. .
   */
  @org.junit.Test
  public void kCondExpr9() {
    final XQuery query = new XQuery(
      "0 eq (if(xs:anyURI(\"\")) then 1 else 0)",
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
   *  An expression that can be rewritten to the empty sequence, no matter the test branch. .
   */
  @org.junit.Test
  public void k2CondExpr1() {
    final XQuery query = new XQuery(
      "empty(if(<e>{current-time()}</e>) then () else ())",
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
        error("XPST0005")
      )
    );
  }

  /**
   *  An expression that can be simplified to any of the result branches, no matter the test branch. .
   */
  @org.junit.Test
  public void k2CondExpr2() {
    final XQuery query = new XQuery(
      "empty(if(<e>{current-time()}</e>) then 1 else 1)",
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
   *  It is ok to name a function 'unordered'. .
   */
  @org.junit.Test
  public void k2CondExpr3() {
    final XQuery query = new XQuery(
      "declare default function namespace \"http://www.example.com/\"; declare function unordered() { 1 }; unordered()",
      ctx);
    try {
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
   *  Extract the EBV from a node sequence. Sorting and deduplication isn't necessary in that case. .
   */
  @org.junit.Test
  public void k2CondExpr4() {
    final XQuery query = new XQuery(
      "if(//(employee[location = \"Denver\"]/ancestor::*)) then 1 else 3",
      ctx);
    try {
      query.context(node(file("op/union/acme_corp.xml")));
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
   *  There is no exclamation mark operator in XQuery. .
   */
  @org.junit.Test
  public void k2CondExpr5() {
    final XQuery query = new XQuery(
      "if(!true()) then 2 else 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  An if expression cannot directly be an axis step. .
   */
  @org.junit.Test
  public void k2CondExpr6() {
    final XQuery query = new XQuery(
      "<e/>/if(true()) then 1 else 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Use a set of expressions that trigger a bug in some parsers. .
   */
  @org.junit.Test
  public void k2CondExpr7() {
    final XQuery query = new XQuery(
      "if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else if (1) then 1 else ()",
      ctx);
    try {
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
   *  Tests if(exists($x)) then f($x) else () optimization .
   */
  @org.junit.Test
  public void cbclCondexpr001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tlet $x := zero-or-one((1 to 10)[. div 2 = 2]) \n" +
      "      \treturn if(exists($x)) then xs:string($x) else ()\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   *  tests if(empty($x)) then () else f($x) optimization .
   */
  @org.junit.Test
  public void cbclCondexpr002() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tlet $x := zero-or-one((1 to 10)[. div 2 = 2]) \n" +
      "      \treturn if(empty($x)) then () else xs:string($x)\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }
}
