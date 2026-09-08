package org.basex.local.single;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.query.*;
import org.junit.jupiter.api.Test;

/**
 * Checks that the namespace structure can be inspected while it is inflated by an update.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class NamespaceRaceTest extends SandboxTest {
  /** Number of documents. */
  private static final int DOCS = 5000;

  /**
   * Queries are compiled before database locks are acquired, so a query that inspects the
   * namespaces can overlap with an update that inflates the compressed structure.
   * @throws Exception exception
   */
  @Test public void compileDuringUpdate() throws Exception {
    // one namespace node per document: the compressed structure has a single inner node
    final StringBuilder sb = new StringBuilder();
    for(int d = 0; d < DOCS; d++) sb.append("<r xmlns='urn:d'><c/></r>");
    execute(new CreateDB(NAME));
    for(int d = 0; d < DOCS; d++) {
      execute(new Add(d + ".xml", "<r xmlns='urn:d'><c/></r>"));
    }
    execute(new Close());
    execute(new Open(NAME));

    final AtomicReference<Throwable> error = new AtomicReference<>();
    final AtomicBoolean stop = new AtomicBoolean();
    final Thread reader = new Thread(() -> {
      try {
        while(!stop.get()) {
          // compilation inspects the namespaces without holding a lock
          try(QueryProcessor qp = new QueryProcessor("db:get('" + NAME + "')//c", context)) {
            qp.compile();
          }
        }
      } catch(final Throwable th) {
        error.compareAndSet(null, th);
      }
    });
    reader.start();

    try {
      for(int c = 0; c < 20 && error.get() == null; c++) {
        // the update inflates the compressed structure and closes it
        execute(new XQuery("insert node <x/> into " + "db:get('" + NAME + "')/*[1]"));
        execute(new Close());
        execute(new Open(NAME));
      }
    } finally {
      stop.set(true);
      reader.join();
    }

    final Throwable th = error.get();
    if(th != null) fail("compilation failed during a concurrent update: " + th);
  }
}
