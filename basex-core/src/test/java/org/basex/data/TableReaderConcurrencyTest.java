package org.basex.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.*;

import org.basex.*;
import org.basex.core.cmd.CreateDB;
import org.junit.jupiter.api.Test;

/**
 * Checks that readers survive updates that are started while a query is reading.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TableReaderConcurrencyTest extends SandboxTest {
  /** Number of update cycles. */
  private static final int CYCLES = 300;

  /**
   * Reads the table while updates are started and finished, and checks that no read fails.
   * Queries are compiled before database locks are acquired, so this overlap can occur.
   * @throws Exception exception
   */
  @Test public void staleReader() throws Exception {
    final StringBuilder sb = new StringBuilder("<x>");
    for(int i = 0; i < 20000; i++) sb.append("<e id='").append(i).append("'/>");
    execute(new CreateDB(NAME, sb.append("</x>").toString()));

    final Data data = context.data();
    final AtomicReference<Throwable> error = new AtomicReference<>();
    final AtomicBoolean stop = new AtomicBoolean();
    final Thread reader = new Thread(() -> {
      try {
        while(!stop.get()) {
          final int size = data.nodes();
          for(int pre = 0; pre < size; pre++) data.kind(pre);
        }
      } catch(final Throwable th) {
        error.compareAndSet(null, th);
      }
    });
    reader.start();

    try {
      for(int c = 0; c < CYCLES && error.get() == null; c++) {
        data.startUpdate(context.options);
        data.finishUpdate(context.options);
      }
    } finally {
      stop.set(true);
      reader.join();
    }

    final Throwable th = error.get();
    if(th != null) fail("read failed during a concurrent update: " + th);
  }
}
