package org.basex.util.hash;

import java.util.*;

/**
 * This is an efficient and memory-saving hash set for storing primitive integers.
 * It is derived from the {@link TokenSet} class.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class IntSet extends ASet {
  /** Hashed keys. */
  int[] keys;

  /**
   * Default constructor.
   */
  public IntSet() {
    this(INITIAL_CAPACITY);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity (will be resized to a power of two)
   */
  public IntSet(final long capacity) {
    super(capacity);
    keys = new int[capacity()];
  }

  /**
   * Stores the specified key if it has not been stored before.
   * @param key key to be added
   * @return {@code true} if the key did not exist yet and was stored
   */
  public final boolean add(final int key) {
    return store(key) > 0;
  }

  /**
   * Stores the specified key and returns its index.
   * @param key key to be added
   * @return index of stored key (larger than {@code 0})
   */
  public final int put(final int key) {
    final int i = store(key);
    return Math.abs(i);
  }

  /**
   * Checks if the set contains the specified key.
   * @param key key to be looked up
   * @return result of check
   */
  public final boolean contains(final int key) {
    return index(key) > 0;
  }

  /**
   * Returns the index of the specified key.
   * @param key key to be looked up
   * @return index, or {@code 0} if key does not exist
   */
  public final int index(final int key) {
    final int b = key & capacity() - 1;
    for(int i = buckets[b]; i != 0; i = next[i]) {
      if(key == keys[i]) return i;
    }
    return 0;
  }

  /**
   * Returns the key with the specified index.
   * @param index index of the key (starts with {@code 1})
   * @return key
   */
  public final int key(final int index) {
    return keys[index];
  }

  /**
   * Stores the specified key and returns its index,
   * or returns the negative index if the key has already been stored.
   * @param key key to be indexed
   * @return index, or negative index if the key already exists
   */
  private int store(final int key) {
    int b = key & capacity() - 1;
    for(int i = buckets[b]; i != 0; i = next[i]) {
      if(key == keys[i]) return -i;
    }
    final int s = size++;
    if(checkCapacity()) b = key & capacity() - 1;
    next[s] = buckets[b];
    keys[s] = key;
    buckets[b] = s;
    return s;
  }

  @Override
  protected final int hashCode(final int index) {
    return keys[index];
  }

  @Override
  protected void rehash(final int newSize) {
    keys = Arrays.copyOf(keys, newSize);
  }

  /**
   * Returns an array with all keys.
   * @return array
   */
  public final int[] keys() {
    return Arrays.copyOfRange(keys, 1, size);
  }

  @Override
  public String toString() {
    return toString(Arrays.stream(keys).boxed().toArray(Integer[]::new));
  }
}
