package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the GroupByClause production in XQuery 3.0.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdGroupByClause extends QT3TestSet {

  /**
   * Group atomic values.
   */
  @org.junit.Test
  public void group001() {
    final XQuery query = new XQuery(
      " \n" +
      "            for $x in 1 to 100 \n" +
      "            let $key := $x mod 10 \n" +
      "            group by $key \n" +
      "            return string(text{$x})\n" +
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
      assertPermutation("\"1 11 21 31 41 51 61 71 81 91\", \"2 12 22 32 42 52 62 72 82 92\", \"3 13 23 33 43 53 63 73 83 93\", \n            \"4 14 24 34 44 54 64 74 84 94\", \"5 15 25 35 45 55 65 75 85 95\", \"6 16 26 36 46 56 66 76 86 96\", \n            \"7 17 27 37 47 57 67 77 87 97\", \"8 18 28 38 48 58 68 78 88 98\", \"9 19 29 39 49 59 69 79 89 99\", \n            \"10 20 30 40 50 60 70 80 90 100\"")
    );
  }

  /**
   * Group atomic values using new syntax agreed Sept 2011.
   */
  @org.junit.Test
  public void group001a() {
    final XQuery query = new XQuery(
      " \n" +
      "            for $x in 1 to 100 \n" +
      "            group by $key := $x mod 10 \n" +
      "            return string(text{$x})\n" +
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
      assertPermutation("\"1 11 21 31 41 51 61 71 81 91\", \"2 12 22 32 42 52 62 72 82 92\", \"3 13 23 33 43 53 63 73 83 93\", \n            \"4 14 24 34 44 54 64 74 84 94\", \"5 15 25 35 45 55 65 75 85 95\", \"6 16 26 36 46 56 66 76 86 96\", \n            \"7 17 27 37 47 57 67 77 87 97\", \"8 18 28 38 48 58 68 78 88 98\", \"9 19 29 39 49 59 69 79 89 99\", \n            \"10 20 30 40 50 60 70 80 90 100\"")
    );
  }

  /**
   *  Basic grouping test, using nodes as input .
   */
  @org.junit.Test
  public void group002() {
    final XQuery query = new XQuery(
      " \n" +
      "            for $x in //employee \n" +
      "            let $key := $x/@gender \n" +
      "            group by $key \n" +
      "            return concat($key, ':',  \n" +
      "                   string-join(for $e in $x return $e/@name/string(), ',')) \n" +
      "      ",
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
      assertPermutation("\n            \"female:Jane Doe 1,Jane Doe 3,Jane Doe 5,Jane Doe 7,Jane Doe 9,Jane Doe 11,Jane Doe 13\",\n            \"male:John Doe 2,John Doe 4,John Doe 6,John Doe 8,John Doe 10,John Doe 12\"\n         ")
    );
  }

  /**
   *  Basic grouping test, using nodes as input, using new Sept 2011 syntax .
   */
  @org.junit.Test
  public void group002a() {
    final XQuery query = new XQuery(
      " \n" +
      "            for $x in //employee \n" +
      "            group by $key := $x/@gender \n" +
      "            return concat($key, ':',  \n" +
      "                   string-join(for $e in $x return $e/@name/string(), ',')) \n" +
      "      ",
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
      assertPermutation("\n            \"female:Jane Doe 1,Jane Doe 3,Jane Doe 5,Jane Doe 7,Jane Doe 9,Jane Doe 11,Jane Doe 13\",\n            \"male:John Doe 2,John Doe 4,John Doe 6,John Doe 8,John Doe 10,John Doe 12\"\n         ")
    );
  }

  /**
   *  Basic grouping test, using nodes as input, boolean grouping key .
   */
  @org.junit.Test
  public void group003() {
    final XQuery query = new XQuery(
      " \n" +
      "            for $x in //employee \n" +
      "            let $key := ($x/@gender = 'male') \n" +
      "            group by $key \n" +
      "            return concat($key, ':',  \n" +
      "                   string-join(for $e in $x return $e/@name/string(), ',')) \n" +
      "      ",
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
      assertPermutation("\n            \"false:Jane Doe 1,Jane Doe 3,Jane Doe 5,Jane Doe 7,Jane Doe 9,Jane Doe 11,Jane Doe 13\",\n            \"true:John Doe 2,John Doe 4,John Doe 6,John Doe 8,John Doe 10,John Doe 12\"\n         ")
    );
  }

  /**
   *  Basic grouping test, using nodes as input, boolean grouping key;
   *           using new Sept 2011 syntax.
   */
  @org.junit.Test
  public void group003a() {
    final XQuery query = new XQuery(
      " \n" +
      "            for $x in //employee \n" +
      "            group by $key := ($x/@gender = 'male') \n" +
      "            return concat($key, ':',  \n" +
      "                   string-join(for $e in $x return $e/@name/string(), ',')) \n" +
      "      ",
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
      assertPermutation("\n            \"false:Jane Doe 1,Jane Doe 3,Jane Doe 5,Jane Doe 7,Jane Doe 9,Jane Doe 11,Jane Doe 13\",\n            \"true:John Doe 2,John Doe 4,John Doe 6,John Doe 8,John Doe 10,John Doe 12\"\n         ")
    );
  }

  /**
   *  Basic grouping test, using nodes as input, aggregate over a group .
   */
  @org.junit.Test
  public void group004() {
    final XQuery query = new XQuery(
      " \n" +
      "            for $x in //employee \n" +
      "            let $key := $x/@gender \n" +
      "            group by $key \n" +
      "            return concat($key, ':', avg($x/hours)) \n" +
      "      ",
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
      assertPermutation("\"female:41.25\", \"male:37.75\"")
    );
  }

  /**
   *  Basic grouping test, multivalued grouping key, currently an error .
   */
  @org.junit.Test
  public void group005() {
    final XQuery query = new XQuery(
      "\n" +
      "            for $x in //employee \n" +
      "            let $key := $x/hours \n" +
      "            group by $key \n" +
      "            return <group hours=\"{$key}\" avHours=\"{avg($x/hours)}\"/> \n" +
      "      ",
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
      error("XPTY0004")
    );
  }

  /**
   *  A grouping key can be empty .
   */
  @org.junit.Test
  public void group006() {
    final XQuery query = new XQuery(
      "\n" +
      "        <out>{ \n" +
      "            for $x in //employee \n" +
      "            group by $key := $x/status \n" +
      "            return <group status=\"{$key}\" count=\"{count($x)}\"/> \n" +
      "        }</out>\n" +
      "      ",
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
      (
        assertSerialization("<out><group status=\"\" count=\"12\"/><group status=\"active\" count=\"1\"/></out>", false)
      ||
        assertSerialization("<out><group status=\"active\" count=\"1\"/><group status=\"\" count=\"12\"/></out>", false)
      )
    );
  }

  /**
   *  Group by with a where clause .
   */
  @org.junit.Test
  public void group007() {
    final XQuery query = new XQuery(
      " \n" +
      "            for $x in //employee \n" +
      "            let $key := $x/hours[1] \n" +
      "            group by $key \n" +
      "            where count($x) gt 2 \n" +
      "            return concat($key, ':', count($x)) \n" +
      "      ",
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
      assertPermutation("\"40:3\", \"80:3\", \"20:5\"")
    );
  }

  /**
   *  Group by with an order-by clause .
   */
  @org.junit.Test
  public void group008() {
    final XQuery query = new XQuery(
      "\n" +
      "        <out>{ \n" +
      "            for $x in //employee \n" +
      "            let $key := $x/empnum \n" +
      "            group by $key \n" +
      "            order by count($x), $key \n" +
      "            return <group count=\"{count($x)}\"> {string-join($x/@name, '|')} </group> \n" +
      "        }</out>\n" +
      "      ",
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
      assertSerialization("<out><group count=\"2\">Jane Doe 7|John Doe 8</group><group count=\"2\">Jane Doe 9|John Doe 10</group><group count=\"3\">Jane Doe 11|John Doe 12|Jane Doe 13</group><group count=\"6\">Jane Doe 1|John Doe 2|Jane Doe 3|John Doe 4|Jane Doe 5|John Doe 6</group></out>", false)
    );
  }

  /**
   *  Group by with a collation, and with sorting .
   */
  @org.junit.Test
  public void group009() {
    final XQuery query = new XQuery(
      "\n" +
      "        <out>{ \n" +
      "            for $x in //employee \n" +
      "            let $key := $x/empnum \n" +
      "            group by $key collation \"http://www.w3.org/2005/xpath-functions/collation/codepoint\" \n" +
      "            order by $key \n" +
      "            return <group count=\"{count($x)}\" key=\"{$key}\"> {string-join($x/pnum, '|')} </group> \n" +
      "        }</out>\n" +
      "      ",
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
      assertSerialization("<out><group count=\"6\" key=\"E1\">P1|P2|P3|P4|P5|P6</group><group count=\"2\" key=\"E2\">P1|P2</group><group count=\"2\" key=\"E3\">P2|P2</group><group count=\"3\" key=\"E4\">P2|P4|P5</group></out>", false)
    );
  }

  /**
   *  Group by with a collation, and with sorting; using new (Sept 2011) syntax .
   */
  @org.junit.Test
  public void group009a() {
    final XQuery query = new XQuery(
      "\n" +
      "        <out>{ \n" +
      "            for $x in //employee \n" +
      "            group by $key := $x/empnum collation \"http://www.w3.org/2005/xpath-functions/collation/codepoint\" \n" +
      "            order by $key \n" +
      "            return <group count=\"{count($x)}\" key=\"{$key}\"> {string-join($x/pnum, '|')} </group> \n" +
      "        }</out>\n" +
      "      ",
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
      assertSerialization("<out><group count=\"6\" key=\"E1\">P1|P2|P3|P4|P5|P6</group><group count=\"2\" key=\"E2\">P1|P2</group><group count=\"2\" key=\"E3\">P2|P2</group><group count=\"3\" key=\"E4\">P2|P4|P5</group></out>", false)
    );
  }

  /**
   *  NaN values go in the same group .
   */
  @org.junit.Test
  public void group010() {
    final XQuery query = new XQuery(
      "\n" +
      "        <out>{ \n" +
      "            for $x in //employee \n" +
      "            let $key := if ($x/@gender='male') then number('NaN') else 42 \n" +
      "            group by $key \n" +
      "            return <group key=\"{$key}\" count=\"{count($x)}\"/> \n" +
      "        }</out>\n" +
      "      ",
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
      (
        assertSerialization("<out><group key=\"NaN\" count=\"6\"/><group key=\"42\" count=\"7\"/></out>", false)
      ||
        assertSerialization("<out><group key=\"42\" count=\"7\"/><group key=\"NaN\" count=\"6\"/></out>", false)
      )
    );
  }

  /**
   * Group by with position variable (Bug report on Saxon submitted by Leo Wörteler of BaseX).
   */
  @org.junit.Test
  public void group011() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $a at $p in 1 to 10\n" +
      "        let $g := $p mod 2\n" +
      "        group by $g\n" +
      "        return string-join($p!string(), ' ')  \n" +
      "     ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertPermutation("\"1 3 5 7 9\", \"2 4 6 8 10\"")
    );
  }

  /**
   *  Grouping of an empty sequence .
   */
  @org.junit.Test
  public void group012() {
    final XQuery query = new XQuery(
      "\n" +
      "        <out>{ \n" +
      "            for $x in //employee[age > 300] \n" +
      "            let $key := @gender \n" +
      "            group by $key \n" +
      "            return <group gender=\"{$key}\"> { \n" +
      "                        for $e in $x return <person>{$e/@name/string()}</person> \n" +
      "                   } </group> \n" +
      "        }</out>\n" +
      "      ",
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
      assertSerialization("<out/>", false)
    );
  }

  /**
   * Grouping by already existing variable.
   */
  @org.junit.Test
  public void group013() {
    final XQuery query = new XQuery(
      "\n" +
      "       for $x in 1 to 10, $y in 1 to 4\n" +
      "       let $org_y := $y\n" +
      "       group by $y, $y := $x mod 2\n" +
      "       return <grp y=\"{$org_y[1]}\" even=\"{$y}\">{$x}</grp>\n" +
      "     ",
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
        assertSerialization("<grp even=\"1\" y=\"1\">1 1 1 1 3 3 3 3 5 5 5 5 7 7 7 7 9 9 9 9</grp><grp even=\"0\" y=\"1\">2 2 2 2 4 4 4 4 6 6 6 6 8 8 8 8 10 10 10 10</grp>", false)
      ||
        assertSerialization("<grp even=\"0\" y=\"1\">2 2 2 2 4 4 4 4 6 6 6 6 8 8 8 8 10 10 10 10</grp><grp even=\"1\" y=\"1\">1 1 1 1 3 3 3 3 5 5 5 5 7 7 7 7 9 9 9 9</grp>", false)
      )
    );
  }

  /**
   * Referenced grouping variable is not in the tuple stream. .
   */
  @org.junit.Test
  public void group014() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $x := 1\n" +
      "         return\n" +
      "           for $i in (\"a\", \"b\")\n" +
      "           group by $x\n" +
      "           return\n" +
      "             ($x, count($i))\n" +
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
      error("XQST0094")
    );
  }

  /**
   * No value comparisons are available to compare the grouping keys..
   */
  @org.junit.Test
  public void group015() {
    final XQuery query = new XQuery(
      "\n" +
      "          for $x in (true(), \"true\", xs:QName(\"true\"))\n" +
      "          group by $x\n" +
      "          return $x\n" +
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
      assertSerialization("true true true", false)
    );
  }

  /**
   * In the first grouping spec, the grouping variable does not reference the generated let binding.  See also group-013 .
   */
  @org.junit.Test
  public void group016() {
    final XQuery query = new XQuery(
      "\n" +
      "       count(\n" +
      "         for $y in 1 to 10\n" +
      "         group by $y := $y, $y := $y mod 2\n" +
      "         return $y\n" +
      "       )\n" +
      "     ",
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
   * Use Case "Group By" - Q1.
   */
  @org.junit.Test
  public void useCaseGroupbyQ1() {
    final XQuery query = new XQuery(
      "\n" +
      "               <sales-qty-by-product>{\n" +
      "                 for $sales in $sales-records-doc/*/record\n" +
      "                 let $pname := $sales/product-name\n" +
      "                 group by $pname\n" +
      "                 order by $pname\n" +
      "                 return\n" +
      "                   <product name=\"{$pname}\">{\n" +
      "                     sum($sales/qty)\n" +
      "                   }</product>\n" +
      "               }</sales-qty-by-product> \n" +
      "      ",
      ctx);
    try {
      query.bind("$products-doc", node(file("prod/GroupByClause/products.xml")));
      query.bind("$sales-records-doc", node(file("prod/GroupByClause/sales-records.xml")));
      query.bind("$stores-doc", node(file("prod/GroupByClause/stores.xml")));
      query.bind("$books-doc", node(file("prod/GroupByClause/books.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<sales-qty-by-product><product name=\"blender\">250</product><product name=\"broiler\">20</product><product name=\"shirt\">10</product><product name=\"socks\">510</product><product name=\"toaster\">200</product></sales-qty-by-product>", false)
    );
  }

  /**
   * Use Case "Group By" - Q2.
   */
  @org.junit.Test
  public void useCaseGroupbyQ2() {
    final XQuery query = new XQuery(
      "\n" +
      "               <result>{\n" +
      "                 for $sales in $sales-records-doc/*/record\n" +
      "                 let $state := $stores-doc/*/store[store-number = $sales/store-number]/state\n" +
      "                 let $category := $products-doc/*/product[name = $sales/product-name]/category\n" +
      "                 group by $state, $category\n" +
      "                 order by $state, $category\n" +
      "                 return\n" +
      "                   <group>\n" +
      "                     <state>{$state}</state>\n" +
      "                     <category>{$category}</category>\n" +
      "                     <total-qty>{sum($sales/qty)}</total-qty>\n" +
      "                   </group>\n" +
      "               }</result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$products-doc", node(file("prod/GroupByClause/products.xml")));
      query.bind("$sales-records-doc", node(file("prod/GroupByClause/sales-records.xml")));
      query.bind("$stores-doc", node(file("prod/GroupByClause/stores.xml")));
      query.bind("$books-doc", node(file("prod/GroupByClause/books.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><group><state>CA</state><category>clothes</category><total-qty>510</total-qty></group><group><state>CA</state><category>kitchen</category><total-qty>170</total-qty></group><group><state>MA</state><category>clothes</category><total-qty>10</total-qty></group><group><state>MA</state><category>kitchen</category><total-qty>300</total-qty></group></result>", false)
    );
  }

  /**
   * Use Case "Group By" - Q3.
   */
  @org.junit.Test
  public void useCaseGroupbyQ3() {
    final XQuery query = new XQuery(
      "\n" +
      "               <result>{\n" +
      "                 for $sales in $sales-records-doc/*/record\n" +
      "                 let $state := $stores-doc/*/store[store-number = $sales/store-number]/state,\n" +
      "                   $product := $products-doc/*/product[name = $sales/product-name],\n" +
      "                   $category := $product/category,\n" +
      "                   $revenue := $sales/qty * $product/price\n" +
      "                 group by $state, $category\n" +
      "                 order by $state, $category\n" +
      "                 return\n" +
      "                   <group>\n" +
      "                     <state>{$state}</state>\n" +
      "                     <category>{$category}</category>\n" +
      "                     <total-revenue>{sum($revenue)}</total-revenue>\n" +
      "                   </group>\n" +
      "               }</result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$products-doc", node(file("prod/GroupByClause/products.xml")));
      query.bind("$sales-records-doc", node(file("prod/GroupByClause/sales-records.xml")));
      query.bind("$stores-doc", node(file("prod/GroupByClause/stores.xml")));
      query.bind("$books-doc", node(file("prod/GroupByClause/books.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><group><state>CA</state><category>clothes</category><total-revenue>2550</total-revenue></group><group><state>CA</state><category>kitchen</category><total-revenue>6500</total-revenue></group><group><state>MA</state><category>clothes</category><total-revenue>100</total-revenue></group><group><state>MA</state><category>kitchen</category><total-revenue>14000</total-revenue></group></result>", false)
    );
  }

  /**
   * Use Case "Group By" - Q4.
   */
  @org.junit.Test
  public void useCaseGroupbyQ4() {
    final XQuery query = new XQuery(
      "\n" +
      "               <result>{\n" +
      "                 for $store in $stores-doc/*/store\n" +
      "                 let $state := $store/state\n" +
      "                 group by $state\n" +
      "                 order by $state\n" +
      "                 return\n" +
      "                   <state name=\"{$state}\">{\n" +
      "                     for $product in $products-doc/*/product\n" +
      "                     let $category := $product/category\n" +
      "                     group by $category\n" +
      "                     order by $category\n" +
      "                     return\n" +
      "                       <category name=\"{$category}\">{\n" +
      "                         for $sales in $sales-records-doc/*/record[store-number = $store/store-number\n" +
      "                           and product-name = $product/name]\n" +
      "                         let $pname := $sales/product-name\n" +
      "                         group by $pname\n" +
      "                         order by $pname\n" +
      "                         return\n" +
      "                           <product name=\"{$pname}\" total-qty=\"{sum($sales/qty)}\" />\n" +
      "                         }</category>\n" +
      "                   }</state>\n" +
      "               }</result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$products-doc", node(file("prod/GroupByClause/products.xml")));
      query.bind("$sales-records-doc", node(file("prod/GroupByClause/sales-records.xml")));
      query.bind("$stores-doc", node(file("prod/GroupByClause/stores.xml")));
      query.bind("$books-doc", node(file("prod/GroupByClause/books.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><state name=\"CA\"><category name=\"clothes\"><product name=\"socks\" total-qty=\"510\"/></category><category name=\"kitchen\"><product name=\"broiler\" total-qty=\"20\"/><product name=\"toaster\" total-qty=\"150\"/></category></state><state name=\"MA\"><category name=\"clothes\"><product name=\"shirt\" total-qty=\"10\"/></category><category name=\"kitchen\"><product name=\"blender\" total-qty=\"250\"/><product name=\"toaster\" total-qty=\"50\"/></category></state><state name=\"WA\"><category name=\"clothes\"/><category name=\"kitchen\"/></state></result>", false)
    );
  }

  /**
   * Use Case "Group By" - Q5.
   */
  @org.junit.Test
  public void useCaseGroupbyQ5() {
    final XQuery query = new XQuery(
      "\n" +
      "               <result>{\n" +
      "                 for $sales in $sales-records-doc/*/record\n" +
      "                 let $storeno := $sales/store-number\n" +
      "                 group by $storeno\n" +
      "                 order by $storeno\n" +
      "                 return\n" +
      "                   <store number = \"{$storeno}\">{\n" +
      "                     for $s in $sales\n" +
      "                     order by xs:int($s/qty) descending\n" +
      "                     return\n" +
      "                       <product name = \"{$s/product-name}\" qty = \"{$s/qty}\"/>\n" +
      "                   }</store>\n" +
      "               }</result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$products-doc", node(file("prod/GroupByClause/products.xml")));
      query.bind("$sales-records-doc", node(file("prod/GroupByClause/sales-records.xml")));
      query.bind("$stores-doc", node(file("prod/GroupByClause/stores.xml")));
      query.bind("$books-doc", node(file("prod/GroupByClause/books.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><store number=\"1\"><product name=\"socks\" qty=\"500\"/><product name=\"broiler\" qty=\"20\"/></store><store number=\"2\"><product name=\"toaster\" qty=\"100\"/><product name=\"toaster\" qty=\"50\"/><product name=\"socks\" qty=\"10\"/></store><store number=\"3\"><product name=\"blender\" qty=\"150\"/><product name=\"blender\" qty=\"100\"/><product name=\"toaster\" qty=\"50\"/><product name=\"shirt\" qty=\"10\"/></store></result>", false)
    );
  }

  /**
   * Use Case "Group By" - Q6.
   */
  @org.junit.Test
  public void useCaseGroupbyQ6() {
    final XQuery query = new XQuery(
      "\n" +
      "               <result>{\n" +
      "                 for $sales in $sales-records-doc/*/record\n" +
      "                 let $storeno := $sales/store-number,\n" +
      "                   $product := $products-doc/*/product[name = $sales/product-name],\n" +
      "                   $prd := $product,\n" +
      "                   $profit := $sales/qty * ($prd/price - $prd/cost)\n" +
      "                 group by $storeno\n" +
      "                 let $total-store-profit := sum($profit)\n" +
      "                 where $total-store-profit > 100\n" +
      "                 order by $total-store-profit descending\n" +
      "                 return\n" +
      "                   <store number = \"{$storeno}\" total-profit = \"{$total-store-profit}\"/>\n" +
      "                }</result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$products-doc", node(file("prod/GroupByClause/products.xml")));
      query.bind("$sales-records-doc", node(file("prod/GroupByClause/sales-records.xml")));
      query.bind("$stores-doc", node(file("prod/GroupByClause/stores.xml")));
      query.bind("$books-doc", node(file("prod/GroupByClause/books.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><store number=\"3\" total-profit=\"7320\"/><store number=\"2\" total-profit=\"3030\"/><store number=\"1\" total-profit=\"2100\"/></result>", false)
    );
  }

  /**
   * Use Case "Group By" - Q7.
   */
  @org.junit.Test
  public void useCaseGroupbyQ7() {
    final XQuery query = new XQuery(
      "\n" +
      "               <result>{\n" +
      "                 for $book in $books-doc/*/book\n" +
      "                 for $author in $book/author\n" +
      "                 group by $author\n" +
      "                 order by $author\n" +
      "                 return\n" +
      "                 <author name=\"{$author}\">{\n" +
      "                   for $b in $book\n" +
      "                   order by $b/title\n" +
      "                   return\n" +
      "                     <title> {fn:data($b/title)} </title>\n" +
      "                 }</author>\n" +
      "               }</result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$products-doc", node(file("prod/GroupByClause/products.xml")));
      query.bind("$sales-records-doc", node(file("prod/GroupByClause/sales-records.xml")));
      query.bind("$stores-doc", node(file("prod/GroupByClause/stores.xml")));
      query.bind("$books-doc", node(file("prod/GroupByClause/books.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><author name=\"Alan Simon\"><title>SQL:1999</title><title>Strategic Database Technology</title></author><author name=\"Andrew Eisenberg\"><title>Understanding SQL and Java Together</title></author><author name=\"Jim Melton\"><title>Advanced SQL:1999</title><title>Querying XML</title><title>SQL:1999</title><title>Understanding SQL and Java Together</title></author><author name=\"Stephen Buxton\"><title>Querying XML</title></author></result>", false)
    );
  }

  /**
   * Use Case "Group By" - Q8.
   */
  @org.junit.Test
  public void useCaseGroupbyQ8() {
    final XQuery query = new XQuery(
      "\n" +
      "               <result>{\n" +
      "                 for $book in $books-doc/*/book\n" +
      "                 let $author-list := fn:string-join($book/author, ', ')\n" +
      "                 group by $author-list\n" +
      "                 order by $author-list\n" +
      "                 return\n" +
      "                   <author-list names=\"{$author-list}\">{\n" +
      "                     for $b in $book\n" +
      "                     order by $b/title\n" +
      "                     return\n" +
      "                       <title> {fn:data($b/title)} </title>\n" +
      "                   }</author-list>\n" +
      "               }</result>\n" +
      "      ",
      ctx);
    try {
      query.bind("$products-doc", node(file("prod/GroupByClause/products.xml")));
      query.bind("$sales-records-doc", node(file("prod/GroupByClause/sales-records.xml")));
      query.bind("$stores-doc", node(file("prod/GroupByClause/stores.xml")));
      query.bind("$books-doc", node(file("prod/GroupByClause/books.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><author-list names=\"Alan Simon\"><title>Strategic Database Technology</title></author-list><author-list names=\"Jim Melton\"><title>Advanced SQL:1999</title></author-list><author-list names=\"Jim Melton, Alan Simon\"><title>SQL:1999</title></author-list><author-list names=\"Jim Melton, Andrew Eisenberg\"><title>Understanding SQL and Java Together</title></author-list><author-list names=\"Jim Melton, Stephen Buxton\"><title>Querying XML</title></author-list></result>", false)
    );
  }
}
