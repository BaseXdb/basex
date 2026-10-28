package org.basex.index;

import java.lang.ref.*;
import java.util.concurrent.locks.*;

import org.basex.util.*;

/**
 * This class caches sizes and offsets from index results.
 *
 * @author BaseX Team, BSD License
 * @author Dimitar Popov
 */
public final class IndexCache {
  /** Queue used to collect unused keys. */
  private final ReferenceQueue<IndexEntry> queue = new ReferenceQueue<>();
  /** Read-write lock. */
  private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
  /** Hash table buckets. */
  private BucketEntry[] buckets = new BucketEntry[Array.INITIAL_CAPACITY];
  /** Number of entries in the cache. */
  private int size;

  /**
   * Gets cached entry for the specified key.
   * @param key key
   * @return cached entry or {@code null} if the entry is stale
   */
  public IndexEntry get(final byte[] key) {
    final int hash = Token.hashCode(key);
    rwl.readLock().lock();

    try {
      return find(hash, key);
    } finally {
      rwl.readLock().unlock();
    }
  }

  /**
   * Adds a new cache entry. If an entry with the specified key already exists,
   * it will be updated.
   * @param key key
   * @param count number of index hits
   * @param offset offset to ID list
   * @return cache entry
   */
  public IndexEntry add(final byte[] key, final int count, final long offset) {
    final int hash = Token.hashCode(key);
    rwl.writeLock().lock();

    try {
      purge();
      IndexEntry entry = find(hash, key);
      if(entry != null) {
        update(entry, count, offset);
      } else {
        entry = new IndexEntry(key, count, offset);
        add(indexFor(hash, buckets.length), hash, entry);
      }
      return entry;
    } finally {
      rwl.writeLock().unlock();
    }
  }

  /**
   * Returns the live cache entry for the specified key.
   * @param hash hash of the key
   * @param key key
   * @return cache entry or {@code null}
   */
  private IndexEntry find(final int hash, final byte[] key) {
    for(BucketEntry e = buckets[indexFor(hash, buckets.length)]; e != null; e = e.next) {
      final IndexEntry entry = e.get();
      if(entry != null && e.hash == hash && Token.eq(entry.key, key)) return entry;
    }
    return null;
  }

  /**
   * Purges stale entries from the cache.
   */
  private void purge() {
    for(Object x; (x = queue.poll()) != null;) {
      final BucketEntry e = (BucketEntry) x;
      final int i = indexFor(e.hash, buckets.length);
      for(BucketEntry prev = null, p = buckets[i]; p != null; prev = p, p = p.next) {
        if(p == e) {
          if(prev == null) buckets[i] = e.next;
          else prev.next = e.next;
          e.next = null;
          --size;
          break;
        }
      }
    }
  }

  /**
   * Add a new index entry to the bucket with the specified index.
   * @param i bucket index
   * @param hash hash of the new index key
   * @param entry index entry
   */
  private void add(final int i, final int hash, final IndexEntry entry) {
    buckets[i] = new BucketEntry(hash, buckets[i], entry, queue);
    if(++size == buckets.length) rehash();
  }

  /**
   * Update an existing index entry.
   * @param entry index entry to update
   * @param size new size
   * @param offset new offset
   */
  private static void update(final IndexEntry entry, final int size, final long offset) {
    entry.size = size;
    entry.offset = offset;
  }

  /**
   * Resizes the hash table.
   */
  private void rehash() {
    purge();

    final int l = buckets.length;
    if(size < l) return;
    final BucketEntry[] tmp = new BucketEntry[l << 1];
    for(int i = 0; i < l; i++) {
      BucketEntry e = buckets[i];
      buckets[i] = null;
      while(e != null) {
        final BucketEntry next = e.next;
        final int p = indexFor(e.hash, tmp.length);
        e.next = tmp[p];
        tmp[p] = e;
        e = next;
      }
    }
    buckets = tmp;
  }

  /**
   * Returns buckets index for a hash code.
   * @param h hash code
   * @param n number of available buckets
   * @return index of a buckets
   */
  private static int indexFor(final int h, final int n) {
    return h & n - 1;
  }

  /**
   * Cache buckets entry. Used to implement a linked list of cache entries for each bucket.
   * It also stores the hash of the current entry for better performance.
   */
  private static final class BucketEntry extends SoftReference<IndexEntry> {
    /** Hash code of the stored cache entry key. */
    final int hash;
    /** Next bucket entry or {@code null} if the last one for this bucket. */
    BucketEntry next;

    /**
     * Constructor.
     * @param h hash code of the cache entry key
     * @param n next buckets entry or {@code null} if the last one
     * @param v stored cache entry
     * @param rq reference queue
     */
    BucketEntry(final int h, final BucketEntry n, final IndexEntry v,
        final ReferenceQueue<IndexEntry> rq) {
      super(v, rq);
      hash = h;
      next = n;
    }
  }
}
