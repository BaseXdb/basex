package org.basex.http.restxq;

import org.junit.jupiter.api.*;

/**
 * Tests that the HTTP request is reachable from a synchronous job but detached from an
 * asynchronous one, so a background job cannot access a recycled request.
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
   * {@code job:eval} runs on a detached context, so {@code request:*} is unavailable and the
   * query falls back to its error branch.
   * @throws Exception exception
   */
  @Test public void asyncJobDropsRequest() throws Exception {
    get("detached",
        "declare %R:path('eval') %output:method('text') function m:f() {" +
        "  let $id := job:eval(" + QUERY + ", map { }, map { 'cache': true() })" +
        "  return (job:wait($id), job:result($id)) };",
        "eval");
  }
}
