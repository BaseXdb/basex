package org.basex.rest;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the handling of unread request bodies.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RestXqDrainTest extends SandboxTest {
  /** HTTP server. */
  private static BaseXHTTP http;

  /**
   * Starts the server and registers the service.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    final StringList sl = new StringList("-p" + DB_PORT, "-h" + HTTP_PORT, "-s" + STOP_PORT,
        "-c", "password " + NAME, "-Uadmin", "-z", "-q");
    http = new BaseXHTTP(sl.finish());
    final String path = HTTPContext.get().context().soptions.get(StaticOptions.WEBPATH);
    new IOFile(path, NAME + IO.XQMSUFFIX).write(
      "module namespace m = 'm';" +
      "declare %rest:path('ignore') %output:method('text') function m:ignore() { 'ok' };");
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterAll public static void stop() throws IOException {
    http.stop();
  }

  /**
   * A connection can be reused if an unread body arrives after the response has been created.
   * @throws Exception exception
   */
  @Test public void delayedBody() throws Exception {
    try(Socket socket = new Socket("localhost", HTTP_PORT)) {
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
