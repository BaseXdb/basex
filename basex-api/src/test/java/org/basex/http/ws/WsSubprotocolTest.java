package org.basex.http.ws;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;

import org.junit.jupiter.api.*;

/**
 * Tests for WebSocket sub-protocol negotiation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsSubprotocolTest extends WsTest {
  /**
   * The first client-offered protocol that the server declares is negotiated.
   * @throws Exception exception
   */
  @Test public void negotiated() throws Exception {
    register(
        "declare %ws:connect('/s') %ws:subprotocol('chat.v2', 'chat.v1') function m:c() { () };" +
        "declare %ws:message('/s', '{$m}') function m:msg($m) { ws:send($m, ws:id()) };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/s", l, "chat.v1", "chat.v2");
    try {
      assertEquals("chat.v1", ws.getSubprotocol());
      ws.sendText("x", true).get(5, TimeUnit.SECONDS);
      assertEquals("x", l.pollText());
    } finally {
      close(ws);
    }
  }

  /**
   * A client that offers no sub-protocol connects without one.
   * @throws Exception exception
   */
  @Test public void noOffer() throws Exception {
    register(
        "declare %ws:connect('/s') %ws:subprotocol('chat.v1') function m:c() { () };" +
        "declare %ws:message('/s', '{$m}') function m:msg($m) { () };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/s", l);
    try {
      assertEquals("", ws.getSubprotocol());
    } finally {
      close(ws);
    }
  }

  /**
   * No overlap between offered and declared protocols: the server must not echo any
   * protocol (JSR-356 semantics). Strict clients such as browsers fail the connection
   * at this point; the JDK client connects and reports an empty protocol.
   * @throws Exception exception
   */
  @Test public void noOverlap() throws Exception {
    register(
        "declare %ws:connect('/s') %ws:subprotocol('chat.v1') function m:c() { () };" +
        "declare %ws:message('/s', '{$m}') function m:msg($m) { () };");

    final java.net.http.WebSocket ws = connect("/s", new Listener(), "other.v1");
    try {
      assertEquals("", ws.getSubprotocol());
    } finally {
      close(ws);
    }
  }

  /**
   * A client that offers protocols to an endpoint that declares none: the server must
   * not echo any protocol (guards against the container auto-accepting an offer).
   * @throws Exception exception
   */
  @Test public void offerWithoutDeclaration() throws Exception {
    register("declare %ws:message('/s', '{$m}') function m:msg($m) { () };");

    final java.net.http.WebSocket ws = connect("/s", new Listener(), "chat.v1");
    try {
      assertEquals("", ws.getSubprotocol());
    } finally {
      close(ws);
    }
  }

  /**
   * {@code %ws:subprotocol} without {@code %ws:connect} is rejected when the module
   * is parsed.
   */
  @Test public void requiresConnect() {
    assertThrows(Exception.class, () -> {
      register(
          "declare %ws:message('/s', '{$m}') %ws:subprotocol('chat.v1') " +
          "function m:msg($m) { () };");
      connect("/s", new Listener());
    });
  }
}
