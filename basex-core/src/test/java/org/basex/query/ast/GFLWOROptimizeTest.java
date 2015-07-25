package org.basex.query.ast;

import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.basex.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Tests for rewritings of FLWOR-expressions.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class GFLWOROptimizeTest extends QueryPlanTest {
  /** Tests the relocation of a static let clause. */
  @Test public void moveTop() {
    check("let $b := <x>a</x> " +
        "for $i in 1 to 2 " +
        "let $m := $b " +
        "return $m/text()",
        "a\na",
        Util.info("every $l in //% satisfies $l << //%", Let.class, For.class)
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
        "let $a := //Let[Var/@name eq '$a'] return " +
        "//For[Var/@name eq '$i'] << $a and $a << //For[Var/@name eq '$j']"
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
        Util.info("every $f in //% satisfies $f << //%[starts-with(Var/@name, '$b')]",
            For.class, Let.class)
    );
  }

  /** Tests the relocation of a let clause. */
  @Test public void dontMove2() {
    check("let $a := <a/>, $b := <b/>, $c := ($a,$a)[1] " +
        "for $i in 1 to 2 return ($c,$b)",
        "<a/>\n<b/>\n<a/>\n<b/>",
        Util.info("//Let[Var/@name='$a'] << //Let[Var/@name='$b'] and " +
            "//Let[Var/@name='$b'] << //Let[Var/@name='$c']")
    );
  }

  /** Tests the relocation of a static let clause. */
  @Test public void moveFor() {
    check("let $x := <x/> " +
        "for $a in 1 to 2 " +
        "for $b as element(x) in $x " +
        "return ($b, $b)[1]",
        "<x/>\n<x/>",
        "//(For | Let)[Var/@name='$b'] << //For[Var/@name='$a']",
        "every $let in //Let, $for in //For satisfies $let << $for"
    );
  }

  /** Tests the relocation of a where clause. */
  @Test public void slideWhere() {
    check("for $i at $p in 1 to 1000 " +
        "let $sum as xs:integer := sum(1 to $i * $i) " +
        "where $i lt 5 " +
        "return ($sum,$sum)[1]",
        "1\n10\n45\n136",
        "//For << //Where and //Where << //Let"
    );

    check("for $len in 1 to 3 " +
        "for sliding window $w in 1 to 3 start at $p when true() only " +
        "end at $q when $q - $p + 1 eq $len where $len > 2 return <window>{$w}</window>",
        "<window>1 2 3</window>",
        "//For << //Window and exists(//For/*[ends-with(name(), 'Filter')])"
    );
  }

  /** Tests if multiple successive where clauses are merged into one. */
  @Test public void mergeWheresTest() {
    check("let $rnd := random:double() where $rnd div 2 >= 0.2 where $rnd < 0.5 " +
        "where round(2 * $rnd) eq 1 return $rnd",
        null,
        "exists(exactly-one(//Where))",
        "exists(//Where/And)"
    );
  }

  /** Tests if where clauses are converted to predicates where applicable. */
  @Test public void whereToPred() {
    check("for $i in 1 to 10 where <x/>[$i] and $i < 3 return $i",
        "1",
        "exists(//*[ends-with(name(), 'Filter')]/" + Util.className(BaseXItemAt.class) + ")"
    );
    check("for $i in 1 to 10 where (<a/>)[$i] return $i",
        "1",
        "exists(//*[ends-with(name(), 'Filter')]/" + Util.className(BaseXItemAt.class) + ")"
    );
    check("for $i in 1 to 3 " +
        "where count(for $j in 1 to $i group by $k := $j mod 2 return $i) > 1 " +
        "return $i",
        "2\n3",
        "empty(//Where)",
        "exists(//*[ends-with(name(), 'Filter')])"
    );
  }

  /** Tests if let clauses are moved out of any loop they don't depend on. */
  @Test public void slideLet() {
    check("for $i in 0 to 3, $j in 0 to 3 where (<x/>)[$i + $j] " +
        "let $foo := $i * $i return $foo * $foo",
        "0\n1",
        "every $let in //Let satisfies $let << exactly-one(//" +
            Util.className(BaseXItemAt.class) + ")"
    );
    check("<x/>/(for $i in 1 to 3 let $x := .  where $x return $x)",
        "<x/>",
        "//GFLWOR/*[1] is //Let"
    );
    check("for $len in 1 to 3 " +
        "for sliding window $w in 1 to 3 start at $p when true() only " +
        "end at $q when $q - $p + 1 eq $len " +
        "let $x := $len div 2 " +
        "return count($w) div ($x + $x)",
        "1\n1\n1\n1\n1\n1",
        "//For << //Let and //Let << //Window"
    );
  }

  /** Tests if multiple let clauses are all moved to their optimal position. */
  @Test public void slideMultipleLets() {
    check("for $i in 1 to 2 for $j in 1 to 2 " +
        "let $a as xs:integer := 3 * $i, " +
        "    $b as xs:integer := 2 * $i " +
        "return $a * $a + $b * $b",
        "13\n13\n52\n52",
        "exists(//For[every $let in //Let satisfies . << $let])",
        "exists(//For[every $let in //Let satisfies . >> $let])",
        "//Let[Var/@name='$a'] << //Let[Var/@name='$b']"
    );
  }

  /** Tests if where clauses containing non-deterministic expressions are left alone. */
  @Test public void dontSlideWhereNDT() {
    check("for $x in 1 to 100 where random:double() gt 0.5 return $x",
        null,
        "exactly-one(//Where) >> exactly-one(//For)"
    );
  }

  /** Tests if let clauses containing non-deterministic expressions are left alone. */
  @Test public void dontSlideLetNDT() {
    check("for $i in 1 to 10 let $rnd := random:double() return $i * $rnd",
        null,
        "exactly-one(//For) << exactly-one(//Let)"
    );
  }

  /** Tests if let clauses containing node constructors are left alone. */
  @Test public void dontSlideLetCNS() {
    check("for $i in 1 to 10 let $x := <x/> return ($i, $x, $x)",
        null,
        "//For << //Let"
    );
  }

  /** Tests is where clauses are rewritten to if. */
  @Test public void whereToIfTest() {
    check("(1 to 3) ! (for $j in 1 to 5 where . eq 1 return $j)",
        "1\n2\n3\n4\n5",
        "exists(//If) and empty(//GFLWOR)"
    );
    check("(1 to 3) ! (for $j at $p in 1 to 5 where . eq 1 return $j * $p)",
        "1\n4\n9\n16\n25",
        "exists(//If) and exists(//GFLWOR)"
    );
    check("let $x := 0 where $x != <x>0</x> return 42 idiv $x", "",
        "exists(//If) and starts-with(//If/*[2]/@name, 'error(')");
  }

  /** Tests if {@code for $x in E return $x} is rewritten to {@code E} inside FLWORs. */
  @Test public void inlineForTest() {
    check("let $x := <x>5</x> for $i in 1 to $x return $i",
        "1\n2\n3\n4\n5",
        "empty(//For)"
    );
  }

  /** Tests if {@link And} expressions inside {@code where} are split. */
  @Test public void splitWhereTest() {
    check("for $i in 1 to 5, $j in 1 to 5 where $i < 3 and $j < 3 return $i * $j",
        "1\n2\n2\n4",
        "empty(//Where)",
        "every $for in //For satisfies exists($for/*[ends-with(name(), 'Filter')])"
    );
  }

  /** Tests if redundant FLWOR expressions are eliminated. */
  @Test public void eliminateFLWORTest() {
    check("let $x := (23, 42) where true() for $y in $x return $y",
        "23\n42",
        "empty(//GFLWOR)"
    );
  }

  /** Tests FLWOR expressions containing updates or non-determinism are left alone. */
  @Test public void dontEliminateFLWORTest() {
    check("copy $x := <x/> modify " +
        "  for $i in 1 to 3 let $y := <y>{$i}</y> return insert node $y into $x " +
        "return $x",
        "<x>\n<y>1</y>\n<y>2</y>\n<y>3</y>\n</x>",
        "exists(//GFLWOR) and exists(//Insert)"
    );
    check("for $i in 1 to 3 let $x := $i * $i return error()",
        null,
        "exists(//GFLWOR)"
    );
  }

  /** Tests if empty FLWOR expressions are replaced by the empty sequence. */
  @Test public void eliminateEmptyFLWORTest() {
    // literal false()
    check("for $x in 1 to 100 where false() return $x",
        "", "exists(//Empty)"
    );

    // empty return clause
    check("for $x in 1 to 100 return ()",
        "", "exists(//Empty)"
    );

    // value with ebv == false in where
    check("for $x in 1 to 100 where () return $x",
        "", "exists(//Empty)"
    );
  }

  /** Tests if {@link And} expressions inside {@code where} are split. */
  @Test public void dontInlineNDTTest() {
    check("let $rnd := random:double() return (1 to 10) ! $rnd",
        null,
        "exists(//Let)"
    );
  }

  /** Checks that clauses can be removed during inlining. */
  @Test public void gh1150() {
    check("for $i in 1 to xs:integer(random:double()) let $x := 'does-not-exist.xml' "
        + "for $x in doc($x) let $x := count($x) return $x",
        "",
        "count(//For) eq 1",
        "count(//GFLWOR/*) eq 2",
        "starts-with(//GFLWOR/*[last()]/@name, 'error(')");
  }
}
