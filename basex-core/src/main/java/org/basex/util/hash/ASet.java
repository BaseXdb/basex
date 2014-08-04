package org.basex.util.hash;

import java.util.*;

/**
 * This is the basic structure of an efficient and memory-saving hash set.
 * The first entry of the token set (offset 0) will always be kept empty.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class ASet {
  /** Hash table buckets. */
  protected int[] buckets;
  /** Pointers to the next entry. */
  protected int[] next;
  /** Hash entries. The actual number of entries is {@code size - 1}. */
  protected int size = 1;

  /**
   * Empty constructor.
   */
  protected ASet() { }

  /**
   * Initializes the data structure with an initial array size.
   * @param capacity initial array capacity (will be resized to a power of two)
   */
  protected ASet(final int capacity) {
    int c = 1;
    while(c < capacity) c <<= 1;
    buckets = new int[c];
    next = new int[c];
  }

  /**
   * Resets the data structure. Must be called when data structure is initialized.
   */
  void clear() {
    Arrays.fill(buckets, 0);
    size = 1;
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
   * Tests is the set is empty.
   * @return result of check
   */
  public final boolean isEmpty() {
    return size == 1;
  }

  /**
   * Resizes the hash table.
   */
  protected final void checkSize() {
    if(size < next.length) return;

    final int s = size << 1;
    final int[] tmp = new int[s];

    for(final int b : buckets) {
      int id = b;
      while(id != 0) {
        final int p = hash(id) & s - 1;
        final int nx = next[id];
        next[id] = tmp[p];
        tmp[p] = id;
        id = nx;
      }
    }
    buckets = tmp;
    next = Arrays.copyOf(next, s);
    rehash(s);
  }

  /**
   * Returns the hash value of the element with the specified id.
   * @param id id of the element
   * @return hash value
   */
  protected abstract int hash(final int id);

  /**
   * Rehashes all entries.
   * @param newSize new hash size
   */
  protected abstract void rehash(final int newSize);
}
