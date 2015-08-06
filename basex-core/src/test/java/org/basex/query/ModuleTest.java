package org.basex.query;

import static org.junit.Assert.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.junit.Test;

/**
 * Module tests.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class ModuleTest extends SandboxTest {
  /**
   * Imports a built-in module.
   */
  @Test
  public void builtIn() {
    query("import module namespace xquery = 'http://basex.org/modules/xquery'; 1");
  }

  /**
   * Tests the {@link QueryContext#parseLibrary(String, String, StaticContext)}
   * method.
   */
  @Test
  public void module() {
    try(final QueryContext qc = new QueryContext(context)) {
      qc.parseLibrary("module namespace m='foo'; declare function m:foo() { m:bar() }; ", "", null);
      fail("Unknown function 'm:bar()' was not detected.");
    } catch(final QueryException e) {
      assertSame(QueryError.FUNCUNKNOWN_X, e.error());
    }
  }

  /**
   * Tests the {@link QueryContext#parseLibrary(String, String, StaticContext)} method.
   * @throws Exception exception
   */
  @Test
  public void module2() throws Exception {
    final IOFile a = new IOFile("src/test/resources/recmod/a.xqm");
    try(final QueryContext qc = new QueryContext(context)) {
      qc.parseLibrary(Token.string(a.read()), a.path(), null);
    }
  }

  /**
   * Tests circular dependencies.
   * @throws Exception exception
   */
  @Test
  public void circularDeps() throws Exception {
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

    try(final QueryContext qc = new QueryContext(context)) {
      qc.parseMain("import module namespace a='a'; ()", null, null);
    }
  }

  /**
   * Uses a URI resolver.
   * @throws Exception exception
   */
  @Test
  public void uriResolver() throws Exception {
    final String query = "import module namespace m='uri' at 'x.xq'; m:f()";
    try(final QueryProcessor qp = new QueryProcessor(query, context)) {
      qp.uriResolver(new UriResolver() {
        @Override
        public IO resolve(final String path, final String uri, final Uri base) {
          return new IOContent("module namespace m='uri'; declare function m:f() { 'OK' };");
        }
      });
      assertEquals(qp.value().serialize().toString(), "OK");
    }
  }
}
