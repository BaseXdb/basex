package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the "outer for" clause - "for $x allowing empty".
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdAllowingEmpty extends QT3TestSet {

  /**
   * outer for clause .
   */
  @org.junit.Test
  public void outer001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $n as xs:integer external := 10; \n" +
      "        <out>{ for $x allowing empty in 1 to $n return <a>{$x}</a> }</out>\n" +
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
      assertSerialization("<out><a>1</a><a>2</a><a>3</a><a>4</a><a>5</a><a>6</a><a>7</a><a>8</a><a>9</a><a>10</a></out>", false)
    );
  }

  /**
   * outer for clause .
   */
  @org.junit.Test
  public void outer002() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $n as xs:integer external := 0; \n" +
      "        <out>{ for $x allowing empty in 1 to $n return <a>{$x}</a> }</out>\n" +
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
      assertSerialization("<out><a/></out>", false)
    );
  }

  /**
   * outer for clause with position variable .
   */
  @org.junit.Test
  public void outer003() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $n as xs:integer external := 10; \n" +
      "        <out>{ for $x allowing empty at $p in 1 to $n return <a position=\"{$p}\">{$x}</a> }</out>\n" +
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
      assertSerialization("<out><a position=\"1\">1</a><a position=\"2\">2</a><a position=\"3\">3</a><a position=\"4\">4</a><a \n         position=\"5\">5</a><a position=\"6\">6</a><a position=\"7\">7</a><a position=\"8\">8</a><a position=\"9\">9</a><a position=\"10\">10</a></out>", false)
    );
  }

  /**
   * outer for clause with position variable .
   */
  @org.junit.Test
  public void outer004() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $n as xs:integer external := 0; \n" +
      "        <out>{ for $x allowing empty at $p in 1 to $n return <a position=\"{$p}\">{$x}</a> }</out>\n" +
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
      assertSerialization("<out><a position=\"0\"/></out>", false)
    );
  }

  /**
   * outer for clause, pull mode .
   */
  @org.junit.Test
  public void outer005() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $n as xs:integer external := 10;\n" +
      "        <out>{ string-join(for $x allowing empty in 1 to $n return concat('[',$x,']'), '|') }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>[1]|[2]|[3]|[4]|[5]|[6]|[7]|[8]|[9]|[10]</out>", false)
    );
  }

  /**
   * outer for clause, pull mode .
   */
  @org.junit.Test
  public void outer006() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $n as xs:integer external := 0; \n" +
      "        <out>{ string-join(for $x allowing empty in 1 to $n return concat('[',$x,']'), '|') }</out>\n" +
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
      assertSerialization("<out>[]</out>", false)
    );
  }

  /**
   * outer for clause, nested .
   */
  @org.junit.Test
  public void outer007() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $m as xs:integer external := 5; \n" +
      "        declare variable $n as xs:integer external := 5;\n" +
      "         <out>{ for $x allowing empty at $p in 1 to $m, $y at $q in 1 to $n return <a p=\"{$p}\" q=\"{$q}\">{$x},{$y}</a> }</out>\n" +
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
      assertSerialization("<out><a p=\"1\" q=\"1\">1,1</a><a p=\"1\" q=\"2\">1,2</a><a p=\"1\" q=\"3\">1,3</a><a p=\"1\" q=\"4\">1,4</a><a p=\"1\" q=\"5\">1,5</a><a \n         p=\"2\" q=\"1\">2,1</a><a p=\"2\" q=\"2\">2,2</a><a p=\"2\" q=\"3\">2,3</a><a p=\"2\" q=\"4\">2,4</a><a p=\"2\" q=\"5\">2,5</a><a \n         p=\"3\" q=\"1\">3,1</a><a p=\"3\" q=\"2\">3,2</a><a p=\"3\" q=\"3\">3,3</a><a p=\"3\" q=\"4\">3,4</a><a p=\"3\" q=\"5\">3,5</a><a \n         p=\"4\" q=\"1\">4,1</a><a p=\"4\" q=\"2\">4,2</a><a p=\"4\" q=\"3\">4,3</a><a p=\"4\" q=\"4\">4,4</a><a p=\"4\" q=\"5\">4,5</a><a \n         p=\"5\" q=\"1\">5,1</a><a p=\"5\" q=\"2\">5,2</a><a p=\"5\" q=\"3\">5,3</a><a p=\"5\" q=\"4\">5,4</a><a p=\"5\" q=\"5\">5,5</a></out>", false)
    );
  }

  /**
   * outer for clause, nested .
   */
  @org.junit.Test
  public void outer008() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $m as xs:integer external := 5; \n" +
      "        declare variable $n as xs:integer external := 0; \n" +
      "        <out>{ for $x allowing empty at $p in 1 to $m, $y allowing empty at $q in 1 to $n return <a p=\"{$p}\" q=\"{$q}\">{$x},{$y}</a> }</out>\n" +
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
      assertSerialization("<out><a p=\"1\" q=\"0\">1,</a><a p=\"2\" q=\"0\">2,</a><a p=\"3\" q=\"0\">3,</a><a p=\"4\" q=\"0\">4,</a><a p=\"5\" q=\"0\">5,</a></out>", false)
    );
  }

  /**
   * outer for clause, nested .
   */
  @org.junit.Test
  public void outer009() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $m as xs:integer external := 0; \n" +
      "        declare variable $n as xs:integer external := 5; \n" +
      "        <out>{ for $x allowing empty at $p in 1 to $m, $y at $q in 1 to $n return <a p=\"{$p}\" q=\"{$q}\">{$x},{$y}</a> }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out><a p=\"0\" q=\"1\">,1</a><a p=\"0\" q=\"2\">,2</a><a p=\"0\" q=\"3\">,3</a><a p=\"0\" q=\"4\">,4</a><a p=\"0\" q=\"5\">,5</a></out>", false)
    );
  }

  /**
   * outer for clause, nested .
   */
  @org.junit.Test
  public void outer010() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $m as xs:integer external := 0; \n" +
      "        declare variable $n as xs:integer external := 0; \n" +
      "        <out>{ for $x allowing empty at $p in 1 to $m, \n" +
      "                   $y allowing empty at $q in 1 to $n return <a p=\"{$p}\" q=\"{$q}\">{$x},{$y}</a> }</out>\n" +
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
      assertSerialization("<out><a p=\"0\" q=\"0\">,</a></out>", false)
    );
  }

  /**
   * outer for clause, nested, one loop depends on the other .
   */
  @org.junit.Test
  public void outer011() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $n as xs:integer external := 5; \n" +
      "        <out>{ for $x allowing empty at $p in 1 to $n, $y allowing empty at $q in ($x+1) to $n return <a p=\"{$p}\" q=\"{$q}\">{$x},{$y}</a> }</out>\n" +
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
      assertSerialization("<out><a p=\"1\" q=\"1\">1,2</a><a p=\"1\" q=\"2\">1,3</a><a p=\"1\" q=\"3\">1,4</a><a p=\"1\" q=\"4\">1,5</a><a \n         p=\"2\" q=\"1\">2,3</a><a p=\"2\" q=\"2\">2,4</a><a p=\"2\" q=\"3\">2,5</a><a p=\"3\" q=\"1\">3,4</a><a p=\"3\" q=\"2\">3,5</a><a p=\"4\" q=\"1\">4,5</a><a p=\"5\" q=\"0\">5,</a></out>", false)
    );
  }

  /**
   * outer for clause, nested, one loop depends on the other .
   */
  @org.junit.Test
  public void outer012() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $n as xs:integer external := 5; \n" +
      "        <out>{ \n" +
      "            for $x as xs:integer allowing empty at $p in 1 to $n, \n" +
      "                $y as xs:integer? allowing empty at $q in ($x+1) to $n \n" +
      "            return <a p=\"{$p}\" q=\"{$q}\">{$x},{$y}</a> }</out>\n" +
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
      assertSerialization("<out><a p=\"1\" q=\"1\">1,2</a><a p=\"1\" q=\"2\">1,3</a><a p=\"1\" q=\"3\">1,4</a><a p=\"1\" q=\"4\">1,5</a><a \n         p=\"2\" q=\"1\">2,3</a><a p=\"2\" q=\"2\">2,4</a><a p=\"2\" q=\"3\">2,5</a><a p=\"3\" q=\"1\">3,4</a><a p=\"3\" q=\"2\">3,5</a><a p=\"4\" q=\"1\">4,5</a><a p=\"5\" q=\"0\">5,</a></out>", false)
    );
  }

  /**
   * outer for clause, nested, one loop depends on the other.
   *          Type error because $y does not allow an empty sequence .
   */
  @org.junit.Test
  public void outer013() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $n as xs:integer external := 5; \n" +
      "        <out>{ for $x as xs:integer allowing empty at $p in 1 to $n, \n" +
      "                   $y as xs:integer allowing empty at $q in ($x+1) to $n \n" +
      "               return <a p=\"{$p}\" q=\"{$q}\">{$x},{$y}</a> }</out>\n" +
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
      error("XPTY0004")
    );
  }

  /**
   * outer for clause, nested, mixed with "non-outer" for .
   */
  @org.junit.Test
  public void outer014() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $n as xs:integer external := 5; \n" +
      "        <out>{ for $x as xs:integer at $p in 1 to $n \n" +
      "               for $y as xs:integer? allowing empty at $q in ($x+1) to $n \n" +
      "               return <a p=\"{$p}\" q=\"{$q}\">{$x},{$y}</a> }</out>\n" +
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
      assertSerialization("<out><a p=\"1\" q=\"1\">1,2</a><a p=\"1\" q=\"2\">1,3</a><a p=\"1\" q=\"3\">1,4</a><a p=\"1\" q=\"4\">1,5</a><a \n         p=\"2\" q=\"1\">2,3</a><a p=\"2\" q=\"2\">2,4</a><a p=\"2\" q=\"3\">2,5</a><a p=\"3\" q=\"1\">3,4</a><a p=\"3\" q=\"2\">3,5</a><a p=\"4\" q=\"1\">4,5</a><a p=\"5\" q=\"0\">5,</a></out>", false)
    );
  }

  /**
   * outer for clause, nested, mixed with "non-outer" for .
   */
  @org.junit.Test
  public void outer015() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $n as xs:integer external := 5; \n" +
      "        <out>{ for $x as xs:integer? allowing empty at $p in 1 to $n \n" +
      "               for $y as xs:integer at $q in (if (empty($x)) then 0 else (1 to $x)) \n" +
      "               return <a p=\"{$p}\" q=\"{$q}\">{$x},{$y}</a> }</out>\n" +
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
      assertSerialization("<out><a p=\"1\" q=\"1\">1,1</a><a p=\"2\" q=\"1\">2,1</a><a p=\"2\" q=\"2\">2,2</a><a \n         p=\"3\" q=\"1\">3,1</a><a p=\"3\" q=\"2\">3,2</a><a p=\"3\" q=\"3\">3,3</a><a \n         p=\"4\" q=\"1\">4,1</a><a p=\"4\" q=\"2\">4,2</a><a p=\"4\" q=\"3\">4,3</a><a p=\"4\" q=\"4\">4,4</a><a \n         p=\"5\" q=\"1\">5,1</a><a p=\"5\" q=\"2\">5,2</a><a p=\"5\" q=\"3\">5,3</a><a p=\"5\" q=\"4\">5,4</a><a p=\"5\" q=\"5\">5,5</a></out>", false)
    );
  }

  /**
   * outer for clause, nested, mixed with "non-outer" for .
   */
  @org.junit.Test
  public void outer016() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $n as xs:integer external := 0; \n" +
      "        <out>{ for $x as xs:integer? allowing empty at $p in 1 to $n \n" +
      "               for $y as xs:integer at $q in (if (empty($x)) then 0 else (1 to $x)) \n" +
      "               return <a p=\"{$p}\" q=\"{$q}\">{$x},{$y}</a> }</out>\n" +
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
      assertSerialization("<out><a p=\"0\" q=\"1\">,0</a></out>", false)
    );
  }

  /**
   * outer for clause, nested, with where clause .
   */
  @org.junit.Test
  public void outer017() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $n as xs:integer external := 5; \n" +
      "        <out>{ for $x as xs:integer? allowing empty at $p in 1 to $n \n" +
      "               for $y as xs:integer? allowing empty at $q in (if (empty($x)) then () else (1 to $x)) \n" +
      "               where deep-equal($x,$y) \n" +
      "               return <a p=\"{$p}\" q=\"{$q}\">{$x},{$y}</a> }</out>\n" +
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
      assertSerialization("<out><a p=\"1\" q=\"1\">1,1</a><a p=\"2\" q=\"2\">2,2</a><a p=\"3\" q=\"3\">3,3</a><a p=\"4\" q=\"4\">4,4</a><a p=\"5\" q=\"5\">5,5</a></out>", false)
    );
  }

  /**
   * outer for clause, nested, with where clause .
   */
  @org.junit.Test
  public void outer018() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $n as xs:integer external := 0; \n" +
      "        <out>{ for $x as xs:integer? allowing empty at $p in 1 to $n \n" +
      "               for $y as xs:integer? allowing empty at $q in (if (empty($x)) then () else (1 to $x)) \n" +
      "               where deep-equal($x,$y) return <a p=\"{$p}\" q=\"{$q}\">{$x},{$y}</a> }</out>\n" +
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
      assertSerialization("<out><a p=\"0\" q=\"0\">,</a></out>", false)
    );
  }
}
