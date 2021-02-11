package org.basex.server;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.cmd.*;
import org.basex.util.*;
import org.junit.jupiter.api.Test;

/**
 * Testing concurrent XQUF statements on a single database.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
public final class XQUFServerStressTest extends SandboxTest {
  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test public void clients10runs10() throws Exception {
    run(10, 10);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test public void clients10runs50() throws Exception {
    run(10, 50);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test public void clients50runs10() throws Exception {
    run(50, 10);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test public void clients50runs50() throws Exception {
    run(50, 50);
  }

  /**
   * Runs the stress test.
   * @param clients number of clients
   * @param runs number of runs per client
   * @throws Exception exception
   */
  private void run(final int clients, final int runs) throws Exception {
    // run server instance
    final BaseXServer server = createServer();
    insert(clients, runs);
    delete(clients, runs);
    try(ClientSession cs = createClient()) {
      cs.execute(new DropDB(NAME));
    }
    stopServer(server);
  }

  /**
   * Performs the query.
   * @param clients number of clients
   * @param runs number of runs
   * @throws Exception exception
   */
  private static void insert(final int clients, final int runs) throws Exception {
    try(ClientSession cs = createClient()) {
      cs.execute(new CreateDB(NAME, "<doc/>"));
    }
    run("insert node <node/> into doc('" + NAME + "')/doc", clients, runs);
  }

  /**
   * Performs the query.
   * @param clients number of clients
   * @param runs number of runs
   * @throws Exception exception
   */
  private static void delete(final int clients, final int runs) throws Exception {
    try(ClientSession cs = createClient()) {
      cs.execute(new CreateDB(NAME, "<doc/>"));
      final int c = 100 + clients * clients;
      cs.execute(new XQuery("for $i in 1 to " + c +
          " return insert node <node/> into doc('" + NAME + "')/doc"));
    }
    run("delete nodes (doc('" + NAME + "')/doc/node)[1]", clients, runs);
  }

  /**
   * Starts concurrent client operations.
   * @param query test query
   * @param clt number of clients
   * @param runs number of runs
   * @throws Exception exception
   */
  private static void run(final String query, final int clt, final int runs) throws Exception {
    final Client[] cl = new Client[clt];
    for(int i = 0; i < clt; ++i) cl[i] = new Client(query, runs);
    for(final Client c : cl) c.start();
    for(final Client c : cl) c.join();
  }

  /** Single client. */
  static final class Client extends Thread {
    /** Client session. */
    private final ClientSession session;
    /** Query to be executed by this client. */
    final String query;
    /** Number of runs. */
    private final int runs;

    /**
     * Default constructor.
     * @param query query string
     * @param runs number of runs
     * @throws Exception exception
     */
    Client(final String query, final int runs) throws Exception {
      session = createClient();
      this.query = query;
      this.runs = runs;
    }

    @Override
    public void run() {
      try {
        for(int i = 0; i < runs; ++i) {
          Performance.sleep(100);
          session.execute("xquery " + query);
        }
        session.close();
      } catch(final Exception ex) {
        ex.printStackTrace();
      }
    }
  }
 }
