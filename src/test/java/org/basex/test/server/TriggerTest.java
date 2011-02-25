package org.basex.test.server;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import org.basex.BaseXServer;
import org.basex.core.BaseXException;
import org.basex.server.ClientSession;
import org.basex.server.trigger.TriggerNotification;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the trigger API.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Roman Raedle
 * @author Andreas Weiler
 */
public final class TriggerTest {
  /** Trigger count. */
  private static final int TRIGGER_COUNT = 10;
  /** Trigger name. */
  private static final String TRIGGER_NAME = "Trigger";
  /** Return value of function util:trigger. */
  private static final String RETURN_VALUE = "GOT TRIGGERED";
  /** Server reference. */
  private static BaseXServer server;
  /** Client session. */
  private ClientSession cs;
  /** Control client sessions. */
  private ClientSession[] ccs = new ClientSession[10];

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    server = new BaseXServer();
  }

  /** Starts all sessions. */
  @Before
  public void startSession() {
    try {
      cs = newSession();
      for(int i = 0; i < ccs.length; i++) {
        ccs[i] = newSession();
      }
    } catch(final IOException ex) {
      fail(ex.toString());
    }
  }

  /** Stops all sessions. */
  @After
  public void stopSession() {
    try {
      cs.close();
      for(ClientSession s : ccs)
        s.close();
    } catch(final IOException ex) {
      fail(ex.toString());
    }
  }

  /** Stops the server. */
  @AfterClass
  public static void stop() {
    server.stop();
  }

  /**
   * Creates triggers.
   * @throws BaseXException command exception
   */
  @Test
  public void create() throws BaseXException {
    for(int i = 1; i < TRIGGER_COUNT; i++) {
      cs.createTrigger(TRIGGER_NAME + i);
    }

    String triggers = cs.execute("show triggers");

    String[] triggerNames = triggers.split("\n");
    Arrays.sort(triggerNames);
    for(int i = 1; i < TRIGGER_COUNT; i++) {
      assertEquals(TRIGGER_NAME + i, triggerNames[i]);
    }
  }

  /**
   * Runs a query command and retrieves the result as string.
   * @throws BaseXException command exception
   */
  @Test
  public void trigger() throws BaseXException {

    // Create a trigger.
    cs.createTrigger(TRIGGER_NAME);

    // Attach half of the clients to the trigger.
    for(int i = ccs.length / 2; i < ccs.length; i++) {
      ccs[i].attachTrigger(TRIGGER_NAME, new TriggerNotification() {
        @Override
        public void update(final String data) {
          assertEquals(RETURN_VALUE, data);
        }
      });
    }

    // Release a trigger.
    cs.trigger("1 to 10", TRIGGER_NAME, RETURN_VALUE);

    // Detach all clients attached to trigger beforehand.
    for(int i = ccs.length / 2; i < ccs.length; i++) {
      ccs[i].detachTrigger(TRIGGER_NAME);
    }

    // Drop trigger.
    cs.dropTrigger(TRIGGER_NAME);
  }

  /**
   * Runs queries and triggers concurrently.
   * @throws BaseXException command exception
   */
  @Test
  public void concurrent() throws BaseXException {

    // Create first trigger.
    cs.createTrigger(TRIGGER_NAME);
    // Drop second trigger.
    cs.dropTrigger(TRIGGER_NAME + 1);
    // Create second trigger.
    cs.createTrigger(TRIGGER_NAME + 1);

    // Attach half of the clients to the trigger and run a query.
    for(int i = ccs.length / 2; i < ccs.length; i++) {
      ccs[i].attachTrigger(TRIGGER_NAME, new TriggerNotification() {
        @Override
        public void update(final String data) {
          assertEquals(RETURN_VALUE, data);
        }
      });
      assertEquals(ccs[i].execute("xquery 1 + 100"), "101");
    }

    // Attach half of the clients to the trigger and run a query.
    for(int i = ccs.length / 2; i < ccs.length; i++) {
      ccs[i].attachTrigger(TRIGGER_NAME + 1, new TriggerNotification() {
        @Override
        public void update(final String data) {
          assertEquals(RETURN_VALUE, data);
        }
      });
      assertEquals(ccs[i].execute("xquery 1 + 100"), "101");
    }

    // Release first trigger.
    cs.trigger("1 to 10", TRIGGER_NAME, RETURN_VALUE);

    // Run for half of the clients a query.
    for(int i = ccs.length / 2; i < ccs.length; i++) {
      assertEquals(ccs[i].execute("xquery 1 + 100"), "101");
    }

    // Release second trigger.
    cs.trigger("1 to 10", TRIGGER_NAME + 1, RETURN_VALUE);

    // Detach all clients attached to trigger beforehand.
    for(int i = ccs.length / 2; i < ccs.length; i++) {
      ccs[i].detachTrigger(TRIGGER_NAME);
    }

    // Drop trigger.
    cs.dropTrigger(TRIGGER_NAME);
  }

  /**
   * Drops triggers.
   * @throws BaseXException command exception
   */
  @Test
  public void drop() throws BaseXException {

    // Drop triggers.
    for(int i = 1; i < TRIGGER_COUNT; i++) {
      cs.dropTrigger(TRIGGER_NAME + i);
    }

    String triggers = cs.execute("show triggers");
    // Query must not return any trigger.
    assertEquals("0", triggers.substring(0, 1));
  }

  /**
   * Returns a session instance.
   * @return session
   * @throws IOException exception
   */
  static ClientSession newSession() throws IOException {
    return new ClientSession("localhost", 1984, "admin", "admin");
  }
}
