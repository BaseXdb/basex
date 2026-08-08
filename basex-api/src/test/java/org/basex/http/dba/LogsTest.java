package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Tests for the WebSocket endpoint of the DBA log view.
 * The sandbox server is started with suppressed logging, so no log file exists to be queried:
 * the tests cover the routing and the error paths of the endpoint.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class LogsTest extends DBATest {
  /**
   * Opens the connection.
   * @throws Exception exception
   */
  @BeforeEach public void open() throws Exception {
    connect("/logs");
  }

  /**
   * An incomplete search input is reported without raising an error (which would be logged).
   * @throws Exception exception
   */
  @Test public void invalidInput() throws Exception {
    sendMessage(entries("(", ""));
    assertEquals("{\"type\":\"error\",\"run\":1," +
        "\"message\":\"Invalid regular expression: (.\"}", pollMessage());
  }

  /**
   * An incomplete column filter is reported in the same way.
   * @throws Exception exception
   */
  @Test public void invalidFilter() throws Exception {
    sendMessage(entries("", "["));
    assertEquals("{\"type\":\"error\",\"run\":1," +
        "\"message\":\"Invalid regular expression: [.\"}", pollMessage());
  }

  /**
   * An unknown log file is reported by the searching job, which returns the number of the run.
   * @throws Exception exception
   */
  @Test public void unknownDate() throws Exception {
    sendMessage(entries("", ""));
    final String message = pollMessage();
    assertTrue(message.startsWith("{\"type\":\"error\",\"run\":1,"), message);
    assertTrue(message.contains("Resource '1999-01-01' not found."), message);
  }

  /**
   * Returns a request for the log entries of a date that has no log file.
   * @param input search input
   * @param filter filter for the text column
   * @return message
   */
  private static String entries(final String input, final String filter) {
    return "{ \"type\": \"entries\", \"run\": 1, \"date\": \"1999-01-01\", \"sort\": \"\"," +
        " \"page\": 1, \"time\": \"\", \"ignore\": \"\", \"input\": \"" + input + "\"," +
        " \"filters\": { \"text\": \"" + filter + "\" } }";
  }
}
