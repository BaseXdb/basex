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
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.cmd.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Concurrency tests of BaseX REST API.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Dimitar Popov
 */
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
    final StringList sl = new StringList();
    sl.add("-p" + DB_PORT, "-h" + HTTP_PORT, "-s" + STOP_PORT, "-z").add("-U" + ADMIN);

    http = new BaseXHTTP(sl.toArray());
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
  @Test @Disabled("Query cannot be stopped") public void testReaderWriter() throws Exception {
    final String readerQuery = "?query=(1%20to%20100000000000000)%5b.=0%5d";
    final String writerQuery = "/test.xml";
    final byte[] content = Token.token("<a/>");

    final Get readerAction = new Get(readerQuery);
    final Put writerAction = new Put(writerQuery, content);

    final ExecutorService exec = Executors.newFixedThreadPool(2);

    // start reader
    exec.submit(readerAction);
    Performance.sleep(TIMEOUT); // delay in order to be sure that the reader has started
    // start writer
    Future<HTTPResponse> writer = exec.submit(writerAction);

    try {
      final HTTPResponse result = writer.get(TIMEOUT, TimeUnit.MILLISECONDS);
      if(result.status >= 200 && result.status < 300)
        fail("Database modified while a reader is running");
      throw new Exception(result.toString());
    } catch(final TimeoutException ex) {
      // writer is blocked by the reader: stop it
      Util.errln(ex);
      writerAction.stop = true;
    }

    // stop reader
    readerAction.stop = true;

    // start the writer again
    writer = exec.submit(writerAction);
    assertEquals(201, writer.get().status);
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
    for(final Future<HTTPResponse> task : tasks) {
      assertEquals(200, task.get(TIMEOUT, TimeUnit.MILLISECONDS).status);
    }
  }

  // REST API

  /** REST GET request. */
  private static class Get implements Callable<HTTPResponse> {
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
  private static class Post extends Put {
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
  private static class HTTPResponse {
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
