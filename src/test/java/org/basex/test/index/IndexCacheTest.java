package org.basex.test.index;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.util.*;

import org.basex.index.*;
import org.basex.index.IndexCache.CacheEntry;
import org.junit.*;

/** Tests for {@link IndexCache}. */
public final class IndexCacheTest {
  /** Test instance. */
  private IndexCache cache;

  /** Set up method. */
  @Before
  public void setUp() {
    cache = new IndexCache();
  }

  /** Test for method {@link IndexCache#add(byte[], int, long)}. */
  @Test
  public void testAdd() {
    for(int i = 0; i < 4000; ++i) {
      final byte[] key = token("keyAdd" + i);
      final int size = i;
      final long pointer = i + 5000L;

      cache.add(key, size, pointer);
      assertCacheEntry(key, size, pointer);
    }
  }

  /** Test for method {@link IndexCache#add(byte[], int, long)}: update. */
  @Test
  public void testUpdate() {
    final byte[] key = token("keyUpdate");
    final int size = 10;
    final long pointer = 12L;

    cache.add(key, size - 1, pointer - 1L);
    cache.add(key, size, pointer);

    assertCacheEntry(key, size, pointer);
  }

  /** Test for method {@link IndexCache#delete(byte[])}. */
  @Test
  public void testDelete() {
    final byte[] key = token("keyDelete");
    final int size = 10;
    final long pointer = 12L;

    cache.add(key, size, pointer);
    cache.delete(key);

    assertNull(cache.get(key));
  }

  /**
   * Test that new records can be continuously added without hitting
   * {@link OutOfMemoryError}.
   * [DP] assert progress, i.e. that the loop is not stuck.
   */
  @Test
  @Ignore("Start this test with a small heap size (e.g. -Xmx64m)")
  public void testPerformance() {
    final Random random = new Random(System.nanoTime());

    while(true) {
      final byte[] key = new byte[100];
      random.nextBytes(key);
      final int size = random.nextInt();
      final long pointer = random.nextLong();

      cache.add(key, size, pointer);
    }
  }

  /**
   * Assert a cache entry is found in the cache.
   * @param key key
   * @param size number of index hits
   * @param pointer pointer to id list
   */
  private void assertCacheEntry(final byte[] key, final int size,
      final long pointer) {
    final CacheEntry entry = cache.get(key);
    assertEquals(entry.size, size);
    assertEquals(entry.pointer, pointer);
  }
}
