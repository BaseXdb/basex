package org.basex.http.restxq;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.core.*;
import org.basex.util.http.*;
import org.junit.jupiter.api.*;

/**
 * This test contains RESTXQ methods.
 *
 * @author BaseX Team 2005-21, BSD License
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
    get("declare %R:method('GET') %R:path('') function m:f() {'x'};", "", "x");
    // standard HTTP method specified twice
    getE("declare %R:method('GET') %R:GET %R:path('') function m:f() {'x'};", "");
    // standard HTTP method without body, body provided in request
    getE("declare %R:method('GET', '{$b}') %R:path('') function m:f($b) {$b};", "");
    // standard HTTP method with body, body provided in request
    post("declare %R:method('POST', '{$b}') %R:path('') function m:f($b) {$b};", "12", "12",
        MediaType.TEXT_PLAIN);

    // ignore case
    get("declare %R:method('get') %R:path('') function m:f() {'x'};", "", "x");
    getE("declare %R:method('get') declare %R:method('GET') %R:path('') "
        + "function m:f() {'x'};", "");

    // custom HTTP method without body
    install("declare %R:method('RETRIEVE') %R:path('') function m:f() {'x'};");
    // java.net.HttpUrlConnection does not support custom HTTP methods
    // assertEquals("x", request("", "RETRIEVE"));

    // custom HTTP method with body
    install("declare %R:method('RETRIEVE', '{$b}') %R:path('') function m:f($b) {$b};");
    // java.net.HttpUrlConnection does not support custom HTTP methods
    // assertEquals("12", request("", "RETRIEVE", "12", MediaType.TEXT_PLAIN));

    // custom HTTP method specified twice
    final String q = "declare %R:method('RETRIEVE') %R:method('RETRIEVE') %R:path('') "
        + "function m:f() {'x'};";
    install(q);
    try {
      // java.net.HttpUrlConnection does not support custom HTTP methods
      request("", "RETRIEVE");
      fail("Error expected: " + q);
    } catch (final BaseXException ignored) {
    }
  }

  /**
   * {@code %HEAD} method.
   * @throws Exception exception
   */
  @Test public void head() throws Exception {
    // correct return type
    headR("declare %R:HEAD %R:path('') function m:f() { <R:response/> };");
    headR("declare %R:HEAD %R:path('') function m:f() as element(R:response) { <R:response/> };");
    // wrong type
    headE("declare %R:HEAD %R:path('') function m:f() { () };");
    headE("declare %R:HEAD %R:path('') function m:f() { <response/> };");
    headE("declare %R:HEAD %R:path('') function m:f() as element(R:response)* {()};");

    // correct return type
    headR("declare %R:GET %R:path('') function m:f() { () };");
    headR("declare %R:GET %R:path('') function m:f() { 1 to 5 };");
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
    install(function);
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
    install(function);
    assertEquals(exp, post("", request, type));
  }

  /**
   * Executes the specified HEAD request and tests the result.
   * @param function function to test
   * @throws IOException I/O exception
   */
  private static void headR(final String function) throws IOException {
    install(function);
    assertEquals("", head(""));
  }

  /**
   * Executes the specified HEAD request and tests for an error.
   * @param function function to test
   * @throws IOException I/O exception
   */
  private static void headE(final String function) throws IOException {
    install(function);
    try {
      head("");
      fail("Error expected: " + "");
    } catch(final BaseXException ignored) {
    }
  }
}
