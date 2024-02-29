package org.basex.query.util.hash;

import java.util.*;

import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This is an efficient and memory-saving hash set for storing QNames.
 * It is derived from the {@link TokenSet} class.
 *
 * @author BaseX Team 2005-24, BSD License
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
    return index(key) > 0;
  }

  /**
   * Stores the specified key and returns its id.
   * @param key key to be added
   * @return unique id of stored key (larger than zero)
   */
  public final int put(final QNm key) {
    final int id = index(key);
    return Math.abs(id);
  }

  /**
   * Checks if the set contains the specified key.
   * @param key key to be looked up
   * @return result of check
   */
  public final boolean contains(final QNm key) {
    return id(key) > 0;
  }

  /**
   * Returns the id of the specified key, or {@code 0} if the key does not exist.
   * @param key key to be looked up
   * @return id, or {@code 0} if key does not exist
   */
  public final int id(final QNm key) {
    final int b = key.hash() & capacity() - 1;
    for(int id = buckets[b]; id != 0; id = next[id]) {
      if(key.eq(keys[id])) return id;
    }
    return 0;
  }

  /**
   * Returns the key with the specified id.
   * All ids start with {@code 1} instead of {@code 0}.
   * @param id id of the key to return
   * @return key
   */
  public final QNm key(final int id) {
    return keys[id];
  }

  /**
   * Stores the specified key and returns its id, or returns the negative id if the key has already
   * been stored. The public method {@link #add} can be used to check if an added value exists.
   * @param key key to be indexed
   * @return id, or negative id if key has already been stored
   */
  private int index(final QNm key) {
    final int h = key.hash();
    int b = h & capacity() - 1;
    for(int id = buckets[b]; id != 0; id = next[id]) {
      if(keys[id].eq(key)) return -id;
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
  protected final int hash(final int id) {
    return hash[id];
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

  @Override
  public String toString() {
    return toString(keys);
  }
}
