package org.basex.test.cs;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;
import org.basex.BaseXServer;
import org.basex.core.Proc;
import org.basex.core.Session;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.Open;
import org.basex.server.ClientSession;
import org.basex.util.Performance;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests transaction and locking cases.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public final class PoolTest {
  /** Test file. */
  private static final String FILE = "etc/xml/input.xml";
  /** Test name. */
  private static final String NAME = "input";

  /** Server reference. */
  static BaseXServer server;
  /** Socket reference. */
  static Session session1;
  /** Socket reference. */
  static Session session2;

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    new Thread() {
      @Override
      public void run() {
        server = new BaseXServer();
      }
    }.start();

    // wait for server to be started
    Performance.sleep(500);

    try {
      session1 = new ClientSession(server.context, ADMIN, ADMIN);
      session2 = new ClientSession(server.context, ADMIN, ADMIN);
    } catch(final Exception ex) {
      throw new AssertionError(ex.toString());
    }
  }

  /** Create and Drop Tests. */
  @Test
  public void createAndDrop() {
    ok(new CreateDB(FILE), session1);
    pins(1, NAME);
    ok(new CreateDB(FILE), session1);
    pins(1, NAME);
    no(new CreateDB(FILE), session2);
    pins(1, NAME);
    no(new CreateDB(FILE), session2);
    pins(1, NAME);
    no(new DropDB(NAME), session2);
    pins(1, NAME);
    ok(new DropDB(NAME), session1);
    pins(0, NAME);
  }

  /** Close and Open Tests. */
  @Test
  public void closeAndOpen() {
    ok(new CreateDB(FILE), session2);
    pins(1, NAME);
    ok(new Close(), session1);
    pins(1, NAME);
    ok(new Close(), session2);
    pins(0, NAME);
    ok(new Open(NAME), session1);
    pins(1, NAME);
    ok(new Open(NAME), session2);
    pins(2, NAME);
    ok(new Close(), session1);
    pins(1, NAME);
    ok(new Close(), session2);
    pins(0, NAME);
  }

  /** Stops the server. */
  @AfterClass
  public static void stop() {
    try {
      session1.close();
      session2.close();
    } catch(final Exception ex) {
      throw new AssertionError(ex.toString());
    }

    // Stop server instance.
    new BaseXServer("stop");
  }

  /**
   * Checks the number of database pins for the specified database.
   * @param pin expected number of pins
   * @param name name of database
   */
  private void pins(final int pin, final String name) {
    assertEquals(pin, server.context.pool.pins(name));
  }

  /**
   * Assumes that this command is successful.
   * @param pr process reference
   * @param s Session
   */
  void ok(final Proc pr, final Session s) {
    final String msg = process(pr, s);
    if(msg != null) fail(msg);
  }

  /**
   * Assumes that this command fails.
   * @param pr process reference
   * @param s Session
   */
  private void no(final Proc pr, final Session s) {
    ok(process(pr, s) != null);
  }

  /**
   * Assumes that the specified flag is successful.
   * @param flag flag
   */
  private static void ok(final boolean flag) {
    assertTrue(flag);
  }

  /**
   * Runs the specified process.
   * @param pr process reference
   * @param session Session
   * @return success flag
   */
  private String process(final Proc pr, final Session session) {
    try {
      return session.execute(pr) ? null : session.info();
    } catch(final Exception ex) {
      return ex.toString();
    }
  }
}
