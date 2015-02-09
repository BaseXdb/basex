package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the "!" simple mapping operator (new in XPath 3.0)..
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpBang extends QT3TestSet {

  /**
   * Simple  mapping of atomic sequence.
   */
  @org.junit.Test
  public void bang1() {
    final XQuery query = new XQuery(
      "(1 to 10)!(.*.)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 4, 9, 16, 25, 36, 49, 64, 81, 100")
    );
  }

  /**
   * rhs of "!" operator returning a sequence.
   */
  @org.junit.Test
  public void bang10() {
    final XQuery query = new XQuery(
      "(1 to 5) ! (1 to .)",
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
      assertDeepEq("1, 1,2, 1,2,3, 1,2,3,4, 1,2,3,4,5")
    );
  }

  /**
   * "!" is not associative when position() is used.
   */
  @org.junit.Test
  public void bang11() {
    final XQuery query = new XQuery(
      "(1 to 5) ! ((1 to .) ! position())",
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
      assertDeepEq("1, 1,2, 1,2,3, 1,2,3,4, 1,2,3,4,5")
    );
  }

  /**
   * "!" is not associative when position() is used.
   */
  @org.junit.Test
  public void bang12() {
    final XQuery query = new XQuery(
      "(1 to 5) ! (1 to .) ! position()",
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
      assertDeepEq("1 to 15")
    );
  }

  /**
   * backwards-axes semantics of [position()] work with "!" operator.
   */
  @org.junit.Test
  public void bang13() {
    final XQuery query = new XQuery(
      "/ works ! employee[4] ! preceding-sibling::*[1] ! string(@name) ",
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
      assertEq("\"Jane Doe 3\"")
    );
  }

  /**
   * Interaction of "!" with "/".
   */
  @org.junit.Test
  public void bang14() {
    final XQuery query = new XQuery(
      "count(/ ! works)",
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
   * Simple  mapping of atomic sequence.
   */
  @org.junit.Test
  public void bang2() {
    final XQuery query = new XQuery(
      "(\"red\", \"blue\", \"green\")!string-length()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("3, 4, 5")
    );
  }

  /**
   * Simple  mapping with position() function.
   */
  @org.junit.Test
  public void bang3() {
    final XQuery query = new XQuery(
      "(\"red\", \"blue\", \"green\")!position()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 2, 3")
    );
  }

  /**
   * Simple  mapping with last() function.
   */
  @org.junit.Test
  public void bang4() {
    final XQuery query = new XQuery(
      "(\"red\", \"blue\", \"green\")!(position() = last())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("false(), false(), true()")
    );
  }

  /**
   * Simple mapping path.
   */
  @org.junit.Test
  public void bang5() {
    final XQuery query = new XQuery(
      "(\"red\", \"blue\", \"green\") ! string-length() ! (.+1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("4, 5, 6")
    );
  }

  /**
   * Simple mapping with nodes: no sorting into document order or deduplication.
   */
  @org.junit.Test
  public void bang6() {
    final XQuery query = new XQuery(
      "(/works/employee[2], /works/employee[1], /works/employee[2]) ! @name ! string()",
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
      assertDeepEq("\"John Doe 2\", \"Jane Doe 1\", \"John Doe 2\"")
    );
  }

  /**
   * Precedence of "!" is less than "[]".
   */
  @org.junit.Test
  public void bang7() {
    final XQuery query = new XQuery(
      "/ works ! employee[2] ! hours[2] ! number()",
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
      assertEq("20")
    );
  }

  /**
   * Precedence of "!" is greater than "+".
   */
  @org.junit.Test
  public void bang8() {
    final XQuery query = new XQuery(
      "2 + (/ works ! employee[2] ! hours[2]) ! number() ! (-.)",
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
      assertEq("-18")
    );
  }

  /**
   * Precedence of "!" is greater than unary "-".
   */
  @org.junit.Test
  public void bang9() {
    final XQuery query = new XQuery(
      "-2!(.+1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-3")
    );
  }
}
