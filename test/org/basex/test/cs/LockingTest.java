package org.basex.test.cs;

import org.basex.BaseXServer;
import org.basex.core.Session;
import org.basex.core.Process;
import org.basex.core.proc.CreateDB;
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
  private static final String FILE = "input.xml";
  /** Test name. */
  //private static final String NAME = "input";
  /** Test query. */
  //private static final String QUERY = "";
  /** Socket reference. */
  static Session session1;
  /** Socket reference. */
  static Session session2;
  /** Tests are running. */
  boolean running = true;
  

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
    while(running) { }
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
    Performance.sleep(200);

    try {
      session1 = new ClientSession(server.context);
      session2 = new ClientSession(server.context);
    } catch(final Exception ex) {
      throw new AssertionError(ex.toString());
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
        System.out.println(process(new CreateDB(FILE), session1));
      }
    }.start();
    new Thread() {
      @Override
      public void run() {
        System.out.println(process(new CreateDB(FILE), session2));
      }
    }.start();
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
