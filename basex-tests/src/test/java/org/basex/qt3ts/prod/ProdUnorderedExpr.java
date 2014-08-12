package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the OrderedExpr and UnorderedExpr productions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdUnorderedExpr extends QT3TestSet {

  /**
   *  A nested expression must be present when 'ordered' is used. .
   */
  @org.junit.Test
  public void kOrderExpr1() {
    final XQuery query = new XQuery(
      "ordered{}",
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
   *  A nested expression must be present when 'unordered' is used. .
   */
  @org.junit.Test
  public void kOrderExpr2() {
    final XQuery query = new XQuery(
      "unordered{}",
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
   *  A simple test of 'ordered{}'. .
   */
  @org.junit.Test
  public void kOrderExpr3() {
    final XQuery query = new XQuery(
      "ordered{true()}",
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
   *  A simple test of 'unordered{}'. .
   */
  @org.junit.Test
  public void kOrderExpr4() {
    final XQuery query = new XQuery(
      "unordered{true()}",
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
   *  path expression ordered .
   */
  @org.junit.Test
  public void orderexpr1() {
    final XQuery query = new XQuery(
      "ordered {//part[@partid < 2]}",
      ctx);
    try {
      query.context(node(file("docs/partlist.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<part partid=\"0\" name=\"car\"/><part partid=\"1\" partof=\"0\" name=\"engine\"/>", false)
    );
  }

  /**
   *  union unordered .
   */
  @org.junit.Test
  public void orderexpr10() {
    final XQuery query = new XQuery(
      "unordered {//part[@partof = 1] union //part[@partid = 1] }",
      ctx);
    try {
      query.context(node(file("docs/partlist.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<part partid=\"1\" partof=\"0\" name=\"engine\"/><part partid=\"3\" partof=\"1\" name=\"piston\"/>", false)
      ||
        assertSerialization("<part partid=\"3\" partof=\"1\" name=\"piston\"/><part partid=\"1\" partof=\"0\" name=\"engine\"/>", false)
      )
    );
  }

  /**
   *  intersect ordered .
   */
  @org.junit.Test
  public void orderexpr11() {
    final XQuery query = new XQuery(
      "ordered {//part[@partof < 2] intersect //part[@partid = 1 or @partid > 2] }",
      ctx);
    try {
      query.context(node(file("docs/partlist.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<part partid=\"1\" partof=\"0\" name=\"engine\"/><part partid=\"3\" partof=\"1\" name=\"piston\"/>", false)
    );
  }

  /**
   *  intersect unordered .
   */
  @org.junit.Test
  public void orderexpr12() {
    final XQuery query = new XQuery(
      "unordered {//part[@partof < 2] intersect //part[@partid = 1 or @partid > 2] }",
      ctx);
    try {
      query.context(node(file("docs/partlist.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<part partid=\"1\" partof=\"0\" name=\"engine\"/><part partid=\"3\" partof=\"1\" name=\"piston\"/>", false)
      ||
        assertSerialization("<part partid=\"3\" partof=\"1\" name=\"piston\"/><part partid=\"1\" partof=\"0\" name=\"engine\"/>", false)
      )
    );
  }

  /**
   *  except ordered .
   */
  @org.junit.Test
  public void orderexpr13() {
    final XQuery query = new XQuery(
      "ordered {//part[@partof < 2] except //part[@partid = 2] }",
      ctx);
    try {
      query.context(node(file("docs/partlist.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<part partid=\"1\" partof=\"0\" name=\"engine\"/><part partid=\"3\" partof=\"1\" name=\"piston\"/>", false)
    );
  }

  /**
   *  except unordered .
   */
  @org.junit.Test
  public void orderexpr14() {
    final XQuery query = new XQuery(
      "unordered {//part[@partof < 2] except //part[@partid = 2] }",
      ctx);
    try {
      query.context(node(file("docs/partlist.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<part partid=\"1\" partof=\"0\" name=\"engine\"/><part partid=\"3\" partof=\"1\" name=\"piston\"/>", false)
      ||
        assertSerialization("<part partid=\"3\" partof=\"1\" name=\"piston\"/><part partid=\"1\" partof=\"0\" name=\"engine\"/>", false)
      )
    );
  }

  /**
   *  fn:subsequence ordered .
   */
  @org.junit.Test
  public void orderexpr15() {
    final XQuery query = new XQuery(
      "ordered {fn:subsequence((1,2,3,4),2,2)}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2 3")
    );
  }

  /**
   *  fn:subsequence unordered .
   */
  @org.junit.Test
  public void orderexpr16() {
    final XQuery query = new XQuery(
      "unordered {fn:subsequence((1,2,3,4),2,2)}",
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
        assertStringValue(false, "2 3")
      ||
        assertStringValue(false, "3 2")
      )
    );
  }

  /**
   *  fn:reverse ordered .
   */
  @org.junit.Test
  public void orderexpr17() {
    final XQuery query = new XQuery(
      "ordered {fn:reverse((3,2))}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2 3")
    );
  }

  /**
   *  fn:reverse unordered .
   */
  @org.junit.Test
  public void orderexpr18() {
    final XQuery query = new XQuery(
      "unordered {fn:reverse((2,3))}",
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
        assertStringValue(false, "2 3")
      ||
        assertStringValue(false, "3 2")
      )
    );
  }

  /**
   *  FLWOR ordered .
   */
  @org.junit.Test
  public void orderexpr19() {
    final XQuery query = new XQuery(
      "ordered { for $i in (//part[@partid = 1], //part[@partid = 2]), $j in (//part[@partof = $i/@partid]) where ($i/@partid + $j/@partid) < 7 return $i/@partid + $j/@partid }",
      ctx);
    try {
      query.context(node(file("docs/partlist.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4 6")
    );
  }

  /**
   *  path expression unordered .
   */
  @org.junit.Test
  public void orderexpr2() {
    final XQuery query = new XQuery(
      "unordered {//part[@partid < 2]}",
      ctx);
    try {
      query.context(node(file("docs/partlist.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<part partid=\"0\" name=\"car\"/><part partid=\"1\" partof=\"0\" name=\"engine\"/>", false)
      ||
        assertSerialization("<part partid=\"1\" partof=\"0\" name=\"engine\"/><part partid=\"0\" name=\"car\"/>", false)
      )
    );
  }

  /**
   *  FLWOR unordered .
   */
  @org.junit.Test
  public void orderexpr20() {
    final XQuery query = new XQuery(
      "unordered { for $i in (//part[@partid = 1], //part[@partid = 2]), $j in (//part[@partof = $i/@partid]) where ($i/@partid + $j/@partid) < 7 return $i/@partid + $j/@partid }",
      ctx);
    try {
      query.context(node(file("docs/partlist.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "4 6")
      ||
        assertStringValue(false, "6 4")
      )
    );
  }

  /**
   *  position predicate ordered .
   */
  @org.junit.Test
  public void orderexpr5() {
    final XQuery query = new XQuery(
      "ordered {//part[@partid < 2][2]}",
      ctx);
    try {
      query.context(node(file("docs/partlist.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<part partid=\"1\" partof=\"0\" name=\"engine\"/>", false)
    );
  }

  /**
   *  position predicate unordered .
   */
  @org.junit.Test
  public void orderexpr6() {
    final XQuery query = new XQuery(
      "unordered {//part[@partid < 2][2]}",
      ctx);
    try {
      query.context(node(file("docs/partlist.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<part partid=\"1\" partof=\"0\" name=\"engine\"/>", false)
      ||
        assertSerialization("<part partid=\"0\" name=\"car\"/>", false)
      )
    );
  }

  /**
   *  union ordered .
   */
  @org.junit.Test
  public void orderexpr9() {
    final XQuery query = new XQuery(
      "ordered {//part[@partof = 1] union //part[@partid = 1] }",
      ctx);
    try {
      query.context(node(file("docs/partlist.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<part partid=\"1\" partof=\"0\" name=\"engine\"/><part partid=\"3\" partof=\"1\" name=\"piston\"/>", false)
    );
  }

  /**
   *  Evaluation of ordered expression together with if expression ("some" operator). .
   */
  @org.junit.Test
  public void orderedunorderedexpr1() {
    final XQuery query = new XQuery(
      "ordered {if (fn:true()) then (0,1,2,3,4) else (\"A\",\"B\",\"C\")}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0 1 2 3 4")
    );
  }

  /**
   *  Evaluation of ordered expression used with "or" expression ("and" operator). .
   */
  @org.junit.Test
  public void orderedunorderedexpr2() {
    final XQuery query = new XQuery(
      "ordered {if (1 eq 1 and 2 eq 2) then (0,1,2,3,4) else (\"a\",\"b\")}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0 1 2 3 4")
    );
  }

  /**
   *  Evaluation of ordered expression used with "or" expression ("or" operator). .
   */
  @org.junit.Test
  public void orderedunorderedexpr3() {
    final XQuery query = new XQuery(
      "ordered {if (1 eq 1 or 2 eq 3) then (0,1,2,3,4) else (\"a\",\"b\")}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0 1 2 3 4")
    );
  }

  /**
   *  Evaluation of ordered expression used with quantified expression. .
   */
  @org.junit.Test
  public void orderedunorderedexpr4() {
    final XQuery query = new XQuery(
      "ordered {if (some $x in (1, 2, 3), $y in (2, 3, 4) satisfies $x + $y = 4) then (0,1,2,3,4) else (\"a\",\"b\")}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0 1 2 3 4")
    );
  }

  /**
   *  Evaluation of ordered expression used with quantified expression. .
   */
  @org.junit.Test
  public void orderedunorderedexpr5() {
    final XQuery query = new XQuery(
      "ordered {if (every $x in (1, 2, 3) satisfies $x < 4) then (0,1,2,3,4) else (\"a\",\"b\")}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0 1 2 3 4")
    );
  }

  /**
   *  Evaluation of ordered expression used with typeswitch expression. .
   */
  @org.junit.Test
  public void orderedunorderedexpr6() {
    final XQuery query = new XQuery(
      "ordered {typeswitch(123) case $i as xs:string return (\"a\",\"b\",\"c\") case $i as xs:double return (\"a\",\"b\",\"c\") case $i as xs:integer return (1,2,3,4) default return (\"a\",\"b\",\"c\") }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4")
    );
  }
}
