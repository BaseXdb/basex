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

    error("1 castable as xs:integer+", CALCEXPR);
    error("1 castable as xs:integer()", SIMPLETYPE_X);
    error("1 castable as xml:integer", WHICHCAST_X);
    error("1 castable as integer", WHICHCAST_X);
    error("1 castable as xs:NOTATION", INVALIDCAST_X);
    error("1 castable as xs:anyAtomicType", INVALIDCAST_X);
    error("(42 cast as enum('42')) cast as enum('x')", FUNCCAST_X_X);
    error("42 cast as xs:error?", FUNCCAST_X_X);
    error("xs:error(42)", FUNCCAST_X_X);
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
