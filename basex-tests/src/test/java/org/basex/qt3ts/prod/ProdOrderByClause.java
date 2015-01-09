package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the OrderByClause production.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdOrderByClause extends QT3TestSet {

  /**
   *  Use a relative, valid collation. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout1() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.w3.org/2005/xpath-functions/\"; let $i as xs:integer* := (1, 2, 3) order by 1 collation \"collation/codepoint\" return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3")
    );
  }

  /**
   * Cardinality error in the order spec. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout10() {
    final XQuery query = new XQuery(
      "for $a in (1, 4, 2) let $i := (1, $a, 2) order by $i return $i",
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
   *  A sort where the for-binding is only used as a sort key. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout11() {
    final XQuery query = new XQuery(
      "for $a in (1, 4, 2) let $i := (1, 3, 2) order by $a return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 3 2 1 3 2 1 3 2")
    );
  }

  /**
   *  A sort with an unused for-binding. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout12() {
    final XQuery query = new XQuery(
      "for $a in (3, 2, 1), $b in (6, 5, 4) order by $a return $a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 1 1 2 2 2 3 3 3")
    );
  }

  /**
   *  A sort with a for-binding whose only purpose is sorting. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout13() {
    final XQuery query = new XQuery(
      "for $a in (3, 2, 1), $b in (6, 5, 4) stable order by $b return $a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3 2 1 3 2 1 3 2 1")
    );
  }

  /**
   *  Apply fn:avg() to the return value of a for clause with sorting. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout14() {
    final XQuery query = new XQuery(
      "declare variable $e := <e> <a>3</a> <a>2</a> <a>1</a> </e>; <result> { avg(for $i in $e/a order by $i return $i) } </result>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result>2</result>", false)
    );
  }

  /**
   *  Ensure that cardinality checks are effective on the return value of a . .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout15() {
    final XQuery query = new XQuery(
      "declare variable $e := <e> <a>3</a> <a>2</a> <a>1</a> </e>; exactly-one(for $i in $e/a order by $i return $i)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0005")
    );
  }

  /**
   *  Sort booleans. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout16() {
    final XQuery query = new XQuery(
      "for $i in (false(), true(), true(), false(), true(), false()) order by $i return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "false false false true true true")
    );
  }

  /**
   *  Extract the effective boolean value from a order by expression. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout17() {
    final XQuery query = new XQuery(
      "boolean((for $i in (false(), true(), true(), false(), true(), false()) order by $i return $i)[1])",
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
   *  Sort a single atomic value. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout18() {
    final XQuery query = new XQuery(
      "(for $i in current-time() order by $i return $i) eq current-time()",
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
   *  Multiple atomic values as sort key trigger a type error. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout19() {
    final XQuery query = new XQuery(
      "let $i := (1, 3, 2) order by $i return $i",
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
        assertStringValue(false, "1 3 2")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Use a relative, invalid collation. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout2() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.w3.org/2005/xpath-functions/\"; let $i as xs:integer* := (1, 2, 3) order by 1 collation \"collation/\" return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0076")
    );
  }

  /**
   *  Multiple nodes as sort key trigger a type error. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout20() {
    final XQuery query = new XQuery(
      "let $i := (<e>1</e>, <e>3</e>, <e>2</e>) order by $i return $i",
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
        assertSerialization("<e>1</e><e>3</e><e>2</e>", false)
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  A sort key that doesn't affect the result. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout21() {
    final XQuery query = new XQuery(
      "let $i := (<e>1</e>, <e>3</e>, <e>2</e>) order by 1 return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e>1</e><e>3</e><e>2</e>", false)
    );
  }

  /**
   *  order by preceded by an unused let binding. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout22() {
    final XQuery query = new XQuery(
      "for $i in (1, 3, 2) let $c := 3 stable order by () return $i",
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
        assertStringValue(false, "1 3 2")
      ||
        assertStringValue(false, "1 2 3")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Multiple atomic values as sort key trigger a type error(stable sort). .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout23() {
    final XQuery query = new XQuery(
      "let $i := (1, 3, 2) stable order by $i return $i",
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
        assertStringValue(false, "1 3 2")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Multiple nodes as sort key trigger a type error(stable sort). .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout24() {
    final XQuery query = new XQuery(
      "let $i := (<e>1</e>, <e>3</e>, <e>2</e>) stable order by $i return $i",
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
        assertSerialization("<e>1</e><e>3</e><e>2</e>", false)
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  xs:hexBinary values cannot be compared(stable sort). .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout25() {
    final XQuery query = new XQuery(
      "let $i := (xs:hexBinary(\"FF\"), xs:hexBinary(\"FF\")) stable order by $i return $i",
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
        assertStringValue(false, "FF FF")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  xs:hexBinary values cannot be compared. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout26() {
    final XQuery query = new XQuery(
      "let $i := (xs:hexBinary(\"FF\"), xs:hexBinary(\"FF\")) order by $i return $i",
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
        assertStringValue(false, "FF FF")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  xs:time and xs:date values cannot be compared(stable sort). .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout27() {
    final XQuery query = new XQuery(
      "let $i := (xs:date(\"2001-02-03\"), xs:time(\"01:02:03Z\")) stable order by $i return $i",
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
        assertStringValue(false, "2001-02-03 01:02:03Z")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  xs:time and xs:date values cannot be compared. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout28() {
    final XQuery query = new XQuery(
      "let $i := (xs:date(\"2001-02-03\"), xs:time(\"01:02:03Z\")) order by $i return $i",
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
        assertStringValue(false, "2001-02-03 01:02:03Z")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  An empty order by and an empty return clause, with node constructor. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout29() {
    final XQuery query = new XQuery(
      "<r> { for $i in attribute name {()} order by () return () } </r>",
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
        assertSerialization("<r/>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Use a relative, unknown collation. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout3() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.w3.org/2005/xpath-functions/\"; let $i as xs:integer* := (1, 2, 3) order by 1 collation \"collation/\" return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0076")
    );
  }

  /**
   *  An empty order by and an empty return clause, with atomic value. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout30() {
    final XQuery query = new XQuery(
      "<r> { for $i in 1 order by () return () } </r>",
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
        assertSerialization("<r/>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  A type error in order by, but without for clause(unstable sort). .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout31() {
    final XQuery query = new XQuery(
      "let $i := (1, 2, 3) order by $i return $i",
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
        assertStringValue(false, "1 2 3")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  A type error in order by, but without for clause(stable sort). .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout32() {
    final XQuery query = new XQuery(
      "let $i := (1, 2, 3) stable order by $i return $i",
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
        assertStringValue(false, "1 2 3")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Uncomparable values in order by, but without for clause(stable sort). .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout33() {
    final XQuery query = new XQuery(
      "let $i := (xs:hexBinary(\"FF\"), xs:hexBinary(\"FF\")) stable order by $i[1] return $i",
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
        assertStringValue(false, "FF FF")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Uncomparable values in order by, but without for clause(stable sort). .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout34() {
    final XQuery query = new XQuery(
      "let $i := (xs:hexBinary(\"FF\"), xs:hexBinary(\"FF\")) order by $i[1] return $i",
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
        assertStringValue(false, "FF FF")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Uncomparable values in order by, but without for clause(stable sort). .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout35() {
    final XQuery query = new XQuery(
      "let $i := (xs:hexBinary(\"FF\"), xs:time(\"03:03:03Z\"), xs:hexBinary(\"FF\")) stable order by $i[1] return $i",
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
        assertStringValue(false, "FF 03:03:03Z FF")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Uncomparable values in order by, but without for clause(stable sort). .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout36() {
    final XQuery query = new XQuery(
      "let $i := (xs:hexBinary(\"FF\"), xs:time(\"03:03:03Z\"), xs:hexBinary(\"FF\")) order by $i[1] return $i",
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
        assertStringValue(false, "FF 03:03:03Z FF")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Unused order by. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout37() {
    final XQuery query = new XQuery(
      "for $i in (1, 3, 2) stable order by () return $i",
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
        assertStringValue(false, "1 3 2")
      ||
        assertStringValue(false, "1 2 3")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  'order by' combined with reverse(). .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout38() {
    final XQuery query = new XQuery(
      "for $i in (1, 2, 3) stable order by 1 return reverse(($i, \"FO\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "FO 1 FO 2 FO 3")
    );
  }

  /**
   *  Contains a type error, but the result can be computed without evaluation. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout39() {
    final XQuery query = new XQuery(
      "for $a in (1, 4, 2) let $i := (1, 3, 2) order by $i return 1",
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
        assertStringValue(false, "1 1 1")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Use an absolute, invalid collation. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout4() {
    final XQuery query = new XQuery(
      "let $i as xs:integer* := (1, 2, 3) order by 1 collation \"http:\\\\invalid%>URI\\someURI\" return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0046")
    );
  }

  /**
   * Updated by Benjamin Nguyen on 2010-10-18T:13:07:50+02:00 to resolve bug 10651  Overshadowing variable that leads to type error. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout40() {
    final XQuery query = new XQuery(
      "for $a in (3, 2, 1) let $a := ($a, 1), $b := (2, 1), $c := (2, 1), $d:= (2, 1) order by $a return $a",
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
   *  order by, with many let bindings inbetween. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout41() {
    final XQuery query = new XQuery(
      "for $a in (3, 2, 1) let $b := (2, 1), $c := (2, 1), $d := (2, 1), $e := (2, 1) order by $a return $a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3")
    );
  }

  /**
   *  Apply fn:string() on a for clause that only produces on item, and that cannot easily constant propagate. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout42() {
    final XQuery query = new XQuery(
      "string(for $i in current-date() order by $i return $i)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(1)
    );
  }

  /**
   *  Extract the effective boolean value from the result of order by. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout43() {
    final XQuery query = new XQuery(
      "if(for $i in <e> <a id=\"3\"/> <b id=\"2\"/> <c id=\"1\"/> </e>/* order by xs:integer($i/@id) return $i) then 4 else 9",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("4")
    );
  }

  /**
   *  Sort, with a where and let clause in between. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout44() {
    final XQuery query = new XQuery(
      "for $a in (2, 1) let $b := 1 where true() order by $a return $a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2")
    );
  }

  /**
   *  Short key-for involved in sorting. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout45() {
    final XQuery query = new XQuery(
      "for $a in (3, 2, 1), $b in (6) stable order by $b return $a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3 2 1")
    );
  }

  /**
   *  Sort special floating point values. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout46() {
    final XQuery query = new XQuery(
      "let $numbers := (1, 2, 1.3, 3e3, xs:double(\"NaN\"), xs:double(\"-INF\"), xs:double(\"INF\")) return (for $i in $numbers order by $i empty least return $i, \"SEP\", for $i in $numbers order by $i empty greatest return $i)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN -INF 1 1.3 2 3000 INF SEP -INF 1 1.3 2 3000 INF NaN")
    );
  }

  /**
   *  Sort special floating point values(stable sort). .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout47() {
    final XQuery query = new XQuery(
      "let $numbers := (1, 2, 1.3, 3e3, xs:double(\"NaN\"), xs:double(\"-INF\"), xs:double(\"INF\")) return (for $i in $numbers stable order by $i empty least return $i, \"SEP\", for $i in $numbers order by $i empty greatest return $i)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN -INF 1 1.3 2 3000 INF SEP -INF 1 1.3 2 3000 INF NaN")
    );
  }

  /**
   *  Sort special floating point values. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout48() {
    final XQuery query = new XQuery(
      "let $numbers := (<e>NaN</e>, <e/>, <e/>, <e>NaN</e>, <e>NaN</e>, <e>INF</e>, <e>NaN</e>, <e/>, <e>3</e>, comment{\"3\"}) return (for $i in $numbers order by xs:double($i/text()) empty least return xs:double($i/text()), \"SEP\", for $i in $numbers order by xs:double($i/text()) empty greatest return xs:double($i/text()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN NaN NaN NaN 3 INF SEP 3 INF NaN NaN NaN NaN")
    );
  }

  /**
   *  Sort special floating point values(stable sort). .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout49() {
    final XQuery query = new XQuery(
      "let $numbers := (<e>NaN</e>, <e/>, <e/>, <e>NaN</e>, <e>NaN</e>, <e>INF</e>, <e>NaN</e>, <e/>, <e>3</e>, comment{\"3\"}) return (for $i in $numbers stable order by xs:double($i/text()) empty least return xs:double($i/text()), \"SEP\", for $i in $numbers stable order by xs:double($i/text()) empty greatest return xs:double($i/text()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN NaN NaN NaN 3 INF SEP 3 INF NaN NaN NaN NaN")
    );
  }

  /**
   *  A simple sorting of integers. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout5() {
    final XQuery query = new XQuery(
      "for $i in (1, 3, 2) order by $i return $i",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3")
    );
  }

  /**
   *  A simple sorting of integers(#2). .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout6() {
    final XQuery query = new XQuery(
      "for $i in (1, 3, 2) order by $i return ($i, 2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 2 2 3 2")
    );
  }

  /**
   *  A simple sorting of integers(#2). .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout7() {
    final XQuery query = new XQuery(
      "for $i in (1, 3, 2) order by $i empty INVALID return ($i, 2)",
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
   *  Sorting only involving a let-binding, no for-clause. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout8() {
    final XQuery query = new XQuery(
      "let $i := (1, 3, 2) order by $i return $i",
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
        assertStringValue(false, "1 3 2")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Sorting of 4 6 5. .
   */
  @org.junit.Test
  public void k2OrderbyExprWithout9() {
    final XQuery query = new XQuery(
      "let $i := (1, 3, 2), $b := (4, 6, 5) order by $b return $b",
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
        assertStringValue(false, "4 6 5")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to 
   *         "($x * -1) ", where $x is a set of negative numbers and the ordering mode set to descending,
   *         orderBy29 using an unvalidated source document.
   */
  @org.junit.Test
  public void orderBy29a() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default element namespace \"http://www.w3.org/XQueryTestOrderBy\"; \n" +
      "        <results> { \n" +
      "            for $x in /DataValues/NegativeNumbers/orderData \n" +
      "            order by ($x * -1) descending \n" +
      "            return ($x * -1e0) (:force to xs:double:) \n" +
      "        } </results>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/OrderByClause/orderData.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results xmlns=\"http://www.w3.org/XQueryTestOrderBy\">1.0E17 1.0E16 1.0E15 1.0E14 1.0E13 1.0E12 1.0E11 1.0E10 1.0E9 1.0E8 1.0E7 1.0E6 100000 10000 1000 100 10 1 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to 
   *         "($x + $x) ", where $x is a set of small negative numbers and the ordering mode set to descending,
   *         orderBy52 using an unvalidated source document .
   */
  @org.junit.Test
  public void orderBy52a() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.w3.org/XQueryTestOrderBy\"; \n" +
      "        <results> { \n" +
      "            for $x in /DataValues/SmallNegativeNumbers/orderData \n" +
      "            order by ($x + $x) descending \n" +
      "            return xs:double($x + $x) \n" +
      "        } </results>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/OrderByClause/orderData.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results xmlns=\"http://www.w3.org/XQueryTestOrderBy\">-0 -2.0E-18 -2.0E-17 -2.0E-16 -2.0E-15 -2.0E-14 -2.0E-13 -2.0E-12 -2.0E-11 -2.0E-10 -2.0E-9 -2.0E-8 -2.0E-7 -0.000002 -0.00002 -0.0002 -0.002 -0.02 -0.2</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "$x ", where $x is a set of Strings and the ordering mode set to ascending Uses a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal1() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\n" +
      "        \"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\n" +
      "        \"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by xs:string($x) \n" +
      "        ascending return xs:string($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>A String B String C String D String E String F String G String H String I String J String K String L String M String N String O String P String R String S String T String U String V String W String X String Y String Z String</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "concat($x,"") ", where $x is a set of Strings and the ordering mode set to ascending Uses a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal10() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by concat(xs:string($x),\"\") ascending return concat(xs:string($x),\"\") } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>A String B String C String D String E String F String G String H String I String J String K String L String M String N String O String P String R String S String T String U String V String W String X String Y String Z String</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "concat("",$x) ", where $x is a set of Strings and the ordering mode set to descending Use locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal11() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by concat(\"\",xs:string($x)) descending return concat(\"\",xs:string($x)) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>Z String Y String X String W String V String U String T String S String R String P String O String N String M String L String K String J String I String H String G String F String E String D String C String B String A String</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "concat("",$x) ", where $x is a set of Strings and the ordering mode set to ascending Uses locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal12() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by concat(\"\",xs:string($x)) ascending return concat(\"\",xs:string($x)) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>A String B String C String D String E String F String G String H String I String J String K String L String M String N String O String P String R String S String T String U String V String W String X String Y String Z String</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "concat($x,$x) ", where $x is a set of Strings and the ordering mode set to ascending Use a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal16() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by concat(xs:string($x),xs:string($x)) ascending return concat(xs:string($x),xs:string($x)) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>A StringA String B StringB String C StringC String D StringD String E StringE String F StringF String G StringG String H StringH String I StringI String J StringJ String K StringK String L StringL String M StringM String N StringN String O StringO String P StringP String R StringR String S StringS String T StringT String U StringU String V StringV String W StringW String X StringX String Y StringY String Z StringZ String</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "string-length($x) ", where $x is a set of Strings and the ordering mode set to ascending Use a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal17() {
    final XQuery query = new XQuery(
      "<results> { for $x in(\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by string-length(xs:string($x)) ascending return string-length(xs:string($x)) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "count($x) ", where $x is a set of Strings and the ordering mode set to ascending Use a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal18() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by count(xs:string($x)) ascending return count(xs:string($x)) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "string-length($x) ", where $x is a set of Strings and the ordering mode set to ascending Use a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal19() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by string-length(xs:string($x)) ascending return string-length(xs:string($x)) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "$x ", where $x is a set of Strings and the ordering mode set to descending Uses a local sequence. .
   */
  @org.junit.Test
  public void orderbylocal2() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\n" +
      "        \"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\n" +
      "        \"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by xs:string($x) \n" +
      "        descending return xs:string($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>Z String Y String X String W String V String U String T String S String R String P String O String N String M String L String K String J String I String H String G String F String E String D String C String B String A String</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "$x ", where $x is a set of negative numbers and the ordering mode set to ascending Use a locally defined sequence .
   */
  @org.junit.Test
  public void orderbylocal20() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-100000000000000000,-10000000000000000,-1000000000000000,-100000000000000,-10000000000000,-1000000000000,-100000000000,-10000000000,-1000000000,-100000000,-10000000,-1000000,-100000,-10000,-1000,-100,-10,-1,-0) order by xs:decimal($x) ascending return xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>-100000000000000000 -10000000000000000 -1000000000000000 -100000000000000 -10000000000000 -1000000000000 -100000000000 -10000000000 -1000000000 -100000000 -10000000 -1000000 -100000 -10000 -1000 -100 -10 -1 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "$x ", where $x is a set of negative numbers and the ordering mode set to descending Use a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal21() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-100000000000000000,-10000000000000000,-1000000000000000,-100000000000000,-10000000000000,-1000000000000,-100000000000,-10000000000,-1000000000,-100000000,-10000000,-1000000,-100000,-10000,-1000,-100,-10,-1,-0) order by xs:decimal($x) descending return xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 -1 -10 -100 -1000 -10000 -100000 -1000000 -10000000 -100000000 -1000000000 -10000000000 -100000000000 -1000000000000 -10000000000000 -100000000000000 -1000000000000000 -10000000000000000 -100000000000000000</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "($x + $x) ", where $x is a set of negative numbers and the ordering mode set to descending Use a locally define sequence. .
   */
  @org.junit.Test
  public void orderbylocal22() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-100000000000000000,-10000000000000000,-1000000000000000,-100000000000000,-10000000000000,-1000000000000,-100000000000,-10000000000,-1000000000,-100000000,-10000000,-1000000,-100000,-10000,-1000,-100,-10,-1,-0) order by (xs:decimal($x) + xs:decimal($x)) descending return xs:decimal($x) + xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 -2 -20 -200 -2000 -20000 -200000 -2000000 -20000000 -200000000 -2000000000 -20000000000 -200000000000 -2000000000000 -20000000000000 -200000000000000 -2000000000000000 -20000000000000000 -200000000000000000</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "xs:float($x) ", where $x is a set of negative numbers and the ordering mode set to descending Use a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal25() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-100000000000000000,-10000000000000000,-1000000000000000,-100000000000000,-10000000000000,-1000000000000,-100000000000,-10000000000,-1000000000,-100000000,-10000000,-1000000,-100000,-10000,-1000,-100,-10,-1,-0) order by xs:float($x) descending return xs:float($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 -1 -10 -100 -1000 -10000 -100000 -1.0E6 -1.0E7 -1.0E8 -1.0E9 -1.0E10 -1.0E11 -1.0E12 -1.0E13 -1.0E14 -1.0E15 -1.0E16 -1.0E17</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "xs:decimal($x) ", where $x is a set of negative numbers and the ordering mode set to descending Use a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal26() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-100000000000000000,-10000000000000000,-1000000000000000,-100000000000000,-10000000000000,-1000000000000,-100000000000,-10000000000,-1000000000,-100000000,-10000000,-1000000,-100000,-10000,-1000,-100,-10,-1,-0) order by xs:decimal($x) descending return xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 -1 -10 -100 -1000 -10000 -100000 -1000000 -10000000 -100000000 -1000000000 -10000000000 -100000000000 -1000000000000 -10000000000000 -100000000000000 -1000000000000000 -10000000000000000 -100000000000000000</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "xs:double($x) ", where $x is a set of negative numbers and the ordering mode set to descending Use a locally defined sequence .
   */
  @org.junit.Test
  public void orderbylocal27() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-100000000000000000,-10000000000000000,-1000000000000000,-100000000000000,-10000000000000,-1000000000000,-100000000000,-10000000000,-1000000000,-100000000,-10000000,-1000000,-100000,-10000,-1000,-100,-10,-1,-0) order by xs:double($x) descending return xs:double($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 -1 -10 -100 -1000 -10000 -100000 -1.0E6 -1.0E7 -1.0E8 -1.0E9 -1.0E10 -1.0E11 -1.0E12 -1.0E13 -1.0E14 -1.0E15 -1.0E16 -1.0E17</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "xs:integer($x) ", where $x is a set of negative numbers and the ordering mode set to descending Use a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal28() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-100000000000000000,-10000000000000000,-1000000000000000,-100000000000000,-10000000000000,-1000000000000,-100000000000,-10000000000,-1000000000,-100000000,-10000000,-1000000,-100000,-10000,-1000,-100,-10,-1,-0) order by xs:integer($x) descending return xs:integer($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 -1 -10 -100 -1000 -10000 -100000 -1000000 -10000000 -100000000 -1000000000 -10000000000 -100000000000 -1000000000000 -10000000000000 -100000000000000 -1000000000000000 -10000000000000000 -100000000000000000</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "($x * -1) ", where $x is a set of negative numbers and the ordering mode set to descending Use a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal29() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-100000000000000000,-10000000000000000,-1000000000000000,-100000000000000,-10000000000000,-1000000000000,-100000000000,-10000000000,-1000000000,-100000000,-10000000,-1000000,-100000,-10000,-1000,-100,-10,-1,-0) order by (xs:decimal($x) * -1) descending return (xs:decimal($x) * -1) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>100000000000000000 10000000000000000 1000000000000000 100000000000000 10000000000000 1000000000000 100000000000 10000000000 1000000000 100000000 10000000 1000000 100000 10000 1000 100 10 1 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "concat($x,"()") ", where $x is a set of Strings and the ordering mode set to ascending Uses a local sequence .
   */
  @org.junit.Test
  public void orderbylocal3() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\n" +
      "        \"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\n" +
      "        \"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by concat(xs:string($x),\"()\") \n" +
      "        ascending return concat(xs:string($x),\"()\") } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>A String() B String() C String() D String() E String() F String() G String() H String() I String() J String() K String() L String() M String() N String() O String() P String() R String() S String() T String() U String() V String() W String() X String() Y String() Z String()</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "$x ", where $x is a set of positive numbers and the ordering mode set to ascending Use a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal30() {
    final XQuery query = new XQuery(
      "<results> { for $x in (100000000000000000,10000000000000000,1000000000000000,100000000000000,10000000000000,1000000000000,100000000000,10000000000,1000000000,100000000,10000000,1000000,100000,10000,1000,100,10,1,0) order by xs:decimal($x) ascending return xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 1 10 100 1000 10000 100000 1000000 10000000 100000000 1000000000 10000000000 100000000000 1000000000000 10000000000000 100000000000000 1000000000000000 10000000000000000 100000000000000000</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "$x ", where $x is a set of positive numbers and the ordering mode set to descending Use a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal31() {
    final XQuery query = new XQuery(
      "<results> { for $x in (100000000000000000,10000000000000000,1000000000000000,100000000000000,10000000000000,1000000000000,100000000000,10000000000,1000000000,100000000,10000000,1000000,100000,10000,1000,100,10,1,0) order by xs:decimal($x) descending return xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>100000000000000000 10000000000000000 1000000000000000 100000000000000 10000000000000 1000000000000 100000000000 10000000000 1000000000 100000000 10000000 1000000 100000 10000 1000 100 10 1 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "($x + $x) ", where $x is a set of positive numbers and the ordering mode set to descending Use a locally defined sequwnce. .
   */
  @org.junit.Test
  public void orderbylocal32() {
    final XQuery query = new XQuery(
      "<results> { for $x in (100000000000000000,10000000000000000,1000000000000000,100000000000000,10000000000000,1000000000000,100000000000,10000000000,1000000000,100000000,10000000,1000000,100000,10000,1000,100,10,1,0) order by (xs:decimal($x) + xs:decimal($x)) descending return xs:decimal($x) + xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>200000000000000000 20000000000000000 2000000000000000 200000000000000 20000000000000 2000000000000 200000000000 20000000000 2000000000 200000000 20000000 2000000 200000 20000 2000 200 20 2 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "xs:float($x) ", where $x is a set of positive numbers and the ordering mode set to descending Use a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal35() {
    final XQuery query = new XQuery(
      "<results> { for $x in (100000000000000000,10000000000000000,1000000000000000,100000000000000,10000000000000,1000000000000,100000000000,10000000000,1000000000,100000000,10000000,1000000,100000,10000,1000,100,10,1,0) order by xs:float($x) descending return xs:float($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>1.0E17 1.0E16 1.0E15 1.0E14 1.0E13 1.0E12 1.0E11 1.0E10 1.0E9 1.0E8 1.0E7 1.0E6 100000 10000 1000 100 10 1 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "xs:decimal($x) ", where $x is a set of positive numbers and the ordering mode set to descending Use a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal36() {
    final XQuery query = new XQuery(
      "<results> { for $x in (100000000000000000,10000000000000000,1000000000000000,100000000000000,10000000000000,1000000000000,100000000000,10000000000,1000000000,100000000,10000000,1000000,100000,10000,1000,100,10,1,0) order by xs:decimal($x) descending return xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>100000000000000000 10000000000000000 1000000000000000 100000000000000 10000000000000 1000000000000 100000000000 10000000000 1000000000 100000000 10000000 1000000 100000 10000 1000 100 10 1 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "xs:double($x) ", where $x is a set of positive numbers and the ordering mode set to descending Use a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal37() {
    final XQuery query = new XQuery(
      "<results> { for $x in (100000000000000000,10000000000000000,1000000000000000,100000000000000,10000000000000,1000000000000,100000000000,10000000000,1000000000,100000000,10000000,1000000,100000,10000,1000,100,10,1,0) order by xs:double($x) descending return xs:double($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>1.0E17 1.0E16 1.0E15 1.0E14 1.0E13 1.0E12 1.0E11 1.0E10 1.0E9 1.0E8 1.0E7 1.0E6 100000 10000 1000 100 10 1 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "xs:integer($x) ", where $x is a set of positive numbers and the ordering mode set to descending Use a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal38() {
    final XQuery query = new XQuery(
      "<results> { for $x in (100000000000000000,10000000000000000,1000000000000000,100000000000000,10000000000000,1000000000000,100000000000,10000000000,1000000000,100000000,10000000,1000000,100000,10000,1000,100,10,1,0) order by xs:integer($x) descending return xs:integer($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>100000000000000000 10000000000000000 1000000000000000 100000000000000 10000000000000 1000000000000 100000000000 10000000000 1000000000 100000000 10000000 1000000 100000 10000 1000 100 10 1 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "string($x) ", where $x is a set of positive numbers and the ordering mode set to ascending Use a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal39() {
    final XQuery query = new XQuery(
      "<results> { for $x in (100000000000000000,10000000000000000,1000000000000000,100000000000000,10000000000000,1000000000000,100000000000,10000000000,1000000000,100000000,10000000,1000000,100000,10000,1000,100,10,1,0) order by string($x) ascending return string($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 1 10 100 1000 10000 100000 1000000 10000000 100000000 1000000000 10000000000 100000000000 1000000000000 10000000000000 100000000000000 1000000000000000 10000000000000000 100000000000000000</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "concat($x,"()") ", where $x is a set of Strings and the ordering mode set to descending Uses a locally defined string .
   */
  @org.junit.Test
  public void orderbylocal4() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by concat(xs:string($x),\"()\") descending return concat(xs:string($x),\"()\") } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>Z String() Y String() X String() W String() V String() U String() T String() S String() R String() P String() O String() N String() M String() L String() K String() J String() I String() H String() G String() F String() E String() D String() C String() B String() A String()</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "$x ", where $x is a set of small positive numbers and the ordering mode set to ascending Use a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal40() {
    final XQuery query = new XQuery(
      "<results> { for $x in (0.000000000000000001,0.00000000000000001,0.0000000000000001,0.000000000000001,0.00000000000001,0.0000000000001,0.000000000001,0.00000000001,0.0000000001,0.000000001,0.00000001,0.0000001,0.000001,0.00001,0.0001,0.001,0.01,0.1,0.0) order by $x ascending return $x } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 0.000000000000000001 0.00000000000000001 0.0000000000000001 0.000000000000001 0.00000000000001 0.0000000000001 0.000000000001 0.00000000001 0.0000000001 0.000000001 0.00000001 0.0000001 0.000001 0.00001 0.0001 0.001 0.01 0.1</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "$x ", where $x is a set of small positive numbers and the ordering mode set to descending Uses a locally defined sequence .
   */
  @org.junit.Test
  public void orderbylocal41() {
    final XQuery query = new XQuery(
      "<results> { for $x in (0.000000000000000001,0.00000000000000001,0.0000000000000001,0.000000000000001,0.00000000000001,0.0000000000001,0.000000000001,0.00000000001,0.0000000001,0.000000001,0.00000001,0.0000001,0.000001,0.00001,0.0001,0.001,0.01,0.1,0.0) order by xs:decimal($x) descending return xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0.1 0.01 0.001 0.0001 0.00001 0.000001 0.0000001 0.00000001 0.000000001 0.0000000001 0.00000000001 0.000000000001 0.0000000000001 0.00000000000001 0.000000000000001 0.0000000000000001 0.00000000000000001 0.000000000000000001 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "($x + $x) ", where $x is a set of small positive numbers and the ordering mode set to descending Use a locally defined sequence .
   */
  @org.junit.Test
  public void orderbylocal42() {
    final XQuery query = new XQuery(
      "<results> { for $x in (0.000000000000000001,0.00000000000000001,0.0000000000000001,0.000000000000001,0.00000000000001,0.0000000000001,0.000000000001,0.00000000001,0.0000000001,0.000000001,0.00000001,0.0000001,0.000001,0.00001,0.0001,0.001,0.01,0.1,0.0) order by (xs:decimal($x) + xs:decimal($x)) descending return xs:decimal($x) + xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0.2 0.02 0.002 0.0002 0.00002 0.000002 0.0000002 0.00000002 0.000000002 0.0000000002 0.00000000002 0.000000000002 0.0000000000002 0.00000000000002 0.000000000000002 0.0000000000000002 0.00000000000000002 0.000000000000000002 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "$x is $x ", where $x is a set of small positive numbers and the ordering mode set to ascending .
   */
  @org.junit.Test
  public void orderbylocal43() {
    final XQuery query = new XQuery(
      "<results>{ for $x in (<a>0.000000000000000001</a>,<a>0.00000000000000001</a>,<a>0.0000000000000001</a>,<a>0.000000000000001</a>,<a>0.00000000000001</a>,<a>0.0000000000001</a>,<a>0.000000000001</a>,<a>0.00000000001</a>,<a>0.0000000001</a>,<a>0.000000001</a>,<a>0.00000001</a>,<a>0.0000001</a>,<a>0.000001</a>,<a>0.00001</a>,<a>0.0001</a>,<a>0.001</a>,<a>0.01</a>,<a>0.1</a>,<a>0.0</a>) order by $x is $x ascending return $x is $x}</results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>true true true true true true true true true true true true true true true true true true true</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "$x is $x ", where $x is a set of small positive numbers and the ordering mode set to descending .
   */
  @org.junit.Test
  public void orderbylocal44() {
    final XQuery query = new XQuery(
      "<results>{ for $x in (<a>0.000000000000000001</a>,<a>0.00000000000000001</a>,<a>0.0000000000000001</a>,<a>0.000000000000001</a>,<a>0.00000000000001</a>,<a>0.0000000000001</a>,<a>0.000000000001</a>,<a>0.00000000001</a>,<a>0.0000000001</a>,<a>0.000000001</a>,<a>0.00000001</a>,<a>0.0000001</a>,<a>0.000001</a>,<a>0.00001</a>,<a>0.0001</a>,<a>0.001</a>,<a>0.01</a>,<a>0.1</a>,<a>0.0</a>) order by $x is $x descending return $x is $x }</results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>true true true true true true true true true true true true true true true true true true true</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "xs:float($x) ", where $x is a set of small positive numbers and the ordering mode set to descending Use a locally defined sequence .
   */
  @org.junit.Test
  public void orderbylocal45() {
    final XQuery query = new XQuery(
      "<results> { for $x in (0.000000000000000001,0.00000000000000001,0.0000000000000001,0.000000000000001,0.00000000000001,0.0000000000001,0.000000000001,0.00000000001,0.0000000001,0.000000001,0.00000001,0.0000001,0.000001,0.00001,0.0001,0.001,0.01,0.1,0.0) order by xs:float($x) descending return xs:float($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0.1 0.01 0.001 0.0001 0.00001 0.000001 1.0E-7 1.0E-8 1.0E-9 1.0E-10 1.0E-11 1.0E-12 1.0E-13 1.0E-14 1.0E-15 1.0E-16 1.0E-17 1.0E-18 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "xs:decimal($x) ", where $x is a set of small positive numbers and the ordering mode set to descending Use a locally defined sequence .
   */
  @org.junit.Test
  public void orderbylocal46() {
    final XQuery query = new XQuery(
      "<results> { for $x in (0.000000000000000001,0.00000000000000001,0.0000000000000001,0.000000000000001,0.00000000000001,0.0000000000001,0.000000000001,0.00000000001,0.0000000001,0.000000001,0.00000001,0.0000001,0.000001,0.00001,0.0001,0.001,0.01,0.1,0.0) order by xs:decimal($x) descending return xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0.1 0.01 0.001 0.0001 0.00001 0.000001 0.0000001 0.00000001 0.000000001 0.0000000001 0.00000000001 0.000000000001 0.0000000000001 0.00000000000001 0.000000000000001 0.0000000000000001 0.00000000000000001 0.000000000000000001 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "xs:double($x) ", where $x is a set of small positive numbers and the ordering mode set to descending Use a locally defined sequence .
   */
  @org.junit.Test
  public void orderbylocal47() {
    final XQuery query = new XQuery(
      "<results> { for $x in (0.000000000000000001,0.00000000000000001,0.0000000000000001,0.000000000000001,0.00000000000001,0.0000000000001,0.000000000001,0.00000000001,0.0000000001,0.000000001,0.00000001,0.0000001,0.000001,0.00001,0.0001,0.001,0.01,0.1,0.0) order by xs:double($x) descending return xs:double($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0.1 0.01 0.001 0.0001 0.00001 0.000001 1.0E-7 1.0E-8 1.0E-9 1.0E-10 1.0E-11 1.0E-12 1.0E-13 1.0E-14 1.0E-15 1.0E-16 1.0E-17 1.0E-18 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "string($x) ", where $x is a set of small positive numbers and the ordering mode set to ascending Use a locally defined sequence .
   */
  @org.junit.Test
  public void orderbylocal49() {
    final XQuery query = new XQuery(
      "<results> { for $x in (0.000000000000000001,0.00000000000000001,0.0000000000000001,0.000000000000001,0.00000000000001,0.0000000000001,0.000000000001,0.00000000001,0.0000000001,0.000000001,0.00000001,0.0000001,0.000001,0.00001,0.0001,0.001,0.01,0.1,0.0) order by string($x) ascending return string($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 0.000000000000000001 0.00000000000000001 0.0000000000000001 0.000000000000001 0.00000000000001 0.0000000000001 0.000000000001 0.00000000001 0.0000000001 0.000000001 0.00000001 0.0000001 0.000001 0.00001 0.0001 0.001 0.01 0.1</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "concat($x,"another String After") ", where $x is a set of Strings and the ordering mode set to ascending Uses Locally define sequence .
   */
  @org.junit.Test
  public void orderbylocal5() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by concat(xs:string($x),\"another String After\") ascending return concat(xs:string($x),\"another String After\") } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>A Stringanother String After B Stringanother String After C Stringanother String After D Stringanother String After E Stringanother String After F Stringanother String After G Stringanother String After H Stringanother String After I Stringanother String After J Stringanother String After K Stringanother String After L Stringanother String After M Stringanother String After N Stringanother String After O Stringanother String After P Stringanother String After R Stringanother String After S Stringanother String After T Stringanother String After U Stringanother String After V Stringanother String After W Stringanother String After X Stringanother String After Y Stringanother String After Z Stringanother String After</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "$x ", where $x is a set of small negative numbers and the ordering mode set to ascending Use a locally definded sequence .
   */
  @org.junit.Test
  public void orderbylocal50() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-0.000000000000000001,-0.00000000000000001,-0.0000000000000001,-0.000000000000001,-0.00000000000001,-0.0000000000001,-0.000000000001,-0.00000000001,-0.0000000001,-0.000000001,-0.00000001,-0.0000001,-0.000001,-0.00001,-0.0001,-0.001,-0.01,-0.0,-0.1) order by xs:decimal($x) ascending return xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>-0.1 -0.01 -0.001 -0.0001 -0.00001 -0.000001 -0.0000001 -0.00000001 -0.000000001 -0.0000000001 -0.00000000001 -0.000000000001 -0.0000000000001 -0.00000000000001 -0.000000000000001 -0.0000000000000001 -0.00000000000000001 -0.000000000000000001 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "$x ", where $x is a set of small negative numbers and the ordering mode set to descending Use a locally defined sequence .
   */
  @org.junit.Test
  public void orderbylocal51() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-0.000000000000000001,-0.00000000000000001,-0.0000000000000001,-0.000000000000001,-0.00000000000001,-0.0000000000001,-0.000000000001,-0.00000000001,-0.0000000001,-0.000000001,-0.00000001,-0.0000001,-0.000001,-0.00001,-0.0001,-0.001,-0.01,-0.0,-0.1) order by xs:decimal($x) descending return xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 -0.000000000000000001 -0.00000000000000001 -0.0000000000000001 -0.000000000000001 -0.00000000000001 -0.0000000000001 -0.000000000001 -0.00000000001 -0.0000000001 -0.000000001 -0.00000001 -0.0000001 -0.000001 -0.00001 -0.0001 -0.001 -0.01 -0.1</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "($x + $x) ", where $x is a set of small negative numbers and the ordering mode set to descending Use a locally defined sequence .
   */
  @org.junit.Test
  public void orderbylocal52() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-0.000000000000000001,-0.00000000000000001,-0.0000000000000001,-0.000000000000001,-0.00000000000001,-0.0000000000001,-0.000000000001,-0.00000000001,-0.0000000001,-0.000000001,-0.00000001,-0.0000001,-0.000001,-0.00001,-0.0001,-0.001,-0.01,-0.0,-0.1) order by (xs:decimal($x) + xs:decimal($x)) descending return xs:decimal($x) + xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 -0.000000000000000002 -0.00000000000000002 -0.0000000000000002 -0.000000000000002 -0.00000000000002 -0.0000000000002 -0.000000000002 -0.00000000002 -0.0000000002 -0.000000002 -0.00000002 -0.0000002 -0.000002 -0.00002 -0.0002 -0.002 -0.02 -0.2</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "xs:float($x) ", where $x is a set of small negative numbers and the ordering mode set to descending Use a locally defined sequence .
   */
  @org.junit.Test
  public void orderbylocal55() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-0.000000000000000001,-0.00000000000000001,-0.0000000000000001,-0.000000000000001,-0.00000000000001,-0.0000000000001,-0.000000000001,-0.00000000001,-0.0000000001,-0.000000001,-0.00000001,-0.0000001,-0.000001,-0.00001,-0.0001,-0.001,-0.01,-0.0,-0.1) order by xs:float($x) descending return xs:float($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 -1.0E-18 -1.0E-17 -1.0E-16 -1.0E-15 -1.0E-14 -1.0E-13 -1.0E-12 -1.0E-11 -1.0E-10 -1.0E-9 -1.0E-8 -1.0E-7 -0.000001 -0.00001 -0.0001 -0.001 -0.01 -0.1</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "xs:decimal($x) ", where $x is a set of small negative numbers and the ordering mode set to descending Use a locally defined sequence .
   */
  @org.junit.Test
  public void orderbylocal56() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-0.000000000000000001,-0.00000000000000001,-0.0000000000000001,-0.000000000000001,-0.00000000000001,-0.0000000000001,-0.000000000001,-0.00000000001,-0.0000000001,-0.000000001,-0.00000001,-0.0000001,-0.000001,-0.00001,-0.0001,-0.001,-0.01,-0.0,-0.1) order by xs:decimal($x) descending return xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 -0.000000000000000001 -0.00000000000000001 -0.0000000000000001 -0.000000000000001 -0.00000000000001 -0.0000000000001 -0.000000000001 -0.00000000001 -0.0000000001 -0.000000001 -0.00000001 -0.0000001 -0.000001 -0.00001 -0.0001 -0.001 -0.01 -0.1</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "xs:double($x) ", where $x is a set of small negative numbers and the ordering mode set to descending Use a locally defined sequence .
   */
  @org.junit.Test
  public void orderbylocal57() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-0.000000000000000001,-0.00000000000000001,-0.0000000000000001,-0.000000000000001,-0.00000000000001,-0.0000000000001,-0.000000000001,-0.00000000001,-0.0000000001,-0.000000001,-0.00000001,-0.0000001,-0.000001,-0.00001,-0.0001,-0.001,-0.01,-0.0,-0.1) order by xs:double($x) descending return xs:double($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 -1.0E-18 -1.0E-17 -1.0E-16 -1.0E-15 -1.0E-14 -1.0E-13 -1.0E-12 -1.0E-11 -1.0E-10 -1.0E-9 -1.0E-8 -1.0E-7 -0.000001 -0.00001 -0.0001 -0.001 -0.01 -0.1</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "string($x) ", where $x is a set of small negative numbers and the ordering mode set to ascending Use a locally defined sequence .
   */
  @org.junit.Test
  public void orderbylocal59() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-0.000000000000000001,-0.00000000000000001,-0.0000000000000001,-0.000000000000001,-0.00000000000001,-0.0000000000001,-0.000000000001,-0.00000000001,-0.0000000001,-0.000000001,-0.00000001,-0.0000001,-0.000001,-0.00001,-0.0001,-0.001,-0.01,-0.0,-0.1) order by string($x) ascending return string($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>-0.000000000000000001 -0.00000000000000001 -0.0000000000000001 -0.000000000000001 -0.00000000000001 -0.0000000000001 -0.000000000001 -0.00000000001 -0.0000000001 -0.000000001 -0.00000001 -0.0000001 -0.000001 -0.00001 -0.0001 -0.001 -0.01 -0.1 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "concat($x,"another String After") ", where $x is a set of Strings and the ordering mode set to descending Uses a locally defined sequence .
   */
  @org.junit.Test
  public void orderbylocal6() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by concat(xs:string($x),\"another String After\") descending return concat(xs:string($x),\"another String After\") } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>Z Stringanother String After Y Stringanother String After X Stringanother String After W Stringanother String After V Stringanother String After U Stringanother String After T Stringanother String After S Stringanother String After R Stringanother String After P Stringanother String After O Stringanother String After N Stringanother String After M Stringanother String After L Stringanother String After K Stringanother String After J Stringanother String After I Stringanother String After H Stringanother String After G Stringanother String After F Stringanother String After E Stringanother String After D Stringanother String After C Stringanother String After B Stringanother String After A Stringanother String After</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with an unknown collation. Use a locally defined sequence .
   */
  @org.junit.Test
  public void orderbylocal60() {
    final XQuery query = new XQuery(
      "for $x in (\"A\",\"B\",\"C\") order by string($x) ascending collation \"http://nonexistentcollition.org/ifsupportedwoooayouarethebestQueryimplementation/makeitharder\" return string($x)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0076")
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "concat("another String Before",$x) ", where $x is a set of Strings and the ordering mode set to ascending uses a locally defined sequence .
   */
  @org.junit.Test
  public void orderbylocal7() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by concat(\"another String Before\",xs:string($x)) ascending return concat(\"another String Before\",xs:string($x)) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>another String BeforeA String another String BeforeB String another String BeforeC String another String BeforeD String another String BeforeE String another String BeforeF String another String BeforeG String another String BeforeH String another String BeforeI String another String BeforeJ String another String BeforeK String another String BeforeL String another String BeforeM String another String BeforeN String another String BeforeO String another String BeforeP String another String BeforeR String another String BeforeS String another String BeforeT String another String BeforeU String another String BeforeV String another String BeforeW String another String BeforeX String another String BeforeY String another String BeforeZ String</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "concat("another String Before",$x) ", where $x is a set of Strings and the ordering mode set to descending Uses a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbylocal8() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by concat(\"another String Before\",xs:string($x)) descending return concat(\"another String Before\",xs:string($x)) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>another String BeforeZ String another String BeforeY String another String BeforeX String another String BeforeW String another String BeforeV String another String BeforeU String another String BeforeT String another String BeforeS String another String BeforeR String another String BeforeP String another String BeforeO String another String BeforeN String another String BeforeM String another String BeforeL String another String BeforeK String another String BeforeJ String another String BeforeI String another String BeforeH String another String BeforeG String another String BeforeF String another String BeforeE String another String BeforeD String another String BeforeC String another String BeforeB String another String BeforeA String</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "concat($x,"") ", where $x is a set of Strings and the ordering mode set to descending Uses a locally defined sequence . .
   */
  @org.junit.Test
  public void orderbylocal9() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by concat(xs:string($x),\"\") descending return concat(xs:string($x),\"\") } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>Z String Y String X String W String V String U String T String S String R String P String O String N String M String L String K String J String I String H String G String F String E String D String C String B String A String</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "$x ", where $x is a set of Strings. .
   */
  @org.junit.Test
  public void orderbywithout1() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by xs:string($x) return xs:string($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>A String B String C String D String E String F String G String H String I String J String K String L String M String N String O String P String R String S String T String U String V String W String X String Y String Z String</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "count($x) ", where $x is a set of node with strings as content. .
   */
  @org.junit.Test
  public void orderbywithout10() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by count(xs:string($x)) return count(xs:string($x)) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "$x ", where $x is a set of negative numbers casted as decimals. .
   */
  @org.junit.Test
  public void orderbywithout11() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-100000000000000000,-10000000000000000,-1000000000000000,-100000000000000,-10000000000000,-1000000000000,-100000000000,-10000000000,-1000000000,-100000000,-10000000,-1000000,-100000,-10000,-1000,-100,-10,-1,-0) order by xs:decimal($x) return xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>-100000000000000000 -10000000000000000 -1000000000000000 -100000000000000 -10000000000000 -1000000000000 -100000000000 -10000000000 -1000000000 -100000000 -10000000 -1000000 -100000 -10000 -1000 -100 -10 -1 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "($x + $x) ", where $x is a set of negative numbers casted as decimals. .
   */
  @org.junit.Test
  public void orderbywithout12() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-100000000000000000,-10000000000000000,-1000000000000000,-100000000000000,-10000000000000,-1000000000000,-100000000000,-10000000000,-1000000000,-100000000,-10000000,-1000000,-100000,-10000,-1000,-100,-10,-1,-0) order by (xs:decimal($x) + xs:decimal($x)) return xs:decimal($x) + xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>-200000000000000000 -20000000000000000 -2000000000000000 -200000000000000 -20000000000000 -2000000000000 -200000000000 -20000000000 -2000000000 -200000000 -20000000 -2000000 -200000 -20000 -2000 -200 -20 -2 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "$x is $x ", where $x is a set of nodes with negative numbers as content. .
   */
  @org.junit.Test
  public void orderbywithout13() {
    final XQuery query = new XQuery(
      "<results> { for $x in (<orderData>-100000000000000000</orderData>,<orderData>-10000000000000000</orderData>,<orderData>-1000000000000000</orderData>,<orderData>-100000000000000</orderData>, <orderData>-10000000000000</orderData>,<orderData>-1000000000000</orderData>,<orderData>-100000000000</orderData>,<orderData>-10000000000</orderData>,<orderData>-1000000000</orderData>, <orderData>-100000000</orderData>,<orderData>-10000000</orderData>,<orderData>-1000000</orderData>,<orderData>-100000</orderData>,<orderData>-10000</orderData>,<orderData>-1000</orderData>, <orderData>-100</orderData>,<orderData>-10</orderData>,<orderData>-1</orderData>,<orderData>-0</orderData>) order by $x is $x return $x is $x } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>true true true true true true true true true true true true true true true true true true true</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "xs:float($x) ", where $x is a set of negative numbers. .
   */
  @org.junit.Test
  public void orderbywithout14() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-100000000000000000,-10000000000000000,-1000000000000000,-100000000000000,-10000000000000,-1000000000000,-100000000000,-10000000000,-1000000000,-100000000,-10000000,-1000000,-100000,-10000,-1000,-100,-10,-1,-0) order by xs:float($x) return xs:float($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>-1.0E17 -1.0E16 -1.0E15 -1.0E14 -1.0E13 -1.0E12 -1.0E11 -1.0E10 -1.0E9 -1.0E8 -1.0E7 -1.0E6 -100000 -10000 -1000 -100 -10 -1 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "xs:double($x) ", where $x is a set of negative numbers. .
   */
  @org.junit.Test
  public void orderbywithout15() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-100000000000000000,-10000000000000000,-1000000000000000,-100000000000000,-10000000000000,-1000000000000,-100000000000,-10000000000,-1000000000,-100000000,-10000000,-1000000,-100000,-10000,-1000,-100,-10,-1,-0) order by xs:double($x) return xs:double($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>-1.0E17 -1.0E16 -1.0E15 -1.0E14 -1.0E13 -1.0E12 -1.0E11 -1.0E10 -1.0E9 -1.0E8 -1.0E7 -1.0E6 -100000 -10000 -1000 -100 -10 -1 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "xs:integer($x) ", where $x is a set of negative numbers. .
   */
  @org.junit.Test
  public void orderbywithout16() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-100000000000000000,-10000000000000000,-1000000000000000,-100000000000000,-10000000000000,-1000000000000,-100000000000,-10000000000,-1000000000,-100000000,-10000000,-1000000,-100000,-10000,-1000,-100,-10,-1,-0) order by xs:integer($x) return xs:integer($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>-100000000000000000 -10000000000000000 -1000000000000000 -100000000000000 -10000000000000 -1000000000000 -100000000000 -10000000000 -1000000000 -100000000 -10000000 -1000000 -100000 -10000 -1000 -100 -10 -1 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "($x * -1) ", where $x is a set of negative numbers. Use a locally defined sequence. .
   */
  @org.junit.Test
  public void orderbywithout17() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-100000000000000000,-10000000000000000,-1000000000000000,-100000000000000,-10000000000000,-1000000000000,-100000000000,-10000000000,-1000000000,-100000000,-10000000,-1000000,-100000,-10000,-1000,-100,-10,-1,-0) order by (xs:decimal($x) * -1) return (xs:decimal($x) * -1) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 1 10 100 1000 10000 100000 1000000 10000000 100000000 1000000000 10000000000 100000000000 1000000000000 10000000000000 100000000000000 1000000000000000 10000000000000000 100000000000000000</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "$x ", where $x is a set of positive numbers. .
   */
  @org.junit.Test
  public void orderbywithout18() {
    final XQuery query = new XQuery(
      "<results> { for $x in (100000000000000000,10000000000000000,1000000000000000,100000000000000,10000000000000,1000000000000,100000000000,10000000000,1000000000,100000000,10000000,1000000,100000,10000,1000,100,10,1,0) order by xs:decimal($x) return xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 1 10 100 1000 10000 100000 1000000 10000000 100000000 1000000000 10000000000 100000000000 1000000000000 10000000000000 100000000000000 1000000000000000 10000000000000000 100000000000000000</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "($x + $x) ", where $x is a set of positive numbers. .
   */
  @org.junit.Test
  public void orderbywithout19() {
    final XQuery query = new XQuery(
      "<results> { for $x in (100000000000000000,10000000000000000,1000000000000000,100000000000000,10000000000000,1000000000000,100000000000,10000000000,1000000000,100000000,10000000,1000000,100000,10000,1000,100,10,1,0) order by (xs:decimal($x) + xs:decimal($x)) return xs:decimal($x) + xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 2 20 200 2000 20000 200000 2000000 20000000 200000000 2000000000 20000000000 200000000000 2000000000000 20000000000000 200000000000000 2000000000000000 20000000000000000 200000000000000000</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "concat($x,"()") ", where $x is a set of Strings. .
   */
  @org.junit.Test
  public void orderbywithout2() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by concat(xs:string($x),\"()\") return concat(xs:string($x),\"()\") } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>A String() B String() C String() D String() E String() F String() G String() H String() I String() J String() K String() L String() M String() N String() O String() P String() R String() S String() T String() U String() V String() W String() X String() Y String() Z String()</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "$x is $x ", where $x is a set of nodes with positive numbers as content. .
   */
  @org.junit.Test
  public void orderbywithout20() {
    final XQuery query = new XQuery(
      "<results> { for $x in (<orderData>100000000000000000</orderData>,<orderData>10000000000000000</orderData>,<orderData>1000000000000000</orderData>, <orderData>100000000000000</orderData>,<orderData>10000000000000</orderData>,<orderData>1000000000000</orderData>,<orderData>100000000000</orderData>, <orderData>10000000000</orderData>,<orderData>1000000000</orderData>,<orderData>100000000</orderData>,<orderData>10000000</orderData>, <orderData>1000000</orderData>,<orderData>100000</orderData>,<orderData>10000</orderData>,<orderData>1000</orderData>,<orderData>100</orderData>, <orderData>10</orderData>,<orderData>1</orderData>,<orderData>0</orderData>) order by $x is $x return $x is $x } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>true true true true true true true true true true true true true true true true true true true</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "xs:float($x) ", where $x is a set of positive numbers. .
   */
  @org.junit.Test
  public void orderbywithout21() {
    final XQuery query = new XQuery(
      "<results> { for $x in (100000000000000000,10000000000000000,1000000000000000,100000000000000,10000000000000,1000000000000,100000000000,10000000000,1000000000,100000000,10000000,1000000,100000,10000,1000,100,10,1,0) order by xs:float($x) return xs:float($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 1 10 100 1000 10000 100000 1.0E6 1.0E7 1.0E8 1.0E9 1.0E10 1.0E11 1.0E12 1.0E13 1.0E14 1.0E15 1.0E16 1.0E17</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWR expression set to "xs:decimal($x)", where $x is a set of positive numbers. .
   */
  @org.junit.Test
  public void orderbywithout22() {
    final XQuery query = new XQuery(
      "<results> { for $x in (100000000000000000,10000000000000000,1000000000000000,100000000000000,10000000000000,1000000000000,100000000000,10000000000,1000000000,100000000,10000000,1000000,100000,10000,1000,100,10,1,0) order by xs:decimal($x) return xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 1 10 100 1000 10000 100000 1000000 10000000 100000000 1000000000 10000000000 100000000000 1000000000000 10000000000000 100000000000000 1000000000000000 10000000000000000 100000000000000000</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "xs:double($x)", where $x is a set of positive numbers. .
   */
  @org.junit.Test
  public void orderbywithout23() {
    final XQuery query = new XQuery(
      "<results> { for $x in (100000000000000000,10000000000000000,1000000000000000,100000000000000,10000000000000,1000000000000,100000000000,10000000000,1000000000,100000000,10000000,1000000,100000,10000,1000,100,10,1,0) order by xs:double($x) return xs:double($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 1 10 100 1000 10000 100000 1.0E6 1.0E7 1.0E8 1.0E9 1.0E10 1.0E11 1.0E12 1.0E13 1.0E14 1.0E15 1.0E16 1.0E17</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "xs:integer($x)", where $x is a set of positive numbers. .
   */
  @org.junit.Test
  public void orderbywithout24() {
    final XQuery query = new XQuery(
      "<results> { for $x in (100000000000000000,10000000000000000,1000000000000000,100000000000000,10000000000000,1000000000000,100000000000,10000000000,1000000000,100000000,10000000,1000000,100000,10000,1000,100,10,1,0) order by xs:integer($x) return xs:integer($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 1 10 100 1000 10000 100000 1000000 10000000 100000000 1000000000 10000000000 100000000000 1000000000000 10000000000000 100000000000000 1000000000000000 10000000000000000 100000000000000000</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "string($x)", where $x is a set of positive numbers. .
   */
  @org.junit.Test
  public void orderbywithout25() {
    final XQuery query = new XQuery(
      "<results> { for $x in (100000000000000000,10000000000000000,1000000000000000,100000000000000,10000000000000,1000000000000,100000000000,10000000000,1000000000,100000000,10000000,1000000,100000,10000,1000,100,10,1,0) order by string($x) return string($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 1 10 100 1000 10000 100000 1000000 10000000 100000000 1000000000 10000000000 100000000000 1000000000000 10000000000000 100000000000000 1000000000000000 10000000000000000 100000000000000000</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "$x", where $x is a set of small positive numbers. .
   */
  @org.junit.Test
  public void orderbywithout26() {
    final XQuery query = new XQuery(
      "<results> { for $x in (0.000000000000000001,0.00000000000000001,0.0000000000000001,0.000000000000001,0.00000000000001,0.0000000000001,0.000000000001,0.00000000001,0.0000000001,0.000000001,0.00000001,0.0000001,0.000001,0.00001,0.0001,0.001,0.01,0.1,0.0) order by $x return $x } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 0.000000000000000001 0.00000000000000001 0.0000000000000001 0.000000000000001 0.00000000000001 0.0000000000001 0.000000000001 0.00000000001 0.0000000001 0.000000001 0.00000001 0.0000001 0.000001 0.00001 0.0001 0.001 0.01 0.1</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "$x", where $x is a set of small positive numbers casted as decimals. .
   */
  @org.junit.Test
  public void orderbywithout27() {
    final XQuery query = new XQuery(
      "<results> { for $x in (0.000000000000000001,0.00000000000000001,0.0000000000000001,0.000000000000001,0.00000000000001,0.0000000000001,0.000000000001,0.00000000001,0.0000000001,0.000000001,0.00000001,0.0000001,0.000001,0.00001,0.0001,0.001,0.01,0.1,0.0) order by xs:decimal($x) return xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 0.000000000000000001 0.00000000000000001 0.0000000000000001 0.000000000000001 0.00000000000001 0.0000000000001 0.000000000001 0.00000000001 0.0000000001 0.000000001 0.00000001 0.0000001 0.000001 0.00001 0.0001 0.001 0.01 0.1</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "($x + $x) ", where $x is a set of small positive numbers casted as decimals. .
   */
  @org.junit.Test
  public void orderbywithout28() {
    final XQuery query = new XQuery(
      "<results> { for $x in (0.000000000000000001,0.00000000000000001,0.0000000000000001,0.000000000000001,0.00000000000001,0.0000000000001,0.000000000001,0.00000000001,0.0000000001,0.000000001,0.00000001,0.0000001,0.000001,0.00001,0.0001,0.001,0.01,0.1,0.0) order by (xs:decimal($x) + xs:decimal($x)) return xs:decimal($x) + xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 0.000000000000000002 0.00000000000000002 0.0000000000000002 0.000000000000002 0.00000000000002 0.0000000000002 0.000000000002 0.00000000002 0.0000000002 0.000000002 0.00000002 0.0000002 0.000002 0.00002 0.0002 0.002 0.02 0.2</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "$x is $x ", where $x is a set nodes with small positive numbers as content. .
   */
  @org.junit.Test
  public void orderbywithout29() {
    final XQuery query = new XQuery(
      "<results> { for $x in (<orderData>0.000000000000000001</orderData>,<orderData>0.00000000000000001</orderData>,<orderData>0.0000000000000001</orderData>,<orderData>0.000000000000001</orderData>, <orderData>0.00000000000001</orderData>,<orderData>0.0000000000001</orderData>,<orderData>0.000000000001</orderData>,<orderData>0.00000000001</orderData>,<orderData>0.0000000001</orderData>, <orderData>0.000000001</orderData>,<orderData>0.00000001</orderData>,<orderData>0.0000001</orderData>,<orderData>0.000001</orderData>,<orderData>0.00001</orderData>, <orderData>0.0001</orderData>,<orderData>0.001</orderData>,<orderData>0.01</orderData>,<orderData>0.1</orderData>,<orderData>0.0</orderData>) order by $x is $x return $x is $x } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>true true true true true true true true true true true true true true true true true true true</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "concat($x,"another String After") ", where $x is a set of Strings. .
   */
  @org.junit.Test
  public void orderbywithout3() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by concat(xs:string($x),\"another String After\") return concat(xs:string($x),\"another String After\") } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>A Stringanother String After B Stringanother String After C Stringanother String After D Stringanother String After E Stringanother String After F Stringanother String After G Stringanother String After H Stringanother String After I Stringanother String After J Stringanother String After K Stringanother String After L Stringanother String After M Stringanother String After N Stringanother String After O Stringanother String After P Stringanother String After R Stringanother String After S Stringanother String After T Stringanother String After U Stringanother String After V Stringanother String After W Stringanother String After X Stringanother String After Y Stringanother String After Z Stringanother String After</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "xs:float($x) ", where $x is a set of small positive numbers. .
   */
  @org.junit.Test
  public void orderbywithout30() {
    final XQuery query = new XQuery(
      "<results> { for $x in (0.000000000000000001,0.00000000000000001,0.0000000000000001,0.000000000000001,0.00000000000001,0.0000000000001,0.000000000001,0.00000000001,0.0000000001,0.000000001,0.00000001,0.0000001,0.000001,0.00001,0.0001,0.001,0.01,0.1,0.0) order by xs:float($x) return xs:float($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 1.0E-18 1.0E-17 1.0E-16 1.0E-15 1.0E-14 1.0E-13 1.0E-12 1.0E-11 1.0E-10 1.0E-9 1.0E-8 1.0E-7 0.000001 0.00001 0.0001 0.001 0.01 0.1</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "xs:decimal($x) ", where $x is a set of small positive numbers. .
   */
  @org.junit.Test
  public void orderbywithout31() {
    final XQuery query = new XQuery(
      "<results> { for $x in (0.000000000000000001,0.00000000000000001,0.0000000000000001,0.000000000000001,0.00000000000001,0.0000000000001,0.000000000001,0.00000000001,0.0000000001,0.000000001,0.00000001,0.0000001,0.000001,0.00001,0.0001,0.001,0.01,0.1,0.0) order by xs:decimal($x) return xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 0.000000000000000001 0.00000000000000001 0.0000000000000001 0.000000000000001 0.00000000000001 0.0000000000001 0.000000000001 0.00000000001 0.0000000001 0.000000001 0.00000001 0.0000001 0.000001 0.00001 0.0001 0.001 0.01 0.1</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "xs:double($x) ", where $x is a set of small positive numbers. .
   */
  @org.junit.Test
  public void orderbywithout32() {
    final XQuery query = new XQuery(
      "<results> { for $x in (0.000000000000000001,0.00000000000000001,0.0000000000000001,0.000000000000001,0.00000000000001,0.0000000000001,0.000000000001,0.00000000001,0.0000000001,0.000000001,0.00000001,0.0000001,0.000001,0.00001,0.0001,0.001,0.01,0.1,0.0) order by xs:double($x) return xs:double($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 1.0E-18 1.0E-17 1.0E-16 1.0E-15 1.0E-14 1.0E-13 1.0E-12 1.0E-11 1.0E-10 1.0E-9 1.0E-8 1.0E-7 0.000001 0.00001 0.0001 0.001 0.01 0.1</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "string($x) ", where $x is a set of small positive numbers. .
   */
  @org.junit.Test
  public void orderbywithout33() {
    final XQuery query = new XQuery(
      "<results> { for $x in (0.000000000000000001,0.00000000000000001,0.0000000000000001,0.000000000000001,0.00000000000001,0.0000000000001,0.000000000001,0.00000000001,0.0000000001,0.000000001,0.00000001,0.0000001,0.000001,0.00001,0.0001,0.001,0.01,0.1,0.0) order by string($x) return string($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>0 0.000000000000000001 0.00000000000000001 0.0000000000000001 0.000000000000001 0.00000000000001 0.0000000000001 0.000000000001 0.00000000001 0.0000000001 0.000000001 0.00000001 0.0000001 0.000001 0.00001 0.0001 0.001 0.01 0.1</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "$x ", where $x is a set of small negative numbers casted as decimals. .
   */
  @org.junit.Test
  public void orderbywithout34() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-0.000000000000000001,-0.00000000000000001,-0.0000000000000001,-0.000000000000001,-0.00000000000001,-0.0000000000001,-0.000000000001,-0.00000000001,-0.0000000001,-0.000000001,-0.00000001,-0.0000001,-0.000001,-0.00001,-0.0001,-0.001,-0.01,-0.0,-0.1) order by xs:decimal($x) return xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>-0.1 -0.01 -0.001 -0.0001 -0.00001 -0.000001 -0.0000001 -0.00000001 -0.000000001 -0.0000000001 -0.00000000001 -0.000000000001 -0.0000000000001 -0.00000000000001 -0.000000000000001 -0.0000000000000001 -0.00000000000000001 -0.000000000000000001 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "($x + $x) ", where $x is a set of small negative numbers casted as decimals. .
   */
  @org.junit.Test
  public void orderbywithout35() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-0.000000000000000001,-0.00000000000000001,-0.0000000000000001,-0.000000000000001,-0.00000000000001,-0.0000000000001,-0.000000000001,-0.00000000001,-0.0000000001,-0.000000001,-0.00000001,-0.0000001,-0.000001,-0.00001,-0.0001,-0.001,-0.01,-0.0,-0.1) order by (xs:decimal($x) + xs:decimal($x)) return xs:decimal($x) + xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>-0.2 -0.02 -0.002 -0.0002 -0.00002 -0.000002 -0.0000002 -0.00000002 -0.000000002 -0.0000000002 -0.00000000002 -0.000000000002 -0.0000000000002 -0.00000000000002 -0.000000000000002 -0.0000000000000002 -0.00000000000000002 -0.000000000000000002 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "$x is $x ", where $x is a set of nodes with small negative numbers as argument. .
   */
  @org.junit.Test
  public void orderbywithout36() {
    final XQuery query = new XQuery(
      "<results> { for $x in (<orderData>-0.000000000000000001</orderData>,<orderData>-0.00000000000000001</orderData>,<orderData>-0.0000000000000001</orderData>,<orderData>-0.000000000000001</orderData>,<orderData>-0.00000000000001</orderData>,<orderData>-0.0000000000001</orderData>, <orderData>-0.000000000001</orderData>,<orderData>-0.00000000001</orderData>,<orderData>-0.0000000001</orderData>,<orderData>-0.000000001</orderData>, <orderData>-0.00000001</orderData>,<orderData>-0.0000001</orderData>,<orderData>-0.000001</orderData>,<orderData>-0.00001</orderData>,<orderData>-0.0001</orderData>,<orderData>-0.001</orderData>,<orderData>-0.01</orderData>,<orderData>-0.0</orderData>, <orderData>-0.1</orderData>) order by $x is $x return $x is $x } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>true true true true true true true true true true true true true true true true true true true</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "xs:float($x) ", where $x is a set of small negative numbers. .
   */
  @org.junit.Test
  public void orderbywithout37() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-0.000000000000000001,-0.00000000000000001,-0.0000000000000001,-0.000000000000001,-0.00000000000001,-0.0000000000001,-0.000000000001,-0.00000000001,-0.0000000001,-0.000000001,-0.00000001,-0.0000001,-0.000001,-0.00001,-0.0001,-0.001,-0.01,-0.0,-0.1) order by xs:float($x) return xs:float($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>-0.1 -0.01 -0.001 -0.0001 -0.00001 -0.000001 -1.0E-7 -1.0E-8 -1.0E-9 -1.0E-10 -1.0E-11 -1.0E-12 -1.0E-13 -1.0E-14 -1.0E-15 -1.0E-16 -1.0E-17 -1.0E-18 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "xs:decimal($x) ", where $x is a set of small negative numbers. .
   */
  @org.junit.Test
  public void orderbywithout38() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-0.000000000000000001,-0.00000000000000001,-0.0000000000000001,-0.000000000000001,-0.00000000000001,-0.0000000000001,-0.000000000001,-0.00000000001,-0.0000000001,-0.000000001,-0.00000001,-0.0000001,-0.000001,-0.00001,-0.0001,-0.001,-0.01,-0.0,-0.1) order by xs:decimal($x) return xs:decimal($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>-0.1 -0.01 -0.001 -0.0001 -0.00001 -0.000001 -0.0000001 -0.00000001 -0.000000001 -0.0000000001 -0.00000000001 -0.000000000001 -0.0000000000001 -0.00000000000001 -0.000000000000001 -0.0000000000000001 -0.00000000000000001 -0.000000000000000001 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "xs:double($x) ", where $x is a set of small negative numbers. .
   */
  @org.junit.Test
  public void orderbywithout39() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-0.000000000000000001,-0.00000000000000001,-0.0000000000000001,-0.000000000000001,-0.00000000000001,-0.0000000000001,-0.000000000001,-0.00000000001,-0.0000000001,-0.000000001,-0.00000001,-0.0000001,-0.000001,-0.00001,-0.0001,-0.001,-0.01,-0.0,-0.1) order by xs:double($x) return xs:double($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>-0.1 -0.01 -0.001 -0.0001 -0.00001 -0.000001 -1.0E-7 -1.0E-8 -1.0E-9 -1.0E-10 -1.0E-11 -1.0E-12 -1.0E-13 -1.0E-14 -1.0E-15 -1.0E-16 -1.0E-17 -1.0E-18 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "concat("another String Before",$x) ", where $x is a set of Strings. .
   */
  @org.junit.Test
  public void orderbywithout4() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by concat(\"another String Before\",xs:string($x)) return concat(\"another String Before\",xs:string($x)) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>another String BeforeA String another String BeforeB String another String BeforeC String another String BeforeD String another String BeforeE String another String BeforeF String another String BeforeG String another String BeforeH String another String BeforeI String another String BeforeJ String another String BeforeK String another String BeforeL String another String BeforeM String another String BeforeN String another String BeforeO String another String BeforeP String another String BeforeR String another String BeforeS String another String BeforeT String another String BeforeU String another String BeforeV String another String BeforeW String another String BeforeX String another String BeforeY String another String BeforeZ String</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "string($x) ", where $x is a set of small negative numbers. .
   */
  @org.junit.Test
  public void orderbywithout40() {
    final XQuery query = new XQuery(
      "<results> { for $x in (-0.000000000000000001,-0.00000000000000001,-0.0000000000000001,-0.000000000000001,-0.00000000000001,-0.0000000000001,-0.000000000001,-0.00000000001,-0.0000000001,-0.000000001,-0.00000001,-0.0000001,-0.000001,-0.00001,-0.0001,-0.001,-0.01,-0.0,-0.1) order by string($x) return string($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>-0.000000000000000001 -0.00000000000000001 -0.0000000000000001 -0.000000000000001 -0.00000000000001 -0.0000000000001 -0.000000000001 -0.00000000001 -0.0000000001 -0.000000001 -0.00000001 -0.0000001 -0.000001 -0.00001 -0.0001 -0.001 -0.01 -0.1 0</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "concat($x,"") ", where $x is a set of Strings. .
   */
  @org.junit.Test
  public void orderbywithout5() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by concat(xs:string($x),\"\") return concat(xs:string($x),\"\") } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>A String B String C String D String E String F String G String H String I String J String K String L String M String N String O String P String R String S String T String U String V String W String X String Y String Z String</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "concat("",$x) ", where $x is a set of Strings. .
   */
  @org.junit.Test
  public void orderbywithout6() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by concat(\"\",xs:string($x)) return concat(\"\",xs:string($x)) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>A String B String C String D String E String F String G String H String I String J String K String L String M String N String O String P String R String S String T String U String V String W String X String Y String Z String</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "$x is $x ", where $x is a set of nodes with strings as content. .
   */
  @org.junit.Test
  public void orderbywithout7() {
    final XQuery query = new XQuery(
      "<results> { for $x in (<a>A String</a>,<a>B String</a>,<a>C String</a>,<a>D String</a>,<a>E String</a>,<a>F String</a>,<a>G String</a>,<a>H String</a>,<a>I String</a>, <a>J String</a>,<a>K String</a>,<a>L String</a>,<a>M String</a>,<a>N String</a>,<a>O String</a>,<a>P String</a>,<a>R String</a>,<a>S String</a>,<a>T String</a>, <a>U String</a>,<a>V String</a>,<a>W String</a>,<a>X String</a>,<a>Y String</a>,<a>Z String</a>) order by $x is $x return $x is $x } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>true true true true true true true true true true true true true true true true true true true true true true true true true</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "concat($x,$x) ", where $x is a set of Strings. .
   */
  @org.junit.Test
  public void orderbywithout8() {
    final XQuery query = new XQuery(
      "<results> { for $x in (\"A String\",\"B String\",\"C String\",\"D String\",\"E String\",\"F String\",\"G String\",\"H String\",\"I String\",\"J String\",\"K String\",\"L String\",\"M String\",\"N String\",\"O String\",\"P String\",\"R String\",\"S String\",\"T String\",\"U String\",\"V String\",\"W String\",\"X String\",\"Y String\",\"Z String\") order by concat(xs:string($x),xs:string($x)) return concat(xs:string($x),xs:string($x)) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>A StringA String B StringB String C StringC String D StringD String E StringE String F StringF String G StringG String H StringH String I StringI String J StringJ String K StringK String L StringL String M StringM String N StringN String O StringO String P StringP String R StringR String S StringS String T StringT String U StringU String V StringV String W StringW String X StringX String Y StringY String Z StringZ String</results>", false)
    );
  }

  /**
   *  Evaluation of "order by" clause with the "order by" clause of a FLWOR expression set to "string-length($x) ", where $x is a set of Nodes with strings as content. .
   */
  @org.junit.Test
  public void orderbywithout9() {
    final XQuery query = new XQuery(
      "<results> { for $x in (<a>A String</a>,<a>B String</a>,<a>C String</a>,<a>D String</a>,<a>E String</a>,<a>F String</a>,<a>G String</a>,<a>H String</a>,<a>I String</a>, <a>J String</a>,<a>K String</a>,<a>L String</a>,<a>M String</a>,<a>N String</a>,<a>O String</a>,<a>P String</a>,<a>R String</a>,<a>S String</a>,<a>T String</a>, <a>U String</a>,<a>V String</a>,<a>W String</a>,<a>X String</a>,<a>Y String</a>,<a>Z String</a>) order by string-length($x) return string-length($x) } </results>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results>8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8 8</results>", false)
    );
  }
}
