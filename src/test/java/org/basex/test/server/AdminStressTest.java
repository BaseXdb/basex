package org.basex.test.server;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.basex.BaseXServer;
import org.basex.core.Text;
import org.basex.server.ClientSession;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Admin stress test.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Dimitar Popov
 */
public final class AdminStressTest {
  /** Test name. */
  private static final String NAME = Util.name(AdminStressTest.class);
  /** Number of clients/events. */
  private static final int NUM = 100;
  /** Server reference. */
  private static BaseXServer server;

  /**
   * Starts the server.
   * @throws IOException I/O exception
   */
  @BeforeClass
  public static void start() throws IOException {
    server = new BaseXServer("-z", "-p9999", "-e9998");
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
   * Start simultaneously clients which create events and clients which list all
   * events.
   * @throws Exception exception
   */
  @Test
  public void createAndListEvents() throws Exception {
    final CountDownLatch start = new CountDownLatch(1);
    final CountDownLatch stop = new CountDownLatch(NUM);

    for(int i = 0; i < NUM; ++i) {
      new Client("create event " + NAME + i, start, stop);
      new Client("show events", start, stop);
    }
    start.countDown(); // start all clients
    stop.await();

    final ClientSession cs = new ClientSession(
        Text.LOCALHOST, 9999, Text.ADMIN, Text.ADMIN);
    for(int i = 0; i < NUM; ++i) cs.execute("drop event " + NAME + i);
    cs.close();
  }

  /**
   * Start simultaneously clients which create events and clients which list all
   * events.
   * @throws Exception exception
   */
  @Test
  public void createAndListSessions() throws Exception {
    final CountDownLatch start = new CountDownLatch(1);
    final CountDownLatch stop = new CountDownLatch(NUM);
    for(int i = 0; i < NUM; ++i) new Client("show sessions", start, stop);
    start.countDown(); // start all clients
    stop.await();
  }

  /** Client. */
  private static final class Client extends Thread {
    /** Start signal. */
    private final CountDownLatch startSignal;
    /** Stop signal. */
    private final CountDownLatch stopSignal;
    /** Client session. */
    private final ClientSession session;
    /** Command string. */
    private final String cmd;

    /**
     * Client constructor.
     * @param c command string to execute
     * @param start start signal
     * @param stop stop signal
     * @throws IOException I/O exception while establishing the session
     */
    public Client(final String c, final CountDownLatch start,
        final CountDownLatch stop) throws IOException {
      session = new ClientSession(Text.LOCALHOST, 9999, Text.ADMIN, Text.ADMIN);
      cmd = c;
      startSignal = start;
      stopSignal = stop;
      start();
    }

    @Override
    public void run() {
      try {
        startSignal.await();
        session.execute(cmd);
        session.close();
      } catch(final Exception ex) {
        ex.printStackTrace();
      } finally {
        stopSignal.countDown();
      }
    }
  }
}
