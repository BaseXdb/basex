package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.query.expr.constr.*;
import org.junit.jupiter.api.*;

/**
 * Lookup operator tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class LookupTest extends SandboxTest {
  /** Multiple inputs and multiple keys are rewritten to a FLWOR expression. */
  @Test public void multipleInputs() {
    query("let $ms := ({ 'a': 1, 'b': 2 }, { 'a': 3, 'b': 4 }) return $ms?('a', 'b')",
        "1\n2\n3\n4");
    query("let $ms := ([ 1, 2 ], [ 3, 4 ]) return $ms?(1, 2)", "1\n2\n3\n4");
  }

  /** Test. */
  @Test public void map() {
    query("{ 'a': 'b' } ? a", "b");
    query("{ 'a': 'b' } ? c", "");
    query("({ 'a': 'b' }, { 'c': 'd' }) ? a", "b");
    query("{ 'a': 'b', 'c': 'd' } ? ('a', 'c')", "b\nd");
    query("({ 'a': 'b' }, { 'c': 'd' }) ? ('a', 'c')", "b\nd");
    query("map:merge(for $i in 1 to 5 return { $i: $i+1 })? 2", 3);

    query("{ 'first' : 'Jenna', 'last' : 'Scott' } ? first", "Jenna");
    query("({ 'first': 'Tom' }, { 'first': 'Dick' }, { 'first': 'Harry' }) ? first",
        "Tom\nDick\nHarry");

    query("<_>X</_>[{ 'Y': <_/> }?(text())]", "");
  }

  /** Test. */
  @Test public void array() {
    query("array { 'a', 'b' } ? 1", "a");
    query("(array { 'a', 'b' }, array { 'c', 'd' }) ? 1", "a\nc");
    query("(array { 'a', 'b', 'c' }) ? (1, 2)", "a\nb");
    query("(array { 'a', 'b', 'c' }) ? (1 to 2)", "a\nb");
    query(_ARRAY_JOIN.args(" for $i in 1 to 5 return array { $i+1 }") + " ? 2", 3);

    query("[1, 2, 5, 7] ?*", "1\n2\n5\n7");
    query("[[1, 2, 3], [4, 5, 6]] ?* ?*", "1\n2\n3\n4\n5\n6");
    query("[4, 5, 6]?2", 5);
    query("([1, 2, 3], [4, 5, 6])?2", "2\n5");
  }

  /** Test. */
  @Test public void mixed() {
    query("({ 1: 'm' }, array { 'a' }) ? 1", "m\na");
  }

  /** Test. */
  @Test public void wildcard() {
    query("({ 1: 'm' }, array { 'a' }) ? *", "m\na");
  }

  /** Test. */
  @Test public void string() {
    query("serialize(fn($m) { $m?a, $m?1, $m?*, $m?('a b') }, { 'method': 'adaptive' })",
        "fn($m) as item()* { ($m?a, $m?1, $m?*, $m?(\"a b\")) }");
  }

  /** Test. */
  @Test public void unary() {
    query("({ 1: 'm' }, array { 'a' }) ! ?*", "m\na");
    query("array { 1 }[?1] ! ?1", 1);
  }

  /** Test. */
  @Test public void wildcardKey() {
    query("{ '*': 's', 'a': 1 }?('*')", "s");
    query("{ '*': 's', 'a': 1 }[" + wrap(1) + " = 1]?('*')", "s");
    query("{ '*': 's', 'a': 1 }[" + wrap(1) + " = 1]?(string(" + wrap("*") + "))", "s");
    query("({ '*': 's' }, [ 1 ])[" + wrap(1) + " = 1]?*", "s\n1");
    query("declare %basex:inline(0) function local:f($m as item(), $b) {"
        + " if($b) then $m?* else $m?('*') }; local:f({ '*': 's', 'a': 1 }, false())", "s");
    query("serialize(fn($m) { $m?('*') }, { 'method': 'adaptive' })",
        "fn($m) as item()* { $m?(\"*\") }");
  }

  /** Test. */
  @Test public void typing() {
    query("empty(array:for-each([], function($i) { string($i) })?*)", true);
    query("empty(array:for-each([], function($i) { string($i) })!?*)", true);
    query("empty(array:for-each([], function($i) { string($i) })?())", true);
    query("empty(array:for-each([], function($i) { string($i) })!?())", true);
  }

  /** Test. */
  @Test public void emptyKey() {
    query("array:for-each([], function($i) { string($i) })?([])", "");
    query("array:for-each([], function($i) { string($i) })!?([])", "");
  }

  /** Rewrite lookups to map:get/array:get unless this could suppress a strict-record error. */
  @Test public void rewrite() {
    check("({ 'a': <x/> }, { 'b': <y/> })?a", "<x/>", exists(Lookup.class), empty(_MAP_GET));
    check("declare record local:r(a, b); local:r(<x/>, <y/>)?('a', 'b')", "<x/>\n<y/>",
        exists(Lookup.class));
    check("[ <x/>, <y/> ]?(1, 2)", "<x/>\n<y/>", exists(Lookup.class));

    check("({ 'a': <x/> })?a", "<x/>", exists(ShapeGet.class), empty(Lookup.class));
    check("({ 'a': <x/> })?b", "", exists(Lookup.class));
    check("declare %basex:inline(0) function local:f($x as record(a, b)) { $x?('a', 'b') };"
        + " local:f({ 'a': 1, 'b': 2 })", "1\n2", empty(Lookup.class));
    check("({ 1: <x/> })?1", "<x/>", exists(_MAP_GET), empty(Lookup.class));
    check("({ 'a': <x/> })?*", "<x/>", exists(_MAP_ITEMS), empty(Lookup.class));
    check("[ <x/> ]?1", "<x/>", exists(_ARRAY_GET), empty(Lookup.class));
    check("[ <x/> ]?*", "<x/>", root(CElem.class));
  }

  /** Focus-dependent key: the focus of the lookup expression is used. */
  @Test public void focusKey() {
    final String map = "map:build((1, 2), value := fn($k) { ('a', 'b')[$k] })";
    query("declare %basex:inline(0) function local:f($m as map(xs:integer, xs:string)?) {"
        + " (1, 2)[$m?(.) = 'b'] }; local:f(" + map + ")", 2);
    query("declare %basex:inline(0) function local:f($m as map(xs:integer, xs:string)*) {"
        + " (1, 2)[$m?(.) = 'b'] }; local:f((" + map + ", " + map + "))", 2);
    query("declare %basex:inline(0) function local:f($a as array(xs:string)?) {"
        + " (1, 2)[$a?(.) = 'b'] }; local:f([ 'a', 'b' ])", 2);
  }

  /** Static typing: choices of records. */
  @Test public void choiceTyping() {
    final String decl = "declare record local:a(x as xs:integer, y as xs:string); "
        + "declare record local:b(x as xs:double); declare %basex:inline(0) function local:f(";
    check(decl + "$r as (local:a|local:b)) { $r?x }; local:f(local:a(1, 's'))", 1,
        type(Lookup.class, "xs:numeric"));
    check(decl + "$r as (local:a|local:b)) { $r?y }; local:f(local:a(1, 's'))", "s",
        type(Lookup.class, "xs:string?"));
    check(decl + "$r as (local:a|xs:integer)) { $r?y }; local:f(local:a(1, 's'))", "s",
        type(Lookup.class, "xs:string"));
    check(decl + "$r as (local:a|local:b)*) { $r?x }; local:f((local:a(1, 's'), local:b(2)))",
        "1\n2", type(Lookup.class, "xs:numeric*"));
    check(decl + "$r as (local:a|local:b)) { map:get($r, 'x') }; local:f(local:a(1, 's'))", 1,
        type(_MAP_GET, "xs:numeric"));
    check(decl + "$r as (local:a|local:b)) { map:get($r, 'y') }; local:f(local:b(1))", "",
        type(_MAP_GET, "xs:string?"));
    check(decl + "$r as (local:a|local:b)) { map:get($r, 'y', 'z') }; local:f(local:b(1))", "z",
        type(_MAP_GET, "xs:string"));

    error(decl + "$r as (local:a|local:b)) { $r?z }; local:f(local:a(1, 's'))",
        RECORDFIELD_X);
  }

  /** JNodes: lookup is applied to each item of the value. */
  @Test public void jnode() {
    query("let $j := { 'a': ({ 'x': 1 }, { 'x': 2 }) }/a return $j?x", "1\n2");
    query("let $j := { 'a': () }/a return $j?x", "");
  }

  /** Test. */
  @Test public void error() {
    error("1?a", LOOKUP_X);
    error("(1, 2)[" + wrap(1) + " = 1]?((1)[" + wrap(1) + " = 2])", LOOKUP_X);
    error("declare record local:r(a, b); local:r(1, 2)?c", RECORDFIELD_X);
    error("declare record local:r(a, b); (local:r(1, 2), { 'c': 3 })?c", RECORDFIELD_X);
    error("declare record local:r(a, b); local:r(<x/>, <y/>)?1", RECORDFIELD_X);
    error("declare record local:r(a, b); declare %basex:inline(0) function local:f("
        + "$x as record(a, b)) { $x?c }; local:f(local:r(1, 2))", RECORDFIELD_X);
    error("declare record local:r(a, b); declare %basex:inline(0) function local:f("
        + "$x as record(*)) { $x?c }; local:f(local:r(1, 2))", RECORDFIELD_X);
    error("declare record local:r(a, b); declare %basex:inline(0) function local:f("
        + "$x as record(*)) { if($x instance of record(a, b)) { $x?c } };"
        + " local:f(local:r(1, 2))", RECORDFIELD_X);
  }
}
