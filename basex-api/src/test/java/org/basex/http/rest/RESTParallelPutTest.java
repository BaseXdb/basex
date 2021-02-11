package org.basex.http.rest;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.http.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class sends parallel PUT requests to the REST API.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class RESTParallelPutTest extends HTTPTest {
  /** Client count. */
  private static final int CLIENTS = 10;
  /** Runs per client. */
  private static final int RUNS = 10;
  /** Failed string. */
  private static String failed;

  // INITIALIZERS =================================================================================

  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    init(REST_ROOT, true);
  }

  // TEST METHODS =================================================================================

  /**
   * Concurrency test.
   * @throws Exception exception
   */
  @Test public void test() throws Exception {
    put(NAME, new ArrayInput(""));

    // start and join concurrent clients
    final Client[] clients = new Client[CLIENTS];
    final int cs = clients.length;
    for(int i = 0; i < cs; i++) clients[i] = new Client();
    for(final Client c : clients) c.start();
    for(final Client c : clients) c.join();

    delete(NAME);
    if(failed != null) fail(failed);
  }

  /** Client class. */
  private static class Client extends Thread {
    @Override
    public void run() {
      try {
        for(int i = 0; i < RUNS && failed == null; i++) {
          put(NAME + '/' + i + ".xml", new ArrayInput("<x/>"));
        }
      } catch(final IOException ex) {
        failed = ex.getMessage();
        Util.stack(ex);
      }
    }
  }
}
