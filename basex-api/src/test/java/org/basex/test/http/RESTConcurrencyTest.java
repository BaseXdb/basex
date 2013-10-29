package org.basex.test.http;

import static org.junit.Assert.*;

import java.io.*;

import org.basex.core.*;
import org.basex.http.rest.*;
import org.junit.*;

/**
 * This class sends parallel requests to the REST API.
 *
 * It currently fails when {@link #CLIENTS} is set to a value larger than 1,
 * because {@link RESTCmd#open} performs separate transactions that may be intervened
 * by the updating transaction of another client.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class RESTConcurrencyTest extends HTTPTest {
  /** REST identifier. */
  private static final String REST = "rest";
  /** Root path. */
  protected static final String ROOT = "http://" + Text.LOCALHOST + ":9998/" + REST + '/';
  /** Client count. */
  private static final int CLIENTS = 10;
  /** Runs per client. */
  private static final int RUNS = 10;
  /** Control client sessions. */
  private final Client[] clients = new Client[CLIENTS];
  /** Failed flag. */
  protected static boolean failed;

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
    for(int i = 0; i < clients.length; i++) clients[i] = new Client();
    for(final Client c : clients) c.start();
    for(final Client c : clients) c.join();

    get("?command=drop+db+" + REST);
  }

  /** Client class. */
  static class Client extends Thread {
    @Override
    public void run() {
      try {
        for(int i = 0; i < RUNS && !failed; i++) {
          final double rnd = Math.random();
          final boolean query = rnd < 1 / 3d;
          final boolean delete = rnd > 2 / 3d;
          if(query) get("/" + REST + "?query=count(.)");
          else if(delete) get("/" + REST + "?query=db:delete('rest','/')");
          else get("/" + REST + "?query=db:add('rest',<a/>,'x')");
        }
      } catch(final IOException ex) {
        failed = true;
        fail(ex.getMessage());
      }
    }
  }
}
