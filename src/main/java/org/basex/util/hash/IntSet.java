package org.basex.util.hash;

import java.util.*;

import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash map for storing primitive integers.
 * It is related to the {@link TokenSet} class.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class IntSet {
  /** Hash entries. Actual hash size is {@code size - 1}. */
  protected int size = 1;
  /** Hash keys. */
  protected int[] keys;

  /** Pointers to the next token. */
  private int[] next;
  /** Hash table buckets. */
  private int[] bucket;

  /**
   * Default constructor.
   */
  public IntSet() {
    keys = new int[Array.CAPACITY];
    next = new int[Array.CAPACITY];
    bucket = new int[Array.CAPACITY];
  }

  /**
   * Stores the specified key if it has not been stored before.
   * @param key key to be added
   * @return {@Code true} if the key did not exist yet and was stored
   */
  public final boolean add(final int key) {
    return index(key) > 0;
  }

  /**
   * Stores the specified key and returns its id.
   * @param key key to be added
   * @return unique id of stored key (larger than zero)
   */
  public final int put(final int key) {
    final int i = index(key);
    return Math.abs(i);
  }

  /**
   * Checks if the set contains the specified key.
   * @param key key to be looked up
   * @return result of check
   */
  public final boolean contains(final int key) {
    return id(key) > 0;
  }

  /**
   * Returns the id of the specified key, or {@code 0} if the key does not exist.
   * @param key key to be looked up
   * @return id, or {@code 0} if key does not exist
   */
  public final int id(final int key) {
    final int p = key & bucket.length - 1;
    for(int id = bucket[p]; id != 0; id = next[id]) if(key == keys[id]) return id;
    return 0;
  }

  /**
   * Returns the key with the specified id.
   * All ids starts with {@code 1} instead of {@code 0}.
   * @param id id of the key to return
   * @return key
   */
  public final int key(final int id) {
    return keys[id];
  }

  /**
   * Returns the number of entries.
   * The actual number of keys may be smaller if keys have been deleted.
   * @return number of entries
   */
  public final int size() {
    return size - 1;
  }

  /**
   * Deletes the specified key.
   * The deletion of keys will lead to empty entries. If {@link #size} is called after
   * deletions, the original number of entries will be returned.
   * @param key key
   * @return deleted key or 0
   */
  public int delete(final int key) {
    final int p = key & bucket.length - 1;
    for(int id = bucket[p]; id != 0; id = next[id]) {
      if(key == keys[id]) {
        if(bucket[p] == id) bucket[p] = next[id];
        else next[id] = next[next[id]];
        keys[id] = 0;
        return id;
      }
    }
    return 0;
  }

  /**
   * Resizes the hash table.
   */
  protected void rehash() {
    final int s = size << 1;
    final int[] tmp = new int[s];

    for(final int b : bucket) {
      int id = b;
      while(id != 0) {
        final int p = keys[id] & s - 1;
        final int nx = next[id];
        next[id] = tmp[p];
        tmp[p] = id;
        id = nx;
      }
    }
    bucket = tmp;
    next = Arrays.copyOf(next, s);
    final int[] k = new int[s];
    System.arraycopy(keys, 0, k, 0, size);
    keys = k;
  }

  /**
   * Stores the specified key and returns its id, or returns the negative id if the
   * key has already been stored.
   * @param key key to be found
   * @return id, or negative id if key has already been stored
   */
  private int index(final int key) {
    if(size == next.length) rehash();
    final int p = key & bucket.length - 1;
    for(int id = bucket[p]; id != 0; id = next[id]) if(key == keys[id]) return -id;
    next[size] = bucket[p];
    keys[size] = key;
    bucket[p] = size;
    return size++;
  }
}
