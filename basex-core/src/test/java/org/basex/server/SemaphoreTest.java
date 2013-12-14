package org.basex.server;

import static org.junit.Assert.*;

import java.io.*;
import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the order of incoming commands.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Andreas Weiler
 */
public final class SemaphoreTest extends SandboxTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/factbook.zip";
  /** Test queries. */
  private static final String [] QUERIES = {
    "(db:open('" + NAME + "')//province)[position() < 10] ! (insert node <test/> into .)",
    "(1 to 100000)[. = 0]"
  };
  /** Number of clients. */
  private static final int CLIENTS = 50;

  /** Server reference. */
  private BaseXServer server;
  /** Socket reference. */
  private Session sess;

  /**
   * Starts the server.
   * @throws IOException exception
   */
  @Before
  public void start() throws IOException {
    server = createServer();
    sess = createClient();
    sess.execute(new CreateDB(NAME, FILE));
  }

  /**
   * Stops the server.
   * @throws Exception exception
   */
  @After
  public void stop() throws Exception {
    sess.execute(new DropDB(NAME));
    sess.close();
    stopServer(server);
  }

  /**
   * Efficiency test.
   * @throws Exception exception
   */
  @Test
  public void runClients() throws Exception {
    final Client[] cl = new Client[CLIENTS];
    for(int i = 0; i < CLIENTS; ++i) cl[i] = new Client(i);
    for(final Client c : cl) c.start();
    for(final Client c : cl) c.join();
    for(final Client c : cl) c.session.close();
  }

  /** Single client. */
  static class Client extends Thread {
    /** Client session. */
    ClientSession session;
    /** Query number. */
    final int number;

    /**
     * Default constructor.
     * @param nr query number
     */
    Client(final int nr) {
      number = nr;
      try {
        session = createClient();
      } catch(final IOException ex) {
        fail(Util.message(ex));
      }
    }

    @Override
    public void run() {
      try {
        session.query(QUERIES[number % QUERIES.length]).execute();
      } catch(final IOException ex) {
        fail(Util.message(ex));
      }
    }
  }
}
