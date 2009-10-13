package org.basex.test.cs;

import java.io.IOException;

import org.basex.BaseXServer;
import org.basex.core.Session;
import org.basex.core.Process;
import org.basex.core.Commands.CmdUpdate;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.Delete;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.Insert;
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
    System.out.println(server.context.pool.info());
    if(server.context.size("factbook") == 2) {
      System.out.println("--> Test 1 successful, Test 2 started...");
    } else {
      err("test failed conCreate");
    }

    // read read test
    runTest(true, true);
    System.out.println("--> Test 2 successful, Test 3 started...");
    done = false;
    
    // write write test
    runTest(false, false);
    process(new XQuery("count(//aa)"), session1);
    if(!checkRes(session1).equals("0")) {
      err("test failed write write");
    } else {
      System.out.println("--> Test 3 successful, Test 4 started...");
    }
    done = false;
    
    // write read test
    runTest(false, true);
    System.out.println("--> Test 4 successful, Test 5 started...");
    done = false;
    
    // read write test
    runTest(true, false);
    System.out.println("--> Test 5 successful, last check...");
    
    process(new XQuery("count(//aa)"), session1);
    if(!checkRes(session1).equals("0")) {
      err("test failed write read / read write");
    } else {
      System.out.println("--> All Locking Tests done...," +
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
        process(new XQuery(QUERYN), s);
        if(!checkRes(s).equals("192000")) err("efficiency test failed");
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
        server = new BaseXServer("-v");
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
      return new ClientSession(server.context);
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

  /*
   * [AW] to add.. - concurrent create commands - concurrent queries -
   * concurrent updates - Test results of queries/commands/... - done flag:
   * method shouldn't be left before all commands have been processed
   */

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
        else System.out.println("Message: " + result);
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
        String result = "";
        if(test2) {
          result = process(new XQuery(QUERY), session2);
          final String res = checkRes(session2);
          if(!res.equals("192000")) err("test failed: " + res);
        } else {
          result = process(new Delete("//aa"), session2);
        }
        if(result != null) err("test failed: " + result);
        done = true;
      }
    }.start();
    if(test1) {
      process(new XQuery(QUERY), session1);
      final String res = checkRes(session1);
      if(!res.equals("192000")) err("test failed: " + res);
    } else {
      process(new Insert(CmdUpdate.ELEMENT, "//members", "aa"), session1);
    }
    while(!done) {
      Performance.sleep(200);
    }
  }

  /**
   * Returns query result.
   * @param s Session
   * @return String result
   */
  String checkRes(final Session s) {
    final CachedOutput out = new CachedOutput();
    try {
      s.output(out);
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
  String process(final Process pr, final Session session) {
    try {
      return session.execute(pr) ? null : session.info();
    } catch(final Exception ex) {
      return ex.toString();
    }
  }
}
