package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the GeneralComp.ne production.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdGeneralCompNe extends QT3TestSet {

  /**
   *  General comparison where one or more operands is the empty sequence. .
   */
  @org.junit.Test
  public void kGenCompNE1() {
    final XQuery query = new XQuery(
      "(() != ()) eq false()",
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
   *  General comparison causing numeric promotion from xs:untypedAtomic. .
   */
  @org.junit.Test
  public void kGenCompNE10() {
    final XQuery query = new XQuery(
      "not(\"2\" = xs:untypedAtomic(\"1\"))",
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
   *  General comparison causing numeric promotion from xs:untypedAtomic. .
   */
  @org.junit.Test
  public void kGenCompNE11() {
    final XQuery query = new XQuery(
      "2 != xs:untypedAtomic(\"1\")",
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
   *  General comparison causing numeric promotion from xs:untypedAtomic. .
   */
  @org.junit.Test
  public void kGenCompNE12() {
    final XQuery query = new XQuery(
      "not(1 != xs:untypedAtomic(\"1\"))",
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
   *  An expression involving the '!=' operator that trigger certain optimization paths in some implementations. .
   */
  @org.junit.Test
  public void kGenCompNE13() {
    final XQuery query = new XQuery(
      "count((0, timezone-from-time(current-time()))) != 0",
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
   *  General comparison involving xs:untypedAtomic/xs:string. .
   */
  @org.junit.Test
  public void kGenCompNE14() {
    final XQuery query = new XQuery(
      "\"a string\" != \"a stringDIFF\"",
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
   *  General comparison involving xs:untypedAtomic/xs:string. .
   */
  @org.junit.Test
  public void kGenCompNE15() {
    final XQuery query = new XQuery(
      "not(\"a string\" != \"a string\")",
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
   *  General comparison involving xs:untypedAtomic/xs:string. .
   */
  @org.junit.Test
  public void kGenCompNE16() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"a string\") != \"a stringDIFF\"",
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
   *  General comparison involving xs:untypedAtomic/xs:string. .
   */
  @org.junit.Test
  public void kGenCompNE17() {
    final XQuery query = new XQuery(
      "\"a string\" != xs:untypedAtomic(\"a stringDIFF\")",
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
   *  General comparison involving xs:untypedAtomic/xs:string. .
   */
  @org.junit.Test
  public void kGenCompNE18() {
    final XQuery query = new XQuery(
      "not(\"a string\" != xs:untypedAtomic(\"a string\"))",
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
   *  General comparison involving xs:untypedAtomic/xs:string. .
   */
  @org.junit.Test
  public void kGenCompNE19() {
    final XQuery query = new XQuery(
      "not(xs:untypedAtomic(\"a string\") != \"a string\")",
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
   *  General comparison where one or more operands is the empty sequence. .
   */
  @org.junit.Test
  public void kGenCompNE2() {
    final XQuery query = new XQuery(
      "not(() = 1 )",
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
   *  General comparison causing a xs:untypedAtomic value to be cast to xs:boolean, and then compared. .
   */
  @org.junit.Test
  public void kGenCompNE20() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") != false()",
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
   *  General comparison causing a xs:untypedAtomic value to be cast to xs:boolean, and then compared. .
   */
  @org.junit.Test
  public void kGenCompNE21() {
    final XQuery query = new XQuery(
      "false() != xs:untypedAtomic(\"true\")",
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
   *  General comparison causing a xs:untypedAtomic value to be cast to xs:boolean, and then compared. .
   */
  @org.junit.Test
  public void kGenCompNE22() {
    final XQuery query = new XQuery(
      "not(xs:untypedAtomic(\"false\") != false())",
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
   *  General comparison causing a xs:untypedAtomic value to be cast to xs:boolean, and then compared. .
   */
  @org.junit.Test
  public void kGenCompNE23() {
    final XQuery query = new XQuery(
      "not(false() != xs:untypedAtomic(\"false\"))",
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
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompNE24() {
    final XQuery query = new XQuery(
      "(1, 2, 3) != 1",
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
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompNE25() {
    final XQuery query = new XQuery(
      "(1, 2, 3) != 2",
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
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompNE26() {
    final XQuery query = new XQuery(
      "(1, 2, 3) != 3",
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
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompNE27() {
    final XQuery query = new XQuery(
      "1 != 2",
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
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompNE28() {
    final XQuery query = new XQuery(
      "1 != (1, 2, 3)",
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
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompNE29() {
    final XQuery query = new XQuery(
      "2 != (1, 2, 3)",
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
   *  General comparison where one or more operands is the empty sequence. .
   */
  @org.junit.Test
  public void kGenCompNE3() {
    final XQuery query = new XQuery(
      "not(() != () )",
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
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompNE30() {
    final XQuery query = new XQuery(
      "3 != (1, 2, 3)",
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
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompNE31() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"three\") != 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompNE32() {
    final XQuery query = new XQuery(
      "3 != xs:untypedAtomic(\"three\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompNE33() {
    final XQuery query = new XQuery(
      "\"2\" != 1",
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
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompNE34() {
    final XQuery query = new XQuery(
      "1 != \"2\"",
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
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompNE35() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"falseERR\") != false()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompNE36() {
    final XQuery query = new XQuery(
      "1 != \"1\"",
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
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompNE37() {
    final XQuery query = new XQuery(
      "xs:string(\"false\") != false()",
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
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompNE38() {
    final XQuery query = new XQuery(
      "false() != xs:string(\"false\")",
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
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompNE39() {
    final XQuery query = new XQuery(
      "false() != xs:untypedAtomic(\"falseERR\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  General comparison where one or more operands is the empty sequence. .
   */
  @org.junit.Test
  public void kGenCompNE4() {
    final XQuery query = new XQuery(
      "not(1 != () )",
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
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompNE40() {
    final XQuery query = new XQuery(
      "false() != xs:anyURI(\"example.com/\")",
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
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompNE41() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"example.com/\") != false()",
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
   *  General comparison where one or more operands is the empty sequence. .
   */
  @org.junit.Test
  public void kGenCompNE5() {
    final XQuery query = new XQuery(
      "not(() != 1 )",
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
   *  A syntactically invalid expression that reminds of a general comparison operator. .
   */
  @org.junit.Test
  public void kGenCompNE6() {
    final XQuery query = new XQuery(
      "1 !! 1",
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
   *  Example from the XPath 2.0 specification. .
   */
  @org.junit.Test
  public void kGenCompNE7() {
    final XQuery query = new XQuery(
      "(1, 2) != (2, 3)",
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
   *  General comparison causing numeric promotion from xs:untypedAtomic. .
   */
  @org.junit.Test
  public void kGenCompNE8() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"2\") != 1",
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
   *  General comparison causing numeric promotion from xs:untypedAtomic. .
   */
  @org.junit.Test
  public void kGenCompNE9() {
    final XQuery query = new XQuery(
      "not(xs:untypedAtomic(\"1\") != 1)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = != operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression100() {
    final XQuery query = new XQuery(
      "() != ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = != operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression101() {
    final XQuery query = new XQuery(
      "() != 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = != operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression102() {
    final XQuery query = new XQuery(
      "() != (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = != operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression103() {
    final XQuery query = new XQuery(
      "() != (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = != operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression104() {
    final XQuery query = new XQuery(
      "() != <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = != operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression105() {
    final XQuery query = new XQuery(
      "() != (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = != operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression106() {
    final XQuery query = new XQuery(
      "() != (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = != operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression107() {
    final XQuery query = new XQuery(
      "() != (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = != operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression108() {
    final XQuery query = new XQuery(
      "() != (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = != operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression109() {
    final XQuery query = new XQuery(
      "() != ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = != operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression110() {
    final XQuery query = new XQuery(
      "10000 != ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = != operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression111() {
    final XQuery query = new XQuery(
      "10000 != (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = != operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression112() {
    final XQuery query = new XQuery(
      "10000 != (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = != operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression113() {
    final XQuery query = new XQuery(
      "10000 != <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = != operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression114() {
    final XQuery query = new XQuery(
      "10000 != (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = != operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression115() {
    final XQuery query = new XQuery(
      "10000 != (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = != operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression116() {
    final XQuery query = new XQuery(
      "10000 != (/works/employee[1]/hours[1])",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = != operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression117() {
    final XQuery query = new XQuery(
      "10000 != (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = != operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression118() {
    final XQuery query = new XQuery(
      "10000 != ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = != operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression119() {
    final XQuery query = new XQuery(
      "(50000) != ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = != operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression120() {
    final XQuery query = new XQuery(
      "(50000) != 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = != operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression121() {
    final XQuery query = new XQuery(
      "(50000) != (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = != operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression122() {
    final XQuery query = new XQuery(
      "(50000) != (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = != operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression123() {
    final XQuery query = new XQuery(
      "(50000) != <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = != operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression124() {
    final XQuery query = new XQuery(
      "(50000) != (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = != operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression125() {
    final XQuery query = new XQuery(
      "(50000) != (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = != operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression126() {
    final XQuery query = new XQuery(
      "(50000) != (/works/employee[1]/hours[1])",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = != operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression127() {
    final XQuery query = new XQuery(
      "(50000) != (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = != operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression128() {
    final XQuery query = new XQuery(
      "(50000) != ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = != operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression129() {
    final XQuery query = new XQuery(
      "(10000,50000) != ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = != operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression130() {
    final XQuery query = new XQuery(
      "(10000,50000) != 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = != operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression131() {
    final XQuery query = new XQuery(
      "(10000,50000) != (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = != operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression132() {
    final XQuery query = new XQuery(
      "(10000,50000) != (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = != operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression133() {
    final XQuery query = new XQuery(
      "(10000,50000) != <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = != operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression134() {
    final XQuery query = new XQuery(
      "(10000,50000) != (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = != operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression135() {
    final XQuery query = new XQuery(
      "(10000,50000) != (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = != operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression136() {
    final XQuery query = new XQuery(
      "(10000,50000) != (/works/employee[1]/hours[1])",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = != operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression137() {
    final XQuery query = new XQuery(
      "(10000,50000) != (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = != operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression138() {
    final XQuery query = new XQuery(
      "(10000,50000) != ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = != operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression139() {
    final XQuery query = new XQuery(
      "<a>10000</a> != ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = != operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression140() {
    final XQuery query = new XQuery(
      "<a>10000</a> != 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = != operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression141() {
    final XQuery query = new XQuery(
      "<a>10000</a> != (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = != operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression142() {
    final XQuery query = new XQuery(
      "<a>10000</a> != (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = != operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression143() {
    final XQuery query = new XQuery(
      "<a>10000</a> != <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = != operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression144() {
    final XQuery query = new XQuery(
      "<a>10000</a> != (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = != operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression145() {
    final XQuery query = new XQuery(
      "<a>10000</a> != (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = != operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression146() {
    final XQuery query = new XQuery(
      "<a>10000</a> != (/works/employee[1]/hours[1])",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = != operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression147() {
    final XQuery query = new XQuery(
      "<a>10000</a> != (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = != operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression148() {
    final XQuery query = new XQuery(
      "<a>10000</a> != ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = != operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression149() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) != ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = != operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression150() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) != 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = != operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression151() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) != (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = != operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression152() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) != (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = != operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression153() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) != <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = != operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression154() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) != (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = != operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression155() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) != (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = != operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression156() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) != (/works/employee[1]/hours[1])",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = != operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression157() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) != (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = != operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression158() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) != ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = != operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression159() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) != ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = != operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression160() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) != 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = != operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression161() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) != (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = != operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression162() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) != (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = != operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression163() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) != <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = != operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression164() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) != (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = != operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression165() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) != (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = != operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression166() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) != (/works/employee[1]/hours[1])",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = != operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression167() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) != (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = != operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression168() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) != ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = != operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression169() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) != ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = != operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression170() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) != 10000",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = != operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression171() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) != (50000)",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = != operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression172() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) != (10000,50000)",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = != operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression173() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) != <a>10000</a>",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = != operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression174() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) != (<a>10000</a>)",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = != operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression175() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) != (<a>10000</a>,<b>50000</b>)",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = != operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression176() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) != (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = != operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression177() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) != (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = != operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression178() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1]) != ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = != operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression179() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) != ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = != operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression180() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) != 10000",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = != operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression181() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) != (50000)",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = != operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression182() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) != (10000,50000)",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = != operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression183() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) != <a>10000</a>",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = != operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression184() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) != (<a>10000</a>)",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = != operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression185() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) != (<a>10000</a>,<b>50000</b>)",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = != operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression186() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) != (/works/employee[1]/hours[1])",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = != operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression187() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) != (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = != operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression188() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$works/works/employee[6]/hours[1]) != ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = != operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression189() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) != ()",
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
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = != operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression190() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) != 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = != operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression191() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) != (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = != operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression192() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) != (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = != operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression193() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) != <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = != operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression194() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) != (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = != operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression195() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) != (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = != operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression196() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) != ($works/works/employee[1]/hours[1])",
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
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = != operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression197() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) != ($works/works/employee[1]/hours[1],$works/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = != operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression198() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) != ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
      assertBoolean(false)
    );
  }
}
