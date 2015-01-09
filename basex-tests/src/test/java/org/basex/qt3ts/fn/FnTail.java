package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the tail() function.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnTail extends QT3TestSet {

  /**
   *  tail() of a simple sequence .
   */
  @org.junit.Test
  public void tail001() {
    final XQuery query = new XQuery(
      "tail(12 to 15)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("13, 14, 15")
    );
  }

  /**
   *  tail() of a simple sequence .
   */
  @org.junit.Test
  public void tail002() {
    final XQuery query = new XQuery(
      "tail((\"a\", \"b\", \"c\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"b\", \"c\"")
    );
  }

  /**
   *  tail() of a singleton sequence .
   */
  @org.junit.Test
  public void tail003() {
    final XQuery query = new XQuery(
      "count(tail(\"a\"))",
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
   *  tail() of an empty sequence .
   */
  @org.junit.Test
  public void tail004() {
    final XQuery query = new XQuery(
      "let $a := /works/employee return count(tail($a/z))",
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
      assertEq("0")
    );
  }

  /**
   *  tail() of a node sequence .
   */
  @org.junit.Test
  public void tail005() {
    final XQuery query = new XQuery(
      "let $a := /works/employee[@gender='female']/@name return tail($a)/string()",
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
      assertDeepEq("\"Jane Doe 3\", \"Jane Doe 5\", \"Jane Doe 7\", \"Jane Doe 9\", \"Jane Doe 11\", \"Jane Doe 13\"")
    );
  }

  /**
   *  head/tail recursion .
   */
  @org.junit.Test
  public void tail006() {
    final XQuery query = new XQuery(
      "declare function local:sum($n) { if (empty($n)) then 0 else head($n) + local:sum(tail($n)) }; \n" +
      "            local:sum(1 to 5)",
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
        assertEq("15")
      &&
        assertType("xs:integer")
      )
    );
  }
}
