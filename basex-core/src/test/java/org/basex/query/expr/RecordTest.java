package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import org.basex.*;
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
        "{\"r\":3.0e0,\"i\":2.0e0}\n{\"r\":3.0e0,\"i\":0.0e0}");
    query("declare namespace cx = 'CX';\n"
        + "declare record cx:complex(r as xs:double, i? as xs:double);\n"
        + "cx:complex(3, 2), cx:complex(3)",
        "{\"r\":3.0e0,\"i\":2.0e0}\n{\"r\":3.0e0}");
    query("declare namespace cx = 'CX';\n"
        + "declare record cx:complex(r as xs:double, i? as xs:double := ());\n"
        + "cx:complex(3, 2), cx:complex(3)",
        "{\"r\":3.0e0,\"i\":2.0e0}\n{\"r\":3.0e0,\"i\":()}");
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
}
