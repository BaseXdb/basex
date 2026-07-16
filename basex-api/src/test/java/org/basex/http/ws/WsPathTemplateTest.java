package org.basex.http.ws;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;

import org.junit.jupiter.api.*;

/**
 * Tests for path templates in WebSocket annotations.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsPathTemplateTest extends WsTest {
  /**
   * A template variable is bound from the connection path.
   * @throws Exception exception
   */
  @Test public void bindVariable() throws Exception {
    register("declare %ws:message('/chat/{$room}', '{$m}') function m:msg($room, $m) {"
        + " ws:send($room || ':' || $m, ws:id()) };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/chat/lobby", l);
    try {
      ws.sendText("hi", true).get(5, TimeUnit.SECONDS);
      assertEquals("lobby:hi", l.pollText());
    } finally {
      close(ws);
    }
  }

  /**
   * Template variables are connection-scoped: the value bound at the handshake is
   * identical in the connect handler and in every message handler of the connection.
   * @throws Exception exception
   */
  @Test public void connectionScoped() throws Exception {
    register(
        "declare %ws:connect('/chat/{$room}') function m:c($room) {"
        + " ws:send('c:' || $room, ws:id()) };" +
        "declare %ws:message('/chat/{$room}', '{$m}') function m:msg($room, $m) {"
        + " ws:send('m:' || $room, ws:id()) };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/chat/games", l);
    try {
      assertEquals("c:games", l.pollText());
      ws.sendText("one", true).get(5, TimeUnit.SECONDS);
      assertEquals("m:games", l.pollText());
      ws.sendText("two", true).get(5, TimeUnit.SECONDS);
      assertEquals("m:games", l.pollText());
    } finally {
      close(ws);
    }
  }

  /**
   * Multiple template variables in a single path.
   * @throws Exception exception
   */
  @Test public void multipleVariables() throws Exception {
    register("declare %ws:message('/{$a}/x/{$b}', '{$m}') function m:msg($a, $b, $m) {"
        + " ws:send($a || '-' || $b, ws:id()) };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/first/x/second", l);
    try {
      ws.sendText("go", true).get(5, TimeUnit.SECONDS);
      assertEquals("first-second", l.pollText());
    } finally {
      close(ws);
    }
  }

  /**
   * Template values are coerced to the declared parameter type.
   * @throws Exception exception
   */
  @Test public void typedVariable() throws Exception {
    register("declare %ws:message('/n/{$i}', '{$m}') function m:msg($i as xs:integer, $m) {"
        + " ws:send(string($i + 1), ws:id()) };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/n/41", l);
    try {
      ws.sendText("go", true).get(5, TimeUnit.SECONDS);
      assertEquals("42", l.pollText());
    } finally {
      close(ws);
    }
  }

  /**
   * A literal path is more specific than a template and wins.
   * @throws Exception exception
   */
  @Test public void specificity() throws Exception {
    register(
        "declare %ws:message('/chat/lobby', '{$m}') function m:l($m) {"
        + " ws:send('literal', ws:id()) };" +
        "declare %ws:message('/chat/{$room}', '{$m}') function m:t($room, $m) {"
        + " ws:send('template', ws:id()) };");

    final Listener ll = new Listener();
    final java.net.http.WebSocket wsl = connect("/chat/lobby", ll);
    final Listener lt = new Listener();
    final java.net.http.WebSocket wst = connect("/chat/other", lt);
    try {
      wsl.sendText("x", true).get(5, TimeUnit.SECONDS);
      assertEquals("literal", ll.pollText());
      wst.sendText("y", true).get(5, TimeUnit.SECONDS);
      assertEquals("template", lt.pollText());
    } finally {
      close(wsl);
      close(wst);
    }
  }

  /**
   * A variable with a regular expression constraint rejects non-matching paths
   * at the handshake.
   * @throws Exception exception
   */
  @Test public void regexConstraint() throws Exception {
    register("declare %ws:message('/n/{$i=[0-9]+}', '{$m}') function m:msg($i, $m) {"
        + " ws:send($i, ws:id()) };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/n/123", l);
    try {
      ws.sendText("go", true).get(5, TimeUnit.SECONDS);
      assertEquals("123", l.pollText());
    } finally {
      close(ws);
    }
    assertThrows(Exception.class, () -> connect("/n/abc", new Listener()));
  }

  /**
   * Two equally specific templates for the same path and annotation are refused
   * at the handshake.
   */
  @Test public void conflict() {
    assertThrows(Exception.class, () -> {
      register(
          "declare %ws:message('/x/{$a}', '{$m}') function m:a($a, $m) { () };" +
          "declare %ws:message('/x/{$b}', '{$m}') function m:b($b, $m) { () };");
      connect("/x/1", new Listener());
    });
  }

  /**
   * A conflict on one annotation refuses the handshake even if another annotation
   * matches the path unambiguously.
   */
  @Test public void conflictOtherAnnotation() {
    assertThrows(Exception.class, () -> {
      register(
          "declare %ws:message('/x/{$a}', '{$m}') function m:msg($a, $m) { () };" +
          "declare %ws:close('/x/{$a}') function m:a($a) { () };" +
          "declare %ws:close('/x/{$b}') function m:b($b) { () };");
      connect("/x/1", new Listener());
    });
  }

  /**
   * A template variable that does not match a function parameter is rejected when
   * the module is parsed.
   * @throws Exception exception
   */
  @Test public void undeclaredVariable() throws Exception {
    register("declare %ws:message('/chat/{$room}', '{$m}') function m:msg($m) { () };");
    assertThrows(Exception.class, () -> connect("/chat/lobby", new Listener()));
  }
}
