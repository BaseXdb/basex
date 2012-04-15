package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the last() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnLast extends QT3TestSet {

  /**
   *  A test whose essence is: `last(1)`. .
   */
  @org.junit.Test
  public void kContextLastFunc1() {
    final XQuery query = new XQuery(
      "last(1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  fn:last() can never return 0('eq'), #2. .
   */
  @org.junit.Test
  public void kContextLastFunc10() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3, current-time(), current-date(), 6, 7, 8) [0 eq last()])",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  fn:last() can never return 0('!='). .
   */
  @org.junit.Test
  public void kContextLastFunc11() {
    final XQuery query = new XQuery(
      "deep-equal( (1, 2, 3, remove((current-time(), 4), 1)) [last() != 0], (1, 2, 3, 4))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:last() can never return 0('ne'). .
   */
  @org.junit.Test
  public void kContextLastFunc12() {
    final XQuery query = new XQuery(
      "deep-equal( (1, 2, 3, remove((current-time(), 4), 1)) [last() ne 0], (1, 2, 3, 4))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:last() can never return 0('!='). .
   */
  @org.junit.Test
  public void kContextLastFunc13() {
    final XQuery query = new XQuery(
      "deep-equal( (1, 2, 3, remove((current-time(), 4), 1)) [0 != last()], (1, 2, 3, 4))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:last() can never return 0('ne'), #2. .
   */
  @org.junit.Test
  public void kContextLastFunc14() {
    final XQuery query = new XQuery(
      "deep-equal( (1, 2, 3, remove((current-time(), 4), 1)) [0 ne last()], (1, 2, 3, 4))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:last() can never return anything less than 1(lt). .
   */
  @org.junit.Test
  public void kContextLastFunc15() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3, current-time(), current-date(), 6, 7, 8) [last() lt 1])",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  fn:last() can never return anything less than 1(<). .
   */
  @org.junit.Test
  public void kContextLastFunc16() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3, current-time(), current-date(), 6, 7, 8) [last() < 1])",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  fn:last() can never return anything less or equal to 0(le). .
   */
  @org.junit.Test
  public void kContextLastFunc17() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3, current-time(), current-date(), 6, 7, 8) [last() le 0])",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  fn:last() can never return anything less or equal to 0(<=). .
   */
  @org.junit.Test
  public void kContextLastFunc18() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3, current-time(), current-date(), 6, 7, 8) [last() <= 0])",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  The return value of fn:last() is always greater than 0('ne'). .
   */
  @org.junit.Test
  public void kContextLastFunc19() {
    final XQuery query = new XQuery(
      "deep-equal( (1, 2, 3, remove((current-time(), 4), 1)) [last() > 0], (1, 2, 3, 4))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `(1, 2, 3)[if(1) then 1 else last()]`. .
   */
  @org.junit.Test
  public void kContextLastFunc2() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[if(1) then 1 else last()]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  The return value of fn:last() is always greater than 0('>'). .
   */
  @org.junit.Test
  public void kContextLastFunc20() {
    final XQuery query = new XQuery(
      "deep-equal( (1, 2, 3, remove((current-time(), 4), 1)) [last() > 0], (1, 2, 3, 4))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The return value of fn:last() is always greater than 0('gt'). .
   */
  @org.junit.Test
  public void kContextLastFunc21() {
    final XQuery query = new XQuery(
      "deep-equal( (1, 2, 3, remove((current-time(), 4), 1)) [last() gt 0], (1, 2, 3, 4))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The return value of fn:last() is always greater or equal to 0('>='). .
   */
  @org.junit.Test
  public void kContextLastFunc22() {
    final XQuery query = new XQuery(
      "deep-equal( (1, 2, 3, remove((current-time(), 4), 1)) [last() >= 1], (1, 2, 3, 4))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The return value of fn:last() is always greater or equal to 0('ge'). .
   */
  @org.junit.Test
  public void kContextLastFunc23() {
    final XQuery query = new XQuery(
      "deep-equal( (1, 2, 3, remove((current-time(), 4), 1)) [last() ge 1], (1, 2, 3, 4))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:last() inside a predicate combined with a offset. .
   */
  @org.junit.Test
  public void kContextLastFunc24() {
    final XQuery query = new XQuery(
      "(1, 2, 3, 4, current-time(), 4, 5, 6)[last() - 2] treat as xs:integer eq 4",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:last() inside a predicate combined with a offset. .
   */
  @org.junit.Test
  public void kContextLastFunc25() {
    final XQuery query = new XQuery(
      "(1, 2, 3, 4, current-time(), 4, 5, 6)[last() - 1] treat as xs:integer eq 5",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:last() inside a predicate. .
   */
  @org.junit.Test
  public void kContextLastFunc26() {
    final XQuery query = new XQuery(
      "(1, 2, 3, 4, current-time(), 4, 5, 6)[last()] treat as xs:integer eq 6",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:last() inside a predicate and an insignificant offset. .
   */
  @org.junit.Test
  public void kContextLastFunc27() {
    final XQuery query = new XQuery(
      "(1, 2, 3, 4, current-time(), 4, 5, 6)[last() - 0] treat as xs:integer eq 6",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:last() inside a predicate combined with a range expression. .
   */
  @org.junit.Test
  public void kContextLastFunc28() {
    final XQuery query = new XQuery(
      "(1 to 6)[last()] eq 6",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:last() inside a predicate combined with a range expression and offset. .
   */
  @org.junit.Test
  public void kContextLastFunc29() {
    final XQuery query = new XQuery(
      "(-20 to -5)[last() - 3]",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("-8")
    );
  }

  /**
   *  A test whose essence is: `deep-equal((1, 2, 3)[count((last(), last())) eq 2], (1, 2, 3))`. .
   */
  @org.junit.Test
  public void kContextLastFunc3() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3)[count((last(), last())) eq 2], (1, 2, 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `deep-equal((1, 2, 3)[last() eq last()], (1, 2, 3))`. .
   */
  @org.junit.Test
  public void kContextLastFunc4() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3)[last() eq last()], (1, 2, 3))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `(1, 2, 3, current-time())[last() - 1] treat as xs:integer eq 3`. .
   */
  @org.junit.Test
  public void kContextLastFunc5() {
    final XQuery query = new XQuery(
      "(1, 2, 3, current-time())[last() - 1] treat as xs:integer eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty((1, 2, 3, current-time())[last() + 1])`. .
   */
  @org.junit.Test
  public void kContextLastFunc6() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3, current-time())[last() + 1])",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:last() can never return 0('='). .
   */
  @org.junit.Test
  public void kContextLastFunc7() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3, current-time(), current-date(), 6, 7, 8) [last() = 0])",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  fn:last() can never return 0('='), #2. .
   */
  @org.junit.Test
  public void kContextLastFunc8() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3, current-time(), current-date(), 6, 7, 8) [0 = last()])",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  fn:last() can never return 0('eq'). .
   */
  @org.junit.Test
  public void kContextLastFunc9() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3, current-time(), current-date(), 6, 7, 8) [last() eq 0])",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertBoolean(true)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Evaluation of "fn:last", where context function is an element node and the position is last. .
   */
  @org.junit.Test
  public void last1() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h//employee[last()]/string(@name)",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Jane Doe 13")
    );
  }

  /**
   *  Evaluation of "fn:last" where the last function is used inside the first predicate. .
   */
  @org.junit.Test
  public void last10() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h//employee[last() = 13][@name = \"Jane Doe 13\"]/string(@name)",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Jane Doe 13")
    );
  }

  /**
   *  Evaluation of "fn:position" used in conjunction with the fn:not function .
   */
  @org.junit.Test
  public void last11() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[12]/overtime) return $h/day[not(position() = 1)]",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Tuesday")
    );
  }

  /**
   *  Evaluation of "fn:position" as a predicate to a wildcard (*). .
   */
  @org.junit.Test
  public void last12() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[12]/overtime) return $h/*[position() = position()]/string()",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"Monday\", \"Tuesday\"")
    );
  }

  /**
   *  Evaluation of "fn:position" used together with an addition operation. .
   */
  @org.junit.Test
  public void last13() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[position() = (2 + 2)]/string(@name)",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "John Doe 4")
    );
  }

  /**
   *  Evaluation of "fn:position" used together with a subtraction operation. .
   */
  @org.junit.Test
  public void last14() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[position() = (5 - 2)]/string(@name)",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Jane Doe 3")
    );
  }

  /**
   *  Evaluation of "fn:position" used together with a multiplication operation. .
   */
  @org.junit.Test
  public void last15() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[position() = (3 * 2)]/string(@name)",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "John Doe 6")
    );
  }

  /**
   *  Evaluation of "fn:position" used together with a division (div) operation. .
   */
  @org.junit.Test
  public void last16() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[position() = (6 div 2)]/string(@name)",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Jane Doe 3")
    );
  }

  /**
   *  Evaluation of "fn:position" used together with a division (idiv) operation. .
   */
  @org.junit.Test
  public void last17() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[position() = (6 idiv 2)]/string(@name)",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Jane Doe 3")
    );
  }

  /**
   *  Evaluation of "fn:position" used together with a logical expression ("and" operator and "true" function). .
   */
  @org.junit.Test
  public void last18() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[12]) return ($h/overtime[position() and fn:true()]/*/string())",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"Monday\", \"Tuesday\"")
    );
  }

  /**
   *  Evaluation of "fn:position" used together with a logical expression ("and" operator and "false" function). .
   */
  @org.junit.Test
  public void last19() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[12]) return $h/overtime[position() and fn:false()]",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEmpty()
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Evaluation of "fn:last", where the context node is not defined. .
   */
  @org.junit.Test
  public void last2() {
    final XQuery query = new XQuery(
      "last()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Evaluation of "fn:position" used together with a logical expression ("or" operator and "true" function). .
   */
  @org.junit.Test
  public void last20() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[12]) return $h/overtime[position() or fn:true()]/*/string()",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"Monday\", \"Tuesday\"")
    );
  }

  /**
   *  Evaluation of "fn:position" used together with a logical expression ("or" operator and "false" function). .
   */
  @org.junit.Test
  public void last21() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[12]) return $h/overtime[position() or fn:false()]/*/string()",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"Monday\", \"Tuesday\"")
    );
  }

  /**
   *  Evaluation of "fn:position" used in the middle of a path expression. .
   */
  @org.junit.Test
  public void last22() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[position() = 12]/overtime/*/string()",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"Monday\", \"Tuesday\"")
    );
  }

  /**
   *  Evaluation of "last()" within a positional predicate .
   */
  @org.junit.Test
  public void last23() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[position() = 5 to last()]/@name/string()",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"Jane Doe 5\", \"John Doe 6\", \"Jane Doe 7\", \"John Doe 8\", \n            \"Jane Doe 9\", \"John Doe 10\", \"Jane Doe 11\", \"John Doe 12\", \"Jane Doe 13\"")
    );
  }

  /**
   * Top-level call on last().
   */
  @org.junit.Test
  public void last24() {
    final XQuery query = new XQuery(
      "last()",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluation of "fn:last", where the first item is selected. .
   */
  @org.junit.Test
  public void last3() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[12]) return $h/overtime[last() = 1]/*/string()",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"Monday\", \"Tuesday\"")
    );
  }

  /**
   *  Evaluation of "fn:last" together with "fn:position". .
   */
  @org.junit.Test
  public void last4() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[position() = last()]/string(@name)",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Jane Doe 13")
    );
  }

  /**
   *  Evaluation of "fn:last" together with "fn:position" (format last() = position()). .
   */
  @org.junit.Test
  public void last5() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[last() = position()]/string(@name)",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Jane Doe 13")
    );
  }

  /**
   *  Evaluation of "fn:last" together with the "<<" node operator .
   */
  @org.junit.Test
  public void last6() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[last()] << $h/employee[last()]",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of "fn:last" together with the "is" node operator casted to integer inside the predicate. .
   */
  @org.junit.Test
  public void last7() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[last()] is $h/employee[last()]",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of "fn:last" where two last functions are used inside the predicate. .
   */
  @org.junit.Test
  public void last8() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[12]) return $h/overtime[last() = last()]/*/string()",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("\"Monday\", \"Tuesday\"")
    );
  }

  /**
   *  Evaluation of "fn:last" where the last function is used inside a second predicate. .
   */
  @org.junit.Test
  public void last9() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h//employee[@name=\"Jane Doe 13\"][last() = 1]/string(@name)",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Jane Doe 13")
    );
  }
}
