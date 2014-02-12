package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Abbreviated axes: tests for the AxisStep production.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdAxisStepAbbr extends QT3TestSet {

  /**
   *  Focus is undefined inside user functions; '..' axis..
   */
  @org.junit.Test
  public void k2AbbrAxes1() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() { .. }; local:myFunc()",
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
   *  Evaluates "hours". Selects the "hours" element children of the context node..
   */
  @org.junit.Test
  public void abbreviatedSyntax1() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[4]) return $h/hours/string()",
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
      assertStringValue(false, "20 40")
    );
  }

  /**
   *  Evaluates "//hours". Selects all the hours descendants of the root document node and thus selects all hours elements in the same document as the context node..
   */
  @org.junit.Test
  public void abbreviatedSyntax10() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h//hours/string()",
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
      assertStringValue(false, "40 70 20 80 20 40 20 30 12 40 80 20 20 20 40 80")
    );
  }

  /**
   *  Evaluates "//overtime/day". Selects all the day elements in the same document as the context node that have an overtime parent..
   */
  @org.junit.Test
  public void abbreviatedSyntax12() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h//overtime/day/string()",
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
      assertStringValue(false, "Monday Tuesday")
    );
  }

  /**
   *  Evaluates ".//day". Selects the day element descendants of the context node..
   */
  @org.junit.Test
  public void abbreviatedSyntax13() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/.//day/string()",
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
      assertStringValue(false, "Monday Tuesday")
    );
  }

  /**
   *  Evaluates "..". Selects the parent of the context node..
   */
  @org.junit.Test
  public void abbreviatedSyntax14() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[12]/overtime) return $h/../@name",
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
      assertStringValue(false, "John Doe 12")
    );
  }

  /**
   *  Evaluates "employee[@name="Jane Doe 11"]". Selects all employee children of the context node that have a name attribute with a value "Jane Doe 11"..
   */
  @org.junit.Test
  public void abbreviatedSyntax16() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[@name=\"Jane Doe 11\"]/@name",
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
      assertStringValue(false, "Jane Doe 11")
    );
  }

  /**
   *  Evaluates "employee[@gender="female"][5]". Selects the fifth element child of the context node that has a gender attribute with value "female"..
   */
  @org.junit.Test
  public void abbreviatedSyntax17() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[@gender=\"female\"][5]/@name",
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
      assertStringValue(false, "Jane Doe 9")
    );
  }

  /**
   *  Evaluates "employee[5][@gender="female"]". Selects the fifth employee child of the context node if that child has a gender attribute with value "female"..
   */
  @org.junit.Test
  public void abbreviatedSyntax18() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[5][@gender=\"female\"]/@name",
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
      assertStringValue(false, "Jane Doe 5")
    );
  }

  /**
   *  Evaluates "employee[status="active"]". Selects the employee children of the context node that have one or more status children whose typed value is equal to the string "active"..
   */
  @org.junit.Test
  public void abbreviatedSyntax19() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[status=\"active\"]/@name",
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
      assertStringValue(false, "Jane Doe 13")
    );
  }

  /**
   *  Evaluates "text()". Selects all text node children of the context node..
   */
  @org.junit.Test
  public void abbreviatedSyntax2() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[2]) return $h/text()",
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
      assertStringValue(true, "Text data from Employee[2]")
    );
  }

  /**
   *  Evaluates "employee[overtime]". Selects the employee children of the context node that have one or more overtime children..
   */
  @org.junit.Test
  public void abbreviatedSyntax20() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[overtime]/@name",
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
      assertStringValue(false, "John Doe 12")
    );
  }

  /**
   *  Evaluates "employee[@name and @type]". Selects all the employee children of the context node that have both a name attribute and a type attribute..
   */
  @org.junit.Test
  public void abbreviatedSyntax21() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[@name and @type]/@name",
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
      assertStringValue(false, "Jane Doe 13")
    );
  }

  /**
   *  Evaluates "employee/(status|overtime)/day". Selects every day element that has a parent that is either a status or an overime element, that in turn is a child of an employee element that is a child of the context node..
   */
  @org.junit.Test
  public void abbreviatedSyntax22() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee/(status|overtime)/day/string()",
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
      assertStringValue(false, "Monday Tuesday")
    );
  }

  /**
   *  Evaluates "employee/(status|overtime)/day". 
   *       Selects every day element that has a parent that is either a status or an overime element, 
   *       that in turn is a child of an employee element that is a child of the context node. 
   *       Uses "union" Operator.
   */
  @org.junit.Test
  public void abbreviatedSyntax24() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee/(status union overtime)/day/string()",
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
      assertStringValue(false, "Monday Tuesday")
    );
  }

  /**
   *  Evaluates "employee[@name = condition @type=condition]". 
   *       Selects all the employee children of the context node that have both a name attribute and a type attribute. 
   *       Uses "or" operator..
   */
  @org.junit.Test
  public void abbreviatedSyntax25() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[@name = \"Jane Doe 13\" or @type=\"FT\"]/@name",
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
      assertStringValue(false, "Jane Doe 13")
    );
  }

  /**
   *  Use an expression that returns atomic values after "//".
   */
  @org.junit.Test
  public void abbreviatedSyntax26() {
    final XQuery query = new XQuery(
      "let $in := <a><b>ABC</b><b>XYZ</b></a> return $in//string-to-codepoints(.)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "65 66 67 88 89 90 65 66 67 65 66 67 88 89 90 88 89 90")
    );
  }

  /**
   *  Evaluates "@name". Selects the name attribute of the context node..
   */
  @org.junit.Test
  public void abbreviatedSyntax3() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[10]) return $h/@name",
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
      assertStringValue(false, "John Doe 10")
    );
  }

  /**
   *  Evaluates "employee[1]". Selects the first employee child of the context node..
   */
  @org.junit.Test
  public void abbreviatedSyntax5() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[1]/@name",
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
      assertStringValue(false, "Jane Doe 1")
    );
  }

  /**
   *  Evaluates "para[fn:last()]". Selects the last employee child of the context node..
   */
  @org.junit.Test
  public void abbreviatedSyntax6() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee[fn:last()]/@name",
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
      assertStringValue(false, "Jane Doe 13")
    );
  }

  /**
   *  Evaluates "* /hours". Selects all hours grandchildren of the context node..
   */
  @org.junit.Test
  public void abbreviatedSyntax7() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/*/hours/string()",
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
      assertStringValue(false, "40 70 20 80 20 40 20 30 12 40 80 20 20 20 40 80")
    );
  }

  /**
   *  Evaluates "/works/employee[5]/hours[2]" selects the second hours of the fifth employee of the book whose parent is the document node that contains the context node..
   */
  @org.junit.Test
  public void abbreviatedSyntax8() {
    final XQuery query = new XQuery(
      "/works/employee[5]/hours[2]",
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
      assertStringValue(false, "30")
    );
  }

  /**
   *  Evaluates "employee//hours". Selects the hours element descendants of the employee element children of the context node..
   */
  @org.junit.Test
  public void abbreviatedSyntax9() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/employee//hours/string()",
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
      assertStringValue(false, "40 70 20 80 20 40 20 30 12 40 80 20 20 20 40 80")
    );
  }
}
