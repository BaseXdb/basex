package org.basex.test.cs;

import java.io.IOException;

import org.basex.BaseXServer;
import org.basex.core.Session;
import org.basex.core.Process;
import org.basex.core.Commands.CmdUpdate;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.Delete;
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
  //private static final String FILE = "f:/xml/xmark/111mb.zip";
  /** Test name. */
  private static final String NAME = "factbook";
  /** Test query. */
  private static final String QUERY = "for $m in //members for $r " +
  		"in //river/name/text() where starts-with($r, 'Q')" +
  		" order by $r return data($r)";
  /* for $c in //country
  for $n in //city
  where $c/@id = $n/@country
  and $n/name = 'Tirane'
  return $n
   */
  
  /** Socket reference. */
  static Session session1;
  /** Socket reference. */
  static Session session2;
  /** Tests are running. */
  boolean running = true;
  /** Status of test. */
  boolean done = false;
  

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
    conCreate();
    System.out.println("--> Test 1 successfull, Test 2 started...");    
    done = false;
    // read read test
    runTest(true, true);
    System.out.println("--> Test 2 successfull, Test 3 started...");
    done = false;
    // write write test
    runTest(false, false);
    checkTest3();
    done = false;
    // write read test
    runTest(false, true);
    System.out.println("--> Test 3 successfull, Test 4 started...");
    done = false;
    // read write test
    runTest(true, false);
    System.out.println("--> Test 4 successfull, Test 5 started...");
    while(running) { Performance.sleep(200); }
    stop();
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

      try {
        session1 = new ClientSession(server.context);
        session2 = new ClientSession(server.context);
      } catch(IOException e) {
        e.printStackTrace();
      }
  }
  
  /** Stops the server. */
  private void stop() {
    try {
      session1.close();
      session2.close();
    } catch(final Exception ex) {
      throw new AssertionError(ex.toString());
    }

    // Stop server instance.
    new BaseXServer("stop");
  }
  
  /* [AW] to add..
   * - concurrent create commands
   * - concurrent queries
   * - concurrent updates
   * 
   * - Test results of queries/commands/...
   * - done flag: method shouldn't be left before
   *   all commands have been processed
   */
  
  /**
   * Tests concurrent create commands.
   */
  private void conCreate() {
    // second thread
    new Thread() {
      @Override
      public void run() {
        Performance.sleep(200);
        String result = process(new CreateDB(FILE), session2);
        if(result == null) err("test failed conCreate");
        else System.out.println(result);
        process(new Open(NAME), session2);
        done = true;
      }
    }.start();

    // first (main) thread
    process(new CreateDB(FILE), session1);

    while(!done) { Performance.sleep(200); }
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
        if(test1) {
          process(new XQuery(QUERY), session1);
        } else {
          process(new Insert(CmdUpdate.ELEMENT, "//members", "aa"), session1);
        }
        new Thread() {
          @Override
          public void run() {
            String result = "";
            if(test2) {
              result = process(new XQuery(QUERY), session2);
            } else {
              result = process(new Delete("//aa"), session2);
            }
            if(result != null) err("test failed readRead: " + result);
            done = true;
          }
        }.start();
      }
    }.start();
    while(!done) { Performance.sleep(200); }
    if(test1 && !test2) running = false;
  }
  
  /**
   * Checks test3 for correctness. 
   */
  private void checkTest3() {
    process(new XQuery("count(//aa)"), session1);
    CachedOutput out = new CachedOutput();
    try {
      session1.output(out);
    } catch(IOException e) {
      e.printStackTrace();
    }
    if(out.toString().equals("0")) {
    System.out.println("--> Test 3 successfull, finished...");
    } else {
      err("test failed: conUpdate");
    }
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
