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
import org.basex.util.Token;

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
  private static final String FILE = "factbook.xml";
  /** Test name. */
  private static final String NAME = "factbook";
  /** Test query. */
  private static final String QUERY = "for $m in //members for $r " +
  		"in //river/name/text() where starts-with($r, 'Q')" +
  		" order by $r return data($r)";
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
    conQuery();
    System.out.println("--> Test 2 successfull, Test 3 started...");
    done = false;
    conUpdate();
    process(new XQuery("count(//aa)"), session1);
    CachedOutput out = new CachedOutput();
    try {
      session1.output(out);
    } catch(IOException e) {
      e.printStackTrace();
    }
    byte[] res = out.finish();
    if(Token.string(res).equals("0")) {
    System.out.println("--> Test 3 successfull, finished...");
    } else {
      err("test failed: conUpdate");
    }
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
    Performance.sleep(300);

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
   * - ..
   */
  
  /**
   * Tests concurrent create commands.
   */
  private void conCreate() {
    new Thread() {
      @Override
      public void run() {
        process(new CreateDB(FILE), session1);
        new Thread() {
          @Override
          public void run() {
            String result = process(new CreateDB(FILE), session2);
            if(result == null) err("test failed: conCreate");
            process(new Open(NAME), session2);
            done = true;
          }
        }.start();
      }
    }.start();
    while(!done) { Performance.sleep(200); }
  }
  
  /**
   * Tests concurrent query commands.
   */
  private void conQuery() {
    new Thread() {
      @Override
      public void run() {
        process(new XQuery(QUERY), session1);
        new Thread() {
          @Override
          public void run() {
            String result = process(new XQuery(QUERY), session2);
            if(result != null) err("test failed conQuery: " + result);
            done = true;
          }
        }.start();
      }
    }.start();
    while(!done) { Performance.sleep(200); }
  }
  
  /**
   * Tests concurrent update commands.
   */
  private void conUpdate() {
    new Thread() {
      @Override
      public void run() {
        process(new Insert(CmdUpdate.ELEMENT, "//members", "aa"), session1);
        new Thread() {
          @Override
          public void run() {
            String result = process(new Delete("//aa"), session2);
            if(result != null) err("test failed conUpdate: " + result);
            done = true;
          }
        }.start();
      }
    }.start();
    while(!done) { Performance.sleep(200); }
    running = false;
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
