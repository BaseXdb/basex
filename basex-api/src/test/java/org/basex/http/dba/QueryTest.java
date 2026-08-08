package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;

import org.junit.jupiter.api.*;

/**
 * Tests for the WebSocket endpoint of the DBA editor.
 * Naming note: the JDK client type {@link java.net.http.WebSocket} collides with
 * BaseX's {@link org.basex.http.ws.WebSocket}; we therefore use the fully-qualified
 * name {@code java.net.http.WebSocket} throughout this file.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QueryTest extends DBATest {
  /** Messages pushed by the server. */
  private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
  /** Connection to the editor endpoint. */
  private java.net.http.WebSocket socket;
  /** Number of the last run. */
  private int run;

  /**
   * Opens the connection.
   * @throws Exception exception
   */
  @BeforeEach public void connect() throws Exception {
    socket = socket("/dba", new java.net.http.WebSocket.Listener() {
      /** Accumulator for text frame parts. */
      private final StringBuilder buffer = new StringBuilder();

      @Override
      public CompletionStage<?> onText(final java.net.http.WebSocket ws, final CharSequence data,
          final boolean last) {
        buffer.append(data);
        if(last) {
          messages.add(buffer.toString());
          buffer.setLength(0);
        }
        ws.request(1);
        return null;
      }
    });
  }

  /**
   * Closes the connection.
   * @throws Exception exception
   */
  @AfterEach public void disconnect() throws Exception {
    socket.sendClose(java.net.http.WebSocket.NORMAL_CLOSURE, "bye").get(5, TimeUnit.SECONDS);
  }

  /**
   * Tests query evaluation.
   * @throws Exception exception
   */
  @Test public void query() throws Exception {
    assertEquals("{\"type\":\"result\",\"run\":1,\"result\":\"2\"}", evaluate("1 + 1"));
    assertEquals("{\"type\":\"result\",\"run\":2,\"result\":\"ok\"}", evaluate("'ok'"));
  }

  /**
   * An updating query is evaluated by the job itself; its output is the result.
   * @throws Exception exception
   */
  @Test public void update() throws Exception {
    assertEquals("{\"type\":\"result\",\"run\":1,\"result\":\"<a\\/>\"}",
        evaluate("copy $a := <a/> modify delete node $a/@* return $a"));
  }

  /**
   * An empty result is pushed as an empty string.
   * @throws Exception exception
   */
  @Test public void emptyResult() throws Exception {
    assertEquals("{\"type\":\"result\",\"run\":1,\"result\":\"\"}", evaluate("()"));
  }

  /**
   * A static error is reported with its position.
   * @throws Exception exception
   */
  @Test public void staticError() throws Exception {
    final String message = evaluate("1 +");
    assertTrue(message.startsWith("{\"type\":\"error\",\"run\":1,"), message);
    assertTrue(message.contains("\"line\":1,\"column\":4"), message);
  }

  /**
   * A dynamic error is reported with its position.
   * @throws Exception exception
   */
  @Test public void dynamicError() throws Exception {
    assertEquals("{\"type\":\"error\",\"run\":1," +
        "\"message\":\"1 cannot be divided by zero.\",\"line\":1,\"column\":7}",
        evaluate("1 div 0"));
  }

  /**
   * A result beyond the default WebSocket frame limit is pushed as a single message.
   * @throws Exception exception
   */
  @Test public void largeResult() throws Exception {
    final String message = evaluate("string-join((1 to 100000) ! 'x')");
    assertTrue(message.length() > 100000, "Truncated message: " + message.length() + " chars.");
  }

  /**
   * A query beyond the default WebSocket frame limit is accepted.
   * @throws Exception exception
   */
  @Test public void largeQuery() throws Exception {
    assertEquals("{\"type\":\"result\",\"run\":1,\"result\":\"1\"}",
        evaluate("1 (: " + "x".repeat(100000) + " :)"));
  }

  /**
   * A stop request is confirmed. The stopped query has no cached result, so the waiting job
   * pushes an empty one; the client discards it, as the number of the run does not match the
   * one it waits for.
   * @throws Exception exception
   */
  @Test public void stopped() throws Exception {
    send("{ \"type\": \"run\", \"run\": 1, \"query\": \"prof:sleep(10000)\", \"indent\": false }");
    send("{ \"type\": \"stop\" }");
    // removing the job releases the waiting one, so the order of the two messages is undefined
    final String messages = poll() + poll();
    assertTrue(messages.contains("{\"type\":\"stopped\"}"), messages);
    assertTrue(messages.contains("{\"type\":\"result\",\"run\":1,\"result\":\"\"}"), messages);
  }

  /**
   * Evaluates a query and returns the pushed message.
   * @param query query
   * @return message
   * @throws Exception exception
   */
  private String evaluate(final String query) throws Exception {
    send("{ \"type\": \"run\", \"run\": " + ++run + ", \"query\": \"" +
        query.replace("\\", "\\\\").replace("\"", "\\\"") + "\", \"indent\": false }");
    return poll();
  }

  /**
   * Sends a message to the server.
   * @param message message
   * @throws Exception exception
   */
  private void send(final String message) throws Exception {
    socket.sendText(message, true).get(5, TimeUnit.SECONDS);
  }

  /**
   * Returns the next message pushed by the server.
   * @return message
   * @throws Exception exception
   */
  private String poll() throws Exception {
    final String message = messages.poll(15, TimeUnit.SECONDS);
    assertNotNull(message, "No message received within timeout.");
    return message;
  }
}
