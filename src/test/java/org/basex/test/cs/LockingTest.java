package org.basex.test.cs;

import static org.junit.Assert.*;
import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.BaseXServer;
import org.basex.core.Session;
import org.basex.core.Proc;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.Open;
import org.basex.core.proc.XQuery;
import org.basex.io.CachedOutput;
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
  private static final String FILE = "etc/xml/factbook.xml";
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
    // Stop server instance.
    new BaseXServer("stop");
  }

  /** Runs a test for concurrent database creations. */
  @Test
  public void createTest() {
    // drops database for clean test
    exec(new DropDB("factbook"), session1);

    // second thread
    new Thread() {
      @Override
      public void run() {
        // wait until first process is running
        Performance.sleep(200);
        final String result = exec(new CreateDB(FILE), session2);
        if(result == null) fail(FILE + " should still be locked.");
      }
    }.start();

    // first (main) thread
    exec(new CreateDB(FILE), session1);
    // opens DB in session2 for further tests
    exec(new Open(NAME), session2);

    if(server.context.pool.pins("factbook") != 2) {
      fail("test failed conCreate");
    }
  }

  /** Read/read test. */
  @Test
  public void readReadTest() {
    runTest(true, true);
  }

  /** Write/write test. */
  @Test
  public void writeWriteTest() {
    runTest(false, false);
    if(!checkRes(new XQuery("count(//aa)"), session1).equals("0")) {
      fail("Not all nodes have been deleted.");
    }
  }

  /** Write/read test. */
  @Test
  public void writeReadTest() {
    runTest(false, true);
  }

  /** Write/read test. */
  @Test
  public void readWriteTest() {
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
   */
  private void runTest(final boolean read1, final boolean read2) {
    new Thread() {
      @Override
      public void run() {
        Performance.sleep(200);
        if(read2) {
          final String res = checkRes(new XQuery(READ1), session2);
          if(!res.equals("192000")) fail("test failed: " + res);
        } else {
          final String res = exec(new XQuery(WRITE2), session2);
          if(res != null) fail("test failed: " + res);
        }
        done = true;
      }
    }.start();

    if(read1) {
      final String res = checkRes(new XQuery(READ1), session1);
      if(!res.equals("192000")) fail("test failed: " + res);
    } else {
      exec(new XQuery(WRITE1), session1);
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
   * @param pr process reference
   * @param session session
   * @return String result
   */
  String checkRes(final Proc pr, final Session session) {
    final CachedOutput co = new CachedOutput();
    try {
      session.execute(pr, co);
    } catch(final IOException ex) {
      fail(ex.toString());
    }
    return co.toString();
  }

  /**
   * Runs the specified process.
   * @param pr process reference
   * @param session Session
   * @return success flag
   */
  String exec(final Proc pr, final Session session) {
    try {
      return session.execute(pr) ? null : session.info();
    } catch(final IOException ex) {
      return ex.toString();
    }
  }
}
