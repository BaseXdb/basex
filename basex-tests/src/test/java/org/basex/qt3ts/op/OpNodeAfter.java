package org.basex.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the node-after() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpNodeAfter extends QT3TestSet {

  /**
   *  A test whose essence is: `empty(1 >> ())`. .
   */
  @org.junit.Test
  public void kNodeAfter1() {
    final XQuery query = new XQuery(
      "empty(1 >> ())",
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
   *  A test whose essence is: `>> 1`. .
   */
  @org.junit.Test
  public void kNodeAfter10() {
    final XQuery query = new XQuery(
      ">> 1",
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
   *  A test whose essence is: `>>>`. .
   */
  @org.junit.Test
  public void kNodeAfter11() {
    final XQuery query = new XQuery(
      ">>>",
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
   *  A test whose essence is: `empty(() >> 1)`. .
   */
  @org.junit.Test
  public void kNodeAfter2() {
    final XQuery query = new XQuery(
      "empty(() >> 1)",
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
   *  A test whose essence is: `1 >> 1`. .
   */
  @org.junit.Test
  public void kNodeAfter3() {
    final XQuery query = new XQuery(
      "1 >> 1",
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
   *  A test whose essence is: `empty(() >> ())`. .
   */
  @org.junit.Test
  public void kNodeAfter4() {
    final XQuery query = new XQuery(
      "empty(() >> ())",
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
   *  A test whose essence is: `() >>> ()`. .
   */
  @org.junit.Test
  public void kNodeAfter5() {
    final XQuery query = new XQuery(
      "() >>> ()",
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
   *  A test whose essence is: `() >>`. .
   */
  @org.junit.Test
  public void kNodeAfter6() {
    final XQuery query = new XQuery(
      "() >>",
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
   *  A test whose essence is: `>> ()`. .
   */
  @org.junit.Test
  public void kNodeAfter7() {
    final XQuery query = new XQuery(
      ">> ()",
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
   *  A test whose essence is: `>>`. .
   */
  @org.junit.Test
  public void kNodeAfter8() {
    final XQuery query = new XQuery(
      ">>",
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
   *  A test whose essence is: `1 >>`. .
   */
  @org.junit.Test
  public void kNodeAfter9() {
    final XQuery query = new XQuery(
      "1 >>",
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
   *  test node after operator .
   */
  @org.junit.Test
  public void cbclNodeAfter001() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tlet $node := <a> <b/> <c/> </a> \n" +
      "      \treturn not(exactly-one($node/b[1]) >> exactly-one($node/c[1]))\n" +
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
   *  test node after operator .
   */
  @org.junit.Test
  public void cbclNodeAfter002() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tlet $node := <a> <b/> <c/> </a> \n" +
      "      \treturn not(not(exactly-one($node/b[1]) >> exactly-one($node/c[1])))\n" +
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
  public void nodecomparisonerr3() {
    final XQuery query = new XQuery(
      "fn:count(() >> 100)",
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
   *  Test of a ComparisonExpression with the operands set as follows operand1 = empty Sequence operator = >> operand2 = empty Sequence .
   */
  @org.junit.Test
  public void nodeexpression33() {
    final XQuery query = new XQuery(
      "count(() >> ())",
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
   *  Test of a ComparisonExpression with the operands set as follows operand1 = empty Sequence operator = >> operand2 = Single Element Constructor .
   */
  @org.junit.Test
  public void nodeexpression34() {
    final XQuery query = new XQuery(
      "count(() >> <a>50000</a>)",
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
   *  Test of a ComparisonExpression with the operands set as follows operand1 = empty Sequence operator = >> operand2 = Single Node Element .
   */
  @org.junit.Test
  public void nodeexpression35() {
    final XQuery query = new XQuery(
      "count(() >> /works[1]/employee[1]/empnum[1])",
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
   *  Test of a ComparisonExpression with the operands set as follows operand1 = empty Sequence operator = >> operand2 = Sequence of single Element Node .
   */
  @org.junit.Test
  public void nodeexpression36() {
    final XQuery query = new XQuery(
      "count(() >> (/staff[1]/employee[1]/empnum[1]))",
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
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Single Element Constructor operator = >> operand2 = empty Sequence .
   */
  @org.junit.Test
  public void nodeexpression37() {
    final XQuery query = new XQuery(
      "count(<a>50000</a> >> ())",
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
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Single Element Constructor operator = >> operand2 = Single Element Constructor .
   */
  @org.junit.Test
  public void nodeexpression38() {
    final XQuery query = new XQuery(
      "<a>50000</a> >> <a>50000</a>",
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
        assertBoolean(true)
      )
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Single Element Constructor operator = >> operand2 = Single Node Element .
   */
  @org.junit.Test
  public void nodeexpression39() {
    final XQuery query = new XQuery(
      "<a>50000</a> >> /works[1]/employee[1]/empnum[1]",
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
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Single Element Constructor operator = >> operand2 = Sequence of single Element Node .
   */
  @org.junit.Test
  public void nodeexpression40() {
    final XQuery query = new XQuery(
      "<a>50000</a> >> (/staff[1]/employee[1]/empnum[1])",
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
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Single Node Element operator = >> operand2 = empty Sequence .
   */
  @org.junit.Test
  public void nodeexpression41() {
    final XQuery query = new XQuery(
      "count(/works[1]/employee[1]/empnum[1] >> ())",
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
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Single Node Element operator = >> operand2 = Single Element Constructor .
   */
  @org.junit.Test
  public void nodeexpression42() {
    final XQuery query = new XQuery(
      "/works[1]/employee[1]/empnum[1] >> <a>50000</a>",
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
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Single Node Element operator = >> operand2 = Single Node Element .
   */
  @org.junit.Test
  public void nodeexpression43() {
    final XQuery query = new XQuery(
      "/works[1]/employee[1]/empnum[1] >> /works[1]/employee[1]/empnum[1]",
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
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Single Node Element operator = >> operand2 = Sequence of single Element Node .
   */
  @org.junit.Test
  public void nodeexpression44() {
    final XQuery query = new XQuery(
      "$works/works[1]/employee[1]/empnum[1] >> ($staff/staff[1]/employee[1]/empnum[1])",
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
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Sequence of single Element Node operator = >> operand2 = empty Sequence .
   */
  @org.junit.Test
  public void nodeexpression45() {
    final XQuery query = new XQuery(
      "count((/staff[1]/employee[1]/empnum[1]) >> ())",
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
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Sequence of single Element Node operator = >> operand2 = Single Element Constructor .
   */
  @org.junit.Test
  public void nodeexpression46() {
    final XQuery query = new XQuery(
      "(/staff[1]/employee[1]/empnum[1]) >> <a>50000</a>",
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
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   * Name nodeexpression47  Test of a ComparisonExpression testing the rule that states "If any node in a given tree, T1, occurs before any node in a different tree, T2, then all nodes in T1 are before all nodes in T2." Compare various nodes of the first operand against various nodes of the second operand. .
   */
  @org.junit.Test
  public void nodeexpression47() {
    final XQuery query = new XQuery(
      "(($works/works[1]/employee[1]/empnum[1] >> $staff/staff[1]/employee[1]/empnum[1]) \n" +
      "         and ($works/works[1]/employee[2]/empnum[1] >> $staff/staff[1]/employee[2]/empnum[1]) \n" +
      "         and ($works/works[1]/employee[3]/empnum[1] >> $staff/staff[1]/employee[3]/empnum[1])) \n" +
      "         or (($works/works[1]/employee[1]/empnum[1] << $staff/staff[1]/employee[1]/empnum[1]) \n" +
      "         and ($works/works[1]/employee[2]/empnum[1] << $staff/staff[1]/employee[2]/empnum[1]) \n" +
      "         and ($works/works[1]/employee[3]/empnum[1] << $staff/staff[1]/employee[3]/empnum[1]))",
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
   *  Test of a ComparisonExpression with the operands set as follows operand1 = Sequence of single Element Node operator = >> operand2 = Sequence of single Element Node .
   */
  @org.junit.Test
  public void nodeexpression48() {
    final XQuery query = new XQuery(
      "(/staff[1]/employee[1]/empnum[1]) >> (/staff[1]/employee[1]/empnum[1])",
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
   * test nodeexpressionhc11  Test of a Node after expression used as an argument to an "fn:not" function. .
   */
  @org.junit.Test
  public void nodeexpressionhc11() {
    final XQuery query = new XQuery(
      "fn:not((/staff[1]/employee[1]/empnum[1]) >> (/staff[1]/employee[1]/empnum[1]))",
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
   * test nodeexpressionhc12  Test of a node after expression used as part of a boolean-less-than expression (lt operator). .
   */
  @org.junit.Test
  public void nodeexpressionhc12() {
    final XQuery query = new XQuery(
      "((/staff[1]/employee[1]/empnum[1]) >> (/staff[1]/employee[1]/empnum[1])) \n" +
      "         lt ((/staff[1]/employee[1]/empnum[1]) >> (/staff[1]/employee[1]/empnum[1]))",
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
   * test nodeexpressionhc13  Test of a node after expression used as part of a boolean-less-than expression (ge operator). .
   */
  @org.junit.Test
  public void nodeexpressionhc13() {
    final XQuery query = new XQuery(
      "((/staff[1]/employee[1]/empnum[1]) >> (/staff[1]/employee[1]/empnum[1])) \n" +
      "         ge ((/staff[1]/employee[1]/empnum[1]) >> (/staff[1]/employee[1]/empnum[1]))",
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
   * test nodeexpression14hc  Test of a node after expression used as part of a boolean-greater-than expression (gt operator). .
   */
  @org.junit.Test
  public void nodeexpressionhc14() {
    final XQuery query = new XQuery(
      "((/staff[1]/employee[1]/empnum[1]) >> (/staff[1]/employee[1]/empnum[1])) \n" +
      "         gt ((/staff[1]/employee[1]/empnum[1]) >> (/staff[1]/employee[1]/empnum[1]))",
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
   * test nodeexpressionhc15  Test of a node after expression used as part of a boolean-greater-than expression (le operator). .
   */
  @org.junit.Test
  public void nodeexpressionhc15() {
    final XQuery query = new XQuery(
      "((/staff[1]/employee[1]/empnum[1]) >> (/staff[1]/employee[1]/empnum[1])) \n" +
      "         le ((/staff[1]/employee[1]/empnum[1]) >> (/staff[1]/employee[1]/empnum[1]))",
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
