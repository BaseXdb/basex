package org.basex.query.util.hash;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This is an efficient and memory-saving hash map for storing items.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class HashItemSet extends ASet implements ItemSet {
  /** Equality vs. equivalence check. */
  private final boolean eq;
  /** Hashed keys. */
  private Item[] keys;
  /** Hash values. */
  private int[] hash;

  /**
   * Default constructor.
   * @param eq equality check
   */
  public HashItemSet(final boolean eq) {
    super(Array.INITIAL_CAPACITY);
    this.eq = eq;
    keys = new Item[capacity()];
    hash = new int[capacity()];
  }

  @Override
  public boolean add(final Item item, final InputInfo ii) throws QueryException {
    return index(item, ii) >= 0;
  }

  /**
   * Checks if the specified item exists.
   * @param item item to look up
   * @param ii input info (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean contains(final Item item, final InputInfo ii) throws QueryException {
    return id(item, ii) > 0;
  }

  /**
   * Returns the id of the specified QName, or {@code 0} if the QName does not exist.
   * @param item item to look up
   * @param ii input info (can be {@code null})
   * @return id, or {@code 0} if QName does not exist
   * @throws QueryException query exception
   */
  public int id(final Item item, final InputInfo ii) throws QueryException {
    final int b = item.hash(ii) & capacity() - 1;
    for(int id = buckets[b]; id != 0; id = next[id]) {
      if(eq ? keys[id].eq(item, null, null, ii) : keys[id].equiv(item, null, ii)) return id;
    }
    return 0;
  }

  /**
   * Stores the specified QName and returns its id, or returns the negative id if the
   * QName has already been stored.
   * @param item item to look up
   * @param ii input info (can be {@code null})
   * @return id, or negative id if QName has already been stored
   * @throws QueryException query exception
   */
  private int index(final Item item, final InputInfo ii) throws QueryException {
    checkSize();
    final int h = item.hash(ii), b = h & capacity() - 1;
    for(int id = buckets[b]; id != 0; id = next[id]) {
      if(eq ? keys[id].eq(item, null, null, ii) : keys[id].equiv(item, null, ii)) return -id;
    }
    final int s = size++;
    next[s] = buckets[b];
    keys[s] = item;
    hash[s] = h;
    buckets[b] = s;
    return s;
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
