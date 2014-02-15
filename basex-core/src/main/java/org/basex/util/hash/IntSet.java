package org.basex.util.hash;

import java.util.*;

import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash map for storing primitive integers.
 * It is related to the {@link TokenSet} class.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class IntSet extends ASet {
  /** Hashed keys. */
  int[] keys;

  /**
   * Default constructor.
   */
  public IntSet() {
    this(Array.CAPACITY);
  }

  /**
   * Default constructor.
   * @param capacity initial array capacity
   */
  public IntSet(final int capacity) {
    super(capacity);
    keys = new int[bucket.length];
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
  final int put(final int key) {
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
  final int id(final int key) {
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
   * Deletes the specified key.
   * The deletion of keys will lead to empty entries. If {@link #size} is called after
   * deletions, the original number of entries will be returned.
   * @param key key
   * @return deleted key or 0
   */
  int delete(final int key) {
    final int b = key & bucket.length - 1;
    for(int p = 0, i = bucket[b]; i != 0; p = i, i = next[i]) {
      if(key != keys[i]) continue;
      if(p == 0) bucket[b] = next[i];
      else next[p] = next[next[i]];
      keys[i] = 0;
      return i;
    }
    return 0;
  }

  /**
   * Stores the specified key and returns its id, or returns the negative id if the
   * key has already been stored.
   * @param key key to be found
   * @return id, or negative id if key has already been stored
   */
  private int index(final int key) {
    checkSize();
    final int b = key & bucket.length - 1;
    for(int r = bucket[b]; r != 0; r = next[r]) if(key == keys[r]) return -r;
    next[size] = bucket[b];
    keys[size] = key;
    bucket[b] = size;
    return size++;
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
}
