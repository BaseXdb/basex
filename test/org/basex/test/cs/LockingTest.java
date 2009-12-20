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
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class LockingTest {
  /** Server reference. */
  static BaseXServer server;
  /** Test file. */
  private static final String FILE = "etc/xml/factbook.xml";
  // private static final String FILE = "f:/xml/xmark/111mb.zip";
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
  /** Test query 3.
  private static final String QUERY3 = "for $c in doc('factbook')//node()" +
      "where contains($c/text(), 'a') return $c";
  */
  /** Socket reference. */
  static Session session1;
  /** Socket reference. */
  static Session session2;
  /** Status of test. */
  boolean done;
  /** Number of done eff tests. */
  int tdone;
  /** Number of eff tests. */
  int tests = 5;

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
    // drops factbook for clean test
    process(new DropDB("factbook"), session1);
    // concurrent create test
    conCreate();
    Main.outln(server.context.pool.info());
    if(server.context.pool.pins("factbook") == 2) {
      Main.outln("--> Test 1 successful, Test 2 started...");
    } else {
      err("test failed conCreate");
    }

    // read read test
    runTest(true, true);
    Main.outln("--> Test 2 successful, Test 3 started...");
    done = false;

    // write write test
    runTest(false, false);
    if(!checkRes(new XQuery("count(//aa)"), session1).equals("0")) {
      err("test failed write write");
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
      err("test failed write read / read write");
    } else {
      Main.outln("--> All Locking Tests done...," +
        " efficiency test started...");
    }
    for(int i = 0; i < tests; i++) {
      startQueryT(createSession());
    }
    while(tdone < tests) Performance.sleep(200);
    stop();
  }

  /**
   * Starts a xquery thread.
   * @param s Session
   */
  private void startQueryT(final ClientSession s) {
    new Thread() {
      @Override
      public void run() {
        if(!checkRes(new XQuery(QUERYN), s).equals("192000"))
          err("efficiency test failed");
        closeSession(s);
        tdone++;
      }
    }.start();
  }

  /** Starts the server. */
  private void start() {
    new Thread() {
      @Override
      public void run() {
        server = new BaseXServer();
      }
    }.start();

    // wait for server to be started
    Performance.sleep(1000);

    session1 = createSession();
    session2 = createSession();
  }

  /**
   * Creates a Clientsession.
   * @return ClientSession
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
   * Closes a Clientsession.
   * @param s Session
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
        if(result == null) err("test failed conCreate");
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
   * @param test1 boolean
   * @param test2 boolean
   */
  private void runTest(final boolean test1, final boolean test2) {
    new Thread() {
      @Override
      public void run() {
        Performance.sleep(300);
        if(test2) {
          final String res = checkRes(new XQuery(QUERY), session2);
          if(!res.equals("192000")) err("test failed: " + res);
        } else {
          final String res = process(new XQuery("delete nodes //aa"), session2);
          if(res != null) err("test failed: " + res);
        }
        done = true;
      }
    }.start();
    if(test1) {
      final String res = checkRes(new XQuery(QUERY), session1);
      if(!res.equals("192000")) err("test failed: " + res);
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
    final CachedOutput out = new CachedOutput();
    try {
      session.execute(pr, out);
    } catch(final IOException ex) {
      ex.printStackTrace();
    }
    return out.toString();
  }

  /**
   * Prints the error message.
   * @param e String
   */
  void err(final String e) {
    System.err.println(e);
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
