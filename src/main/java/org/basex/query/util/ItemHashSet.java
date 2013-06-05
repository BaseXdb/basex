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
public class ItemHashSet implements ItemSet {
  /** Hash entries. Actual hash size is {@code size - 1}. */
  protected int size = 1;

  /** Hash values. */
  private int[] hash = new int[Array.CAPACITY];
  /** Pointers to the next token. */
  private int[] next = new int[Array.CAPACITY];
  /** Hash table buckets. */
  private int[] bucket = new int[Array.CAPACITY];
  /** Hashed items. */
  private Item[] keys = new Item[Array.CAPACITY];

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
    if(size == next.length) rehash();
    final int h = key.hash(ii);
    final int p = h & bucket.length - 1;
    for(int id = bucket[p]; id != 0; id = next[id]) {
      if(keys[id].equiv(key, null, ii)) return -id;
    }
    next[size] = bucket[p];
    hash[size] = h;
    keys[size] = key;
    bucket[p] = size;
    return size++;
  }

  /**
   * Returns the number of entries.
   * @return number of entries
   */
  public int size() {
    return size - 1;
  }

  /**
   * Resizes the hash table.
   */
  protected void rehash() {
    final int s = size << 1;
    final int[] tmp = new int[s];
    for(final int b : bucket) {
      int id = b;
      while(id != 0) {
        final int p = hash[id] & s - 1;
        final int nx = next[id];
        next[id] = tmp[p];
        tmp[p] = id;
        id = nx;
      }
    }
    bucket = tmp;
    next = Arrays.copyOf(next, s);
    hash = Arrays.copyOf(hash, s);
    final Item[] i = new Item[s];
    System.arraycopy(keys, 0, i, 0, size);
    keys = i;
  }

  @Override
  public Iterator<Item> iterator() {
    return new ArrayIterator<Item>(keys, 1, size);
  }
}
