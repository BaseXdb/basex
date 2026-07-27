package org.basex.http.restxq;

import org.basex.util.http.MediaType;
import org.junit.jupiter.api.*;

/**
 * This test contains RESTXQ filters.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RestXqInputTest extends RestXqTest {
  /**
   * JSON: {@code %input} annotation.
   * @throws Exception exception
   */
  @Test public void jsonInput() throws Exception {
    post("<A__B/>", "declare %R:POST('{$x}') %R:path('') %input:json('lax=no') "
        + "function m:f($x) {$x/*/*};", "", "{ \"A_B\": \"\" }", MediaType.APPLICATION_JSON);
    post("<A_B/>", "declare %R:POST('{$x}') %R:path('') %input:json('lax=true') "
        + "function m:f($x) {$x/*/*};", "", "{ \"A_B\": \"\" }", MediaType.APPLICATION_JSON);
  }

  /**
   * JSON: content-type parameters.
   * @throws Exception exception
   */
  @Test public void jsonContentType() throws Exception {
    post("<A__B/>", "declare %R:POST('{$x}') %R:path('') function m:f($x) {$x/*/*};", "",
        "{ \"A_B\": \"\" }",
        new MediaType(MediaType.APPLICATION_JSON + ";lax=false"));
    post("<A_B/>", "declare %R:POST('{$x}') %R:path('') function m:f($x) {$x/*/*};", "",
        "{ \"A_B\": \"\" }", new MediaType(MediaType.APPLICATION_JSON + ";lax=yes"));
  }

  /**
   * JSON.
   * @throws Exception exception
   */
  @Test public void json() throws Exception {
    post("<A__B/>", "declare %R:POST('{$x}') %R:path('') function m:f($x) {$x/*/*};", "",
        "{ \"A_B\": \"\" }", MediaType.APPLICATION_JSON);
  }

  /**
   * CSV: {@code %input} annotation.
   * @throws Exception exception
   */
  @Test public void csvInput() throws Exception {
    // test input annotation
    post("", "declare %R:POST('{$x}') %R:path('') %input:csv('header=no') "
        + "function m:f($x) {$x//A};", "", "A\n1", MediaType.TEXT_CSV);
    post("<A>1</A>", "declare %R:POST('{$x}') %R:path('') %input:csv('header=yes') "
        + "function m:f($x) {$x//A};", "", "A\n1", MediaType.TEXT_CSV);
  }

  /**
   * CSV: content-type parameters.
   * @throws Exception exception
   */
  @Test public void csvContentType() throws Exception {
    post("", "declare %R:POST('{$x}') %R:path('') function m:f($x) {$x//A};", "",
        "A\n1", new MediaType(MediaType.TEXT_CSV + ";header=no"));
    post("<A>1</A>", "declare %R:POST('{$x}') %R:path('') function m:f($x) {$x//A};", "",
        "A\n1", new MediaType(MediaType.TEXT_CSV + ";header=yes"));
  }

  /**
   * CSV.
   * @throws Exception exception
   */
  @Test public void csv() throws Exception {
    post("", "declare %R:POST('{$x}') %R:path('') function m:f($x) {$x//A};", "",
        "A\n1", MediaType.TEXT_CSV);
  }

  /**
   * XML: entity-expansion bomb in the request body must be rejected, not expanded.
   * @throws Exception exception
   */
  @Test public void xmlEntityExpansion() throws Exception {
    register("declare %R:POST('{$x}') %R:path('') function m:f($x) { $x };");
    // bounded 'billion laughs' (~111k expansions, exceeding the JDK default limit)
    final String bomb = "<?xml version='1.0'?>\n"
        + "<!DOCTYPE x [\n"
        + "<!ENTITY a 'aaaaaaaaaa'>\n"
        + "<!ENTITY b '&a;&a;&a;&a;&a;&a;&a;&a;&a;&a;'>\n"
        + "<!ENTITY c '&b;&b;&b;&b;&b;&b;&b;&b;&b;&b;'>\n"
        + "<!ENTITY d '&c;&c;&c;&c;&c;&c;&c;&c;&c;&c;'>\n"
        + "<!ENTITY e '&d;&d;&d;&d;&d;&d;&d;&d;&d;&d;'>\n"
        + "<!ENTITY f '&e;&e;&e;&e;&e;&e;&e;&e;&e;&e;'>\n"
        + "]>\n<x>&f;</x>";
    post(400, bomb, MediaType.APPLICATION_XML, "");
  }
}
