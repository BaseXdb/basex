package org.basex.test.http;

import static org.basex.core.Text.*;
import static org.basex.io.MimeTypes.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class contains RESTXQ API tests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class RestXqTest extends HTTPTest {
  /** Root path. */
  private static final String ROOT = "http://" + LOCALHOST + ":9998/restxq/";
  /** Query header. */
  private static final String HEADER =
    "module  namespace m = 'http://basex.org/modules/restxq/test';" + Prop.NL +
    "declare namespace R = 'http://exquery.org/ns/restxq';" + Prop.NL;
  /** Counter. */
  private static int count;

  // INITIALIZERS =============================================================

  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeClass
  public static void start() throws Exception {
    init(ROOT, true);
  }

  // TEST METHODS =============================================================

  /** Retrieve root.
   * @throws Exception exception */
  @Test public void get() throws Exception {
    get("declare %R:path('') function m:f() { 'root' };", "", "root");
    get("declare %R:path('') function m:f() { 'root' };", "/", "root");
    // explicit GET method
    get("declare %R:GET %R:path('') function m:f() { 'root' };", "", "root");
    // duplicate GET method
    getE("declare %R:GET %R:GET %R:path('') function m:f() { 'root' };", "");
  }

  /** Retrieve path.
   * @throws Exception exception */
  @Test public void getTest() throws Exception {
    get("declare %R:path('/test') function m:f() {'ok'};", "test", "ok");
  }

  /** Retrieve path with variable.
   * @throws Exception exception */
  @Test public void getVariable() throws Exception {
    final String f = "declare %R:path('/var/{$x}') function m:f($x) {$x};";
    get(f, "var/x", "x");
    get(f, "var/y", "y");
  }

  /** Retrieve path with variable on root level.
   * @throws Exception exception */
  @Test public void getRootVariable() throws Exception {
    final String f = "declare %R:path('{$x}/a/b/c') function m:f($x) {$x};";
    get(f, "x/a/b/c", "x");
    // wrong path
    getE(f, "x/a/b/d");
  }

  /** Retrieve path with typed variable.
   * @throws Exception exception */
  @Test public void getInteger() throws Exception {
    final String f = "declare %R:path('/{$x}') function m:f($x as xs:int) {$x};";
    get(f, "2", "2");
    getE(f, "StRiNg");
  }

  /** Retrieve path with multiple variables.
   * @throws Exception exception */
  @Test public void getMultiply() throws Exception {
    final String f = "declare %R:path('{$x}/{$y}') function " +
        "m:f($x as xs:integer,$y as xs:integer) {$x*$y};";
    get(f, "2/3", "6");
    getE(f, "2/x");
  }

  /** Retrieve path with typed variable.
   * @throws Exception exception */
  @Test public void post() throws Exception {
    String f = "declare %R:POST('{$x}') %R:path('') function m:f($x) {$x};";
    post(f, "", "12", "12", TEXT_PLAIN);
    post(f, "", "<x>A</x>", "<x>A</x>", APP_XML);
    f = "declare %R:POST('{$x}') %R:path('') function m:f($x) {$x/json/*};";
    post(f, "", "<A>B</A>", "{ \"A\":\"B\" }", APP_JSON);
    f = "declare %R:POST('{$x}') %R:path('') function m:f($x) {$x};";
    post(f, "", "<A/>", "[\"A\"]", APP_JSONML);
    f = "declare %R:POST('{$x}') %R:path('') function m:f($x) {$x/csv/*/*};";
    post(f, "", "<col>A</col>", "A", TEXT_CSV);
    f = "declare %R:POST('{$x}') %R:path('') function m:f($x) {$x};";
    post(f, "", "QUFB", "AAA", APP_OCTET);
    post(f, "", "QUFB", "AAA", "whatever/type");
  }

  /**
   * {@code %path} annotation.
   * @throws Exception exception
   */
  @Test public void path() throws Exception {
    // correct syntax
    get("declare %R:path('') function m:f() {()};", "", "");
    // no path annotation
    getE("declare %R:GET function m:f() {()};", "");
    // no path argument
    getE("declare %R:path function m:f() {()};", "");
    // empty path argument
    getE("declare %R:path() function m:f() {()};", "");
    // two path arguments
    getE("declare %R:path('a', 'b') function m:f() {()};", "a");
    getE("declare %R:path('a') %R:path('b') function m:f() {()};", "a");
    // path not found
    getE("declare %R:path('') function m:f() { 1 };", "X");
    getE("declare %R:path('a') function m:f() { 1 };", "");
  }

  /**
   * {@code %path} segments.
   * @throws Exception exception
   */
  @Test public void pathVar() throws Exception {
    // correct syntax
    get("declare %R:path('{$x}') function m:f($x) {$x};", "1", "1");
    // invalid variable definitions
    getE("declare %R:path('{a}') function m:f() {()};", "a");
    getE("declare %R:path('{ $a }') function m:f() {()};", "a");
    // invalid variable name
    getE("declare %R:path('{$x::x}') function m:f() {()};", "a");
    getE("declare %R:path('{$x x}') function m:f() {()};", "a");
    // missing argument
    getE("declare %R:path('{$x}') function m:f() {()};", "a");
    // variable in template specified twice
    getE("declare %R:path('{$x}/{$x}') function m:f($x) {()};", "a");
    // variable in template missing
    getE("declare %R:path('') function m:f($x) {()};", "");
    // variable must inherit xs:anyAtomicType
    getE("declare %R:path('{$x}') function m:f($x as node()) {$x};", "1");
  }

  /**
   * Various annotations.
   * @throws Exception exception
   */
  @Test public void various() throws Exception {
    // correct syntax
    get("declare %R:path('') function m:f() {'x'};", "", "x");
    // invalid annotation
    getE("declare %R:path('') %R:xyz function m:f() {'x'};", "");
  }

  /**
   * Serialization parameters.
   * @throws Exception exception
   */
  @Test public void output() throws Exception {
    // correct syntax
    get("declare %R:path('') %output:method('text') function m:f() {'9'};", "", "9");
    // unknown serialization parameter
    getE("declare %R:path('') %output:xyz('abc') function m:f() {'9'};", "");
    // parameter must contain single string
    getE("declare %R:path('') %output:method function m:f() {'9'};", "");
    getE("declare %R:path('') %output:method('xml','html') function m:f() {'9'};", "");
  }

  /**
   * {@code %consumes} annotation.
   * @throws Exception exception
   */
  @Test public void consumes() throws Exception {
    // correct syntax
    get("declare %R:path('') %R:consumes('text/plain') function m:f() {1};", "", "1");
    get("declare %R:path('') %R:consumes('*/*') function m:f() {1};", "", "1");
    // duplicate annotations
    get("declare %R:path('') %R:consumes('text/plain','*/*') function m:f() {1};",
        "", "1");
    get("declare %R:path('') %R:consumes('text/plain') %R:consumes('*/*') " +
        "function m:f() {1};", "", "1");
    // invalid content type: ignored as no content type has been specified by user
    get("declare %R:path('') %R:consumes('X') function m:f() {1};", "", "1");
  }

  /**
   * {@code %produces} annotation.
   * @throws Exception exception
   */
  @Test public void produces() throws Exception {
    // correct syntax
    get("declare %R:path('') %R:produces('text/plain') function m:f() {1};", "", "1");
    get("declare %R:path('') %R:produces('*/*') function m:f() {1};", "", "1");
    // duplicate annotations
    get("declare %R:path('') %R:produces('text/plain','*/*') function m:f() {1};",
        "", "1");
    get("declare %R:path('') %R:produces('text/plain') %R:produces('*/*') " +
        "function m:f() {1};", "", "1");
    // invalid content type
    getE("declare %R:path('') %R:produces('X') function m:f() {1};", "");
  }

  /**
   * {@code %HEAD} method.
   * @throws Exception exception
   */
  @Test public void head() throws Exception {
    // correct return type
    head("declare %R:HEAD %R:path('') function m:f() { <R:response/> };", "", "");
    head("declare %R:HEAD %R:path('') function m:f() as element(R:response) " +
        "{ <R:response/> };", "", "");
    // wrong type
    headE("declare %R:HEAD %R:path('') function m:f() { () };", "");
    headE("declare %R:HEAD %R:path('') function m:f() { <response/> };", "");
    headE("declare %R:HEAD %R:path('') function m:f() as element(R:response)* {()};", "");
  }

  /**
   * Query parameters.
   * @throws Exception exception
   */
  @Test public void queryParams() throws Exception {
    // correct syntax
    get("declare %R:path('') %R:query-param('a','{$v}') " +
        "function m:f($v) {$v};", "?a=1", "1");
    get("declare %R:path('') %R:query-param('a','{$a}') " +
        "function m:f($a) {$a*2};", "?a=1", "2");
    get("declare %R:path('') %R:query-param('a','{$a}') " +
        "function m:f($a as xs:integer*) {count($a)};", "?a=4&a=8", "2");
    get("declare %R:path('') %R:query-param('a','{$v}',3) " +
        "function m:f($v) {$v};", "", "3");
    get("declare %R:path('') %R:query-param('a','{$v}',4,8) " +
        "function m:f($v) {count($v)};", "", "2");
    get("declare %R:path('') %R:query-param('a','{$a}') %R:query-param('b','{$b}') " +
        "function m:f($a,$b) {$a*$b};", "?a=2&b=3", "6");
    // missing assignment: default value is empty sequence
    get("declare %R:path('') %R:query-param('a','{$v}') " +
        "function m:f($v) {count($v)};", "", "0");
    // missing variable declaration
    getE("declare %R:path('') %R:query-param('a','{$a}') function m:f() {1};", "?a=2");
    // variable is specified more than once
    getE("declare %R:path('') %R:query-param('a','{$a}') %R:query-param('a','{$a}') " +
        "function m:f($a) {$a};", "?a=2");
    // parameter is no string
    getE("declare %R:path('') %R:query-param(1,'{$a}') function m:f($a) {$a};", "?a=2");
    // invalid path template
    getE("declare %R:path('') %R:query-param('a','$a') function m:f($a) {$a};", "?a=2");
    // invalid type cardinality
    getE("declare %R:path('') %R:query-param('a','{$a}') " +
        "function m:f($a as item()) {()};", "?a=4&a=8");
  }

  /**
   * Form parameters (same as %query-param if GET is used).
   * @throws Exception exception
   */
  @Test public void formParams() throws Exception {
    // correct syntax
    get("declare %R:path('') %R:form-param('a','{$v}') " +
        "function m:f($v) {$v};", "?a=1", "1");
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Executes the specified POST request and tests the result.
   * @param function function to test
   * @param query request
   * @param exp expected result
   * @param request request body
   * @param param post parameters
   * @throws IOException I/O exception
   */
  private static void post(final String function, final String query, final String exp,
      final String request, final String param) throws IOException {
    install(function);
    assertEquals(exp, post(query, request, param));
  }

  /**
   * Executes the specified GET request and tests the result.
   * @param function function to test
   * @param query request
   * @param exp expected result
   * @throws IOException I/O exception
   */
  private static void get(final String function, final String query, final String exp)
      throws IOException {
    install(function);
    assertEquals(exp, get(query));
  }

  /**
   * Executes the specified GET request and tests for an error.
   * @param function function to test
   * @param query request
   * @throws IOException I/O exception
   */
  private static void getE(final String function, final String query) throws IOException {
    install(function);
    try {
      get(query);
      fail("Error expected: " + query);
    } catch(final BaseXException ex) {
    }
  }

  /**
   * Executes the specified HEAD request and tests the result.
   * @param function function to test
   * @param query request
   * @param exp expected result
   * @throws IOException I/O exception
   */
  private static void head(final String function, final String query, final String exp)
      throws IOException {
    install(function);
    assertEquals(exp, head(query));
  }

  /**
   * Executes the specified HEAD request and tests for an error.
   * @param function function to test
   * @param query request
   * @throws IOException I/O exception
   */
  private static void headE(final String function, final String query)
      throws IOException {

    install(function);
    try {
      head(query);
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
    // delete old module
    final String path = CONTEXT.mprop.get(MainProp.HTTPPATH);
    for(final IOFile f : new IOFile(path).children()) assertTrue(f.delete());
    // create new module
    module().write(new TokenBuilder(HEADER).add(function).finish());
  }

  /**
   * Returns the XQuery test module.
   * @return test module
   */
  private static IOFile module() {
    final String path = CONTEXT.mprop.get(MainProp.HTTPPATH);
    return new IOFile(path, DB + count++ + IO.XQMSUFFIX);
  }
}
