package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the GeneralComp production using the "=" operator.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdGeneralCompEq extends QT3TestSet {

  /**
   *  General comparison where both types are instances of xs:untypedAtomic. .
   */
  @org.junit.Test
  public void genCompEq1() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"a string\") = xs:untypedAtomic(\"a stringDIFF\")",
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
   *  General comparison where one of the types is xs:untypedAtomic and the other xs:NCName. Expected error FORG0001. .
   */
  @org.junit.Test
  public void genCompEq2() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"1\") = xs:NCName(\"string\")",
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
   *  General comparison where one of the types is xs:untypedAtomic and the other xs:dayTimeDuration .
   */
  @org.junit.Test
  public void genCompEq3() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT08H34M12.143S\") = xs:untypedAtomic(\"P3DT08H34M12.143S\")",
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
   *  General comparison where one of the types is xs:untypedAtomic and the other xs:dayTimeDuration Expected error FORG0001. .
   */
  @org.junit.Test
  public void genCompEq4() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"0\") = xs:dayTimeDuration(\"PT0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  General comparison where one of the types is xs:untypedAtomic and the other xs:yearMonthDuration .
   */
  @org.junit.Test
  public void genCompEq5() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"P1999Y10M\") = xs:yearMonthDuration(\"P1999Y10M\")",
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
   *  General comparison where one of the types is xs:untypedAtomic and the other xs:yearMonthDuration Expected error FORG0001. .
   */
  @org.junit.Test
  public void genCompEq6() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y\") = xs:untypedAtomic(\"1999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  General comparisons involving the empty sequence. .
   */
  @org.junit.Test
  public void kGenCompEq1() {
    final XQuery query = new XQuery(
      "not(() = ())",
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
   *  Example from the XPath 2.0 specification. .
   */
  @org.junit.Test
  public void kGenCompEq10() {
    final XQuery query = new XQuery(
      "not((xs:untypedAtomic(\"1\"), xs:untypedAtomic(\"2\")) = (xs:untypedAtomic(\"2.0\"), 3.0))",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  General comparison causing numeric promotion from xs:untypedAtomic. .
   */
  @org.junit.Test
  public void kGenCompEq11() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"1\") = 1",
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
   *  General comparison causing numeric promotion from xs:untypedAtomic. .
   */
  @org.junit.Test
  public void kGenCompEq12() {
    final XQuery query = new XQuery(
      "not(xs:untypedAtomic(\"2\") = 1)",
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
   *  General comparison causing numeric promotion from xs:untypedAtomic. .
   */
  @org.junit.Test
  public void kGenCompEq13() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"1\") = 1",
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
   *  General comparison causing numeric promotion from xs:untypedAtomic. .
   */
  @org.junit.Test
  public void kGenCompEq14() {
    final XQuery query = new XQuery(
      "1 = xs:untypedAtomic(\"1\")",
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
   *  General comparison causing numeric promotion from xs:untypedAtomic. .
   */
  @org.junit.Test
  public void kGenCompEq15() {
    final XQuery query = new XQuery(
      "1 = xs:untypedAtomic(\"1\")",
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
   *  A general comparison involving the error() function. .
   */
  @org.junit.Test
  public void kGenCompEq16() {
    final XQuery query = new XQuery(
      "error() = 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   *  A general comparison involving the error() function. .
   */
  @org.junit.Test
  public void kGenCompEq17() {
    final XQuery query = new XQuery(
      "(error(), 3) = 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   *  A general comparison involving the error() function. .
   */
  @org.junit.Test
  public void kGenCompEq18() {
    final XQuery query = new XQuery(
      "3 = error()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   *  A general comparison involving the error() function. .
   */
  @org.junit.Test
  public void kGenCompEq19() {
    final XQuery query = new XQuery(
      "3 = (error(), 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   *  General comparisons involving the empty sequence. .
   */
  @org.junit.Test
  public void kGenCompEq2() {
    final XQuery query = new XQuery(
      "not((() = ()))",
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
   *  An expression involving the '=' operator that trigger certain optimization paths in some implementations. .
   */
  @org.junit.Test
  public void kGenCompEq20() {
    final XQuery query = new XQuery(
      "count(remove(remove((current-time(), 1), 1), 1)) = 0",
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
   *  Test that fn:count combined with expressions that might disable compile time evaluations(optimization) as well as the '=' operator, is conformant. .
   */
  @org.junit.Test
  public void kGenCompEq21() {
    final XQuery query = new XQuery(
      "not(0 = count((1, 2, timezone-from-time(current-time()))))",
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
   *  Test that fn:count combined with expressions that might disable compile time evaluations(optimization) as well as the '=' operator, is conformant. .
   */
  @org.junit.Test
  public void kGenCompEq22() {
    final XQuery query = new XQuery(
      "0 != count((1, 2, timezone-from-time(current-time())))",
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
   *  Comparison where type of operands are incompatible. .
   */
  @org.junit.Test
  public void kGenCompEq23() {
    final XQuery query = new XQuery(
      "(xs:anyURI(\"example.com/\"), 1, QName(\"example.com\", \"ncname\"), false(), xs:hexBinary(\"FF\")) = (xs:anyURI(\"example.com/NOT\"), 0, QName(\"example.com\", \"p:ncname\"), true(), xs:hexBinary(\"EF\"))",
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
   *  General comparison involving xs:untypedAtomic/xs:string. .
   */
  @org.junit.Test
  public void kGenCompEq24() {
    final XQuery query = new XQuery(
      "\"a string\" = \"a string\"",
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
   *  General comparison involving xs:untypedAtomic/xs:string. .
   */
  @org.junit.Test
  public void kGenCompEq25() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"a string\") = \"a string\"",
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
   *  General comparison involving xs:untypedAtomic/xs:string. .
   */
  @org.junit.Test
  public void kGenCompEq26() {
    final XQuery query = new XQuery(
      "\"a string\" = xs:untypedAtomic(\"a string\")",
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
   *  General comparison involving xs:untypedAtomic/xs:string. .
   */
  @org.junit.Test
  public void kGenCompEq27() {
    final XQuery query = new XQuery(
      "not(xs:untypedAtomic(\"a string\") = \"a stringDIFF\")",
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
   *  General comparison involving xs:untypedAtomic/xs:string. .
   */
  @org.junit.Test
  public void kGenCompEq28() {
    final XQuery query = new XQuery(
      "not(\"a string\" = xs:untypedAtomic(\"a stringDIFF\"))",
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
   *  General comparison involving xs:untypedAtomic/xs:string. .
   */
  @org.junit.Test
  public void kGenCompEq29() {
    final XQuery query = new XQuery(
      "not(\"a string\" = \"a stringDIFF\")",
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
   *  General comparisons involving the empty sequence. .
   */
  @org.junit.Test
  public void kGenCompEq3() {
    final XQuery query = new XQuery(
      "not((() = 1))",
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
   *  General comparison with remove() as operand, resulting in incompatible operand types. .
   */
  @org.junit.Test
  public void kGenCompEq30() {
    final XQuery query = new XQuery(
      "remove((6, \"a string\"), 1) = 6",
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
   *  General comparison with remove() as operand, resulting in incompatible operand types. .
   */
  @org.junit.Test
  public void kGenCompEq31() {
    final XQuery query = new XQuery(
      "6 = remove((\"a string\", 6), 2)",
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
   *  General comparison involving remove(), resulting in operands that require conversion to numeric from xs:untypedAtomic. .
   */
  @org.junit.Test
  public void kGenCompEq32() {
    final XQuery query = new XQuery(
      "remove((6, \"a string\"), 2) = xs:untypedAtomic(\"6\")",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  General comparison involving remove(), resulting in operands that require conversion to numeric from xs:untypedAtomic. .
   */
  @org.junit.Test
  public void kGenCompEq33() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"6\") = remove((\"a string\", 6), 1)",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  General comparison involving remove(), resulting in operands that require conversion to numeric from xs:untypedAtomic. Implementations supporting the static typing feature may raise XPTY0004. .
   */
  @org.junit.Test
  public void kGenCompEq34() {
    final XQuery query = new XQuery(
      "(remove((xs:untypedAtomic(\"6\"), \"a string\"), 2)) = 6",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  General comparison involving remove(), resulting in operands that require conversion to numeric from xs:untypedAtomic. Implementations supporting the static typing feature may raise XPTY0004. .
   */
  @org.junit.Test
  public void kGenCompEq35() {
    final XQuery query = new XQuery(
      "6 = (remove((\"a string\", xs:untypedAtomic(\"6\")), 1))",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompEq36() {
    final XQuery query = new XQuery(
      "1 = 1",
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
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompEq37() {
    final XQuery query = new XQuery(
      "(1, 2, 3) = 1",
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
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompEq38() {
    final XQuery query = new XQuery(
      "(1, 2, 3) = 2",
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
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompEq39() {
    final XQuery query = new XQuery(
      "(1, 2, 3) = 3",
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
   *  General comparisons involving the empty sequence. .
   */
  @org.junit.Test
  public void kGenCompEq4() {
    final XQuery query = new XQuery(
      "not(1 = ())",
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
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompEq40() {
    final XQuery query = new XQuery(
      "2 = (1, 2, 3)",
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
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompEq41() {
    final XQuery query = new XQuery(
      "1 = (1, 2, 3)",
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
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompEq42() {
    final XQuery query = new XQuery(
      "3 = (1, 2, 3)",
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
   *  General comparison causing a xs:untypedAtomic value to be cast to xs:boolean, and then compared. .
   */
  @org.junit.Test
  public void kGenCompEq43() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"false\") = false()",
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
   *  General comparison causing a xs:untypedAtomic value to be cast to xs:boolean, and then compared. .
   */
  @org.junit.Test
  public void kGenCompEq44() {
    final XQuery query = new XQuery(
      "false() = xs:untypedAtomic(\"false\")",
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
   *  General comparison causing a xs:untypedAtomic value to be cast to xs:boolean, and then compared. .
   */
  @org.junit.Test
  public void kGenCompEq45() {
    final XQuery query = new XQuery(
      "not(xs:untypedAtomic(\"true\") = false())",
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
   *  General comparison causing a xs:untypedAtomic value to be cast to xs:boolean, and then compared. .
   */
  @org.junit.Test
  public void kGenCompEq46() {
    final XQuery query = new XQuery(
      "(true() = xs:untypedAtomic(\"true\"))",
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
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompEq47() {
    final XQuery query = new XQuery(
      "\"1\" = 1",
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
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompEq48() {
    final XQuery query = new XQuery(
      "1 = \"1\"",
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
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompEq49() {
    final XQuery query = new XQuery(
      "false() = 5",
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
   *  A syntactically invalid expression that reminds of a general comparison operator. .
   */
  @org.junit.Test
  public void kGenCompEq5() {
    final XQuery query = new XQuery(
      "1 == 1",
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
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompEq50() {
    final XQuery query = new XQuery(
      "5 = false()",
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
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompEq51() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"three\") = 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompEq52() {
    final XQuery query = new XQuery(
      "xs:string(\"false\") = false()",
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
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompEq53() {
    final XQuery query = new XQuery(
      "false() = xs:string(\"false\")",
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
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompEq54() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"falseERR\") = false()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompEq55() {
    final XQuery query = new XQuery(
      "(xs:untypedAtomic(\"1\"), xs:anyURI(\"example.com\")) = (xs:untypedAtomic(\"2.0\"), 3.0)",
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
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompEq56() {
    final XQuery query = new XQuery(
      "false() = xs:untypedAtomic(\"falseERR\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompEq57() {
    final XQuery query = new XQuery(
      "3 = xs:untypedAtomic(\"three\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompEq58() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"example.com/\") = false()",
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
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompEq59() {
    final XQuery query = new XQuery(
      "false() = xs:anyURI(\"example.com/\")",
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
   *  Example from the XPath 2.0 specification. .
   */
  @org.junit.Test
  public void kGenCompEq6() {
    final XQuery query = new XQuery(
      "(1, 2) = (2, 3)",
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
   *  Example from the XPath 2.0 specification. .
   */
  @org.junit.Test
  public void kGenCompEq7() {
    final XQuery query = new XQuery(
      "(2, 3) = (3, 4)",
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
   *  Example from the XPath 2.0 specification. .
   */
  @org.junit.Test
  public void kGenCompEq8() {
    final XQuery query = new XQuery(
      "not((1, 2) = (3, 4))",
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
   *  Example from the XPath 2.0 specification. .
   */
  @org.junit.Test
  public void kGenCompEq9() {
    final XQuery query = new XQuery(
      "(xs:untypedAtomic(\"1\"), xs:untypedAtomic(\"2\")) = (xs:untypedAtomic(\"2.0\"), 2.0)",
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
        error("XPTY0004")
      )
    );
  }

  /**
   *  Compare two values returned from fn:lower-case(). .
   */
  @org.junit.Test
  public void k2GenCompEq1() {
    final XQuery query = new XQuery(
      "declare variable $vA as xs:string := (\"B STRING\", current-time(), string(<e>content</e>))[1] treat as xs:string; declare variable $vB as xs:string := (\"b string\", current-time(), string(<e>content</e>))[1] treat as xs:string; (lower-case($vA) = lower-case($vB))",
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
   *  Compare two values returned from fn:upper-case(). .
   */
  @org.junit.Test
  public void k2GenCompEq2() {
    final XQuery query = new XQuery(
      "declare variable $vA as xs:string := (\"B STRING\", current-time(), string(<e>content</e>))[1] treat as xs:string; declare variable $vB as xs:string := (\"b string\", current-time(), string(<e>content</e>))[1] treat as xs:string; (upper-case($vA) = upper-case($vB))",
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
   *  Compare two non-matching values returned from fn:lower-case(). .
   */
  @org.junit.Test
  public void k2GenCompEq3() {
    final XQuery query = new XQuery(
      "declare variable $vA as xs:string := (\"B STRING\", current-time(), string(<e>content</e>))[1] treat as xs:string; declare variable $vB as xs:string := (\"no match\", current-time(), string(<e>content</e>))[1] treat as xs:string; (lower-case($vA) = lower-case($vB))",
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
   *  Compare two non-matching values returned from fn:upper-case(). .
   */
  @org.junit.Test
  public void k2GenCompEq4() {
    final XQuery query = new XQuery(
      "declare variable $vA as xs:string := (\"B STRING\", current-time(), string(<e>content</e>))[1] treat as xs:string; declare variable $vB as xs:string := (\"no match\", current-time(), string(<e>content</e>))[1] treat as xs:string; (upper-case($vA) = upper-case($vB))",
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
   *  Compare two non-matching values returned from fn:lower-case() and fn:upper-case(). .
   */
  @org.junit.Test
  public void k2GenCompEq5() {
    final XQuery query = new XQuery(
      "declare variable $vA as xs:string := (\"B STRING\", current-time(), string(<e>content</e>))[1] treat as xs:string; declare variable $vB as xs:string := (\"b string\", current-time(), string(<e>content</e>))[1] treat as xs:string; (upper-case($vA) = lower-case($vB))",
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
   *  Compare two non-matching values returned from fn:lower-case() and fn:upper-case(). .
   */
  @org.junit.Test
  public void k2GenCompEq6() {
    final XQuery query = new XQuery(
      "declare variable $vA as xs:string := (\"B STRING\", current-time(), string(<e>content</e>))[1] treat as xs:string; declare variable $vB as xs:string := (\"b string\", current-time(), string(<e>content</e>))[1] treat as xs:string; (lower-case($vA) = upper-case($vB))",
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
   *  A failing call to a codepoints-to-string(), that may be written to true. See section 2.3.4. .
   */
  @org.junit.Test
  public void k2GenCompEq7() {
    final XQuery query = new XQuery(
      "let $x := codepoints-to-string(12) return ($x = $x)",
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
        error("FOCH0001")
      )
    );
  }

  /**
   *  The operator causes atomization. .
   */
  @org.junit.Test
  public void k2GenCompEq8() {
    final XQuery query = new XQuery(
      "empty(for $b in <e/> where $b/@id=\"person0\" return ())",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = = operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression1() {
    final XQuery query = new XQuery(
      "() = ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = = operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression10() {
    final XQuery query = new XQuery(
      "() = ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = = operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression11() {
    final XQuery query = new XQuery(
      "10000 = ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = "=" operand2 = empty sequence .
   */
  @org.junit.Test
  public void generalexpression12() {
    final XQuery query = new XQuery(
      "10000 = ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = = operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression13() {
    final XQuery query = new XQuery(
      "10000 = (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = = operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression14() {
    final XQuery query = new XQuery(
      "10000 = <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = = operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression15() {
    final XQuery query = new XQuery(
      "10000 = (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = = operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression16() {
    final XQuery query = new XQuery(
      "10000 = (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = = operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression17() {
    final XQuery query = new XQuery(
      "10000 = (/works/employee[1]/hours[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = = operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression18() {
    final XQuery query = new XQuery(
      "10000 = (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = = operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression19() {
    final XQuery query = new XQuery(
      "10000 = ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = = operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression2() {
    final XQuery query = new XQuery(
      "() = 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = = operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression20() {
    final XQuery query = new XQuery(
      "(50000) = ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = = operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression21() {
    final XQuery query = new XQuery(
      "(50000) = 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = = operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression22() {
    final XQuery query = new XQuery(
      "(50000) = (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = = operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression23() {
    final XQuery query = new XQuery(
      "(50000) = (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = = operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression24() {
    final XQuery query = new XQuery(
      "(50000) = <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = = operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression25() {
    final XQuery query = new XQuery(
      "(50000) = (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = = operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression26() {
    final XQuery query = new XQuery(
      "(50000) = (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = = operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression27() {
    final XQuery query = new XQuery(
      "(50000) = (/works/employee[1]/hours[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = = operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression28() {
    final XQuery query = new XQuery(
      "(50000) = (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = = operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression29() {
    final XQuery query = new XQuery(
      "(50000) = ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = = operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression3() {
    final XQuery query = new XQuery(
      "() = (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = = operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression30() {
    final XQuery query = new XQuery(
      "(10000,50000) = ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = = operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression31() {
    final XQuery query = new XQuery(
      "(10000,50000) = 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = = operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression32() {
    final XQuery query = new XQuery(
      "(10000,50000) = (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = = operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression33() {
    final XQuery query = new XQuery(
      "(10000,50000) = (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = = operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression34() {
    final XQuery query = new XQuery(
      "(10000,50000) = <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = = operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression35() {
    final XQuery query = new XQuery(
      "(10000,50000) = (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = = operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression36() {
    final XQuery query = new XQuery(
      "(10000,50000) = (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = = operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression37() {
    final XQuery query = new XQuery(
      "(10000,50000) = (/works/employee[1]/hours[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = = operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression38() {
    final XQuery query = new XQuery(
      "(10000,50000) = (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = = operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression39() {
    final XQuery query = new XQuery(
      "(10000,50000) = ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = = operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression4() {
    final XQuery query = new XQuery(
      "() = (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = = operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression40() {
    final XQuery query = new XQuery(
      "<a>10000</a> = ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = = operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression41() {
    final XQuery query = new XQuery(
      "<a>10000</a> = 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = = operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression42() {
    final XQuery query = new XQuery(
      "<a>10000</a> = (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = = operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression43() {
    final XQuery query = new XQuery(
      "<a>10000</a> = (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = = operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression44() {
    final XQuery query = new XQuery(
      "<a>10000</a> = <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = = operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression45() {
    final XQuery query = new XQuery(
      "<a>10000</a> = (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = = operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression46() {
    final XQuery query = new XQuery(
      "<a>10000</a> = (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = = operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression47() {
    final XQuery query = new XQuery(
      "<a>10000</a> = (/works/employee[1]/hours[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = = operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression48() {
    final XQuery query = new XQuery(
      "<a>10000</a> = (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = = operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression49() {
    final XQuery query = new XQuery(
      "<a>10000</a> = ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = = operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression5() {
    final XQuery query = new XQuery(
      "() = <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = = operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression50() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) = ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = = operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression51() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) = 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = = operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression52() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) = (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = = operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression53() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) = (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = = operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression54() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) = <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = = operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression55() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) = (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = = operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression56() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) = (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = = operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression57() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) = (/works/employee[1]/hours[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = = operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression58() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) = (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = = operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression59() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) = ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = = operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression6() {
    final XQuery query = new XQuery(
      "() = (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = = operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression60() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) = ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = = operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression61() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) = 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = = operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression62() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) = (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = = operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression63() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) = (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = = operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression64() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) = <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = = operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression65() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) = (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = = operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression66() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) = (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = = operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression67() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) = (/works/employee[1]/hours[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = = operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression68() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) = (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = = operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression69() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) = ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = = operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression7() {
    final XQuery query = new XQuery(
      "() = (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = = operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression70() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) = ()",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = = operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression71() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) = 10000",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = = operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression72() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) = (50000)",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = = operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression73() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) = (10000,50000)",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = = operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression74() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) = <a>10000</a>",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = = operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression75() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) = (<a>10000</a>)",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = = operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression76() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) = (<a>10000</a>,<b>50000</b>)",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = = operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression77() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) = (/works/employee[1]/hours[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = = operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression78() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) = (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = = operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression79() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1]) = ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = = operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression8() {
    final XQuery query = new XQuery(
      "() = (/works/employee[1]/hours[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = = operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression80() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) = ()",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = = operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression81() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) = 10000",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = = operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression82() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) = (50000)",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = = operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression83() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) = (10000,50000)",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = = operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression84() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) = <a>10000</a>",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = = operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression85() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) = (<a>10000</a>)",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = = operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression86() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) = (<a>10000</a>,<b>50000</b>)",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = = operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression87() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) = (/works/employee[1]/hours[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = = operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression88() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) = (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = = operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression89() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$works/works/employee[6]/hours[1]) = \n" +
      "         ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = = operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression9() {
    final XQuery query = new XQuery(
      "() = (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = = operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression90() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) = ()",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = = operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression91() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) = 10000",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = = operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression92() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) = (50000)",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = = operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression93() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) = (10000,50000)",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = = operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression94() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) = <a>10000</a>",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = = operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression95() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) = (<a>10000</a>)",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = = operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression96() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) = (<a>10000</a>,<b>50000</b>)",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = = operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression97() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) =\n" +
      "         ($works/works/employee[1]/hours[1])",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = = operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression98() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) = ($works/works/employee[1]/hours[1],$works/works/employee[6]/hours[1])",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = = operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression99() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) = ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
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
