package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the is-same-node() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpIsSameNode extends QT3TestSet {

  /**
   *  A test whose essence is: `empty(1 is ())`. .
   */
  @org.junit.Test
  public void kNodeSame1() {
    final XQuery query = new XQuery(
      "empty(1 is ())",
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
        error("XPTY0004")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  A test whose essence is: `empty(() is 1)`. .
   */
  @org.junit.Test
  public void kNodeSame2() {
    final XQuery query = new XQuery(
      "empty(() is 1)",
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
        error("XPTY0004")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  A test whose essence is: `1 is 1`. .
   */
  @org.junit.Test
  public void kNodeSame3() {
    final XQuery query = new XQuery(
      "1 is 1",
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
   *  A test whose essence is: `empty(() is ())`. .
   */
  @org.junit.Test
  public void kNodeSame4() {
    final XQuery query = new XQuery(
      "empty(() is ())",
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
   *  A syntactically invalid expression. .
   */
  @org.junit.Test
  public void kNodeSame5() {
    final XQuery query = new XQuery(
      "() is",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  A query reminding of an invokation of 'is' operator. However, this is not a parse error(XPST0003), because it is a valid function call, although to a non-existant function. 'is' is not a reserved function name. .
   */
  @org.junit.Test
  public void kNodeSame6() {
    final XQuery query = new XQuery(
      "is ()",
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
   *  Ensure node identity is is correct for variables and element constructors. .
   */
  @org.junit.Test
  public void k2NodeSame1() {
    final XQuery query = new XQuery(
      "declare variable $var := <elem/>; <a>{$var}</a>/elem[1] is $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Ensure node identity is is correct between variable references. .
   */
  @org.junit.Test
  public void k2NodeSame2() {
    final XQuery query = new XQuery(
      "declare variable $var := <elem/>; $var is $var",
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
   *  Ensure node identity is is correct for variables and element constructors(#2). .
   */
  @org.junit.Test
  public void k2NodeSame3() {
    final XQuery query = new XQuery(
      "declare variable $var := <elem/>; not($var is <elem/>)",
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
   *  Ensure node identity of nodes used with node constructors. .
   */
  @org.junit.Test
  public void k2NodeSame4() {
    final XQuery query = new XQuery(
      "declare variable $e := attribute name {()}; $e is $e, <is/> is <is/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true false")
    );
  }

  /**
   *  exactly-one() to is, that fails. Inferrence may conclude that it will always evaluate to false, so that is valid as well. .
   */
  @org.junit.Test
  public void k2NodeSame5() {
    final XQuery query = new XQuery(
      "empty(exactly-one(<e/>/*) is exactly-one(<e/>/*))",
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
        error("FORG0005")
      )
    );
  }

  /**
   *  Compare two empty sequences, that are tricky to infer at compile time. .
   */
  @org.junit.Test
  public void k2NodeSame6() {
    final XQuery query = new XQuery(
      "empty(zero-or-one(<e/>/*) is zero-or-one(<e/>/*))",
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
   *  Check node identity for return values of creative user defined functions. .
   */
  @org.junit.Test
  public void cbclIsSameNode001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:f() as node() { <a/> }; \n" +
      "      \tlocal:f() is local:f()\n" +
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
      assertBoolean(false)
    );
  }

  /**
   *  test is same node operator .
   */
  @org.junit.Test
  public void cbclNodeSame001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tlet $node := <a> <b/> <c/> </a> \n" +
      "      \treturn not(exactly-one($node/b[1]) is exactly-one($node/c[1]))\n" +
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
      assertBoolean(true)
    );
  }

  /**
   *  test is same node operator .
   */
  @org.junit.Test
  public void cbclNodeSame002() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tlet $node := <a> <b/> <c/> </a> \n" +
      "      \treturn not(not(exactly-one($node/b[1]) is exactly-one($node/c[1])))\n" +
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
      assertBoolean(false)
    );
  }

  /**
   *  A node comparison where one of the operands is not the empty sequence or a single node. .
   */
  @org.junit.Test
  public void nodecomparisonerr1() {
    final XQuery query = new XQuery(
      "fn:count(() is 100)",
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
        assertEq("0")
      ||
        error("XPTY0004")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = empty Sequence operator = is operand2 = empty Sequence .
   */
  @org.junit.Test
  public void nodeexpression1() {
    final XQuery query = new XQuery(
      "count(() is ())",
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
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Single Node Element operator = is operand2 = Single Element Constructor .
   */
  @org.junit.Test
  public void nodeexpression10() {
    final XQuery query = new XQuery(
      "/works[1]/employee[1]/empnum[1] is <a>50000</a>",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Single Node Element operator = is operand2 = Single Node Element .
   */
  @org.junit.Test
  public void nodeexpression11() {
    final XQuery query = new XQuery(
      "/works[1]/employee[1]/empnum[1] is /works[1]/employee[1]/empnum[1]",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
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
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Single Node Element operator = is operand2 = Sequence of single Element Node .
   */
  @org.junit.Test
  public void nodeexpression12() {
    final XQuery query = new XQuery(
      "\n" +
      "         $works/works[1]/employee[1]/empnum[1] is $staff/staff[1]/employee[1]/empnum[1]",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Sequence of single Element Node operator = is operand2 = empty Sequence .
   */
  @org.junit.Test
  public void nodeexpression13() {
    final XQuery query = new XQuery(
      "count((/staff[1]/employee[1]/empnum[1]) is ())",
      ctx);
    try {
      query.context(node(file("docs/staff.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Sequence of single Element Node operator = is operand2 = Single Element Constructor .
   */
  @org.junit.Test
  public void nodeexpression14() {
    final XQuery query = new XQuery(
      "(/staff[1]/employee[1]/empnum[1]) is <a>50000</a>",
      ctx);
    try {
      query.context(node(file("docs/staff.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Sequence of single Element Node operator = is operand2 = Single Node Element .
   */
  @org.junit.Test
  public void nodeexpression15() {
    final XQuery query = new XQuery(
      "\n" +
      "          ($staff/staff[1]/employee[1]/empnum[1]) is $works/works[1]/employee[1]/empnum[1]",
      ctx);
    try {
      query.bind("$works", node(file("docs/works.xml")));
      query.bind("$staff", node(file("docs/staff.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Sequence of single Element Node operator = is operand2 = Sequence of single Element Node .
   */
  @org.junit.Test
  public void nodeexpression16() {
    final XQuery query = new XQuery(
      "(/staff[1]/employee[1]/empnum[1]) is (/staff[1]/employee[1]/empnum[1])",
      ctx);
    try {
      query.context(node(file("docs/staff.xml")));
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
   *  Test of a ComparisonExpression with the operands set as follows operand1 = empty Sequence operator = is operand2 = Single Element Constructor .
   */
  @org.junit.Test
  public void nodeexpression2() {
    final XQuery query = new XQuery(
      "count(() is <a>50000</a>)",
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
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = empty Sequence operator = is operand2 = Single Node Element .
   */
  @org.junit.Test
  public void nodeexpression3() {
    final XQuery query = new XQuery(
      "count(() is /works[1]/employee[1]/empnum[1])",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = empty Sequence operator = is operand2 = Sequence of single Element Node .
   */
  @org.junit.Test
  public void nodeexpression4() {
    final XQuery query = new XQuery(
      "count(() is (/staff[1]/employee[1]/empnum[1]))",
      ctx);
    try {
      query.context(node(file("docs/staff.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Single Element Constructor operator = is operand2 = empty Sequence .
   */
  @org.junit.Test
  public void nodeexpression5() {
    final XQuery query = new XQuery(
      "count(<a>50000</a> is ())",
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
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Single Element Constructor operator = is operand2 = Single Element Constructor .
   */
  @org.junit.Test
  public void nodeexpression6() {
    final XQuery query = new XQuery(
      "<a>50000</a> is <a>50000</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Single Element Constructor operator = is operand2 = Single Node Element .
   */
  @org.junit.Test
  public void nodeexpression7() {
    final XQuery query = new XQuery(
      "<a>50000</a> is /works[1]/employee[1]/empnum[1]",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Single Element Constructor operator = is operand2 = Sequence of single Element Node .
   */
  @org.junit.Test
  public void nodeexpression8() {
    final XQuery query = new XQuery(
      "<a>50000</a> is (/staff[1]/employee[1]/empnum[1])",
      ctx);
    try {
      query.context(node(file("docs/staff.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Single Node Element operator = is operand2 = empty Sequence .
   */
  @org.junit.Test
  public void nodeexpression9() {
    final XQuery query = new XQuery(
      "count(/works[1]/employee[1]/empnum[1] is ())",
      ctx);
    try {
      query.context(node(file("docs/works.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Test of a Node Expression used as an argument to the fn not function .
   */
  @org.junit.Test
  public void nodeexpressionhc1() {
    final XQuery query = new XQuery(
      "fn:not((/staff[1]/employee[1]/empnum[1]) is (/staff[1]/employee[1]/empnum[1]))",
      ctx);
    try {
      query.context(node(file("docs/staff.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a Node Expression used as part of a boolean-less-than expression (lt operator). .
   */
  @org.junit.Test
  public void nodeexpressionhc2() {
    final XQuery query = new XQuery(
      "((/staff[1]/employee[1]/empnum[1]) is (/staff[1]/employee[1]/empnum[1])) lt ((/staff[1]/employee[1]/empnum[1]) is (/staff[1]/employee[1]/empnum[1]))",
      ctx);
    try {
      query.context(node(file("docs/staff.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a Node Expression used as part of a boolean less than expression (ge) operator .
   */
  @org.junit.Test
  public void nodeexpressionhc3() {
    final XQuery query = new XQuery(
      "((/staff[1]/employee[1]/empnum[1]) is (/staff[1]/employee[1]/empnum[1])) ge ((/staff[1]/employee[1]/empnum[1]) is (/staff[1]/employee[1]/empnum[1]))",
      ctx);
    try {
      query.context(node(file("docs/staff.xml")));
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
   *  Test of a Node Expression used as part of a boolean greater than expression (gt) operator. .
   */
  @org.junit.Test
  public void nodeexpressionhc4() {
    final XQuery query = new XQuery(
      "((/staff[1]/employee[1]/empnum[1]) is (/staff[1]/employee[1]/empnum[1])) gt ((/staff[1]/employee[1]/empnum[1]) is (/staff[1]/employee[1]/empnum[1]))",
      ctx);
    try {
      query.context(node(file("docs/staff.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a Node Expression used as part of a boolean greater than expression (le) operator .
   */
  @org.junit.Test
  public void nodeexpressionhc5() {
    final XQuery query = new XQuery(
      "((/staff[1]/employee[1]/empnum[1]) is (/staff[1]/employee[1]/empnum[1])) le ((/staff[1]/employee[1]/empnum[1]) is (/staff[1]/employee[1]/empnum[1]))",
      ctx);
    try {
      query.context(node(file("docs/staff.xml")));
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
}
