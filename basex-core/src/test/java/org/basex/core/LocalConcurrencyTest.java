package org.basex.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.cmd.*;
import org.basex.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Local concurrency tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class LocalConcurrencyTest extends SandboxTest {
  /**
   * Test for concurrently accessing and modifying users.
   * @throws InterruptedException interrupted exception
   */
  @Test @Timeout(60) public void userTest() throws InterruptedException {
    final int runs = 500;
    final CountDownLatch stopped = new CountDownLatch(runs);
    final AtomicReference<Exception> error = new AtomicReference<>();

    try {
      execute(new CreateDB("store"));
      execute(new CreateUser("user", ""));
      execute(new Grant("write", "user"));

      for(int d = 0; d < runs; d++) {
        execute(new Grant("write", "user", "doc" + d));
        new Thread(() -> {
          try(Session session = new LocalSession(context)) {
            session.execute(new Open("store"));
          } catch(final Exception ex) {
            error.compareAndSet(null, ex);
          } finally {
            stopped.countDown();
          }
        }).start();
      }
    } finally {
      assertTrue(stopped.await(60, TimeUnit.SECONDS), "Threads did not finish in time.");
      final Exception ex = error.get();
      if(ex != null) {
        Util.stack(ex);
        fail(ex.toString());
      }
      execute(new DropDB("store"));
      execute(new DropUser("user"));
    }
  }
}
