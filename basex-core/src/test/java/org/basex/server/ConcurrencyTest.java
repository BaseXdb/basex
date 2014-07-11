package org.basex.server;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.util.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class tests the execution of parallel commands.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Andreas Weiler
 */
public final class ConcurrencyTest extends SandboxTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/factbook.zip";
  /** Test queries. */
  private static final String [] QUERIES = {
    "(db:open('" + NAME + "')//province)[position() < 10] ! (insert node <test/> into .)",
    "(1 to 100000)[. = 0]"
  };

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
  public void runQueries() throws Exception {
    final QueryClient[] cl = new QueryClient[50];
    for(int i = 0; i < cl.length; ++i) cl[i] = new QueryClient(i);
    for(final QueryClient c : cl) c.start();
    for(final QueryClient c : cl) c.join();
    for(final QueryClient c : cl) c.session.close();
  }

  /** Single client. */
  static class QueryClient extends Thread {
    /** Client session. */
    ClientSession session;
    /** Query number. */
    final int number;

    /**
     * Default constructor.
     * @param nr query number
     */
    QueryClient(final int nr) {
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

  /**
   * Efficiency test.
   * @throws Exception exception
   */
  @Test
  public void runCommands() throws Exception {
    final Thread[] th = new Thread[100];
    for(int i = 0; i < th.length; ++i) th[i] = new CommandClient();
    for(final Thread c : th) c.start();
    for(final Thread c : th) c.join();
  }

  /** Random counter. */
  private static final Random RANDOM = new Random();

  /** Single client. */
  private static class CommandClient extends Thread {
    @Override
    public void run() {
      try {
        final int i = RANDOM.nextInt(2);
        final Command cmd;
        if(i == 0) cmd = new CreateDB(NAME);
        else cmd = new DropDB(NAME);
        cmd.execute(context);
      } catch(final IOException ex) {
        fail(Util.message(ex));
      }
    }
  }
}
