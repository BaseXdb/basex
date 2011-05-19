package org.basex.test.server;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import org.basex.BaseXServer;
import org.basex.core.BaseXException;
import org.basex.server.ClientSession;
import org.basex.server.EventNotification;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the event API.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Roman Raedle
 * @author Andreas Weiler
 */
public final class EventTest {
  /** Event count. */
  private static final int EVENT_COUNT = 10;
  /** Event name. */
  private static final String EVENT_NAME = "event";
  /** Return value of function util:event. */
  private static final String RETURN_VALUE = "event received";
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
   * Creates and drops events.
   * @throws BaseXException command exception
   */
  @Test
  public void createDrop() throws BaseXException {

    // create event
    for(int i = 1; i < EVENT_COUNT; i++) {
      cs.execute("create event " + EVENT_NAME + i);
    }
    // query must not contain all events
    String events = cs.execute("show events");
    String[] eventNames = events.split("\\r?\\n");
    Arrays.sort(eventNames);
    for(int i = 1; i < EVENT_COUNT; i++) {
      assertEquals(EVENT_NAME + i, eventNames[i]);
    }

    // drop event
    for(int i = 1; i < EVENT_COUNT; i++) {
      cs.execute("drop event " + EVENT_NAME + i);
    }
    // query must not return any event
    events = cs.execute("show events");
    assertEquals("0", events.substring(0, 1));
  }

  /**
   * Watches and unwatches events.
   */
  @Test
  public void watchUnwatch() {
    // create event
    try {
      cs.execute("create event " + EVENT_NAME);
    } catch(BaseXException e) {
      fail(e.getMessage());
    }

    // create event
    try {
      cs.execute("create event " + EVENT_NAME);
      fail("This was supposed to fail.");
    } catch(BaseXException e) { }

    // watch an event
    try {
      for(int i = ccs.length / 2; i < ccs.length; i++) {
        ccs[i].watchEvent(EVENT_NAME, new EventNotification() {
          @Override
          public void update(final String data) { }
        });
      }
    } catch(BaseXException e) {
      fail(e.getMessage());
    }

    // watch an event
    try {
      for(int i = ccs.length / 2; i < ccs.length; i++) {
        ccs[i].watchEvent(EVENT_NAME + 1, new EventNotification() {
          @Override
          public void update(final String data) { }
        });
        fail("This was supposed to fail.");
      }
    } catch(BaseXException e) { }

    // unwatch event
    try {
      for(int i = ccs.length / 2; i < ccs.length; i++) {
        ccs[i].unwatchEvent(EVENT_NAME);
      }
    } catch(BaseXException e) {
      fail(e.getMessage());
    }

    // unwatch event
    try {
      for(int i = ccs.length / 2; i < ccs.length; i++) {
        ccs[i].unwatchEvent(EVENT_NAME + 1);
        fail("This was supposed to fail.");
      }
    } catch(BaseXException e) { }

    // drop the event
    try {
      cs.execute("drop event " + EVENT_NAME);
    } catch(BaseXException e) {
      fail(e.getMessage());
    }
  }

  /**
   * Runs a query command and retrieves the result as string.
   * @throws BaseXException command exception
   */
  @Test
  public void event() throws BaseXException {

    // create the event
    cs.execute("create event " + EVENT_NAME);
    // watch event with half of the clients
    for(int i = ccs.length / 2; i < ccs.length; i++) {
      ccs[i].watchEvent(EVENT_NAME, new EventNotification() {
        @Override
        public void update(final String data) {
          assertEquals(RETURN_VALUE, data);
        }
      });
    }

    // release an event
    cs.event("1 to 10", EVENT_NAME, RETURN_VALUE);

    // all clients unwatch the events
    for(int i = ccs.length / 2; i < ccs.length; i++) {
      ccs[i].unwatchEvent(EVENT_NAME);
    }

    // drop event
    cs.execute("drop event " + EVENT_NAME);
  }

  /**
   * Concurrent events.
   * @throws BaseXException command exception
   * @throws Exception exception
   */
  @Test
  public void concurrent() throws Exception {
    // create event.
    cs.execute("create event " + EVENT_NAME);
    cs.execute("create event " + EVENT_NAME + 1);

    // watch event with half of the clients
    for(int i = ccs.length / 2; i < ccs.length; i++) {
      ccs[i].watchEvent(EVENT_NAME, new EventNotification() {
        @Override
        public void update(final String data) {
          assertEquals(RETURN_VALUE, data);
        }
      });
      ccs[i].watchEvent(EVENT_NAME + 1, new EventNotification() {
        @Override
        public void update(final String data) {
          assertEquals(RETURN_VALUE, data);
        }
      });
    }

    // concurrent event activation
    Client c1 = new Client(true);
    Client c2 = new Client(false);
    c1.start();
    c2.start();
    c1.join();
    c2.join();

    // drop event
    cs.execute("drop event " + EVENT_NAME);
    cs.execute("drop event " + EVENT_NAME + 1);
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
    /** First event. */
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
          session.event("1 to 10", EVENT_NAME, RETURN_VALUE);
        } else {
          session.event("1 to 10", EVENT_NAME + 1, RETURN_VALUE);
        }
        session.close();
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
}
