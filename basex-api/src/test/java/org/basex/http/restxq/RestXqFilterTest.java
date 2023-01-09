package org.basex.http.restxq;

import org.junit.jupiter.api.*;

/**
 * This test contains RESTXQ filters.
 *
 * @author BaseX Team 2005-23, BSD License
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
}
