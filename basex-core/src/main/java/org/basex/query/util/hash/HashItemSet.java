package org.basex.query.util.hash;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This is an efficient and memory-saving hash map for storing items. Items with identical hash
 * keys are checked for equivalence.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public class HashItemSet extends ASet implements ItemSet {
  /** Hash values. */
  private int[] hash = new int[Array.CAPACITY];
  /** Hashed items. */
  private Item[] items = new Item[Array.CAPACITY];
  /** Equality check (stricter than equivalence check). */
  private final boolean eq;

  /**
   * Default constructor.
   * @param eq equality check
   */
  public HashItemSet(final boolean eq) {
    super(Array.CAPACITY);
    this.eq = eq;
  }

  @Override
  public final boolean add(final Item item, final InputInfo ii) throws QueryException {
    return !check(item, ii, true);
  }

  /**
   * Checks if the specified item exists.
   * @param item item to look up
   * @param ii input info
   * @return result of check
   * @throws QueryException query exception
   */
  public final boolean contains(final Item item, final InputInfo ii) throws QueryException {
    return check(item, ii, false);
  }

  /**
   * Checks if an item exists in the index.
   * @param item item to look up
   * @param ii input info
   * @param add add entry
   * @return id, or negative id if key has already been stored
   * @throws QueryException query exception
   */
  private boolean check(final Item item, final InputInfo ii, final boolean add)
      throws QueryException {

    checkSize();
    final int h = item.hash(ii), b = h & buckets.length - 1;
    for(int id = buckets[b]; id != 0; id = next[id]) {
      if(eq ? items[id].eq(item, null, null, ii) : items[id].equiv(item, null, ii)) return true;
    }
    if(add) {
      final int s = size;
      next[s] = buckets[b];
      items[s] = item;
      hash[s] = h;
      buckets[b] = s;
      size = s + 1;
    }
    return false;
  }

  @Override
  public Iterator<Item> iterator() {
    return new ArrayIterator<>(items, 1, size);
  }

  @Override
  protected int hash(final int id) {
    return hash[id];
  }

  @Override
  protected void rehash(final int newSize) {
    items = Array.copy(items, new Item[newSize]);
    hash = Arrays.copyOf(hash, newSize);
  }
}
