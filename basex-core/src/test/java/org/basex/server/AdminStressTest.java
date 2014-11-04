package org.basex.server;

import static org.junit.Assert.*;

import java.io.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.cmd.*;
import org.basex.util.*;
import org.junit.*;
import org.junit.Test;

/**
 * Admin stress test.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Dimitar Popov
 */
public final class AdminStressTest extends SandboxTest {
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
    server = createServer();
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterClass
  public static void stop() throws IOException {
    stopServer(server);
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

    final Client[] c1 = new Client[NUM];
    final Client[] c2 = new Client[NUM];
    for(int i = 0; i < NUM; ++i) {
      c1[i] = new Client(new CreateEvent(NAME + i), start, stop);
      c2[i] = new Client(new ShowEvents(), start, stop);
    }
    start.countDown(); // start all clients
    stop.await();

    Performance.sleep(200);
    try(final ClientSession cs = createClient()) {
      for(int i = 0; i < NUM; ++i) cs.execute("drop event " + NAME + i);
    }

    for(final Client c : c1) if(c.error != null) fail(c.error);
    for(final Client c : c2) if(c.error != null) fail(c.error);
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
    final Client[] clients = new Client[NUM];
    for(int i = 0; i < NUM; ++i) clients[i] = new Client(new ShowSessions(), start, stop);
    start.countDown(); // start all clients
    stop.await();
    for(final Client c : clients) if(c.error != null) fail(c.error);
  }
}
