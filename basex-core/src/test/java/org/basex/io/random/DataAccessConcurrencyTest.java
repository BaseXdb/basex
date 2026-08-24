package org.basex.io.random;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.*;

import org.basex.*;
import org.basex.core.cmd.CreateDB;
import org.basex.data.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.Test;

/**
 * Checks that concurrent reads on one database return correct text lengths.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DataAccessConcurrencyTest extends SandboxTest {
  /** Number of reader threads. */
  private static final int THREADS = 8;
  /** Number of passes per thread. */
  private static final int PASSES = 40;

  /**
   * Reads text lengths from several threads and compares them with the expected values.
   * @throws Exception exception
   */
  @Test public void textLen() throws Exception {
    // texts long enough to be stored compressed: their length is read with two calls
    final StringBuilder sb = new StringBuilder("<x>");
    for(int i = 0; i < 2000; i++) {
      sb.append("<t>").append("abcdefghij".repeat(30)).append(i).append("</t>");
    }
    execute(new CreateDB(NAME, sb.append("</x>").toString()));

    final Data data = context.data();
    final IntList pres = new IntList();
    final int size = data.nodes();
    for(int pre = 0; pre < size; pre++) {
      if(data.kind(pre) == Data.TEXT) pres.add(pre);
    }
    final int ps = pres.size();
    assertTrue(ps > 1000, "no text nodes found");

    // expected values, read single-threaded
    final int[] expected = new int[ps];
    for(int p = 0; p < ps; p++) expected[p] = data.textLen(pres.get(p), true);

    final AtomicInteger wrong = new AtomicInteger();
    final Thread[] threads = new Thread[THREADS];
    for(int t = 0; t < THREADS; t++) {
      threads[t] = new Thread(() -> {
        for(int pass = 0; pass < PASSES; pass++) {
          for(int p = 0; p < ps; p++) {
            if(data.textLen(pres.get(p), true) != expected[p]) wrong.incrementAndGet();
          }
        }
      });
      threads[t].start();
    }
    for(final Thread thread : threads) thread.join();
    assertEquals(0, wrong.get(), "wrong text lengths from concurrent reads");
  }
}
