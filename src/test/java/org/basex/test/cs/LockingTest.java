package org.basex.test.cs;

import static org.junit.Assert.*;
import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.BaseXServer;
import org.basex.core.BaseXException;
import org.basex.core.Session;
import org.basex.core.Command;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.XQuery;
import org.basex.server.ClientSession;
import org.basex.util.Performance;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the four locking cases.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public final class LockingTest {
  /** Test file. */
  private static final String FILE = "etc/xml/factbook.zip";
  /** Test name. */
  private static final String NAME = "factbook";
  /** Test query. */
  private static final String READ1 =
    "for $c in (//country)[position() < 10] " +
    "for $n in //city where $c/@id = $n/@country and $n/name = 'Tirane' " +
    "return $n/population/text()";
  /** Test update query. */
  private static final String WRITE1 =
    "for $i in (//members)[position() < 10] " +
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
  /** Number of performance tests. */
  private static final int TESTS = 5;

  /** Server reference. */
  static BaseXServer server;
  /** Socket reference. */
  static Session session1;
  /** Socket reference. */
  static Session session2;

  /** Status of test. */
  boolean done;
  /** Number of done tests. */
  static int tdone;

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    server = new BaseXServer();
    session1 = createSession();
    session2 = createSession();
  }

  /** Stops the server. */
  @AfterClass
  public static void stop() {
    closeSession(session1);
    closeSession(session2);
    // stop server instance
    new BaseXServer("stop");
  }

  /**
   * Runs a test for concurrent database creations.
   * @throws BaseXException database exception
   */
  @Test
  public void createTest() throws BaseXException {
    // drops database for clean test
    session1.execute(new DropDB("factbook"));

    // second thread
    new Thread() {
      @Override
      public void run() {
        // wait until first command is running
        Performance.sleep(200);
        try {
          session2.execute(new CreateDB(NAME, FILE));
          fail(FILE + " should still be locked.");
        } catch(final BaseXException ex) {
        }
      }
    }.start();

    // first (main) thread
    session1.execute(new CreateDB(NAME, FILE));
    // opens DB in session2 for further tests
    session2.execute(new Open(NAME));

    if(server.context.pool.pins("factbook") != 2) {
      fail("test failed conCreate");
    }
  }

  /**
   * Read/read test.
   * @throws BaseXException database exception
   */
  @Test
  public void readReadTest() throws BaseXException {
    runTest(true, true);
  }

  /**
   * Write/write test.
   * @throws BaseXException database exception
   */
  @Test
  public void writeWriteTest() throws BaseXException {
    runTest(false, false);
    if(!checkRes(new XQuery("count(//aa)"), session1).equals("0")) {
      fail("Not all nodes have been deleted.");
    }
  }

  /**
   * Write/read test.
   * @throws BaseXException database exception
   */
  @Test
  public void writeReadTest() throws BaseXException {
    runTest(false, true);
  }

  /**
   * Write/read test.
   * @throws BaseXException database exception
   */
  @Test
  public void readWriteTest() throws BaseXException {
    runTest(true, false);
    if(!checkRes(new XQuery("count(//aa)"), session1).equals("0")) {
      fail("Not all nodes have been deleted.");
    }
  }

  /** Efficiency test. */
  @Test
  public void efficiencyTest() {
    for(int i = 0; i < TESTS; i++) {
      final ClientSession s = createSession();
      new Thread() {
        @Override
        public void run() {
          if(!checkRes(new XQuery(PERF), s).equals("192000"))
            fail("efficiency test failed");
          closeSession(s);
          tdone++;
        }
      }.start();
    }

    // wait until all test have been finished
    while(tdone < TESTS) Performance.sleep(200);
  }

  /**
   * Runs the tests.
   * @param read1 perform read/write query in main thread
   * @param read2 perform read/write query in second thread
   * @throws BaseXException database exception
   */
  private void runTest(final boolean read1, final boolean read2)
      throws BaseXException {

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
          } catch(final BaseXException bx) {
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
    while(!done) {
      Performance.sleep(200);
    }
    done = false;
  }

  /**
   * Creates a client session.
   * @return client session
   */
  private static ClientSession createSession() {
    try {
      return new ClientSession(server.context, ADMIN, ADMIN);
    } catch(final IOException ex) {
      fail(ex.toString());
    }
    return null;
  }

  /**
   * Closes a client session.
   * @param s session to be closed
   */
  static void closeSession(final Session s) {
    try {
      s.close();
    } catch(final IOException ex) {
      fail(ex.toString());
    }
  }

  /**
   * Returns query result.
   * @param cmd command reference
   * @param session session
   * @return String result
   */
  String checkRes(final Command cmd, final Session session) {
    try {
      return session.execute(cmd);
    } catch(final BaseXException ex) {
      fail(ex.toString());
      return null;
    }
  }
}
