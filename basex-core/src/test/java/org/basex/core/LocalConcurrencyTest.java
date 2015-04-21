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
   * @throws Exception exception
   */
  @Test
  public void userTest() throws Exception {
    final AtomicInteger counter = new AtomicInteger();
    final Exception[] error = new Exception[1];
    final int runs = 250;

    try {
      new CreateDB("store").execute(context);
      new CreateUser("user", "").execute(context);
      new Grant("write", "user").execute(context);

      for(int d = 0; d < runs; d++) {
        new Grant("write", "user", "doc" + d).execute(context);
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
      new DropDB("store").execute(context);
      new DropUser("user").execute(context);

      if(error[0] != null) {
        error[0].printStackTrace();
        fail(error[0].toString());
      }
    }
  }
}
