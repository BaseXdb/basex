package org.basex.core;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.cmd.*;
import org.junit.Test;

/**
 * Local concurrency tests.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class LocalConcurrencyTest extends SandboxTest {
  /**
   * Test for concurrently accessing and modifying users.
   */
  @Test
  public void userTest() {
    final AtomicInteger counter = new AtomicInteger();
    final Exception[] error = new Exception[1];
    final int runs = 250;

    try {
      execute(new CreateDB("store"));
      execute(new CreateUser("user", ""));
      execute(new Grant("write", "user"));

      for(int d = 0; d < runs; d++) {
        execute(new Grant("write", "user", "doc" + d));
        new Thread() {
          @Override
          public void run() {
            try(Session session = new LocalSession(context, "user", "")) {
              session.execute(new Open("store"));
              counter.incrementAndGet();
            } catch(final Exception ex) {
              error[0] = ex;
            }
          }
        }.start();
      }
    } finally {
      while(counter.get() < runs && error[0] == null) Thread.yield();
      execute(new DropDB("store"));
      execute(new DropUser("user"));

      if(error[0] != null) {
        error[0].printStackTrace();
        fail(error[0].toString());
      }
    }
  }
}
