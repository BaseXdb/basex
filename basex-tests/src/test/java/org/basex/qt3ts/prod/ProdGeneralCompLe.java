package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the GeneralComp.le production.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdGeneralCompLe extends QT3TestSet {

  /**
   *  General comparison where one or more operands is the empty sequence. .
   */
  @org.junit.Test
  public void kGenCompLTEQ1() {
    final XQuery query = new XQuery(
      "not(() <= () )",
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
  public void kGenCompLTEQ2() {
    final XQuery query = new XQuery(
      "not(1 <= () )",
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
  public void kGenCompLTEQ3() {
    final XQuery query = new XQuery(
      "not(() <= 1 )",
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
  public void kGenCompLTEQ4() {
    final XQuery query = new XQuery(
      "1 <= 1",
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
  public void kGenCompLTEQ5() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"false\") <= false()",
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
  public void kGenCompLTEQ6() {
    final XQuery query = new XQuery(
      "false() <= xs:untypedAtomic(\"false\")",
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
  public void kGenCompLTEQ7() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"false\") <= false()",
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
  public void kGenCompLTEQ8() {
    final XQuery query = new XQuery(
      "true() <= xs:untypedAtomic(\"true\")",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = <= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression298() {
    final XQuery query = new XQuery(
      "() <= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = <= operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression299() {
    final XQuery query = new XQuery(
      "() <= 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = <= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression300() {
    final XQuery query = new XQuery(
      "() <= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = <= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression301() {
    final XQuery query = new XQuery(
      "() <= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = <= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression302() {
    final XQuery query = new XQuery(
      "() <= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = <= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression303() {
    final XQuery query = new XQuery(
      "() <= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = <= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression304() {
    final XQuery query = new XQuery(
      "() <= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = <= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression305() {
    final XQuery query = new XQuery(
      "() <= (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = <= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression306() {
    final XQuery query = new XQuery(
      "() <= (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = <= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression307() {
    final XQuery query = new XQuery(
      "() <= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = <= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression308() {
    final XQuery query = new XQuery(
      "10000 <= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = <= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression309() {
    final XQuery query = new XQuery(
      "10000 <= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = <= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression310() {
    final XQuery query = new XQuery(
      "10000 <= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = <= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression311() {
    final XQuery query = new XQuery(
      "10000 <= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = <= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression312() {
    final XQuery query = new XQuery(
      "10000 <= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = <= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression313() {
    final XQuery query = new XQuery(
      "10000 <= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = <= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression314() {
    final XQuery query = new XQuery(
      "10000 <= (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = <= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression315() {
    final XQuery query = new XQuery(
      "10000 <= (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = <= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression316() {
    final XQuery query = new XQuery(
      "10000 <= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = <= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression317() {
    final XQuery query = new XQuery(
      "(50000) <= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = <= operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression318() {
    final XQuery query = new XQuery(
      "(50000) <= 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = <= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression319() {
    final XQuery query = new XQuery(
      "(50000) <= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = <= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression320() {
    final XQuery query = new XQuery(
      "(50000) <= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = <= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression321() {
    final XQuery query = new XQuery(
      "(50000) <= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = <= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression322() {
    final XQuery query = new XQuery(
      "(50000) <= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = <= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression323() {
    final XQuery query = new XQuery(
      "(50000) <= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = <= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression324() {
    final XQuery query = new XQuery(
      "(50000) <= (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = <= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression325() {
    final XQuery query = new XQuery(
      "(50000) <= (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = <= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression326() {
    final XQuery query = new XQuery(
      "(50000) <= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = <= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression327() {
    final XQuery query = new XQuery(
      "(10000,50000) <= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = <= operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression328() {
    final XQuery query = new XQuery(
      "(10000,50000) <= 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = <= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression329() {
    final XQuery query = new XQuery(
      "(10000,50000) <= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = <= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression330() {
    final XQuery query = new XQuery(
      "(10000,50000) <= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = <= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression331() {
    final XQuery query = new XQuery(
      "(10000,50000) <= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = <= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression332() {
    final XQuery query = new XQuery(
      "(10000,50000) <= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = <= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression333() {
    final XQuery query = new XQuery(
      "(10000,50000) <= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = <= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression334() {
    final XQuery query = new XQuery(
      "(10000,50000) <= (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = <= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression335() {
    final XQuery query = new XQuery(
      "(10000,50000) <= (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = <= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression336() {
    final XQuery query = new XQuery(
      "(10000,50000) <= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = <= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression337() {
    final XQuery query = new XQuery(
      "<a>10000</a> <= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = <= operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression338() {
    final XQuery query = new XQuery(
      "<a>10000</a> <= 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = <= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression339() {
    final XQuery query = new XQuery(
      "<a>10000</a> <= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = <= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression340() {
    final XQuery query = new XQuery(
      "<a>10000</a> <= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = <= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression341() {
    final XQuery query = new XQuery(
      "<a>10000</a> <= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = <= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression342() {
    final XQuery query = new XQuery(
      "<a>10000</a> <= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = <= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression343() {
    final XQuery query = new XQuery(
      "<a>10000</a> <= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = <= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression344() {
    final XQuery query = new XQuery(
      "<a>10000</a> <= (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = <= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression345() {
    final XQuery query = new XQuery(
      "<a>10000</a> <= (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = <= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression346() {
    final XQuery query = new XQuery(
      "<a>10000</a> <= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = <= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression347() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) <= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = <= operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression348() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) <= 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = <= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression349() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) <= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = <= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression350() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) <= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = <= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression351() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) <= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = <= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression352() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) <= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = <= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression353() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) <= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = <= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression354() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) <= (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = <= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression355() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) <= (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = <= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression356() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) <= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = <= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression357() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) <= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = <= operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression358() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) <= 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = <= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression359() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) <= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = <= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression360() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) <= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = <= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression361() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) <= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = <= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression362() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) <= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = <= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression363() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) <= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = <= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression364() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) <= (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = <= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression365() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) <= (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = <= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression366() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) <= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = <= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression367() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) <= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = <= operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression368() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) <= 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = <= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression369() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) <= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = <= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression370() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) <= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = <= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression371() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) <= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = <= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression372() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) <= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = <= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression373() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) <= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = <= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression374() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) <= (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = <= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression375() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) <= (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = <= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression376() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1]) <= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = <= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression377() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) <= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = <= operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression378() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) <= 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = <= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression379() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) <= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = <= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression380() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) <= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = <= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression381() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) <= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = <= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression382() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) <= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = <= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression383() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) <= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = <= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression384() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) <= (/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = <= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression385() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) <= (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = <= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression386() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$works/works/employee[6]/hours[1]) <= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = <= operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression387() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) <= ()",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = <= operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression388() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) <= 10000",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = <= operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression389() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) <= (50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = <= operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression390() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) <= (10000,50000)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = <= operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression391() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) <= <a>10000</a>",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = <= operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression392() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) <= (<a>10000</a>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = <= operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression393() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) <= (<a>10000</a>,<b>50000</b>)",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = <= operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression394() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) <= ($works/works/employee[1]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = <= operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression395() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) <= ($works/works/employee[1]/hours[1],$works/works/employee[6]/hours[1])",
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
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = <= operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression396() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) <= ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
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
   * Test - lessthaneqonanyuri-1  Evaluation of "le" operator on xs:anyURI datatype. .
   */
  @org.junit.Test
  public void lessthaneqonanyuri1() {
    final XQuery query = new XQuery(
      "(xs:anyURI(\"http://www.example/com\")) <= (xs:anyURI(\"http://www.example/com\"))",
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
