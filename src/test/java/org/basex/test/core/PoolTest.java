package org.basex.test.core;

import static org.junit.Assert.*;

import java.io.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.server.*;
import org.basex.test.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests transaction and locking cases.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Andreas Weiler
 */
public final class PoolTest extends SandboxTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/input.xml";

  /** Server reference. */
  private static BaseXServer server;
  /** Socket reference. */
  private static Session session1;
  /** Socket reference. */
  private static Session session2;

  /**
   * Starts the server.
   * @throws Exception exception
   */
  @BeforeClass
  public static void start() throws Exception {
    server = createServer();
    session1 = createClient();
    session2 = createClient();
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterClass
  public static void stop() throws IOException {
    session1.close();
    session2.close();
    stopServer(server);
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
    ok(new DropDB(NAME), session1);
  }

  /**
   * Checks the number of database pins for the specified database.
   * @param pin expected number of pins
   * @param name name of database
   */
  private static void pins(final int pin, final String name) {
    assertEquals(pin, server.context.dbs.pins(name));
  }

  /**
   * Assumes that this command is successful.
   * @param cmd command reference
   * @param session Session
   */
  static void ok(final Command cmd, final Session session) {
    try {
      session.execute(cmd);
    } catch(final IOException ex) {
      fail(Util.message(ex));
    }
  }

  /**
   * Assumes that this command fails.
   * @param cmd command reference
   * @param session Session
   */
  private static void no(final Command cmd, final Session session) {
    try {
      session.execute(cmd);
      fail("Command was supposed to fail.");
    } catch(final IOException ex) {
    }
  }
}
