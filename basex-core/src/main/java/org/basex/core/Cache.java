package org.basex.core;

import java.util.*;

import org.basex.query.value.*;
import org.basex.util.list.*;

/**
 * This class provides access to main-memory caches.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Cache {
  /** Caches. */
  private final HashMap<String, LinkedHashMap<String, Value>> caches = new HashMap<>();
  /** Database context. */
  private final Context context;

  /**
   * Constructor.
   * @param context database context
   */
  public Cache(final Context context) {
    this.context = context;
  }

  /**
   * Returns a value.
   * @param key key
   * @param name name of cache
   * @return value or {@code null}
   */
  public synchronized Value get(final String key, final String name) {
    return caches.containsKey(name) ? caches.get(name).get(key) : null;
  }

  /**
   * Stores a value.
   * @param key key
   * @param value value
   * @param name name of cache
   */
  public synchronized void put(final String key, final Value value, final String name) {
    caches.computeIfAbsent(name, n -> create()).put(key, value);
  }

  /**
   * Returns the number of entries.
   * @param name name of cache
   * @return number of entries
   */
  public synchronized int size(final String name) {
    return caches.containsKey(name) ? caches.get(name).size() : 0;
  }

  /**
   * Deletes a cache.
   * @param name name of cache
   */
  public synchronized void delete(final String name) {
    caches.remove(name);
  }

  /**
   * Returns the names of all caches.
   * @return names
   */
  public synchronized TokenList list() {
    final TokenList list = new TokenList(caches.size());
    for(final String name : caches.keySet()) {
      if(!name.isEmpty()) list.add(name);
    }
    return list;
  }

  /**
   * Clears all caches.
   */
  public synchronized void clear() {
    caches.clear();
  }

  /**
   * Creates a new cache.
   * @return cache
   */
  private LinkedHashMap<String, Value> create() {
    final int max = context.soptions.get(StaticOptions.CACHEMAX);
    return new LinkedHashMap<>(8, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(final Map.Entry<String, Value> eldest) {
        return size() > max;
      }
    };
  }
}
