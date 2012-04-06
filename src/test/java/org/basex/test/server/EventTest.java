package org.basex.test.server;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.basex.*;
import org.basex.server.*;
import org.basex.test.*;
import org.junit.*;

/**
 * This class tests the event API.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Roman Raedle
 * @author Andreas Weiler
 */
public final class EventTest extends SandboxTest {
  /** Return value of function db:event. */
  private static final String RETURN = "ABCDEFGHIJKLMNOP";
  /** Event count. */
  private static final int EVENT_COUNT = 1;
  /** Client count. */
  private static final int CLIENTS = 1;

  /** Server reference. */
  private static BaseXServer server;
  /** Admin session. */
  private ClientSession session;
  /** Control client sessions. */
  private final ClientSession[] sessions = new ClientSession[CLIENTS];

  /**
   * Starts the server.
   * @throws IOException I/O exception
   */
  @BeforeClass
  public static void start() throws IOException {
    server = createServer();
  }

  /**
   * Starts the sessions.
   * @throws IOException I/O exception
   */
  @Before
  public void startSessions() throws IOException {
    session = createClient();
    // drop event, if not done yet
    try {
      session.execute("drop event " + NAME);
    } catch(final IOException ex) { }

    for(int i = 0; i < sessions.length; i++) sessions[i] = createClient();
  }

  /**
   * Stops the sessions.
   * @throws IOException I/O exception
   */
  @After
  public void stopSessions() throws IOException {
    for(final ClientSession cs : sessions) cs.close();
    session.close();
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterClass
  public static void stop() throws IOException {
    server.stop();
  }

  /**
   * Creates and drops events.
   * @throws IOException I/O exception
   */
  @Test
  public void createDrop() throws IOException {
    final String[] events = new String[EVENT_COUNT];
    for(int i = 0; i < EVENT_COUNT; i++) events[i] = NAME + i;

    try {
      // create event
      for(final String e : events) session.execute("create event " + e);

      // query must return all events
      final HashSet<String> names = new HashSet<String>();
      final String result = session.execute("show events");
      for(final String line : result.split("\\r?\\n|\\r"))
        if(line.startsWith("- ")) names.add(line.substring(2));

      for(final String ev : events)
        assertTrue("Event '" + ev + "' not created!", names.contains(ev));
    } finally {
      // drop events as last action, preventing leftovers
      for(final String ev : events) session.execute("drop event " + ev);
    }
  }

  /**
   * Watches and unwatches events.
   * @throws IOException I/O exception
   */
  @Test
  public void watchUnwatch() throws IOException {
    // create event
    session.execute("create event " + NAME);
    // create event
    try {
      session.execute("create event " + NAME);
      fail("This was supposed to fail.");
    } catch(final IOException ex) { /* expected. */ }

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
    } catch(final IOException ex) { /* expected. */ }

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
    } catch(final IOException ex) { /* expected. */ }

    // drop the event
    session.execute("drop event " + NAME);
    // drop event
    try {
      session.execute("drop event " + NAME);
      fail("This was supposed to fail.");
    } catch(final IOException ex) { /* expected. */ }
  }

  /**
   * Runs event test with specified second query and without.
   * @throws IOException I/O exception
   */
  @Test
  public void event() throws IOException {
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

  /** Single client. */
  static final class Client extends Thread {
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
      cs = createClient();
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
      } catch(final Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
