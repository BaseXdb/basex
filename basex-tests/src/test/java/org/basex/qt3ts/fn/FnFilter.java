package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the higher-order fn:filter function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnFilter extends QT3TestSet {

  /**
   * Basic test using starts-with().
   */
  @org.junit.Test
  public void filter001() {
    final XQuery query = new XQuery(
      "filter(starts-with(?, \"a\"), (\"apple\", \"pear\", \"apricot\", \"advocado\", \"orange\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"apple\", \"apricot\", \"advocado\"")
    );
  }

  /**
   * Test using an inline user-defined function.
   */
  @org.junit.Test
  public void filter002() {
    final XQuery query = new XQuery(
      "filter(function($x){$x gt 10}, (12, 4, 46, 23, -8))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("12, 46, 23")
    );
  }

  /**
   * Test using an inline user-defined function.
   */
  @org.junit.Test
  public void filter003() {
    final XQuery query = new XQuery(
      "let $data := (/employees)\n" +
      "              return filter(function($x as element(emp)){xs:int($x/@salary) lt 300}, $data/emp)",
      ctx);
    try {
      query.context(node(file("fn/filter/filter003.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertCount(3)
      &&
        assertType("element(emp)*")
      &&
        assertQuery("$result/@name = 'john'")
      &&
        assertQuery("$result/@name = 'anne'")
      &&
        assertQuery("$result/@name = 'kumar'")
      )
    );
  }

  /**
   * Test using an inline user-defined function.
   */
  @org.junit.Test
  public void filter004() {
    final XQuery query = new XQuery(
      "(1 to 20)[. = filter(function($x){$x idiv 2 * 2 = $x}, 1 to position())]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("2, 4, 6, 8, 10, 12, 14, 16, 18, 20")
    );
  }

  /**
   * implement eg:index-of-node().
   */
  @org.junit.Test
  public void filter005() {
    final XQuery query = new XQuery(
      "let $index-of-node := function($seqParam as node()*, $srchParam as node()) as xs:integer* \n" +
      "                                    { filter( function($this as xs:integer) as xs:boolean \n" +
      "                                              {$seqParam[$this] is $srchParam}, 1 to count($seqParam) ) },\n" +
      "            $nodes := /*/*,\n" +
      "            $perm := ($nodes[1], $nodes[2], $nodes[3], $nodes[1], $nodes[2], $nodes[4], $nodes[2], $nodes[1]) \n" +
      "            return $index-of-node($perm, $nodes[2]) ",
      ctx);
    try {
      query.context(node(file("fn/filter/filter005.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("2, 5, 7")
    );
  }

  /**
   * filter function - not a boolean.
   */
  @org.junit.Test
  public void filter901() {
    final XQuery query = new XQuery(
      "filter(normalize-space#1, (\"apple\", \"pear\", \"apricot\", \"advocado\", \"orange\"))",
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
   * filter function - returns empty sequence .
   */
  @org.junit.Test
  public void filter902() {
    final XQuery query = new XQuery(
      "filter(function($x){if(starts-with($x,'a')) then true() else ()}, (\"apple\", \"pear\", \"apricot\", \"advocado\", \"orange\"))",
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
   * filter function - returns non-singleton sequence.
   */
  @org.junit.Test
  public void filter903() {
    final XQuery query = new XQuery(
      "filter(function($x){if(starts-with($x,'a')) then (true(), true()) else false()}, (\"apple\", \"pear\", \"apricot\", \"advocado\", \"orange\"))",
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
   * filter function - item in sequence is of wrong type.
   */
  @org.junit.Test
  public void filter904() {
    final XQuery query = new XQuery(
      "filter(ends-with(?, 'e'), (\"apple\", \"pear\", \"apricot\", \"advocado\", \"orange\", current-date()))",
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
   * Attempts to evaluate the "filter" function with no arguments..
   */
  @org.junit.Test
  public void fnFilter001() {
    final XQuery query = new XQuery(
      "fn:filter()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to reference the "filter" function with arity zero..
   */
  @org.junit.Test
  public void fnFilter002() {
    final XQuery query = new XQuery(
      "fn:filter#0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to evaluate the "filter" function with one argument..
   */
  @org.junit.Test
  public void fnFilter003() {
    final XQuery query = new XQuery(
      "fn:filter( fn:boolean#1 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to reference the "filter" function with arity one..
   */
  @org.junit.Test
  public void fnFilter004() {
    final XQuery query = new XQuery(
      "fn:filter#1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to reference the "filter" function with arity two..
   */
  @org.junit.Test
  public void fnFilter005() {
    final XQuery query = new XQuery(
      "fn:exists( fn:filter#2 )",
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
   * Tests the type checking of the $f argument..
   */
  @org.junit.Test
  public void fnFilter010() {
    final XQuery query = new XQuery(
      "fn:filter( function($a as item()) as xs:boolean* { fn:boolean($a), fn:boolean($a) }, () )",
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
        error("XPST0005")
      ||
        assertEmpty()
      )
    );
  }

  /**
   * Tests the type checking of the $f argument..
   */
  @org.junit.Test
  public void fnFilter011() {
    final XQuery query = new XQuery(
      "fn:filter( function($a as item()) as xs:boolean? { () }, () )",
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
        error("XPST0005")
      ||
        assertEmpty()
      )
    );
  }

  /**
   * Tests the type checking of the $f argument..
   */
  @org.junit.Test
  public void fnFilter012() {
    final XQuery query = new XQuery(
      "fn:filter( fn:string#1, () )",
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
        error("XPTY0004")
      ||
        error("XPST0005")
      ||
        assertEmpty()
      )
    );
  }

  /**
   * Evaluates the "filter" function with $seq set to a mix of item types .
   */
  @org.junit.Test
  public void fnFilter013() {
    final XQuery query = new XQuery(
      "fn:filter( function($arg) { $arg instance of function(*) }, (//node(), 1, \"string\", 3.14, 2.7e0, fn:exists#1) )",
      ctx);
    try {
      query.context(node(file("fn/filter/fn-filter-012.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(1)
    );
  }

  /**
   * Evaluates the "filter" function with $f set to a function which _could_ (but doesn't) return a non-boolean value .
   */
  @org.junit.Test
  public void fnFilter014() {
    final XQuery query = new XQuery(
      "fn:filter( function($arg) { if ($arg eq 100) then () else fn:true()}, 1 to 10)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(10)
    );
  }

  /**
   * Evaluates the "filter" function with $f set to a function which _could_ (and does) return a non-boolean value .
   */
  @org.junit.Test
  public void fnFilter015() {
    final XQuery query = new XQuery(
      "fn:filter( function($arg) { if ($arg eq 10) then () else fn:true()}, 1 to 10)",
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
   * Evaluates the "filter" function with $f set to a function which _could_ (but doesn't) return a non-boolean value .
   */
  @org.junit.Test
  public void fnFilter017() {
    final XQuery query = new XQuery(
      "fn:filter( function($arg) { if ($arg eq 100) then 0 else fn:true()}, 1 to 10)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(10)
    );
  }

  /**
   * Evaluates the "filter" function with $f set to a function which _could_ (and does) return a non-boolean value .
   */
  @org.junit.Test
  public void fnFilter018() {
    final XQuery query = new XQuery(
      "fn:filter( function($arg) { if ($arg eq 10) then 0 else fn:true()}, 1 to 10)",
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
   * Evaluates the "filter" function with $f set to a function which _could_ (but doesn't) return a non-boolean value .
   */
  @org.junit.Test
  public void fnFilter020() {
    final XQuery query = new XQuery(
      "fn:filter( function($arg) { if ($arg eq 100) then (fn:true(), fn:false()) else fn:true()}, 1 to 10)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(10)
    );
  }

  /**
   * Evaluates the "filter" function with $f set to a function which _could_ (and does) return a non-boolean value .
   */
  @org.junit.Test
  public void fnFilter021() {
    final XQuery query = new XQuery(
      "fn:filter( function($arg) { if ($arg eq 10) then (fn:true(), fn:false()) else fn:true()}, 1 to 10)",
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
}
