package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.up.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Test cases for FLWOR expressions.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class GFLWORTest extends SandboxTest {
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
        root(DualIterMap.class),
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
        Util.info("every $f in //% satisfies $f << //%[starts-with(@name, '$b')]",
            For.class, Let.class)
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
        root(DualIterMap.class),
        exists(REPLICATE)
    );

    check("let $x := <x/> " +
        "for $a in 1 to 2 " +
        "for $b as element(x) in $x " +
        "return (<_>{ $b }</_>, $b)[1]/x",
        "<x/>\n<x/>",
        empty(Let.class),
        empty(For.class),
        root(DualIterMap.class),
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
        exists(ItemMap.class)
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
    check("let $x := 0 where $x != <x>0</x> return 42 idiv $x", "",
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
        root(DualIterMap.class),
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
    check("for $v at $p in (1, 2) where $p = 2 return $v", 2, root(Int.class));
  }

  /** Allowing empty. */
  @Test public void allowingEmpty() {
    check("for $x allowing empty in () return $x", "", empty());
    check("for $x allowing empty in" + VOID.args(1) + " return $x", "", exists(GFLWOR.class));
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
    error("for $a in (1, 4, 2) let $i := (1, $a, 2) order by $i return $i + 1", SEQFOUND_X);
  }

  /** Remove clauses that will never be executed. */
  @Test public void gh1999() {
    check("for $a in () return delete node a", "", empty());
    check("for $a in" + VOID.args(1) + " return delete node a", "", root(VOID));
  }

  /** Sliding windows. */
  @Test public void gh2252() {
    query("for sliding window $w in 1 to 3 start at $p when true()"
        + "only end when $p = 2 return <w>{ $w }</w>", "<w>2</w>");
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
        "1\n2\n1", count(Where.class, 1));
    check("for $i at $p in (1 to 4) where $i <= 3 where $p < 5 "
        + "for $j at $q in ($i to 2) where $q < 5 return $q",
        "1\n2\n1", count(Where.class, 2));
    check("for $i at $p in (1 to 4) where $i <= 3 where $p < 5 "
        + "for $j at $q in ($i to 2) where $j > 1 where $p < 5 return $q",
        "2\n1", count(Where.class, 2));
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
        "1\n2", exists(DualIterMap.class));
    check("let $a := <a/>[text()] while $a return $a", "", root(IterFilter.class));
  }
}
