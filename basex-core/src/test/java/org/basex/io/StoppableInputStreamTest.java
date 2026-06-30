package org.basex.io;

import static java.util.concurrent.TimeUnit.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.basex.*;
import org.basex.core.cmd.XQuery;
import org.basex.util.*;
import org.junit.jupiter.api.*;

import com.sun.net.httpserver.*;

/**
 * Tests that blocking reads of an HTTP response body are aborted when the running job is stopped.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StoppableInputStreamTest extends SandboxTest {
  /** Released once per request after the server has sent the headers and stalls the body. */
  private static final Semaphore STALLING = new Semaphore(0);
  /** Released on teardown to let the stalling handlers finish. */
  private static final CountDownLatch RELEASE = new CountDownLatch(1);
  /** HTTP server that sends response headers and then stalls the body. */
  private static HttpServer server;

  /**
   * Starts the stalling HTTP server.
   * @throws IOException I/O exception
   */
  @BeforeAll public static void start() throws IOException {
    server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    server.setExecutor(Executors.newCachedThreadPool(runnable -> {
      final Thread thread = new Thread(runnable);
      thread.setDaemon(true);
      return thread;
    }));
    server.createContext("/ok", exchange -> {
      final byte[] body = "hello".getBytes();
      exchange.getResponseHeaders().add("Content-Type", "text/plain");
      exchange.sendResponseHeaders(200, body.length);
      try(OutputStream os = exchange.getResponseBody()) {
        os.write(body);
      }
    });
    server.createContext("/stall", exchange -> {
      // send headers and a first chunk, then block until the test tears down
      exchange.sendResponseHeaders(200, 0);
      try(OutputStream os = exchange.getResponseBody()) {
        os.write("partial".getBytes());
        os.flush();
        STALLING.release();
        RELEASE.await(60, SECONDS);
      } catch(final InterruptedException ex) {
        Util.debug(ex);
      } finally {
        exchange.close();
      }
    });
    server.start();
  }

  /** Stops the HTTP server. */
  @AfterAll public static void stop() {
    RELEASE.countDown();
    server.stop(0);
  }

  /** A regular request reads its response body unaffected by the stoppable wrapper. */
  @Test public void normalRequestSucceeds() {
    assertEquals("hello", query(
        "http:send-request(<http:request method='GET' href='" + url("/ok") + "'/>)[2]"));
  }

  /**
   * Stopping a job aborts a request whose response body has stalled mid-stream.
   * @throws Exception exception
   */
  @Test public void stopInterruptsStalledBody() throws Exception {
    assertInterrupted("http:send-request(<http:request method='GET' href='" +
        url("/stall") + "'/>)");
  }

  /**
   * Stopping a job aborts a stalled request that runs in a parallel fork-join branch.
   * @throws Exception exception
   */
  @Test public void stopInterruptsParallelBranch() throws Exception {
    final String req = "http:send-request(<http:request method='GET' href='" +
        url("/stall") + "'/>)";
    assertInterrupted("xquery:fork-join((function() { " + req + " }, function() { " + req + " }))");
  }

  /**
   * Runs a query on a background thread, stops it once a stalled body read is in progress, and
   * asserts that it terminates promptly with an error.
   * @param query query to run
   * @throws Exception exception
   */
  private static void assertInterrupted(final String query) throws Exception {
    final XQuery cmd = new XQuery(query);
    final AtomicReference<Throwable> caught = new AtomicReference<>();
    final Thread worker = new Thread(() -> {
      try {
        cmd.execute(context);
      } catch(final Throwable th) {
        caught.set(th);
      }
    });
    worker.start();

    // wait until a body read is in progress, then stop the running command
    assertTrue(STALLING.tryAcquire(10, SECONDS), "server should have started stalling the body");
    cmd.stop();

    worker.join(10_000);
    assertFalse(worker.isAlive(), "stalled body read should have been interrupted");
    assertNotNull(caught.get(), "interrupted request should have raised an exception");
  }

  /**
   * Returns the URL of the local server for the given path.
   * @param path request path
   * @return URL
   */
  private static String url(final String path) {
    return "http://" + server.getAddress().getHostString() + ':' +
        server.getAddress().getPort() + path;
  }
}
