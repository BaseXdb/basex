package org.basex.query.simple;

import org.basex.query.*;

/**
 * XQuery type tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TypeTest extends QueryTest {
  static {
    queries = new Object[][] {
        { "Simple 1", booleans(true), "1 castable as xs:integer" },
        { "Simple 2", booleans(true), "1 castable as xs:integer?" },
        { "Simple 3", booleans(true), "() castable as xs:integer?" },
        { "Simple 4", strings("42"), "(42 cast as enum('42')) cast as enum('42', '43')" },
        { "Simple 5", booleans(true), "(42 cast as enum('42')) castable as enum('42', '43')" },
        { "Simple 6", booleans(false), "(42 cast as enum('42')) castable as enum('43')" },
        { "Simple 7", booleans(true), "() castable as xs:error?" },
        { "Simple 8", booleans(false), "42 castable as xs:error?" },
        { "Simple 9", emptySequence(), "() cast as xs:error?" },
        { "Simple 10", emptySequence(), "xs:error(())" },

        { "SimpleErr 1", "1 castable as xs:integer+" },
        { "SimpleErr 2", "1 castable as xs:integer()" },
        { "SimpleErr 3", "1 castable as xml:integer" },
        { "SimpleErr 4", "1 castable as integer" },
        { "SimpleErr 5", "1 castable as xs:NOTATION" },
        { "SimpleErr 6", "1 castable as xs:anyAtomicType" },
        { "SimpleErr 7", "(42 cast as enum('42')) cast as enum('x')" },
        { "SimpleErr 8", "42 cast as xs:error?" },
        { "SimpleErr 9", "xs:error(42)" },

        { "Type 1", booleans(true), "1 instance of item()" },
        { "Type 2", booleans(true), "1 instance of xs:anyAtomicType" },
        { "Type 3", booleans(true), "1 instance of xs:decimal" },
        { "Type 4", booleans(true), "1 instance of xs:integer" },
        { "Type 5", booleans(false), "1 instance of xs:string" },
        { "Type 6", booleans(false), "1 instance of xs:untypedAtomic" },
        { "Type 7", booleans(true),
            "{1: 1, 'a': 2} instance of map((xs:integer|xs:string), item())"},
        { "Type 8", booleans(false), "{1: 1} instance of map((xs:unsignedByte|xs:byte), item())"},
        { "Type 9", booleans(true),
            "{ 'a' cast as enum('a'): 1 } instance of map(enum('a'), item())"},
        { "Type 10", booleans(false),
            "{ 'b' cast as enum('b'): 1 } instance of map(enum('a'), item())"},
        { "Type 11", booleans(true), "fn($a as (enum('a')|enum('b'))) as item()* { $a } "
            + "instance of fn(enum('a', 'b')) as item()*"
        },
        { "Type 12", booleans(true), "fn() as record(a as xs:integer) { { 'a': 42 } } instance of "
            + "fn() as record(a as xs:integer?)"},
        { "Type 13", booleans(false), "fn() as record(a as xs:integer?) { {} } instance of "
            + "fn() as record(a as xs:integer)"},
        { "Type 14", strings("xs:dateTimeStamp"), "type-of(current-dateTime())" },
        { "Type 15", strings("xs:dateTime"), "type-of(current-dateTime() cast as xs:dateTime)" },
        { "Type 16", booleans(false), "42 instance of xs:error" },
        { "Type 16", booleans(true), "() instance of xs:error?" },

        { "TypeErr 1", "1 instance of xs:abcde" },
        { "TypeErr 2", "1 instance of xs:string()" },
        { "TypeErr 3", "1 instance of item" },

        { "Subtyping 1", booleans(true), "declare namespace p1='p1'; declare namespace p2='p2'; "
            + "declare variable $x external := ''; function() as element(p1:a)? { $x } "
            + "instance of function() as element(p1:*)?" },
        { "Subtyping 2", booleans(false), "declare namespace p1='p1'; declare namespace p2='p2'; "
            + "declare variable $x external := ''; function() as element(p1:*|p2:*)? { $x } "
            + "instance of function() as element(p1:*)?" },
        { "Subtyping 3", booleans(true), "declare variable $x external := ''; function() as "
            + "element(a|b)? { $x } instance of function() as element(*:a|*:b)?" },
        { "Subtyping 4", booleans(false), "declare variable $x external := ''; function() as "
            + "element(a|b|c)? { $x } instance of function() as (element(a)|element(b))?" },
        { "Subtyping 5", booleans(true),
            "fn($x as xs:int) as xs:int {'x'} instance of fn((xs:error | xs:int)) as xs:int"},
        { "Subtyping 6", booleans(true),
            "fn($x as (xs:error | xs:int)) as xs:int {'x'} instance of fn(xs:int) as xs:int"},
        { "Subtyping 7", booleans(true),
            "fn($x as xs:int) as xs:int {'x'} instance of fn(xs:int) as (xs:error | xs:int)"},
        { "Subtyping 8", booleans(true),
            "fn($x as xs:int) as (xs:error | xs:int) {'x'} instance of fn(xs:int) as xs:int"},
        { "Subtyping 9", booleans(false),
            "fn() as xs:string? { 'x' } instance of fn() as xs:string"},
        { "Subtyping 10", booleans(false),
            "fn() as xs:string* { 'x' } instance of fn() as xs:string"},
        { "Subtyping 11", booleans(false),
            "fn() as xs:anyAtomicType? { 'x' } instance of fn() as xs:string"},
        { "Subtyping 12", booleans(true),
            "fn() as xs:string { 'x' } instance of fn() as xs:string"},
        { "Subtyping 13", booleans(true),
            "fn() as xs:string { 'x' } instance of fn() as xs:string?"},
        { "Subtyping 14", booleans(true),
            "fn() as xs:string { 'x' } instance of fn() as xs:anyAtomicType"},
        { "Subtyping 15", booleans(false),
            "let $g as function(xs:anyAtomicType) as xs:anyAtomicType := "
            + "fn($x as xs:integer) as xs:string { 'x' } "
            + "return ($g, $g)[1] instance of function(xs:integer) as xs:string"},
        { "Subtyping 16", booleans(false),
            "for $c in (1 to 1) "
            + "let $g as function(xs:anyAtomicType) as xs:anyAtomicType := "
            + "fn($x as xs:integer) as xs:string { concat('x', $c) } "
            + "return $g instance of function(xs:integer) as xs:string"},
        { "Subtyping 17", booleans(false),
            "(fn($f as function(xs:anyAtomicType) as xs:anyAtomicType) { ($f, $f)[1] })"
            + "(fn($x as xs:integer) as xs:string { 'x' }) "
            + "instance of function(xs:integer) as xs:string"},
        { "Subtyping 18", booleans(false),
            "let $f := (fn() as item() { 'a' }, fn() as item() { 1 })[trace(2)] "
            + "return $f() instance of xs:string"},
        { "Subtyping 19", booleans(true), "fn() { 'x' } instance of function() as item()*"},
        { "Subtyping 20", booleans(false), "fn() { 'x' } instance of function() as xs:string"},
    };
  }
}
