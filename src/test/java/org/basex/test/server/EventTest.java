package org.basex.test.server;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import org.basex.BaseXServer;
import org.basex.core.BaseXException;
import org.basex.server.ClientSession;
import org.basex.server.EventNotifier;
import org.basex.util.Util;
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
  /** Event name. */
  static final String NAME = Util.name(EventTest.class);

  /** Return value of function db:event. */
  private static final String RETURN = "ABCDEFGHIJKLMNOP";
  /** Event count. */
  private static final int EVENT_COUNT = 50;
  /** Client count. */
  private static final int CLIENTS = 50;

  /** Server reference. */
  private static BaseXServer server;
  /** Admin session. */
  private ClientSession session;
  /** Control client sessions. */
  private final ClientSession[] sessions = new ClientSession[CLIENTS];

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    server = new BaseXServer("-z");
  }

  /**
   * Starts the sessions.
   * @throws Exception exception
   */
  @Before
  public void startSessions() throws Exception {
    session = newSession();
    // drop event, if not done yet
    try {
      session.execute("drop event " + NAME);
    } catch(final BaseXException e) { }

    for(int i = 0; i < sessions.length; i++) sessions[i] = newSession();
  }

  /**
   * Stops the sessions.
   * @throws Exception exception
   */
  @After
  public void stopSessions() throws Exception {
    for(final ClientSession cs : sessions) cs.close();
    session.close();
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
    final String[] events = new String[EVENT_COUNT];
    for(int i = 0; i < EVENT_COUNT; i++) events[i] = NAME + i;
    Arrays.sort(events);

    // create event
    for(final String e : events) session.execute("create event " + e);

    // query must return all events
    String result = session.execute("show events");
    result = result.substring(result.indexOf('\n') + 1);
    result = result.replaceAll("- ", "");

    // compare events
    final String[] names = result.split("\\r?\\n");
    Arrays.sort(names);
    assertTrue(Arrays.equals(events, names));

    // drop events
    for(final String e : events) session.execute("drop event " + e);

    // query must not return any event
    assertEquals("0 ", session.execute("show events").substring(0, 2));
  }

  /**
   * Watches and unwatches events.
   * @throws Exception exception
   */
  @Test
  public void watchUnwatch() throws Exception {
    // create event
    session.execute("create event " + NAME);
    // create event
    try {
      session.execute("create event " + NAME);
      fail("This was supposed to fail.");
    } catch(final BaseXException e) { }

    // watch an event
    for(final ClientSession cs : sessions) {
      cs.watch(NAME, new EventNotifier() {
        @Override
        public void notify(final String data) { }
      });
    }
    // watch an unknown event
    try {
      for(final ClientSession cs : sessions) {
        cs.watch(NAME + 1, new EventNotifier() {
          @Override
          public void notify(final String data) { }
        });
        fail("This was supposed to fail.");
      }
    } catch(final BaseXException e) { }

    // unwatch event
    for(final ClientSession cs : sessions) {
      cs.unwatch(NAME);
    }
    // unwatch unknown event
    try {
      for(final ClientSession cs : sessions) {
        cs.unwatch(NAME + 1);
        fail("This was supposed to fail.");
      }
    } catch(final BaseXException e) { }

    // drop the event
    session.execute("drop event " + NAME);
    // drop event
    try {
      session.execute("drop event " + NAME);
      fail("This was supposed to fail.");
    } catch(final BaseXException e) { }
  }

  /**
   * Runs event test with specified second query and without.
   * @throws BaseXException command exception
   */
  @Test
  public void event() throws BaseXException {
    // create the event
    session.execute("create event " + NAME);
    // watch event
    for(final ClientSession cs : sessions) {
      cs.watch(NAME, new EventNotifier() {
        @Override
        public void notify(final String data) {
          assertEquals(RETURN, data);
        }
      });
    }
    // fire an event
    session.query("db:event('" + NAME + "', '" + RETURN + "')").execute();

    // all clients unwatch the events
    for(final ClientSession cs : sessions) cs.unwatch(NAME);

    // drop event
    session.execute("drop event " + NAME);
  }

  /**
   * Concurrent events.
   * @throws BaseXException command exception
   * @throws Exception exception
   */
  @Test
  public void concurrent() throws Exception {
    // create events
    session.execute("create event " + NAME);
    session.execute("create event " + NAME + 1);

    // watch events on all clients
    for(final ClientSession cs : sessions) {
      cs.watch(NAME, new EventNotifier() {
        @Override
        public void notify(final String data) {
          assertEquals(RETURN, data);
        }
      });
      cs.watch(NAME + 1, new EventNotifier() {
        @Override
        public void notify(final String data) {
          assertEquals(RETURN, data);
        }
      });
    }

    // fire events
    final Client[] clients = new Client[CLIENTS];
    for(int i = 0; i < sessions.length; i++) {
      clients[i] = new Client(i % 2 == 0, RETURN);
    }
    for(final Client c : clients) c.start();
    for(final Client c : clients) c.join();

    // unwatch events
    for(final ClientSession cs : sessions) {
      cs.unwatch(NAME);
      cs.unwatch(NAME + 1);
    }

    // drop event
    session.execute("drop event " + NAME);
    session.execute("drop event " + NAME + 1);
  }

  /**
   * Returns a session instance.
   * @return session
   * @throws IOException exception
   */
  static ClientSession newSession() throws IOException {
    return new ClientSession(server.context, ADMIN, ADMIN);
  }

  /** Single client. */
  private static final class Client extends Thread {
    /** Client session. */
    private final ClientSession cs;
    /** First event. */
    private final boolean first;
    /** Event value. */
    private final String value;

    /**
     * Default constructor.
     * @param f first flag
     * @param v value
     * @throws IOException I/O exception
     */
    public Client(final boolean f, final String v) throws IOException {
      cs = newSession();
      first = f;
      value = v;
    }

    @Override
    public void run() {
      try {
        String name = NAME;
        if(!first) name += 1;
        cs.query("db:event('" + name + "', '" + value + "')").execute();
        cs.close();
      } catch(final Exception e) {
        e.printStackTrace();
      }
    }
  }
}
