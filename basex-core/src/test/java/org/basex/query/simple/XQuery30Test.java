package org.basex.query.simple;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * XQuery 3.0 tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQuery30Test extends SandboxTest {
  /** Group by. */
  @Test public void groupBy() {
    query("for $i in (1, 1) group by $i return $i", 1);
    query("for $i in (1, 2, 2, 1) group by $i return $i", "1\n2");
    query("for $x in (1, 2, 2, 1) for $y in ('a', 'a') group by $y return $x",
        "1\n1\n2\n2\n2\n2\n1\n1");
    query("for $x in (1, 2) for $y in ('b', 'a', 'a') group by $y return $x", "1\n2\n1\n1\n2\n2");
    query("for $a in 1 let $b := (1, 2) group by $a return $b", "1\n2");

    query("for $x in (2, 1) let $y:=($x+1) group by $y return $x", "2\n1");
    query("for $x in (2, 1) let $y:=($x+1) group by $y order by $y return $x", "1\n2");
    query("for $x in (1, 2, 3) for $y in ('a') group by $x order by $y, $x descending return $x",
        "3\n2\n1");

    error("let $x := (1, 2) group by $x return $x", INVTYPE_X);
    error("let $x := (1, 2) group by $z return $x", GVARNOTDEFINED_X);
    error("for $a in (1, 1) let $b := $a group by $b order by $a return 1", INVTYPE_X);

    query("for $a in 1 for $a in 2 group by $a return $a", 2);
    query("for $a in 1 for $a in (2, 3) group by $a return $a", "2\n3");
    query("for $s in (1, 1) let $p := $s group by $p order by $p return $s", "1\n1");
    query("for $s in (1) let $p := () group by $p order by $p return 0", 0);
    query("for $i as xs:integer in 1 for $i in 1 return $i", 1);
    error("declare variable $a := 1; for $b in 1 group by $a return $b", GVARNOTDEFINED_X);
    query("for $x in (1, 1) let $y := () group by $x order by $y return 1", 1);
    query("for $a at $p in 1 to 10 let $g := $a mod 2 group by $g return $p",
        "1\n3\n5\n7\n9\n2\n4\n6\n8\n10");
    query("for $a in 1 let $a:=$a+1 group by $a return $a", 2);
    query("for $i as xs:integer in (1, 2) let $j := 42 group by $j return $i", "1\n2");
    query("for $i in 1 to 4 group by $g := $i mod 2 order by $g return $i", "2\n4\n1\n3");
    query("for $i in 1 to 4 group by $g := 5 return $i", "1\n2\n3\n4");
    query("for $i in 1 to 4 group by $g as xs:integer := $i mod 2 order by $g return $i",
        "2\n4\n1\n3");
    query("for $i in 1 to 2 group by $g as item() := 5 return $i", "1\n2");
    error("for $i in 1 to 2 group by $g as node() := 5 return $i", INVTYPE_X);
    error("for $i in 1 to 2 let $g := $i group by $i as xs:integer return $i", WRONGCHAR_X_X);
  }

  /** String concatenation. */
  @Test public void concat() {
    query("'a'||'b'", "ab");
    query("'a' || 'b'", "ab");
    query("'a' || 'b' || 'c'", "abc");
    query("1 || true() || '3'", "1true3");
  }

  /** fn:sort. */
  @Test public void sort() {
    query("sort((1, 2), ())", "1\n2");
  }

  /** try/catch. */
  @Test public void tryCatch() {
    query("try { 1+'a' } catch * { 'X' }", "X");
    query("try { for $i in (42, 0) return 1 idiv $i } catch * { 'X' }", "X");
    query("try { error(xs:QName('local:error')) } catch local:error { 42 }", 42);
    query("try { error(xs:QName('local:error')) } "
        + "catch Q{http://www.w3.org/2005/xquery-local-functions}error { 42 }", 42);
    query("try { error() } catch err:FOER0000 { 42 }", 42);
    query("declare function local:a($n) { try { local:b() } catch * { $n } };"
        + "declare function local:b() { (: fails at compile-time :) xs:QName('b:b') };"
        + "local:a(42)", 42);
    query("let $seq := (1, 2) return try { <x/>[*] } catch * { zero-or-one($seq) }", "");
  }
}
