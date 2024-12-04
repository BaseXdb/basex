package org.basex.query;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.junit.jupiter.api.Test;

/**
 * Module tests.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class ModuleTest extends SandboxTest {
  /**
   * Imports a built-in module.
   */
  @Test public void builtIn() {
    query("import module namespace xquery = 'http://basex.org/modules/xquery'; 1");
  }

  /**
   * Tests the {@link QueryContext#parseLibrary} method.
   */
  @Test public void module() {
    try(QueryContext qc = new QueryContext(context)) {
      qc.parse("module namespace m='foo'; declare function m:foo() { m:bar() }; ", "");
      fail("Unknown function 'm:bar()' was not detected.");
    } catch(final QueryException ex) {
      assertSame(QueryError.WHICHFUNC_X, ex.error());
    }
  }

  /**
   * Tests the {@link QueryContext#parseLibrary} method.
   * @throws Exception exception
   */
  @Test public void module2() throws Exception {
    final IOFile file = new IOFile("src/test/resources/recmod/a.xqm");
    try(QueryContext qc = new QueryContext(context)) {
      qc.parse(file.string(), file.path());
    }
  }

  /**
   * Tests a repository import.
   * @throws Exception exception
   */
  @Test public void repoFile() throws Exception {
    // repository files
    final IOFile repo = new IOFile(sandbox(), "repo");
    repo.md();
    write(new IOFile(repo, "a.xqm"), "module namespace a='a'; declare function a:a(){()};");

    try(QueryContext qc = new QueryContext(context)) {
      qc.parse("import module namespace a='a'; a:a()", null);
    }
  }

  /**
   * Tests circular dependencies.
   * @throws Exception exception
   */
  @Test public void circularDeps() throws Exception {
    // module files in same directory
    final IOFile sandbox = sandbox();
    final IOFile file = new IOFile(sandbox, "x.xq");
    write(file, "import module namespace a='a'at'a.xqm'; ()");
    write(new IOFile(sandbox, "a.xqm"), "module namespace a='a';"
        + "import module namespace b='b' at 'b.xqm'; declare function a:a(){()};");
    write(new IOFile(sandbox, "b.xqm"), "module namespace b='b';"
        + "import module namespace a='a' at 'a.xqm'; declare function b:b(){a:a()};");
    execute(new Run(file.path()));

    // repository files
    final IOFile repo = new IOFile(sandbox, "repo");
    repo.md();
    write(new IOFile(repo, "a.xqm"), "module namespace a='a';"
        + "import module namespace b='b'; declare function a:a(){()};");
    write(new IOFile(repo, "b.xqm"), "module namespace b='b';"
        + "import module namespace a='a'; declare function b:b(){a:a()};");

    try(QueryContext qc = new QueryContext(context)) {
      qc.parse("import module namespace a='a'; ()", null);
    }
  }

  /**
   * Uses a URI resolver.
   * @throws Exception exception
   */
  @Test public void uriResolver() throws Exception {
    final String query = "import module namespace m='uri' at 'x.xq'; m:f()";
    try(QueryProcessor qp = new QueryProcessor(query, context)) {
      qp.uriResolver((path, uri, base) ->
        new IOContent("module namespace m='uri'; declare function m:f() { 'OK' };"));
      assertEquals(qp.value().serialize().toString(), "OK");
    }
  }

  /** Tests type import. */
  @Test public void importedTypes() {
    final IOFile sandbox = sandbox();
    final IOFile a = new IOFile(sandbox, "a.xqm");
    write(a, "module namespace a = 'a';\n"
        + "import module namespace b = 'b' at 'b.xqm';\n"
        + "declare variable $a:v as b:t := 42;");
    final IOFile b = new IOFile(sandbox, "b.xqm");
    write(b, "module namespace b = 'b';\n"
        + "declare type b:t as xs:integer;"
        + "declare %private type b:p as xs:string;");

    // type is visible from second import of module b
    query("import module namespace a = 'a' at '" + a.path() + "';\n"
        + "import module namespace b = 'b' at '" + b.path() + "';\n"
        + "let $v as b:t := $a:v return $v");

    // private type remains invisible
    error("import module namespace b = 'b' at '" + b.path() + "';\n"
        + "let $v as b:p := 'b:p should not be visible' return $v", QueryError.TYPEUNKNOWN_X);
  }

  /** Tests fn:load-xquery-module with nested module imports. */
  @Test public void dynamicNestedImport() {
    final IOFile sandbox = sandbox();
    final IOFile m1 = new IOFile(sandbox, "m1.xqm");
    write(m1, "module namespace m = 'm';\n"
        + "import module namespace n = 'n' at 'n.xqm';");
    final IOFile m2 = new IOFile(sandbox, "m2.xqm");
    write(m2, "module namespace m = 'm';\n"
        + "import module namespace o = 'o' at 'o.xqm';\n"
        + "declare variable $m:v := 42;");
    final IOFile n = new IOFile(sandbox, "n.xqm");
    write(n, "module namespace n = 'n';");
    final IOFile o = new IOFile(sandbox, "o.xqm");
    write(o, "module namespace o = 'o';");

    // import of m works from m1, m2
    query("fn:load-xquery-module('m',"
        + "{'location-hints': ('" + m1.path() + "', '" + m2.path() + "')})?variables?*", 42);
    // import of m fails from m1, m2, o
    error("fn:load-xquery-module('m', {'location-hints': ('" + m1.path() + "', '" + m2.path()
        + "', '" + o.path() + "')})", QueryError.MODULE_FOUND_OTHER_X_X);
  }

  /** Tests rejection of functions and variables, when their modules are not explicitly imported. */
  @Test public void gh2048() {
    final IOFile sandbox = sandbox();
    final IOFile b = new IOFile(sandbox, "b.xqm");
    write(b, "module namespace b = 'b';\n"
        + "import module namespace c = 'c' at 'c.xqm';");
    write(new IOFile(sandbox, "c.xqm"), "module namespace c = 'c';\n"
        + "declare function c:hello(){\n"
        + "  $c:hello\n"
        + "};\n"
        + "declare variable $c:hello := 'can you see me now';");

    // function is still visible to fn:function-lookup
    query("import module namespace b = 'b'  at '" + b.path() + "';\n"
        + "declare namespace c = 'c';\n"
        + "fn:function-lookup(xs:QName('c:hello'), 0)()", "can you see me now");
    // function is still visible to inspect:functions
    query("import module namespace b  = 'b'  at '" + b.path() + "';\n"
        + "declare namespace c = 'c';\n"
        + "inspect:functions()", "Q{c}hello#0");

    error("import module namespace b = 'b'  at '" + b.path() + "';\n"
        + "declare namespace c = 'c';\n"
        + "c:hello()", QueryError.INVISIBLEFUNC_X);
    error("import module namespace b = 'b'  at '" + b.path() + "';\n"
        + "declare namespace c = 'c';\n"
        + "c:hello#0()", QueryError.INVISIBLEFUNC_X);
    error("import module namespace b = 'b'  at '" + b.path() + "';\n"
        + "declare namespace c = 'c';\n"
        + "$c:hello", QueryError.INVISIBLEVAR_X);
  }
}
