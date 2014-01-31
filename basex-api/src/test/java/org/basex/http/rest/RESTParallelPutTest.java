package org.basex.http.rest;

import static org.junit.Assert.*;

import java.io.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class sends parallel PUT requests to the REST API.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class RESTParallelPutTest extends HTTPTest {
  /** REST identifier. */
  private static final String REST = "rest";
  /** Root path. */
  private static final String ROOT = "http://" + Text.S_LOCALHOST + ":9998/" + REST + '/';
  /** Client count. */
  private static final int CLIENTS = 2;
  /** Runs per client. */
  private static final int RUNS = 1;
  /** Failed string. */
  private static String failed;

  // INITIALIZERS =============================================================

  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeClass
  public static void start() throws Exception {
    init(ROOT, true);
  }

  // TEST METHODS =============================================================

  /**
   * Concurrency test.
   * @throws Exception exception
   */
  @Ignore("GH-666")
  @Test
  public void test() throws Exception {
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
