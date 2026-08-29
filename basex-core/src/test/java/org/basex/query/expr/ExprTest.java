package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.junit.jupiter.api.Test;

/**
 * Checks the rewritings of specific expressions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ExprTest extends SandboxTest {
  /** Checks OR optimizations. */
  @Test public void or() {
    query("<a/> or <a/>", true);
    query("<a/> or (<a/> or <a/>)", true);
    query("not(<a/>) or (not(<a/>) or not(<a/>))", false);
    query("fold-left(true(), false(), function($a, $b) { $a or $b })", true);
    // the second operand is not evaluated
    query("1 or (1 + 'x')", true);

    check("('' or '')", false, empty(Or.class));
    check("('x' or 'x' = 'x')", true, empty(Or.class));
    check("(false()   or <x/> = 'x')", false, empty(Or.class));
    check("(true()    or <x/> = 'x')", true, empty(Or.class));
    check("('x' = 'x' or <x/> = 'x')", true, empty(Or.class));

    // {@link CmpG} rewritings
    check("let $x := <x/>     return ($x = 'x' or $x = 'y')", false, empty(Or.class));
    check("let $x := <x>x</x> return ($x = 'x' or $x = 'y')", true,  empty(Or.class));
  }

  /** Checks AND optimizations. */
  @Test public void and() {
    query("<a/> and <a/>", true);
    query("<a/> and (<a/> and <a/>)", true);
    query("<a/> and (<a/> and not(<a/>))", false);
    // the second operand is not evaluated
    query("0 and (1 + 'x')", false);

    check("('x' and 'y')", true, empty(And.class));
    check("('x' and 'x' = 'x')", true, empty(And.class));
    check("(true()    and <x>x</x> = 'x')", true, empty(And.class));
    check("(false()   and <x>x</x> = 'x')", false, empty(And.class));
    check("('x' = 'x' and <x>x</x> = 'x')", true, empty(And.class));
  }

  /** Checks {@link CmpIR} optimizations. */
  @Test public void cmpIR() {
    final Class<CmpIR> cmpir = CmpIR.class;
    check("(1, 2)[. = 1] = 1 to 2", true, exists(cmpir));
    check("(1, 2)[. = 3] = 1 to 2", false, exists(cmpir));
    check("(1, 2)[. = 3] = 1 to 2", false, exists(cmpir));

    // do not rewrite equality comparisons against single integers
    check("(1, 2)[. = 1] = 1", true, empty(cmpir));

    // flatten predicate: exists(E[. > 3]) → E > 3
    final String range = " (1 to " + wrap(6) + ')';
    check(EXISTS.args(range + "[. > 3]"), true, root(cmpir), empty(IterFilter.class));
    check(EMPTY.args(range + "[. > 9]"), true, root(NOT), exists(cmpir));

    // rewrite to positional test
    check("(1 to 5)[let $p := position() return $p = 2]", 2,
        empty(cmpir), empty(Let.class), empty(POSITION));
    check("1[let $p := position() return $p = 0]", "", empty());
    check("1[let $p := position() return $p = (-5 to -1)]", "", empty());
  }

  /** Checks {@link CmpR} optimizations. */
  @Test public void cmpR() {
    final Class<CmpR> cmpr = CmpR.class;
    check("<a>5</a>[text() > 1 and text() < 9]", "<a>5</a>", count(cmpr, 1));
    check("<a>5</a>[text() > 1 and text() < 9 and <b/>]", "<a>5</a>", count(cmpr, 1));
    check("<a>5</a>[text() > 1 and . < 9]", "<a>5</a>", count(cmpr, 2));

    // GH-1744
    check("<a>5</a>[text() < 5 or text() > 5]", "", count(cmpr, 2));
    check("<a>5</a>[text() > 5 or text() < 5]", "", count(cmpr, 2));
    check("<a>5</a>[5 > text() or 5 < text()]", "", count(cmpr, 2));
    check("<a>5</a>[5 < text() or 5 > text()]", "", count(cmpr, 2));

    check("<a>5</a>[text() > 800000000]", "", exists(cmpr));
    check("<a>5</a>[text() < -800000000]", "", exists(cmpr));
    check("<a>5</a>[text() <= -800000000]", "", exists(cmpr));
    check("<a>5</a>[text() > 8000000000000000000]", "", exists(cmpr));
    check("<a>5</a>[text() < -8000000000000000000]", "", exists(cmpr));
    check("exists(<x>1234567890.12345678</x>[. = 1234567890.1234567])", false, empty(cmpr));
    check("exists(<x>1234567890.12345678e0</x>[. = 1234567890.1234567e0])", true, empty(cmpr));

    check("exists(<x>123456789012345678</x> [. = 123456789012345679])", false, empty(cmpr));
    check("exists(<x>123456789012345678e0</x> [. = 123456789012345679e0])", true, empty(cmpr));
    check("<a>5</a>[xs:integer(.) > 8000000000000000000]", "", empty(cmpr));
    check("<a>5</a>[xs:integer(.) < -8000000000000000000]", "", empty(cmpr));
    check("(1, 1234567890.12345678)[. = 1234567890.1234567]", "", empty(cmpr));
    check("(1, 123456789012345678 )[. = 123456789012345679]", "", empty(cmpr));

    // rewrite equality comparisons
    check("(0, 1)[. = 1] >= 1e0", true, exists(cmpr));
    check("(0e0, 1e0)[. = 1] >= 1", true, exists(cmpr));
    check("(0e0, 1e0)[. = 1] >= 1e0", true, exists(cmpr));
    check(wrap("1.1") + ">= 1.1", true, exists(cmpr));
    check("(0e0, 1e0)[. = 1] >= 1.0", true, exists(cmpr));
    check("(0e0, 1e0)[. = 1] >= 1.000000000000001", false, exists(cmpr));
    check("(0e0, 1e0)[. = 1] >= 1.0000000000000001", true, exists(cmpr));

    // do not rewrite decimal/double comparisons
    check("(0, 1)[. = 1] >= 1.0", true, empty(cmpr));
    check("(0, 1)[. = 1] >= 1.0000000000000001", false, empty(cmpr));
    check("(0.0, 1.0)[. = 1] >= 1e0", true, empty(cmpr));
    check("(0.0, 1.0)[. = 1] >= 1.000000000000001e0", false, empty(cmpr));
    check("(0.0, 1.0)[. = 1] >= 1.0000000000000001e0", true, empty(cmpr));

    // do not rewrite equality comparisons
    check("(0, 1)[. = 1] = 1.0", true, empty(cmpr));
    check("(0, 1)[. = 1] = 1e0", true, empty(cmpr));
    check("(0e0, 1e0)[. = 1] = 1", true, empty(cmpr));
    check("(0e0, 1e0)[. = 1] = 1.0", true, empty(cmpr));
    check("(0e0, 1e0)[. = 1] = 1e0", true, empty(cmpr));
    check(wrap("1.1") + "= 1.1", true, empty(cmpr));

    // suppressed rewritings
    check(_RANDOM_DOUBLE.args() + " = 2", false, empty(cmpr));
    check("(0.1, 1.1)[. != 0] = 1.3", false, empty(cmpr));
    check("('x', 'y')[. = 'x'] = 'x'", true, empty(cmpr));
    check("('x', 'x')[. != 'x'] = 1.3", false, empty(cmpr));

    check("(0.1, 1.1)[. = 1.1] = 1.1", true, empty(cmpr));

    // rewrite to positional test
    check("1[let $p := position() return $p = 0.0]", "", empty());
    check("(1 to 5)[let $p := position() return $p >= 2.5e0]", "3\n4\n5",
        root(RangeSeq.class));
    check("(1 to 5)[let $p := position() return $p <= 2.5e0]", "1\n2",
        root(RangeSeq.class));

    // flatten predicate: exists(E[text() > 1]) → E/text() > 1
    check(EXISTS.args(" <a>5</a>[text() > 1]"), true, root(cmpr), empty(IterFilter.class));

    // operand may be wrapped: no single-item optimization
    check("head(<a>5</a>/text()) >= 1e0", true, exists(cmpr));

    // merge range with equality comparison
    check("<a>5</a>[text() > 1 and text() = 5]", "<a>5</a>", count(cmpr, 1));
    // no merge: operator is not '=', operand is no number
    check("<a>5</a>[text() > 1 and text() != 5]", "", count(cmpr, 1));
    check("<a>5</a>[text() > 1 and text() = 'x']", "", count(cmpr, 1));
  }

  /** Checks the rewriting of count comparisons. */
  @Test public void cmpCount() {
    final String[] general = { "=", "!=", "<", "<=", ">", ">=" };
    final String[] ops = { "=", "!=", "<", "<=", ">", ">=", "eq", "ne", "lt", "le", "gt", "ge" };
    final String prolog = "declare %basex:inline(0) function local:c($input as item()*, " +
        "$count as xs:integer) as xs:boolean* { count($input) ";

    for(int s = 0; s <= 3; s++) {
      // result size is unknown at compile time
      final String count = "count((1 to 3)[. > " + wrap(3 - s) + "])";
      for(int c = 0; c <= 3; c++) {
        for(final String op : ops) {
          final boolean result = compare(s, op, c);
          // integer literal, variable reference
          query(count + ' ' + op + ' ' + c, result);
          query(prolog + op + " $count }; local:c((1 to " + s + "), " + c + ')', result);
        }
      }
      // general comparisons with a range operand
      for(final String op : general) {
        boolean result = false;
        for(int c = 1; c <= 3; c++) result |= compare(s, op, c);
        query(count + ' ' + op + " (1 to 3)", result);
      }
    }
  }

  /**
   * Compares a result size with a single operand.
   * @param size result size
   * @param op comparison operator
   * @param count operand
   * @return result of check
   */
  private static boolean compare(final int size, final String op, final int count) {
    return switch(op) {
      case "=", "eq" -> size == count;
      case "!=", "ne" -> size != count;
      case "<", "lt" -> size < count;
      case "<=", "le" -> size <= count;
      case ">", "gt" -> size > count;
      default -> size >= count;
    };
  }

  /** Checks {@link CmpV} optimizations. */
  @Test public void cmpV() {
    final Class<CmpV> cmpv = CmpV.class;

    // swap operands: move count() to the left
    check("1 eq count((1 to 6)[. > 3])", false, empty(cmpv));
    check("3 eq count((1 to 6)[. > 3])", true, empty(cmpv));

    // operand may yield no item: comparison is preserved
    check("head((1 to 2)[. > 5]) eq 1", "", exists(cmpv));

    // operands yield at least one item: result is a single boolean
    check("(1, (1 to 2)[. > 5]) eq 1", true, type(cmpv, "xs:boolean"));
  }

  /** Checks {@link CmpSR} optimizations. */
  @Test public void cmpSR() {
    check("<a>5</a>[text() > '1' and text() < '9']", "<a>5</a>", count(CmpSR.class, 1));
    check("<a>5</a>[text() > '1' and text() < '9' and <b/>]", "<a>5</a>", count(CmpSR.class, 1));
    check("<a>5</a>[text() > '1' and . < '9']", "<a>5</a>", count(CmpSR.class, 2));

    // flatten predicate: exists(E[text() > '1']) → E/text() > '1'
    check(EXISTS.args(" <a>5</a>[text() > '1']"), true, root(CmpSR.class),
        empty(IterFilter.class));

    // GH-2194: String range comparisons including/excluding min/max values
    check("<a>X</a>[. <= 'X' and . >= 'X' ]", "<a>X</a>", count(CmpSimpleG.class, 1));
    check("<a>X</a>[. <= 'X' and . >  'X' ]", "", empty());
    check("<a>X</a>[. <  'X' and . >= 'X' ]", "", empty());
    check("<a>X</a>[. <  'X' and . >  'X' ]", "", empty());

    check("<a>X</a>[. <= 'W' and . >= 'W' ]", "", count(CmpSimpleG.class, 1));
    check("<a>X</a>[. <= 'W' and . >  'W' ]", "", empty());
    check("<a>X</a>[. <  'W' and . >= 'W' ]", "", empty());
    check("<a>X</a>[. <  'W' and . >  'W' ]", "", empty());

    check("<a>X</a>[. <= 'Y' and . >= 'Y' ]", "", count(CmpSimpleG.class, 1));
    check("<a>X</a>[. <= 'Y' and . >  'Y' ]", "", empty());
    check("<a>X</a>[. <  'Y' and . >= 'Y' ]", "", empty());
    check("<a>X</a>[. <  'Y' and . >  'Y' ]", "", empty());

    check("<a>X</a>[. <  'X' and . <  'XX']", "", count(CmpSR.class, 1));
    check("<a>X</a>[. >= 'X' and . >= 'XX']", "", count(CmpSR.class, 1));

    check("<a>X</a>[.  = 'X' and .  < 'Y' ]", "<a>X</a>", count(CmpSimpleG.class, 1));
    check("<a>X</a>[. <= 'X' and .  < 'Y' ]", "<a>X</a>", count(CmpSR.class, 1));
    check("<a>X</a>[. >= 'X' and . <= 'Y' ]", "<a>X</a>", count(CmpSR.class, 1));
  }

  /** Comparison expressions. */
  @Test public void cmpG() {
    check("count(let $s := (-1, 1 to 99999) return $s[. = $s])", 100000, exists(CmpHashG.class));
  }

  /** Comparisons of values with different types. */
  @Test public void compare() {
    query("xs:QName('b') = attribute a { 'b' }", true);
    query("<a/> ! (x = (c, ()))", false);
    query("(4, 5, 6) < (1, 2)", false);
    query("(4, 5) < (1, 2, 3)", false);
    query("1234567890.12345678 = 1234567890.1234567", false);
    query("123456789012345678  = 123456789012345679", false);
    // GH-2112, GH-2115
    query("xs:decimal(1.13) gt xs:double(1.13)", true);
    query("xs:decimal(1.13) gt xs:float(1.13)", true);
    query("xs:decimal(1.13) le xs:double(1.13)", false);
    query("xs:decimal(1.13) le xs:float(1.13)", false);
    // GH-2113, GH-2114
    query("xs:float (1.13) ge xs:double(1.13)", false);
    query("xs:float (1.13) le xs:double(1.13)", true);
    query("xs:float (1.13) lt xs:double(1.13)", true);
    query("xs:float (1.13) gt xs:double(1.13)", false);
    query("xs:double(1.13) ge xs:float (1.13)", true);
    query("xs:double(1.13) le xs:float (1.13)", false);
    query("xs:double(1.13) lt xs:float (1.13)", false);
    query("xs:double(1.13) gt xs:float (1.13)", true);

    query("xs:hexBinary('41') = xs:untypedAtomic('41')", true);
    query("xs:untypedAtomic('41') = xs:hexBinary('41')", true);
    query("xs:untypedAtomic('41') <= xs:hexBinary('41')", true);
    query("xs:hexBinary('41') <= xs:untypedAtomic('41')", true);

    query("string-join(replicate(1, 40)) -> (xs:float(.) = <x>{ . }</x>)", false);
    query("string-join(replicate(1, 40)) -> (<x>{ . }</x> = xs:float(.))", false);
    query("string-join(replicate(1, 40)) -> (xs:double(.) = <x>{ . }</x>)", true);
    query("string-join(replicate(1, 40)) -> (<x>{ . }</x> = xs:double(.))", true);
  }

  /** If expressions. */
  @Test public void ifExpr() {
    query("if(true()) then true() else false()", true);
    query("if(false()) then true() else false()", false);
    query("if(true() = true()) then true() else false()", true);
    query("if(boolean(<x/>) eq true()) then 1 else 2", 1);
    query("if(boolean(<x/>) ne true()) then 1 else 2", 2);
    query("if(boolean(<x/>) eq false()) then 1 else 2", 2);
    query("if(boolean(<x/>) ne false()) then 1 else 2", 1);
    query("if(boolean(<x/>) = true()) then 1 else 2", 1);
    query("if(boolean(<x/>) != true()) then 1 else 2", 2);
    query("if(boolean(<x/>) = false()) then 1 else 2", 2);
    query("if(boolean(<x/>) != false()) then 1 else 2", 1);
    error("if(<x/> = true()) then 1 else 2", FUNCCAST_X_X_X);
  }

  /** Range expressions. */
  @Test public void range() {
    query("count((1 to 10) ! (. to . + 9))", 100);
    query("count((1 to 10) ! (. to . - -9))", 100);
    query("count((1 to 100_000) ! (. to . + 9_999))", 1_000_000_000);
    query("count((-9223372036854775807 - 1) to 9223372036854775807)", Long.MAX_VALUE);
    query("head((-9223372036854775807 - 1) to 9223372036854775807)", Long.MIN_VALUE);

    // operand is xs:integer? and empty at runtime, so the range is empty
    final String h = "head((1 to 10)[. > 100])";
    query("count(" + h + " to " + h + ")", 0);
    query("exists(" + h + " to " + h + ")", false);
    query("count(" + h + " to " + h + " + 10)", 0);
    query("exists(" + h + " to " + h + " + 10)", false);
    // single-item operands: result is still computed correctly
    query("let $i := (1 to 10)[. = 5] return count($i to $i)", 1);
    query("let $i := (1 to 10)[. = 5] return count($i to $i + 4)", 5);
  }

  /** Checks {@link Union} optimizations. */
  @Test public void union() {
    check("<_><a/></_>/(a union a)", "<a/>",
        empty(Union.class), type(IterPath.class, "element(a)*"));
    check("<_><a/></_>/(a union b)", "<a/>",
        empty(Union.class), type(IterPath.class, "(element(a)|element(b))*"));
    check("<_><a/></_>/(*:a union *:b)", "<a/>",
        empty(Union.class), type(IterPath.class, "(element(*:a)|element(*:b))*"));
    check("<_><a/></_>/(Q{}a union Q{}b)", "<a/>",
        empty(Union.class));
    check("declare namespace a = 'A'; declare namespace b = 'A'; " +
        "<_><a:a/></_>/(a:a union b:a)", "<a:a xmlns:a=\"A\"/>",
        empty(Union.class));
    check("declare namespace a = 'A'; declare namespace b = 'B'; " +
        "<_><a:a/></_>/(a:a union b:a)", "<a:a xmlns:a=\"A\"/>",
        empty(Union.class), type(IterPath.class, "(element(a:a)|element(b:a))*"));
    check("declare namespace a = 'A'; declare namespace b = 'B'; " +
        "<_><b:a/></_>/(a:a union b:a)", "<b:a xmlns:b=\"B\"/>",
        empty(Union.class), type(IterPath.class, "(element(a:a)|element(b:a))*"));
    check("<_><a/></_>/(a union *)", "<a/>",
        empty(Union.class), type(IterPath.class, "element()*"));
    check("<_><a/></_>/(* union a)", "<a/>",
        empty(Union.class), type(IterPath.class, "element()*"));
    check("<_><a/></_>/(*:a union *)", "<a/>",
        empty(Union.class), type(IterPath.class, "element()*"));
    check("<_><a/></_>/(* union *:a)", "<a/>",
        empty(Union.class), type(IterPath.class, "element()*"));
    check("<_><a/></_>/(* union Q{uri}a)", "<a/>",
        empty(Union.class), type(IterPath.class, "element()*"));
  }

  /** Checks {@link Intersect} optimizations. */
  @Test public void intersect() {
    // empty operand: the result is always empty
    check("<a/> intersect ()", "", empty());
    // duplicate operands are removed
    check("<x><a/></x>[a intersect a]", "<x><a/></x>", empty(Intersect.class));

    query("<a/> intersect <b/>", "");
    query("<a/> ! (. intersect (., <b/>))/name()", "a");

    // merge node tests
    check("<_><a/></_>/(node() intersect * intersect a)", "<a/>",
        empty(Intersect.class), type(IterPath.class, "element(a)*"));
    check("<_><a/></_>/(a intersect * intersect node())", "<a/>",
        empty(Intersect.class), type(IterPath.class, "element(a)*"));
    check("<_><a/></_>/(a intersect b)", "", empty());

    // GH-2599: operands with combined node tests
    query("let $in := <doc><b/></doc> return $in/(a, b) intersect $in/(b, c)", "<b/>");
    query("let $in := <doc><b/></doc> return $in/(a, b) intersect $in/(c, b)", "<b/>");
    query("let $in := <doc><b/></doc> return $in/(b, a) intersect $in/(b, c)", "<b/>");
    query("let $in := <doc><b/></doc> return $in/(b, a) intersect $in/(c, b)", "<b/>");
    query("let $in := <doc><b/></doc> return $in/(b, a) intersect $in/b", "<b/>");
    query("let $in := <doc><b/></doc> return $in/b intersect $in/(b, c)", "<b/>");
    query("let $in := <doc><b/></doc> return $in/a intersect $in/b", "");
    query("let $in := <doc><b/></doc> return $in/(a, d) intersect $in/(b, c)", "");
  }

  /** Checks {@link Except} optimizations. */
  @Test public void except() {
    check("<_><a/></_>/(* except text())", "<a/>",
        empty(Except.class), type(IterPath.class, "element()*"));
    check("<_><a/></_>/(a except b)", "<a/>",
        empty(Except.class), type(IterPath.class, "element(a)*"));
    check("<_><a/></_>/(node() except * except a)", "",
        count(Except.class, 1), type(MixedPath.class, "node()*"));
    check("<_><a/></_>/(a except *)", "", empty());

    query("count(<a/> except <b/>)", 1);
    // 'except' and 'intersect' have the same precedence and are evaluated from left to right
    query("<a/> intersect <b/> except <c/>", "");
    query("<a/> except <b/> intersect <c/>", "");
  }

  /** Checks the pre-evaluation of typeswitch expressions. */
  @Test public void typeswitch() {
    check("typeswitch(1) case xs:integer return 'int' default return 'other'", "int",
        root(Str.class));
    check("typeswitch('x') case xs:integer return 'int' default return 'other'", "other",
        root(Str.class));
    check("typeswitch(()) case empty-sequence() return 'empty' default return 'other'", "empty",
        root(Str.class));
  }

  /** Checks the simplification of catch clauses. */
  @Test public void catchClauses() {
    // clauses after a wildcard test are removed
    check("try { <a>x</a> + 1 } catch * { 'w' } catch err:FORG0001 { 'a' }", "w",
        root(Str.class));
    // repeated tests are removed
    check("try { <a>x</a> + 1 } catch err:FORG0001 { 'a' } catch err:FORG0001 { 'b' }", "a",
        root(Str.class));

    // a wildcard test supersedes the other tests of its clause
    check("try { <a>x</a> + 1 } catch err:FOAR0002 | * { 'a' }", "a", root(Str.class));
    check("try { <a>x</a> + 1 } catch * | err:FOAR0002 { 'a' }", "a", root(Str.class));
    // clauses without a wildcard test are left alone
    check("try { <a>x</a> + 1 } catch err:FORG0001 | err:FOAR0002 { 'a' }", "a",
        root(Str.class));
    check("try { <a>x</a> + 1 } catch err:FOAR0002 | err:FOAR0003 { 'a' } catch * { 'b' }", "b",
        root(Str.class));

    // inline a variable into a catch clause
    check("let $x := 'v' return try { <a>x</a> + 1 } catch * { $x }", "v", root(Str.class));
    // an error raised while inlining is deferred to runtime
    error("let $x := 'z' return try { <a>x</a> + 1 } catch * { xs:integer($x) }", FUNCCAST_X_X);

    // try expression is retained: the variable is inlined into the clause itself
    check("let $x := 'v' return try { (1 to 2)[. = 1] + 1 } catch * { $x }", 2,
        exists(Try.class));
    // an error raised while inlining the clause is deferred to the clause
    check("let $x := 'z' return try { (1 to 2)[. = 1] + 1 } catch * { xs:integer($x) }", 2,
        exists(Try.class));

    // error values are inlined if the error is known at compile time
    check("try { 1 div 0 } catch * { $err:code }", "#err:FOAR0001", root(QNm.class));
    check("try { 1 div 0 } catch * { $err:description }", "1 cannot be divided by zero.",
        root(Str.class));

    // error tests must be namespace-qualified
    error("try { 1+'' } catch XPTY0004 { 1 }", NONUMBER_X_X);
    query("try { 1+'' } catch err:XPTY0004 { 1 }", 1);
    query("try { 1+'' } catch *:XPTY0004 { 1 }", 1);
    query("try { 1+'' } catch err:* { 1 }", 1);
    query("try { 1+'' } catch * { 1 }", 1);
    query("declare function local:f($x) { try { 1 idiv $x } catch * { 1 } }; local:f(0)", 1);
  }

  /** Checks EBV optimizations. */
  @Test public void ebv() {
    query("not(<a/>[b])", true);
    query("empty(<a/>[b])", true);
    query("exists(<a/>[b])", false);

    query("not(<a/>[b = 'c'])", true);
    query("empty(<a/>[b = 'c'])", true);
    query("exists(<a/>[b = 'c'])", false);

    query("let $n := <n/> where $n[<a><b/><b/></a>/*] return $n", "<n/>");

    check("empty(<a>X</a>[text()])", null, "//@axis = 'child'");
    check("exists(<a>X</a>[text()])", null, "//@axis = 'child'");
    check("boolean(<a>X</a>[text()])", null, "//@axis = 'child'");
    check("not(<a>X</a>[text()])", null, "//@axis = 'child'");

    check("if(<a>X</a>[text()]) then 1 else 2", null, "//@axis = 'child'");
    check("<a>X</a>[text()] and <a/>", null, "//@axis = 'child'");
    check("<a>X</a>[text()] or <a/>", null, "//Bln = 'true'");
    check("<a>X</a>[text()] or <a/>[text()]", null, "//@axis = 'child'");
    check("for $a in <a>X</a> where $a[text()] return $a", null, "//@axis = 'child'");

    check("empty(<a>X</a>/.[text()])", null, "//@axis = 'child'");
  }

  /** Checks that operands behind a compile-time error are discarded. */
  @Test public void logicalError() {
    check("let $x := 'z' return (<a/> = 'a' and xs:integer($x))", false,
        exists(FnError.class), empty(Let.class));
  }

  /** Checks the optimizations of typeswitch groups. */
  @Test public void typeswitchGroup() {
    // the pre-evaluated condition is inlined into the matching group
    check("typeswitch(1) case $i as xs:integer return $i + 1 default return 0", 2,
        root(Itr.class));
    // an error raised while inlining into a group is deferred to that group
    check("let $x := 'z' return typeswitch(<a/>) case xs:integer return xs:integer($x) "
        + "default return 'ok'", "ok", root(Str.class));
  }

  /** Checks {@link SimpleMap} simplifications. */
  @Test public void simpleMap() {
    // boolean(@id ! true()) → boolean(@id)
    check("boolean(<a id='1'/>/@id ! true())", true, empty(SimpleMap.class));
    check("boolean(<a id='1'/>/@id ! false())", false, empty(SimpleMap.class));

    // single operand and context function: E ! data(.) → data(E)
    final String node = " (<a>1</a>)[. = " + wrap(1) + ']';
    check('(' + node + " ! data()) instance of xs:untypedAtomic", true, empty(SimpleMap.class));
    check('(' + node + " ! data(.)) instance of xs:untypedAtomic", true, empty(SimpleMap.class));
  }

  /** Checks {@link Treat} optimizations. */
  @Test public void treat() {
    // matching type: the check is discarded
    check("1 treat as xs:integer", 1, empty(Treat.class));
    // value with a non-matching type: the error is raised at compile time
    error("'a' treat as xs:integer", NOTREAT_X_X_X);
  }

  /** Untyped atomic items are simplified in a boolean context. */
  @Test public void atm() {
    check("(1 to 3)[xs:untypedAtomic('x')]", "1\n2\n3", root(RangeSeq.class));
    check("(1 to 3)[xs:untypedAtomic('')]", "", empty());
  }

  /** Checks the simplification of integer sequences. */
  @Test public void itrSeq() {
    // positional predicate: duplicates are removed and the positions are ordered
    check("(1 to 5)[(2, 1, 2)]", "1\n2", root(RangeSeq.class));
    // distinct-values: duplicates are removed, the original order is preserved
    query("distinct-values((2, 1, 2))", "2\n1");
  }

  /** Checks {@link Try} optimizations. */
  @Test public void tryExpr() {
    // an error that is raised by fn:error at compile time is caught right away
    check("try { error() } catch * { 'caught' }", "caught", root(Str.class));
    // an error raised while inlining into the try clause is caught as well
    check("let $x := 'z' return try { xs:integer($x) } catch * { 'caught' }", "caught",
        root(Str.class));
  }

  /** Checks the simplification of boolean sequences. */
  @Test public void blnSeq() {
    // all values are equal: single boolean
    check("distinct-values((true(), true(), true()))", true, root(Bln.class));
    check("distinct-values((false(), false()))", false, root(Bln.class));

    // both values occur: two-item sequence, order of the first value is kept
    check("distinct-values((true(), false(), true()))", "true\nfalse", root(BlnSeq.class));
    check("distinct-values((false(), true(), false()))", "false\ntrue", root(BlnSeq.class));
  }

  /** Checks the rewriting of constant FLWOR conditions. */
  @Test public void flworCondition() {
    // strings and zero are simplified to booleans by the operand itself
    check("for $i in 1 to 3 where 'x' return $i", "1\n2\n3", root(RangeSeq.class));
    check("for $i in 1 to 3 where 0 return $i", "", empty());
    check("for $i in 1 to 3 while 'x' return $i", "1\n2\n3", root(RangeSeq.class));
    check("for $i in 1 to 3 while 0 return $i", "", empty());

    // other values are replaced with their effective boolean value by the clause
    check("for $i in 1 to 3 where 1 return $i", "1\n2\n3", root(RangeSeq.class));
    check("for $i in 1 to 3 while 1 return $i", "1\n2\n3", root(RangeSeq.class));
  }

  /** Checks that empty sequences are eliminated and that singleton lists are flattened. */
  @Test public void list() {
    check("((), <x/>, ())", "<x/>", empty(List.class), empty(Empty.class), exists(CElem.class));

    // nested parentheses are flattened
    query("((( )  )    )", "");
    query("((( 1 )  )    )", 1);
    query("((( 1, 2 )  )    )", "1\n2");
    query("(1, (( 2,3 )  )    )", "1\n2\n3");
    query("(1, (( 2,3 )  ),4   )", "1\n2\n3\n4");

    // operands with a huge result size
    query("for $i in (1, 10000000000) return (1 to $i)[last()]", "1\n10000000000");
    query("for $i in (1, 10000000000) return (1 to $i, 'x')[last()]", "x\nx");
    query("for $i in (1, 10000000000) return count((1 to $i, 'x'))", "2\n10000000001");
  }

  /** Positional predicates. */
  @Test public void pos() {
    // check if positional predicates are pre-evaluated
    check("'a'[1]", "a", exists(Str.class));
    check("'a'[position() = 1]", "a", "exists(QueryPlan/Str)");
    check("'a'[position() = 1 to 2]", "a", "exists(QueryPlan/Str)");
    check("'a'[position() > 0]", "a", "exists(QueryPlan/Str)");
    check("'a'[position() < 2]", "a", "exists(QueryPlan/Str)");
    check("'a'[position() >= 1]", "a", "exists(QueryPlan/Str)");
    check("'a'[position() <= 1]", "a", "exists(QueryPlan/Str)");

    // check if positional predicates are rewritten to utility functions
    check("for $i in (1, 2) return 'a'[$i]", "a", root(Str.class));
    check("for $i in (1, 2) return 'a'[position() = $i]", "a", root(Str.class));
    check("for $i in (1, 2) return 'a'[position() = $i to $i]", "a", root(Str.class));

    check("for $i in (1, 2)[. > 0] return 9[position() = $i to 1]", 9, root(DualMap.class));

    check("for $i in (1, 2)[. > 0] return 9[position() = $i to $i + 1]", 9, exists(_UTIL_RANGE));
    check("for $i in (1, 2)[. > 0] return 9[position() >= $i]", 9, exists(_UTIL_RANGE));
    check("for $i in (1, 2)[. > 0] return 9[position() > $i]", "", exists(_UTIL_RANGE));
    check("for $i in (1, 2)[. > 0] return 9[position() <= $i]", "9\n9", exists(_UTIL_RANGE));
    check("for $i in (1, 2)[. > 0] return 9[position() < $i]", 9, exists(_UTIL_RANGE));

    // check if positional predicates are rewritten to utility functions
    String seq = " (0, 1, 2, 3, 3, 4, 5) ";
    check("for $i in" + seq + "return ('a', 'b')[$i]",
        "a\nb", exists(StrSeq.class));
    check("for $i in" + seq + "return ('a', 'b')[position() = $i]",
        "a\nb", exists(StrSeq.class));
    check("for $i in" + seq + "return ('a', 'b')[position() = $i and position() = $i]", "a\nb",
        exists(StrSeq.class));
    check("for $i in (3, 5, 7, 8, 11, 13) return ('a', 'b')[position() = $i and position() = $i]",
        "", empty());

    check("for $i in" + seq + "return ('a', 'b')[$i][$i]",
        "a", count(ITEMS_AT, 2));
    check("for $i in" + seq + "return ('a', 'b')[position() = $i][position() = $i]",
        "a", count(ITEMS_AT, 2));

    // check if positional predicates are rewritten to utility functions
    seq = " (1, 1.1, 1.9, 2, 2.1, 2.2, 2.1, 2.2) ";
    check("for $i in" + seq + "return ('a', 'b')[position() >= $i]",
        "a\nb\nb\nb\nb", exists(_UTIL_RANGE));
    check("for $i in" + seq + "return ('a', 'b')[position() > $i]",
        "b\nb\nb", exists(_UTIL_RANGE));
    check("for $i in" + seq + "return ('a', 'b')[position() <= $i]",
        "a\na\na\na\nb\na\nb\na\nb\na\nb\na\nb", exists(_UTIL_RANGE));
    check("for $i in" + seq + "return ('a', 'b')[position() < $i]",
        "a\na\na\na\nb\na\nb\na\nb\na\nb", exists(_UTIL_RANGE));

    // check if multiple positional predicates are rewritten to utility functions
    check("for $i in" + seq + "return ('a', 'b')[position() < $i][position() < $i]",
        "a\na\na\na\nb\na\nb\na\nb\na\nb", count(_UTIL_RANGE, 2));

    // check if positional predicates are merged and rewritten to utility functions
    check("for $i in" + seq + "return ('a', 'b')[position() >= $i and position() <= $i]", "a\nb",
        exists(ITEMS_AT));
    check("for $i in" + seq + "return ('a', 'b')[position() <= $i and position() >= $i]",
        "a\nb", exists(ITEMS_AT));
    check("for $i in" + seq + "return ('a', 'b')[position() > $i and position() < $i]",
        "", exists(_UTIL_RANGE));
    check("for $i in" + seq + "return ('a', 'b')[position() < $i and position() > $i]",
        "", exists(_UTIL_RANGE));

    // no rewriting possible (conflicting positional predicates)
    check("for $i in" + seq + "return ('a', 'b')[position() = $i and position() = $i + 1]",
        "", exists(CachedFilter.class));
    check("for $i in" + seq + "return ('a', 'b')[position() >= $i and position() > $i]",
        "b\nb\nb", exists(CachedFilter.class));
    check("for $i in" + seq + "return ('a', 'b')[position() >= $i and position() >= $i + 1]",
        "b", exists(CachedFilter.class));
    check("for $i in" + seq + "return ('a', 'b')[position() < $i and position() < $i + 1]",
        "a\na\na\na\nb\na\nb\na\nb\na\nb", exists(CachedFilter.class));

    check("(<a/>, <b/>)[last()]",
        "<b/>", root(CElem.class));
    check("(<a/>, <b/>[. = ''])[last()]",
        "<b/>", count(FOOT, 1));
    check("(<a/>, <b/>)[position() > 1 and position() < 3]",
        "<b/>", root(CElem.class));
    check("(<a/>, <b/>[. = ''])[position() > 1 and position() < 3]",
        "<b/>", root(IterFilter.class));
    check("(<a/>[. = ''], <b/>)[position() > 1 and position() < 3]",
        "<b/>", count(ITEMS_AT, 1));
    check("(<a/>, <b/>)[position() > 1 and position() < 3 and <b/>]",
        "<b/>", root(CElem.class));
    check("(<a/>, <b/>[. = ''])[position() > 1 and position() < 3 and <b/>]",
        "<b/>", root(IterFilter.class));
    check("(<a/>[. = ''], <b/>)[position() > 1 and position() < 3 and <b/>]",
        "<b/>", count(ITEMS_AT, 1));
    check("(<a/>, <b/>)[position() > 1 and position() < 4]",
        "<b/>", root(CElem.class));
    check("(<a/>, <b/>[. = ''])[position() > 1 and position() < 4]",
        "<b/>", empty(List.class), root(IterFilter.class));

    check("<a/>[position() >= last() - 1]",
        "<a/>", root(CElem.class));
    check("<a/>[position() > last() - 2]",
        "<a/>", root(CElem.class));
    check("<a/>[position() = 0 to 9223372036854775807]",
        "<a/>", root(CElem.class));
    check("<a/>[position() = -1 to 9223372036854775807]",
        "<a/>", root(CElem.class));

    // GH-2219: Bug on node selection with position()
    check("<a><b/></a>/*[position()  = position()]", "<b/>", empty(POSITION));
    check("<a><b/></a>/*[position() >= position()]", "<b/>", empty(POSITION));
    check("<a><b/></a>/*[position() <= position()]", "<b/>", empty(POSITION));
    check("<a><b/></a>/*[position() >  position()]", "", empty());
    check("<a><b/></a>/*[position() <  position()]", "", empty());
    check("<a><b/></a>/*[position() != position()]", "", empty());

    // GH-2224: Unexpected exception, arithmetic operations with positional expression
    check("document { <X/> }//X[not(position() * 2 = last())]", "<X/>");
    check("document { <X/> }//X[not(position() + position() = last())]", "<X/>");

    // GH-2747: Avoid rewriting positional range predicates to util:range if bounds may be empty
    final String empty = "let $x := 0\n"
        + "let $empty := head(for $c at $i in $x where $c != 0 return $i)\n"
        + "return ";
    query(empty + "$x[position() >= $empty]", "");
    query(empty + "$x[position() >  $empty]", "");
    query(empty + "$x[position() <= $empty]", "");
    query(empty + "$x[position() <  $empty]", "");
  }

  /** Predicates. */
  @Test public void preds() {
    // context value: rewrite if root is of type string or node
    check("('s', 't')[.]", "s\nt", exists(ContextValue.class));
    check("<a/>[.]", "<a/>", exists(CElem.class), empty(ContextValue.class));
    check("<a/>[.][.]", "<a/>", exists(CElem.class), empty(ContextValue.class));
    check("<a/>/self::*[.][.]", "<a/>", empty(ContextValue.class));
    check("<a/>/self::*[.][.]", "<a/>", empty(ContextValue.class));
    check("('a', 'b')[. ! position()]", "a", exists(Pipeline.class));
    check("(1, 0)[.]", 1, exists(ContextValue.class));
    error("true#0[.]", ARGTYPE_X_X_X);
    error("(true#0, false#0)[.]", ARGTYPE_X_X_X);

    // map expression
    check("'s'['s' ! <a/>]", "s", root(Str.class));
    check("'s'['s' ! <a/>]", "s", root(Str.class));
    check("'s'['x' ! <a/> ! <b/>]", "s", root(Str.class));
    check("'s'['x' ! (<a/>, <b/>) ! <b/>]", "s", root(Str.class));
    check("'s'['x' ! " + wrapContext() + "[. = 'x']]", "s", root(If.class));

    // path expression
    check("let $a := <a/> return $a[$a/self::a]", "<a/>", empty(VarRef.class));
    check("let $a := <a/> return $a[$a]", "<a/>", empty(VarRef.class));

    // drop predicates that do not influence the existence of path results
    final String node = " <a><b><c/></b></a>";
    check(EXISTS.args(node + "/b[1]"), true, empty(IntPos.class));
    check(EMPTY.args(node + "/b[1]"), false, empty(IntPos.class));
    check(EXISTS.args(node + "/b[last()]"), true, empty(Pos.class));
    check(EXISTS.args(node + "/b[position() <= 2]"), true, empty(IntPos.class));
    check(BOOLEAN.args(node + "/b[1]"), true, empty(IntPos.class));
    check(node + "[b[1]]", "<a><b><c/></b></a>", empty(IntPos.class));

    // other positions, other steps and fn:count are not rewritten
    check(EXISTS.args(node + "/b[2]"), false, exists(IntPos.class));
    check(EXISTS.args(node + "/b[1]/c"), true, exists(IntPos.class));
    check(COUNT.args(node + "/b[1]"), 1, exists(IntPos.class));
  }
}
