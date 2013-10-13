package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the GeneralComp.ge production.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdGeneralCompGe extends QT3TestSet {

  /**
   *  General comparison where one or more operands is the empty sequence. .
   */
  @org.junit.Test
  public void kGenCompGTEQ1() {
    final XQuery query = new XQuery(
      "not(() >= () )",
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
   *  <= combined with count(). .
   */
  @org.junit.Test
  public void kGenCompGTEQ10() {
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
   *  General comparison where one or more operands is the empty sequence. .
   */
  @org.junit.Test
  public void kGenCompGTEQ2() {
    final XQuery query = new XQuery(
      "not(1 >= () )",
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
  public void kGenCompGTEQ3() {
    final XQuery query = new XQuery(
      "not(() >= 1 )",
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
  public void kGenCompGTEQ4() {
    final XQuery query = new XQuery(
      "1 =>; 1",
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
   *  An expression involving the '>=' operator that trigger certain optimization paths in some implementations. .
   */
  @org.junit.Test
  public void kGenCompGTEQ5() {
    final XQuery query = new XQuery(
      "count((0, timezone-from-time(current-time()))) >= 1",
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
  public void kGenCompGTEQ6() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"false\") >= false()",
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
  public void kGenCompGTEQ7() {
    final XQuery query = new XQuery(
      "true() >= xs:untypedAtomic(\"false\")",
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
  public void kGenCompGTEQ8() {
    final XQuery query = new XQuery(
      "false() >= xs:untypedAtomic(\"false\")",
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
  public void kGenCompGTEQ9() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"false\") >= false()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = >= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression496() {
    final XQuery query = new XQuery(
      "() >= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = >= operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression497() {
    final XQuery query = new XQuery(
      "() >= 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = >= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression498() {
    final XQuery query = new XQuery(
      "() >= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = >= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression499() {
    final XQuery query = new XQuery(
      "() >= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = >= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression500() {
    final XQuery query = new XQuery(
      "() >= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = >= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression501() {
    final XQuery query = new XQuery(
      "() >= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = >= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression502() {
    final XQuery query = new XQuery(
      "() >= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = >= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression503() {
    final XQuery query = new XQuery(
      "() >= (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = >= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression504() {
    final XQuery query = new XQuery(
      "() >= (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = >= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression505() {
    final XQuery query = new XQuery(
      "() >= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = >= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression506() {
    final XQuery query = new XQuery(
      "10000 >= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = >= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression507() {
    final XQuery query = new XQuery(
      "10000 >= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = >= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression508() {
    final XQuery query = new XQuery(
      "10000 >= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = >= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression509() {
    final XQuery query = new XQuery(
      "10000 >= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = >= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression510() {
    final XQuery query = new XQuery(
      "10000 >= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = >= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression511() {
    final XQuery query = new XQuery(
      "10000 >= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = >= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression512() {
    final XQuery query = new XQuery(
      "10000 >= (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = >= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression513() {
    final XQuery query = new XQuery(
      "10000 >= (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = >= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression514() {
    final XQuery query = new XQuery(
      "10000 >= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = >= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression515() {
    final XQuery query = new XQuery(
      "(50000) >= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = >= operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression516() {
    final XQuery query = new XQuery(
      "(50000) >= 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = >= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression517() {
    final XQuery query = new XQuery(
      "(50000) >= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = >= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression518() {
    final XQuery query = new XQuery(
      "(50000) >= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = >= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression519() {
    final XQuery query = new XQuery(
      "(50000) >= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = >= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression520() {
    final XQuery query = new XQuery(
      "(50000) >= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = >= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression521() {
    final XQuery query = new XQuery(
      "(50000) >= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = >= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression522() {
    final XQuery query = new XQuery(
      "(50000) >= (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = >= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression523() {
    final XQuery query = new XQuery(
      "(50000) >= (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = >= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression524() {
    final XQuery query = new XQuery(
      "(50000) >= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = >= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression525() {
    final XQuery query = new XQuery(
      "(10000,50000) >= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = >= operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression526() {
    final XQuery query = new XQuery(
      "(10000,50000) >= 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = >= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression527() {
    final XQuery query = new XQuery(
      "(10000,50000) >= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = >= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression528() {
    final XQuery query = new XQuery(
      "(10000,50000) >= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = >= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression529() {
    final XQuery query = new XQuery(
      "(10000,50000) >= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = >= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression530() {
    final XQuery query = new XQuery(
      "(10000,50000) >= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = >= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression531() {
    final XQuery query = new XQuery(
      "(10000,50000) >= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = >= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression532() {
    final XQuery query = new XQuery(
      "(10000,50000) >= (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = >= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression533() {
    final XQuery query = new XQuery(
      "(10000,50000) >= (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = >= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression534() {
    final XQuery query = new XQuery(
      "(10000,50000) >= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = >= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression535() {
    final XQuery query = new XQuery(
      "<a>10000</a> >= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = >= operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression536() {
    final XQuery query = new XQuery(
      "<a>10000</a> >= 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = >= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression537() {
    final XQuery query = new XQuery(
      "<a>10000</a> >= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = >= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression538() {
    final XQuery query = new XQuery(
      "<a>10000</a> >= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = >= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression539() {
    final XQuery query = new XQuery(
      "<a>10000</a> >= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = >= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression540() {
    final XQuery query = new XQuery(
      "<a>10000</a> >= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = >= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression541() {
    final XQuery query = new XQuery(
      "<a>10000</a> >= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = >= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression542() {
    final XQuery query = new XQuery(
      "<a>10000</a> >= (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = >= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression543() {
    final XQuery query = new XQuery(
      "<a>10000</a> >= (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = >= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression544() {
    final XQuery query = new XQuery(
      "<a>10000</a> >= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = >= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression545() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) >= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = >= operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression546() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) >= 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = >= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression547() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) >= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = >= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression548() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) >= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = >= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression549() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) >= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = >= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression550() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) >= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = >= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression551() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) >= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = >= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression552() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) >= (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = >= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression553() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) >= (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = >= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression554() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) >= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = >= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression555() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) >= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = >= operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression556() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) >= 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = >= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression557() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) >= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = >= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression558() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) >= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = >= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression559() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) >= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = >= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression560() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) >= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = >= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression561() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) >= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = >= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression562() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) >= (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = >= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression563() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) >= (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = >= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression564() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) >= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = >= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression565() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) >= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = >= operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression566() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) >= 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = >= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression567() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) >= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = >= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression568() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) >= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = >= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression569() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) >= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = >= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression570() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) >= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = >= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression571() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) >= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = >= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression572() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) >= (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = >= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression573() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) >= (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = >= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression574() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1]) >= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = >= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression575() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) >= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = >= operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression576() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) >= 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = >= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression577() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) >= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = >= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression578() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) >= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = >= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression579() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) >= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = >= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression580() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) >= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = >= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression581() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) >= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = >= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression582() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) >= (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = >= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression583() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) >= (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = >= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression584() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$works/works/employee[6]/hours[1]) >= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = >= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression585() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) >= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = >= operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression586() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) >= 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = >= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression587() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) >= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = >= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression588() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) >= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = >= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression589() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) >= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = >= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression590() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) >= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = >= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression591() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) >= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = >= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression592() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) >= ($works/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = >= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression593() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) >= ($works/works/employee[1]/hours[1],$works/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = >= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression594() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) >= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   * Test - greaterthaneqonanyuri-1  Evaluation of "ge" operator on xs:anyURI datatype. .
   */
  @org.junit.Test
  public void greaterthaneqonanyuri1() {
    final XQuery query = new XQuery(
      "(xs:anyURI(\"http://www.example/com\")) >= (xs:anyURI(\"http://www.example/com\"))",
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
}
