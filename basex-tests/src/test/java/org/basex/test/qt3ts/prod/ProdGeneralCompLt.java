package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the GeneralComp.lt production.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdGeneralCompLt extends QT3TestSet {

  /**
   *  General comparison where one or more operands is the empty sequence. .
   */
  @org.junit.Test
  public void kGenCompLT1() {
    final XQuery query = new XQuery(
      "not(() < () )",
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
  public void kGenCompLT10() {
    final XQuery query = new XQuery(
      "0 < (1, 2, 3)",
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
  public void kGenCompLT11() {
    final XQuery query = new XQuery(
      "1 < 2",
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
  public void kGenCompLT12() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"false\") < true()",
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
  public void kGenCompLT13() {
    final XQuery query = new XQuery(
      "false() < xs:untypedAtomic(\"true\")",
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
  public void kGenCompLT14() {
    final XQuery query = new XQuery(
      "not(xs:untypedAtomic(\"true\") < false())",
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
  public void kGenCompLT15() {
    final XQuery query = new XQuery(
      "not(true() < xs:untypedAtomic(\"false\"))",
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
  public void kGenCompLT16() {
    final XQuery query = new XQuery(
      "1 < xs:anyURI(\"0\")",
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
  public void kGenCompLT17() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"0\") < 1",
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
   *  <= combined with count(). .
   */
  @org.junit.Test
  public void kGenCompLT18() {
    final XQuery query = new XQuery(
      "1 <= count((1, 2, 3, timezone-from-time(current-time()), 4))",
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
   *  < combined with count(). .
   */
  @org.junit.Test
  public void kGenCompLT19() {
    final XQuery query = new XQuery(
      "0 < count((1, 2, 3, timezone-from-time(current-time()), 4))",
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
  public void kGenCompLT2() {
    final XQuery query = new XQuery(
      "not(1 < () )",
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
  public void kGenCompLT3() {
    final XQuery query = new XQuery(
      "not(() < 1 )",
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
  public void kGenCompLT4() {
    final XQuery query = new XQuery(
      "1 =< 1",
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
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompLT5() {
    final XQuery query = new XQuery(
      "(1, 2, 3) < 4",
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
  public void kGenCompLT6() {
    final XQuery query = new XQuery(
      "(1, 2, 3) < 2",
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
  public void kGenCompLT7() {
    final XQuery query = new XQuery(
      "(1, 2, 3) < 3",
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
  public void kGenCompLT8() {
    final XQuery query = new XQuery(
      "1 < (1, 2, 3)",
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
  public void kGenCompLT9() {
    final XQuery query = new XQuery(
      "2 < (1, 2, 3)",
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
   *  Do a comparison that requires conversion via element()->xs:untypedAtomic->xs:double(LHS), triggered by xs:integer. .
   */
  @org.junit.Test
  public void k2GenCompLT1() {
    final XQuery query = new XQuery(
      "<e>1.1</e> < 3",
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
   *  Do a comparison that requires conversion via element()->xs:untypedAtomic->xs:double(RHS), triggered by xs:integer. .
   */
  @org.junit.Test
  public void k2GenCompLT2() {
    final XQuery query = new XQuery(
      "3. < <e>1.1</e>",
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
   *  Do a comparison that requires conversion via element()->xs:untypedAtomic->xs:double(LHS), triggered by xs:integer. .
   */
  @org.junit.Test
  public void k2GenCompLT3() {
    final XQuery query = new XQuery(
      "<e>1.1</e> < 3.",
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
   *  Do a comparison that requires conversion via element()->xs:untypedAtomic->xs:double(RHS), triggered by xs:integer. .
   */
  @org.junit.Test
  public void k2GenCompLT4() {
    final XQuery query = new XQuery(
      "3 < <e>1.1</e>",
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
   *  Do a comparison that requires conversion via element()->xs:untypedAtomic->xs:double(LHS), triggered by xs:double. .
   */
  @org.junit.Test
  public void k2GenCompLT5() {
    final XQuery query = new XQuery(
      "<e>1.1</e> < 3e3",
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
   *  Do a comparison that requires conversion via element()->xs:untypedAtomic->xs:double(RHS), triggered by xs:double. .
   */
  @org.junit.Test
  public void k2GenCompLT6() {
    final XQuery query = new XQuery(
      "3e3 < <e>1.1</e>",
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
   *  Do a comparison that requires conversion via element()->xs:untypedAtomic->xs:double(LHS), triggered by xs:double. .
   */
  @org.junit.Test
  public void k2GenCompLT7() {
    final XQuery query = new XQuery(
      "<e>1.1</e> < xs:float(3e3)",
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
   *  Do a comparison that requires conversion via element()->xs:untypedAtomic->xs:double(RHS), triggered by xs:double. .
   */
  @org.junit.Test
  public void k2GenCompLT8() {
    final XQuery query = new XQuery(
      "xs:float(3e3) < <e>1.1</e>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = < operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression199() {
    final XQuery query = new XQuery(
      "() < ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = < operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression200() {
    final XQuery query = new XQuery(
      "() < 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = < operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression201() {
    final XQuery query = new XQuery(
      "() < (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = < operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression202() {
    final XQuery query = new XQuery(
      "() < (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = < operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression203() {
    final XQuery query = new XQuery(
      "() < <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = < operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression204() {
    final XQuery query = new XQuery(
      "() < (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = < operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression205() {
    final XQuery query = new XQuery(
      "() < (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = < operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression206() {
    final XQuery query = new XQuery(
      "() < (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = < operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression207() {
    final XQuery query = new XQuery(
      "() < (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = < operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression208() {
    final XQuery query = new XQuery(
      "\n" +
      "         () < ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = < operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression209() {
    final XQuery query = new XQuery(
      "10000 < ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = < operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression210() {
    final XQuery query = new XQuery(
      "10000 < (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = < operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression211() {
    final XQuery query = new XQuery(
      "10000 < (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = < operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression212() {
    final XQuery query = new XQuery(
      "10000 < <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = < operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression213() {
    final XQuery query = new XQuery(
      "10000 < (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = < operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression214() {
    final XQuery query = new XQuery(
      "10000 < (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = < operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression215() {
    final XQuery query = new XQuery(
      "10000 < (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = < operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression216() {
    final XQuery query = new XQuery(
      "10000 < (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = < operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression217() {
    final XQuery query = new XQuery(
      "\n" +
      "         10000 < ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = < operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression218() {
    final XQuery query = new XQuery(
      "(50000) < ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = < operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression219() {
    final XQuery query = new XQuery(
      "(50000) < 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = < operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression220() {
    final XQuery query = new XQuery(
      "(50000) < (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = < operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression221() {
    final XQuery query = new XQuery(
      "(50000) < (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = < operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression222() {
    final XQuery query = new XQuery(
      "(50000) < <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = < operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression223() {
    final XQuery query = new XQuery(
      "(50000) < (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = < operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression224() {
    final XQuery query = new XQuery(
      "(50000) < (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = < operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression225() {
    final XQuery query = new XQuery(
      "(50000) < (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = < operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression226() {
    final XQuery query = new XQuery(
      "(50000) < (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = < operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression227() {
    final XQuery query = new XQuery(
      "\n" +
      "         (50000) < ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = < operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression228() {
    final XQuery query = new XQuery(
      "(10000,50000) < ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = < operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression229() {
    final XQuery query = new XQuery(
      "(10000,50000) < 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = < operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression230() {
    final XQuery query = new XQuery(
      "(10000,50000) < (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = < operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression231() {
    final XQuery query = new XQuery(
      "(10000,50000) < (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = < operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression232() {
    final XQuery query = new XQuery(
      "(10000,50000) < <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = < operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression233() {
    final XQuery query = new XQuery(
      "(10000,50000) < (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = < operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression234() {
    final XQuery query = new XQuery(
      "(10000,50000) < (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = < operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression235() {
    final XQuery query = new XQuery(
      "(10000,50000) < (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = < operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression236() {
    final XQuery query = new XQuery(
      "(10000,50000) < (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = < operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression237() {
    final XQuery query = new XQuery(
      "\n" +
      "         (10000,50000) < ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = < operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression238() {
    final XQuery query = new XQuery(
      "<a>10000</a> < ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = < operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression239() {
    final XQuery query = new XQuery(
      "<a>10000</a> < 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = < operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression240() {
    final XQuery query = new XQuery(
      "<a>10000</a> < (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = < operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression241() {
    final XQuery query = new XQuery(
      "<a>10000</a> < (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = < operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression242() {
    final XQuery query = new XQuery(
      "<a>10000</a> < <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = < operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression243() {
    final XQuery query = new XQuery(
      "<a>10000</a> < (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = < operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression244() {
    final XQuery query = new XQuery(
      "<a>10000</a> < (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = < operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression245() {
    final XQuery query = new XQuery(
      "<a>10000</a> < (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = < operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression246() {
    final XQuery query = new XQuery(
      "<a>10000</a> < (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = < operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression247() {
    final XQuery query = new XQuery(
      "\n" +
      "         <a>10000</a> < ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = < operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression248() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) < ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = < operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression249() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) < 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = < operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression250() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) < (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = < operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression251() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) < (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = < operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression252() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) < <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = < operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression253() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) < (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = < operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression254() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) < (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = < operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression255() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) < (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = < operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression256() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) < (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = < operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression257() {
    final XQuery query = new XQuery(
      "\n" +
      "         (<a>10000</a>) < ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = < operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression258() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) < ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = < operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression259() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) < 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = < operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression260() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) < (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = < operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression261() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) < (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = < operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression262() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) < <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = < operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression263() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) < (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = < operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression264() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) < (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = < operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression265() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) < (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = < operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression266() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) < (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = < operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression267() {
    final XQuery query = new XQuery(
      "(\n" +
      "         <a>10000</a>,<b>50000</b>) < ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = < operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression268() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) < ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = < operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression269() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) < 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = < operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression270() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) < (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = < operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression271() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) < (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = < operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression272() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) < <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = < operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression273() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) < (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = < operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression274() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) < (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = < operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression275() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) < (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = < operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression276() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) < (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = < operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression277() {
    final XQuery query = new XQuery(
      "\n" +
      "          ($works/works/employee[1]/hours[1]) < ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = < operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression278() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) < ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = < operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression279() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) < 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = < operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression280() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) < (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = < operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression281() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) < (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = < operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression282() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) < <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = < operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression283() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) < (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = < operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression284() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) < (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = < operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression285() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) < (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = < operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression286() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) < (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = < operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression287() {
    final XQuery query = new XQuery(
      "\n" +
      "         ($works/works/employee[1]/hours[1],$works/works/employee[6]/hours[1]) < ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = < operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression288() {
    final XQuery query = new XQuery(
      "\n" +
      "         ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) < ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = < operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression289() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) < 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = < operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression290() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) < (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = < operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression291() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) < (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = < operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression292() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) < <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = < operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression293() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) < (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = < operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression294() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) < (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = < operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression295() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) < ($works/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = < operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression296() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) < ($works/works/employee[1]/hours[1],$works/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = < operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression297() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) < ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   * Test - lessthanonanyuri-1  Evaluation of "lt" operator on xs:anyURI datatype. .
   */
  @org.junit.Test
  public void lessthanonanyuri1() {
    final XQuery query = new XQuery(
      "(xs:anyURI(\"http://www.example/com\")) < (xs:anyURI(\"http://www.example/com\"))",
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
