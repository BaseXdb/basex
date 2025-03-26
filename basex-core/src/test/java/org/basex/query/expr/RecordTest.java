package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.query.func.*;
import org.basex.query.func.map.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
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

    query("{ } instance of record()", true);
    query("{ 'x': () } instance of record()", false);
    query("declare record local:empty(); {} instance of local:empty", true);
    query("declare record local:empty(); { 'x': () } instance of local:empty", false);

    query("{ 'x': () } instance of record(x)", true);
    query("{ } instance of record(x)", false);

    query("{ 'x': (), 'y': () } instance of record(x, *)", true);
    query("{ 'x': (), 'y': () } instance of record(x)", false);
    query("{ 'x': (), 0: () } instance of record(x, *)", true);
    query("{ 'x': (), 0: () } instance of record(x)", false);

    query("declare record local:coord(x, y); "
        + "let $coord := local:coord(1, 2) "
        + "let $new := map:remove($coord, 'x') "
        + "return $new instance of local:coord", false);
  }

  /** Recursive records. */
  @Test public void recRec() {
    query("declare variable $v as list := {'value':42,'next':{'value':43,'next':{'value':44}}};\n"
        + "declare record list(value as item()*, next? as list);\n"
        + "$v",
        "{\"value\":42,\"next\":{\"value\":43,\"next\":{\"value\":44}}}");
    query("declare variable $v :=\n"
        + "  {'value':42,'next':{'value':43,'next':{'value':44}}} instance of list;\n"
        + "declare record list(value as item()*, next? as list);\n"
        + "$v",
        true);
    query("declare variable $v :=\n"
        + "  {'value':42,'next':{'value':43,'next':{'value':44,'next':()}}} instance of list;\n"
        + "declare record list(value as item()*, next? as list);"
        + "$v",
        false);
    // recursive RecordType.instanceOf
    query("declare record list1(value, next? as list1);\n"
        + "declare record list2(value, next? as list2);\n"
        + "fn($l as list2) as list1 {$l} ({'value': ()})",
        "{\"value\":()}");
    // recursive RecordType.eq and RecordType.instanceOf
    query("declare record list1(value, next? as list1);\n"
        + "declare record list2(value, next? as list2);\n"
        + "declare function local:f1($l as list1) as list2 {$l};\n"
        + "declare function local:f2($f as fn(list2) as list1, $l as list1) as list2 {$f($l)};\n"
        + "local:f2(local:f1#1, {'value': ()})",
        "{\"value\":()}");
    // recursive RecordType.eq and RecordType.instanceOf
    query("declare function local:f2($f as fn(list2) as list1, $l as list1) as list2 {$f($l)};\n"
        + "declare record list1(value, next? as list1);\n"
        + "declare record list2(value, next? as record(value, next? as list2));\n"
        + "local:f2(fn($l as list1) as list2 {$l}, {'value': 42, 'next': {'value': 43}})",
        "{\"value\":42,\"next\":{\"value\":43}}");
    // recursive RecordType.union
    query("declare record list1(value, next? as list1);declare record list2(item, next? as list2);"
        + "fn($l1 as list1, $l2 as list2) {map:merge(($l1, $l2))} ({'value':42,'next':{'value':43}}"
        + ",{'item':44,'next':{'item':45}})", "{\"value\":42,\"next\":{\"value\":43},\"item\":44}");
    // recursive RecordType.intersect
    query("declare record list1(next? as list1, x, y?);\n"
        + "declare record list2(next? as list2, x, z?);\n"
        + "let $f := fn($r as record(next? as list2, x as xs:boolean)) as xs:boolean {$r?x}\n"
        + "let $r as record(next? as list1, x as xs:untypedAtomic) := {'x':<a>0</a>}\n"
        + "return $f($r)",
        "false");

    //  cannot convert empty-sequence() to optional list
    error("declare variable $v as list :=\n"
        + "  {'value':42,'next':{'value':43,'next':{'value':44,'next':()}}};\n"
        + "declare record list(value as item()*, next? as list);\n"
        + "$v",
        INVCONVERT_X_X_X);
    // recursive RecordType.eq and RecordType.instanceOf
    error("declare function local:f2($f as fn(list2) as list1, $l as list1) as list2 {$f($l)};\n"
        + "declare record list1(value, next? as list1);\n"
        + "declare record list2(value, next? as record(value as xs:string, next? as list2));\n"
        + "local:f2(fn($l as list1) as list2 {$l}, {'value': 42, 'next': {'value': 43}})",
        INVCONVERT_X_X_X);
  }

  /** Record constructor function. */
  @Test public void recConstr() {
    query("declare namespace cx = 'CX';\n"
        + "declare record cx:complex(r as xs:double, i as xs:double := 0);\n"
        + "cx:complex(3, 2), cx:complex(3)",
        "{\"r\":3,\"i\":2}\n{\"r\":3,\"i\":0}");
    query("declare namespace cx = 'CX';\n"
        + "declare record cx:complex(r as xs:double, i? as xs:double);\n"
        + "cx:complex(3, 2), cx:complex(3)",
        "{\"r\":3,\"i\":2}\n{\"r\":3}");
    query("declare namespace cx = 'CX';\n"
        + "declare record cx:complex(r as xs:double, i? as xs:double := ());\n"
        + "cx:complex(3, 2), cx:complex(3)",
        "{\"r\":3,\"i\":2}\n{\"r\":3,\"i\":()}");
    query("declare namespace p = 'P'\n;"
        + "declare record p:person(first as xs:string, last as xs:string, *);\n"
        + "p:person('John', 'Smith', {'last': 'Miller', 'middle': 'A.'})",
        "{\"first\":\"John\",\"last\":\"Smith\",\"middle\":\"A.\"}");
    // recursive record type constructor function
    query("declare function local:f($x, $y) {local:list($x, $y)};\n"
        + "declare record local:list (value as item()*, next? as local:list);\n"
        + "local:f(42, local:f(43, local:f(44, ())))",
        "{\"value\":42,\"next\":{\"value\":43,\"next\":{\"value\":44}}}");
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
  }

  /** Static typing. */
  @Test public void typing() {
    check("declare record local:x(x); local:x(1)", "{\"x\":1}",
        type(StaticFuncCall.class, "local:x"));
    check("declare record local:x(x, y); local:x(1, 2)", "{\"x\":1,\"y\":2}",
        type(StaticFuncCall.class, "local:x"));
  }

  /** Type propagation when removing entries. */
  @Test public void typeRemove() {
    final Function func = _MAP_REMOVE;
    check("declare record local:x(x); local:x(1) => map:remove('x')", "{}",
        type(func, "map(xs:string, item()*)"));
    check("declare record local:x(x); local:x(1) => map:remove(<_>x</_>)", "{}",
        type(func, "map(xs:string, item()*)"));
    check("declare record local:x(x); local:x(1) => map:remove('y')", "{\"x\":1}",
        empty(func));
    check("declare record local:x(x); local:x(1) => map:remove(<_>y</_>)", "{\"x\":1}",
        empty(func));
    check("declare record local:x(x); local:x(1) => map:remove(1)", "{\"x\":1}",
        empty(func));

    check("declare record local:x(x, *); local:x(1) => map:remove('x')", "{}",
        type(func, "map(*)"));
    check("declare record local:x(x, *); local:x(1) => map:remove(<_>x</_>)", "{}",
        type(func, "map(*)"));
    check("declare record local:x(x, *); local:x(1) => map:remove('y')", "{\"x\":1}",
        type(func, "local:x"));
    check("declare record local:x(x, *); local:x(1) => map:remove(<_>y</_>)", "{\"x\":1}",
        type(func, "local:x"));
    check("declare record local:x(x, *); local:x(1) => map:remove(1)", "{\"x\":1}",
        type(func, "local:x"));

    check("declare record local:x(x, y?); local:x(1) => map:remove('x')", "{}",
        type(func, "map(xs:string, item()*)"));
    check("declare record local:x(x, y?); local:x(1) => map:remove(<_>x</_>)", "{}",
        type(func, "map(xs:string, item()*)"));
    check("declare record local:x(x, y?); local:x(1) => map:remove('y')", "{\"x\":1}",
        type(func, "local:x"));
    check("declare record local:x(x, y?); local:x(1) => map:remove(<_>y</_>)", "{\"x\":1}",
        type(func, "local:x"));
    check("declare record local:x(x, y?); local:x(1) => map:remove('z')", "{\"x\":1}",
        empty(func), type(StaticFuncCall.class, "local:x"));
    check("declare record local:x(x, y?); local:x(1) => map:remove(1)", "{\"x\":1}",
        empty(func));
  }

  /** Type propagation when inserting entries. */
  @Test public void typePut() {
    final Function func = _MAP_PUT;
    check("declare record local:x(x); local:x(1) => map:put('x', 2)", "{\"x\":2}",
        type(func, "local:x"));
    check("declare record local:x(x); local:x(1) => map:put(<_>x</_>, 2)", "{\"x\":2}",
        type(func, "local:x"));
    check("declare record local:x(x); local:x(1) => map:put('y', 2)", "{\"x\":1,\"y\":2}",
        type(func, "map(xs:string, item()*)"));
    check("declare record local:x(x); local:x(1) => map:put(<_>y</_>, 2)", "{\"x\":1,\"y\":2}",
        type(func, "map(*)"));
    check("declare record local:x(x); local:x(1) => map:put(0, 0)", "{\"x\":1,0:0}",
        type(func, "map(*)"));

    check("declare record local:x(x as xs:int); local:x(1) => map:put('x', <x/>)", "{\"x\":<x/>}",
        type(func, "map(xs:string, item())"));

    check("declare record local:x(x, *); local:x(1) => map:put('x', 2)", "{\"x\":2}",
        type(func, "local:x"));
    check("declare record local:x(x, *); local:x(1) => map:put(<_>x</_>, 2)", "{\"x\":2}",
        type(func, "local:x"));
    check("declare record local:x(x, *); local:x(1) => map:put('y', 2)", "{\"x\":1,\"y\":2}",
        type(func, "local:x"));
    check("declare record local:x(x, *); local:x(1) => map:put(<_>y</_>, 2)", "{\"x\":1,\"y\":2}",
        type(func, "local:x"));
    check("declare record local:x(x, *); local:x(1) => map:put(0, 0)", "{\"x\":1,0:0}",
        type(func, "local:x"));

    check("declare record local:x(x, y?); local:x(1) => map:put('x', 2)", "{\"x\":2}",
        type(func, "local:x"));
    check("declare record local:x(x, y?); local:x(1) => map:put(<_>x</_>, 2)", "{\"x\":2}",
        type(func, "local:x"));
    check("declare record local:x(x, y?); local:x(1) => map:put('y', 2)", "{\"x\":1,\"y\":2}",
        type(func, "local:x"));
    check("declare record local:x(x, y?); local:x(1) => map:put(<_>y</_>, 2)", "{\"x\":1,\"y\":2}",
        type(func, "local:x"));
    check("declare record local:x(x, y?); local:x(1) => map:put(0, 0)", "{\"x\":1,0:0}",
        type(func, "map(*)"));
  }

  /** Tests for the compact record map implementation. */
  @Test public void recordMap() {
    String map = "{ 'a': 1, 'b': 2 }";
    check(map + " => map:get('a')", 1, root(Int.class));
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
    check(map + " => map:get('a')", 1, type(MapGet.class, "xs:integer"));
    check(map + " => map:get('c')", "", empty());
    check(map + " => map:get(1)", "", empty());
    check(map + " => map:get(<?_ 1?> cast as xs:integer)", "", empty());
    check(map + " => map:get(<?_ a?>)", 1, type(MapGet.class, "xs:integer?"));
    check(map + " => map:get(<?_ c?>)", "", type(MapGet.class, "xs:integer?"));

    map = "for $b in 1 to 6 return { 'a': 1, 'b': $b }";
    check(map + " => map:get('b')", "1\n2\n3\n4\n5\n6", type(DualMap.class, "xs:integer+"));
  }
}
