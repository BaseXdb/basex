package org.basex.index;

import static org.basex.util.Token.*;

import java.lang.ref.*;
import java.util.concurrent.locks.*;

import org.basex.util.*;

/**
 * This class caches sizes and offsets from index results.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Dimitar Popov
 */
public final class IndexCache {
  /** Queue used to collect unused keys. */
  private final ReferenceQueue<IndexEntry> queue = new ReferenceQueue<>();
  /** Read-write lock. */
  private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
  /** Hash table buckets. */
  private BucketEntry[] buckets = new BucketEntry[Array.CAPACITY];
  /** Number of entries in the cache. */
  private int size;

  /**
   * Gets cached entry for the specified key.
   * @param key key
   * @return cached entry or {@code null} if the entry is stale
   */
  public IndexEntry get(final byte[] key) {
    final int hash = hash(key);
    rwl.readLock().lock();

    try {
      final int i = indexFor(hash, buckets.length);
      BucketEntry e = buckets[i];
      while(e != null) {
        final IndexEntry entry = e.get();
        if(entry != null && e.hash == hash && eq(entry.key, key)) return entry;
        e = e.next;
      }
    } finally {
      rwl.readLock().unlock();
    }

    return null;
  }

  /**
   * Adds a new cache entry. If an entry with the specified key already exists,
   * it will be updated.
   * @param key key
   * @param sz number of index hits
   * @param off offset to id list
   * @return cache entry
   */
  public IndexEntry add(final byte[] key, final int sz, final long off) {
    final int hash = hash(key);
    rwl.writeLock().lock();

    try {
      purge();
      final int i = indexFor(hash, buckets.length);
      BucketEntry current = buckets[i], prev = current;
      while(current != null) {
        final BucketEntry next = current.next;
        final IndexEntry entry = current.get();
        if(entry == null) {
          delete(i, current, prev, next);
        } else if(current.hash == hash && eq(entry.key, key)) {
          update(entry, sz, off);
          return entry;
        }
        prev = current;
        current = next;
      }

      final IndexEntry entry = new IndexEntry(key, sz, off);
      add(i, hash, entry);
      return entry;
    } finally {
      rwl.writeLock().unlock();
    }
  }

  /**
   * Deletes a cached entry.
   * @param key key
   */
  public void delete(final byte[] key) {
    final int hash = hash(key);
    rwl.writeLock().lock();

    try {
      purge();
      final int i = indexFor(hash, buckets.length);
      BucketEntry e = buckets[i], prev = e;
      while(e != null) {
        final BucketEntry next = e.next;
        final IndexEntry entry = e.get();
        if(entry == null) {
          delete(i, e, prev, next);
        } else if(e.hash == hash && eq(entry.key, key)) {
          delete(i, e, prev, next);
          break;
        }
        prev = e;
        e = next;
      }
    } finally {
      rwl.writeLock().unlock();
    }
  }

  /**
   * Purges stale entries from the cache.
   */
  private void purge() {
    for(Object x; (x = queue.poll()) != null;) {
      final BucketEntry e = (BucketEntry) x;
      final int i = indexFor(e.hash, buckets.length);
      BucketEntry prev = buckets[i], p = prev;
      while(p != null) {
        final BucketEntry next = p.next;
        if(p == e) {
          delete(i, e, prev, next);
          break;
        }
        prev = p;
        p = next;
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
   * @param sz new size
   * @param off new offset
   */
  private static void update(final IndexEntry entry, final int sz, final long off) {
    entry.size = sz;
    entry.offset = off;
  }

  /**
   * Deletes a cached entry from the buckets with the specified index.
   * @param i buckets index
   * @param e cached entry to delete
   * @param p previous cache entry
   * @param n next cache entry
   */
  private void delete(final int i, final BucketEntry e, final BucketEntry p, final BucketEntry n) {
    if(p == e) buckets[i] = n;
    else p.next = n;
    e.next = null;
    --size;
  }

  /**
   * Resizes the hash table.
   */
  private void rehash() {
    purge();

    final int s = size << 1;
    final BucketEntry[] tmp = new BucketEntry[s];

    final int l = buckets.length;
    for(int i = 0; i < l; ++i) {
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
   * Cache buckets entry. Used to implement a linked list of cache entries for
   * each buckets. It also stores the hash of the current entry for better
   * performance.
   */
  private static class BucketEntry extends SoftReference<IndexEntry> {
    /** Hash code of the stored cache entry key. */
    final int hash;
    /** Next buckets entry or {@code null} if the last one for this buckets. */
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
