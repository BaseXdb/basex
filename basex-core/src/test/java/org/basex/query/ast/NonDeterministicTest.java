package org.basex.query.ast;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import java.io.*;

import org.basex.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.func.util.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Timeout.*;

/**
 * Checks that optimizations preserve the side effects of nondeterministic code.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class NonDeterministicTest extends SandboxTest {
  /** Log file recording evaluated side effects. */
  private IOFile log;

  /** Prepares an empty log file before each test. */
  @BeforeEach public void prepareLog() {
    log = new IOFile(sandbox(), "log");
    write(log, "");
  }

  /** Checks that expressions marked as nondeterministic are not rewritten. */
  @Test public void pragma() {
    check("count((# basex:nondeterministic #) { <x/> })", 1, exists(COUNT));
  }

  /**
   * Checks that arithmetic keeps a nondeterministic operand instead of applying rewrites that might
   * remove them, such as {@code $ndt * 0}.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link Arith#optimize}.
   */
  @Test public void arith() {
    query("(# basex:nondeterministic #) { " + fileAppend() + ", 5 } * 0", 0, "x");
  }

  /**
   * Checks that a binary operator keeps a nondeterministic operand when the other operand is empty.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link Arr#emptyExpr} (arithmetic and comparison
   * operators).
   */
  @Test public void emptyOperand() {
    query("() = (# basex:nondeterministic #) { " + fileAppend() + ", 5 }", false, "x");
  }

  /**
   * Checks that comparisons of {@code fn:count} results do not shortcut the evaluation of
   * nondeterministic input.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link Cmp#optCount} and {@link StandardFunc#embed}.
   */
  @Test public void countComparison() {
    query("count((1 to 10)[" + fileExists() + "] ! (" + fileAppend() + ", .)) >= 1", true,
        "xxxxxxxxxx");
  }

  /**
   * Checks that {@code fn:distinct-values} does not drop equal nondeterministic operands of a list.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link List#simplifyFor}; the duplicates are
   * non-adjacent, as adjacent ones are merged early into a side-effect-preserving replicate.
   */
  @Test public void distinctList() {
    query("distinct-values((" + fileAppend() + ", 2, " + fileAppend() + "))", 2, "xx");
  }

  /**
   * Checks that {@code fn:distinct-values} does not merge equal nondeterministic expressions in a
   * simple map body.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link SimpleMap#simplifyFor} and
   * {@link List#simplifyFor}; the duplicates are non-adjacent, as adjacent ones are merged early.
   */
  @Test public void distinctValues() {
    query("count(distinct-values((1 to 10) ! (" + fileAppend() + ", ., " + fileAppend() + ")))", 10,
        "xxxxxxxxxxxxxxxxxxxx");
  }

  /**
   * Checks that {@code switch} does not drop duplicate nondeterministic case operands, whose side
   * effects would be lost when no case matches the operand.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link Switch#opt} and {@link List#simplifyFor} (the
   * switch is rewritten to a comparison whose operand list is deduplicated).
   */
  @Test public void switchCase() {
    query("switch(5) case (" + fileAppend() + ", 9) case (" + fileAppend() + ", 9) return 'a' " +
        "default return 'b'", "b", "xx");
  }

  /**
   * Checks that order by clauses with nondeterministic keys are not removed when counting results.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnCount#simplifyArgs} (first line of defense)
   * and {@link GFLWOR#simplifyFor} (second line, shared with {@code fn:empty} and
   * {@code util:count-within}).
   */
  @Test public void orderBy() {
    query("count(for $num in (2, 3, 1) order by (" + fileAppend() + ", $num) return $num)", 3,
        "xxx");
  }

  /**
   * Checks that a nondeterministic function item bound to a variable keeps its side effects when
   * the enclosing for clause is rewritten to a single-evaluation replicate.
   * <p>
   * Requires the {@code referencesNdtFunction} check in {@link GFLWOR#inlineForLet} and the
   * {@code containsNdtFunction} check in {@link DynFuncCall#optimize}.
   */
  @Test public void functionVariable() {
    query("let $f := function() { " + fileAppend() + " } for $x in (1 to 2) return $f()", "", "xx");
  }

  /**
   * Checks that a nondeterministic closure bound to a variable keeps its side effects when the
   * enclosing for clause is rewritten to a single-evaluation replicate.
   * <p>
   * Requires the {@code referencesNdtFunction} check in {@link GFLWOR#inlineForLet} and the
   * {@code containsNdtFunction} check in {@link DynFuncCall#optimize}.
   */
  @Test public void closureVariable() {
    query("let $c := 'x' let $f := function() { " + _FILE_APPEND_TEXT.args(log.path(), " $c") +
        " } for $x in (1 to 2) return $f()", "", "xx");
  }

  /**
   * Checks that a sequence of nondeterministic function items bound to a variable keeps its side
   * effects when the enclosing for clause is rewritten to a single-evaluation replicate.
   * <p>
   * Requires the {@code referencesNdtFunction} check in {@link GFLWOR#inlineForLet} and the
   * {@code containsNdtFunction} check in {@link DynFuncCall#optimize}.
   */
  @Test public void functionSequence() {
    query("let $f := (function() { " + fileAppend() + " }, function() { " + fileAppend() + " }) " +
        "for $x in (1 to 2) return $f()", "", "xxxx");
  }

  /**
   * Checks that a sequence of nondeterministic closures bound to a variable keeps its side effects
   * when the enclosing for clause is rewritten to a single-evaluation replicate.
   * <p>
   * Requires the {@code referencesNdtFunction} check in {@link GFLWOR#inlineForLet} and the
   * {@code containsNdtFunction} check in {@link DynFuncCall#optimize}.
   */
  @Test public void closureSequence() {
    query("let $c := 'x' let $f := (function() { " + _FILE_APPEND_TEXT.args(log.path(), " $c") +
        " }, function() { " + _FILE_APPEND_TEXT.args(log.path(), " $c") + " }) " +
        "for $x in (1 to 2) return $f()", "", "xxxx");
  }

  /**
   * Checks that an array of nondeterministic function items bound to a variable keeps its side
   * effects when the enclosing for clause is rewritten to a single-evaluation replicate.
   * <p>
   * Requires the {@code referencesNdtFunction} check in {@link GFLWOR#inlineForLet} and the
   * {@code containsNdtFunction} check in {@link DynFuncCall#optimize}.
   */
  @Test public void functionArray() {
    query("let $f := [function() { " + fileAppend() + " }] for $x in (1 to 2) return $f?1()", "",
        "xx");
  }

  /**
   * Checks that a map of nondeterministic function items bound to a variable keeps its side
   * effects when the enclosing for clause is rewritten to a single-evaluation replicate.
   * <p>
   * Requires the {@code referencesNdtFunction} check in {@link GFLWOR#inlineForLet} and the
   * {@code containsNdtFunction} check in {@link DynFuncCall#optimize}.
   */
  @Test public void functionMap() {
    query("let $f := map { 'k': function() { " + fileAppend() + " } } for $x in (1 to 2) " +
        "return $f?k()", "", "xx");
  }

  /**
   * Checks that a nondeterministic function item bound to a variable keeps its side effects when a
   * simple map over a constant range is rewritten to a single-evaluation replicate.
   * <p>
   * Requires the {@link Expr#mayInvokeVariable} check in {@link CompileContext#replicate} and
   * {@link SimpleMap#dropOps}.
   */
  @Test public void simpleMapVariable() {
    query("let $f := function() { " + fileAppend() + " } return (1 to 2) ! $f()", "", "xx");
  }

  /**
   * Checks that a sequence of nondeterministic function items bound to a variable keeps its side
   * effects when a simple map over a constant range is rewritten to a single-evaluation replicate.
   * <p>
   * Requires the {@link Expr#mayInvokeVariable} check in {@link CompileContext#replicate} and
   * {@link SimpleMap#dropOps}.
   */
  @Test public void simpleMapSequence() {
    query("let $f := (function() { " + fileAppend() + " }, function() { " + fileAppend() + " }) " +
        "return (1 to 2) ! $f()", "", "xxxx");
  }

  /**
   * Checks that nondeterministic code in coerced function items is not discarded.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link DynFuncCall#optimize}: without inlining, the
   * coercion of the key function wraps it in a new function item whose body is a dynamic call of
   * the original function, hiding its nondeterministic property.
   */
  @Test public void functionCoercion() {
    query("count(sort((2, 1), (), function($key) { " + fileAppend() + ", $key }))", 2, "xx");
  }

  /**
   * Checks that nondeterministic bodies of invoked function items are visible to optimizations.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link StandardFunc#embed} (which consults
   * {@link FuncItem#ndt}, as the pre-evaluated function item is a value and reports no flags) and
   * {@link FnCount}.
   */
  @Test public void functionItem() {
    query("count((1 to 2) ! (function($arg) as xs:integer { " + fileAppend() + ", $arg }(.)))", 2,
        "xx");
  }

  /**
   * Checks that {@code fn:count} does not discard the nondeterministic body of a simple map.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link StandardFunc#embed}; the nondeterministic filter
   * makes the result size unknown until runtime.
   */
  @Test public void countSimpleMap() {
    query("count((1 to 10)[" + fileExists() + "] ! (" + fileAppend() + ", .))", 10, "xxxxxxxxxx");
  }

  /**
   * Checks that {@code fn:count} evaluates nondeterministic input even if its iterator size is
   * known.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnCount#item}; the conditional
   * prevents compile-time rewriting of the simple map.
   */
  @Test public void countKnownSize() {
    query("count(if (" + fileExists() + ") then (1 to 10) ! (" + fileAppend() + ", .) else ())", 10,
        "xxxxxxxxxx");
  }

  /**
   * Checks that {@code fn:exactly-one} evaluates nondeterministic input before raising a
   * cardinality error for statically oversized input.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnExactlyOne#opt}.
   * @throws IOException I/O exception
   */
  @Test public void exactlyOneNdt() throws IOException {
    error("exactly-one((" + fileAppend() + ", 1, 2))", EXACTLYONE);
    query(_FILE_READ_TEXT.args(log.path()), "x");

    log.write("");
    error("exactly-one(" + fileAppend() + ")", EXACTLYONE);
    query(_FILE_READ_TEXT.args(log.path()), "x");
  }

  /**
   * Checks that {@code fn:zero-or-one} evaluates nondeterministic input before raising a
   * cardinality error for statically oversized input.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnZeroOrOne#opt}.
   */
  @Test public void zeroOrOneNdt() {
    error("zero-or-one((" + fileAppend() + ", 1, 2))", ZEROORONE);
    query(_FILE_READ_TEXT.args(log.path()), "x");
  }

  /**
   * Checks that {@code fn:one-or-more} evaluates nondeterministic input before raising a
   * cardinality error for statically empty input.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnOneOrMore#opt}.
   */
  @Test public void oneOrMoreNdt() {
    error("one-or-more(" + fileAppend() + ")", ONEORMORE);
    query(_FILE_READ_TEXT.args(log.path()), "x");
  }

  /**
   * Checks that {@code fn:count} does not trigger simplifications that drop nondeterministic order
   * by keys.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnCount#simplifyArgs} (first line of defense)
   * and {@link GFLWOR#simplifyFor} (second line).
   */
  @Test public void countOrderBy() {
    query("count(for $item in (2, 1) order by (" + fileAppend() + ", $item) return $item)", 2,
        "xx");
  }

  /**
   * Checks that {@code fn:count} used as an effective boolean value does not simplify
   * nondeterministic input to {@code fn:exists}.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnCount#simplifyFor} and
   * {@link StandardFunc#embed}.
   */
  @Test public void countEbv() {
    query("if(count((1 to 10) ! (" + fileAppend() + ", .))) then true() else false()", true,
        "xxxxxxxxxx");
  }

  /**
   * Checks that {@code fn:empty} does not trigger simplifications that drop nondeterministic order
   * by keys.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnEmpty#simplifyArgs} (first line of defense)
   * and {@link GFLWOR#simplifyFor} (second line); retrieving the first item of the ordered
   * sequence evaluates all order by keys.
   */
  @Test public void emptyOrderBy() {
    query("empty(for $entry in (2, 1) order by (" + fileAppend() + ", $entry) return $entry)",
        false, "xx");
  }

  /**
   * Checks that sort operations with nondeterministic keys are not removed when counting results.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnCount#simplifyArgs} (first line of defense)
   * and {@link FnSortBy#simplifyFor} (second line, covering {@code fn:sort} and
   * {@code fn:sort-by}); inlining is enabled, as the coercion of the key function otherwise hides
   * its nondeterministic property in a dynamic function call.
   */
  @Test public void sort() {
    inline(true);
    query("count(sort((2, 1), (), function($int) { " + fileAppend() + ", $int }))", 2, "xx");
  }

  /**
   * Checks that sort operations with nondeterministic comparators are not removed when counting
   * results.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnCount#simplifyArgs} (first line of defense)
   * and {@link FnSortWith#simplifyFor} (second line); inlining is enabled, as the coercion of the
   * comparator otherwise hides its nondeterministic property in a dynamic call.
   */
  @Test public void sortWith() {
    inline(true);
    query("count(sort-with((2, 1), function($a, $b) { " + fileAppend() + ", $a - $b }))", 2, "x");
  }

  /**
   * Checks that {@code util:count-within} does not trigger simplifications that drop
   * nondeterministic order by keys.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link UtilCountWithin#simplifyArgs} (first line of
   * defense) and {@link GFLWOR#simplifyFor} (second line); counting up to the minimum retrieves
   * items of the ordered sequence, evaluating all keys.
   */
  @Test public void countWithin() {
    query("util:count-within(for $value in (2, 1) order by (" + fileAppend() +
        ", $value) return $value, 2)", true, "xx");
  }

  /**
   * Checks that {@code util:count-within} evaluates nondeterministic input even if its iterator
   * size is known.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link UtilCountWithin#test} and
   * {@link StandardFunc#embed}; the minimum equals the input size, so counting cannot stop before
   * the last item.
   */
  @Test public void countWithinKnownSize() {
    query("util:count-within((1 to 10) ! (" + fileAppend() + ", .), 10)", true, "xxxxxxxxxx");
  }

  /**
   * Checks that {@code util:count-within} does not discard nondeterministic input with zero-or-one
   * cardinality.
   * <p>
   * Requires the {@link Flag#NDT} check for the zero-or-one rewrites in
   * {@link UtilCountWithin#opt}.
   */
  @Test public void countWithinZeroOrOne() {
    query("util:count-within(" + fileAppend() + ", 2)", false, "x");
  }

  /**
   * Checks that {@code util:count-within} does not simplify nondeterministic input to
   * {@code fn:exists}.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link UtilCountWithin#opt} and
   * {@link StandardFunc#embed}.
   */
  @Test public void countWithinExists() {
    query("util:count-within((1 to 10) ! (" + fileAppend() + ", .), 1)", true, "xxxxxxxxxx");
  }

  /**
   * Checks that {@code util:count-within} with an always-satisfied range does not drop
   * nondeterministic input.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link UtilCountWithin#opt} and
   * {@link StandardFunc#embed}.
   */
  @Test public void countWithinAlways() {
    query("util:count-within((1 to 10) ! (" + fileAppend() + ", .), 0)", true, "xxxxxxxxxx");
  }

  /**
   * Checks that {@code util:count-within} with an impossible range does not drop nondeterministic
   * input.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link UtilCountWithin#opt} and
   * {@link StandardFunc#embed}.
   */
  @Test public void countWithinImpossible() {
    query("util:count-within((1 to 10) ! (" + fileAppend() + ", .), 2, 1)", false, "xxxxxxxxxx");
  }

  /**
   * Checks that {@code fn:count} consumes all mapped items to preserve side effects in the map
   * body.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnCount} and {@link StandardFunc#embed}.
   */
  @Test public void gh2673() {
    query("let $ten-strings := replicate('x', 10) return count($ten-strings ! (" + fileAppend() +
        ", .))", 10, "xxxxxxxxxx");
  }

  /**
   * Checks that distinct-values does not reorder a sort with a nondeterministic key, which
   * would reduce the number of key evaluations and drop side effects.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnDistinctValues#opt}.
   */
  @Test public void distinctValuesSort() {
    inline(true);
    query("count(distinct-values(sort(('b', 'a', 'b'), (), fn($x) { " + fileAppend() + ", $x })))",
        2, "xxx");
  }

  /**
   * Checks that fn:count does not drop the input of fn:reverse with nondeterministic content.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnReverse#simplifyFor}.
   */
  @Test public void countReverse() {
    query("count(reverse((1 to 2) ! (" + fileAppend() + ", .)))", 2, "xx");
  }

  /**
   * Checks that fn:fold-left does not drop a nondeterministic input when it has empty type.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnFoldLeft#optType}.
   */
  @Test public void foldLeftNdt() {
    query("fold-left(" + fileAppend() + ", 0, fn($z, $x) { $z + 1 })", 0, "x");
  }

  /**
   * Checks that fn:for-each-pair does not drop a nondeterministic second input when the first is
   * empty, or a nondeterministic first input when the second is empty.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnForEachPair#opt}.
   */
  @Test public void forEachPairNdt() {
    query("for-each-pair((), (1 to 2) ! (" + fileAppend() + ", .), fn($a, $b) { $a })", "", "xx");
    query("for-each-pair((1 to 2) ! (" + fileAppend() + ", .), (), fn($a, $b) { $a })", "", "xxxx");
  }

  /**
   * Checks that fn:contains-subsequence and fn:ends-with-subsequence do not drop a
   * nondeterministic input when the subsequence is empty.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnContainsSubsequence#opt}.
   */
  @Test public void containsSubsequenceNdt() {
    query("contains-subsequence((1 to 2) ! (" + fileAppend() + ", .), ())", true, "xx");
    query("ends-with-subsequence((1 to 2) ! (" + fileAppend() + ", .), ())", true, "xxxx");
  }

  /**
   * Checks that fn:compare does not drop a nondeterministic operand when the other is empty.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnCompare#opt}.
   */
  @Test public void compareNdt() {
    query("compare((), (" + fileAppend() + ", 'a'))", "", "x");
    query("compare((" + fileAppend() + ", 'a'), ())", "", "xx");
  }

  /**
   * Checks that fn:codepoint-equal does not drop a nondeterministic operand when the other is
   * empty.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnCodepointEqual#opt}.
   */
  @Test public void codepointEqualNdt() {
    query("codepoint-equal((), (" + fileAppend() + ", 'a'))", "", "x");
    query("codepoint-equal((" + fileAppend() + ", 'a'), ())", "", "xx");
  }

  /**
   * Checks that fn:dateTime does not drop a nondeterministic operand when the other is empty.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnDateTime#opt}.
   */
  @Test public void dateTimeNdt() {
    query("dateTime((), (" + fileAppend() + ", xs:time('10:00:00')))", "", "x");
    query("dateTime((" + fileAppend() + ", xs:date('2020-01-01')), ())", "", "xx");
  }

  /**
   * Checks that fn:items-at does not drop a nondeterministic input when the position list is empty.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnItemsAt#opt}.
   */
  @Test public void itemsAtNdt() {
    query("items-at((1 to 2) ! (" + fileAppend() + ", .), ())", "", "xx");
  }

  /**
   * Checks that fn:insert-separator does not drop a nondeterministic separator when the input has
   * at most one item.
   * <p>
   * Requires the {@link Flag#NDT} check in {@link FnInsertSeparator#opt}.
   */
  @Test public void insertSeparatorNdt() {
    query("insert-separator(('a'), (" + fileAppend() + ", '-'))", "a", "x");
  }

  /**
   * Checks that fn:subsequence does not drop a nondeterministic input when the position range is
   * empty or starts beyond a single-item sequence.
   * <p>
   * Requires the use of {@link CompileContext#voidAndReturn} in {@link FnSubsequence#opt}.
   */
  @Test public void subsequenceNdt() {
    query("subsequence((" + fileAppend() + ", 1), 2, 0)", "", "x");
    query("subsequence((" + fileAppend() + ", 1)[. > 0], 2)", "", "xx");
  }

  /**
   * Checks that fn:deep-equal does not drop nondeterministic inputs when sizes differ.
   * <p>
   * Requires the use of {@link CompileContext#voidAndReturn} in {@link FnDeepEqual#opt}.
   */
  @Test public void deepEqualNdt() {
    query("deep-equal((" + fileAppend() + ", 1), (" + fileAppend() + ", 1, 2))", false, "xx");
  }

  /**
   * Checks that fn:tail does not drop a nondeterministic input with zero-or-one cardinality.
   * <p>
   * Requires the use of {@link CompileContext#voidAndReturn} in {@link FnTail#opt}.
   */
  @Test public void tailNdt() {
    query("tail(" + fileAppend() + ")", "", "x");
  }

  /**
   * Checks that fn:trunk does not drop a nondeterministic input with zero-or-one cardinality.
   * <p>
   * Requires the use of {@link CompileContext#voidAndReturn} in {@link FnTrunk#opt}.
   */
  @Test public void trunkNdt() {
    query("trunk(" + fileAppend() + ")", "", "x");
  }

  /**
   * Checks that the nondeterminism check does not traverse atomic values. Regression: a dynamic
   * call on a large constant array or sequence walked all members at compile time.
   * <p>
   * Requires the atomic type checks in {@code containsNdtFunction} in {@link DynFuncCall#optimize}.
   * The timeout is preemptive, as the traversal would not be interrupted by the default one.
   */
  @Test @Timeout(value = 60, threadMode = ThreadMode.SEPARATE_THREAD) public void largeValues() {
    query("array { 1 to 100_000_000_000 }(1)", 1);
    error("(1 to 100_000_000_000)(1)", INVFUNCITEM_X_X);
  }

  /**
   * Checks that the nondeterminism check does not traverse large function sequences, arrays and
   * maps. Regression: a dynamic call on a large constant array of functions walked all members at
   * compile time.
   * <p>
   * Requires the size checks in {@code containsNdtFunction} in {@link DynFuncCall#optimize}.
   * The timeout is preemptive, as the traversal would not be interrupted by the default one.
   */
  @Test @Timeout(value = 60, threadMode = ThreadMode.SEPARATE_THREAD) public void largeFunctions() {
    query("array { (1 to 100_000_000_000) ! identity#1 }(1)(42)", 42);
    query("array { subsequence((1 to 100_000_000_000) ! identity#1, 3) }(1)(42)", 42);
    query("[ (1 to 100_000_000_000) ! identity#1 ](1)[1](42)", 42);
    query("((1 to 100_000_000_000) ! identity#1)[1](42)", 42);
  }

  /**
   * Checks that a nondeterministic function item in a large array is still detected, so that its
   * side effects are preserved when the enclosing for clause is collapsed.
   * <p>
   * Guards the conservative result of the size checks in {@code containsNdtFunction}.
   */
  @Test public void largeNdtFunctions() {
    query("let $f := array { (1 to 300000) ! (function() { " + fileAppend() + " }, identity#1) } " +
        "for $x in (1 to 2) return $f(1)()", "", "xx");
  }

  /**
   * Returns a function call that records a single evaluation in the log.
   * @return file:append-text call
   */
  private String fileAppend() {
    return _FILE_APPEND_TEXT.args(log.path(), "x");
  }

  /**
   * Returns a function call that is true if the log file exists. As the file is always present at
   * runtime but its existence is unknown at compile time, it acts as a non-foldable condition.
   * @return file:exists call
   */
  private String fileExists() {
    return _FILE_EXISTS.args(log.path());
  }

  /**
   * Runs a query and checks its result and the side effects recorded in the log.
   * @param query query to run
   * @param result expected query result
   * @param recorded expected recorded side effects
   */
  private void query(final String query, final Object result, final String recorded) {
    query(query, result);
    query(_FILE_READ_TEXT.args(log.path()), recorded);
  }
}
