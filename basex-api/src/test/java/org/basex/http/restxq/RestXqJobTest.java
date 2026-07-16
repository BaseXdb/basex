package org.basex.http.restxq;

import org.junit.jupiter.api.*;

/**
 * Tests that request values are accessible from synchronous and asynchronous jobs.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RestXqJobTest extends RestXqTest {
  /** Query that returns the HTTP method, or {@code "detached"} if no request is reachable. */
  private static final String QUERY =
    "'try { Q{http://exquery.org/ns/request}method() } catch * { \"detached\" }'";

  /**
   * {@code job:execute} runs synchronously while the request is live, so {@code request:*} works.
   * @throws Exception exception
   */
  @Test public void syncJobKeepsRequest() throws Exception {
    get("GET",
        "declare %R:path('exec') %output:method('text') function m:f() {" +
        "  job:execute(" + QUERY + ") };",
        "exec");
  }

  /**
   * {@code job:eval} runs on a detached context with captured request values.
   * @throws Exception exception
   */
  @Test public void asyncJobKeepsRequest() throws Exception {
    get("GET",
        "declare %R:path('eval') %output:method('text') function m:f() {" +
        "  let $id := job:eval(" + QUERY + ", map { }, map { 'cache': true() })" +
        "  return (job:wait($id), job:result($id)) };",
        "eval");
  }

  /**
   * Headers are captured for asynchronous jobs; lookup is case-insensitive.
   * @throws Exception exception
   */
  @Test public void asyncJobKeepsHeaders() throws Exception {
    get("true",
        "declare %R:path('header') %output:method('text') function m:f() {" +
        "  let $id := job:eval('" +
        "    let $h := Q{http://exquery.org/ns/request}header(\"Host\")" +
        "    return string(exists($h) and " +
        "      $h = Q{http://exquery.org/ns/request}header(\"HOST\"))'," +
        "    {}, { 'cache': true() })" +
        "  return (job:wait($id), job:result($id)) };",
        "header");
  }
}
