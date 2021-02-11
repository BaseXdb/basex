package org.basex.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.cmd.*;
import org.basex.util.*;
import org.junit.jupiter.api.Test;

/**
 * Local concurrency tests.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class LocalConcurrencyTest extends SandboxTest {
  /** Test for concurrently accessing and modifying users. */
  @Test public void userTest() {
    final AtomicInteger counter = new AtomicInteger();
    final Exception[] error = new Exception[1];
    final int runs = 500;

    try {
      execute(new CreateDB("store"));
      execute(new CreateUser("user", ""));
      execute(new Grant("write", "user"));

      for(int d = 0; d < runs; d++) {
        execute(new Grant("write", "user", "doc" + d));
        new Thread(() -> {
          try(Session session = new LocalSession(context, "user", "")) {
            session.execute(new Open("store"));
          } catch(final Exception ex) {
            error[0] = ex;
          }
          counter.incrementAndGet();
        }).start();
      }
    } finally {
      while(counter.get() < runs) Performance.sleep(1);
      if(error[0] != null) {
        error[0].printStackTrace();
        fail(error[0].toString());
      }
      execute(new DropDB("store"));
      execute(new DropUser("user"));
    }
  }
}
