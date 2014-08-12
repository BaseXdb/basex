package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the expr-AxisStep() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdAxisStep extends QT3TestSet {

  /**
   *  Path 'child::*' from an element..
   */
  @org.junit.Test
  public void axes0011() {
    final XQuery query = new XQuery(
      "fn:count(//center/child::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path 'child::*' from an element..
   */
  @org.junit.Test
  public void axes0012() {
    final XQuery query = new XQuery(
      "fn:count(//center/child::*)",
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
      assertEq("1")
    );
  }

  /**
   *  Path 'child::*' from an element..
   */
  @org.junit.Test
  public void axes0013() {
    final XQuery query = new XQuery(
      "fn:count(//center/child::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeRepeat.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("6")
    );
  }

  /**
   *  Path 'child::' with element name from an element..
   */
  @org.junit.Test
  public void axes0021() {
    final XQuery query = new XQuery(
      "fn:count(//center/child::south-east)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path 'child::' with element name from an element..
   */
  @org.junit.Test
  public void axes0022() {
    final XQuery query = new XQuery(
      "fn:count(//center/child::south-east)",
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
      assertEq("0")
    );
  }

  /**
   *  Path 'child::' with element name from an element..
   */
  @org.junit.Test
  public void axes0023() {
    final XQuery query = new XQuery(
      "fn:count(//center/child::south-east)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path 'child::' with element name from an element..
   */
  @org.junit.Test
  public void axes0024() {
    final XQuery query = new XQuery(
      "fn:count(//center/child::south-east)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeRepeat.xml")));
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
   *  Path 'child::node()' from an element..
   */
  @org.junit.Test
  public void axes0031() {
    final XQuery query = new XQuery(
      "fn:count(//center/child::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path 'child::node()' from an element..
   */
  @org.junit.Test
  public void axes0032() {
    final XQuery query = new XQuery(
      "fn:count(//center/child::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/Tree1Text.xml")));
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
   *  Path 'child::node()' from an element..
   */
  @org.junit.Test
  public void axes0033() {
    final XQuery query = new XQuery(
      "fn:count(//center/child::node())",
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
      assertEq("1")
    );
  }

  /**
   *  Path 'child::node()' from an element..
   */
  @org.junit.Test
  public void axes0034() {
    final XQuery query = new XQuery(
      "fn:count(//center/child::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeRepeat.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("19")
    );
  }

  /**
   *  Test implied child axis in '*' from an element..
   */
  @org.junit.Test
  public void axes0041() {
    final XQuery query = new XQuery(
      "fn:count(//center/*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Test implied child axis in '*' from an element..
   */
  @org.junit.Test
  public void axes0042() {
    final XQuery query = new XQuery(
      "fn:count(//center/*)",
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
      assertEq("1")
    );
  }

  /**
   *  Test implied child axis in '*' from an element..
   */
  @org.junit.Test
  public void axes0043() {
    final XQuery query = new XQuery(
      "fn:count(//center/*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeRepeat.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("6")
    );
  }

  /**
   *  Test implied child axis with element name, from an element..
   */
  @org.junit.Test
  public void axes0051() {
    final XQuery query = new XQuery(
      "fn:count(//center/south-east)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Test implied child axis with element name, from an element..
   */
  @org.junit.Test
  public void axes0052() {
    final XQuery query = new XQuery(
      "fn:count(//center/south-east)",
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
      assertEq("0")
    );
  }

  /**
   *  Test implied child axis with element name, from an element..
   */
  @org.junit.Test
  public void axes0053() {
    final XQuery query = new XQuery(
      "fn:count(//center/south-east)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Test implied child axis with element name, from an element..
   */
  @org.junit.Test
  public void axes0054() {
    final XQuery query = new XQuery(
      "fn:count(//center/south-east)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeRepeat.xml")));
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
   *  Test implied child axis in 'node()' from an element..
   */
  @org.junit.Test
  public void axes0061() {
    final XQuery query = new XQuery(
      "fn:count(//center/node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Test implied child axis in 'node()' from an element..
   */
  @org.junit.Test
  public void axes0062() {
    final XQuery query = new XQuery(
      "fn:count(//center/node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/Tree1Text.xml")));
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
   *  Test implied child axis in 'node()' from an element..
   */
  @org.junit.Test
  public void axes0063() {
    final XQuery query = new XQuery(
      "fn:count(//center/node())",
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
      assertEq("1")
    );
  }

  /**
   *  Test implied child axis in 'node()' from an element..
   */
  @org.junit.Test
  public void axes0064() {
    final XQuery query = new XQuery(
      "fn:count(//center/node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeRepeat.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("19")
    );
  }

  /**
   *  Path 'attribute::*' from an element..
   */
  @org.junit.Test
  public void axes0071() {
    final XQuery query = new XQuery(
      "fn:count(//west/attribute::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path 'attribute::*' from an element..
   */
  @org.junit.Test
  public void axes0072() {
    final XQuery query = new XQuery(
      "fn:count(//west/attribute::*)",
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
      assertEq("1")
    );
  }

  /**
   *  Path 'attribute::*' from an element..
   */
  @org.junit.Test
  public void axes0073() {
    final XQuery query = new XQuery(
      "fn:count(//west/attribute::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path 'attribute::' with name of attribute, from an element..
   */
  @org.junit.Test
  public void axes0081() {
    final XQuery query = new XQuery(
      "fn:count(//west/attribute::west-attr-2)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path 'attribute::' with name of attribute, from an element..
   */
  @org.junit.Test
  public void axes0082() {
    final XQuery query = new XQuery(
      "fn:count(//west/attribute::west-attr-2)",
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
      assertEq("0")
    );
  }

  /**
   *  Path 'attribute::' with name of attribute, from an element..
   */
  @org.junit.Test
  public void axes0083() {
    final XQuery query = new XQuery(
      "fn:count(//west/attribute::west-attr-2)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path 'attribute::node()' from an element..
   */
  @org.junit.Test
  public void axes0091() {
    final XQuery query = new XQuery(
      "fn:count(//west/attribute::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path 'attribute::node()' from an element..
   */
  @org.junit.Test
  public void axes0092() {
    final XQuery query = new XQuery(
      "fn:count(//west/attribute::node())",
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
      assertEq("1")
    );
  }

  /**
   *  Path 'attribute::node()' from an element..
   */
  @org.junit.Test
  public void axes0093() {
    final XQuery query = new XQuery(
      "fn:count(//west/attribute::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path '@*' (abbreviated syntax) from an element..
   */
  @org.junit.Test
  public void axes0101() {
    final XQuery query = new XQuery(
      "fn:count(//west/@*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path '@*' (abbreviated syntax) from an element..
   */
  @org.junit.Test
  public void axes0102() {
    final XQuery query = new XQuery(
      "fn:count(//west/@*)",
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
      assertEq("1")
    );
  }

  /**
   *  Path '@*' (abbreviated syntax) from an element..
   */
  @org.junit.Test
  public void axes0103() {
    final XQuery query = new XQuery(
      "fn:count(//west/@*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path '@' with name of attribute, from an element..
   */
  @org.junit.Test
  public void axes0111() {
    final XQuery query = new XQuery(
      "fn:count(//west/@west-attr-2)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path '@' with name of attribute, from an element..
   */
  @org.junit.Test
  public void axes0112() {
    final XQuery query = new XQuery(
      "fn:count(//west/@west-attr-2)",
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
      assertEq("0")
    );
  }

  /**
   *  Path '@' with name of attribute, from an element..
   */
  @org.junit.Test
  public void axes0113() {
    final XQuery query = new XQuery(
      "fn:count(//west/@west-attr-2)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Test '/' alone..
   */
  @org.junit.Test
  public void axes0121() {
    final XQuery query = new XQuery(
      "fn:count( / )",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path 'parent::*' from an element..
   */
  @org.junit.Test
  public void axes0131() {
    final XQuery query = new XQuery(
      "fn:count(//center/parent::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path 'parent::*' from document element..
   */
  @org.junit.Test
  public void axes0141() {
    final XQuery query = new XQuery(
      "fn:count(/far-north/parent::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path 'parent::' with specified element name, from an element..
   */
  @org.junit.Test
  public void axes0151() {
    final XQuery query = new XQuery(
      "fn:count(//center/parent::near-north)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path 'parent::' with specified element name that is not found, from an element..
   */
  @org.junit.Test
  public void axes0161() {
    final XQuery query = new XQuery(
      "fn:count(//center/parent::nowhere)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path 'parent::node()' from an element..
   */
  @org.junit.Test
  public void axes0171() {
    final XQuery query = new XQuery(
      "fn:count(//center/parent::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path 'parent::node()' from document element..
   */
  @org.junit.Test
  public void axes0181() {
    final XQuery query = new XQuery(
      "fn:count(/far-north/parent::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Test abbreviated '..' syntax from an element..
   */
  @org.junit.Test
  public void axes0191() {
    final XQuery query = new XQuery(
      "fn:count(//center/..)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path 'self::*' from an element..
   */
  @org.junit.Test
  public void axes0201() {
    final XQuery query = new XQuery(
      "fn:count(//center/self::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path 'self::' with specified element name, from an element..
   */
  @org.junit.Test
  public void axes0211() {
    final XQuery query = new XQuery(
      "fn:count(//center/self::center)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path 'self::node()' from an element..
   */
  @org.junit.Test
  public void axes0231() {
    final XQuery query = new XQuery(
      "fn:count(//center/self::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path 'self::node()' axis from an attribute..
   */
  @org.junit.Test
  public void axes0271() {
    final XQuery query = new XQuery(
      "fn:count(//center/@center-attr-3/self::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path 'self::node()' from a text node..
   */
  @org.junit.Test
  public void axes0301() {
    final XQuery query = new XQuery(
      "fn:count(//center/text()/self::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path 'self::node()' from a text node..
   */
  @org.junit.Test
  public void axes0302() {
    final XQuery query = new XQuery(
      "fn:count(//center/text()/self::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/Tree1Text.xml")));
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
   *  Path 'descendant::*' from an element..
   */
  @org.junit.Test
  public void axes0311() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path 'descendant::*' from an element..
   */
  @org.junit.Test
  public void axes0312() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/Tree1Text.xml")));
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
   *  Path 'descendant::*' from an element..
   */
  @org.junit.Test
  public void axes0313() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant::*)",
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
      assertEq("1")
    );
  }

  /**
   *  Path 'descendant::*' from an element..
   */
  @org.junit.Test
  public void axes0314() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("5")
    );
  }

  /**
   *  Path 'descendant::' with specified element name, from an element..
   */
  @org.junit.Test
  public void axes0321() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path 'descendant::' with specified element name, from an element..
   */
  @org.junit.Test
  public void axes0322() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant::south)",
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
      assertEq("0")
    );
  }

  /**
   *  Path 'descendant::' with specified element name, from an element..
   */
  @org.junit.Test
  public void axes0323() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path 'descendant::' with specified element name, from an element..
   */
  @org.junit.Test
  public void axes0324() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeStack.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("8")
    );
  }

  /**
   *  Path 'descendant::node()' from an element..
   */
  @org.junit.Test
  public void axes0331() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path 'descendant::node()' from an element..
   */
  @org.junit.Test
  public void axes0332() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/Tree1Text.xml")));
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
   *  Path 'descendant::node()' from an element..
   */
  @org.junit.Test
  public void axes0333() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant::node())",
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
      assertEq("1")
    );
  }

  /**
   *  Path 'descendant::node()' from an element..
   */
  @org.junit.Test
  public void axes0334() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("21")
    );
  }

  /**
   *  Path 'descendant-or-self::*' from an element..
   */
  @org.junit.Test
  public void axes0341() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant-or-self::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path 'descendant-or-self::*' from an element..
   */
  @org.junit.Test
  public void axes0342() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant-or-self::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("6")
    );
  }

  /**
   *  Path 'descendant-or-self::' with specified element name, from an element..
   */
  @org.junit.Test
  public void axes0351() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant-or-self::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path 'descendant-or-self::' with specified element name, from an element..
   */
  @org.junit.Test
  public void axes0352() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant-or-self::south)",
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
      assertEq("0")
    );
  }

  /**
   *  Path 'descendant-or-self::' with specified element name, from an element..
   */
  @org.junit.Test
  public void axes0353() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant-or-self::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path 'descendant-or-self::' with specified element name, from an element..
   */
  @org.junit.Test
  public void axes0354() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant-or-self::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeStack.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("8")
    );
  }

  /**
   *  Path 'descendant-or-self::' with name of self specified..
   */
  @org.junit.Test
  public void axes0361() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant-or-self::center)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path 'descendant-or-self::' with name of self specified..
   */
  @org.junit.Test
  public void axes0362() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant-or-self::center)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeRepeat.xml")));
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
   *  Path 'descendant-or-self::node()' from an element..
   */
  @org.junit.Test
  public void axes0371() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant-or-self::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path 'descendant-or-self::node()' from an element..
   */
  @org.junit.Test
  public void axes0372() {
    final XQuery query = new XQuery(
      "fn:count(//center/descendant-or-self::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("22")
    );
  }

  /**
   *  Path 'descendant-or-self::node()' from an attribute..
   */
  @org.junit.Test
  public void axes0411() {
    final XQuery query = new XQuery(
      "fn:count(//center/@center-attr-3/descendant-or-self::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path 'descendant-or-self::node()' from a text node..
   */
  @org.junit.Test
  public void axes0431() {
    final XQuery query = new XQuery(
      "fn:count(//center/text()/descendant-or-self::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path 'descendant-or-self::node()' from a text node..
   */
  @org.junit.Test
  public void axes0432() {
    final XQuery query = new XQuery(
      "fn:count(//center/text()/descendant-or-self::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/Tree1Text.xml")));
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
   *  Test '/child::*' absolute path..
   */
  @org.junit.Test
  public void axes0441() {
    final XQuery query = new XQuery(
      "fn:count(/child::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Test '/child::*' absolute path..
   */
  @org.junit.Test
  public void axes0442() {
    final XQuery query = new XQuery(
      "fn:count(/child::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
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
   *  Absolute path '/child::' with element name..
   */
  @org.junit.Test
  public void axes0451() {
    final XQuery query = new XQuery(
      "fn:count(/child::far-north)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  Absolute path '/child::' with element name..
   */
  @org.junit.Test
  public void axes0452() {
    final XQuery query = new XQuery(
      "fn:count(/child::far-north)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Test '/child::node()' absolute path..
   */
  @org.junit.Test
  public void axes0461() {
    final XQuery query = new XQuery(
      "fn:count(/child::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Test '/child::node()' absolute path..
   */
  @org.junit.Test
  public void axes0462() {
    final XQuery query = new XQuery(
      "fn:count(/child::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
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
   *  Test implied child axis in '/*' absolute path..
   */
  @org.junit.Test
  public void axes0471() {
    final XQuery query = new XQuery(
      "fn:count(/*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Test implied child axis in '/*' absolute path..
   */
  @org.junit.Test
  public void axes0472() {
    final XQuery query = new XQuery(
      "fn:count(/*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
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
   *  Test implied child axis with element name after /..
   */
  @org.junit.Test
  public void axes0481() {
    final XQuery query = new XQuery(
      "fn:count(/far-north)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  Test implied child axis with element name after /..
   */
  @org.junit.Test
  public void axes0482() {
    final XQuery query = new XQuery(
      "fn:count(/far-north)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Test implied child axis in '/node()' absolute path..
   */
  @org.junit.Test
  public void axes0491() {
    final XQuery query = new XQuery(
      "fn:count(/node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Test implied child axis in '/node()' absolute path..
   */
  @org.junit.Test
  public void axes0492() {
    final XQuery query = new XQuery(
      "fn:count(/node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
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
   *  Test '/self::node()' absolute path..
   */
  @org.junit.Test
  public void axes0551() {
    final XQuery query = new XQuery(
      "fn:count(/self::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
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
   *  Test '/descendant::*' absolute path..
   */
  @org.junit.Test
  public void axes0561() {
    final XQuery query = new XQuery(
      "fn:count(/descendant::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  Test '/descendant::*' absolute path..
   */
  @org.junit.Test
  public void axes0562() {
    final XQuery query = new XQuery(
      "fn:count(/descendant::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("15")
    );
  }

  /**
   *  Test '/descendant::*' absolute path..
   */
  @org.junit.Test
  public void axes0563() {
    final XQuery query = new XQuery(
      "fn:count(/descendant::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("16")
    );
  }

  /**
   *  Absolute path '/descendant::' with specified element name..
   */
  @org.junit.Test
  public void axes0571() {
    final XQuery query = new XQuery(
      "fn:count(/descendant::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Absolute path '/descendant::' with specified element name..
   */
  @org.junit.Test
  public void axes0572() {
    final XQuery query = new XQuery(
      "fn:count(/descendant::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  Absolute path '/descendant::' with specified element name..
   */
  @org.junit.Test
  public void axes0573() {
    final XQuery query = new XQuery(
      "fn:count(/descendant::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Absolute path '/descendant::' with specified element name..
   */
  @org.junit.Test
  public void axes0574() {
    final XQuery query = new XQuery(
      "fn:count(/descendant::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeStack.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("8")
    );
  }

  /**
   *  Test '/descendant::node()' absolute path..
   */
  @org.junit.Test
  public void axes0581() {
    final XQuery query = new XQuery(
      "fn:count(/descendant::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  Test '/descendant::node()' absolute path..
   */
  @org.junit.Test
  public void axes0582() {
    final XQuery query = new XQuery(
      "fn:count(/descendant::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("56")
    );
  }

  /**
   *  Test '/descendant::node()' absolute path..
   */
  @org.junit.Test
  public void axes0583() {
    final XQuery query = new XQuery(
      "fn:count(/descendant::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("58")
    );
  }

  /**
   *  Test '/descendant-or-self::*' absolute path..
   */
  @org.junit.Test
  public void axes0591() {
    final XQuery query = new XQuery(
      "fn:count(/descendant-or-self::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  Test '/descendant-or-self::*' absolute path..
   */
  @org.junit.Test
  public void axes0592() {
    final XQuery query = new XQuery(
      "fn:count(/descendant-or-self::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("15")
    );
  }

  /**
   *  Absolute path '/descendant-or-self::' with specified element name..
   */
  @org.junit.Test
  public void axes0601() {
    final XQuery query = new XQuery(
      "fn:count(/descendant-or-self::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Absolute path '/descendant-or-self::' with specified element name..
   */
  @org.junit.Test
  public void axes0602() {
    final XQuery query = new XQuery(
      "fn:count(/descendant-or-self::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  Absolute path '/descendant-or-self::' with specified element name..
   */
  @org.junit.Test
  public void axes0603() {
    final XQuery query = new XQuery(
      "fn:count(/descendant-or-self::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Absolute path '/descendant-or-self::' with specified element name..
   */
  @org.junit.Test
  public void axes0604() {
    final XQuery query = new XQuery(
      "fn:count(/descendant-or-self::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeStack.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("8")
    );
  }

  /**
   *  Test '/descendant-or-self::node()' absolute path..
   */
  @org.junit.Test
  public void axes0611() {
    final XQuery query = new XQuery(
      "fn:count(/descendant-or-self::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("57")
    );
  }

  /**
   *  Test '/descendant-or-self::node()' absolute path..
   */
  @org.junit.Test
  public void axes0612() {
    final XQuery query = new XQuery(
      "fn:count(/descendant-or-self::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("59")
    );
  }

  /**
   *  Test '//child::*' absolute path..
   */
  @org.junit.Test
  public void axes0621() {
    final XQuery query = new XQuery(
      "fn:count(//child::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  Test '//child::*' absolute path..
   */
  @org.junit.Test
  public void axes0622() {
    final XQuery query = new XQuery(
      "fn:count(//child::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("15")
    );
  }

  /**
   *  Absolute path '//child::' with specified element name..
   */
  @org.junit.Test
  public void axes0631() {
    final XQuery query = new XQuery(
      "fn:count(//child::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Absolute path '//child::' with specified element name..
   */
  @org.junit.Test
  public void axes0632() {
    final XQuery query = new XQuery(
      "fn:count(//child::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  Absolute path '//child::' with specified element name..
   */
  @org.junit.Test
  public void axes0633() {
    final XQuery query = new XQuery(
      "fn:count(//child::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Absolute path '//child::' with specified element name..
   */
  @org.junit.Test
  public void axes0634() {
    final XQuery query = new XQuery(
      "fn:count(//child::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeStack.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("8")
    );
  }

  /**
   *  Test '//child::node()' absolute path..
   */
  @org.junit.Test
  public void axes0641() {
    final XQuery query = new XQuery(
      "fn:count(//child::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  Test '//child::node()' absolute path..
   */
  @org.junit.Test
  public void axes0642() {
    final XQuery query = new XQuery(
      "fn:count(//child::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("56")
    );
  }

  /**
   *  Test '//child::node()' absolute path..
   */
  @org.junit.Test
  public void axes0643() {
    final XQuery query = new XQuery(
      "fn:count(//child::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("58")
    );
  }

  /**
   *  Test implied child axis in '//*' absolute path..
   */
  @org.junit.Test
  public void axes0651() {
    final XQuery query = new XQuery(
      "fn:count(//*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  Test implied child axis in '//*' absolute path..
   */
  @org.junit.Test
  public void axes0652() {
    final XQuery query = new XQuery(
      "fn:count(//*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("15")
    );
  }

  /**
   *  Absolute path with element name after // implies child axis..
   */
  @org.junit.Test
  public void axes0661() {
    final XQuery query = new XQuery(
      "fn:count(//south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Absolute path with element name after // implies child axis..
   */
  @org.junit.Test
  public void axes0662() {
    final XQuery query = new XQuery(
      "fn:count(//south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  Absolute path with element name after // implies child axis..
   */
  @org.junit.Test
  public void axes0663() {
    final XQuery query = new XQuery(
      "fn:count(//south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Absolute path with element name after // implies child axis..
   */
  @org.junit.Test
  public void axes0664() {
    final XQuery query = new XQuery(
      "fn:count(//south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeStack.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("8")
    );
  }

  /**
   *  Test implied child axis in '//node()' absolute path..
   */
  @org.junit.Test
  public void axes0671() {
    final XQuery query = new XQuery(
      "fn:count(//node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  Test implied child axis in '//node()' absolute path..
   */
  @org.junit.Test
  public void axes0672() {
    final XQuery query = new XQuery(
      "fn:count(//node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("56")
    );
  }

  /**
   *  Test implied child axis in '//node()' absolute path..
   */
  @org.junit.Test
  public void axes0673() {
    final XQuery query = new XQuery(
      "fn:count(//node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("58")
    );
  }

  /**
   *  Test '//attribute::*' absolute path..
   */
  @org.junit.Test
  public void axes0681() {
    final XQuery query = new XQuery(
      "fn:count(//attribute::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Test '//attribute::*' absolute path..
   */
  @org.junit.Test
  public void axes0682() {
    final XQuery query = new XQuery(
      "fn:count(//attribute::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  Test '//attribute::*' absolute path..
   */
  @org.junit.Test
  public void axes0683() {
    final XQuery query = new XQuery(
      "fn:count(//attribute::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("14")
    );
  }

  /**
   *  Absolute path '//attribute::' with specified name..
   */
  @org.junit.Test
  public void axes0691() {
    final XQuery query = new XQuery(
      "fn:count(//attribute::mark)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Absolute path '//attribute::' with specified name..
   */
  @org.junit.Test
  public void axes0692() {
    final XQuery query = new XQuery(
      "fn:count(//attribute::mark)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  Absolute path '//attribute::' with specified name..
   */
  @org.junit.Test
  public void axes0693() {
    final XQuery query = new XQuery(
      "fn:count(//attribute::mark)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("6")
    );
  }

  /**
   *  Absolute path '//@*' has abbreviated syntax..
   */
  @org.junit.Test
  public void axes0701() {
    final XQuery query = new XQuery(
      "fn:count(//@*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Absolute path '//@*' has abbreviated syntax..
   */
  @org.junit.Test
  public void axes0702() {
    final XQuery query = new XQuery(
      "fn:count(//@*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  Absolute path '//@*' has abbreviated syntax..
   */
  @org.junit.Test
  public void axes0703() {
    final XQuery query = new XQuery(
      "fn:count(//@*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("14")
    );
  }

  /**
   *  Absolute path '//@' with specified name..
   */
  @org.junit.Test
  public void axes0711() {
    final XQuery query = new XQuery(
      "fn:count(//@mark)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Absolute path '//@' with specified name..
   */
  @org.junit.Test
  public void axes0712() {
    final XQuery query = new XQuery(
      "fn:count(//@mark)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  Absolute path '//@' with specified name..
   */
  @org.junit.Test
  public void axes0713() {
    final XQuery query = new XQuery(
      "fn:count(//@mark)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("6")
    );
  }

  /**
   *  Test '//self::*' absolute path..
   */
  @org.junit.Test
  public void axes0721() {
    final XQuery query = new XQuery(
      "fn:count(//self::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeEmpty.xml")));
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
   *  Test '//self::*' absolute path..
   */
  @org.junit.Test
  public void axes0722() {
    final XQuery query = new XQuery(
      "fn:count(//self::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("15")
    );
  }

  /**
   *  Test '//self::node()' absolute path..
   */
  @org.junit.Test
  public void axes0731() {
    final XQuery query = new XQuery(
      "fn:count(//self::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("57")
    );
  }

  /**
   *  Test '//self::node()' absolute path..
   */
  @org.junit.Test
  public void axes0732() {
    final XQuery query = new XQuery(
      "fn:count(//self::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("59")
    );
  }

  /**
   *  Path '//child::*' from an element..
   */
  @org.junit.Test
  public void axes0741() {
    final XQuery query = new XQuery(
      "fn:count(//center//child::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path '//child::*' from an element..
   */
  @org.junit.Test
  public void axes0742() {
    final XQuery query = new XQuery(
      "fn:count(//center//child::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/Tree1Text.xml")));
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
   *  Path '//child::*' from an element..
   */
  @org.junit.Test
  public void axes0743() {
    final XQuery query = new XQuery(
      "fn:count(//center//child::*)",
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
      assertEq("1")
    );
  }

  /**
   *  Path '//child::*' from an element..
   */
  @org.junit.Test
  public void axes0744() {
    final XQuery query = new XQuery(
      "fn:count(//center//child::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeRepeat.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("12")
    );
  }

  /**
   *  Path '//child::' with element name, from an element..
   */
  @org.junit.Test
  public void axes0751() {
    final XQuery query = new XQuery(
      "fn:count(//center//child::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path '//child::' with element name, from an element..
   */
  @org.junit.Test
  public void axes0752() {
    final XQuery query = new XQuery(
      "fn:count(//center//child::south)",
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
      assertEq("0")
    );
  }

  /**
   *  Path '//child::' with element name, from an element..
   */
  @org.junit.Test
  public void axes0753() {
    final XQuery query = new XQuery(
      "fn:count(//center//child::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path '//child::' with element name, from an element..
   */
  @org.junit.Test
  public void axes0754() {
    final XQuery query = new XQuery(
      "fn:count(//center//child::south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeStack.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("8")
    );
  }

  /**
   *  Path '//child::node()' from an element..
   */
  @org.junit.Test
  public void axes0761() {
    final XQuery query = new XQuery(
      "fn:count(//center//child::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path '//child::node()' from an element..
   */
  @org.junit.Test
  public void axes0762() {
    final XQuery query = new XQuery(
      "fn:count(//center//child::node())",
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
      assertEq("1")
    );
  }

  /**
   *  Path '//child::node()' from an element..
   */
  @org.junit.Test
  public void axes0763() {
    final XQuery query = new XQuery(
      "fn:count(//center//child::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/Tree1Text.xml")));
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
   *  Path '//child::node()' from an element..
   */
  @org.junit.Test
  public void axes0764() {
    final XQuery query = new XQuery(
      "fn:count(//center//child::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeRepeat.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("37")
    );
  }

  /**
   *  Test implied child axis in '//*' after an element..
   */
  @org.junit.Test
  public void axes0771() {
    final XQuery query = new XQuery(
      "fn:count(//center//*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Test implied child axis in '//*' after an element..
   */
  @org.junit.Test
  public void axes0772() {
    final XQuery query = new XQuery(
      "fn:count(//center//*)",
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
      assertEq("1")
    );
  }

  /**
   *  Test implied child axis in '//*' after an element..
   */
  @org.junit.Test
  public void axes0773() {
    final XQuery query = new XQuery(
      "fn:count(//center//*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeRepeat.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("12")
    );
  }

  /**
   *  Test implied child axis in element//element..
   */
  @org.junit.Test
  public void axes0781() {
    final XQuery query = new XQuery(
      "fn:count(//center//south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Test implied child axis in element//element..
   */
  @org.junit.Test
  public void axes0782() {
    final XQuery query = new XQuery(
      "fn:count(//center//south)",
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
      assertEq("0")
    );
  }

  /**
   *  Test implied child axis in element//element..
   */
  @org.junit.Test
  public void axes0783() {
    final XQuery query = new XQuery(
      "fn:count(//center//south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Test implied child axis in element//element..
   */
  @org.junit.Test
  public void axes0784() {
    final XQuery query = new XQuery(
      "fn:count(//center//south)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeStack.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("8")
    );
  }

  /**
   *  Test implied child axis in '//node()' from an element..
   */
  @org.junit.Test
  public void axes0791() {
    final XQuery query = new XQuery(
      "fn:count(//center//node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Test implied child axis in '//node()' from an element..
   */
  @org.junit.Test
  public void axes0792() {
    final XQuery query = new XQuery(
      "fn:count(//center//node())",
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
      assertEq("1")
    );
  }

  /**
   *  Test implied child axis in '//node()' from an element..
   */
  @org.junit.Test
  public void axes0793() {
    final XQuery query = new XQuery(
      "fn:count(//center//node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/Tree1Text.xml")));
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
   *  Test implied child axis in '//node()' from an element..
   */
  @org.junit.Test
  public void axes0794() {
    final XQuery query = new XQuery(
      "fn:count(//center//node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeRepeat.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("37")
    );
  }

  /**
   *  Path '//attribute::*' after an element..
   */
  @org.junit.Test
  public void axes0801() {
    final XQuery query = new XQuery(
      "fn:count(//west//attribute::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path '//attribute::*' after an element..
   */
  @org.junit.Test
  public void axes0802() {
    final XQuery query = new XQuery(
      "fn:count(//west//attribute::*)",
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
      assertEq("1")
    );
  }

  /**
   *  Path '//attribute::*' after an element..
   */
  @org.junit.Test
  public void axes0803() {
    final XQuery query = new XQuery(
      "fn:count(//west//attribute::*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path '//attribute::' with name of attribute, after an element..
   */
  @org.junit.Test
  public void axes0811() {
    final XQuery query = new XQuery(
      "fn:count(//center//attribute::center-attr-2)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path '//attribute::' with name of attribute, after an element..
   */
  @org.junit.Test
  public void axes0812() {
    final XQuery query = new XQuery(
      "fn:count(//center//attribute::center-attr-2)",
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
      assertEq("0")
    );
  }

  /**
   *  Path '//attribute::' with name of attribute, after an element..
   */
  @org.junit.Test
  public void axes0813() {
    final XQuery query = new XQuery(
      "fn:count(//center//attribute::center-attr-2)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path '//attribute::' with name of attribute, after an element..
   */
  @org.junit.Test
  public void axes0814() {
    final XQuery query = new XQuery(
      "fn:count(//center//attribute::center-attr-2)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeRepeat.xml")));
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
   *  Path '//attribute::node()' after an element..
   */
  @org.junit.Test
  public void axes0821() {
    final XQuery query = new XQuery(
      "fn:count(//west//attribute::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path '//attribute::node()' after an element..
   */
  @org.junit.Test
  public void axes0822() {
    final XQuery query = new XQuery(
      "fn:count(//west//attribute::node())",
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
      assertEq("1")
    );
  }

  /**
   *  Path '//attribute::node()' after an element..
   */
  @org.junit.Test
  public void axes0823() {
    final XQuery query = new XQuery(
      "fn:count(//west//attribute::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path '//@*' (abbreviated syntax) after an element..
   */
  @org.junit.Test
  public void axes0831() {
    final XQuery query = new XQuery(
      "fn:count(//west//@*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path '//@*' (abbreviated syntax) after an element..
   */
  @org.junit.Test
  public void axes0832() {
    final XQuery query = new XQuery(
      "fn:count(//west//@*)",
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
      assertEq("1")
    );
  }

  /**
   *  Path '//@*' (abbreviated syntax) after an element..
   */
  @org.junit.Test
  public void axes0833() {
    final XQuery query = new XQuery(
      "fn:count(//west//@*)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path '//@' with name of attribute, after an element..
   */
  @org.junit.Test
  public void axes0841() {
    final XQuery query = new XQuery(
      "fn:count(//center//@center-attr-2)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Path '//@' with name of attribute, after an element..
   */
  @org.junit.Test
  public void axes0842() {
    final XQuery query = new XQuery(
      "fn:count(//center//@center-attr-2)",
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
      assertEq("0")
    );
  }

  /**
   *  Path '//@' with name of attribute, after an element..
   */
  @org.junit.Test
  public void axes0843() {
    final XQuery query = new XQuery(
      "fn:count(//center//@center-attr-2)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeCompass.xml")));
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
   *  Path '//@' with name of attribute, after an element..
   */
  @org.junit.Test
  public void axes0844() {
    final XQuery query = new XQuery(
      "fn:count(//center//@center-attr-2)",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeRepeat.xml")));
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
   *  Use of // to get all elements of a given name.
   */
  @org.junit.Test
  public void axes085() {
    final XQuery query = new XQuery(
      "<root> {//Customers} </root>",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/nw_Customers.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<root><Customers CustomerID=\"ALFKI\">\n\t\t<CompanyName>Alfreds Futterkiste</CompanyName>\n\t\t<ContactName>Maria Anders</ContactName>\n\t\t<ContactTitle>Sales Representative</ContactTitle>\n\t\t<Phone>030-0074321</Phone>\n\t\t<Fax>030-0076545</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Obere Str. 57</Address>\n\t\t\t<City>Berlin</City>\n\t\t\t<PostalCode>12209</PostalCode>\n\t\t\t<Country>Germany</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"ANATR\">\n\t\t<CompanyName>Ana Trujillo Emparedados y helados</CompanyName>\n\t\t<ContactName>Ana Trujillo</ContactName>\n\t\t<ContactTitle>Owner</ContactTitle>\n\t\t<Phone>(5) 555-4729</Phone>\n\t\t<Fax>(5) 555-3745</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Avda. de la Constituci&#243;n 2222</Address>\n\t\t\t<City>M&#233;xico D.F.</City>\n\t\t\t<PostalCode>05021</PostalCode>\n\t\t\t<Country>Mexico</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"ANTON\">\n\t\t<CompanyName>Antonio Moreno Taquer&#237;a</CompanyName>\n\t\t<ContactName>Antonio Moreno</ContactName>\n\t\t<ContactTitle>Owner</ContactTitle>\n\t\t<Phone>(5) 555-3932</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>Mataderos  2312</Address>\n\t\t\t<City>M&#233;xico D.F.</City>\n\t\t\t<PostalCode>05023</PostalCode>\n\t\t\t<Country>Mexico</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"AROUT\">\n\t\t<CompanyName>Around the Horn</CompanyName>\n\t\t<ContactName>Thomas Hardy</ContactName>\n\t\t<ContactTitle>Sales Representative</ContactTitle>\n\t\t<Phone>(171) 555-7788</Phone>\n\t\t<Fax>(171) 555-6750</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>120 Hanover Sq.</Address>\n\t\t\t<City>London</City>\n\t\t\t<PostalCode>WA1 1DP</PostalCode>\n\t\t\t<Country>UK</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"BERGS\">\n\t\t<CompanyName>Berglunds snabbk&#246;p</CompanyName>\n\t\t<ContactName>Christina Berglund</ContactName>\n\t\t<ContactTitle>Order Administrator</ContactTitle>\n\t\t<Phone>0921-12 34 65</Phone>\n\t\t<Fax>0921-12 34 67</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Berguvsv&#228;gen  8</Address>\n\t\t\t<City>Lule&#229;</City>\n\t\t\t<PostalCode>S-958 22</PostalCode>\n\t\t\t<Country>Sweden</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"BLAUS\">\n\t\t<CompanyName>Blauer See Delikatessen</CompanyName>\n\t\t<ContactName>Hanna Moos</ContactName>\n\t\t<ContactTitle>Sales Representative</ContactTitle>\n\t\t<Phone>0621-08460</Phone>\n\t\t<Fax>0621-08924</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Forsterstr. 57</Address>\n\t\t\t<City>Mannheim</City>\n\t\t\t<PostalCode>68306</PostalCode>\n\t\t\t<Country>Germany</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"BLONP\">\n\t\t<CompanyName>Blondesddsl p&#232;re et fils</CompanyName>\n\t\t<ContactName>Fr&#233;d&#233;rique Citeaux</ContactName>\n\t\t<ContactTitle>Marketing Manager</ContactTitle>\n\t\t<Phone>88.60.15.31</Phone>\n\t\t<Fax>88.60.15.32</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>24, place Kl&#233;ber</Address>\n\t\t\t<City>Strasbourg</City>\n\t\t\t<PostalCode>67000</PostalCode>\n\t\t\t<Country>France</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"BOLID\">\n\t\t<CompanyName>B&#243;lido Comidas preparadas</CompanyName>\n\t\t<ContactName>Mart&#237;n Sommer</ContactName>\n\t\t<ContactTitle>Owner</ContactTitle>\n\t\t<Phone>(91) 555 22 82</Phone>\n\t\t<Fax>(91) 555 91 99</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>C/ Araquil, 67</Address>\n\t\t\t<City>Madrid</City>\n\t\t\t<PostalCode>28023</PostalCode>\n\t\t\t<Country>Spain</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"BONAP\">\n\t\t<CompanyName>Bon app'</CompanyName>\n\t\t<ContactName>Laurence Lebihan</ContactName>\n\t\t<ContactTitle>Owner</ContactTitle>\n\t\t<Phone>91.24.45.40</Phone>\n\t\t<Fax>91.24.45.41</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>12, rue des Bouchers</Address>\n\t\t\t<City>Marseille</City>\n\t\t\t<PostalCode>13008</PostalCode>\n\t\t\t<Country>France</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"BOTTM\">\n\t\t<CompanyName>Bottom-Dollar Markets</CompanyName>\n\t\t<ContactName>Elizabeth Lincoln</ContactName>\n\t\t<ContactTitle>Accounting Manager</ContactTitle>\n\t\t<Phone>(604) 555-4729</Phone>\n\t\t<Fax>(604) 555-3745</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>23 Tsawassen Blvd.</Address>\n\t\t\t<City>Tsawassen</City>\n\t\t\t<Region>BC</Region>\n\t\t\t<PostalCode>T2F 8M4</PostalCode>\n\t\t\t<Country>Canada</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"BSBEV\">\n\t\t<CompanyName>B's Beverages</CompanyName>\n\t\t<ContactName>Victoria Ashworth</ContactName>\n\t\t<ContactTitle>Sales Representative</ContactTitle>\n\t\t<Phone>(171) 555-1212</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>Fauntleroy Circus</Address>\n\t\t\t<City>London</City>\n\t\t\t<PostalCode>EC2 5NT</PostalCode>\n\t\t\t<Country>UK</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"CACTU\">\n\t\t<CompanyName>Cactus Comidas para llevar</CompanyName>\n\t\t<ContactName>Patricio Simpson</ContactName>\n\t\t<ContactTitle>Sales Agent</ContactTitle>\n\t\t<Phone>(1) 135-5555</Phone>\n\t\t<Fax>(1) 135-4892</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Cerrito 333</Address>\n\t\t\t<City>Buenos Aires</City>\n\t\t\t<PostalCode>1010</PostalCode>\n\t\t\t<Country>Argentina</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"CENTC\">\n\t\t<CompanyName>Centro comercial Moctezuma</CompanyName>\n\t\t<ContactName>Francisco Chang</ContactName>\n\t\t<ContactTitle>Marketing Manager</ContactTitle>\n\t\t<Phone>(5) 555-3392</Phone>\n\t\t<Fax>(5) 555-7293</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Sierras de Granada 9993</Address>\n\t\t\t<City>M&#233;xico D.F.</City>\n\t\t\t<PostalCode>05022</PostalCode>\n\t\t\t<Country>Mexico</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"CHOPS\">\n\t\t<CompanyName>Chop-suey Chinese</CompanyName>\n\t\t<ContactName>Yang Wang</ContactName>\n\t\t<ContactTitle>Owner</ContactTitle>\n\t\t<Phone>0452-076545</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>Hauptstr. 29</Address>\n\t\t\t<City>Bern</City>\n\t\t\t<PostalCode>3012</PostalCode>\n\t\t\t<Country>Switzerland</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"COMMI\">\n\t\t<CompanyName>Com&#233;rcio Mineiro</CompanyName>\n\t\t<ContactName>Pedro Afonso</ContactName>\n\t\t<ContactTitle>Sales Associate</ContactTitle>\n\t\t<Phone>(11) 555-7647</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>Av. dos Lus&#237;adas, 23</Address>\n\t\t\t<City>Sao Paulo</City>\n\t\t\t<Region>SP</Region>\n\t\t\t<PostalCode>05432-043</PostalCode>\n\t\t\t<Country>Brazil</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"CONSH\">\n\t\t<CompanyName>Consolidated Holdings</CompanyName>\n\t\t<ContactName>Elizabeth Brown</ContactName>\n\t\t<ContactTitle>Sales Representative</ContactTitle>\n\t\t<Phone>(171) 555-2282</Phone>\n\t\t<Fax>(171) 555-9199</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Berkeley Gardens 12  Brewery</Address>\n\t\t\t<City>London</City>\n\t\t\t<PostalCode>WX1 6LT</PostalCode>\n\t\t\t<Country>UK</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"DRACD\">\n\t\t<CompanyName>Drachenblut Delikatessen</CompanyName>\n\t\t<ContactName>Sven Ottlieb</ContactName>\n\t\t<ContactTitle>Order Administrator</ContactTitle>\n\t\t<Phone>0241-039123</Phone>\n\t\t<Fax>0241-059428</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Walserweg 21</Address>\n\t\t\t<City>Aachen</City>\n\t\t\t<PostalCode>52066</PostalCode>\n\t\t\t<Country>Germany</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"DUMON\">\n\t\t<CompanyName>Du monde entier</CompanyName>\n\t\t<ContactName>Janine Labrune</ContactName>\n\t\t<ContactTitle>Owner</ContactTitle>\n\t\t<Phone>40.67.88.88</Phone>\n\t\t<Fax>40.67.89.89</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>67, rue des Cinquante Otages</Address>\n\t\t\t<City>Nantes</City>\n\t\t\t<PostalCode>44000</PostalCode>\n\t\t\t<Country>France</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"EASTC\">\n\t\t<CompanyName>Eastern Connection</CompanyName>\n\t\t<ContactName>Ann Devon</ContactName>\n\t\t<ContactTitle>Sales Agent</ContactTitle>\n\t\t<Phone>(171) 555-0297</Phone>\n\t\t<Fax>(171) 555-3373</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>35 King George</Address>\n\t\t\t<City>London</City>\n\t\t\t<PostalCode>WX3 6FW</PostalCode>\n\t\t\t<Country>UK</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"ERNSH\">\n\t\t<CompanyName>Ernst Handel</CompanyName>\n\t\t<ContactName>Roland Mendel</ContactName>\n\t\t<ContactTitle>Sales Manager</ContactTitle>\n\t\t<Phone>7675-3425</Phone>\n\t\t<Fax>7675-3426</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Kirchgasse 6</Address>\n\t\t\t<City>Graz</City>\n\t\t\t<PostalCode>8010</PostalCode>\n\t\t\t<Country>Austria</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"FAMIA\">\n\t\t<CompanyName>Familia Arquibaldo</CompanyName>\n\t\t<ContactName>Aria Cruz</ContactName>\n\t\t<ContactTitle>Marketing Assistant</ContactTitle>\n\t\t<Phone>(11) 555-9857</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>Rua Or&#243;s, 92</Address>\n\t\t\t<City>Sao Paulo</City>\n\t\t\t<Region>SP</Region>\n\t\t\t<PostalCode>05442-030</PostalCode>\n\t\t\t<Country>Brazil</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"FISSA\">\n\t\t<CompanyName>FISSA Fabrica Inter. Salchichas S.A.</CompanyName>\n\t\t<ContactName>Diego Roel</ContactName>\n\t\t<ContactTitle>Accounting Manager</ContactTitle>\n\t\t<Phone>(91) 555 94 44</Phone>\n\t\t<Fax>(91) 555 55 93</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>C/ Moralzarzal, 86</Address>\n\t\t\t<City>Madrid</City>\n\t\t\t<PostalCode>28034</PostalCode>\n\t\t\t<Country>Spain</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"FOLIG\">\n\t\t<CompanyName>Folies gourmandes</CompanyName>\n\t\t<ContactName>Martine Ranc&#233;</ContactName>\n\t\t<ContactTitle>Assistant Sales Agent</ContactTitle>\n\t\t<Phone>20.16.10.16</Phone>\n\t\t<Fax>20.16.10.17</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>184, chauss&#233;e de Tournai</Address>\n\t\t\t<City>Lille</City>\n\t\t\t<PostalCode>59000</PostalCode>\n\t\t\t<Country>France</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"FOLKO\">\n\t\t<CompanyName>Folk och f&#228; HB</CompanyName>\n\t\t<ContactName>Maria Larsson</ContactName>\n\t\t<ContactTitle>Owner</ContactTitle>\n\t\t<Phone>0695-34 67 21</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>&#197;kergatan 24</Address>\n\t\t\t<City>Br&#228;cke</City>\n\t\t\t<PostalCode>S-844 67</PostalCode>\n\t\t\t<Country>Sweden</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"FRANK\">\n\t\t<CompanyName>Frankenversand</CompanyName>\n\t\t<ContactName>Peter Franken</ContactName>\n\t\t<ContactTitle>Marketing Manager</ContactTitle>\n\t\t<Phone>089-0877310</Phone>\n\t\t<Fax>089-0877451</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Berliner Platz 43</Address>\n\t\t\t<City>M&#252;nchen</City>\n\t\t\t<PostalCode>80805</PostalCode>\n\t\t\t<Country>Germany</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"FRANR\">\n\t\t<CompanyName>France restauration</CompanyName>\n\t\t<ContactName>Carine Schmitt</ContactName>\n\t\t<ContactTitle>Marketing Manager</ContactTitle>\n\t\t<Phone>40.32.21.21</Phone>\n\t\t<Fax>40.32.21.20</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>54, rue Royale</Address>\n\t\t\t<City>Nantes</City>\n\t\t\t<PostalCode>44000</PostalCode>\n\t\t\t<Country>France</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"FRANS\">\n\t\t<CompanyName>Franchi S.p.A.</CompanyName>\n\t\t<ContactName>Paolo Accorti</ContactName>\n\t\t<ContactTitle>Sales Representative</ContactTitle>\n\t\t<Phone>011-4988260</Phone>\n\t\t<Fax>011-4988261</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Via Monte Bianco 34</Address>\n\t\t\t<City>Torino</City>\n\t\t\t<PostalCode>10100</PostalCode>\n\t\t\t<Country>Italy</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"FURIB\">\n\t\t<CompanyName>Furia Bacalhau e Frutos do Mar</CompanyName>\n\t\t<ContactName>Lino Rodriguez</ContactName>\n\t\t<ContactTitle>Sales Manager</ContactTitle>\n\t\t<Phone>(1) 354-2534</Phone>\n\t\t<Fax>(1) 354-2535</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Jardim das rosas n. 32</Address>\n\t\t\t<City>Lisboa</City>\n\t\t\t<PostalCode>1675</PostalCode>\n\t\t\t<Country>Portugal</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"GALED\">\n\t\t<CompanyName>Galer&#237;a del gastr&#243;nomo</CompanyName>\n\t\t<ContactName>Eduardo Saavedra</ContactName>\n\t\t<ContactTitle>Marketing Manager</ContactTitle>\n\t\t<Phone>(93) 203 4560</Phone>\n\t\t<Fax>(93) 203 4561</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Rambla de Catalu&#241;a, 23</Address>\n\t\t\t<City>Barcelona</City>\n\t\t\t<PostalCode>08022</PostalCode>\n\t\t\t<Country>Spain</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"GODOS\">\n\t\t<CompanyName>Godos Cocina T&#237;pica</CompanyName>\n\t\t<ContactName>Jos&#233; Pedro Freyre</ContactName>\n\t\t<ContactTitle>Sales Manager</ContactTitle>\n\t\t<Phone>(95) 555 82 82</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>C/ Romero, 33</Address>\n\t\t\t<City>Sevilla</City>\n\t\t\t<PostalCode>41101</PostalCode>\n\t\t\t<Country>Spain</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"GOURL\">\n\t\t<CompanyName>Gourmet Lanchonetes</CompanyName>\n\t\t<ContactName>Andr&#233; Fonseca</ContactName>\n\t\t<ContactTitle>Sales Associate</ContactTitle>\n\t\t<Phone>(11) 555-9482</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>Av. Brasil, 442</Address>\n\t\t\t<City>Campinas</City>\n\t\t\t<Region>SP</Region>\n\t\t\t<PostalCode>04876-786</PostalCode>\n\t\t\t<Country>Brazil</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"GREAL\">\n\t\t<CompanyName>Great Lakes Food Market</CompanyName>\n\t\t<ContactName>Howard Snyder</ContactName>\n\t\t<ContactTitle>Marketing Manager</ContactTitle>\n\t\t<Phone>(503) 555-7555</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>2732 Baker Blvd.</Address>\n\t\t\t<City>Eugene</City>\n\t\t\t<Region>OR</Region>\n\t\t\t<PostalCode>97403</PostalCode>\n\t\t\t<Country>USA</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"GROSR\">\n\t\t<CompanyName>GROSELLA-Restaurante</CompanyName>\n\t\t<ContactName>Manuel Pereira</ContactName>\n\t\t<ContactTitle>Owner</ContactTitle>\n\t\t<Phone>(2) 283-2951</Phone>\n\t\t<Fax>(2) 283-3397</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>5&#170; Ave. Los Palos Grandes</Address>\n\t\t\t<City>Caracas</City>\n\t\t\t<Region>DF</Region>\n\t\t\t<PostalCode>1081</PostalCode>\n\t\t\t<Country>Venezuela</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"HANAR\">\n\t\t<CompanyName>Hanari Carnes</CompanyName>\n\t\t<ContactName>Mario Pontes</ContactName>\n\t\t<ContactTitle>Accounting Manager</ContactTitle>\n\t\t<Phone>(21) 555-0091</Phone>\n\t\t<Fax>(21) 555-8765</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Rua do Pa&#231;o, 67</Address>\n\t\t\t<City>Rio de Janeiro</City>\n\t\t\t<Region>RJ</Region>\n\t\t\t<PostalCode>05454-876</PostalCode>\n\t\t\t<Country>Brazil</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"HILAA\">\n\t\t<CompanyName>HILARION-Abastos</CompanyName>\n\t\t<ContactName>Carlos Hern&#225;ndez</ContactName>\n\t\t<ContactTitle>Sales Representative</ContactTitle>\n\t\t<Phone>(5) 555-1340</Phone>\n\t\t<Fax>(5) 555-1948</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Carrera 22 con Ave. Carlos Soublette #8-35</Address>\n\t\t\t<City>San Crist&#243;bal</City>\n\t\t\t<Region>T&#225;chira</Region>\n\t\t\t<PostalCode>5022</PostalCode>\n\t\t\t<Country>Venezuela</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"HUNGC\">\n\t\t<CompanyName>Hungry Coyote Import Store</CompanyName>\n\t\t<ContactName>Yoshi Latimer</ContactName>\n\t\t<ContactTitle>Sales Representative</ContactTitle>\n\t\t<Phone>(503) 555-6874</Phone>\n\t\t<Fax>(503) 555-2376</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>City Center Plaza 516 Main St.</Address>\n\t\t\t<City>Elgin</City>\n\t\t\t<Region>OR</Region>\n\t\t\t<PostalCode>97827</PostalCode>\n\t\t\t<Country>USA</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"HUNGO\">\n\t\t<CompanyName>Hungry Owl All-Night Grocers</CompanyName>\n\t\t<ContactName>Patricia McKenna</ContactName>\n\t\t<ContactTitle>Sales Associate</ContactTitle>\n\t\t<Phone>2967 542</Phone>\n\t\t<Fax>2967 3333</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>8 Johnstown Road</Address>\n\t\t\t<City>Cork</City>\n\t\t\t<Region>Co. Cork</Region>\n\t\t\t<Country>Ireland</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"ISLAT\">\n\t\t<CompanyName>Island Trading</CompanyName>\n\t\t<ContactName>Helen Bennett</ContactName>\n\t\t<ContactTitle>Marketing Manager</ContactTitle>\n\t\t<Phone>(198) 555-8888</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>Garden House Crowther Way</Address>\n\t\t\t<City>Cowes</City>\n\t\t\t<Region>Isle of Wight</Region>\n\t\t\t<PostalCode>PO31 7PJ</PostalCode>\n\t\t\t<Country>UK</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"KOENE\">\n\t\t<CompanyName>K&#246;niglich Essen</CompanyName>\n\t\t<ContactName>Philip Cramer</ContactName>\n\t\t<ContactTitle>Sales Associate</ContactTitle>\n\t\t<Phone>0555-09876</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>Maubelstr. 90</Address>\n\t\t\t<City>Brandenburg</City>\n\t\t\t<PostalCode>14776</PostalCode>\n\t\t\t<Country>Germany</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"LACOR\">\n\t\t<CompanyName>La corne d'abondance</CompanyName>\n\t\t<ContactName>Daniel Tonini</ContactName>\n\t\t<ContactTitle>Sales Representative</ContactTitle>\n\t\t<Phone>30.59.84.10</Phone>\n\t\t<Fax>30.59.85.11</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>67, avenue de l'Europe</Address>\n\t\t\t<City>Versailles</City>\n\t\t\t<PostalCode>78000</PostalCode>\n\t\t\t<Country>France</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"LAMAI\">\n\t\t<CompanyName>La maison d'Asie</CompanyName>\n\t\t<ContactName>Annette Roulet</ContactName>\n\t\t<ContactTitle>Sales Manager</ContactTitle>\n\t\t<Phone>61.77.61.10</Phone>\n\t\t<Fax>61.77.61.11</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>1 rue Alsace-Lorraine</Address>\n\t\t\t<City>Toulouse</City>\n\t\t\t<PostalCode>31000</PostalCode>\n\t\t\t<Country>France</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"LAUGB\">\n\t\t<CompanyName>Laughing Bacchus Wine Cellars</CompanyName>\n\t\t<ContactName>Yoshi Tannamuri</ContactName>\n\t\t<ContactTitle>Marketing Assistant</ContactTitle>\n\t\t<Phone>(604) 555-3392</Phone>\n\t\t<Fax>(604) 555-7293</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>1900 Oak St.</Address>\n\t\t\t<City>Vancouver</City>\n\t\t\t<Region>BC</Region>\n\t\t\t<PostalCode>V3F 2K1</PostalCode>\n\t\t\t<Country>Canada</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"LAZYK\">\n\t\t<CompanyName>Lazy K Kountry Store</CompanyName>\n\t\t<ContactName>John Steel</ContactName>\n\t\t<ContactTitle>Marketing Manager</ContactTitle>\n\t\t<Phone>(509) 555-7969</Phone>\n\t\t<Fax>(509) 555-6221</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>12 Orchestra Terrace</Address>\n\t\t\t<City>Walla Walla</City>\n\t\t\t<Region>WA</Region>\n\t\t\t<PostalCode>99362</PostalCode>\n\t\t\t<Country>USA</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"LEHMS\">\n\t\t<CompanyName>Lehmanns Marktstand</CompanyName>\n\t\t<ContactName>Renate Messner</ContactName>\n\t\t<ContactTitle>Sales Representative</ContactTitle>\n\t\t<Phone>069-0245984</Phone>\n\t\t<Fax>069-0245874</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Magazinweg 7</Address>\n\t\t\t<City>Frankfurt a.M.</City>\n\t\t\t<PostalCode>60528</PostalCode>\n\t\t\t<Country>Germany</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"LETSS\">\n\t\t<CompanyName>Let's Stop N Shop</CompanyName>\n\t\t<ContactName>Jaime Yorres</ContactName>\n\t\t<ContactTitle>Owner</ContactTitle>\n\t\t<Phone>(415) 555-5938</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>87 Polk St. Suite 5</Address>\n\t\t\t<City>San Francisco</City>\n\t\t\t<Region>CA</Region>\n\t\t\t<PostalCode>94117</PostalCode>\n\t\t\t<Country>USA</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"LILAS\">\n\t\t<CompanyName>LILA-Supermercado</CompanyName>\n\t\t<ContactName>Carlos Gonz&#225;lez</ContactName>\n\t\t<ContactTitle>Accounting Manager</ContactTitle>\n\t\t<Phone>(9) 331-6954</Phone>\n\t\t<Fax>(9) 331-7256</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Carrera 52 con Ave. Bol&#237;var #65-98 Llano Largo</Address>\n\t\t\t<City>Barquisimeto</City>\n\t\t\t<Region>Lara</Region>\n\t\t\t<PostalCode>3508</PostalCode>\n\t\t\t<Country>Venezuela</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"LINOD\">\n\t\t<CompanyName>LINO-Delicateses</CompanyName>\n\t\t<ContactName>Felipe Izquierdo</ContactName>\n\t\t<ContactTitle>Owner</ContactTitle>\n\t\t<Phone>(8) 34-56-12</Phone>\n\t\t<Fax>(8) 34-93-93</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Ave. 5 de Mayo Porlamar</Address>\n\t\t\t<City>I. de Margarita</City>\n\t\t\t<Region>Nueva Esparta</Region>\n\t\t\t<PostalCode>4980</PostalCode>\n\t\t\t<Country>Venezuela</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"LONEP\">\n\t\t<CompanyName>Lonesome Pine Restaurant</CompanyName>\n\t\t<ContactName>Fran Wilson</ContactName>\n\t\t<ContactTitle>Sales Manager</ContactTitle>\n\t\t<Phone>(503) 555-9573</Phone>\n\t\t<Fax>(503) 555-9646</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>89 Chiaroscuro Rd.</Address>\n\t\t\t<City>Portland</City>\n\t\t\t<Region>OR</Region>\n\t\t\t<PostalCode>97219</PostalCode>\n\t\t\t<Country>USA</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"MAGAA\">\n\t\t<CompanyName>Magazzini Alimentari Riuniti</CompanyName>\n\t\t<ContactName>Giovanni Rovelli</ContactName>\n\t\t<ContactTitle>Marketing Manager</ContactTitle>\n\t\t<Phone>035-640230</Phone>\n\t\t<Fax>035-640231</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Via Ludovico il Moro 22</Address>\n\t\t\t<City>Bergamo</City>\n\t\t\t<PostalCode>24100</PostalCode>\n\t\t\t<Country>Italy</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"MAISD\">\n\t\t<CompanyName>Maison Dewey</CompanyName>\n\t\t<ContactName>Catherine Dewey</ContactName>\n\t\t<ContactTitle>Sales Agent</ContactTitle>\n\t\t<Phone>(02) 201 24 67</Phone>\n\t\t<Fax>(02) 201 24 68</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Rue Joseph-Bens 532</Address>\n\t\t\t<City>Bruxelles</City>\n\t\t\t<PostalCode>B-1180</PostalCode>\n\t\t\t<Country>Belgium</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"MEREP\">\n\t\t<CompanyName>M&#232;re Paillarde</CompanyName>\n\t\t<ContactName>Jean Fresni&#232;re</ContactName>\n\t\t<ContactTitle>Marketing Assistant</ContactTitle>\n\t\t<Phone>(514) 555-8054</Phone>\n\t\t<Fax>(514) 555-8055</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>43 rue St. Laurent</Address>\n\t\t\t<City>Montr&#233;al</City>\n\t\t\t<Region>Qu&#233;bec</Region>\n\t\t\t<PostalCode>H1J 1C3</PostalCode>\n\t\t\t<Country>Canada</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"MORGK\">\n\t\t<CompanyName>Morgenstern Gesundkost</CompanyName>\n\t\t<ContactName>Alexander Feuer</ContactName>\n\t\t<ContactTitle>Marketing Assistant</ContactTitle>\n\t\t<Phone>0342-023176</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>Heerstr. 22</Address>\n\t\t\t<City>Leipzig</City>\n\t\t\t<PostalCode>04179</PostalCode>\n\t\t\t<Country>Germany</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"NORTS\">\n\t\t<CompanyName>North/South</CompanyName>\n\t\t<ContactName>Simon Crowther</ContactName>\n\t\t<ContactTitle>Sales Associate</ContactTitle>\n\t\t<Phone>(171) 555-7733</Phone>\n\t\t<Fax>(171) 555-2530</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>South House 300 Queensbridge</Address>\n\t\t\t<City>London</City>\n\t\t\t<PostalCode>SW7 1RZ</PostalCode>\n\t\t\t<Country>UK</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"OCEAN\">\n\t\t<CompanyName>Oc&#233;ano Atl&#225;ntico Ltda.</CompanyName>\n\t\t<ContactName>Yvonne Moncada</ContactName>\n\t\t<ContactTitle>Sales Agent</ContactTitle>\n\t\t<Phone>(1) 135-5333</Phone>\n\t\t<Fax>(1) 135-5535</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Ing. Gustavo Moncada 8585 Piso 20-A</Address>\n\t\t\t<City>Buenos Aires</City>\n\t\t\t<PostalCode>1010</PostalCode>\n\t\t\t<Country>Argentina</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"OLDWO\">\n\t\t<CompanyName>Old World Delicatessen</CompanyName>\n\t\t<ContactName>Rene Phillips</ContactName>\n\t\t<ContactTitle>Sales Representative</ContactTitle>\n\t\t<Phone>(907) 555-7584</Phone>\n\t\t<Fax>(907) 555-2880</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>2743 Bering St.</Address>\n\t\t\t<City>Anchorage</City>\n\t\t\t<Region>AK</Region>\n\t\t\t<PostalCode>99508</PostalCode>\n\t\t\t<Country>USA</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"OTTIK\">\n\t\t<CompanyName>Ottilies K&#228;seladen</CompanyName>\n\t\t<ContactName>Henriette Pfalzheim</ContactName>\n\t\t<ContactTitle>Owner</ContactTitle>\n\t\t<Phone>0221-0644327</Phone>\n\t\t<Fax>0221-0765721</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Mehrheimerstr. 369</Address>\n\t\t\t<City>K&#246;ln</City>\n\t\t\t<PostalCode>50739</PostalCode>\n\t\t\t<Country>Germany</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"PARIS\">\n\t\t<CompanyName>Paris sp&#233;cialit&#233;s</CompanyName>\n\t\t<ContactName>Marie Bertrand</ContactName>\n\t\t<ContactTitle>Owner</ContactTitle>\n\t\t<Phone>(1) 42.34.22.66</Phone>\n\t\t<Fax>(1) 42.34.22.77</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>265, boulevard Charonne</Address>\n\t\t\t<City>Paris</City>\n\t\t\t<PostalCode>75012</PostalCode>\n\t\t\t<Country>France</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"PERIC\">\n\t\t<CompanyName>Pericles Comidas cl&#225;sicas</CompanyName>\n\t\t<ContactName>Guillermo Fern&#225;ndez</ContactName>\n\t\t<ContactTitle>Sales Representative</ContactTitle>\n\t\t<Phone>(5) 552-3745</Phone>\n\t\t<Fax>(5) 545-3745</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Calle Dr. Jorge Cash 321</Address>\n\t\t\t<City>M&#233;xico D.F.</City>\n\t\t\t<PostalCode>05033</PostalCode>\n\t\t\t<Country>Mexico</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"PICCO\">\n\t\t<CompanyName>Piccolo und mehr</CompanyName>\n\t\t<ContactName>Georg Pipps</ContactName>\n\t\t<ContactTitle>Sales Manager</ContactTitle>\n\t\t<Phone>6562-9722</Phone>\n\t\t<Fax>6562-9723</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Geislweg 14</Address>\n\t\t\t<City>Salzburg</City>\n\t\t\t<PostalCode>5020</PostalCode>\n\t\t\t<Country>Austria</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"PRINI\">\n\t\t<CompanyName>Princesa Isabel Vinhos</CompanyName>\n\t\t<ContactName>Isabel de Castro</ContactName>\n\t\t<ContactTitle>Sales Representative</ContactTitle>\n\t\t<Phone>(1) 356-5634</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>Estrada da sa&#250;de n. 58</Address>\n\t\t\t<City>Lisboa</City>\n\t\t\t<PostalCode>1756</PostalCode>\n\t\t\t<Country>Portugal</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"QUEDE\">\n\t\t<CompanyName>Que Del&#237;cia</CompanyName>\n\t\t<ContactName>Bernardo Batista</ContactName>\n\t\t<ContactTitle>Accounting Manager</ContactTitle>\n\t\t<Phone>(21) 555-4252</Phone>\n\t\t<Fax>(21) 555-4545</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Rua da Panificadora, 12</Address>\n\t\t\t<City>Rio de Janeiro</City>\n\t\t\t<Region>RJ</Region>\n\t\t\t<PostalCode>02389-673</PostalCode>\n\t\t\t<Country>Brazil</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"QUEEN\">\n\t\t<CompanyName>Queen Cozinha</CompanyName>\n\t\t<ContactName>L&#250;cia Carvalho</ContactName>\n\t\t<ContactTitle>Marketing Assistant</ContactTitle>\n\t\t<Phone>(11) 555-1189</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>Alameda dos Can&#224;rios, 891</Address>\n\t\t\t<City>Sao Paulo</City>\n\t\t\t<Region>SP</Region>\n\t\t\t<PostalCode>05487-020</PostalCode>\n\t\t\t<Country>Brazil</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"QUICK\">\n\t\t<CompanyName>QUICK-Stop</CompanyName>\n\t\t<ContactName>Horst Kloss</ContactName>\n\t\t<ContactTitle>Accounting Manager</ContactTitle>\n\t\t<Phone>0372-035188</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>Taucherstra&#223;e 10</Address>\n\t\t\t<City>Cunewalde</City>\n\t\t\t<PostalCode>01307</PostalCode>\n\t\t\t<Country>Germany</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"RANCH\">\n\t\t<CompanyName>Rancho grande</CompanyName>\n\t\t<ContactName>Sergio Guti&#233;rrez</ContactName>\n\t\t<ContactTitle>Sales Representative</ContactTitle>\n\t\t<Phone>(1) 123-5555</Phone>\n\t\t<Fax>(1) 123-5556</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Av. del Libertador 900</Address>\n\t\t\t<City>Buenos Aires</City>\n\t\t\t<PostalCode>1010</PostalCode>\n\t\t\t<Country>Argentina</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"RATTC\">\n\t\t<CompanyName>Rattlesnake Canyon Grocery</CompanyName>\n\t\t<ContactName>Paula Wilson</ContactName>\n\t\t<ContactTitle>Assistant Sales Representative</ContactTitle>\n\t\t<Phone>(505) 555-5939</Phone>\n\t\t<Fax>(505) 555-3620</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>2817 Milton Dr.</Address>\n\t\t\t<City>Albuquerque</City>\n\t\t\t<Region>NM</Region>\n\t\t\t<PostalCode>87110</PostalCode>\n\t\t\t<Country>USA</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"REGGC\">\n\t\t<CompanyName>Reggiani Caseifici</CompanyName>\n\t\t<ContactName>Maurizio Moroni</ContactName>\n\t\t<ContactTitle>Sales Associate</ContactTitle>\n\t\t<Phone>0522-556721</Phone>\n\t\t<Fax>0522-556722</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Strada Provinciale 124</Address>\n\t\t\t<City>Reggio Emilia</City>\n\t\t\t<PostalCode>42100</PostalCode>\n\t\t\t<Country>Italy</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"RICAR\">\n\t\t<CompanyName>Ricardo Adocicados</CompanyName>\n\t\t<ContactName>Janete Limeira</ContactName>\n\t\t<ContactTitle>Assistant Sales Agent</ContactTitle>\n\t\t<Phone>(21) 555-3412</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>Av. Copacabana, 267</Address>\n\t\t\t<City>Rio de Janeiro</City>\n\t\t\t<Region>RJ</Region>\n\t\t\t<PostalCode>02389-890</PostalCode>\n\t\t\t<Country>Brazil</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"RICSU\">\n\t\t<CompanyName>Richter Supermarkt</CompanyName>\n\t\t<ContactName>Michael Holz</ContactName>\n\t\t<ContactTitle>Sales Manager</ContactTitle>\n\t\t<Phone>0897-034214</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>Grenzacherweg 237</Address>\n\t\t\t<City>Gen&#232;ve</City>\n\t\t\t<PostalCode>1203</PostalCode>\n\t\t\t<Country>Switzerland</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"ROMEY\">\n\t\t<CompanyName>Romero y tomillo</CompanyName>\n\t\t<ContactName>Alejandra Camino</ContactName>\n\t\t<ContactTitle>Accounting Manager</ContactTitle>\n\t\t<Phone>(91) 745 6200</Phone>\n\t\t<Fax>(91) 745 6210</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Gran V&#237;a, 1</Address>\n\t\t\t<City>Madrid</City>\n\t\t\t<PostalCode>28001</PostalCode>\n\t\t\t<Country>Spain</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"SANTG\">\n\t\t<CompanyName>Sant&#233; Gourmet</CompanyName>\n\t\t<ContactName>Jonas Bergulfsen</ContactName>\n\t\t<ContactTitle>Owner</ContactTitle>\n\t\t<Phone>07-98 92 35</Phone>\n\t\t<Fax>07-98 92 47</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Erling Skakkes gate 78</Address>\n\t\t\t<City>Stavern</City>\n\t\t\t<PostalCode>4110</PostalCode>\n\t\t\t<Country>Norway</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"SAVEA\">\n\t\t<CompanyName>Save-a-lot Markets</CompanyName>\n\t\t<ContactName>Jose Pavarotti</ContactName>\n\t\t<ContactTitle>Sales Representative</ContactTitle>\n\t\t<Phone>(208) 555-8097</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>187 Suffolk Ln.</Address>\n\t\t\t<City>Boise</City>\n\t\t\t<Region>ID</Region>\n\t\t\t<PostalCode>83720</PostalCode>\n\t\t\t<Country>USA</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"SEVES\">\n\t\t<CompanyName>Seven Seas Imports</CompanyName>\n\t\t<ContactName>Hari Kumar</ContactName>\n\t\t<ContactTitle>Sales Manager</ContactTitle>\n\t\t<Phone>(171) 555-1717</Phone>\n\t\t<Fax>(171) 555-5646</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>90 Wadhurst Rd.</Address>\n\t\t\t<City>London</City>\n\t\t\t<PostalCode>OX15 4NB</PostalCode>\n\t\t\t<Country>UK</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"SIMOB\">\n\t\t<CompanyName>Simons bistro</CompanyName>\n\t\t<ContactName>Jytte Petersen</ContactName>\n\t\t<ContactTitle>Owner</ContactTitle>\n\t\t<Phone>31 12 34 56</Phone>\n\t\t<Fax>31 13 35 57</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Vinb&#230;ltet 34</Address>\n\t\t\t<City>Kobenhavn</City>\n\t\t\t<PostalCode>1734</PostalCode>\n\t\t\t<Country>Denmark</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"SPECD\">\n\t\t<CompanyName>Sp&#233;cialit&#233;s du monde</CompanyName>\n\t\t<ContactName>Dominique Perrier</ContactName>\n\t\t<ContactTitle>Marketing Manager</ContactTitle>\n\t\t<Phone>(1) 47.55.60.10</Phone>\n\t\t<Fax>(1) 47.55.60.20</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>25, rue Lauriston</Address>\n\t\t\t<City>Paris</City>\n\t\t\t<PostalCode>75016</PostalCode>\n\t\t\t<Country>France</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"SPLIR\">\n\t\t<CompanyName>Split Rail Beer &amp; Ale</CompanyName>\n\t\t<ContactName>Art Braunschweiger</ContactName>\n\t\t<ContactTitle>Sales Manager</ContactTitle>\n\t\t<Phone>(307) 555-4680</Phone>\n\t\t<Fax>(307) 555-6525</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>P.O. Box 555</Address>\n\t\t\t<City>Lander</City>\n\t\t\t<Region>WY</Region>\n\t\t\t<PostalCode>82520</PostalCode>\n\t\t\t<Country>USA</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"SUPRD\">\n\t\t<CompanyName>Supr&#234;mes d&#233;lices</CompanyName>\n\t\t<ContactName>Pascale Cartrain</ContactName>\n\t\t<ContactTitle>Accounting Manager</ContactTitle>\n\t\t<Phone>(071) 23 67 22 20</Phone>\n\t\t<Fax>(071) 23 67 22 21</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Boulevard Tirou, 255</Address>\n\t\t\t<City>Charleroi</City>\n\t\t\t<PostalCode>B-6000</PostalCode>\n\t\t\t<Country>Belgium</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"THEBI\">\n\t\t<CompanyName>The Big Cheese</CompanyName>\n\t\t<ContactName>Liz Nixon</ContactName>\n\t\t<ContactTitle>Marketing Manager</ContactTitle>\n\t\t<Phone>(503) 555-3612</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>89 Jefferson Way Suite 2</Address>\n\t\t\t<City>Portland</City>\n\t\t\t<Region>OR</Region>\n\t\t\t<PostalCode>97201</PostalCode>\n\t\t\t<Country>USA</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"THECR\">\n\t\t<CompanyName>The Cracker Box</CompanyName>\n\t\t<ContactName>Liu Wong</ContactName>\n\t\t<ContactTitle>Marketing Assistant</ContactTitle>\n\t\t<Phone>(406) 555-5834</Phone>\n\t\t<Fax>(406) 555-8083</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>55 Grizzly Peak Rd.</Address>\n\t\t\t<City>Butte</City>\n\t\t\t<Region>MT</Region>\n\t\t\t<PostalCode>59801</PostalCode>\n\t\t\t<Country>USA</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"TOMSP\">\n\t\t<CompanyName>Toms Spezialit&#228;ten</CompanyName>\n\t\t<ContactName>Karin Josephs</ContactName>\n\t\t<ContactTitle>Marketing Manager</ContactTitle>\n\t\t<Phone>0251-031259</Phone>\n\t\t<Fax>0251-035695</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Luisenstr. 48</Address>\n\t\t\t<City>M&#252;nster</City>\n\t\t\t<PostalCode>44087</PostalCode>\n\t\t\t<Country>Germany</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"TORTU\">\n\t\t<CompanyName>Tortuga Restaurante</CompanyName>\n\t\t<ContactName>Miguel Angel Paolino</ContactName>\n\t\t<ContactTitle>Owner</ContactTitle>\n\t\t<Phone>(5) 555-2933</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>Avda. Azteca 123</Address>\n\t\t\t<City>M&#233;xico D.F.</City>\n\t\t\t<PostalCode>05033</PostalCode>\n\t\t\t<Country>Mexico</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"TRADH\">\n\t\t<CompanyName>Tradi&#231;&#227;o Hipermercados</CompanyName>\n\t\t<ContactName>Anabela Domingues</ContactName>\n\t\t<ContactTitle>Sales Representative</ContactTitle>\n\t\t<Phone>(11) 555-2167</Phone>\n\t\t<Fax>(11) 555-2168</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Av. In&#234;s de Castro, 414</Address>\n\t\t\t<City>Sao Paulo</City>\n\t\t\t<Region>SP</Region>\n\t\t\t<PostalCode>05634-030</PostalCode>\n\t\t\t<Country>Brazil</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"TRAIH\">\n\t\t<CompanyName>Trail's Head Gourmet Provisioners</CompanyName>\n\t\t<ContactName>Helvetius Nagy</ContactName>\n\t\t<ContactTitle>Sales Associate</ContactTitle>\n\t\t<Phone>(206) 555-8257</Phone>\n\t\t<Fax>(206) 555-2174</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>722 DaVinci Blvd.</Address>\n\t\t\t<City>Kirkland</City>\n\t\t\t<Region>WA</Region>\n\t\t\t<PostalCode>98034</PostalCode>\n\t\t\t<Country>USA</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"VAFFE\">\n\t\t<CompanyName>Vaffeljernet</CompanyName>\n\t\t<ContactName>Palle Ibsen</ContactName>\n\t\t<ContactTitle>Sales Manager</ContactTitle>\n\t\t<Phone>86 21 32 43</Phone>\n\t\t<Fax>86 22 33 44</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Smagsloget 45</Address>\n\t\t\t<City>&#197;rhus</City>\n\t\t\t<PostalCode>8200</PostalCode>\n\t\t\t<Country>Denmark</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"VICTE\">\n\t\t<CompanyName>Victuailles en stock</CompanyName>\n\t\t<ContactName>Mary Saveley</ContactName>\n\t\t<ContactTitle>Sales Agent</ContactTitle>\n\t\t<Phone>78.32.54.86</Phone>\n\t\t<Fax>78.32.54.87</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>2, rue du Commerce</Address>\n\t\t\t<City>Lyon</City>\n\t\t\t<PostalCode>69004</PostalCode>\n\t\t\t<Country>France</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"VINET\">\n\t\t<CompanyName>Vins et alcools Chevalier</CompanyName>\n\t\t<ContactName>Paul Henriot</ContactName>\n\t\t<ContactTitle>Accounting Manager</ContactTitle>\n\t\t<Phone>26.47.15.10</Phone>\n\t\t<Fax>26.47.15.11</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>59 rue de l'Abbaye</Address>\n\t\t\t<City>Reims</City>\n\t\t\t<PostalCode>51100</PostalCode>\n\t\t\t<Country>France</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"WANDK\">\n\t\t<CompanyName>Die Wandernde Kuh</CompanyName>\n\t\t<ContactName>Rita M&#252;ller</ContactName>\n\t\t<ContactTitle>Sales Representative</ContactTitle>\n\t\t<Phone>0711-020361</Phone>\n\t\t<Fax>0711-035428</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Adenauerallee 900</Address>\n\t\t\t<City>Stuttgart</City>\n\t\t\t<PostalCode>70563</PostalCode>\n\t\t\t<Country>Germany</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"WARTH\">\n\t\t<CompanyName>Wartian Herkku</CompanyName>\n\t\t<ContactName>Pirkko Koskitalo</ContactName>\n\t\t<ContactTitle>Accounting Manager</ContactTitle>\n\t\t<Phone>981-443655</Phone>\n\t\t<Fax>981-443655</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Torikatu 38</Address>\n\t\t\t<City>Oulu</City>\n\t\t\t<PostalCode>90110</PostalCode>\n\t\t\t<Country>Finland</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"WELLI\">\n\t\t<CompanyName>Wellington Importadora</CompanyName>\n\t\t<ContactName>Paula Parente</ContactName>\n\t\t<ContactTitle>Sales Manager</ContactTitle>\n\t\t<Phone>(14) 555-8122</Phone>\n\t\t<FullAddress>\n\t\t\t<Address>Rua do Mercado, 12</Address>\n\t\t\t<City>Resende</City>\n\t\t\t<Region>SP</Region>\n\t\t\t<PostalCode>08737-363</PostalCode>\n\t\t\t<Country>Brazil</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"WHITC\">\n\t\t<CompanyName>White Clover Markets</CompanyName>\n\t\t<ContactName>Karl Jablonski</ContactName>\n\t\t<ContactTitle>Owner</ContactTitle>\n\t\t<Phone>(206) 555-4112</Phone>\n\t\t<Fax>(206) 555-4115</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>305 - 14th Ave. S. Suite 3B</Address>\n\t\t\t<City>Seattle</City>\n\t\t\t<Region>WA</Region>\n\t\t\t<PostalCode>98128</PostalCode>\n\t\t\t<Country>USA</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"WILMK\">\n\t\t<CompanyName>Wilman Kala</CompanyName>\n\t\t<ContactName>Matti Karttunen</ContactName>\n\t\t<ContactTitle>Owner/Marketing Assistant</ContactTitle>\n\t\t<Phone>90-224 8858</Phone>\n\t\t<Fax>90-224 8858</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>Keskuskatu 45</Address>\n\t\t\t<City>Helsinki</City>\n\t\t\t<PostalCode>21240</PostalCode>\n\t\t\t<Country>Finland</Country>\n\t\t</FullAddress>\n\t</Customers><Customers CustomerID=\"WOLZA\">\n\t\t<CompanyName>Wolski  Zajazd</CompanyName>\n\t\t<ContactName>Zbyszek Piestrzeniewicz</ContactName>\n\t\t<ContactTitle>Owner</ContactTitle>\n\t\t<Phone>(26) 642-7012</Phone>\n\t\t<Fax>(26) 642-7012</Fax>\n\t\t<FullAddress>\n\t\t\t<Address>ul. Filtrowa 68</Address>\n\t\t\t<City>Warszawa</City>\n\t\t\t<PostalCode>01-012</PostalCode>\n\t\t\t<Country>Poland</Country>\n\t\t</FullAddress>\n\t</Customers></root>", false)
    );
  }

  /**
   *  Parent of attribute node.
   */
  @org.junit.Test
  public void axes086() {
    final XQuery query = new XQuery(
      "<root> {//@*/..} </root>",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/Tree1Text.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<root><west mark=\"w0\" /><center mark=\"c0\">Text in center</center><east mark=\"e0\">Text in east</east></root>", false)
    );
  }

  /**
   *  Parent of text nodes.
   */
  @org.junit.Test
  public void axes087() {
    final XQuery query = new XQuery(
      "<root> {/doc/part/*/text()/..} </root>",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/xq311B.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<root><retail>62.50 USD</retail><wholesale>55.00 USD</wholesale><internal>31.25 USD</internal></root>", false)
    );
  }

  /**
   *  Empty step, should result in parse error.
   */
  @org.junit.Test
  public void axes088() {
    final XQuery query = new XQuery(
      "/*/",
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
   * XQuery program to perform a knight's tour of the chessboard.Author: Michael H. KayThis version modified to use XQuery 1.0, with sequences and functions.This query does not use a source document.There is an optional parameter, start, which can be set to any square on thechessboard, e.g. a3 or h5. XQuery does not allow parameters to be given adefault value, so the parameter is mandatory.There is a second optional parameter, end, which indicates that the processing should stopafter a given number of steps. This can be used to animate the display of the tour. Thisworks especially well when the query is compiled into a Java servlet.The output is an HTML display of the completed tour.Internally, the following data representations are used:* A square on the chessboard: represented as a number in the range 0 to 63* A state of the chessboard: a sequence of 64 integers, each containing a move number.A square that has not been visited yet is represented by a zero.* A set of possible moves: represented as a sequence of integers,* each integer representing the number of the destination squarestart-column is an integer in the range 0-7start-row is an integer in the range 0-7, with zero at the topThis function controls the processing. It does not access the source document.Validate the input parameterSet up the empty boardPlace the knight on the board at the chosen starting positionEvaluate the knight's tourproduce the HTML outputrange 0 to 63This function places a knight on the board at a given square. The returned value isthe supplied board, modified to indicate that the knight reached a given square at a givenmoverange 0 to 63This function takes the board in a given state, decides on the next move to make,and then calls itself recursively to make further moves, until the knight has completedhis tour of the board. It returns the board in its final state.determine the possible moves that the knight can maketry these moves in turn until one is found that worksrange 0 to 63This function tries a set of possible moves that the knight can makefrom a given position. It determines the best move as the one to the square withfewest exits. If this is unsuccessful then it can backtrack andtry another move; however this turns out rarely to be necessary.The function makes the selected move, and then calls make-moves() to makesubsequent moves, returning the final state of the board.if there is no possible move, we return the special value () as the final stateof the board, to indicate to the caller that we got stuckrange 0 to 63this function, given the state of the board and a set of possible moves,determines which of the moves is the best one. It then makes this move,and proceeds recursively to make further moves, eventually returning thefinal state of the board.if at least one move is possible, find the best onefind the list of possible moves excluding the best oneupdate the board to make the move chosen as the best onenow make further moves using a recursive call, until the board is completecount($next-board[.=0])!=0if the final board has the special value '()', we got stuck, and have to choosethe next best of the possible moves. This is done by a recursive call. I thoughtthat the knight never did get stuck, but it does: if the starting square is f1,the wrong choice is made at move 58, and needs to be reversed.This function finds from among the possible moves, the one with fewest exits.It calls itself recursively.split the list of possible moves into the first move and the rest of the movestry making the first movesee how many moves would be possible the next timedetermine whether this trial move has fewer exits than those considered up till nowdetermine which is the best move (the one with fewest exits) so farif there are other possible moves, consider them too, using a recursive call.Otherwise return the best move found.This function, given the knight's position on the board, returns the set of squareshe can move to. The squares will be ones that have not been visited beforeOutput the board in HTML format.
   */
  @org.junit.Test
  public void axes089() {
    final XQuery query = new XQuery(
      "xquery version \"1.0\"; declare namespace saxon=\"http://example.com/VendorNamespace\"; declare namespace tour=\"http://example.com/Knight'sTour\"; (: XQuery program to perform a knight's tour of the chessboard. Author: Michael H. Kay Date: 26 June 2003 This version modified to use XQuery 1.0, with sequences and functions. This query does not use a source document. There is an optional parameter, start, which can be set to any square on the chessboard, e.g. a3 or h5. XQuery does not allow parameters to be given a default value, so the parameter is mandatory. There is a second optional parameter, end, which indicates that the processing should stop after a given number of steps. This can be used to animate the display of the tour. This works especially well when the query is compiled into a Java servlet. The output is an HTML display of the completed tour. Internally, the following data representations are used: * A square on the chessboard: represented as a number in the range 0 to 63 * A state of the chessboard: a sequence of 64 integers, each containing a move number. A square that has not been visited yet is represented by a zero. * A set of possible moves: represented as a sequence of integers, * each integer representing the number of the destination square :) declare option saxon:default \"'a1'\"; declare variable $start as xs:string := 'a1'; declare option saxon:default \"'64'\"; declare variable $end as xs:string := '64'; declare variable $endd as xs:integer := xs:integer($end); declare variable $start-column as xs:integer := xs:integer(translate(substring($start, 1, 1), 'abcdefgh', '01234567')); declare variable $start-row as xs:integer := 8 - xs:integer(substring($start, 2, 1)); declare function tour:main () as element() { if (not(string-length($start)=2) or not(translate(substring($start,1,1), 'abcdefgh', 'aaaaaaaa')='a') or not(translate(substring($start,2,1), '12345678', '11111111')='1')) then error((), \"Invalid start parameter: try say 'a1' or 'g6'\") else if (not($endd = 1 to 64)) then error((), \"Invalid end parameter: must be in range 1 to 64\") else let $empty-board as xs:integer* := for $i in (1 to 64) return 0 let $initial-board as xs:integer* := tour:place-knight(1, $empty-board, $start-row * 8 + $start-column) let $final-board as xs:integer* := tour:make-moves(2, $initial-board, $start-row * 8 + $start-column) return tour:print-board($final-board) }; declare function tour:place-knight ( $move as xs:integer, $board as xs:integer*, $square as xs:integer ) as xs:integer* { (: This function places a knight on the board at a given square. The returned value is the supplied board, modified to indicate that the knight reached a given square at a given move :) for $i in 1 to 64 return if ($i = $square + 1) then $move else $board[$i] }; declare function tour:make-moves ( $move as xs:integer, $board as xs:integer*, $square as xs:integer ) as xs:integer* { (: This function takes the board in a given state, decides on the next move to make, and then calls itself recursively to make further moves, until the knight has completed his tour of the board. It returns the board in its final state. :) let $possible-move-list as xs:integer* := tour:list-possible-moves($board, $square) return tour:try-possible-moves($move, $board, $square, $possible-move-list) }; declare function tour:try-possible-moves ( $move as xs:integer, $board as xs:integer*, $square as xs:integer, $possible-moves as xs:integer* ) as xs:integer* { (: This function tries a set of possible moves that the knight can make from a given position. It determines the best move as the one to the square with fewest exits. If this is unsuccessful then it can backtrack and try another move; however this turns out rarely to be necessary. The function makes the selected move, and then calls make-moves() to make subsequent moves, returning the final state of the board. :) if (count($possible-moves)!=0) then tour:make-best-move($move, $board, $square, one-or-more($possible-moves)) else () (: if there is no possible move, we return the special value () as the final state of the board, to indicate to the caller that we got stuck :) }; declare function tour:make-best-move ( $move as xs:integer, $board as xs:integer*, $square as xs:integer, $possible-moves as xs:integer+ ) as xs:integer* { (: this function, given the state of the board and a set of possible moves, determines which of the moves is the best one. It then makes this move, and proceeds recursively to make further moves, eventually returning the final state of the board. :) let $best-move as xs:integer := tour:find-best-move($board, $possible-moves, 9, 999) let $other-possible-moves as xs:integer* := $possible-moves[. != $best-move] let $next-board as xs:integer* := tour:place-knight($move, $board, $best-move) let $final-board as xs:integer* := if ($move < $endd) then tour:make-moves($move+1, $next-board, $best-move) else $next-board (: if the final board has the special value '()', we got stuck, and have to choose the next best of the possible moves. This is done by a recursive call. I thought that the knight never did get stuck, but it does: if the starting square is f1, the wrong choice is made at move 58, and needs to be reversed. :) return if (empty($final-board)) then tour:try-possible-moves($move, $board, $square, $other-possible-moves) else $final-board }; declare function tour:find-best-move ( $board as xs:integer*, $possible-moves as xs:integer+, $fewest-exits as xs:integer, $best-so-far as xs:integer ) as xs:integer { (: This function finds from among the possible moves, the one with fewest exits. It calls itself recursively. :) let $trial-move as xs:integer := $possible-moves[1] let $other-possible-moves as xs:integer* := $possible-moves[position() > 1] let $trial-board as xs:integer* := tour:place-knight(99, $board, $trial-move) let $trial-move-exit-list as xs:integer* := tour:list-possible-moves($trial-board, $trial-move) let $number-of-exits as xs:integer := count($trial-move-exit-list) let $minimum-exits as xs:integer := min(($number-of-exits, $fewest-exits)) let $new-best-so-far as xs:integer := if ($number-of-exits < $fewest-exits) then $trial-move else $best-so-far (: if there are other possible moves, consider them too, using a recursive call. Otherwise return the best move found. :) return if (count($other-possible-moves)!=0) then tour:find-best-move($board, one-or-more($other-possible-moves), $minimum-exits, $new-best-so-far) else $new-best-so-far }; declare function tour:list-possible-moves ( $board as xs:integer*, $square as xs:integer ) as xs:integer* { (: This function, given the knight's position on the board, returns the set of squares he can move to. The squares will be ones that have not been visited before :) let $row as xs:integer := $square idiv 8 let $column as xs:integer := $square mod 8 return (if ($row > 1 and $column > 0 and $board[($square - 17) + 1]=0) then $square - 17 else (), if ($row > 1 and $column < 7 and $board[($square - 15) + 1]=0) then $square - 15 else (), if ($row > 0 and $column > 1 and $board[($square - 10) + 1]=0) then $square - 10 else (), if ($row > 0 and $column < 6 and $board[($square - 6) + 1]=0) then $square - 6 else (), if ($row < 6 and $column > 0 and $board[($square + 15) + 1]=0) then $square + 15 else (), if ($row < 6 and $column < 7 and $board[($square + 17) + 1]=0) then $square + 17 else (), if ($row < 7 and $column > 1 and $board[($square + 6) + 1]=0) then $square + 6 else (), if ($row < 7 and $column < 6 and $board[($square + 10) + 1]=0) then $square + 10 else () ) }; declare function tour:print-board ( $board as xs:integer* ) as element() { <html> <head> <title>Knight's tour</title> </head> <body> <div align=\"center\"> <h1>Knight's tour starting at {$start}</h1> <table border=\"1\" cellpadding=\"4\"> {for $row in 0 to 7 return <tr> {for $column in 0 to 7 let $color := if ((($row + $column) mod 2)=1) then 'xffff44' else 'white' return <td align=\"center\" bgcolor=\"{$color}\" width=\"22\">{ let $n := $board[$row * 8 + $column + 1] return if ($endd != 64 and $n = $endd) then <b>{$n}</b> else if ($n = 0) then \"&#xa0;\" else $n }</td> } </tr> } </table> <p>{ if ($endd != 64) then <a href=\"Tour?start={$start}&amp;end={$endd+1}\">Step</a> else () }</p> </div> </body> </html> }; tour:main()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<html><head><title>Knight's tour</title></head><body><div align=\"center\"><h1>Knight's tour starting at a1</h1><table border=\"1\" cellpadding=\"4\"><tr><td width=\"22\" align=\"center\" bgcolor=\"white\">36</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">19</td><td width=\"22\" align=\"center\" bgcolor=\"white\">22</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">5</td><td width=\"22\" align=\"center\" bgcolor=\"white\">38</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">9</td><td width=\"22\" align=\"center\" bgcolor=\"white\">24</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">7</td></tr><tr><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">21</td><td width=\"22\" align=\"center\" bgcolor=\"white\">4</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">37</td><td width=\"22\" align=\"center\" bgcolor=\"white\">42</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">23</td><td width=\"22\" align=\"center\" bgcolor=\"white\">6</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">39</td><td width=\"22\" align=\"center\" bgcolor=\"white\">10</td></tr><tr><td width=\"22\" align=\"center\" bgcolor=\"white\">18</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">35</td><td width=\"22\" align=\"center\" bgcolor=\"white\">20</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">49</td><td width=\"22\" align=\"center\" bgcolor=\"white\">44</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">41</td><td width=\"22\" align=\"center\" bgcolor=\"white\">8</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">25</td></tr><tr><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">3</td><td width=\"22\" align=\"center\" bgcolor=\"white\">50</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">43</td><td width=\"22\" align=\"center\" bgcolor=\"white\">46</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">55</td><td width=\"22\" align=\"center\" bgcolor=\"white\">62</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">11</td><td width=\"22\" align=\"center\" bgcolor=\"white\">40</td></tr><tr><td width=\"22\" align=\"center\" bgcolor=\"white\">34</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">17</td><td width=\"22\" align=\"center\" bgcolor=\"white\">54</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">59</td><td width=\"22\" align=\"center\" bgcolor=\"white\">48</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">45</td><td width=\"22\" align=\"center\" bgcolor=\"white\">26</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">63</td></tr><tr><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">51</td><td width=\"22\" align=\"center\" bgcolor=\"white\">2</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">47</td><td width=\"22\" align=\"center\" bgcolor=\"white\">56</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">61</td><td width=\"22\" align=\"center\" bgcolor=\"white\">58</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">29</td><td width=\"22\" align=\"center\" bgcolor=\"white\">12</td></tr><tr><td width=\"22\" align=\"center\" bgcolor=\"white\">16</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">33</td><td width=\"22\" align=\"center\" bgcolor=\"white\">60</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">53</td><td width=\"22\" align=\"center\" bgcolor=\"white\">14</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">31</td><td width=\"22\" align=\"center\" bgcolor=\"white\">64</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">27</td></tr><tr><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">1</td><td width=\"22\" align=\"center\" bgcolor=\"white\">52</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">15</td><td width=\"22\" align=\"center\" bgcolor=\"white\">32</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">57</td><td width=\"22\" align=\"center\" bgcolor=\"white\">28</td><td width=\"22\" align=\"center\" bgcolor=\"xffff44\">13</td><td width=\"22\" align=\"center\" bgcolor=\"white\">30</td></tr></table><p/></div></body></html>", false)
    );
  }

  /**
   *  Self axis on exactly one element node with name test that does not match..
   */
  @org.junit.Test
  public void axes090() {
    final XQuery query = new XQuery(
      "let $element as element(foo) := <foo/> return count($element/self::bar)",
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
   *  Self axis on exactly one element node with local name test that does not match..
   */
  @org.junit.Test
  public void axes091() {
    final XQuery query = new XQuery(
      "let $element as element(foo) := <foo/> return count($element/self::*:bar)",
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
   *  Self axis on exactly one element node with namespace test that does not match..
   */
  @org.junit.Test
  public void axes092() {
    final XQuery query = new XQuery(
      "declare namespace ns1 = \"http://www.example.org/ns1\"; declare namespace ns2 = \"http://www.example.org/ns2\"; let $element as element(ns1:foo) := <ns1:foo/> return count($element/self::ns2:*)",
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
   *  Self axis on exactly one attribute node with name test that does not match..
   */
  @org.junit.Test
  public void axes093() {
    final XQuery query = new XQuery(
      "let $attribute as attribute(foo) := attribute foo { } return count($attribute/self::bar)",
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
   *  Self axis on exactly one attribute node with local name test that does not match..
   */
  @org.junit.Test
  public void axes094() {
    final XQuery query = new XQuery(
      "let $attribute as attribute(foo) := attribute foo { } \n" +
      "            return count($attribute/self::*:bar)\n" +
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
      (
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Self axis on exactly one attribute node with namespace test that does not match..
   */
  @org.junit.Test
  public void axes095() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace ns1 = \"http://www.example.org/ns1\"; \n" +
      "        declare namespace ns2 = \"http://www.example.org/ns2\"; \n" +
      "        let $attribute as attribute(ns1:foo) := attribute ns1:foo { } \n" +
      "        return count($attribute/self::ns2:*)\n" +
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
      (
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Self axis on exactly one element node with name test that does not match..
   */
  @org.junit.Test
  public void axes096() {
    final XQuery query = new XQuery(
      "let $element as element(*) := <foo/> return count($element/self::bar)",
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
   *  Self axis on exactly one element node with local name test that does not match..
   */
  @org.junit.Test
  public void axes097() {
    final XQuery query = new XQuery(
      "let $element as element(*) := <foo/> return count($element/self::*:bar)",
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
   *  Self axis on exactly one element node with namespace test that does not match..
   */
  @org.junit.Test
  public void axes098() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace ns1 = \"http://www.example.org/ns1\"; \n" +
      "        declare namespace ns2 = \"http://www.example.org/ns2\"; \n" +
      "        let $element as element(*) := <ns1:foo/> \n" +
      "        return count($element/self::ns2:*)",
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
   *  Self axis on exactly one attribute node with name test that does not match..
   */
  @org.junit.Test
  public void axes099() {
    final XQuery query = new XQuery(
      "let $attribute as attribute(*) := attribute foo { } return count($attribute/self::bar)",
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
   *  Self axis on exactly one attribute node with local name test that does not match..
   */
  @org.junit.Test
  public void axes100() {
    final XQuery query = new XQuery(
      "let $attribute as attribute(*) := attribute foo { } return count($attribute/self::*:bar)",
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
   *  Self axis on exactly one attribute node with namespace test that does not match..
   */
  @org.junit.Test
  public void axes101() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace ns1 = \"http://www.example.org/ns1\"; \n" +
      "        declare namespace ns2 = \"http://www.example.org/ns2\"; \n" +
      "        let $attribute as attribute(*) := attribute ns1:foo { } \n" +
      "        return count($attribute/self::ns2:*)",
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
   *  Assert that the static type of a wild name test on an element with a type annotation is computed correctly..
   */
  @org.junit.Test
  public void axes102() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace ns1 = \"http://www.example.org/ns1\"; \n" +
      "        declare namespace ns2 = \"http://www.example.org/ns2\"; \n" +
      "        declare construction strip; \n" +
      "        let $element as element(*, xs:untyped) := <e>test</e> \n" +
      "        let $element as element(*, xs:untyped) := $element/self::* \n" +
      "        return count($element)\n" +
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
      assertEq("1")
    );
  }

  /**
   *  Wild name test on a comment node..
   */
  @org.junit.Test
  public void axes103() {
    final XQuery query = new XQuery(
      "let $comment as comment() := <!--comment--> return count($comment/self::*)",
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
   *  Name test on a comment node..
   */
  @org.junit.Test
  public void axes104() {
    final XQuery query = new XQuery(
      "let $comment as comment() := <!--comment--> return count($comment/self::foo)",
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
   *  Local name test on a comment node..
   */
  @org.junit.Test
  public void axes105() {
    final XQuery query = new XQuery(
      "let $comment as comment() := <!--comment--> return count($comment/self::*:foo)",
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
   *  Namespace test on a comment node..
   */
  @org.junit.Test
  public void axes106() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace ns1 = \"http://www.example.org/ns1\"; \n" +
      "        let $comment as comment() := <!--comment--> \n" +
      "        return count($comment/self::ns1:*)",
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
   *  Wild name test on a processing instruction node..
   */
  @org.junit.Test
  public void axes107() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $processing-instruction as processing-instruction() := <?processing instruction?> \n" +
      "        return count($processing-instruction/self::*)",
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
   *  Name test on a processing instruction node..
   */
  @org.junit.Test
  public void axes108() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $processing-instruction as processing-instruction() := <?processing instruction?> \n" +
      "        return count($processing-instruction/self::processing)",
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
   *  Local name test on a processing instruction node..
   */
  @org.junit.Test
  public void axes109() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $processing-instruction as processing-instruction() := <?processing instruction?> \n" +
      "        return count($processing-instruction/self::*:processing)",
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
   *  namespacs test on a processing instruction node..
   */
  @org.junit.Test
  public void axes110() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace ns1 = \"http://www.example.org/ns1\"; \n" +
      "        let $processing-instruction as processing-instruction() := <?processing instruction?> \n" +
      "        return count($processing-instruction/self::ns1:*)",
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
   *  Assert that the static type of a wild name test on an attribute with a type annotation is computed correctly..
   */
  @org.junit.Test
  public void axes111() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace ns1 = \"http://www.example.org/ns1\"; \n" +
      "        declare namespace ns2 = \"http://www.example.org/ns2\"; declare construction strip; \n" +
      "        let $element as element(*, xs:untyped) := <e a=\"value\" /> \n" +
      "        let $attribute as attribute(*, xs:untypedAtomic)* := $element/attribute::* \n" +
      "        return count($attribute)",
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
   *  The namespace axis is not recognized in XQuery..
   */
  @org.junit.Test
  public void axes112() {
    final XQuery query = new XQuery(
      "/*/namespace-node()",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  Use kind-test namespace-node() in an axis step for the attribute axis. Note: applies to both XPath and XQuery..
   */
  @org.junit.Test
  public void axes115() {
    final XQuery query = new XQuery(
      "/*/attribute::namespace-node()",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  Use kind-test namespace-node() with the self axis.
   */
  @org.junit.Test
  public void axes117() {
    final XQuery query = new XQuery(
      "namespace {\"xsl\"}{\"http://www.w3.org/1999/XSL/Transform\"} / self::namespace-node()",
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
        assertCount(1)
      &&
        assertType("namespace-node()")
      &&
        assertStringValue(false, "http://www.w3.org/1999/XSL/Transform")
      &&
        assertQuery("name($result) = \"xsl\"")
      )
    );
  }

  /**
   *  The namespace axis is not recognized in XQuery.  The namespace-node() kind test is new in XQuery 3.0..
   */
  @org.junit.Test
  public void axes127() {
    xquery10();
    final XQuery query = new XQuery(
      "/*/namespace-node()",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TreeTrunc.xml")));
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
   *  A complex set of root path expressions..
   */
  @org.junit.Test
  public void k2Axes1() {
    final XQuery query = new XQuery(
      "declare variable $var := document{<e><f>f's value</f></e>}; $var/(/)/(/)//f",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<f>f's value</f>", false)
    );
  }

  /**
   *  A comment can't appear between the ncname and the colon.a comment.
   */
  @org.junit.Test
  public void k2Axes10() {
    final XQuery query = new XQuery(
      "name(:hey:):*",
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
   *  Do a peculiar combination between axis self and last()..
   */
  @org.junit.Test
  public void k2Axes100() {
    final XQuery query = new XQuery(
      "empty(<a> <b/> <c/> </a>[self::b][last()])",
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
   *  Do a peculiar combination between axis self and last()..
   */
  @org.junit.Test
  public void k2Axes101() {
    final XQuery query = new XQuery(
      "<b/>[self::b][last()]",
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
        assertSerialization("<b/>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Execute a query in several different ways..
   */
  @org.junit.Test
  public void k2Axes102() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $a in (/) return \n" +
      "        for $b in $a/child::site return \n" +
      "        for $c in $b/child::people return \n" +
      "        for $d in $c/child::person return \n" +
      "            if ((some $id in $d/attribute::id satisfies \n" +
      "                    typeswitch ($id)\n" +
      "                     case $n as node() return $id = \"person0\"\n" +
      "                     default $d return ())) \n" +
      "            then $d/child::name \n" +
      "            else (), \n" +
      "        for $b in /site/people/person where $b/@id=\"person0\" return $b/name, \n" +
      "        /site/people/person[@id eq \"person0\"]/name",
      ctx);
    try {
      query.context(node(file("app/XMark/XMarkAuction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<name>Seongtaek Mattern</name><name>Seongtaek Mattern</name><name>Seongtaek Mattern</name>", false)
    );
  }

  /**
   *  Combine a function call with paths that requires sorting..
   */
  @org.junit.Test
  public void k2Axes103() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:isComplexType($typeID) { string($typeID) }; \n" +
      "        \"|\", //*/local:isComplexType(@type), \"|\"",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/CPPGlobals.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "|       _17 _17  _11c _11c _11 _11  _17c _17c _17 _17   _11   _23  _17 _11c  |")
    );
  }

  /**
   *  Use the focus from within an element constructor..
   */
  @org.junit.Test
  public void k2Axes104() {
    final XQuery query = new XQuery(
      "<a/>/<b>{.}</b>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b><a/></b>", false)
    );
  }

  /**
   *  Whitespace can't appear between the wildcard and the colon..
   */
  @org.junit.Test
  public void k2Axes11() {
    final XQuery query = new XQuery(
      "* :ncname",
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
   *  Whitespace can't appear between the wildcard and the colon..
   */
  @org.junit.Test
  public void k2Axes12() {
    final XQuery query = new XQuery(
      "ncname: *",
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
   *  A comment can't appear between the wildcard and the colon.a comment.
   */
  @org.junit.Test
  public void k2Axes13() {
    final XQuery query = new XQuery(
      "*(:hey:):ncname",
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
   *  A comment can't appear between the wildcard and the colon.a comment.
   */
  @org.junit.Test
  public void k2Axes14() {
    final XQuery query = new XQuery(
      "ncname:(:hey:)*",
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
   *  Whitespace can't appear between the wildcard, colon and ncname..
   */
  @org.junit.Test
  public void k2Axes15() {
    final XQuery query = new XQuery(
      "*(:hey:):(:hey:) ncname",
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
   *  Comments can't appear between wildcards and ncnames.a commenta comment.
   */
  @org.junit.Test
  public void k2Axes16() {
    final XQuery query = new XQuery(
      "*:(:hey:)ncname",
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
   *  A nametest cannot with a colon..
   */
  @org.junit.Test
  public void k2Axes17() {
    final XQuery query = new XQuery(
      "*:",
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
   *  Apply the parent axis to a computed text constructor..
   */
  @org.junit.Test
  public void k2Axes18() {
    final XQuery query = new XQuery(
      "empty(text {\"some text\"}/..)",
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
   *  Apply the parent axis to a computed processing instruction constructor..
   */
  @org.junit.Test
  public void k2Axes19() {
    final XQuery query = new XQuery(
      "empty(processing-instruction theName {\"some text\"}/..)",
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
   *  A complex set of root path expressions for where the context item is not a document node..
   */
  @org.junit.Test
  public void k2Axes2() {
    final XQuery query = new XQuery(
      "declare variable $var := <e><f>f's value</f></e>; $var/(/)/(/)//f",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0050")
    );
  }

  /**
   *  Apply the parent axis to a computed attribute constructor..
   */
  @org.junit.Test
  public void k2Axes20() {
    final XQuery query = new XQuery(
      "empty(attribute theName {\"some text\"}/..)",
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
   *  Apply the parent axis to a computed attribute constructor..
   */
  @org.junit.Test
  public void k2Axes21() {
    final XQuery query = new XQuery(
      "empty(element theName {\"some text\"}/..)",
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
   *  Apply the parent axis to a computed comment constructor..
   */
  @org.junit.Test
  public void k2Axes22() {
    final XQuery query = new XQuery(
      "empty(comment {\"some text\"}/..)",
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
   *  Apply the parent axis to a direct element constructor..
   */
  @org.junit.Test
  public void k2Axes23() {
    final XQuery query = new XQuery(
      "empty(<elem/>/..)",
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
   *  Apply the parent axis to a direct element constructor's text-child..
   */
  @org.junit.Test
  public void k2Axes24() {
    final XQuery query = new XQuery(
      "<e>some text</e>/text()/..",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e>some text</e>", false)
    );
  }

  /**
   *  Apply the parent axis to a direct element constructor's text-child..
   */
  @org.junit.Test
  public void k2Axes25() {
    final XQuery query = new XQuery(
      "<e><b/></e>/b/..",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e><b/></e>", false)
    );
  }

  /**
   *  Apply the parent axis to a direct element constructor's attribute..
   */
  @org.junit.Test
  public void k2Axes26() {
    final XQuery query = new XQuery(
      "<e attr=\"c\"/>/@attr/..",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e attr=\"c\"/>", false)
    );
  }

  /**
   *  Apply the parent axis to a direct element constructor's processing-instruction-child..
   */
  @org.junit.Test
  public void k2Axes27() {
    final XQuery query = new XQuery(
      "<e><?name data?></e>/processing-instruction()/..",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e><?name data?></e>", false)
    );
  }

  /**
   *  Apply the parent axis to a direct element constructor's comment-child..
   */
  @org.junit.Test
  public void k2Axes28() {
    final XQuery query = new XQuery(
      "<e><!--data --></e>/comment()/..",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e><!--data --></e>", false)
    );
  }

  /**
   *  No axis by name preceding-or-ancestor exists..
   */
  @org.junit.Test
  public void k2Axes29() {
    final XQuery query = new XQuery(
      "preceding-or-ancestor::*",
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
   *  Path expression where the last step in an xs:integer..
   */
  @org.junit.Test
  public void k2Axes3() {
    final XQuery query = new XQuery(
      "declare variable $v := <a><b/><b/><b/></a>; deep-equal($v//45, (45, 45, 45, 45))",
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
   *  Apply axis '..' to the return value of fn:root()..
   */
  @org.junit.Test
  public void k2Axes30() {
    final XQuery query = new XQuery(
      "empty(fn:root(<e/>)/..)",
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
   *  Apply axis '..' to a directly constructed element..
   */
  @org.junit.Test
  public void k2Axes31() {
    final XQuery query = new XQuery(
      "empty(<e/>/..)",
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
   *  Apply axis '..' to the return value of fn:root()..
   */
  @org.junit.Test
  public void k2Axes32() {
    final XQuery query = new XQuery(
      "declare variable $myVar := <e/>; empty(fn:root($myVar/(/)/..))",
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
      ||
        error("XPDY0050")
      )
    );
  }

  /**
   *  Apply axis '..' to a '/' step..
   */
  @org.junit.Test
  public void k2Axes33() {
    final XQuery query = new XQuery(
      "declare variable $myVar := document { <e/>}; empty($myVar/(/)/..)",
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
   *  '//' at the end of a path expression is a syntax error..
   */
  @org.junit.Test
  public void k2Axes34() {
    final XQuery query = new XQuery(
      "nametest//",
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
        error("XPST0003")
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   *  '/' at the end of a path expression is a syntax error..
   */
  @org.junit.Test
  public void k2Axes35() {
    final XQuery query = new XQuery(
      "nametest/",
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
        error("XPST0003")
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   *  '/' at the end of a path expression is a syntax error(#2)..
   */
  @org.junit.Test
  public void k2Axes36() {
    final XQuery query = new XQuery(
      "declare variable $myVar := <e/>; $myVar/(/)/",
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
   *  'self()' is an invalid note test..
   */
  @org.junit.Test
  public void k2Axes37() {
    final XQuery query = new XQuery(
      "parent::self()",
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
   *  '..' inside a predicate where the context item is of wrong type..
   */
  @org.junit.Test
  public void k2Axes38() {
    final XQuery query = new XQuery(
      "123[..]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0020")
    );
  }

  /**
   *  'element()' inside a predicate where the context item is of wrong type..
   */
  @org.junit.Test
  public void k2Axes39() {
    final XQuery query = new XQuery(
      "1[element()]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0020")
    );
  }

  /**
   *  Path expression where the last step in an xs:integer(#2)..
   */
  @org.junit.Test
  public void k2Axes4() {
    final XQuery query = new XQuery(
      "declare variable $v := <a><b/><b/><b/></a>; $v//45",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "45 45 45 45")
    );
  }

  /**
   *  '..' inside a predicate where the context item is of wrong type(#2)..
   */
  @org.junit.Test
  public void k2Axes40() {
    final XQuery query = new XQuery(
      "(1, <e/>)[..]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0020")
    );
  }

  /**
   *  '..' inside a predicate where the context item is of wrong type(#3)..
   */
  @org.junit.Test
  public void k2Axes41() {
    final XQuery query = new XQuery(
      "(<e/>, 1)[..]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0020")
    );
  }

  /**
   *  Ensure a parent axis inside a predicate where the source is a direct element constructor, evaluate to the empty sequence..
   */
  @org.junit.Test
  public void k2Axes42() {
    final XQuery query = new XQuery(
      "empty(<e/>[parent::node()])",
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
   *  Ensure '5 * /' is parsed properly..
   */
  @org.junit.Test
  public void k2Axes43() {
    final XQuery query = new XQuery(
      "(1, 5 * /)[1]",
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
        assertEq("1")
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   *  Ensure '/' is parsed properly..
   */
  @org.junit.Test
  public void k2Axes44() {
    final XQuery query = new XQuery(
      "(1, /)[1]",
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
        assertEq("1")
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   *  Ensure '/' is parsed properly(#2)..
   */
  @org.junit.Test
  public void k2Axes45() {
    final XQuery query = new XQuery(
      "(/, 1)[2]",
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
        assertEq("1")
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   *  '//' by itself is not a valid expression..
   */
  @org.junit.Test
  public void k2Axes46() {
    final XQuery query = new XQuery(
      "//",
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
   *  Apply '/' to a variable..
   */
  @org.junit.Test
  public void k2Axes47() {
    final XQuery query = new XQuery(
      "declare variable $myVar := <e/>; empty($myVar/descendant-or-self::text())",
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
   *  Apply '/' to a variable, with an xs:integer at the end..
   */
  @org.junit.Test
  public void k2Axes48() {
    final XQuery query = new XQuery(
      "declare variable $myVar := <e/>; $myVar/(<a/>, <b/>, <?d ?>, <!-- e-->, attribute name {}, document {()})/3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3 3 3 3 3 3")
    );
  }

  /**
   *  Apply '/' to a variable, with fn:number() at the end. That is, an implicit dependency on the next-last step..
   */
  @org.junit.Test
  public void k2Axes49() {
    final XQuery query = new XQuery(
      "declare variable $myVar := <e/>; $myVar/(<a/>, <b/>, <?d ?>, <!-- e-->, attribute name {}, document {()})/number()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN NaN NaN NaN NaN NaN")
    );
  }

  /**
   *  Whitespace: comment can't appear between the ncname and the colon..
   */
  @org.junit.Test
  public void k2Axes5() {
    final XQuery query = new XQuery(
      "*:(:hey:)ncname",
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
   *  '1/3' is a type error..
   */
  @org.junit.Test
  public void k2Axes50() {
    final XQuery query = new XQuery(
      "1/3",
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
   *  'xs:string/3' is a type error..
   */
  @org.junit.Test
  public void k2Axes51() {
    final XQuery query = new XQuery(
      "string(<e/>)/3",
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
   *  A type error in a complex path expression. Some implementations may optimize away the error..
   */
  @org.junit.Test
  public void k2Axes52() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $myVar := <e/>; \n" +
      "        empty($myVar/preceding-sibling::comment()/a/b/c/1/@*)",
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
      ||
        error("XPTY0019")
      ||
        error("XPTY0020")
      )
    );
  }

  /**
   *  Many atomic values with predicates in a path expression..
   */
  @org.junit.Test
  public void k2Axes53() {
    final XQuery query = new XQuery(
      "(1, 2, 3)[1]/(1, 2)[last()]/\"a string\"",
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
   *  The namespace axis is not recognized in XQuery..
   */
  @org.junit.Test
  public void k2Axes54() {
    final XQuery query = new XQuery(
      "namespace::*",
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
   *  Apply the attribute axis to the empty sequence..
   */
  @org.junit.Test
  public void k2Axes55() {
    final XQuery query = new XQuery(
      "empty(()/@attr)",
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
   *  Apply the child axis to the empty sequence..
   */
  @org.junit.Test
  public void k2Axes56() {
    final XQuery query = new XQuery(
      "empty(()/name)",
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
   *  The context item invokes sorting and duplicate elimination..
   */
  @org.junit.Test
  public void k2Axes57() {
    final XQuery query = new XQuery(
      "declare variable $var := <a> <b/> <c/> <d/> </a>; ($var/*, $var/*)/.",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b/><c/><d/>", false)
    );
  }

  /**
   *  Content being duplicated with the comma operator...
   */
  @org.junit.Test
  public void k2Axes58() {
    final XQuery query = new XQuery(
      "declare variable $var := <a> <b/> <c/> <d/> </a>; ($var/*, $var/*)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b/><c/><d/><b/><c/><d/>", false)
    );
  }

  /**
   *  Evaluate the child node from the last node in a tree..
   */
  @org.junit.Test
  public void k2Axes59() {
    final XQuery query = new XQuery(
      "declare variable $i := <root> <child/> <child/> <child> <child2> <child3> <leaf/> </child3> </child2> </child> </root>; 1, root($i)//leaf/child::node(), 1",
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
        assertStringValue(false, "1 1")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  A comment can't appear between the ncname and the colon.a comment.
   */
  @org.junit.Test
  public void k2Axes6() {
    final XQuery query = new XQuery(
      "*(:hey:):ncname",
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
   *  Apply the child axis on a document whose last nodes are attributes..
   */
  @org.junit.Test
  public void k2Axes60() {
    final XQuery query = new XQuery(
      "empty(<element attr=\"foo\" attr2=\"foo\"/>/*)",
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
   *  Apply the child axis on an element that has attributes only..
   */
  @org.junit.Test
  public void k2Axes61() {
    final XQuery query = new XQuery(
      "empty(<element attr=\"foo\" attr2=\"foo\"/>/*)",
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
   *  Apply descendant-or-self to a node set constructed with element constructors..
   */
  @org.junit.Test
  public void k2Axes62() {
    final XQuery query = new XQuery(
      "<e> <b attr=\"fo\"/> <b/> </e>/descendant-or-self::node()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e><b attr=\"fo\"/><b/></e><b attr=\"fo\"/><b/>", false)
    );
  }

  /**
   *  Apply the child axis to an element who has one attribute, combined with the sequence iterator..
   */
  @org.junit.Test
  public void k2Axes63() {
    final XQuery query = new XQuery(
      "1, <b attr=\"fo\"/>/child::node(), 1",
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
   *  Invoke the child axis on two elements with one attribute..
   */
  @org.junit.Test
  public void k2Axes64() {
    final XQuery query = new XQuery(
      "<e> <b/> <b a=\"\"/> </e>/b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b/><b a=\"\"/>", false)
    );
  }

  /**
   *  Invoke the child axis on two elements with one attribute..
   */
  @org.junit.Test
  public void k2Axes65() {
    final XQuery query = new XQuery(
      "<e> <b a=\"\"/> <b/> </e>/b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b a=\"\"/><b/>", false)
    );
  }

  /**
   *  Invoke the child axis on mixed content..
   */
  @org.junit.Test
  public void k2Axes66() {
    final XQuery query = new XQuery(
      "<e> <!-- comment --> <?PA ?>text<b/> <?PB ?> <b/> <?PC ?> </e>/child::node()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!-- comment --><?PA ?>text<b/><?PB ?><b/><?PC ?>", false)
    );
  }

  /**
   *  Apply the descendant axis on an element, as operand to the comma operator..
   */
  @org.junit.Test
  public void k2Axes67() {
    final XQuery query = new XQuery(
      "1, <e/>/descendant::node(), 1",
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
   *  Apply the descendant-or-self axis on an element, as operand to the comma operator..
   */
  @org.junit.Test
  public void k2Axes68() {
    final XQuery query = new XQuery(
      "1, <e/>/descendant-or-self::node(), 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("1<e/>1", false)
    );
  }

  /**
   *  Apply the descendant axis on an element that has an attribute, as operand to the comma operator..
   */
  @org.junit.Test
  public void k2Axes69() {
    final XQuery query = new XQuery(
      "1, <e attr=\"\"/>/descendant::node(), 1",
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
   *  Whitespace can't appear between the wildcard and the colon..
   */
  @org.junit.Test
  public void k2Axes7() {
    final XQuery query = new XQuery(
      "* :ncname",
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
   *  Apply the descendant-or-self axis on an element that has an attribute, as operand to the comma operator..
   */
  @org.junit.Test
  public void k2Axes70() {
    final XQuery query = new XQuery(
      "1, <e attr=\"\"/>/descendant-or-self::node(), 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("1<e attr=\"\"/>1", false)
    );
  }

  /**
   *  Apply function fn:empty() on the descendant axis applied on an element constructor..
   */
  @org.junit.Test
  public void k2Axes71() {
    final XQuery query = new XQuery(
      "empty(<e/>/descendant::node())",
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
   *  Apply function fn:empty() on the descendant-or-self axis applied on an element constructor..
   */
  @org.junit.Test
  public void k2Axes72() {
    final XQuery query = new XQuery(
      "empty(<e/>/descendant-or-self::node())",
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
   *  Apply function fn:empty() on the descendant axis applied on an element constructor that has an attribute..
   */
  @org.junit.Test
  public void k2Axes73() {
    final XQuery query = new XQuery(
      "empty(<e attr=\"\"/>/descendant::node())",
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
   *  Apply function fn:empty() on the descendant-or-self axis applied on an element constructor that has an attribute..
   */
  @org.junit.Test
  public void k2Axes74() {
    final XQuery query = new XQuery(
      "empty(<e attr=\"\"/>/descendant-or-self::node())",
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
   *  Combine axis descendant-or-self, function last() and axis ancestor..
   */
  @org.junit.Test
  public void k2Axes75() {
    final XQuery query = new XQuery(
      "<a> <b c=\"\"/> <d/> </a>/descendant-or-self::node()[last()]/ancestor::*",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a><b c=\"\"/><d/></a>", false)
    );
  }

  /**
   *  Combine axis descendant-or-self and function last()..
   */
  @org.junit.Test
  public void k2Axes76() {
    final XQuery query = new XQuery(
      "<a> <b c=\"\"/> <d/> </a>/descendant-or-self::node()[last()]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<d/>", false)
    );
  }

  /**
   *  There is no axis by name 'preceeding'..
   */
  @org.junit.Test
  public void k2Axes77() {
    final XQuery query = new XQuery(
      "preceeding::node()",
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
   *  Apply fn:count() to a set of nodes involving the parent axis..
   */
  @org.junit.Test
  public void k2Axes78() {
    final XQuery query = new XQuery(
      "<a> <b c=\"\"/> <d/> </a>//node()/../count(.)",
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
   *  Apply fn:count() to a set of nodes involving the descendant axis..
   */
  @org.junit.Test
  public void k2Axes79() {
    final XQuery query = new XQuery(
      "count(<a> <b c=\"\"/> <d/> </a>//node())",
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
   *  A comment can't appear between the wildcard and the colon.a comment.
   */
  @org.junit.Test
  public void k2Axes8() {
    final XQuery query = new XQuery(
      "*(:hey:):ncname",
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
   *  Apply fn:count() to a set of nodes involving the descendant axis..
   */
  @org.junit.Test
  public void k2Axes80() {
    final XQuery query = new XQuery(
      "count(<a> <b c=\"\"/> <d/> </a>/descendant-or-self::node())",
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
   *  Ensure that an element name test after an attribute test matches..
   */
  @org.junit.Test
  public void k2Axes81() {
    final XQuery query = new XQuery(
      "<a> <b id=\"person0\"> <c/> </b> </a>/*[attribute::id eq \"person0\"]/c",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<c/>", false)
    );
  }

  /**
   *  Ensure that an element name test after an attribute with abbreviated syntax test matches..
   */
  @org.junit.Test
  public void k2Axes82() {
    final XQuery query = new XQuery(
      "<a> <b id=\"person0\"> <c/> </b> </a>/*[@id eq \"person0\"]/c",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<c/>", false)
    );
  }

  /**
   *  Simple parser test of the attribute() test appearing after the attribute axis..
   */
  @org.junit.Test
  public void k2Axes83() {
    final XQuery query = new XQuery(
      "empty(<e/>/attribute::attribute())",
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
   *  Simple parser test of the schema-attribute() test appearing after the attribute axis..
   */
  @org.junit.Test
  public void k2Axes84() {
    final XQuery query = new XQuery(
      "<e/>/attribute::schema-attribute(foo)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  The attribute name isn't optional..
   */
  @org.junit.Test
  public void k2Axes85() {
    final XQuery query = new XQuery(
      "<e/>/attribute::schema-attribute()",
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
   *  A significant set of path expressions with different combinations of source values, axes and node tests, 
   *         that always evaluate to the empty sequence, no matter what document structure that is being walked. 
   *         The list is not guaranteed to be exhaustive. Some implementations detect this and rewrite away such 
   *         combinations or warn the user about such constructs. Since static typing implementations may infer 
   *         these expressions to the empty sequence, they may raise XPST0005.attribute axis, and all other axes 
   *         that inference can assert might produce attributes.
   *       .
   */
  @org.junit.Test
  public void k2Axes86() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $root as document-node() := . treat as document-node(); \n" +
      "        <empty> { (: attribute axis, and all other axes that inference can assert might produce attributes. \n" +
      "             These are placed first, so we don't raise XQTY0024. :) \n" +
      "             $root/foo/child::attribute(foo), \n" +
      "             $root//element()/self::attribute(), \n" +
      "             $root//text()/self::attribute(), \n" +
      "             $root/self::attribute(), \n" +
      "             $root/foo/descendant::attribute(foo), \n" +
      "             $root//processing-instruction()/self::attribute(), \n" +
      "             $root/attribute(foo), $root/attribute(), \n" +
      "             $root/attribute::attribute(), \n" +
      "             $root//parent::attribute(), \n" +
      "             $root//document-node()/self::attribute(), \n" +
      "             $root/@*, $root/*/@*[self::*], \n" +
      "             $root//comment()/descendant-or-self::attribute(), \n" +
      "             $root//processing-instruction()/descendant-or-self::attribute(), \n" +
      "             $root//text()/descendant-or-self::attribute(), \n" +
      "             $root//document-node()/descendant-or-self::attribute(), \n" +
      "             $root//parent::attribute(), \n" +
      "             $root//ancestor::attribute(), \n" +
      "             $root//ancestor-or-self::attribute(), \n" +
      "             $root/self::attribute(), \n" +
      "             $root//attribute()/child::node(), \n" +
      "             $root//attribute::text(), \n" +
      "             $root//attribute::comment(), \n" +
      "             $root//attribute::processing-instruction(), \n" +
      "             $root//attribute::document-node(), \n" +
      "             $root//attribute::document-node(element(foo)),\n" +
      "             $root//attribute()/self::text(),\n" +
      "             $root//attribute()/self::processing-instruction(), \n" +
      "             $root//attribute()/self::element(), \n" +
      "             $root//attribute()/self::document-node(), \n" +
      "             $root//attribute()/self::comment(), \n" +
      "             $root//*/attribute()//node(), \n" +
      "             $root//*/attribute()/descendant::node(), \n" +
      "             $root//attribute::element(), \n" +
      "             $root//comment()/child::node(), \n" +
      "             $root//processing-instruction()/child::node(), \n" +
      "             $root//text()/child::node(),\n" +
      "             $root//attribute()/descendant::node(), \n" +
      "             $root//comment()/descendant::node(), \n" +
      "             $root//text()/descendant::node(), \n" +
      "             $root//processing-instruction()/descendant::node(), \n" +
      "             $root//comment()/descendant-or-self::text(), \n" +
      "             $root//comment()/descendant-or-self::processing-instruction(), \n" +
      "             $root//comment()/descendant-or-self::element(), \n" +
      "             $root//comment()/descendant-or-self::document-node(), \n" +
      "             $root//processing-instruction()/descendant-or-self::text(), \n" +
      "             $root//processing-instruction()/descendant-or-self::comment(), \n" +
      "             $root//processing-instruction()/descendant-or-self::element(), \n" +
      "             $root//processing-instruction()/descendant-or-self::document-node(), \n" +
      "             $root//text()/descendant-or-self::processing-instruction(), \n" +
      "             $root//text()/descendant-or-self::comment(), \n" +
      "             $root//text()/descendant-or-self::element(), \n" +
      "             $root//text()/descendant-or-self::document-node(), \n" +
      "             $root//attribute()/descendant-or-self::processing-instruction(), \n" +
      "             $root//attribute()/descendant-or-self::text(), \n" +
      "             $root//attribute()/descendant-or-self::comment(), \n" +
      "             $root//attribute()/descendant-or-self::element(), \n" +
      "             $root//attribute()/descendant-or-self::document-node(), \n" +
      "             $root/.., $root//parent::comment(), \n" +
      "             $root//parent::processing-instruction(), \n" +
      "             $root//parent::text(), \n" +
      "             $root//ancestor::comment(), \n" +
      "             $root//ancestor::processing-instruction(), \n" +
      "             $root//ancestor::text(), \n" +
      "             $root/self::comment(), \n" +
      "             $root/self::processing-instruction(), \n" +
      "             $root/self::text(), \n" +
      "             $root/self::element(), \n" +
      "             $root//element()/self::text(), \n" +
      "             $root//element()/self::processing-instruction(), \n" +
      "             $root//element()/self::document-node(), \n" +
      "             $root//element()/self::comment(), \n" +
      "             $root//processing-instruction()/self::text(), \n" +
      "             $root//processing-instruction()/self::element(), \n" +
      "             $root//processing-instruction()/self::document-node(), \n" +
      "             $root//processing-instruction()/self::comment(), \n" +
      "             $root//text()/self::processing-instruction(), \n" +
      "             $root//text()/self::element(), \n" +
      "             $root//text()/self::document-node(), \n" +
      "             $root//text()/self::comment(), \n" +
      "             $root//document-node()/self::processing-instruction(), \n" +
      "             $root//document-node()/self::element(), \n" +
      "             $root//document-node()/self::text(), \n" +
      "             $root//document-node()/self::comment() } </empty>, \n" +
      "             exists($root//*/attribute()/descendant-or-self::node())",
      ctx);
    try {
      query.context(node(file("prod/AxisStep/TopMany.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<empty/>true", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Ensure node sorting is applied when the left operand is the result of the comma operator..
   */
  @org.junit.Test
  public void k2Axes87() {
    final XQuery query = new XQuery(
      "<result> { <e> <a>1</a> <b>2</b> </e>/(b, a)/. } </result>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><a>1</a><b>2</b></result>", false)
    );
  }

  /**
   *  Ensure node sorting is applied when the left operand is the result of the comma operator, while using variable references..
   */
  @org.junit.Test
  public void k2Axes88() {
    final XQuery query = new XQuery(
      "declare variable $i := <e> <a>1</a> <b>2</b> </e>; <result> { ($i/b, $i/a)/. } </result>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><a>1</a><b>2</b></result>", false)
    );
  }

  /**
   *  Ensure attribute tests match, and that attributes are properly copied when combined with a default element namespace declaration..
   */
  @org.junit.Test
  public void k2Axes89() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://typedecl\"; <r> { <e attr=\"foo\"/>/@attr } </r>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<r xmlns=\"http://typedecl\" attr=\"foo\"/>", false)
    );
  }

  /**
   *  Whitespace can't appear between the ncname and the colon..
   */
  @org.junit.Test
  public void k2Axes9() {
    final XQuery query = new XQuery(
      "ncname :*",
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
   *  An incorrectly ending QName..
   */
  @org.junit.Test
  public void k2Axes90() {
    final XQuery query = new XQuery(
      "prefix:",
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
   *  An incorrectly ending QName, with space..
   */
  @org.junit.Test
  public void k2Axes91() {
    final XQuery query = new XQuery(
      "prefix:",
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
   *  Test parsing of 'gt'..
   */
  @org.junit.Test
  public void k2Axes92() {
    final XQuery query = new XQuery(
      "empty(<e/>/(gt gt gt))",
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
        error("XPDY0002")
      )
    );
  }

  /**
   *  Space is disallowed after the first angle bracket in a direct element constructor..
   */
  @org.junit.Test
  public void k2Axes93() {
    final XQuery query = new XQuery(
      "< asd />",
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
   *  Use a single 'declare' nametest..
   */
  @org.junit.Test
  public void k2Axes94() {
    final XQuery query = new XQuery(
      "declare",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Use an invalid function declaration..
   */
  @org.junit.Test
  public void k2Axes95() {
    final XQuery query = new XQuery(
      "eclare function",
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
   *  Use an invalid function declaration(#2)..
   */
  @org.junit.Test
  public void k2Axes96() {
    final XQuery query = new XQuery(
      "declare function name",
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
   *  Use an invalid function declaration(#3)..
   */
  @org.junit.Test
  public void k2Axes97() {
    final XQuery query = new XQuery(
      "declare function local:foo() external;",
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
        error("XPST0003")
      ||
        error("XPST0017")
      )
    );
  }

  /**
   *  Use a set of reserved keywords as nametests..
   */
  @org.junit.Test
  public void k2Axes98() {
    final XQuery query = new XQuery(
      "xquery, version, encoding, default, declare, function, option, collation, schema, import",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Use an invalid function declaration(#3)..
   */
  @org.junit.Test
  public void k2Axes99() {
    final XQuery query = new XQuery(
      "declare function foo() external; 1",
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
        error("XQST0045")
      ||
        error("XPST0017")
      )
    );
  }

  /**
   *  Evaluation of a step axis, which operates on a non node context item..
   */
  @org.junit.Test
  public void axisErr1() {
    final XQuery query = new XQuery(
      "let $var := <anElement>Some content</anElement> return $var/20[child::text()]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0020")
    );
  }

  /**
   *  Tests to child::attribute(b) .
   */
  @org.junit.Test
  public void cbclChildAxis001() {
    final XQuery query = new XQuery(
      "count(<a b=\"blah\"/>/child::attribute(b))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Tests Equals method on NodeOperator tests .
   */
  @org.junit.Test
  public void cbclExcept001() {
    final XQuery query = new XQuery(
      "\n" +
      "      let $x := <a><b/><c/><d/></a> return count(($x/(node() except b) | $x/(node() except b)))\n" +
      "   ",
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
   *  Tests for following-sibling::* .
   */
  @org.junit.Test
  public void cbclFollowingAxis001() {
    final XQuery query = new XQuery(
      "count(<a><b/><b/></a>/b[1]/following-sibling::*)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  A test with both nodes and atomics appearing in a (not last) step .
   */
  @org.junit.Test
  public void cbclPathNodesAndAtomics() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:f($x) { if ($x mod 2 = 1) then <a/> else \"a\" };\n" +
      "        let $y := for $x in (1 to 10) return <e>{$x}</e> return $y/local:f(.)/a",
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
   *  Tests for preceding-sibling::* .
   */
  @org.junit.Test
  public void cbclPrecedingAxis001() {
    final XQuery query = new XQuery(
      "count(<a><b/><b/></a>/b[2]/preceding-sibling::*)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Test for preceding-sibling::b .
   */
  @org.junit.Test
  public void cbclPrecedingAxis002() {
    final XQuery query = new XQuery(
      "count(<a><b/><b/></a>/b[2]/preceding-sibling::b)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Test for preceding-sibling::b .
   */
  @org.junit.Test
  public void cbclPrecedingAxis003() {
    final XQuery query = new XQuery(
      "count(<a><b/><b/></a>/b[1]/preceding-sibling::b)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   *  Tests self attribute test .
   */
  @org.junit.Test
  public void cbclSelfAxis001() {
    final XQuery query = new XQuery(
      "<a> { <a b=\"blah\"/>/@*/self::attribute(b) } </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a b=\"blah\"/>", false)
    );
  }

  /**
   *  Tests Equals function on wild localname tests .
   */
  @org.junit.Test
  public void cbclWild001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace foo=\"test\";\n" +
      "        let $x := <a><foo:b/><c/><d/></a> return count($x/foo:* | $x/foo:*)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Tests the typing of a test foo:bar/foo:* .
   */
  @org.junit.Test
  public void cbclWild002() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace foo=\"http://localhost/\";\n" +
      "        <a><foo:b/><foo:c/></a>/foo:b/self::foo:*",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<foo:b xmlns:foo=\"http://localhost/\"/>", false)
    );
  }

  /**
   *  Evaluation of static typing feature for the child axes. Context item not a node .
   */
  @org.junit.Test
  public void statictypingaxis1() {
    final XQuery query = new XQuery(
      "(10)/child::*",
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
   *  Evaluation of static typing feature for the self axes. Context item not a node .
   */
  @org.junit.Test
  public void statictypingaxis2() {
    final XQuery query = new XQuery(
      "(10)/self::*",
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
   *  Evaluation of static typing feature for the attribute axes. Context item not a node .
   */
  @org.junit.Test
  public void statictypingaxis3() {
    final XQuery query = new XQuery(
      "(10)/attribute::*",
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
   *  Evaluation of static typing feature for the parent axes. Context item not a node .
   */
  @org.junit.Test
  public void statictypingaxis4() {
    final XQuery query = new XQuery(
      "(10)/parent::*",
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
   *  Evaluation of static typing feature for the descendant axes. Context item not a node .
   */
  @org.junit.Test
  public void statictypingaxis5() {
    final XQuery query = new XQuery(
      "(10)/descendant::*",
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
   *  Evaluation of static typing feature for the descendant-or-self axes. Context item not a node .
   */
  @org.junit.Test
  public void statictypingaxis6() {
    final XQuery query = new XQuery(
      "(10)/descendant-or-self::*",
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
}
