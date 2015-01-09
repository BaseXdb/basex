package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the OrExpr and AndExpr productions.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdOrExpr extends QT3TestSet {

  /**
   *  A test whose essence is: `not("" or 0)`. .
   */
  @org.junit.Test
  public void kLogicExpr1() {
    final XQuery query = new XQuery(
      "not(\"\" or 0)",
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
   *  Non-empty xs:anyURIs in the left branch of an or-expression has an EBV value of true. .
   */
  @org.junit.Test
  public void kLogicExpr10() {
    final XQuery query = new XQuery(
      "not(xs:anyURI(\"\") or 0)",
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
   *  Non-empty xs:anyURIs in the right branch of an or-expression has an EBV value of true. .
   */
  @org.junit.Test
  public void kLogicExpr11() {
    final XQuery query = new XQuery(
      "not(0 or xs:anyURI(\"\"))",
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
   *  Non-empty xs:untypedAtomics in the left branch of an or-expression has an EBV value of true. .
   */
  @org.junit.Test
  public void kLogicExpr12() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"a string\") or 0",
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
   *  Non-empty xs:untypedAtomics in the right branch of an or-expression has an EBV value of true. .
   */
  @org.junit.Test
  public void kLogicExpr13() {
    final XQuery query = new XQuery(
      "0 or xs:untypedAtomic(\"a string\")",
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
   *  A test whose essence is: `true() or (0, current-time())[1] treat as xs:integer`. .
   */
  @org.junit.Test
  public void kLogicExpr14() {
    final XQuery query = new XQuery(
      "true() or (0, current-time())[1] treat as xs:integer",
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
   *  A test whose essence is: `(0, current-time())[1] treat as xs:integer or true()`. .
   */
  @org.junit.Test
  public void kLogicExpr15() {
    final XQuery query = new XQuery(
      "(0, current-time())[1] treat as xs:integer or true()",
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
   *  An 'or' expression having an operand which EBV cannot be extracted from. .
   */
  @org.junit.Test
  public void kLogicExpr16() {
    final XQuery query = new XQuery(
      "current-date() or 0",
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
   *  An 'or' expression having an operand which EBV cannot be extracted from. .
   */
  @org.junit.Test
  public void kLogicExpr17() {
    final XQuery query = new XQuery(
      "0 or current-date()",
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
   *  An 'or' expression whose operands EBV cannot be extracted from. .
   */
  @org.junit.Test
  public void kLogicExpr18() {
    final XQuery query = new XQuery(
      "current-date() or current-date()",
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
   *  An 'and' expression having an operand which EBV cannot be extracted from. .
   */
  @org.junit.Test
  public void kLogicExpr19() {
    final XQuery query = new XQuery(
      "current-date() and current-date()",
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
   *  A test whose essence is: `not(0 or 0)`. .
   */
  @org.junit.Test
  public void kLogicExpr2() {
    final XQuery query = new XQuery(
      "not(0 or 0)",
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
   *  An 'and' expression having an operand which EBV cannot be extracted from. .
   */
  @org.junit.Test
  public void kLogicExpr20() {
    final XQuery query = new XQuery(
      "1 and current-date()",
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
   *  An 'and' expression whose operands EBV cannot be extracted from. .
   */
  @org.junit.Test
  public void kLogicExpr21() {
    final XQuery query = new XQuery(
      "current-date() and 1",
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
   *  An and-test applied on fn:count(). .
   */
  @org.junit.Test
  public void kLogicExpr22() {
    final XQuery query = new XQuery(
      "count((1, 2, 3, timezone-from-time(current-time()), 4)) or false()",
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
   *  An or-test applied on fn:count(). .
   */
  @org.junit.Test
  public void kLogicExpr23() {
    final XQuery query = new XQuery(
      "false() or count((1, 2, 3, timezone-from-time(current-time()), 4))",
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
   *  An or-test applied on fn:count(). .
   */
  @org.junit.Test
  public void kLogicExpr24() {
    final XQuery query = new XQuery(
      "count((1, 2, 3, timezone-from-time(current-time()), 4)) or count((1, 2, 3, timezone-from-time(current-time()), 4))",
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
   *  or expression combined with fn:boolean(). .
   */
  @org.junit.Test
  public void kLogicExpr25() {
    final XQuery query = new XQuery(
      "boolean((1, 2, 3, current-time())[1] treat as xs:integer) or boolean((1, 2, 3, current-time())[1] treat as xs:integer)",
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
   *  and expression combined or fn:boolean(). .
   */
  @org.junit.Test
  public void kLogicExpr26() {
    final XQuery query = new XQuery(
      "boolean((1, 2, 3, current-time())[1] treat as xs:integer) and true()",
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
   *  or expression combined with fn:boolean(). .
   */
  @org.junit.Test
  public void kLogicExpr27() {
    final XQuery query = new XQuery(
      "true() or boolean((1, 2, 3, current-time())[1] treat as xs:integer)",
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
   *  A test whose essence is: `not("" and 0)`. .
   */
  @org.junit.Test
  public void kLogicExpr28() {
    final XQuery query = new XQuery(
      "not(\"\" and 0)",
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
   *  A test whose essence is: `1 and 1`. .
   */
  @org.junit.Test
  public void kLogicExpr29() {
    final XQuery query = new XQuery(
      "1 and 1",
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
   *  A test whose essence is: `not(0 or 0)`. .
   */
  @org.junit.Test
  public void kLogicExpr3() {
    final XQuery query = new XQuery(
      "not(0 or 0)",
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
   *  A test whose essence is: `10.3 and "non-zero length string"`. .
   */
  @org.junit.Test
  public void kLogicExpr30() {
    final XQuery query = new XQuery(
      "10.3 and \"non-zero length string\"",
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
   *  A test whose essence is: `1 and 5`. .
   */
  @org.junit.Test
  public void kLogicExpr31() {
    final XQuery query = new XQuery(
      "1 and 5",
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
   *  A test whose essence is: `not((1, current-time())[1] treat as xs:integer and false())`. .
   */
  @org.junit.Test
  public void kLogicExpr32() {
    final XQuery query = new XQuery(
      "not((1, current-time())[1] treat as xs:integer and false())",
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
   *  A test whose essence is: `not(false() and (1, current-time())[1] treat as xs:integer)`. .
   */
  @org.junit.Test
  public void kLogicExpr33() {
    final XQuery query = new XQuery(
      "not(false() and (1, current-time())[1] treat as xs:integer)",
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
   *  Non-empty xs:anyURIs have an EBV value of true. .
   */
  @org.junit.Test
  public void kLogicExpr34() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"example.com/\") and xs:anyURI(\"example.com/\")",
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
   *  Empty xs:anyURIs have an EBV value of false. .
   */
  @org.junit.Test
  public void kLogicExpr35() {
    final XQuery query = new XQuery(
      "not(xs:anyURI(\"example.com/\") and xs:anyURI(\"\"))",
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
   *  Empty xs:anyURIs have an EBV value of false. .
   */
  @org.junit.Test
  public void kLogicExpr36() {
    final XQuery query = new XQuery(
      "not(xs:anyURI(\"\") and xs:anyURI(\"example.com/\"))",
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
   *  Non-empty xs:untypedAtomics have an EBV value of true. .
   */
  @org.junit.Test
  public void kLogicExpr37() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"a string\") and xs:untypedAtomic(\"a string\")",
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
   *  Empty xs:untypedAtomics have an EBV value of false. .
   */
  @org.junit.Test
  public void kLogicExpr38() {
    final XQuery query = new XQuery(
      "not(xs:untypedAtomic(\"a string\") and xs:untypedAtomic(\"\"))",
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
   *  An and-test applied on fn:count(). .
   */
  @org.junit.Test
  public void kLogicExpr39() {
    final XQuery query = new XQuery(
      "count((1, 2, 3, timezone-from-time(current-time()), 4)) and true()",
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
   *  A test whose essence is: `not("" or 0)`. .
   */
  @org.junit.Test
  public void kLogicExpr4() {
    final XQuery query = new XQuery(
      "not(\"\" or 0)",
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
   *  An and-test applied on fn:count(). .
   */
  @org.junit.Test
  public void kLogicExpr40() {
    final XQuery query = new XQuery(
      "true() and count((1, 2, 3, timezone-from-time(current-time()), 4))",
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
   *  An and-test applied on fn:count(). .
   */
  @org.junit.Test
  public void kLogicExpr41() {
    final XQuery query = new XQuery(
      "count((1, 2, 3, timezone-from-time(current-time()), 4)) and count((1, 2, 3, timezone-from-time(current-time()), 4))",
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
   *  and-expression combined with fn:boolean(). .
   */
  @org.junit.Test
  public void kLogicExpr42() {
    final XQuery query = new XQuery(
      "boolean((1, 2, 3, current-time())[1] treat as xs:integer) and boolean((1, 2, 3, current-time())[1] treat as xs:integer)",
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
   *  and-expression combined with fn:boolean(). .
   */
  @org.junit.Test
  public void kLogicExpr43() {
    final XQuery query = new XQuery(
      "boolean((1, 2, 3, current-time())[1] treat as xs:integer) and true()",
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
   *  and-expression combined with fn:boolean(). .
   */
  @org.junit.Test
  public void kLogicExpr44() {
    final XQuery query = new XQuery(
      "true() and boolean((1, 2, 3, current-time())[1] treat as xs:integer)",
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
   *  A test whose essence is: `"a string is ok" and QName("", "local")`. .
   */
  @org.junit.Test
  public void kLogicExpr45() {
    final XQuery query = new XQuery(
      "\"a string is ok\" and QName(\"\", \"local\")",
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
   *  A test whose essence is: `"a string is ok" or QName("", "local")`. .
   */
  @org.junit.Test
  public void kLogicExpr46() {
    final XQuery query = new XQuery(
      "\"a string is ok\" or QName(\"\", \"local\")",
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
        error("FORG0006")
      )
    );
  }

  /**
   *  A test whose essence is: `1 or 1`. .
   */
  @org.junit.Test
  public void kLogicExpr5() {
    final XQuery query = new XQuery(
      "1 or 1",
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
   *  A test whose essence is: `1 or 0`. .
   */
  @org.junit.Test
  public void kLogicExpr6() {
    final XQuery query = new XQuery(
      "1 or 0",
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
   *  A test whose essence is: `1 or 5`. .
   */
  @org.junit.Test
  public void kLogicExpr7() {
    final XQuery query = new XQuery(
      "1 or 5",
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
   *  Non-empty xs:anyURIs in the left branch of an or-expression has an EBV value of true. .
   */
  @org.junit.Test
  public void kLogicExpr8() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"example.com/\") or 0",
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
   *  Non-empty xs:anyURIs in the right branch of an or-expression has an EBV value of true. .
   */
  @org.junit.Test
  public void kLogicExpr9() {
    final XQuery query = new XQuery(
      "0 or xs:anyURI(\"example.com/\")",
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
   *  Keywords are case-sensitive. .
   */
  @org.junit.Test
  public void k2LogicExpr1() {
    final XQuery query = new XQuery(
      "1 OR 0",
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
   *  Keywords are case-sensitive. .
   */
  @org.junit.Test
  public void k2LogicExpr2() {
    final XQuery query = new XQuery(
      "1 AND 0",
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
   *  test logical and .
   */
  @org.junit.Test
  public void cbclLogicalAnd002() {
    final XQuery query = new XQuery(
      "declare function local:times-table($n as xs:integer) as xs:integer* { for $x in 1 to 12 return $x * $n }; (every $x in local:times-table(15) satisfies ($x mod 3 eq 0)) and (every $y in local:times-table(15) satisfies ($y mod 5 eq 0))",
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
   *  test logical and .
   */
  @org.junit.Test
  public void cbclLogicalAnd003() {
    final XQuery query = new XQuery(
      "declare function local:factorial($n as xs:integer) as xs:integer { if ($n le 1) then 1 else $n * local:factorial($n - 1) }; declare function local:is-divisible($n as xs:integer, $d as xs:integer) { $n mod $d eq 0 }; not(local:is-divisible(local:factorial(5), 3)) and not(local:is-divisible(local:factorial(5), 2))",
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
   *  test logical or .
   */
  @org.junit.Test
  public void cbclLogicalOr001() {
    final XQuery query = new XQuery(
      "declare function local:times-table($n as xs:integer) as xs:integer* { for $x in 1 to 12 return $x * $n }; (some $x in local:times-table(15) satisfies ($x mod 2 eq 0)) or (some $y in local:times-table(15) satisfies ($y mod 3 eq 0))",
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
   *  test logical or .
   */
  @org.junit.Test
  public void cbclLogicalOr002() {
    final XQuery query = new XQuery(
      "declare function local:factorial($n as xs:integer) as xs:integer? { if ($n lt 1) then () else if ($n eq 1) then 1 else $n * local:factorial($n - 1) }; (every $x in local:factorial(5) satisfies ($x mod 3 eq 0)) or (every $y in local:factorial(5) satisfies ($y mod 5 eq 0))",
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
   *  test logical or .
   */
  @org.junit.Test
  public void cbclLogicalOr003() {
    final XQuery query = new XQuery(
      "declare function local:factorial($n as xs:integer) as xs:integer { if ($n le 1) then 1 else $n * local:factorial($n - 1) }; declare function local:is-divisible($n as xs:integer, $d as xs:integer) { $n mod $d eq 0 }; not(local:is-divisible(local:factorial(5), 3)) or not(local:is-divisible(local:factorial(5), 2))",
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
   *  Logical 'and' using Boolean values .
   */
  @org.junit.Test
  public void opLogicalAnd001() {
    final XQuery query = new XQuery(
      "false() and false()",
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
   *  Logical 'and' using Boolean values .
   */
  @org.junit.Test
  public void opLogicalAnd002() {
    final XQuery query = new XQuery(
      "true() and false()",
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
   *  Logical 'and' using Boolean values .
   */
  @org.junit.Test
  public void opLogicalAnd003() {
    final XQuery query = new XQuery(
      "false() and true()",
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
   *  Logical 'and' using Boolean values .
   */
  @org.junit.Test
  public void opLogicalAnd004() {
    final XQuery query = new XQuery(
      "true() and true()",
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
   *  Logical 'and' using sequences .
   */
  @org.junit.Test
  public void opLogicalAnd005() {
    final XQuery query = new XQuery(
      "() and ()",
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
   *  Logical 'and' using sequences .
   */
  @org.junit.Test
  public void opLogicalAnd006() {
    final XQuery query = new XQuery(
      "(1) and ()",
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
   *  Logical 'and' using sequences .
   */
  @org.junit.Test
  public void opLogicalAnd007() {
    final XQuery query = new XQuery(
      "() and (1)",
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
   *  Logical 'and' using sequences .
   */
  @org.junit.Test
  public void opLogicalAnd008() {
    final XQuery query = new XQuery(
      "(1) and (1)",
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
   *  Logical 'and' using sequences .
   */
  @org.junit.Test
  public void opLogicalAnd009() {
    final XQuery query = new XQuery(
      "(0) and ()",
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
   *  Logical 'and' using sequences .
   */
  @org.junit.Test
  public void opLogicalAnd010() {
    final XQuery query = new XQuery(
      "() and (0)",
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
   *  Logical 'and' using sequences .
   */
  @org.junit.Test
  public void opLogicalAnd011() {
    final XQuery query = new XQuery(
      "(0) and (0)",
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
   *  Logical 'and' using sequences .
   */
  @org.junit.Test
  public void opLogicalAnd012() {
    final XQuery query = new XQuery(
      "(1) and (0)",
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
   *  Logical 'and' using sequences .
   */
  @org.junit.Test
  public void opLogicalAnd013() {
    final XQuery query = new XQuery(
      "(0) and (1)",
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
   *  Logical 'and' using sequences .
   */
  @org.junit.Test
  public void opLogicalAnd014() {
    final XQuery query = new XQuery(
      "(0) and (/bib/book/price/text())",
      ctx);
    try {
      query.context(node(file("docs/bib.xml")));
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
   *  Logical 'and' using sequences .
   */
  @org.junit.Test
  public void opLogicalAnd015() {
    final XQuery query = new XQuery(
      "(/bib/book/price/text()) and (1)",
      ctx);
    try {
      query.context(node(file("docs/bib.xml")));
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
   *  Logical 'and' using sequences .
   */
  @org.junit.Test
  public void opLogicalAnd016() {
    final XQuery query = new XQuery(
      "(/bib/book/price/text()) and (/bib/book/price/text())",
      ctx);
    try {
      query.context(node(file("docs/bib.xml")));
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
   *  Logical 'and' using string values .
   */
  @org.junit.Test
  public void opLogicalAnd017() {
    final XQuery query = new XQuery(
      "\"\" and ''",
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
   *  Logical 'and' using string values .
   */
  @org.junit.Test
  public void opLogicalAnd018() {
    final XQuery query = new XQuery(
      "\"\" and 'a'",
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
   *  Logical 'and' using string values .
   */
  @org.junit.Test
  public void opLogicalAnd019() {
    final XQuery query = new XQuery(
      "\"0\" and ''",
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
   *  Logical 'and' using string values .
   */
  @org.junit.Test
  public void opLogicalAnd020() {
    final XQuery query = new XQuery(
      "\"a\" and '0'",
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
   *  Logical 'and' using string values .
   */
  @org.junit.Test
  public void opLogicalAnd021() {
    final XQuery query = new XQuery(
      "xs:string(\"\") and xs:string('')",
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
   *  Logical 'and' using string values .
   */
  @org.junit.Test
  public void opLogicalAnd022() {
    final XQuery query = new XQuery(
      "xs:string(\"\") and xs:string('abc')",
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
   *  Logical 'and' using string values .
   */
  @org.junit.Test
  public void opLogicalAnd023() {
    final XQuery query = new XQuery(
      "xs:string(\"abc\") and xs:string('')",
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
   *  Logical 'and' using string values .
   */
  @org.junit.Test
  public void opLogicalAnd024() {
    final XQuery query = new XQuery(
      "xs:string(\"0\") and xs:string('abc')",
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
   *  Logical 'and' using untypedAtomic values .
   */
  @org.junit.Test
  public void opLogicalAnd025() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic('') and xs:untypedAtomic(\"\")",
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
   *  Logical 'and' using untypedAtomic values .
   */
  @org.junit.Test
  public void opLogicalAnd026() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic('abc') and xs:untypedAtomic(\"\")",
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
   *  Logical 'and' using untypedAtomic values .
   */
  @org.junit.Test
  public void opLogicalAnd027() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic('') and xs:untypedAtomic(\"0\")",
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
   *  Logical 'and' using untypedAtomic values .
   */
  @org.junit.Test
  public void opLogicalAnd028() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic('abc') and xs:untypedAtomic(\"0\")",
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
   *  Logical 'and' using number values .
   */
  @org.junit.Test
  public void opLogicalAnd029() {
    final XQuery query = new XQuery(
      "0 and 0",
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
   *  Logical 'and' using number values .
   */
  @org.junit.Test
  public void opLogicalAnd030() {
    final XQuery query = new XQuery(
      "0 and 1",
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
   *  Logical 'and' using number values .
   */
  @org.junit.Test
  public void opLogicalAnd031() {
    final XQuery query = new XQuery(
      "1 and 0",
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
   *  Logical 'and' using number values .
   */
  @org.junit.Test
  public void opLogicalAnd032() {
    final XQuery query = new XQuery(
      "0 and -1",
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
   *  Logical 'and' using number values .
   */
  @org.junit.Test
  public void opLogicalAnd033() {
    final XQuery query = new XQuery(
      "-1 and 1",
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
   *  Logical 'and' using float values .
   */
  @org.junit.Test
  public void opLogicalAnd034() {
    final XQuery query = new XQuery(
      "xs:float(0) and xs:float(0)",
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
   *  Logical 'and' using float values .
   */
  @org.junit.Test
  public void opLogicalAnd035() {
    final XQuery query = new XQuery(
      "xs:float(0) and xs:float(1)",
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
   *  Logical 'and' using float values .
   */
  @org.junit.Test
  public void opLogicalAnd036() {
    final XQuery query = new XQuery(
      "xs:float(-1) and xs:float(0)",
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
   *  Logical 'and' using float values .
   */
  @org.junit.Test
  public void opLogicalAnd037() {
    final XQuery query = new XQuery(
      "xs:float(1) and xs:float(-1)",
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
   *  Logical 'and' using float values .
   */
  @org.junit.Test
  public void opLogicalAnd038() {
    final XQuery query = new XQuery(
      "xs:float('NaN') and xs:float(0)",
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
   *  Logical 'and' using float values .
   */
  @org.junit.Test
  public void opLogicalAnd039() {
    final XQuery query = new XQuery(
      "xs:float('NaN') and xs:float(1)",
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
   *  Logical 'and' using float values .
   */
  @org.junit.Test
  public void opLogicalAnd040() {
    final XQuery query = new XQuery(
      "xs:float('NaN') and xs:float('NaN')",
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
   *  Logical 'and' using float values .
   */
  @org.junit.Test
  public void opLogicalAnd041() {
    final XQuery query = new XQuery(
      "xs:float('INF') and xs:float(0)",
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
   *  Logical 'and' using float values .
   */
  @org.junit.Test
  public void opLogicalAnd042() {
    final XQuery query = new XQuery(
      "xs:float('INF') and xs:float(1)",
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
   *  Logical 'and' using float values .
   */
  @org.junit.Test
  public void opLogicalAnd043() {
    final XQuery query = new XQuery(
      "xs:float('INF') and xs:float('NaN')",
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
   *  Logical 'and' using float values .
   */
  @org.junit.Test
  public void opLogicalAnd044() {
    final XQuery query = new XQuery(
      "xs:float('-INF') and xs:float(0)",
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
   *  Logical 'and' using float values .
   */
  @org.junit.Test
  public void opLogicalAnd045() {
    final XQuery query = new XQuery(
      "xs:float('-INF') and xs:float(1)",
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
   *  Logical 'and' using float values .
   */
  @org.junit.Test
  public void opLogicalAnd046() {
    final XQuery query = new XQuery(
      "xs:float('-INF') and xs:float('NaN')",
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
   *  Logical 'and' using float values .
   */
  @org.junit.Test
  public void opLogicalAnd047() {
    final XQuery query = new XQuery(
      "xs:float('-INF') and xs:float('INF')",
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
   *  Logical 'and' using double values .
   */
  @org.junit.Test
  public void opLogicalAnd048() {
    final XQuery query = new XQuery(
      "xs:double(0) and xs:double(0)",
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
   *  Logical 'and' using double values .
   */
  @org.junit.Test
  public void opLogicalAnd049() {
    final XQuery query = new XQuery(
      "xs:double(0) and xs:double(1)",
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
   *  Logical 'and' using double values .
   */
  @org.junit.Test
  public void opLogicalAnd050() {
    final XQuery query = new XQuery(
      "xs:double(-1) and xs:double(0)",
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
   *  Logical 'and' using double values .
   */
  @org.junit.Test
  public void opLogicalAnd051() {
    final XQuery query = new XQuery(
      "xs:double(1) and xs:double(-1)",
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
   *  Logical 'and' using double values .
   */
  @org.junit.Test
  public void opLogicalAnd052() {
    final XQuery query = new XQuery(
      "xs:double('NaN') and xs:double(0)",
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
   *  Logical 'and' using double values .
   */
  @org.junit.Test
  public void opLogicalAnd053() {
    final XQuery query = new XQuery(
      "xs:double('NaN') and xs:double(1)",
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
   *  Logical 'and' using double values .
   */
  @org.junit.Test
  public void opLogicalAnd054() {
    final XQuery query = new XQuery(
      "xs:double('NaN') and xs:double('NaN')",
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
   *  Logical 'and' using double values .
   */
  @org.junit.Test
  public void opLogicalAnd055() {
    final XQuery query = new XQuery(
      "xs:double('INF') and xs:double(0)",
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
   *  Logical 'and' using double values .
   */
  @org.junit.Test
  public void opLogicalAnd056() {
    final XQuery query = new XQuery(
      "xs:double('INF') and xs:double(1)",
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
   *  Logical 'and' using double values .
   */
  @org.junit.Test
  public void opLogicalAnd057() {
    final XQuery query = new XQuery(
      "xs:double('INF') and xs:double('NaN')",
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
   *  Logical 'and' using double values .
   */
  @org.junit.Test
  public void opLogicalAnd058() {
    final XQuery query = new XQuery(
      "xs:double('-INF') and xs:double(0)",
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
   *  Logical 'and' using double values .
   */
  @org.junit.Test
  public void opLogicalAnd059() {
    final XQuery query = new XQuery(
      "xs:double('-INF') and xs:double(1)",
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
   *  Logical 'and' using double values .
   */
  @org.junit.Test
  public void opLogicalAnd060() {
    final XQuery query = new XQuery(
      "xs:double('-INF') and xs:double('NaN')",
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
   *  Logical 'and' using double values .
   */
  @org.junit.Test
  public void opLogicalAnd061() {
    final XQuery query = new XQuery(
      "xs:double('-INF') and xs:double('INF')",
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
   *  Logical 'and' using decimal values .
   */
  @org.junit.Test
  public void opLogicalAnd062() {
    final XQuery query = new XQuery(
      "xs:decimal(0) and xs:decimal(0)",
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
   *  Logical 'and' using decimal values .
   */
  @org.junit.Test
  public void opLogicalAnd063() {
    final XQuery query = new XQuery(
      "xs:decimal(0) and xs:decimal(1)",
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
   *  Logical 'and' using decimal values .
   */
  @org.junit.Test
  public void opLogicalAnd064() {
    final XQuery query = new XQuery(
      "xs:decimal(-1) and xs:decimal(0)",
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
   *  Logical 'and' using decimal values .
   */
  @org.junit.Test
  public void opLogicalAnd065() {
    final XQuery query = new XQuery(
      "xs:decimal(1) and xs:decimal(-1)",
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
   *  Logical 'and' using decimal values .
   */
  @org.junit.Test
  public void opLogicalAnd066() {
    final XQuery query = new XQuery(
      "xs:decimal(9.99999999999999999999999999) and xs:decimal(0)",
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
        error("FOCA0006")
      )
    );
  }

  /**
   *  Logical 'and' using decimal values .
   */
  @org.junit.Test
  public void opLogicalAnd067() {
    final XQuery query = new XQuery(
      "xs:decimal(-123456789.123456789123456789) and xs:decimal(1)",
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
   *  Logical 'and' using decimal values .
   */
  @org.junit.Test
  public void opLogicalAnd068() {
    final XQuery query = new XQuery(
      "xs:decimal(9.99999999999999999999999999) and xs:decimal(-123456789.123456789123456789)",
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
        error("FOCA0006")
      )
    );
  }

  /**
   *  Logical 'and' using integer values .
   */
  @org.junit.Test
  public void opLogicalAnd069() {
    final XQuery query = new XQuery(
      "xs:integer(0) and xs:integer(0)",
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
   *  Logical 'and' using integer values .
   */
  @org.junit.Test
  public void opLogicalAnd070() {
    final XQuery query = new XQuery(
      "xs:integer(0) and xs:integer(1)",
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
   *  Logical 'and' using integer values .
   */
  @org.junit.Test
  public void opLogicalAnd071() {
    final XQuery query = new XQuery(
      "xs:integer(-1) and xs:integer(0)",
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
   *  Logical 'and' using integer values .
   */
  @org.junit.Test
  public void opLogicalAnd072() {
    final XQuery query = new XQuery(
      "xs:integer(1) and xs:integer(-1)",
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
   *  Logical 'and' using integer values .
   */
  @org.junit.Test
  public void opLogicalAnd073() {
    final XQuery query = new XQuery(
      "xs:integer(99999999999999999) and xs:integer(0)",
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
   *  Logical 'and' using integer values .
   */
  @org.junit.Test
  public void opLogicalAnd074() {
    final XQuery query = new XQuery(
      "xs:integer(-99999999999999999) and xs:integer(1)",
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
   *  Logical 'and' using integer values .
   */
  @org.junit.Test
  public void opLogicalAnd075() {
    final XQuery query = new XQuery(
      "xs:integer(99999999999999999) and xs:integer(-99999999999999999)",
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
   *  Logical 'and' using nonPositiveInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd076() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(0) and xs:nonPositiveInteger(0)",
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
   *  Logical 'and' using nonPositiveInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd077() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(0) and xs:nonPositiveInteger(-1)",
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
   *  Logical 'and' using nonPositiveInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd078() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(-1) and xs:nonPositiveInteger(0)",
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
   *  Logical 'and' using nonPositiveInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd079() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(-1) and xs:nonPositiveInteger(-1)",
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
   *  Logical 'and' using nonPositiveInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd080() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(-99999999999999999) and xs:nonPositiveInteger(0)",
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
   *  Logical 'and' using nonPositiveInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd081() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(-1) and xs:nonPositiveInteger(-9999999999)",
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
   *  Logical 'and' using nonPositiveInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd082() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(-99999999999999999) and xs:nonPositiveInteger(-99999999999999999)",
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
   *  Logical 'and' using nonNegativeInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd083() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(0) and xs:nonNegativeInteger(0)",
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
   *  Logical 'and' using nonNegativeInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd084() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(0) and xs:nonNegativeInteger(1)",
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
   *  Logical 'and' using nonNegativeInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd085() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(1) and xs:nonNegativeInteger(0)",
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
   *  Logical 'and' using nonNegativeInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd086() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(1) and xs:nonNegativeInteger(1)",
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
   *  Logical 'and' using nonNegativeInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd087() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(9999999999) and xs:nonNegativeInteger(0)",
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
   *  Logical 'and' using nonNegativeInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd088() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(1) and xs:nonNegativeInteger(99999999999999999)",
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
   *  Logical 'and' using nonNegativeInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd089() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(99999999999999999) and xs:nonNegativeInteger(99999999999999999)",
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
   *  Logical 'and' using negativeInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd090() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(-1) and xs:negativeInteger(-1)",
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
   *  Logical 'and' using negativeInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd091() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(-99999999999999999) and xs:negativeInteger(-1)",
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
   *  Logical 'and' using negativeInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd092() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(-99999999999999999) and xs:negativeInteger(-99999999999999999)",
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
   *  Logical 'and' using positiveInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd093() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(1) and xs:positiveInteger(1)",
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
   *  Logical 'and' using positiveInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd094() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(99999999999999999) and xs:positiveInteger(1)",
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
   *  Logical 'and' using positiveInteger values .
   */
  @org.junit.Test
  public void opLogicalAnd095() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(99999999999999999) and xs:positiveInteger(99999999999999999)",
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
   *  Logical 'and' using long values .
   */
  @org.junit.Test
  public void opLogicalAnd096() {
    final XQuery query = new XQuery(
      "xs:long(0) and xs:long(0)",
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
   *  Logical 'and' using long values .
   */
  @org.junit.Test
  public void opLogicalAnd097() {
    final XQuery query = new XQuery(
      "xs:long(0) and xs:long(1)",
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
   *  Logical 'and' using long values .
   */
  @org.junit.Test
  public void opLogicalAnd098() {
    final XQuery query = new XQuery(
      "xs:long(-1) and xs:long(0)",
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
   *  Logical 'and' using long values .
   */
  @org.junit.Test
  public void opLogicalAnd099() {
    final XQuery query = new XQuery(
      "xs:long(1) and xs:long(-1)",
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
   *  Logical 'and' using long values .
   */
  @org.junit.Test
  public void opLogicalAnd100() {
    final XQuery query = new XQuery(
      "xs:long(9223372036854775807) and xs:long(0)",
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
   *  Logical 'and' using long values .
   */
  @org.junit.Test
  public void opLogicalAnd101() {
    final XQuery query = new XQuery(
      "xs:long(9223372036854775807) and xs:long(-1)",
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
   *  Logical 'and' using long values .
   */
  @org.junit.Test
  public void opLogicalAnd102() {
    final XQuery query = new XQuery(
      "xs:long(-99999999999999999) and xs:long(0)",
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
   *  Logical 'and' using long values .
   */
  @org.junit.Test
  public void opLogicalAnd103() {
    final XQuery query = new XQuery(
      "xs:long(-99999999999999999) and xs:long(1)",
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
   *  Logical 'and' using long values .
   */
  @org.junit.Test
  public void opLogicalAnd104() {
    final XQuery query = new XQuery(
      "xs:long(99999999999999999) and xs:long(-99999999999999999)",
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
   *  Logical 'and' using unsignedLong values .
   */
  @org.junit.Test
  public void opLogicalAnd105() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(0) and xs:unsignedLong(0)",
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
   *  Logical 'and' using unsignedLong values .
   */
  @org.junit.Test
  public void opLogicalAnd106() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(0) and xs:unsignedLong(1)",
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
   *  Logical 'and' using unsignedLong values .
   */
  @org.junit.Test
  public void opLogicalAnd107() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(1) and xs:unsignedLong(0)",
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
   *  Logical 'and' using unsignedLong values .
   */
  @org.junit.Test
  public void opLogicalAnd108() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(1) and xs:unsignedLong(1)",
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
   *  Logical 'and' using unsignedLong values .
   */
  @org.junit.Test
  public void opLogicalAnd109() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(9223372036854775807) and xs:unsignedLong(0)",
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
   *  Logical 'and' using unsignedLong values .
   */
  @org.junit.Test
  public void opLogicalAnd110() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(9223372036854775807) and xs:unsignedLong(1)",
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
   *  Logical 'and' using unsignedLong values .
   */
  @org.junit.Test
  public void opLogicalAnd111() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(99999999999999999) and xs:unsignedLong(99999999999999999)",
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
   *  Logical 'and' using int values .
   */
  @org.junit.Test
  public void opLogicalAnd112() {
    final XQuery query = new XQuery(
      "xs:int(0) and xs:int(0)",
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
   *  Logical 'and' using int values .
   */
  @org.junit.Test
  public void opLogicalAnd113() {
    final XQuery query = new XQuery(
      "xs:int(0) and xs:int(1)",
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
   *  Logical 'and' using int values .
   */
  @org.junit.Test
  public void opLogicalAnd114() {
    final XQuery query = new XQuery(
      "xs:int(-1) and xs:int(0)",
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
   *  Logical 'and' using int values .
   */
  @org.junit.Test
  public void opLogicalAnd115() {
    final XQuery query = new XQuery(
      "xs:int(1) and xs:int(-1)",
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
   *  Logical 'and' using int values .
   */
  @org.junit.Test
  public void opLogicalAnd116() {
    final XQuery query = new XQuery(
      "xs:int(2147483647) and xs:int(0)",
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
   *  Logical 'and' using int values .
   */
  @org.junit.Test
  public void opLogicalAnd117() {
    final XQuery query = new XQuery(
      "xs:int(2147483647) and xs:int(-1)",
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
   *  Logical 'and' using int values .
   */
  @org.junit.Test
  public void opLogicalAnd118() {
    final XQuery query = new XQuery(
      "xs:int(-2147483648) and xs:int(0)",
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
   *  Logical 'and' using int values .
   */
  @org.junit.Test
  public void opLogicalAnd119() {
    final XQuery query = new XQuery(
      "xs:int(-2147483648) and xs:int(1)",
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
   *  Logical 'and' using int values .
   */
  @org.junit.Test
  public void opLogicalAnd120() {
    final XQuery query = new XQuery(
      "xs:int(2147483647) and xs:int(-2147483648)",
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
   *  Logical 'and' using unsignedInt values .
   */
  @org.junit.Test
  public void opLogicalAnd121() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(0) and xs:unsignedInt(0)",
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
   *  Logical 'and' using unsignedInt values .
   */
  @org.junit.Test
  public void opLogicalAnd122() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(0) and xs:unsignedInt(1)",
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
   *  Logical 'and' using unsignedInt values .
   */
  @org.junit.Test
  public void opLogicalAnd123() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(1) and xs:unsignedInt(0)",
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
   *  Logical 'and' using unsignedInt values .
   */
  @org.junit.Test
  public void opLogicalAnd124() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(1) and xs:unsignedInt(1)",
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
   *  Logical 'and' using unsignedInt values .
   */
  @org.junit.Test
  public void opLogicalAnd125() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(4294967295) and xs:unsignedInt(0)",
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
   *  Logical 'and' using unsignedInt values .
   */
  @org.junit.Test
  public void opLogicalAnd126() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(4294967295) and xs:unsignedInt(1)",
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
   *  Logical 'and' using unsignedInt values .
   */
  @org.junit.Test
  public void opLogicalAnd127() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(4294967295) and xs:unsignedInt(4294967295)",
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
   *  Logical 'and' using short values .
   */
  @org.junit.Test
  public void opLogicalAnd128() {
    final XQuery query = new XQuery(
      "xs:short(0) and xs:short(0)",
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
   *  Logical 'and' using short values .
   */
  @org.junit.Test
  public void opLogicalAnd129() {
    final XQuery query = new XQuery(
      "xs:short(0) and xs:short(1)",
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
   *  Logical 'and' using short values .
   */
  @org.junit.Test
  public void opLogicalAnd130() {
    final XQuery query = new XQuery(
      "xs:short(-1) and xs:short(0)",
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
   *  Logical 'and' using short values .
   */
  @org.junit.Test
  public void opLogicalAnd131() {
    final XQuery query = new XQuery(
      "xs:short(1) and xs:short(-1)",
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
   *  Logical 'and' using short values .
   */
  @org.junit.Test
  public void opLogicalAnd132() {
    final XQuery query = new XQuery(
      "xs:short(32767) and xs:short(0)",
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
   *  Logical 'and' using short values .
   */
  @org.junit.Test
  public void opLogicalAnd133() {
    final XQuery query = new XQuery(
      "xs:short(32767) and xs:short(-1)",
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
   *  Logical 'and' using short values .
   */
  @org.junit.Test
  public void opLogicalAnd134() {
    final XQuery query = new XQuery(
      "xs:short(-32768) and xs:short(0)",
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
   *  Logical 'and' using short values .
   */
  @org.junit.Test
  public void opLogicalAnd135() {
    final XQuery query = new XQuery(
      "xs:short(-32768) and xs:short(1)",
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
   *  Logical 'and' using short values .
   */
  @org.junit.Test
  public void opLogicalAnd136() {
    final XQuery query = new XQuery(
      "xs:short(32767) and xs:short(-32768)",
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
   *  Logical 'and' using unsignedShort values .
   */
  @org.junit.Test
  public void opLogicalAnd137() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(0) and xs:unsignedShort(0)",
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
   *  Logical 'and' using unsignedShort values .
   */
  @org.junit.Test
  public void opLogicalAnd138() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(0) and xs:unsignedShort(1)",
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
   *  Logical 'and' using unsignedShort values .
   */
  @org.junit.Test
  public void opLogicalAnd139() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(1) and xs:unsignedShort(0)",
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
   *  Logical 'and' using unsignedShort values .
   */
  @org.junit.Test
  public void opLogicalAnd140() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(1) and xs:unsignedShort(1)",
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
   *  Logical 'and' using unsignedShort values .
   */
  @org.junit.Test
  public void opLogicalAnd141() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(65535) and xs:unsignedShort(0)",
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
   *  Logical 'and' using unsignedShort values .
   */
  @org.junit.Test
  public void opLogicalAnd142() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(65535) and xs:unsignedShort(1)",
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
   *  Logical 'and' using unsignedShort values .
   */
  @org.junit.Test
  public void opLogicalAnd143() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(65535) and xs:unsignedShort(65535)",
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
   *  Logical 'and' using byte values .
   */
  @org.junit.Test
  public void opLogicalAnd144() {
    final XQuery query = new XQuery(
      "xs:byte(0) and xs:byte(0)",
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
   *  Logical 'and' using byte values .
   */
  @org.junit.Test
  public void opLogicalAnd145() {
    final XQuery query = new XQuery(
      "xs:byte(0) and xs:byte(1)",
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
   *  Logical 'and' using byte values .
   */
  @org.junit.Test
  public void opLogicalAnd146() {
    final XQuery query = new XQuery(
      "xs:byte(-1) and xs:byte(0)",
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
   *  Logical 'and' using byte values .
   */
  @org.junit.Test
  public void opLogicalAnd147() {
    final XQuery query = new XQuery(
      "xs:byte(1) and xs:byte(-1)",
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
   *  Logical 'and' using byte values .
   */
  @org.junit.Test
  public void opLogicalAnd148() {
    final XQuery query = new XQuery(
      "xs:byte(127) and xs:byte(0)",
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
   *  Logical 'and' using byte values .
   */
  @org.junit.Test
  public void opLogicalAnd149() {
    final XQuery query = new XQuery(
      "xs:byte(127) and xs:byte(-1)",
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
   *  Logical 'and' using byte values .
   */
  @org.junit.Test
  public void opLogicalAnd150() {
    final XQuery query = new XQuery(
      "xs:byte(-128) and xs:byte(0)",
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
   *  Logical 'and' using byte values .
   */
  @org.junit.Test
  public void opLogicalAnd151() {
    final XQuery query = new XQuery(
      "xs:byte(-128) and xs:byte(1)",
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
   *  Logical 'and' using byte values .
   */
  @org.junit.Test
  public void opLogicalAnd152() {
    final XQuery query = new XQuery(
      "xs:byte(127) and xs:byte(-128)",
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
   *  Logical 'and' using unsignedByte values .
   */
  @org.junit.Test
  public void opLogicalAnd153() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(0) and xs:unsignedByte(0)",
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
   *  Logical 'and' using unsignedByte values .
   */
  @org.junit.Test
  public void opLogicalAnd154() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(0) and xs:unsignedByte(1)",
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
   *  Logical 'and' using unsignedByte values .
   */
  @org.junit.Test
  public void opLogicalAnd155() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(1) and xs:unsignedByte(0)",
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
   *  Logical 'and' using unsignedByte values .
   */
  @org.junit.Test
  public void opLogicalAnd156() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(1) and xs:unsignedByte(1)",
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
   *  Logical 'and' using unsignedByte values .
   */
  @org.junit.Test
  public void opLogicalAnd157() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(255) and xs:unsignedByte(0)",
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
   *  Logical 'and' using unsignedByte values .
   */
  @org.junit.Test
  public void opLogicalAnd158() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(255) and xs:unsignedByte(1)",
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
   *  Logical 'and' using unsignedByte values .
   */
  @org.junit.Test
  public void opLogicalAnd159() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(255) and xs:unsignedByte(255)",
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
   *  Logical 'or' using boolean values .
   */
  @org.junit.Test
  public void opLogicalOr001() {
    final XQuery query = new XQuery(
      "false() or false()",
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
   *  Logical 'or' using boolean values .
   */
  @org.junit.Test
  public void opLogicalOr002() {
    final XQuery query = new XQuery(
      "true() or false()",
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
   *  Logical 'or' using boolean values .
   */
  @org.junit.Test
  public void opLogicalOr003() {
    final XQuery query = new XQuery(
      "false() or true()",
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
   *  Logical 'or' using boolean values .
   */
  @org.junit.Test
  public void opLogicalOr004() {
    final XQuery query = new XQuery(
      "true() or true()",
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
   *  Logical 'or' using sequences values .
   */
  @org.junit.Test
  public void opLogicalOr005() {
    final XQuery query = new XQuery(
      "() or ()",
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
   *  Logical 'or' using sequences values .
   */
  @org.junit.Test
  public void opLogicalOr006() {
    final XQuery query = new XQuery(
      "(1) or ()",
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
   *  Logical 'or' using sequences values .
   */
  @org.junit.Test
  public void opLogicalOr007() {
    final XQuery query = new XQuery(
      "() or (1)",
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
   *  Logical 'or' using sequences values .
   */
  @org.junit.Test
  public void opLogicalOr008() {
    final XQuery query = new XQuery(
      "(1) or (1)",
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
   *  Logical 'or' using sequences values .
   */
  @org.junit.Test
  public void opLogicalOr009() {
    final XQuery query = new XQuery(
      "(0) or ()",
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
   *  Logical 'or' using sequences values .
   */
  @org.junit.Test
  public void opLogicalOr010() {
    final XQuery query = new XQuery(
      "() or (0)",
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
   *  Logical 'or' using sequences values .
   */
  @org.junit.Test
  public void opLogicalOr011() {
    final XQuery query = new XQuery(
      "(0) or (0)",
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
   *  Logical 'or' using sequences values .
   */
  @org.junit.Test
  public void opLogicalOr012() {
    final XQuery query = new XQuery(
      "(1) or (0)",
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
   *  Logical 'or' using sequences values .
   */
  @org.junit.Test
  public void opLogicalOr013() {
    final XQuery query = new XQuery(
      "(0) or (1)",
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
   *  Logical 'or' using sequences values .
   */
  @org.junit.Test
  public void opLogicalOr014() {
    final XQuery query = new XQuery(
      "(0) or (/bib/book/price/text())",
      ctx);
    try {
      query.context(node(file("docs/bib.xml")));
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
   *  Logical 'or' using sequences values .
   */
  @org.junit.Test
  public void opLogicalOr015() {
    final XQuery query = new XQuery(
      "(/bib/book/price/text()) or (1)",
      ctx);
    try {
      query.context(node(file("docs/bib.xml")));
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
   *  Logical 'or' using sequences values .
   */
  @org.junit.Test
  public void opLogicalOr016() {
    final XQuery query = new XQuery(
      "(/bib/book/price/text()) or (/bib/book/price/text())",
      ctx);
    try {
      query.context(node(file("docs/bib.xml")));
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
   *  Logical 'or' using string values .
   */
  @org.junit.Test
  public void opLogicalOr017() {
    final XQuery query = new XQuery(
      "\"\" or ''",
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
   *  Logical 'or' using string values .
   */
  @org.junit.Test
  public void opLogicalOr018() {
    final XQuery query = new XQuery(
      "\"\" or 'a'",
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
   *  Logical 'or' using string values .
   */
  @org.junit.Test
  public void opLogicalOr019() {
    final XQuery query = new XQuery(
      "\"0\" or ''",
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
   *  Logical 'or' using string values .
   */
  @org.junit.Test
  public void opLogicalOr020() {
    final XQuery query = new XQuery(
      "\"a\" or '0'",
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
   *  Logical 'or' using string values .
   */
  @org.junit.Test
  public void opLogicalOr021() {
    final XQuery query = new XQuery(
      "xs:string(\"\") or xs:string('')",
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
   *  Logical 'or' using string values .
   */
  @org.junit.Test
  public void opLogicalOr022() {
    final XQuery query = new XQuery(
      "xs:string(\"\") or xs:string('abc')",
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
   *  Logical 'or' using string values .
   */
  @org.junit.Test
  public void opLogicalOr023() {
    final XQuery query = new XQuery(
      "xs:string(\"abc\") or xs:string('')",
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
   *  Logical 'or' using string values .
   */
  @org.junit.Test
  public void opLogicalOr024() {
    final XQuery query = new XQuery(
      "xs:string(\"0\") or xs:string('abc')",
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
   *  Logical 'or' using untypedAtomic values .
   */
  @org.junit.Test
  public void opLogicalOr025() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic('') or xs:untypedAtomic(\"\")",
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
   *  Logical 'or' using untypedAtomic values .
   */
  @org.junit.Test
  public void opLogicalOr026() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic('abc') or xs:untypedAtomic(\"\")",
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
   *  Logical 'or' using untypedAtomic values .
   */
  @org.junit.Test
  public void opLogicalOr027() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic('') or xs:untypedAtomic(\"0\")",
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
   *  Logical 'or' using untypedAtomic values .
   */
  @org.junit.Test
  public void opLogicalOr028() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic('abc') or xs:untypedAtomic(\"0\")",
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
   *  Logical 'or' using number values .
   */
  @org.junit.Test
  public void opLogicalOr029() {
    final XQuery query = new XQuery(
      "0 or 0",
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
   *  Logical 'or' using number values .
   */
  @org.junit.Test
  public void opLogicalOr030() {
    final XQuery query = new XQuery(
      "0 or 1",
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
   *  Logical 'or' using number values .
   */
  @org.junit.Test
  public void opLogicalOr031() {
    final XQuery query = new XQuery(
      "1 or 0",
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
   *  Logical 'or' using number values .
   */
  @org.junit.Test
  public void opLogicalOr032() {
    final XQuery query = new XQuery(
      "0 or -1",
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
   *  Logical 'or' using number values .
   */
  @org.junit.Test
  public void opLogicalOr033() {
    final XQuery query = new XQuery(
      "-1 or 1",
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
   *  Logical 'or' using float values .
   */
  @org.junit.Test
  public void opLogicalOr034() {
    final XQuery query = new XQuery(
      "xs:float(0) or xs:float(0)",
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
   *  Logical 'or' using float values .
   */
  @org.junit.Test
  public void opLogicalOr035() {
    final XQuery query = new XQuery(
      "xs:float(0) or xs:float(1)",
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
   *  Logical 'or' using float values .
   */
  @org.junit.Test
  public void opLogicalOr036() {
    final XQuery query = new XQuery(
      "xs:float(-1) or xs:float(0)",
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
   *  Logical 'or' using float values .
   */
  @org.junit.Test
  public void opLogicalOr037() {
    final XQuery query = new XQuery(
      "xs:float(1) or xs:float(-1)",
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
   *  Logical 'or' using float values .
   */
  @org.junit.Test
  public void opLogicalOr038() {
    final XQuery query = new XQuery(
      "xs:float('NaN') or xs:float(0)",
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
   *  Logical 'or' using float values .
   */
  @org.junit.Test
  public void opLogicalOr039() {
    final XQuery query = new XQuery(
      "xs:float('NaN') or xs:float(1)",
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
   *  Logical 'or' using float values .
   */
  @org.junit.Test
  public void opLogicalOr040() {
    final XQuery query = new XQuery(
      "xs:float('NaN') or xs:float('NaN')",
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
   *  Logical 'or' using float values .
   */
  @org.junit.Test
  public void opLogicalOr041() {
    final XQuery query = new XQuery(
      "xs:float('INF') or xs:float(0)",
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
   *  Logical 'or' using float values .
   */
  @org.junit.Test
  public void opLogicalOr042() {
    final XQuery query = new XQuery(
      "xs:float('INF') or xs:float(1)",
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
   *  Logical 'or' using float values .
   */
  @org.junit.Test
  public void opLogicalOr043() {
    final XQuery query = new XQuery(
      "xs:float('INF') or xs:float('NaN')",
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
   *  Logical 'or' using float values .
   */
  @org.junit.Test
  public void opLogicalOr044() {
    final XQuery query = new XQuery(
      "xs:float('-INF') or xs:float(0)",
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
   *  Logical 'or' using float values .
   */
  @org.junit.Test
  public void opLogicalOr045() {
    final XQuery query = new XQuery(
      "xs:float('-INF') or xs:float(1)",
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
   *  Logical 'or' using float values .
   */
  @org.junit.Test
  public void opLogicalOr046() {
    final XQuery query = new XQuery(
      "xs:float('-INF') or xs:float('NaN')",
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
   *  Logical 'or' using float values .
   */
  @org.junit.Test
  public void opLogicalOr047() {
    final XQuery query = new XQuery(
      "xs:float('-INF') or xs:float('INF')",
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
   *  Logical 'or' using double values .
   */
  @org.junit.Test
  public void opLogicalOr048() {
    final XQuery query = new XQuery(
      "xs:double(0) or xs:double(0)",
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
   *  Logical 'or' using double values .
   */
  @org.junit.Test
  public void opLogicalOr049() {
    final XQuery query = new XQuery(
      "xs:double(0) or xs:double(1)",
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
   *  Logical 'or' using double values .
   */
  @org.junit.Test
  public void opLogicalOr050() {
    final XQuery query = new XQuery(
      "xs:double(-1) or xs:double(0)",
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
   *  Logical 'or' using double values .
   */
  @org.junit.Test
  public void opLogicalOr051() {
    final XQuery query = new XQuery(
      "xs:double(1) or xs:double(-1)",
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
   *  Logical 'or' using double values .
   */
  @org.junit.Test
  public void opLogicalOr052() {
    final XQuery query = new XQuery(
      "xs:double('NaN') or xs:double(0)",
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
   *  Logical 'or' using double values .
   */
  @org.junit.Test
  public void opLogicalOr053() {
    final XQuery query = new XQuery(
      "xs:double('NaN') or xs:double(1)",
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
   *  Logical 'or' using double values .
   */
  @org.junit.Test
  public void opLogicalOr054() {
    final XQuery query = new XQuery(
      "xs:double('NaN') or xs:double('NaN')",
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
   *  Logical 'or' using double values .
   */
  @org.junit.Test
  public void opLogicalOr055() {
    final XQuery query = new XQuery(
      "xs:double('INF') or xs:double(0)",
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
   *  Logical 'or' using double values .
   */
  @org.junit.Test
  public void opLogicalOr056() {
    final XQuery query = new XQuery(
      "xs:double('INF') or xs:double(1)",
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
   *  Logical 'or' using double values .
   */
  @org.junit.Test
  public void opLogicalOr057() {
    final XQuery query = new XQuery(
      "xs:double('INF') or xs:double('NaN')",
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
   *  Logical 'or' using double values .
   */
  @org.junit.Test
  public void opLogicalOr058() {
    final XQuery query = new XQuery(
      "xs:double('-INF') or xs:double(0)",
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
   *  Logical 'or' using double values .
   */
  @org.junit.Test
  public void opLogicalOr059() {
    final XQuery query = new XQuery(
      "xs:double('-INF') or xs:double(1)",
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
   *  Logical 'or' using double values .
   */
  @org.junit.Test
  public void opLogicalOr060() {
    final XQuery query = new XQuery(
      "xs:double('-INF') or xs:double('NaN')",
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
   *  Logical 'or' using double values .
   */
  @org.junit.Test
  public void opLogicalOr061() {
    final XQuery query = new XQuery(
      "xs:double('-INF') or xs:double('INF')",
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
   *  Logical 'or' using decimal values .
   */
  @org.junit.Test
  public void opLogicalOr062() {
    final XQuery query = new XQuery(
      "xs:decimal(0) or xs:decimal(0)",
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
   *  Logical 'or' using decimal values .
   */
  @org.junit.Test
  public void opLogicalOr063() {
    final XQuery query = new XQuery(
      "xs:decimal(0) or xs:decimal(1)",
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
   *  Logical 'or' using decimal values .
   */
  @org.junit.Test
  public void opLogicalOr064() {
    final XQuery query = new XQuery(
      "xs:decimal(-1) or xs:decimal(0)",
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
   *  Logical 'or' using decimal values .
   */
  @org.junit.Test
  public void opLogicalOr065() {
    final XQuery query = new XQuery(
      "xs:decimal(1) or xs:decimal(-1)",
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
   *  Logical 'or' using decimal values .
   */
  @org.junit.Test
  public void opLogicalOr066() {
    final XQuery query = new XQuery(
      "xs:decimal(9.99999999999999999999999999) or xs:decimal(0)",
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
        error("FOCA0006")
      )
    );
  }

  /**
   *  Logical 'or' using decimal values .
   */
  @org.junit.Test
  public void opLogicalOr067() {
    final XQuery query = new XQuery(
      "xs:decimal(-123456789.123456789123456789) or xs:decimal(1)",
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
   *  Logical 'or' using decimal values .
   */
  @org.junit.Test
  public void opLogicalOr068() {
    final XQuery query = new XQuery(
      "xs:decimal(9.99999999999999999999999999) or xs:decimal(-123456789.123456789123456789)",
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
        error("FOCA0006")
      )
    );
  }

  /**
   *  Logical 'or' using integer values .
   */
  @org.junit.Test
  public void opLogicalOr069() {
    final XQuery query = new XQuery(
      "xs:integer(0) or xs:integer(0)",
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
   *  Logical 'or' using integer values .
   */
  @org.junit.Test
  public void opLogicalOr070() {
    final XQuery query = new XQuery(
      "xs:integer(0) or xs:integer(1)",
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
   *  Logical 'or' using integer values .
   */
  @org.junit.Test
  public void opLogicalOr071() {
    final XQuery query = new XQuery(
      "xs:integer(-1) or xs:integer(0)",
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
   *  Logical 'or' using integer values .
   */
  @org.junit.Test
  public void opLogicalOr072() {
    final XQuery query = new XQuery(
      "xs:integer(1) or xs:integer(-1)",
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
   *  Logical 'or' using integer values .
   */
  @org.junit.Test
  public void opLogicalOr073() {
    final XQuery query = new XQuery(
      "xs:integer(99999999999999999) or xs:integer(0)",
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
   *  Logical 'or' using integer values .
   */
  @org.junit.Test
  public void opLogicalOr074() {
    final XQuery query = new XQuery(
      "xs:integer(-99999999999999999) or xs:integer(1)",
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
   *  Logical 'or' using integer values .
   */
  @org.junit.Test
  public void opLogicalOr075() {
    final XQuery query = new XQuery(
      "xs:integer(99999999999999999) or xs:integer(-99999999999999999)",
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
   *  Logical 'or' using nonPositiveInteger values .
   */
  @org.junit.Test
  public void opLogicalOr076() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(0) or xs:nonPositiveInteger(0)",
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
   *  Logical 'or' using nonPositiveInteger values .
   */
  @org.junit.Test
  public void opLogicalOr077() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(0) or xs:nonPositiveInteger(-1)",
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
   *  Logical 'or' using nonPositiveInteger values .
   */
  @org.junit.Test
  public void opLogicalOr078() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(-1) or xs:nonPositiveInteger(0)",
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
   *  Logical 'or' using nonPositiveInteger values .
   */
  @org.junit.Test
  public void opLogicalOr079() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(-1) or xs:nonPositiveInteger(-1)",
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
   *  Logical 'or' using nonPositiveInteger values .
   */
  @org.junit.Test
  public void opLogicalOr080() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(-99999999999999999) or xs:nonPositiveInteger(0)",
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
   *  Logical 'or' using nonPositiveInteger values .
   */
  @org.junit.Test
  public void opLogicalOr081() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(-1) or xs:nonPositiveInteger(-99999999999999999)",
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
   *  Logical 'or' using nonPositiveInteger values .
   */
  @org.junit.Test
  public void opLogicalOr082() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(-99999999999999999) or xs:nonPositiveInteger(-99999999999999999)",
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
   *  Logical 'or' using nonNegativeInteger values .
   */
  @org.junit.Test
  public void opLogicalOr083() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(0) or xs:nonNegativeInteger(0)",
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
   *  Logical 'or' using nonNegativeInteger values .
   */
  @org.junit.Test
  public void opLogicalOr084() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(0) or xs:nonNegativeInteger(1)",
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
   *  Logical 'or' using nonNegativeInteger values .
   */
  @org.junit.Test
  public void opLogicalOr085() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(1) or xs:nonNegativeInteger(0)",
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
   *  Logical 'or' using nonNegativeInteger values .
   */
  @org.junit.Test
  public void opLogicalOr086() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(1) or xs:nonNegativeInteger(1)",
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
   *  Logical 'or' using nonNegativeInteger values .
   */
  @org.junit.Test
  public void opLogicalOr087() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(99999999999999999) or xs:nonNegativeInteger(0)",
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
   *  Logical 'or' using nonNegativeInteger values .
   */
  @org.junit.Test
  public void opLogicalOr088() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(1) or xs:nonNegativeInteger(99999999999999999)",
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
   *  Logical 'or' using nonNegativeInteger values .
   */
  @org.junit.Test
  public void opLogicalOr089() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(99999999999999999) or xs:nonNegativeInteger(99999999999999999)",
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
   *  Logical 'or' using negativeInteger values .
   */
  @org.junit.Test
  public void opLogicalOr090() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(-1) or xs:negativeInteger(-1)",
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
   *  Logical 'or' using negativeInteger values .
   */
  @org.junit.Test
  public void opLogicalOr091() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(-99999999999999999) or xs:negativeInteger(-1)",
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
   *  Logical 'or' using negativeInteger values .
   */
  @org.junit.Test
  public void opLogicalOr092() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(-99999999999999999) or xs:negativeInteger(-99999999999999999)",
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
   *  Logical 'or' using positiveInteger values .
   */
  @org.junit.Test
  public void opLogicalOr093() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(1) or xs:positiveInteger(1)",
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
   *  Logical 'or' using positiveInteger values .
   */
  @org.junit.Test
  public void opLogicalOr094() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(99999999999999999) or xs:positiveInteger(1)",
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
   *  Logical 'or' using positiveInteger values .
   */
  @org.junit.Test
  public void opLogicalOr095() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(99999999999999999) or xs:positiveInteger(99999999999999999)",
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
   *  Logical 'or' using long values .
   */
  @org.junit.Test
  public void opLogicalOr096() {
    final XQuery query = new XQuery(
      "xs:long(0) or xs:long(0)",
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
   *  Logical 'or' using long values .
   */
  @org.junit.Test
  public void opLogicalOr097() {
    final XQuery query = new XQuery(
      "xs:long(0) or xs:long(1)",
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
   *  Logical 'or' using long values .
   */
  @org.junit.Test
  public void opLogicalOr098() {
    final XQuery query = new XQuery(
      "xs:long(-1) or xs:long(0)",
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
   *  Logical 'or' using long values .
   */
  @org.junit.Test
  public void opLogicalOr099() {
    final XQuery query = new XQuery(
      "xs:long(1) or xs:long(-1)",
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
   *  Logical 'or' using long values .
   */
  @org.junit.Test
  public void opLogicalOr100() {
    final XQuery query = new XQuery(
      "xs:long(9223372036854775807) or xs:long(0)",
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
   *  Logical 'or' using long values .
   */
  @org.junit.Test
  public void opLogicalOr101() {
    final XQuery query = new XQuery(
      "xs:long(9223372036854775807) or xs:long(-1)",
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
   *  Logical 'or' using long values .
   */
  @org.junit.Test
  public void opLogicalOr102() {
    final XQuery query = new XQuery(
      "xs:long(-99999999999999999) or xs:long(0)",
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
   *  Logical 'or' using long values .
   */
  @org.junit.Test
  public void opLogicalOr103() {
    final XQuery query = new XQuery(
      "xs:long(-99999999999999999) or xs:long(1)",
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
   *  Logical 'or' using long values .
   */
  @org.junit.Test
  public void opLogicalOr104() {
    final XQuery query = new XQuery(
      "xs:long(99999999999999999) or xs:long(-99999999999999999)",
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
   *  Logical 'or' using unsignedLong values .
   */
  @org.junit.Test
  public void opLogicalOr105() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(0) or xs:unsignedLong(0)",
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
   *  Logical 'or' using unsignedLong values .
   */
  @org.junit.Test
  public void opLogicalOr106() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(0) or xs:unsignedLong(1)",
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
   *  Logical 'or' using unsignedLong values .
   */
  @org.junit.Test
  public void opLogicalOr107() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(1) or xs:unsignedLong(0)",
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
   *  Logical 'or' using unsignedLong values .
   */
  @org.junit.Test
  public void opLogicalOr108() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(1) or xs:unsignedLong(1)",
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
   *  Logical 'or' using unsignedLong values .
   */
  @org.junit.Test
  public void opLogicalOr109() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(9223372036854775807) or xs:unsignedLong(0)",
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
   *  Logical 'or' using unsignedLong values .
   */
  @org.junit.Test
  public void opLogicalOr110() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(9223372036854775807) or xs:unsignedLong(1)",
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
   *  Logical 'or' using unsignedLong values .
   */
  @org.junit.Test
  public void opLogicalOr111() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(99999999999999999) or xs:unsignedLong(99999999999999999)",
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
   *  Logical 'or' using int values .
   */
  @org.junit.Test
  public void opLogicalOr112() {
    final XQuery query = new XQuery(
      "xs:int(0) or xs:int(0)",
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
   *  Logical 'or' using int values .
   */
  @org.junit.Test
  public void opLogicalOr113() {
    final XQuery query = new XQuery(
      "xs:int(0) or xs:int(1)",
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
   *  Logical 'or' using int values .
   */
  @org.junit.Test
  public void opLogicalOr114() {
    final XQuery query = new XQuery(
      "xs:int(-1) or xs:int(0)",
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
   *  Logical 'or' using int values .
   */
  @org.junit.Test
  public void opLogicalOr115() {
    final XQuery query = new XQuery(
      "xs:int(1) or xs:int(-1)",
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
   *  Logical 'or' using int values .
   */
  @org.junit.Test
  public void opLogicalOr116() {
    final XQuery query = new XQuery(
      "xs:int(2147483647) or xs:int(0)",
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
   *  Logical 'or' using int values .
   */
  @org.junit.Test
  public void opLogicalOr117() {
    final XQuery query = new XQuery(
      "xs:int(2147483647) or xs:int(-1)",
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
   *  Logical 'or' using int values .
   */
  @org.junit.Test
  public void opLogicalOr118() {
    final XQuery query = new XQuery(
      "xs:int(-2147483648) or xs:int(0)",
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
   *  Logical 'or' using int values .
   */
  @org.junit.Test
  public void opLogicalOr119() {
    final XQuery query = new XQuery(
      "xs:int(-2147483648) or xs:int(1)",
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
   *  Logical 'or' using int values .
   */
  @org.junit.Test
  public void opLogicalOr120() {
    final XQuery query = new XQuery(
      "xs:int(2147483647) or xs:int(-2147483648)",
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
   *  Logical 'or' using unsignedInt values .
   */
  @org.junit.Test
  public void opLogicalOr121() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(0) or xs:unsignedInt(0)",
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
   *  Logical 'or' using unsignedInt values .
   */
  @org.junit.Test
  public void opLogicalOr122() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(0) or xs:unsignedInt(1)",
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
   *  Logical 'or' using unsignedInt values .
   */
  @org.junit.Test
  public void opLogicalOr123() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(1) or xs:unsignedInt(0)",
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
   *  Logical 'or' using unsignedInt values .
   */
  @org.junit.Test
  public void opLogicalOr124() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(1) or xs:unsignedInt(1)",
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
   *  Logical 'or' using unsignedInt values .
   */
  @org.junit.Test
  public void opLogicalOr125() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(4294967295) or xs:unsignedInt(0)",
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
   *  Logical 'or' using unsignedInt values .
   */
  @org.junit.Test
  public void opLogicalOr126() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(4294967295) or xs:unsignedInt(1)",
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
   *  Logical 'or' using unsignedInt values .
   */
  @org.junit.Test
  public void opLogicalOr127() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(4294967295) or xs:unsignedInt(4294967295)",
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
   *  Logical 'or' using short values .
   */
  @org.junit.Test
  public void opLogicalOr128() {
    final XQuery query = new XQuery(
      "xs:short(0) or xs:short(0)",
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
   *  Logical 'or' using short values .
   */
  @org.junit.Test
  public void opLogicalOr129() {
    final XQuery query = new XQuery(
      "xs:short(0) or xs:short(1)",
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
   *  Logical 'or' using short values .
   */
  @org.junit.Test
  public void opLogicalOr130() {
    final XQuery query = new XQuery(
      "xs:short(-1) or xs:short(0)",
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
   *  Logical 'or' using short values .
   */
  @org.junit.Test
  public void opLogicalOr131() {
    final XQuery query = new XQuery(
      "xs:short(1) or xs:short(-1)",
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
   *  Logical 'or' using short values .
   */
  @org.junit.Test
  public void opLogicalOr132() {
    final XQuery query = new XQuery(
      "xs:short(32767) or xs:short(0)",
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
   *  Logical 'or' using short values .
   */
  @org.junit.Test
  public void opLogicalOr133() {
    final XQuery query = new XQuery(
      "xs:short(32767) or xs:short(-1)",
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
   *  Logical 'or' using short values .
   */
  @org.junit.Test
  public void opLogicalOr134() {
    final XQuery query = new XQuery(
      "xs:short(-32768) or xs:short(0)",
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
   *  Logical 'or' using short values .
   */
  @org.junit.Test
  public void opLogicalOr135() {
    final XQuery query = new XQuery(
      "xs:short(-32768) or xs:short(1)",
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
   *  Logical 'or' using short values .
   */
  @org.junit.Test
  public void opLogicalOr136() {
    final XQuery query = new XQuery(
      "xs:short(32767) or xs:short(-32768)",
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
   *  Logical 'or' using unsignedShort values .
   */
  @org.junit.Test
  public void opLogicalOr137() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(0) or xs:unsignedShort(0)",
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
   *  Logical 'or' using unsignedShort values .
   */
  @org.junit.Test
  public void opLogicalOr138() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(0) or xs:unsignedShort(1)",
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
   *  Logical 'or' using unsignedShort values .
   */
  @org.junit.Test
  public void opLogicalOr139() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(1) or xs:unsignedShort(0)",
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
   *  Logical 'or' using unsignedShort values .
   */
  @org.junit.Test
  public void opLogicalOr140() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(1) or xs:unsignedShort(1)",
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
   *  Logical 'or' using unsignedShort values .
   */
  @org.junit.Test
  public void opLogicalOr141() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(65535) or xs:unsignedShort(0)",
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
   *  Logical 'or' using unsignedShort values .
   */
  @org.junit.Test
  public void opLogicalOr142() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(65535) or xs:unsignedShort(1)",
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
   *  Logical 'or' using unsignedShort values .
   */
  @org.junit.Test
  public void opLogicalOr143() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(65535) or xs:unsignedShort(65535)",
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
   *  Logical 'or' using byte values .
   */
  @org.junit.Test
  public void opLogicalOr144() {
    final XQuery query = new XQuery(
      "xs:byte(0) or xs:byte(0)",
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
   *  Logical 'or' using byte values .
   */
  @org.junit.Test
  public void opLogicalOr145() {
    final XQuery query = new XQuery(
      "xs:byte(0) or xs:byte(1)",
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
   *  Logical 'or' using byte values .
   */
  @org.junit.Test
  public void opLogicalOr146() {
    final XQuery query = new XQuery(
      "xs:byte(-1) or xs:byte(0)",
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
   *  Logical 'or' using byte values .
   */
  @org.junit.Test
  public void opLogicalOr147() {
    final XQuery query = new XQuery(
      "xs:byte(1) or xs:byte(-1)",
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
   *  Logical 'or' using byte values .
   */
  @org.junit.Test
  public void opLogicalOr148() {
    final XQuery query = new XQuery(
      "xs:byte(127) or xs:byte(0)",
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
   *  Logical 'or' using byte values .
   */
  @org.junit.Test
  public void opLogicalOr149() {
    final XQuery query = new XQuery(
      "xs:byte(127) or xs:byte(-1)",
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
   *  Logical 'or' using byte values .
   */
  @org.junit.Test
  public void opLogicalOr150() {
    final XQuery query = new XQuery(
      "xs:byte(-128) or xs:byte(0)",
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
   *  Logical 'or' using byte values .
   */
  @org.junit.Test
  public void opLogicalOr151() {
    final XQuery query = new XQuery(
      "xs:byte(-128) or xs:byte(1)",
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
   *  Logical 'or' using byte values .
   */
  @org.junit.Test
  public void opLogicalOr152() {
    final XQuery query = new XQuery(
      "xs:byte(127) or xs:byte(-128)",
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
   *  Logical 'or' using unsignedByte values .
   */
  @org.junit.Test
  public void opLogicalOr153() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(0) or xs:unsignedByte(0)",
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
   *  Logical 'or' using unsignedByte values .
   */
  @org.junit.Test
  public void opLogicalOr154() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(0) or xs:unsignedByte(1)",
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
   *  Logical 'or' using unsignedByte values .
   */
  @org.junit.Test
  public void opLogicalOr155() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(1) or xs:unsignedByte(0)",
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
   *  Logical 'or' using unsignedByte values .
   */
  @org.junit.Test
  public void opLogicalOr156() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(1) or xs:unsignedByte(1)",
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
   *  Logical 'or' using unsignedByte values .
   */
  @org.junit.Test
  public void opLogicalOr157() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(255) or xs:unsignedByte(0)",
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
   *  Logical 'or' using unsignedByte values .
   */
  @org.junit.Test
  public void opLogicalOr158() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(255) or xs:unsignedByte(1)",
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
   *  Logical 'or' using unsignedByte values .
   */
  @org.junit.Test
  public void opLogicalOr159() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(255) or xs:unsignedByte(255)",
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
}
