package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Tests for the WebSocket endpoint of the DBA editor.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QueryTest extends DBATest {
  /** Number of the last run. */
  private int run;

  /**
   * Opens the connection.
   * @throws Exception exception
   */
  @BeforeEach public void open() throws Exception {
    connect("");
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
   * A stop request is confirmed. The job that waits for the query result is stopped as well,
   * so no result is pushed.
   * @throws Exception exception
   */
  @Test public void stopped() throws Exception {
    sendMessage("{ \"type\": \"run\", \"run\": 1, \"query\": \"prof:sleep(10000)\"," +
        " \"indent\": false }");
    sendMessage("{ \"type\": \"stop\" }");
    assertEquals("{\"type\":\"stopped\"}", pollMessage());
  }

  /**
   * Evaluates a query and returns the pushed message.
   * @param query query
   * @return message
   * @throws Exception exception
   */
  private String evaluate(final String query) throws Exception {
    sendMessage("{ \"type\": \"run\", \"run\": " + ++run + ", \"query\": \"" +
        query.replace("\\", "\\\\").replace("\"", "\\\"") + "\", \"indent\": false }");
    return pollMessage();
  }
}
