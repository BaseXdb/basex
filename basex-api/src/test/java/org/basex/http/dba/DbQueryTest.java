package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.core.cmd.XQuery;
import org.basex.http.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the WebSocket endpoint of the DBA resource query.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbQueryTest extends DBATest {
  /** Test database. */
  private static final String DB = "dba-junit-query";
  /** Test resource. */
  private static final String RESOURCE = "doc.xml";

  /**
   * Creates a database with one resource and opens the connection.
   * @throws Exception exception
   */
  @BeforeEach public void open() throws Exception {
    execute("db:create('" + DB + "', <x><y>1</y><y>2</y></x>, '" + RESOURCE + "')");
    connect("/db-query");
  }

  /**
   * Drops the test database.
   * @throws Exception exception
   */
  @AfterEach public void drop() throws Exception {
    execute("db:drop('" + DB + "')");
  }

  /**
   * Runs a query in the context of the HTTP server.
   * @param query query
   * @throws Exception exception
   */
  private static void execute(final String query) throws Exception {
    new XQuery(query).execute(HTTPContext.get().context());
  }

  /**
   * Requests the resource itself.
   * @throws Exception exception
   */
  @Test public void resource() throws Exception {
    assertEquals("{\"type\":\"result\",\"run\":1," +
        "\"result\":\"<x><y>1<\\/y><y>2<\\/y><\\/x>\"}", evaluate("."));
  }

  /**
   * Runs a query on the resource.
   * @throws Exception exception
   */
  @Test public void context() throws Exception {
    assertEquals("{\"type\":\"result\",\"run\":1,\"result\":\"1\\n2\"}",
        evaluate("//y/string()"));
  }

  /**
   * A failing query is reported with its position.
   * @throws Exception exception
   */
  @Test public void error() throws Exception {
    final String message = evaluate("1 +");
    assertTrue(message.startsWith("{\"type\":\"error\",\"run\":1,"), message);
    assertTrue(message.contains("\"line\":1,\"column\":4"), message);
  }

  /**
   * Sends a query and returns the pushed message.
   * @param string query
   * @return message
   * @throws Exception exception
   */
  private String evaluate(final String string) throws Exception {
    sendMessage("{ \"type\": \"query\", \"run\": 1, \"name\": \"" + DB + "\"," +
        " \"resource\": \"" + RESOURCE + "\", \"query\": \"" +
        string.replace("\\", "\\\\").replace("\"", "\\\"") + "\", \"indent\": false }");
    return pollMessage();
  }
}
