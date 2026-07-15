package org.basex.local.single;

import java.util.*;

import org.basex.index.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;

/**
 * Micro-benchmark for the updatable ID -&gt; PRE mapping ({@link IdPreMap}).
 *
 * <p>Isolates the two dominant costs of the structure so that optimizations
 * (interval coalescing, a last-interval cache, a secondary FID index) can be
 * measured without the noise of the full update stack:</p>
 * <ul>
 *   <li><b>Build</b> &ndash; {@code insert} shifts all following tuples (array
 *       copy) and re-touches them ({@code increment}), so a batch of {@code n}
 *       random-position updates is {@code O(n^2)}.</li>
 *   <li><b>Lookup</b> &ndash; {@code pre(id)} bisects for original IDs, but
 *       falls back to a linear scan over all tuples for inserted IDs
 *       ({@code id > baseid}).</li>
 * </ul>
 *
 * <p>The reported tuple count ({@link IdPreMap#size()}) is the figure that
 * coalescing would reduce; both build and scan costs are proportional to it.
 * Not part of the regular test run &ndash; execute on demand.</p>
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IdPreMapBench {
  /** Number of records before the first update. */
  private static final int BASEID = 1_000;
  /** Numbers of update operations to benchmark (drives the map size). */
  private static final int[] SIZES = { 5_000, 10_000, 20_000, 40_000 };
  /** Number of lookups per measurement. */
  private static final int LOOKUPS = 5_000;
  /** Fixed seed for reproducible runs. */
  private static final long SEED = 0x5DEECE66DL;
  /** Sink to keep the JIT from eliminating the lookup loops. */
  @SuppressWarnings("unused")
  private static volatile long blackhole;

  /** Random single inserts, followed by inserted-ID and original-ID lookups. */
  @Test public void singleInserts() {
    run("single inserts (c = 1)", 1);
  }

  /** Random bulk inserts, followed by inserted-ID and original-ID lookups. */
  @Test public void bulkInserts() {
    run("bulk inserts (c = 1..50)", 50);
  }

  /**
   * Random single insert/delete churn: reports the resulting tuple count, which
   * measures how much interval fragmentation the map accumulates (and hence how
   * much coalescing can reclaim). Deletes always target a still-present inserted
   * record, whose current PRE is obtained from the forward map, so the operation
   * arguments stay valid without a reference map.
   */
  @Test public void insertDeleteChurn() {
    // warm up the JIT
    churn(SIZES[0]);

    Util.errln("");
    Util.errln("# insert/delete churn, base id = " + BASEID);
    Util.errln("#     ops |   tuples |     time");
    for(final int ops : SIZES) {
      final IdPreMap map = new IdPreMap(BASEID);
      final long time = churn(ops, map);
      Util.errln(String.format(Locale.ENGLISH, "%9d | %8d | %6.1f ms",
          ops, map.size(), Performance.nanoToMilli(time)));
    }
  }

  /**
   * Performs a random insert/delete churn on a fresh map (throwaway; for warm-up).
   * @param ops number of operations
   */
  private static void churn(final int ops) {
    churn(ops, new IdPreMap(BASEID));
  }

  /**
   * Performs a random insert/delete churn on the given map.
   * @param ops number of operations
   * @param map map to update
   * @return elapsed time in nanoseconds
   */
  private static long churn(final int ops, final IdPreMap map) {
    final Random rnd = new Random(SEED);
    final IntList present = new IntList();
    int id = BASEID + 1, n = BASEID + 1;
    final Performance p = new Performance();
    for(int i = 0; i < ops; i++) {
      if(present.size() == 0 || rnd.nextBoolean()) {
        // insert one record at a random position
        map.insert(rnd.nextInt(n), id, 1);
        present.add(id);
        id++;
        n++;
      } else {
        // delete a still-present inserted record via its current PRE
        final int j = rnd.nextInt(present.size());
        final int did = present.get(j);
        map.delete(map.pre(did), did, -1);
        final int last = present.pop();
        if(j < present.size()) present.set(j, last);
        n--;
      }
    }
    return p.nanoRuntime();
  }

  /**
   * Runs one benchmark series across all {@link #SIZES}.
   * @param title description of the operation mix
   * @param bulk maximum number of records per insert (1 = single inserts)
   */
  private static void run(final String title, final int bulk) {
    // warm up the JIT with a throwaway series
    build(SIZES[0], bulk);

    Util.errln("");
    Util.errln("# " + title + ", base id = " + BASEID + ", " + LOOKUPS + " lookups per row");
    Util.errln("#     ops |   tuples |     build |  scan/op (ins.) |  bisect/op (orig.)");
    for(final int ops : SIZES) {
      final IdPreMap map = build(ops, bulk);
      final Random rnd = new Random(SEED);

      // inserted IDs (id > baseid): linear-scan path
      Performance p = new Performance();
      long sum = 0;
      for(int i = 0; i < LOOKUPS; i++) sum += map.pre(BASEID + 1 + rnd.nextInt(lastId - BASEID));
      final long scan = p.nanoRuntime();

      // original IDs (id <= baseid): binary-search path
      p = new Performance();
      for(int i = 0; i < LOOKUPS; i++) sum += map.pre(rnd.nextInt(BASEID + 1));
      final long bisect = p.nanoRuntime();
      blackhole = sum;

      Util.errln(String.format(Locale.ENGLISH, "%9d | %8d | %6.1f ms | %11d ns | %14d ns",
          ops, map.size(), Performance.nanoToMilli(buildTime), scan / LOOKUPS, bisect / LOOKUPS));
    }
  }

  /** Build time of the most recent {@link #build} call, in nanoseconds. */
  private static long buildTime;
  /** Last ID assigned by the most recent {@link #build} call. */
  private static int lastId;

  /**
   * Builds a map by performing the given number of random-position inserts.
   * @param ops number of insert operations
   * @param bulk maximum number of records per insert
   * @return populated map
   */
  private static IdPreMap build(final int ops, final int bulk) {
    final Random rnd = new Random(SEED);
    final IdPreMap map = new IdPreMap(BASEID);
    final Performance p = new Performance();
    int id = BASEID + 1;
    for(int i = 0; i < ops; i++) {
      final int c = 1 + rnd.nextInt(bulk);
      map.insert(rnd.nextInt(id), id, c);
      id += c;
    }
    buildTime = p.nanoRuntime();
    lastId = id - 1;
    return map;
  }
}
