package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.query.func.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.junit.jupiter.api.*;

/**
 * Tests for XQuery records.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class RecordTest extends SandboxTest {
  /** Empty record. */
  @Test public void instanceOf() {
    query("declare record x(x); { 'x': () } instance of x", true);

    query("{} instance of record()", true);
    query("{ 'x': () } instance of record()", false);
    query("declare record local:empty(); {} instance of local:empty", true);
    query("declare record local:empty(); { 'x': () } instance of local:empty", false);

    query("{ 'x': () } instance of record(x)", true);
    query("{} instance of record(x)", false);

    query("{ 'x': (), 'y': () } instance of record(x, y)", true);
    query("{ 'x': (), 'y': () } instance of record(x)", false);
    query("{ 'x': (), 0: () } instance of record(x, y)", false);
    query("{ 'x': (), 0: () } instance of record(x)", false);

    query("declare record local:coord(x, y); "
        + "let $coord := local:coord(1, 2) "
        + "let $new := map:remove($coord, 'x') "
        + "return $new instance of local:coord", false);

    query("map:entries({ 'a': 1 }) instance of record(a)", true);
    query("map:entries({ 'x': 1 }) instance of record(a)", false);

    // a non-constant map (node/function value keeps it an expression) must not be statically
    // folded to false: the type intersection of an inferred and a declared record was wrong
    query("{ 'a': 1, 'b': <x/> } instance of record(a, b)", true);
    query("{ 'a': 1, 'b': fn($x) { $x } } instance of record(a, b)", true);
    query("{ 'a': 1, 'b': <x/> } instance of record(a as xs:integer, b as element(x))", true);
    query("{ 'a': 1, 'b': <x/>, 'c': 2 } instance of record(a, b)", false);
    query("{ 'a': <x/>, 'b': 2 } instance of record(a as xs:integer, b)", false);
    // the parsed-csv-structure built-in record (function-valued 'get' field)
    query("{ 'columns': ('a', 'b'), 'column-index': { 'a': 1 }, 'rows': [ 'p' ],\n"
        + "  'get': fn($r as xs:positiveInteger, $c as (xs:positiveInteger | xs:string))"
        + " as xs:string { 'x' } } instance of fn:parsed-csv-structure-record", true);
    query("{ 'columns': ('a', 'b', 'c'), 'column-index': { 'a': 1, 'b': 2, 'c': 3 },\n"
        + "  'rows': ([ 'p', 'q', 'r' ], [ 's', 't', 'u' ]),\n"
        + "  'get': fn($row as xs:positiveInteger, $col as (xs:positiveInteger | xs:string))"
        + " as xs:anyAtomicType? { 'banana' } } instance of fn:parsed-csv-structure-record", false);
    query("{ 'number': 0.937e0, 'next': fn() { fn:random-number-generator() },\n"
        + "  'permute': fn($in) { reverse($in) } }"
        + " instance of fn:random-number-generator-record", false);
    query("{ 'name': xs:QName('platonic'), 'is-simple': true(),\n"
        + "  'base-type': fn() as fn:schema-type-record { atomic-type-annotation(3) },\n"
        + "  'primitive-type': fn() as fn:schema-type-record { atomic-type-annotation(3) },\n"
        + "  'variety': 'atomic',\n"
        + "  'members': fn() as fn:schema-type-record* { () },\n"
        + "  'simple-content-type': fn() as fn:schema-type-record { atomic-type-annotation(3) },\n"
        + "  'matches': fn($x as xs:anyAtomicType) as xs:boolean { true() },\n"
        + "  'constructor': xs:integer#1 } instance of fn:schema-type-record", true);
    error("fn() as fn:schema-type-record* { 1 }()", INVTYPE_X);
    query("let $map := (\n"
        + "  {'x':5, 'y':6}\n"
        + "  => map:put(xs:NCName('x'), true())\n"
        + "  => map:put(xs:NCName('y'), (false(), false()))\n"
        + ")\n"
        + "return (\n"
        + "  $map instance of map(xs:NCName, xs:boolean+) or\n"
        + "  $map instance of map(xs:string, xs:boolean+)\n"
        + ")", true);
  }

  /** Strict field access on sealed records. */
  @Test public void lookup() {
    // sealed record: lookup or call of an undeclared field is a type error
    error("let $r as record(a) := { 'a': 1 } return $r?b", RECORDFIELD_X_X);
    error("let $r as record(a) := { 'a': 1 } return $r('b')", RECORDFIELD_X_X);
    error("let $r as record(a) := { 'a': 1 } return $r(('b')[. != ''])", RECORDFIELD_X_X);
    error("declare record local:c(x, y); local:c(1, 2)?z", RECORDFIELD_X_X);
    // declared fields are accessible
    query("let $r as record(a) := { 'a': 1 } return $r?a", 1);
    query("let $r as record(a, b) := { 'a': 1, 'b': 2 } return $r?b", 2);
    query("let $r as record(a) := { 'a': 1 } return $r('a')", 1);
    // a plain map literal is not sealed: lookup or call of an absent key returns ()
    query("{ 'a': 1 }?b", "");
    query("{ 'a': 1 }('b')", "");
    // map:get stays lenient even on a sealed record
    query("let $r as record(a) := { 'a': 1 } return map:get($r, 'b')", "");
    query("let $r as record(a) := { 'a': 1 } return $r => map:get('b')", "");
    // map:put and map:remove de-seal: the teeth no longer apply to the result
    query("let $r as record(a) := { 'a': 1 } return map:put($r, 'a', 2)?b", "");
    query("let $r as record(a, b) := { 'a': 1, 'b': 2 } return map:remove($r, 'a')?z", "");
    // a declared return type re-seals a result de-sealed by map:put: the teeth apply again
    query("declare record local:coord(x, y);\n"
        + "declare function local:reset($c as local:coord) as local:coord { map:put($c, 'x', 0) }; "
        + "local:reset(local:coord(1, 2)) instance of local:coord", true);
    error("declare record local:coord(x, y);\n"
        + "declare function local:reset($c as local:coord) as local:coord { map:put($c, 'x', 0) }; "
        + "local:reset(local:coord(1, 2))?z", RECORDFIELD_X_X);
    // multiple keys: every key must be a declared field
    query("let $r as record(a, b) := { 'a': 1, 'b': 2 } return $r?('a', 'b')", "1\n2");
    error("let $r as record(a, b) := { 'a': 1, 'b': 2 } return $r?('a', 'c')", RECORDFIELD_X_X);
    // wildcard returns all field values and never errors
    query("let $r as record(a, b) := { 'a': 1, 'b': 2 } return $r?*", "1\n2");
    // nested record lookup
    query("let $o as record(in as record(x)) := { 'in': { 'x': 5 } } return $o?in?x", 5);
    // map:get with a default stays lenient on a sealed record
    query("let $r as record(a) := { 'a': 1 } return map:get($r, 'b', 99)", 99);
  }

  /** The {@code +:=} (record put) operator. */
  @Test public void recordPut() {
    query("let $r as record(a, b) := { 'a': 1, 'b': 2 } return $r +:= { 'b': 9 }",
        "{\"a\":1,\"b\":9}");
    query("let $r as record(a, b) := { 'a': 1, 'b': 2 } return $r +:= { 'a': 7, 'b': 8 }",
        "{\"a\":7,\"b\":8}");
    // left-associative chaining
    query("let $r as record(a) := { 'a': 1 } return ($r +:= { 'a': 2 }) +:= { 'a': 3 }",
        "{\"a\":3}");
    // coercion applies: an integer is promoted to the required type
    query("let $r as record(a as xs:double) := { 'a': 1 } return ($r +:= { 'a': 2 })?a", 2);
    // the result is a sealed record: looking up an undeclared field still errors
    error("let $r as record(a) := { 'a': 1 } return ($r +:= { 'a': 2 })?b", RECORDFIELD_X_X);
    // an undeclared field in the right operand is a type error
    error("let $r as record(a) := { 'a': 1 } return $r +:= { 'c': 9 }", INVTYPE_X);
    // a value that does not conform to the field type is a type error
    error("let $r as record(a as xs:integer) := { 'a': 1 } return $r +:= { 'a': 'x' }", INVTYPE_X);
    // the left operand must be a record (a map with non-string keys is not)
    error("{ 1: 'a' } +:= { 1: 'b' }", INVTYPE_X);
    // updates a record built by a named constructor; unmentioned fields are preserved
    query("declare record local:p(x, y); local:p(1, 2) +:= { 'y': 9 }", "{\"x\":1,\"y\":9}");
    query("let $r as record(a, b) := { 'a': 1, 'b': 2 } return ($r +:= { 'a': 9 })?b", 2);
    // an empty right operand leaves the record unchanged (and still sealed)
    query("let $r as record(a) := { 'a': 1 } return $r +:= {}", "{\"a\":1}");
    error("let $r as record(a) := { 'a': 1 } return ($r +:= {})?b", RECORDFIELD_X_X);
  }

  /** Width-invariant subtyping and {@code record(*)}. */
  @Test public void subtyping() {
    // record(*) matches any record (a map with string keys), but not a non-string-keyed map
    query("{} instance of record(*)", true);
    query("{ 'x': 1 } instance of record(*)", true);
    query("{ 1: 2 } instance of record(*)", false);
    query("fn($r as record(*)) { count(map:keys($r)) }({ 'a': 1, 'b': 2 })", 2);

    // record subtyping is width-invariant: the field-name sets must match
    query("let $r as record(x, y) := { 'x': 1, 'y': 2 } return $r instance of record(x)", false);
    query("let $r as record(x) := { 'x': 1 } return $r instance of record(x, y)", false);
    // field types are covariant
    query("let $r as record(x as xs:integer) := { 'x': 1 } "
        + "return $r instance of record(x as xs:decimal)", true);

    // a sealed record is an instance of a structurally equal open record
    query("declare record local:coord(x, y); local:coord(1, 2) instance of record(x, y)", true);

    // function-argument coercion widens a narrower record (a missing field becomes ())
    query("fn($r as record(x, y as item()*)) { $r }({ 'x': 1 })", "{\"x\":1,\"y\":()}");
    // but a wider record is not narrowed: an extra field is rejected
    error("fn($r as record(x)) { $r }({ 'x': 1, 'y': 2 })", INVTYPE_X);
  }

  /** Recursive records. */
  @Test public void recRec() {
    query("declare variable $v as list := "
        + "{ 'value': 42, 'next': { 'value': 43, 'next': { 'value': 44 } } };\n"
        + "declare record list(value as item()*, next as list?);\n"
        + "$v",
        "{\"value\":42,\"next\":{\"value\":43,\"next\":{\"value\":44,\"next\":()}}}");
    query("declare variable $v := "
        + "  { 'value': 42, 'next': { 'value': 43, 'next': { 'value': 44 } } } instance of list;\n"
        + "declare record list(value as item()*, next as list?);\n"
        + "$v",
        false);
    query("declare variable $v := "
        + "  { 'value': 42, 'next': { 'value': 43, 'next': { 'value': 44, 'next': () } } } "
        + "instance of list;\n"
        + "declare record list(value as item()*, next as list?);"
        + "$v",
        true);
    // recursive RecordType.instanceOf
    query("declare record list1(value, next as list1?);\n"
        + "declare record list2(value, next as list2?);\n"
        + "fn($l as list2) as list1 { $l }({ 'value': () })",
        "{\"value\":(),\"next\":()}");
    // recursive RecordType.eq and RecordType.instanceOf
    query("declare record list1(value, next as list1?);\n"
        + "declare record list2(value, next as list2?);\n"
        + "declare function local:f1($l as list1) as list2 { $l };\n"
        + "declare function local:f2($f as fn(list2) as list1, $l as list1) as list2 { $f($l) };\n"
        + "local:f2(local:f1#1, { 'value': () })",
        "{\"value\":(),\"next\":()}");
    // recursive RecordType.eq and RecordType.instanceOf
    query("declare function local:f2($f as fn(list2) as list1, $l as list1) as list2 { $f($l) };\n"
        + "declare record list1(value, next as list1?);\n"
        + "declare record list2(value, next as record(value, next as list2?)?);\n"
        + "local:f2(fn($l as list1) as list2 { $l }, { 'value': 42, 'next': { 'value': 43 } })",
        "{\"value\":42,\"next\":{\"value\":43,\"next\":()}}");
    // recursive RecordType.union
    query("declare record list1(value, next as list1?);declare record list2(item, next as list2?);"
        + "fn($l1 as list1, $l2 as list2) { "
        + "map:merge(($l1, $l2))}("
        + "{ 'value': 42, 'next': { 'value': 43 } }, { 'item': 44,'next': { 'item': 45 } })",
        "{\"value\":42,\"next\":{\"value\":43,\"next\":()},\"item\":44}");
    // recursive RecordType.intersect
    query("declare record list1(next as list1?, x, y);\n"
        + "declare record list2(next as list2?, x, z);\n"
        + "let $f := fn($r as record(next as list2?, x as xs:boolean)) as xs:boolean { $r?x }\n"
        + "let $r as record(next as list1?, x as xs:untypedAtomic) := { 'x': <a>0</a> }\n"
        + "return $f($r)",
        "false");

    // recursive RecordType.eq and RecordType.instanceOf
    error("declare function local:f2($f as fn(list2) as list1, $l as list1) as list2 { $f($l) };\n"
        + "declare record list1(value, next as list1?);\n"
        + "declare record list2(value, next as record(value as xs:string, next as list2?)?);\n"
        + "local:f2(fn($l as list1) as list2 { $l }, { 'value': 42, 'next': { 'value': 43 } })",
        INVTYPE_X);
  }

  /** Record constructor function. */
  @Test public void recConstr() {
    query("declare namespace cx = 'CX';\n"
        + "declare record cx:complex(r as xs:double, i as xs:double := 0);\n"
        + "cx:complex(3, 2), cx:complex(3)",
        "{\"r\":3,\"i\":2}\n{\"r\":3,\"i\":0}");
    query("declare namespace cx = 'CX';\n"
        + "declare record cx:complex(r as xs:double, i as xs:double? := ());\n"
        + "cx:complex(3, 2), cx:complex(3)",
        "{\"r\":3,\"i\":2}\n{\"r\":3,\"i\":()}");
    query("declare namespace p = 'P'\n;"
        + "declare record p:person(first as xs:string, last as xs:string);\n"
        + "p:person('John', 'Smith')",
        "{\"first\":\"John\",\"last\":\"Smith\"}");
    // recursive record type constructor function
    query("declare function local:f($x, $y) {local:list($x, $y)};\n"
        + "declare record local:list (value as item()*, next as local:list?);\n"
        + "local:f(42, local:f(43, local:f(44, ())))",
        "{\"value\":42,\"next\":{\"value\":43,\"next\":{\"value\":44,\"next\":()}}}");
    // function as entry in record
    query("declare namespace geom = 'GEOM';\n"
        + "declare record geom:rectangle(\n"
        + "         width as xs:double,\n"
        + "         height as xs:double,\n"
        + "         area as fn(geom:rectangle) as xs:double :=\n"
        + "            fn($this as geom:rectangle) {\n"
        + "                $this?width \u00d7 $this?height\n"
        + "            }\n"
        + ");\n"
        + "let $box := geom:rectangle(3, 2)\n"
        + "return $box?area($box)",
        6);

    // constructor via function item
    query("declare record local:r(x); local:r#1(42)", "{\"x\":42}");
    // constructor via function lookup
    query("declare record local:r(x); function-lookup(#local:r, 1)(42)", "{\"x\":42}");

    // function declaration and record constructor with the same signature
    error("""
      declare function local:f($x) { local:r($x) };
      declare function local:r($x) { 43 };
      declare record local:r(x);
      local:f(42)
      """,
      DUPLFUNC_X);
    // record constructor and function declaration with the same signature
    error("""
      declare function local:f($x) { local:r($x) };
      declare record local:r(x);
      declare function local:r($x) { 43 };
      local:f(42)
      """,
      DUPLFUNC_X);
    // invalid initializer
    error("""
      declare namespace cx = 'CX';
      declare record cx:complex(r as xs:double, i as xs:double := ());
      cx:complex(3, 2), cx:complex(3)
      """,
      INVTYPE_X);
  }

  /** Static typing. */
  @Test public void typing() {
    check("declare record local:x(x); local:x(1)", "{\"x\":1}",
        type(StaticFuncCall.class, "local:x"));
    check("declare record local:x(x, y); local:x(1, 2)", "{\"x\":1,\"y\":2}",
        type(StaticFuncCall.class, "local:x"));
  }

  /** Inferred record types. */
  @Test public void inferredFieldTypes() {
    check("declare function local:f($cx as xs:double) {"
        + "  fold-left(<x><a/><a/></x>//a, { 'x': 0e0, 'y': 0e0 }, "
        + "    fn($acc, $node) { { 'x': $acc?x + $cx, 'y': $acc?y + 2e0 } })?x"
        + "}; local:f(1e0)",
        2, exists("RecordGet[@type = 'xs:double']"),
        empty("RecordGet[contains(@type, 'anyAtomicType')]"));

    inline(true);
    // while-do: 'x' stays xs:double, 'i' stays xs:integer
    check("while-do({ 'x': 0e0, 'i': 0 }, fn($s) { $s?i < 3 }, "
        + "fn($s) { { 'x': $s?x + 1e0, 'i': $s?i + 1 } })?x",
        3, exists("RecordGet[@type = 'xs:double']"),
        empty("RecordGet[contains(@type, 'anyAtomicType')]"));
    // fold-left over constructed nodes (not pre-evaluable): same specialization
    check("fold-left(<x><a/><a/></x>//a, { 'x': 0e0, 'y': 0e0 }, "
        + "fn($acc, $node) { { 'x': $acc?x + 1e0, 'y': $acc?y + 2e0 } })?x",
        2, exists("RecordGet[@type = 'xs:double']"),
        empty("RecordGet[contains(@type, 'anyAtomicType')]"));
  }

  /** Type propagation when removing entries. */
  @Test public void typeRemove() {
    final Function func = _MAP_REMOVE;
    check("declare record local:x(x); local:x(1) => map:remove('x')", "{}",
        root(XQTrieMap.class));
    check("declare record local:x(x); local:x(1) => map:remove(<_>x</_>)", "{}",
        root(XQTrieMap.class));
    check("declare record local:x(x); local:x(1) => map:remove('y')", "{\"x\":1}",
        empty(func));
    check("declare record local:x(x); local:x(1) => map:remove(<_>y</_>)", "{\"x\":1}",
        empty(func));
    check("declare record local:x(x); local:x(1) => map:remove(1)", "{\"x\":1}",
        empty(func));

    check("declare record local:x(x, y := ()); local:x(1) => map:remove('x')", "{\"y\":()}",
        type(func, "record(y)"));
    check("declare record local:x(x, y := ()); local:x(1) => map:remove(<_>x</_>)", "{\"y\":()}",
        type(func, "record(y)"));
    check("declare record local:x(x, y := ()); local:x(1) => map:remove('y')", "{\"x\":1}",
        type(func, "record(x)"));
    check("declare record local:x(x, y := ()); local:x(1) => map:remove(<_>y</_>)", "{\"x\":1}",
        type(func, "record(x)"));
    check("declare record local:x(x, y := ()); local:x(1) => map:remove('z')", "{\"x\":1,\"y\":()}",
        empty(func), type(StaticFuncCall.class, "local:x"));
    check("declare record local:x(x, y := ()); local:x(1) => map:remove(1)", "{\"x\":1,\"y\":()}",
        empty(func));
  }

  /** Type propagation when inserting entries. */
  @Test public void typePut() {
    final Function func = _MAP_PUT;
    check("declare record local:x(x); local:x(1) => map:put('x', 2)",
        "{\"x\":2}", type(RecordSet.class, "record(x)"));
    check("declare record local:x(x); local:x(1) => map:put(<_>x</_>, 2)",
        "{\"x\":2}", type(RecordSet.class, "record(x)"));
    check("declare record local:x(x); local:x(1) => map:put('y', 2)",
        "{\"x\":1,\"y\":2}", type(func, "record(x, y)"));
    check("declare record local:x(x); local:x(1) => map:put(<_>y</_>, 2)",
        "{\"x\":1,\"y\":2}", type(func, "record(x, y)"));
    check("declare record local:x(x); local:x(1) => map:put(0, 0)",
        "{\"x\":1,0:0}", type(func, "map(*)"));

    check("declare record local:x(x as xs:int); local:x(1) => map:put('x', <x/>)",
        "{\"x\":<x/>}", type(RecordSet.class, "record(x)"));

    check("declare record local:x(x, y := ()); local:x(1) => map:put('x', 2)",
        "{\"x\":2,\"y\":()}", type(RecordSet.class, "record(x, y)"));
    check("declare record local:x(x, y := ()); local:x(1) => map:put(<_>x</_>, 2)",
        "{\"x\":2,\"y\":()}", type(RecordSet.class, "record(x, y)"));
    check("declare record local:x(x, y := ()); local:x(1) => map:put('y', 2)",
        "{\"x\":1,\"y\":2}", type(RecordSet.class, "record(x, y)"));
    check("declare record local:x(x, y := ()); local:x(1) => map:put(<_>y</_>, 2)",
        "{\"x\":1,\"y\":2}", type(RecordSet.class, "record(x, y)"));
    check("declare record local:x(x, y := ()); local:x(1) => map:put(0, 0)",
        "{\"x\":1,\"y\":(),0:0}", type(func, "map(*)"));
  }

  /** A field update coerced back to its record type is fused into the {@code +:=} operator. */
  @Test public void typePutCoerce() {
    // map:put(R, FIELD, VALUE) coerce to RECORD  ->  R +:= map:entry(FIELD, VALUE)
    check("let $r as record(a, b) := { 'a': <a/>, 'b': 2 } "
        + "let $s as record(a, b) := map:put($r, 'a', 0) return $s",
        "{\"a\":0,\"b\":2}", root(RecordPut.class), empty(RecordSet.class));
    // the fused result is sealed again: strict field access applies
    error("declare record local:coord(x, y);\n"
        + "declare function local:reset($c as local:coord) as local:coord { map:put($c, 'x', 0) }; "
        + "local:reset(local:coord(<x>1</x>, <y>2</y>))?z", RECORDFIELD_X_X);
    // no fusion when the coercion target is not the record's own (strict) type
    check("let $r as record(a, b) := { 'a': <a/>, 'b': 2 } return map:put($r, 'a', 0)",
        "{\"a\":0,\"b\":2}", empty(RecordPut.class));
    // a chain of updates unrolls into +:= operations and the constant updates merge into one
    check("let $r as record(a, b) := { 'a': <a/>, 'b': 2 } "
        + "let $s as record(a, b) := $r => map:put('a', 0) => map:put('b', 9) return $s",
        "{\"a\":0,\"b\":9}",
        root(RecordPut.class), empty(RecordSet.class), count(RecordPut.class, 1));
    error("declare record local:coord(x, y);\n"
        + "declare function local:reset($c as local:coord) as local:coord "
        + "{ $c => map:put('x', 0) => map:put('y', 0) };\n"
        + "local:reset(local:coord(<x>1</x>, 2))?z", RECORDFIELD_X_X);
  }

  /** Consecutive constant {@code +:=} updates with disjoint keys are merged. */
  @Test public void recordPutMerge() {
    // disjoint keys merge into one update
    check("declare record local:c(x, y); "
        + "local:c(<x>1</x>, <x>2</x>) +:= { 'x': 0 } +:= { 'y': 0 }",
        "{\"x\":0,\"y\":0}", root(RecordPut.class), count(RecordPut.class, 1));
    // overlapping keys are not merged (the earlier value is still coerced), but use-last holds
    check("declare record local:c(x, y); "
        + "local:c(<x>1</x>, <x>2</x>) +:= { 'x': 0 } +:= { 'x': 1 }",
        "{\"x\":1,\"y\":<x>2</x>}", count(RecordPut.class, 2));
    // only the disjoint pair collapses; the overlapping update stays separate
    check("declare record local:c(x, y); "
        + "local:c(<x>1</x>, <x>2</x>) +:= { 'x': 0 } +:= { 'y': 0 } +:= { 'x': 9 }",
        "{\"x\":9,\"y\":0}", count(RecordPut.class, 2));
    // a non-constant update is not merged (the field value is evaluated at runtime)
    check("declare record local:c(x, y); "
        + "local:c(<x>1</x>, <x>2</x>) +:= { 'x': <n/> } +:= { 'y': 0 }",
        "{\"x\":<n/>,\"y\":0}", count(RecordPut.class, 2));
    // merging must not drop the shadowed value and mask its coercion error
    error("declare record local:c(x as xs:integer); "
        + "local:c(1) +:= { 'x': 'y' } +:= { 'x': 3 }", INVTYPE_X);
  }

  /** An update that supplies every field overwrites the record and drops the merge operator. */
  @Test public void recordPutCovered() {
    // a covering constant update makes the left operand dead: the operator folds away entirely
    check("let $r as record(a, b) := { 'a': 1, 'b': 2 } return $r +:= { 'a': 7, 'b': 8 }",
        "{\"a\":7,\"b\":8}", empty(RecordPut.class));
    // record(...) +:= record(...): every field is overwritten by the right operand
    query("declare record local:rec(a, b); local:rec(1, 2) +:= local:rec(4, 5)",
        "{\"a\":4,\"b\":5}");
    // a non-constant covering update also drops the operator; only the update construction remains
    check("let $r as record(a, b) := { 'a': 1, 'b': 2 } return $r +:= { 'a': <c/>, 'b': <d/> }",
        "{\"a\":<c/>,\"b\":<d/>}", empty(RecordPut.class), count(RecordConstructor.class, 1));
    // a covering constructor update builds the record type directly: no intermediate + coercion,
    // so the plan carries no TypeCheck (field types differ: integer arguments, double fields)
    check("declare record local:pt(x as xs:double, y as xs:double);\n"
        + "declare function f($p as local:pt, $i) as local:pt { $p +:= { 'x': $i, 'y': $i } };\n"
        + "f(local:pt(0, 0), 3)",
        "{\"x\":3,\"y\":3}", empty(RecordPut.class), empty(TypeCheck.class));
    // coercion still applies on the collapsed path: the integer is promoted to the field type
    query("let $r as record(a as xs:double) := { 'a': 1 } return ($r +:= { 'a': 2 })?a", 2);
    // the collapsed result stays a sealed record: an undeclared field in the update still errors
    error("let $r as record(a) := { 'a': 1 } return $r +:= { 'a': 2, 'c': 3 }", INVTYPE_X);
    // a partial update does NOT trigger the rewrite: the merge is kept, unmentioned fields survive
    check("declare record local:c(x, y); local:c(<x>1</x>, <y>2</y>) +:= { 'x': 0 }",
        "{\"x\":0,\"y\":<y>2</y>}", root(RecordPut.class));

    inline(true);
    // local:rec(...) is inlined and the whole expression constant-folds to a single record
    check("declare record local:rec(a, b); local:rec(1, 2) +:= local:rec(4, 5)",
        "{\"a\":4,\"b\":5}", empty(RecordPut.class), root(XQRecordMap.class));
  }

  /** Tests for the compact record map implementation. */
  @Test public void recordMap() {
    String map = "{ 'a': 1, 'b': 2 }";
    check(map + " => map:get('a')", 1, root(Itr.class));
    check(map + " => map:get('c')", "", empty());
    check(map + " => map:get(1)", "", empty());
    check(map + " => map:get(<?_ 1?> cast as xs:integer)", "", empty());
    check(map + " => map:put('b', 3)", "{\"a\":1,\"b\":3}", root(XQRecordMap.class));
    check(map + " => map:put('b', xs:byte(3))", "{\"a\":1,\"b\":3}", root(XQRecordMap.class));
    check(map + " => map:put('b', '3')", "{\"a\":1,\"b\":\"3\"}", root(XQTrieMap.class));
    check(map + " => map:put('c', 3)", "{\"a\":1,\"b\":2,\"c\":3}", root(XQTrieMap.class));
    check(map + " => map:put(3, 3)", "{\"a\":1,\"b\":2,3:3}", root(XQTrieMap.class));
    check(map + " => map:remove('b')", "{\"a\":1}", root(XQTrieMap.class));
    check(map + " => map:remove('c')", "{\"a\":1,\"b\":2}", root(XQRecordMap.class));
    check(map + " => map:remove(1)", "{\"a\":1,\"b\":2}", root(XQRecordMap.class));
    check(map + " => map:remove(<?_ 1?> cast as xs:integer)", "{\"a\":1,\"b\":2}",
        root(XQRecordMap.class));

    map = "{ 'a': 1, 'b': <?_ 2?> cast as xs:integer }";
    check(map + " => map:get('a')", 1, type(RecordGet.class, "xs:integer"));
    check(map + " => map:get('c')", "", empty());
    check(map + " => map:get(1)", "", empty());
    check(map + " => map:get(<?_ 1?> cast as xs:integer)", "", empty());
    check(map + " => map:get(<?_ a?>)", 1, type(_MAP_GET, "xs:integer?"));
    check(map + " => map:get(<?_ c?>)", "", type(_MAP_GET, "xs:integer?"));

    map = "for $b in 1 to 6 return { 'a': 1, 'b': $b }";
    check(map + " => map:get('b')", "1\n2\n3\n4\n5\n6", type(DualMap.class, "xs:integer+"));
  }

  /** {@code map:empty} must be structural: a non-singleton empty record is still empty. */
  @Test public void mapEmpty() {
    // constructed empty record: an XQRecordMap, not the shared empty map, but still empty
    query("declare record local:e(); "
        + "declare %basex:inline(0) function local:f() as map(*) { local:e() }; "
        + "map:empty(local:f())", true);
    // coerced empty record: same runtime type, reached via the coercion path
    query("declare record local:e(); "
        + "declare %basex:inline(0) function local:f($x as local:e) as map(*) { $x }; "
        + "map:empty(local:f({}))", true);
    // a populated record on the same runtime path must report non-empty
    query("declare record local:e(x); "
        + "declare %basex:inline(0) function local:f() as map(*) { local:e(1) }; "
        + "map:empty(local:f())", false);
  }

  /** A record-typed value that is not a compact XQRecordMap must still support field access. */
  @Test public void recordFieldAccess() {
    // RecordGet/RecordSet index into an XQRecordMap by field position; a record-typed plain map
    // (here an XQSingletonMap, kept non-constant so it survives to runtime) must not class-cast
    query("declare function local:f($v) { { 'a': $v }?a }; local:f(1)", 1);   // RecordGet
    query("map:get({ 'a': (1, 2)[. = 1] }, 'a')", 1);                         // MapGet -> RecordGet
    query("map:put({ 'a': (1, 2)[. = 1] }, 'a', 5)?a", 5);                    // RecordSet
    // a two-field record written out of order is materialized as a field-ordered XQRecordMap,
    // so positional field access still resolves each field to its own value
    query("declare function local:f($v) { { 'b': $v, 'a': 9 }?a }; local:f(1)", 9);
    query("declare function local:f($v) { { 'b': $v, 'a': 9 }?b }; local:f(1)", 1);
  }

  /** Coercion of records. */
  @Test public void coercion() {
    // empty record: only the empty map; any field is rejected
    query("let $m as record() := {} return $m", "{}");
    error("let $m as record() := { 'a': 1 } return $m", INVTYPE_X);
    error("let $m as record() := { 0: 2 } return $m", INVTYPE_X);

    // single field: extra fields are rejected
    query("let $m as record(a) := { 'a': 1 } return $m", "{\"a\":1}");
    error("let $m as record(a) := { 'a': 1, 'b': 2 } return $m", INVTYPE_X);
    error("let $m as record(a) := { 'a': 1, 0: 2 } return $m", INVTYPE_X);
    error("let $m as record(b) := { 'a': 1, 'b': 2 } return $m", INVTYPE_X);
    query("let $m as record(a) := map { xs:untypedAtomic('a'): 1 } return $m", "{\"a\":1}");

    // missing field (untyped, so item()*) becomes the empty sequence
    query("let $m as record(a) := {} return $m", "{\"a\":()}");
    query("let $m as record(a, b) := { 'a': 1 } return $m", "{\"a\":1,\"b\":()}");
    query("let $m as record(b, a) := { 'a': 1 } return $m", "{\"b\":(),\"a\":1}");

    // entry order follows the record type definition
    query("let $m as record(a, b) := { 'a': 1, 'b': 2 } return $m", "{\"a\":1,\"b\":2}");
    query("let $m as record(a, b) := { 'b': 2, 'a': 1 } return $m", "{\"a\":1,\"b\":2}");
    query("let $m as record(b, a) := { xs:untypedAtomic('a'): 1, 'b': 2 } return $m",
        "{\"b\":2,\"a\":1}");

    // extra field is rejected even when reordering occurs
    error("let $m as record(b, a) := { 'a': 1, 'x': 9, 'b': 2 } return $m", INVTYPE_X);

    // function-argument coercion rejects extra fields
    error("fn($r as record(a)) { $r } ({ 'a': 1, 'b': 2 })", INVTYPE_X);

    // missing field whose type does not admit the empty sequence is an error
    error("let $m as record(a as xs:integer) := {} return $m", INVTYPE_X);
    error("let $m as record(a, b as xs:integer) := { 'a': 1 } return $m", INVTYPE_X);
  }

  /** Equality of map/record constructors. */
  @Test public void equal() {
    // empty record
    String constr = "{}";
    check("(" + constr + ", " + constr + ")", "{}\n{}", exists(SingletonSeq.class));

    // single-entry record (compile-time evaluation)
    constr = "{ 'AA': 0 }";
    check("(" + constr + ", " + constr + ")?AA", "0\n0", exists(SingletonSeq.class));
    // small record (compile-time evaluation)
    constr = "{ 'AA': 0, 'AB': 1 }";
    check("(" + constr + ", " + constr + ")?AA", "0\n0", exists(SingletonSeq.class));
    check("(" + constr + ", { 'AA': 0, 'AB': 2 })?AA", "0\n0", empty(SingletonSeq.class));
    // big record (compile-time evaluation)
    constr =
        "{ 'AA': 0, 'AB': 0, 'AC': 0, 'AD': 0, 'AE': 0, 'AF': 0, 'AG': 0, 'AH': 0, 'AI': 0, "
        + "'AJ': 0, 'AK': 0, 'AL': 0, 'AM': 0, 'AN': 0, 'AO': 0, 'AP': 0, 'AQ': 0, 'AR': 0, "
        + "'AS': 0, 'AT': 0, 'AU': 0, 'AV': 0, 'AW': 0, 'AX': 0, 'AY': 0, 'AZ': 0 }";
    check("(" + constr + ", " + constr + ")?AA", "0\n0", exists(SingletonSeq.class));
    // map (compile-time evaluation)
    constr =
        "{ 'AA': 0, 'AB': 0, 'AC': 0, 'AD': 0, 'AE': 0, 'AF': 0, 'AG': 0, 'AH': 0, 'AI': 0, "
        + "'AJ': 0, 'AK': 0, 'AL': 0, 'AM': 0, 'AN': 0, 'AO': 0, 'AP': 0, 'AQ': 0, 'AR': 0, "
        + "'AS': 0, 'AT': 0, 'AU': 0, 'AV': 0, 'AW': 0, 'AX': 0, 'AY': 0, 'AZ': 0, "
        + "'BA': 0, 'BB': 0, 'BC': 0, 'BD': 0, 'BE': 0, 'BF': 0, 'BG': 0 }";
    check("(" + constr + ", " + constr + ")?AA", "0\n0", exists(SingletonSeq.class));

    // single-entry record (runtime evaluation)
    constr = "{ 'AA': <a/> }";
    check("(" + constr + ", " + constr + ")?AA", "<a/>\n<a/>", exists(REPLICATE));
    // small record (runtime evaluation)
    constr = "{ 'AA': 0, 'AB': <a/> }";
    check("(" + constr + ", " + constr + ")?AA", "0\n0", exists(REPLICATE));
    check("(" + constr + ", { 'AA': 0, 'AB': <b/> })?AA", "0\n0", empty(REPLICATE));
    // big record (runtime evaluation)
    constr =
        "{ 'AA': 0, 'AB': 0, 'AC': 0, 'AD': 0, 'AE': 0, 'AF': 0, 'AG': 0, 'AH': 0, 'AI': 0, "
        + "'AJ': 0, 'AK': 0, 'AL': 0, 'AM': 0, 'AN': 0, 'AO': 0, 'AP': 0, 'AQ': 0, 'AR': 0, "
        + "'AS': 0, 'AT': 0, 'AU': 0, 'AV': 0, 'AW': 0, 'AX': 0, 'AY': 0, 'AZ': <a/> }";
    check("(" + constr + ", " + constr + ")?AA", "0\n0", exists(REPLICATE));
    // map (runtime evaluation)
    constr =
        "{ 'AA': 0, 'AB': 0, 'AC': 0, 'AD': 0, 'AE': 0, 'AF': 0, 'AG': 0, 'AH': 0, 'AI': 0, "
        + "'AJ': 0, 'AK': 0, 'AL': 0, 'AM': 0, 'AN': 0, 'AO': 0, 'AP': 0, 'AQ': 0, 'AR': 0, "
        + "'AS': 0, 'AT': 0, 'AU': 0, 'AV': 0, 'AW': 0, 'AX': 0, 'AY': 0, 'AZ': 0, "
        + "'BA': 0, 'BB': 0, 'BC': 0, 'BD': 0, 'BE': 0, 'BF': 0, 'BG': <a/> }";
    check("(" + constr + ", " + constr + ")?AA", "0\n0", exists(REPLICATE));

    // named record (runtime evaluation)
    check("declare record local:r(AA, AB); (local:r(0, <a/>), local:r(0, <a/>))?AA",
        "0\n0", exists(REPLICATE));
  }

  /** Checks that every built-in record type has a constructor function in {@link Function}. */
  @Test public void builtInRecordsHaveConstructors() {
    final QNmSet funcNames = new QNmSet();
    for(final Function f : Function.values()) {
      funcNames.add(f.definition().name);
    }
    for(final Records record : Records.values()) {
      assertTrue(funcNames.contains(record.get().name()),
          "Missing constructor function for " + record);
    }
  }
}
