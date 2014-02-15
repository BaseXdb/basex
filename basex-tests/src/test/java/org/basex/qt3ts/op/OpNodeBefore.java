package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the node-before() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpNodeBefore extends QT3TestSet {

  /**
   *  A test whose essence is: `empty(1 << ())`. .
   */
  @org.junit.Test
  public void kNodeBefore1() {
    final XQuery query = new XQuery(
      "empty(1 << ())",
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
   *  A test whose essence is: `<< 1`. .
   */
  @org.junit.Test
  public void kNodeBefore10() {
    final XQuery query = new XQuery(
      "<< 1",
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
   *  A test whose essence is: `<<<`. .
   */
  @org.junit.Test
  public void kNodeBefore11() {
    final XQuery query = new XQuery(
      "<<<",
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
   *  A test whose essence is: `empty(() << 1)`. .
   */
  @org.junit.Test
  public void kNodeBefore2() {
    final XQuery query = new XQuery(
      "empty(() << 1)",
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
   *  A test whose essence is: `1 << 1`. .
   */
  @org.junit.Test
  public void kNodeBefore3() {
    final XQuery query = new XQuery(
      "1 << 1",
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
   *  A test whose essence is: `empty(() << ())`. .
   */
  @org.junit.Test
  public void kNodeBefore4() {
    final XQuery query = new XQuery(
      "empty(() << ())",
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
   *  A test whose essence is: `() <<< ()`. .
   */
  @org.junit.Test
  public void kNodeBefore5() {
    final XQuery query = new XQuery(
      "() <<< ()",
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
   *  A test whose essence is: `() <<`. .
   */
  @org.junit.Test
  public void kNodeBefore6() {
    final XQuery query = new XQuery(
      "() <<",
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
   *  A test whose essence is: `<< ()`. .
   */
  @org.junit.Test
  public void kNodeBefore7() {
    final XQuery query = new XQuery(
      "<< ()",
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
   *  A test whose essence is: `<<`. .
   */
  @org.junit.Test
  public void kNodeBefore8() {
    final XQuery query = new XQuery(
      "<<",
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
   *  A test whose essence is: `1 <<`. .
   */
  @org.junit.Test
  public void kNodeBefore9() {
    final XQuery query = new XQuery(
      "1 <<",
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
   *  test node before operator .
   */
  @org.junit.Test
  public void cbclNodeBefore001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tlet $node := <a> <b/> <c/> </a> \n" +
      "      \treturn not(exactly-one($node/b[1]) << exactly-one($node/c[1]))\n" +
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
   *  test node before operator .
   */
  @org.junit.Test
  public void cbclNodeBefore002() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tlet $node := <a> <b/> <c/> </a> \n" +
      "      \treturn not(not(exactly-one($node/b[1]) << exactly-one($node/c[1])))\n" +
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
   *  A node comparison where one of the operands is not the empty sequence or a single node. .
   */
  @org.junit.Test
  public void nodecomparisonerr2() {
    final XQuery query = new XQuery(
      "fn:count(() << 100)",
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
   *  Test of a ComparisonExpression with the operands set as follows 
   *          operand1 = empty Sequence operator = << operand2 = empty Sequence .
   */
  @org.junit.Test
  public void nodeexpression17() {
    final XQuery query = new XQuery(
      "count(() << ())",
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
   *  Test of a ComparisonExpression with the operands set as follows 
   *          operand1 = empty Sequence operator = << operand2 = Single Element Constructor .
   */
  @org.junit.Test
  public void nodeexpression18() {
    final XQuery query = new XQuery(
      "count(() << <a>50000</a>)",
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
   *  Test of a ComparisonExpression with the operands set as follows 
   *          operand1 = empty Sequence operator = << operand2 = Single Node Element .
   */
  @org.junit.Test
  public void nodeexpression19() {
    final XQuery query = new XQuery(
      "count(() << /works[1]/employee[1]/empnum[1])",
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
   *  Test of a ComparisonExpression with the operands set as follows 
   *          operand1 = empty Sequence operator = << operand2 = Sequence of single Element Node .
   */
  @org.junit.Test
  public void nodeexpression20() {
    final XQuery query = new XQuery(
      "count(() << /staff[1]/employee[1]/empnum[1])",
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
   *  Test of a ComparisonExpression with the operands set as follows 
   *          operand1 = Single Element Constructor operator = << operand2 = empty Sequence .
   */
  @org.junit.Test
  public void nodeexpression21() {
    final XQuery query = new XQuery(
      "count(<a>50000</a> << ())",
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
   *  Test of a ComparisonExpression with the operands set as follows 
   *          operand1 = Single Element Constructor operator = << operand2 = Single Element Constructor .
   */
  @org.junit.Test
  public void nodeexpression22() {
    final XQuery query = new XQuery(
      "<a>50000</a> << <a>50000</a>",
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
        assertBoolean(false)
      )
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows 
   *          operand1 = Single Element Constructor operator = << operand2 = Single Node Element .
   */
  @org.junit.Test
  public void nodeexpression23() {
    final XQuery query = new XQuery(
      "<a>50000</a> << /works[1]/employee[1]/empnum[1]",
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
        assertBoolean(true)
      ||
        assertBoolean(false)
      )
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows 
   *          operand1 = Single Element Constructor operator = << operand2 = Sequence of single Element Node .
   */
  @org.junit.Test
  public void nodeexpression24() {
    final XQuery query = new XQuery(
      "<a>50000</a> << (/staff[1]/employee[1]/empnum[1])",
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
        assertBoolean(true)
      ||
        assertBoolean(false)
      )
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows 
   *          operand1 = Single Node Element operator = << operand2 = empty Sequence .
   */
  @org.junit.Test
  public void nodeexpression25() {
    final XQuery query = new XQuery(
      "count(/works[1]/employee[1]/empnum[1] << ())",
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
   *  Test of a ComparisonExpression with the operands set as follows 
   *          operand1 = Single Node Element operator = << operand2 = Single Element Constructor .
   */
  @org.junit.Test
  public void nodeexpression26() {
    final XQuery query = new XQuery(
      "/works[1]/employee[1]/empnum[1] << <a>50000</a>",
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
        assertBoolean(true)
      ||
        assertBoolean(false)
      )
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows 
   *          operand1 = Single Node Element operator = << operand2 = Single Node Element .
   */
  @org.junit.Test
  public void nodeexpression27() {
    final XQuery query = new XQuery(
      "/works[1]/employee[1]/empnum[1] << /works[1]/employee[1]/empnum[1]",
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
   *  Test of a ComparisonExpression with the operands set as follows 
   *          operand1 = Single Node Element operator = << operand2 = Sequence of single Element Node .
   */
  @org.junit.Test
  public void nodeexpression28() {
    final XQuery query = new XQuery(
      "$works/works[1]/employee[1]/empnum[1] << $staff/staff[1]/employee[1]/empnum[1]",
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
      (
        assertBoolean(true)
      ||
        assertBoolean(false)
      )
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows 
   *          operand1 = Sequence of single Element Node operator = << operand2 = empty Sequence .
   */
  @org.junit.Test
  public void nodeexpression29() {
    final XQuery query = new XQuery(
      "count((/staff[1]/employee[1]/empnum[1]) << ())",
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
   *  Test of a ComparisonExpression with the operands set as follows 
   *          operand1 = Sequence of single Element Node operator = << operand2 = Single Element Constructor .
   */
  @org.junit.Test
  public void nodeexpression30() {
    final XQuery query = new XQuery(
      "(/staff[1]/employee[1]/empnum[1]) << <a>50000</a>",
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
        assertBoolean(true)
      ||
        assertBoolean(false)
      )
    );
  }

  /**
   * Name nodeexpression31  Test of a ComparisonExpression testing the 
   *          rule that states "If any node in a given tree, T1, occurs before any node in a 
   *          different tree, T2, then all nodes in T1 are before all nodes in T2." 
   *          Compare the first node of the first operand against various nodes of the second operand. .
   */
  @org.junit.Test
  public void nodeexpression31() {
    final XQuery query = new XQuery(
      "(($works/works[1]/employee[1]/empnum[1] << $staff/staff[1]/employee[1]/empnum[1]) \n" +
      "         and ($works/works[1]/employee[1]/empnum[1] << $staff/staff[1]/employee[2]/empnum[1]) \n" +
      "         and ($works/works[1]/employee[1]/empnum[1] << $staff/staff[1]/employee[3]/empnum[1])) \n" +
      "         or (($works/works[1]/employee[1]/empnum[1] >> $staff/staff[1]/employee[1]/empnum[1]) \n" +
      "         and ($works/works[1]/employee[1]/empnum[1] >> $staff/staff[1]/employee[2]/empnum[1]) \n" +
      "         and ($works/works[1]/employee[1]/empnum[1] >> $staff/staff[1]/employee[3]/empnum[1]))",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows 
   *          operand1 = Sequence of single Element Node operator = << operand2 = Sequence of single Element Node .
   */
  @org.junit.Test
  public void nodeexpression32() {
    final XQuery query = new XQuery(
      "(/staff[1]/employee[1]/empnum[1]) << (/staff[1]/employee[1]/empnum[1])",
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
   *  Test of a Node before Expression used as part of a boolean-greater-than expression (le operator). .
   */
  @org.junit.Test
  public void nodeexpressionhc10() {
    final XQuery query = new XQuery(
      "((/staff[1]/employee[1]/empnum[1]) << (/staff[1]/employee[1]/empnum[1])) \n" +
      "         le ((/staff[1]/employee[1]/empnum[1]) << (/staff[1]/employee[1]/empnum[1]))",
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
   *  Test of a Node before Expression used as an argument to a "fn:not" function. .
   */
  @org.junit.Test
  public void nodeexpressionhc6() {
    final XQuery query = new XQuery(
      "fn:not((/staff[1]/employee[1]/empnum[1]) << (/staff[1]/employee[1]/empnum[1]))",
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
   *  Test of a Node before Expression used as part of a boolean-less-than expression (lt operator). .
   */
  @org.junit.Test
  public void nodeexpressionhc7() {
    final XQuery query = new XQuery(
      "((/staff[1]/employee[1]/empnum[1]) << (/staff[1]/employee[1]/empnum[1])) \n" +
      "         lt \n" +
      "         ((/staff[1]/employee[1]/empnum[1]) << (/staff[1]/employee[1]/empnum[1]))",
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
   *  Test of a Node before Expression used as part of a boolean-less-than expression (ge operator). .
   */
  @org.junit.Test
  public void nodeexpressionhc8() {
    final XQuery query = new XQuery(
      "((/staff[1]/employee[1]/empnum[1]) << (/staff[1]/employee[1]/empnum[1])) \n" +
      "         ge\n" +
      "         ((/staff[1]/employee[1]/empnum[1]) << (/staff[1]/employee[1]/empnum[1]))",
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
   *  Test of a Node before Expression used as part of a boolean-greater-than expression (gt operator). .
   */
  @org.junit.Test
  public void nodeexpressionhc9() {
    final XQuery query = new XQuery(
      "((/staff[1]/employee[1]/empnum[1]) << (/staff[1]/employee[1]/empnum[1])) \n" +
      "         gt ((/staff[1]/employee[1]/empnum[1]) << (/staff[1]/employee[1]/empnum[1]))",
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
}
