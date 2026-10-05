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

  /** Keyword used as QName prefix does not trigger an alternative error. */
  @Test public void keywordPrefix() {
    error("{ 'a': 2 } => map:keys() ! 1", QUERYEND_X);
    error("[ 1 ] => array:size() ! 1", QUERYEND_X);
    error("<a/>/attribute::b ! 1 1", QUERYEND_X);
  }

  /** Keyword parsed as a name: the hint is reported after the keyword. */
  @Test public void keywordHint() {
    errorAt("for x in 1 return x", EXPECTAFTER_X_X, 5);
    errorAt("let x := 1 return x", EXPECTAFTER_X_X, 5);
    errorAt("for $x in 1 where if group by $x retrun $x", EXPECTAFTER_X_X, 22);
    errorAt("return 1", UNEXPRETURN, 8);
    errorAt("copy x := <a/> modify () return x", EXPECTAFTER_X_X, 6);
    errorAt("delete $x", EXPECTAFTER_X_X, 8);
    errorAt("insert $a into $b", EXPECTAFTER_X_X, 8);
    errorAt("rename $a as 'b'", EXPECTAFTER_X_X, 8);
    errorAt("replace $a with $b", EXPECTAFTER_X_X, 9);
    errorAt("element a 1", EXPECTAFTER_X_X, 9);
    errorAt("element { 1 }", EXPECTAFTER_X_X, 9);
    errorAt("attribute a 1", EXPECTAFTER_X_X, 11);
    errorAt("text 1", EXPECTAFTER_X_X, 6);
    errorAt("comment 'x' 1", EXPECTAFTER_X_X, 9);
    errorAt("function $x { $x }", EXPECTAFTER_X_X, 10);
    errorAt("fn $x { $x }", EXPECTAFTER_X_X, 4);
  }

  /** No hint for keywords that are parsed successfully or not followed by an operand. */
  @Test public void noKeywordHint() {
    errorAt("if/a 1", QUERYEND_X, 7);
    errorAt("if/child::a 1", QUERYEND_X, 14);
    errorAt("for $x in 1 count $c retrun $x", FLWORRETURN, 22);
    errorAt("map { } 1", QUERYEND_X, 10);
    errorAt("1 instance of empty-sequence() 2", QUERYEND_X, 33);
    errorAt("<a/>/text = 1 2", QUERYEND_X, 16);
    errorAt("<a/>/functions 1", QUERYEND_X, 17);
    errorAt("fn:abs(1) 2", QUERYEND_X, 12);
    errorAt("<a/>/element(a) 1", QUERYEND_X, 18);
    error("{ { }?a : 1 } 2", QUERYEND_X);
    error("<local:a/>/self::local :*", QUERYEND_X);
  }

  /** Common mistakes are reported with specific messages. */
  @Test public void commonMistakes() {
    errorAt("1; 2", QUERYSEMI, 3);
    errorAt("1 = 1 ? 2 : 3", QUERYCOLON, 12);
    errorAt("1 == 2", UNKNOWNOP_X_X, 4);
    errorAt("(1 && 2)", UNKNOWNOP_X_X, 6);
    errorAt("try { 1 } catch { 2 }", EXPECTAFTER_X_X, 16);
    errorAt("if (1) 2 else 3", NOTHEN_X, 8);
    errorAt("insert node $a in $b", INSERTMODE, 16);
    errorAt("1 instance of empty-sequence x", WRONGCHAR_X_X, 30);
    errorAt("for $x in 1 where return $x", UNEXPRETURN, 26);
    errorAt("for $x in", NOEXPR, 10);
  }

  /** Missing operands are reported with the operator. */
  @Test public void missingOperand() {
    for(final String op : new String[] { "!", "to", "||", "+", "div", "=", "eq", "is", ",", "and",
        "or", "otherwise", "->" }) {
      errorMessage("1 " + op, EXPECTAFTER_X_X, "after '" + op + "'");
    }
    errorMessage("some x in 1 satisfies x", EXPECTAFTER_X_X, "'$' after 'some'");
    errorMessage("switch 1 case 1 return 2 default return 3", EXPECTAFTER_X_X,
        "'(' after 'switch'");
    errorMessage("map a", EXPECTAFTER_X_X, "'{' after 'map'");
    errorMessage("for $x in 1 count c return 1", EXPECTAFTER_X_X, "'$' after 'count'");
  }

  /** Error messages report the complete name that was found. */
  @Test public void found() {
    errorMessage("some $x in 1 where $x", WRONGCHAR_X_X, "found 'where'");
    errorMessage("1 instance xs:integer", WRONGCHAR_X_X, "found 'xs:integer'");
    errorMessage("1 => 1", ARROWSPEC_X, "found '1'");
    errorMessage("1 =>", ARROWSPEC_X, "arrow operator.");
    errorMessage("1 cast as 2", TYPEINVALID_X, "found '2'");
  }

  /** Coercion errors in record constructors are reported at the record declaration. */
  @Test public void recordConstructor() {
    errorAt("declare record local:r(x as xs:integer); local:r('a')", INVTYPE_X, 16);
    errorAt("declare type local:r as record(x as xs:integer); local:r('a')", INVTYPE_X, 14);
  }

  /** Destructuring errors name the binding pattern. */
  @Test public void destructuring() {
    errorMessage("let $( $a, $b ) as xs:integer := ('x', 2) return $a", INVTYPE_X,
        "$( $a, $b ): ");
    errorMessage("let $[ $a ] := 1 return $a", INVTYPE_X, "$[ $a ]: ");
    errorMessage("let ${ $a } := 1 return $a", INVTYPE_X, "${ $a }: ");
  }

  /** Unprefixed call of a user-defined function with wrong arity reports an arity mismatch. */
  @Test public void wrongArityNoNamespace() {
    error("declare function abc($j) { }; abc()", PARAMMISSING_X_X);
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
    unknownName("'a' cast as xs:strin", TYPEUNKNOWN_X, "xs:string");
    // prefix fallback for short input that is too far in Levenshtein distance
    unknownName("'a' cast as xs:integ", TYPEUNKNOWN_X, "xs:integer");
  }

  /**
   * Checks that the error message includes a similar-name hint.
   * @param query query that should fail
   * @param code expected error code
   * @param similar expected leading substring of the "maybe: ..." hint
   */
  private static void unknownName(final String query, final QueryError code,
      final String similar) {
    errorMessage(query, code, "(maybe: " + similar);
  }

  /**
   * Checks that a query fails with the specified error at the specified column.
   * @param query query that should fail
   * @param code expected error code
   * @param column expected column
   */
  private static void errorAt(final String query, final QueryError code, final int column) {
    final QueryException ex = fails(query, code);
    assertEquals(column, ex.column(), ex.getLocalizedMessage());
  }

  /**
   * Checks that a query fails with the specified error and message.
   * @param query query that should fail
   * @param code expected error code
   * @param text expected substring of the error message
   */
  private static void errorMessage(final String query, final QueryError code, final String text) {
    final String msg = fails(query, code).getLocalizedMessage();
    assertTrue(msg.contains(text), msg);
  }

  /**
   * Checks that the error message includes no similar-name hint.
   * @param query query that should fail
   * @param code expected error code
   */
  private static void noHint(final String query, final QueryError code) {
    final String msg = fails(query, code).getLocalizedMessage();
    assertFalse(msg.contains("maybe"), msg);
  }

  /**
   * Evaluates a query that is expected to fail with the specified error.
   * @param query query that should fail
   * @param code expected error code
   * @return raised exception
   */
  private static QueryException fails(final String query, final QueryError code) {
    try {
      eval(query);
    } catch(final QueryException ex) {
      assertSame(code, ex.error(), ex.getLocalizedMessage());
      return ex;
    } catch(final Exception ex) {
      return fail(ex);
    }
    return fail("Query did not fail.");
  }
}
