package org.basex.performance;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.SandboxTest;
import org.junit.*;

/**
 * This class offers utility methods to perform simple benchmarks.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Benchmark extends SandboxTest {
  /** Test document. */
  private static final String INPUT = "src/test/resources/factbook.zip";
  /** Server reference. */
  private static BaseXServer server;
  /** Session. */
  private static Session session;
  /** Local vs server flag. */
  private static boolean local;

  /**
   * Initializes the benchmark.
   * @throws IOException I/O exception
   */
  @BeforeClass
  public static void init() throws IOException {
    // check if server is (not) running
    final int sp = context.globalopts.get(GlobalOptions.SERVERPORT);
    server = local || BaseXServer.ping(S_LOCALHOST, sp) ? null : createServer();
    session = local ? new LocalSession(context) : createClient();

    // create test database
    session.execute(new Set(MainOptions.QUERYINFO, true));
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterClass
  public static void stop() throws IOException {
    stopServer(server);
  }

  /**
   * Creates a new database instance and performs a query.
   * @param queries queries to be evaluated
   * @throws Exception exception
   */
  protected static void update(final String queries) throws Exception {
    update(1, queries);
  }

  /**
   * Creates a new database instance and performs a query for the
   * specified number of runs.
   * @param query queries to be evaluated
   * @param r runs the number for the specified number of time
   * @throws IOException I/O exception
   */
  protected static void update(final int r, final String query) throws IOException {
    // loop through number of runs for a single query
    check();
    for(int rn = 0; rn < r; ++rn) session.execute(new XQuery(query));
  }

  /**
   * Performs the specified query and returns the result.
   * @param query query to be evaluated
   * @return result
   * @throws IOException I/O exception
   */
  protected static String query(final String query) throws IOException {
    check();
    return session.execute(new XQuery(query));
  }

  /**
   * Creates or opens the test database.
   * @throws IOException I/O exception
   */
  private static void check() throws IOException {
    session.execute(new Check(INPUT));
  }
}
