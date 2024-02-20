package org.basex.http.restxq;

import org.junit.jupiter.api.*;

/**
 * This test contains RESTXQ paths.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class RestXqPathTest extends RestXqTest {
  /**
   * Retrieves the root.
   * @throws Exception exception
   */
  @Test public void get() throws Exception {
    // root access
    get("root", "declare %R:path('') function m:f() { 'root' };", "");
    get("root", "declare %R:path('/') function m:f() { 'root' };", "");
    // explicit GET method
    get("root", "declare %R:GET %R:path('') function m:f() { 'root' };", "");
    // duplicate GET method
    get(500, "declare %R:GET %R:GET %R:path('') function m:f() { 'root' };", "");
  }

  /**
   * Retrieves paths.
   * @throws Exception exception
   */
  @Test public void getTest() throws Exception {
    get("ok", "declare %R:path('/test') function m:f() {'ok'};", "test");
  }

  /**
   * Retrieves paths with variables.
   * @throws Exception exception
   */
  @Test public void getVariable() throws Exception {
    final String f = "declare %R:path('/var/{$x}') function m:f($x) {$x};";
    get("x", f, "var/x");
    get("y", f, "var/y");
  }

  /**
   * Retrieves paths with namespace declarations.
   * @throws Exception exception
   */
  @Test public void getVariableNS() throws Exception {
    get("z", "declare default element namespace 'X';" +
            "declare %R:path('{$x}') function m:f($x) {$x};", "z");
    get("z", "declare %R:path('{$m:x}') function m:f($m:x) {$m:x};", "z");
  }

  /**
   * Retrieves path with variables on root level.
   * @throws Exception exception
   */
  @Test public void getRootVariable() throws Exception {
    final String f = "declare %R:path('{$x}/a/b/c') function m:f($x) {$x};";
    get("x", f, "x/a/b/c");
    get(404, f, "x/a/b/d");
  }

  /**
   * Retrieves paths with typed variables.
   * @throws Exception exception
   */
  @Test public void getInteger() throws Exception {
    final String f = "declare %R:path('/{$x}') function m:f($x as xs:int) {$x};";
    get("2", f, "2");
    get(500, f, "StRiNg");
  }

  /**
   * Retrieves path with multiple variables.
   * @throws Exception exception
   */
  @Test public void getMultiply() throws Exception {
    final String f = "declare %R:path('{$x}/{$y}') function " +
        "m:f($x as xs:integer,$y as xs:integer) {$x*$y};";
    get("6", f, "2/3");
    get(500, f, "2/x");
  }

  /**
   * Retrieves path with encoded URI.
   * @throws Exception exception
   */
  @Test public void getEncodedURI() throws Exception {
    get("1", "declare %R:path('%7b') function m:f() {1};", "%7b");
    get("1", "declare %R:path('%7b') function m:f() {1};", "%7B");
    get("1", "declare %R:path('%7B') function m:f() {1};", "%7b");
    get("1", "declare %R:path('%7C') function m:f() {1};", "%7C");
    get("1", "declare %R:path('+') function m:f() {1};", "+");
    get("1", "declare %R:path(' ') function m:f() {1};", "%20");
    get("1", "declare %R:path('%2b') function m:f() {1};", "+");
    get("1", "declare %R:path('%20') function m:f() {1};", "%20");
    get(500, "declare %R:path('%F') function m:f() {1};", "");
    get(500, "declare %R:path('%') function m:f() {1};", "");
  }

  /**
   * Checks if undeclared functions are reported.
   * @throws Exception exception
   */
  @Test public void unknownFunction() throws Exception {
    get(500, "declare %R:path('') function m:f() { m:foo() };", "");
  }

  /**
   * {@code %path} annotation.
   * @throws Exception exception
   */
  @Test public void pathAnn() throws Exception {
    // correct syntax
    get(200, "declare %R:path('') function m:f() {()};", "");
    // no path annotation
    get(500, "declare %R:GET function m:f() {()};", "");
    // no path argument
    get(500, "declare %R:path function m:f() {()};", "");
    // empty path argument
    get(500, "declare %R:path() function m:f() {()};", "");
    // two path arguments
    get(500, "declare %R:path('a', 'b') function m:f() {()};", "a");
    get(500, "declare %R:path('a') %R:path('b') function m:f() {()};", "a");
    // path not found
    get(404, "declare %R:path('') function m:f() { 1 };", "X");
    get(404, "declare %R:path('a') function m:f() { 1 };", "");
  }

  /**
   * {@code %path} segments.
   * @throws Exception exception
   */
  @Test public void pathVar() throws Exception {
    // correct syntax
    get("1", "declare %R:path('{$x}') function m:f($x) {$x};", "1");
    // invalid variable definitions
    get(500, "declare %R:path('{a}') function m:f() {()};", "a");
    get(500, "declare %R:path('{ $a }') function m:f() {()};", "a");
    // invalid variable name
    get(500, "declare %R:path('{$x::x}') function m:f() {()};", "a");
    get(500, "declare %R:path('{$x x}') function m:f() {()};", "a");
    // missing argument
    get(500, "declare %R:path('{$x}') function m:f() {()};", "a");
    // variable in template specified twice
    get(500, "declare %R:path('{$x}/{$x}') function m:f($x) {()};", "a");
    // variable in template missing
    get(500, "declare %R:path('') function m:f($x) {()};", "");
    // variable must inherit xs:anyAtomicType
    get(500, "declare %R:path('{$x}') function m:f($x as node()) {$x};", "1");
  }

  /**
   * Paths with regular expressions.
   * @throws Exception exception
   */
  @Test public void regex() throws Exception {
    get("a/b/c", "declare %R:path('p/{$x=.+}') function m:f($x) { $x };", "p/a/b/c");
    get("123", "declare %R:path('p/{$x=[0-9]+}') function m:f($x) { $x };", "p/123");
    get(404, "declare %R:path('p/{$x=[0-9]+}') function m:f($x) { $x };", "p/123a");
    get("3ab12",
        "declare %R:path('{$a=\\d+}{$b=\\w+}{$c=\\d}') " +
            "function m:f($a, $b, $c) { $c || $b || $a };", "12ab3");
    get(404,
        "declare %R:path('{$a=\\d+}{$b=\\w+}{$c=\\d}') " +
            "function m:f($a, $b, $c) { $c || $b || $a };", "12ab3x");

    get("2",
        "declare %R:path('{$p=.+}') function m:f1($p) { 1 }; " +
            "declare %R:path('{$p=.+}/x') function m:f2($p) { 2 };", "1/x");
    get("1",
        "declare %R:path('{$p=.+}') function m:f1($p) { 1 }; " +
            "declare %R:path('{$p=.+}/x') function m:f2($p) { 2 };", "1");
  }

  /**
   * Various annotations.
   * @throws Exception exception
   */
  @Test public void various() throws Exception {
    // correct syntax
    get("x", "declare %R:path('') function m:f() {'x'};", "");
    // invalid annotation
    get(500, "declare %R:path('') %R:xyz function m:f() {'x'};", "");
  }
}
