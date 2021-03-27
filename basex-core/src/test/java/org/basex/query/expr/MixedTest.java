package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import java.io.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Mixed XQuery tests.
 *
 * @author BaseX Team 2005-21, BSD License
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

  /** Catches duplicate module import with different module uri. */
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
    query(COUNT.args(_DB_OPEN.args(NAME, "a") + "/a"), 1);
    query(COUNT.args(_DB_OPEN.args(NAME) + "/a"), 2);
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

    query("<a><b/></a>  ! <x>{ . }</x>/a/b/ancestor::node() ! name()", "x\na");
    query("(<a><b/></a> update {}) ! <x>{ . }</x>/a/b/ancestor::node() ! name()", "x\na");

    query("<A>{ <a><b/><c/></a>/* }</A>/b/following-sibling::c", "<c/>");
    query("<A>{ (<a><b/><c/></a> update {})/* }</A>/b/following-sibling::c", "<c/>");

    query("<A>{ document { <a><b/><c/></a> }/a/* }</A>/b/following-sibling::c", "<c/>");
    query("<A>{ (document { <a><b/><c/></a> } update {})/a/* }</A>/b/following-sibling::c", "<c/>");

    query("<A>{ <a><b/><c/></a>/* }</A>/c/preceding-sibling::b", "<b/>");
    query("<A>{ (<a><b/><c/></a> update {})/* }</A>/c/preceding-sibling::b", "<b/>");

    query("<A>{ document { <a><b/><c/></a> }/a/* }</A>/c/preceding-sibling::b", "<b/>");
    query("<A>{ (document { <a><b/><c/></a> } update {})/a/* }</A>/c/preceding-sibling::b", "<b/>");

    error("let $doc := document { <a><b/></a> } update ()"
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
    final String query = "let $id := '0' return db:attribute('" + NAME + "', '1')/..";
    query(query, "<b id=\"1\"/>");
    query(query + "[../@id = $id]", "<b id=\"1\"/>");
    query(query + "[..[@id = $id]]", "<b id=\"1\"/>");
    query(query + "[..[@id = $id]/parent::document-node()]", "<b id=\"1\"/>");
  }

  /** Node ids. */
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
    query("db:open('" + NAME + "')/*/* union db:open('" + NAME + "2')/*/*",
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

    error("element { '' } {}", INVNAME_X);
    error("element { ' ' } {}", INVNAME_X);
    error("element { 'a b' } {}", INVNAME_X);
    error("element { 'a:b' } {}", INVPREF_X);
    error("element { 'a: b' } {}", INVNAME_X);
    error("element { 'Q{}' } {}", INVNAME_X);
    error("element { 'Q{ }' } {}", INVNAME_X);

    error("element { 'Q{ http://www.w3.org/2000/xmlns/ }a' } {}", INVNAME_X);
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

  /** JSON documents, node ids. */
  public void gh1983() {
    query("tail(json:parse('{}')/*/ancestor-or-self::node()) instance of element()", true);
    query("tail(csv:parse('')/*/ancestor-or-self::node()) instance of element()", true);
  }

  /** fn:json-to-xml, namespaces. */
  @Test public void gh1997() {
    execute(new Close());
    query("db:create('" + NAME + "', analyze-string('a', 'a')/*, '" + NAME + "')");
    query("db:open('" + NAME + "')/* => namespace-uri()", "http://www.w3.org/2005/xpath-functions");
    query("db:create('" + NAME + "', json-to-xml('[1]')/*/*, '" + NAME + "')");
    query("db:open('" + NAME + "')/* => namespace-uri()", "http://www.w3.org/2005/xpath-functions");
  }
}
