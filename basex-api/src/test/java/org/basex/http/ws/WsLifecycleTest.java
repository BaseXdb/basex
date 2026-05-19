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
    final org.basex.core.Context httpCtx = org.basex.http.HTTPContext.get().context();
    new org.basex.core.cmd.XQuery("cache:put('ws-close', false())").execute(httpCtx);
    register(
        "declare %ws:message('/c', '{$m}') function m:msg($m) { () };" +
        "declare %ws:close('/c') function m:cl() { cache:put('ws-close', true()) };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/c", l);
    close(ws);

    // wait until the close handler has flipped the flag
    await(() -> {
      try {
        final String value = new org.basex.core.cmd.XQuery(
            "cache:get('ws-close')").execute(httpCtx);
        return "true".equals(value.trim()) ? Boolean.TRUE : null;
      } catch(final Exception ex) {
        Util.debug(ex);
        return null;
      }
    });
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
}
