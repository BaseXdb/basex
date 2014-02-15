package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the AxisStep production.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdAxisStepAncestorOrSelf extends QT3TestSet {

  /**
   *  Apply axis ancestor on a single element. .
   */
  @org.junit.Test
  public void k2AncestorOrSelfAxis1() {
    final XQuery query = new XQuery(
      "1, <element/>/ancestor-or-self::node(), 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("1<element/>1", false)
    );
  }

  /**
   *  Apply axis ancestor to a single processing instruction. .
   */
  @org.junit.Test
  public void k2AncestorOrSelfAxis2() {
    final XQuery query = new XQuery(
      "1, <?target data?>/ancestor-or-self::node(), 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("1<?target data?>1", false)
    );
  }

  /**
   *  Apply axis ancestor to a single attribute. .
   */
  @org.junit.Test
  public void k2AncestorOrSelfAxis3() {
    final XQuery query = new XQuery(
      "1, (attribute name {\"content\"}/ancestor-or-self::node() instance of attribute(name)), 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, true(), 1")
    );
  }

  /**
   *  Apply axis ancestor to a single comment. .
   */
  @org.junit.Test
  public void k2AncestorOrSelfAxis4() {
    final XQuery query = new XQuery(
      "1, <!-- content -->/ancestor-or-self::node(), 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("1<!-- content -->1", false)
    );
  }

  /**
   *  Apply axis ancestor-or-self to a single document node. .
   */
  @org.junit.Test
  public void k2AncestorOrSelfAxis5() {
    final XQuery query = new XQuery(
      "1, document {()}/ancestor-or-self::node(), 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("11", false)
    );
  }

  /**
   *  Apply axis ancestor to a single text node. .
   */
  @org.junit.Test
  public void k2AncestorOrSelfAxis6() {
    final XQuery query = new XQuery(
      "1, text {\"\"}/ancestor-or-self::node(), 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("11", false)
    );
  }

  /**
   *  Apply fn:count() to the result of axis ancestor-or-self. .
   */
  @org.junit.Test
  public void k2AncestorOrSelfAxis7() {
    final XQuery query = new XQuery(
      "count(<a> <b c=\"\"> <c/> </b> <d/> </a>/b/c/(ancestor-or-self::*))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
   *  Apply fn:count() to the result of axis ancestor-or-self(#2). .
   */
  @org.junit.Test
  public void k2AncestorOrSelfAxis8() {
    final XQuery query = new XQuery(
      "<a> <b c=\"\"> <c/> </b> <d/> </a>//count(ancestor-or-self::*)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 2")
    );
  }

  /**
   *  Use positional predicates. .
   */
  @org.junit.Test
  public void k2AncestorOrSelfAxis9() {
    final XQuery query = new XQuery(
      "<r> <a> <b> <c/> </b> </a> </r>/a/b/c/(ancestor-or-self::*[1], ancestor-or-self::*[2], ancestor-or-self::*[last()], ancestor-or-self::*[10])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<r><a><b><c/></b></a></r><b><c/></b><c/>", false)
    );
  }

  /**
   *  Evaluation of the ancestor-or-self axis for which the context node is not a node. .
   */
  @org.junit.Test
  public void ancestorself1() {
    final XQuery query = new XQuery(
      "(200)/ancestor-or-self::*",
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
   *  Evaluation of the ancestor-or-self axis that is part of an "node after" expression with both operands the same (returns false). .
   */
  @org.junit.Test
  public void ancestorself10() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[1]/ancestor-or-self::works) >> exactly-one(/works/employee[1]/hours)",
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
   *  Evaluation of the ancestor-or-self axis that is part of an "union " operation. Both operands are the same. .
   */
  @org.junit.Test
  public void ancestorself11() {
    final XQuery query = new XQuery(
      "((/works/employee[12]/*/day/ancestor-or-self::overtime) | (/works/employee[12]/*/day/ancestor-or-self::overtime))/count(*)",
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
      assertStringValue(false, "2")
    );
  }

  /**
   *  Evaluation of the ancestor-or-self axis that is part of an "union" operation. Both operands are different .
   */
  @org.junit.Test
  public void ancestorself12() {
    final XQuery query = new XQuery(
      "((/works/employee[12]/*/day[1]/ancestor-or-self::overtime) | (/works/employee[12]/*/day[2]/ancestor-or-self::overtime))/count(*)",
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
      assertStringValue(false, "2")
    );
  }

  /**
   *  Evaluation of the ancestor-or-self axis that is part of an "intersect" operation. Both operands are the same. .
   */
  @org.junit.Test
  public void ancestorself13() {
    final XQuery query = new XQuery(
      "((/works/employee[12]/overtime/day/ancestor-or-self::employee) intersect (/works/employee[12]/overtime/day/ancestor-or-self::employee))/@name",
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
      assertStringValue(false, "John Doe 12")
    );
  }

  /**
   *  Evaluation of the ancestor-self axis that is part of an "except" operation. Both operands are the same. Uses fn:count to avoid empty file .
   */
  @org.junit.Test
  public void ancestorself14() {
    final XQuery query = new XQuery(
      "fn:count((/works/employee[12]/overtime/day[ancestor-or-self::overtime]) except (/works/employee[12]/overtime/day[ancestor-or-self::overtime]))",
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
   *  Evaluation of the ancestor-or-self axis that is part of an "except" operation. Both operands are different. .
   */
  @org.junit.Test
  public void ancestorself15() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/overtime/day[ancestor-or-self::overtime]) except (/works/employee[12]/overtime/day[1])",
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
   *  Evaluation of the ancestor-or-self axis that is part of a boolean expression ("and" and fn:true(). .
   */
  @org.junit.Test
  public void ancestorself16() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/overtime/day[ancestor-or-self::overtime]) and fn:true()",
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
   *  Evaluation of the ancestor-or-self axis that is part of a boolean expression ("and" and fn:false()). .
   */
  @org.junit.Test
  public void ancestorself17() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/overtime/day[ancestor-or-self::overtime]) and fn:false()",
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
   *  Evaluation of the ancestor-or-self axis that is part of a boolean expression ("or" and fn:true()). .
   */
  @org.junit.Test
  public void ancestorself18() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/overtime/day[ancestor-or-self::overtime]) or fn:true()",
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
   *  Evaluation of the ancestor-or-self axis that is part of a boolean expression ("or" and fn:false()). .
   */
  @org.junit.Test
  public void ancestorself19() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/overtime/day[ancestor-or-self::overtime]) or fn:false()",
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
   *  Evaluation of the ancestor-or-self axis for which the given node does not exists. Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void ancestorself2() {
    final XQuery query = new XQuery(
      "fn:count(/works/employee[1]/ancestor-or-self::noSuchNode)",
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
   *  Evaluation of the ancestor-or-self axis that used as part of the deep-equal-function. .
   */
  @org.junit.Test
  public void ancestorself20() {
    final XQuery query = new XQuery(
      "fn:deep-equal(/works/employee[12]/overtime/ancestor-or-self::works,/works/employee[12]/overtime/ancestor-or-self::works)",
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
   *  Evaluation of the ancestor-or-self axis used together with a newly constructed element. .
   */
  @org.junit.Test
  public void ancestorself21() {
    final XQuery query = new XQuery(
      "let $var := <anElement>Some Content</anElement> return $var/ancestor-or-self::*",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<anElement>Some Content</anElement>", false)
    );
  }

  /**
   *  Evaluation of the ancestor-or-self axis that is part of an "is" expression (return true). .
   */
  @org.junit.Test
  public void ancestorself3() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[1]/ancestor-or-self::works) is exactly-one(/works)",
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
   *  Evaluation of the ancestor-or-self axis that is part of an "is" expression (return false). .
   */
  @org.junit.Test
  public void ancestorself4() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[1]/ancestor-or-self::works) is exactly-one(/works/employee[1])",
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
   *  Evaluation of the ancestor-or-self axis that is part of an "node before" expression (return true). .
   */
  @org.junit.Test
  public void ancestorself5() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[1]/ancestor-or-self::works) << exactly-one(/works/employee[1])",
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
   *  Evaluation of the ancestor-or-self axis that is part of an "node before" expression and both operands are the same (return false). .
   */
  @org.junit.Test
  public void ancestorself6() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[1]/ancestor-or-self::works) << exactly-one(/works/employee[1]/ancestor-or-self::works)",
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
   *  Evaluation of the ancestor-or-self axis that is part of an "node before" expression both operands are differents (return false). .
   */
  @org.junit.Test
  public void ancestorself7() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[1]/ancestor-or-self::works) << exactly-one(/works/employee[1])",
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
   *  Evaluation of the ancestor-or-self axis that is part of an "node after" expression (returns true). .
   */
  @org.junit.Test
  public void ancestorself8() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[1]) >> exactly-one(/works/employee[1]/ancestor-or-self::works)",
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
   *  Evaluation of the ancestor-or-self axis that is part of an "node after" expression with both operands the same (returns false). .
   */
  @org.junit.Test
  public void ancestorself9() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[1]/ancestor-or-self::works) >> exactly-one(/works/employee[1]/ancestor-or-self::works)",
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
   *  Evaluate selecting an ancestor or self (ancestor-or-self::employee)- Select the "employee" ancestors of the context node and if the context is "employee" select it as well. .
   */
  @org.junit.Test
  public void unabbreviatedSyntax11() {
    final XQuery query = new XQuery(
      "(for $h in (/works/employee[1]/hours) return $h/ancestor-or-self::employee)/@name",
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
