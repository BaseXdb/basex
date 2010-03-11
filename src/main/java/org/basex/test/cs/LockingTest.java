package org.basex.test.cs;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.BaseXServer;
import org.basex.core.Main;
import org.basex.core.Session;
import org.basex.core.Proc;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.Open;
import org.basex.core.proc.XQuery;
import org.basex.io.CachedOutput;
import org.basex.server.ClientSession;
import org.basex.util.Performance;

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
  private static final String QUERY = "for $c in //country for $n in"
      + " //city where $c/@id = $n/@country and $n/name = 'Tirane'" +
        " return $n/population/text()";
  /** Test query 2. */
  private static final String QUERYN = "for $c in doc('factbook')" +
    "//country for $n in doc('factbook')//city where $c/@id =" +
    " $n/@country and $n/name = 'Tirane' return $n/population/text()";
  /** Number of tests. */
  private static final int TESTS = 5;

  /** Server reference. */
  BaseXServer server;
  /** Socket reference. */
  Session session1;
  /** Socket reference. */
  Session session2;
  /** Status of test. */
  boolean done;
  /** Number of done tests. */
  int tdone;

  /**
   * Main method, launching the test.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new LockingTest();
  }

  /**
   * Private constructor.
   */
  private LockingTest() {
    start();
    // drops database for clean test
    process(new DropDB("factbook"), session1);
    // concurrent create test
    conCreate();
    Main.outln(server.context.pool.info());
    if(server.context.pool.pins("factbook") == 2) {
      Main.outln("--> Test 1 successful, Test 2 started...");
    } else {
      Main.errln("test failed conCreate");
    }

    // read read test
    runTest(true, true);
    Main.outln("--> Test 2 successful, Test 3 started...");
    done = false;

    // write write test
    runTest(false, false);
    if(!checkRes(new XQuery("count(//aa)"), session1).equals("0")) {
      Main.errln("test failed write write");
    } else {
      Main.outln("--> Test 3 successful, Test 4 started...");
    }
    done = false;

    // write read test
    runTest(false, true);
    Main.outln("--> Test 4 successful, Test 5 started...");
    done = false;

    // read write test
    runTest(true, false);
    Main.outln("--> Test 5 successful, last check...");

    if(!checkRes(new XQuery("count(//aa)"), session1).equals("0")) {
      Main.errln("test failed write read / read write");
    } else {
      Main.outln("--> All Locking Tests done...," +
        " efficiency test started...");
    }
    for(int i = 0; i < TESTS; i++) {
      startQueryT(createSession());
    }
    while(tdone < TESTS) Performance.sleep(200);
    stop();
  }

  /**
   * Starts a query thread.
   * @param s session
   */
  private void startQueryT(final ClientSession s) {
    new Thread() {
      @Override
      public void run() {
        if(!checkRes(new XQuery(QUERYN), s).equals("192000"))
          Main.errln("efficiency test failed");
        closeSession(s);
        tdone++;
      }
    }.start();
  }

  /** Starts the server. */
  private void start() {
    server = new BaseXServer();
    session1 = createSession();
    session2 = createSession();
  }

  /**
   * Creates a client session.
   * @return client session
   */
  ClientSession createSession() {
    try {
      return new ClientSession(server.context, ADMIN, ADMIN);
    } catch(final IOException ex) {
      ex.printStackTrace();
    }
    return null;
  }

  /**
   * Closes a client session.
   * @param s session to be closed
   */
  void closeSession(final Session s) {
    try {
      s.close();
    } catch(final Exception ex) {
      throw new AssertionError(ex.toString());
    }
  }

  /** Stops the server. */
  private void stop() {
    closeSession(session1);
    closeSession(session2);
    // Stop server instance.
    new BaseXServer("stop");
  }

  /**
   * Tests concurrent create commands.
   */
  private void conCreate() {
    // second thread
    new Thread() {
      @Override
      public void run() {
        Performance.sleep(300);
        final String result = process(new CreateDB(FILE), session2);
        if(result == null) Main.errln("test failed conCreate");
        else Main.outln("Message: " + result);
      }
    }.start();
    // first (main) thread
    process(new CreateDB(FILE), session1);
    // opens DB in session2 for further tests
    process(new Open(NAME), session2);
  }

  /**
   * Runs the tests.
   * @param test1 first test flag
   * @param test2 second test flag
   */
  private void runTest(final boolean test1, final boolean test2) {
    new Thread() {
      @Override
      public void run() {
        Performance.sleep(300);
        if(test2) {
          final String res = checkRes(new XQuery(QUERY), session2);
          if(!res.equals("192000")) Main.errln("test failed: " + res);
        } else {
          final String res = process(new XQuery("delete nodes //aa"), session2);
          if(res != null) Main.errln("test failed: " + res);
        }
        done = true;
      }
    }.start();

    if(test1) {
      final String res = checkRes(new XQuery(QUERY), session1);
      if(!res.equals("192000")) Main.errln("test failed: " + res);
    } else {
      process(new XQuery(
          "for $i in //members return insert node <aa/> into $i"), session1);
    }
    while(!done) {
      Performance.sleep(200);
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
      ex.printStackTrace();
    }
    return co.toString();
  }

  /**
   * Runs the specified process.
   * @param pr process reference
   * @param session Session
   * @return success flag
   */
  String process(final Proc pr, final Session session) {
    try {
      return session.execute(pr) ? null : session.info();
    } catch(final Exception ex) {
      return ex.toString();
    }
  }
}
