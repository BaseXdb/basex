package org.basex.http.ws;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;

import org.junit.jupiter.api.*;

/**
 * Tests for the {@code ws:*} XQuery function module.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsPoolTest extends WsTest {
  /**
   * Removes lingering pool entries from earlier tests.
   */
  @BeforeEach public void cleanPool() {
    for(final byte[] id : WsPool.ids().finish()) {
      WsPool.remove(new String(id));
    }
  }

  /**
   * {@code ws:id()} inside a handler returns the current client ID;
   * {@code ws:ids()} reports it as a connected client.
   * @throws Exception exception
   */
  @Test public void idAndIds() throws Exception {
    register(
        "declare %ws:connect('/p') function m:c() { ws:emit('id=' || ws:id()) };" +
        "declare %ws:message('/p', '{$m}') function m:msg($m) {" +
        "  ws:emit('ids=' || string-join(ws:ids(), ','))" +
        "};");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/p", l);
    try {
      final String id = l.pollText();
      assertTrue(id.startsWith("id=websocket"), "Unexpected id message: " + id);
      ws.sendText("?", true).get(5, TimeUnit.SECONDS);
      final String ids = l.pollText();
      assertTrue(ids.startsWith("ids=websocket"), "Unexpected ids message: " + ids);
      assertTrue(ids.contains(id.substring(3)), "ws:ids() should contain own id: " + ids);
    } finally {
      close(ws);
    }
  }

  /**
   * {@code ws:emit($msg)} sends a message to all connected clients
   * (including the sender).
   * @throws Exception exception
   */
  @Test public void emit() throws Exception {
    register(
        "declare %ws:message('/p', '{$m}') function m:msg($m) { ws:emit('all:' || $m) };");

    final Listener la = new Listener();
    final Listener lb = new Listener();
    final java.net.http.WebSocket wsa = connect("/p", la);
    final java.net.http.WebSocket wsb = connect("/p", lb);
    try {
      wsa.sendText("hi", true).get(5, TimeUnit.SECONDS);
      assertEquals("all:hi", la.pollText());
      assertEquals("all:hi", lb.pollText());
    } finally {
      close(wsa);
      close(wsb);
    }
  }

  /**
   * {@code ws:broadcast($msg)} sends to all clients except the sender.
   * @throws Exception exception
   */
  @Test public void broadcast() throws Exception {
    register(
        "declare %ws:message('/p', '{$m}') function m:msg($m) { ws:broadcast('other:' || $m) };");

    final Listener la = new Listener();
    final Listener lb = new Listener();
    final java.net.http.WebSocket wsa = connect("/p", la);
    final java.net.http.WebSocket wsb = connect("/p", lb);
    try {
      wsa.sendText("ping", true).get(5, TimeUnit.SECONDS);
      assertEquals("other:ping", lb.pollText());
      // sender should not have received anything; allow some time and assert absence
      Thread.sleep(200);
      assertNull(la.texts.poll(), "Sender should not have received broadcast.");
    } finally {
      close(wsa);
      close(wsb);
    }
  }

  /**
   * {@code ws:send($msg, $ids)} sends only to the explicitly addressed clients.
   * @throws Exception exception
   */
  @Test public void send() throws Exception {
    // connect handler sends the new client its own id (target=self only, to avoid noise)
    register(
        "declare %ws:connect('/p') function m:c() { ws:send(ws:id(), ws:id()) };" +
        "declare %ws:message('/p', '{$m}') function m:msg($m) {" +
        "  ws:send('target:' || ws:id(), tokenize($m, ','))" +
        "};");

    final Listener la = new Listener();
    final Listener lb = new Listener();
    final Listener lc = new Listener();
    final java.net.http.WebSocket wsa = connect("/p", la);
    final String aid = la.pollText();
    final java.net.http.WebSocket wsb = connect("/p", lb);
    final String bid = lb.pollText();
    final java.net.http.WebSocket wsc = connect("/p", lc);
    final String cid = lc.pollText();

    try {
      // a tells server to send to b and c only
      wsa.sendText(bid + ',' + cid, true).get(5, TimeUnit.SECONDS);
      assertEquals("target:" + aid, lb.pollText());
      assertEquals("target:" + aid, lc.pollText());
      Thread.sleep(200);
      assertNull(la.texts.poll(), "Sender (a) should not have received its own send().");
    } finally {
      close(wsa);
      close(wsb);
      close(wsc);
    }
  }

  /**
   * {@code ws:set} / {@code ws:get} / {@code ws:delete} round-trip on the
   * current connection's attribute map.
   * @throws Exception exception
   */
  @Test public void setGetDelete() throws Exception {
    register(
        "declare %ws:message('/p', '{$m}') function m:msg($m) {" +
        "  switch($m)" +
        "    case 'set'    return (ws:set(ws:id(), 'k', 'hello'),    ws:emit('done'))" +
        "    case 'get'    return ws:emit(ws:get(ws:id(), 'k'))" +
        "    case 'delete' return (ws:delete(ws:id(), 'k'),           ws:emit('done'))" +
        "    case 'gone'   return ws:emit(ws:get(ws:id(), 'k', 'fallback'))" +
        "    default       return ws:emit('?')" +
        "};");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/p", l);
    try {
      ws.sendText("set", true).get(5, TimeUnit.SECONDS);
      assertEquals("done", l.pollText());
      ws.sendText("get", true).get(5, TimeUnit.SECONDS);
      assertEquals("hello", l.pollText());
      ws.sendText("delete", true).get(5, TimeUnit.SECONDS);
      assertEquals("done", l.pollText());
      ws.sendText("gone", true).get(5, TimeUnit.SECONDS);
      assertEquals("fallback", l.pollText());
    } finally {
      close(ws);
    }
  }

  /**
   * {@code ws:get($unknown-id, ...)} raises a query error which is delivered to the client.
   * @throws Exception exception
   */
  @Test public void unknownIdError() throws Exception {
    register(
        "declare %ws:message('/p', '{$m}') function m:msg($m) {" +
        "  ws:get('websocket-does-not-exist', 'k')" +
        "};");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/p", l);
    try {
      ws.sendText("?", true).get(5, TimeUnit.SECONDS);
      final String msg = l.pollText();
      assertTrue(msg.toLowerCase(java.util.Locale.ROOT).contains("websocket")
          || msg.contains("not found"), "Expected error message; got: " + msg);
    } finally {
      close(ws);
    }
  }
}
