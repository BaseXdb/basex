package org.basex.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.util.*;
import org.junit.jupiter.api.Test;

/**
 * Checks that the compressed namespace structure can be read by several threads at a time.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class NamespaceConcurrencyTest extends SandboxTest {
  /** Number of elements that declare namespaces. */
  private static final int COUNT = 10000;
  /** Number of readers. */
  private static final int READERS = 8;
  /** Number of lookups per reader. */
  private static final int LOOKUPS = 20000;
  /** Number of distinct sets of prefix/URI pairs. */
  private static final int SETS = 20;

  /**
   * Resolves prefixes in several threads. The lookups jump between the blocks of the structure,
   * so the readers compete for the cached block.
   * @throws Exception exception
   */
  @Test public void concurrentLookups() throws Exception {
    final StringBuilder sb = new StringBuilder("<a xmlns:x='X'>");
    for(int c = 0; c < COUNT; c++) {
      sb.append("<b xmlns:p").append(c % SETS).append("='U").append(c % SETS).append("'/>");
    }
    execute(new CreateDB(NAME, sb.append("</a>").toString()));
    // reopen the database: the compressed structure is read from disk
    execute(new Close());
    execute(new Open(NAME));

    final Data data = context.data();
    final AtomicReference<Throwable> error = new AtomicReference<>();
    final Thread[] readers = new Thread[READERS];
    for(int r = 0; r < READERS; r++) {
      final int seed = r;
      readers[r] = new Thread(() -> {
        try {
          final Random rnd = new Random(seed);
          for(int l = 0; l < LOOKUPS; l++) {
            final int c = rnd.nextInt(COUNT);
            // PRE values: 0 is the document, 1 the root element
            final int pre = c + 2;
            final byte[] prefix = Token.token("p" + c % SETS);
            final int uriId = data.nspaces.uriIdForPrefix(prefix, pre, data);
            assertEquals("U" + c % SETS, Token.string(data.nspaces.uri(uriId)));
          }
        } catch(final Throwable th) {
          error.compareAndSet(null, th);
        }
      });
    }
    for(final Thread reader : readers) reader.start();
    for(final Thread reader : readers) reader.join();

    final Throwable th = error.get();
    if(th != null) fail("lookup failed during concurrent access: " + th);
  }
}
