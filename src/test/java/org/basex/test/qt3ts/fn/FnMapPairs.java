package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests the fn:map-pairs() function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnMapPairs extends QT3TestSet {

  /**
   * Attempts to evaluate the "map-pairs" function with no arguments..
   */
  @org.junit.Test
  public void fnMapPairs001() {
    final XQuery query = new XQuery(
      "fn:map-pairs()",
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
   * Attempts to reference the "map-pairs" function with arity zero..
   */
  @org.junit.Test
  public void fnMapPairs002() {
    final XQuery query = new XQuery(
      "fn:map-pairs#0",
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
   * Attempts to evaluate the "map-pairs" function with one argument..
   */
  @org.junit.Test
  public void fnMapPairs003() {
    final XQuery query = new XQuery(
      "fn:map-pairs( fn:concat#2 )",
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
   * Attempts to reference the "map-pairs" function with arity one..
   */
  @org.junit.Test
  public void fnMapPairs004() {
    final XQuery query = new XQuery(
      "fn:map-pairs#1",
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
   * Attempts to evaluate the "map-pairs" function with two arguments..
   */
  @org.junit.Test
  public void fnMapPairs005() {
    final XQuery query = new XQuery(
      "fn:map-pairs( fn:concat#2, () )",
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
   * Attempts to reference the "map-pairs" function with arity one..
   */
  @org.junit.Test
  public void fnMapPairs006() {
    final XQuery query = new XQuery(
      "fn:map-pairs#2",
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
   * Attempts to evaluate the "map-pairs" function with three arguments..
   */
  @org.junit.Test
  public void fnMapPairs007() {
    final XQuery query = new XQuery(
      "fn:map-pairs( fn:concat#2, (), () )",
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
        assertEmpty()
      ||
        error("XPST0005")
      )
    );
  }

  /**
   * Attempts to reference the "map-pairs" function with arity three..
   */
  @org.junit.Test
  public void fnMapPairs008() {
    final XQuery query = new XQuery(
      "fn:exists( fn:map-pairs#3 )",
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
  public void fnMapPairs009() {
    final XQuery query = new XQuery(
      "( fn:map-pairs( if ( fn:current-dateTime() eq\n" +
      "                                 fn:dateTime( fn:current-date(),\n" +
      "                                              fn:current-time() ))\n" +
      "                            then fn:concat#2 \n" +
      "                            else (),\n" +
      "                            (), () ),\n" +
      "              fn:map-pairs( if ( fn:current-dateTime() eq\n" +
      "                                fn:dateTime( fn:current-date(),\n" +
      "                                             fn:current-time() ))\n" +
      "                            then () \n" +
      "                            else fn:concat#2,\n" +
      "                            (), () ) )",
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
   * Tests the type checking of the $f argument..
   */
  @org.junit.Test
  public void fnMapPairs011() {
    final XQuery query = new XQuery(
      "fn:map-pairs( (fn:concat#2, fn:concat#2), (), () )",
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
   * Tests the type checking of the $f argument..
   */
  @org.junit.Test
  public void fnMapPairs012() {
    final XQuery query = new XQuery(
      "fn:map-pairs( fn:true(), (), () )",
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
   * Tests the type checking of the $f argument..
   */
  @org.junit.Test
  public void fnMapPairs013() {
    final XQuery query = new XQuery(
      " fn:map-pairs( /root, (), () )",
      ctx);
    try {
      query.context(node(file("fn/map-pairs/fn-map-pairs-013.xml")));
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
   * Tests the type checking of the $f argument..
   */
  @org.junit.Test
  public void fnMapPairs014() {
    final XQuery query = new XQuery(
      "fn:map-pairs( fn:boolean#1, (), () )",
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
   * Tests the type checking of the $f argument..
   */
  @org.junit.Test
  public void fnMapPairs015() {
    final XQuery query = new XQuery(
      "fn:map-pairs( fn:concat#3, (), () )",
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
   * Tests the type checking of the $f argument..
   */
  @org.junit.Test
  public void fnMapPairs016() {
    final XQuery query = new XQuery(
      "fn:map-pairs( function($a as item(), $b as item()) as item()* { fn:boolean($a), fn:boolean($b) }, (), () )",
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
   * Evaluates the "map-pairs" function with $seq1, $seq2 set to a mix of item types .
   */
  @org.junit.Test
  public void fnMapPairs017() {
    final XQuery query = new XQuery(
      "fn:map-pairs( function($a, $b) \n" +
      "                          { if ($a instance of function(*))\n" +
      "                            then $b instance of function(*)\n" +
      "                            else if ($b instance of function(*))\n" +
      "                            then false()\n" +
      "                            else deep-equal($a, $b)\n" +
      "                          },\n" +
      "                          (//node(), 1, \"string\", 3.14, 2.7e0, fn:exists#1),\n" +
      "                          (//node(), 1, \"string\", 3.14, 2.7e0, fn:exists#1) )",
      ctx);
    try {
      query.context(node(file("fn/map-pairs/fn-map-pairs-013.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true true true true true true true true true true true true true true")
    );
  }

  /**
   * Evaluates the "map-pairs" function with empty $seq1 and error in $seq2 .
   */
  @org.junit.Test
  public void fnMapPairs018() {
    final XQuery query = new XQuery(
      "fn:map-pairs( concat#2, (), fn:error())",
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
        assertEmpty()
      ||
        error("FOER0000")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   * Evaluates the "map-pairs" function with empty $seq2 and error in $seq1 .
   */
  @org.junit.Test
  public void fnMapPairs019() {
    final XQuery query = new XQuery(
      "fn:map-pairs( concat#2, fn:error(), ())",
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
        assertEmpty()
      ||
        error("FOER0000")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   * Evaluates the "map-pairs" function with a function which raises an error, $seq1 and $seq2 empty..
   */
  @org.junit.Test
  public void fnMapPairs020() {
    final XQuery query = new XQuery(
      "fn:map-pairs( function($arg, $arg2) { fn:error() }, (), ())",
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
        assertEmpty()
      ||
        error("XPST0005")
      )
    );
  }

  /**
   * Evaluates the "map-pairs" function with a function which raises an error.
   */
  @org.junit.Test
  public void fnMapPairs021() {
    final XQuery query = new XQuery(
      "fn:map-pairs( function($arg, $arg2) { fn:error() }, 1 to 10, 1 to 10)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   * Evaluates the "map-pairs" function with a function which returns the empty sequence, $seq1 and $seq2 raise errors.
   */
  @org.junit.Test
  public void fnMapPairs022() {
    final XQuery query = new XQuery(
      "fn:map-pairs( function($arg1, $arg2) { () }, fn:error(), fn:error())",
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
        assertEmpty()
      ||
        error("XPST0005")
      ||
        error("FOER0000")
      )
    );
  }

  /**
   * Evaluates the "map-pairs" function with a function which returns the empty sequence, $seq1 and $seq2 raise errors.
   */
  @org.junit.Test
  public void fnMapPairs023() {
    final XQuery query = new XQuery(
      "fn:map-pairs( function($arg1, $arg2) { ($arg1, $arg2) }, (1, fn:error()), 1)",
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
        assertStringValue(false, "1")
      ||
        error("FOER0000")
      )
    );
  }

  /**
   * Evaluates the "map-pairs" function with a function which returns the empty sequence, $seq1 and $seq2 raise errors.
   */
  @org.junit.Test
  public void fnMapPairs024() {
    final XQuery query = new XQuery(
      "fn:map-pairs( function($arg1, $arg2) { ($arg1, $arg2) }, 1, (1, fn:error()))",
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
        assertStringValue(false, "1 1")
      ||
        error("FOER0000")
      )
    );
  }

  /**
   * Evaluates the "map-pairs" function with a function which returns exactly one item per call.
   */
  @org.junit.Test
  public void fnMapPairs025() {
    final XQuery query = new XQuery(
      "fn:map-pairs( function($arg1, $arg2) { ($arg1 + $arg2) }, 1 to 3, 1 to 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2 4 6")
    );
  }

  /**
   * Evaluates the "map-pairs" function with a function which returns zero or one item per call.
   */
  @org.junit.Test
  public void fnMapPairs026() {
    final XQuery query = new XQuery(
      "fn:map-pairs( function($arg1, $arg2) { if ($arg1) then $arg2 else () }, (true(), false(), true()), 1 to 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 3")
    );
  }

  /**
   * Evaluates the "map-pairs" function with a function which returns more than one item per call.
   */
  @org.junit.Test
  public void fnMapPairs027() {
    final XQuery query = new XQuery(
      "fn:map-pairs( function($arg1, $arg2) { ($arg1, $arg2) }, 1 to 3, 1 to 3)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 1 2 2 3 3")
    );
  }

  /**
   * Evaluates the "map-pairs" function with $f set to a function which expects integers, but is supplied with strings..
   */
  @org.junit.Test
  public void fnMapPairs028() {
    final XQuery query = new XQuery(
      "fn:map-pairs( function($arg1 as xs:integer, $arg2 as xs:integer) { $arg1 + $arg2 }, (\"1\", \"2\"), (1, 2) )",
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
   * Evaluates the "map-pairs" function with $f set to a function which expects integers, but is supplied with strings..
   */
  @org.junit.Test
  public void fnMapPairs029() {
    final XQuery query = new XQuery(
      "fn:map-pairs( function($arg1 as xs:integer, $arg2 as xs:integer) { $arg1 + $arg2 }, (1, 2), (\"1\", \"2\") )",
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
   * Evaluates the "map-pairs" function with $f set to a function which expects integers, but is supplied with strings..
   */
  @org.junit.Test
  public void fnMapPairs030() {
    final XQuery query = new XQuery(
      "fn:map-pairs( function($arg1 as xs:integer, $arg2 as xs:integer) { $arg1 + $arg2 }, (\"1\", \"2\"), (\"1\", \"2\") )",
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
   * Apply deep-equal to corresponding pairs.
   */
  @org.junit.Test
  public void mapPairs001() {
    final XQuery query = new XQuery(
      "map-pairs(deep-equal#2, (\"aa\", \"bb\", \"cc\", \"dd\", \"ee\"), (\"AA\", \"BB\", \"cc\", \"dd\", \"EE\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("false(), false(), true(), true(), false()")
    );
  }

  /**
   * Apply deep-equal to corresponding pairs.
   */
  @org.junit.Test
  public void mapPairs002() {
    final XQuery query = new XQuery(
      "map-pairs(deep-equal#2, (\"aa\", \"bb\", \"cc\", \"dd\", \"ee\", \"ff\"), (\"AA\", \"BB\", \"cc\", \"dd\", \"EE\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("false(), false(), true(), true(), false()")
    );
  }

  /**
   * Apply deep-equal to corresponding pairs.
   */
  @org.junit.Test
  public void mapPairs003() {
    final XQuery query = new XQuery(
      "map-pairs(deep-equal#2, (\"aa\", \"bb\", \"cc\", \"dd\", \"ee\"), (\"AA\", \"BB\", \"cc\", \"dd\", \"EE\", \"ff\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("false(), false(), true(), true(), false()")
    );
  }

  /**
   * Apply concat to corresponding pairs.
   */
  @org.junit.Test
  public void mapPairs004() {
    final XQuery query = new XQuery(
      "map-pairs(concat(?, '-', ?), (\"aa\", \"bb\", \"cc\", \"dd\", \"ee\"), (\"AA\", \"BB\", \"cc\", \"dd\", \"EE\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("'aa-AA', 'bb-BB', 'cc-cc', 'dd-dd', 'ee-EE'")
    );
  }

  /**
   * Apply user-defined anonymous function to corresponding pairs.
   */
  @org.junit.Test
  public void mapPairs005() {
    final XQuery query = new XQuery(
      "map-pairs(function($a as xs:integer, $b as xs:integer) as xs:integer{$a + $b}, 1 to 5, 1 to 5)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("2, 4, 6, 8, 10")
    );
  }

  /**
   * Use a function that has a closure.
   */
  @org.junit.Test
  public void mapPairs006() {
    final XQuery query = new XQuery(
      " \n" +
      "            let $millenium := year-from-date(current-date()) idiv 1000 \n" +
      "            return map-pairs(function($a as xs:integer, $b as xs:integer) as xs:integer{$a + $b + $millenium}, 1 to 5, 2 to 6)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("5, 7, 9, 11, 13")
    );
  }

  /**
   * Use a function that has a closure.
   */
  @org.junit.Test
  public void mapPairs007() {
    final XQuery query = new XQuery(
      " \n" +
      "            let $millenium := year-from-date(current-date()) idiv 1000 \n" +
      "            return map-pairs(function($a, $b) as xs:integer* {1 to (string-length($a) + string-length($b))}, (\"a\", \"ab\", \"abc\", \"\"), (\"\", \"\", \"\", \"\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 1, 2, 1, 2, 3")
    );
  }

  /**
   * map-pairs function - exercise from XML Prague 2010:
   *             form sum of adjacent values in an input sequence.
   */
  @org.junit.Test
  public void mapPairs008() {
    final XQuery query = new XQuery(
      " let $in := 1 to 5 return map-pairs(function($a, $b){$a+$b}, $in, tail($in)) ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("3, 5, 7, 9")
    );
  }

  /**
   * map-pairs function, wrong arity function.
   */
  @org.junit.Test
  public void mapPairs901() {
    final XQuery query = new XQuery(
      "map-pairs(deep-equal#3, (\"aa\", \"bb\", \"cc\", \"dd\", \"ee\"), (\"AA\", \"BB\", \"cc\", \"dd\", \"EE\"))",
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
   * map-pairs function, wrong input to function.
   */
  @org.junit.Test
  public void mapPairs902() {
    final XQuery query = new XQuery(
      "map-pairs(contains#2, (\"aa\", \"bb\", \"cc\", \"dd\", \"ee\"), (\"AA\", \"BB\", \"cc\", \"dd\", 12))",
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
