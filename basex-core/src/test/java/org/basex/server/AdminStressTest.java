package org.basex.server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Admin stress test.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 */
public final class AdminStressTest extends SandboxTest {
  /** Number of clients. */
  private static final int NUM = 100;
  /** Server reference. */
  private static BaseXServer server;

  /**
   * Starts the server.
   * @throws IOException I/O exception
   */
  @BeforeAll public static void start() throws IOException {
    server = createServer();
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterAll public static void stop() throws IOException {
    stopServer(server);
  }

  /**
   * Test simultaneous client sessions.
   * @throws Exception exception
   */
  @Test public void createAndListSessions() throws Exception {
    final CountDownLatch start = new CountDownLatch(1);
    final CountDownLatch stop = new CountDownLatch(NUM);
    final Client[] clients = new Client[NUM];
    for(int i = 0; i < NUM; ++i) clients[i] = new Client(new ShowSessions(), start, stop);
    start.countDown(); // start all clients
    stop.await();
    for(final Client c : clients) {
      if(c.error != null) fail(c.error);
    }
  }
}
