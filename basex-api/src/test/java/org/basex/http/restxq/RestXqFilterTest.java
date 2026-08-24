package org.basex.http.restxq;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.util.http.MediaType;
import org.junit.jupiter.api.*;

/**
 * This test contains RESTXQ filters.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RestXqFilterTest extends RestXqTest {
  /**
   * {@code %consumes} annotation.
   * @throws Exception exception
   */
  @Test public void consumes() throws Exception {
    get(404, "declare %R:path('') %R:consumes('text/plain') function m:f() { 1 };", "");
    get("1", "declare %R:path('') %R:consumes('*/*') function m:f() { 1 };", "");
    get(404, "declare %R:path('') %R:consumes('text/plain;bla=blu') function m:f() { 1 };", "");
  }

  /**
   * {@code %consumes} annotation.
   * @throws Exception exception
   */
  @Test public void consumesMultiple() throws Exception {
    get("1", "declare %R:path('') %R:consumes('text/plain', '*/*') function m:f() { 1 };", "");
    get("1", "declare %R:path('') %R:consumes('text/plain') %R:consumes('*/*')"
        + "function m:f() { 1 };", "");
    get("2", "declare %R:path('') %R:consumes('text/plain') function m:f() { 1 };"
        + "declare %R:path('') %R:consumes('*/*') function m:g() { 2 };", "");
  }

  /**
   * {@code %consumes} annotation.
   * @throws Exception exception
   */
  @Test public void consumesError() throws Exception {
    get(404, "declare %R:path('') %R:consumes('X') function m:f() { 1 };", "");
  }

  /**
   * Content types that are not consumed by an existing path.
   * @throws Exception exception
   */
  @Test public void unsupportedType() throws Exception {
    register("declare %R:POST %R:path('') %R:consumes('application/xml') function m:f() { 1 };");
    assertEquals("Unsupported content type: text/plain. Supported: application/xml.",
        post(415, "x", MediaType.TEXT_PLAIN, ""));
    assertEquals("1", post("<x/>", MediaType.APPLICATION_XML, ""));
  }

  /**
   * Media types that are not produced by an existing path.
   * @throws Exception exception
   */
  @Test public void notAcceptable() throws Exception {
    register("declare %R:path('') %R:produces('application/xml') function m:f() { 1 };");
    assertEquals("No acceptable media type. Supported: application/xml.",
        send(406, "GET", null, null, Map.of("Accept", "image/png"), ""));
    send(200, "GET", null, null, Map.of("Accept", "application/xml"), "");
  }

  /**
   * {@code %produces} annotation.
   * @throws Exception exception
   */
  @Test public void produces() throws Exception {
    get("1", "declare %R:path('') %R:produces('text/plain') function m:f() { 1 };", "");
    get("1", "declare %R:path('') %R:produces('*/*') function m:f() { 1 };", "");
    get("1", "declare %R:path('') %R:produces('text/plain;bla=blu') function m:f() { 1 };", "");
  }

  /**
   * {@code %produces} annotation.
   * @throws Exception exception
   */
  @Test public void producesMultiple() throws Exception {
    get("1", "declare %R:path('') %R:produces('text/plain', '*/*') function m:f() { 1 };", "");
    get("1", "declare %R:path('') %R:produces('text/plain') %R:produces('*/*') " +
            "function m:f() { 1 };", "");
  }

  /**
   * Invalid quality factors.
   * @throws Exception exception
   */
  @Test public void qualityFactor() throws Exception {
    register("declare %R:path('') %R:produces('text/plain;qs=5') function m:f() { 1 };");
    assertContains(get(500, ""), "Quality factor must be in the range 0-1: qs=5.");
  }

  /**
   * {@code <restxq:response/>} elements.
   * @throws Exception exception
   */
  @Test public void response() throws Exception {
    get("1", "declare %R:path('') function m:f() { <R:response/>, 1 };", "");
    get("<R:R xmlns:R=\"http://exquery.org/ns/restxq\"/>", "declare %R:path('')" +
        "function m:f() { <R:R/> };", "");
    get(200, "declare %R:path('') function m:f() { " +
        "<R:response><http:response/></R:response> };", "");
    get(200, "declare %R:path('') function m:f() { " +
        "<R:response><http:response status='200'/></R:response> };", "");
    get("OK", "declare %R:path('') function m:f() { " +
        "<R:response><http:response status='200' message='OK'/></R:response>, 'OK'};", "");
  }

  /**
   * Erroneous {@code <restxq:response/>} elements.
   * @throws Exception exception
   */
  @Test public void responseError() throws Exception {
    get(500, "declare %R:path('') function m:f() { " +
        "<R:response abc='x'/> };", "");
    get(500, "declare %R:path('') function m:f() { " +
        "<R:response>X</R:response> };", "");
    get(500, "declare %R:path('') function m:f() { " +
        "<R:response><X/></R:response> };", "");
    get(500, "declare %R:path('') function m:f() { " +
        "<R:response><http:response stat='200'/></R:response> };", "");
    get(500, "declare %R:path('') function m:f() { " +
        "<R:response><http:response>X</http:response></R:response> };", "");
  }

  /**
   * Status codes of {@code <http:response/>} elements.
   * @throws Exception exception
   */
  @Test public void responseStatus() throws Exception {
    final String f = "declare %R:path('') function m:f() { " +
        "<R:response><http:response status='CODE'/></R:response> };";
    get(200, f.replace("CODE", "200"), "");
    get(999, f.replace("CODE", "999"), "");
    // invalid status codes are rejected as in web:error
    register(f.replace("CODE", "abc"));
    assertContains(get(500, ""), "Invalid status code: abc");
    register(f.replace("CODE", "0"));
    assertContains(get(500, ""), "Invalid status code: 0");
    register(f.replace("CODE", "1000"));
    assertContains(get(500, ""), "Invalid status code: 1000");
  }
}
