package org.basex.http.restxq;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.io.in.*;
import org.basex.util.http.*;
import org.junit.jupiter.api.*;

/**
 * This test contains RESTXQ methods.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class RestXqMethodTest extends RestXqTest {
  /**
   * Retrieve path with typed variable.
   * @throws Exception exception
   */
  @Test public void post() throws Exception {
    // text
    String f = "declare %R:POST('{$x}') %R:path('') function m:f($x) {$x};";
    post(f, "12", "12", MediaType.TEXT_PLAIN);
    post(f, "<x>A</x>", "<x>A</x>", MediaType.APPLICATION_XML);
    // json
    f = "declare %R:POST('{$x}') %R:path('') function m:f($x) {$x/json/*};";
    post(f, "<A>B</A>", "{ \"A\":\"B\" }", MediaType.APPLICATION_JSON);
    // csv
    f = "declare %R:POST('{$x}') %R:path('') function m:f($x) {$x/csv/*/*};";
    post(f, "<entry>A</entry>", "A", MediaType.TEXT_CSV);
    // binary
    f = "declare %R:POST('{$x}') %R:path('') function m:f($x) {$x};";
    post(f, "AAA", "AAA", MediaType.APPLICATION_OCTET_STREAM);
    post(f, "AAA", "AAA", new MediaType("whatever/type"));
  }

  /**
   * Custom method.
   * @throws Exception exception */
  @Test public void method() throws Exception {
    // standard HTTP method without body
    get("x", "declare %R:method('GET') %R:path('') function m:f() {'x'};", "");
    // standard HTTP method specified twice
    get(500, "declare %R:method('GET') %R:GET %R:path('') function m:f() {'x'};", "");
    // standard HTTP method without body, body provided in request
    get(500, "declare %R:method('GET', '{$b}') %R:path('') function m:f($b) {$b};", "");
    // standard HTTP method with body, body provided in request
    post("declare %R:method('POST', '{$b}') %R:path('') function m:f($b) {$b};", "12", "12",
        MediaType.TEXT_PLAIN);

    // ignore case
    get("x", "declare %R:method('get') %R:path('') function m:f() {'x'};", "");
    get(500, "declare %R:method('get') declare %R:method('GET') %R:path('') "
        + "function m:f() {'x'};", "");

    // custom HTTP method without body
    register("declare %R:method('RETRIEVE') %R:path('') function m:f() {'x'};");
    assertEquals("x", send(200, "RETRIEVE", null, null, ""));

    // custom HTTP method with body
    register("declare %R:method('RETRIEVE', '{$b}') %R:path('') function m:f($b) {$b};");
    assertEquals("12", send(200, "RETRIEVE", new ArrayInput("12"), MediaType.TEXT_PLAIN, ""));

    // custom HTTP method specified twice
    register("declare %R:method('RETRIEVE') %R:method('RETRIEVE') %R:path('') "
        + "function m:f() {'x'};");
    send(500, "RETRIEVE", null, null, "");
  }

  /**
   * {@code %HEAD} method.
   * @throws Exception exception
   */
  @Test public void head() throws Exception {
    // correct return type
    head("declare %R:HEAD %R:path('') function m:f() { <R:response/> };");
    head("declare %R:HEAD %R:path('') function m:f() as element(R:response) { <R:response/> };");
    // wrong type
    headError("declare %R:HEAD %R:path('') function m:f() { () };");
    headError("declare %R:HEAD %R:path('') function m:f() { <response/> };");
    headError("declare %R:HEAD %R:path('') function m:f() as element(R:response)* {()};");

    // correct return type
    head("declare %R:GET %R:path('') function m:f() { () };");
    head("declare %R:GET %R:path('') function m:f() { 1 to 5 };");
  }

  /**
   * {@code %OPTIONS} method.
   * @throws Exception exception
   */
  @Test public void options() throws Exception {
    options("declare %R:OPTIONS %R:path('') function m:f() { };", "");
    options("declare %R:OPTIONS %R:path('') function m:f() { 1 };", "1");

    options("declare %R:GET %R:path('') function m:f() { <R:response/> };", "");
    options("declare %R:GET %R:path('sdfdfs') function m:f() { <R:response/> };", "");
  }

  /**
   * Executes the specified OPTIONS request and tests the result.
   * @param function function to test
   * @param exp expected result
   * @throws IOException I/O exception
   */
  private static void options(final String function, final String exp) throws IOException {
    register(function);
    assertEquals(exp, options(""));
  }

  /**
   * Executes the specified OPTIONS request and tests the result.
   * @param function function to test
   * @param exp expected result
   * @param request request body
   * @param type media type
   * @throws IOException I/O exception
   */
  private static void post(final String function, final String exp, final String request,
      final MediaType type) throws IOException {
    register(function);
    assertEquals(exp, post(request, type, ""));
  }

  /**
   * Executes the specified HEAD request and tests the result.
   * @param function function to test
   * @throws Exception exception
   */
  private static void head(final String function) throws Exception {
    register(function);
    assertEquals("", head(200, ""));
  }

  /**
   * Executes the specified HEAD request and tests for an error.
   * @param function function to test
   * @throws Exception exception
   */
  private static void headError(final String function) throws Exception {
    register(function);
    head(500, "");
  }
}
