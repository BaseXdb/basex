package org.basex.http.restxq;

import org.junit.jupiter.api.*;

/**
 * This test contains RESTXQ filters.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class RestXqFilterTest extends RestXqTest {
  /**
   * {@code %consumes} annotation.
   * @throws Exception exception
   */
  @Test public void consumes() throws Exception {
    // correct syntax
    getE("declare %R:path('') %R:consumes('text/plain') function m:f() { 1 };", "");
    get("declare %R:path('') %R:consumes('*/*') function m:f() { 1 };", "", "1");
    getE("declare %R:path('') %R:consumes('text/plain;bla=blu') function m:f() { 1 };", "");

    // multiple types
    get("declare %R:path('') %R:consumes('text/plain','*/*') function m:f() { 1 };", "", "1");
    get("declare %R:path('') %R:consumes('text/plain') %R:consumes('*/*') function m:f() { 1 };",
        "", "1");
    get("declare %R:path('') %R:consumes('text/plain') function m:f() { 1 };" +
        "declare %R:path('') %R:consumes('*/*') function m:g() { 2 };",
        "", "2");

    // invalid content type: ignored as no content type has been specified by user
    getE("declare %R:path('') %R:consumes('X') function m:f() { 1 };", "");
  }

  /**
   * {@code %produces} annotation.
   * @throws Exception exception
   */
  @Test public void produces() throws Exception {
    // correct syntax
    get("declare %R:path('') %R:produces('text/plain') function m:f() { 1 };", "", "1");
    get("declare %R:path('') %R:produces('*/*') function m:f() { 1 };", "", "1");
    get("declare %R:path('') %R:produces('text/plain;bla=blu') function m:f() { 1 };", "", "1");
    // multiple types
    get("declare %R:path('') %R:produces('text/plain','*/*') function m:f() { 1 };", "", "1");
    get("declare %R:path('') %R:produces('text/plain') %R:produces('*/*') " +
        "function m:f() { 1 };", "", "1");
  }

  /**
   * {@code <restxq:response/>} elements.
   * @throws Exception exception
   */
  @Test public void response() throws Exception {
    get("declare %R:path('') function m:f() { <R:response/>, 1 };", "", "1");
    get("declare %R:path('') function m:f() { <R:R/> };", "",
        "<R:R xmlns:R=\"http://exquery.org/ns/restxq\"/>");
    get("declare %R:path('') function m:f() { " +
        "<R:response><http:response/></R:response> };", "", "");
    get("declare %R:path('') function m:f() { " +
        "<R:response><http:response status='200'/></R:response> };", "", "");
    get("declare %R:path('') function m:f() { " +
        "<R:response><http:response status='200' message='OK'/></R:response>, 'OK'};", "", "OK");

    // wrong syntax
    getE("declare %R:path('') function m:f() { " +
        "<R:response abc='x'/> };", "");
    getE("declare %R:path('') function m:f() { " +
        "<R:response>X</R:response> };", "");
    getE("declare %R:path('') function m:f() { " +
        "<R:response><X/></R:response> };", "");
    getE("declare %R:path('') function m:f() { " +
        "<R:response><http:response stat='200'/></R:response> };", "");
    getE("declare %R:path('') function m:f() { " +
        "<R:response><http:response>X</http:response></R:response> };", "");
  }
}
