package org.basex.test.server;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.basex.BaseXServer;
import org.basex.core.Command;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.XQuery;
import org.basex.server.ClientSession;
import org.basex.server.Session;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the four locking cases.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Andreas Weiler
 */
public final class LockingTest {
  /** Positions to check in the query. */
  private static final int POS = 50;
  /** Number of performance tests. */
  private static final int TESTS = 5;

  /** Test file. */
  private static final String FILE = "etc/test/factbook.zip";
  /** Test name. */
  private static final String NAME = "factbook";
  /** Test query. */
  private static final String READ1 =
    "for $c in (//country)[position() < " + POS + "] " +
    "for $n in //city where $c/@id = $n/@country and $n/name = 'Tirane' " +
    "return $n/population/text()";
  /** Test update query. */
  private static final String WRITE1 =
    "for $i in (//members)[position() < " + POS + "] " +
    "return insert node <aa/> into $i";
  /** Test update query. */
  private static final String WRITE2 =
    "delete nodes //aa";

  /** Performance query. */
  private static final String PERF =
    "for $c in (doc('factbook')//country)[position() < 3] " +
    "for $n at $p in doc('factbook')//city " +
    "where $c/@id = $n/@country and $n/name = 'Tirane' " +
    "return $n/population/text()";

  /** Server reference. */
  static BaseXServer server;
  /** Socket reference. */
  static Session session1;
  /** Socket reference. */
  static Session session2;

  /** Status of test. */
  boolean done;

  /** Starts the server.
   * @throws IOException exception
   */
  @BeforeClass
  public static void start() throws IOException {
    server = new BaseXServer("-z");
    session1 = newSession();
    session2 = newSession();
  }

  /**
   * Stops the server.
   * @throws Exception exception
   */
  @AfterClass
  public static void stop() throws Exception {
    session1.close();
    session2.execute(new DropDB(NAME));
    session2.close();
    // stop server instance
    server.stop();
  }

  /**
   * Runs a test for concurrent database creations.
   * @throws Exception database exception
   */
  @Test
  public void createTest() throws Exception {
    // first thread
    final Thread t1 = new Thread() {
      @Override
      public void run() {
        try {
          session1.execute(new CreateDB(NAME, FILE));
        } catch(final IOException ex) {
        }
      }
    };
    // second thread
    final Thread t2 = new Thread() {
      @Override
      public void run() {
        // wait until first command is running
        Performance.sleep(100);
        try {
          session2.execute(new CreateDB(NAME, FILE));
          fail(FILE + " should still be locked.");
        } catch(final IOException ex) {
        }
      }
    };

    // start and join threads
    t1.start();
    t2.start();
    t1.join();
    t2.join();

    // opens DB in session2 for further tests
    session2.execute(new Open(NAME));

    if(server.context.datas.pins(NAME) != 2) fail("test failed conCreate");
  }

  /**
   * Read/read test.
   * @throws IOException I/O exception
   */
  @Test
  public void readReadTest() throws IOException {
    runTest(true, true);
  }

  /**
   * Write/write test.
   * @throws IOException I/O exception
   */
  @Test
  public void writeWriteTest() throws IOException {
    runTest(false, false);
    if(!checkRes(new XQuery("count(//aa)"), session1).equals("0")) {
      fail("Not all nodes have been deleted.");
    }
  }

  /**
   * Write/read test.
   * @throws IOException I/O exception
   */
  @Test
  public void writeReadTest() throws IOException {
    runTest(false, true);
  }

  /**
   * Write/read test.
   * @throws IOException I/O exception
   */
  @Test
  public void readWriteTest() throws IOException {
    runTest(true, false);
    if(!checkRes(new XQuery("count(//aa)"), session1).equals("0")) {
      fail("Not all nodes have been deleted.");
    }
  }

  /** Efficiency test.
   * @throws Exception exception
   */
  @Test
  public void efficiencyTest() throws Exception {
    final Client[] cl = new Client[TESTS];
    for(int i = 0; i < TESTS; ++i) cl[i] = new Client();
    for(final Client c : cl) c.start();
    for(final Client c : cl) c.join();
  }

  /**
   * Create db method test.
   * @throws Exception exception
   */
  @Test
  public void createDBTest() throws Exception {
    final byte[] hello = Token.token("<xml>Hello World!</xml>");
    InputStream bais = new ByteArrayInputStream(hello);
    session1.create("database", bais);
    bais = new ByteArrayInputStream(hello);
    session1.create("database2", bais);
    session1.execute("drop db database");
    bais = new ByteArrayInputStream(hello);
    session1.create("database2", bais);
    session1.execute("drop db database2");
  }

  /**
   * Runs the tests.
   * @param read1 perform read/write query in main thread
   * @param read2 perform read/write query in second thread
   * @throws IOException I/O exception
   */
  private void runTest(final boolean read1, final boolean read2)
      throws IOException {

    new Thread() {
      @Override
      public void run() {
        Performance.sleep(200);
        if(read2) {
          final String res = checkRes(new XQuery(READ1), session2);
          if(!res.equals("192000")) fail("test failed: " + res);
        } else {
          try {
            session2.execute(new XQuery(WRITE2));
          } catch(final IOException bx) {
            fail("test failed: " + bx.getMessage());
          }
        }
        done = true;
      }
    }.start();

    if(read1) {
      final String res = checkRes(new XQuery(READ1), session1);
      if(!res.equals("192000")) fail("test failed: " + res);
    } else {
      session1.execute(new XQuery(WRITE1));
    }
    while(!done) Performance.sleep(100);
    done = false;
  }

  /**
   * Returns a session instance.
   * @return session
   * @throws IOException exception
   */
  static ClientSession newSession() throws IOException {
    return new ClientSession(server.context, ADMIN, ADMIN);
  }

  /**
   * Returns query result.
   * @param cmd command reference
   * @param session session
   * @return String result
   */
  static String checkRes(final Command cmd, final Session session) {
    try {
      return session.execute(cmd);
    } catch(final IOException ex) {
      fail(ex.toString());
      return null;
    }
  }

  /** Single client. */
  static class Client extends Thread {
    /** Client session. */
    private final ClientSession session;

    /**
     * Default constructor.
     * @throws IOException exception
     */
    public Client() throws IOException {
        session = newSession();
    }

    @Override
    public void run() {
      if(!checkRes(new XQuery(PERF), session).equals("192000"))
        fail("efficiency test failed");
    }
  }
}
