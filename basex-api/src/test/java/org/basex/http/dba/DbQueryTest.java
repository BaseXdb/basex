package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.core.cmd.XQuery;
import org.basex.http.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the WebSocket endpoint of the DBA databases view, which serves both its panels
 * and the queries on a resource.
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
    connect("/databases");
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
   * The database list is pushed as the markup of its panel.
   * @throws Exception exception
   */
  @Test public void databasesPanel() throws Exception {
    final String message = panel("{ \"type\": \"databases\", \"name\": \"" + DB +
        "\", \"sort\": \"\", \"page\": 1 }", "databases");
    assertTrue(message.contains(DB), "database missing from the panel: " + message);
  }

  /**
   * The panel of a database lists its resources.
   * @throws Exception exception
   */
  @Test public void databasePanel() throws Exception {
    final String message = panel("{ \"type\": \"database\", \"name\": \"" + DB +
        "\", \"resource\": \"\", \"sort\": \"\", \"page\": 1 }", "database");
    assertTrue(message.contains(RESOURCE), "resource missing from the panel: " + message);
  }

  /**
   * A panel with nothing to show answers with empty contents, which hides it.
   * @throws Exception exception
   */
  @Test public void emptyPanel() throws Exception {
    assertEquals("{\"type\":\"database\",\"html\":\"\"}",
        panel("{ \"type\": \"database\", \"name\": \"\", \"resource\": \"\"," +
            " \"sort\": \"\", \"page\": 1 }", "database"));
  }

  /**
   * The resource message carries the panel, the document and its edit state.
   * @throws Exception exception
   */
  @Test public void resourcePanel() throws Exception {
    final String message = panel("{ \"type\": \"resource\", \"name\": \"" + DB +
        "\", \"resource\": \"" + RESOURCE + "\" }", "resource");
    assertTrue(message.contains("\"editable\":true"), "document not editable: " + message);
    assertTrue(message.contains("<x><y>1<\\/y><y>2<\\/y><\\/x>"), "document missing: " + message);
  }

  /**
   * An unknown message type is reported.
   * @throws Exception exception
   */
  @Test public void unknownType() throws Exception {
    sendMessage("{ \"type\": \"nonsense\" }");
    final String message = pollMessage();
    assertTrue(message.contains("Unknown message type: nonsense"), message);
  }

  /**
   * Sends a message and returns the pushed panel.
   * @param message message
   * @param type expected type of the answer
   * @return message
   * @throws Exception exception
   */
  private String panel(final String message, final String type) throws Exception {
    sendMessage(message);
    final String answer = pollMessage();
    assertTrue(answer.startsWith("{\"type\":\"" + type + "\""), answer);
    return answer;
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
