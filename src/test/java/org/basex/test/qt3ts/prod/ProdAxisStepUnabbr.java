package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the AxisStep production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdAxisStepUnabbr extends QT3TestSet {

  /**
   *  Evaluate the child axis of the context node (child::empnum) .
   */
  @org.junit.Test
  public void unabbreviatedSyntax1() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee) return $h/child::empnum",
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
      assertSerialization("<empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E2</empnum><empnum>E2</empnum><empnum>E3</empnum><empnum>E3</empnum><empnum>E4</empnum><empnum>E4</empnum><empnum>E4</empnum>", false)
    );
  }

  /**
   *  Evaluate selecting an descendant or self (descendant-or-self::employee)- Select the "employee" descendants of the context node and if the context is "employee" select it as well. .
   */
  @org.junit.Test
  public void unabbreviatedSyntax12() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[1]) return $h/descendant-or-self::employee",
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
      assertSerialization("<employee name=\"Jane Doe 1\" gender=\"female\">\n   <empnum>E1</empnum>\n   <pnum>P1</pnum>\n   <hours>40</hours>\n  </employee>", false)
    );
  }

  /**
   *  Evaluate self (self::employee)- Select the context node, if it is an "employee", otherwise return empty sequence This test selects an "employee" element .
   */
  @org.junit.Test
  public void unabbreviatedSyntax13() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[1]) return $h/self::employee",
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
      assertSerialization("<employee name=\"Jane Doe 1\" gender=\"female\">\n   <empnum>E1</empnum>\n   <pnum>P1</pnum>\n   <hours>40</hours>\n  </employee>", false)
    );
  }

  /**
   *  Evaluate self (self::hours)- Select the context node, if it is an "employee", otherwise return empty sequence This test selects an empty sequence. .
   */
  @org.junit.Test
  public void unabbreviatedSyntax14() {
    final XQuery query = new XQuery(
      "for $h in (/works[1]/employee[1]) return fn:count(($h/self::employee[1000]))",
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
   *  Evaluate more on child/descendant (child::employee/descendant:empnum)- selects the empnum element descendants of the employee element children of the context node .
   */
  @org.junit.Test
  public void unabbreviatedSyntax15() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/child::employee/descendant::empnum",
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
      assertSerialization("<empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E2</empnum><empnum>E2</empnum><empnum>E3</empnum><empnum>E3</empnum><empnum>E4</empnum><empnum>E4</empnum><empnum>E4</empnum>", false)
    );
  }

  /**
   *  Evaluate child::* /child::pnum - Selects the "pnum" grandchildren of the context node .
   */
  @org.junit.Test
  public void unabbreviatedSyntax16() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/child::*/child::pnum",
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
      assertSerialization("<pnum>P1</pnum><pnum>P2</pnum><pnum>P3</pnum><pnum>P4</pnum><pnum>P5</pnum><pnum>P6</pnum><pnum>P1</pnum><pnum>P2</pnum><pnum>P2</pnum><pnum>P2</pnum><pnum>P2</pnum><pnum>P4</pnum><pnum>P5</pnum>", false)
    );
  }

  /**
   *  Evaluate /descendant::pnum selects all the pnum elements in the same document as the context node. .
   */
  @org.junit.Test
  public void unabbreviatedSyntax18() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/descendant::pnum",
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
      assertSerialization("<pnum>P1</pnum><pnum>P2</pnum><pnum>P3</pnum><pnum>P4</pnum><pnum>P5</pnum><pnum>P6</pnum><pnum>P1</pnum><pnum>P2</pnum><pnum>P2</pnum><pnum>P2</pnum><pnum>P2</pnum><pnum>P4</pnum><pnum>P5</pnum>", false)
    );
  }

  /**
   *  Evaluate "/descendant::employee/child::pnum" - Selects all the pnum elements that have an "employee" parent and that are in the same document as the context node. .
   */
  @org.junit.Test
  public void unabbreviatedSyntax19() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/descendant::employee/child::pnum",
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
      assertSerialization("<pnum>P1</pnum><pnum>P2</pnum><pnum>P3</pnum><pnum>P4</pnum><pnum>P5</pnum><pnum>P6</pnum><pnum>P1</pnum><pnum>P2</pnum><pnum>P2</pnum><pnum>P2</pnum><pnum>P2</pnum><pnum>P4</pnum><pnum>P5</pnum>", false)
    );
  }

  /**
   *  Evaluate selecting all element children of the context node .
   */
  @org.junit.Test
  public void unabbreviatedSyntax2() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[1]) return $h/child::*",
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
      assertSerialization("<empnum>E1</empnum><pnum>P1</pnum><hours>40</hours>", false)
    );
  }

  /**
   *  Evaluate "child::employee[fn:position() = 1]". Selects the first employee child of the context node. .
   */
  @org.junit.Test
  public void unabbreviatedSyntax20() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/child::employee[fn:position() = 1]",
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
      assertSerialization("<employee name=\"Jane Doe 1\" gender=\"female\">\n   <empnum>E1</empnum>\n   <pnum>P1</pnum>\n   <hours>40</hours>\n  </employee>", false)
    );
  }

  /**
   *  Evaluate "child::employee[fn:position() = fn:last()]" selects the previous to the one "employee" child of the context node. .
   */
  @org.junit.Test
  public void unabbreviatedSyntax21() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/child::employee[fn:position() = fn:last()]",
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
      assertSerialization("<employee name=\"Jane Doe 13\" gender=\"female\" type=\"FT\">\n   <empnum>E4</empnum>\n   <pnum>P5</pnum>\n   <hours>80</hours>\n   <status>active</status>\n  </employee>", false)
    );
  }

  /**
   *  Evaluate "child::employee[fn:position() = fn:last()-1]" Selects the last but one "employee" child of the context node. .
   */
  @org.junit.Test
  public void unabbreviatedSyntax22() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/child::employee[fn:position() = fn:last()-1]",
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
      assertSerialization("<employee name=\"John Doe 12\" gender=\"male\">\n   <empnum>E4</empnum>\n   <pnum>P4</pnum>\n   <hours>40</hours>\n   <overtime>\n     <day>Monday</day>\n     <day>Tuesday</day>\n   </overtime>\n  </employee>", false)
    );
  }

  /**
   *  Evaluate "child::hours[fn:position() > 1]". Selects all the hours children of the context node other than the first hours child of the context node. .
   */
  @org.junit.Test
  public void unabbreviatedSyntax23() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee) return $h/child::hours[fn:position() > 1]",
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
      assertSerialization("<hours>20</hours><hours>40</hours><hours>30</hours>", false)
    );
  }

  /**
   *  Evaluate "/descendant::employee[fn:position() = 12]". Selects the twelfth employee element in the document containing the context node. .
   */
  @org.junit.Test
  public void unabbreviatedSyntax26() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/descendant::employee[fn:position() = 12]",
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
      assertSerialization("<employee name=\"John Doe 12\" gender=\"male\">\n   <empnum>E4</empnum>\n   <pnum>P4</pnum>\n   <hours>40</hours>\n   <overtime>\n     <day>Monday</day>\n     <day>Tuesday</day>\n   </overtime>\n  </employee>", false)
    );
  }

  /**
   *  Evaluate "/child::works/child::employee[fn:position() = 5]/child::hours[fn:position() = 2]". Selects the second hour of the fifth employee of the works whose parent is the document node that contains the context node. .
   */
  @org.junit.Test
  public void unabbreviatedSyntax27() {
    final XQuery query = new XQuery(
      "/child::works/child::employee[fn:position() = 5]/child::hours[fn:position() = 2]",
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
      assertSerialization("<hours>30</hours>", false)
    );
  }

  /**
   *  Evaluate "child::employee[attribute::name eq "Jane Doe 11"]". Selects all "employee" children of the context node that have a "name" attribute with value "Jane Doe 11". .
   */
  @org.junit.Test
  public void unabbreviatedSyntax28() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/child::employee[attribute::name eq \"Jane Doe 11\"]",
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
      assertSerialization("<employee name=\"Jane Doe 11\" gender=\"female\">\n   <empnum>E4</empnum>\n   <pnum>P2</pnum>\n   <hours>20</hours>\n  </employee>", false)
    );
  }

  /**
   *  Evaluate "child::employee[attribute::gender eq 'female'][fn:position() = 5]". Selects the fifth employee child of the context node that has a gender attribute with value "female". .
   */
  @org.junit.Test
  public void unabbreviatedSyntax29() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/child::employee[attribute::gender eq 'female'][fn:position() = 5]",
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
      assertSerialization("<employee name=\"Jane Doe 9\" gender=\"female\">\n   <empnum>E3</empnum>\n   <pnum>P2</pnum>\n   <hours>20</hours>\n  </employee>", false)
    );
  }

  /**
   *  Evaluate selecting all text node children of the context node .
   */
  @org.junit.Test
  public void unabbreviatedSyntax3() {
    final XQuery query = new XQuery(
      "for $h in (/works[1]/employee[2]) return $h/child::text()",
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
   *  Evaluate "child::employee[child::empnum = 'E3']". Selects the employee children of the context node that have one or more empnum children whose typed value is equal to the string "E3". .
   */
  @org.junit.Test
  public void unabbreviatedSyntax30() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/child::employee[child::empnum = 'E3']",
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
      assertSerialization("<employee name=\"Jane Doe 9\" gender=\"female\">\n   <empnum>E3</empnum>\n   <pnum>P2</pnum>\n   <hours>20</hours>\n  </employee><employee name=\"John Doe 10\" gender=\"male\">\n   <empnum>E3</empnum>\n   <pnum>P2</pnum>\n   <hours>20</hours>\n  </employee>", false)
    );
  }

  /**
   *  Evaluate "child::employee[child::status]". Selects the employee children of the context node that have one or more status children. .
   */
  @org.junit.Test
  public void unabbreviatedSyntax31() {
    final XQuery query = new XQuery(
      "for $h in (/works) return $h/child::employee[child::status]",
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
      assertSerialization("<employee name=\"Jane Doe 13\" gender=\"female\" type=\"FT\">\n   <empnum>E4</empnum>\n   <pnum>P5</pnum>\n   <hours>80</hours>\n   <status>active</status>\n  </employee>", false)
    );
  }

  /**
   *  Evaluate "child::*[self::pnum or self::empnum]". Selects the pnum and empnum children of the context node. .
   */
  @org.junit.Test
  public void unabbreviatedSyntax32() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[7]) return $h/child::*[self::pnum or self::empnum]",
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
      assertSerialization("<empnum>E2</empnum><pnum>P1</pnum>", false)
    );
  }

  /**
   *  Evaluate "child::*[self::empnum or self::pnum][fn:position() = fn:last()]". Selects the last empnum or pnum child of the context node. .
   */
  @org.junit.Test
  public void unabbreviatedSyntax33() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[6]) return $h/child::*[self::empnum or self::pnum][fn:position() = fn:last()]",
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
      assertSerialization("<pnum>P6</pnum>", false)
    );
  }

  /**
   *  Evaluate selecting all children of the context node .
   */
  @org.junit.Test
  public void unabbreviatedSyntax4() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[1]) return $h/child::node()",
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
      assertSerialization("\n   <empnum>E1</empnum>\n   <pnum>P1</pnum>\n   <hours>40</hours>\n  ", false)
    );
  }

  /**
   *  Evaluate selecting all the children the context node (child::node). .
   */
  @org.junit.Test
  public void unabbreviatedSyntax5() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[2]) return $h/child::node()",
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
      assertSerialization("\n   <empnum>E1</empnum>\n   <pnum>P2</pnum>\n   <hours>70</hours>\n   <hours>20</hours>Text data from Employee[2]\n  ", false)
    );
  }

  /**
   *  Evaluate selecting the parent (parent::node()) of the context node .
   */
  @org.junit.Test
  public void unabbreviatedSyntax8() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[1]/hours) return $h/parent::node()",
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
      assertSerialization("<employee name=\"Jane Doe 1\" gender=\"female\">\n   <empnum>E1</empnum>\n   <pnum>P1</pnum>\n   <hours>40</hours>\n  </employee>", false)
    );
  }

  /**
   *  Evaluate selecting a descendant (descendant::empnum)- Select the "empnum" descendants of the context node .
   */
  @org.junit.Test
  public void unabbreviatedSyntax9() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee) return $h/descendant::empnum",
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
      assertSerialization("<empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E1</empnum><empnum>E2</empnum><empnum>E2</empnum><empnum>E3</empnum><empnum>E3</empnum><empnum>E4</empnum><empnum>E4</empnum><empnum>E4</empnum>", false)
    );
  }
}
