package org.basex.query.value.map;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.basex.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests that {@link XQMap} preserves insertion order across its representations (trie, specialized
 * hash maps, singleton) and under concurrent access to a shared map. Order is tracked separately
 * from the map contents ({@link TrieOrder}, {@link TrieKeys}), so these are the guards that keep
 * the two in sync.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MapOrderTest extends SandboxTest {
  /**
   * Randomized differential test: replays put/remove operations on an {@link XQMap} and on a
   * {@link LinkedHashMap} reference model, comparing keys and values in insertion order. A
   * re-{@code put} of an existing key keeps its position; a remove-then-{@code put} moves it to
   * the end -- identical semantics in both structures.
   * @throws QueryException query exception
   */
  @Test public void fuzzy() throws QueryException {
    final int ops = 30_000, domain = 400;
    final Random rng = new Random(42);
    final LinkedHashMap<Long, Long> ref = new LinkedHashMap<>();
    XQMap map = XQMap.empty();
    for(int i = 0; i < ops; i++) {
      final long key = rng.nextInt(domain);
      if(rng.nextInt(4) == 0) {
        ref.remove(key);
        map = map.remove(Itr.get(key));
      } else {
        final long val = i;
        ref.put(key, val);
        map = map.put(Itr.get(key), Itr.get(val));
      }
      if(i % 500 == 0) assertOrder(map, ref);
    }
    assertOrder(map, ref);
    assertTrue(ref.size() > 100, "reference model stayed trivially small");
  }

  /**
   * A removed key is re-appended at the end when it is put back.
   * @throws QueryException query exception
   */
  @Test public void removeReaddToEnd() throws QueryException {
    XQMap map = XQMap.empty();
    for(int i = 0; i < 5; i++) map = map.put(Itr.get(i), Itr.get(i));
    map = map.remove(Itr.get(1)).remove(Itr.get(3)).put(Itr.get(1), Itr.get(10));
    assertKeys(map, 0, 2, 4, 1);
    // 3 was removed and never re-added
    assertNull(map.getOrNull(Itr.get(3)));
    assertEquals(10L, ((Itr) map.getOrNull(Itr.get(1))).itr());
  }

  /**
   * Re-putting an existing key keeps its position and updates only the value.
   * @throws QueryException query exception
   */
  @Test public void valueReplaceKeepsPosition() throws QueryException {
    XQMap map = XQMap.empty();
    for(int i = 0; i < 6; i++) map = map.put(Itr.get(i), Itr.get(i));
    map = map.put(Itr.get(2), Itr.get(99)).put(Itr.get(0), Itr.get(88));
    assertKeys(map, 0, 1, 2, 3, 4, 5);
    assertEquals(88L, ((Itr) map.valueAt(0)).itr());
    assertEquals(99L, ((Itr) map.valueAt(2)).itr());
  }

  /**
   * Order survives the singleton boundary (2 entries -&gt; 1 -&gt; 2).
   * @throws QueryException query exception
   */
  @Test public void singletonBoundary() throws QueryException {
    XQMap map = XQMap.empty().put(Itr.get(1), Itr.get(1)).put(Itr.get(2), Itr.get(2));
    map = map.remove(Itr.get(1));
    assertEquals(1, map.structSize());
    map = map.put(Itr.get(9), Itr.get(9));
    assertKeys(map, 2, 9);
  }

  /**
   * Repeated add/remove of a single key must not corrupt the order or leak state.
   * @throws QueryException query exception
   */
  @Test public void singleKeyChurn() throws QueryException {
    final XQMap base = XQMap.empty().put(Itr.get(-1), Itr.get(-1)).put(Itr.get(-2), Itr.get(-2));
    XQMap map = base;
    for(int i = 0; i < 5000; i++) {
      map = (i & 1) == 0 ? map.put(Itr.get(7), Itr.get(i)) : map.remove(Itr.get(7));
    }
    // 5000 iterations: last op (i=4999, odd) is a remove -> only the two fixed keys remain
    assertKeys(map, -1, -2);
    // one more put lands at the end with the latest value
    map = map.put(Itr.get(7), Itr.get(123));
    assertKeys(map, -1, -2, 7);
    assertEquals(123L, ((Itr) map.getOrNull(Itr.get(7))).itr());
  }

  /**
   * Order is preserved when a specialized hash map (built via {@link MapBuilder}) is converted to
   * a trie by the first {@code put}/{@code remove}.
   * @throws QueryException query exception
   */
  @Test public void representationConversion() throws QueryException {
    // integer keys -> XQIntMap
    final MapBuilder imb = new MapBuilder();
    final LinkedHashMap<Long, Long> ref = new LinkedHashMap<>();
    for(int i = 0; i < 40; i++) {
      imb.put(Itr.get(i * 2L), Itr.get(i));
      ref.put(i * 2L, (long) i);
    }
    XQMap map = imb.map();
    assertOrder(map, ref);
    // force conversion to a trie: append a key, drop a middle one
    map = map.put(Itr.get(999), Itr.get(-1));
    ref.put(999L, -1L);
    map = map.remove(Itr.get(20));
    ref.remove(20L);
    assertOrder(map, ref);

    // string keys -> XQStrMap
    final MapBuilder smb = new MapBuilder();
    for(int i = 0; i < 30; i++) smb.put(Str.get("k" + i), Str.get("v" + i));
    final XQMap smap = smb.map();
    int i = 0;
    for(final Item key : smap.keys()) assertEquals("k" + i++, string(key));
    assertEquals(30, i);
  }

  /**
   * Derives many maps from one shared base map in parallel and checks that each result keeps a
   * consistent insertion order. Regression: the order tracking mutated a shared key array in
   * place, so concurrent {@code put}s could leak keys between results (fails on the unfixed code).
   * @throws Exception exception
   */
  @Test @Timeout(120) public void concurrentSharedBase() throws Exception {
    // base map with spare order capacity: the seam where the race lived
    XQMap base = XQMap.empty();
    for(int b = 0; b < 8; b++) base = base.put(Str.get("b" + b), Itr.get(b));
    final XQMap shared = base;
    final int threads = 32, per = 12, reps = 50;

    for(int rep = 0; rep < reps; rep++) {
      final AtomicReference<Throwable> error = new AtomicReference<>();
      // release all threads into the first (shared) put at once to widen the race window
      final CyclicBarrier barrier = new CyclicBarrier(threads);
      final Thread[] ts = new Thread[threads];
      for(int t = 0; t < threads; t++) {
        final int k = t;
        ts[t] = new Thread(() -> {
          try {
            barrier.await();
            XQMap m = shared;
            for(int j = 0; j < per; j++) m = m.put(Str.get("t" + k + '_' + j), Itr.get(j));
            assertEquals(8 + per, m.structSize());
            for(int b = 0; b < 8; b++) assertEquals("b" + b, string(m.keyAt(b)));
            for(int j = 0; j < per; j++) assertEquals("t" + k + '_' + j, string(m.keyAt(8 + j)));
          } catch(final Throwable ex) {
            error.compareAndSet(null, ex);
          }
        });
      }
      for(final Thread th : ts) th.start();
      for(final Thread th : ts) th.join();
      final Throwable ex = error.get();
      if(ex != null) throw new AssertionError("inconsistent order in rep " + rep, ex);
    }
  }

  /**
   * Checks that write-heavy churn that never reads the keys keeps the order state near the live
   * size (compaction in {@link TrieOrder#remove}).
   * @throws QueryException query exception
   */
  @Test public void churnBloat() throws QueryException {
    final int keep = 64, churn = 20_000;
    XQMap map = XQMap.empty();
    for(int i = 0; i < keep; i++) map = map.put(Itr.get(i), Itr.get(i));
    // put and remove one extra key over and over; never read the keys back
    for(int i = 0; i < churn; i++) {
      map = (i & 1) == 0 ? map.put(Itr.get(999), Itr.get(i)) : map.remove(Itr.get(999));
    }
    final long live = map.structSize();
    assertEquals(keep, live);
    final int state = orderState(map);
    assertTrue(state <= 8 * live, "order state " + state + " exceeds 8x the live size " + live);
  }

  /**
   * Returns the number of key references retained by a map's order structure.
   * @param map map to inspect
   * @return retained key-reference count (0 for empty/singleton maps without an order)
   */
  private static int orderState(final XQMap map) {
    if(!(map instanceof final XQTrieMap tm)) return 0;
    final TrieOrder order = tm.order();
    return order == null ? 0 : order.retained();
  }

  /**
   * Asserts that a map's keys, values, {@code keys()} sequence, and lookups all agree with a
   * reference model in insertion order.
   * @param map map to check
   * @param ref reference model (integer keys and values)
   * @throws QueryException query exception
   */
  private static void assertOrder(final XQMap map, final LinkedHashMap<Long, Long> ref)
      throws QueryException {
    assertEquals(ref.size(), map.structSize(), "size");
    int i = 0;
    for(final Map.Entry<Long, Long> e : ref.entrySet()) {
      assertEquals((long) e.getKey(), ((Itr) map.keyAt(i)).itr(), "keyAt " + i);
      assertEquals((long) e.getValue(), ((Itr) map.valueAt(i)).itr(), "valueAt " + i);
      final Value got = map.getOrNull(Itr.get(e.getKey()));
      assertNotNull(got, "getOrNull " + e.getKey());
      assertEquals((long) e.getValue(), ((Itr) got).itr(), "get value " + e.getKey());
      i++;
    }
    i = 0;
    final Iterator<Map.Entry<Long, Long>> it = ref.entrySet().iterator();
    for(final Item key : map.keys()) {
      assertEquals((long) it.next().getKey(), ((Itr) key).itr(), "keys() " + i++);
    }
    assertFalse(it.hasNext(), "keys() too short");
  }

  /**
   * Asserts that the integer keys of a map appear in the given order.
   * @param map map to check
   * @param keys expected keys
   */
  private static void assertKeys(final XQMap map, final long... keys) {
    assertEquals(keys.length, map.structSize(), "size");
    for(int i = 0; i < keys.length; i++) {
      assertEquals(keys[i], ((Itr) map.keyAt(i)).itr(), "keyAt " + i);
    }
  }

  /**
   * Returns the string value of an item.
   * @param item item
   * @return string
   * @throws QueryException query exception
   */
  private static String string(final Item item) throws QueryException {
    return Token.string(item.string(null));
  }
}
