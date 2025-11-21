package org.basex.core;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class provides a main-memory key/value store.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Cache {
  /** Cache. */
  private final TokenObjectMap<Entry> cache = new TokenObjectMap<>();
  /** Timer for cleaning up cache. */
  private Timer timer;

  /**
   * Returns all keys.
   * @return keys
   */
  public synchronized Value keys() {
    final TokenList list = new TokenList();
    for(final byte[] key : cache) list.add(key);
    return StrSeq.get(list);
  }

  /**
   * Returns a value.
   * @param key key
   * @return value or empty sequence
   */
  public synchronized Value get(final byte[] key) {
    final Entry entry = cache.get(key);
    if(entry == null) return Empty.VALUE;

    entry.current = System.currentTimeMillis();
    return entry.value;
  }

  /**
   * Stores a value.
   * @param key key
   * @param value value
   * @param expires expiration time in milliseconds
   */
  public synchronized void put(final byte[] key, final Value value, final long expires) {
    if(value.isEmpty() || expires <= 0) {
      remove(key);
    } else {
      cache.put(key, new Entry(value, System.currentTimeMillis(), expires));
      cleanup();
    }
  }

  /**
   * Removes a value.
   * @param key key
   */
  public synchronized void remove(final byte[] key) {
    cache.remove(key);
  }

  /**
   * Clears the map.
   */
  public synchronized void clear() {
    cache.clear();
  }

  // PRIVATE FUNCTIONS ============================================================================

  /**
   * Cleans up the cache.
   */
  private synchronized void cleanup() {
    if(timer != null) return;

    final TimerTask task = new TimerTask() {
      @Override
      public void run() {
        synchronized(Cache.this) {
          final long current = System.currentTimeMillis();
          for(final byte[] key : cache) {
            final Entry entry = cache.get(key);
            if(current > entry.current + entry.expires) cache.remove(key);
          }
          if(!cache.iterator().hasNext()) {
            cache.clear();
            timer.cancel();
            timer = null;
          }
        }
      }
    };
    timer = new Timer(true);
    timer.schedule(task, 0, 1000);
  }

  /**
   * Cache entry.
   */
  private static final class Entry {
    /** Value. */
    Value value;
    /** Current time. */
    long current;
    /** Expiration time in milliseconds. */
    long expires;

    /**
     * Constructor.
     * @param value value
     * @param current current time
     * @param expires duration to keep
     */
    private Entry(final Value value, final long current, final long expires) {
      this.value = value;
      this.current = current;
      this.expires = expires;
    }
  }
}
