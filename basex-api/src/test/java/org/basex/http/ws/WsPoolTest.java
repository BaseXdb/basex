package org.basex.http.ws;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
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
   * {@code ws:ping} sends a protocol-level ping frame; no message handler is involved
   * on either side.
   * @throws Exception exception
   */
  @Test public void ping() throws Exception {
    register(
        "declare %ws:message('/p', '{$m}') function m:msg($m) { ws:ping(ws:id()) };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/p", l);
    try {
      ws.sendText("beat", true).get(5, TimeUnit.SECONDS);
      await(() -> l.pinged ? Boolean.TRUE : null);
      assertNull(l.texts.poll(), "Ping should not arrive as a text message.");
    } finally {
      close(ws);
    }
  }

  /**
   * Pool functions can be called from outside a WebSocket context: a scheduled job
   * pings all connected clients (as the chat demo's heartbeat does).
   * @throws Exception exception
   */
  @Test public void pingFromJob() throws Exception {
    register("declare %ws:message('/p', '{$m}') function m:msg($m) {"
        + " void(job:eval('ws:ids() ! ws:ping(.)', ())) };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/p", l);
    try {
      ws.sendText("go", true).get(5, TimeUnit.SECONDS);
      await(() -> l.pinged ? Boolean.TRUE : null);
    } finally {
      close(ws);
    }
  }

  /**
   * Several jobs push large messages to the same client at the same time; all of them must
   * arrive, as only a single send operation may be in progress for a connection.
   * @throws Exception exception
   */
  @Test public void concurrentSends() throws Exception {
    final int count = 20;
    register("declare %ws:message('/p', '{$m}') function m:msg($m) {" +
        "  let $id := ws:id()" +
        "  let $payload := string-join((1 to 100000) ! 'x')" +
        "  return (1 to " + count + ") ! void(job:eval(" +
        "    'declare variable $m external; declare variable $id external; ws:send($m, $id)'," +
        "    { 'm': 'm' || . || ':' || $payload, 'id': $id }, { 'start': 'PT1S' }))" +
        "};");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/p", l);
    try {
      ws.sendText("go", true).get(5, TimeUnit.SECONDS);
      final HashSet<String> received = new HashSet<>();
      for(int c = 0; c < count; c++) {
        final String text = l.pollText();
        received.add(text.substring(0, text.indexOf(':')));
      }
      for(int c = 1; c <= count; c++) {
        assertTrue(received.contains("m" + c), "Missing message: " + received);
      }
    } finally {
      close(ws);
    }
  }

  /**
   * {@code ws:get($unknown-id, ...)} raises a query error which reaches {@code %ws:error}.
   * @throws Exception exception
   */
  @Test public void unknownIdError() throws Exception {
    register(
        "declare %ws:message('/p', '{$m}') function m:msg($m) {" +
        "  ws:get('websocket-does-not-exist', 'k')" +
        "};" +
        "declare %ws:error('/p', '{$m}') function m:err($m) { ws:send($m, ws:id()) };");

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

  /**
   * {@code ws:list-details} reports the properties of a connection; an unknown ID yields an
   * empty sequence.
   * @throws Exception exception
   */
  @Test public void listDetails() throws Exception {
    register(
        "declare %ws:message('/p', '{$m}') function m:msg($m) {" +
        "  switch($m)" +
        "    case 'own'     return ws:emit(serialize(ws:list-details(ws:id())))" +
        "    case 'all'     return ws:emit('count=' || count(ws:list-details()))" +
        "    case 'unknown' return ws:emit('count=' || count(ws:list-details('websocket-none')))" +
        "    default        return ws:emit('?')" +
        "};");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/p", l);
    try {
      ws.sendText("own", true).get(5, TimeUnit.SECONDS);
      final String xml = l.pollText();
      assertTrue(xml.startsWith("<websocket "), "Unexpected element: " + xml);
      assertTrue(xml.contains("path=\"/p\""), "Path missing: " + xml);
      assertTrue(xml.contains("created=\""), "Creation time missing: " + xml);
      assertTrue(xml.contains("accessed=\""), "Access time missing: " + xml);

      ws.sendText("all", true).get(5, TimeUnit.SECONDS);
      assertEquals("count=1", l.pollText());
      ws.sendText("unknown", true).get(5, TimeUnit.SECONDS);
      assertEquals("count=0", l.pollText());
    } finally {
      close(ws);
    }
  }

  /**
   * {@code ws:names} lists the attributes of a connection; an unknown ID is rejected.
   * @throws Exception exception
   */
  @Test public void names() throws Exception {
    register(
        "declare %ws:message('/p', '{$m}') function m:msg($m) {" +
        "  let $id := ws:id()" +
        "  return switch($m)" +
        "    case 'set'     return (ws:set($id, 'a', 1), ws:set($id, 'b', 2), ws:emit('done'))" +
        "    case 'delete'  return (ws:delete($id, 'a'), ws:emit('done'))" +
        "    case 'names'   return ws:emit('names=' || string-join(sort(ws:names($id)), ','))" +
        "    case 'unknown' return ws:emit(" +
        "      try { ws:names('websocket-none') } catch ws:not-found { 'not-found' })" +
        "    default        return ws:emit('?')" +
        "};");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/p", l);
    try {
      ws.sendText("names", true).get(5, TimeUnit.SECONDS);
      assertEquals("names=", l.pollText());
      ws.sendText("set", true).get(5, TimeUnit.SECONDS);
      assertEquals("done", l.pollText());
      ws.sendText("names", true).get(5, TimeUnit.SECONDS);
      assertEquals("names=a,b", l.pollText());
      ws.sendText("delete", true).get(5, TimeUnit.SECONDS);
      assertEquals("done", l.pollText());
      ws.sendText("names", true).get(5, TimeUnit.SECONDS);
      assertEquals("names=b", l.pollText());
      ws.sendText("unknown", true).get(5, TimeUnit.SECONDS);
      assertEquals("not-found", l.pollText());
    } finally {
      close(ws);
    }
  }
}
