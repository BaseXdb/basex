package org.basex.query.util.hash;

import java.util.*;

import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This is an efficient and memory-saving hash set for storing QNames.
 * It is derived from the {@link TokenSet} class.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class QNmSet extends ASet implements Iterable<QNm> {
  /** Hashed keys. */
  protected QNm[] keys;
  /** Hash values. */
  private int[] hash;

  /**
   * Default constructor.
   */
  public QNmSet() {
    super(Array.INITIAL_CAPACITY);
    keys = new QNm[capacity()];
    hash = new int[capacity()];
  }

  /**
   * Stores the specified key if it has not been stored before.
   * @param key key to be added
   * @return {@code true} if the key did not exist yet and was stored
   */
  public final boolean add(final QNm key) {
    return store(key) > 0;
  }

  /**
   * Stores the specified key and returns its index.
   * @param key key to be added
   * @return index of stored key (larger than {@code 0})
   */
  public final int put(final QNm key) {
    final int i = store(key);
    return Math.abs(i);
  }

  /**
   * Checks if the set contains the specified key.
   * @param key key to be looked up
   * @return result of check
   */
  public final boolean contains(final QNm key) {
    return index(key) > 0;
  }

  /**
   * Returns the index of the specified key.
   * @param key key to be looked up
   * @return index, or {@code 0} if key does not exist
   */
  public final int index(final QNm key) {
    final int b = key.hashCode() & capacity() - 1;
    for(int i = buckets[b]; i != 0; i = next[i]) {
      if(key.eq(keys[i])) return i;
    }
    return 0;
  }

  /**
   * Stores the specified key and returns its index,
   * or returns the negative index if the key has already been stored.
   * @param key key to be indexed
   * @return index, or negative index if the key already exists
   */
  private int store(final QNm key) {
    final int h = key.hashCode();
    int b = h & capacity() - 1;
    for(int i = buckets[b]; i != 0; i = next[i]) {
      if(keys[i].eq(key)) return -i;
    }
    final int s = size++;
    if(checkCapacity()) b = h & capacity() - 1;
    next[s] = buckets[b];
    keys[s] = key;
    hash[s] = h;
    buckets[b] = s;
    return s;
  }

  @Override
  protected final int hashCode(final int index) {
    return hash[index];
  }

  @Override
  protected void rehash(final int newSize) {
    keys = Array.copy(keys, new QNm[newSize]);
    hash = Arrays.copyOf(hash, newSize);
  }

  @Override
  public final Iterator<QNm> iterator() {
    return new ArrayIterator<>(keys, 1, size);
  }

  /**
   * Returns an array with all keys.
   * @return array
   */
  public final QNm[] keys() {
    return Arrays.copyOfRange(keys, 1, size);
  }

  @Override
  public String toString() {
    return toString(keys);
  }
}
