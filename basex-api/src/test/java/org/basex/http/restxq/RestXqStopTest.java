package org.basex.http.restxq;

import static org.basex.http.web.WebText.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;

import org.basex.core.jobs.*;
import org.basex.http.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for interrupting RESTXQ queries.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RestXqStopTest extends RestXqTest {
  /**
   * Disconnect test.
   * @throws Exception exception
   */
  @Test public void clientDisconnect() throws Exception {
    // function producing a long stream of output
    register("declare %R:path('2679') function m:f() {" +
        "(1 to 1000000000) ! (prof:sleep(1), string-join((1 to 1000) ! 'x')) };");

    final JobPool jobs = HTTPContext.get().context().jobs;
    final Job job;
    try(Socket socket = new Socket("localhost", HTTP_PORT)) {
      // send a raw GET request and start streaming the response
      final OutputStream out = socket.getOutputStream();
      out.write(Token.token("GET /2679 HTTP/1.1\r\nHost: localhost:" + HTTP_PORT + "\r\n\r\n"));
      out.flush();
      // wait until the RESTXQ query has been registered and is running
      job = waitForQuery(jobs);
      assertFalse(job.stopped(), "Query was stopped before the client disconnected.");
    }

    // the socket is closed: the next serialization write fails, and the query must be stopped
    final long end = System.currentTimeMillis() + 10000;
    while(!job.stopped() && System.currentTimeMillis() < end) Performance.sleep(10);
    assertTrue(job.stopped(), "Query was not stopped after the client disconnected.");
  }

  /**
   * Waits until a RESTXQ query shows up in the job pool.
   * @param jobs job pool
   * @return running query job
   */
  private static Job waitForQuery(final JobPool jobs) {
    final long end = System.currentTimeMillis() + 5000;
    while(System.currentTimeMillis() < end) {
      for(final Job job : jobs.active.values()) {
        if(RESTXQ.equals(job.jc().type())) return job;
      }
      Performance.sleep(10);
    }
    throw new AssertionError("RESTXQ query was not started.");
  }
}
