package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

/**
 * Tests for the WebSocket endpoint of the DBA stores view, which serves its panels and the
 * value that is looked at.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StoresSocketTest extends DBATest {
  /** Test store. */
  private static final String STORE = "dba-junit-socket";

  /**
   * Fills a store with one entry and opens the connection.
   * @throws Exception exception
   */
  @BeforeEach public void open() throws Exception {
    post("stores/add", Map.of("name", STORE, "path", "", "index", "false",
        "step", "'key'", "value", "'text'"));
    connect("/stores");
  }

  /**
   * Deletes the test store.
   * @throws Exception exception
   */
  @AfterEach public void delete() throws Exception {
    post("stores/delete", Map.of("name", STORE));
  }

  /**
   * The store list is pushed as the markup of its panel.
   * @throws Exception exception
   */
  @Test public void storesPanel() throws Exception {
    final String message = panel("{ \"type\": \"stores\", \"name\": \"" + STORE +
        "\", \"sort\": \"\", \"page\": 1 }", "stores");
    assertTrue(message.contains(STORE), "store missing from the panel: " + message);
  }

  /**
   * The panel of a store lists its entries.
   * @throws Exception exception
   */
  @Test public void entriesPanel() throws Exception {
    final String message = panel("{ \"type\": \"entries\", \"name\": \"" + STORE +
        "\", \"path\": \"\", \"selected\": \"key\", \"sort\": \"\", \"page\": 1 }", "entries");
    assertTrue(message.contains("key"), "entry missing from the panel: " + message);
  }

  /**
   * The value message carries the panel, the value and its edit state.
   * @throws Exception exception
   */
  @Test public void value() throws Exception {
    final String message = panel("{ \"type\": \"value\", \"name\": \"" + STORE +
        "\", \"path\": \"key\" }", "value");
    assertTrue(message.contains("\"editable\":true"), "value not editable: " + message);
    assertTrue(message.contains("\\\"text\\\""), "value missing: " + message);
  }

  /**
   * A path that leads to no value answers with empty contents, which hides the panel.
   * @throws Exception exception
   */
  @Test public void emptyValue() throws Exception {
    final String message = panel("{ \"type\": \"value\", \"name\": \"" + STORE +
        "\", \"path\": \"none\" }", "value");
    assertTrue(message.contains("\"html\":\"\""), "panel not hidden: " + message);
    assertTrue(message.contains("\"editable\":false"), "value reported as editable: " + message);
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
}
