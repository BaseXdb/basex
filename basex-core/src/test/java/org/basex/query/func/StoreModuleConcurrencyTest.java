package org.basex.query.func;

import static org.basex.query.func.Function.*;

import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests the Store Module under concurrent access.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StoreModuleConcurrencyTest extends SandboxTest {
  /** Number of entries written by a single client. */
  private static final int ENTRIES = 200;
  /** Number of clients. */
  private static final int CLIENTS = 20;

  /**
   * Clears the store.
   */
  @BeforeEach public void init() {
    query(_STORE_CLEAR.args());
  }

  /**
   * Clears the store.
   */
  @AfterEach public void finish() {
    query(_STORE_CLEAR.args());
  }

  /**
   * Every client writes and reads its own keys.
   * @throws Exception exception
   */
  @Test @Timeout(60) public void distinctKeys() throws Exception {
    final ArrayList<Callable<?>> tasks = new ArrayList<>(CLIENTS);
    for(int c = 0; c < CLIENTS; c++) {
      final int client = c;
      tasks.add(() -> {
        for(int e = 0; e < ENTRIES; e++) {
          final String key = client + "-" + e;
          query(_STORE_PUT.args(key, e));
          query(_STORE_GET.args(key), e);
        }
        return null;
      });
    }
    parallel(tasks);
    query("count(" + _STORE_KEYS.args() + ')', CLIENTS * ENTRIES);
  }

  /**
   * All clients write to and read from the same key.
   * @throws Exception exception
   */
  @Test @Timeout(60) public void sharedKey() throws Exception {
    final ArrayList<Callable<?>> tasks = new ArrayList<>(CLIENTS);
    for(int c = 0; c < CLIENTS; c++) {
      final int client = c;
      tasks.add(() -> {
        for(int e = 0; e < ENTRIES; e++) {
          query(_STORE_PUT.args("key", client));
          // whatever is read back must be a value that some client has written
          query("let $value := " + _STORE_GET.args("key") +
              " return $value >= 0 and $value < " + CLIENTS, true);
        }
        return null;
      });
    }
    parallel(tasks);
    query("count(" + _STORE_KEYS.args() + ')', 1);
  }

  /**
   * All clients initialize the same key: every one of them must see the stored value.
   * @throws Exception exception
   */
  @Test @Timeout(60) public void getOrPut() throws Exception {
    parallel(CLIENTS, () -> {
      for(int e = 0; e < ENTRIES; e++) {
        query(_STORE_GET_OR_PUT.args("key", " fn() { 'value' }"), "value");
      }
      return null;
    });
    query(_STORE_GET.args("key"), "value");
    query("count(" + _STORE_KEYS.args() + ')', 1);
  }
}
