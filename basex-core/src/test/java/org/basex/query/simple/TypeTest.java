package org.basex.query.simple;

import org.basex.query.*;

/**
 * XQuery type tests.
 *
 * @author BaseX Team 2005-24, BSD License
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

        { "SimpleErr 1", "1 castable as xs:integer+" },
        { "SimpleErr 2", "1 castable as xs:integer()" },
        { "SimpleErr 3", "1 castable as xml:integer" },
        { "SimpleErr 4", "1 castable as integer" },
        { "SimpleErr 5", "1 castable as xs:NOTATION" },
        { "SimpleErr 6", "1 castable as xs:anyAtomicType" },
        { "SimpleErr 7", "(42 cast as enum('42')) cast as enum('x')" },

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
            "{'a' cast as enum('a'): 1} instance of map(enum('a'), item())"},
        { "Type 10", booleans(false),
            "{'b' cast as enum('b'): 1} instance of map(enum('a'), item())"},
        { "Type 11", booleans(true), "fn($a as (enum('a')|enum('b'))) as item()* {$a} "
            + "instance of fn(enum('a', 'b')) as item()*"
        },
        { "Type 12", booleans(true), "fn() as record(a as xs:integer) {map{'a': 42}} instance of "
            + "fn() as record(a? as xs:integer)"},
        { "Type 13", booleans(false), "fn() as record(a? as xs:integer) {map{}} instance of "
            + "fn() as record(a as xs:integer)"},

        { "TypeErr 1", "1 instance of xs:abcde" },
        { "TypeErr 2", "1 instance of xs:string()" },
        { "TypeErr 3", "1 instance of item" },
    };
  }
}
