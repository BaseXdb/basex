package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the position() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnPosition extends QT3TestSet {

  /**
   *  A test whose essence is: `position(1)`. .
   */
  @org.junit.Test
  public void kContextPositionFunc1() {
    final XQuery query = new XQuery(
      "position(1)",
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
   *  fn:position() can never return 0('ne'), #2. .
   */
  @org.junit.Test
  public void kContextPositionFunc10() {
    final XQuery query = new XQuery(
      "deep-equal( (1, 2, 3, remove((current-time(), 4), 1)) [0 ne position()], (1, 2, 3, 4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:position() can never return anything less than 1(lt). .
   */
  @org.junit.Test
  public void kContextPositionFunc11() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3, current-time(), current-date(), 6, 7, 8) [position() lt 1])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:position() can never return anything less than 1(<). .
   */
  @org.junit.Test
  public void kContextPositionFunc12() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3, current-time(), current-date(), 6, 7, 8) [position() < 1])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:position() can never return anything less or equal to 0(le). .
   */
  @org.junit.Test
  public void kContextPositionFunc13() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3, current-time(), current-date(), 6, 7, 8) [position() le 0])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:position() can never return anything less or equal to 0(<=). .
   */
  @org.junit.Test
  public void kContextPositionFunc14() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3, current-time(), current-date(), 6, 7, 8) [position() <= 0])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The return value of fn:position() is always greater than 0('ne'). .
   */
  @org.junit.Test
  public void kContextPositionFunc15() {
    final XQuery query = new XQuery(
      "deep-equal( (1, 2, 3, remove((current-time(), 4), 1)) [position() > 0], (1, 2, 3, 4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The return value of fn:position() is always greater than 0('>'). .
   */
  @org.junit.Test
  public void kContextPositionFunc16() {
    final XQuery query = new XQuery(
      "deep-equal( (1, 2, 3, remove((current-time(), 4), 1)) [position() > 0], (1, 2, 3, 4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The return value of fn:position() is always greater than 0('gt'). .
   */
  @org.junit.Test
  public void kContextPositionFunc17() {
    final XQuery query = new XQuery(
      "deep-equal( (1, 2, 3, remove((current-time(), 4), 1)) [position() gt 0], (1, 2, 3, 4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The return value of fn:position() is always greater or equal to 0('>='). .
   */
  @org.junit.Test
  public void kContextPositionFunc18() {
    final XQuery query = new XQuery(
      "deep-equal( (1, 2, 3, remove((current-time(), 4), 1)) [position() >= 1], (1, 2, 3, 4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The return value of fn:position() is always greater or equal to 0('ge'). .
   */
  @org.junit.Test
  public void kContextPositionFunc19() {
    final XQuery query = new XQuery(
      "deep-equal( (1, 2, 3, remove((current-time(), 4), 1)) [position() ge 1], (1, 2, 3, 4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `(1, 2, 3)[if(1) then 1 else position()]`. .
   */
  @org.junit.Test
  public void kContextPositionFunc2() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[if(1) then 1 else position()]",
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
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc20() {
    final XQuery query = new XQuery(
      "1 eq (0, 1, current-time(), 4)[position() = 2] treat as xs:integer",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc21() {
    final XQuery query = new XQuery(
      "1 eq (0, 1, current-time(), 4)[position() eq 2] treat as xs:integer",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc22() {
    final XQuery query = new XQuery(
      "1 eq (0, 1, current-time(), 4)[2 eq position()] treat as xs:integer",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc23() {
    final XQuery query = new XQuery(
      "1 eq (0, 1, current-time(), 4)[2 = position()] treat as xs:integer",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc24() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 4), (1, 2, current-time(), 4)[position() != 3])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc25() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 4), (1, 2, current-time(), 4)[position() ne 3])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc26() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 4), (1, 2, current-time(), 4)[3 ne position()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc27() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 4), (1, 2, current-time(), 4)[3 != position()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc28() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), (1, 2, 3, current-time())[position() lt 4])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc29() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), (1, 2, 3, current-time())[position() < 4])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:position() can never return 0('='). .
   */
  @org.junit.Test
  public void kContextPositionFunc3() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3, current-time(), current-date(), 6, 7, 8) [position() = 0])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc30() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), (1, 2, 3, current-time())[4 gt position()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc31() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), (1, 2, 3, current-time())[4 > position()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc32() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), (1, 2, 3, current-time())[position() le 3])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc33() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), (1, 2, 3, current-time())[position() <= 3])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc34() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), (1, 2, 3, current-time())[3 ge position()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc35() {
    final XQuery query = new XQuery(
      "deep-equal((1, 2, 3), (1, 2, 3, current-time())[3 >= position()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc36() {
    final XQuery query = new XQuery(
      "deep-equal((4, 5), (1, 2, current-time(), 4, 5)[3 lt position()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc37() {
    final XQuery query = new XQuery(
      "deep-equal((4, 5), (1, 2, current-time(), 4, 5)[3 < position()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc38() {
    final XQuery query = new XQuery(
      "deep-equal((4, 5), (1, 2, current-time(), 4, 5)[position() gt 3])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc39() {
    final XQuery query = new XQuery(
      "deep-equal((4, 5), (1, 2, current-time(), 4, 5)[position() > 3])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:position() can never return 0('='), #2. .
   */
  @org.junit.Test
  public void kContextPositionFunc4() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3, current-time(), current-date(), 6, 7, 8) [0 = position()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc40() {
    final XQuery query = new XQuery(
      "deep-equal((4, 5), (1, 2, current-time(), 4, 5)[4 le position()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc41() {
    final XQuery query = new XQuery(
      "deep-equal((4, 5), (1, 2, current-time(), 4, 5)[4 <= position()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc42() {
    final XQuery query = new XQuery(
      "deep-equal((4, 5), (1, 2, current-time(), 4, 5)[position() ge 4])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  position() combined with a comparison operator inside a predicate. .
   */
  @org.junit.Test
  public void kContextPositionFunc43() {
    final XQuery query = new XQuery(
      "deep-equal((4, 5), (1, 2, current-time(), 4, 5)[position() >= 4])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:position() can never return 0('eq'). .
   */
  @org.junit.Test
  public void kContextPositionFunc5() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3, current-time(), current-date(), 6, 7, 8) [position() eq 0])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:position() can never return 0('eq'), #2. .
   */
  @org.junit.Test
  public void kContextPositionFunc6() {
    final XQuery query = new XQuery(
      "empty((1, 2, 3, current-time(), current-date(), 6, 7, 8) [0 eq position()])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:position() can never return 0('!='). .
   */
  @org.junit.Test
  public void kContextPositionFunc7() {
    final XQuery query = new XQuery(
      "deep-equal( (1, 2, 3, remove((current-time(), 4), 1)) [position() != 0], (1, 2, 3, 4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:position() can never return 0('ne'). .
   */
  @org.junit.Test
  public void kContextPositionFunc8() {
    final XQuery query = new XQuery(
      "deep-equal( (1, 2, 3, remove((current-time(), 4), 1)) [position() ne 0], (1, 2, 3, 4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:position() can never return 0('!='), #2. .
   */
  @org.junit.Test
  public void kContextPositionFunc9() {
    final XQuery query = new XQuery(
      "deep-equal( (1, 2, 3, remove((current-time(), 4), 1)) [0 != position()], (1, 2, 3, 4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of "fn"position", where context function is an element node and position is first. .
   */
  @org.junit.Test
  public void position1() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[position() = 1]/string(@name)",
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
      assertStringValue(false, "Jane Doe 1")
    );
  }

  /**
   *  Evaluation of "fn:position" where the position function is used inside the first predicate. .
   */
  @org.junit.Test
  public void position10() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[position() = 1][@name]/string(@name)",
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
      assertStringValue(false, "Jane Doe 1")
    );
  }

  /**
   *  Evaluation of "fn:position" used in conjucntion with the fn:not function .
   */
  @org.junit.Test
  public void position11() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[12]/overtime) return $h/day[not(position() = 1)]/string()",
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
      assertStringValue(false, "Tuesday")
    );
  }

  /**
   *  Evaluation of "fn:position" as a predicate to a wildcard (*). .
   */
  @org.junit.Test
  public void position12() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[12]/overtime) return $h/*[position() = position()]/string()",
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
      assertDeepEq("\"Monday\", \"Tuesday\"")
    );
  }

  /**
   *  Evaluation of "fn:position" used together with an addition operation. .
   */
  @org.junit.Test
  public void position13() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[position() = (2 + 2)]/string(@name)",
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
      assertStringValue(false, "John Doe 4")
    );
  }

  /**
   *  Evaluation of "fn:position" used together with a subtraction operation. .
   */
  @org.junit.Test
  public void position14() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[position() = (5 - 2)]/string(@name)",
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
      assertStringValue(false, "Jane Doe 3")
    );
  }

  /**
   *  Evaluation of "fn:position" used together with a multiplication operation. .
   */
  @org.junit.Test
  public void position15() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[position() = (3 * 2)]/string(@name)",
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
      assertStringValue(false, "John Doe 6")
    );
  }

  /**
   *  Evaluation of "fn:position" used together with a division (div) operation. .
   */
  @org.junit.Test
  public void position16() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[position() = (6 div 2)]/string(@name)",
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
      assertStringValue(false, "Jane Doe 3")
    );
  }

  /**
   *  Evaluation of "fn:position" used together with a division (idiv) operation. .
   */
  @org.junit.Test
  public void position17() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[position() = (6 idiv 2)]/string(@name)",
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
      assertStringValue(false, "Jane Doe 3")
    );
  }

  /**
   *  Evaluation of "fn:position" used together with a logical expression ("and" operator and "true" function). .
   */
  @org.junit.Test
  public void position18() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[12]) return ($h/overtime[position() and fn:true()]/*/string())",
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
      assertDeepEq("\"Monday\", \"Tuesday\"")
    );
  }

  /**
   *  Evaluation of "fn:position" used together with a logical expression ("and" operator and "false" function). .
   */
  @org.junit.Test
  public void position19() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[12]) return ($h/overtime[position() and fn:false()])",
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
      (
        assertEmpty()
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Evaluation of "fn:position", where the context node is not defined. .
   */
  @org.junit.Test
  public void position2() {
    final XQuery query = new XQuery(
      "position()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Evaluation of "fn:position" used together with a logical expression ("or" operator and "true" function). .
   */
  @org.junit.Test
  public void position20() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[12]) return $h/overtime[position() or fn:true()]/*/string()",
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
      assertDeepEq("\"Monday\", \"Tuesday\"")
    );
  }

  /**
   *  Evaluation of "fn:position" used together with a logical expression ("or" operator and "false" function). .
   */
  @org.junit.Test
  public void position21() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[12]) return $h/overtime[position() or fn:false()]/*/string()",
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
      assertDeepEq("\"Monday\", \"Tuesday\"")
    );
  }

  /**
   *  Evaluation of "fn:position" used in the middle of a path expression. .
   */
  @org.junit.Test
  public void position22() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[position() = 12]/overtime/*/string()",
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
      assertDeepEq("\"Monday\", \"Tuesday\"")
    );
  }

  /**
   * Top-level call on position().
   */
  @org.junit.Test
  public void position23() {
    final XQuery query = new XQuery(
      "position()",
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
      assertEq("1")
    );
  }

  /**
   *  Evaluation of "fn"position", where the last item is selected. .
   */
  @org.junit.Test
  public void position3() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[position() = 13]/string(@name)",
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
      assertStringValue(false, "Jane Doe 13")
    );
  }

  /**
   *  Evaluation of "fn"position" together with "fn:last". .
   */
  @org.junit.Test
  public void position4() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[position() = last()]/string(@name)",
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
      assertStringValue(false, "Jane Doe 13")
    );
  }

  /**
   *  Evaluation of "fn:position" together with "fn:last" (format last() = position()). .
   */
  @org.junit.Test
  public void position5() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[last() = position()]/string(@name)",
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
      assertStringValue(false, "Jane Doe 13")
    );
  }

  /**
   *  Evaluation of "fn"position" together with a variable of type integer .
   */
  @org.junit.Test
  public void position6() {
    final XQuery query = new XQuery(
      "for $var in 1 return for $h in (/works) return $h/employee[position() = $var]/string(@name)",
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
      assertStringValue(false, "Jane Doe 1")
    );
  }

  /**
   *  Evaluation of "fn"position" together with a variable of type string casted to integer inside the predicate. .
   */
  @org.junit.Test
  public void position7() {
    final XQuery query = new XQuery(
      "for $var in \"1\" return for $h in (/works) return $h/employee[position() = xs:integer($var)]/string(@name)",
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
      assertStringValue(false, "Jane Doe 1")
    );
  }

  /**
   *  Evaluation of "fn"position" where two position functions are used inside the predicate. .
   */
  @org.junit.Test
  public void position8() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[12]) return $h/overtime[position() = position()]/day/string()",
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
      assertDeepEq("\"Monday\", \"Tuesday\"")
    );
  }

  /**
   *  Evaluation of "fn:position" where the position function is used inside a second predicate. .
   */
  @org.junit.Test
  public void position9() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[@name][position() = 1]/string(@name)",
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
      assertStringValue(false, "Jane Doe 1")
    );
  }
}
