package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.fn.*;
import org.basex.query.up.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Test cases for FLWOR expressions.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class GFLWORTest extends SandboxTest {
  /** FLWOR expressions. */
  @Test public void flwor() {
    query("(for $i in 1 to 5 return $i)[3]", 3);
    query("(for $a in 1 to 5 for $b in 1 to 5 return $a * $b)[7]", 4);
    query("declare namespace x = 'X'; let $a := <a>0</a> let $b := $a return $b = 0", true);
    query("for $a in (1, 2) let $b := 'a' where $a = 1 return $a", 1);
    query("for $a in (1, 2) let $b := 'a'[$a = 1] return $a", "1\n2");
    query("for $a in (1, 2) let $b := 3 where $b = 4 return $a", "");
    query("for $a in (1, 2) let $b := 3[. = 4] return $a", "1\n2");
    query("for $a at $p in (1, 2) where $a = 2 return $p", 2);
  }

  /** For and let clauses. */
  @Test public void forLet() {
    query("for $a in 1 to 2 let $b := 3 return $b", "3\n3");
    query("for $a in 1 to 2 let $b := 3 let $c := 3 return $c", "3\n3");
    query("for $a in 1 to 2 let $b := 3 let $b := 4 return $b", "4\n4");
    query("for $a score $s in 1 let $s := 3 return $s", 3);
    query("for $a at $p in 1 let $s := $p return $s", 1);
  }

  /** Tests shadowing of outer variables. */
  @Test public void shadowTest() {
    assertEquals("<x>1</x>",
        query("for $a in for $a in <a>1</a> return $a/text() return <x>{ $a }</x>"));
  }

  /** Tests shadowing between grouping variables. */
  @Test public void groupShadowTest() {
    assertEquals("1", query("let $i := 1 group by $i, $i return $i"));
  }

  /** Positional optimization. */
  @Test public void posOptimizationTest() {
    assertEquals("<a/>", query("for $a at $p in (<a/>, <b/>)/. where $p < 2 return $a"));
  }

  /** Tests the relocation of a static let clause. */
  @Test public void moveTop() {
    check("let $b := <x>a</x> " +
        "for $i in 1 to 2 " +
        "let $m := $b " +
        "return <_>{ $m }</_>//text()",
        "a\na",
        empty(Let.class),
        empty(For.class),
        root(Pipeline.class),
        exists(REPLICATE)
    );
  }

  /** Tests the relocation of a let clause. */
  @Test public void moveMid() {
    check("let $seq := ('a', 'b', 'c') " +
        "for $i in 1 to count($seq) " +
        "for $j in $i + 1 to count($seq) " +
        "let $a as xs:string := $seq[$i] " +
        "return concat($i, $j, $a, $a)",
        "12aa\n13aa\n23bb",
        count(Let.class, 1),
        count(For.class, 1)
    );
  }

  /** Tests the relocation of a let clause. */
  @Test public void dontMove() {
    check("let $seq := ('a', 'b', 'c') " +
        "for $i in 1 to count($seq) " +
        "for $j in $i + 1 to count($seq) " +
        "let $b as xs:string := $seq[$j] " +
        "return concat($i, $j, $b, $b)",
        "12bb\n13cc\n23cc",
        "every $f in //For satisfies $f << //" + Util.className(Pipeline.class),
        empty(Let.class)
    );
  }

  /** Tests the relocation of a let clause. */
  @Test public void gh1236() {
    check("for $x in 1 to 2 " +
        "for $y in 1 to 2 " +
        "where $x + $y > 4 " +
        "let $z := $x + 1 " +
        "return $z",
        "",
        "empty((//Let, //Where))"
    );
  }

  /** Tests the relocation of a static let clause. */
  @Test public void moveFor() {
    check("let $x := <x/> " +
        "for $a in 1 to 2 " +
        "for $b in $x " +
        "return (<_>{ $b }</_>, $b)[1]/x",
        "<x/>\n<x/>",
        empty(Let.class),
        empty(For.class),
        root(Pipeline.class),
        exists(REPLICATE)
    );

    check("let $x := <x/> " +
        "for $a in 1 to 2 " +
        "for $b as element(x) in $x " +
        "return (<_>{ $b }</_>, $b)[1]/x",
        "<x/>\n<x/>",
        empty(Let.class),
        empty(For.class),
        root(Pipeline.class),
        exists(REPLICATE)
    );
  }

  /** Tests the relocation of a where clause. */
  @Test public void slideWhere() {
    check("for $i at $p in (1, 2)\n" +
        "where $i = 1\n" +
        "return $i",
        1,
        root(IterFilter.class), empty(Where.class)
    );

    check("for $i in 1 to 1000 " +
        "let $sum := sum(1 to $i * $i) " +
        "where $i lt 5 " +
        "return ($sum, $sum)[1]",
        "1\n10\n45\n136",
        empty(Where.class)
    );

    check("for $i at $p in 1 to 1000 " +
        "let $sum := sum(1 to $i * $i) " +
        "where $i lt 5 " +
        "return ($sum, $sum)[1]",
        "1\n10\n45\n136",
        empty(Where.class)
    );

    check("for $i score $s in 1 to 1000 " +
        "let $sum := sum(1 to $i * $i) " +
        "where $i lt 5 " +
        "return ($sum, $sum)[1]",
        "1\n10\n45\n136",
        empty(Where.class)
    );

    check("for $len in 1 to 3 " +
        "for sliding window $w in 1 to 3 start at $p when true() only " +
        "end at $q when $q - $p + 1 eq $len where $len > 2 return <window>{ $w }</window>",
        "<window>1 2 3</window>",
        "//For << //Window",
        exists("For/*[ends-with(name(), 'Filter')]")
    );
  }

  /** Tests if multiple successive where clauses are merged into one. */
  @Test public void mergeWheresTest() {
    check("let $rnd := " + _RANDOM_DOUBLE.args() + " where $rnd div 2 >= 0.2 where $rnd < 0.5 " +
        "where round(2 * $rnd) eq 1 return $rnd",
        null,
        root(IterFilter.class)
    );
  }

  /** Tests if where clauses are converted to predicates where applicable. */
  @Test public void whereToPred() {
    check("for $i in 1 to 10 where <x/>[$i] and $i < 3 return $i",
        1,
        exists("*[ends-with(name(), 'Filter')]/FnItemsAt")
    );
    check("for $i in 1 to 10 where (<a/>)[$i] return $i",
        1,
        exists("*[ends-with(name(), 'Filter')]/FnItemsAt")
    );
    check("for $i in 1 to 3 " +
        "where count(for $j in 1 to $i group by $k := $j mod 2 return $k) > 1 " +
        "return $i",
        "2\n3",
        empty(Where.class),
        exists("*[ends-with(name(), 'Filter')]")
    );
  }

  /** Tests if let clauses are moved out of any loop they do not depend on. */
  @Test public void slideLet() {
    check("for $i in 0 to 3, $j in 0 to 3 where (<x/>)[$i + $j] " +
        "let $foo := $i * $i return $foo * $foo",
        "0\n1",
        "every $let in //Let satisfies $let << exactly-one(//FnItemsAt)"
    );
    check("<x/>/(for $i in 1 to 3 let $x := . where $x return $x)",
        "<x/>",
        root(CElem.class)
    );
    check("for $len in 1 to 3 " +
        "for sliding window $w in 1 to 3 " +
        "  start at $p when true() only " +
        "  end at $q when $q - $p + 1 eq $len " +
        "let $x := $len div 2 " +
        "return count($w) div ($x + $x)",
        "1\n1\n1\n1\n1\n1",
        "//Let << //Window"
    );
  }

  /** Tests if let clauses are moved out of any loop they do not depend on. */
  @Test public void replicate() {
    check("for $r in 1 to 2 return (3, 4)",
        "3\n4\n3\n4",
        empty(For.class),
        exists(SingletonSeq.class)
    );
    check("for $r in 1 to 2 return (3, 4)[. = 5]",
        "",
        empty(For.class),
        exists(REPLICATE)
    );
    check("let $n := for $r in 1 to 2 return <_>3</_> return $n[1] is $n[2]",
        false,
        empty(For.class),
        exists(REPLICATE)
    );
    check("for $r in 1 to 2 return 3[. = 4]",
        "",
        empty()
    );
  }

  /** Tests if multiple let clauses are all moved to their optimal position. */
  @Test public void slideMultipleLets() {
    check("for $i in 1 to 2 " +
        "for $j in 1 to 2 " +
        "let $a as xs:integer := 3 * $i " +
        "let $b as xs:integer := 2 * $i " +
        "return $a * $a + $b * $b",
        "13\n13\n52\n52",
        exists("For[every $let in //Let satisfies . << $let]"),
        count(For.class, 1),
        count(Let.class, 1)
    );
  }

  /** Tests if where clauses containing nondeterministic expressions are left alone. */
  @Test public void dontSlideWhereNDT() {
    check("for $x in 1 to 100 where " + _RANDOM_DOUBLE.args() + " gt 0.5 return $x",
        null,
        "exactly-one(//Where) >> exactly-one(//For)"
    );
  }

  /** Tests if let clauses containing nondeterministic expressions are left alone. */
  @Test public void dontSlideLetNDT() {
    check("for $i in 1 to 10 let $rnd := " + _RANDOM_DOUBLE.args() + " return $i * $rnd",
        null,
        empty(Let.class),
        exists(Pipeline.class)
    );
  }

  /** Tests if let clauses containing node constructors are left alone. */
  @Test public void dontSlideLetCNS() {
    check("for $i in 1 to 10 let $x := <x/> return ($i, $x, $x)",
        null,
        empty(Let.class),
        exists(DualIterMap.class)
    );
  }

  /** Tests is where clauses are rewritten to if. */
  @Test public void whereToIfTest() {
    check("(1 to 6) ! (for $j in 1 to 5 where . eq 1 return $j)",
        "1\n2\n3\n4\n5",
        exists(IterFilter.class),
        empty(GFLWOR.class)
    );
    check("(1 to 6) ! (for $j at $p in 1 to 5 where . eq 1 return $j * $p)",
        "1\n4\n9\n16\n25",
        exists(IterFilter.class),
        exists(GFLWOR.class)
    );
    check("let $x := 0 where $x !=" + wrap(0) + "return 42 idiv $x", "",
        exists(If.class),
        "//If/*[2]/@name = 'error'");
  }

  /** Tests if {@code for $x in E return $x} is rewritten to {@code E} inside FLWORs. */
  @Test public void inlineForTest() {
    check("let $x := <x>5</x> for $i in 1 to $x return $i",
        "1\n2\n3\n4\n5",
        empty(For.class)
    );
  }

  /** Tests if {@link And} expressions inside {@code where} are split. */
  @Test public void splitWhereTest() {
    check("for $i in 1 to 5, $j in 1 to 5 where $i < 3 and $j < 3 return $i * $j",
        "1\n2\n2\n4",
        empty(Where.class),
        "every $for in //For satisfies exists($for/*[ends-with(name(), 'Filter')])"
    );
  }

  /** Tests if redundant FLWOR expressions are eliminated. */
  @Test public void eliminateFLWORTest() {
    check("let $x := (23, 42) where true() for $y in $x return $y",
        "23\n42",
        empty(GFLWOR.class)
    );
  }

  /** FLWOR expressions containing updates or nondeterminism. */
  @Test public void updatesNdt() {
    check("copy $x := <x/> modify (" +
        "  for $i in 1 to 6" +
        "  let $y := <y>{ $i }</y>" +
        "  return insert node $y into $x" +
        ") return $x",
        "<x><y>1</y><y>2</y><y>3</y><y>4</y><y>5</y><y>6</y></x>",
        empty(GFLWOR.class),
        exists(DualMap.class),
        exists(Insert.class)
    );
    check("for $i in 1 to 3 let $x := $i * $i return error()",
        null,
        root(REPLICATE)
    );
  }

  /** Tests if empty FLWOR expressions are replaced by the empty sequence. */
  @Test public void eliminateEmptyFLWORTest() {
    // literal false()
    check("for $x in 1 to 100 where false() return $x",
        "", empty()
    );

    // empty return clause
    check("for $x in 1 to 100 return ()",
        "", empty()
    );

    // value with ebv == false in where
    check("for $x in 1 to 100 where () return $x",
        "", empty()
    );
  }

  /** Preserve empty updating results. */
  @Test public void gh1636() {
    check("<a>x</a> update {" +
        " let $fn := function($n) { delete node $n/text() }" +
        " for $f in ($fn, $fn)" +
        " return updating $f(.)" +
        "}", "<a/>", exists(GFLWOR.class)
    );
  }

  /** Ensures that nondeterministic expressions are rewritten to a simple map. */
  @Test public void inlineNDTTest() {
    check("let $rnd := " + _RANDOM_DOUBLE.args() + " return (1 to 10) ! $rnd",
        null,
        empty(Let.class),
        root(Pipeline.class),
        exists(REPLICATE)
    );
  }

  /** Checks that clauses can be removed during inlining. */
  @Test public void gh1150() {
    check("for $i in 1 to xs:integer(" + _RANDOM_DOUBLE.args() + ") "
        + "let $x := 'does-not-exist.xml' "
        + "for $x in try { doc($x) } catch * { 1 }"
        + "let $x := count($x) "
        + "return $x",
        "",
        root(DualMap.class),
        empty(GFLWOR.class)
    );
  }

  /** Tests flattening. */
  @Test public void flattening1() {
    check("for $a at $p in " +
        "  for $x in (1 to 6) " +
        "  return $x * 2 " +
        "return $a",
        "2\n4\n6\n8\n10\n12",
        root(DualMap.class)
    );
    check("for $a at $p in " +
        "  for $x in (1 to 6) " +
        "  return $x * 2 " +
        "return $p",
        "1\n2\n3\n4\n5\n6",
        root(RangeSeq.class)
    );
  }

  /** Tests flattening. */
  @Test public void flattening2() {
    check("for $a in (1 to 2) return let $b := <a>1</a> return $b + 1", "2\n2",
        empty(VarRef.class));
  }

  /** Tests inlining. */
  @Test public void inlineLetTest() {
    check("let $x := 1 let $b := $x + 2 return $b + 3", 6, empty(Let.class));
    check("let $x := <x>0</x> let $b := $x/text() return $b + 1", 1, empty(Let.class));
    check("let $x := <x>false</x> let $b as xs:boolean := $x/text() return $b", false,
        empty(Let.class));
    error("let $x := <x>42</x> let $b as xs:boolean := $x/text() return $b", FUNCCAST_X_X_X);
  }

  /** Tests flattening. */
  @Test public void gh1684() {
    query(
        "let $a := <x/>\n" +
        "let $b := $a/.\n" +
        "return\n" +
        "  for $c in $b\n" +
        "  where $c/(. = '')\n" +
        "  return $c",
        "<x/>"
    );
  }

  /** Inlining of positional variable. */
  @Test public void posVar() {
    check("for $v at $p in (1, 2) where $p = 2 return $v", 2, root(Itr.class));

    final String seq = "(1 to 6)[. > 0]";
    check("for $v at $p in " + seq + " while $p <= 3 return $v", "1\n2\n3", empty(While.class));
    check("for $v at $p in " + seq + " while $p >= 2 return $v", "", exists(While.class));
    check("for $o in 1 to 2 for $v at $p in " + seq + " while $p <= 2 return $v", "1\n2",
        exists(While.class));
    check("for $v at $p in " + seq + " while $v < 2 where $p = 3 return $v", "",
        exists(While.class));
    check("for $v at $p in " + seq + " for $w in $v where $w = 1 where $p = 3 return $v", "",
        empty(Where.class));
    check("let $e := <a/>/b for $v allowing empty at $p in $e where $p = 1 return 1", "",
        exists(Where.class));

    // positional variables in arbitrary where clauses
    check("for $v at $p in " + seq + " where $p mod 2 = 0 return $v", "2\n4\n6",
        empty(GFLWOR.class));
    check("for $v at $p in (3, 2, 1, 4)[. > 0] where $v = $p return $v", "2\n4",
        empty(GFLWOR.class));
    check("for $v at $p in " + seq + " where $p - 1 return $v", "2\n3\n4\n5\n6",
        empty(GFLWOR.class));
    check("for $v at $p in " + seq + " where (fn() { $p = 2 })() return $v", 2);
    check("for $v at $p in " + seq + " where $p mod 3 = 0 return $p", "3\n6",
        exists(Where.class));
    check("<x/> ! (for $v at $p in " + seq + " where $p = 1 and . instance of element() "
        + "return $v)", 1);
  }

  /** Group by to distinct-values. */
  @Test public void groupToDistinct() {
    final String seq = "(1 to 10)[. > 0]";
    check("for $x in " + seq + " let $k := $x mod 3 where $x > 4 group by $k "
        + "order by $k descending return $k * 10", "20\n10\n0",
        exists(DISTINCT_VALUES), empty(GroupBy.class));
    check("for $x in " + seq + " for $y in 1 to 2 group by $k := $x * $y mod 4 "
        + "where $k > 0 return $k", "1\n2\n3",
        exists(DISTINCT_VALUES), empty(GroupBy.class));
    check("for $x in " + seq + " group by $k := $x mod 2 order by $k return count($x)",
        "5\n5", exists(GroupBy.class));

    // constant keys
    query("for $x in " + seq + " group by $k := 1 return ($k, count($x))", "1\n10");
    query("for $x in " + seq + " where $x > 10 group by $k := 1 return count($x)", "");
    query("for $x in " + seq + " let $y := () group by $k := 1 return count($y)", 0);
  }

  /** Unused count clauses. */
  @Test public void unusedCount() {
    final String seq = "(3, 1, 2)[. > 0]";
    check("for $i in " + seq + " order by $i count $c return $i", "1\n2\n3", empty(Count.class));
    check("for $i in " + seq + " order by $i count $c return $c * 10 + $i", "11\n22\n33",
        empty(Count.class));
  }

  /** Allowing empty. */
  @Test public void allowingEmpty() {
    check("for $x allowing empty in () return $x", "", empty());
    check("for $x allowing empty in void(1) return $x", "", root(VOID));

    check("for $x allowing empty in head(<x><a/><a/></x>/a) return (name($x), 'x')", "a\nx",
        empty(GFLWOR.class));
    check("for $x allowing empty in head(<x/>/a) return (name($x), 'x')", "\nx",
        empty(GFLWOR.class));
    check("for $x allowing empty at $p in head(<x/>/a) return $p", 0, exists(For.class));
  }

  /** Merge order by clause with for clause. */
  @Test public void orderByMerge() {
    final String seq = "(<a>1</a>, <b>1</b>, <c>2</c>, <d>1</d>)[text()]";
    check("for $x in " + seq + " stable order by $x return name($x)", "a\nb\nd\nc",
        exists(SORT), empty(OrderBy.class));
    check("for $x in " + seq + " stable order by $x descending return name($x)", "c\na\nb\nd",
        exists(SORT_BY), empty(REVERSE), empty(OrderBy.class));

    // keys that only depend on the for variable
    final String nodes = "(<a id='2'/>, <b id='3'/>, <c id='1'/>)";
    check("for $x in " + nodes + " order by $x/@id return name($x)", "c\na\nb",
        exists(SORT), empty(OrderBy.class));
    check("for $x in " + nodes + " order by $x/@id descending return name($x)", "b\na\nc",
        exists(SORT_BY), empty(OrderBy.class));
    check("for $x in " + nodes + " order by $x/@id return $x/@id ! string()", "1\n2\n3",
        exists(SORT), empty(OrderBy.class));
    error("for $x in (<a><i/><i/></a>, <b/>) order by $x/i return $x", INVTYPE_X);
    check("head(for $x in " + nodes + " order by xs:integer($x/@id) descending return $x)",
        "<b id=\"3\"/>", exists(HIGHEST), empty(OrderBy.class));

    // no merge: context-dependent key, coerced for variable
    check("<_/> ! (for $x in (2, 1) order by name() return $x)", "2\n1", exists(OrderBy.class));
    check("for $i as xs:integer in (<a>10</a>, <a>9</a>) ! data() order by $i return $i",
        "9\n10", exists(OrderBy.class));

    // no merge: window clause yields multiple tuples
    final String window = "for tumbling window $w in (1, 2) start when true() ";
    check(window + "for $i in (3 - $w, 5 - $w) order by $i return $i", "1\n2\n3\n4",
        exists(OrderBy.class), empty(SORT));
    check(window + "for $i at $p in (3 - $w, 5 - $w) order by $i return $i", "1\n2\n3\n4",
        exists(OrderBy.class), empty(SORT));
  }

  /** Merge for/let clauses. */
  @Test public void gh1995() {
    check("let $a := (<a/> | <b/>) for $b in $a/* return <c/> ! <d>{ $b, . }</d>", "",
        empty(Let.class), count(For.class, 1));
    check("for $a in (<a/> | <b/>) for $b in $a/* return <c/> ! <d>{ $b, . }</d>", "",
        count(For.class, 1));

    query("for $a in (<a/>, <b/>) for $b at $c in $a/self::* return $c", "1\n1");
    query("for $a in (<a/>, <b/>) for $b at $c in $a return $c", "1\n1");

    check("for $e in (<a/>, <b/>) let $n := $e/name() order by $n return $n", "a\nb", root(SORT));
    error("for $a in (1, 4, 2) let $i := (1, $a, 2) order by $i return $i + 1", INVTYPE_X);
  }

  /** Remove clauses that will never be executed. */
  @Test public void gh1999() {
    check("for $a in () return delete node a", "", empty());
    check("for $a in void(1) return delete node a", "", root(VOID));
  }

  /** Sliding windows. */
  @Test public void gh2252() {
    query("for sliding window $w in 1 to 3 start at $p when true()"
        + "only end when $p = 2 return <w>{ $w }</w>", "<w>2</w>");
  }

  /** Java call in copy/modify/return. */
  @Test public void gh2455() {
    query("declare namespace ll = \"java:java.util.LinkedList\";\n"
        + "declare variable $linkedList := ll:new();\n"
        + "\n"
        + "copy $test := <A>{ll:size($linkedList)}</A>\n"
        + "modify ()\n"
        + "return $test",
        "<A>0</A>");
  }

  /** Merge where clauses. */
  @Test public void mergeWhere() {
    check("for $i at $p in (1 to 4) where $i <= 3 return $p",
        "1\n2\n3", count(Where.class, 1));
    check("for $i at $p in (1 to 4) where $i <= 3 where $i >= 2 return $p",
        "2\n3", count(Where.class, 1));
    check("for $i at $p in (1 to 4) where $i <= 3 where $i >= 2 where $p < 5 return $p",
        "2\n3", count(Where.class, 1));

    check("for $i at $p in (1 to 4) where $i <= 3 where $p < 5 "
        + "for $j at $q in ($i to 2) return $q",
        "1\n2\n1", empty(Where.class));
    check("for $i at $p in (1 to 4) where $i <= 3 where $p < 5 "
        + "for $j at $q in ($i to 2) where $q < 5 return $q",
        "1\n2\n1", empty(Where.class));
    check("for $i at $p in (1 to 4) where $i <= 3 where $p < 5 "
        + "for $j at $q in ($i to 2) where $j > 1 where $p < 5 return $q",
        "2\n1", count(Where.class, 1));
  }

  /** Merge where clauses. */
  @Test public void mergeWhile() {
    check("for $i at $p in (1 to 4) while $i <= 3 return $p",
        "1\n2\n3", count(While.class, 1));
    check("for $i at $p in (1 to 4) while $i <= 3 while $i >= 0 return $p",
        "1\n2\n3", count(While.class, 1));
    check("for $i at $p in (1 to 4) while $i <= 3 while $i >= 0 while $p < 5 return $p",
        "1\n2\n3", count(While.class, 1));

    check("for $i at $p in (1 to 4) while $i <= 3 while $p < 5 "
        + "for $j at $q in ($i to 2) return $q",
        "1\n2\n1", count(While.class, 1));
    check("for $i at $p in (1 to 4) while $i <= 3 while $p < 5 "
        + "for $j at $q in ($i to 2) while $q < 5 return $q",
        "1\n2\n1", count(While.class, 2));
    check("for $i at $p in (1 to 4) while $i <= 3 while $p < 5 "
        + "for $j at $q in ($i to 2) while $j >= 1 while $p < 5 return $q",
        "1\n2\n1", count(While.class, 2));
  }

  /** While clause optimizations. */
  @Test public void whilee() {
    check("for $i in 1 to 6 while true() return $i", "1\n2\n3\n4\n5\n6", root(RangeSeq.class));
    check("for $i in 1 to 6 while false() return $i", "", empty());

    check("for $i in 1 to 6 let $j := text { $i } while $i < 2 return ($j, $j + 1)",
        "1\n2", exists(Pipeline.class));
    check("let $a := <a/>[text()] while $a return $a", "", root(IterFilter.class));
  }

  /** Trace clause. */
  @Test public void trace() {
    // pass-through: result is unchanged
    query("for $i in 1 to 3 trace $i return $i", "1\n2\n3");
    query("for $i in 1 to 3 trace $i * $i return $i", "1\n2\n3");
    // error in trace expression does not abort the FLWOR
    query("for $i in 1 to 3 trace (if($i = 2) then error() else $i) return $i * 10",
        "10\n20\n30");
    // not optimized away (NDT)
    check("for $i in 1 to 3 trace $i return $i", "1\n2\n3", exists(Trace.class));
    check("for $i in 1 to 1 trace 'x' return $i", 1, exists(Trace.class));
  }

  /** Do not flatten nested FLWOR expressions with while clauses. */
  @Test public void gh2634() {
    query("for $max in (1 to 10) return (for $a in 0 to $max while $a < 1 return $a)",
        "0\n0\n0\n0\n0\n0\n0\n0\n0\n0");
    query("for $x in (1, 2) return (for $a in (1, 2, 3) while $a < 3 return $a)",
        "1\n2\n1\n2");
    query("for $x in (1, 2) for $y in (for $a in (1, 2, 3) while $a < 3 return $a) return ($x, $y)",
        "1\n1\n1\n2\n2\n1\n2\n2");
  }

  /** Errors raised at compile time are deferred if an outer clause can prevent them. */
  @Test public void clauseError() {
    final String none = "(1 to 1000000)[. < 1]";

    // failing clause: the error is bound to the return clause
    check("count(for $i in " + none + " let $x := xs:integer('z') return $x)", 0,
        exists(FnError.class));
    // failing return clause
    check("count(for $i in " + none + " return xs:integer('z'))", 0, exists(FnError.class));
    // no outer clause can prevent the error
    error("for $i in (1 to 2) let $x := xs:integer('z') return $x", FUNCCAST_X_X);
  }

  /** Simplifications that discard the FLWOR expression. */
  @Test public void simplify() {
    // for $_ allowing empty in () return $_ → ()
    check("for $x allowing empty in () return $x", "", empty());
    // exactly one iteration, no referenced variables
    check("let $x := 1 return 'y'", "y", root(Str.class));
    // fixed number of iterations, no referenced variables
    check("for $i in 1 to 2 return 3", "3\n3", root(SingletonSeq.class));
  }

  /** Order by: NaN precedes all other numbers, regardless of the empty order. */
  @Test public void orderByNaN() {
    final String keys = "let $keys := { 'E': (), 'N': xs:double('NaN'), '1': 1, '2': 2 } "
        + "return string-join(for $k in map:keys($keys) order by $keys($k) ";
    query(keys + "empty greatest return $k)", "N12E");
    query(keys + "descending empty greatest return $k)", "E21N");
    query(keys + "empty least return $k)", "EN12");
    query(keys + "descending empty least return $k)", "21NE");
  }

  /** Order by: keys of common types. */
  @Test public void orderByTypes() {
    final String pairs = "string-join(for $p in (";
    // integers, doubles (with NaN), stable order of equal keys
    query(pairs + "[ 3, 'a' ], [ 1, 'b' ], [ 3, 'c' ], [ 2, 'd' ]) "
        + "order by $p?1 return $p?2)", "bdac");
    query(pairs + "[ 3e0, 'a' ], [ xs:double('NaN'), 'b' ], [ -1e0, 'c' ]) "
        + "order by $p?1 return $p?2)", "bca");
    query(pairs + "[ 3e0, 'a' ], [ xs:double('NaN'), 'b' ], [ -1e0, 'c' ]) "
        + "order by $p?1 descending return $p?2)", "acb");
    // strings and untyped values, empty keys
    final String strings = pairs + "[ 'b', 'x' ], [ (), 'y' ], [ <a>a</a>/text(), 'z' ]) ";
    query(strings + "order by $p?1 empty least return $p?2)", "yzx");
    query(strings + "order by $p?1 empty greatest return $p?2)", "zxy");
    query(strings + "order by $p?1 descending empty least return $p?2)", "xzy");
    // multiple keys of different types
    query(pairs + "[ 'b', 2, 'w' ], [ 'a', 2, 'x' ], [ 'b', 1, 'y' ], [ 'a', 3, 'z' ]) "
        + "order by $p?1, $p?2 descending return $p?3)", "zxwy");
    // incomparable keys
    error("for $k in (1, 'a', 2) order by ($k, 'x')[1] return $k", CMPTYPES_X_X_X_X);
  }

  /** Order by: smallest tuples. */
  @Test public void orderByTopK() {
    final String flwor = "(for $i in 1 to 100 ";
    // multiple keys, stable order of equal keys
    check(flwor + "order by $i mod 7, $i descending return $i)[position() <= 3]",
        "98\n91\n84", exists(OrderBy.class), root(_UTIL_RANGE));
    query(flwor + "order by $i mod 7 empty greatest return $i)[position() <= 3]", "7\n14\n21");
    query(SUBSEQUENCE.args(" " + flwor + "order by $i mod 7 empty greatest return $i)", 99, 5),
        "90\n97");
    query(SUBSEQUENCE.args(" " + flwor + "order by $i mod 7 empty greatest return $i)", 3, 0), "");
    query(SUM.args(SUBSEQUENCE.args(" " + flwor + "order by $i empty greatest return $i)", 1,
        1000000000)), 5050);
    query(SUM.args(SUBSEQUENCE.args(" " + flwor + "order by $i empty greatest return $i)", 1,
        100000)), 5050);
    // first item, single position
    check(HEAD.args(" " + flwor + "order by $i mod 7, $i descending return $i)"), 98,
        exists(OrderBy.class), root(HEAD));
    check(ITEMS_AT.args(" " + flwor + "order by $i mod 7, $i descending return $i)", 3), 84,
        exists(OrderBy.class), root(ITEMS_AT));
    // empty keys
    final String keys = flwor + "let $k := if($i mod 25) then $i else () ";
    query(keys + "order by $k empty greatest return $i)[position() <= 3]", "1\n2\n3");
    query(keys + "order by $k descending empty greatest return $i)[position() <= 3]",
        "25\n50\n75");
    // collation
    query("(for $s in ('b', 'A', 'a', 'B') order by $s collation "
        + "'http://www.w3.org/2005/xpath-functions/collation/html-ascii-case-insensitive' "
        + "return $s)[position() <= 2]", "A\na");
    // clauses after order by
    query(flwor + "order by $i mod 7 empty greatest let $j := $i * 2 return $j)"
        + "[position() <= 3]", "14\n28\n42");
    query(flwor + "order by $i mod 7 empty greatest where $i > 7 return $i)[position() <= 2]",
        "14\n21");
    query(flwor + "order by $i mod 7 empty greatest return ($i, -$i))[position() <= 3]",
        "7\n-7\n14");
    // incomparable keys
    error("(for $k in (1 to 100) ! (if(. = 50) then 'a' else .) order by $k empty greatest "
        + "return $k)[position() <= 3]", INVTYPE_X);
  }

  /** Positional variables that are referenced after the filter. */
  @Test public void posToPredReferenced() {
    final String flwor = "for $i at $p in (1 to 1000)[. mod 3 = 0] ";
    final String rtrn = " return $p * 1000 + $i";
    check(flwor + "where $p <= 2" + rtrn, "1003\n2006", empty(Where.class));
    check(flwor + "while $p <= 2" + rtrn, "1003\n2006", empty(While.class));
    check(flwor + "where $p = 3" + rtrn, "3009", empty(Where.class));
    check(flwor + "where $p = 2 to 3" + rtrn, "2006\n3009", empty(Where.class));
    check(flwor + "where $p >= 333" + rtrn, "333999", empty(Where.class));
    check("for $n in 1 to 2 " + flwor + "where $p = $n + 1" + rtrn, "2006\n3009",
        exists(Where.class));
  }

  /** Count clauses: early exit. */
  @Test public void countToWhile() {
    final String flwor = "for $i in 1 to 1000 for $j in 1 to $i count $c ";
    final String rtrn = " return $c * 100 + $i * 10 + $j";
    check(flwor + "where $c <= 3" + rtrn, "111\n221\n322",
        exists(While.class), empty(Where.class));
    check(flwor + "where $c = 2 to 3" + rtrn, "221\n322",
        exists(While.class), exists(Where.class));
    check(flwor + "where $j > 0 where $c <= 2" + rtrn, "111\n221",
        exists(While.class));
    check(flwor + "let $k := $j * 2 where $c <= 2 return $k + $i", "3\n4",
        exists(While.class));
    final String small = "for $i in 1 to 3 for $j in 1 to $i count $c ";
    check(small + "order by $j descending where $c <= 2" + rtrn, "111\n221",
        "//While << //OrderBy");

    // no rewrite: no upper bound, tuples are affected by side effects
    check(small + "where $c >= 5" + rtrn, "532\n633", empty(While.class));
    check(small + "let $m := message($c) where $c <= 2" + rtrn, "111\n221",
        empty(While.class));
  }

  /** Sliding windows with a fixed size. */
  @Test public void windowToSubsequence() {
    final String window = "for sliding window $w in (2, 4, 6, 8, 10, 12) start at $s when true() ";
    check(window + "end at $e when $e - $s eq 2 return sum($w)",
        "12\n18\n24\n30\n22\n12", empty(Window.class), exists(SUBSEQUENCE));
    check(window + "only end at $e when $e - $s eq 2 return sum($w)",
        "12\n18\n24\n30", empty(Window.class), exists(SUBSEQUENCE));
    check(window + "only end at $e when $e - $s + 1 eq 3 return sum($w)",
        "12\n18\n24\n30", empty(Window.class));
    check(window + "end at $e when $e = $s + 1 return $s * 100 + sum($w)",
        "106\n210\n314\n418\n522\n612", empty(Window.class));
    check(window + "only end at $e when 2 + $s = $e return count($w)",
        "3\n3\n3\n3", empty(Window.class));
    check(window + "only end at $e when $e - $s eq 0 return $w",
        "2\n4\n6\n8\n10\n12", empty(Window.class));
    check(window + "only end at $e when $e - $s eq 6 return $w",
        "", empty(Window.class));
    check("for $i in 1 to 2 let $in := (1 to $i + 2) ! (. * $i) "
        + "for sliding window $w in $in start at $s when true() "
        + "only end at $e when $e - $s eq 1 return sum($w) + count($in)",
        "6\n8\n10\n14\n18", empty(Window.class));

    // no rewrite: input is computed, end position is referenced, or no fixed distance
    check("for $i in 1 to 2 for sliding window $w in 1 to $i + 2 start at $s when true() "
        + "only end at $e when $e - $s eq 1 return sum($w)",
        "3\n5\n3\n5\n7", exists(Window.class));
    check("for sliding window $w in (1 to 6) ! (. * 2) start at $s when true() "
        + "only end at $e when $e - $s eq 2 return sum($w)",
        "12\n18\n24\n30", exists(Window.class));
    check(window + "only end at $e when $e - $s eq 2 return $e",
        "3\n4\n5\n6", exists(Window.class));
    check(window + "only end at $e when $e - $s eq -1 return $e",
        "", exists(Window.class));
    check("for $i in 1 to 2 " + window + "only end at $e when $e - $s eq $i return count($w)",
        "2\n2\n2\n2\n2\n3\n3\n3\n3", exists(Window.class));
    check(window + "only end $x at $e when $e - $s eq 1 and $x > 4 return sum($w)",
        "10\n14\n18\n22", exists(Window.class));
    check("for tumbling window $w in 1 to 6 start at $s when true() "
        + "only end at $e when $e - $s eq 2 return sum($w)",
        "6\n15", exists(Window.class));
  }
}
