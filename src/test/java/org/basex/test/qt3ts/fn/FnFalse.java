package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the function false().
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnFalse extends QT3TestSet {

  /**
   *  A test whose essence is: `false(1)`. .
   */
  @org.junit.Test
  public void kFalseFunc1() {
    final XQuery query = new XQuery(
      "false(1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `false() eq false()`. .
   */
  @org.junit.Test
  public void kFalseFunc2() {
    final XQuery query = new XQuery(
      "false() eq false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(false())`. .
   */
  @org.junit.Test
  public void kFalseFunc3() {
    final XQuery query = new XQuery(
      "not(false())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple call to an "fn:false" function. .
   */
  @org.junit.Test
  public void fnFalse1() {
    final XQuery query = new XQuery(
      "fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(false)
      &&
        assertType("xs:boolean")
      )
    );
  }

  /**
   *  Evaluation of an "fn:false" function with a comparison expression involving the "ge" operator. .
   */
  @org.junit.Test
  public void fnFalse10() {
    final XQuery query = new XQuery(
      "fn:false() ge fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of an "fn:false" function with a comparison expression involving the "=" operator. .
   */
  @org.junit.Test
  public void fnFalse11() {
    final XQuery query = new XQuery(
      "fn:false() = fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of an "fn:false" function with a comparison expression involving the "!=" operator. .
   */
  @org.junit.Test
  public void fnFalse12() {
    final XQuery query = new XQuery(
      "fn:false() != fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of an "fn:false" function with a comparison expression involving the "<" operator. .
   */
  @org.junit.Test
  public void fnFalse13() {
    final XQuery query = new XQuery(
      "fn:false() < fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of an "fn:false" function with a comparison expression involving the "<=" operator. .
   */
  @org.junit.Test
  public void fnFalse14() {
    final XQuery query = new XQuery(
      "fn:false() <= fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of an "fn:false" function with a comparison expression involving the ">" operator. .
   */
  @org.junit.Test
  public void fnFalse15() {
    final XQuery query = new XQuery(
      "fn:false() > fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of an "fn:false" function with a comparison expression involving the ">=" operator. .
   */
  @org.junit.Test
  public void fnFalse16() {
    final XQuery query = new XQuery(
      "fn:false() >= fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of an "fn:false" function as an argument to an "xs:boolean" function. .
   */
  @org.junit.Test
  public void fnFalse17() {
    final XQuery query = new XQuery(
      "xs:boolean(fn:false())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of an "fn:false" function as an argument to an "fn:string" function. .
   */
  @org.junit.Test
  public void fnFalse18() {
    final XQuery query = new XQuery(
      "fn:string(fn:false())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("\"false\"")
    );
  }

  /**
   *  Evaluation of an "fn:false" function as arguments to an "fn:concat" function. .
   */
  @org.junit.Test
  public void fnFalse19() {
    final XQuery query = new XQuery(
      "fn:concat(xs:string(fn:false()),xs:string(fn:false()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "falsefalse")
    );
  }

  /**
   *  Evaliation of "fn:false" function as argument to fn:not function. .
   */
  @org.junit.Test
  public void fnFalse2() {
    final XQuery query = new XQuery(
      "fn:not(fn:false())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of an "fn:false" function as arguments to an "fn:contains" function. .
   */
  @org.junit.Test
  public void fnFalse20() {
    final XQuery query = new XQuery(
      "fn:contains(xs:string(fn:false()),xs:string(fn:false()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of an "fn:false" function as an argument to an "fn:string-length" function. .
   */
  @org.junit.Test
  public void fnFalse21() {
    final XQuery query = new XQuery(
      "fn:string-length(xs:string(fn:false()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "5")
    );
  }

  /**
   * false() as a function item.
   */
  @org.junit.Test
  public void fnFalse22() {
    final XQuery query = new XQuery(
      "let $f := false#0 return 3[$f()]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   *  Evaluation of a "false" function with a logical expression involving the "and" operator. .
   */
  @org.junit.Test
  public void fnFalse3() {
    final XQuery query = new XQuery(
      "fn:false() and fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of a "fn:false" function with a logical expression involving the "or" operator. .
   */
  @org.junit.Test
  public void fnFalse4() {
    final XQuery query = new XQuery(
      "fn:false() or fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of an "fn:false" function with a comparison expression involving the "eq" operator. .
   */
  @org.junit.Test
  public void fnFalse5() {
    final XQuery query = new XQuery(
      "fn:false() eq fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of an "fn:false" function with a comparison expression involving the "ne" operator. .
   */
  @org.junit.Test
  public void fnFalse6() {
    final XQuery query = new XQuery(
      "fn:false() ne fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of an "fn:false" function with a comparison expression involving the "lt" operator. .
   */
  @org.junit.Test
  public void fnFalse7() {
    final XQuery query = new XQuery(
      "fn:false() lt fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of an "fn:false" function with a comparison expression involving the "le" operator. .
   */
  @org.junit.Test
  public void fnFalse8() {
    final XQuery query = new XQuery(
      "fn:false() le fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of an "fn:false" function with a comparison expression involving the "gt" operator. .
   */
  @org.junit.Test
  public void fnFalse9() {
    final XQuery query = new XQuery(
      "fn:false() gt fn:false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }
}
