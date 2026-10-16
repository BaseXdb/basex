package org.basex.http.ws;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the module cache of WebSocket functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsModuleTest extends WsTest {
  /**
   * Steady WebSocket messages do not keep modules from being checked for changes.
   * @throws Exception exception
   */
  @Test public void changedModule() throws Exception {
    register("declare %ws:message('/x', '{$m}') function m:msg($m) { ws:emit('old') };");

    final Listener l = new Listener();
    final java.net.http.WebSocket ws = connect("/x", l);
    try {
      ws.sendText("go", true).get(5, TimeUnit.SECONDS);
      assertEquals("old", l.pollText());

      final IOFile module = new IOFile(context.soptions.get(StaticOptions.WEBPATH)).children()[0];
      module.write(HEADER +
          "declare %ws:message('/x', '{$m}') function m:msg($m) { ws:emit('new') };");
      assertTrue(module.file().setLastModified(System.currentTimeMillis() - 60000));

      // messages are sent more often than the idle time of the module cache
      String text = "old";
      final long end = System.currentTimeMillis() + 10000;
      while(text.equals("old") && System.currentTimeMillis() < end) {
        Performance.sleep(500);
        ws.sendText("go", true).get(5, TimeUnit.SECONDS);
        text = l.pollText();
      }
      assertEquals("new", text);
    } finally {
      close(ws);
    }
  }
}
