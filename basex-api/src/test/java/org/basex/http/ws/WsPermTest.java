package org.basex.http.ws;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;

import org.junit.jupiter.api.*;

/**
 * Permission checks for WebSocket upgrades.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsPermTest extends WsTest {
  /**
   * Permission function is invoked with the full request path.
   * @throws Exception exception
   */
  @Test public void checked() throws Exception {
    register(
      "declare %perm:check('ws', '{$perm}') function m:check($perm) {" +
      "  cache:put('path', $perm?path)," +
      "  cache:put('method', $perm?method)," +
      "  cache:put('allow', string-join($perm?allow))" +
      "};" +
      "declare %ws:message('/app', '{$message}') function m:message($message) { $message };");

    final Listener listener = new Listener();
    final java.net.http.WebSocket ws = connect("/app", listener);
    ws.sendText("echo", true).get(5, TimeUnit.SECONDS);
    assertEquals("echo", listener.pollText());
    close(ws);

    awaitCache("path", "/ws/app");
    awaitCache("method", "GET");
    awaitCache("allow", "");
  }

  /**
   * Permission function raises an error: upgrade is refused.
   * @throws Exception exception
   */
  @Test public void forbidden() throws Exception {
    register(
      "declare %perm:check('ws') function m:check() { web:error(403, 'Forbidden.') };" +
      "declare %ws:message('/app', '{$message}') function m:message($message) { $message };");

    assertThrows(ExecutionException.class, () -> connect("/app", new Listener()));
  }

  /**
   * Permission function redirects (as e.g. the DBA does): upgrade is refused.
   * @throws Exception exception
   */
  @Test public void redirect() throws Exception {
    register(
      "declare %perm:check('ws') function m:check() { web:redirect('/login') };" +
      "declare %ws:message('/app', '{$message}') function m:message($message) { $message };");

    assertThrows(ExecutionException.class, () -> connect("/app", new Listener()));
  }

  /**
   * Permission paths are not matched against the servlet-relative path.
   * @throws Exception exception
   */
  @Test public void otherPath() throws Exception {
    register(
      "declare %perm:check('app') function m:check() { web:error(403, 'Forbidden.') };" +
      "declare %ws:message('/app', '{$message}') function m:message($message) { $message };");

    // check path 'app' does not match '/ws/app'
    final Listener listener = new Listener();
    final java.net.http.WebSocket ws = connect("/app", listener);
    ws.sendText("echo", true).get(5, TimeUnit.SECONDS);
    assertEquals("echo", listener.pollText());
    close(ws);
  }

  /**
   * Unknown paths are rejected before permission checks are invoked.
   * @throws Exception exception
   */
  @Test public void unknownPath() throws Exception {
    register(
      "declare %perm:check('ws') function m:check() {" +
      "  cache:put('checked', 'yes'), web:error(403, 'Forbidden.')" +
      "};" +
      "declare %ws:message('/app', '{$message}') function m:message($message) { $message };");

    // no function matches the path: check function is not invoked
    assertThrows(ExecutionException.class, () -> connect("/unknown", new Listener()));
    assertEquals("", cacheGet("checked"));

    // function matches the path: check function is invoked
    assertThrows(ExecutionException.class, () -> connect("/app", new Listener()));
    awaitCache("checked", "yes");
  }
}
