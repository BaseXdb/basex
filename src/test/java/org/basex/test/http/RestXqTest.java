package org.basex.test.http;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.api.*;
import org.basex.core.*;
import org.basex.io.*;
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

  /** Retrieve root.
   * @throws Exception exception */
  @Test public void getRoot() throws Exception {
    ok("declare %R:path('') function m:f() { 'root' };", "/", "root");
  }

  /** Retrieve path.
   * @throws Exception exception */
  @Test public void getTest() throws Exception {
    ok("declare %R:path('/test') function m:f() {'ok'};", "test", "ok");
  }

  /** Retrieve path with variable.
   * @throws Exception exception */
  @Test public void getVariable() throws Exception {
    final String f = "declare %R:path('/var/{$x}') function m:f($x) {$x};";
    ok(f, "var/x", "x");
    ok(f, "var/y", "y");
  }

  /** Retrieve path with variable on root level.
   * @throws Exception exception */
  @Test public void getRootVariable() throws Exception {
    final String f = "declare %R:path('{$x}/a/b/c') function m:f($x) {$x};";
    ok(f, "x/a/b/c", "x");
    // wrong path
    no(f, "x/a/b/d");
  }

  /** Retrieve path with typed variable.
   * @throws Exception exception */
  @Test public void getInteger() throws Exception {
    final String f = "declare %R:path('/{$x}') function m:f($x as xs:int) {$x};";
    ok(f, "2", "2");
    no(f, "StRiNg");
  }

  /** Retrieve path with multiple variables.
   * @throws Exception exception */
  @Test public void getMultiply() throws Exception {
    final String f = "declare %R:path('{$x}/{$y}') function " +
        "m:f($x as xs:integer,$y as xs:integer) {$x*$y};";
    ok(f, "2/3", "6");
    no(f, "2/x");
  }

  /**
   * Errors around the {@code %path} annotation.
   * @throws Exception exception
   */
  @Test public void errorPath() throws Exception {
    // correct syntax
    ok("declare %R:path('') function m:f() {()};", "", "");
    // no path annotation
    no("declare %R:GET function m:f() {()};", "");
    // no path argument
    no("declare %R:path function m:f() {()};", "");
    // empty path argument
    no("declare %R:path(()) function m:f() {()};", "");
    // two path arguments
    no("declare %R:path(('a', 'b')) function m:f() {()};", "a");
    no("declare %R:path('a') %R:path('b') function m:f() {()};", "a");
    // path not found
    no("declare %R:path('') function m:f() { 1 };", "X");
    no("declare %R:path('a') function m:f() { 1 };", "");
  }

  /**
   * Errors around {@code %path} segments.
   * @throws Exception exception
   */
  @Test public void errorPathVar() throws Exception {
    // correct syntax
    ok("declare %R:path('{$x}') function m:f($x) {$x};", "1", "1");
    // invalid variable definitions
    no("declare %R:path('{a}') function m:f() {()};", "a");
    no("declare %R:path('{ $a }') function m:f() {()};", "a");
    // invalid variable name
    no("declare %R:path('{$x::x}') function m:f() {()};", "a");
    no("declare %R:path('{$x x}') function m:f() {()};", "a");
    // missing argument
    no("declare %R:path('{$x}') function m:f() {()};", "a");
    // variable in template specified twice
    no("declare %R:path('{$x}/{$x}') function m:f($x) {()};", "a");
    // variable in template missing
    no("declare %R:path('') function m:f($x) {()};", "");
    // variable must inherit xs:anyAtomicType
    no("declare %R:path('{$x}') function m:f($x as node()) {$x};", "1");
  }

  /**
   * Errors around various annotations.
   * @throws Exception exception
   */
  @Test public void errorAnn() throws Exception {
    // correct syntax
    ok("declare %R:path('') function m:f() {'x'};", "", "x");
    // invalid annotation
    no("declare %R:path('') %R:xyz function m:f() {'x'};", "");
  }

  /**
   * Errors around serialization parameters.
   * @throws Exception exception
   */
  @Test public void errorOutput() throws Exception {
    // correct syntax
    ok("declare %R:path('') %output:method('text') function m:f() {'9'};", "", "9");
    // unknown serialization parameter
    no("declare %R:path('') %output:xyz('abc') function m:f() {'9'};", "");
    // parameter must contain single string
    no("declare %R:path('') %output:method function m:f() {'9'};", "");
    no("declare %R:path('') %output:method(('xml','html')) function m:f() {'9'};", "");
  }

  /**
   * Errors around the {@code %consumes} annotation.
   * @throws Exception exception
   */
  @Test public void errorConsumes() throws Exception {
    // correct syntax
    ok("declare %R:path('') %R:consumes('a/b') function m:f() {()};", "", "");
    // duplicate annotation
    no("declare %R:path('') %R:consumes(('a','b')) function m:f(){()};", "");
    no("declare %R:path('') %R:consumes('a') %R:consumes('b') function m:f(){()};", "");
  }

  /**
   * Errors around the {@code %produces} annotation.
   * @throws Exception exception
   */
  @Test public void errorProduces() throws Exception {
    // correct syntax
    ok("declare %R:path('') %R:produces('a/b') function m:f() {()};", "", "");
    // duplicate annotation
    no("declare %R:path('') %R:produces(('a','b')) function m:f(){()};", "");
    no("declare %R:path('') %R:produces('a') %R:produces('b') function m:f(){()};", "");
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Executes the specified GET request and tests the result.
   * @param function function to test
   * @param query request
   * @param exp expected result
   * @throws IOException I/O exception
   */
  private static void ok(final String function, final String query, final String exp)
      throws IOException {
    install(function);
    assertEquals(exp, get(ROOT, query));
  }

  /**
   * Executes the specified GET request and tests for an error.
   * @param function function to test
   * @param query request
   * @throws IOException I/O exception
   */
  private static void no(final String function, final String query) throws IOException {
    install(function);
    try {
      get(ROOT, query);
      fail("Error expected: " + query);
    } catch(final BaseXException ex) {
    }
  }

  /**
   * Installs a new module and removes all others.
   * @param function function to be tested
   * @throws IOException I/O exception
   */
  private static void install(final String function) throws IOException {
    module().write(new TokenBuilder(HEADER).add(function).finish());
    // wait 1 millisecond to guarantee new timestamp
    Performance.sleep(1);
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
