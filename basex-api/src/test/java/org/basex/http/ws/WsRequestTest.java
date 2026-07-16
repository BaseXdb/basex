package org.basex.http.ws;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Tests for request functions in WebSocket functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsRequestTest extends WsTest {
  /**
   * {@code request:method} returns the method of the upgrade request.
   * @throws Exception exception
   */
  @Test public void requestMethod() throws Exception {
    register(
        "declare %ws:connect('/h')" +
        "        function m:c() { ws:emit(request:method()) };" +
        "declare %ws:message('/h', '{$m}') function m:msg($m) { () };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/h", l);
    try {
      assertEquals("GET", l.pollText());
    } finally {
      close(ws);
    }
  }

  /**
   * {@code request:header} returns upgrade headers; lookup is case-insensitive.
   * @throws Exception exception
   */
  @Test public void requestHeader() throws Exception {
    register(
        "declare %ws:connect('/h')" +
        "        function m:c() { ws:emit(string(request:header('HOST'))) };" +
        "declare %ws:message('/h', '{$m}') function m:msg($m) { () };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/h", l);
    try {
      final String host = l.pollText();
      assertTrue(host.contains("localhost"), "host should contain localhost: " + host);
    } finally {
      close(ws);
    }
  }

  /**
   * A header that is not sent by the upgrade request yields the supplied default.
   * @throws Exception exception
   */
  @Test public void requestHeaderDefault() throws Exception {
    register(
        "declare %ws:connect('/h')" +
        "        function m:c() { ws:emit(request:header('Origin', 'fallback')) };" +
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
   * {@code request:scheme} and {@code request:hostname} return the values of the upgrade request.
   * @throws Exception exception
   */
  @Test public void requestScheme() throws Exception {
    register(
        "declare %ws:connect('/h')" +
        "        function m:c() { ws:emit(request:scheme() || '|' || request:hostname()) };" +
        "declare %ws:message('/h', '{$m}') function m:msg($m) { () };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/h", l);
    try {
      assertEquals("http|localhost", l.pollText());
    } finally {
      close(ws);
    }
  }

  /**
   * {@code request:parameter} returns the query parameters of the upgrade request.
   * @throws Exception exception
   */
  @Test public void requestParameter() throws Exception {
    register(
        "declare %ws:connect('/h')" +
        "        function m:c() { ws:emit(request:parameter('version')) };" +
        "declare %ws:message('/h', '{$m}') function m:msg($m) { () };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/h?version=12", l);
    try {
      assertEquals("12", l.pollText());
    } finally {
      close(ws);
    }
  }

  /**
   * {@code request:query} returns the raw query string of the upgrade request.
   * @throws Exception exception
   */
  @Test public void requestQuery() throws Exception {
    register(
        "declare %ws:connect('/h')" +
        "        function m:c() { ws:emit(request:query()) };" +
        "declare %ws:message('/h', '{$m}') function m:msg($m) { () };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/h?a=1&b=2", l);
    try {
      assertEquals("a=1&b=2", l.pollText());
    } finally {
      close(ws);
    }
  }

  /**
   * {@code request:uri} returns the URL of the upgrade request.
   * @throws Exception exception
   */
  @Test public void requestUri() throws Exception {
    register(
        "declare %ws:connect('/h')" +
        "        function m:c() { ws:emit(ends-with(request:uri(), '/ws/h')) };" +
        "declare %ws:message('/h', '{$m}') function m:msg($m) { () };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/h", l);
    try {
      assertEquals("true", l.pollText());
    } finally {
      close(ws);
    }
  }

  /**
   * {@code request:path} returns the path of the upgrade request.
   * @throws Exception exception
   */
  @Test public void requestPath() throws Exception {
    register(
        "declare %ws:connect('/h')" +
        "        function m:c() { ws:emit(request:path()) };" +
        "declare %ws:message('/h', '{$m}') function m:msg($m) { () };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/h", l);
    try {
      assertEquals("/ws/h", l.pollText());
    } finally {
      close(ws);
    }
  }
}
