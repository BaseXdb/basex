package org.basex.test.server;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;
import org.basex.BaseXServer;
import org.basex.core.BaseXException;
import org.basex.core.Command;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;
import org.basex.server.ClientSession;
import org.basex.server.Session;
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
  /** Test database name. */
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
    server = new BaseXServer("-z");
    try {
      session1 = new ClientSession(server.context, ADMIN, ADMIN);
      session2 = new ClientSession(server.context, ADMIN, ADMIN);
    } catch(final Exception ex) {
      fail(ex.toString());
    }
  }

  /** Create and Drop Tests. */
  @Test
  public void createAndDrop() {
    ok(new CreateDB(NAME, FILE), session1);
    pins(1, NAME);
    ok(new CreateDB(NAME, FILE), session1);
    pins(1, NAME);
    no(new CreateDB(NAME, FILE), session2);
    pins(1, NAME);
    no(new CreateDB(NAME, FILE), session2);
    pins(1, NAME);
    no(new DropDB(NAME), session2);
    pins(1, NAME);
    ok(new DropDB(NAME), session1);
    pins(0, NAME);
  }

  /** Close and Open Tests. */
  @Test
  public void closeAndOpen() {
    ok(new CreateDB(NAME, FILE), session2);
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
      fail(ex.toString());
    }
    // stop server instance
    server.stop();
  }

  /**
   * Checks the number of database pins for the specified database.
   * @param pin expected number of pins
   * @param name name of database
   */
  private void pins(final int pin, final String name) {
    assertEquals(pin, server.context.datas.pins(name));
  }

  /**
   * Assumes that this command is successful.
   * @param cmd command reference
   * @param s Session
   */
  void ok(final Command cmd, final Session s) {
    try {
      s.execute(cmd);
    } catch(final BaseXException ex) {
      fail(ex.getMessage());
    }
  }

  /**
   * Assumes that this command fails.
   * @param cmd command reference
   * @param s Session
   */
  private void no(final Command cmd, final Session s) {
    try {
      s.execute(cmd);
      fail("Command was supposed to fail.");
    } catch(final BaseXException ex) {
    }
  }
}
