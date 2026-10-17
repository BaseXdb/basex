package org.basex.http.restxq;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;

import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the handling of unread request bodies.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RestXqDrainTest extends RestXqTest {
  /**
   * Registers the service.
   * @throws IOException I/O exception
   */
  @BeforeAll public static void services() throws IOException {
    register("declare %R:path('ignore') %output:method('text') function m:ignore() { 'ok' };");
  }

  /**
   * A connection can be reused if an unread body arrives after the response has been created.
   * @throws Exception exception
   */
  @Test public void delayedBody() throws Exception {
    try(Socket socket = new Socket("localhost", 9865)) {
      socket.setSoTimeout(5000);
      final OutputStream out = socket.getOutputStream();
      final InputStream in = socket.getInputStream();
      send(out, "POST /ignore HTTP/1.1\r\nHost: localhost\r\n"
        + "Content-Type: text/plain\r\nContent-Length: 1\r\n\r\n");
      Performance.sleep(1500);
      send(out, "x");
      assertTrue(response(in).startsWith("HTTP/1.1 200"));
      send(out, "GET /ignore HTTP/1.1\r\nHost: localhost\r\n\r\n");
      assertTrue(response(in).startsWith("HTTP/1.1 200"));
    }
  }

  /**
   * Sends a string.
   * @param out output stream
   * @param string string
   * @throws IOException I/O exception
   */
  private static void send(final OutputStream out, final String string) throws IOException {
    out.write(string.getBytes(StandardCharsets.US_ASCII));
    out.flush();
  }

  /**
   * Reads a response with the body of the service.
   * @param in input stream
   * @return response, or the partial response if the connection was closed
   * @throws IOException I/O exception
   */
  private static String response(final InputStream in) throws IOException {
    final StringBuilder sb = new StringBuilder();
    while(!sb.toString().endsWith("\r\n\r\nok")) {
      final int b = in.read();
      if(b == -1) break;
      sb.append((char) b);
    }
    return sb.toString();
  }
}
