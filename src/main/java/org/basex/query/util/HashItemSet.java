package org.basex.query.util;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This is an efficient and memory-saving hash map for storing items.
 * It is related to the {@link TokenSet} class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class HashItemSet extends ASet implements ItemSet {
  /** Hash values. */
  private int[] hash;
  /** Hashed items. */
  private Item[] keys;

  /**
   * Default constructor.
   */
  public HashItemSet() {
    hash = new int[Array.CAPACITY];
    keys = new Item[Array.CAPACITY];
    clear();
  }


  @Override
  public final boolean add(final Item key, final InputInfo ii) throws QueryException {
    return index(key, ii) > 0;
  }

  /**
   * Stores the specified key and returns its id.
   * @param key key to be added
   * @param ii input info
   * @return unique id of stored key (larger than zero)
   * @throws QueryException query exception
   */
  public int put(final Item key, final InputInfo ii) throws QueryException {
    final int i = index(key, ii);
    return Math.abs(i);
  }

  /**
   * Returns the id of the specified key, or {@code 0} if the key does not exist.
   * @param key key to be looked up
   * @param ii input info
   * @return id, or {@code 0} if key does not exist
   * @throws QueryException query exception
   */
  public final int id(final Item key, final InputInfo ii) throws QueryException {
    final int h = key.hash(ii);
    final int p = h & bucket.length - 1;
    for(int id = bucket[p]; id != 0; id = next[id]) {
      if(keys[id].equiv(key, null, ii)) return -id;
    }
    return 0;
  }

  /**
   * Stores the specified key and returns its id, or returns the negative id if the
   * key has already been stored.
   * @param key key to be found
   * @param ii input info
   * @return id, or negative id if key has already been stored
   * @throws QueryException query exception
   */
  private int index(final Item key, final InputInfo ii) throws QueryException {
    checkSize();
    final int h = key.hash(ii);
    final int b = h & bucket.length - 1;
    for(int r = bucket[b]; r != 0; r = next[r]) {
      if(keys[r].equiv(key, null, ii)) return -r;
    }
    next[size] = bucket[b];
    keys[size] = key;
    hash[size] = h;
    bucket[b] = size;
    return size++;
  }

  @Override
  public Iterator<Item> iterator() {
    return new ArrayIterator<Item>(keys, 1, size);
  }

  @Override
  protected int hash(final int id) {
    return hash[id];
  }

  @Override
  protected void rehash(final int newSize) {
    keys = Array.copy(keys, new Item[newSize]);
  }
}
