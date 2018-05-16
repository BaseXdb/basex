package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.query.*;
import org.junit.*;
import org.junit.Test;

/**
 * Mixed XQuery tests.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class MixedTest extends AdvancedQueryTest {
  /** Test XQuery module file. */
  private static final String XQMFILE = "src/test/resources/hello.xqm";

  /**
   * Drops the collection.
   */
  @AfterClass
  public static void after() {
    execute(new DropDB(NAME));
    execute(new DropDB(NAME + '2'));
  }

  /** Catches duplicate module import. */
  @Test
  public void duplImport() {
    error("import module namespace a='world' at '" + XQMFILE + "';" +
      "import module namespace a='world' at '" + XQMFILE + "'; 1",
      DUPLMODULE_X);
  }

  /** Catches duplicate module import with different module uri. */
  @Test
  public void duplImportDiffUri() {
    error("import module namespace a='world' at '" + XQMFILE + "';" +
      "import module namespace a='galaxy' at '" + XQMFILE + "'; 1",
      DUPLNSDECL_X);
  }

  /** Catches duplicate module import. */
  @Test
  public void duplLocation() {
    error("import module namespace a='world' at '" + XQMFILE + "';" +
      "import module namespace b='galaxy' at '" + XQMFILE + "'; 1",
      WRONGMODULE_X_X_X);
  }

  /** Checks static context scoping in variables. */
  @Test
  public void varsInModules() {
    contains("import module namespace a='world' at '" + XQMFILE + "'; $a:eager", "hello:foo");
    contains("import module namespace a='world' at '" + XQMFILE + "'; $a:lazy", "hello:foo");
    contains("import module namespace a='world' at '" + XQMFILE + "'; $a:func()", "hello:foo");
    contains("import module namespace a='world' at '" + XQMFILE + "'; a:inlined()", "hello:foo");
  }

  /**
   * Overwrites an empty attribute value.
   */
  @Test
  public void emptyAttValues() {
    execute(new CreateDB(NAME, "<A a='' b=''/>"));
    query("replace value of node /A/@a with 'A'");
    query("/", "<A a=\"A\" b=\"\"/>");
  }

  /**
   * Parse recursive queries.
   */
  @Test
  public void parseRec() {
    // simple call
    query("declare function local:x() { if(<a/>) then 1 else local:x() }; local:x()");
    // call from FLWOR expression
    query("declare function local:x() { if(<a/>) then 1 else local:x() }; " +
        "let $x := local:x() return $x", 1);
  }

  /**
   * Performs count() on parts of a collection.
   */
  @Test
  public void countCollection() {
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
  @Test
  public void gh907() {
    execute(new CreateDB(NAME, "<x/>"));
    query("declare function local:a($a) { contains($a, 'a') }; //x[local:a(.)]", "");
  }

  /** Type intersections. */
  @Test
  public void gh1427() {
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
  @Test
  public void gh1567() {
    query("let $_ := '_' return document { <_/> }/*[self::*[name() = $_]]", "<_/>");
  }

  /** Node ids. */
  @Test
  public void gh1566() {
    query("((<_><a/><b/></_> update {})/* ! element _ { . })/*", "<a/>\n<b/>");
    query("((<_><a>A</a><b>B</b></_> update {})/* ! element _ { . })/*/node()", "A\nB");
  }

  /**
   * Tests document order across multiple documents or databases.
   * @throws IOException I/O exception
   */
  @Test
  public void diffDatabases() throws IOException {
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
}
