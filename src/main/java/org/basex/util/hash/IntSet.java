package org.basex.util.hash;

import java.util.*;

/**
 * This is an efficient hash set, storing keys in a simple integer array.
 * The {@link IntMap} class extends it to a hash map.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class IntSet {
  /** Initial hash capacity. */
  static final int CAP = 1 << 3;
  /** Hash entries. Actual hash size is {@code size - 1}. */
  int size = 1;
  /** Hash keys. */
  int[] keys;

  /** Pointers to the next token. */
  private int[] next;
  /** Hash table buckets. */
  private int[] bucket;

  /**
   * Constructor.
   */
  public IntSet() {
    keys = new int[CAP];
    next = new int[CAP];
    bucket = new int[CAP];
  }

  /**
   * Indexes the specified key and returns the offset of the added key.
   * If the key already exists, a negative offset is returned.
   * @param key key
   * @return offset of added key, negative offset otherwise
   */
  public final int add(final int key) {
    if(size == next.length) rehash();
    final int p = key & bucket.length - 1;
    for(int id = bucket[p]; id != 0; id = next[id]) {
      if(key == keys[id]) return -id;
    }
    next[size] = bucket[p];
    keys[size] = key;
    bucket[p] = size;
    return size++;
  }

  /**
   * Returns true if the given key is contained in this set.
   * @param key key to look for
   * @return true if key contained, else false
   */
  public boolean contains(final int key) {
    return id(key) != 0;
  }

  /**
   * Returns the id of the specified key or -1 if key was not found.
   * @param key key to be found
   * @return id or 0 if nothing was found
   */
  public final int id(final int key) {
    final int p = key & bucket.length - 1;
    for(int id = bucket[p]; id != 0; id = next[id]) {
      if(key == keys[id]) return id;
    }
    return 0;
  }

  /**
   * Returns the specified key.
   * @param i key index
   * @return key
   */
  public final int key(final int i) {
    return keys[i];
  }

  /**
   * Returns number of entries.
   * @return number of entries
   */
  public final int size() {
    return size - 1;
  }

  /**
   * Resizes the hash table.
   */
  void rehash() {
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
   * Deletes the specified key.
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
}
