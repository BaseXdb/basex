package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the following-sibling axis.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdAxisStepFollowingSibling extends QT3TestSet {

  /**
   *  Apply following-sibling to a child whose siblings are the last in a document. .
   */
  @org.junit.Test
  public void k2FollowingSiblingAxis1() {
    final XQuery query = new XQuery(
      "<root> <child/> <child/> <child/> </root>/child[1]/following-sibling::node()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<child/><child/>", false)
    );
  }

  /**
   *  Use a positional predicate beyond the output. .
   */
  @org.junit.Test
  public void k2FollowingSiblingAxis10() {
    final XQuery query = new XQuery(
      "<result> { <a><b/></a>/*/following::*[2] } </result>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result/>", false)
    );
  }

  /**
   *  Apply following-sibling to a child whose last nodes in document order are attributes. .
   */
  @org.junit.Test
  public void k2FollowingSiblingAxis2() {
    final XQuery query = new XQuery(
      "<root> <child/> <child/> <child attr=\"foo\" attr2=\"foo\"/> </root>/child[1]/following-sibling::node()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<child/><child attr=\"foo\" attr2=\"foo\"/>", false)
    );
  }

  /**
   *  Apply fn:count() to the result of axis following-sibling. .
   */
  @org.junit.Test
  public void k2FollowingSiblingAxis3() {
    final XQuery query = new XQuery(
      "count(<root> <child/> <child/> <child attr=\"foo\" attr2=\"foo\"/> </root>/child[1]/following-sibling::node())",
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
   *  Evaluate fn:count() on a range of nodes, navigated with axis following. .
   */
  @org.junit.Test
  public void k2FollowingSiblingAxis4() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $i := <root> <child/> <child/> <child> <child2> <child3> <leaf/> </child3> </child2> </child> </root>; \n" +
      "        root($i)//count(following-sibling::node())\n" +
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
      assertStringValue(false, "0 2 1 0 0 0 0")
    );
  }

  /**
   *  Evaluate on a small tree. .
   */
  @org.junit.Test
  public void k2FollowingSiblingAxis5() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $i := <root> <child/> <child/> <child> <child2> <child3> <leaf/> </child3> </child2> </child> </root>; \n" +
      "        root($i)//following-sibling::node()\n" +
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
      assertSerialization("<child/><child><child2><child3><leaf/></child3></child2></child>", false)
    );
  }

  /**
   *  A type error with the following-sibling axis. .
   */
  @org.junit.Test
  public void k2FollowingSiblingAxis6() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $i := <root> <child/> <child/> <child> <child2> <child3> <leaf/> </child3> </child2> </child> </root>; \n" +
      "        root($i)//(following-sibling::node(), \"BOO\")\n" +
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
      error("XPTY0018")
    );
  }

  /**
   *  Apply count to axis following-sibling. .
   */
  @org.junit.Test
  public void k2FollowingSiblingAxis7() {
    final XQuery query = new XQuery(
      "count(<root> <child/> </root>/following-sibling::node())",
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
   *  Apply count to axis following-sibling, combined with the comma operator. .
   */
  @org.junit.Test
  public void k2FollowingSiblingAxis8() {
    final XQuery query = new XQuery(
      "1, <root> <child/> </root>/following-sibling::node(), 1",
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
   *  Apply count to axis following-sibling to a single node, combined with the comma operator. .
   */
  @org.junit.Test
  public void k2FollowingSiblingAxis9() {
    final XQuery query = new XQuery(
      "1, <root/>/following-sibling::node(), 1",
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
   *  Apply the following-sibling axis to an attribute node. .
   */
  @org.junit.Test
  public void followingSiblingAttr() {
    final XQuery query = new XQuery(
      "<foo a='1' b='2' c='3'> <bar>4</bar> <bar>5</bar> <bar>6</bar> </foo>/@a/following-sibling::node()",
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
   *  Evaluation of the following-sibling axis for which the context node is not a node. .
   */
  @org.junit.Test
  public void followingsibling1() {
    final XQuery query = new XQuery(
      "(200)/following-sibling::*",
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
   *  Evaluation of the following-sibling axis that is part of an "node after" expression with different operands (returns false). .
   */
  @org.junit.Test
  public void followingsibling10() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[12]) >> exactly-one(/works[1]/employee[12]/following-sibling::employee)",
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
   *  Evaluation of the following-sibling axis that is part of an "union " operation. Both operands are the same. .
   */
  @org.junit.Test
  public void followingsibling11() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/*/day[1]/following-sibling::day) | (/works/employee[12]/*/day[1]/following-sibling::day)",
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
   *  Evaluation of the following-sibling axis that is part of an "union" operation. Both operands are different .
   */
  @org.junit.Test
  public void followingsibling12() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/*/day[1]/following-sibling::day) | (/works/employee[12]/*/day[1])",
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
   *  Evaluation of the following-sibling axis that is part of an "intersect" operation. Both operands are the same. .
   */
  @org.junit.Test
  public void followingsibling13() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/overtime[1]/day[1]/following-sibling::day) intersect (/works[1]/employee[12]/overtime[1]/day[1]/following-sibling::day)",
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
   *  Evaluation of the following-sibling axis that is part of an "except" operation. Both operands are the same. Uses fn:count to avoid empty file .
   */
  @org.junit.Test
  public void followingsibling14() {
    final XQuery query = new XQuery(
      "fn:count((/works[1]/employee[12]/following-sibling::employee) except (/works[1]/employee[12]/following-sibling::employee))",
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
   *  Evaluation of the following-sibling axis that is part of an "except" operation. Both operands are different. .
   */
  @org.junit.Test
  public void followingsibling15() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/overtime/day) except (/works[1]/employee[12]/overtime/day[1]/following-sibling::day)",
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
   *  Evaluation of the following-sibling axis that is part of a boolean expression ("and" and fn:true(). .
   */
  @org.junit.Test
  public void followingsibling16() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/following-sibling::employee) and fn:true()",
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
   *  Evaluation of the following-sibling axis that is part of a boolean expression ("and" and fn:false()). .
   */
  @org.junit.Test
  public void followingsibling17() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/following-sibling::employee) and fn:false()",
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
   *  Evaluation of the following-sibling axis that is part of a boolean expression ("or" and fn:true()). .
   */
  @org.junit.Test
  public void followingsibling18() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/following-sibling::employee) or fn:true()",
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
   *  Evaluation of the following-sibling axis that is part of a boolean expression ("or" and fn:false()). .
   */
  @org.junit.Test
  public void followingsibling19() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/following-sibling::employee) or fn:false()",
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
   *  Evaluation of the following-sibling axis for which the given node does not exists. Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void followingsibling2() {
    final XQuery query = new XQuery(
      "fn:count(/works/employee[1]/following-sibling::noSuchNode)",
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
   *  Evaluation of the following-sibling axis that used as part of the deep-equal-function. .
   */
  @org.junit.Test
  public void followingsibling20() {
    final XQuery query = new XQuery(
      "fn:deep-equal(/works[1]/employee[12]/following-sibling::employee,/works[1]/employee[12]/following-sibling::employee)",
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
   *  Evaluation of the following axis used together with a newly constructed element. Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void followingsibling21() {
    final XQuery query = new XQuery(
      "let $var := <anElement>Some Content</anElement> return fn:count($var/following::*)",
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
   *  Evaluation of the following-sibling axis that is part of an "is" expression (return true). .
   */
  @org.junit.Test
  public void followingsibling3() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[12]/following-sibling::employee) is exactly-one(/works/employee[13])",
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
   *  Evaluation of the following-sibling axis that is part of an "is" expression (return false). .
   */
  @org.junit.Test
  public void followingsibling4() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[12]/following-sibling::employee) is exactly-one(/works[1]/employee[12])",
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
   *  Evaluation of the following-sibling axis that is part of an "node before" expression (return true). .
   */
  @org.junit.Test
  public void followingsibling5() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[12]/overtime/day[1]/following-sibling::day) << exactly-one(/works[1]/employee[13])",
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
   *  Evaluation of the following-sibling axis that is part of an "node before" expression and both operands are the same (return false). .
   */
  @org.junit.Test
  public void followingsibling6() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[12]/following-sibling::employee) << exactly-one(/works[1]/employee[12]/following-sibling::employee)",
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
   *  Evaluation of the following-sibling axis that is part of an "node before" expression both operands are differents (return false). .
   */
  @org.junit.Test
  public void followingsibling7() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[12]/following-sibling::employee) << exactly-one(/works[1]/employee[12]/overtime[1])",
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
   *  Evaluation of the following-sibling axis that is part of an "node after" expression (returns true). .
   */
  @org.junit.Test
  public void followingsibling8() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[13]) >> exactly-one(/works[1]/employee[12]/overtime[1]/day[1]/following-sibling::day)",
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
   *  Evaluation of the following-sibling axis that is part of an "node after" expression with both operands the same (returns false). .
   */
  @org.junit.Test
  public void followingsibling9() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[12]/following-sibling::employee) >> exactly-one(/works[1]/employee[12]/following-sibling::employee)",
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
   *  Evaluate "following-sibling::employee[fn:position() = 1]". Selects the next employee sibling of the context node. .
   */
  @org.junit.Test
  public void unabbreviatedSyntax24() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[2]) return $h/following-sibling::employee[fn:position() = 1]",
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
      assertSerialization("<employee name=\"Jane Doe 3\" gender=\"female\">\n   <empnum>E1</empnum>\n   <pnum>P3</pnum>\n   <hours>80</hours>\n  </employee>", false)
    );
  }
}
