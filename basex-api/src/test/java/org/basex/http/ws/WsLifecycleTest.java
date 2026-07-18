package org.basex.http.ws;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.*;
import java.util.concurrent.*;

import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the WebSocket lifecycle annotations and path routing.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsLifecycleTest extends WsTest {
  /**
   * {@code %ws:connect} is invoked when a client opens a connection.
   * @throws Exception exception
   */
  @Test public void connect() throws Exception {
    register(
        "declare %ws:connect('/echo') function m:c() { ws:emit('connected') };" +
        "declare %ws:message('/echo', '{$m}') function m:msg($m) { () };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/echo", l);
    try {
      assertEquals("connected", l.pollText());
    } finally {
      close(ws);
    }
  }

  /**
   * {@code %ws:message} is invoked once per text message.
   * @throws Exception exception
   */
  @Test public void messageText() throws Exception {
    register("declare %ws:message('/echo', '{$m}') function m:msg($m) {"
        + " ws:emit('echo:' || $m) };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/echo", l);
    try {
      ws.sendText("hello", true).get(5, TimeUnit.SECONDS);
      assertEquals("echo:hello", l.pollText());
      ws.sendText("welt", true).get(5, TimeUnit.SECONDS);
      assertEquals("echo:welt", l.pollText());
    } finally {
      close(ws);
    }
  }

  /**
   * {@code %ws:message} receives a binary message as base64 binary and can echo it back.
   * @throws Exception exception
   */
  @Test public void messageBinary() throws Exception {
    register(
        "declare %ws:message('/echo', '{$m}') function m:msg($m) as xs:base64Binary { $m };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/echo", l);
    try {
      final byte[] payload = { 1, 2, 3, 4, 5 };
      ws.sendBinary(ByteBuffer.wrap(payload), true).get(5, TimeUnit.SECONDS);
      assertArrayEquals(payload, l.pollBinary());
    } finally {
      close(ws);
    }
  }

  /**
   * {@code %ws:close} is invoked when the connection is closed. The side effect is
   * verified via the {@code cache} module since the close handler cannot deliver output
   * to the (already-closed) client. The cache is read from the HTTP server's context,
   * which the WebSocket inherits caches from.
   * @throws Exception exception
   */
  @Test public void closeHandler() throws Exception {
    putCache("ws-close", "false");
    register(
        "declare %ws:message('/c', '{$m}') function m:msg($m) { () };" +
        "declare %ws:close('/c') function m:cl() { cache:put('ws-close', true()) };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/c", l);
    close(ws);

    // wait until the close handler has flipped the flag
    awaitCache("ws-close", "true");
  }

  /**
   * Different paths route to different functions. Each handler responds only to the
   * caller (via {@code ws:send}) to avoid cross-talk from {@code ws:emit} broadcasts.
   * @throws Exception exception
   */
  @Test public void pathRouting() throws Exception {
    register(
        "declare %ws:message('/a', '{$m}') function m:a($m) { ws:send('a:' || $m, ws:id()) };" +
        "declare %ws:message('/b', '{$m}') function m:b($m) { ws:send('b:' || $m, ws:id()) };");

    final Listener la = new Listener();
    final java.net.http.WebSocket wsa = connect("/a", la);
    final Listener lb = new Listener();
    final java.net.http.WebSocket wsb = connect("/b", lb);
    try {
      wsa.sendText("x", true).get(5, TimeUnit.SECONDS);
      wsb.sendText("y", true).get(5, TimeUnit.SECONDS);
      assertEquals("a:x", la.pollText());
      assertEquals("b:y", lb.pollText());
    } finally {
      close(wsa);
      close(wsb);
    }
  }

  /**
   * Connecting to a path without registered WebSocket function must fail.
   * @throws Exception exception
   */
  @Test public void unmatchedPath() throws Exception {
    register("declare %ws:message('/a', '{$m}') function m:a($m) { () };");
    assertThrows(Exception.class, () -> connect("/no-such-path", new Listener()));
  }

  /**
   * When the XQuery message handler raises an error, the exception message is sent back
   * to the client as a text frame.
   * @throws Exception exception
   */
  @Test public void handlerError() throws Exception {
    register("declare %ws:message('/e', '{$m}') function m:msg($m) {"
        + " error(xs:QName('m:boom'), 'BOOM') };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/e", l);
    try {
      ws.sendText("trigger", true).get(5, TimeUnit.SECONDS);
      assertTrue(l.pollText().contains("BOOM"));
    } finally {
      close(ws);
    }
  }

  /**
   * A handler that returns a sequence of items delivers them to the caller as separate
   * frames, in order (guards the asynchronous send path).
   * @throws Exception exception
   */
  @Test public void messageSequence() throws Exception {
    register("declare %ws:message('/echo', '{$m}') function m:msg($m) { 'a', 'b', 'c' };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/echo", l);
    try {
      ws.sendText("go", true).get(5, TimeUnit.SECONDS);
      assertEquals("a", l.pollText());
      assertEquals("b", l.pollText());
      assertEquals("c", l.pollText());
    } finally {
      close(ws);
    }
  }

  /**
   * A larger binary payload survives the byte-buffer round trip unchanged.
   * @throws Exception exception
   */
  @Test public void binaryLarge() throws Exception {
    register(
        "declare %ws:message('/echo', '{$m}') function m:msg($m) as xs:base64Binary { $m };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/echo", l);
    try {
      final byte[] payload = new byte[8192];
      for(int i = 0; i < payload.length; i++) payload[i] = (byte) (i % 251);
      ws.sendBinary(ByteBuffer.wrap(payload), true).get(5, TimeUnit.SECONDS);
      assertArrayEquals(payload, l.pollBinary());
    } finally {
      close(ws);
    }
  }

  /**
   * The {@code request:*} module is available inside a WebSocket message handler.
   * @throws Exception exception
   */
  @Test public void requestModuleAvailable() throws Exception {
    register("declare %ws:message('/echo', '{$m}') function m:msg($m) {"
        + " Q{http://exquery.org/ns/request}method() };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/echo", l);
    try {
      ws.sendText("go", true).get(5, TimeUnit.SECONDS);
      assertEquals("GET", l.pollText());
    } finally {
      close(ws);
    }
  }

  /**
   * The {@code session:*} module works inside a WebSocket handler: a value stored in the
   * HTTP session is read back within the same connection (guards the request/session
   * decoupling — the WebSocket path must not require an HTTP {@code RequestContext}).
   * @throws Exception exception
   */
  @Test public void sessionAccess() throws Exception {
    register(
        "declare namespace session = 'http://basex.org/modules/session';" +
        "declare %ws:message('/echo', '{$m}') function m:msg($m) {" +
        "  session:set('k', $m), ws:emit(session:get('k')) };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/echo", l);
    try {
      ws.sendText("payload", true).get(5, TimeUnit.SECONDS);
      assertEquals("payload", l.pollText());
    } finally {
      close(ws);
    }
  }

  /**
   * A handler that calls {@code ws:close} on the current client closes the connection;
   * the client observes the close handshake.
   * @throws Exception exception
   */
  @Test public void serverClose() throws Exception {
    register("declare %ws:message('/x', '{$m}') function m:msg($m) { ws:close(ws:id()) };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/x", l);
    try {
      ws.sendText("bye", true).get(5, TimeUnit.SECONDS);
      await(() -> l.closeStatus != -1 ? Boolean.TRUE : null);
    } finally {
      // the server has already closed the connection; ignore any resulting error
      try {
        close(ws);
      } catch(final Exception ignore) {
        Util.debug(ignore);
      }
    }
  }

  /**
   * {@code %ws:close} can bind the close status and reason sent by the client.
   * @throws Exception exception
   */
  @Test public void closeStatusReason() throws Exception {
    putCache("ws-close-info", "");
    register(
        "declare %ws:message('/c', '{$m}') function m:msg($m) { () };" +
        "declare %ws:close('/c', '{$status}', '{$reason}') function m:cl($status, $reason) {" +
        "  cache:put('ws-close-info', $status || ':' || $reason) };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/c", l);
    ws.sendClose(4000, "test-reason").get(5, TimeUnit.SECONDS);

    awaitCache("ws-close-info", "4000:test-reason");
  }

  /**
   * {@code ws:close} with status and reason: the client observes both values.
   * @throws Exception exception
   */
  @Test public void serverCloseStatus() throws Exception {
    register("declare %ws:message('/x', '{$m}') function m:msg($m) {"
        + " ws:close(ws:id(), 4123, 'server bye') };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/x", l);
    try {
      ws.sendText("bye", true).get(5, TimeUnit.SECONDS);
      await(() -> l.closeStatus != -1 ? Boolean.TRUE : null);
      assertEquals(4123, l.closeStatus);
      assertEquals("server bye", l.closeReason);
    } finally {
      // the server has already closed the connection; ignore any resulting error
      try {
        close(ws);
      } catch(final Exception ignore) {
        Util.debug(ignore);
      }
    }
  }

  /**
   * A server-side {@code ws:close} leaves the connection queryable in {@code %ws:close}.
   * @throws Exception exception
   */
  @Test public void serverCloseHandlerSeesConnection() throws Exception {
    putCache("ws-close-attr", "");
    register(
        "declare %ws:connect('/sc') function m:c() { ws:set(ws:id(), 'k', 'v') };" +
        "declare %ws:message('/sc', '{$m}') function m:msg($m) { ws:close(ws:id()) };" +
        "declare %ws:close('/sc') function m:cl() {" +
        "  cache:put('ws-close-attr', ws:get(ws:id(), 'k')) };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/sc", l);
    try {
      ws.sendText("go", true).get(5, TimeUnit.SECONDS);
      // the close handler ran with the connection still in the pool and read its attribute
      awaitCache("ws-close-attr", "v");
    } finally {
      // the server has already closed the connection; ignore any resulting error
      try {
        close(ws);
      } catch(final Exception ignore) {
        Util.debug(ignore);
      }
    }
  }

  /**
   * {@code ws:close} rejects status codes outside the RFC 6455 range.
   * @throws Exception exception
   */
  @Test public void closeInvalidStatus() throws Exception {
    register("declare %ws:message('/x', '{$m}') function m:msg($m) {"
        + " ws:close(ws:id(), 5000) };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/x", l);
    try {
      ws.sendText("go", true).get(5, TimeUnit.SECONDS);
      assertTrue(l.pollText().contains("Invalid close status"));
    } finally {
      close(ws);
    }
  }
}
