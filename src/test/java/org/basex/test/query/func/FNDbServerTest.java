package org.basex.test.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.io.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.server.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery database functions prefixed with "db".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNDbServerTest extends AdvancedQueryTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/input.xml";
  /** Server reference. */
  private static BaseXServer server;

  /**
   * Initializes a test.
   * @throws BaseXException database exception
   */
  @Before
  public void finishTest() throws BaseXException {
    new DropDB(NAME).execute(context);
  }

  /**
   * Starts the server.
   * @throws IOException I/O exception
   */
  @BeforeClass
  public static void start() throws IOException {
    server = createServer();
  }

  /**
   * Finishes the test.
   * @throws IOException I/O exception
   */
  @AfterClass
  public static void finish() throws IOException {
    stopServer(server);
  }

  /**
   * Tests client / server functionality of database functions.
   * @throws IOException I/O exception
   */
  @Test
  public void clientServer() throws IOException {
    final ClientSession c1 = createClient();
    final ClientSession c2 = createClient();

    c1.execute(new XQuery(_DB_CREATE.args(NAME)));
    c2.execute(new XQuery(_DB_CREATE.args(NAME)));
    Assert.assertEquals("true", c1.execute(new XQuery(_DB_EXISTS.args(NAME))));
  }

  /**
   * Tests client / server functionality of database functions.
   * @throws IOException I/O exception
   * @throws InterruptedException interrupted exception
   */
  @Test
  public void concurrentClients() throws IOException, InterruptedException {
    final ClientSession check = createClient();

    // same DB name
    runTwoClients(new XQuery(_DB_CREATE.args(NAME)));
    Assert.assertEquals("true", check.execute(new XQuery(_DB_EXISTS.args(NAME))));

    // same DB name and files
    runTwoClients(new XQuery(_DB_CREATE.args(NAME, FILE, "in/")));
    Assert.assertEquals("true", check.execute(new XQuery(_DB_EXISTS.args(NAME))));

    // create, run query, drop
    runTwoClients(new XQuery(
      _DB_CREATE.args(NAME, FILE, "in/") +
      ",insert node <dummy/> into " + _DB_OPEN.args(NAME) + "," +
      _DB_DROP.args(NAME)));

    check.execute(new DropDB(NAME));
  }

  /**
   * Runs a number of clients in parallel that execute the same query.
   * @param cmd command
   * @throws IOException I/O exception
   * @throws InterruptedException interrupted exception
   */
  private void runTwoClients(final Command cmd)
      throws IOException, InterruptedException {

    final int n = 2;
    final CountDownLatch start = new CountDownLatch(1);
    final CountDownLatch stop = new CountDownLatch(n);
    final Client[] clients = new Client[n];
    for(int i = 0; i < n; i++) {
      clients[i] = new Client(cmd, start, stop);
    }
    start.countDown();
    stop.await();
    for(final Client c : clients) {
      if(c.error != null) fail(c.error);
    }
  }
}
