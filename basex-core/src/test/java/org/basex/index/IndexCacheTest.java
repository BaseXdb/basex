package org.basex.index;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

/**
 * Tests for {@link IndexCache}.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 */
public final class IndexCacheTest {
  /** Test instance. */
  private IndexCache cache;

  /** Set up method. */
  @Before
  public void setUp() {
    cache = new IndexCache();
  }

  /** Test for method {@link IndexCache#get(byte[])}. */
  @Test
  public void testGetNotExisting() {
    for(int i = 0; i < 4000; ++i) {
      final byte[] key = token("keyAdd" + i);
      final long pointer = i + 5000L;

      cache.add(key, i, pointer);
      assertCacheEntry(key, i, pointer);
    }
    assertNull(cache.get(token("keyAdd" + 4000)));
  }

  /** Test for method {@link IndexCache#add(byte[], int, long)}. */
  @Test
  public void testAdd() {
    for(int i = 0; i < 4000; ++i) {
      final byte[] key = token("keyAdd" + i);
      final long pointer = i + 5000L;

      cache.add(key, i, pointer);
      assertCacheEntry(key, i, pointer);
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
  private void assertCacheEntry(final byte[] key, final int size, final long pointer) {
    final IndexEntry entry = cache.get(key);
    assertEquals(entry.size, size);
    assertEquals(entry.pointer, pointer);
  }
}
