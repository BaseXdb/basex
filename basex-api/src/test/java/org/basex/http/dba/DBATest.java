package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;

import org.basex.http.*;
import org.junit.jupiter.api.*;

/**
 * Base class for DBA tests: deploys the DBA into the sandbox webapp, holds a login session, and
 * connects to the WebSocket endpoints of the application.
 * Naming note: the JDK client type {@link java.net.http.WebSocket} collides with
 * BaseX's {@link org.basex.http.ws.WebSocket}; we therefore use the fully-qualified
 * name {@code java.net.http.WebSocket} throughout this file.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class DBATest extends WebappTest {
  /** Messages pushed by the server. */
  private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
  /** Connection to a WebSocket endpoint ({@code null} if none was opened). */
  private java.net.http.WebSocket ws;

  /**
   * Deploys the DBA, starts the server and logs in.
   * @throws Exception exception
   */
  @BeforeAll public static void startDBA() throws Exception {
    init("dba");
    final String page = post("login", Map.of("_name", "admin", "_pass", NAME));
    assertFalse(page.contains("_pass"), "DBA login failed");
  }

  /**
   * Closes the connection.
   * @throws Exception exception
   */
  @AfterEach public void disconnect() throws Exception {
    if(ws != null) {
      ws.sendClose(java.net.http.WebSocket.NORMAL_CLOSURE, "bye").get(5, TimeUnit.SECONDS);
      ws = null;
    }
  }

  /**
   * Opens a connection to a WebSocket endpoint of the application.
   * @param path path, relative to the WebSocket root of the DBA
   * @throws Exception exception
   */
  protected void connect(final String path) throws Exception {
    ws = socket("/dba" + path, new java.net.http.WebSocket.Listener() {
      /** Accumulator for text frame parts. */
      private final StringBuilder buffer = new StringBuilder();

      @Override
      public CompletionStage<?> onText(final java.net.http.WebSocket socket,
          final CharSequence data, final boolean last) {
        buffer.append(data);
        if(last) {
          messages.add(buffer.toString());
          buffer.setLength(0);
        }
        socket.request(1);
        return null;
      }
    });
  }

  /**
   * Sends a message to the server.
   * @param message message
   * @throws Exception exception
   */
  protected void sendMessage(final String message) throws Exception {
    ws.sendText(message, true).get(5, TimeUnit.SECONDS);
  }

  /**
   * Returns the next message pushed by the server.
   * @return message
   * @throws Exception exception
   */
  protected String pollMessage() throws Exception {
    final String message = messages.poll(15, TimeUnit.SECONDS);
    assertNotNull(message, "No message received within timeout.");
    return message;
  }
}
