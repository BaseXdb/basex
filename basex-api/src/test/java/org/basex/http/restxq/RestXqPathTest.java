package org.basex.http.restxq;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;

/**
 * This test contains RESTXQ paths.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class RestXqPathTest extends RestXqTest {
  /**
   * Retrieves the root.
   * @throws Exception exception
   */
  @Test
  public void get() throws Exception {
    // ignore duplicate slashes
    get("declare %R:path('') function m:f() { 'root' };", "", "root");
    get("declare %R:path('') function m:f() { 'root' };", "/", "root");
    get("declare %R:path('') function m:f() { 'root' };", "//", "root");
    get("declare %R:path('') function m:f() { 'root' };", "/////", "root");
    get("declare %R:path('/') function m:f() { 'root' };", "", "root");
    get("declare %R:path('/') function m:f() { 'root' };", "/", "root");
    // explicit GET method
    get("declare %R:GET %R:path('') function m:f() { 'root' };", "", "root");
    // duplicate GET method
    getE("declare %R:GET %R:GET %R:path('') function m:f() { 'root' };", "");
  }

  /**
   * Retrieves paths.
   * @throws Exception exception
   */
  @Test
  public void getTest() throws Exception {
    get("declare %R:path('/test') function m:f() {'ok'};", "test", "ok");
  }

  /**
   * Retrieves paths with variables.
   * @throws Exception exception
   */
  @Test
  public void getVariable() throws Exception {
    final String f = "declare %R:path('/var/{$x}') function m:f($x) {$x};";
    get(f, "var/x", "x");
    get(f, "var/y", "y");
  }

  /**
   * Retrieves paths with namespace declarations.
   * @throws Exception exception
   */
  @Test
  public void getVariableNS() throws Exception {
    get("declare default element namespace 'X';" +
        "declare %R:path('{$x}') function m:f($x) {$x};", "z", "z");
    get("declare %R:path('{$m:x}') function m:f($m:x) {$m:x};", "z", "z");
  }

  /**
   * Retrieves path with variables on root level.
   * @throws Exception exception
   */
  @Test
  public void getRootVariable() throws Exception {
    final String f = "declare %R:path('{$x}/a/b/c') function m:f($x) {$x};";
    get(f, "x/a/b/c", "x");
    // wrong path
    getE(f, "x/a/b/d");
  }

  /**
   * Retrieves paths with typed variables.
   * @throws Exception exception
   */
  @Test
  public void getInteger() throws Exception {
    final String f = "declare %R:path('/{$x}') function m:f($x as xs:int) {$x};";
    get(f, "2", "2");
    getE(f, "StRiNg");
  }

  /**
   * Retrieves path with multiple variables.
   * @throws Exception exception
   */
  @Test
  public void getMultiply() throws Exception {
    final String f = "declare %R:path('{$x}/{$y}') function " +
        "m:f($x as xs:integer,$y as xs:integer) {$x*$y};";
    get(f, "2/3", "6");
    getE(f, "2/x");
  }

  /**
   * Retrieves path with encoded URI.
   * @throws Exception exception
   */
  @Test
  public void getEncodedURI() throws Exception {
    get("declare %R:path('%7b') function m:f() {1};", "%7b", "1");
    get("declare %R:path('%7b') function m:f() {1};", "%7B", "1");
    get("declare %R:path('%7B') function m:f() {1};", "%7b", "1");
    get("declare %R:path('%7C') function m:f() {1};", "%7C", "1");
    get("declare %R:path('+') function m:f() {1};", "+", "1");
    get("declare %R:path('+') function m:f() {1};", "%20", "1");
    get("declare %R:path('%20') function m:f() {1};", "+", "1");
    getE("declare %R:path('%F') function m:f() {1};", "");
    getE("declare %R:path('%') function m:f() {1};", "");
  }

  /**
   * Checks if undeclared functions are reported.
   */
  @Test
  public void unknownFunction() {
    try {
      get("declare function m:foo($x) { $x };" +
          "declare %R:path('') function m:f() { m:foo() };", "", "");
      fail("Unknown function 'm:foo()' should not be found.");
    } catch(final IOException ex) {
      assertTrue(ex.getMessage().contains("XPST0017"));
    }
  }

  /**
   * {@code %path} annotation.
   * @throws Exception exception
   */
  @Test
  public void pathAnn() throws Exception {
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
  @Test
  public void pathVar() throws Exception {
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

    // regular expression
    get("declare %R:path('p/{$x=.+}') function m:f($x) {$x};", "p/a/b/c", "a/b/c");
    get("declare %R:path('p/{$x=[0-9]+}') function m:f($x) {$x};", "p/123", "123");
    getE("declare %R:path('p/{$x=[0-9]+}') function m:f($x) {$x};", "p/123a");
    get("declare %R:path('{$a=\\d+}{$b=\\w+}{$c=\\d}') function m:f($a,$b,$c) {$c||$b||$a};",
        "12ab3", "3ab12");
    getE("declare %R:path('{$a=\\d+}{$b=\\w+}{$c=\\d}') function m:f($a,$b,$c) {$c||$b||$a};",
        "12ab3x");
  }

  /**
   * Various annotations.
   * @throws Exception exception
   */
  @Test
  public void various() throws Exception {
    // correct syntax
    get("declare %R:path('') function m:f() {'x'};", "", "x");
    // invalid annotation
    getE("declare %R:path('') %R:xyz function m:f() {'x'};", "");
  }
}
