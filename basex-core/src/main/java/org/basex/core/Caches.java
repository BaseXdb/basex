package org.basex.core;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.list.*;

/**
 * This class provides access to main-memory caches.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Caches {
  /** Caches. */
  private final HashMap<String, Cache> caches = new HashMap<>();
  /** Configurations of initialized caches. */
  private final HashMap<String, Config> configs = new HashMap<>();
  /** Database context. */
  private final Context context;

  /**
   * Constructor.
   * @param context database context
   */
  public Caches(final Context context) {
    this.context = context;
  }

  /**
   * Initializes a cache.
   * @param max maximum number of entries
   * @param ttl lifetime of entries in seconds (0 for unlimited lifetime)
   * @param name name of cache
   */
  public synchronized void init(final int max, final long ttl, final String name) {
    final Config config = new Config(max, ttl);
    configs.put(name, config);
    final Cache cache = caches.get(name);
    if(cache != null) {
      cache.config = config;
      cache.trim();
    }
  }

  /**
   * Returns a value.
   * @param key key
   * @param name name of cache
   * @return value or {@code null}
   */
  public synchronized Value get(final String key, final String name) {
    final Cache cache = caches.get(name);
    if(cache == null) return null;
    cache.cleanup();
    final CacheEntry entry = cache.get(key);
    if(entry == null) {
      cache.misses++;
      return null;
    }
    cache.hits++;
    return entry.value;
  }

  /**
   * Returns the keys of a cache.
   * @param name name of cache
   * @return keys
   */
  public synchronized TokenList keys(final String name) {
    final Cache cache = caches.get(name);
    if(cache != null) cache.cleanup();
    final Set<String> keys = cache != null ? cache.keySet() : Set.of();
    final TokenList list = new TokenList(keys.size());
    for(final String key : keys) list.add(key);
    return list;
  }

  /**
   * Returns statistics of a cache.
   * @param name name of cache
   * @return number of entries, hits, misses, evicted and expired entries
   * @throws QueryException query exception
   */
  public synchronized XQMap info(final String name) throws QueryException {
    final Cache cache = caches.get(name);
    if(cache != null) cache.cleanup();
    return new MapBuilder().
      put("entries", Itr.get(cache != null ? cache.size() : 0)).
      put("hits", Itr.get(cache != null ? cache.hits : 0)).
      put("misses", Itr.get(cache != null ? cache.misses : 0)).
      put("evictions", Itr.get(cache != null ? cache.evictions : 0)).
      put("expirations", Itr.get(cache != null ? cache.expirations : 0)).map();
  }

  /**
   * Stores a value. An empty value removes the addressed entry.
   * @param key key
   * @param value value
   * @param name name of cache
   */
  public synchronized void put(final String key, final Value value, final String name) {
    if(value.isEmpty()) {
      remove(key, name);
    } else {
      final Cache cache = caches.computeIfAbsent(name, this::create);
      cache.cleanup();
      cache.add(key, value);
    }
  }

  /**
   * Removes an entry.
   * @param key key
   * @param name name of cache
   */
  public synchronized void remove(final String key, final String name) {
    final Cache cache = caches.get(name);
    if(cache != null) cache.remove(key);
  }

  /**
   * Returns the number of entries.
   * @param name name of cache
   * @return number of entries
   */
  public synchronized int size(final String name) {
    final Cache cache = caches.get(name);
    if(cache == null) return 0;
    cache.cleanup();
    return cache.size();
  }

  /**
   * Deletes a cache. Its configuration is preserved.
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
   * Clears all caches and their configurations.
   */
  public synchronized void clear() {
    caches.clear();
    configs.clear();
  }

  /**
   * Creates a new cache.
   * @param name name of cache
   * @return cache
   */
  private Cache create(final String name) {
    Config config = configs.get(name);
    if(config == null) config = new Config(context.soptions.get(StaticOptions.CACHEMAX),
      context.soptions.get(StaticOptions.CACHETTL));
    return new Cache(config);
  }

  /**
   * Cache with LRU replacement policy.
   *
   * @author BaseX Team, BSD License
   * @author Christian Gruen
   */
  private static final class Cache extends LinkedHashMap<String, CacheEntry> {
    /** Configuration. */
    private Config config;
    /** Earliest expiration time. */
    private long minExpires = Long.MAX_VALUE;
    /** Number of cache hits. */
    private long hits;
    /** Number of cache misses. */
    private long misses;
    /** Number of evicted entries. */
    private long evictions;
    /** Number of expired entries. */
    private long expirations;

    /**
     * Constructor.
     * @param config configuration
     */
    private Cache(final Config config) {
      super(8, 0.75f, true);
      this.config = config;
    }

    /**
     * Adds an entry.
     * @param key key
     * @param value value
     */
    private void add(final String key, final Value value) {
      final long ttl = config.ttl;
      final long expires = ttl == 0 ? Long.MAX_VALUE :
        System.currentTimeMillis() + ttl * 1000;
      if(expires < minExpires) minExpires = expires;
      put(key, new CacheEntry(value, expires));
    }

    /**
     * Discards all expired entries.
     */
    private void cleanup() {
      final long now = System.currentTimeMillis();
      if(now < minExpires) return;
      long min = Long.MAX_VALUE;
      final Iterator<CacheEntry> iter = values().iterator();
      while(iter.hasNext()) {
        final long expires = iter.next().expires;
        if(expires <= now) {
          iter.remove();
          expirations++;
        } else if(expires < min) {
          min = expires;
        }
      }
      minExpires = min;
    }

    /**
     * Discards the least recently used entries that exceed the maximum number of entries.
     */
    private void trim() {
      final Iterator<String> iter = keySet().iterator();
      while(size() > config.max && iter.hasNext()) {
        iter.next();
        iter.remove();
        evictions++;
      }
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<String, CacheEntry> eldest) {
      final boolean evict = size() > config.max;
      if(evict) evictions++;
      return evict;
    }
  }

  /**
   * Cache configuration.
   *
   * @author BaseX Team, BSD License
   * @author Christian Gruen
   */
  private static final class Config {
    /** Maximum number of entries. */
    private final int max;
    /** Lifetime of entries in seconds (0 for unlimited lifetime). */
    private final long ttl;

    /**
     * Constructor.
     * @param max maximum number of entries
     * @param ttl lifetime of entries in seconds
     */
    private Config(final int max, final long ttl) {
      this.max = max;
      this.ttl = ttl;
    }
  }

  /**
   * Cache entry.
   *
   * @author BaseX Team, BSD License
   * @author Christian Gruen
   */
  private static final class CacheEntry {
    /** Value. */
    private final Value value;
    /** Expiration time in milliseconds. */
    private final long expires;

    /**
     * Constructor.
     * @param value value
     * @param expires expiration time in milliseconds
     */
    private CacheEntry(final Value value, final long expires) {
      this.value = value;
      this.expires = expires;
    }
  }
}
