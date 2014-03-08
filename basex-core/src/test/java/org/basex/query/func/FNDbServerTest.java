package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.io.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.server.*;
import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Database Module in a client/server environment.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNDbServerTest extends AdvancedQueryTest {
  /** Number of clients. */
  private static final int NUM = 5;
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
   * Tests client/server functionality of database functions.
   * @throws IOException I/O exception
   */
  @Test
  public void clientServer() throws IOException {
    final ClientSession c1 = createClient();
    final ClientSession c2 = createClient();

    c1.execute(new XQuery(_DB_CREATE.args(NAME)));
    c2.execute(new XQuery(_DB_CREATE.args(NAME)));
    assertEquals("true", c1.execute(new XQuery(_DB_EXISTS.args(NAME))));
  }

  /**
   * Tests client/server functionality of database functions.
   * @throws IOException I/O exception
   * @throws InterruptedException interrupted exception
   */
  @Test
  public void concurrentClients() throws IOException, InterruptedException {
    final ClientSession check = createClient();

    // same DB name, which is 2 x NUM times
    runClients(new XQuery(_DB_CREATE.args(NAME)));
    assertEquals("true", check.execute(new XQuery(_DB_EXISTS.args(NAME))));
    runClients(new XQuery(_DB_CREATE.args(NAME)));
    assertEquals("true", check.execute(new XQuery(_DB_EXISTS.args(NAME))));

    // same DB name and files
    runClients(new XQuery(_DB_CREATE.args(NAME, FILE, "in/")));
    assertEquals("true", check.execute(new XQuery(_DB_EXISTS.args(NAME))));

    // create
    runClients(new XQuery(
      _DB_DROP.args(NAME) + ',' +
      _DB_CREATE.args(NAME, FILE, "in/")
    ));

    // add, create
    runClients(new XQuery(
        _DB_ADD.args(NAME, "<X/>", "x.xml") + ',' +
      _DB_DROP.args(NAME) + ',' +
      _DB_CREATE.args(NAME, FILE)));

    check.execute(new DropDB(NAME));
  }

  /**
   * Runs a number of clients in parallel that execute the same query.
   * @param cmd command
   * @throws IOException I/O exception
   * @throws InterruptedException interrupted exception
   */
  private static void runClients(final Command cmd) throws IOException, InterruptedException {
    final CountDownLatch start = new CountDownLatch(1);
    final CountDownLatch stop = new CountDownLatch(NUM);
    final Client[] clients = new Client[NUM];
    for(int i = 0; i < NUM; i++) {
      clients[i] = new Client(cmd, start, stop);
    }
    start.countDown();
    stop.await();
    for(final Client c : clients) if(c.error != null) fail(c.error);
  }
}
