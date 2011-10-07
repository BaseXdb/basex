package org.basex.test.server;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.basex.BaseXServer;
import org.basex.core.Command;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.XQuery;
import org.basex.io.in.ArrayInput;
import org.basex.server.ClientSession;
import org.basex.server.Session;
import org.basex.util.Performance;
import org.basex.util.Util;
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
  private static final int POS = 10;

  /** Test name. */
  static final String NAME = Util.name(LockingTest.class);
  /** Performance query. */
  static final String PERF =
    "for $c in (db:open('" + NAME + "')//country)[position() < 3] " +
    "for $n at $p in db:open('" + NAME + "')//city " +
    "where $c/@id = $n/@country and $n/name = 'Tirane' " +
    "return $n/population/text()";

  /** Test file. */
  private static final String FILE = "etc/test/factbook.zip";
  /** Test query. */
  private static final String READ1 =
    "for $c in (//country)[position() <= " + POS + "] " +
    "for $n in //city where $c/@id = $n/@country and $n/name = 'Tirane' " +
    "return $n/population/text()";
  /** Test update query. */
  private static final String WRITE1 =
    "for $i in (//members)[position() <= " + POS + "] " +
    "return insert node <aa/> into $i";
  /** Test update query. */
  private static final String WRITE2 =
    "delete nodes //aa";

  /** Server reference. */
  static BaseXServer server;
  /** Socket reference. */
  static Session session1;
  /** Socket reference. */
  static Session session2;

  /** Status of test. */
  boolean done;

  /**
   * Starts the server.
   * @throws IOException exception
   */
  @BeforeClass
  public static void start() throws IOException {
    server = new BaseXServer("-z -p9999 -e9998");
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

    assertEquals(1, server.context.datas.pins(NAME));
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
    assertEquals("Not all nodes have been deleted.",
        "0", execute(new XQuery("count(//aa)"), session1));
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
    assertEquals("Not all nodes have been deleted.",
        "0", execute(new XQuery("count(//aa)"), session1));
  }

  /**
   * Create db method test.
   * @throws IOException I/O exception
   */
  @Test
  public void createDBTest() throws IOException {
    final String hello = "<xml/>";
    session1.create("database", new ArrayInput(hello));
    session1.create("database2", new ArrayInput(hello));
    session1.execute("drop db database");
    session1.create("database2", new ArrayInput(hello));
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

    session2.execute(new Open(NAME));

    new Thread() {
      @Override
      public void run() {
        Performance.sleep(200);
        if(read2) {
          assertEquals("192000", execute(new XQuery(READ1), session2));
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
      assertEquals("192000", execute(new XQuery(READ1), session1));
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
    return new ClientSession(LOCALHOST, 9999, ADMIN, ADMIN);
  }

  /**
   * Returns query result.
   * @param cmd command reference
   * @param session session
   * @return String result
   */
  static String execute(final Command cmd, final Session session) {
    try {
      return session.execute(cmd);
    } catch(final IOException ex) {
      fail(Util.message(ex));
      return null;
    }
  }
}
