package org.basex.local.single;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;

/**
 * This class offers utility methods to perform simple benchmarks.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class Benchmark extends SandboxTest {
  /** Test document. */
  private static final String INPUT = "src/test/resources/factbook.zip";
  /** Local vs client/server mode (system property {@code local}). */
  private static final boolean LOCAL = Boolean.getBoolean("local");
  /** Server reference. */
  private static BaseXServer server;
  /** Session. */
  private static Session session;

  /**
   * Initializes the benchmark.
   * @throws IOException I/O exception
   */
  @BeforeAll public static void init() throws IOException {
    // check if server is (not) running
    final int sp = context.soptions.get(StaticOptions.SERVERPORT);
    server = LOCAL || BaseXServer.ping(S_LOCALHOST, sp) ? null : createServer();
    session = LOCAL ? new LocalSession(context) : createClient();
    session.execute(new Set(MainOptions.QUERYINFO, true));
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterAll public static void stop() throws IOException {
    stopServer(server);
  }

  /**
   * Creates the test database. Every benchmark starts from the same input: many of these
   * benchmarks are destructive, and a database left over by a previous one measures nothing.
   * @throws IOException I/O exception
   */
  @BeforeEach public void createDB() throws IOException {
    session.execute(new CreateDB(NAME, INPUT));
  }

  /**
   * Creates a new database instance and performs a query.
   * @param query query to be evaluated
   * @return resulting string
   * @throws IOException I/O exception
   */
  protected static String eval(final String query) throws IOException {
    return eval(1, query);
  }

  /**
   * Performs the specified query n times and and returns the result.
   * @param query query to be evaluated
   * @param n number of runs
   * @return resulting string
   * @throws IOException I/O exception
   */
  protected static String eval(final int n, final String query) throws IOException {
    // loop through number of runs for a single query
    String result = "";
    for(int rn = 0; rn < n; ++rn) result = session.execute(new XQuery(query));
    return result;
  }
}
