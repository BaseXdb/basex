package org.basex.rest;

import static org.basex.core.users.UserText.*;
import static org.basex.util.http.Method.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.cmd.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.http.MediaType;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Concurrency tests of BaseX REST API.
 *
 * @author BaseX Team, BSD License
 * @author Dimitar Popov
 */
@Timeout(60)
public final class RESTConcurrencyTest extends SandboxTest {
  /** HTTP server. */
  private static BaseXHTTP http;

  /** Time a long-running request occupies the database (ms). */
  private static final long BLOCK = 3000;
  /** Socket time-out (ms): must outlast a request that waits for a lock. */
  private static final int SOCKET_TIMEOUT = 30000;
  /** BaseX HTTP base URL. */
  private static final String REST_URL = REST_ROOT + NAME;

  /**
   * Creates a test database and starts BaseXHTTP.
   * @throws Exception if database cannot be created or server cannot be started
   */
  @BeforeEach public void setUp() throws Exception {
    // -L: start a database server, which the client session below connects to
    final StringList sl = new StringList("-p" + DB_PORT, "-h" + HTTP_PORT, "-s" + STOP_PORT,
        "-c", "password " + NAME, "-U" + ADMIN, "-L", "-z", "-q");
    http = new BaseXHTTP(sl.finish());
    try(ClientSession cs = createClient()) {
      cs.execute(new CreateDB(NAME));
    }
    // warm up: the first request of a server initializes the web application and the query
    // processor, which takes seconds and would invalidate every timing assumption below
    final HTTPResponse response = new Get("?query=1").call();
    assertEquals(200, response.status);
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterEach public void tearDown() throws IOException {
    http.stop();
  }

  /**
   * Test 2 concurrent readers (GH-458).
   * <p><b>Test case:</b>
   * <ol>
   * <li/>start a long running reader;
   * <li/>start a fast reader: it should succeed.
   * </ol>
   * @throws Exception error during request execution
   */
  @Test public void testMultipleReaders() throws Exception {
    final String number = "63177";
    final Get slowAction = new Get(readerQuery());
    final Get fastAction = new Get("?query=" + number);

    final ExecutorService exec = Executors.newFixedThreadPool(2);
    try {
      // start the reader and wait until it holds the read lock
      final Future<HTTPResponse> slow = exec.submit(slowAction);
      awaitRunning();

      // a second reader is not blocked by the first one
      final HTTPResponse result = exec.submit(fastAction).get();
      assertEquals(200, result.status);
      assertEquals(number, result.data);
      assertEquals(200, slow.get().status);
    } finally {
      exec.shutdownNow();
    }
  }

  /**
   * Test concurrent reader and writer (GH-458).
   * <p><b>Test case:</b>
   * <ol>
   * <li/>start a long running reader;
   * <li/>try to start a writer: it should time out;
   * <li/>stop the reader;
   * <li/>start the writer again: it should succeed.
   * </ol>
   * @throws Exception error during request execution
   */
  @Test public void testReaderWriter() throws Exception {
    // the reader holds a read lock on the database for a bounded time and then terminates,
    // so the writer is blocked only until the reader releases the lock (no forced stop needed)
    final Get readerAction = new Get(readerQuery());
    final Put writerAction = new Put("/test.xml", Token.token("<a/>"));

    final ExecutorService exec = Executors.newFixedThreadPool(2);
    try {
      // start the reader and wait until it holds the read lock
      final Future<HTTPResponse> reader = exec.submit(readerAction);
      awaitRunning();

      // the writer must not modify the database while the reader holds the read lock
      final Future<HTTPResponse> writer = exec.submit(writerAction);
      assertThrows(TimeoutException.class, () -> writer.get(BLOCK / 2, TimeUnit.MILLISECONDS));

      // as soon as the reader releases the lock, the writer succeeds
      assertEquals(201, writer.get().status);
      assertEquals(200, reader.get().status);
    } finally {
      exec.shutdownNow();
    }
  }

  /**
   * Test concurrent writers (GH-458).
   * <p><b>Test case:</b>
   * <ol>
   * <li/>start several writers one after another;
   * <li/>all writers should succeed.
   * </ol>
   * @throws Exception error during request execution
   */
  @Test public void testMultipleWriters() throws Exception {
    final int count = 10;
    final String template =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<command xmlns=\"http://basex.org/rest\"><text><![CDATA[" +
        "ADD TO %1$d <node id=\"%1$d\"/>" +
        "]]></text></command>";

    final ArrayList<Future<HTTPResponse>> tasks = new ArrayList<>();
    final ExecutorService exec = Executors.newFixedThreadPool(count);

    // start all writers (not at the same time, but still in parallel)
    for(int i = 0; i < count; i++) {
      final String command = String.format(template, i);
      tasks.add(exec.submit(new Post("", Token.token(command))));
    }

    // check if all have finished successfully
    try {
      for(final Future<HTTPResponse> task : tasks) {
        assertEquals(200, task.get().status);
      }
      // every writer must have added its document
      assertEquals(Integer.toString(count), new Get(
          "?query=" + encode("count(db:get('" + NAME + "'))")).call().data);
    } finally {
      exec.shutdownNow();
    }
  }

  // TOOLBOX ======================================================================================

  /**
   * Returns a query that holds a read lock on the test database for a bounded time.
   * @return request string
   */
  private static String readerQuery() {
    return "?query=" + encode("db:get('" + NAME + "'), prof:sleep(" + BLOCK + ")");
  }

  /**
   * Waits until a second job is running, i.e. until the request that was started before this call
   * has acquired its locks. The polling request itself is the first of the two jobs.
   * @throws Exception error during request execution
   */
  private static void awaitRunning() throws Exception {
    final String request = "?query=" + encode("count(job:list-details()[@state = 'running'])");
    final long end = System.nanoTime() + BLOCK * 1000000;
    do {
      final HTTPResponse response = new Get(request).call();
      assertEquals(200, response.status);
      if(Integer.parseInt(response.data) > 1) return;
      Performance.sleep(10);
    } while(System.nanoTime() < end);
    fail("Request did not start within " + BLOCK + " ms.");
  }

  /**
   * URL-encodes a query string.
   * @param query query
   * @return encoded query
   */
  private static String encode(final String query) {
    return URLEncoder.encode(query, StandardCharsets.UTF_8);
  }

  // REST API =====================================================================================

  /** REST GET request. */
  private static final class Get implements Callable<HTTPResponse> {
    /** Request URI. */
    private final URI uri;

    /**
     * Construct a new GET request.
     * @param request request string without the base URI
     */
    Get(final String request) {
      uri = URI.create(REST_URL + request);
    }

    @Override
    public HTTPResponse call() throws Exception {
      final HttpRequest request = HttpRequest.newBuilder(uri).
          timeout(Duration.ofMillis(SOCKET_TIMEOUT)).build();
      final HttpResponse<String> response = HttpClient.newHttpClient().send(request,
          HttpResponse.BodyHandlers.ofString());
      return new HTTPResponse(response.statusCode(), response.body());
    }
  }

  /** REST PUT request. */
  private static class Put implements Callable<HTTPResponse> {
    /** Request URI. */
    private final URI uri;
    /** Content to send to the server. */
    private final byte[] data;
    /** HTTP method. */
    private final Method method;

    /**
     * Construct a new PUT request.
     * @param request request string without the base URI
     * @param data data to send to the server
     */
    Put(final String request, final byte[] data) {
      this(request, data, PUT);
    }

    /**
     * Construct a new request.
     * @param request request string without the base URI
     * @param data data to send to the server
     * @param method HTTP method
     */
    protected Put(final String request, final byte[] data, final Method method) {
      this.data = data;
      this.method = method;
      uri = URI.create(REST_URL + request);
    }

    @Override
    public HTTPResponse call() throws Exception {
      final HttpRequest request = HttpRequest.newBuilder(uri).
          method(method.name(), HttpRequest.BodyPublishers.ofByteArray(data)).
          setHeader(HTTPText.CONTENT_TYPE, MediaType.APPLICATION_XML.toString()).
          timeout(Duration.ofMillis(SOCKET_TIMEOUT)).build();
      return new HTTPResponse(HttpClient.newHttpClient().send(request,
          HttpResponse.BodyHandlers.discarding()).statusCode());
    }
  }

  /** REST POST request. */
  private static final class Post extends Put {
    /**
     * Construct a new POST request.
     * @param request request string without the base URI
     * @param data data to send to the server
     */
    Post(final String request, final byte[] data) {
      super(request, data, POST);
    }
  }

  /** Simple HTTP response. */
  private static final class HTTPResponse {
    /** Status code. */
    private final int status;
    /** Response data or {@code null} if no data was returned. */
    private final String data;

    /**
     * Constructor.
     * @param code HTTP response status code
     */
    HTTPResponse(final int code) {
      this(code, null);
    }

    /**
     * Constructor.
     * @param status HTTP response status code
     * @param data data
     */
    HTTPResponse(final int status, final String data) {
      this.data = data;
      this.status = status;
    }
  }
}
