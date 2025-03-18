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
    this(mode, info, Array.INITIAL_CAPACITY);
  }

  /**
   * Default constructor.
   * @param mode comparison mode
   * @param info input info (can be {@code null})
   * @param capacity initial capacity
   */
  public HashItemSet(final Mode mode, final InputInfo info, final long capacity) {
    super(capacity);
    switch(mode) {
      case ATOMIC:
        this.equal = Item::atomicEqual;
        break;
      case EQUAL:
        this.equal = (k1, k2) -> k1.equal(k2, null, info);
        break;
      default:
        final DeepEqual deep = new DeepEqual(info);
        this.equal = (k1, k2) -> deep.equal(k1, k2);
    }
    keys = new Item[capacity()];
  }

  /**
   * Returns the key with the specified id.
   * All ids start with {@code 1} instead of {@code 0}.
   * @param id id of the key
   * @return key
   */
  public final Item key(final int id) {
    return keys[id];
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
    return index(key) >= 0;
  }

  /**
   * Stores the specified key and returns its id.
   * @param key key to be added
   * @return unique id of stored key (larger than zero)
   * @throws QueryException query exception
   */
  public final int put(final Item key) throws QueryException {
    final int id = index(key);
    return Math.abs(id);
  }

  /**
   * Checks if the set contains the specified key.
   * @param key key to be looked up
   * @return result of check
   * @throws QueryException query exception
   */
  public final boolean contains(final Item key) throws QueryException {
    return id(key) > 0;
  }

  /**
   * Returns the id of the specified key, or {@code 0} if the key does not exist.
   * @param key key to be looked up
   * @return id, or {@code 0} if key does not exist
   * @throws QueryException query exception
   */
  public final int id(final Item key) throws QueryException {
    return id(key, key.hashCode() & capacity() - 1);
  }

  /**
   * Stores the specified key and returns its index, or returns the negative index if a key
   * already existed.
   * @param key key to be indexed
   * @return id, or negative id if the key already exists
   * @throws QueryException query exception
   */
  private int index(final Item key) throws QueryException {
    final int h = key.hashCode();
    int b = h & capacity() - 1;
    final int id = id(key, b);
    if(id > 0) {
      keys[id] = key;
      return -id;
    }

    final int s = size++;
    if(checkCapacity()) b = h & capacity() - 1;
    next[s] = buckets[b];
    keys[s] = key;
    buckets[b] = s;
    return s;
  }

  /**
   * Returns the id for the specified key and bucket, or {@code 0} if the key does not exist.
   * @param key key to be looked up
   * @param b bucket index
   * @return id, or {@code 0} if key does not exist
   * @throws QueryException query exception
   */
  private int id(final Item key, final int b) throws QueryException {
    for(int id = buckets[b]; id != 0; id = next[id]) {
      if(equal.test(keys[id], key)) return id;
    }
    return 0;
  }

  @Override
  protected final int hashCode(final int id) {
    return keys[id].hashCode();
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
