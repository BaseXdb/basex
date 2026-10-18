package org.basex.http.restxq;

import org.basex.util.http.MediaType;
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

  /**
   * Finished requests are listed for a while, with their path.
   * @throws Exception exception
   */
  @Test public void listed() throws Exception {
    register("declare %R:path('id') %output:method('text') function m:id() { job:current() };" +
        "declare %R:path('details/{$id}') %output:method('text') function m:details($id) {" +
        "  job:list-details($id) ! `{ @state } { @type } { @path }" +
        " { normalize-space(substring-after(., ':)')) }`" +
        "};");
    final String id = get(200, "id");
    get("finished RESTXQ /id m:id#0", "details/" + id);
  }

  /**
   * Bodies that have not been read by the request are empty in asynchronous jobs.
   * @throws Exception exception
   */
  @Test public void asyncJobUnreadBody() throws Exception {
    post("0",
        "declare %R:path('body') %R:POST %output:method('text') function m:f() {" +
        "  let $id := job:eval('count(Q{http://exquery.org/ns/request}body())'," +
        "    {}, { 'cache': true() })" +
        "  return (job:wait($id), job:result($id)) };",
        "body", "abc", MediaType.APPLICATION_OCTET_STREAM);
  }
}
