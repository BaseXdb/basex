package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the preceding axis.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdAxisStepPreceding extends QT3TestSet {

  /**
   *  Evaluate the child node from the last node in a tree. .
   */
  @org.junit.Test
  public void k2PrecedingAxis1() {
    final XQuery query = new XQuery(
      "declare variable $i := <root> <child/> <child/> <child> <child2> <child3> <leaf/> </child3> </child2> </child> </root>; empty(root($i)/preceding::node())",
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
   *  Evaluate from a node that has no preceding nodes. .
   */
  @org.junit.Test
  public void k2PrecedingAxis2() {
    final XQuery query = new XQuery(
      "declare variable $i := <root> <child> <child2> <child3> <leaf/> </child3> <following/> </child2> <following/> </child> <following/> </root>; empty(root($i)//leaf/preceding::node())",
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
   *  Apply a numeric predicate to axis preceding. .
   */
  @org.junit.Test
  public void k2PrecedingAxis3() {
    final XQuery query = new XQuery(
      "declare variable $i := <root> <child> <preceding2/> <child2> <preceding1/> <child3> <leaf/> </child3> <following/> </child2> <following/> </child> <following/> </root>; root($i)//leaf/preceding::node()[1]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<preceding1/>", false)
    );
  }

  /**
   *  Apply a numeric predicate to axis preceding, combined with a numeric predicate. The paranteses ensures the step is parsed as a primary expression, and hence is in document order, not reversed. .
   */
  @org.junit.Test
  public void k2PrecedingAxis4() {
    final XQuery query = new XQuery(
      "declare variable $i := <root> <child> <preceding2/> <child2> <preceding1/> <child3> <leaf/> </child3> <following/> </child2> <following/> </child> <following/> </root>; root($i)//leaf/(preceding::node())[1]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<preceding2/>", false)
    );
  }

  /**
   *  Apply a numeric predicate to axis preceding, combined with fn:last(). The paranteses ensures the step is parsed as a primary expression, and hence is in document order, not reversed. .
   */
  @org.junit.Test
  public void k2PrecedingAxis5() {
    final XQuery query = new XQuery(
      "declare variable $i := <root> <child> <preceding2/> <child2> <preceding1/> <child3> <leaf/> </child3> <following/> </child2> <following/> </child> <following/> </root>; root($i)//leaf/(preceding::node())[last()]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<preceding1/>", false)
    );
  }

  /**
   *  Apply fn:last() to axis preceding. .
   */
  @org.junit.Test
  public void k2PrecedingAxis6() {
    final XQuery query = new XQuery(
      "declare variable $i := <root> <child> <preceding2/> <child2> <preceding1/> <child3> <leaf/> </child3> <following/> </child2> <following/> </child> <following/> </root>; root($i)//leaf/preceding::node()[last()]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<preceding2/>", false)
    );
  }

  /**
   *  Apply fn:count() to axis preceding. .
   */
  @org.junit.Test
  public void k2PrecedingAxis7() {
    final XQuery query = new XQuery(
      "declare variable $i := <root> <child> <preceding2/> <child2> <preceding1/> <child3> <leaf/> </child3> <following/> </child2> <following/> </child> <following/> </root>; count(root($i)//leaf/preceding::node())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
   *  Apply fn:count() to axis preceding. .
   */
  @org.junit.Test
  public void k2PrecedingAxis8() {
    final XQuery query = new XQuery(
      "declare variable $i := <root> <child> <preceding2/> <child2> <preceding1/> <child3> <leaf/> </child3> <following/> </child2> <following/> </child> <following/> </root>; root($i)//count(preceding::*)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0 0 0 1 1 2 2 4 6 8")
    );
  }

  /**
   *  Evaluation of the preceding axis for which the context node is not a node. .
   */
  @org.junit.Test
  public void preceding1() {
    final XQuery query = new XQuery(
      "(200)/preceding::*",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0019")
    );
  }

  /**
   *  Evaluation of the preceding axis that is part of an "node after" expression with different operands (returns false). .
   */
  @org.junit.Test
  public void preceding10() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[13]) >> exactly-one(/works[1]/employee[2]/preceding::employee)",
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
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of the preceding axis that is part of an "union " operation. Both operands are the same. .
   */
  @org.junit.Test
  public void preceding11() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/*/day[2]/preceding::day) | (/works/employee[12]/*/day[2]/preceding::day)",
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
      assertSerialization("<day>Monday</day>", false)
    );
  }

  /**
   *  Evaluation of the preceding axis that is part of an "union" operation. Both operands are different .
   */
  @org.junit.Test
  public void preceding12() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/*/day[2]/preceding::day) | (/works/employee[12]/*/day[2])",
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
      assertSerialization("<day>Monday</day><day>Tuesday</day>", false)
    );
  }

  /**
   *  Evaluation of the preceding axis that is part of an "intersect" operation. Both operands are the same. .
   */
  @org.junit.Test
  public void preceding13() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/overtime[1]/day[2]/preceding::day) intersect (/works[1]/employee[12]/overtime[1]/day[2]/preceding::day)",
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
      assertSerialization("<day>Monday</day>", false)
    );
  }

  /**
   *  Evaluation of the preceding axis that is part of an "except" operation. Both operands are the same. Uses fn:count to avoid empty file .
   */
  @org.junit.Test
  public void preceding14() {
    final XQuery query = new XQuery(
      "fn:count((/works[1]/employee[12]/preceding::employee) except (/works[1]/employee[12]/preceding::employee))",
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
   *  Evaluation of the preceding axis that is part of an "except" operation. Both operands are different. .
   */
  @org.junit.Test
  public void preceding15() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/overtime/day) except (/works[1]/employee[12]/overtime/day[2]/preceding::day)",
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
      assertSerialization("<day>Tuesday</day>", false)
    );
  }

  /**
   *  Evaluation of the preceding axis that is part of a boolean expression ("and" and fn:true(). .
   */
  @org.junit.Test
  public void preceding16() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/preceding::employee) and fn:true()",
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
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of the preceding axis that is part of a boolean expression ("and" and fn:false()). .
   */
  @org.junit.Test
  public void preceding17() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/preceding::employee) and fn:false()",
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
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of the preceding axis that is part of a boolean expression ("or" and fn:true()). .
   */
  @org.junit.Test
  public void preceding18() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/preceding::employee) or fn:true()",
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
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of the preceding axis that is part of a boolean expression ("or" and fn:false()). .
   */
  @org.junit.Test
  public void preceding19() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/preceding::employee) or fn:false()",
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
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of the preceding axis for which the given node does not exists. Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void preceding2() {
    final XQuery query = new XQuery(
      "fn:count(/works/employee[1]/preceding::noSuchNode)",
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
   *  Evaluation of the preceding axis that used as part of the deep-equal-function. .
   */
  @org.junit.Test
  public void preceding20() {
    final XQuery query = new XQuery(
      "fn:deep-equal(/works[1]/employee[12]/preceding::employee,/works[1]/employee[12]/preceding::employee)",
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
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of the preceding axis used together with a newly constructed element. Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void preceding21() {
    final XQuery query = new XQuery(
      "let $var := <anElement>Some Content</anElement> return fn:count($var/preceding::*)",
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
   *  Evaluation of the preceding axis from the last node in the document. .
   */
  @org.junit.Test
  public void preceding22() {
    final XQuery query = new XQuery(
      "<result> { (//node())[last()]/preceding::node() } </result>",
      ctx);
    try {
      query.context(node(file("prod/AxisStep.preceding/SmallTree.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result>\n    <section1 noValue=\"\">\n        text1\n        <?target1 data1?>\n        <?target2 data2?>\n        <?target3 data3?>\n    </section1>\n        text1\n        <?target1 data1?>\n        <?target2 data2?>\n        <?target3 data3?>\n    \n    <section2 attr1=\"1\" attr2=\"2\"/>\n    <!-- comment1 -->\n    <section3/>\n    <!-- comment2 -->\n    <!-- comment3 -->\n    <noChildren/>\n    <oneTextChild>theTextChild2</oneTextChild>theTextChild2</result>", false)
    );
  }

  /**
   *  Evaluation of the preceding axis from the last node in the document, containing only attributes and elements. .
   */
  @org.junit.Test
  public void preceding23() {
    final XQuery query = new XQuery(
      "<result> { (//node())[last()]/preceding::node(), empty((//node())[last()]/preceding::node()) } </result>",
      ctx);
    try {
      query.context(node(file("prod/AxisStep.preceding/AttributesAndElements.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result>true</result>", false)
    );
  }

  /**
   *  Evaluation of the preceding axis that is part of an "is" expression (return true). .
   */
  @org.junit.Test
  public void preceding3() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[2]/preceding::employee) is exactly-one(/works/employee[1])",
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
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of the preceding axis that is part of an "is" expression (return false). .
   */
  @org.junit.Test
  public void preceding4() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[2]/preceding::employee) is exactly-one(/works[1]/employee[2])",
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
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of the preceding axis that is part of an "node before" expression (return true). .
   */
  @org.junit.Test
  public void preceding5() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[2]/preceding::employee) << exactly-one(/works[1]/employee[2])",
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
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of the preceding axis that is part of an "node before" expression and both operands are the same (return false). .
   */
  @org.junit.Test
  public void preceding6() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[2]/preceding::employee) << exactly-one(/works[1]/employee[2]/preceding::employee)",
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
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of the preceding axis that is part of an "node before" expression both operands are differents (return false). .
   */
  @org.junit.Test
  public void preceding7() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[2]/preceding::employee) << exactly-one(/works[1])",
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
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of the preceding axis that is part of a "node after" expression (returns true). .
   */
  @org.junit.Test
  public void preceding8() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[13]) >> exactly-one(/works[1]/employee[12]/overtime[1]/day[2]/preceding::day)",
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
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of the preceding axis that is part of an "node after" expression with both operands the same (returns false). .
   */
  @org.junit.Test
  public void preceding9() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[2]/preceding::employee) >> exactly-one(/works[1]/employee[2]/preceding::employee)",
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
      assertBoolean(false)
    );
  }
}
