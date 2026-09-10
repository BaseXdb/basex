package org.basex.local.single;

import static org.junit.jupiter.api.Assumptions.*;

import java.lang.management.*;
import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Measures index creation, query latency and small-transaction throughput on the large document
 * specified with the system property {@code xmark}, as reference numbers for the updatable
 * full-text index.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FTIndexBaselineTest extends SandboxTest {
  /** Input document. */
  private static final String INPUT = System.getProperty("xmark", "f:/XML/xmark/1gb.xml");
  /** Number of update transactions. */
  private static final int UPDATES = 200;
  /** Number of runs per query. */
  private static final int RUNS = 6;

  /** Skips the test if the input document is missing. */
  @BeforeAll public static void input() {
    assumeTrue(new IOFile(INPUT).exists(), "Input document not found: " + INPUT);
  }

  /** Drops the database. */
  @AfterEach public void after() {
    execute(new DropDB(NAME));
    set(MainOptions.FTINDEX, false);
    set(MainOptions.UPDINDEX, false);
  }

  /**
   * Creates a full-text index and measures query latency.
   * @throws Exception exception
   */
  @Test public void fulltext() throws Exception {
    set(MainOptions.FTINDEX, true);
    final long start = System.nanoTime(), cpu = cpu();
    execute(new CreateDB(NAME, INPUT));
    Util.println("Create with full-text index: % ms wall, % ms cpu, % MB peak heap",
      (System.nanoTime() - start) / 1000000, (cpu() - cpu) / 1000000, peakHeap());

    for(final String token : new String[] { "will", "states" }) {
      for(final String query : new String[] {
        "count(ft:search('" + NAME + "', '" + token + "'))",
        "count(ft:search('" + NAME + "', '" + token + "', map { 'fuzzy': true() }))",
        "count(db:get('" + NAME + "')//text[text() contains text '" + token + "'])"
      }) {
        final long[] runs = new long[RUNS];
        String result = "";
        for(int r = 0; r < RUNS; r++) {
          final long c = cpu();
          try(QueryProcessor qp = new QueryProcessor(query, context)) {
            result = qp.value().serialize().toString();
          }
          runs[r] = (cpu() - c) / 1000000;
        }
        Util.println("%: % hits, cold % ms, warm min % ms", query, result, runs[0],
          Arrays.stream(runs, 1, RUNS).min().getAsLong());
      }
    }
  }

  /**
   * Runs small update transactions on a database with updatable value indexes.
   * @throws Exception exception
   */
  @Test public void updates() throws Exception {
    set(MainOptions.UPDINDEX, true);
    execute(new CreateDB(NAME, INPUT));
    final int nodes = Integer.parseInt(query("db:info('" + NAME + "')//nodes/text()"));

    // first run: the old values are the frequent original texts, and deleting one of their IDs
    // rewrites the whole ID list of the text index key; second run: the old values are unique
    for(final String run : new String[] { "original", "unique" }) {
      final Random rnd = new Random(0);
      final long[] times = new long[UPDATES];
      final long start = System.nanoTime(), cpu = cpu();
      for(int u = 0; u < UPDATES; u++) {
        final int pre = rnd.nextInt(nodes);
        final long c = cpu();
        query("let $t := (db:get-pre('" + NAME + "', " + pre + ")/descendant-or-self::text())[1] " +
          "return if(exists($t)) { replace value of node $t with '" + run + ' ' + u + "' }");
        times[u] = (cpu() - c) / 1000000;
      }
      Arrays.sort(times);
      Util.println("% update transactions, old values %: % ms wall, % ms cpu, " +
        "median % ms, max % ms (sandbox: %)", UPDATES, run, (System.nanoTime() - start) / 1000000,
        (cpu() - cpu) / 1000000, times[UPDATES / 2], times[UPDATES - 1], sandbox());
    }
  }

  /**
   * Returns the CPU time of the current thread.
   * @return nanoseconds
   */
  private static long cpu() {
    return ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
  }

  /**
   * Returns the peak usage of the old generation.
   * @return megabytes
   */
  private static long peakHeap() {
    long peak = 0;
    for(final MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
      final String name = pool.getName();
      if(name.contains("Old") || name.contains("Tenured")) peak += pool.getPeakUsage().getUsed();
    }
    return peak >> 20;
  }
}
