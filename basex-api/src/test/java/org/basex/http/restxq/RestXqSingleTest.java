package org.basex.http.restxq;

import static org.basex.http.web.WebText.*;
import static org.junit.jupiter.api.Assertions.*;

import java.net.*;
import java.net.http.*;
import java.util.concurrent.atomic.*;

import org.basex.core.jobs.*;
import org.basex.http.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the {@code %rest:single} annotation, which stops a running query when the same
 * function is requested again within the same session.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RestXqSingleTest extends RestXqTest {
  /**
   * A second request to a singleton function stops the running one, which is then rejected
   * with the HTTP status code 460.
   * @throws Exception exception
   */
  @Test public void supersede() throws Exception {
    // duration of the function is controlled via a query parameter
    register("declare %R:path('single') %R:query-param('ms', '{$ms}') %R:single " +
        "%output:method('text') function m:f($ms as xs:integer) {" +
        "  (1 to $ms) ! prof:sleep(1), 'done' };");

    final JobPool jobs = HTTPContext.get().context().jobs;
    // shared cookie handler: keeps the HTTP session alive across requests
    final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).
        cookieHandler(new CookieManager()).build();

    // initial request: establishes the session and returns its cookie to the client
    assertEquals("done", call(client, 0));
    waitWhileRunning(jobs);

    // long-running request (same session): it will be superseded
    final AtomicInteger status = new AtomicInteger();
    final Thread bg = new Thread(() -> {
      try {
        status.set(request(client, 100000000).statusCode());
      } catch(final Exception ex) {
        Util.debug(ex);
        status.set(-1);
      }
    });
    bg.start();

    // wait until the long-running query has been registered
    final Job job = waitForRunning(jobs);

    // second request (same session): supersedes and stops the running query
    assertEquals("done", call(client, 0));

    bg.join(10000);
    assertTrue(job.stopped(), "Running query was not stopped.");
    assertEquals(460, status.get(), "Superseded request was not rejected with 460.");
  }

  /**
   * Sends a request and checks for a successful response.
   * @param client HTTP client
   * @param ms requested duration of the query
   * @return response body
   * @throws Exception exception
   */
  private static String call(final HttpClient client, final int ms) throws Exception {
    final HttpResponse<String> response = request(client, ms);
    assertEquals(200, response.statusCode());
    return response.body().trim();
  }

  /**
   * Sends a request to the singleton function.
   * @param client HTTP client
   * @param ms requested duration of the query
   * @return response
   * @throws Exception exception
   */
  private static HttpResponse<String> request(final HttpClient client, final int ms)
      throws Exception {
    final HttpRequest request = HttpRequest.newBuilder(
        URI.create(HTTP_ROOT + "single?ms=" + ms)).GET().build();
    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  /**
   * Waits until a RESTXQ query shows up in the job pool.
   * @param jobs job pool
   * @return running query job
   */
  private static Job waitForRunning(final JobPool jobs) {
    final long end = System.currentTimeMillis() + 5000;
    while(System.currentTimeMillis() < end) {
      final Job job = running(jobs);
      if(job != null) return job;
      Performance.sleep(10);
    }
    throw new AssertionError("RESTXQ query was not started.");
  }

  /**
   * Waits until no RESTXQ query is running anymore.
   * @param jobs job pool
   */
  private static void waitWhileRunning(final JobPool jobs) {
    final long end = System.currentTimeMillis() + 5000;
    while(running(jobs) != null && System.currentTimeMillis() < end) Performance.sleep(10);
  }

  /**
   * Returns a running RESTXQ query from the job pool.
   * @param jobs job pool
   * @return query job, or {@code null}
   */
  private static Job running(final JobPool jobs) {
    for(final Job job : jobs.active.values()) {
      if(RESTXQ.equals(job.jc().type())) return job;
    }
    return null;
  }
}
