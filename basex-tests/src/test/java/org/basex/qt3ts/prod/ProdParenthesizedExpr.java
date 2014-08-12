package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the ParenthesizedExpr production.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdParenthesizedExpr extends QT3TestSet {

  /**
   *  Arithmetic operations .
   */
  @org.junit.Test
  public void parenexpr1() {
    final XQuery query = new XQuery(
      "(1 + 2) * 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("9")
    );
  }

  /**
   *  Logical expressions .
   */
  @org.junit.Test
  public void parenexpr10() {
    final XQuery query = new XQuery(
      "fn:true() or (fn:true() and fn:false())",
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
   *  FLWOR expression .
   */
  @org.junit.Test
  public void parenexpr11() {
    final XQuery query = new XQuery(
      "(for $x in (1) where (fn:true()) order by ($x) return ($x))",
      ctx);
    try {
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
   *  if expression .
   */
  @org.junit.Test
  public void parenexpr12() {
    final XQuery query = new XQuery(
      "for $x in (1,2) return (if (($x eq 1)) then ($x) else ($x + 1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 3")
    );
  }

  /**
   *  Literal .
   */
  @org.junit.Test
  public void parenexpr13() {
    final XQuery query = new XQuery(
      "(1)",
      ctx);
    try {
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
   *  Sequence .
   */
  @org.junit.Test
  public void parenexpr14() {
    final XQuery query = new XQuery(
      "(1, (2, (3, 4)), (5))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4 5")
    );
  }

  /**
   *  union and except .
   */
  @org.junit.Test
  public void parenexpr15() {
    final XQuery query = new XQuery(
      "<elem>{//node() | (//node() except //comment())}</elem>",
      ctx);
    try {
      query.context(node(file("prod/CompAttrConstructor/DupNode.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem><root><child1><a>text</a><!--comment--><?pi content?></child1><child2><a>text</a><!--comment--><?pi content?></child2></root><child1><a>text</a><!--comment--><?pi content?></child1><a>text</a>text<!--comment--><?pi content?><child2><a>text</a><!--comment--><?pi content?></child2><a>text</a>text<!--comment--><?pi content?></elem>", false)
    );
  }

  /**
   *  union and except .
   */
  @org.junit.Test
  public void parenexpr16() {
    final XQuery query = new XQuery(
      "<elem>{(//node() | //node()) except //comment()}</elem>",
      ctx);
    try {
      query.context(node(file("prod/CompAttrConstructor/DupNode.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem><root><child1><a>text</a><!--comment--><?pi content?></child1><child2><a>text</a><!--comment--><?pi content?></child2></root><child1><a>text</a><!--comment--><?pi content?></child1><a>text</a>text<?pi content?><child2><a>text</a><!--comment--><?pi content?></child2><a>text</a>text<?pi content?></elem>", false)
    );
  }

  /**
   *  Constructor .
   */
  @org.junit.Test
  public void parenexpr17() {
    final XQuery query = new XQuery(
      "(<elem/>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem/>", false)
    );
  }

  /**
   *  Constructor .
   */
  @org.junit.Test
  public void parenexpr18() {
    final XQuery query = new XQuery(
      "<elem attr=\"{(1)}\">{(<child/>),(<child/>)}</elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem attr=\"1\"><child/><child/></elem>", false)
    );
  }

  /**
   *  Input context .
   */
  @org.junit.Test
  public void parenexpr19() {
    final XQuery query = new XQuery(
      "(.)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/Tree1Child.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("name($result/*) = \"far-north\"")
    );
  }

  /**
   *  Arithmetic operations .
   */
  @org.junit.Test
  public void parenexpr2() {
    final XQuery query = new XQuery(
      "1 + (2 * 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("7")
    );
  }

  /**
   *  Path expression .
   */
  @org.junit.Test
  public void parenexpr20() {
    final XQuery query = new XQuery(
      "<elem>{(//(north)/(/)//(@mark)[(1)]/(.)/(..))}</elem>",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/Tree1Child.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem><west mark=\"w0\"/><center mark=\"c0\"><the1child/></center><east mark=\"e0\">Text in east</east></elem>", false)
    );
  }

  /**
   *  Arithmetic operations .
   */
  @org.junit.Test
  public void parenexpr3() {
    final XQuery query = new XQuery(
      "-(2 + 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-7")
    );
  }

  /**
   *  Arithmetic operations .
   */
  @org.junit.Test
  public void parenexpr4() {
    final XQuery query = new XQuery(
      "(-2) + 5",
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
   *  Arithmetic operations .
   */
  @org.junit.Test
  public void parenexpr5() {
    final XQuery query = new XQuery(
      "2 + (4 idiv 2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("4")
    );
  }

  /**
   *  Arithmetic operations .
   */
  @org.junit.Test
  public void parenexpr6() {
    final XQuery query = new XQuery(
      "(2 + 4) idiv 2",
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
   *  Arithmetic operations .
   */
  @org.junit.Test
  public void parenexpr7() {
    final XQuery query = new XQuery(
      "2 * (5 mod 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("4")
    );
  }

  /**
   *  Arithmetic operations .
   */
  @org.junit.Test
  public void parenexpr8() {
    final XQuery query = new XQuery(
      "(2 * 5) mod 3",
      ctx);
    try {
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
   *  Logical expressions .
   */
  @org.junit.Test
  public void parenexpr9() {
    final XQuery query = new XQuery(
      "(fn:true() or fn:true()) and fn:false()",
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
}
