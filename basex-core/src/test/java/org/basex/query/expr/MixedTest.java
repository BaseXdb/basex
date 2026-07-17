package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.prof.ProfType.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Mixed XQuery tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MixedTest extends SandboxTest {
  /** Test XQuery module file. */
  private static final String XQMFILE = "src/test/resources/hello.xqm";

  /**
   * Drops the collection.
   */
  @AfterAll public static void after() {
    execute(new DropDB(NAME));
    execute(new DropDB(NAME + '2'));
  }

  /** Catches duplicate module import. */
  @Test public void duplImport() {
    error("import module namespace a='world' at '" + XQMFILE + "';" +
      "import module namespace a='world' at '" + XQMFILE + "'; 1",
      DUPLMODULE_X);
  }

  /** Catches duplicate module import with different module URI. */
  @Test public void duplImportDiffUri() {
    error("import module namespace a='world' at '" + XQMFILE + "';" +
      "import module namespace a='galaxy' at '" + XQMFILE + "'; 1",
      DUPLNSDECL_X);
  }

  /** Catches duplicate module import. */
  @Test public void duplLocation() {
    error("import module namespace a='world' at '" + XQMFILE + "';" +
      "import module namespace b='galaxy' at '" + XQMFILE + "'; 1",
      WRONGMODULE_X_X_X);
  }

  /** Checks static context scoping in variables. */
  @Test public void varsInModules() {
    contains("import module namespace a='world' at '" + XQMFILE + "'; $a:eager", "hello:foo");
    contains("import module namespace a='world' at '" + XQMFILE + "'; $a:lazy", "hello:foo");
    contains("import module namespace a='world' at '" + XQMFILE + "'; $a:func()", "hello:foo");
    contains("import module namespace a='world' at '" + XQMFILE + "'; a:inlined()", "hello:foo");
  }

  /** Checks imported module's types. */
  @Test public void typesInModules() {
    query("import module namespace a='world' at '" + XQMFILE + "'; '42' cast as a:int", 42);
    query("import module namespace a='world' at '" + XQMFILE + "';" +
      "declare type a:private-int as a:int; '42' cast as a:private-int", 42);

    error("import module namespace a='world' at '" + XQMFILE + "';" +
      "declare type Q{world}int as xs:double; '42' cast as a:int", DUPLTYPE_X);
    error("import module namespace a='world' at '" + XQMFILE + "'; '42' cast as a:private-int",
      WHICHCAST_X);
  }

  /** Constructor functions for declared item types. */
  @Test public void typeConstructors() {
    // constructor functions for atomic and enumeration types
    query("declare type local:int as xs:integer; local:int('42')", 42);
    query("declare type local:int as xs:integer; local:int('42') instance of local:int", true);
    query("declare type local:e as enum('a', 'b'); local:e('a')", "a");
    query("declare type local:int as xs:integer; ['1'] ! local:int()", 1);
    query("declare type local:int as xs:integer; local:int(value := '7')", 7);
    query("declare type local:int as xs:integer; local:int(())", "");
    query("declare type local:int as xs:integer; function-lookup(xs:QName('local:int'), 1)('9')",
        9);
    // forward references, aliases of aliases
    query("declare function local:f() { local:b('42') };"
        + "declare type local:b as local:a; declare type local:a as xs:integer; local:f()", 42);
    // types of imported modules
    query("import module namespace a='world' at '" + XQMFILE + "'; a:int('42')", 42);
    query("import module namespace a='world' at '" + XQMFILE + "';"
        + "declare type local:t as a:int; local:t('42')", 42);
    error("import module namespace a='world' at '" + XQMFILE + "'; a:private-int('42')",
        FUNCPRIVATE_X);

    // constructor functions for union types
    query("declare type local:u as (xs:date | xs:time); local:u('12:00:00') instance of xs:time",
        true);
    query("declare type local:n as xs:numeric; local:n('1') instance of xs:double", true);
    // constructor functions for record types
    query("declare type local:r as record(x as xs:integer, y as xs:string); local:r(1, 'a')?y",
        "a");
    query("declare type local:r as record(x as xs:integer); local:r(2) instance of local:r",
        true);
    // no constructor functions for other item types
    error("declare type local:m as map(*); local:m({})", WHICHFUNC_X);
    error("declare type local:e as element(); local:e(<x/>)", WHICHFUNC_X);
    // failing constructions
    error("declare type local:int as xs:integer; local:int('x')", FUNCCAST_X_X);
    error("declare type local:e as enum('a', 'b'); local:e('c')", FUNCCAST_X_X);
    // arity overlap with user-defined function
    error("declare type local:int as xs:integer;"
        + "declare function local:int($value) { $value }; 1", DUPLFUNC_X);
  }

  /**
   * Overwrites an empty attribute value.
   */
  @Test public void emptyAttValues() {
    execute(new CreateDB(NAME, "<A a='' b=''/>"));
    query("replace value of node /A/@a with 'A'");
    query("/", "<A a=\"A\" b=\"\"/>");
  }

  /**
   * Parse recursive queries.
   */
  @Test public void parseRec() {
    // simple call
    query("declare function local:x() { if(<a/>) then 1 else local:x() }; local:x()");
    // call from FLWOR expression
    query("declare function local:x() { if(<a/>) then 1 else local:x() }; " +
        "let $x := local:x() return $x", 1);
  }

  /**
   * Performs count() on parts of a collection.
   */
  @Test public void countCollection() {
    execute(new CreateDB(NAME));
    execute(new Add("a", "<a/>"));
    execute(new Add("b", "<a/>"));
    execute(new Optimize());
    query(COUNT.args(_DB_GET.args(NAME, "a") + "/a"), 1);
    query(COUNT.args(_DB_GET.args(NAME) + "/a"), 2);
  }

  /**
   * Tests constant-propagating variables that were introduced by inlining.
   */
  @Test public void gh907() {
    execute(new CreateDB(NAME, "<x/>"));
    query("declare function local:a($a) { contains($a, 'a') }; //x[local:a(.)]", "");
  }

  /** Ancestor axis of DBNodes wrapped in FNodes. */
  @Test public void gh919() {
    query("<a><b/></a>/b ! name(..)", "a");

    query("<a>{ <b/> }</a>/b ! name(..)", "a");
    query("<a>{ <b/> update {} }</a>/b ! name(..)", "a");
    query("(<a><b/></a> update {})/b ! name(..)", "a");

    query("<a>{ <b><c/></b> }</a>/b ! name(..)", "a");
    query("<a>{ <b><c/></b> update {} }</a>/b ! name(..)", "a");
    query("(<a><b><c/></b></a> update {})/b ! name(..)", "a");

    query("<a>{ <b><c/></b>/c }</a>/c ! name(..)", "a");
    query("<a>{ (<b><c/></b> update {})/c }</a>/c ! name(..)", "a");
    query("(<a><b><c/></b></a> update {})/b/c ! name(..)", "b");
    query("(<a><b><c/></b></a> update {})/b/c/.. ! name(..)", "a");

    query("<a><b/></a> ! <x>{ . }</x>/a/b/ancestor::node() ! name()", "x\na");
    query("(<a><b/></a> update {}) ! <x>{ . }</x>/a/b/ancestor::node() ! name()", "x\na");

    query("<A>{ <a><b/><c/></a>/* }</A>/b/following-sibling::c", "<c/>");
    query("<A>{ (<a><b/><c/></a> update {})/* }</A>/b/following-sibling::c", "<c/>");

    query("<A>{ document { <a><b/><c/></a> }/a/* }</A>/b/following-sibling::c", "<c/>");
    query("<A>{ (document { <a><b/><c/></a> } update {})/a/* }</A>/b/following-sibling::c", "<c/>");

    query("<A>{ <a><b/><c/></a>/* }</A>/c/preceding-sibling::b", "<b/>");
    query("<A>{ (<a><b/><c/></a> update {})/* }</A>/c/preceding-sibling::b", "<b/>");

    query("<A>{ document { <a><b/><c/></a> }/a/* }</A>/c/preceding-sibling::b", "<b/>");
    query("<A>{ (document { <a><b/><c/></a> } update {})/a/* }</A>/c/preceding-sibling::b", "<b/>");

    error("let $doc := document { <a><b/></a> } update {}"
        + "return id('id', element c { $doc/*/node() }/*)", IDDOC);
  }

  /** Type intersections. */
  @Test public void gh1427() {
    query("let $a := function($f) as element(*) { $f() }"
        + "let $b := $a(function() as element(xml)* { <xml/> })"
        + "return $b", "<xml/>");
    query("let $a := function($f) as element(xml)* { $f() }"
        + "let $b := $a(function() as element(*) { <xml/> })"
        + "return $b", "<xml/>");
    query("let $a := function($f) as element(xml) { $f() }"
        + "let $b := $a(function() as element() { <xml/> })"
        + "return $b", "<xml/>");
    query("let $a := function($f) as element(xml) { $f() }"
        + "let $b := $a(function() as element(*)* { <xml/> })"
        + "return $b", "<xml/>");
  }

  /** Optimizations of nested path expressions. */
  @Test public void gh1567() {
    query("let $_ := '_' return document { <_/> }/*[self::*[name() = $_]]", "<_/>");
    query("let $_ := '_' return document { <_/> }/*[self::*/name() = $_]", "<_/>");
    query("let $_ := '_' return document { <_/> }/*[name(self::*) = $_]", "<_/>");
    query("let $_ := '_' return document { <_/> }/*[name() = $_]", "<_/>");
    query("document { <_/> }/*[self::*[name() = '_']]", "<_/>");
    query("document { <_/> }/*[self::*/name() = '_']", "<_/>");
    query("document { <_/> }/*[name(self::*) = '_']", "<_/>");
    query("document { <_/> }/*[name() = '_']", "<_/>");
  }

  /** Optimizations of nested path expressions. */
  @Test public void gh1587() {
    execute(new CreateDB(NAME, "<a id='0'><b id='1'/></a>"));
    final String query = "let $id := '0' return " + _DB_ATTRIBUTE.args(NAME, " '1'") + "/..";
    query(query, "<b id=\"1\"/>");
    query(query + "[../@id = $id]", "<b id=\"1\"/>");
    query(query + "[..[@id = $id]]", "<b id=\"1\"/>");
    query(query + "[..[@id = $id]/parent::document-node()]", "<b id=\"1\"/>");
  }

  /** Node IDs. */
  @Test public void gh1566() {
    query("((<_><a/><b/></_> update {})/* ! element _ { . })/*", "<a/>\n<b/>");
    query("((<_><a>A</a><b>B</b></_> update {})/* ! element _ { . })/*/node()", "A\nB");
  }

  /**
   * Tests document order across multiple documents or databases.
   * @throws IOException I/O exception
   */
  @Test public void diffDatabases() throws IOException {
    final String xml1 = "<xml><n1a/><n1b/></xml>";
    final String xml2 = "<xml><n2a/><n2b/></xml>";
    final IOFile file1 = new IOFile(sandbox(), "doc1.xml");
    final IOFile file2 = new IOFile(sandbox(), "doc2.xml");
    file1.write(xml1);
    file2.write(xml2);

    // compare order of multiple document (based on original path)
    query("doc('" + file1.path() + "')/*/* union doc('" + file2.path() + "')/*/*",
        "<n1a/>\n<n1b/>\n<n2a/>\n<n2b/>");

    // compare order of multiple document (based on database path)
    execute(new CreateDB(NAME, file1.path()));
    execute(new CreateDB(NAME + '2', file2.path()));
    execute(new Close());
    query(_DB_GET.args(NAME) + "/*/* union " + _DB_GET.args(NAME + '2') + "/*/*",
        "<n1a/>\n<n1b/>\n<n2a/>\n<n2b/>");
  }

  /** Node construction, dynamic EQNames with URIs. */
  @Test public void gh1912() {
    query("element { 'a' } {}", "<a/>");
    query("element { ' a ' } {}", "<a/>");
    query("element { ' Q{}a ' } {}", "<a/>");
    query("element { ' Q{ }a ' } {}", "<a/>");
    query("element { ' Q{b}a ' } {}", "<a xmlns=\"b\"/>");
    query("element { ' Q{ b }a ' } {}", "<a xmlns=\"b\"/>");
    query("element { ' xml:a ' } {}", "<xml:a/>");
    query("declare namespace p = 'u'; element { 'p:l' } {}", "<p:l xmlns:p=\"u\"/>");

    query("element Q{ }x {}[namespace-uri() = (' ')]", "");
    query("element { 'Q{ }x' } {}[namespace-uri() = (' ')]", "");

    query("attribute { ' a ' } {}", "a=\"\"");

    error("element { '' } {}", INVQNAME_X);
    error("element { ' ' } {}", INVQNAME_X);
    error("element { 'a b' } {}", INVQNAME_X);
    error("element { 'a:b' } {}", NOQNNAMENS_X);
    error("element { 'a: b' } {}", INVQNAME_X);
    error("element { 'Q{}' } {}", INVQNAME_X);
    error("element { 'Q{ }' } {}", INVQNAME_X);

    error("element { 'Q{ http://www.w3.org/2000/xmlns/ }a' } {}", CEINV_X);
  }

  /** Faster instance of checks. */
  @Test public void gh1939() {
    query("128 instance of xs:integer", true);

    query("<a/>[.  = ''] instance of element(a) ", true);
    query("<a/>[.  = ''] instance of element(a)?", true);
    query("<a/>[.  = ''] instance of element(a)+", true);
    query("<a/>[.  = ''] instance of element(a)*", true);
    query("<a/>[. != ''] instance of element(a) ", false);
    query("<a/>[. != ''] instance of element(a)?", true);
    query("<a/>[. != ''] instance of element(a)+", false);
    query("<a/>[. != ''] instance of element(a)*", true);

    query("('' , <a/>)[.  = ''] instance of empty-sequence()", false);
    query("('a', <a/>)[.  = ''] instance of empty-sequence()", false);
    query("('' , <a/>)[. != ''] instance of empty-sequence()", true);
    query("('a', <a/>)[. != ''] instance of empty-sequence()", false);

    error("error() instance of xs:string*", FUNERR1);

    query("(xs:anyURI('b'), 'a', 'a')[. = 'a'] instance of xs:string ", false);
    query("(xs:anyURI('b'), 'a', 'a')[. = 'a'] instance of xs:string?", false);
    query("(xs:anyURI('b'), 'a', 'a')[. = 'a'] instance of xs:string+", true);
    query("(xs:anyURI('b'), 'a', 'a')[. = 'a'] instance of xs:string*", true);
    query("(xs:anyURI('b'), 'a', 'a')[. = 'b'] instance of xs:string ", false);
    query("(xs:anyURI('b'), 'a', 'a')[. = 'b'] instance of xs:string?", false);
    query("(xs:anyURI('b'), 'a', 'a')[. = 'b'] instance of xs:string+", false);
    query("(xs:anyURI('b'), 'a', 'a')[. = 'b'] instance of xs:string*", false);
    query("(xs:anyURI('b'), 'a', 'a')[. = 'c'] instance of xs:string ", false);
    query("(xs:anyURI('b'), 'a', 'a')[. = 'c'] instance of xs:string?", true);
    query("(xs:anyURI('b'), 'a', 'a')[. = 'c'] instance of xs:string+", false);
    query("(xs:anyURI('b'), 'a', 'a')[. = 'c'] instance of xs:string*", true);
  }

  /** JSON documents, node IDs. */
  @Test public void gh1983() {
    query("tail(" + _JSON_PARSE.args("{}") +  "/*/ancestor-or-self::node()) instance of element()",
        true);
    query("tail(" + _CSV_PARSE.args("{}") +  "/*/ancestor-or-self::node()) instance of element()",
        true);
  }

  /** fn:json-to-xml, namespaces. */
  @Test public void gh1997() {
    execute(new Close());
    query(_DB_CREATE.args(NAME, " analyze-string('a', 'a')/*", NAME));
    query(_DB_GET.args(NAME) + "/* => namespace-uri()", "http://www.w3.org/2005/xpath-functions");
    query(_DB_CREATE.args(NAME, " json-to-xml('[1]')/*/*", NAME));
    query(_DB_GET.args(NAME) + "/* => namespace-uri()", "http://www.w3.org/2005/xpath-functions");
  }

  /** Binary storage: out of bounds. */
  @Test public void gh2100() {
    query(_DB_CREATE.args("x", " <x>A</x>", "x.xml"));
    query(_DB_GET.args("x") + " ! (delete node x, " +
        _DB_PUT_BINARY.args("x", " convert:string-to-base64(string(x))", "pth") + ')');
    query(_DB_GET_BINARY.args("x", "pth"), "A");

    query(_DB_CREATE.args("x", " <x>A</x>", "x.xml"));
    query(_DB_GET.args("x") + " ! (delete node x, " + _DB_ADD.args("x", " x", "pth") + ')');
  }

  /**
   * Distinct document/database references.
   * @throws IOException I/O exception
   */
  @Test public void gh2147() throws IOException {
    final IOFile file = new IOFile(sandbox(), "test.xml");
    file.write("<file/>");
    query(_DB_CREATE.args("test", " <db/>", "test.xml"));

    query("doc('" + file + "'), doc('test')", "<file/>\n<db/>");
    query("doc('test'), doc('" + file + "')", "<db/>\n<file/>");
  }

  /** Arrow tests. */
  @Test public void arrow() {
    query("1 => count()", 1);
    query("() => count()", 0);
    query("() => count() => count()", 1);

    query("(for $i in ('a', 'b') return $i => string-length()) => count()", 2);
    query("let $string := 'a b c' "
        + "let $result := $string=>upper-case()=>normalize-unicode()=>tokenize('\\s+')"
        + "return ($result, count($result))", "A\nB\nC\n3");

    query("1 => (count#1)()", 1);
    query("('ab' => substring(?))(2)", "b");
    query("'ab' => (substring(?, 2))()", "b");
    query("('ab' => (substring(?, ?))(?))(2)", "b");
    query("let $a := count#1 return 1 => $a()", 1);

    error("1 => 1", ARROWSPEC_X);
    error("1 => (1)()", INVFUNCITEM_X_X);
  }

  /** Recursive query. */
  @Test public void recursive() {
    // runtime degrades if no tree sequence builder is used
    query("declare function local:f($x) {"
        + "  if(count($x) < 100000) then local:f((1, $x)) else $x"
        + "};"
        + "count(local:f(()))", 100000);
    query("declare function local:f($x) {"
        + "  if(count($x) < 100000) then local:f(($x, 1)) else $x"
        + "};"
        + "count(local:f(()))", 100000);
    query("declare function local:f($x) {"
        + "  if(count($x) < 100000) then local:f((1, 2, $x)) else $x"
        + "};"
        + "count(local:f(()))", 100000);
    query("declare function local:f($x) {"
        + "  if(count($x) < 100000) then local:f(($x, 2, 1)) else $x"
        + "};"
        + "count(local:f(()))", 100000);
    query("declare function local:f($x) {"
        + "  if(count($x) < 100000) then local:f(insert-before($x, 3, 1)) else $x"
        + "};"
        + "count(local:f(1 to 5))", 100000);
    query("declare function local:f($x) {"
        + "  if(count($x) > 1) then local:f(remove($x, 2)) else $x"
        + "};"
        + "count(local:f(1 to 100000))", 1);
    query("declare function local:f($x) {"
        + "  if(count($x) > 0) then local:f(remove($x, 1)) else $x"
        + "};"
        + "count(local:f(1 to 100000))", 0);
    query("declare function local:f($x) {"
        + "  if(count($x) > 0) then local:f(tail($x)) else $x"
        + "};"
        + "count(local:f(1 to 100000))", 0);
    query("declare function local:f($x) {"
        + "  if(count($x) > 0) then local:f(trunk($x)) else $x"
        + "};"
        + "count(local:f(1 to 100000))", 0);
  }

  /** Recursive array query. */
  @Test public void recursiveArray() {
    // runtime degrades if no tree sequence builder is used
    query("declare function local:f($x) {"
        + "  if(array:size($x) < 100000) then local:f(array:insert-before($x, 1, 1)) else $x"
        + "};"
        + "array:size(local:f(array {}))", 100000);
    query("declare function local:f($x) {"
        + "  if(array:size($x) < 100000) then local:f(array:append($x, 1)) else $x"
        + "};"
        + "array:size(local:f(array {}))", 100000);
    query("declare function local:f($x) {"
        + "  if(array:size($x) < 100000) then local:f(array:insert-before($x, 3, 1)) else $x"
        + "};"
        + "array:size(local:f(array { 1 to 5 }))", 100000);
    query("declare function local:f($x) {"
        + "  if(array:size($x) > 1) then local:f(array:remove($x, 2)) else $x"
        + "};"
        + "array:size(local:f(array { 1 to 100000 }))", 1);
    query("declare function local:f($x) {"
        + "  if(array:size($x) > 0) then local:f(array:remove($x, 1)) else $x"
        + "};"
        + "array:size(local:f(array { 1 to 100000 }))", 0);
    query("declare function local:f($x) {"
        + "  if(array:size($x) > 0) then local:f(array:tail($x)) else $x"
        + "};"
        + "array:size(local:f(array { 1 to 100000 }))", 0);
    query("declare function local:f($x) {"
        + "  if(array:size($x) > 0) then local:f(array:trunk($x)) else $x"
        + "};"
        + "array:size(local:f(array { 1 to 100000 }))", 0);
  }

  /** Sequence tests. */
  @Test public void sequences() {
    // list optimizations
    check("('A', 'B')",
        "A\nB", root(StrSeq.class), type(StrSeq.class, "xs:string+"));
    check("('A', 'B', 'A', 'B', 'A', 'B')",
        "A\nB\nA\nB\nA\nB", root(StrSeq.class), type(StrSeq.class, "xs:string+"));
    check("(1, 3)",
        "1\n3", root(IntSeq.class), type(IntSeq.class, "xs:integer+"));
    check("(1, 3, 1, 3, 1, 3)",
        "1\n3\n1\n3\n1\n3", root(IntSeq.class), type(IntSeq.class, "xs:integer+"));
    check("data((<a>A</a>))",
        "A", root(Atm.class));
    check("data((<a>A</a>, <a>B</a>))",
        "A\nB", root(StrSeq.class), type(StrSeq.class, "xs:untypedAtomic+"));
    check("data((<a>A</a>, <a>B</a>, <a>A</a>, <a>B</a>, <a>A</a>, <a>B</a>))",
        "A\nB\nA\nB\nA\nB", root(StrSeq.class), type(StrSeq.class, "xs:untypedAtomic+"));

    // inspection
    checkType("('A', 'B', 'A', 'B', 'A', 'B')",
        new TypeInfo(StrSeq.class, "xs:string+", 6));
    checkType("(1, 3, 1, 3, 1, 3)",
        new TypeInfo(IntSeq.class, "xs:integer+", 6));
    checkType("data((<a>A</a>, <a>B</a>, <a>A</a>, <a>B</a>, <a>A</a>, <a>B</a>))",
        new TypeInfo(StrSeq.class, "xs:untypedAtomic+", 6));

    // filters
    checkType("('A', 'B', 'A', 'B', 'A', 'B')[not(. = 'C')]",
        new TypeInfo(IterFilter.class, "xs:string*", -1),
        new TypeInfo(StrSeq.class, "xs:string+", 6));
    checkType("data((<a>A</a>, <a>B</a>, <a>A</a>, <a>B</a>, <a>A</a>, <a>B</a>))[not(. = 'C')]",
        new TypeInfo(IterFilter.class, "xs:untypedAtomic*", -1),
        new TypeInfo(StrSeq.class, "xs:untypedAtomic+", 6));
    checkType("(1, 3, 1, 3, 1, 3)[not(. = 2)]",
        new TypeInfo(IterFilter.class, "xs:integer*", -1),
        new TypeInfo(IntSeq.class, "xs:integer+", 6));

    // map operator
    checkType("(1 to 6) ! string()",
        new TypeInfo(DualMap.class, "xs:string+", 6),
        new TypeInfo(StrSeq.class, "xs:string+", 6));
    checkType("(1 to 6) ! xs:untypedAtomic()",
        new TypeInfo(DualMap.class, "xs:untypedAtomic+", 6),
        new TypeInfo(StrSeq.class, "xs:untypedAtomic+", 6));
    checkType("(1 to 6) ! ('0' || .) ! xs:integer()",
        new TypeInfo(DualMap.class, "xs:integer+", 6),
        new TypeInfo(RangeSeq.class, "xs:integer+", 6));
    checkType("(1, 3, 1, 3, 1, 3) ! ('0' || .) ! xs:integer()",
        new TypeInfo(DualMap.class, "xs:integer+", 6),
        new TypeInfo(IntSeq.class, "xs:integer+", 6));
  }

  /** Tests the data structure before and after shrinking. */
  @Test public void shrink() {
    test("('x', 1 to 3) => remove(1)",
        new TypeInfo(SubSeq.class, "xs:anyAtomicType+", 3),
        new TypeInfo(RangeSeq.class, "xs:integer+", 3));
    test("('x', 1 to 100) => remove(1)",
        new TypeInfo(SubSeq.class, "xs:anyAtomicType+", 100),
        new TypeInfo(RangeSeq.class, "xs:integer+", 100));

    test("map:build(1 to 3) => map:remove(1)",
        new TypeInfo(XQTrieMap.class, "map(xs:integer, xs:integer)", 2),
        new TypeInfo(XQIntMap.class, "map(xs:integer, xs:integer)", 2));
    test("{ 1: ('x', 1 to 3) => remove(1) }",
        new TypeInfo(XQSingletonMap.class, "map(xs:integer, xs:anyAtomicType+)", 1),
        new TypeInfo(XQSingletonMap.class, "map(xs:integer, xs:integer+)", 1));
    test("{ 1: ('x', 1 to 3) => remove(1), 2: 3 }",
        new TypeInfo(XQIntValueMap.class, "map(xs:integer, xs:anyAtomicType+)", 2),
        new TypeInfo(XQIntValueMap.class, "map(xs:integer, xs:integer+)", 2));
    test("{ 1: ('x', 1 to 3) => remove(1), 2: 3 }",
        new TypeInfo(XQIntValueMap.class, "map(xs:integer, xs:anyAtomicType+)", 2),
        new TypeInfo(XQIntValueMap.class, "map(xs:integer, xs:integer+)", 2));
    test("{ 'a': ('x', 1 to 3) => remove(1), 'b': 3 }",
        new TypeInfo(XQRecordMap.class, "record(a, b)", 2),
        new TypeInfo(XQRecordMap.class, "record(a, b)", 2));

    test("[ (1, 2), 3, 4 ] => array:remove(1)",
        new TypeInfo(SmallArray.class, "array(xs:integer+)", 2),
        new TypeInfo(ItemArray.class, "array(xs:integer)", 2));
    test("[ (1, 2), 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 ] => array:remove(1)",
        new TypeInfo(BigArray.class, "array(xs:integer+)", 10),
        new TypeInfo(ItemArray.class, "array(xs:integer)", 10));
    test("array { ('x', 1 to 3) => remove(1) }",
        new TypeInfo(ItemArray.class, "array(xs:anyAtomicType)", 3),
        new TypeInfo(ItemArray.class, "array(xs:integer)", 3));
  }

  /** Unknown keyword parameter: hint to similar parameter name. */
  @Test public void unknownKeyword() {
    // Levenshtein match for typo
    unknownName("declare function local:x($alpha) { }; local:x(alph := 0)",
        PARAMUNKNOWN_X_X, "alpha");
    // prefix fallback for input that is too far in Levenshtein distance
    unknownName("declare function local:c($langitude, $longitude) { };"
        + " local:c(langi := 1, longitude := 2)", PARAMUNKNOWN_X_X, "langitude");
  }

  /** Unknown built-in function: hint to similar function name. */
  @Test public void unknownFunction() {
    // Levenshtein match for typo (unprefixed call, hint without fn: prefix)
    unknownName("coun()", WHICHFUNC_X, "count");
    // prefix fallback for input that is too far in Levenshtein distance
    unknownName("all-eq()", WHICHFUNC_X, "all-equal");
    // shortest name wins among multiple prefix matches
    unknownName("fold-(1)", WHICHFUNC_X, "fold-left");
    // no hint for inputs that cover less than half of the closest name
    noHint("x()", WHICHFUNC_X);
    // prefix fallback for user-defined function (local: prefix is preserved)
    unknownName("declare function local:abcde($john) { }; local:abc()",
        WHICHFUNC_X, "local:abcde");
    // built-in match preferred over user-defined when both exist
    unknownName("declare function local:subsequence-after() { };"
        + " subsequenc(1, 2, 3)", WHICHFUNC_X, "subsequence");
  }

  /** Unprefixed call of a user-defined function with wrong arity reports an arity mismatch. */
  @Test public void wrongArityNoNamespace() {
    error("declare function abc($j) { }; abc()", INVNARGS_X_X);
  }

  /** Unprefixed call of a built-in must still resolve when a same-named user function exists. */
  @Test public void shadowedBuiltin() {
    query("declare function abs($x as xs:integer, $y as xs:integer) as xs:integer"
        + " { $x + $y }; abs(-5)", 5);
  }

  /** Unknown variable: hint to similar variable name. */
  @Test public void unknownVariable() {
    unknownName("for $letter in 1 to 5 return $lette", VARUNDEF_X, "$letter");
    // innermost binding wins on Levenshtein ties
    unknownName("let $l1 := 1 let $l2 := 2 return $l", VARUNDEF_X, "$l2");
  }

  /** Unknown annotation: hint to similar annotation name. */
  @Test public void unknownAnnotation() {
    // XQuery namespace (reserved): "private" is the spec annotation
    unknownName("declare %privte function local:f() { 1 }; local:f()",
        ANNRESERVED_X, "%private");
    // BaseX namespace: "lazy" is a valid annotation
    unknownName("declare %basex:lasy function local:f() { 1 }; local:f()",
        BASEX_ANN1_X, "%basex:lazy");
    // prefix fallback for short input
    unknownName("declare %output:inden('yes') function local:f() { 1 }; local:f()",
        BASEX_ANN1_X, "%output:indent");
  }

  /** Unknown atomic type: hint to similar type name. */
  @Test public void unknownType() {
    // Levenshtein match for typo
    unknownName("'a' cast as xs:strin", WHICHCAST_X, "xs:string");
    // prefix fallback for short input that is too far in Levenshtein distance
    unknownName("'a' cast as xs:integ", WHICHCAST_X, "xs:integer");
  }

  /**
   * Checks that the error message includes a similar-name hint.
   * @param query query that should fail
   * @param code expected error code
   * @param similar expected leading substring of the "maybe: ..." hint
   */
  private static void unknownName(final String query, final QueryError code,
      final String similar) {
    try {
      eval(query);
      fail("Query did not fail.");
    } catch(final QueryException ex) {
      assertSame(code, ex.error(), ex.getLocalizedMessage());
      final String msg = ex.getLocalizedMessage();
      assertTrue(msg.contains("(maybe: " + similar), msg);
    } catch(final Exception ex) {
      fail(ex);
    }
  }

  /**
   * Checks that the error message includes no similar-name hint.
   * @param query query that should fail
   * @param code expected error code
   */
  private static void noHint(final String query, final QueryError code) {
    try {
      eval(query);
      fail("Query did not fail.");
    } catch(final QueryException ex) {
      assertSame(code, ex.error(), ex.getLocalizedMessage());
      final String msg = ex.getLocalizedMessage();
      assertFalse(msg.contains("maybe"), msg);
    } catch(final Exception ex) {
      fail(ex);
    }
  }

  /**
   * Tests the data structure of an expression before and after shrinking it.
   * @param query query
   * @param before type before shrinking
   * @param after type after shrinking
   */
  private void test(final String query, final TypeInfo before, final TypeInfo after) {
    checkType(query, before);
    checkType(_PROF_SHRINK.args(" " + query), after);
  }
}
