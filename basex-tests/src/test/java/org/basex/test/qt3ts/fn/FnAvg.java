package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the avg() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnAvg extends QT3TestSet {

  /**
   *  A test whose essence is: `avg()`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc1() {
    final XQuery query = new XQuery(
      "avg()",
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
   *  A test whose essence is: `avg((xs:float(1), xs:integer(0), xs:float(5))) eq 2.0`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc10() {
    final XQuery query = new XQuery(
      "avg((xs:float(1), xs:integer(0), xs:float(5))) eq 2.0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `avg((xs:float(1), xs:integer(0), xs:untypedAtomic(-4))) eq -1`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc11() {
    final XQuery query = new XQuery(
      "avg((xs:float(1), xs:integer(0), xs:untypedAtomic(-4))) eq -1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `avg((xs:float(1), xs:integer(0), xs:untypedAtomic(3))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc12() {
    final XQuery query = new XQuery(
      "avg((xs:float(1), xs:integer(0), xs:untypedAtomic(3))) instance of xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `avg((xs:untypedAtomic(3), xs:integer(0), xs:decimal(1))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc13() {
    final XQuery query = new XQuery(
      "avg((xs:untypedAtomic(3), xs:integer(0), xs:decimal(1))) instance of xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(avg((3, 3, xs:double("NaN")))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc14() {
    final XQuery query = new XQuery(
      "string(avg((3, 3, xs:double(\"NaN\")))) eq \"NaN\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(avg((3, xs:double("NaN"), 3))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc15() {
    final XQuery query = new XQuery(
      "string(avg((3, xs:double(\"NaN\"), 3))) eq \"NaN\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(avg((xs:double("NaN"), 3, 3))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc16() {
    final XQuery query = new XQuery(
      "string(avg((xs:double(\"NaN\"), 3, 3))) eq \"NaN\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(avg(()))`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc17() {
    final XQuery query = new XQuery(
      "empty(avg(()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(avg(((), ())))`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc18() {
    final XQuery query = new XQuery(
      "empty(avg(((), ())))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `avg((-5, -0, -3, -6)) eq -3.5`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc19() {
    final XQuery query = new XQuery(
      "avg((-5, -0, -3, -6)) eq -3.5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `avg(1, "wrong param")`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc2() {
    final XQuery query = new XQuery(
      "avg(1, \"wrong param\")",
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
   *  A test whose essence is: `string(avg((1, 2, 3, xs:float("NaN")))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc20() {
    final XQuery query = new XQuery(
      "string(avg((1, 2, 3, xs:float(\"NaN\")))) eq \"NaN\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(avg((1, 2, 3, xs:double("NaN")))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc21() {
    final XQuery query = new XQuery(
      "string(avg((1, 2, 3, xs:double(\"NaN\")))) eq \"NaN\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(avg((xs:double("NaN"), 1, 2, 3))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc22() {
    final XQuery query = new XQuery(
      "string(avg((xs:double(\"NaN\"), 1, 2, 3))) eq \"NaN\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(avg((xs:float("NaN"), 1, 2, 3))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc23() {
    final XQuery query = new XQuery(
      "string(avg((xs:float(\"NaN\"), 1, 2, 3))) eq \"NaN\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(avg((1, 2, xs:double("NaN"), 1, 2, 3))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc24() {
    final XQuery query = new XQuery(
      "string(avg((1, 2, xs:double(\"NaN\"), 1, 2, 3))) eq \"NaN\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(avg((1, 2, xs:float("NaN"), 1, 2, 3))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc25() {
    final XQuery query = new XQuery(
      "string(avg((1, 2, xs:float(\"NaN\"), 1, 2, 3))) eq \"NaN\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `avg(xs:untypedAtomic("3")) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc26() {
    final XQuery query = new XQuery(
      "avg(xs:untypedAtomic(\"3\")) instance of xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `avg((1, 2, xs:untypedAtomic("3"))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc27() {
    final XQuery query = new XQuery(
      "avg((1, 2, xs:untypedAtomic(\"3\"))) instance of xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `avg((1, 2, xs:untypedAtomic("3"))) eq 2`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc28() {
    final XQuery query = new XQuery(
      "avg((1, 2, xs:untypedAtomic(\"3\"))) eq 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `avg((xs:float(1), 2, xs:untypedAtomic("3"))) eq 2`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc29() {
    final XQuery query = new XQuery(
      "avg((xs:float(1), 2, xs:untypedAtomic(\"3\"))) eq 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(avg(()))`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc3() {
    final XQuery query = new XQuery(
      "empty(avg(()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `avg((xs:float(1), 2, xs:untypedAtomic("3"))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc30() {
    final XQuery query = new XQuery(
      "avg((xs:float(1), 2, xs:untypedAtomic(\"3\"))) instance of xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `avg("a string")`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc31() {
    final XQuery query = new XQuery(
      "avg(\"a string\")",
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
   *  A test whose essence is: `avg(xs:anyURI("a string"))`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc32() {
    final XQuery query = new XQuery(
      "avg(xs:anyURI(\"a string\"))",
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
   *  A test whose essence is: `avg((1, 2, 3, xs:anyURI("a string"), xs:double("NaN")))`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc33() {
    final XQuery query = new XQuery(
      "avg((1, 2, 3, xs:anyURI(\"a string\"), xs:double(\"NaN\")))",
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
   *  A test whose essence is: `avg("a string")`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc34() {
    final XQuery query = new XQuery(
      "avg(\"a string\")",
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
   *  A test whose essence is: `avg((xs:untypedAtomic(3), xs:integer(3), xs:string(1)))`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc35() {
    final XQuery query = new XQuery(
      "avg((xs:untypedAtomic(3), xs:integer(3), xs:string(1)))",
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
   *  A test whose essence is: `avg((xs:string(1), xs:integer(3), xs:untypedAtomic(3)))`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc36() {
    final XQuery query = new XQuery(
      "avg((xs:string(1), xs:integer(3), xs:untypedAtomic(3)))",
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
   *  A test whose essence is: `avg((xs:float(2), xs:integer(3), "a string", xs:double(2)))`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc37() {
    final XQuery query = new XQuery(
      "avg((xs:float(2), xs:integer(3), \"a string\", xs:double(2)))",
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
   *  A test whose essence is: `avg((xs:yearMonthDuration("P20Y"), (3, 4, 5)))`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc38() {
    final XQuery query = new XQuery(
      "avg((xs:yearMonthDuration(\"P20Y\"), (3, 4, 5)))",
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
   *  A test whose essence is: `avg((xs:yearMonthDuration("P20Y"), xs:yearMonthDuration("P10M"))) eq xs:yearMonthDuration("P125M")`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc39() {
    final XQuery query = new XQuery(
      "avg((xs:yearMonthDuration(\"P20Y\"), xs:yearMonthDuration(\"P10M\"))) eq xs:yearMonthDuration(\"P125M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `avg((3, 3, 3)) eq 3`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc4() {
    final XQuery query = new XQuery(
      "avg((3, 3, 3)) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(avg( () ))`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc40() {
    final XQuery query = new XQuery(
      "empty(avg( () ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(avg((xs:float('INF'), xs:float('-INF')))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc41() {
    final XQuery query = new XQuery(
      "string(avg((xs:float('INF'), xs:float('-INF')))) eq \"NaN\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `string(avg(((3, 4, 5), xs:float('NaN')))) eq "NaN"`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc42() {
    final XQuery query = new XQuery(
      "string(avg(((3, 4, 5), xs:float('NaN')))) eq \"NaN\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `avg((3, 4, 5)) eq 4.0`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc43() {
    final XQuery query = new XQuery(
      "avg((3, 4, 5)) eq 4.0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `avg((-3, -3, -3)) eq -3`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc5() {
    final XQuery query = new XQuery(
      "avg((-3, -3, -3)) eq -3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `avg((xs:float(1), xs:integer(3), xs:float(3))) instance of xs:float`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc6() {
    final XQuery query = new XQuery(
      "avg((xs:float(1), xs:integer(3), xs:float(3))) instance of xs:float",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `avg((xs:float(1), xs:integer(3), xs:decimal(3))) instance of xs:float`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc7() {
    final XQuery query = new XQuery(
      "avg((xs:float(1), xs:integer(3), xs:decimal(3))) instance of xs:float",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `avg((xs:float(1), xs:integer(3), xs:double(3))) instance of xs:double`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc8() {
    final XQuery query = new XQuery(
      "avg((xs:float(1), xs:integer(3), xs:double(3))) instance of xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `avg((xs:integer(1), xs:integer(3), xs:decimal(3))) instance of xs:decimal`. .
   */
  @org.junit.Test
  public void kSeqAVGFunc9() {
    final XQuery query = new XQuery(
      "avg((xs:integer(1), xs:integer(3), xs:decimal(3))) instance of xs:decimal",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Ensure the return type is correct when type promotion is required. .
   */
  @org.junit.Test
  public void k2SeqAVGFunc1() {
    final XQuery query = new XQuery(
      "avg((xs:float('NaN'), 2, 3, 4, xs:double('NaN'))) instance of xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
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
  public void k2SeqAVGFunc2() {
    final XQuery query = new XQuery(
      "avg((xs:float('NaN'), 2, 3.3, 4, xs:double('NaN'))) instance of xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Check the average of two integers. .
   */
  @org.junit.Test
  public void k2SeqAVGFunc3() {
    final XQuery query = new XQuery(
      "avg((1, 1))",
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
   *  Testing fn:avg overflows correctly with xs:decimals .
   */
  @org.junit.Test
  public void cbclAvg001() {
    final XQuery query = new XQuery(
      "fn:avg((xs:decimal(\"79000000000000000000000000000\"),\n" +
      "                    xs:decimal(\"79000000000000000000000000000\")))\n" +
      "            eq 79000000000000000000000000000\n" +
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
      (
        assertBoolean(true)
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  Test fn:avg overflows correctly with xs:dayTimeDurations.
   */
  @org.junit.Test
  public void cbclAvg002() {
    final XQuery query = new XQuery(
      "fn:avg((xs:dayTimeDuration(\"P9223372036854775807D\"), xs:dayTimeDuration(\"P1D\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0002")
    );
  }

  /**
   * Test fn:avg overflows correctly with xs:yearMonthDurations .
   */
  @org.junit.Test
  public void cbclAvg003() {
    final XQuery query = new XQuery(
      "fn:avg((xs:yearMonthDuration(\"P768614336404564650Y\"), xs:yearMonthDuration(\"P1Y\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0002")
    );
  }

  /**
   * Test fn:avg on xs:dayTimeDurations .
   */
  @org.junit.Test
  public void cbclAvg004() {
    final XQuery query = new XQuery(
      "fn:avg((xs:dayTimeDuration(\"P1DT2H\"), xs:dayTimeDuration(\"PT22H\"), xs:dayTimeDuration(\"P1D\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1D")
    );
  }

  /**
   * Test fn:avg raises error with xs:dayTimeDurations followed by xs:yearMonthDuration .
   */
  @org.junit.Test
  public void cbclAvg005() {
    final XQuery query = new XQuery(
      "fn:avg((xs:dayTimeDuration(\"P1DT2H\"), xs:dayTimeDuration(\"PT22H\"), xs:yearMonthDuration(\"P1M\")))",
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
   *  Test fn:avg on xs:yearMonthDurations .
   */
  @org.junit.Test
  public void cbclAvg006() {
    final XQuery query = new XQuery(
      "fn:avg((xs:yearMonthDuration(\"P1Y1M\"), xs:yearMonthDuration(\"P11M\"), xs:yearMonthDuration(\"P1Y\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1Y")
    );
  }

  /**
   * Test fn:avg raises error with xs:yearMonthDurations followed by xs:dayTimeDuration.
   */
  @org.junit.Test
  public void cbclAvg007() {
    final XQuery query = new XQuery(
      "fn:avg((xs:yearMonthDuration(\"P1Y1M\"), xs:yearMonthDuration(\"P11M\"), xs:dayTimeDuration(\"P1D\")))",
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
   * Test fn:avg on mixed numeric types. .
   */
  @org.junit.Test
  public void cbclAvg008() {
    final XQuery query = new XQuery(
      "typeswitch (fn:avg((xs:float(1), xs:double(2), xs:float(3)))) case $x as xs:double return $x default return \"FAIL\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   * Test boolean(avg) and avg(())..
   */
  @org.junit.Test
  public void cbclAvg009() {
    final XQuery query = new XQuery(
      "boolean(avg(()))",
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
        error("XPST0005")
      )
    );
  }

  /**
   * Test tpe checking is performed when optimizin fn:avg to NaN. .
   */
  @org.junit.Test
  public void cbclAvg010() {
    final XQuery query = new XQuery(
      "avg((xs:double(\"NaN\"), current-date() - xs:date(\"1997-01-01\") ))",
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
   * Test fn:avg returning double NaN..
   */
  @org.junit.Test
  public void cbclAvg011() {
    final XQuery query = new XQuery(
      "avg((xs:double(\"NaN\"), day-from-date(current-date())))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * Test fn:avg returning float NaN. .
   */
  @org.junit.Test
  public void cbclAvg012() {
    final XQuery query = new XQuery(
      "avg((xs:float(\"NaN\"), day-from-date(current-date())))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  Test fn:avg on (). .
   */
  @org.junit.Test
  public void cbclAvg013() {
    final XQuery query = new XQuery(
      "empty(avg(()))",
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
   *  Evaluation of the fn:avg function with argument to sequence of strings. .
   */
  @org.junit.Test
  public void fnAvg1() {
    final XQuery query = new XQuery(
      "fn:avg((\"a\",\"b\",\"c\"))",
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
   *  Evaluation of the fn:avg function with argument to empty sequence. .
   */
  @org.junit.Test
  public void fnAvg2() {
    final XQuery query = new XQuery(
      "fn:avg(())",
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
   *  Test fn:sum on a sequence of xs:dayTimeDuration arguments .
   */
  @org.junit.Test
  public void fnAvg3() {
    final XQuery query = new XQuery(
      "avg((xs:dayTimeDuration(\"P1D\"), xs:dayTimeDuration(\"PT2H\"))) instance of xs:dayTimeDuration",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test fn:avg on a sequence of xs:dayTimeDuration arguments .
   */
  @org.junit.Test
  public void fnAvg4() {
    final XQuery query = new XQuery(
      "avg(for $x in 1 to 10 return xs:dayTimeDuration(concat(\"PT\",$x,\"H\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "PT5H30M")
    );
  }

  /**
   *  Test fn:avg on a sequence of xs:yearMonthDuration arguments .
   */
  @org.junit.Test
  public void fnAvg5() {
    final XQuery query = new XQuery(
      "avg((xs:yearMonthDuration(\"P1Y\"), xs:yearMonthDuration(\"P1M\"))) instance of xs:yearMonthDuration",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
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
  public void fnAvg6() {
    final XQuery query = new XQuery(
      "avg(for $x in 1 to 9 return xs:yearMonthDuration(concat(\"P\",$x,\"M\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P5M")
    );
  }

  /**
   *  Test fn:sum on a sequence of xs:yearMonthDuration and xs:dayTimeDuration arguments .
   */
  @org.junit.Test
  public void fnAvg7() {
    final XQuery query = new XQuery(
      "avg((xs:yearMonthDuration(\"P1Y\"), xs:dayTimeDuration(\"P1D\")))",
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
   *  Test fn:sum on a sequence of xs:duration arguments .
   */
  @org.junit.Test
  public void fnAvg8() {
    final XQuery query = new XQuery(
      "avg(xs:duration(\"P1Y1M1D\"))",
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
   *  arg: seq of integer .
   */
  @org.junit.Test
  public void fnAvgMixArgs001() {
    final XQuery query = new XQuery(
      "fn:avg( (3, 4, 5) )",
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
   *  arg: yearMonthDuration .
   */
  @org.junit.Test
  public void fnAvgMixArgs002() {
    final XQuery query = new XQuery(
      "fn:avg(( xs:yearMonthDuration(\"P20Y\") , xs:yearMonthDuration(\"P10M\") ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P10Y5M")
    );
  }

  /**
   *  arg: empty seq .
   */
  @org.junit.Test
  public void fnAvgMixArgs003() {
    final XQuery query = new XQuery(
      "fn:avg(())",
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
   *  arg: float .
   */
  @org.junit.Test
  public void fnAvgMixArgs004() {
    final XQuery query = new XQuery(
      "fn:avg(( xs:float('INF'), xs:float('-INF')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  arg: seq of integer, float .
   */
  @org.junit.Test
  public void fnAvgMixArgs005() {
    final XQuery query = new XQuery(
      "fn:avg(( (3, 4, 5), xs:float('NaN') ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  arg: seq of integer .
   */
  @org.junit.Test
  public void fnAvgMixArgs006() {
    final XQuery query = new XQuery(
      "fn:avg(( fn:string-length(\"Hello\") ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("5")
    );
  }

  /**
   *  arg: seq of integer .
   */
  @org.junit.Test
  public void fnAvgMixArgs007() {
    final XQuery query = new XQuery(
      "fn:avg(( fn:count(\"Hello\") ))",
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
   *  arg: seq of integer .
   */
  @org.junit.Test
  public void fnAvgMixArgs008() {
    final XQuery query = new XQuery(
      "fn:avg( ( ( xs:integer(\"100\"), xs:integer(\"-100\"))))",
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
   *  arg: seq of integer,decimal .
   */
  @org.junit.Test
  public void fnAvgMixArgs009() {
    final XQuery query = new XQuery(
      "fn:avg( ( ( xs:decimal(\"-1.000000000001\"), xs:integer(\"-100\"))))",
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
        assertEq("-50.5000000000005")
      ||
        assertEq("-50.5")
      )
    );
  }

  /**
   *  arg: seq of integer,decimal .
   */
  @org.junit.Test
  public void fnAvgMixArgs010() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:decimal(\"1.01\"), xs:integer(\"12\") )))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("6.505")
    );
  }

  /**
   *  arg: seq of integer,float,empty seq .
   */
  @org.junit.Test
  public void fnAvgMixArgs011() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:float(\"NaN\"), 100, (), 2)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  arg: seq of float,decimal .
   */
  @org.junit.Test
  public void fnAvgMixArgs012() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:float(\"-3.4028235E38\"), xs:decimal(\"-999999999999999999\") )))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(\"-1.7014117E38\")")
    );
  }

  /**
   *  arg: seq of float,decimal .
   */
  @org.junit.Test
  public void fnAvgMixArgs013() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:float(\"0\"), xs:decimal(\"-999999999999999999\") ))) eq xs:float(\"-4.9999999999999999E17\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  arg: seq of float,decimal .
   */
  @org.junit.Test
  public void fnAvgMixArgs014() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:float(\"3.4028235E38\"), xs:decimal(\"-999999999999999999\") )))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(\"1.7014117E38\")")
    );
  }

  /**
   *  arg: seq of float,decimal .
   */
  @org.junit.Test
  public void fnAvgMixArgs015() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:float(\"-0\"), xs:decimal(\"-999999999999999999\") ))) eq xs:float(\"-4.9999999999999999E17\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  arg: seq of float,decimal .
   */
  @org.junit.Test
  public void fnAvgMixArgs016() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:float(\"NaN\"), xs:decimal(\"-999999999999999999\") )))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  arg: seq of float,decimal .
   */
  @org.junit.Test
  public void fnAvgMixArgs017() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:float(\"INF\"), xs:decimal(\"-999999999999999999\") )))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "INF")
    );
  }

  /**
   *  arg: seq of float .
   */
  @org.junit.Test
  public void fnAvgMixArgs018() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:float(\"1.01\"))))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1.01")
    );
  }

  /**
   *  arg: seq of float,decimal .
   */
  @org.junit.Test
  public void fnAvgMixArgs019() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:float(\"-INF\"), xs:decimal(\"2.34\"))))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-INF")
    );
  }

  /**
   *  arg: seq of double,integer .
   */
  @org.junit.Test
  public void fnAvgMixArgs020() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:double(\"-1.7976931348623157E308\"), xs:integer(\"-999999999999999999\") ) )) eq xs:double(\"-8.988465674311579E307\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  arg: seq of double,integer .
   */
  @org.junit.Test
  public void fnAvgMixArgs021() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:double(\"0\"), xs:integer(\"-999999999999999999\") ) ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-5.0E17")
    );
  }

  /**
   *  arg: seq of double,integer .
   */
  @org.junit.Test
  public void fnAvgMixArgs022() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:double(\"1.7976931348623157E308\"), xs:integer(\"-999999999999999999\") ) )) eq xs:double(\"8.988465674311579E307\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  arg: seq of double,integer .
   */
  @org.junit.Test
  public void fnAvgMixArgs023() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:double(\"-0\"), xs:integer(\"-999999999999999999\") ) ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-5.0E17")
    );
  }

  /**
   *  arg: seq of double,integer .
   */
  @org.junit.Test
  public void fnAvgMixArgs024() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:double(\"-INF\"), xs:integer(\"-999999999999999999\") ) ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-INF")
    );
  }

  /**
   *  arg: seq of double,integer .
   */
  @org.junit.Test
  public void fnAvgMixArgs025() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:double(\"NaN\"), xs:integer(\"-999999999999999999\") ) ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  arg: seq of double,float .
   */
  @org.junit.Test
  public void fnAvgMixArgs026() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:double(\"1.34\"), xs:float(\"INF\"))))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "INF")
    );
  }

  /**
   *  arg: seq of double,integer .
   */
  @org.junit.Test
  public void fnAvgMixArgs027() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:double(\"INF\"), 2, 3)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "INF")
    );
  }

  /**
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs028() {
    final XQuery query = new XQuery(
      "fn:avg((xs:yearMonthDuration(\"P20Y\") , (3, 4, 5)))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs029() {
    final XQuery query = new XQuery(
      "fn:avg(( fn:empty(\"Hello\")) or fn:boolean(fn:count(\"Hello\")))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs030() {
    final XQuery query = new XQuery(
      "fn:avg(( concat('hi',' all') ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs031() {
    final XQuery query = new XQuery(
      "fn:avg(( fn:empty(\"Hello\") ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs032() {
    final XQuery query = new XQuery(
      "fn:avg(( (\"a\", \"b\", \"c\", true()) ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs033() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:string(\"xyz\"), (), (), \"a\" , \"b\") ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs034() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:anyURI(\"www.example.com\"), \"a\", (\"\"), \"b\")))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs035() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:integer(\"100\"), xs:string(\"abc\"))))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs036() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:integer(\"-999999999999999999\") , \"a\", (), \"3\") ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs037() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:integer(\"830993497117024304\"), \"a\", (), \"3\") ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs038() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:integer(\"999999999999999999\"), \"a\", (), \"3\") ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs039() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:decimal(\"-999999999999999999\") , \"a\", (), \"3\") ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs040() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:decimal(\"617375191608514839\"), \"a\", (), \"3\") ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs041() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:decimal(\"999999999999999999\"), \"a\", (), \"3\") ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs042() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:decimal(\"1.01\"), xs:integer(\"12\"), xs:anyURI(\"www.example.com\"))))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs043() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:float(\"-3.4028235E38\"), xs:decimal(\"-999999999999999999\") , \"a\", (), \"3\") ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs044() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:float(\"0\"), xs:decimal(\"-999999999999999999\") , \"a\", (), \"3\") ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs045() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:float(\"3.4028235E38\"), xs:decimal(\"-999999999999999999\") , \"a\", (), \"3\") ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs046() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:float(\"-0\"), xs:decimal(\"-999999999999999999\") , \"a\", (), \"3\") ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs047() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:float(\"NaN\"), xs:decimal(\"-999999999999999999\") , \"a\", (), \"3\") ))",
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
        assertStringValue(false, "NaN")
      ||
        error("FORG0006")
      )
    );
  }

  /**
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs048() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:float(\"INF\"), xs:decimal(\"-999999999999999999\") , \"a\", (), \"3\") ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs049() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:float(\"1.01\"), xs:string(\"a\"))))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs050() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:float(\"-INF\"), xs:decimal(\"2.34\"), \"abc\")))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs051() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:double(\"-1.7976931348623157E308\"), xs:integer(\"-999999999999999999\") , \"a\", (), \"3\") ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs052() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:double(\"0\"), xs:integer(\"-999999999999999999\") , \"a\", (), \"3\") ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs053() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:double(\"1.7976931348623157E308\"), xs:integer(\"-999999999999999999\") , \"a\", (), \"3\") ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs054() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:double(\"-0\"), xs:integer(\"-999999999999999999\") , \"a\", (), \"3\") ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs055() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:double(\"-INF\"), xs:integer(\"-999999999999999999\") , \"a\", (), \"3\") ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs056() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:double(\"NaN\"), xs:integer(\"-999999999999999999\") , \"a\", (), \"3\") ))",
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
        assertStringValue(false, "NaN")
      ||
        error("FORG0006")
      )
    );
  }

  /**
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs057() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:double(\"1.34\"), xs:float(\"INF\"), true())))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs058() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:boolean(\"false\"), xs:string(\"xyz\"), (), (), \"a\" , \"b\")))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs059() {
    final XQuery query = new XQuery(
      "fn:avg(( (true(), xs:string(\"xyz\"), (), (), \"a\" , \"b\")))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs060() {
    final XQuery query = new XQuery(
      "fn:avg(( (false(), xs:string(\"xyz\"), (), (), \"a\" , \"b\")))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs061() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:boolean(\"1\"), xs:double(\"-INF\"), \"s\")))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs062() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:boolean(\"true\"), xs:date(\"1993-03-31\"), 4, \"a\")))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs063() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:date(\"1993-03-31\"), xs:string(\"xyz\"), (), (), \"a\" , \"b\")))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs064() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:dateTime(\"1972-12-31T00:00:00\"), xs:boolean(\"false\"), (), (\" \")) ))",
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
   *  Negative test gives FORG0006 .
   */
  @org.junit.Test
  public void fnAvgMixArgs065() {
    final XQuery query = new XQuery(
      "fn:avg(( (xs:time(\"12:30:00\"), xs:decimal(\"2.000003\"), 2)))",
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
   *  Negative test gives FORG0001 .
   */
  @org.junit.Test
  public void fnAvgMixArgs066() {
    final XQuery query = new XQuery(
      "fn:avg(/works/employee[1])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnAvgdbl1args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:double(\"-1.7976931348623157E308\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnAvgdbl1args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:double(\"0\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnAvgdbl1args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:double(\"1.7976931348623157E308\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnAvgdbl2args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:double(\"-1.7976931348623157E150\"),xs:double(\"-1.7976931348623157E150\"))) eq -1.7976931348623157E150",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:double(mid range) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnAvgdbl2args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:double(\"0\"),xs:double(\"-1.7976931348623157E308\"))) eq -8.9884656743115785E307",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:double(upper bound) $arg2 = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnAvgdbl2args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:double(\"1.7976931348623157E308\"),xs:double(\"-1.7976931348623157E308\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnAvgdbl2args4() {
    final XQuery query = new XQuery(
      "fn:avg((xs:double(\"-1.7976931348623157E308\"),xs:double(\"0\"))) eq -8.9884656743115785E307",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:double(lower bound) $arg2 = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnAvgdbl2args5() {
    final XQuery query = new XQuery(
      "fn:avg((xs:double(\"-1.7976931348623157E308\"),xs:double(\"1.7976931348623157E308\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnAvgdec1args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:decimal(\"-999999999999999999\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnAvgdec1args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:decimal(\"617375191608514839\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnAvgdec1args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:decimal(\"999999999999999999\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnAvgdec2args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:decimal(\"-999999999999999999\"),xs:decimal(\"-999999999999999999\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:decimal(mid range) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnAvgdec2args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:decimal(\"617375191608514839\"),xs:decimal(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-191312404195742580")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:decimal(upper bound) $arg2 = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnAvgdec2args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:decimal(\"999999999999999999\"),xs:decimal(\"-999999999999999999\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnAvgdec2args4() {
    final XQuery query = new XQuery(
      "fn:avg((xs:decimal(\"-999999999999999999\"),xs:decimal(\"617375191608514839\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-191312404195742580")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:decimal(lower bound) $arg2 = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnAvgdec2args5() {
    final XQuery query = new XQuery(
      "fn:avg((xs:decimal(\"-999999999999999999\"),xs:decimal(\"999999999999999999\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnAvgflt1args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:float(\"-3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float('-3.4028235E38')")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnAvgflt1args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:float(\"0\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnAvgflt1args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:float(\"3.4028235E38\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnAvgflt2args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:float(\"-3.4028235E38\"),xs:float(\"-3.4028235E38\")))",
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
        assertEq("-3.4028235E38")
      ||
        error("FOAR0002")
      )
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:float(mid range) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnAvgflt2args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:float(\"0\"),xs:float(\"-3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float('-1.7014117E38')")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:float(upper bound) $arg2 = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnAvgflt2args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:float(\"3.4028235E38\"),xs:float(\"-3.4028235E38\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnAvgflt2args4() {
    final XQuery query = new XQuery(
      "fn:avg((xs:float(\"-3.4028235E38\"),xs:float(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float('-1.7014117E38')")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:float(lower bound) $arg2 = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnAvgflt2args5() {
    final XQuery query = new XQuery(
      "fn:avg((xs:float(\"-3.4028235E38\"),xs:float(\"3.4028235E38\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnAvgint1args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:int(\"-2147483648\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnAvgint1args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:int(\"-1873914410\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnAvgint1args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:int(\"2147483647\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnAvgint2args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:int(\"-2147483648\"),xs:int(\"-2147483648\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:int(mid range) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnAvgint2args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:int(\"-1873914410\"),xs:int(\"-2147483648\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-2010699029")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:int(upper bound) $arg2 = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnAvgint2args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:int(\"2147483647\"),xs:int(\"-2147483648\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-0.5")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnAvgint2args4() {
    final XQuery query = new XQuery(
      "fn:avg((xs:int(\"-2147483648\"),xs:int(\"-1873914410\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-2010699029")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:int(lower bound) $arg2 = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnAvgint2args5() {
    final XQuery query = new XQuery(
      "fn:avg((xs:int(\"-2147483648\"),xs:int(\"2147483647\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-0.5")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnAvgintg1args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:integer(\"-999999999999999999\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnAvgintg1args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:integer(\"830993497117024304\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnAvgintg1args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:integer(\"999999999999999999\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnAvgintg2args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:integer(\"-999999999999999999\"),xs:integer(\"-999999999999999999\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:integer(mid range) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnAvgintg2args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:integer(\"830993497117024304\"),xs:integer(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-84503251441487847.5")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:integer(upper bound) $arg2 = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnAvgintg2args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:integer(\"999999999999999999\"),xs:integer(\"-999999999999999999\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnAvgintg2args4() {
    final XQuery query = new XQuery(
      "fn:avg((xs:integer(\"-999999999999999999\"),xs:integer(\"830993497117024304\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-84503251441487847.5")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:integer(lower bound) $arg2 = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnAvgintg2args5() {
    final XQuery query = new XQuery(
      "fn:avg((xs:integer(\"-999999999999999999\"),xs:integer(\"999999999999999999\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnAvglng1args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:long(\"-92233720368547758\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnAvglng1args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:long(\"-47175562203048468\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnAvglng1args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:long(\"92233720368547758\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnAvglng2args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:long(\"-92233720368547758\"),xs:long(\"-92233720368547758\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:long(mid range) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnAvglng2args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:long(\"-47175562203048468\"),xs:long(\"-92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-69704641285798113")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:long(upper bound) $arg2 = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnAvglng2args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:long(\"92233720368547758\"),xs:long(\"-92233720368547758\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnAvglng2args4() {
    final XQuery query = new XQuery(
      "fn:avg((xs:long(\"-92233720368547758\"),xs:long(\"-47175562203048468\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-69704641285798113")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:long(lower bound) $arg2 = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnAvglng2args5() {
    final XQuery query = new XQuery(
      "fn:avg((xs:long(\"-92233720368547758\"),xs:long(\"92233720368547758\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAvgnint1args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:negativeInteger(\"-999999999999999999\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnAvgnint1args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:negativeInteger(\"-297014075999096793\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnAvgnint1args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:negativeInteger(\"-1\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAvgnint2args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:negativeInteger(\"-999999999999999999\"),xs:negativeInteger(\"-999999999999999999\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:negativeInteger(mid range) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAvgnint2args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:negativeInteger(\"-297014075999096793\"),xs:negativeInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-648507037999548396")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:negativeInteger(upper bound) $arg2 = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAvgnint2args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:negativeInteger(\"-1\"),xs:negativeInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-500000000000000000")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnAvgnint2args4() {
    final XQuery query = new XQuery(
      "fn:avg((xs:negativeInteger(\"-999999999999999999\"),xs:negativeInteger(\"-297014075999096793\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-648507037999548396")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:negativeInteger(lower bound) $arg2 = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnAvgnint2args5() {
    final XQuery query = new XQuery(
      "fn:avg((xs:negativeInteger(\"-999999999999999999\"),xs:negativeInteger(\"-1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-500000000000000000")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAvgnni1args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:nonNegativeInteger(\"0\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnAvgnni1args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:nonNegativeInteger(\"303884545991464527\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnAvgnni1args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:nonNegativeInteger(\"999999999999999999\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAvgnni2args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"0\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(mid range) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAvgnni2args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:nonNegativeInteger(\"303884545991464527\"),xs:nonNegativeInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("151942272995732263.5")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(upper bound) $arg2 = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAvgnni2args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:nonNegativeInteger(\"999999999999999999\"),xs:nonNegativeInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("499999999999999999.5")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnAvgnni2args4() {
    final XQuery query = new XQuery(
      "fn:avg((xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"303884545991464527\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("151942272995732263.5")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:nonNegativeInteger(lower bound) $arg2 = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnAvgnni2args5() {
    final XQuery query = new XQuery(
      "fn:avg((xs:nonNegativeInteger(\"0\"),xs:nonNegativeInteger(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("499999999999999999.5")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAvgnpi1args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:nonPositiveInteger(\"-999999999999999999\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnAvgnpi1args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:nonPositiveInteger(\"-475688437271870490\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnAvgnpi1args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:nonPositiveInteger(\"0\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAvgnpi2args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:nonPositiveInteger(\"-999999999999999999\"),xs:nonPositiveInteger(\"-999999999999999999\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(mid range) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAvgnpi2args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:nonPositiveInteger(\"-475688437271870490\"),xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-737844218635935244.5")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(upper bound) $arg2 = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAvgnpi2args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:nonPositiveInteger(\"0\"),xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-499999999999999999.5")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnAvgnpi2args4() {
    final XQuery query = new XQuery(
      "fn:avg((xs:nonPositiveInteger(\"-999999999999999999\"),xs:nonPositiveInteger(\"-475688437271870490\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-737844218635935244.5")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:nonPositiveInteger(lower bound) $arg2 = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnAvgnpi2args5() {
    final XQuery query = new XQuery(
      "fn:avg((xs:nonPositiveInteger(\"-999999999999999999\"),xs:nonPositiveInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-499999999999999999.5")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAvgpint1args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:positiveInteger(\"1\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnAvgpint1args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:positiveInteger(\"52704602390610033\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnAvgpint1args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:positiveInteger(\"999999999999999999\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAvgpint2args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:positiveInteger(\"1\"),xs:positiveInteger(\"1\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:positiveInteger(mid range) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAvgpint2args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:positiveInteger(\"52704602390610033\"),xs:positiveInteger(\"1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("26352301195305017")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:positiveInteger(upper bound) $arg2 = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnAvgpint2args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:positiveInteger(\"999999999999999999\"),xs:positiveInteger(\"1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("500000000000000000")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnAvgpint2args4() {
    final XQuery query = new XQuery(
      "fn:avg((xs:positiveInteger(\"1\"),xs:positiveInteger(\"52704602390610033\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("26352301195305017")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:positiveInteger(lower bound) $arg2 = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnAvgpint2args5() {
    final XQuery query = new XQuery(
      "fn:avg((xs:positiveInteger(\"1\"),xs:positiveInteger(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("500000000000000000")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnAvgsht1args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:short(\"-32768\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnAvgsht1args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:short(\"-5324\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnAvgsht1args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:short(\"32767\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnAvgsht2args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:short(\"-32768\"),xs:short(\"-32768\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:short(mid range) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnAvgsht2args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:short(\"-5324\"),xs:short(\"-32768\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-19046")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:short(upper bound) $arg2 = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnAvgsht2args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:short(\"32767\"),xs:short(\"-32768\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-0.5")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnAvgsht2args4() {
    final XQuery query = new XQuery(
      "fn:avg((xs:short(\"-32768\"),xs:short(\"-5324\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-19046")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:short(lower bound) $arg2 = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnAvgsht2args5() {
    final XQuery query = new XQuery(
      "fn:avg((xs:short(\"-32768\"),xs:short(\"32767\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-0.5")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnAvgulng1args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:unsignedLong(\"0\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnAvgulng1args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:unsignedLong(\"130747108607674654\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnAvgulng1args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:unsignedLong(\"184467440737095516\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnAvgulng2args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:unsignedLong(\"0\"),xs:unsignedLong(\"0\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:unsignedLong(mid range) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnAvgulng2args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:unsignedLong(\"130747108607674654\"),xs:unsignedLong(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("65373554303837327")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:unsignedLong(upper bound) $arg2 = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnAvgulng2args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:unsignedLong(\"184467440737095516\"),xs:unsignedLong(\"0\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnAvgulng2args4() {
    final XQuery query = new XQuery(
      "fn:avg((xs:unsignedLong(\"0\"),xs:unsignedLong(\"130747108607674654\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("65373554303837327")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:unsignedLong(lower bound) $arg2 = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnAvgulng2args5() {
    final XQuery query = new XQuery(
      "fn:avg((xs:unsignedLong(\"0\"),xs:unsignedLong(\"184467440737095516\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnAvgusht1args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:unsignedShort(\"0\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnAvgusht1args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:unsignedShort(\"44633\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnAvgusht1args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:unsignedShort(\"65535\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnAvgusht2args1() {
    final XQuery query = new XQuery(
      "fn:avg((xs:unsignedShort(\"0\"),xs:unsignedShort(\"0\")))",
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
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:unsignedShort(mid range) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnAvgusht2args2() {
    final XQuery query = new XQuery(
      "fn:avg((xs:unsignedShort(\"44633\"),xs:unsignedShort(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("22316.5")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:unsignedShort(upper bound) $arg2 = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnAvgusht2args3() {
    final XQuery query = new XQuery(
      "fn:avg((xs:unsignedShort(\"65535\"),xs:unsignedShort(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("32767.5")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnAvgusht2args4() {
    final XQuery query = new XQuery(
      "fn:avg((xs:unsignedShort(\"0\"),xs:unsignedShort(\"44633\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("22316.5")
    );
  }

  /**
   *  Evaluates The "avg" function with the arguments set as follows: $arg1 = xs:unsignedShort(lower bound) $arg2 = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnAvgusht2args5() {
    final XQuery query = new XQuery(
      "fn:avg((xs:unsignedShort(\"0\"),xs:unsignedShort(\"65535\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("32767.5")
    );
  }
}
