package org.basex.query.simple;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * XQuery type tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TypeTest extends SandboxTest {
  /** Casts. */
  @Test public void cast() {
    query("1 castable as xs:integer", true);
    query("1 castable as xs:integer?", true);
    query("() castable as xs:integer?", true);
    query("(42 cast as enum('42')) cast as enum('42', '43')", "42");
    query("(42 cast as enum('42')) castable as enum('42', '43')", true);
    query("(42 cast as enum('42')) castable as enum('43')", false);
    query("() castable as xs:error?", true);
    query("42 castable as xs:error?", false);
    query("() cast as xs:error?", "");
    query("xs:error(())", "");
    query("1 castable as xs:integer+", true);
    query("(1, 2) castable as xs:integer+", true);
    query("[1, 2] castable as array(xs:integer)", true);
    query("[1, 'x'] castable as array(xs:integer)", false);
    query("{ 'a': 1 } castable as record(a as xs:integer)", true);
    query("{ 'a': 1 } castable as record(a as xs:integer, b as xs:integer)", false);
    query("{ 'a': 1, 'b': 2 } castable as record(a as xs:integer)", true);
    query("string(([1] cast as array(xs:integer))?1)", "1");
    query("string(({ 'a': '2' } cast as record(a as xs:integer))?a)", "2");
    query("true#0 castable as xs:string", false);
    query("name(<x/> cast as item())", "x");

    error("1 castable as xs:integer()", SIMPLETYPE_X);
    error("1 castable as xml:integer", WHICHCAST_X);
    error("1 castable as integer", WHICHCAST_X);
    error("1 castable as xs:NOTATION", INVALIDCAST_X);
    error("1 castable as xs:anyAtomicType", INVALIDCAST_X);
    error("(42 cast as enum('42')) cast as enum('x')", FUNCCAST_X_X);
    error("42 cast as xs:error?", FUNCCAST_X_X);
    error("xs:error(42)", FUNCCAST_X_X);
    error("1 cast as function(*)", INVALIDCAST_X);
    // component types need not be eligible; the integer just fails to coerce to an array/map
    error("1 cast as array(element())", INVTYPE_X);
    error("1 cast as map(xs:anyAtomicType, xs:string)", INVTYPE_X);

    query("xs:integer('+1')", 1);
    error("xs:integer('++1')", FUNCCAST_X_X);
    query("string('/') castable as xs:QName", false);
    query("try { '1999-12-31'/. castable as xs:date } catch err:XPTY0004 { 'error' }", "error");
    query("declare function local:shortcircuit($a) {"
        + "  if($a castable as xs:double and xs:double($a) gt 0) then $a else 'bar'"
        + "};"
        + "local:shortcircuit('foo')", "bar");
    query("xs:integer(())", "");
    query("xs:integer#1(())", "");
    query("xs:integer(?)(())", "");
    query("('1', '2 3') ! xs:NMTOKENS(.)[1]", "1\n2");
    error("exactly-one(xs:NMTOKENS(<x>1 2</x>))", EXACTLYONE);
  }

  /** xs:float. */
  @Test public void xsFloat() {
    error("xs:float('Infinity')", FUNCCAST_X_X);
    error("xs:float('infinity')", FUNCCAST_X_X);
    query("xs:float('INF') > 0", true);
    error("xs:float('inf')", FUNCCAST_X_X);
    error("xs:float('-Infinity')", FUNCCAST_X_X);
    error("xs:float('-infinity')", FUNCCAST_X_X);
    query("xs:float('-INF') < 0", true);
    error("xs:float('-inf')", FUNCCAST_X_X);
    query("xs:float('+INF') > 0", true);
  }

  /** xs:double. */
  @Test public void xsDouble() {
    error("xs:double('Infinity')", FUNCCAST_X_X);
    error("xs:double('infinity')", FUNCCAST_X_X);
    query("xs:double('INF') > 0", true);
    error("xs:double('inf')", FUNCCAST_X_X);
    error("xs:double('-Infinity')", FUNCCAST_X_X);
    error("xs:double('-infinity')", FUNCCAST_X_X);
    query("xs:double('-INF') < 0", true);
    error("xs:double('-inf')", FUNCCAST_X_X);
    query("xs:double('+INF') > 0", true);
  }

  /** xs:unsignedLong comparisons. */
  @Test public void unsignedLong() {
    query("xs:unsignedLong('3') eq 3.1", false);
    query("3.1 eq xs:unsignedLong('3')", false);
    query("xs:unsignedLong(3) lt 3.1", true);
    query("3.1 gt xs:unsignedLong(3)", true);
    query("compare(3.1, xs:unsignedLong('3'))", 1);
    query("compare(xs:unsignedLong('3'), 3.1)", -1);
  }

  /** Node tests. */
  @Test public void nodeTest() {
    query("let $d as document-node(element()) := parse-xml('<!--a--><a/>') return name($d/*)", "a");
    query("let $d as document-node(element(a)) := parse-xml('<!--a--><a/>') return name($d/*)",
        "a");
  }

  /** Typeswitch. */
  @Test public void typeswitch() {
    query("typeswitch(<a>1</a>) case xs:string return 1 default return 1", 1);
    query("typeswitch(<a>1</a>) case $a as xs:string return 1 default return 1", 1);
    query("typeswitch(<a>1</a>) case $a as xs:string return (1, 2) default return (1, 2)", "1\n2");
    query("(xs:byte(0), xs:short(0), xs:int(0), xs:long(0), 0) ! "
        + "(typeswitch (.)"
        + " case xs:byte return 1"
        + " case xs:short return 2"
        + " case xs:int return 3"
        + " case xs:long return 4"
        + " case xs:decimal | xs:integer return 5"
        + " default return 6"
        + ")", "1\n2\n3\n4\n5");
    query("(0, xs:byte(0)) ! "
        + "(typeswitch (.)"
        + " case xs:integer return 1"
        + " case xs:byte return 2"
        + " default return 3"
        + ")", "1\n1");
  }

  /** instance of. */
  @Test public void instanceOf() {
    query("1 instance of item()", true);
    query("1 instance of xs:anyAtomicType", true);
    query("1 instance of xs:decimal", true);
    query("1 instance of xs:integer", true);
    query("1 instance of xs:string", false);
    query("1 instance of xs:untypedAtomic", false);
    query("{1: 1, 'a': 2} instance of map((xs:integer|xs:string), item())", true);
    query("{1: 1} instance of map((xs:unsignedByte|xs:byte), item())", false);
    query("{ 'a' cast as enum('a'): 1 } instance of map(enum('a'), item())", true);
    query("{ 'b' cast as enum('b'): 1 } instance of map(enum('a'), item())", false);
    query("fn($a as (enum('a')|enum('b'))) as item()* { $a } "
        + "instance of fn(enum('a', 'b')) as item()*", true);
    query("fn() as record(a as xs:integer) { { 'a': 42 } } instance of "
        + "fn() as record(a as xs:integer?)", true);
    query("fn() as record(a as xs:integer?) { {} } instance of "
        + "fn() as record(a as xs:integer)", false);
    query("type-of(current-dateTime())", "xs:dateTimeStamp");
    query("type-of(current-dateTime() cast as xs:dateTime)", "xs:dateTime");
    query("42 instance of xs:error", false);
    query("() instance of xs:error?", true);

    error("1 instance of xs:abcde", TYPEUNKNOWN_X);
    error("1 instance of xs:string()", WHICHTYPE_X);
    error("1 instance of item", TYPEUNKNOWN_X);
  }

  /** Subtyping. */
  @Test public void subtyping() {
    query("declare namespace p1='p1'; declare namespace p2='p2'; "
        + "declare variable $x external := ''; function() as element(p1:a)? { $x } "
        + "instance of function() as element(p1:*)?", true);
    query("declare namespace p1='p1'; declare namespace p2='p2'; "
        + "declare variable $x external := ''; function() as element(p1:*|p2:*)? { $x } "
        + "instance of function() as element(p1:*)?", false);
    query("declare variable $x external := ''; function() as "
        + "element(a|b)? { $x } instance of function() as element(*:a|*:b)?", true);
    query("declare variable $x external := ''; function() as "
        + "element(a|b|c)? { $x } instance of function() as (element(a)|element(b))?", false);
    query("fn($x as xs:int) as xs:int {'x'} instance of fn((xs:error | xs:int)) as xs:int", true);
    query("fn($x as (xs:error | xs:int)) as xs:int {'x'} instance of fn(xs:int) as xs:int", true);
    query("fn($x as xs:int) as xs:int {'x'} instance of fn(xs:int) as (xs:error | xs:int)", true);
    query("fn($x as xs:int) as (xs:error | xs:int) {'x'} instance of fn(xs:int) as xs:int", true);
    query("fn() as xs:string? { 'x' } instance of fn() as xs:string", false);
    query("fn() as xs:string* { 'x' } instance of fn() as xs:string", false);
    query("fn() as xs:anyAtomicType? { 'x' } instance of fn() as xs:string", false);
    query("fn() as xs:string { 'x' } instance of fn() as xs:string", true);
    query("fn() as xs:string { 'x' } instance of fn() as xs:string?", true);
    query("fn() as xs:string { 'x' } instance of fn() as xs:anyAtomicType", true);
    query("let $g as function(xs:anyAtomicType) as xs:anyAtomicType := "
        + "fn($x as xs:integer) as xs:string { 'x' } "
        + "return ($g, $g)[1] instance of function(xs:integer) as xs:string", false);
    query("for $c in (1 to 1) "
        + "let $g as function(xs:anyAtomicType) as xs:anyAtomicType := "
        + "fn($x as xs:integer) as xs:string { concat('x', $c) } "
        + "return $g instance of function(xs:integer) as xs:string", false);
    query("(fn($f as function(xs:anyAtomicType) as xs:anyAtomicType) { ($f, $f)[1] })"
        + "(fn($x as xs:integer) as xs:string { 'x' }) "
        + "instance of function(xs:integer) as xs:string", false);
    query("let $f := (fn() as item() { 'a' }, fn() as item() { 1 })[trace(2)] "
        + "return $f() instance of xs:string", false);
    query("fn() { 'x' } instance of function() as item()*", true);
    query("fn() { 'x' } instance of function() as xs:string", false);
  }
}
