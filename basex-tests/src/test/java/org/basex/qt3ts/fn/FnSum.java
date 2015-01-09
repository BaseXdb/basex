package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the sum() function.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnSum extends QT3TestSet {

  /**
   *  A test whose essence is: `sum()`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc1() {
    final XQuery query = new XQuery(
      "sum()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `sum((-5, -0, -3, -6)) eq -14`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc10() {
    final XQuery query = new XQuery(
      "sum((-5, -0, -3, -6)) eq -14",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(sum((1, 2, 3, xs:float("NaN")))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc11() {
    final XQuery query = new XQuery(
      "string(sum((1, 2, 3, xs:float(\"NaN\")))) eq \"NaN\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(sum((1, 2, 3, xs:double("NaN")))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc12() {
    final XQuery query = new XQuery(
      "string(sum((1, 2, 3, xs:double(\"NaN\")))) eq \"NaN\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(sum((xs:double("NaN"), 1, 2, 3))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc13() {
    final XQuery query = new XQuery(
      "string(sum((xs:double(\"NaN\"), 1, 2, 3))) eq \"NaN\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(sum((xs:float("NaN"), 1, 2, 3))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc14() {
    final XQuery query = new XQuery(
      "string(sum((xs:float(\"NaN\"), 1, 2, 3))) eq \"NaN\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(sum((1, 2, xs:double("NaN"), 1, 2, 3))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc15() {
    final XQuery query = new XQuery(
      "string(sum((1, 2, xs:double(\"NaN\"), 1, 2, 3))) eq \"NaN\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(sum((1, 2, xs:float("NaN"), 1, 2, 3))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc16() {
    final XQuery query = new XQuery(
      "string(sum((1, 2, xs:float(\"NaN\"), 1, 2, 3))) eq \"NaN\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `sum(xs:untypedAtomic("3")) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc17() {
    final XQuery query = new XQuery(
      "sum(xs:untypedAtomic(\"3\")) instance of xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `sum((1, 2, xs:untypedAtomic("3"))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc18() {
    final XQuery query = new XQuery(
      "sum((1, 2, xs:untypedAtomic(\"3\"))) instance of xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `sum((1, 2, xs:untypedAtomic("3"))) eq 6`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc19() {
    final XQuery query = new XQuery(
      "sum((1, 2, xs:untypedAtomic(\"3\"))) eq 6",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `sum(1, 1, "wrong param")`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc2() {
    final XQuery query = new XQuery(
      "sum(1, 1, \"wrong param\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `sum((xs:float(1), 2, xs:untypedAtomic("3"))) eq 6`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc20() {
    final XQuery query = new XQuery(
      "sum((xs:float(1), 2, xs:untypedAtomic(\"3\"))) eq 6",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `sum((xs:float(1), 2, xs:untypedAtomic("3"))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc21() {
    final XQuery query = new XQuery(
      "sum((xs:float(1), 2, xs:untypedAtomic(\"3\"))) instance of xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:sum() having an input sequence whose static type is xs:anyAtomicType. .
   */
  @org.junit.Test
  public void kSeqSUMFunc22() {
    final XQuery query = new XQuery(
      "sum(remove((1.0, xs:float(1), 2, xs:untypedAtomic(\"3\")), 1)) eq 6",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `sum("a string")`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc23() {
    final XQuery query = new XQuery(
      "sum(\"a string\")",
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
   *  A test whose essence is: `sum(xs:anyURI("a string"))`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc24() {
    final XQuery query = new XQuery(
      "sum(xs:anyURI(\"a string\"))",
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
   *  A test whose essence is: `sum((1, 2, 3, xs:anyURI("a string"), xs:double("NaN")))`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc25() {
    final XQuery query = new XQuery(
      "sum((1, 2, 3, xs:anyURI(\"a string\"), xs:double(\"NaN\")))",
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
   *  A test whose essence is: `sum((1, 2, 3, xs:anyURI("a string"), xs:double("NaN")), 3)`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc26() {
    final XQuery query = new XQuery(
      "sum((1, 2, 3, xs:anyURI(\"a string\"), xs:double(\"NaN\")), 3)",
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
   *  A test whose essence is: `sum(((),())) eq 0`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc27() {
    final XQuery query = new XQuery(
      "sum(((),())) eq 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `sum(()) eq 0`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc28() {
    final XQuery query = new XQuery(
      "sum(()) eq 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `sum((3, 4, 5)) eq 12`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc29() {
    final XQuery query = new XQuery(
      "sum((3, 4, 5)) eq 12",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `sum((), 3) eq 3`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc3() {
    final XQuery query = new XQuery(
      "sum((), 3) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `sum((xs:yearMonthDuration("P20Y"), xs:yearMonthDuration("P10M"))) eq xs:yearMonthDuration("P250M")`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc30() {
    final XQuery query = new XQuery(
      "sum((xs:yearMonthDuration(\"P20Y\"), xs:yearMonthDuration(\"P10M\"))) eq xs:yearMonthDuration(\"P250M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `sum((xs:yearMonthDuration("P20Y"), xs:yearMonthDuration("P10M")) [. < xs:yearMonthDuration("P3M")], xs:yearMonthDuration("P0M")) eq xs:yearMonthDuration("P0M")`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc31() {
    final XQuery query = new XQuery(
      "sum((xs:yearMonthDuration(\"P20Y\"), xs:yearMonthDuration(\"P10M\")) [. < xs:yearMonthDuration(\"P3M\")], xs:yearMonthDuration(\"P0M\")) eq xs:yearMonthDuration(\"P0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `sum((1 to 100)[. < 0], 0) eq 0`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc32() {
    final XQuery query = new XQuery(
      "sum((1 to 100)[. < 0], 0) eq 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `sum((xs:yearMonthDuration("P20Y"), (3, 4, 5)))`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc33() {
    final XQuery query = new XQuery(
      "sum((xs:yearMonthDuration(\"P20Y\"), (3, 4, 5)))",
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
   *  A test whose essence is: `sum((), 3) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc4() {
    final XQuery query = new XQuery(
      "sum((), 3) instance of xs:integer",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(sum((), ()))`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc5() {
    final XQuery query = new XQuery(
      "empty(sum((), ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `sum((), 0.0) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc6() {
    final XQuery query = new XQuery(
      "sum((), 0.0) instance of xs:decimal",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `sum((), 0.0) eq 0.0`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc7() {
    final XQuery query = new XQuery(
      "sum((), 0.0) eq 0.0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `sum(()) eq 0`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc8() {
    final XQuery query = new XQuery(
      "sum(()) eq 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `sum(()) instance of xs:integer`. .
   */
  @org.junit.Test
  public void kSeqSUMFunc9() {
    final XQuery query = new XQuery(
      "sum(()) instance of xs:integer",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Pass in an empty sequence as zero. .
   */
  @org.junit.Test
  public void k2SeqSUMFunc1() {
    final XQuery query = new XQuery(
      "sum((), ())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  Ensure the return type is correct when type promotion is required. .
   */
  @org.junit.Test
  public void k2SeqSUMFunc2() {
    final XQuery query = new XQuery(
      "sum((xs:float('NaN'), 2, 3, 4, xs:double('NaN'))) instance of xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure the return type is correct when type promotion is required(#2). .
   */
  @org.junit.Test
  public void k2SeqSUMFunc3() {
    final XQuery query = new XQuery(
      "sum((xs:float('NaN'), 2, 3.3, 4, xs:double('NaN'))) instance of xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure the return type is properly inferred. .
   */
  @org.junit.Test
  public void k2SeqSUMFunc4() {
    final XQuery query = new XQuery(
      "sum(xs:unsignedShort(\"1\")) instance of xs:unsignedShort",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test sum(1 to 10) .
   */
  @org.junit.Test
  public void cbclSumFunc001() {
    final XQuery query = new XQuery(
      "sum(1 to 10)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "55")
    );
  }

  /**
   *  Test sum(foreach) .
   */
  @org.junit.Test
  public void cbclSumFunc002() {
    final XQuery query = new XQuery(
      "sum(for $x in 1 to 10 return 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "50")
    );
  }

  /**
   *  Test sum($x to $y) .
   */
  @org.junit.Test
  public void cbclSumFunc003() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $x in 1 to 10, $y in 1 to 10 return concat('sum(',$x,' to ',$y,') = ',sum($x to $y))\n" +
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
      assertStringValue(false, "sum(1 to 1) = 1 sum(1 to 2) = 3 sum(1 to 3) = 6 sum(1 to 4) = 10 sum(1 to 5) = 15 sum(1 to 6) = 21 sum(1 to 7) = 28 sum(1 to 8) = 36 sum(1 to 9) = 45 sum(1 to 10) = 55 sum(2 to 1) = 0 sum(2 to 2) = 2 sum(2 to 3) = 5 sum(2 to 4) = 9 sum(2 to 5) = 14 sum(2 to 6) = 20 sum(2 to 7) = 27 sum(2 to 8) = 35 sum(2 to 9) = 44 sum(2 to 10) = 54 sum(3 to 1) = 0 sum(3 to 2) = 0 sum(3 to 3) = 3 sum(3 to 4) = 7 sum(3 to 5) = 12 sum(3 to 6) = 18 sum(3 to 7) = 25 sum(3 to 8) = 33 sum(3 to 9) = 42 sum(3 to 10) = 52 sum(4 to 1) = 0 sum(4 to 2) = 0 sum(4 to 3) = 0 sum(4 to 4) = 4 sum(4 to 5) = 9 sum(4 to 6) = 15 sum(4 to 7) = 22 sum(4 to 8) = 30 sum(4 to 9) = 39 sum(4 to 10) = 49 sum(5 to 1) = 0 sum(5 to 2) = 0 sum(5 to 3) = 0 sum(5 to 4) = 0 sum(5 to 5) = 5 sum(5 to 6) = 11 sum(5 to 7) = 18 sum(5 to 8) = 26 sum(5 to 9) = 35 sum(5 to 10) = 45 sum(6 to 1) = 0 sum(6 to 2) = 0 sum(6 to 3) = 0 sum(6 to 4) = 0 sum(6 to 5) = 0 sum(6 to 6) = 6 sum(6 to 7) = 13 sum(6 to 8) = 21 sum(6 to 9) = 30 sum(6 to 10) = 40 sum(7 to 1) = 0 sum(7 to 2) = 0 sum(7 to 3) = 0 sum(7 to 4) = 0 sum(7 to 5) = 0 sum(7 to 6) = 0 sum(7 to 7) = 7 sum(7 to 8) = 15 sum(7 to 9) = 24 sum(7 to 10) = 34 sum(8 to 1) = 0 sum(8 to 2) = 0 sum(8 to 3) = 0 sum(8 to 4) = 0 sum(8 to 5) = 0 sum(8 to 6) = 0 sum(8 to 7) = 0 sum(8 to 8) = 8 sum(8 to 9) = 17 sum(8 to 10) = 27 sum(9 to 1) = 0 sum(9 to 2) = 0 sum(9 to 3) = 0 sum(9 to 4) = 0 sum(9 to 5) = 0 sum(9 to 6) = 0 sum(9 to 7) = 0 sum(9 to 8) = 0 sum(9 to 9) = 9 sum(9 to 10) = 19 sum(10 to 1) = 0 sum(10 to 2) = 0 sum(10 to 3) = 0 sum(10 to 4) = 0 sum(10 to 5) = 0 sum(10 to 6) = 0 sum(10 to 7) = 0 sum(10 to 8) = 0 sum(10 to 9) = 0 sum(10 to 10) = 10")
    );
  }

  /**
   *  Tests boolean call to sum .
   */
  @org.junit.Test
  public void cbclSumFunc004() {
    final XQuery query = new XQuery(
      "boolean(sum((1 to 10)[. mod 2 = 0]))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Tests sum of a sum .
   */
  @org.junit.Test
  public void cbclSumFunc005() {
    final XQuery query = new XQuery(
      "sum(sum((1 to 10)[. mod 2 = 0],()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "30")
    );
  }

  /**
   *  Tests sum of two integer converts to plus .
   */
  @org.junit.Test
  public void cbclSumFunc006() {
    final XQuery query = new XQuery(
      "sum((1,exactly-one((1 to 10)[. div 2 = 2])))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5")
    );
  }

  /**
   *  Test fn:sum on a sequence of xs:dayTimeDuration arguments .
   */
  @org.junit.Test
  public void fnSum1() {
    final XQuery query = new XQuery(
      "sum((xs:dayTimeDuration(\"P1D\"), xs:dayTimeDuration(\"PT1H\"))) instance of xs:dayTimeDuration",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test fn:sum on a sequence of xs:duration arguments .
   */
  @org.junit.Test
  public void fnSum10() {
    final XQuery query = new XQuery(
      "sum(xs:duration(\"P1Y1M1D\"), xs:duration(\"PT0S\"))",
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
        error("FORG0006")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Test fn:sum on a sequence of xs:dayTimeDuration arguments .
   */
  @org.junit.Test
  public void fnSum2() {
    final XQuery query = new XQuery(
      "sum((), xs:dayTimeDuration(\"PT0S\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   *  Test fn:sum on a sequence of xs:dayTimeDuration arguments .
   */
  @org.junit.Test
  public void fnSum3() {
    final XQuery query = new XQuery(
      "sum(for $x in 1 to 10 return xs:dayTimeDuration(concat(\"PT\",$x,\"H\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P2DT7H")
    );
  }

  /**
   *  Test fn:sum on a sequence of xs:yearMonthDuration arguments .
   */
  @org.junit.Test
  public void fnSum4() {
    final XQuery query = new XQuery(
      "sum((xs:yearMonthDuration(\"P1Y\"), xs:yearMonthDuration(\"P1M\"))) instance of xs:yearMonthDuration",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test fn:sum on a sequence of xs:yearMonthDuration arguments .
   */
  @org.junit.Test
  public void fnSum5() {
    final XQuery query = new XQuery(
      "sum((), xs:yearMonthDuration(\"P0M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P0M")
    );
  }

  /**
   *  Test fn:sum on a sequence of xs:yearMonthDuration arguments .
   */
  @org.junit.Test
  public void fnSum6() {
    final XQuery query = new XQuery(
      "sum(for $x in 1 to 10 return xs:yearMonthDuration(concat(\"P\",$x,\"M\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P4Y7M")
    );
  }

  /**
   *  Test fn:sum on a sequence of xs:yearMonthDuration and xs:dayTimeDuration arguments .
   */
  @org.junit.Test
  public void fnSum7() {
    final XQuery query = new XQuery(
      "sum((xs:yearMonthDuration(\"P1Y\"), xs:dayTimeDuration(\"P1D\")))",
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
        error("FORG0006")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Test fn:sum with $zero as xs:duration .
   */
  @org.junit.Test
  public void fnSum8() {
    final XQuery query = new XQuery(
      "sum((), xs:duration(\"P0M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT0S")
    );
  }

  /**
   *  Test fn:sum on a sequence of xs:duration arguments .
   */
  @org.junit.Test
  public void fnSum9() {
    final XQuery query = new XQuery(
      "sum(xs:duration(\"P1Y1M1D\"))",
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
        error("FORG0006")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnSumdbl1args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:double(\"-1.7976931348623157E308\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnSumdbl1args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:double(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnSumdbl1args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:double(\"1.7976931348623157E308\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:double(mid range) $zero = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnSumdbl2args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:double(\"0\"),xs:double(\"-1.7976931348623157E308\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:double(upper bound) $zero = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnSumdbl2args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:double(\"1.7976931348623157E308\"),xs:double(\"-1.7976931348623157E308\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:double(lower bound) $zero = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnSumdbl2args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:double(\"-1.7976931348623157E308\"),xs:double(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:double(lower bound) $zero = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnSumdbl2args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:double(\"-1.7976931348623157E308\"),xs:double(\"1.7976931348623157E308\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:double(mid range) $arg2 = xs:double(lower bound) $zero = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnSumdbl3args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:double(\"0\"),xs:double(\"-1.7976931348623157E308\"),xs:double(\"-1.7976931348623157E308\")))",
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
        assertStringValue(false, "-INF")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:double(upper bound) $arg2 = xs:double(lower bound) $zero = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnSumdbl3args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:double(\"1.7976931348623157E308\"),xs:double(\"-1.7976931348623157E308\"),xs:double(\"-1.7976931348623157E308\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(mid range) $zero = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnSumdbl3args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:double(\"-1.7976931348623157E308\"),xs:double(\"0\"),xs:double(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(upper bound) $zero = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnSumdbl3args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:double(\"-1.7976931348623157E308\"),xs:double(\"1.7976931348623157E308\"),xs:double(\"-1.7976931348623157E308\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(lower bound) $zero = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnSumdbl3args5() {
    final XQuery query = new XQuery(
      "fn:sum((xs:double(\"0\"),xs:double(\"0\"),xs:double(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(lower bound) $zero = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnSumdbl3args6() {
    final XQuery query = new XQuery(
      "fn:sum((xs:double(\"-1.7976931348623157E308\"),xs:double(\"-1.7976931348623157E308\"),xs:double(\"1.7976931348623157E308\")))",
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
        assertStringValue(false, "-INF")
      ||
        assertEq("-1.7976931348623157E308")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnSumdec1args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:decimal(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnSumdec1args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:decimal(\"617375191608514839\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("617375191608514839")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnSumdec1args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:decimal(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:decimal(mid range) $zero = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnSumdec2args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:decimal(\"617375191608514839\"),xs:decimal(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-382624808391485160")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:decimal(upper bound) $zero = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnSumdec2args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:decimal(\"999999999999999999\"),xs:decimal(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:decimal(lower bound) $zero = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnSumdec2args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:decimal(\"-999999999999999999\"),xs:decimal(\"617375191608514839\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-382624808391485160")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:decimal(lower bound) $zero = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnSumdec2args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:decimal(\"-999999999999999999\"),xs:decimal(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:decimal(upper bound) $arg2 = xs:decimal(lower bound) $zero = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnSumdec3args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:decimal(\"999999999999999999\"),xs:decimal(\"-999999999999999999\"),xs:decimal(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(upper bound) $zero = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnSumdec3args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:decimal(\"-999999999999999999\"),xs:decimal(\"999999999999999999\"),xs:decimal(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(lower bound) $zero = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnSumdec3args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:decimal(\"0\"),xs:decimal(\"0\"),xs:decimal(\"617375191608514839\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("617375191608514839")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(lower bound) $zero = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnSumdec3args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:decimal(\"-999999999999999999\"),xs:decimal(\"-999999999999999999\"),xs:decimal(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnSumflt1args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:float(\"-3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(\"-3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnSumflt1args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:float(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnSumflt1args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:float(\"3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(\"3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:float(mid range) $zero = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnSumflt2args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:float(\"0\"),xs:float(\"-3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(\"-3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:float(upper bound) $zero = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnSumflt2args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:float(\"3.4028235E38\"),xs:float(\"-3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:float(lower bound) $zero = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnSumflt2args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:float(\"-3.4028235E38\"),xs:float(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(\"-3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:float(lower bound) $zero = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnSumflt2args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:float(\"-3.4028235E38\"),xs:float(\"3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:float(mid range) $arg2 = xs:float(lower bound) $zero = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnSumflt3args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:float(\"0\"),xs:float(\"-3.4028235E38\"),xs:float(\"-3.4028235E38\")))",
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
        assertStringValue(false, "-INF")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:float(upper bound) $arg2 = xs:float(lower bound) $zero = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnSumflt3args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:float(\"3.4028235E38\"),xs:float(\"-3.4028235E38\"),xs:float(\"-3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(\"-3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(mid range) $zero = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnSumflt3args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:float(\"-3.4028235E38\"),xs:float(\"0\"),xs:float(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(\"-3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(upper bound) $zero = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnSumflt3args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:float(\"-3.4028235E38\"),xs:float(\"3.4028235E38\"),xs:float(\"-3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(\"-3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(lower bound) $zero = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnSumflt3args5() {
    final XQuery query = new XQuery(
      "fn:sum((xs:float(\"0\"),xs:float(\"0\"),xs:float(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(lower bound) $zero = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnSumflt3args6() {
    final XQuery query = new XQuery(
      "fn:sum((xs:float(\"-3.4028235E38\"),xs:float(\"-3.4028235E38\"),xs:float(\"3.4028235E38\")))",
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
        assertStringValue(false, "-INF")
      ||
        assertEq("xs:float(\"-3.4028235E38\")")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnSumint1args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:int(\"-2147483648\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-2147483648")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnSumint1args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:int(\"-1873914410\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1873914410")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnSumint1args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:int(\"2147483647\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2147483647")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:int(mid range) $zero = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnSumint2args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:int(\"-1873914410\"),xs:int(\"-273569238\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-2147483648")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:int(upper bound) $zero = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnSumint2args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:int(\"2147483647\"),xs:int(\"-2147483648\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:int(lower bound) $zero = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnSumint2args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:int(\"-273569238\"),xs:int(\"-1873914410\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-2147483648")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:int(lower bound) $zero = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnSumint2args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:int(\"-2147483648\"),xs:int(\"2147483647\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:int(mid range) $arg2 = xs:int(lower bound) $zero = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnSumint3args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:int(\"-1873914410\"),xs:int(\"-273569238\"),xs:int(\"-273569238\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-2421052886")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(mid range) $zero = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnSumint3args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:int(\"-273569238\"),xs:int(\"-1873914410\"),xs:int(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-2147483648")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(lower bound) $zero = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnSumint3args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:int(\"0\"),xs:int(\"0\"),xs:int(\"-1873914410\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1873914410")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(lower bound) $zero = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnSumint3args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:int(\"0\"),xs:int(\"0\"),xs:int(\"2147483647\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2147483647")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnSumintg1args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:integer(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnSumintg1args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:integer(\"830993497117024304\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("830993497117024304")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnSumintg1args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:integer(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:integer(mid range) $zero = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnSumintg2args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:integer(\"830993497117024304\"),xs:integer(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-169006502882975695")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:integer(upper bound) $zero = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnSumintg2args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:integer(\"999999999999999999\"),xs:integer(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:integer(lower bound) $zero = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnSumintg2args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:integer(\"-999999999999999999\"),xs:integer(\"830993497117024304\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-169006502882975695")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:integer(lower bound) $zero = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnSumintg2args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:integer(\"-999999999999999999\"),xs:integer(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:integer(upper bound) $arg2 = xs:integer(lower bound) $zero = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnSumintg3args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:integer(\"999999999999999999\"),xs:integer(\"-999999999999999999\"),xs:integer(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(upper bound) $zero = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnSumintg3args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:integer(\"-999999999999999999\"),xs:integer(\"999999999999999999\"),xs:integer(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(lower bound) $zero = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnSumintg3args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:integer(\"0\"),xs:integer(\"0\"),xs:integer(\"830993497117024304\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("830993497117024304")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(lower bound) $zero = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnSumintg3args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:integer(\"-999999999999999999\"),xs:integer(\"-999999999999999999\"),xs:integer(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnSumlng1args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:long(\"-92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnSumlng1args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:long(\"-47175562203048468\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-47175562203048468")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnSumlng1args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:long(\"92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("92233720368547758")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:long(mid range) $zero = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnSumlng2args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:long(\"-47175562203048468\"),xs:long(\"-45058158165499290\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:long(upper bound) $zero = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnSumlng2args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:long(\"92233720368547758\"),xs:long(\"-92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:long(lower bound) $zero = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnSumlng2args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:long(\"-45058158165499290\"),xs:long(\"-47175562203048468\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:long(lower bound) $zero = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnSumlng2args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:long(\"-92233720368547758\"),xs:long(\"92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:long(mid range) $arg2 = xs:long(lower bound) $zero = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnSumlng3args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:long(\"-47175562203048468\"),xs:long(\"-45058158165499290\"),xs:long(\"-45058158165499290\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-137291878534047048")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:long(upper bound) $arg2 = xs:long(lower bound) $zero = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnSumlng3args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:long(\"92233720368547758\"),xs:long(\"-92233720368547758\"),xs:long(\"-92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(mid range) $zero = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnSumlng3args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:long(\"-45058158165499290\"),xs:long(\"-47175562203048468\"),xs:long(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(upper bound) $zero = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnSumlng3args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:long(\"-92233720368547758\"),xs:long(\"92233720368547758\"),xs:long(\"-92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(lower bound) $zero = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnSumlng3args5() {
    final XQuery query = new XQuery(
      "fn:sum((xs:long(\"0\"),xs:long(\"0\"),xs:long(\"-47175562203048468\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-47175562203048468")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(lower bound) $zero = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnSumlng3args6() {
    final XQuery query = new XQuery(
      "fn:sum((xs:long(\"-92233720368547758\"),xs:long(\"-92233720368547758\"),xs:long(\"92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnint1args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:negativeInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnSumnint1args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:negativeInteger(\"-297014075999096793\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-297014075999096793")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnSumnint1args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:negativeInteger(\"-1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) $zero = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnint2args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:negativeInteger(\"-297014075999096793\"),xs:negativeInteger(\"-702985924000903206\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) $zero = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnint2args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:negativeInteger(\"-1\"),xs:negativeInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1000000000000000000")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) $zero = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnSumnint2args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:negativeInteger(\"-702985924000903206\"),xs:negativeInteger(\"-297014075999096793\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) $zero = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnSumnint2args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:negativeInteger(\"-999999999999999999\"),xs:negativeInteger(\"-1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1000000000000000000")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:negativeInteger(mid range) $arg2 = xs:negativeInteger(lower bound) $zero = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnint3args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:negativeInteger(\"-297014075999096793\"),xs:negativeInteger(\"-702985924000903206\"),xs:negativeInteger(\"-702985924000903206\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1702985924000903205")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:negativeInteger(upper bound) $arg2 = xs:negativeInteger(lower bound) $zero = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnint3args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:negativeInteger(\"-1\"),xs:negativeInteger(\"-999999999999999999\"),xs:negativeInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnni1args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonNegativeInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnSumnni1args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonNegativeInteger(\"303884545991464527\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("303884545991464527")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnSumnni1args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonNegativeInteger(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) $zero = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnni2args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) $zero = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnni2args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonNegativeInteger(\"303884545991464527\"),xs:nonNegativeInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("303884545991464527")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) $zero = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnni2args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonNegativeInteger(\"999999999999999999\"),xs:nonNegativeInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) $zero = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnSumnni2args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"303884545991464527\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("303884545991464527")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) $zero = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnSumnni2args5() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(lower bound) $zero = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnni3args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(mid range) $arg2 = xs:nonNegativeInteger(lower bound) $zero = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnni3args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonNegativeInteger(\"303884545991464527\"),xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("303884545991464527")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(upper bound) $arg2 = xs:nonNegativeInteger(lower bound) $zero = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnni3args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonNegativeInteger(\"999999999999999999\"),xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(mid range) $zero = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnni3args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"303884545991464527\"),xs:nonNegativeInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("303884545991464527")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(upper bound) $zero = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnni3args5() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"999999999999999999\"),xs:nonNegativeInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(lower bound) $zero = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnSumnni3args6() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"303884545991464527\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("303884545991464527")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(lower bound) $zero = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnSumnni3args7() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnpi1args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnSumnpi1args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonPositiveInteger(\"-475688437271870490\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-475688437271870490")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnSumnpi1args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonPositiveInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) $zero = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnpi2args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonPositiveInteger(\"-475688437271870490\"),xs:nonPositiveInteger(\"-524311562728129509\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) $zero = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnpi2args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonPositiveInteger(\"0\"),xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) $zero = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnSumnpi2args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonPositiveInteger(\"-524311562728129509\"),xs:nonPositiveInteger(\"-475688437271870490\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) $zero = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnSumnpi2args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonPositiveInteger(\"-999999999999999999\"),xs:nonPositiveInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(mid range) $arg2 = xs:nonPositiveInteger(lower bound) $zero = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnpi3args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonPositiveInteger(\"-475688437271870490\"),xs:nonPositiveInteger(\"-524311562728129509\"),xs:nonPositiveInteger(\"-524311562728129509\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1524311562728129508")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(upper bound) $arg2 = xs:nonPositiveInteger(lower bound) $zero = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnpi3args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonPositiveInteger(\"0\"),xs:nonPositiveInteger(\"-999999999999999999\"),xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1999999999999999998")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(mid range) $zero = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnpi3args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonPositiveInteger(\"-524311562728129509\"),xs:nonPositiveInteger(\"-475688437271870490\"),xs:nonPositiveInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(upper bound) $zero = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumnpi3args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonPositiveInteger(\"-999999999999999999\"),xs:nonPositiveInteger(\"0\"),xs:nonPositiveInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(lower bound) $zero = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnSumnpi3args5() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonPositiveInteger(\"0\"),xs:nonPositiveInteger(\"0\"),xs:nonPositiveInteger(\"-475688437271870490\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-475688437271870490")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(lower bound) $zero = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnSumnpi3args6() {
    final XQuery query = new XQuery(
      "fn:sum((xs:nonPositiveInteger(\"0\"),xs:nonPositiveInteger(\"0\"),xs:nonPositiveInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumpint1args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:positiveInteger(\"1\")))",
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
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnSumpint1args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:positiveInteger(\"52704602390610033\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("52704602390610033")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnSumpint1args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:positiveInteger(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) $zero = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumpint2args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:positiveInteger(\"1\"),xs:positiveInteger(\"1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) $zero = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumpint2args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:positiveInteger(\"52704602390610033\"),xs:positiveInteger(\"1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("52704602390610034")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) $zero = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumpint2args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:positiveInteger(\"999999999999999998\"),xs:positiveInteger(\"1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) $zero = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnSumpint2args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:positiveInteger(\"1\"),xs:positiveInteger(\"52704602390610033\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("52704602390610034")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) $zero = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnSumpint2args5() {
    final XQuery query = new XQuery(
      "fn:sum((xs:positiveInteger(\"1\"),xs:positiveInteger(\"999999999999999998\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(lower bound) $zero = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumpint3args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:positiveInteger(\"1\"),xs:positiveInteger(\"1\"),xs:positiveInteger(\"1\")))",
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
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:positiveInteger(mid range) $arg2 = xs:positiveInteger(lower bound) $zero = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumpint3args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:positiveInteger(\"52704602390610033\"),xs:positiveInteger(\"1\"),xs:positiveInteger(\"1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("52704602390610035")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:positiveInteger(upper bound) $arg2 = xs:positiveInteger(lower bound) $zero = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumpint3args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:positiveInteger(\"999999999999999998\"),xs:positiveInteger(\"1\"),xs:positiveInteger(\"1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000000000000000000")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(mid range) $zero = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumpint3args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:positiveInteger(\"1\"),xs:positiveInteger(\"52704602390610033\"),xs:positiveInteger(\"1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("52704602390610035")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(upper bound) $zero = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnSumpint3args5() {
    final XQuery query = new XQuery(
      "fn:sum((xs:positiveInteger(\"1\"),xs:positiveInteger(\"999999999999999998\"),xs:positiveInteger(\"1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000000000000000000")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(lower bound) $zero = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnSumpint3args6() {
    final XQuery query = new XQuery(
      "fn:sum((xs:positiveInteger(\"1\"),xs:positiveInteger(\"1\"),xs:positiveInteger(\"52704602390610033\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("52704602390610035")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(lower bound) $zero = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnSumpint3args7() {
    final XQuery query = new XQuery(
      "fn:sum((xs:positiveInteger(\"1\"),xs:positiveInteger(\"1\"),xs:positiveInteger(\"999999999999999998\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000000000000000000")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnSumsht1args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:short(\"-32768\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-32768")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnSumsht1args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:short(\"-5324\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-5324")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnSumsht1args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:short(\"32767\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("32767")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:short(mid range) $zero = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnSumsht2args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:short(\"-5324\"),xs:short(\"-27444\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-32768")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:short(upper bound) $zero = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnSumsht2args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:short(\"32767\"),xs:short(\"-32768\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:short(lower bound) $zero = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnSumsht2args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:short(\"-27444\"),xs:short(\"-5324\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-32768")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:short(lower bound) $zero = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnSumsht2args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:short(\"-32768\"),xs:short(\"32767\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:short(mid range) $arg2 = xs:short(lower bound) $zero = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnSumsht3args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:short(\"-5324\"),xs:short(\"-27444\"),xs:short(\"-27444\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-60212")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(mid range) $zero = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnSumsht3args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:short(\"-27444\"),xs:short(\"-5324\"),xs:short(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-32768")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(lower bound) $zero = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnSumsht3args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:short(\"0\"),xs:short(\"0\"),xs:short(\"-5324\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-5324")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(lower bound) $zero = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnSumsht3args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:short(\"0\"),xs:short(\"0\"),xs:short(\"32767\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("32767")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnSumulng1args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedLong(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnSumulng1args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedLong(\"130747108607674654\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("130747108607674654")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnSumulng1args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedLong(\"184467440737095516\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("184467440737095516")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) $zero = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnSumulng2args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedLong(\"0\"),xs:unsignedLong(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) $zero = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnSumulng2args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedLong(\"130747108607674654\"),xs:unsignedLong(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("130747108607674654")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) $zero = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnSumulng2args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedLong(\"184467440737095516\"),xs:unsignedLong(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("184467440737095516")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) $zero = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnSumulng2args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedLong(\"0\"),xs:unsignedLong(\"130747108607674654\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("130747108607674654")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) $zero = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnSumulng2args5() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedLong(\"0\"),xs:unsignedLong(\"184467440737095516\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("184467440737095516")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(lower bound) $zero = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnSumulng3args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedLong(\"0\"),xs:unsignedLong(\"0\"),xs:unsignedLong(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:unsignedLong(mid range) $arg2 = xs:unsignedLong(lower bound) $zero = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnSumulng3args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedLong(\"130747108607674654\"),xs:unsignedLong(\"0\"),xs:unsignedLong(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("130747108607674654")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:unsignedLong(upper bound) $arg2 = xs:unsignedLong(lower bound) $zero = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnSumulng3args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedLong(\"184467440737095516\"),xs:unsignedLong(\"0\"),xs:unsignedLong(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("184467440737095516")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(mid range) $zero = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnSumulng3args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedLong(\"0\"),xs:unsignedLong(\"130747108607674654\"),xs:unsignedLong(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("130747108607674654")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(upper bound) $zero = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnSumulng3args5() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedLong(\"0\"),xs:unsignedLong(\"184467440737095516\"),xs:unsignedLong(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("184467440737095516")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(lower bound) $zero = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnSumulng3args6() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedLong(\"0\"),xs:unsignedLong(\"0\"),xs:unsignedLong(\"130747108607674654\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("130747108607674654")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(lower bound) $zero = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnSumulng3args7() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedLong(\"0\"),xs:unsignedLong(\"0\"),xs:unsignedLong(\"184467440737095516\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("184467440737095516")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnSumusht1args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedShort(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnSumusht1args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedShort(\"44633\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("44633")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnSumusht1args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedShort(\"65535\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("65535")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) $zero = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnSumusht2args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedShort(\"0\"),xs:unsignedShort(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) $zero = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnSumusht2args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedShort(\"44633\"),xs:unsignedShort(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("44633")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) $zero = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnSumusht2args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedShort(\"65535\"),xs:unsignedShort(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("65535")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) $zero = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnSumusht2args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedShort(\"0\"),xs:unsignedShort(\"44633\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("44633")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) $zero = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnSumusht2args5() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedShort(\"0\"),xs:unsignedShort(\"65535\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("65535")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(lower bound) $zero = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnSumusht3args1() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedShort(\"0\"),xs:unsignedShort(\"0\"),xs:unsignedShort(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:unsignedShort(mid range) $arg2 = xs:unsignedShort(lower bound) $zero = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnSumusht3args2() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedShort(\"44633\"),xs:unsignedShort(\"0\"),xs:unsignedShort(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("44633")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:unsignedShort(upper bound) $arg2 = xs:unsignedShort(lower bound) $zero = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnSumusht3args3() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedShort(\"65535\"),xs:unsignedShort(\"0\"),xs:unsignedShort(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("65535")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(mid range) $zero = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnSumusht3args4() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedShort(\"0\"),xs:unsignedShort(\"44633\"),xs:unsignedShort(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("44633")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(upper bound) $zero = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnSumusht3args5() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedShort(\"0\"),xs:unsignedShort(\"65535\"),xs:unsignedShort(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("65535")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(lower bound) $zero = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnSumusht3args6() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedShort(\"0\"),xs:unsignedShort(\"0\"),xs:unsignedShort(\"44633\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("44633")
    );
  }

  /**
   *  Evaluates The "sum" function with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(lower bound) $zero = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnSumusht3args7() {
    final XQuery query = new XQuery(
      "fn:sum((xs:unsignedShort(\"0\"),xs:unsignedShort(\"0\"),xs:unsignedShort(\"65535\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("65535")
    );
  }
}
