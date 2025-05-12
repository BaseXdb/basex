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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class HashItemSet extends ASet implements ItemSet {
  /** Equality function. */
  private final QueryBiPredicate<Item, Item> equal;
  /** Hashed keys. */
  Item[] keys;

  /**
   * Default constructor.
   * @param mode comparison mode
   * @param info input info (can be {@code null})
   */
  public HashItemSet(final Mode mode, final InputInfo info) {
    this(mode, info, INITIAL_CAPACITY);
  }

  /**
   * Default constructor.
   * @param mode comparison mode
   * @param info input info (can be {@code null})
   * @param capacity initial capacity
   */
  public HashItemSet(final Mode mode, final InputInfo info, final long capacity) {
    super(capacity);
    equal = switch(mode) {
      case ATOMIC -> Item::atomicEqual;
      case EQUAL -> (k1, k2) -> k1.equal(k2, null, info);
      default -> new DeepEqual(info)::equal;
    };
    keys = new Item[capacity()];
  }

  /**
   * Returns the key with the specified index.
   * @param index index of the key (starts with {@code 1})
   * @return key
   */
  public final Item key(final int index) {
    return keys[index];
  }

  /**
   * Returns all keys.
   * @return keys
   */
  public Item[] keys() {
    return Arrays.copyOfRange(keys, 1, size);
  }

  @Override
  public final boolean add(final Item key) throws QueryException {
    return store(key) >= 0;
  }

  /**
   * Stores the specified key and returns its index.
   * @param key key to be added
   * @return index of stored key (larger than {@code 0})
   * @throws QueryException query exception
   */
  public final int put(final Item key) throws QueryException {
    final int i = store(key);
    return Math.abs(i);
  }

  @Override
  public final boolean contains(final Item key) throws QueryException {
    return index(key) > 0;
  }

  /**
   * Returns the index of the specified key.
   * @param key key to be looked up
   * @return index, or {@code 0} if key does not exist
   * @throws QueryException query exception
   */
  public final int index(final Item key) throws QueryException {
    return index(key, key.hashCode() & capacity() - 1);
  }

  /**
   * Stores the specified key and returns its index, or returns the negative index if a key
   * already existed.
   * @param key key to be indexed
   * @return index, or negative index if the key already exists
   * @throws QueryException query exception
   */
  private int store(final Item key) throws QueryException {
    final int h = key.hashCode();
    int b = h & capacity() - 1;
    final int i = index(key, b);
    if(i > 0) {
      keys[i] = key;
      return -i;
    }

    final int s = size++;
    if(checkCapacity()) b = h & capacity() - 1;
    next[s] = buckets[b];
    keys[s] = key;
    buckets[b] = s;
    return s;
  }

  /**
   * Returns the index for the specified key and bucket.
   * @param key key to be looked up
   * @param b bucket index
   * @return index, or {@code 0} if key does not exist
   * @throws QueryException query exception
   */
  private int index(final Item key, final int b) throws QueryException {
    for(int i = buckets[b]; i != 0; i = next[i]) {
      if(equal.test(keys[i], key)) return i;
    }
    return 0;
  }

  @Override
  protected final int hashCode(final int index) {
    return keys[index].hashCode();
  }

  @Override
  protected void rehash(final int newSize) {
    keys = Array.copy(keys, new Item[newSize]);
  }

  @Override
  public final Iterator<Item> iterator() {
    return new ArrayIterator<>(keys, 1, size);
  }

  @Override
  public String toString() {
    return toString(keys);
  }
}
