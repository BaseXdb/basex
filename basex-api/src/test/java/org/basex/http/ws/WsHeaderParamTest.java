package org.basex.http.ws;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Tests for {@code %ws:header-param} annotations.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsHeaderParamTest extends WsTest {
  /**
   * Standard upgrade headers (host, request-uri, http-version, is-secure) are bound.
   * @throws Exception exception
   */
  @Test public void standardHeaders() throws Exception {
    register(
        "declare %ws:connect('/h')" +
        "        %ws:header-param('host', '{$host}')" +
        "        %ws:header-param('request-uri', '{$uri}')" +
        "        %ws:header-param('is-secure', '{$secure}')" +
        "        function m:c($host, $uri, $secure) {" +
        "  ws:emit($host || '|' || $uri || '|' || $secure)" +
        "};" +
        "declare %ws:message('/h', '{$m}') function m:msg($m) { () };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/h", l);
    try {
      final String[] parts = l.pollText().split("\\|", -1);
      assertEquals(3, parts.length);
      assertTrue(parts[0].contains("localhost"), "host should contain localhost: " + parts[0]);
      assertTrue(parts[1].endsWith("/ws/h"), "request-uri should end with /ws/h: " + parts[1]);
      assertEquals("false", parts[2]);
    } finally {
      close(ws);
    }
  }

  /**
   * A header that is not provided by the upgrade request falls back to the
   * declared default value.
   * @throws Exception exception
   */
  @Test public void defaultValue() throws Exception {
    register(
        "declare %ws:connect('/h')" +
        "        %ws:header-param('origin', '{$o}', 'fallback')" +
        "        function m:c($o) { ws:emit($o) };" +
        "declare %ws:message('/h', '{$m}') function m:msg($m) { () };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/h", l);
    try {
      // the JDK HttpClient does not send an Origin header; the default must apply
      assertEquals("fallback", l.pollText());
    } finally {
      close(ws);
    }
  }

  /**
   * The {@code sub-protocols} pseudo-header is bound as an (here: empty) sequence
   * when the client does not offer sub-protocols.
   * @throws Exception exception
   */
  @Test public void subProtocolsEmpty() throws Exception {
    register(
        "declare %ws:connect('/h')" +
        "        %ws:header-param('sub-protocols', '{$p}')" +
        "        function m:c($p) { ws:emit('count=' || count($p)) };" +
        "declare %ws:message('/h', '{$m}') function m:msg($m) { () };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/h", l);
    try {
      assertEquals("count=0", l.pollText());
    } finally {
      close(ws);
    }
  }
}
