package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.junit.jupiter.api.*;

/**
 * Tests for XQuery maps.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class MapTest extends SandboxTest {
  /** A map as key should lead to FOTY0013. */
  @Test public void mapAsKeyTest() {
    error("declare variable $m := map { 'a': 'b' };" +
          "declare variable $q := map { $m: 'a' };" +
          "$q", FIATOMIZE_X);
  }

  /** Tests the map constructor. */
  @Test public void constructor() {
    check("map { 'A': 1, 2: 3 }?A", 1, root(Int.class));
    check("map { <_>A</_>: 1, 2: 3 }?A", 1, root(Int.class));
  }

  /** Tests the new syntax for map literals. */
  @Test public void gh755() {
    query("(<x><y/></x> / map { 'test': y, 42: 'asdf' })('test')", "<y/>");
  }

  /** Tests keys. */
  @Test public void keys() {
    error(" map { ('a', 'b'): 'b' }", SEQFOUND_X);
    error(" map { 'a': 'b', 'a': 'c' }", MAPDUPLKEY_X);
    error(" map { xs:time('01:01:01'): 1, xs:time('01:01:01'): 1 }", MAPDUPLKEY_X);

    query(_MAP_SIZE.args(" map { xs:time('01:01:01'): 1, xs:time('02:02:02'): 2 }"), 2);
    query("let $k1 := xs:time('01:01:01')"
        + "let $k2 := xs:time('01:01:02+01:00')"
        + "let $m := map { $k1: 1 }"
        + "return map:put(map:remove($m, $k1), $k2, 2)($k2)", 2);

    query(" map {  xs:time('01:01:01'): 1, xs:time('01:01:02+01:00'): 2 }");
    query(" map {  xs:time('01:01:01'): 1, xs:time('01:01:02+01:00'): 2 }");

    query("let $k1 := xs:time('01:01:01')"
        + "let $k2 := xs:dateTime('2001-01-01T01:01:01+01:00')"
        + "let $m := map { $k1: 1 }"
        + "return map:put($m, $k2, 2)");
    query("let $k1 := xs:time('01:01:01')"
        + "let $k2 := xs:time('01:01:02')"
        + "let $k3 := xs:time('01:01:03+01:00')"
        + "let $m := map { $k1: 1, $k2: 2 }"
        + "return map:put(map:remove($m, $k2), $k3, 3)");
    query("let $k1 := xs:time('01:01:01')"
        + "let $k2 := xs:time('01:01:02')"
        + "let $k3 := xs:time('01:01:02+01:00')"
        + "let $m := map { $k1: 1, $k2: 2 }"
        + "return map:merge((map:remove($m, $k2), map { $k3: 3 }))");
  }

  /** Stack overflow bug. */
  @Test public void so() {
    query("let $x := map { 'f': map { 1: 1, 2: 2 } } "
        + "return (every $k in map:keys($x('f')) satisfies $k eq $x('f')($k))", true);
  }

  /** Stack overflow bug. */
  @Test public void soType() {
    query("count(( map { }, array { <a/> } ))", 2);
    query("count(( array { <a/> }, map { } ))", 2);
  }

  /** GitHub bug (#1012). */
  @Test public void gh1012() {
    error("map { }(())", EMPTYFOUND);
  }

  /** GitHub bug (#1297). */
  @Test public void gh1297() {
    query("let $m := map { 'A_': 1, 'B@': 2, 'C!': 3 }"
        + "return (map:remove($m, 'A_')('A_'), map:remove($m, 'C!')('C!'))", "");
  }

  /** GitHub bug (#1480). */
  @Test public void gh1480() {
    query("map:merge(( map { 'AQ': 'A' }, map { 'B2': 'C' }, map { 'B2': 'X' }),"
        + "map { 'duplicates': 'use-last' })?B2", "X");
    query("map:merge(( map { 'AQ': 'A' }, map { 'B2': 'C' }, map { 'B2': 'X' }),"
        + "map { 'duplicates': 'use-first' })?B2", "C");
    query("map:merge(( map { 'AQ': 'A' }, map { 'B2': 'C' }, map { 'B2': 'X' }),"
        + "map { 'duplicates': 'combine' })?B2", "C\nX");
    error("map:merge(( map { 'AQ': 'A' }, map { 'B2': 'C' }, map { 'B2': 'X' }),"
        + "map { 'duplicates': 'reject' })?B2", MERGE_DUPLICATE_X);
  }

  /** map:put: Always replace equal values of different type. */
  @Test public void gh2358() {
    query("let $m := { 48e0: 1 }"
        + "return some(map:keys($m), fn { . instance of xs:double })", true);
    query("let $m := { 48: 1 } => map:put(48e0, 2)"
        + "return every(map:keys($m), fn { . instance of xs:integer })", true);
    query("let $m := { '0': 1 } => map:put(48, 2) => map:put(48e0, 3)"
        + "return every(map:keys($m), fn { not(. instance of xs:double) })", true);
    query("let $m := { '0': 1 } => map:put(48, 2) => map:put(48.0, 3) => map:put(48e0, 4)"
        + "return every(map:keys($m), fn { not(. instance of xs:double) })", true);
  }

  /** Atomize key. */
  @Test public void atomKey() {
    query("map { 'x': 42 }([ 'x' ])", 42);
  }

  /** Ordered map construction. */
  @Test public void ordered() {
    query("{ 1: 1, 2: 2, 3: 3 }", "{1:1,2:2,3:3}");
    query("{ 3: 3, 2: 2, 1: 1 }", "{3:3,2:2,1:1}");

    query("map:merge(({ 1: 2 }, { 2: 1 }))", "{1:2,2:1}");
    query("map:merge(({ 2: 1 }, { 1: 2 }))", "{2:1,1:2}");

    query("map:build(1 to 3)", "{1:1,2:2,3:3}");
    query("(1 to 3) => reverse() => map:build()", "{3:3,2:2,1:1}");
    query("(1 to 100000) => map:build() => map:size()", 100000);
    query("(1 to 100000) => reverse() => map:build() => map:size()", 100000);
  }

  /** Ordered deletes and puts. */
  @Test public void orderedDeletes() {
    query("map:build(1 to 3) => map:remove(3)", "{1:1,2:2}");
    query("map:build(1 to 3) => map:remove(3) => map:remove(2)", "{1:1}");
    query("map:build(1 to 3) => map:remove(3) => map:remove(2) => map:remove(1)", "{}");
    query("map:build(1 to 3) => map:remove((3, 2, 1, 0))", "{}");
    query("map:build(1 to 3) => map:remove((3, 3, 2, 2, 1, 1))", "{}");

    query("map:build(1 to 3) => map:remove(1)", "{2:2,3:3}");
    query("map:build(1 to 3) => map:remove(1) => map:remove(2)", "{3:3}");
    query("map:build(1 to 3) => map:remove(1) => map:remove(2) => map:remove(3)", "{}");

    query("map:build(1 to 1000) ! fold-left(1 to 1000, ., map:remove#2)", "{}");
    query("map:build(1 to 1000) ! fold-left(reverse(1 to 1000), ., map:remove#2)", "{}");
  }

  /** Ordered puts. */
  @Test public void orderedPuts() {
    query("map:build((1 to 10)[. = 0]) => map:put(1, 2)", "{1:2}");
    query("map:build((1 to 10)[. = 0]) => map:put(1, 2) => map:put(3, 4)", "{1:2,3:4}");
    query("map:build((1 to 10)[. = 0]) => map:put(1, 2) => map:put(1, 3)", "{1:3}");

    query("{} => map:put(1, 2)", "{1:2}");
    query("{ 1: 2 } => map:put(3, 4)", "{1:2,3:4}");
    query("{ 1: 2, 3: 4 } => map:put(5, 6)", "{1:2,3:4,5:6}");

    query("{} ! fold-left(reverse(1 to 100000), ., map:put(?, ?, ())) ! sum(map:keys(.))",
        5000050000L);
  }

  /** Operations on the same hash. */
  @Test public void orderedSameHash() {
    query("{ 48: 'I' } => map:put('0', 'S')", "{48:\"I\",\"0\":\"S\"}");
    query("{ '0': 'S' } => map:put(48, 'I')", "{\"0\":\"S\",48:\"I\"}");

    query("{ '0': 'S' } => map:put(48, 'I') => map:put('0', 'S2')",
        "{\"0\":\"S2\",48:\"I\"}");
    query("{ '0': 'S' } => map:put(48, 'I') => map:put('0', 'S2') => map:put(48, 'I2')",
        "{\"0\":\"S2\",48:\"I2\"}");

    query("map:build(48 to 57) "
        + "! fold-left(map:keys(.), ., fn($m, $i) { map:put($m, char($i), $i) })"
        + "! map:size(.)",
        20);

    query("{ 48: 'I', '0': 'S' } => map:remove('0')", "{48:\"I\"}");
    query("{ 48: 'I', '0': 'S' } => map:remove(48)", "{\"0\":\"S\"}");
    query("{ 48: 'I', 'x': 'x' } => map:remove('0')", "{48:\"I\",\"x\":\"x\"}");

    query("{ 48: 'I' } => map:get(48)", "I");
    query("{ 48: 'I' } => map:get('0')", "");

    query("{ 48: 'I', '0': 'S' } => map:get(48)", "I");
    query("{ 48: 'I', '0': 'S' } => map:get('0')", "S");
    query("{ 48: 'I', 'x': 'x' } => map:get('0')", "");

    query("{ 48: 'I', '0': 'S' } => map:put(9, 9) => map:get(48)", "I");
    query("{ 48: 'I', '0': 'S' } => map:put(9, 9) => map:get('0')", "S");
    query("{ 48: 'I', 'x': 'x' } => map:put(9, 9) => map:get('0')", "");
  }

  /** Operations on the same key. */
  @Test public void orderedSameKey() {
    query("{ 1: 'A' } => map:put(1, 'B')", "{1:\"B\"}");
    query("{ 1: 'A' } => map:put(1, 'B') => map:put(1,'C')", "{1:\"C\"}");

    query("{ 1: 'A', 2: 'B' } => map:put(2, 'C')", "{1:\"A\",2:\"C\"}");
    query("{ 1: 'A', 2: 'B' } => map:put(2, 'C') => map:put(2, 'D')", "{1:\"A\",2:\"D\"}");

    query("map:build(1 to 99) "
        + "! map:put(., 50e0, '') "
        + "! every(map:keys(.), fn { . instance of xs:integer })",
        true);
    query("map:build(1 to 99) "
        + "! map:put(., 50e0, '') "
        + "! map:put(., 50, '') "
        + "! some(map:keys(.), fn { . instance of xs:double })",
        false);
    query("map:build(1 to 99) "
        + "! fold-left(map:keys(.), ., fn($m, $i) { map:put($m, xs:double($i), ()) })"
        + "! every(map:keys(.), fn { . instance of xs:integer })",
        true);
    query("map:build(1 to 99) "
        + "! fold-left(map:keys(.), ., fn($m, $i) { map:put($m, xs:double($i), ()) })"
        + "! fold-left(map:keys(.), ., fn($m, $i) { map:put($m, xs:integer($i), ()) })"
        + "! every(map:keys(.), fn { . instance of xs:integer })",
        true);

    query("{ 48: 'I', '0': 'S' } => map:get(48)", "I");
    query("{ 48: 'I', '0': 'S' } => map:get('0')", "S");
  }

  /** Replacements with equal keys of different type. */
  @Test public void orderedEqualKey() {
    query("{ 1: 'A' } => map:put(1e0, 'B')", "{1:\"B\"}");
    query("{ 1: 'A' } => map:put(1e0, 'B') => map:put(1.0,'C')", "{1:\"C\"}");

    query("{ 1: 'A', 2: 'B' } => map:put(2e0, 'C')", "{1:\"A\",2:\"C\"}");
    query("{ 1: 'A', 2: 'B' } => map:put(2e0, 'C') => map:put(2.0,'D')", "{1:\"A\",2:\"D\"}");

    // equal key: append new entries
    query("{ 1: 'A', 2: 'B' } => map:put(1e0, 'C')", "{1:\"C\",2:\"B\"}");

    query("{ 1: 1, 2: 2 } => map:remove(1e0)", "{2:2}");
    query("{ 1: 1, 2: 2 } => map:remove(1.0)", "{2:2}");

    query("{ 1: 1, 2: 2 } => map:remove(1e0)", "{2:2}");
    query("{ 1: 1, 2: 2 } => map:remove(1.0)", "{2:2}");

    query("{ 48: 'I', '0': 'S' } => map:get(48e0)", "I");
    query("{ 48: 'I', '0': 'S' } => map:get(48.0)", "I");
    query("{ 48: 'I', '0': 'S' } => map:put(9, 9) => map:get(48e0)", "I");
    query("{ 48: 'I', '0': 'S' } => map:put(9, 9) => map:get(48.0)", "I");

    query("{ 48: 'I', '0': 'S' } => map:put('0', 'S') => map:get('0')", "S");
    query("{ 48: 'I', '0': 'S' } => map:put('0', 'T') => map:get('0')", "T");
  }

  /** Multiple puts/removes in the same map. */
  @Test public void orderedMultiple() {
    query("{} ! (map:put(., 9, 9), map:put(., 8, 8))",
        "{9:9}\n{8:8}");
    query("{ 1: 1 } ! (map:put(., 9, 9), map:put(., 8, 8))",
        "{1:1,9:9}\n{1:1,8:8}");
    query("{ 1: 1, 2: 2 } ! (map:put(., 9, 9), map:put(., 8, 8))",
        "{1:1,2:2,9:9}\n{1:1,2:2,8:8}");
    query("{ 1: 1, 2: 2, 3: 3 } ! (map:put(., 9, 9), map:put(., 8, 8))",
        "{1:1,2:2,3:3,9:9}\n{1:1,2:2,3:3,8:8}");
    query("{ 1: 1, 2: 2, 3: 3, 4: 4 } ! (map:put(., 9, 9), map:put(., 8, 8))",
        "{1:1,2:2,3:3,4:4,9:9}\n{1:1,2:2,3:3,4:4,8:8}");

    query("{ 1: 1, 2: 2, 3: 3, 4: 4 } ! (map:remove(., 4), map:put(., 8, 8))",
        "{1:1,2:2,3:3}\n{1:1,2:2,3:3,4:4,8:8}");
    query("{ 1: 1, 2: 2, 3: 3, 4: 4 } ! (map:put(., 8, 8), map:remove(., 4))",
        "{1:1,2:2,3:3,4:4,8:8}\n{1:1,2:2,3:3}");

    query("{ 1: 1, 2: 2, 3: 3 } ! (map:remove(., 1), map:remove(., 2))",
        "{2:2,3:3}\n{1:1,3:3}");
    query("{ 1: 1, 2: 2, 3: 3 } ! (map:remove(., 2), map:remove(., 1))",
        "{1:1,3:3}\n{2:2,3:3}");

    query("let $map := map:build(1 to 5) "
        + "for $i in map:keys($map) "
        + "return map:keys(map:remove($map, $i)) "
        + "=> sum()",
        "14\n13\n12\n11\n10");

    query("{} ! (map:put(., 9, 9), map:put(., 8, 8)) ! map:keys(.)",
        "9\n8");
    query("{} ! (map:put(., 9, 9), map:put(., 8, 8)) ! ?*",
        "9\n8");
    query("{ 1: 1, 2: 2, 3: 3, 4: 4 } ! (map:put(., 9, 9), map:put(., 8, 8)) ! map:keys(.)",
        "1\n2\n3\n4\n9\n1\n2\n3\n4\n8");
    query("{ 1: 1, 2: 2, 3: 3, 4: 4 } ! (map:put(., 9, 9), map:put(., 8, 8)) ! ?*",
        "1\n2\n3\n4\n9\n1\n2\n3\n4\n8");
  }

  /** Ordered deletes and puts. */
  @Test public void orderedDeletesPuts() {
    query("map:build(1 to 3) => map:remove(3) => map:put(3, 9)", "{1:1,2:2,3:9}");
    query("map:build(1 to 3) => map:remove(3) => map:put(3, 9) "
        + "=> map:remove(3) => map:put(3, 8)", "{1:1,2:2,3:8}");

    query("map:build(1 to 3) => map:remove(1) => map:put(1, 9)", "{2:2,3:3,1:9}");
    query("map:build(1 to 3) => map:remove(1) => map:put(1, 9) "
        + "=> map:remove(1) => map:put(1, 8)", "{2:2,3:3,1:8}");

    query("map:build(1 to 1000) "
        + "! fold-left(reverse(1 to 1000), .,"
        + " fn($m, $i) { $m => map:remove($i) => map:put($i, 1) }) "
        + "! sum(?*)", 1000);
    query("map:build(1 to 10000) "
        + "! fold-left(reverse(1 to 10000), .,"
        + " fn($m, $i) { $m => map:remove(1) => map:put(1, 1) }) "
        + "! sum(?*)", 50005000);
    query("map:build(1 to 100000) "
        + "! fold-left(random-number-generator()?permute(1 to 100000), ., "
        + " fn($m, $i) { $m => map:remove(1) => map:put(1, 1) }) "
        + "! sum(?*)", 5000050000L);
  }

  /** Instance of tests. */
  @Test public void instanceOf() {
    query("map:build(1 to 100) instance of map(xs:integer, xs:integer)", true);
    query("map:build(1 to 10000) instance of map(xs:integer, xs:integer)", true);
    query("(map:build(1 to 10000) => map:put(0, 0)) instance of map(xs:integer, xs:integer)", true);

    query("map:build(1 to 100) instance of map(xs:integer, xs:int)", false);
    query("map:build(1 to 10000) instance of map(xs:integer, xs:int)", false);
    query("(map:build(1 to 10000) => map:put(0, 0)) instance of map(xs:integer, xs:int)", false);
  }

  /** Traversal. */
  @Test public void traversal() {
    // variants: hash vs. trie map; iteration vs. value-based retrieval
    query("map:build(1 to 10000) "
        + "=> map:for-each(fn($k, $v) { $k + $v }) "
        + "=> sum()",
        100010000);
    query("map:build(1 to 10000) "
        + "=> map:for-each(fn($k, $v) { $k + $v }) "
        + "=> sort()"
        + "=> sum()",
        100010000);
    query("map:build(1 to 10000) "
        + "=> map:put(0, 0) "
        + "=> map:for-each(fn($k, $v) { $k + $v }) "
        + "=> sum()",
        100010000);
    query("map:build(1 to 10000) "
        + "=> map:put(0, 0) "
        + "=> map:for-each(fn($k, $v) { $k + $v }) "
        + "=> sort()"
        + "=> sum()",
        100010000);
  }

  /** Deep equality. */
  @Test public void deepEqual() {
    // compare pristine maps
    query("let $a := map:build(1 to 1000) "
        + "let $b := map:build(1 to 1000) "
        + "return deep-equal($a, $b)",
        true);
    query("let $a := map:build(1 to 1000) "
        + "let $b := map:build(reverse(1 to 1000)) "
        + "return deep-equal($a, $b)",
        true);
    // compare pristine and updated maps
    query("let $a := map:build(0 to 1000) "
        + "let $b := map:build(1 to 1000) => map:put(0, 0) "
        + "return deep-equal($a, $b)",
        true);
    query("let $a := map:build(1 to 1000) => map:put(0, 0) "
        + "let $b := map:build(0 to 1000) "
        + "return deep-equal($a, $b)",
        true);
    query("let $a := map:build(1 to 1000) => map:put(0, 0) "
        + "let $b := map:build(reverse(0 to 1000)) "
        + "return deep-equal($a, $b)",
        true);
    // compare updated maps
    query("let $a := map:build(1 to 1000) => map:put(0, 0) "
        + "let $b := map:build(0 to 999) => map:put(1000, 1000) "
        + "return deep-equal($a, $b)",
        true);
    query("let $a := map:build(0 to 999) => map:put(1000, 1000) "
        + "let $b := map:build(1 to 1000) => map:put(0, 0) "
        + "return deep-equal($a, $b)",
        true);
    query("let $a := map:build(0 to 999) => map:put(1000, 1000) "
        + "let $b := map:build(reverse(1 to 1000)) => map:put(0, 0) "
        + "return deep-equal($a, $b)",
        true);

    // ignore different types
    query("deep-equal({ 1: 2 }, { 1e0: 2 })", true);
    query("deep-equal({ 1: 2 }, { 1.0: 2 })", true);
    query("deep-equal({ 1: 2 }, { '1': 2 })", false);
    query("deep-equal({}, [])", false);

    // compare unequal maps
    query("deep-equal({}, { 1: 2 })", false);
    query("deep-equal({ 1: 2 }, {})", false);
    query("deep-equal({ 1: 2 }, { 1: 3 })", false);
    query("deep-equal({ 1: 2 }, { 2: 2 })", false);
    query("deep-equal({ 1: 2, 3: 4 }, { 1: 2, 3: 3 })", false);
    query("deep-equal({ 1: 2, 3: 4 }, { 1: 2, 4: 4 })", false);
    query("deep-equal({ 1: 2, 3: 4, 5: 6 }, { 1: 2, 3: 4, 5: 5 })", false);
  }

  /** Deep equality checks triggered by optimizations. */
  @Test public void deepEqualCode() {
    query("{}, {}", "{}\n{}");
    query("{1:2}, {1:2}", "{1:2}\n{1:2}");
    query("{1:2,3:4}, {1:2,3:4}", "{1:2,3:4}\n{1:2,3:4}");

    query("{1:2}, []", "{1:2}\n[]");
    query("[], {1:2}", "[]\n{1:2}");
    query("{1:2}, {}", "{1:2}\n{}");
    query("{}, {1:2}", "{}\n{1:2}");
    query("{1:2}, {2:3}", "{1:2}\n{2:3}");
  }

  /** Tests integer maps. */
  @Test public void intMaps() {
    query("map:build(1)", "{1:1}");
    query("map:build(1) => map:keys()", 1);
    query("map:build(1) => map:items()", 1);
    query("map:build(1) => map:entries()", "{1:1}");
    query("map:build(1) => map:get(1)", 1);
    query("map:build(1) => map:get(1e0)", 1);
    query("map:build(1) => map:get(1.0)", 1);
    query("map:build(1) => map:get('1')", "");
    query("map:build(1) => map:get(true())", "");

    query("map:build(10_000_000_000)", "{10000000000:10000000000}");
    query("map:build(1, value := fn { 10_000_000_000 })", "{1:10000000000}");
    query("map:build(1, keys := fn { 10_000_000_000 })", "{10000000000:1}");
    query("map:build(xs:byte(1)) -> map:keys(.) -> (. instance of xs:byte)", true);

    check("map:merge(({ 1: 1 }, { 2: 2 }, { 3: 3 }))",
        "{1:1,2:2,3:3}", root(XQIntMap.class));
    check("map:merge(({ 1: 1 }, { 2: '2' }, { 3: '3' }))",
        "{1:1,2:\"2\",3:\"3\"}", root(XQIntObjMap.class));
    check("map:merge(({ 1: 1 }, { 2: '2' }, { '3': '3' }))",
        "{1:1,2:\"2\",\"3\":\"3\"}", root(XQItemObjMap.class));
    check("map:merge(({ 1: 1 }, { '2': '2' }, { '3': '3' }))",
        "{1:1,\"2\":\"2\",\"3\":\"3\"}", root(XQItemObjMap.class));
    check("map:merge(({ 1: 1 }, { '2': '2' }, { 3: 3 }))",
        "{1:1,\"2\":\"2\",3:3}", root(XQItemObjMap.class));

    query("map:build('1')", "{\"1\":\"1\"}");
    query("map:build('1') => map:keys()", 1);
    query("map:build('1') => map:items()", 1);
    query("map:build('1') => map:entries()", "{\"1\":\"1\"}");
    query("map:build('1') => map:get(xs:anyURI('1'))", 1);
    query("map:build('1') => map:get(xs:untypedAtomic('1'))", 1);
    query("map:build('1') => map:get(1)", "");
    query("map:build('1') => map:get(true())", "");

    query("map:build(xs:token('1')) -> map:keys(.) -> (. instance of xs:token)", true);

    check("map:merge(({ '1': '1' }, { '2': '2' }, { '3': '3' }))",
        "{\"1\":\"1\",\"2\":\"2\",\"3\":\"3\"}", root(XQTokenMap.class));
    check("map:merge(({ '1': '1' }, { '2': '2' }, { '3': 3 }))",
        "{\"1\":\"1\",\"2\":\"2\",\"3\":3}", root(XQTokenObjMap.class));
    check("map:merge(({ '1': '1' }, { '2': '2' }, { 3: '3' }))",
        "{\"1\":\"1\",\"2\":\"2\",3:\"3\"}", root(XQItemObjMap.class));
    check("map:merge(({ '1': '1' }, { 2: 2 }, { 3: 3 }))",
        "{\"1\":\"1\",2:2,3:3}", root(XQItemObjMap.class));
    check("map:merge(({ '1': '1' }, { 2: 2 }, { '3': '3' }))",
        "{\"1\":\"1\",2:2,\"3\":\"3\"}", root(XQItemObjMap.class));

    check("map:merge(({ 1: '1' }, { 2: '2' }, { 3: '3' }))",
        "{1:\"1\",2:\"2\",3:\"3\"}", root(XQIntTokenMap.class));
    check("map:merge(({ 1: '1' }, { 2: '2' }, { 3: 3 }))",
        "{1:\"1\",2:\"2\",3:3}", root(XQIntObjMap.class));
  }

  /** Tests string maps. */
  @Test public void stringMaps() {
    query("map:build(1)", "{1:1}");
    query("map:build(1) => map:keys()", 1);
    query("map:build(1) => map:items()", 1);
    query("map:build(1) => map:entries()", "{1:1}");
    query("map:build(1) => map:get(1)", 1);
    query("map:build(1) => map:get(1e0)", 1);
    query("map:build(1) => map:get(1.0)", 1);
    query("map:build(1) => map:get('1')", "");
    query("map:build(1) => map:get(true())", "");

    query("map:build(10_000_000_000)", "{10000000000:10000000000}");
    query("map:build(1, value := fn { 10_000_000_000 })", "{1:10000000000}");
    query("map:build(1, keys := fn { 10_000_000_000 })", "{10000000000:1}");
    query("map:build(xs:byte(1)) -> map:keys(.) -> (. instance of xs:byte)", true);

    check("map:merge(({ 1: 1 }, { 2: 2 }, { 3: 3 }))",
        "{1:1,2:2,3:3}", root(XQIntMap.class));
    check("map:merge(({ 1: 1 }, { 2: '2' }, { 3: '3' }))",
        "{1:1,2:\"2\",3:\"3\"}", root(XQIntObjMap.class));
    check("map:merge(({ 1: 1 }, { 2: '2' }, { '3': '3' }))",
        "{1:1,2:\"2\",\"3\":\"3\"}", root(XQItemObjMap.class));
    check("map:merge(({ 1: 1 }, { '2': '2' }, { '3': '3' }))",
        "{1:1,\"2\":\"2\",\"3\":\"3\"}", root(XQItemObjMap.class));
    check("map:merge(({ 1: 1 }, { '2': '2' }, { 3: 3 }))",
        "{1:1,\"2\":\"2\",3:3}", root(XQItemObjMap.class));

    query("map:build('1')", "{\"1\":\"1\"}");
    query("map:build('1') => map:keys()", 1);
    query("map:build('1') => map:items()", 1);
    query("map:build('1') => map:entries()", "{\"1\":\"1\"}");
    query("map:build('1') => map:get(xs:anyURI('1'))", 1);
    query("map:build('1') => map:get(xs:untypedAtomic('1'))", 1);
    query("map:build('1') => map:get(1)", "");
    query("map:build('1') => map:get(true())", "");

    query("map:build(xs:token('1')) -> map:keys(.) -> (. instance of xs:token)", true);

    check("map:merge(({ '1': '1' }, { '2': '2' }, { '3': '3' }))",
        "{\"1\":\"1\",\"2\":\"2\",\"3\":\"3\"}", root(XQTokenMap.class));
    check("map:merge(({ '1': '1' }, { '2': '2' }, { '3': 3 }))",
        "{\"1\":\"1\",\"2\":\"2\",\"3\":3}", root(XQTokenObjMap.class));
    check("map:merge(({ '1': '1' }, { '2': '2' }, { 3: '3' }))",
        "{\"1\":\"1\",\"2\":\"2\",3:\"3\"}", root(XQItemObjMap.class));
    check("map:merge(({ '1': '1' }, { 2: 2 }, { 3: 3 }))",
        "{\"1\":\"1\",2:2,3:3}", root(XQItemObjMap.class));
    check("map:merge(({ '1': '1' }, { 2: 2 }, { '3': '3' }))",
        "{\"1\":\"1\",2:2,\"3\":\"3\"}", root(XQItemObjMap.class));

    check("map:merge(({ '1': 1 }, { '2': 2 }, { '3': 3 }))",
        "{\"1\":1,\"2\":2,\"3\":3}", root(XQTokenIntMap.class));
    check("map:merge(({ '1': 1 }, { '2': 2 }, { '3': '3' }))",
        "{\"1\":1,\"2\":2,\"3\":\"3\"}", root(XQTokenObjMap.class));
  }
}
