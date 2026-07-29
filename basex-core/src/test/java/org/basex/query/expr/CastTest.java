package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * Tests for cast and castable expressions with sequence, item, array, map, and record targets.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CastTest extends SandboxTest {
  /** Casts to atomic types with an occurrence indicator. */
  @Test public void sequences() {
    query("() cast as xs:integer?", "");
    query("() cast as xs:integer*", "");
    query("1 cast as xs:integer", 1);
    query("'1' cast as xs:integer+", 1);
    query("string-join(('1', '2', '3') cast as xs:integer*, ',')", "1,2,3");
    query("string-join(('1', '2') cast as xs:integer+, ',')", "1,2");
    query("count((1 to 5) cast as xs:string*)", 5);
    query("sum(('1', '2', '3') cast as xs:integer+)", 6);

    // the occurrence indicator constrains the result
    error("() cast as xs:integer", INVTYPE_X);
    error("() cast as xs:integer+", INVTYPE_X);
    error("(1, 2) cast as xs:integer", INVTYPE_X);
    error("(1, 2) cast as xs:integer?", INVTYPE_X);

    // failing conversions remain dynamic errors
    error("'x' cast as xs:integer*", FUNCCAST_X_X);
    error("('1', 'x') cast as xs:integer*", FUNCCAST_X_X);
  }

  /** Casts to item(). */
  @Test public void items() {
    query("<x/> cast as item()", "<x/>");
    query("[ 1 ] cast as item()", "[1]");
    query("{ 'a': 1 } cast as item()", "{\"a\":1}");
    query("(true#0 cast as item()) instance of fn(*)", true);
    query("() cast as item()?", "");
    query("count((1, 2, 3) cast as item()*)", 3);

    // item() only checks the cardinality
    error("() cast as item()", INVTYPE_X);
    error("(1, 2) cast as item()", INVTYPE_X);
    error("(1, 2) cast as item()?", INVTYPE_X);
  }

  /** Casts to array types. */
  @Test public void arrays() {
    query("[ '1', '2' ] cast as array(xs:integer)", "[1,2]");
    query("[[ 1 ], [ 2 ]] cast as array(xs:integer)", "[1,2]");
    query("[] cast as array(xs:integer)", "[]");
    query("[ (1, 2) ] cast as array(xs:string*)", "[(\"1\",\"2\")]");
    query("count(([ 1 ], [ 2 ]) cast as array(xs:integer)*)", 2);

    // the input is coerced to array(*)*
    error("1 cast as array(xs:integer)", INVTYPE_X);
    error("([ 1 ], [ 2 ]) cast as array(xs:integer)", INVTYPE_X);
    error("[ 'x' ] cast as array(xs:integer)", FUNCCAST_X_X);
  }

  /** Casts to map types. */
  @Test public void maps() {
    query("{ '1': 'a' } cast as map(xs:integer, xs:string)", "{1:\"a\"}");
    query("{ '1': 'a', '2': 'b' } cast as map(xs:integer, xs:string)", "{1:\"a\",2:\"b\"}");
    query("{} cast as map(xs:integer, xs:string)", "{}");
    query("{ 'a': ('1', '2') } cast as map(xs:string, xs:integer+)", "{\"a\":(1,2)}");

    // casting the keys must not produce duplicates
    error("{ '1': 'a', '01': 'b' } cast as map(xs:integer, xs:string)", MAPDUPLKEY_X);
    // the input is coerced to map(*)*
    error("1 cast as map(xs:integer, xs:string)", INVTYPE_X);
    error("{ 'x': 'a' } cast as map(xs:integer, xs:string)", FUNCCAST_X_X);
  }

  /** Casts to record types: missing fields default to (), undeclared entries are discarded. */
  @Test public void records() {
    query("{ 'a': '1' } cast as record(a as xs:integer)", "{\"a\":1}");
    // undeclared entries are discarded
    query("{ 'a': 1, 'b': 2 } cast as record(a as xs:integer)", "{\"a\":1}");
    query("{ 'a': 1 } cast as record()", "{}");
    // missing fields default to the empty sequence
    query("{} cast as record(a as xs:integer?)", "{\"a\":()}");
    query("{ 'a': 1 } cast as record(a as xs:integer, b as xs:integer?)", "{\"a\":1,\"b\":()}");
    // a missing field whose type is not emptiable is a type error
    error("{} cast as record(a as xs:integer)", INVCONVERT_X_X);
    error("{ 'a': 1 } cast as record(a as xs:integer, b as xs:integer)", INVCONVERT_X_X);

    // named record types (the use case of issue #2644)
    query("declare record local:point(x as xs:double, y as xs:double);\n"
        + "{ 'x': '1', 'y': '2' } cast as local:point", "{\"x\":1,\"y\":2}");
    // a map that coercion would reject can be cast
    query("declare record local:point(x as xs:double, y as xs:double);\n"
        + "({ 'x': 1, 'y': 2, 'z': 3 } cast as local:point) instance of local:point", true);
    error("declare record local:point(x as xs:double, y as xs:double);\n"
        + "fn($p as local:point) { $p }({ 'x': 1, 'y': 2, 'z': 3 })", INVTYPE_X);

    // the input is coerced to map(*)*
    error("1 cast as record(a)", INVTYPE_X);
  }

  /** Casts to list types: the occurrence indicator does not constrain the result. */
  @Test public void lists() {
    query("string-join('a b' cast as xs:IDREFS, ',')", "a,b");
    // a single input item may produce several items, regardless of the occurrence indicator
    query("count('a b c' cast as xs:IDREFS)", 3);
    query("count('a b c' cast as xs:IDREFS?)", 3);
    query("count(('a b', 'c d') cast as xs:IDREFS)", 4);
    query("string-join('a b' cast as xs:NMTOKENS, ',')", "a,b");

    // a list type is only permitted as the direct target of a cast: the item type of a sequence
    // type is an atomic or union type. List types can therefore never be nested, which is why the
    // occurrence indicator needs to be waived for the cast expression only.
    error("['a b'] cast as array(xs:IDREFS)", TYPEUNKNOWN_X);
    error("{ 'k': 'a b' } cast as map(xs:string, xs:IDREFS)", TYPEUNKNOWN_X);
    error("{ 'ids': 'a b' } cast as record(ids as xs:IDREFS)", TYPEUNKNOWN_X);
    error("'a b' instance of xs:IDREFS", TYPEUNKNOWN_X);
  }

  /** Casts to the wildcard types array(*), map(*) and record(*). */
  @Test public void wildcards() {
    query("[ 1, 'a' ] cast as array(*)", "[1,\"a\"]");
    query("{ 'a': 1 } cast as map(*)", "{\"a\":1}");
    // record(*) is abstract: only a record can be cast to it
    query("let $r as record(a) := { 'a': 1 } return $r cast as record(*)", "{\"a\":1}");
    error("{ 'a': 1 } cast as record(*)", INVTYPE_X);
    query("count(([ 1 ], [ 2 ]) cast as array(*)*)", 2);

    query("([ 1 ] cast as array(*)) instance of array(*)", true);
    query("({ 'a': 1 } cast as map(*)) instance of map(*)", true);

    error("1 cast as array(*)", INVTYPE_X);
    error("1 cast as map(*)", INVTYPE_X);
  }

  /** Recursive composition of the cast rules. */
  @Test public void composition() {
    query("[ { 'x': ('1', '2') } ] cast as array(record(x as xs:integer+))", "[{\"x\":(1,2)}]");
    query("{ 'a': [ { 'b': '1' } ] } cast as record(a as array(record(b as xs:integer)))",
        "{\"a\":[{\"b\":1}]}");
    query("{ 'k': [ '1' ] } cast as map(xs:string, array(xs:integer))", "{\"k\":[1]}");
    query("[[ '1' ]] cast as array(array(xs:integer))", "[[1]]");
    // errors propagate out of nested casts
    error("[ { 'x': 'y' } ] cast as array(record(x as xs:integer))", FUNCCAST_X_X);
  }

  /** The result of a successful cast is an instance of the target type. */
  @Test public void resultType() {
    query("(('1', '2') cast as xs:integer*) instance of xs:integer*", true);
    query("('1' cast as xs:integer) instance of xs:integer", true);
    query("(['1'] cast as array(xs:integer)) instance of array(xs:integer)", true);
    query("({ '1': 'a' } cast as map(xs:integer, xs:string)) "
        + "instance of map(xs:integer, xs:string)", true);
    query("({ 'a': '1' } cast as record(a as xs:integer)) "
        + "instance of record(a as xs:integer)", true);
    query("({ 'a': 1, 'b': 2 } cast as record(a as xs:integer)) "
        + "instance of record(a as xs:integer)", true);
    query("([ { 'b': '1' } ] cast as array(record(b as xs:integer)))"
        + " instance of array(record(b as xs:integer))", true);
    // a cast never returns a subtype of the target type
    query("(1 cast as xs:decimal) instance of xs:integer", false);
  }

  /** Castable mirrors cast, but reports failures as false. */
  @Test public void castables() {
    query("1 castable as xs:integer+", true);
    query("(1, 2) castable as xs:integer+", true);
    query("(1, 2) castable as xs:integer", false);
    query("() castable as xs:integer*", true);
    query("('1', 'x') castable as xs:integer*", false);

    query("[ '1' ] castable as array(xs:integer)", true);
    query("[ 'x' ] castable as array(xs:integer)", false);
    query("1 castable as array(xs:integer)", false);

    query("{ 'a': 1 } castable as record(a as xs:integer)", true);
    // undeclared entries are discarded, so the cast succeeds
    query("{ 'a': 1, 'b': 2 } castable as record(a as xs:integer)", true);
    query("{} castable as record(a as xs:integer)", false);

    // a dynamic error inside the cast yields false, it is not propagated
    query("{ '1': 'a', '01': 'b' } castable as map(xs:integer, xs:string)", false);
    // an error while atomizing the operand yields false
    query("true#0 castable as xs:string", false);
    query("[ 1, 2 ] castable as xs:integer", false);

    // an error while evaluating the operand is propagated
    error("(1 div 0) castable as xs:integer", DIVZERO_X);
  }

  /** Atomization of the operand (only for atomic, list, union and enumeration targets). */
  @Test public void atomization() {
    // nodes are atomized
    query("<x>1</x> cast as xs:integer", 1);
    query("string-join((<a>1</a>, <b>2</b>) cast as xs:integer*, ',')", "1,2");
    query("string-join(<x>a b</x> cast as xs:IDREFS, ',')", "a,b");
    query("(<x>1</x>/@*, <y a='2'/>/@a) cast as xs:integer", 2);

    // arrays are atomized, i.e. flattened into their members
    query("string-join([ 1, 2 ] cast as xs:integer*, ',')", "1,2");
    query("string-join([[ 1 ], [ 2 ]] cast as xs:integer*, ',')", "1,2");
    query("[] cast as xs:integer*", "");
    query("[ 1 ] cast as xs:integer", 1);

    // maps and functions cannot be atomized
    error("{ 'a': 1 } cast as xs:integer", FIATOMIZE_X);
    error("true#0 cast as xs:string", FIATOMIZE_X);
    // ...but item() does not atomize, so it accepts them
    query("({ 'a': 1 } cast as item()) instance of map(*)", true);
    query("([ 1, 2 ] cast as item()) instance of array(*)", true);
  }

  /** The empty sequence as operand. */
  @Test public void emptySequence() {
    query("count(() cast as xs:integer*)", 0);
    query("count(() cast as item()*)", 0);
    query("count(() cast as array(xs:integer)*)", 0);
    query("count(() cast as array(xs:integer)?)", 0);
    query("count(() cast as map(xs:string, xs:integer)*)", 0);
    query("count(() cast as record(a)?)", 0);
    query("count(() cast as record(a)*)", 0);

    query("() castable as record(a)?", true);
    query("() castable as array(xs:integer)", false);
    error("() cast as array(xs:integer)", INVTYPE_X);
    error("() cast as record(a)", INVTYPE_X);
    error("() cast as map(xs:string, xs:integer)", INVTYPE_X);
  }

  /** Edge cases of array casts. */
  @Test public void arrayEdges() {
    query("[] cast as array(xs:integer)", "[]");
    query("[ () ] cast as array(xs:integer*)", "[()]");
    query("[[ 1, 2 ]] cast as array(xs:integer*)", "[(1,2)]");
    query("[ 1, 2 ] cast as array(xs:integer+)", "[1,2]");

    // a member is cast to the member type: its cardinality must match
    error("[ () ] cast as array(xs:integer)", INVTYPE_X);
    // the members of [[1, 2]] atomize to two items, which do not fit into a single-item member type
    error("[[ 1, 2 ]] cast as array(xs:integer)", INVTYPE_X);
    // ...whereas the members of [[1], [2]] atomize to one item each (the example of the PR)
    query("[[ 1 ], [ 2 ], [ 3 ]] cast as array(xs:integer)", "[1,2,3]");
  }

  /** Edge cases of map casts. */
  @Test public void mapEdges() {
    query("{} cast as map(xs:integer, xs:string)", "{}");
    query("{ 1: 'a' } cast as map(xs:string, xs:string)", "{\"1\":\"a\"}");
    query("{ xs:untypedAtomic('1'): 'a' } cast as map(xs:integer, xs:string)", "{1:\"a\"}");
    // the insertion order is preserved
    query("{ '2': 'b', '1': 'a' } cast as map(xs:integer, xs:string)", "{2:\"b\",1:\"a\"}");
    // an empty value is allowed if the value type is emptiable
    query("{ 'a': () } cast as map(xs:string, xs:integer*)", "{\"a\":()}");
    error("{ 'a': () } cast as map(xs:string, xs:integer)", INVTYPE_X);

    // keys of different types can collapse into duplicates
    error("{ 1: 'a', '1': 'b' } cast as map(xs:string, xs:string)", MAPDUPLKEY_X);
  }

  /** Edge cases of record casts. */
  @Test public void recordEdges() {
    // entries with non-string keys are discarded, like any other undeclared entry
    query("{ 1: 'a', 'b': 2 } cast as record(b as xs:integer)", "{\"b\":2}");
    // the field order of the result follows the record type, not the input map
    query("{ 'b': 2, 'a': 1 } "
        + "cast as record(a as xs:integer, b as xs:integer)", "{\"a\":1,\"b\":2}");
    // the empty record type discards all entries
    query("{ 'a': 1, 'b': 2 } cast as record()", "{}");

    // record-to-record projection (a record is a map)
    query("declare record local:xyz(x, y, z);\n"
        + "declare record local:xy(x, y);\n"
        + "(local:xyz(1, 2, 3) cast as local:xy) instance of local:xy", true);

    // recursive record types
    query("declare record local:n(v as xs:integer, next as local:n?);\n"
        + "({ 'v': '1', 'next': { 'v': '2' } } cast as local:n)?next?v", 2);

    // sequence-valued fields
    query("{ 'a': ('1', '2') } cast as record(a as xs:integer+)", "{\"a\":(1,2)}");
    error("{ 'a': ('1', '2') } cast as record(a as xs:integer)", INVTYPE_X);

    // a field initializer is ignored by a cast: a missing field yields (), not the initializer
    query("declare record local:r(a as xs:integer := 5); local:r()?a", 5);
    error("declare record local:r(a as xs:integer := 5); {} cast as local:r", INVCONVERT_X_X);
    query("declare record local:r(a as xs:integer := 5); ({ 'a': '1' } cast as local:r)?a", 1);
  }

  /** Enumeration and choice item types. */
  @Test public void enumsAndChoices() {
    query("string-join(('a', 'b') cast as enum('a', 'b')*, ',')", "a,b");
    query("('a', 'b') castable as enum('a', 'b')+", true);
    query("('a', 'c') castable as enum('a', 'b')*", false);
    query("[ 'a' ] cast as array(enum('a', 'b'))", "[\"a\"]");
    query("{ 'k': 'a' } cast as map(xs:string, enum('a', 'b'))", "{\"k\":\"a\"}");

    query("('1', '2') cast as (xs:integer | xs:string)*", "1\n2");
    query("({ 'a': '1' } cast as record(a as (xs:integer | xs:date))) ?a", 1);
    query("1 castable as (xs:integer | xs:date)", true);
  }

  /** Namespace-sensitive types use the static context of the cast expression. */
  @Test public void nsSensitive() {
    query("prefix-from-QName('xs:string' cast as xs:QName)", "xs");
    query("prefix-from-QName(({ 'q': 'xs:string' } cast as record(q as xs:QName))?q)", "xs");
    query("prefix-from-QName((([ 'xs:string' ] cast as array(xs:QName))?1))", "xs");
    // a cast resolves an atomized node against the static context (the 'surprise' the spec warns
    // about); coercion, in contrast, rejects namespace-sensitive conversion from untypedAtomic
    query("prefix-from-QName(({ 'q': <x>xs:string</x> } cast as record(q as xs:QName))?q)", "xs");
    error("fn($r as record(q as xs:QName)) { $r }({ 'q': <x>xs:string</x> })", NSSENS_X_X);
  }

  /** Cast-target eligibility: the top-level type is checked statically, components at runtime. */
  @Test public void eligibility() {
    // top-level target types that are not eligible are static errors
    error("1 cast as xs:anyAtomicType", INVALIDCAST_X);
    error("1 cast as xs:NOTATION", INVALIDCAST_X);
    error("1 cast as function(*)", INVALIDCAST_X);
    error("1 castable as xs:anyAtomicType", INVALIDCAST_X);
    // a named type must resolve to an eligible type
    error("declare type local:f as fn(*); 1 cast as local:f", INVALIDCAST_X);
    error("1 cast as local:unknown", WHICHCAST_X);

    // a component type need not be a valid cast target; a value that already matches is kept as-is
    query("([ <a/> ] cast as array(element())) instance of array(element())", true);
    query("([ true#0 ] cast as array(function(*))) instance of array(function(*))", true);
    query("({ 'k': <a/> } cast as map(xs:string, element())) "
        + "instance of map(xs:string, element())", true);
    query("({ 'a': <e/> } cast as record(a as element())) "
        + "instance of record(a as element())", true);
    query("[ <a/> ] castable as array(element())", true);

    // a component value that neither matches nor can be cast raises a type error
    error("[ 1 ] cast as array(element())", INVCONVERT_X_X);
    error("[ 1 ] cast as array(function(*))", INVCONVERT_X_X);
    error("{ 'k': 1 } cast as map(xs:string, element())", INVCONVERT_X_X);
    error("{ 'a': 1 } cast as record(a as element())", INVCONVERT_X_X);
    query("[ 1 ] castable as array(element())", false);
  }
}
