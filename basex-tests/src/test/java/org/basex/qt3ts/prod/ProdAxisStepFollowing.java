package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the following axis.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdAxisStepFollowing extends QT3TestSet {

  /**
   *  Evaluate the 'following' axis on a html document. .
   */
  @org.junit.Test
  public void k2FollowingAxis1() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $i := <html> <head/> <body> <p attr=\"foo\"> </p> <p attr=\"boo\"> </p> <p> </p> <p> </p> <p> </p> </body> </html> \n" +
      "        return $i//p[1]/following::*\n" +
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
      assertSerialization("<p attr=\"boo\"/><p/><p/><p/>", false)
    );
  }

  /**
   *  Evaluate the child node from the last node in a tree. .
   */
  @org.junit.Test
  public void k2FollowingAxis2() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $i := <root> <child/> <child/> <child> <child2> <child3> <leaf/> </child3> </child2> </child> </root>;\n" +
      "         1, root($i)//leaf/following::node(), 1\n" +
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
      assertStringValue(false, "1 1")
    );
  }

  /**
   *  Evaluate the child node from root of a tree. .
   */
  @org.junit.Test
  public void k2FollowingAxis3() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $i := <root> <child/> <child/> <child> <child2> <child3> <leaf/> </child3> </child2> </child> </root>; \n" +
      "        empty(root($i)/following::node())\n" +
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
   *  Evaluate fn:count() on a range of nodes, navigated with axis following. .
   */
  @org.junit.Test
  public void k2FollowingAxis4() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $i := <root> <child/> <child/> <child> <child2> <child3> <leaf/> </child3> </child2> </child> </root>; \n" +
      "        root($i)//count(following::node())\n" +
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
      assertStringValue(false, "0 5 4 0 0 0 0")
    );
  }

  /**
   *  Evaluation of the following axis for which the context node is not a node. .
   */
  @org.junit.Test
  public void following1() {
    final XQuery query = new XQuery(
      "(200)/following::*",
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
   *  Evaluation of the following axis that is part of an "node after" expression with different operands (returns false). .
   */
  @org.junit.Test
  public void following10() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[12]) >> exactly-one(/works[1]/employee[12]/following::employee)",
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
   *  Evaluation of the following axis that is part of an "union " operation. Both operands are the same. .
   */
  @org.junit.Test
  public void following11() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/*/day[1]/following::day) | (/works/employee[12]/*/day[1]/following::day)",
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
   *  Evaluation of the following axis that is part of an "union" operation. Both operands are different .
   */
  @org.junit.Test
  public void following12() {
    final XQuery query = new XQuery(
      "(/works/employee[12]/*/day[1]/following::day) | (/works/employee[12]/*/day[1])",
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
   *  Evaluation of the following axis that is part of an "intersect" operation. Both operands are the same. .
   */
  @org.junit.Test
  public void following13() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/overtime[1]/day[1]/following::day) intersect (/works[1]/employee[12]/overtime[1]/day[1]/following::day)",
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
   *  Evaluation of the following axis that is part of an "except" operation. Both operands are the same. Uses fn:count to avoid empty file .
   */
  @org.junit.Test
  public void following14() {
    final XQuery query = new XQuery(
      "fn:count((/works[1]/employee[12]/following::employee) except (/works[1]/employee[12]/following::employee))",
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
   *  Evaluation of the following axis that is part of an "except" operation. Both operands are different. .
   */
  @org.junit.Test
  public void following15() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/overtime/day) except (/works[1]/employee[12]/overtime/day[1]/following::day)",
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
   *  Evaluation of the following axis that is part of a boolean expression ("and" and fn:true(). .
   */
  @org.junit.Test
  public void following16() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/following::employee) and fn:true()",
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
   *  Evaluation of the following axis that is part of a boolean expression ("and" and fn:false()). .
   */
  @org.junit.Test
  public void following17() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/following::employee) and fn:false()",
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
   *  Evaluation of the following axis that is part of a boolean expression ("or" and fn:true()). .
   */
  @org.junit.Test
  public void following18() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/following::employee) or fn:true()",
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
   *  Evaluation of the following axis that is part of a boolean expression ("or" and fn:false()). .
   */
  @org.junit.Test
  public void following19() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[12]/following::employee) or fn:false()",
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
   *  Evaluation of the following axis for which the given node does not exists. Uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void following2() {
    final XQuery query = new XQuery(
      "fn:count(/works/employee[1]/following::noSuchNode)",
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
   *  Evaluation of the following axis that used as part of the deep-equal-function. .
   */
  @org.junit.Test
  public void following20() {
    final XQuery query = new XQuery(
      "fn:deep-equal(/works[1]/employee[12]/following::employee,/works[1]/employee[12]/following::employee)",
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
  public void following21() {
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
   *  Evaluation of the following axis that is part of an "is" expression (return true). .
   */
  @org.junit.Test
  public void following3() {
    final XQuery query = new XQuery(
      "exactly-one(/works/employee[12]/following::employee) is exactly-one(/works/employee[13])",
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
   *  Evaluation of the following axis that is part of an "is" expression (return false). .
   */
  @org.junit.Test
  public void following4() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[12]/following::employee) is exactly-one(/works[1]/employee[12])",
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
   *  Evaluation of the following axis that is part of an "node before" expression (return true). .
   */
  @org.junit.Test
  public void following5() {
    final XQuery query = new XQuery(
      "(/works[1]/employee[11]/following::employee[1]) << (/works[1]/employee[13])",
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
   *  Evaluation of the following axis that is part of an "node before" expression and both operands are the same (return false). .
   */
  @org.junit.Test
  public void following6() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[12]/following::employee) << exactly-one(/works[1]/employee[12]/following::employee)",
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
   *  Evaluation of the following axis that is part of an "node before" expression both operands are differents (return false). .
   */
  @org.junit.Test
  public void following7() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[12]/following::employee) << exactly-one(/works[1]/employee[12]/overtime[1])",
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
   *  Evaluation of the following axis that is part of an "node after" expression (returns true). .
   */
  @org.junit.Test
  public void following8() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[13]) >> exactly-one(/works[1]/employee[12]/overtime[1]/day[1]/following::day)",
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
   *  Evaluation of the following axis that is part of an "node after" expression with both operands the same (returns false). .
   */
  @org.junit.Test
  public void following9() {
    final XQuery query = new XQuery(
      "exactly-one(/works[1]/employee[12]/following::employee) >> exactly-one(/works[1]/employee[12]/following::employee)",
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
