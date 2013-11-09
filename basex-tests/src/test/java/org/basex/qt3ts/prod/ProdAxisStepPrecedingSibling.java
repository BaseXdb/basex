package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the AxisStep.preceding-sibling production.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdAxisStepPrecedingSibling extends QT3TestSet {

  /**
   *  Iterate from the root node. .
   */
  @org.junit.Test
  public void k2PrecedingSiblingAxis1() {
    final XQuery query = new XQuery(
      "declare variable $i := <root> <child> <preceding2/> <child2> <preceding1/> <child3> <leaf/> </child3> <following/> </child2> <following/> </child> <following/> </root>; empty(root($i)/preceding-sibling::node())",
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
   *  Iterate from the root node. .
   */
  @org.junit.Test
  public void k2PrecedingSiblingAxis2() {
    final XQuery query = new XQuery(
      "declare variable $i := <root> <child> <preceding2/> <child2> <preceding1/> <child3> <leaf/> </child3> <following/> </child2> <following/> </child> <following/> </root>; empty(root($i)/preceding::node())",
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
   *  Apply fn:count() to the result of axis preceding. .
   */
  @org.junit.Test
  public void k2PrecedingSiblingAxis3() {
    final XQuery query = new XQuery(
      "declare variable $i := <root> <child> <preceding2/> <child2> <preceding1/> <child3> <leaf/> </child3> <following/> </child2> <following/> </child> <following/> </root>; count(root($i)/preceding::node())",
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
   *  Apply fn:count() to the result of axis preceding. .
   */
  @org.junit.Test
  public void k2PrecedingSiblingAxis4() {
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
   *  Apply fn:count() to the result of axis preceding. .
   */
  @org.junit.Test
  public void k2PrecedingSiblingAxis5() {
    final XQuery query = new XQuery(
      "declare variable $i := <root> <child> <preceding2/> <child2> <preceding1/> <child3> <leaf/> </child3> <following/> </child2> <following/> </child> <following/> </root>; root($i)//count(preceding-sibling::node())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0 0 0 1 0 1 0 2 2 1")
    );
  }

  /**
   *  Evaluation of the preceding-sibling axis for which the context node is not a node. .
   */
  @org.junit.Test
  public void precedingSibling1() {
    final XQuery query = new XQuery(
      "(200)/preceding-sibling::*",
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
   *  Evaluation of the preceding-sibling axis that is part of an "node after" expression with different operands (returns false). .
   */
  @org.junit.Test
  public void precedingSibling10() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[13]) >> exactly-one(/works[1]/employee[2]/preceding-sibling::employee)",
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
   *  Evaluation of the preceding-sibling axis that is part of an "union " operation. Both operands are the same. .
   */
  @org.junit.Test
  public void precedingSibling11() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/*/day[2]/preceding-sibling::day) | (/works/employee[12]/*/day[2]/preceding-sibling::day)",
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
   *  Evaluation of the preceding-sibling axis that is part of an "union" operation. Both operands are different .
   */
  @org.junit.Test
  public void precedingSibling12() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/*/day[2]/preceding-sibling::day) | (/works/employee[12]/*/day[2])",
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
   *  Evaluation of the preceding-sibling axis that is part of an "intersect" operation. Both operands are the same. .
   */
  @org.junit.Test
  public void precedingSibling13() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/overtime[1]/day[2]/preceding-sibling::day) intersect (/works[1]/employee[12]/overtime[1]/day[2]/preceding-sibling::day)",
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
   *  Evaluation of the preceding-sibling axis that is part of an "except" operation. Both operands are the same. Uses fn:count to avoid empty file .
   */
  @org.junit.Test
  public void precedingSibling14() {
    final XQuery query = new XQuery(
      "fn:count((/works[1]/employee[12]/preceding-sibling::employee) except (/works[1]/employee[12]/preceding-sibling::employee))",
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
   *  Evaluation of the preceding-sibling axis that is part of an "except" operation. Both operands are different. .
   */
  @org.junit.Test
  public void precedingSibling15() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/overtime/day) except (/works[1]/employee[12]/overtime/day[2]/preceding-sibling::day)",
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
   *  Evaluation of the preceding-sibling axis that is part of a boolean expression ("and" and fn:true(). .
   */
  @org.junit.Test
  public void precedingSibling16() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/preceding-sibling::employee) and fn:true()",
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
   *  Evaluation of the preceding-sibling axis that is part of a boolean expression ("and" and fn:false()). .
   */
  @org.junit.Test
  public void precedingSibling17() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/preceding-sibling::employee) and fn:false()",
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
   *  Evaluation of the preceding-sibling axis that is part of a boolean expression ("or" and fn:true()). .
   */
  @org.junit.Test
  public void precedingSibling18() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/preceding-sibling::employee) or fn:true()",
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
   *  Evaluation of the preceding-sibling axis that is part of a boolean expression ("or" and fn:false()). .
   */
  @org.junit.Test
  public void precedingSibling19() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/preceding-sibling::employee) or fn:false()",
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
   *  Evaluation of the preceding-sibling axis for which the given node does not exists. Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void precedingSibling2() {
    final XQuery query = new XQuery(
      "fn:count(/works/employee[1]/preceding-sibling::noSuchNode)",
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
   *  Evaluation of the preceding-sibling axis that used as part of the deep-equal-function. .
   */
  @org.junit.Test
  public void precedingSibling20() {
    final XQuery query = new XQuery(
      "fn:deep-equal(/works[1]/employee[12]/preceding-sibling::employee,/works[1]/employee[12]/preceding-sibling::employee)",
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
   *  Evaluation of the preceding-sibling axis used together with a newly constructed element. Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void precedingSibling21() {
    final XQuery query = new XQuery(
      "let $var := <anElement>Some Content</anElement> return fn:count($var/preceding-sibling::*)",
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
   *  Evaluation of the preceding-sibling axis that is part of an "is" expression (return true). .
   */
  @org.junit.Test
  public void precedingSibling3() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[2]/preceding-sibling::employee) is exactly-one(/works/employee[1])",
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
   *  Evaluation of the preceding-sibling axis that is part of an "is" expression (return false). .
   */
  @org.junit.Test
  public void precedingSibling4() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[2]/preceding-sibling::employee) is exactly-one(/works[1]/employee[2])",
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
   *  Evaluation of the preceding-sibling axis that is part of an "node before" expression (return true). .
   */
  @org.junit.Test
  public void precedingSibling5() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[2]/preceding-sibling::employee) << exactly-one(/works[1]/employee[2])",
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
   *  Evaluation of the preceding-sibling axis that is part of an "node before" expression and both operands are the same (return false). .
   */
  @org.junit.Test
  public void precedingSibling6() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[2]/preceding-sibling::employee) << exactly-one(/works[1]/employee[2]/preceding-sibling::employee)",
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
   *  Evaluation of the preceding-sibling axis that is part of an "node before" expression both operands are differents (return false). .
   */
  @org.junit.Test
  public void precedingSibling7() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[2]/preceding-sibling::employee) << exactly-one(/works[1])",
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
   *  Evaluation of the preceding-sibling axis that is part of a "node after" expression (returns true). .
   */
  @org.junit.Test
  public void precedingSibling8() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[13]) >> exactly-one(/works[1]/employee[12]/preceding-sibling::employee[1])",
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
   *  Evaluation of the preceding-sibling axis that is part of an "node after" expression with both operands the same (returns false). .
   */
  @org.junit.Test
  public void precedingSibling9() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[2]/preceding-sibling::employee) >> exactly-one(/works[1]/employee[2]/preceding-sibling::employee)",
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
   *  Apply the preceding-sibling axis to an attribute node. .
   */
  @org.junit.Test
  public void precedingSiblingAttr() {
    final XQuery query = new XQuery(
      "<foo a='1' b='2' c='3'> <bar>4</bar> <bar>5</bar> <bar>6</bar> </foo>/@c/preceding-sibling::node()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
   *  Evaluate "preceding-sibling::employee[fn:position() = 1]". Selects the previous employee sibling of the context node. .
   */
  @org.junit.Test
  public void unabbreviatedSyntax25() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[2]) return $h/preceding-sibling::employee[fn:position() = 1]",
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
