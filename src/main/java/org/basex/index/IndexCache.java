package org.basex.index;

import static org.basex.util.Token.*;

import java.lang.ref.*;

/**
 * This class caches sizes and pointers from index results.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public final class IndexCache {
  /** Default initial capacity: MUST be a power of two. */
  private static final int DEFAULT_INITIAL_CAPACITY = 16;
  /** Default load factor. */
  private static final float DEFAULT_LOAD_FACTOR = 0.75f;

  /** Hash table buckets. */
  private BucketEntry[] bucket = new BucketEntry[DEFAULT_INITIAL_CAPACITY];
  /** Queue used to collect unused keys. */
  private final ReferenceQueue<CacheEntry> queue = new ReferenceQueue<CacheEntry>();

  /** Load factor. */
  private final float loadFactor = DEFAULT_LOAD_FACTOR;
  /** Next size value at which to resize. */
  private int threshold = (int) (DEFAULT_INITIAL_CAPACITY * loadFactor);
  /** Number of entries in the cache. */
  private int size;

  /**
   * Get cached entry for the specified key.
   * @param key key
   * @return cached entry or {@code null} if the entry is stale
   */
  public CacheEntry get(final byte[] key) {
    purge();

    final int hash = hash(key);
    final int i = indexFor(hash, bucket.length);

    BucketEntry e = bucket[i];
    BucketEntry prev = e;
    while(e != null) {
      final BucketEntry next = e.next;
      final CacheEntry entry = e.get();
      if(entry == null) {
        delete(i, e, prev, next);
      } else if(e.hash == hash && (entry.key == key || eq(entry.key, key))) {
        return entry;
      }

      prev = e;
      e = next;
    }

    return null;
  }

  /**
   * Add a new cache entry. If an entry with the specified key already exists,
   * it will be updated.
   * @param key key
   * @param s number of index hits
   * @param p pointer to id list
   */
  public void add(final byte[] key, final int s, final long p) {
    purge();

    final int hash = hash(key);
    final int i = indexFor(hash, bucket.length);

    BucketEntry e = bucket[i];
    BucketEntry prev = e;
    while(e != null) {
      final BucketEntry next = e.next;
      final CacheEntry entry = e.get();
      if(entry == null) {
        delete(i, e, prev, next);
      } else if(e.hash == hash && (entry.key == key || eq(entry.key, key))) {
        entry.size = s;
        entry.pointer = p;
        return;
      }

      prev = e;
      e = next;
    }

    e = bucket[i];
    bucket[i] = new BucketEntry(hash, e, new CacheEntry(key, s, p), queue);
    if(++size >= threshold) resize(2 * bucket.length);
  }

  /**
   * Delete a cached entry.
   * @param key key
   */
  public void delete(final byte[] key) {
    purge();

    final int hash = hash(key);
    final int i = indexFor(hash, bucket.length);

    BucketEntry e = bucket[i];
    BucketEntry prev = e;
    while(e != null) {
      final BucketEntry next = e.next;
      final CacheEntry entry = e.get();
      if(entry == null) {
        delete(i, e, prev, next);
      } else if(e.hash == hash && (entry.key == key || eq(entry.key, key))) {
        delete(i, e, prev, next);
        return;
      }

      prev = e;
      e = next;
    }
  }

  /**
   * Delete a cached entry from the bucket with the specified index.
   * @param i bucket index
   * @param e cached entry to delete
   * @param p previous cache entry
   * @param n next cache entry
   */
  private void delete(final int i, final BucketEntry e,
      final BucketEntry p, final BucketEntry n) {
    if(p == e) bucket[i] = n;
    else p.next = n;
    e.next = null;
    --size;
  }

  /**
   * Purge stale entries from the cache.
   * TODO: add a minimal load, after which the bucket array should be shrunk
   */
  private void purge() {
    for(Object x; (x = queue.poll()) != null;) {
      // {@link java.lang.ref.ReferenceQueue} is not thread-safe.
      synchronized(queue) {
        BucketEntry e = (BucketEntry) x;
        int i = indexFor(e.hash, bucket.length);

        BucketEntry prev = bucket[i];
        BucketEntry p = prev;
        while(p != null) {
          BucketEntry next = p.next;
          if(p == e) {
            if(prev == e) bucket[i] = next;
            else prev.next = next;
            e.next = null;
            --size;
            break;
          }
          prev = p;
          p = next;
        }
      }
    }
  }

  /**
   * Rehashes the contents of the cache into a new array with larger capacity.
   * @param newCapacity new capacity; MUST be a power of two
   */
  private void resize(final int newCapacity) {
    purge();

    final BucketEntry[] newBucket = new BucketEntry[newCapacity];
    transfer(bucket, newBucket);
    bucket = newBucket;

    threshold = (int) (newCapacity * loadFactor);
  }

  /**
   * Returns bucket index for a hash code.
   * @param h hash code
   * @param n number of available buckets
   * @return index of a bucket
   */
  private static int indexFor(final int h, final int n) {
    return h & (n - 1);
  }

  /**
   * Transfer all entries from source buckets to destination buckets.
   * @param src source buckets
   * @param dest destination buckets
   */
  private static void transfer(final BucketEntry[] src,
      final BucketEntry[] dest) {
    for(int j = 0; j < src.length; ++j) {
      BucketEntry e = src[j];
      src[j] = null;
      while(e != null) {
        BucketEntry next = e.next;
        int i = indexFor(e.hash, dest.length);
        e.next = dest[i];
        dest[i] = e;
        e = next;
      }
    }
  }

  /** Cache entry data. */
  public static class CacheEntry {
    /** Entry key. */
    public byte[] key;
    /** Number of index hits for the key. */
    public int size;
    /** Pointer to the id list for the key. */
    public long pointer;

    /**
     * Constructor.
     * @param k key
     * @param s number of hits
     * @param p pointer to the id list
     */
    public CacheEntry(final byte[] k, final int s, final long p) {
      key = k;
      size = s;
      pointer = p;
    }
  }

  /**
   * Cache bucket entry. Used to implement a linked list of cache entries for
   * each bucket. It also stores the hash of the current entry for better
   * performance.
   */
  private static class BucketEntry extends SoftReference<CacheEntry> {
    /** Hash code of the stored cache entry key. */
    final int hash;
    /** Next bucket entry or {@code null} if the last one for this bucket. */
    BucketEntry next;

    /**
     * Constructor.
     * @param h hash code of the cache entry key
     * @param n next bucket entry or {@code null} if the last one
     * @param v stored cache entry
     * @param rq reference queue
     */
    public BucketEntry(final int h, final BucketEntry n,
        final CacheEntry v, final ReferenceQueue<CacheEntry> rq) {
      super(v, rq);
      hash = h;
      next = n;
    }
  }
}
