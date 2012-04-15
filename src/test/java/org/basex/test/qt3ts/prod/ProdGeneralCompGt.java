package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the GeneralComp.gt production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdGeneralCompGt extends QT3TestSet {

  /**
   *  General comparison where one or more operands is the empty sequence. .
   */
  @org.junit.Test
  public void kGenCompGT1() {
    final XQuery query = new XQuery(
      "not(() > () )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompGT10() {
    final XQuery query = new XQuery(
      "2 > 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompGT11() {
    final XQuery query = new XQuery(
      "1 >= 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  General comparison causing a xs:untypedAtomic value to be cast to xs:boolean, and then compared. .
   */
  @org.junit.Test
  public void kGenCompGT12() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") > false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  General comparison causing a xs:untypedAtomic value to be cast to xs:boolean, and then compared. .
   */
  @org.junit.Test
  public void kGenCompGT13() {
    final XQuery query = new XQuery(
      "true() > xs:untypedAtomic(\"false\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  General comparison causing a xs:untypedAtomic value to be cast to xs:boolean, and then compared. .
   */
  @org.junit.Test
  public void kGenCompGT14() {
    final XQuery query = new XQuery(
      "not(xs:untypedAtomic(\"false\") > true())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  General comparison causing a xs:untypedAtomic value to be cast to xs:boolean, and then compared. .
   */
  @org.junit.Test
  public void kGenCompGT15() {
    final XQuery query = new XQuery(
      "not(false() > xs:untypedAtomic(\"true\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompGT16() {
    final XQuery query = new XQuery(
      "1 > xs:anyURI(\"2\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  General comparison which fails due to invalid operator combination or casting. .
   */
  @org.junit.Test
  public void kGenCompGT17() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"2\") > 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   *  > combined with count(). .
   */
  @org.junit.Test
  public void kGenCompGT18() {
    final XQuery query = new XQuery(
      "count((1, 2, 3, timezone-from-time(current-time()), 4)) > 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  >= combined with count(). .
   */
  @org.junit.Test
  public void kGenCompGT19() {
    final XQuery query = new XQuery(
      "count((1, 2, 3, timezone-from-time(current-time()), 4)) >= 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  General comparison where one or more operands is the empty sequence. .
   */
  @org.junit.Test
  public void kGenCompGT2() {
    final XQuery query = new XQuery(
      "not(1 > () )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  General comparison where one or more operands is the empty sequence. .
   */
  @org.junit.Test
  public void kGenCompGT3() {
    final XQuery query = new XQuery(
      "not(() > 1 )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompGT4() {
    final XQuery query = new XQuery(
      "(1, 2, 3) > 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompGT5() {
    final XQuery query = new XQuery(
      "(1, 2, 3) > 2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompGT6() {
    final XQuery query = new XQuery(
      "(1, 2, 3) > 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompGT7() {
    final XQuery query = new XQuery(
      "4 > (1, 2, 3)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompGT8() {
    final XQuery query = new XQuery(
      "2 > (1, 2, 3)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  General comparison where the operands are various various sequence of xs:integers. .
   */
  @org.junit.Test
  public void kGenCompGT9() {
    final XQuery query = new XQuery(
      "3 > (1, 2, 3)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = > operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression397() {
    final XQuery query = new XQuery(
      "() > ()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = > operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression398() {
    final XQuery query = new XQuery(
      "() > 10000",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = > operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression399() {
    final XQuery query = new XQuery(
      "() > (50000)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = > operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression400() {
    final XQuery query = new XQuery(
      "() > (10000,50000)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = > operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression401() {
    final XQuery query = new XQuery(
      "() > <a>10000</a>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = > operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression402() {
    final XQuery query = new XQuery(
      "() > (<a>10000</a>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = > operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression403() {
    final XQuery query = new XQuery(
      "() > (<a>10000</a>,<b>50000</b>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = > operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression404() {
    final XQuery query = new XQuery(
      "() > (/works/employee[1]/hours[1])",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = > operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression405() {
    final XQuery query = new XQuery(
      "() > (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Empty sequence operator = > operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression406() {
    final XQuery query = new XQuery(
      "() > ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    query.bind("$works", node(file("docs/works.xml")));
    query.bind("$staff", node(file("docs/staff.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = > operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression407() {
    final XQuery query = new XQuery(
      "10000 > ()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = > operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression408() {
    final XQuery query = new XQuery(
      "10000 > (50000)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = > operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression409() {
    final XQuery query = new XQuery(
      "10000 > (10000,50000)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = > operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression410() {
    final XQuery query = new XQuery(
      "10000 > <a>10000</a>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = > operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression411() {
    final XQuery query = new XQuery(
      "10000 > (<a>10000</a>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = > operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression412() {
    final XQuery query = new XQuery(
      "10000 > (<a>10000</a>,<b>50000</b>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = > operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression413() {
    final XQuery query = new XQuery(
      "10000 > (/works/employee[1]/hours[1])",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = > operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression414() {
    final XQuery query = new XQuery(
      "10000 > (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Atomic Value operator = > operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression415() {
    final XQuery query = new XQuery(
      "10000 > ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    query.bind("$works", node(file("docs/works.xml")));
    query.bind("$staff", node(file("docs/staff.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = > operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression416() {
    final XQuery query = new XQuery(
      "(50000) > ()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = > operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression417() {
    final XQuery query = new XQuery(
      "(50000) > 10000",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = > operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression418() {
    final XQuery query = new XQuery(
      "(50000) > (50000)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = > operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression419() {
    final XQuery query = new XQuery(
      "(50000) > (10000,50000)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = > operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression420() {
    final XQuery query = new XQuery(
      "(50000) > <a>10000</a>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = > operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression421() {
    final XQuery query = new XQuery(
      "(50000) > (<a>10000</a>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = > operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression422() {
    final XQuery query = new XQuery(
      "(50000) > (<a>10000</a>,<b>50000</b>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = > operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression423() {
    final XQuery query = new XQuery(
      "(50000) > (/works/employee[1]/hours[1])",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = > operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression424() {
    final XQuery query = new XQuery(
      "(50000) > (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic value operator = > operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression425() {
    final XQuery query = new XQuery(
      "(50000) > ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    query.bind("$works", node(file("docs/works.xml")));
    query.bind("$staff", node(file("docs/staff.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = > operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression426() {
    final XQuery query = new XQuery(
      "(10000,50000) > ()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = > operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression427() {
    final XQuery query = new XQuery(
      "(10000,50000) > 10000",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = > operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression428() {
    final XQuery query = new XQuery(
      "(10000,50000) > (50000)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = > operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression429() {
    final XQuery query = new XQuery(
      "(10000,50000) > (10000,50000)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = > operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression430() {
    final XQuery query = new XQuery(
      "(10000,50000) > <a>10000</a>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = > operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression431() {
    final XQuery query = new XQuery(
      "(10000,50000) > (<a>10000</a>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = > operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression432() {
    final XQuery query = new XQuery(
      "(10000,50000) > (<a>10000</a>,<b>50000</b>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = > operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression433() {
    final XQuery query = new XQuery(
      "(10000,50000) > (/works/employee[1]/hours[1])",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = > operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression434() {
    final XQuery query = new XQuery(
      "(10000,50000) > (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single atomic values operator = > operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression435() {
    final XQuery query = new XQuery(
      "(10000,50000) > ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    query.bind("$works", node(file("docs/works.xml")));
    query.bind("$staff", node(file("docs/staff.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = > operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression436() {
    final XQuery query = new XQuery(
      "<a>10000</a> > ()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = > operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression437() {
    final XQuery query = new XQuery(
      "<a>10000</a> > 10000",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = > operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression438() {
    final XQuery query = new XQuery(
      "<a>10000</a> > (50000)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = > operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression439() {
    final XQuery query = new XQuery(
      "<a>10000</a> > (10000,50000)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = > operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression440() {
    final XQuery query = new XQuery(
      "<a>10000</a> > <a>10000</a>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = > operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression441() {
    final XQuery query = new XQuery(
      "<a>10000</a> > (<a>10000</a>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = > operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression442() {
    final XQuery query = new XQuery(
      "<a>10000</a> > (<a>10000</a>,<b>50000</b>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = > operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression443() {
    final XQuery query = new XQuery(
      "<a>10000</a> > (/works/employee[1]/hours[1])",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = > operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression444() {
    final XQuery query = new XQuery(
      "<a>10000</a> > (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Element Constructor operator = > operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression445() {
    final XQuery query = new XQuery(
      "<a>10000</a> > ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    query.bind("$works", node(file("docs/works.xml")));
    query.bind("$staff", node(file("docs/staff.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = > operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression446() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) > ()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = > operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression447() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) > 10000",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = > operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression448() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) > (50000)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = > operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression449() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) > (10000,50000)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = > operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression450() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) > <a>10000</a>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = > operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression451() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) > (<a>10000</a>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = > operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression452() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) > (<a>10000</a>,<b>50000</b>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = > operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression453() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) > (/works/employee[1]/hours[1])",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = > operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression454() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) > (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element constructor operator = > operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression455() {
    final XQuery query = new XQuery(
      "(<a>10000</a>) > ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    query.bind("$works", node(file("docs/works.xml")));
    query.bind("$staff", node(file("docs/staff.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = > operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression456() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) > ()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = > operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression457() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) > 10000",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = > operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression458() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) > (50000)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = > operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression459() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) > (10000,50000)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = > operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression460() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) > <a>10000</a>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = > operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression461() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) > (<a>10000</a>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = > operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression462() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) > (<a>10000</a>,<b>50000</b>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = > operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression463() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) > (/works/employee[1]/hours[1])",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = > operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression464() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) > (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element constructors operator = > operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression465() {
    final XQuery query = new XQuery(
      "(<a>10000</a>,<b>50000</b>) > ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    query.bind("$works", node(file("docs/works.xml")));
    query.bind("$staff", node(file("docs/staff.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = > operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression466() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) > ()",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = > operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression467() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) > 10000",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = > operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression468() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) > (50000)",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = > operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression469() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) > (10000,50000)",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = > operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression470() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) > <a>10000</a>",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = > operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression471() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) > (<a>10000</a>)",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = > operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression472() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) > (<a>10000</a>,<b>50000</b>)",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = > operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression473() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) > (/works/employee[1]/hours[1])",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = > operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression474() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1]) > (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of single element nodes operator = > operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression475() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1]) > ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    query.bind("$works", node(file("docs/works.xml")));
    query.bind("$staff", node(file("docs/staff.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = > operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression476() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) > ()",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = > operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression477() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) > 10000",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = > operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression478() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) > (50000)",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = > operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression479() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) > (10000,50000)",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = > operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression480() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) > <a>10000</a>",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = > operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression481() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) > (<a>10000</a>)",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = > operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression482() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) > (<a>10000</a>,<b>50000</b>)",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = > operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression483() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) > (/works/employee[1]/hours[1])",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = > operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression484() {
    final XQuery query = new XQuery(
      "(/works/employee[1]/hours[1],/works/employee[6]/hours[1]) > (/works/employee[1]/hours[1],/works/employee[6]/hours[1])",
      ctx);
    query.context(node(file("docs/works.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (single source) operator = > operand2 = Sequence of multiple element nodes (multiple sources) .
   */
  @org.junit.Test
  public void generalexpression485() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$works/works/employee[6]/hours[1]) > ($works/works/employee[1]/hours[1],$staff/staff/employee[6]/grade[1])",
      ctx);
    query.bind("$works", node(file("docs/works.xml")));
    query.bind("$staff", node(file("docs/staff.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = > operand2 = Empty sequence .
   */
  @org.junit.Test
  public void generalexpression486() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) > ()",
      ctx);
    query.bind("$works", node(file("docs/works.xml")));
    query.bind("$staff", node(file("docs/staff.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = > operand2 = Atomic Value .
   */
  @org.junit.Test
  public void generalexpression487() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) > 10000",
      ctx);
    query.bind("$works", node(file("docs/works.xml")));
    query.bind("$staff", node(file("docs/staff.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = > operand2 = Sequence of single atomic value .
   */
  @org.junit.Test
  public void generalexpression488() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) > (50000)",
      ctx);
    query.bind("$works", node(file("docs/works.xml")));
    query.bind("$staff", node(file("docs/staff.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = > operand2 = Sequence of single atomic values .
   */
  @org.junit.Test
  public void generalexpression489() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) > (10000,50000)",
      ctx);
    query.bind("$works", node(file("docs/works.xml")));
    query.bind("$staff", node(file("docs/staff.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = > operand2 = Element Constructor .
   */
  @org.junit.Test
  public void generalexpression490() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) > <a>10000</a>",
      ctx);
    query.bind("$works", node(file("docs/works.xml")));
    query.bind("$staff", node(file("docs/staff.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = > operand2 = Sequence of single element constructor .
   */
  @org.junit.Test
  public void generalexpression491() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) > (<a>10000</a>)",
      ctx);
    query.bind("$works", node(file("docs/works.xml")));
    query.bind("$staff", node(file("docs/staff.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = > operand2 = Sequence of multiple element constructors .
   */
  @org.junit.Test
  public void generalexpression492() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) > (<a>10000</a>,<b>50000</b>)",
      ctx);
    query.bind("$works", node(file("docs/works.xml")));
    query.bind("$staff", node(file("docs/staff.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = > operand2 = Sequence of single element nodes .
   */
  @org.junit.Test
  public void generalexpression493() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) > ($works/works/employee[1]/hours[1])",
      ctx);
    query.bind("$works", node(file("docs/works.xml")));
    query.bind("$staff", node(file("docs/staff.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Test of a General Expression with the operands set as follows operand1 = Sequence of multiple element nodes (multiple sources) operator = > operand2 = Sequence of multiple element nodes (single source) .
   */
  @org.junit.Test
  public void generalexpression494() {
    final XQuery query = new XQuery(
      "($works/works/employee[1]/hours[1],$staff/staff/employee[6]/hours[1]) > ($works/works/employee[1]/hours[1],$works/works/employee[6]/hours[1])",
      ctx);
    query.bind("$works", node(file("docs/works.xml")));
    query.bind("$staff", node(file("docs/staff.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Test - greaterthanonanyuri-1  Evaluation of "gt" operator on xs:anyURI datatype. .
   */
  @org.junit.Test
  public void greaterthanonanyuri1() {
    final XQuery query = new XQuery(
      "(xs:anyURI(\"http://www.example/com\")) > (xs:anyURI(\"http://www.example/com\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }
}
