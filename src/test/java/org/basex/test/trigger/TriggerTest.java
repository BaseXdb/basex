package org.basex.test.trigger;

import static org.basex.core.Text.*;
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
 * @author Workgroup HCI, University of Konstanz 2005-10, ISC License
 * @author Roman Raedle
 */
public final class TriggerTest {
  /** Trigger count. */
  private static final int TRIGGER_COUNT = 4;
  /** Trigger name. */
  private static final String TRIGGER_NAME = "myTrigger";
  /** Return value of function util:trigger. */
  private static final String RETURN_VALUE = "return";
  /** Server reference. */
  private static BaseXServer server;
  /** Client session. */
  private ClientSession mc;
  /** Control client sessions. */
  private ClientSession[] ccs = new ClientSession[4];

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    server = new BaseXServer("");
  }

  /** Starts all sessions. */
  @Before
  public void startSession() {
    try {
      mc = new ClientSession(server.context, ADMIN, ADMIN);
      for(int i = 0; i < ccs.length; i++) {
        ccs[i] = new ClientSession(server.context, ADMIN, ADMIN);
      }
    } catch(final IOException ex) {
      fail(ex.toString());
    }
  }

  /** Stops all sessions. */
  @After
  public void stopSession() {
    try {
      mc.close();
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
    for(int i = 0; i < TRIGGER_COUNT; i++) {
      mc.execute("create trigger " + TRIGGER_NAME + i);
    }

    String triggers = mc.execute("show triggers");

    String[] triggerNames = triggers.split("\n");
    Arrays.sort(triggerNames);
    for(int i = 0; i < TRIGGER_COUNT; i++) {
      assertEquals(TRIGGER_NAME + i, triggerNames[i]);
    }
  }

  /**
   * Runs a query command and retrieves the result as string.
   * @throws BaseXException command exception
   */
  @Test
  public void command() throws BaseXException {

    // Create a trigger.
    mc.execute("create trigger " + TRIGGER_NAME);
    
    // Attach half of the clients to the trigger.
    for(int i = ccs.length / 2; i < ccs.length; i++) {
      ccs[i].attachTrigger(TRIGGER_NAME, new TriggerNotification() {
        
        /* (non-Javadoc)
         * @see org.basex.server.trigger.TriggerNotification#income(byte[])
         */
        @Override
        public void update(final String data) {
          assertEquals(RETURN_VALUE, data);
        }
      });
    }

    // Release a trigger.
    mc.trigger("1 to 10", TRIGGER_NAME, RETURN_VALUE);

    // Detach all clients attached to trigger beforehand.
    for(int i = ccs.length / 2; i < ccs.length; i++) {
      ccs[i].detachTrigger(TRIGGER_NAME);
    }
    
    // Create a trigger.
    mc.execute("drop trigger " + TRIGGER_NAME);
  }

  /**
   * Drops triggers.
   * @throws BaseXException command exception
   */
  @Test
  public void drop() throws BaseXException {
    // Create triggers.
    for(int i = 0; i < TRIGGER_COUNT; i++) {
      mc.execute("create trigger " + TRIGGER_NAME + i);
    }
    
    // Delete triggers manually.
    for(int i = 0; i < TRIGGER_COUNT; i++) {
      mc.execute("drop trigger " + TRIGGER_NAME + i);
    }

    String triggers = mc.execute("show triggers");

    // Query must not return any trigger.
    assertEquals("", triggers);
  }
}
