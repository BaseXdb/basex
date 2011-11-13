package org.basex.test.performance;

import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.BaseXServer;
import org.basex.core.MainProp;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.Text;
import org.basex.core.cmd.Check;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;
import org.basex.server.ClientSession;
import org.basex.server.LocalSession;
import org.basex.server.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * This class offers utility methods to perform simple benchmarks.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class Benchmark {
  /** Global context. */
  private static final Context CONTEXT = new Context();
  /** Server reference. */
  private static BaseXServer server;
  /** Session. */
  private static Session session;
  /** Test document. */
  private static String input = "etc/factbook.zip";
  /** Local vs server flag. */
  private static boolean local;

  /**
   * Initializes the benchmark.
   * @throws IOException I/O exception
   */
  @BeforeClass
  public static void init() throws IOException {
    // Check if server is (not) running
    server = !local && !BaseXServer.ping(LOCALHOST,
        CONTEXT.mprop.num(MainProp.SERVERPORT)) ?
        new BaseXServer("-z", "-p9999", "-e9998") : null;

    session = local ? new LocalSession(CONTEXT) :
      new ClientSession(Text.LOCALHOST, 9999, Text.ADMIN, Text.ADMIN);

    // Create test database
    session.execute(new Set(Prop.QUERYINFO, true));
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterClass
  public static void stop() throws IOException {
    server.stop();
  }

  /**
   * Creates a new database instance and performs a query.
   * @param queries queries to be evaluated
   * @throws Exception exception
   */
  protected void update(final String queries) throws Exception {
    update(1, queries);
  }

  /**
   * Creates a new database instance and performs a query for the
   * specified number of runs.
   * @param query queries to be evaluated
   * @param r runs the number for the specified number of time
   * @throws IOException I/O exception
   */
  protected void update(final int r, final String query)
      throws IOException {

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
  protected String query(final String query) throws IOException {
    check();
    return session.execute(new XQuery(query));
  }

  /**
   * Creates or opens the test database.
   * @throws IOException I/O exception
   */
  private void check() throws IOException {
    session.execute(new Check(input));
  }
}
