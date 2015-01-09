package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the AxisStep production.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdAxisStepAncestor extends QT3TestSet {

  /**
   *  Apply the ancestor axis to a single processing instruction. .
   */
  @org.junit.Test
  public void k2AncestorAxis1() {
    final XQuery query = new XQuery(
      "empty(<?target data?>/ancestor::node())",
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
   *  Apply axis ancestor on a single root node. .
   */
  @org.junit.Test
  public void k2AncestorAxis10() {
    final XQuery query = new XQuery(
      "empty(<element/>/ancestor::node())",
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
   *  Apply the ancestor axis to a simple tree constructed with constructors, combined with fn:last(). .
   */
  @org.junit.Test
  public void k2AncestorAxis11() {
    final XQuery query = new XQuery(
      "<a> <b c=\"\"> <c/> </b> <d/> </a>/b/c/ancestor::*[fn:last()]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a><b c=\"\"><c/></b><d/></a>", false)
    );
  }

  /**
   *  Apply the ancestor axis to a simple tree constructed with constructors, combined with fn:last(). Paranteses are added to ensure that the result of the axis step is delivered in document order. .
   */
  @org.junit.Test
  public void k2AncestorAxis12() {
    final XQuery query = new XQuery(
      "<a> <b c=\"\"> <c/> </b> <d/> </a>/b/c/(ancestor::*)[last()]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b c=\"\"><c/></b>", false)
    );
  }

  /**
   *  Apply axis ancestor on a single element. .
   */
  @org.junit.Test
  public void k2AncestorAxis13() {
    final XQuery query = new XQuery(
      "1, <element/>/ancestor::node(), 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 1")
    );
  }

  /**
   *  Apply axis ancestor to a single processing instruction. .
   */
  @org.junit.Test
  public void k2AncestorAxis14() {
    final XQuery query = new XQuery(
      "1, <?target data?>/ancestor::node(), 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 1")
    );
  }

  /**
   *  Apply axis ancestor to a single attribute. .
   */
  @org.junit.Test
  public void k2AncestorAxis15() {
    final XQuery query = new XQuery(
      "1, attribute name {\"content\"}/ancestor::node(), 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 1")
    );
  }

  /**
   *  Apply axis ancestor to a single comment. .
   */
  @org.junit.Test
  public void k2AncestorAxis16() {
    final XQuery query = new XQuery(
      "1, <!-- content -->/ancestor::node(), 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 1")
    );
  }

  /**
   *  Apply axis ancestor to a single document node. .
   */
  @org.junit.Test
  public void k2AncestorAxis17() {
    final XQuery query = new XQuery(
      "document {()}/ancestor::node(), count(document {()}/ancestor::node()), 1",
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
        assertStringValue(false, "0 1")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Apply axis ancestor to a single text node. .
   */
  @org.junit.Test
  public void k2AncestorAxis18() {
    final XQuery query = new XQuery(
      "1, text {\"\"}/ancestor::node(), 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 1")
    );
  }

  /**
   *  Apply fn:count() to the result of axis ancestor. .
   */
  @org.junit.Test
  public void k2AncestorAxis19() {
    final XQuery query = new XQuery(
      "count(<a> <b c=\"\"> <c/> </b> <d/> </a>/b/c/(ancestor::*))",
      ctx);
    try {
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
   *  Apply the ancestor axis to a single comment. .
   */
  @org.junit.Test
  public void k2AncestorAxis2() {
    final XQuery query = new XQuery(
      "empty(<!-- content -->/ancestor::node())",
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
   *  Apply fn:count() to the result of axis ancestor(#2). .
   */
  @org.junit.Test
  public void k2AncestorAxis20() {
    final XQuery query = new XQuery(
      "<a> <b c=\"\"> <c/> </b> <d/> </a>//count(ancestor::*)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0 1 2 1")
    );
  }

  /**
   *  Use positional predicates. .
   */
  @org.junit.Test
  public void k2AncestorAxis21() {
    final XQuery query = new XQuery(
      "<r> <a> <b> <c/> </b> </a> </r>/a/b/c/(ancestor::*[1], ancestor::*[2], ancestor::*[last()], ancestor::*[10])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<r><a><b><c/></b></a></r><a><b><c/></b></a><b><c/></b>", false)
    );
  }

  /**
   *  Apply the ancestor axis to a single element. .
   */
  @org.junit.Test
  public void k2AncestorAxis3() {
    final XQuery query = new XQuery(
      "empty(<anElement/>/ancestor::node())",
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
   *  Apply the ancestor axis to a single attribute. .
   */
  @org.junit.Test
  public void k2AncestorAxis4() {
    final XQuery query = new XQuery(
      "empty(attribute name {\"content\"}/ancestor::node())",
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
   *  Apply the ancestor axis to a single document node. .
   */
  @org.junit.Test
  public void k2AncestorAxis5() {
    final XQuery query = new XQuery(
      "empty(document {()}/ancestor::node())",
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
   *  Apply the ancestor axis to a single document node with content. .
   */
  @org.junit.Test
  public void k2AncestorAxis6() {
    final XQuery query = new XQuery(
      "empty(document {<e><f/><f/>text</e>}/ancestor::node())",
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
   *  Apply the ancestor axis to a simple tree constructed with constructors. .
   */
  @org.junit.Test
  public void k2AncestorAxis7() {
    final XQuery query = new XQuery(
      "<a> <b c=\"\"> <c/> </b> <d/> </a>/b/c/ancestor::*",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a><b c=\"\"><c/></b><d/></a><b c=\"\"><c/></b>", false)
    );
  }

  /**
   *  Apply the ancestor axis to a simple tree constructed with constructors, combined with a [1]-predicate. .
   */
  @org.junit.Test
  public void k2AncestorAxis8() {
    final XQuery query = new XQuery(
      "<a> <b c=\"\"> <c/> </b> <d/> </a>/b/c/ancestor::*[1]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b c=\"\"><c/></b>", false)
    );
  }

  /**
   *  Apply the ancestor axis to a simple tree constructed with constructors, combined with a [1]-predicate. Paranteses are added to ensure that the result of the axis step is delivered in document order. .
   */
  @org.junit.Test
  public void k2AncestorAxis9() {
    final XQuery query = new XQuery(
      "<a> <b c=\"\"> <c/> </b> <d/> </a>/b/c/(ancestor::*)[1]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a><b c=\"\"><c/></b><d/></a>", false)
    );
  }

  /**
   *  Evaluation of the ancestor axis for which the context node is not a node. .
   */
  @org.junit.Test
  public void ancestor1() {
    final XQuery query = new XQuery(
      "(200)/ancestor::*",
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
   *  Evaluation of the ancestor axis that is part of an "node after" expression with both operands the same (returns false). .
   */
  @org.junit.Test
  public void ancestor10() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[1]/ancestor::works) >> exactly-one(/works/employee[1]/hours)",
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
   *  Evaluation of the ancestor axis that is part of an "union " operation. Both operands are the same. .
   */
  @org.junit.Test
  public void ancestor11() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/*/day/ancestor::overtime) | (/works/employee[12]/*/day/ancestor::overtime)",
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
      assertSerialization("<overtime>\n     <day>Monday</day>\n     <day>Tuesday</day>\n   </overtime>", false)
    );
  }

  /**
   *  Evaluation of the ancestor axis that is part of an "union" operation. Both operands are different .
   */
  @org.junit.Test
  public void ancestor12() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/*/day[1]/ancestor::overtime) | (/works/employee[12]/*/day[2]/ancestor::overtime)",
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
      assertSerialization("<overtime>\n     <day>Monday</day>\n     <day>Tuesday</day>\n   </overtime>", false)
    );
  }

  /**
   *  Evaluation of the ancestor axis that is part of an "intersect" operation. Both operands are the same. Uses fn:count to avoid empty file .
   */
  @org.junit.Test
  public void ancestor13() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/overtime/day/ancestor::employee) intersect (/works/employee[12]/overtime/day/ancestor::employee)",
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
      assertSerialization("<employee name=\"John Doe 12\" gender=\"male\">\n   <empnum>E4</empnum>\n   <pnum>P4</pnum>\n   <hours>40</hours>\n   <overtime>\n     <day>Monday</day>\n     <day>Tuesday</day>\n   </overtime>\n  </employee>", false)
    );
  }

  /**
   *  Evaluation of the ancestor axis that is part of an "except" operation. Both operands are the same. Uses fn:count to avoid empty file .
   */
  @org.junit.Test
  public void ancestor14() {
    final XQuery query = new XQuery(
      "fn:count((/works/employee[12]/overtime/day[ancestor::overtime]) except (/works/employee[12]/overtime/day[ancestor::overtime]))",
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
   *  Evaluation of the ancestor axis that is part of an "except" operation. Both operands are different. Uses fn:count to avoid empty file .
   */
  @org.junit.Test
  public void ancestor15() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/overtime/day[ancestor::overtime]) except (/works/employee[12]/overtime/day[1])",
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
   *  Evaluation of the ancestor axis that is part of a boolean expression ("and" and fn:true(). .
   */
  @org.junit.Test
  public void ancestor16() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/overtime/day[ancestor::overtime]) and fn:true()",
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
   *  Evaluation of the ancestor axis that is part of a boolean expression ("and" and fn:false()). .
   */
  @org.junit.Test
  public void ancestor17() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/overtime/day[ancestor::overtime]) and fn:false()",
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
   *  Evaluation of the ancestor axis that is part of a boolean expression ("or" and fn:true()). .
   */
  @org.junit.Test
  public void ancestor18() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/overtime/day[ancestor::overtime]) or fn:true()",
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
   *  Evaluation of the ancestor axis that is part of a boolean expression ("or" and fn:false()). .
   */
  @org.junit.Test
  public void ancestor19() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/overtime/day[ancestor::overtime]) or fn:false()",
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
   *  Evaluation of the ancestor axis for which the given node does not exists. Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void ancestor2() {
    final XQuery query = new XQuery(
      "fn:count(/works/employee[1]/ancestor::noSuchNode)",
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
   *  Evaluation of the ancestor axis that used as part of the deep-equal-function. .
   */
  @org.junit.Test
  public void ancestor20() {
    final XQuery query = new XQuery(
      "fn:deep-equal(/works/employee[12]/overtime/ancestor::works,/works/employee[12]/overtime/ancestor::works)",
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
   *  Evaluation of the ancestor axis used together with a newly constructed element. Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void ancestor21() {
    final XQuery query = new XQuery(
      "let $var := <anElement>Some Content</anElement> return fn:count($var/ancestor::*)",
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
   *  Evaluation of the ancestor axis that is part of an "is" expression (return true). .
   */
  @org.junit.Test
  public void ancestor3() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[1]/ancestor::works) is exactly-one(/works)",
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
   *  Evaluation of the ancestor axis that is part of an "is" expression (return false). .
   */
  @org.junit.Test
  public void ancestor4() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[1]/ancestor::works) is exactly-one(/works/employee[1])",
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
   *  Evaluation of the ancestor axis that is part of an "node before" expression (return true). .
   */
  @org.junit.Test
  public void ancestor5() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[1]/ancestor::works) << exactly-one(/works/employee[1])",
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
   *  Evaluation of the ancestor axis that is part of an "node before" expression and both operands are the same (return false). .
   */
  @org.junit.Test
  public void ancestor6() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[1]/ancestor::works) << exactly-one(/works/employee[1]/ancestor::works)",
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
   *  Evaluation of the ancestor axis that is part of an "node before" expression both operands are differents (return false). .
   */
  @org.junit.Test
  public void ancestor7() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[1]/ancestor::works) << exactly-one(/works/employee[1])",
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
   *  Evaluation of the ancestor axis that is part of an "node after" expression (returns true). .
   */
  @org.junit.Test
  public void ancestor8() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[1]) >> exactly-one(/works/employee[1]/ancestor::works)",
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
   *  Evaluation of the ancestor axis that is part of an "node after" expression with both operands the same (returns false). .
   */
  @org.junit.Test
  public void ancestor9() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[1]/ancestor::works) >> exactly-one(/works/employee[1]/ancestor::works)",
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
   *  Evaluate selecting an ancestor (ancestor::employee)- Select the "employee" ancestors of the context node .
   */
  @org.junit.Test
  public void unabbreviatedSyntax10() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[1]/hours) return $h/ancestor::employee",
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
      assertSerialization("<employee name=\"Jane Doe 1\" gender=\"female\">\n   <empnum>E1</empnum>\n   <pnum>P1</pnum>\n   <hours>40</hours>\n  </employee>", false)
    );
  }
}
