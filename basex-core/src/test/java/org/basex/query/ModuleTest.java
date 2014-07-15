package org.basex.query;

import static org.junit.Assert.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.query.util.*;
import org.basex.util.*;
import org.junit.Test;

/**
 * Module tests.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public class ModuleTest extends SandboxTest {
  /**
   * Tests the {@link QueryContext#parseLibrary(String, String, StaticContext)}
   * method.
   */
  @Test
  public void module() {
    final QueryContext qc = new QueryContext(context);
    try {
      qc.parseLibrary("module namespace m='foo'; declare function m:foo() { m:bar() }; ",
          "", null);
      fail("Unknown function 'm:bar()' was not detected.");
    } catch(final QueryException e) {
      assertSame(Err.FUNCUNKNOWN, e.err());
    } finally {
      qc.close();
    }
  }

  /**
   * Tests the {@link QueryContext#parseLibrary(String, String, StaticContext)} method.
   * @throws Exception exception
   */
  @Test
  public void module2() throws Exception {
    final QueryContext qc = new QueryContext(context);
    final IOFile a = new IOFile("src/test/resources/recmod/a.xqm");
    qc.parseLibrary(Token.string(a.read()), a.path(), null);
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
    file.write(Token.token("import module namespace a='a'at'a.xqm'; ()"));
    new IOFile(sandbox, "a.xqm").write(Token.token("module namespace a='a';"
        + "import module namespace b='b' at 'b.xqm'; declare function a:a(){()};"));
    new IOFile(sandbox, "b.xqm").write(Token.token("module namespace b='b';"
        + "import module namespace a='a' at 'a.xqm'; declare function b:b(){a:a()};"));
    new Run(file.path()).execute(context);

    // repository files
    final IOFile repo = new IOFile(sandbox, "repo");
    repo.md();
    new IOFile(repo, "a.xqm").write(Token.token("module namespace a='a';"
        + "import module namespace b='b'; declare function a:a(){()};"));
    new IOFile(repo, "b.xqm").write(Token.token("module namespace b='b';"
        + "import module namespace a='a'; declare function b:b(){a:a()};"));

    new QueryContext(context).parseMain("import module namespace a='a'; ()", null, null);
  }
}
