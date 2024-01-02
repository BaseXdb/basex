package org.basex.util.hash;

import java.util.*;

import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash set for storing primitive integers.
 * It is derived from the {@link TokenSet} class.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class IntSet extends ASet {
  /** Hashed keys. */
  int[] keys;

  /**
   * Default constructor.
   */
  public IntSet() {
    this(Array.INITIAL_CAPACITY);
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
    return index(key) > 0;
  }

  /**
   * Stores the specified key and returns its id.
   * @param key key to be added
   * @return unique id of stored key (larger than zero)
   */
  public final int put(final int key) {
    final int id = index(key);
    return Math.abs(id);
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
    final int b = key & capacity() - 1;
    for(int id = buckets[b]; id != 0; id = next[id]) {
      if(key == keys[id]) return id;
    }
    return 0;
  }

  /**
   * Returns the key with the specified id.
   * All ids start with {@code 1} instead of {@code 0}.
   * @param id id of the key to return
   * @return key
   */
  public final int key(final int id) {
    return keys[id];
  }

  /**
   * Stores the specified key and returns its id, or returns the negative id if the key has already
   * been stored. The public method {@link #add} can be used to check if an added value exists.
   * @param key key to be indexed
   * @return id, or negative id if key has already been stored
   */
  private int index(final int key) {
    int b = key & capacity() - 1;
    for(int id = buckets[b]; id != 0; id = next[id]) {
      if(key == keys[id]) return -id;
    }
    final int s = size++;
    if(checkCapacity()) b = key & capacity() - 1;
    next[s] = buckets[b];
    keys[s] = key;
    buckets[b] = s;
    return s;
  }

  @Override
  protected int hash(final int id) {
    return keys[id];
  }

  @Override
  protected void rehash(final int newSize) {
    keys = Arrays.copyOf(keys, newSize);
  }

  /**
   * Returns an array with all elements.
   * @return array
   */
  public final int[] toArray() {
    return Arrays.copyOfRange(keys, 1, size);
  }

  @Override
  public String toString() {
    return toString(Arrays.stream(keys).boxed().toArray(Integer[]::new));
  }
}
