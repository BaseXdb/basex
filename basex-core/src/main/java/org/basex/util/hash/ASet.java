package org.basex.util.hash;

import java.util.*;
import java.util.function.*;

import org.basex.util.*;

/**
 * This is the basic structure of an efficient and memory-saving hash set.
 * The first entry of the token set (offset 0) will always be kept empty.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class ASet {
  /** Initial default size for new sets. */
  public static final int INITIAL_CAPACITY = 6;
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
   * Constructor with initial capacity.
   * @param capacity array capacity (will be resized to a power of two)
   */
  protected ASet(final long capacity) {
    // do not exceed maximum capacity, add 2 more slots (empty first slot, trailing placeholder)
    final int s = Array.checkCapacity(Long.highestOneBit(capacity + 1) << 1);
    buckets = new int[s];
    next = new int[s];
  }

  /**
   * Resets the data structure.
   */
  protected void clear() {
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
   * Checks the capacity of the hash table and resizes it if necessary.
   * @return {@code true} if the hash table was resized
   */
  protected final boolean checkCapacity() {
    return checkCapacity((index, bucket) -> { });
  }

  /**
   * Checks the capacity of the hash table and resizes it if necessary.
   * @param relocateAction action to be executed while relocating index to new bucket
   * @return {@code true} if the hash table was resized
   */
  protected final boolean checkCapacity(final BiConsumer<Integer, Integer> relocateAction) {
    if(size < capacity()) return false;

    final int newSize = size << 1;
    final int[] bckts = new int[newSize];

    for(final int bucket : buckets) {
      for(int i = bucket; i != 0;) {
        final int b = hashCode(i) & newSize - 1, nx = next[i];
        relocateAction.accept(i, b);
        next[i] = bckts[b];
        bckts[b] = i;
        i = nx;
      }
    }
    buckets = bckts;
    next = Arrays.copyOf(next, newSize);
    rehash(newSize);
    return true;
  }

  /**
   * Returns the current array capacity.
   * @return array capacity
   */
  protected final int capacity() {
    return next.length;
  }

  /**
   * Returns a hash code for the entry at the specified index.
   * @param index index of the entry
   * @return hash value
   */
  protected abstract int hashCode(int index);

  /**
   * Rehashes all entries.
   * @param newSize new hash size
   */
  protected abstract void rehash(int newSize);

  /**
   * Returns a string representation of the set or map.
   * @param keys hash keys
   * @return string
   */
  public String toString(final Object[] keys) {
    return toString(keys, null);
  }

  /**
   * Returns a string representation of the set or map.
   * @param keys hash keys
   * @param values hash values or {@code null}
   * @return string
   */
  public String toString(final Object[] keys, final Object[] values) {
    final TokenBuilder tb = new TokenBuilder().add(Util.className(this)).add('[');
    boolean more = false;
    for(int i = 1; i < size; i++) {
      final Object key = keys[i];
      if(key == null) continue;
      if(more) tb.add(',');
      tb.add(key);
      if(values != null) tb.add('=').add(values[i]);
      more = true;
    }
    return tb.add(']').toString();
  }
}
