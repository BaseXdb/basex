package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the EmptyOrderDecl production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdOrderingModeDecl extends QT3TestSet {

  /**
   *  A simple 'declare ordering mode' declaration, specifying 'preserve'. .
   */
  @org.junit.Test
  public void kDefaultOrderingProlog1() {
    final XQuery query = new XQuery(
      "declare(::)ordering ordered; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A simple 'declare ordering mode' declaration, specifying 'strip'. .
   */
  @org.junit.Test
  public void kDefaultOrderingProlog2() {
    final XQuery query = new XQuery(
      "declare(::)ordering unordered; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Two 'declare ordering mode' declarations are invalid. .
   */
  @org.junit.Test
  public void kDefaultOrderingProlog3() {
    final XQuery query = new XQuery(
      "declare(::)ordering unordered; declare(::)ordering ordered; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0065")
    );
  }

  /**
   *  Ensure the 'ordering' keyword is parsed correctly. .
   */
  @org.junit.Test
  public void k2DefaultOrderingProlog1() {
    final XQuery query = new XQuery(
      "ordering eq ordering",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Ensure the 'order' keyword is parsed correctly. .
   */
  @org.junit.Test
  public void k2DefaultOrderingProlog2() {
    final XQuery query = new XQuery(
      "order eq order",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPDY0002")
    );
  }

  /**
   *  Two ordering mode declarations. Should raise static error. .
   */
  @org.junit.Test
  public void orderDecl1() {
    final XQuery query = new XQuery(
      "declare ordering unordered; declare ordering ordered; \"aa\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0065")
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "ordered" for a FLOWR expression with order modifier (ascending). .
   */
  @org.junit.Test
  public void orderDecl10() {
    final XQuery query = new XQuery(
      "declare ordering ordered; for $x in //hours order by $x ascending return $x",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<hours>12</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>30</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>70</hours><hours>80</hours><hours>80</hours><hours>80</hours>", false)
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "unordered" for a FLOWR expression with order modifier (descending). .
   */
  @org.junit.Test
  public void orderDecl11() {
    final XQuery query = new XQuery(
      "declare ordering unordered; for $x in //hours order by $x descending return $x",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<hours>80</hours><hours>80</hours><hours>80</hours><hours>70</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>30</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>12</hours>", false)
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "unordered" for a FLOWR expression with order modifier (ascending). .
   */
  @org.junit.Test
  public void orderDecl12() {
    final XQuery query = new XQuery(
      "declare ordering unordered; for $x in //hours order by $x ascending return $x",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<hours>12</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>30</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>70</hours><hours>80</hours><hours>80</hours><hours>80</hours>", false)
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "ordered" for an XPATH expression containing "/" .
   */
  @org.junit.Test
  public void orderDecl13() {
    final XQuery query = new XQuery(
      "declare ordering ordered; for $x in (/works/employee/hours) return $x",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<hours>40</hours><hours>70</hours><hours>20</hours><hours>80</hours><hours>20</hours><hours>40</hours><hours>20</hours><hours>30</hours><hours>12</hours><hours>40</hours><hours>80</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>40</hours><hours>80</hours>", false)
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "ordered" for an XPATH expression containing "//" .
   */
  @org.junit.Test
  public void orderDecl15() {
    final XQuery query = new XQuery(
      "declare ordering ordered; for $x in (//day) return $x",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<day>Monday</day><day>Tuesday</day>", false)
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "unordered" for an XPATH expression containing "//" .
   */
  @org.junit.Test
  public void orderDecl16() {
    final XQuery query = new XQuery(
      "declare ordering unordered; for $x in (//day) return $x",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<day>Monday</day><day>Tuesday</day>", false)
      ||
        assertSerialization("<day>Tuesday</day><day>Monday</day>", false)
      )
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "unordered" in the prolog, but overridden by an "ordered" expression .
   */
  @org.junit.Test
  public void orderDecl17() {
    final XQuery query = new XQuery(
      "declare ordering unordered; ordered { for $x in /works//day return $x }",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<day>Monday</day><day>Tuesday</day>", false)
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "ordered" in the prolog, but overridden by an "unordered" expression .
   */
  @org.junit.Test
  public void orderDecl18() {
    final XQuery query = new XQuery(
      "declare ordering ordered; unordered { for $x in /works//day return $x }",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<day>Monday</day><day>Tuesday</day>", false)
      ||
        assertSerialization("<day>Tuesday</day><day>Monday</day>", false)
      )
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "ordered". Use "union" operator. .
   */
  @org.junit.Test
  public void orderDecl2() {
    final XQuery query = new XQuery(
      "declare ordering ordered; let $a := <a><b>1</b><c>2</c></a> return $a/b union $a/c",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<b>1</b><c>2</c>", false)
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "ordered" in the prolog, and use of the "child" axis". .
   */
  @org.junit.Test
  public void orderDecl20() {
    final XQuery query = new XQuery(
      "declare ordering ordered; for $x in /works//overtime return $x/child::day",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<day>Monday</day><day>Tuesday</day>", false)
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "ordered" in the prolog, and use of the "parent" axis". .
   */
  @org.junit.Test
  public void orderDecl21() {
    final XQuery query = new XQuery(
      "declare ordering ordered; for $x in /works//day[1] return $x/parent::node()",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<overtime>\n     <day>Monday</day>\n     <day>Tuesday</day>\n   </overtime>", false)
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "ordered" in the prolog, and use of the "following" axis". .
   */
  @org.junit.Test
  public void orderDecl22() {
    final XQuery query = new XQuery(
      "declare ordering ordered; for $x in /works//day[1] return $x/following::day",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<day>Tuesday</day>", false)
      ||
        error("XPST0010")
      )
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "ordered" in the prolog, and use of the "descendant" axis". .
   */
  @org.junit.Test
  public void orderDecl23() {
    final XQuery query = new XQuery(
      "declare ordering ordered; for $x in /works//overtime return $x/descendant::day",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<day>Monday</day><day>Tuesday</day>", false)
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "ordered". Use "intersect" operator. .
   */
  @org.junit.Test
  public void orderDecl4() {
    final XQuery query = new XQuery(
      "declare ordering ordered; (//overtime) intersect (//overtime)",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<overtime>\n     <day>Monday</day>\n     <day>Tuesday</day>\n   </overtime>", false)
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "unordered". Use "except" operator. .
   */
  @org.junit.Test
  public void orderDecl7() {
    final XQuery query = new XQuery(
      "declare ordering unordered; (//employee[1]) except (//employee[2])",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<employee name=\"Jane Doe 1\" gender=\"female\">\n   <empnum>E1</empnum>\n   <pnum>P1</pnum>\n   <hours>40</hours>\n  </employee>", false)
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "ordered" for a FLOWR expression with no ordering mode. .
   */
  @org.junit.Test
  public void orderDecl8() {
    final XQuery query = new XQuery(
      "declare ordering ordered; for $x in //hours return $x",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<hours>40</hours><hours>70</hours><hours>20</hours><hours>80</hours><hours>20</hours><hours>40</hours><hours>20</hours><hours>30</hours><hours>12</hours><hours>40</hours><hours>80</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>40</hours><hours>80</hours>", false)
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "ordered" for a FLOWR expression with order modifier (descending). .
   */
  @org.junit.Test
  public void orderDecl9() {
    final XQuery query = new XQuery(
      "declare ordering ordered; for $x in //hours order by $x descending return $x",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<hours>80</hours><hours>80</hours><hours>80</hours><hours>70</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>40</hours><hours>30</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>20</hours><hours>12</hours>", false)
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "unordered" for an XPATH expression containing "/" .
   */
  @org.junit.Test
  public void orderdecl14() {
    final XQuery query = new XQuery(
      "declare ordering unordered; for $x in (/works/employee[4]/hours) return $x",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<hours>20</hours><hours>40</hours>", false)
      ||
        assertSerialization("<hours>40</hours><hours>20</hours>", false)
      )
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "unordered". Use "union" operator. .
   */
  @org.junit.Test
  public void orderdecl3() {
    final XQuery query = new XQuery(
      "declare ordering unordered; (<a>1</a>) union (<b>2</b>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<a>1</a><b>2</b>", false)
      ||
        assertSerialization("<b>2</b><a>1</a>", false)
      )
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "unordered". Use "intersect" operator. .
   */
  @org.junit.Test
  public void orderdecl5() {
    final XQuery query = new XQuery(
      "declare ordering unordered; (//overtime) intersect (//overtime)",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<overtime>\n     <day>Monday</day>\n     <day>Tuesday</day>\n   </overtime>", false)
    );
  }

  /**
   *  Simple ordering mode test. Mode set to "ordered". Use "except" operator. .
   */
  @org.junit.Test
  public void orderdecl6() {
    final XQuery query = new XQuery(
      "declare ordering ordered; (//employee[1]) except (//employee[2])",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<employee name=\"Jane Doe 1\" gender=\"female\">\n   <empnum>E1</empnum>\n   <pnum>P1</pnum>\n   <hours>40</hours>\n  </employee>", false)
    );
  }
}
