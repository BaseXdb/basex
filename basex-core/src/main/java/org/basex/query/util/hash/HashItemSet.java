package org.basex.query.util.hash;

import java.util.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This is an efficient and memory-saving hash set for storing items.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class HashItemSet extends ASet implements ItemSet {
  /** Equality function. */
  private final QueryBiPredicate<Item, Item> equal;
  /** Hashed keys. */
  private Item[] keys;
  /** Hash values. */
  private int[] hash;

  /**
   * Default constructor.
   * @param mode comparison mode
   * @param info input info (can be {@code null})
   */
  public HashItemSet(final Mode mode, final InputInfo info) {
    super(Array.INITIAL_CAPACITY);
    switch(mode) {
      case ATOMIC:
        this.equal = (k1, k2) -> k1.atomicEqual(k2);
        break;
      case EQUAL:
        this.equal = (k1, k2) -> k1.equal(k2, null, info);
        break;
      default:
        final DeepEqual deep = new DeepEqual(info);
        this.equal = (k1, k2) -> deep.equal(k1, k2);
    }
    final int c = capacity();
    keys = new Item[c];
    hash = new int[c];
  }

  @Override
  public boolean add(final Item key) throws QueryException {
    return index(key) >= 0;
  }

  /**
   * Checks if the set contains the specified key.
   * @param key key to be looked up
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean contains(final Item key) throws QueryException {
    return id(key) > 0;
  }

  /**
   * Returns the id of the specified key, or {@code 0} if the key does not exist.
   * @param key key to be looked up
   * @return id, or {@code 0} if key does not exist
   * @throws QueryException query exception
   */
  public int id(final Item key) throws QueryException {
    return id(key, key.hash() & capacity() - 1);
  }

  /**
   * Stores the specified key and returns its index, or returns the negative index if the key has
   * already been stored. The public method {@link #add} can be used to check if an added value
   * exists.
   * @param key key to be indexed
   * @return id, or negative id if key has already been stored
   * @throws QueryException query exception
   */
  private int index(final Item key) throws QueryException {
    final int h = key.hash();
    int b = h & capacity() - 1;
    final int id = id(key, b);
    if(id > 0) return -id;

    final int s = size++;
    if(checkCapacity()) b = h & capacity() - 1;
    next[s] = buckets[b];
    keys[s] = key;
    hash[s] = h;
    buckets[b] = s;
    return s;
  }

  /**
   * Returns the id for the specified key and bucket, or {@code 0} if the key does not exist.
   * @param key key to be looked up
   * @return id, or {@code 0} if key does not exist
   * @param b bucket index
   * @throws QueryException query exception
   */
  private int id(final Item key, final int b) throws QueryException {
    for(int id = buckets[b]; id != 0; id = next[id]) {
      if(equal.test(keys[id], key)) return id;
    }
    return 0;
  }

  @Override
  protected int hash(final int id) {
    return hash[id];
  }

  @Override
  protected void rehash(final int newSize) {
    keys = Array.copy(keys, new Item[newSize]);
    hash = Arrays.copyOf(hash, newSize);
  }

  @Override
  public Iterator<Item> iterator() {
    return new ArrayIterator<>(keys, 1, size);
  }

  @Override
  public String toString() {
    return toString(keys);
  }
}
