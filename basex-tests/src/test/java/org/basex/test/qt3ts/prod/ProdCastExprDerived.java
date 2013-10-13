package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the CastExpr production: cast expressions casting to/from built-in derived types.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdCastExprDerived extends QT3TestSet {

  /**
   * Casting from float to decimal. .
   */
  @org.junit.Test
  public void castDerived1() {
    final XQuery query = new XQuery(
      "let $value := xs:float(10.0) return $value cast as xs:decimal",
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
   *  Casting from float to a short. .
   */
  @org.junit.Test
  public void castDerived10() {
    final XQuery query = new XQuery(
      "let $value := xs:float(10.0) return $value cast as xs:short",
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
   *  Casting from float to an unsignedInt. .
   */
  @org.junit.Test
  public void castDerived11() {
    final XQuery query = new XQuery(
      "let $value := xs:float(10.0) return $value cast as xs:unsignedInt",
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
   *  Casting from float to an unsignedShort. .
   */
  @org.junit.Test
  public void castDerived12() {
    final XQuery query = new XQuery(
      "let $value := xs:float(10.0) return $value cast as xs:unsignedShort",
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
   *  Casting from float to an unsignedByte. .
   */
  @org.junit.Test
  public void castDerived13() {
    final XQuery query = new XQuery(
      "let $value := xs:float(10.0) return $value cast as xs:unsignedByte",
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
   *  Casting from float to a byte. .
   */
  @org.junit.Test
  public void castDerived14() {
    final XQuery query = new XQuery(
      "let $value := xs:float(10.0) return $value cast as xs:byte",
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
   *  Casting from double to a decimal. .
   */
  @org.junit.Test
  public void castDerived15() {
    final XQuery query = new XQuery(
      "let $value := xs:double(10E2) return $value cast as xs:decimal",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000")
    );
  }

  /**
   *  Casting from double to an integer. .
   */
  @org.junit.Test
  public void castDerived16() {
    final XQuery query = new XQuery(
      "let $value := xs:double(10E2) return $value cast as xs:integer",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000")
    );
  }

  /**
   *  Casting from double to an positiveInteger. .
   */
  @org.junit.Test
  public void castDerived17() {
    final XQuery query = new XQuery(
      "let $value := xs:double(10E2) return $value cast as xs:positiveInteger",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000")
    );
  }

  /**
   *  Casting from double to a long. .
   */
  @org.junit.Test
  public void castDerived18() {
    final XQuery query = new XQuery(
      "let $value := xs:double(10E2) return $value cast as xs:long",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000")
    );
  }

  /**
   *  Casting from double to an int. .
   */
  @org.junit.Test
  public void castDerived19() {
    final XQuery query = new XQuery(
      "let $value := xs:double(10E2) return $value cast as xs:int",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000")
    );
  }

  /**
   *  Casting from float to integer. .
   */
  @org.junit.Test
  public void castDerived2() {
    final XQuery query = new XQuery(
      "let $value := xs:float(10.0) return $value cast as xs:decimal",
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
   *  Casting from double to an unsignedLong. .
   */
  @org.junit.Test
  public void castDerived20() {
    final XQuery query = new XQuery(
      "let $value := xs:double(10E2) return $value cast as xs:unsignedLong",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000")
    );
  }

  /**
   *  Casting from double to a short. .
   */
  @org.junit.Test
  public void castDerived21() {
    final XQuery query = new XQuery(
      "let $value := xs:double(10E2) return $value cast as xs:short",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000")
    );
  }

  /**
   *  Casting from float to nonPositiveInteger. .
   */
  @org.junit.Test
  public void castDerived3() {
    final XQuery query = new XQuery(
      "let $value := xs:float(-10.0) return $value cast as xs:nonPositiveInteger",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-10")
    );
  }

  /**
   *  Casting from float to a long. .
   */
  @org.junit.Test
  public void castDerived4() {
    final XQuery query = new XQuery(
      "let $value := xs:float(10.0) return $value cast as xs:long",
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
   *  Casting from float to a nonNegativeInteger. .
   */
  @org.junit.Test
  public void castDerived5() {
    final XQuery query = new XQuery(
      "let $value := xs:float(10.0) return $value cast as xs:nonNegativeInteger",
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
   *  Casting from float to a negativeInteger. .
   */
  @org.junit.Test
  public void castDerived6() {
    final XQuery query = new XQuery(
      "let $value := xs:float(-10.0) return $value cast as xs:negativeInteger",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-10")
    );
  }

  /**
   *  Casting from float to an int. .
   */
  @org.junit.Test
  public void castDerived7() {
    final XQuery query = new XQuery(
      "let $value := xs:float(10.0) return $value cast as xs:int",
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
   *  Casting from float to an unsignedLong. .
   */
  @org.junit.Test
  public void castDerived8() {
    final XQuery query = new XQuery(
      "let $value := xs:float(10.0) return $value cast as xs:unsignedLong",
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
   *  Casting from float to a positiveInteger. .
   */
  @org.junit.Test
  public void castDerived9() {
    final XQuery query = new XQuery(
      "let $value := xs:float(10.0) return $value cast as xs:positiveInteger",
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
   * Casting from decimal to float. .
   */
  @org.junit.Test
  public void castToParent1() {
    final XQuery query = new XQuery(
      "let $value := xs:decimal(10.0) return $value cast as xs:float",
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
   *  Casting from a shortto a float. .
   */
  @org.junit.Test
  public void castToParent10() {
    final XQuery query = new XQuery(
      "let $value := xs:short(10.0) return $value cast as xs:float",
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
   *  Casting from an unsignedInt to a float. .
   */
  @org.junit.Test
  public void castToParent11() {
    final XQuery query = new XQuery(
      "let $value := xs:unsignedInt(10.0) return $value cast as xs:float",
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
   *  Casting from an unsignedShort to a float. .
   */
  @org.junit.Test
  public void castToParent12() {
    final XQuery query = new XQuery(
      "let $value := xs:unsignedShort(10.0) return $value cast as xs:float",
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
   *  Casting from an unsignedByte to a float. .
   */
  @org.junit.Test
  public void castToParent13() {
    final XQuery query = new XQuery(
      "let $value := xs:unsignedByte(10.0) return $value cast as xs:float",
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
   *  Casting from a byte to a float. .
   */
  @org.junit.Test
  public void castToParent14() {
    final XQuery query = new XQuery(
      "let $value := xs:byte(10.0) return $value cast as xs:float",
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
   *  Casting from a decimal to a double. .
   */
  @org.junit.Test
  public void castToParent15() {
    final XQuery query = new XQuery(
      "let $value := xs:decimal(10E2) return $value cast as xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000")
    );
  }

  /**
   *  Casting from an integer to a double. .
   */
  @org.junit.Test
  public void castToParent16() {
    final XQuery query = new XQuery(
      "let $value := xs:integer(10E2) return $value cast as xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000")
    );
  }

  /**
   *  Casting from a positiveInteger to a double. .
   */
  @org.junit.Test
  public void castToParent17() {
    final XQuery query = new XQuery(
      "let $value := xs:positiveInteger(10E2) return $value cast as xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000")
    );
  }

  /**
   *  Casting from a long to a double. .
   */
  @org.junit.Test
  public void castToParent18() {
    final XQuery query = new XQuery(
      "let $value := xs:long(10E2) return $value cast as xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000")
    );
  }

  /**
   *  Casting from an int to a double. .
   */
  @org.junit.Test
  public void castToParent19() {
    final XQuery query = new XQuery(
      "let $value := xs:int(10E2) return $value cast as xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000")
    );
  }

  /**
   *  Casting from intger to float. .
   */
  @org.junit.Test
  public void castToParent2() {
    final XQuery query = new XQuery(
      "let $value := xs:integer(10.0) return $value cast as xs:float",
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
   *  Casting from an unsignedLong to a double. .
   */
  @org.junit.Test
  public void castToParent20() {
    final XQuery query = new XQuery(
      "let $value := xs:unsignedLong(10E2) return $value cast as xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000")
    );
  }

  /**
   *  Casting from a short to a double. .
   */
  @org.junit.Test
  public void castToParent21() {
    final XQuery query = new XQuery(
      "let $value := xs:short(10E2) return $value cast as xs:double",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1000")
    );
  }

  /**
   *  Casting from nonPositiveInteger to float. .
   */
  @org.junit.Test
  public void castToParent3() {
    final XQuery query = new XQuery(
      "let $value := xs:nonPositiveInteger(-10.0) return $value cast as xs:float",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-10")
    );
  }

  /**
   *  Casting from long to float. .
   */
  @org.junit.Test
  public void castToParent4() {
    final XQuery query = new XQuery(
      "let $value := xs:long(10.0) return $value cast as xs:float",
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
   *  Casting from nonNegativeInteger to float. .
   */
  @org.junit.Test
  public void castToParent5() {
    final XQuery query = new XQuery(
      "let $value := xs:nonNegativeInteger(10.0) return $value cast as xs:float",
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
   *  Casting from a negativeInteger to a float. .
   */
  @org.junit.Test
  public void castToParent6() {
    final XQuery query = new XQuery(
      "let $value := xs:negativeInteger(-10.0) return $value cast as xs:float",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-10")
    );
  }

  /**
   *  Casting from an int to a float. .
   */
  @org.junit.Test
  public void castToParent7() {
    final XQuery query = new XQuery(
      "let $value := xs:int(10.0) return $value cast as xs:float",
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
   *  Casting from an unsignedLong to a float. .
   */
  @org.junit.Test
  public void castToParent8() {
    final XQuery query = new XQuery(
      "let $value := xs:unsignedLong(10.0) return $value cast as xs:float",
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
   *  Casting from a positiveInteger to a float. .
   */
  @org.junit.Test
  public void castToParent9() {
    final XQuery query = new XQuery(
      "let $value := xs:positiveInteger(10.0) return $value cast as xs:float",
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
   * Casting from integer to nonPositiveInteger. .
   */
  @org.junit.Test
  public void castWithin1() {
    final XQuery query = new XQuery(
      "let $value := xs:integer(-10.0) return $value cast as xs:nonPositiveInteger",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-10")
    );
  }

  /**
   *  Casting from integer to byte. .
   */
  @org.junit.Test
  public void castWithin10() {
    final XQuery query = new XQuery(
      "let $value := xs:integer(10.0) return $value cast as xs:byte",
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
   *  Casting from integer to unsignedShort. .
   */
  @org.junit.Test
  public void castWithin11() {
    final XQuery query = new XQuery(
      "let $value := xs:integer(10.0) return $value cast as xs:unsignedShort",
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
   *  Casting from integer to an unsignedByte. .
   */
  @org.junit.Test
  public void castWithin12() {
    final XQuery query = new XQuery(
      "let $value := xs:integer(10.0) return $value cast as xs:unsignedByte",
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
   *  Casting from a long to an integer. .
   */
  @org.junit.Test
  public void castWithin13() {
    final XQuery query = new XQuery(
      "let $value := xs:long(10.0) return $value cast as xs:integer",
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
   *  Casting from long to a nonNegativeInteger. .
   */
  @org.junit.Test
  public void castWithin14() {
    final XQuery query = new XQuery(
      "let $value := xs:long(10.0) return $value cast as xs:nonNegativeInteger",
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
   *  Casting from long to a negativeInteger. .
   */
  @org.junit.Test
  public void castWithin15() {
    final XQuery query = new XQuery(
      "let $value := xs:long(-10) return $value cast as xs:negativeInteger",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-10")
    );
  }

  /**
   *  Casting from long to an int. .
   */
  @org.junit.Test
  public void castWithin16() {
    final XQuery query = new XQuery(
      "let $value := xs:long(10) return $value cast as xs:int",
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
   *  Casting from long to an unsignedLong. .
   */
  @org.junit.Test
  public void castWithin17() {
    final XQuery query = new XQuery(
      "let $value := xs:long(10) return $value cast as xs:unsignedLong",
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
   *  Casting from long to a positiveInteger. .
   */
  @org.junit.Test
  public void castWithin18() {
    final XQuery query = new XQuery(
      "let $value := xs:long(10) return $value cast as xs:positiveInteger",
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
   *  Casting from long to a short. .
   */
  @org.junit.Test
  public void castWithin19() {
    final XQuery query = new XQuery(
      "let $value := xs:long(10) return $value cast as xs:short",
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
   *  Casting from integer to long. .
   */
  @org.junit.Test
  public void castWithin2() {
    final XQuery query = new XQuery(
      "let $value := xs:integer(10.0) return $value cast as xs:long",
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
   *  Casting from long to unsignedInt. .
   */
  @org.junit.Test
  public void castWithin20() {
    final XQuery query = new XQuery(
      "let $value := xs:long(10) return $value cast as xs:unsignedInt",
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
   *  Casting from long to a byte. .
   */
  @org.junit.Test
  public void castWithin21() {
    final XQuery query = new XQuery(
      "let $value := xs:long(10) return $value cast as xs:byte",
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
   *  Casting from integer to nonNegativeInteger. .
   */
  @org.junit.Test
  public void castWithin3() {
    final XQuery query = new XQuery(
      "let $value := xs:integer(10.0) return $value cast as xs:nonNegativeInteger",
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
   *  Casting from integer to negativeInteger. .
   */
  @org.junit.Test
  public void castWithin4() {
    final XQuery query = new XQuery(
      "let $value := xs:integer(-10.0) return $value cast as xs:negativeInteger",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-10")
    );
  }

  /**
   *  Casting from integer to int. .
   */
  @org.junit.Test
  public void castWithin5() {
    final XQuery query = new XQuery(
      "let $value := xs:integer(10.0) return $value cast as xs:int",
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
   *  Casting from integer to unsignedLong. .
   */
  @org.junit.Test
  public void castWithin6() {
    final XQuery query = new XQuery(
      "let $value := xs:integer(10.0) return $value cast as xs:unsignedLong",
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
   *  Casting from integer to positiveInteger. .
   */
  @org.junit.Test
  public void castWithin7() {
    final XQuery query = new XQuery(
      "let $value := xs:integer(10.0) return $value cast as xs:positiveInteger",
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
   *  Casting from integer to short. .
   */
  @org.junit.Test
  public void castWithin8() {
    final XQuery query = new XQuery(
      "let $value := xs:integer(10.0) return $value cast as xs:short",
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
   *  Casting from integer to unsignedInt. .
   */
  @org.junit.Test
  public void castWithin9() {
    final XQuery query = new XQuery(
      "let $value := xs:integer(10.0) return $value cast as xs:unsignedInt",
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
   *  Test a uri with an empty path, but with a valid query .
   */
  @org.junit.Test
  public void cbclCaseAnyUri001() {
    final XQuery query = new XQuery(
      "\"http://example.com?query=\" cast as xs:anyURI",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://example.com?query=")
    );
  }

  /**
   *  test cast to xs:byte of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastByte001() {
    final XQuery query = new XQuery(
      "xs:byte(128)",
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
   *  test cast to xs:byte of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastByte002() {
    final XQuery query = new XQuery(
      "xs:byte(-129)",
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
   *  test cast to xs:byte of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastByte003() {
    final XQuery query = new XQuery(
      "xs:byte(\"128\")",
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
   *  test cast to xs:byte of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastByte004() {
    final XQuery query = new XQuery(
      "xs:byte(\"-129\")",
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
   *  test cast to xs:date with overflow .
   */
  @org.junit.Test
  public void cbclCastDate001() {
    final XQuery query = new XQuery(
      "\"-25252734927766555-06-06\" cast as xs:date",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  test cast to xs:date with overflow .
   */
  @org.junit.Test
  public void cbclCastDate002() {
    final XQuery query = new XQuery(
      "\"25252734927766555-07-29\" cast as xs:date",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  test cast to xs:date with overflow .
   */
  @org.junit.Test
  public void cbclCastDate003() {
    final XQuery query = new XQuery(
      "\"18446744073709551616-05-15\" cast as xs:date",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  test cast to xs:date with invalid format .
   */
  @org.junit.Test
  public void cbclCastDate004() {
    final XQuery query = new XQuery(
      "\"18446744073709551616-QQ-15\" cast as xs:date",
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
   *  test cast to xs:dateTime with overflow .
   */
  @org.junit.Test
  public void cbclCastDateTime001() {
    final XQuery query = new XQuery(
      "\"-25252734927766555-06-06T00:00:00Z\" cast as xs:dateTime",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  test cast to xs:dateTime with overflow .
   */
  @org.junit.Test
  public void cbclCastDateTime002() {
    final XQuery query = new XQuery(
      "\"25252734927766555-07-29T00:00:00Z\" cast as xs:dateTime",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  test cast to xs:dateTime with overflow .
   */
  @org.junit.Test
  public void cbclCastDateTime003() {
    final XQuery query = new XQuery(
      "\"18446744073709551616-05-15T16:15:00\" cast as xs:dateTime",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  test cast to xs:dateTime with invalid format .
   */
  @org.junit.Test
  public void cbclCastDateTime004() {
    final XQuery query = new XQuery(
      "\"18446744073709551616-QQ-15T16:15:00\" cast as xs:dateTime",
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
   *  test cast to xs:dayTimeDuration with overflow .
   */
  @org.junit.Test
  public void cbclCastDayTimeDuration001() {
    final XQuery query = new XQuery(
      "\"P11768614336404564651D\" cast as xs:dayTimeDuration",
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
   *  test cast to xs:dayTimeDuration with overflow .
   */
  @org.junit.Test
  public void cbclCastDayTimeDuration002() {
    final XQuery query = new XQuery(
      "\"-P11768614336404564651D\" cast as xs:duration",
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
   *  test cast to xs:decimal of large double value .
   */
  @org.junit.Test
  public void cbclCastDecimal001() {
    final XQuery query = new XQuery(
      "1.7976931348623157E+308 cast as xs:decimal",
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
        error("FOCA0001")
      ||
        assertQuery("string-length(string($result)) gt 300")
      )
    );
  }

  /**
   *  test cast to xs:decimal of large negative double value .
   */
  @org.junit.Test
  public void cbclCastDecimal002() {
    final XQuery query = new XQuery(
      "-1.7976931348623157E+308 cast as xs:decimal",
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
        error("FOCA0001")
      ||
        assertQuery("string-length(string($result)) gt 300")
      )
    );
  }

  /**
   *  test cast to xs:decimal of large float value .
   */
  @org.junit.Test
  public void cbclCastDecimal003() {
    final XQuery query = new XQuery(
      "xs:float('3.402823e38') cast as xs:decimal",
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
        error("FOCA0001")
      ||
        assertQuery("string-length(string($result)) gt 36")
      )
    );
  }

  /**
   *  test cast to xs:decimal of large negative float value .
   */
  @org.junit.Test
  public void cbclCastDecimal004() {
    final XQuery query = new XQuery(
      "xs:float('-3.402823e38') cast as xs:decimal",
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
        error("FOCA0001")
      ||
        assertQuery("string-length(string($result)) gt 36")
      )
    );
  }

  /**
   *  test cast to xs:duration with overflow .
   */
  @org.junit.Test
  public void cbclCastDuration001() {
    final XQuery query = new XQuery(
      "\"-P768614336404564651Y\" cast as xs:duration",
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
   *  test cast to xs:duration with overflow .
   */
  @org.junit.Test
  public void cbclCastDuration002() {
    final XQuery query = new XQuery(
      "\"P768614336404564651Y\" cast as xs:duration",
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
   *  test cast to xs:ENTITY from a subtype of xs:NCName .
   */
  @org.junit.Test
  public void cbclCastEntity001() {
    final XQuery query = new XQuery(
      "xs:NCName('entity') cast as xs:ENTITY",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "entity")
    );
  }

  /**
   *  test cast to xs:ENTITY from a type which will always fail .
   */
  @org.junit.Test
  public void cbclCastEntity002() {
    final XQuery query = new XQuery(
      "fn:current-time() cast as xs:ENTITY",
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
   *  test cast to xs:gYear with overflow .
   */
  @org.junit.Test
  public void cbclCastGYear001() {
    final XQuery query = new XQuery(
      "\"99999999999999999999999999999\" cast as xs:gYear",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  test casting 0000 to xs:gYear .
   */
  @org.junit.Test
  public void cbclCastGYear002() {
    final XQuery query = new XQuery(
      "\"0000\" cast as xs:gYear",
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
   *  test casting -0000 to xs:gYear .
   */
  @org.junit.Test
  public void cbclCastGYear003() {
    final XQuery query = new XQuery(
      "\"-0000\" cast as xs:gYear",
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
   *  test cast to xs:gYearMonth with overflow .
   */
  @org.junit.Test
  public void cbclCastGYearMonth001() {
    final XQuery query = new XQuery(
      "\"99999999999999999999999999999-01\" cast as xs:gYearMonth",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  test cast to xs:gYearMonth with overflow .
   */
  @org.junit.Test
  public void cbclCastGYearMonth002() {
    final XQuery query = new XQuery(
      "\"99999999999999999999999999999-XX\" cast as xs:gYearMonth",
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
   *  test cast year 0000 xs:gYearMonth .
   */
  @org.junit.Test
  public void cbclCastGYearMonth003() {
    final XQuery query = new XQuery(
      "\"0000-05\" cast as xs:gYearMonth",
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
   *  test cast year 0000 xs:gYearMonth .
   */
  @org.junit.Test
  public void cbclCastGYearMonth004() {
    final XQuery query = new XQuery(
      "\"-0000-05\" cast as xs:gYearMonth",
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
   *  test cast to xs:ID from a subtype of xs:NCName .
   */
  @org.junit.Test
  public void cbclCastId001() {
    final XQuery query = new XQuery(
      "xs:NCName('id') cast as xs:ID",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "id")
    );
  }

  /**
   *  test cast to xs:ID from a type which will always fail .
   */
  @org.junit.Test
  public void cbclCastId002() {
    final XQuery query = new XQuery(
      "fn:current-time() cast as xs:ID",
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
   *  test cast to xs:IDREF from a subtype of xs:NCName .
   */
  @org.junit.Test
  public void cbclCastIdref001() {
    final XQuery query = new XQuery(
      "xs:NCName('idref') cast as xs:IDREF",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "idref")
    );
  }

  /**
   *  test cast to xs:IDREF from a type which will always fail .
   */
  @org.junit.Test
  public void cbclCastIdref002() {
    final XQuery query = new XQuery(
      "fn:current-time() cast as xs:IDREF",
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
   *  test cast to xs:int of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastInt001() {
    final XQuery query = new XQuery(
      "xs:int(2147483648)",
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
   *  test cast to xs:int of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastInt002() {
    final XQuery query = new XQuery(
      "xs:int(-2147483649)",
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
   *  test cast to xs:int of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastInt003() {
    final XQuery query = new XQuery(
      "xs:int(\"2147483648\")",
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
   *  test cast to xs:int of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastInt004() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483649\")",
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
   *  test cast to xs:integer of large double value .
   */
  @org.junit.Test
  public void cbclCastInteger001() {
    final XQuery query = new XQuery(
      "1.7976931348623157E+308 cast as xs:integer",
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
        error("FOCA0003")
      ||
        assertQuery("string-length(string($result)) gt 300")
      )
    );
  }

  /**
   *  test cast to xs:integer of large negative double value .
   */
  @org.junit.Test
  public void cbclCastInteger002() {
    final XQuery query = new XQuery(
      "-1.7976931348623157E+308 cast as xs:integer",
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
        error("FOCA0003")
      ||
        assertQuery("string-length(string($result)) gt 300")
      )
    );
  }

  /**
   *  test cast to xs:integer of large float value .
   */
  @org.junit.Test
  public void cbclCastInteger003() {
    final XQuery query = new XQuery(
      "xs:float('3.402823e38') cast as xs:integer",
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
        error("FOCA0003")
      ||
        assertQuery("string-length(string($result)) gt 36")
      )
    );
  }

  /**
   *  test cast to xs:integer of large negative float value .
   */
  @org.junit.Test
  public void cbclCastInteger004() {
    final XQuery query = new XQuery(
      "xs:float('-3.402823e38') cast as xs:integer",
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
        error("FOCA0003")
      ||
        assertQuery("string-length(string($result)) gt 36")
      )
    );
  }

  /**
   *  test cast to xs:language .
   */
  @org.junit.Test
  public void cbclCastLanguage001() {
    final XQuery query = new XQuery(
      "xs:language('en-gb') cast as xs:language",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "en-gb")
    );
  }

  /**
   *  test cast to xs:long of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastLong001() {
    final XQuery query = new XQuery(
      "xs:long(9223372036854775808)",
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
   *  test cast to xs:long of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastLong002() {
    final XQuery query = new XQuery(
      "xs:long(-9223372036854775809)",
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
   *  test cast to xs:long of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastLong003() {
    final XQuery query = new XQuery(
      "xs:long(\"9223372036854775808\")",
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
   *  test cast to xs:long of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastLong004() {
    final XQuery query = new XQuery(
      "xs:long(\"-9223372036854775809\")",
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
   *  test cast to xs:Name from a subtype of xs:Name .
   */
  @org.junit.Test
  public void cbclCastName001() {
    final XQuery query = new XQuery(
      "xs:NCName('NCName') cast as xs:Name",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NCName")
    );
  }

  /**
   *  test cast to xs:Name from a type which will always fail .
   */
  @org.junit.Test
  public void cbclCastName002() {
    final XQuery query = new XQuery(
      "fn:current-time() cast as xs:Name",
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
   *  test cast to xs:NCName from a subtype of xs:NCName .
   */
  @org.junit.Test
  public void cbclCastNcname001() {
    final XQuery query = new XQuery(
      "xs:ID('id') cast as xs:NCName",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "id")
    );
  }

  /**
   *  test cast to xs:NCName from a type which will always fail .
   */
  @org.junit.Test
  public void cbclCastNcname002() {
    final XQuery query = new XQuery(
      "fn:current-time() cast as xs:NCName",
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
   *  test cast to xs:negativeInteger of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastNegativeInteger001() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(0)",
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
   *  test cast to xs:negativeInteger of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastNegativeInteger002() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"0\")",
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
   *  test cast to xs:NMTOKEN .
   */
  @org.junit.Test
  public void cbclCastNmtoken001() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $string := (\"&#xD;&#xA;&#x9; foobar &#xA;&#xD;&#x9;\" cast as xs:NMTOKEN) return not(contains($string, '&#x9;') or contains($string, '&#xA;') or contains($string, '&#xD;') or string-length($string) ne 6)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test cast to xs:NMTOKEN from a subtype of xs:NMTOKEN .
   */
  @org.junit.Test
  public void cbclCastNmtoken002() {
    final XQuery query = new XQuery(
      "xs:NMTOKEN('NMTOKEN') cast as xs:NMTOKEN",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NMTOKEN")
    );
  }

  /**
   *  test cast to xs:nonNegativeInteger of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastNonNegativeInteger001() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(-1)",
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
   *  test cast to xs:nonNegativeInteger of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastNonNegativeInteger002() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"-1\")",
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
   *  Ensure that "-00" is a valid lexical value for xs:nonNegativeInteger .
   */
  @org.junit.Test
  public void cbclCastNonNegativeInteger003() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"-00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  test cast to xs:nonPositiveInteger of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastNonPositiveInteger001() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(1)",
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
   *  test cast to xs:nonPositiveInteger of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastNonPositiveInteger002() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"1\")",
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
   *  Ensure that "+00" is a valid lexical value for xs:nonPositiveInteger .
   */
  @org.junit.Test
  public void cbclCastNonPositiveInteger003() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"+00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  test cast to xs:normalizedString .
   */
  @org.junit.Test
  public void cbclCastNormalizedString001() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $string in (\"&#xD; foo &#x9; bar &#xA;\" cast as xs:normalizedString)\n" +
      "        return not(contains($string, '&#x9;') or \n" +
      "                   contains($string, '&#xA;') or \n" +
      "                   contains($string, '&#xD;') or \n" +
      "                   string-length($string) ne 13)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test cast to xs:positiveInteger of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastPositiveInteger001() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(0)",
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
   *  test cast to xs:positiveInteger of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastPositiveInteger002() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"0\")",
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
   *  test cast to xs:short of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastShort001() {
    final XQuery query = new XQuery(
      "xs:short(32768)",
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
   *  test cast to xs:short of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastShort002() {
    final XQuery query = new XQuery(
      "xs:short(-32769)",
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
   *  test cast to xs:short of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastShort003() {
    final XQuery query = new XQuery(
      "xs:short(\"32768\")",
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
   *  test cast to xs:short of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastShort004() {
    final XQuery query = new XQuery(
      "xs:short(\"-32769\")",
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
   *  test cast to xs:token .
   */
  @org.junit.Test
  public void cbclCastToken001() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $string in (\"&#xD; foo &#x9; bar &#xA;\" cast as xs:token) \n" +
      "        return not(contains($string, '&#x9;') or \n" +
      "                   contains($string, '&#xA;') or \n" +
      "                   contains($string, '&#xD;') or \n" +
      "                   string-length($string) ne 7)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test cast to xs:unsignedByte of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastUnsignedByte001() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(256)",
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
   *  test cast to xs:unsignedByte of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastUnsignedByte002() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(-1)",
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
   *  test cast to xs:unsignedByte of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastUnsignedByte003() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(\"256\")",
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
   *  test cast to xs:unsignedByte of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastUnsignedByte004() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(\"-1\")",
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
   *  Ensure that "-00" is a valid lexical value for xs:unsignedByte .
   */
  @org.junit.Test
  public void cbclCastUnsignedByte005() {
    final XQuery query = new XQuery(
      "xs:unsignedByte(\"-00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  test cast to xs:unsignedInt of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastUnsignedInt001() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(4294967296)",
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
   *  test cast to xs:unsignedInt of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastUnsignedInt002() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(-1)",
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
   *  test cast to xs:unsignedInt of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastUnsignedInt003() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(\"4294967296\")",
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
   *  test cast to xs:unsignedInt of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastUnsignedInt004() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(\"-1\")",
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
   *  Ensure that "-00" is a valid lexical value for xs:unsignedInt .
   */
  @org.junit.Test
  public void cbclCastUnsignedInt005() {
    final XQuery query = new XQuery(
      "xs:unsignedInt(\"-00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  test cast to xs:unsignedLong of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastUnsignedLong001() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(18446744073709551616)",
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
   *  test cast to xs:unsignedLong of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastUnsignedLong002() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(-1)",
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
   *  test cast to xs:unsignedLong of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastUnsignedLong003() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"18446744073709551616\")",
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
   *  test cast to xs:unsignedLong of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastUnsignedLong004() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"-1\")",
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
   *  Ensure that "-00" is a valid lexical value for xs:unsignedLong .
   */
  @org.junit.Test
  public void cbclCastUnsignedLong005() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"-00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  test cast to xs:unsignedShort of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastUnsignedShort001() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(65536)",
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
   *  test cast to xs:unsignedShort of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastUnsignedShort002() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(-1)",
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
   *  test cast to xs:unsignedShort of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastUnsignedShort003() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"65536\")",
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
   *  test cast to xs:unsignedShort of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastUnsignedShort004() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"-1\")",
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
   *  Ensure that "-00" is a valid lexical value for xs:unsignedShort .
   */
  @org.junit.Test
  public void cbclCastUnsignedShort005() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"-00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  test cast to xs:yearMonthDuration with overflow .
   */
  @org.junit.Test
  public void cbclCastYearMonthDuration001() {
    final XQuery query = new XQuery(
      "\"-P768614336404564651Y\" cast as xs:yearMonthDuration",
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
   *  test cast to xs:yearMonthDuration with overflow .
   */
  @org.junit.Test
  public void cbclCastYearMonthDuration002() {
    final XQuery query = new XQuery(
      "\"P768614336404564651Y\" cast as xs:yearMonthDuration",
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
}
