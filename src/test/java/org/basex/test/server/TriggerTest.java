package org.basex.test.server;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import org.basex.BaseXServer;
import org.basex.core.BaseXException;
import org.basex.server.ClientSession;
import org.basex.server.trigger.TriggerEvent;
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
  private static final String TRIGGER_NAME = "trigger";
  /** Return value of function util:trigger. */
  private static final String RETURN_VALUE = "TRIGGERED";
  /** Server reference. */
  private static BaseXServer server;
  /** Client session. */
  private static ClientSession cs;
  /** Control client sessions. */
  private static ClientSession[] ccs = new ClientSession[10];

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    server = new BaseXServer();
  }

  /** Starts the server. */
  @Before
  public void startSessions() {
    try {
      cs = newSession();
      for(int i = 0; i < ccs.length; i++) {
        ccs[i] = newSession();
      }
    } catch(final IOException ex) {
      fail(ex.toString());
    }
  }

  /** Starts the server. */
  @After
  public void stopSessions() {
    try {
      for(int i = 0; i < ccs.length; i++) {
        ccs[i].close();
      }
      cs.close();
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
   * Creates and drops triggers.
   * @throws BaseXException command exception
   */
  @Test
  public void createDrop() throws BaseXException {

    // create trigger
    for(int i = 1; i < TRIGGER_COUNT; i++) {
      cs.execute("create trigger " + TRIGGER_NAME + i);
    }
    // query must not contain all triggers
    String triggers = cs.execute("show triggers");
    String[] triggerNames = triggers.split("\\r?\\n");
    Arrays.sort(triggerNames);
    for(int i = 1; i < TRIGGER_COUNT; i++) {
      assertEquals(TRIGGER_NAME + i, triggerNames[i]);
    }

    // drop trigger
    for(int i = 1; i < TRIGGER_COUNT; i++) {
      cs.execute("drop trigger " + TRIGGER_NAME + i);
    }
    // query must not return any trigger
    triggers = cs.execute("show triggers");
    assertEquals("0", triggers.substring(0, 1));
  }

  /**
   * Attaches and detaches clients.
   */
  @Test
  public void attachDetach() {
    // create trigger
    try {
      cs.execute("create trigger " + TRIGGER_NAME);
    } catch(BaseXException e) {
      fail(e.getMessage());
    }

    // create trigger
    try {
      cs.execute("create trigger " + TRIGGER_NAME);
      fail("This was supposed to fail.");
    } catch(BaseXException e) { }

    // attach at trigger
    try {
      for(int i = ccs.length / 2; i < ccs.length; i++) {
        ccs[i].attachTrigger(TRIGGER_NAME, new TriggerEvent() {
          @Override
          public void update(final String data) { }
        });
      }
    } catch(BaseXException e) {
      fail(e.getMessage());
    }

    // attach at trigger
    try {
      for(int i = ccs.length / 2; i < ccs.length; i++) {
        ccs[i].attachTrigger(TRIGGER_NAME + 1, new TriggerEvent() {
          @Override
          public void update(final String data) { }
        });
        fail("This was supposed to fail.");
      }
    } catch(BaseXException e) { }

    // detach from trigger
    try {
      for(int i = ccs.length / 2; i < ccs.length; i++) {
        ccs[i].detachTrigger(TRIGGER_NAME);
      }
    } catch(BaseXException e) {
      fail(e.getMessage());
    }

    // detach from trigger
    try {
      for(int i = ccs.length / 2; i < ccs.length; i++) {
        ccs[i].detachTrigger(TRIGGER_NAME + 1);
        fail("This was supposed to fail.");
      }
    } catch(BaseXException e) { }

    // drop the trigger
    try {
      cs.execute("drop trigger " + TRIGGER_NAME);
    } catch(BaseXException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Runs a query command and retrieves the result as string.
   * @throws BaseXException command exception
   */
  @Test
  public void trigger() throws BaseXException {

    // create the trigger
    cs.execute("create trigger " + TRIGGER_NAME);
    // attach half of the clients to the trigger
    for(int i = ccs.length / 2; i < ccs.length; i++) {
      ccs[i].attachTrigger(TRIGGER_NAME, new TriggerEvent() {
        @Override
        public void update(final String data) {
          assertEquals(RETURN_VALUE, data);
        }
      });
    }

    // release a trigger
    cs.trigger("1 to 10", TRIGGER_NAME, RETURN_VALUE, "m");

    // detach all clients attached to trigger beforehand
    for(int i = ccs.length / 2; i < ccs.length; i++) {
      ccs[i].detachTrigger(TRIGGER_NAME);
    }

    // drop trigger
    cs.execute("drop trigger " + TRIGGER_NAME);
  }

  /**
   * Concurrent triggers.
   * @throws BaseXException command exception
   * @throws Exception exception
   */
  @Test
  public void concurrent() throws Exception {
    // create trigger.
    cs.execute("create trigger " + TRIGGER_NAME);
    cs.execute("create trigger " + TRIGGER_NAME + 1);

    // attach half of the clients to the triggers
    for(int i = ccs.length / 2; i < ccs.length; i++) {
      ccs[i].attachTrigger(TRIGGER_NAME, new TriggerEvent() {
        @Override
        public void update(final String data) {
          assertEquals(RETURN_VALUE, data);
        }
      });
      ccs[i].attachTrigger(TRIGGER_NAME + 1, new TriggerEvent() {
        @Override
        public void update(final String data) {
          assertEquals(RETURN_VALUE, data);
        }
      });
    }

    // concurrent trigger activation
    Client c1 = new Client(true);
    Client c2 = new Client(false);
    c1.start();
    c2.start();
    c1.join();
    c2.join();

    // drop trigger
    cs.execute("drop trigger " + TRIGGER_NAME);
    cs.execute("drop trigger " + TRIGGER_NAME + 1);
  }

  /**
   * Returns a session instance.
   * @return session
   * @throws IOException exception
   */
  static ClientSession newSession() throws IOException {
    return new ClientSession("localhost", 1984, "admin", "admin");
  }

  /** Single client. */
  static final class Client extends Thread {
    /** Client session. */
    private ClientSession session;
    /** First trigger. */
    private boolean first;

    /**
     * Default constructor.
     * @param f first flag
     */
    public Client(final boolean f) {
      this.first = f;
      try {
        session = newSession();
      } catch(final IOException ex) {
        ex.printStackTrace();
      }
    }

    @Override
    public void run() {
      try {
        if(first) {
          session.trigger("1 to 10", TRIGGER_NAME, RETURN_VALUE, "m");
        } else {
          session.trigger("1 to 10", TRIGGER_NAME + 1, RETURN_VALUE, "m");
        }
        session.close();
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
}
