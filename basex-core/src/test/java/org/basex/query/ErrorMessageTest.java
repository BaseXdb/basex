package org.basex.query;

import static org.basex.query.QueryError.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for query error messages.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ErrorMessageTest extends SandboxTest {
  /** Unknown keyword parameter: hint to similar parameter name. */
  @Test public void unknownKeyword() {
    // Levenshtein match for typo
    unknownName("declare function local:x($alpha) { }; local:x(alph := 0)",
        PARAMUNKNOWN_X_X, "alpha");
    // prefix fallback for input that is too far in Levenshtein distance
    unknownName("declare function local:c($langitude, $longitude) { };"
        + " local:c(langi := 1, longitude := 2)", PARAMUNKNOWN_X_X, "langitude");
  }

  /** Unknown built-in function: hint to similar function name. */
  @Test public void unknownFunction() {
    // Levenshtein match for typo (unprefixed call, hint without fn: prefix)
    unknownName("coun()", WHICHFUNC_X, "count");
    // prefix fallback for input that is too far in Levenshtein distance
    unknownName("all-eq()", WHICHFUNC_X, "all-equal");
    // shortest name wins among multiple prefix matches
    unknownName("fold-(1)", WHICHFUNC_X, "fold-left");
    // no hint for inputs that cover less than half of the closest name
    noHint("x()", WHICHFUNC_X);
    // prefix fallback for user-defined function (local: prefix is preserved)
    unknownName("declare function local:abcde($john) { }; local:abc()",
        WHICHFUNC_X, "local:abcde");
    // built-in match preferred over user-defined when both exist
    unknownName("declare function local:subsequence-after() { };"
        + " subsequenc(1, 2, 3)", WHICHFUNC_X, "subsequence");
  }

  /** Incomplete argument list: report the missing token instead of a missing argument. */
  @Test public void incompleteArgumentList() {
    error("true(", INCOMPLETE);
    error("true(1", WRONGCHAR_X_X);
    error("true(1 2)", WRONGCHAR_X_X);
    error("true(1,", FUNCARG_X);
    error("true(,", FUNCARG_X);
  }

  /** Unprefixed call of a user-defined function with wrong arity reports an arity mismatch. */
  @Test public void wrongArityNoNamespace() {
    error("declare function abc($j) { }; abc()", INVNARGS_X_X);
  }

  /** Unprefixed call of a built-in must still resolve when a same-named user function exists. */
  @Test public void shadowedBuiltin() {
    query("declare function abs($x as xs:integer, $y as xs:integer) as xs:integer"
        + " { $x + $y }; abs(-5)", 5);
  }

  /** Unknown variable: hint to similar variable name. */
  @Test public void unknownVariable() {
    unknownName("for $letter in 1 to 5 return $lette", VARUNDEF_X, "$letter");
    // innermost binding wins on Levenshtein ties
    unknownName("let $l1 := 1 let $l2 := 2 return $l", VARUNDEF_X, "$l2");
  }

  /** Unknown annotation: hint to similar annotation name. */
  @Test public void unknownAnnotation() {
    // XQuery namespace (reserved): "private" is the spec annotation
    unknownName("declare %privte function local:f() { 1 }; local:f()",
        ANNRESERVED_X, "%private");
    // BaseX namespace: "lazy" is a valid annotation
    unknownName("declare %basex:lasy function local:f() { 1 }; local:f()",
        BASEX_ANN1_X, "%basex:lazy");
    // prefix fallback for short input
    unknownName("declare %output:inden('yes') function local:f() { 1 }; local:f()",
        BASEX_ANN1_X, "%output:indent");
  }

  /** Unknown atomic type: hint to similar type name. */
  @Test public void unknownType() {
    // Levenshtein match for typo
    unknownName("'a' cast as xs:strin", WHICHCAST_X, "xs:string");
    // prefix fallback for short input that is too far in Levenshtein distance
    unknownName("'a' cast as xs:integ", WHICHCAST_X, "xs:integer");
  }

  /**
   * Checks that the error message includes a similar-name hint.
   * @param query query that should fail
   * @param code expected error code
   * @param similar expected leading substring of the "maybe: ..." hint
   */
  private static void unknownName(final String query, final QueryError code,
      final String similar) {
    try {
      eval(query);
      fail("Query did not fail.");
    } catch(final QueryException ex) {
      assertSame(code, ex.error(), ex.getLocalizedMessage());
      final String msg = ex.getLocalizedMessage();
      assertTrue(msg.contains("(maybe: " + similar), msg);
    } catch(final Exception ex) {
      fail(ex);
    }
  }

  /**
   * Checks that the error message includes no similar-name hint.
   * @param query query that should fail
   * @param code expected error code
   */
  private static void noHint(final String query, final QueryError code) {
    try {
      eval(query);
      fail("Query did not fail.");
    } catch(final QueryException ex) {
      assertSame(code, ex.error(), ex.getLocalizedMessage());
      final String msg = ex.getLocalizedMessage();
      assertFalse(msg.contains("maybe"), msg);
    } catch(final Exception ex) {
      fail(ex);
    }
  }
}
