package org.basex.test.restxq;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.api.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.test.rest.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the RESTful Annotations for XQuery implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class RestXqTest extends HTTPTest {
  /** Root path. */
  private static final String ROOT = "http://" + LOCALHOST + ":9998/restxq/";
  /** Query header. */
  private static final String HEADER =
    "module namespace m = 'http://basex.org/modules/restxq/test';" + Prop.NL +
    "declare namespace R = 'http://exquery.org/ns/rest/annotation';" + Prop.NL;

  // INITIALIZERS =============================================================

  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeClass
  public static void start() throws Exception {
    init(true);
  }

  // TEST METHODS =============================================================

  /** Test of the GET request.
   * @throws Exception exception */
  @Test public void getRoot() throws Exception {
    install("declare %R:GET %R:path('') function m:f() { 'root' };");
    ok("/", "root");
  }

  /** Test of the GET request.
   * @throws Exception exception */
  @Test public void getTest() throws Exception {
    install("declare %R:GET %R:path('/test') function m:f() {'ok'};");
    ok("test", "ok");
  }

  /** Test of the GET request.
   * @throws Exception exception */
  @Test public void getVariable() throws Exception {
    install("declare %R:GET %R:path('/var/{$x}') function m:f($x) {$x};");
    ok("var/x", "x");
    ok("var/y", "y");
  }

  /** Test of the GET request.
   * @throws Exception exception */
  @Test public void getRootVariable() throws Exception {
    install("declare %R:GET %R:path('{$x}/a/b/c') function m:f($x) {$x};");
    ok("x/a/b/c", "x");
    no("x/a/b/d");
  }

  /** Test of the GET request.
   * @throws Exception exception */
  @Test public void getInteger() throws Exception {
    install("declare %R:GET %R:path('/{$x}') function m:f($x as xs:integer) {$x};");
    ok("2", "2");
    no("StRiNg");
  }

  /** Test of the GET request.
   * @throws Exception exception */
  @Test public void getMultiply() throws Exception {
    install("declare %R:GET %R:path('{$x}/{$y}') " +
        "function m:f($x as xs:integer,$y as xs:integer) {$x*$y};");
    ok("2/3", "6");
    no("2/x");
  }

  /** Test of GET request errors.
   * @throws Exception exception */
  @Test public void getMissing() throws Exception {
    install("declare %R:GET %R:path('a/b') function m:f() { 1 };");
    no("c");
    no("a/c");
  }

  /**
   * GET, static errors.
   * @throws Exception exception
   */
  @Test public void errorSinglePath() throws Exception {
    // correct syntax
    install("declare %R:GET %R:path('') function m:f() {()};");
    ok("", "");
    // no path annotation
    installError("declare %R:GET function m:f() {()};");
    // no path argument
    installError("declare %R:GET %R:path function m:f() {()};");
    // empty path argument
    installError("declare %R:GET %R:path(()) function m:f() {()};");
    // two path arguments
    installError("declare %R:GET %R:path(('a', 'b')) function m:f() {()};");
    // two path arguments
    installError("declare %R:GET %R:path('a') %R:path('b') function m:f() {()};");
  }

  /**
   * GET, static errors.
   * @throws Exception exception
   */
  @Test public void errorConsumes() throws Exception {
    // correct syntax
    install("declare %R:GET %R:path('') %R:consumes('a/b') function m:f() {()};");
    ok("", "");
    // duplicate annotation
    installError("declare %R:GET %R:path('') %R:consumes(('a','b')) function m:f(){()};");
    // duplicate annotation
    installError("declare %R:GET %R:path('') %R:consumes('a') %R:consumes('b') " +
        "function m:f(){()};");
  }

  /**
   * GET, static errors.
   * @throws Exception exception
   */
  @Test public void errorProduces() throws Exception {
    // correct syntax
    install("declare %R:GET %R:path('') %R:produces('a/b') function m:f() {()};");
    ok("", "");
    // duplicate annotation
    installError("declare %R:GET %R:path('') %R:produces(('a','b')) function m:f(){()};");
    // duplicate annotation
    installError("declare %R:GET %R:path('') %R:produces('a') %R:produces('b') " +
        "function m:f(){()};");
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Executes the specified GET request and tests the result.
   * @param query request
   * @param exp expected result
   * @throws IOException I/O exception
   */
  private static void ok(final String query, final String exp) throws IOException {
    assertEquals(exp, get(ROOT, query));
  }

  /**
   * Executes the specified GET request and tests for an error.
   * @param query request
   * @throws IOException I/O exception
   */
  private static void no(final String query) throws IOException {
    try {
      get(ROOT, query);
      fail("Error expected: " + query);
    } catch(final BaseXException ex) {
    }
  }

  /**
   * Installs a new module and checks if it yields an error.
   * @param function function to be tested
   * @throws IOException I/O exception
   */
  private static void installError(final String function) throws IOException {
    install(function);
    no("");
  }

  /**
   * Installs a new module and removes all others.
   * @param function function to be tested
   * @throws IOException I/O exception
   */
  private static void install(final String function) throws IOException {
    module().write(new TokenBuilder(HEADER).add(function).finish());
  }

  /**
   * Returns the XQuery test module.
   * @return test module
   */
  private static IOFile module() {
    final Context ctx = HTTPSession.context();
    final String path = ctx.mprop.get(MainProp.HTTPPATH);
    return new IOFile(path, DB + IO.XQMSUFFIX);
  }
}
