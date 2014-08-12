package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the Window Clause production.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdWindowClause extends QT3TestSet {

  /**
   * Sliding Window Clause, using variables : window, start item position, end item position.
   */
  @org.junit.Test
  public void slidingWindowExpr501() {
    final XQuery query = new XQuery(
      "for sliding window $w in (1, 2, 3, 4) \n" +
      "      start at $s when fn:true()\n" +
      "      end at $e when $e - $s eq 1\n" +
      "      return $w",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("(1, 2, 2, 3, 3, 4, 4)")
    );
  }

  /**
   * Sliding Window Clause, using only keyword, using variables : window, start item position, end item position.
   */
  @org.junit.Test
  public void slidingWindowExpr502() {
    final XQuery query = new XQuery(
      "for sliding window $w in (1, 2, 3, 4) \n" +
      "      start at $s when fn:true()\n" +
      "      only end at $e when $e - $s eq 1\n" +
      "      return $w",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("(1, 2, 2, 3, 3, 4)")
    );
  }

  /**
   * Sliding Window Clause, with errors due to having similar variable names : http://www.w3.org/TR/xquery-30/#ERRXQST0089 or http://www.w3.org/TR/xquery-30/#ERR.
   */
  @org.junit.Test
  public void slidingWindowExpr503() {
    final XQuery query = new XQuery(
      "for sliding window $w in (1, 2, 3, 4) \n" +
      "      start $s at $s previous $s when fn:true()\n" +
      "      only end $s at $s previous $s when $s - $s eq 1\n" +
      "      return $w",
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
        error("XQST0089")
      ||
        error("XQST0103")
      )
    );
  }

  /**
   * Sliding Window Clause, with errors due to having similar variable names : http://www.w3.org/TR/xquery-30/#ERRXQST0103.
   */
  @org.junit.Test
  public void slidingWindowExpr504() {
    final XQuery query = new XQuery(
      "for sliding window $w in (1, 2, 3, 4) \n" +
      "      start $s at $ps previous $pps when fn:true()\n" +
      "      only end $s at $ps previous $pps when $ps - $ps eq 1\n" +
      "      return $w",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0103")
    );
  }

  /**
   * Sliding Window Clause, positional variable is integer : http://www.w3.org/TR/xquery-30/#dt-positional-variable.
   */
  @org.junit.Test
  public void slidingWindowExpr505() {
    final XQuery query = new XQuery(
      "for sliding window $w in (1, 2, 3, 4) \n" +
      "      start at $s when fn:true()\n" +
      "      end at $e  when $s - $e eq 1\n" +
      "      return $s",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("$result[1] instance of xs:integer")
    );
  }

  /**
   * Sliding Window Clause, windows must be delivered in order of start position.
   */
  @org.junit.Test
  public void slidingWindowExpr506() {
    final XQuery query = new XQuery(
      "\n" +
      "          for sliding window $w in (1, 2, 3, 4, 14, 13, 12, 11) \n" +
      "          start $s when fn:true()\n" +
      "          only end $e when $e eq $s + 10\n" +
      "          return string-join($w!string(), ' ')\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"1 2 3 4 14 13 12 11\", \"2 3 4 14 13 12\", \"3 4 14 13\", \"4 14\"")
    );
  }

  /**
   * Sliding Window Clause, effect of type declaration: success case.
   */
  @org.junit.Test
  public void slidingWindowExpr507() {
    final XQuery query = new XQuery(
      "\n" +
      "          for sliding window $w as xs:integer+ in (1, 2, \"london\", 3, 4, \"paris\")\n" +
      "          start $start when $start instance of xs:integer\n" +
      "          only end next $beyond when $beyond instance of xs:string\n" +
      "          return string-join($w!string(), ' ')\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"1 2\", \"2\", \"3 4\", \"4\"")
    );
  }

  /**
   * Sliding Window Clause, effect of type declaration: failure case.
   */
  @org.junit.Test
  public void slidingWindowExpr508() {
    final XQuery query = new XQuery(
      "\n" +
      "          for sliding window $w as xs:integer+ in (1, 2, \"london\", 3, 4.1, \"paris\")\n" +
      "          start $start when $start instance of xs:integer\n" +
      "          only end next $beyond when $beyond instance of xs:string\n" +
      "          return string-join($w!string(), ' ')\n" +
      "        ",
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
   * Sliding Window Clause, average window size.
   */
  @org.junit.Test
  public void slidingWindowExpr509() {
    final XQuery query = new XQuery(
      "\n" +
      "            avg(\n" +
      "              for sliding window $w in (1, 2, \"london\", 3, 4, \"paris\")\n" +
      "              start $start when $start instance of xs:integer\n" +
      "              only end next $beyond when $beyond instance of xs:string\n" +
      "              return count($w)\n" +
      "            )\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1.5")
    );
  }

  /**
   * Sliding Window Clause, start end end condition always true.
   */
  @org.junit.Test
  public void slidingWindowExpr510() {
    final XQuery query = new XQuery(
      "\n" +
      "          for sliding window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e when true()\n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1</window><window>2</window><window>3</window><window>4</window><window>5</window><window>6</window><window>7</window><window>8</window><window>9</window><window>10</window>", false)
    );
  }

  /**
   * Sliding Window Clause, start condition always true, fixed sized ends .
   */
  @org.junit.Test
  public void slidingWindowExpr511() {
    final XQuery query = new XQuery(
      "\n" +
      "          for sliding window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e when $e - $s eq 2\n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1 2 3</window><window>2 3 4</window><window>3 4 5</window><window>4 5 6</window><window>5 6 7</window><window>6 7 8</window><window>7 8 9</window><window>8 9 10</window><window>9 10</window><window>10</window>", false)
    );
  }

  /**
   * Sliding Window Clause, positional start and end conditions.
   */
  @org.junit.Test
  public void slidingWindowExpr512() {
    final XQuery query = new XQuery(
      "\n" +
      "          for sliding window $w in (1 to 10)\n" +
      "          start $s at $x when true()\n" +
      "          end $e at $y when $y - $x eq 2\n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1 2 3</window><window>2 3 4</window><window>3 4 5</window><window>4 5 6</window><window>5 6 7</window><window>6 7 8</window><window>7 8 9</window><window>8 9 10</window><window>9 10</window><window>10</window>", false)
    );
  }

  /**
   * Sliding Window Clause, positional start and end conditions with only-end clause.
   */
  @org.junit.Test
  public void slidingWindowExpr513() {
    final XQuery query = new XQuery(
      "\n" +
      "          for sliding window $w in (1 to 10)\n" +
      "          start $s at $x when true()\n" +
      "          only end $e at $y when $y - $x eq 2\n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1 2 3</window><window>2 3 4</window><window>3 4 5</window><window>4 5 6</window><window>5 6 7</window><window>6 7 8</window><window>7 8 9</window><window>8 9 10</window>", false)
    );
  }

  /**
   * Sliding Window Clause, end condition always false with only-end clause.
   */
  @org.junit.Test
  public void slidingWindowExpr514() {
    final XQuery query = new XQuery(
      "\n" +
      "          for sliding window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          only end $e when false() \n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Sliding Window Clause, end condition always false without only-end clause.
   */
  @org.junit.Test
  public void slidingWindowExpr515() {
    final XQuery query = new XQuery(
      "\n" +
      "          for sliding window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e when false() \n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1 2 3 4 5 6 7 8 9 10</window><window>2 3 4 5 6 7 8 9 10</window><window>3 4 5 6 7 8 9 10</window><window>4 5 6 7 8 9 10</window><window>5 6 7 8 9 10</window><window>6 7 8 9 10</window><window>7 8 9 10</window><window>8 9 10</window><window>9 10</window><window>10</window>", false)
    );
  }

  /**
   * Sliding Window Clause, EQNamed window variable.
   */
  @org.junit.Test
  public void slidingWindowExpr517() {
    final XQuery query = new XQuery(
      "\n" +
      "          declare namespace window = \"foo:bar\";\n" +
      "          \n" +
      "          for sliding window $Q{foo:bar}w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e when false() \n" +
      "          return <window>{$window:w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1 2 3 4 5 6 7 8 9 10</window><window>2 3 4 5 6 7 8 9 10</window><window>3 4 5 6 7 8 9 10</window><window>4 5 6 7 8 9 10</window><window>5 6 7 8 9 10</window><window>6 7 8 9 10</window><window>7 8 9 10</window><window>8 9 10</window><window>9 10</window><window>10</window>", false)
    );
  }

  /**
   * Sliding Window Clause, all window variables names are given as EQNames but refered to using QNames.
   */
  @org.junit.Test
  public void slidingWindowExpr518() {
    final XQuery query = new XQuery(
      "\n" +
      "          declare namespace w = \"foo:bar\";\n" +
      "          \n" +
      "          for sliding window $Q{foo:bar}w in (1 to 10)\n" +
      "          start $Q{foo:bar}s at $Q{foo:bar}x previous $Q{foo:bar}sp next $Q{foo:bar}sn when true()\n" +
      "          end $Q{foo:bar}e at $Q{foo:bar}y previous $Q{foo:bar}ep next $Q{foo:bar}en when false() \n" +
      "          return <window>{\n" +
      "            string-join (\n" +
      "              for $w:w in ($w:w, $w:s, $w:x, $w:sp, $w:sn, $w:e, $w:y, $w:ep, $w:en)\n" +
      "              return string($w:w), \" \"\n" +
      "            )}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1 2 3 4 5 6 7 8 9 10 1 1 2 10 10 9</window><window>2 3 4 5 6 7 8 9 10 2 2 1 3 10 10 9</window><window>3 4 5 6 7 8 9 10 3 3 2 4 10 10 9</window><window>4 5 6 7 8 9 10 4 4 3 5 10 10 9</window><window>5 6 7 8 9 10 5 5 4 6 10 10 9</window><window>6 7 8 9 10 6 6 5 7 10 10 9</window><window>7 8 9 10 7 7 6 8 10 10 9</window><window>8 9 10 8 8 7 9 10 10 9</window><window>9 10 9 9 8 10 10 10 9</window><window>10 10 10 9 10 10 9</window>", false)
    );
  }

  /**
   * Sliding Window Clause, window variable not bound in end's when clause.
   */
  @org.junit.Test
  public void slidingWindowExpr519() {
    final XQuery query = new XQuery(
      "\n" +
      "          for sliding window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e when $e - $w eq 2\n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   * Sliding Window Clause, all variables are in scope in the following where clause (and have the correct value).
   */
  @org.junit.Test
  public void slidingWindowExpr529() {
    final XQuery query = new XQuery(
      "\n" +
      "          for sliding window $w in (1 to 10)\n" +
      "          start $s at $x previous $sp next $sn when true()\n" +
      "          end $e at $y previous $ep next $en when false() \n" +
      "          where count($w) eq 10 and $x eq 1 and empty($sp) and $sn eq 2 and $e eq 10 and $y eq 10 and $ep eq 9 and empty($en)\n" +
      "          return true()\n" +
      "        ",
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
   * Sliding Window Clause, overwrite all variables of preceding for clauses.
   */
  @org.junit.Test
  public void slidingWindowExpr534() {
    final XQuery query = new XQuery(
      "\n" +
      "          for $x1 in 11\n" +
      "          for $x2 in 12\n" +
      "          for $x3 in 13\n" +
      "          for $x4 in 14\n" +
      "          for $x5 in 15\n" +
      "          for $x6 in 16\n" +
      "          for $x7 in 17\n" +
      "          for $x8 in 18\n" +
      "          for $x9 in 19\n" +
      "          for sliding window $x1 in (1 to 10)\n" +
      "          start $x2 at $x3 previous $x4 next $x5 when true()\n" +
      "          end $x6 at $x7 previous $x8 next $x9 when false()\n" +
      "          return \n" +
      "            string-join(\n" +
      "              for $i in ($x1, $x2, $x3, $x4, $x5, $x6, $x7, $x8, $x9)\n" +
      "              return string($i), \" \"\n" +
      "            )\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("1 2 3 4 5 6 7 8 9 10 1 1 2 10 10 9 2 3 4 5 6 7 8 9 10 2 2 1 3 10 10 9 3 4 5 6 7 8 9 10 3 3 2 4 10 10 9 4 5 6 7 8 9 10 4 4 3 5 10 10 9 5 6 7 8 9 10 5 5 4 6 10 10 9 6 7 8 9 10 6 6 5 7 10 10 9 7 8 9 10 7 7 6 8 10 10 9 8 9 10 8 8 7 9 10 10 9 9 10 9 9 8 10 10 10 9 10 10 10 9 10 10 9", false)
    );
  }

  /**
   * Sliding Window Clause, enumerate windows using count clause and filter.
   */
  @org.junit.Test
  public void slidingWindowExpr538() {
    final XQuery query = new XQuery(
      "\n" +
      "          for sliding window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e when $e - $s eq 2\n" +
      "          count $r\n" +
      "          where $r le 2\n" +
      "          return <window num=\"{$r}\">{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window num=\"1\">1 2 3</window><window num=\"2\">2 3 4</window>", false)
    );
  }

  /**
   * Sliding Window Clause, count clause preceding window clause.
   */
  @org.junit.Test
  public void slidingWindowExpr539() {
    final XQuery query = new XQuery(
      "\n" +
      "          for $i in 1 to 3\n" +
      "          count $r\n" +
      "          for sliding window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e when $e - $s eq 2\n" +
      "          where $w = $r + 1\n" +
      "          return <window num=\"{$r}\">{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window num=\"1\">1 2 3</window><window num=\"1\">2 3 4</window><window num=\"2\">1 2 3</window><window num=\"2\">2 3 4</window><window num=\"2\">3 4 5</window><window num=\"3\">2 3 4</window><window num=\"3\">3 4 5</window><window num=\"3\">4 5 6</window>", false)
    );
  }

  /**
   * Sliding Window Clause, order by second item in each window.
   */
  @org.junit.Test
  public void slidingWindowExpr540() {
    final XQuery query = new XQuery(
      "\n" +
      "          for sliding window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          only end $e when $e - $s eq 2\n" +
      "          order by $w[2] descending\n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>8 9 10</window><window>7 8 9</window><window>6 7 8</window><window>5 6 7</window><window>4 5 6</window><window>3 4 5</window><window>2 3 4</window><window>1 2 3</window>", false)
    );
  }

  /**
   * Sliding Window Clause, consume each window and order by its components.
   */
  @org.junit.Test
  public void slidingWindowExpr544() {
    final XQuery query = new XQuery(
      "\n" +
      "          for sliding window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          only end $e when $e - $s eq 2\n" +
      "          count $r\n" +
      "          return\n" +
      "            <window num=\"{$r}\">{\n" +
      "              for $i in $w\n" +
      "              order by $i descending\n" +
      "              return $i\n" +
      "            }</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window num=\"1\">3 2 1</window><window num=\"2\">4 3 2</window><window num=\"3\">5 4 3</window><window num=\"4\">6 5 4</window><window num=\"5\">7 6 5</window><window num=\"6\">8 7 6</window><window num=\"7\">9 8 7</window><window num=\"8\">10 9 8</window>", false)
    );
  }

  /**
   * Sliding Window Clause, wrongly typed window variable.
   */
  @org.junit.Test
  public void slidingWindowExpr550() {
    final XQuery query = new XQuery(
      "\n" +
      "          for sliding window $w in (1 to 3)\n" +
      "          start when true()\n" +
      "          end when false()\n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1 2 3</window><window>2 3</window><window>3</window>", false)
    );
  }

  /**
   * Sliding Window Clause, with errors due to having similar variable names : http://www.w3.org/TR/xquery-30/#ERRXQST0089 or http://www.w3.org/TR/xquery-30/#ERR.
   */
  @org.junit.Test
  public void tumblingWindowExpr503() {
    final XQuery query = new XQuery(
      "for tumbling window $w in (1, 2, 3, 4) \n" +
      "      start $s at $s previous $s when fn:true()\n" +
      "      only end $s at $s previous $s when $s - $s eq 1\n" +
      "      return $w",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0103")
    );
  }

  /**
   * Tumbling Window Clause, with errors due to having similar variable names : http://www.w3.org/TR/xquery-30/#ERRXQST0103.
   */
  @org.junit.Test
  public void tumblingWindowExpr504() {
    final XQuery query = new XQuery(
      "for tumbling window $w in (1, 2, 3, 4) \n" +
      "      start $s at $ps previous $pps when fn:true()\n" +
      "      only end $s at $ps previous $pps when $ps - $ps eq 1\n" +
      "      return $w",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0103")
    );
  }

  /**
   * Sliding Window Clause, positional variable is integer : http://www.w3.org/TR/xquery-30/#dt-positional-variable.
   */
  @org.junit.Test
  public void tumblingWindowExpr505() {
    final XQuery query = new XQuery(
      "for tumbling window $w in (1, 2, 3, 4) \n" +
      "      start at $s when fn:true()\n" +
      "      end at $e  when $s - $e eq 1\n" +
      "      return $s",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("$result[1] instance of xs:integer")
    );
  }

  /**
   * Tumbling Window Clause, effect of type declaration: success case.
   */
  @org.junit.Test
  public void tumblingWindowExpr507() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w as xs:integer+ in (1, 2, \"london\", 3, 4, \"paris\")\n" +
      "          start $start when $start instance of xs:integer\n" +
      "          only end next $beyond when $beyond instance of xs:string\n" +
      "          return string-join($w!string(), ' ')\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"1 2\", \"3 4\"")
    );
  }

  /**
   * Tumbling Window Clause, effect of type declaration: failure case.
   */
  @org.junit.Test
  public void tumblingWindowExpr508() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w as xs:integer+ in (1, 2, \"london\", 3, 4.1, \"paris\")\n" +
      "          start $start when $start instance of xs:integer\n" +
      "          only end next $beyond when $beyond instance of xs:string\n" +
      "          return string-join($w!string(), ' ')\n" +
      "        ",
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
   * Tumbling Window Clause, average window size.
   */
  @org.junit.Test
  public void tumblingWindowExpr509() {
    final XQuery query = new XQuery(
      "\n" +
      "          avg(\n" +
      "              for tumbling window $w in (1, 2, \"london\", 3, 4, \"paris\")\n" +
      "              start $start when $start instance of xs:integer\n" +
      "              only end next $beyond when $beyond instance of xs:string\n" +
      "              return count($w)\n" +
      "            )\n" +
      "        ",
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
   * Tumbling Window Clause, start end end condition always true.
   */
  @org.junit.Test
  public void tumblingWindowExpr510() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e when true()\n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1</window><window>2</window><window>3</window><window>4</window><window>5</window><window>6</window><window>7</window><window>8</window><window>9</window><window>10</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, start condition always true, fixed sized ends .
   */
  @org.junit.Test
  public void tumblingWindowExpr511() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e when $e - $s eq 2\n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1 2 3</window><window>4 5 6</window><window>7 8 9</window><window>10</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, positional start and end conditions.
   */
  @org.junit.Test
  public void tumblingWindowExpr512() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s at $x when true()\n" +
      "          end $e at $y when $y - $x eq 2\n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1 2 3</window><window>4 5 6</window><window>7 8 9</window><window>10</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, positional start and end conditions with only-end clause.
   */
  @org.junit.Test
  public void tumblingWindowExpr513() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s at $x when true()\n" +
      "          only end $e at $y when $y - $x eq 2\n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1 2 3</window><window>4 5 6</window><window>7 8 9</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, end condition always false with only-end clause.
   */
  @org.junit.Test
  public void tumblingWindowExpr514() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          only end $e when false() \n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Tumbling Window Clause, end condition always false without only-end clause.
   */
  @org.junit.Test
  public void tumblingWindowExpr515() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e when false() \n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1 2 3 4 5 6 7 8 9 10</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, prefixed window variable.
   */
  @org.junit.Test
  public void tumblingWindowExpr516() {
    final XQuery query = new XQuery(
      "\n" +
      "          declare namespace window = \"foo:bar\";\n" +
      "          \n" +
      "          for tumbling window $window:w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e when false() \n" +
      "          return <window>{$window:w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1 2 3 4 5 6 7 8 9 10</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, EQNamed window variable.
   */
  @org.junit.Test
  public void tumblingWindowExpr517() {
    final XQuery query = new XQuery(
      "\n" +
      "          declare namespace window = \"foo:bar\";\n" +
      "          \n" +
      "          for tumbling window $Q{foo:bar}w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e when false() \n" +
      "          return <window>{$window:w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1 2 3 4 5 6 7 8 9 10</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, all window variables names are given as EQNames but refered to using QNames.
   */
  @org.junit.Test
  public void tumblingWindowExpr518() {
    final XQuery query = new XQuery(
      "\n" +
      "          declare namespace w = \"foo:bar\";\n" +
      "          \n" +
      "          for tumbling window $Q{foo:bar}w in (1 to 10)\n" +
      "          start $Q{foo:bar}s at $Q{foo:bar}x previous $Q{foo:bar}sp next $Q{foo:bar}sn when true()\n" +
      "          end $Q{foo:bar}e at $Q{foo:bar}y previous $Q{foo:bar}ep next $Q{foo:bar}en when false() \n" +
      "          return <window>{\n" +
      "            string-join (\n" +
      "              for $w:w in ($w:w, $w:s, $w:x, $w:sp, $w:sn, $w:e, $w:y, $w:ep, $w:en)\n" +
      "              return string($w:w), \" \"\n" +
      "            )}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1 2 3 4 5 6 7 8 9 10 1 1 2 10 10 9</window>", false)
    );
  }

  /**
   * Variant of TumblingWindow518 to run inside an element constructor.
   *         Tumbling Window Clause, all window variables names are given as EQNames but refered to using QNames.
   */
  @org.junit.Test
  public void tumblingWindowExpr518a() {
    final XQuery query = new XQuery(
      "\n" +
      "          declare namespace w = \"foo:bar\";\n" +
      "          <window>{\n" +
      "          \tfor tumbling window $Q{foo:bar}w in (1 to 10)\n" +
      "          \tstart $Q{foo:bar}s at $Q{foo:bar}x previous $Q{foo:bar}sp next $Q{foo:bar}sn when true()\n" +
      "          \tend $Q{foo:bar}e at $Q{foo:bar}y previous $Q{foo:bar}ep next $Q{foo:bar}en when false() \n" +
      "          \treturn \n" +
      "            \tstring-join (\n" +
      "              \t\tfor $w:w in ($w:w, $w:s, $w:x, $w:sp, $w:sn, $w:e, $w:y, $w:ep, $w:en)\n" +
      "              \t\treturn string($w:w), \" \"\n" +
      "            )\n" +
      "          }</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1 2 3 4 5 6 7 8 9 10 1 1 2 10 10 9</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, window variable not bound in end's when clause.
   */
  @org.junit.Test
  public void tumblingWindowExpr519() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e when $e - $w eq 2\n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   * Tumbling Window Clause, distinct variable name for start variable.
   */
  @org.junit.Test
  public void tumblingWindowExpr520() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $w when true()\n" +
      "          end $e when false() \n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0103")
    );
  }

  /**
   * Tumbling Window Clause, distinc variable name for end variable.
   */
  @org.junit.Test
  public void tumblingWindowExpr521() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $w when false() \n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0103")
    );
  }

  /**
   * Tumbling Window Clause, distinct variable name for position start variable.
   */
  @org.junit.Test
  public void tumblingWindowExpr522() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s at $w when true()\n" +
      "          end $e when false() \n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0103")
    );
  }

  /**
   * Tumbling Window Clause, distinct variable name for position end variable.
   */
  @org.junit.Test
  public void tumblingWindowExpr523() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e at $w when false() \n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0103")
    );
  }

  /**
   * Tumbling Window Clause, distinct variable name for previous start variable.
   */
  @org.junit.Test
  public void tumblingWindowExpr524() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s previous $w when true()\n" +
      "          end $e when false() \n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0103")
    );
  }

  /**
   * Tumbling Window Clause, distinct variable name for next start variable.
   */
  @org.junit.Test
  public void tumblingWindowExpr525() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s next $w when true()\n" +
      "          end $e when false() \n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0103")
    );
  }

  /**
   * Tumbling Window Clause, distinct variable name for previous end variable.
   */
  @org.junit.Test
  public void tumblingWindowExpr526() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e previous $w when false() \n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0103")
    );
  }

  /**
   * Tumbling Window Clause, distinct variable name for next end variable.
   */
  @org.junit.Test
  public void tumblingWindowExpr527() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e next $w when false() \n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0103")
    );
  }

  /**
   * Tumbling Window Clause, distinct start and end variables.
   */
  @org.junit.Test
  public void tumblingWindowExpr528() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $s when false() \n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0103")
    );
  }

  /**
   * Tumbling Window Clause, all variables are in scope in the following where clause (and have the correct value).
   */
  @org.junit.Test
  public void tumblingWindowExpr529() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s at $x previous $sp next $sn when true()\n" +
      "          end $e at $y previous $ep next $en when false() \n" +
      "          where count($w) eq 10 and $x eq 1 and empty($sp) and $sn eq 2 and $e eq 10 and $y eq 10 and $ep eq 9 and empty($en)\n" +
      "          return true()\n" +
      "        ",
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
   * Tumbling Window Clause, all variables are in scope in the return clause (and have the correct values which could be the empty sequence).
   */
  @org.junit.Test
  public void tumblingWindowExpr530() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in ()\n" +
      "          start $s at $x previous $sp next $sn when true()\n" +
      "          end $e at $y previous $ep next $en when false() \n" +
      "          return ($w, $s, $x, $sp, $sn, $e, $y, $ep, $en)\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Tumbling Window Clause, start variable in scope in the start when clause.
   */
  @org.junit.Test
  public void tumblingWindowExpr531() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
      "          start $first when $first mod 3 = 0\n" +
      "          return <window>{ $w }</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>6 8 10</window><window>12 14</window>", false)
    );
  }

  /**
   * Variant of TumblingWindowExpr531 in an element constructor.
   *         Tumbling Window Clause, start variable in scope in the start when clause.
   */
  @org.junit.Test
  public void tumblingWindowExpr531a() {
    final XQuery query = new XQuery(
      "\n" +
      "        <o>{\n" +
      "          for tumbling window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
      "          start $first when $first mod 3 = 0\n" +
      "          return <window>{ $w }</window>\n" +
      "        }</o>  \n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<o><window>6 8 10</window><window>12 14</window></o>", false)
    );
  }

  /**
   * Tumbling Window Clause, overwriting the window variable.
   */
  @org.junit.Test
  public void tumblingWindowExpr532() {
    final XQuery query = new XQuery(
      "\n" +
      "          for $w in (1 to 2)\n" +
      "          for tumbling window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
      "          start $first when $first mod 3 = 0\n" +
      "          return <window>{ $w }</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>6 8 10</window><window>12 14</window><window>6 8 10</window><window>12 14</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, refer to positional variable of preceding for clause.
   */
  @org.junit.Test
  public void tumblingWindowExpr533() {
    final XQuery query = new XQuery(
      "\n" +
      "          for $w at $y in (1 to 2)\n" +
      "          for tumbling window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
      "          start $first when $first mod $y = 0\n" +
      "          return <window>{ $y }</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1</window><window>1</window><window>1</window><window>1</window><window>1</window><window>1</window><window>1</window><window>2</window><window>2</window><window>2</window><window>2</window><window>2</window><window>2</window><window>2</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, overwrite all variables of preceding for clauses.
   */
  @org.junit.Test
  public void tumblingWindowExpr534() {
    final XQuery query = new XQuery(
      "\n" +
      "          for $x1 in 11\n" +
      "          for $x2 in 12\n" +
      "          for $x3 in 13\n" +
      "          for $x4 in 14\n" +
      "          for $x5 in 15\n" +
      "          for $x6 in 16\n" +
      "          for $x7 in 17\n" +
      "          for $x8 in 18\n" +
      "          for $x9 in 19\n" +
      "          for tumbling window $x1 in (1 to 10)\n" +
      "          start $x2 at $x3 previous $x4 next $x5 when true()\n" +
      "          end $x6 at $x7 previous $x8 next $x9 when false()\n" +
      "          return \n" +
      "            string-join(\n" +
      "              for $i in ($x1, $x2, $x3, $x4, $x5, $x6, $x7, $x8, $x9)\n" +
      "              return string($i), \" \"\n" +
      "            )\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("1 2 3 4 5 6 7 8 9 10 1 1 2 10 10 9", false)
    );
  }

  /**
   * Tumbling Window Clause, invalid order of previous and next bindings in start condition.
   */
  @org.junit.Test
  public void tumblingWindowExpr535a() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s next $sn previous $pn when true()\n" +
      "          return <window>{$w}</window>\n" +
      "        ",
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
   * Tumbling Window Clause, invalid order of previous and next bindings in end condition.
   */
  @org.junit.Test
  public void tumblingWindowExpr535b() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          end $e next $en previous $en when true()\n" +
      "          return <window>{$w}</window>\n" +
      "        ",
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
   * Tumbling Window Clause, simple example from the spec.
   */
  @org.junit.Test
  public void tumblingWindowExpr536() {
    final XQuery query = new XQuery(
      "\n" +
      "          let $stock :=\n" +
      "            <stock>\n" +
      "              <closing> <date>2008-01-01</date> <price>105</price> </closing>\n" +
      "              <closing> <date>2008-01-02</date> <price>101</price> </closing>\n" +
      "              <closing> <date>2008-01-03</date> <price>102</price> </closing>\n" +
      "              <closing> <date>2008-01-04</date> <price>103</price> </closing>\n" +
      "              <closing> <date>2008-01-05</date> <price>102</price> </closing>\n" +
      "              <closing> <date>2008-01-06</date> <price>104</price> </closing>\n" +
      "            </stock>\n" +
      "          for tumbling window $w in $stock//closing\n" +
      "             start $first next $second when $first/price < $second/price\n" +
      "             end $last next $beyond when $last/price > $beyond/price\n" +
      "          return\n" +
      "             <run-up>\n" +
      "                <start-date>{fn:data($first/date)}</start-date>\n" +
      "                <start-price>{fn:data($first/price)}</start-price>\n" +
      "                <end-date>{fn:data($last/date)}</end-date>\n" +
      "                <end-price>{fn:data($last/price)}</end-price>\n" +
      "             </run-up>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<run-up><start-date>2008-01-02</start-date><start-price>101</start-price><end-date>2008-01-04</end-date><end-price>103</end-price></run-up><run-up><start-date>2008-01-05</start-date><start-price>102</start-price><end-date>2008-01-06</end-date><end-price>104</end-price></run-up>", false)
    );
  }

  /**
   * Tumbling Window Clause, enumerate windows using count clause.
   */
  @org.junit.Test
  public void tumblingWindowExpr537() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e when $e - $s eq 2\n" +
      "          count $r\n" +
      "          return <window num=\"{$r}\">{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window num=\"1\">1 2 3</window><window num=\"2\">4 5 6</window><window num=\"3\">7 8 9</window><window num=\"4\">10</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, enumerate windows using count clause and filter.
   */
  @org.junit.Test
  public void tumblingWindowExpr538() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e when $e - $s eq 2\n" +
      "          count $r\n" +
      "          where $r le 2\n" +
      "          return <window num=\"{$r}\">{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window num=\"1\">1 2 3</window><window num=\"2\">4 5 6</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, count clause preceding window clause.
   */
  @org.junit.Test
  public void tumblingWindowExpr539() {
    final XQuery query = new XQuery(
      "\n" +
      "          for $i in 1 to 3\n" +
      "          count $r\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e when $e - $s eq 2\n" +
      "          where $w = $r + 1\n" +
      "          return <window num=\"{$r}\">{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window num=\"1\">1 2 3</window><window num=\"2\">1 2 3</window><window num=\"3\">4 5 6</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, order by second item in each window.
   */
  @org.junit.Test
  public void tumblingWindowExpr540() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          only end $e when $e - $s eq 2\n" +
      "          order by $w[2] descending\n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>7 8 9</window><window>4 5 6</window><window>1 2 3</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, undeclared window variable because of nested window clause.
   */
  @org.junit.Test
  public void tumblingWindowExpr541() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w1 in\n" +
      "            for tumbling window $w2 in (1 to 10)\n" +
      "            start $s when true()\n" +
      "            only end $e when $e - $s eq 2\n" +
      "            return $w2\n" +
      "          start $s when true()\n" +
      "          end $e when $e - $s eq 2\n" +
      "          return <window>{$w2}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   * Tumbling Window Clause, nested window clause.
   */
  @org.junit.Test
  public void tumblingWindowExpr542() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w1 in\n" +
      "            for tumbling window $w2 in (1 to 10)\n" +
      "            start $s when true()\n" +
      "            only end $e when $e - $s eq 2\n" +
      "            return $w2\n" +
      "          start $s when true()\n" +
      "          end $e when $e - $s eq 2\n" +
      "          return <window>{$w1}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1 2 3</window><window>4 5 6</window><window>7 8 9</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, consume each window and order by its components.
   */
  @org.junit.Test
  public void tumblingWindowExpr544() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          only end $e when $e - $s eq 2\n" +
      "          count $r\n" +
      "          return\n" +
      "            <window num=\"{$r}\">{\n" +
      "              for $i in $w\n" +
      "              order by $i descending\n" +
      "              return $i\n" +
      "            }</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window num=\"1\">3 2 1</window><window num=\"2\">6 5 4</window><window num=\"3\">9 8 7</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, window clause after group by (query contained in the spec).
   */
  @org.junit.Test
  public void tumblingWindowExpr545() {
    final XQuery query = new XQuery(
      "\n" +
      "          let $s := <stocks>\n" +
      "            <closing> <symbol>ABC</symbol> <date>2008-01-01</date> <price>105</price> </closing>\n" +
      "            <closing> <symbol>DEF</symbol> <date>2008-01-01</date> <price>057</price> </closing>\n" +
      "            <closing> <symbol>ABC</symbol> <date>2008-01-02</date> <price>101</price> </closing>\n" +
      "            <closing> <symbol>DEF</symbol> <date>2008-01-02</date> <price>054</price> </closing>\n" +
      "            <closing> <symbol>ABC</symbol> <date>2008-01-03</date> <price>102</price> </closing>\n" +
      "            <closing> <symbol>DEF</symbol> <date>2008-01-03</date> <price>056</price> </closing>\n" +
      "            <closing> <symbol>ABC</symbol> <date>2008-01-04</date> <price>103</price> </closing>\n" +
      "            <closing> <symbol>DEF</symbol> <date>2008-01-04</date> <price>052</price> </closing>\n" +
      "            <closing> <symbol>ABC</symbol> <date>2008-01-05</date> <price>101</price> </closing>\n" +
      "            <closing> <symbol>DEF</symbol> <date>2008-01-05</date> <price>055</price> </closing>\n" +
      "            <closing> <symbol>ABC</symbol> <date>2008-01-06</date> <price>104</price> </closing>\n" +
      "            <closing> <symbol>DEF</symbol> <date>2008-01-06</date> <price>059</price> </closing>\n" +
      "          </stocks>\n" +
      "          for $closings in $s//closing\n" +
      "          let $symbol := $closings/symbol\n" +
      "          group by $symbol\n" +
      "          for tumbling window $w in $closings\n" +
      "             start $first next $second when $first/price < $second/price\n" +
      "             end $last next $beyond when $last/price > $beyond/price\n" +
      "          return\n" +
      "             <run-up symbol=\"{$symbol}\">\n" +
      "                <start-date>{fn:data($first/date)}</start-date>\n" +
      "                <start-price>{fn:data($first/price)}</start-price>\n" +
      "                <end-date>{fn:data($last/date)}</end-date>\n" +
      "                <end-price>{fn:data($last/price)}</end-price>\n" +
      "             </run-up>\n" +
      "        ",
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
        assertSerialization("<run-up symbol=\"ABC\"><start-date>2008-01-02</start-date><start-price>101</start-price><end-date>2008-01-04</end-date><end-price>103</end-price></run-up><run-up symbol=\"ABC\"><start-date>2008-01-05</start-date><start-price>101</start-price><end-date>2008-01-06</end-date><end-price>104</end-price></run-up><run-up symbol=\"DEF\"><start-date>2008-01-02</start-date><start-price>054</start-price><end-date>2008-01-03</end-date><end-price>056</end-price></run-up><run-up symbol=\"DEF\"><start-date>2008-01-04</start-date><start-price>052</start-price><end-date>2008-01-06</end-date><end-price>059</end-price></run-up>", false)
      ||
        assertSerialization("<run-up symbol=\"DEF\"><start-date>2008-01-02</start-date><start-price>054</start-price><end-date>2008-01-03</end-date><end-price>056</end-price></run-up><run-up symbol=\"DEF\"><start-date>2008-01-04</start-date><start-price>052</start-price><end-date>2008-01-06</end-date><end-price>059</end-price></run-up><run-up symbol=\"ABC\"><start-date>2008-01-02</start-date><start-price>101</start-price><end-date>2008-01-04</end-date><end-price>103</end-price></run-up><run-up symbol=\"ABC\"><start-date>2008-01-05</start-date><start-price>101</start-price><end-date>2008-01-06</end-date><end-price>104</end-price></run-up>", false)
      )
    );
  }

  /**
   * Tumbling Window Clause, windowing flwor used in function called window.
   */
  @org.junit.Test
  public void tumblingWindowExpr546() {
    final XQuery query = new XQuery(
      "\n" +
      "          declare function local:window()\n" +
      "          {\n" +
      "            for tumbling window $w in (1 to 10)\n" +
      "            start $s when true()\n" +
      "            end $e when $e - $s eq 3\n" +
      "            return <window>{$w}</window>\n" +
      "          };\n" +
      "          \n" +
      "          local:window()\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1 2 3 4</window><window>5 6 7 8</window><window>9 10</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, forbidden TypeDeclaration for positional start variable.
   */
  @org.junit.Test
  public void tumblingWindowExpr547() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s at $x as xs:integer when true()\n" +
      "          end $e when $e - $s eq 3\n" +
      "          return <window>{$w}</window>\n" +
      "        ",
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
   * Tumbling Window Clause, forbidden type declaration for next variable declaration.
   */
  @org.junit.Test
  public void tumblingWindowExpr549() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s next $sn as xs:integer when true()\n" +
      "          end $e when $e - $s eq 3\n" +
      "          return <window>{$w}</window>\n" +
      "        ",
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
   * Tumbling Window Clause, wrongly typed window variable.
   */
  @org.junit.Test
  public void tumblingWindowExpr550() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 3)\n" +
      "          start when true()\n" +
      "          end when false()\n" +
      "          return <window>{$w}</window>\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1 2 3</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, checking sequence type of value of each window.
   */
  @org.junit.Test
  public void tumblingWindowExpr551() {
    final XQuery query = new XQuery(
      "\n" +
      "          for tumbling window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e when $e - $s\n" +
      "          return $w instance of xs:integer\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("false false false false false", false)
    );
  }

  /**
   * Tumbling Window Clause, binding sequence coming from a parameter.
   */
  @org.junit.Test
  public void tumblingWindowExpr552() {
    final XQuery query = new XQuery(
      "\n" +
      "          declare function local:window($seq)\n" +
      "          {\n" +
      "            for tumbling window $w in $seq\n" +
      "            start $s when true()\n" +
      "            end $e when $e - $s eq 3\n" +
      "            return\n" +
      "            <window>{\n" +
      "              if ($w instance of xs:integer)\n" +
      "              then\n" +
      "                $w\n" +
      "              else\n" +
      "                $s\n" +
      "            }</window>\n" +
      "          };\n" +
      "          \n" +
      "          local:window(1 to 10)\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1</window><window>5</window><window>9</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, binding sequence coming from a module global variable.
   */
  @org.junit.Test
  public void tumblingWindowExpr553() {
    final XQuery query = new XQuery(
      "\n" +
      "          declare variable $local:foo as xs:integer* := 1 to 10;\n" +
      "          \n" +
      "          declare function local:window()\n" +
      "          {\n" +
      "            for tumbling window $w in $local:foo\n" +
      "            start $s when true()\n" +
      "            end $e when $e - $s eq 3\n" +
      "            return\n" +
      "            <window>{\n" +
      "              if ($w instance of xs:integer)\n" +
      "              then\n" +
      "                $w\n" +
      "              else\n" +
      "                $s\n" +
      "            }</window>\n" +
      "          };\n" +
      "          \n" +
      "          local:window()\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1</window><window>5</window><window>9</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, window over the sequence of a window but both windows used in return.
   */
  @org.junit.Test
  public void tumblingWindowExpr554() {
    final XQuery query = new XQuery(
      "\n" +
      "          declare variable $local:foo as xs:integer* := 1 to 5;\n" +
      "          \n" +
      "          declare function local:window()\n" +
      "          {\n" +
      "            for tumbling window $w1 in $local:foo\n" +
      "            start $s when true()\n" +
      "            end $e when $e - $s eq 3\n" +
      "            for tumbling window $w2 in $w1\n" +
      "            start $s when true()\n" +
      "            end $e when true()\n" +
      "            return\n" +
      "            <window>{\n" +
      "              fn:distinct-values($w1[.=$w2])\n" +
      "            }</window>\n" +
      "          };\n" +
      "          \n" +
      "          local:window()\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<window>1</window><window>2</window><window>3</window><window>4</window><window>5</window>", false)
    );
  }

  /**
   * Tumbling Window Clause, false start and end condition.
   */
  @org.junit.Test
  public void tumblingWindowExpr555() {
    final XQuery query = new XQuery(
      "\n" +
      "          declare variable $local:foo as xs:integer* := 1 to 10;\n" +
      "          \n" +
      "          declare function local:window()\n" +
      "          {\n" +
      "            for tumbling window $w in $local:foo\n" +
      "            start $s when false()\n" +
      "            end $e when false()\n" +
      "            return\n" +
      "              <window>{$w}</window>\n" +
      "          };\n" +
      "          \n" +
      "          local:window()\n" +
      "        ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Window Clause, missing sliding or tumbling.
   */
  @org.junit.Test
  public void windowExpr500() {
    final XQuery query = new XQuery(
      "\n" +
      "          for window $w in (1 to 10)\n" +
      "          start $s when true()\n" +
      "          end $e when false() \n" +
      "          return <window>{$w}</window>\n" +
      "        ",
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
   *  use case 1: Arrange a sequence of items as a table with three columns (using as many rows as necessary). .
   */
  @org.junit.Test
  public void windowingUseCase01() {
    final XQuery query = new XQuery(
      "\n" +
      "<table>{\n" +
      "  for tumbling window $w in ./doc/*\n" +
      "    start at $x when fn:true()\n" +
      "    end at $y when $y - $x = 2\n" +
      "  return\n" +
      "    <tr>{\n" +
      "      for $i in $w\n" +
      "      return\n" +
      "        <td>{data($i)}</td>\n" +
      "    }</tr>\n" +
      "}</table>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/arrange_rows.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<table><tr><td>Green</td><td>Pink</td><td>Lilac</td></tr><tr><td>Turquoise</td><td>Peach</td><td>Opal</td></tr><tr><td>Champagne</td></tr></table>", false)
    );
  }

  /**
   *  use case 2: Convert a structure with implicit sections to a structure with explicit sections. .
   */
  @org.junit.Test
  public void windowingUseCase02() {
    final XQuery query = new XQuery(
      "\n" +
      "<chapter>{\n" +
      "  for tumbling window $w in ./body/*\n" +
      "    start previous $s when $s[self::h2]\n" +
      "    end next $e when $e[self::h2]\n" +
      "  return\n" +
      "    <section title=\"{data($s)}\">{\n" +
      "       for $x in $w\n" +
      "       return\n" +
      "         <para>{data($x)}</para>\n" +
      "  }</section>\n" +
      "}</chapter>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/head_para.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<chapter><section title=\"heading1\"><para>para1</para><para>para2</para></section><section title=\"heading2\"><para>para3</para><para>para4</para><para>para5</para></section></chapter>", false)
    );
  }

  /**
   *  use case 3: Within a glossary in HTML, a defined term <dt> can be followed by a definition <dd>. The task is to group these together within a <term> element, where a group can consist of one or more <dt> elements followed by one or more <dd> elements. .
   */
  @org.junit.Test
  public void windowingUseCase03() {
    final XQuery query = new XQuery(
      "\n" +
      "<doc>{\n" +
      "for tumbling window $w in ./doc/*\n" +
      "  start $x when $x[self::dt]\n" +
      "  end $y next $z when $y[self::dd] and $z[self::dt]\n" +
      "return\n" +
      "  <term>{\n" +
      "    $w\n" +
      "  }</term>\n" +
      "}</doc>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/term_def_list.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<doc><term><dt>XML</dt><dd>Extensible Markup Language</dd></term><term><dt>XSLT</dt><dt>XSL Transformations</dt><dd>A language for transforming XML</dd><dd>A specification produced by W3C</dd></term></doc>", false)
    );
  }

  /**
   *  use case 4: Calculate the moving average of temperature values for the 3 last seconds. .
   */
  @org.junit.Test
  public void windowingUseCase04() {
    final XQuery query = new XQuery(
      "\n" +
      "let $MAX_DIFF := 2\n" +
      "\n" +
      "for sliding window $w in ./stream/event\n" +
      "  start  $s_curr at $s_pos previous $s_prev\n" +
      "    when ($s_curr/@time ne $s_prev/@time) or (empty($s_prev))\n" +
      "  only end next $e_next\n" +
      "    when $e_next/@time - $s_curr/@time gt $MAX_DIFF\n" +
      "return\n" +
      "  avg( $w/@temp )\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/temp_events.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("8 9 17 18", false)
    );
  }

  /**
   *  use case 5: Single exponential smoothing (3 last values and smoothing factor 0.2) .
   */
  @org.junit.Test
  public void windowingUseCase05() {
    final XQuery query = new XQuery(
      "\n" +
      "let $SMOOTH_CONST := 0.2\n" +
      "\n" +
      "for sliding window $w in ./stream/event\n" +
      "  start at $s_pos when true()\n" +
      "  only end at $e_pos when $e_pos - $s_pos eq 2\n" +
      "return\n" +
      "  round-half-to-even($SMOOTH_CONST * data($w[3]/@temp) + (1 - $SMOOTH_CONST) *\n" +
      "    ( $SMOOTH_CONST * data($w[2]/@temp) +\n" +
      "      (1 - $SMOOTH_CONST) * data($w[1]/@temp) ), 2)\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/temp_events.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("8.88 8.68 12.32 15.24 23.92", false)
    );
  }

  /**
   *  use case 6: Detect outliers (current value is two times higher (lower) than the average of the previous three values) in a sequence of temp values.  .
   */
  @org.junit.Test
  public void windowingUseCase06() {
    final XQuery query = new XQuery(
      "\n" +
      "for sliding window $w in ./stream/event\n" +
      "  start  $s_curr when fn:true()\n" +
      "  only end next $next when $next/@time > $s_curr/@time + 3\n" +
      "return\n" +
      "  let $avg := fn:avg($w/@temp)\n" +
      "  where $avg * 2 lt xs:double($next/@temp) or $avg div 2 gt xs:double($next/@temp)\n" +
      "  return <alarm>Outlier detected. Event id:{data($next/@time)}</alarm>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/temp_events.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<alarm>Outlier detected. Event id:5</alarm>", false)
    );
  }

  /**
   *  use case 7: Notify when Barbara enters the building within 1 hour after Anton .
   */
  @org.junit.Test
  public void windowingUseCase07() {
    final XQuery query = new XQuery(
      "\n" +
      "<result>{\n" +
      "  for tumbling window $w in ./stream/event\n" +
      "    start  $s when $s/person eq \"Anton\" and $s/direction eq \"in\"\n" +
      "    only end $e next $n when  xs:dateTime($n/@time) - xs:dateTime($s/@time) gt\n" +
      "      xs:dayTimeDuration(\"PT1H\")\n" +
      "      or  ($e/person eq \"Barbara\" and $e/direction eq \"in\")\n" +
      "      or ($e/person eq \"Anton\" and $e/direction eq \"out\")\n" +
      "  where $e/person eq \"Barbara\" and $e/direction eq \"in\"\n" +
      "  return\n" +
      "    <warning time=\"{ $e/@time }\">Barbara: Anton arrived 1h ago</warning>\n" +
      "}</result>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/person_events.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><warning time=\"2006-01-01T11:00:00-00:00\">Barbara: Anton arrived 1h ago</warning></result>", false)
    );
  }

  /**
   *  use case 8: Measure the working time of each person .
   */
  @org.junit.Test
  public void windowingUseCase08() {
    final XQuery query = new XQuery(
      "\n" +
      "<result>{\n" +
      "for sliding window $w in ./stream/event\n" +
      "  start  $s when $s/direction eq \"in\"\n" +
      "  only end  $e when $s/person eq $e/person and\n" +
      "    $e/direction eq \"out\"\n" +
      "return\n" +
      "  <working-time>\n" +
      "      {$s/person}\n" +
      "      <time>{ xs:dateTime($e/@time) - xs:dateTime($s/@time)}</time>\n" +
      "  </working-time>\n" +
      "}</result>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/person_events.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><working-time><person>Anton</person><time>PT4H30M</time></working-time><working-time><person>Barbara</person><time>PT3H</time></working-time><working-time><person>Clara</person><time>PT1H</time></working-time><working-time><person>Anton</person><time>PT5H</time></working-time><working-time><person>Clara</person><time>PT10M</time></working-time><working-time><person>Clara</person><time>PT5M</time></working-time><working-time><person>Clara</person><time>PT15M</time></working-time><working-time><person>Clara</person><time>PT2H15M</time></working-time></result>", false)
    );
  }

  /**
   *  use case 9: Measure the overall working time for each person. .
   */
  @org.junit.Test
  public void windowingUseCase09() {
    final XQuery query = new XQuery(
      "\n" +
      "<result>{\n" +
      "for sliding window $w in ./stream/event\n" +
      "  start  $s when $s/direction eq \"in\"\n" +
      "  only end  $e when $s/person eq $e/person and\n" +
      "    $e/direction eq \"out\"\n" +
      "let $person := $s/person\n" +
      "let $workingTime := xs:dateTime($e/@time) - xs:dateTime($s/@time)\n" +
      "group by $person\n" +
      "order by $person\n" +
      "return\n" +
      "  <working-time>\n" +
      "    <person>{ $person }</person>\n" +
      "    <time>{ sum($workingTime) }</time>\n" +
      "  </working-time>\n" +
      "}</result>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/person_events.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><working-time><person>Anton</person><time>PT9H30M</time></working-time><working-time><person>Barbara</person><time>PT3H</time></working-time><working-time><person>Clara</person><time>PT3H45M</time></working-time></result>", false)
    );
  }

  /**
   *  use case 10: Display a warning if Barbara does not come to work. .
   */
  @org.junit.Test
  public void windowingUseCase10() {
    final XQuery query = new XQuery(
      "\n" +
      "<result>{\n" +
      "  for tumbling window $w in ./stream/event[direction eq \"in\"]\n" +
      "    start  $s when fn:true()\n" +
      "    end next $e when xs:date( xs:dateTime($s/@time) ) ne xs:date( xs:dateTime($e/@time) )\n" +
      "  let $date := xs:date(xs:dateTime($s/@time))\n" +
      "  where not($w[person eq \"Barbara\"])\n" +
      "  return <alert date=\"{ $date }\">Barbara did not come to work</alert>\n" +
      "}</result>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/person_events.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><alert date=\"2006-01-02Z\">Barbara did not come to work</alert></result>", false)
    );
  }

  /**
   *  use case 11: Identify every person who enters the building before Clara withing a 15 minute timeframe (Clara's arrival time - 15 minutes). .
   */
  @org.junit.Test
  public void windowingUseCase11() {
    final XQuery query = new XQuery(
      "\n" +
      "<results>{\n" +
      "  for tumbling window $w in ./stream/event[direction eq \"in\"]\n" +
      "    start when true()\n" +
      "    only end next $x when  $x/person eq \"Clara\"\n" +
      "  return\n" +
      "    <result time=\"{ $x/@time }\">{\n" +
      "      distinct-values(for $y in $w\n" +
      "        where (xs:dateTime($y/@time) + xs:dayTimeDuration(\"PT15M\") ) ge xs:dateTime($x/@time)\n" +
      "        return $y/person)\n" +
      "    }</result>\n" +
      "}</results>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/person_events.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results><result time=\"2006-01-01T11:15:00-00:00\">Barbara</result><result time=\"2006-01-02T12:00:00-00:00\"/><result time=\"2006-01-02T12:15:00-00:00\">Clara</result><result time=\"2006-01-02T12:25:00-00:00\">Clara</result><result time=\"2006-01-02T14:00:00-00:00\"/></results>", false)
    );
  }

  /**
   *  use case 12: Notify when both Anton and Barbara enter the office within 30 minutes of one another..
   */
  @org.junit.Test
  public void windowingUseCase12() {
    final XQuery query = new XQuery(
      "\n" +
      "<result>{\n" +
      "        for tumbling window $w in ./stream/event[direction eq \"in\"]\n" +
      "                start  $x when $x/person = (\"Barbara\", \"Anton\")\n" +
      "                end next $y when xs:dateTime($y/@time) - xs:dateTime($x/@time) gt xs:dayTimeDuration(\"PT30M\")\n" +
      "        where $w[person eq \"Anton\"] and $w[person eq \"Barbara\"]\n" +
      "        return\n" +
      "                <alert time=\"{ xs:dateTime($y/@time) }\">Anton and Barbara just arrived</alert>\n" +
      "}</result>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/person_events.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><alert time=\"2006-01-01T11:15:00Z\">Anton and Barbara just arrived</alert></result>", false)
    );
  }

  /**
   *  use case 13: Inform when a person enters the building at least 3 times within 1 hour .
   */
  @org.junit.Test
  public void windowingUseCase13() {
    final XQuery query = new XQuery(
      "\n" +
      "<result>{\n" +
      "  for sliding window $w in ./stream/event\n" +
      "    start  $s when true()\n" +
      "    end next $e when xs:dateTime($e/@time) - xs:dateTime($s/@time) gt\n" +
      "      xs:dayTimeDuration(\"PT1H\")\n" +
      "  where count($w[person eq $s/person and direction eq \"in\"]) ge 3\n" +
      "  return\n" +
      "    <alert time=\"{ $e/@time }\">{fn:data($s/person)} is suspicious</alert>\n" +
      "}</result>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/person_events.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><alert time=\"2006-01-02T14:00:00-00:00\">Clara is suspicious</alert></result>", false)
    );
  }

  /**
   *  use case 14: Find all annoying authors who have posted three consecutive items in the RSS feed. .
   */
  @org.junit.Test
  public void windowingUseCase14() {
    final XQuery query = new XQuery(
      "\n" +
      "<result>{\n" +
      "  for tumbling window $w in ./rss/channel/item\n" +
      "    start  $first when fn:true()\n" +
      "    end next $lookAhead when $first/author ne $lookAhead/author\n" +
      "  where count($w) ge 3\n" +
      "  return <annoying-author>{\n" +
      "      $w[1]/author\n" +
      "    }</annoying-author>\n" +
      "}</result>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/rss.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><annoying-author><author>rokas@e-mail.de</author></annoying-author></result>", false)
    );
  }

  /**
   *  use case 15: Every day, provide a list of interesting topics in the RSS feed. In our example, interesting means that the title of the item contains the specific word XQuery. .
   */
  @org.junit.Test
  public void windowingUseCase15() {
    final XQuery query = new XQuery(
      "\n" +
      "<result>{\n" +
      "  for tumbling window $w in ./rss/channel/item\n" +
      "    start  $s_curr when true()\n" +
      "    end next $e_next when\n" +
      "      fn:day-from-dateTime(xs:dateTime($e_next/pubDate)) ne\n" +
      "      fn:day-from-dateTime(xs:dateTime($s_curr/pubDate))\n" +
      "  return\n" +
      "    <item>\n" +
      "        <date>{xs:date(xs:dateTime($s_curr/pubDate))}</date>\n" +
      "        {  for $item in $w\n" +
      "                   where fn:contains( xs:string($item/title), 'XQuery')\n" +
      "                   return $item/title   }\n" +
      "      </item>\n" +
      "}</result>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/rss.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><item><date>2003-06-03</date><title>Extending XQuery with Window Functions</title><title>XQueryP: A new programming language is born</title></item><item><date>2003-06-04</date></item></result>", false)
    );
  }

  /**
   *  use case 16: Every day, provide a summary of the RSS feed grouped by author. .
   */
  @org.junit.Test
  public void windowingUseCase16() {
    final XQuery query = new XQuery(
      "\n" +
      "<result>{\n" +
      "  for tumbling window $w in ./rss/channel/item\n" +
      "    start  $s_curr when true()\n" +
      "    end next $e_next when\n" +
      "      fn:day-from-dateTime(xs:dateTime($e_next/pubDate)) ne\n" +
      "      fn:day-from-dateTime(xs:dateTime($s_curr/pubDate))\n" +
      "  return\n" +
      "    <item>\n" +
      "      <date>{xs:date(xs:dateTime($s_curr/pubDate))}</date>\n" +
      "       {  for $a in fn:distinct-values($w/author)\n" +
      "           return\n" +
      "             <author name=\"{$a}\">\n" +
      "               <titles>\n" +
      "                 { $w[author eq $a]/title }\n" +
      "               </titles>\n" +
      "             </author>\n" +
      "            }\n" +
      "          </item>\n" +
      "}</result>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/rss.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><item><date>2003-06-03</date><author name=\"rokas@e-mail.de\"><titles><title>Why use cases are important Part 1.</title><title>Why use cases are important Part 2.</title><title>Why use cases are important Part 3.</title></titles></author><author name=\"tim@e-mail.de\"><titles><title>Extending XQuery with Window Functions</title></titles></author><author name=\"david@e-mail.de\"><titles><title>XQueryP: A new programming language is born</title></titles></author></item><item><date>2003-06-04</date><author name=\"rokas@e-mail.de\"><titles><title>Why use cases are annoying to write.</title></titles></author></item></result>", false)
    );
  }

  /**
   *  use case 17: At the end of a day, list the most valuable customers. .
   */
  @org.junit.Test
  public void windowingUseCase17() {
    final XQuery query = new XQuery(
      "\n" +
      "<result>{\n" +
      "for sliding window $w in ./sequence/*\n" +
      "  start  $cur previous $prev\n" +
      "   when day-from-dateTime($cur/@date) ne day-from-dateTime($prev/@date) or empty($prev)\n" +
      "  end $end next $next\n" +
      "   when day-from-dateTime(xs:dateTime($end/@date)) ne\n" +
      "day-from-dateTime(xs:dateTime($next/@date))\n" +
      "return\n" +
      "  <mostValuableCustomer endOfDay=\"{xs:dateTime($cur/@date)}\">{\n" +
      "    let $companies :=   for $x in distinct-values($w/@billTo )\n" +
      "                        return <amount company=\"{$x}\">{sum($w[@billTo eq $x]/@total)}</amount>\n" +
      "    let $max := max($companies)\n" +
      "    for $company in $companies\n" +
      "    where $company eq xs:untypedAtomic($max)\n" +
      "    return $company\n" +
      "  }</mostValuableCustomer>\n" +
      "}</result>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/cxml.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><mostValuableCustomer endOfDay=\"2006-01-01T00:00:00Z\"><amount company=\"ACME1\">1100</amount></mostValuableCustomer><mostValuableCustomer endOfDay=\"2006-01-02T00:00:00Z\"><amount company=\"ACME1\">10000</amount></mostValuableCustomer><mostValuableCustomer endOfDay=\"2006-01-03T00:00:00Z\"/><mostValuableCustomer endOfDay=\"2006-01-04T00:00:00Z\"/><mostValuableCustomer endOfDay=\"2006-01-05T00:00:00Z\"/><mostValuableCustomer endOfDay=\"2006-01-06T00:00:00Z\"><amount company=\"ACME2\">100</amount></mostValuableCustomer><mostValuableCustomer endOfDay=\"2006-01-07T00:00:00Z\"/></result>", false)
    );
  }

  /**
   *  use case 18: Calculate the time needed to process an order from the request up to the shipping. .
   */
  @org.junit.Test
  public void windowingUseCase18() {
    final XQuery query = new XQuery(
      "\n" +
      "<result>{\n" +
      "  for sliding window $w in ./sequence/*\n" +
      "    start $s when $s[self::OrderRequest]\n" +
      "    end   $e when $e/@orderID eq  $s/@orderID\n" +
      "             and ($e[self::ConfirmationRequest] and $e/@status eq \"reject\"\n" +
      "                  or $e[self::ShipNotice])\n" +
      "  where $e[self::ShipNotice]\n" +
      "  return\n" +
      "    <timeToShip orderID=\"{ $s/@orderID}\">{xs:dateTime($e/@date) - xs:dateTime($s/@date) }</timeToShip>\n" +
      "}</result>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/cxml.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><timeToShip orderID=\"OID01\">P3DT22H</timeToShip><timeToShip orderID=\"OID03\">P2DT19H</timeToShip></result>", false)
    );
  }

  /**
   *  use case 19: At the moment of the shipping notification, calculate if an open request exists that can be shipped to the same address. .
   */
  @org.junit.Test
  public void windowingUseCase19() {
    final XQuery query = new XQuery(
      "\n" +
      "<result>{\n" +
      "  for sliding window $w in ./sequence/*\n" +
      "    start previous $wSPrev when $wSPrev[self::OrderRequest]\n" +
      "    end next $wENext when $wENext/@orderID eq  $wSPrev/@orderID\n" +
      "        and ($wENext[self::ConfirmationRequest] and $wENext/@status eq \"reject\"\n" +
      "                 or $wENext[self::ShipNotice])\n" +
      "  where $wENext[self::ShipNotice]\n" +
      "  return\n" +
      "    <bundleWith orderId=\"{$wSPrev/@orderID}\">{\n" +
      "        for sliding window $bundle in $w\n" +
      "          start  $bSCur\n" +
      "            when $bSCur[self::OrderRequest] and $bSCur/@shipTo eq $wSPrev/@shipTo\n" +
      "          end  $bECur next $bENext\n" +
      "            when $bECur/@orderID eq  $bSCur/@orderID\n" +
      "             and ($bECur[self::ConfirmationRequest] and $bECur/@status eq \"reject\"\n" +
      "              or $bECur[self::ShipNotice])\n" +
      "          where empty($bENext)\n" +
      "          return $bSCur\n" +
      "    }</bundleWith>\n" +
      "}</result>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/WindowClause/cxml.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result><bundleWith orderId=\"OID01\"><OrderRequest billTo=\"ACME1\" date=\"2006-01-02T14:00:00-00:00\" orderID=\"OID03\" shipTo=\"ACME1\" total=\"10000\" type=\"new\">\n    <Item partID=\"ID3\" quantity=\"100\" unitPrice=\"100\"/>\n  </OrderRequest></bundleWith><bundleWith orderId=\"OID03\"/></result>", false)
    );
  }
}
