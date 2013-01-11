package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the ValueComp (value comparison) production.
 *       See also tests for the individual operators..
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdValueComp extends QT3TestSet {

  /**
   *  A value comparison where the cardinality of the operand(s) is wrong. .
   */
  @org.junit.Test
  public void kValCompTypeChecking1() {
    final XQuery query = new XQuery(
      "(1, 2, 3) eq 3",
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
   *  A value comparison where the cardinality of the operand(s) is wrong. .
   */
  @org.junit.Test
  public void kValCompTypeChecking10() {
    final XQuery query = new XQuery(
      "3 gt (1, 2, 3)",
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
   *  A value comparison where the cardinality of the operand(s) is wrong. .
   */
  @org.junit.Test
  public void kValCompTypeChecking11() {
    final XQuery query = new XQuery(
      "(1, 2, 3) ge 3",
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
   *  A value comparison where the cardinality of the operand(s) is wrong. .
   */
  @org.junit.Test
  public void kValCompTypeChecking12() {
    final XQuery query = new XQuery(
      "3 ge (1, 2, 3)",
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
   *  A value comparison involving empty sequence(s). .
   */
  @org.junit.Test
  public void kValCompTypeChecking13() {
    final XQuery query = new XQuery(
      "empty(() eq 1)",
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
   *  A value comparison involving empty sequence(s). .
   */
  @org.junit.Test
  public void kValCompTypeChecking14() {
    final XQuery query = new XQuery(
      "empty(1 eq ())",
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
   *  A value comparison involving empty sequence(s). .
   */
  @org.junit.Test
  public void kValCompTypeChecking15() {
    final XQuery query = new XQuery(
      "empty(() eq ())",
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
   *  Value comparison involving xs:untypedAtomic, which leads to an inexistent operator mapping. .
   */
  @org.junit.Test
  public void kValCompTypeChecking16() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"1\") eq xs:integer(1)",
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
   *  Value comparison involving xs:untypedAtomic, which leads to an inexistent operator mapping. .
   */
  @org.junit.Test
  public void kValCompTypeChecking17() {
    final XQuery query = new XQuery(
      "xs:integer(1) eq xs:untypedAtomic(\"1\")",
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
   *  Value comparison involving xs:untypedAtomic, which leads to an inexistent operator mapping. .
   */
  @org.junit.Test
  public void kValCompTypeChecking18() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"0\") ne xs:double(1)",
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
   *  Value comparison involving xs:untypedAtomic, which leads to an inexistent operator mapping. .
   */
  @org.junit.Test
  public void kValCompTypeChecking19() {
    final XQuery query = new XQuery(
      "xs:double(1) ne xs:untypedAtomic(\"0\")",
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
   *  A value comparison where the cardinality of the operand(s) is wrong. .
   */
  @org.junit.Test
  public void kValCompTypeChecking2() {
    final XQuery query = new XQuery(
      "3 eq (1, 2, 3)",
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
   *  Value comparison involving xs:untypedAtomic, which leads to an inexistent operator mapping. .
   */
  @org.junit.Test
  public void kValCompTypeChecking20() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"0\") lt xs:float(1)",
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
   *  Value comparison involving xs:untypedAtomic, which leads to an inexistent operator mapping. .
   */
  @org.junit.Test
  public void kValCompTypeChecking21() {
    final XQuery query = new XQuery(
      "xs:float(0) lt xs:untypedAtomic(\"1\")",
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
   *  Value comparison involving xs:untypedAtomic, which leads to an inexistent operator mapping. .
   */
  @org.junit.Test
  public void kValCompTypeChecking22() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"0\") gt xs:decimal(1)",
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
   *  Value comparison involving xs:untypedAtomic, which leads to an inexistent operator mapping. .
   */
  @org.junit.Test
  public void kValCompTypeChecking23() {
    final XQuery query = new XQuery(
      "xs:decimal(0) gt xs:untypedAtomic(\"1\")",
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
   *  Value comparison involving xs:untypedAtomic, which leads to an inexistent operator mapping. .
   */
  @org.junit.Test
  public void kValCompTypeChecking24() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"4\") eq 4",
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
   *  Value comparison involving xs:untypedAtomic, which leads to an inexistent operator mapping. .
   */
  @org.junit.Test
  public void kValCompTypeChecking25() {
    final XQuery query = new XQuery(
      "4 eq xs:untypedAtomic(\"4\")",
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
   *  Value comparison involving xs:untypedAtomic, which leads to an inexistent operator mapping. .
   */
  @org.junit.Test
  public void kValCompTypeChecking26() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"3\") eq 3",
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
   *  Value comparison involving xs:untypedAtomic, which leads to an inexistent operator mapping. .
   */
  @org.junit.Test
  public void kValCompTypeChecking27() {
    final XQuery query = new XQuery(
      "xs:double(2) lt xs:untypedAtomic(\"3\")",
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
   *  Value comparison involving xs:untypedAtomic, which leads to an inexistent operator mapping. .
   */
  @org.junit.Test
  public void kValCompTypeChecking28() {
    final XQuery query = new XQuery(
      "xs:float(4) gt xs:untypedAtomic(\"3\")",
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
   *  Value comparison involving xs:untypedAtomic, which leads to an inexistent operator mapping. .
   */
  @org.junit.Test
  public void kValCompTypeChecking29() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"3\") ne xs:decimal(3.1)",
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
   *  A value comparison where the cardinality of the operand(s) is wrong. .
   */
  @org.junit.Test
  public void kValCompTypeChecking3() {
    final XQuery query = new XQuery(
      "(1, 2, 3) ne 3",
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
   *  Compare xs:untypedAtomic and xs:string. .
   */
  @org.junit.Test
  public void kValCompTypeChecking30() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"a string\") eq \"a string\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Compare xs:untypedAtomic and xs:string. .
   */
  @org.junit.Test
  public void kValCompTypeChecking31() {
    final XQuery query = new XQuery(
      "\"a string\" eq xs:untypedAtomic(\"a string\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  An expression involving the ge operator that trigger certain optimization paths in some implementations. .
   */
  @org.junit.Test
  public void kValCompTypeChecking32() {
    final XQuery query = new XQuery(
      "count((0, current-time())) ge 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  An expression involving the ne operator that trigger certain optimization paths in some implementations. .
   */
  @org.junit.Test
  public void kValCompTypeChecking33() {
    final XQuery query = new XQuery(
      "count((0, current-time())) ne 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
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
  public void kValCompTypeChecking34() {
    final XQuery query = new XQuery(
      "not(count((1, 2, current-time())) eq 0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Combine error() with a value comparison, testing handling of the 'none' type. .
   */
  @org.junit.Test
  public void kValCompTypeChecking35() {
    final XQuery query = new XQuery(
      "error() eq 3",
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
   *  Combine error() with a value comparison, testing handling of the 'none' type. .
   */
  @org.junit.Test
  public void kValCompTypeChecking36() {
    final XQuery query = new XQuery(
      "error() eq error()",
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
   *  Combine error() with a value comparison, testing handling of the 'none' type. .
   */
  @org.junit.Test
  public void kValCompTypeChecking37() {
    final XQuery query = new XQuery(
      "3 eq error()",
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
   *  Combine error() with a value comparison, testing handling of the 'none' type. .
   */
  @org.junit.Test
  public void kValCompTypeChecking38() {
    final XQuery query = new XQuery(
      "3 eq (error(), 3)",
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
   *  Combine error() with a value comparison, testing handling of the 'none' type. .
   */
  @org.junit.Test
  public void kValCompTypeChecking39() {
    final XQuery query = new XQuery(
      "3 eq (3, error())",
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
   *  A value comparison where the cardinality of the operand(s) is wrong. .
   */
  @org.junit.Test
  public void kValCompTypeChecking4() {
    final XQuery query = new XQuery(
      "3 ne (1, 2, 3)",
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
   *  Combine error() with a value comparison, testing handling of the 'none' type. .
   */
  @org.junit.Test
  public void kValCompTypeChecking40() {
    final XQuery query = new XQuery(
      "(error(), 3) eq 3",
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
   *  Combine error() with a value comparison, testing handling of the 'none' type. .
   */
  @org.junit.Test
  public void kValCompTypeChecking41() {
    final XQuery query = new XQuery(
      "(3, error()) eq 3",
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
   *  A simple value comparison that in some Bison grammars triggers a bug. .
   */
  @org.junit.Test
  public void kValCompTypeChecking42() {
    final XQuery query = new XQuery(
      "count((1, 2)) eq 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A value comparison where the cardinality of the operand(s) is wrong. .
   */
  @org.junit.Test
  public void kValCompTypeChecking5() {
    final XQuery query = new XQuery(
      "(1, 2, 3) lt 3",
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
   *  A value comparison where the cardinality of the operand(s) is wrong. .
   */
  @org.junit.Test
  public void kValCompTypeChecking6() {
    final XQuery query = new XQuery(
      "3 lt (1, 2, 3)",
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
   *  A value comparison where the cardinality of the operand(s) is wrong. .
   */
  @org.junit.Test
  public void kValCompTypeChecking7() {
    final XQuery query = new XQuery(
      "(1, 2, 3) le 3",
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
   *  A value comparison where the cardinality of the operand(s) is wrong. .
   */
  @org.junit.Test
  public void kValCompTypeChecking8() {
    final XQuery query = new XQuery(
      "3 le (1, 2, 3)",
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
   *  A value comparison where the cardinality of the operand(s) is wrong. .
   */
  @org.junit.Test
  public void kValCompTypeChecking9() {
    final XQuery query = new XQuery(
      "(1, 2, 3) gt 3",
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
}
