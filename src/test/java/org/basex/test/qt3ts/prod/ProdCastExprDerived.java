package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the CastExpr production: cast expressions casting to/from built-in derived types.
 *
 * @author BaseX Team 2005-12, BSD License
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("10")
    );
  }
}
