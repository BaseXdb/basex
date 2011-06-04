package org.basex.test.server;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.basex.BaseXServer;
import org.basex.core.BaseXException;
import org.basex.server.ClientSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/** Test events. */
public final class EventStressTest {
  /** Number of clients/events. */
  private static final int NUM = 100;
  /** Event name prefix. */
  private static final String NAME = "myevent";
  /** Server reference. */
  private static BaseXServer server;

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    server = new BaseXServer("-z");
  }

  /** Stops the server. */
  @AfterClass
  public static void stop() {
    server.stop();
  }

  /**
   * Start simultaneously clients which create events and clients which list all
   * events.
   * @throws IOException I/O exception while establishing a session
   */
  @Test
  public void testCreateAndListEvents() throws IOException {
    final CountDownLatch startSignal = new CountDownLatch(1);
    final CountDownLatch stopSignal = new CountDownLatch(NUM);

    for(int i = 0; i < NUM; ++i) {
      new Client("create event " + NAME + i, startSignal, stopSignal).start();
      new Client("show events", startSignal, stopSignal).start();
    }

    startSignal.countDown(); // start all clients
    try {
      stopSignal.await();
    } catch(InterruptedException e) {
      return;
    }
  }

  /**
   * Start simultaneously clients which create events and clients which list all
   * events.
   * @throws IOException I/O exception while establishing a session
   */
  @Test
  public void testCreateAndListSessions() throws IOException {
    final CountDownLatch startSignal = new CountDownLatch(1);
    final CountDownLatch stopSignal = new CountDownLatch(NUM);

    for(int i = 0; i < NUM; ++i) {
      new Client("show sessions", startSignal, stopSignal).start();
    }

    startSignal.countDown(); // start all clients
    try {
      stopSignal.await();
    } catch(InterruptedException e) {
      return;
    }
  }

  /**
   * Clean up the mess.
   * @throws IOException I/O exception
   */
  @After
  public void cleanUp() throws IOException {
    final ClientSession session = new ClientSession("localhost", 1984, "admin",
        "admin", null);
    try {
      for(int i = 0; i < NUM; ++i) {
        try {
          session.execute("drop event " + NAME + i);
        } catch(BaseXException e) { }
      }
    } finally {
      session.close();
    }
  }

  /** Client. */
  private static final class Client extends Thread {
    /** Client session. */
    private final ClientSession session;
    /** Command string. */
    private final String cmd;
    /** Start signal. */
    private final CountDownLatch startSignal;
    /** Stop signal. */
    private final CountDownLatch stopSignal;

    /**
     * Client constructor.
     * @param c command string to execute
     * @param start start signal
     * @param stop stop signal
     * @throws IOException I/O exception while establishing the session
     */
    public Client(final String c, final CountDownLatch start,
        final CountDownLatch stop) throws IOException {
      session = new ClientSession("localhost", 1984, "admin", "admin", null);
      cmd = c;
      startSignal = start;
      stopSignal = stop;
    }

    @Override
    public void run() {
      try {
        startSignal.await();
        try {
          session.execute(cmd);
        } catch(final BaseXException e) {
          e.printStackTrace();
        } finally {
          session.close();
        }
      } catch(final IOException e) {
        e.printStackTrace();
      } catch(final InterruptedException e) {
        return;
      } finally {
        stopSignal.countDown();
      }
    }
  }
}
