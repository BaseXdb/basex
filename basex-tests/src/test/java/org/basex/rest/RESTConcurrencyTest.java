package org.basex.rest;

import static org.basex.core.users.UserText.*;
import static org.basex.util.http.Method.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
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

  /** Time-out in (ms): increase if running on a slower system. */
  private static final long TIMEOUT = 2000;
  /** Socket time-out in (ms). */
  private static final int SOCKET_TIMEOUT = 3000;
  /** BaseX HTTP base URL. */
  private static final String REST_URL = REST_ROOT + NAME;

  /**
   * Creates a test database and starts BaseXHTTP.
   * @throws Exception if database cannot be created or server cannot be started
   */
  @BeforeEach public void setUp() throws Exception {
    final StringList sl = new StringList("-p" + DB_PORT, "-h" + HTTP_PORT, "-s" + STOP_PORT,
        "-c", "password " + NAME, "-U" + ADMIN, "-z", "-q");
    http = new BaseXHTTP(sl.finish());
    try(ClientSession cs = createClient()) {
      cs.execute(new CreateDB(NAME));
    }
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
    final String slowQuery = "?query=(1%20to%20100000000000)%5b.=0%5d";
    final String fastQuery = "?query=" + number;

    final Get slowAction = new Get(slowQuery);
    final Get fastAction = new Get(fastQuery);

    final ExecutorService exec = Executors.newFixedThreadPool(2);
    final Future<HTTPResponse> slow = exec.submit(slowAction);
    Performance.sleep(TIMEOUT); // delay in order to be sure that the reader has started
    final Future<HTTPResponse> fast = exec.submit(fastAction);

    try {
      final HTTPResponse result = fast.get();
      assertEquals(200, result.status);
      assertEquals(number, result.data);
    } finally {
      slowAction.stop = true;
      slow.get();
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
    final String readerQuery = "?query=" + URLEncoder.encode(
        "db:get('" + NAME + "'), prof:sleep(2500)", StandardCharsets.UTF_8);
    final byte[] content = Token.token("<a/>");

    final Get readerAction = new Get(readerQuery);
    final Put writerAction = new Put("/test.xml", content);

    final ExecutorService exec = Executors.newFixedThreadPool(2);
    try {
      // start the reader and wait until it is running and holds the read lock
      final Future<HTTPResponse> reader = exec.submit(readerAction);
      Performance.sleep(800);

      // the writer must not modify the database while the reader holds the read lock
      final Future<HTTPResponse> writer = exec.submit(writerAction);
      assertThrows(TimeoutException.class, () -> writer.get(1000, TimeUnit.MILLISECONDS));

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
        assertEquals(200, task.get(TIMEOUT, TimeUnit.MILLISECONDS).status);
      }
    } finally {
      exec.shutdownNow();
    }
  }

  // REST API

  /** REST GET request. */
  private static final class Get implements Callable<HTTPResponse> {
    /** Request URI. */
    protected final URI uri;
    /** Stop signal. */
    public volatile boolean stop;

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

      while(!stop) {
        try {
          final HttpResponse<String> response = HttpClient.newHttpClient().send(request,
              HttpResponse.BodyHandlers.ofString());
          return new HTTPResponse(response.statusCode(), response.body());
        } catch(final HttpTimeoutException ex) {
          Util.errln(ex);
        }
      }
      return null;
    }
  }

  /** REST PUT request. */
  private static class Put implements Callable<HTTPResponse> {
    /** Request URI. */
    private final URI uri;
    /** Content to send to the server. */
    private final byte[] data;
    /** HTTP method. */
    protected final Method method;
    /** Stop signal. */
    public volatile boolean stop;

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
      while(!stop) {
        try {
          return new HTTPResponse(HttpClient.newHttpClient().send(request,
              HttpResponse.BodyHandlers.discarding()).statusCode());
        } catch(final HttpTimeoutException ex) {
          Util.errln(ex);
        }
      }
      return null;
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

  // Toolbox

  /** Simple HTTP response. */
  private static final class HTTPResponse {
    /** Status code. */
    public final int status;
    /** Response data or {@code null} if no data was returned. */
    public final String data;

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
