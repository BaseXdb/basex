package org.basex.core;

import java.util.*;
import java.util.concurrent.*;

import org.basex.query.value.*;
import org.basex.util.hash.*;

/**
 * This class provides a main-memory cache.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Cache {
  /** Cache entry. */
  private static final class Entry {
    /** Value. */                Value value;
    /** Current time, in ms. */  long current;
    /** Lifetime time, in ms. */ long lifetime;
  }
  /** Cache entries. */
  private final TokenObjectMap<Entry> cache = new TokenObjectMap<>();
  /** Executor for cleaning up the cache. */
  private final ScheduledThreadPoolExecutor cleanup = new ScheduledThreadPoolExecutor(1);
  /** Currently registered cleanup task. */
  private ScheduledFuture<?> task;

  /**
   * Returns a value.
   * @param key key
   * @return value or {@code null}
   */
  public synchronized Value get(final byte[] key) {
    final Entry entry = cache.get(key);
    if(entry == null) return null;

    entry.current = System.currentTimeMillis();
    return entry.value;
  }

  /**
   * Stores a value.
   * @param key key
   * @param value value
   * @param lifetime lifetime, in milliseconds
   */
  public synchronized void put(final byte[] key, final Value value, final long lifetime) {
    if(lifetime <= 0) {
      cache.remove(key);
    } else {
      final Entry entry = new Entry();
      entry.value = value;
      entry.current = System.currentTimeMillis();
      entry.lifetime = lifetime;
      cache.put(key, entry);

      // schedule cleanup if no task exists, or if its lifetime is longer than new lifetime
      if(task == null || task.getDelay(TimeUnit.MILLISECONDS) > lifetime) {
        if(task != null) task.cancel(false);
        schedule(lifetime);
      }
    }
  }

  /**
   * Returns the number of entries in the cache.
   * @return number of entries
   */
  public synchronized int size() {
    int size = 0;
    for(final Iterator<byte[]> iter = cache.iterator(); iter.hasNext(); iter.next()) size++;
    return size;
  }

  /**
   * Clears the map.
   */
  public synchronized void clear() {
    cache.clear();
  }

  // PRIVATE FUNCTIONS ============================================================================

  /**
   * Schedules a new cleanup task.
   * @param delay delay in milliseconds
   */
  private synchronized void schedule(final long delay) {
    // minimum delay: 1 second
    task = cleanup.schedule(this::cleanup, Math.max(1000, delay), TimeUnit.MILLISECONDS);
  }

  /**
   * Cleans up the cache.
   */
  private synchronized void cleanup() {
    final long current = System.currentTimeMillis();
    long delay = Long.MAX_VALUE;
    for(final byte[] key : cache) {
      final Entry entry = cache.get(key);
      final long lifetime = entry.current + entry.lifetime - current;
      if(lifetime < 0) {
        cache.remove(key);
      } else if(lifetime < delay) {
        delay = lifetime;
      }
    }
    if(delay < Long.MAX_VALUE) {
      // register cleanup task for entry that becomes obsolete next
      schedule(delay);
    } else {
      // no entries left: reset cache
      cache.clear();
      task = null;
    }
  }
}
