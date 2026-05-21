package org.basex.http.rest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;

import org.basex.util.*;
import org.basex.util.http.MediaType;
import org.junit.jupiter.api.*;

/**
 * End-to-end test for the global-read / local-write deadlock in
 * {@link org.basex.core.locks.Locking}, triggered through real REST requests.
 *
 * A POST to the {@code <commands>} endpoint that bundles a globally reading command
 * ({@code <list/>}) with a command writing a named database ({@code db:create}) yields a job with
 * a global read lock and a local write lock at the same time (see {@link RESTCmd#addLocks()}).
 * Two such jobs, running concurrently with a pure local writer, used to deadlock in the locking
 * monitor.
 *
 * @author BaseX Team, BSD License
 */
public final class RESTDeadlockTest extends RESTTest {
  /** Database name of the pure local writer. */
  private static final String DB_P = "rest-deadlock-p";
  /** Database name of the first hybrid job. */
  private static final String DB_A = "rest-deadlock-a";
  /** Database name of the second hybrid job. */
  private static final String DB_B = "rest-deadlock-b";

  /**
   * Removes the databases created by the test.
   * @throws Exception arbitrary exception
   */
  @AfterEach public void cleanup() throws Exception {
    post("<commands><xquery>for $d in ('" + DB_P + "', '" + DB_A + "', '" + DB_B +
        "')[db:exists(.)] return db:drop($d)</xquery></commands>", MediaType.APPLICATION_XML, "");
  }

  /**
   * One pure local writer plus two hybrid jobs (global read and local write) run concurrently.
   * All three requests must complete; a hang indicates the locking deadlock.
   * @throws Exception exception
   */
  @Test @Timeout(60) public void globalReadLocalWriteDeadlock() throws Exception {
    final ExecutorService exec = Executors.newFixedThreadPool(3);
    try {
      // pure local writer; holds its write lock for two seconds
      final Future<?> p = exec.submit(() -> post("<commands><xquery>db:create('" + DB_P +
          "'), prof:sleep(2000)</xquery></commands>", MediaType.APPLICATION_XML, ""));

      // give the writer time to acquire its lock
      Thread.sleep(500);

      // two jobs that hold a global read lock and a local write lock at the same time
      final Future<?> a = exec.submit(() -> post("<commands><list/><xquery>db:create('" + DB_A +
          "')</xquery></commands>", MediaType.APPLICATION_XML, ""));
      final Future<?> b = exec.submit(() -> post("<commands><list/><xquery>db:create('" + DB_B +
          "')</xquery></commands>", MediaType.APPLICATION_XML, ""));

      // all requests must finish; on a deadlock the hybrid jobs never return
      for(final Future<?> future : new Future<?>[] { p, a, b }) {
        try {
          future.get(30, TimeUnit.SECONDS);
        } catch(final TimeoutException ex) {
          Util.debug(ex);
          fail("REST request did not complete: deadlock in Locking?");
        }
      }
    } finally {
      exec.shutdownNow();
    }
  }
}
