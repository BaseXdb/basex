package org.basex.http.restxq;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.io.*;
import org.junit.jupiter.api.*;

/**
 * This test contains RESTXQ modules that cannot be parsed.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RestXqModuleTest extends RestXqTest {
  /**
   * Endpoints of intact modules are still served.
   * @throws Exception exception
   */
  @Test public void brokenModule() throws Exception {
    register("declare %R:path('ok') function m:f() { 'x' };");
    add("declare %R:path('broken') function m:g($unbound) { $unbound };");

    assertEquals("x", get(200, "ok"));
    // the parse error is reported for requests that match no function
    assertContains(get(500, "broken"), "No binding defined for $unbound.");
    assertContains(get(500, "unknown"), "No binding defined for $unbound.");
  }

  /**
   * Modules are parsed again if an imported module has been changed.
   * @throws Exception exception
   */
  @Test public void importedModule() throws Exception {
    register("import module namespace lib = 'lib' at 'lib.xqm';" +
      "declare %R:path('imported') function m:f() { lib:f() };");
    assertContains(get(500, "imported"), "lib.xqm");

    // create imported module; simulate cache update after idle time
    final String path = context.soptions.get(StaticOptions.WEBPATH);
    new IOFile(path, "lib.xqm").write("module namespace lib = 'lib';" +
      "declare function lib:f() { 'x' };");
    WebModules.get(context).init(true);
    assertEquals("x", get(200, "imported"));
  }

  /**
   * Modules are parsed again if a transitively imported module has been changed.
   * @throws Exception exception
   */
  @Test public void transitiveImport() throws Exception {
    register("import module namespace lib = 'lib' at 'lib.xqm';" +
      "declare %R:path('imported') function m:f() { lib:f() };");
    final String path = context.soptions.get(StaticOptions.WEBPATH);
    new IOFile(path, "lib.xqm").write("module namespace lib = 'lib';" +
      "import module namespace lib2 = 'lib2' at 'lib2.xqm';" +
      "declare function lib:f() { lib2:f() };");
    final IOFile lib2 = new IOFile(path, "lib2.xqm");
    lib2.write("module namespace lib2 = 'lib2'; declare function lib2:f() { $unbound };");
    WebModules.get(context).init(true);
    assertContains(get(500, "imported"), "$unbound");

    // fix inner module; simulate cache update after idle time
    lib2.write("module namespace lib2 = 'lib2'; declare function lib2:f() { 'x' };");
    assertTrue(lib2.file().setLastModified(System.currentTimeMillis() - 60000));
    WebModules.get(context).init(true);
    assertEquals("x", get(200, "imported"));
  }

  /**
   * Modules are parsed again if an imported module was modified while being parsed.
   * @throws Exception exception
   */
  @Test public void modifiedWhileParsing() throws Exception {
    register("import module namespace lib = 'lib' at 'lib.xqm';" +
      "declare %R:path('imported') function m:f() { lib:f() };");
    final IOFile lib = new IOFile(context.soptions.get(StaticOptions.WEBPATH), "lib.xqm");
    final long ts = System.currentTimeMillis() + 60000;
    lib.write("module namespace lib = 'lib'; declare function lib:f() { lib:g() };");
    assertTrue(lib.file().setLastModified(ts));
    assertContains(get(500, "imported"), "lib:g");

    // fixed module has the timestamp of the broken one
    lib.write("module namespace lib = 'lib'; declare function lib:f() { 'x' };");
    assertTrue(lib.file().setLastModified(ts));
    WebModules.get(context).init(true);
    assertEquals("x", get(200, "imported"));
  }

  /**
   * New functions are registered after a syntax error in an imported module has been fixed.
   * @throws Exception exception
   */
  @Test public void newRouteAfterSyntaxError() throws Exception {
    final String path = context.soptions.get(StaticOptions.WEBPATH);
    final String header = "module namespace m = 'm';" +
      "import module namespace lib = 'lib' at 'lib/lib.xqm';" +
      "declare %rest:path('/{$p=.+}') function m:all($p) { 'all' };";
    final String ok = "module namespace lib = 'lib'; declare function lib:f() { 'x' };";
    final IOFile lib = new IOFile(path, "lib/lib.xqm"), mod = new IOFile(path, "m.xqm");
    // clear web directory
    register("declare %R:path('dummy') function m:dummy() { 'x' };");
    lib.write(ok);
    mod.write(header);
    assertEquals("all", get(200, "new/1"));

    // add function, break library
    mod.write(header + "declare %rest:path('/new/{$id}') function m:new($id) { lib:f() };");
    lib.write("module namespace lib = 'lib'; declare function lib:f() { 'x' ");
    WebModules.get(context).init(true);
    assertContains(get(500, "new/1"), "lib.xqm");

    // fix library
    lib.write(ok);
    WebModules.get(context).init(true);
    assertEquals("x", get(200, "new/1"));
  }

  /**
   * Functions of an imported module with the same file name are registered only once.
   * @throws Exception exception
   */
  @Test public void sameFileName() throws Exception {
    final String path = context.soptions.get(StaticOptions.WEBPATH);
    // clear web directory
    register("declare %R:path('dummy') function m:dummy() { 'x' };");
    new IOFile(path, "lib/m.xqm").write("module namespace lib = 'lib';" +
      "declare %rest:path('lib') function lib:f() { 'x' };");
    new IOFile(path, "m.xqm").write("module namespace m = 'm';" +
      "import module namespace lib = 'lib' at 'lib/m.xqm';");
    assertEquals("x", get(200, "lib"));
  }

  /**
   * Directories with an ignore file are skipped.
   * @throws Exception exception
   */
  @Test public void ignoredDirectory() throws Exception {
    register("declare %R:path('ok') function m:f() { 'x' };");
    final IOFile dir = new IOFile(context.soptions.get(StaticOptions.WEBPATH), "sub/");
    new IOFile(dir, "sub.xqm").write("module namespace sub = 'sub';" +
      "declare %rest:path('sub') function sub:f() { 'y' };");
    final IOFile ignore = new IOFile(dir, IO.IGNORESUFFIX);
    ignore.write("");

    WebModules.get(context).init(false);
    assertEquals("x", get(200, "ok"));
    get(404, "sub");

    assertTrue(ignore.delete());
    WebModules.get(context).init(false);
    assertEquals("y", get(200, "sub"));
  }

  /**
   * Parse errors are hidden if {@link StaticOptions#RESTXQERRORS} is disabled.
   * @throws Exception exception
   */
  @Test public void hiddenErrors() throws Exception {
    register("declare %R:path('ok') function m:f() { 'x' };");
    add("declare %R:path('broken') function m:g($unbound) { $unbound };");

    final StaticOptions sopts = HTTPContext.get().context().soptions;
    sopts.set(StaticOptions.RESTXQERRORS, false);
    try {
      assertEquals("x", get(200, "ok"));
      assertEquals("Service not found.", get(404, "broken"));
    } finally {
      sopts.set(StaticOptions.RESTXQERRORS, true);
    }
  }
}
