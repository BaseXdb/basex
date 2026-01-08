package org.basex.query;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.junit.jupiter.api.Test;

/**
 * Module tests.
 *
 * @author BaseX Team, BSD License
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
      qc.parse(file.readString(), file.path());
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
      assertEquals("OK", qp.value().serialize().toString());
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
        + "{ 'location-hints': ('" + m1.path() + "', '" + m2.path() + "') })?variables?*", 42);
    // import of m fails from m1, m2, o
    error("fn:load-xquery-module('m', { 'location-hints': ('" + m1.path() + "', '" + m2.path()
        + "', '" + o.path() + "') })", QueryError.MODULE_FOUND_OTHER_X_X);
  }

  /** Tests variable visibility. */
  @Test public void variableVisibility() {
    final IOFile sandbox = sandbox();
    final IOFile m = new IOFile(sandbox, "m.xqm");
    write(m, "module namespace m = 'm';\n"
        + "declare %private variable $m:x := 'module';\n"
        + "declare function m:f() {$m:x};");
    final IOFile n1 = new IOFile(sandbox, "n1.xqm");
    write(n1, "module namespace n = 'n';\n"
        + "declare function n:f() {$x};");
    final IOFile n2 = new IOFile(sandbox, "n2.xqm");
    write(n2, "module namespace n = 'n';\n"
        + "declare %private variable $x := 42;");
    final IOFile o = new IOFile(sandbox, "o.xqm");
    write(o, "module namespace o = 'o';\n"
        + "import module 'o' at '" + o.path() + "';\n"
        + "declare function o:f() {$o:x};");

    // private variable does not clash with the same name in another module
    query("import module namespace m = 'm' at '" + m.path() + "';\n"
        + "declare variable $m:x := 'main';\n"
        + "m:f(), $m:x", "module\nmain");

    // private variable is visible throughout module, even in different file
    query("import module namespace n = 'n' at '" + n1.path() + "', '" + n2.path() + "';\n"
        + "n:f()", 42);

    // variable in main module is not visible in imported module
    error("import module namespace o = 'o' at '" + o.path() + "';\n"
        + "declare variable $o:x := 42;\n"
        + "o:f()", QueryError.VARUNDEF_X);
  }

  /** Tests function visibility. */
  @Test public void functionVisibility() {
    final IOFile sandbox = sandbox();
    final IOFile p = new IOFile(sandbox, "p.xqm");
    write(p, "module namespace p = 'p';\n"
        + "declare %private function p:x() {'module'};\n"
        + "declare function p:f() {p:x()};");
    final IOFile q1 = new IOFile(sandbox, "q1.xqm");
    write(q1, "module namespace q = 'q';\n"
        + "declare function q:f() {x()};");
    final IOFile q2 = new IOFile(sandbox, "q2.xqm");
    write(q2, "module namespace q = 'q';\n"
        + "declare %private function x() {42};");
    final IOFile r = new IOFile(sandbox, "r.xqm");
    write(r, "module namespace r = 'r';\n"
        + "declare function r:f() {\n"
        + "  fn:divided-decimals-record(1, 2),\n"
        + "  fn:divided-decimals-record#2(3, 4),\n"
        + "  5 => fn:divided-decimals-record(6),\n"
        + "  function-lookup(#fn:divided-decimals-record, 2)(7, 8)\n"
        + "};");
    final IOFile s = new IOFile(sandbox, "s.xqm");
    write(s, "module namespace s = 's';\n"
        + "import module 's' at '" + s.path() + "';\n"
        + "declare function s:f() {s:x()};");

    // private function does not clash with the same name in another module
    query("import module namespace p = 'p' at '" + p.path() + "';\n"
        + "declare function p:x() {'main'};\n"
        + "p:f(), p:x()", "module\nmain");

    // private function is visible throughout module, even in different file
    query("import module namespace q = 'q' at '" + q1.path() + "', '" + q2.path() + "';\n"
        + "q:f()", 42);

    // built-in record constructor visible in library module
    query("import module namespace r = 'r' at '" + r.path() + "';\n"
        + "r:f()",
          "{\"quotient\":1,\"remainder\":2}\n"
        + "{\"quotient\":3,\"remainder\":4}\n"
        + "{\"quotient\":5,\"remainder\":6}\n"
        + "{\"quotient\":7,\"remainder\":8}");

    // private function reported as such
    error("import module namespace p = 'p' at '" + p.path() + "';\n"
        + "p:x()", QueryError.FUNCPRIVATE_X);

    // function in main module is not visible in imported module
    error("import module namespace s = 's' at '" + s.path() + "';\n"
        + "declare function s:x() {42};\n"
        + "s:f()", QueryError.WHICHFUNC_X);
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

    // function is not visible to fn:function-lookup (not in dynamically known function definitions)
    query("import module namespace b = 'b'  at '" + b.path() + "';\n"
        + "fn:function-lookup(#Q{c}hello, 0)", "");
    // function is still visible to inspect:functions
    query("import module namespace b  = 'b'  at '" + b.path() + "';\n"
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
