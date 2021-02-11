package org.basex.query.simple;

import org.basex.query.*;

/**
 * XQuery 3.0 tests.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class XQuery30Test extends QueryTest {
  static {
    queries = new Object[][] {
      { "FLWOR 1", integers(1), "for $i in (1,1) group by $i return $i" },
      { "FLWOR 2", integers(1, 2), "for $i in (1, 2, 2, 1) group by $i return $i" },
      { "FLWOR 3", integers(1, 1, 2, 2, 2, 2, 1, 1),
         "for $x in (1, 2, 2, 1) for $y in ('a','a') group by $y return $x " },
      { "FLWOR 4", integers(1, 2, 1, 1, 2, 2),
         "for $x in (1, 2) for $y in ('b','a','a') group by $y return $x " },
      { "FLWOR 5", integers(1, 2),
         "for $a in 1 let $b := (1, 2) group by $a return $b" },

      { "FLWOR group varref", integers(2, 1),
         "for $x in (2,1) let $y:=($x+1) group by $y return $x" },
      { "GFLWOR varref ordered", integers(1, 2),
         "for $x in (2,1) let $y:=($x+1) group by $y order by $y return $x" },
      { "FLWOR group ngvar", integers(3, 2, 1),
         "for $x in (1,2,3) for $y in ('a') group by $x order by $y, " +
         "$x descending return $x" },

      { "FLWOR Err 1", "let $x := (1,2) group by $x return $x" },
      { "FLWOR Err 2", "let $x := (1,2) group by $z return $x" },
      { "FLWOR Err 3",
        "for $a in (1,1) let $b := $a group by $b order by $a return 1" },
      { "FLWOR Err 4",
        "for $a in (1,1) let $b := $a group by $b order by $a return 1" },

      { "FLWOR 6", integers(2),
        "for $a in 1 for $a in 2 group by $a return $a" },
      { "FLWOR 7", integers(2, 3),
        "for $a in 1 for $a in (2,3) group by $a return $a" },
      { "FLWOR 8", integers(1, 1),
       "for $s in (1,1) let $p := $s group by " +
       "$p order by $p return $s" },
      { "FLWOR 9", integers(0),
        "for $s in (1) let $p := () group by $p order by $p return 0" },
      { "FLWOR 10", integers(1),
        "for $i as xs:integer in 1 for $i in 1 return $i" },
      { "FLWOR Err 4 (global)",
        "declare variable $a := 1; for $b in 1 group by $a return $b" },
      { "FLWOR 11", integers(1),
        "for $x in (1,1) let $y := () group by $x order by $y return 1" },
      { "FLWOR 12", integers(1, 3, 5, 7, 9, 2, 4, 6, 8, 10),
          "for $a at $p in 1 to 10 let $g := $a mod 2 group by $g return $p" },
      { "FLWOR 13", integers(2), "for $a in 1 let $a:=$a+1 group by $a return $a" },
      { "FLWOR 14", integers(1, 2),
          "for $i as xs:integer in (1,2) let $j := 42 group by $j return $i" },
      { "FLWOR 15", integers(2, 4, 1, 3), "for $i in 1 to 4 group by $g := $i mod 2 " +
          "order by $g return $i" },
      { "FLWOR 16", integers(1, 2, 3, 4), "for $i in 1 to 4 group by $g := 5 return $i" },
      { "FLWOR 17", integers(2, 4, 1, 3), "for $i in 1 to 4 " +
          "group by $g as xs:integer := $i mod 2 order by $g return $i" },
      { "FLWOR 18", integers(1, 2), "for $i in 1 to 2 group by $g as item() := 5 return $i" },
      { "FLWOR 19", "for $i in 1 to 2 group by $g as node() := 5 return $i" },
      { "FLWOR 20", "for $i in 1 to 2 let $g := $i group by $i as xs:integer return $i" },

      { "Concat 1", strings("ab"), "'a'||'b'" },
      { "Concat 2", strings("ab"), "'a' || 'b'" },
      { "Concat 3", strings("abc"), "'a' || 'b' || 'c'" },
      { "Concat 4", strings("1true3"), "1 || true() || '3'" },

      { "Sort 1", integers(1, 2), "sort((1,2), ())" },

      { "Try/catch 1", strings("X"), "try { 1+'a' } catch * { 'X' }" },
      { "Try/catch 2", strings("X"), "try { for $i in (42,0)" +
          "return 1 idiv $i } catch * {'X'}" },
      { "Try/catch 3", integers(42), "try { error(xs:QName('local:error')) } " +
          "catch local:error { 42 }" },
      { "Try/catch 4", integers(42), "try { error(xs:QName('local:error')) } " +
          "catch Q{http://www.w3.org/2005/xquery-local-functions}error { 42 }" },
      { "Try/catch 5", integers(42), "try { error() } catch err:FOER0000 { 42 }" },
      { "Try/catch 6", integers(42),
          "declare function local:a($n) { try { local:b() } catch * { $n } };" +
          "declare function local:b() { (: fails at compile-time :) xs:QName('b:b') };" +
          "local:a(42)" },
      { "Try/catch 7", integers(),
          "let $seq := (1,2) return try { <x/>[*] } catch * { zero-or-one($seq) }" },
    };
  }
}
