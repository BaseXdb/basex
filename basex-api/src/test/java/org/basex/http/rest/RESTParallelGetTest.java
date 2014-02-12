package org.basex.http.rest;

import static org.junit.Assert.*;

import java.io.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class sends parallel GET requests to the REST API.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class RESTParallelGetTest extends HTTPTest {
  /** REST identifier. */
  private static final String REST = "rest";
  /** Root path. */
  private static final String ROOT = "http://" + Text.S_LOCALHOST + ":9998/" + REST + '/';
  /** Client count. */
  private static final int CLIENTS = 10;
  /** Runs per client. */
  private static final int RUNS = 10;
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
  @Test
  public void test() throws Exception {
    get("?command=create+db+" + REST + "+<a/>");

    // start and join concurrent clients
    final Client[] clients = new Client[CLIENTS];
    final int cs = clients.length;
    for(int i = 0; i < cs; i++) clients[i] = new Client();
    for(final Client c : clients) c.start();
    for(final Client c : clients) c.join();

    get("?command=drop+db+" + REST);
    if(failed != null) fail(failed);
  }

  /** Client class. */
  private static class Client extends Thread {
    @Override
    public void run() {
      try {
        for(int i = 0; i < RUNS && failed == null; i++) {
          final double rnd = Math.random();
          final boolean query = rnd < 1 / 3d;
          final boolean delete = rnd > 2 / 3d;
          if(query) get('/' + REST + "?query=count(.)");
          else if(delete) get('/' + REST + "?query=db:delete('rest','/')");
          else get('/' + REST + "?query=db:add('rest',<a/>,'x')");
        }
      } catch(final IOException ex) {
        failed = ex.getMessage();
        Util.stack(ex);
      }
    }
  }
}
