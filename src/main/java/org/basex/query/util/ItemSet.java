package org.basex.query.util;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Set for quickly indexing items.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class ItemSet implements Iterable<Item> {
  /** Initial hash capacity. */
  protected static final int CAP = 1 << 3;
  /** Hash values. */
  private int[] hash = new int[CAP];
  /** Pointers to the next token. */
  private int[] next = new int[CAP];
  /** Hash table buckets. */
  private int[] bucket = new int[CAP];
  /** Hashed items. */
  Item[] keys = new Item[CAP];
  /** Hash entries. Actual hash size is {@code size - 1}. */
  int size = 1;

  /**
   * Indexes the specified key and returns the offset of the added key.
   * If the key already exists, a negative offset is returned.
   * @param key key
   * @param ii input info
   * @return offset of added key, negative offset otherwise
   * @throws QueryException query exception
   */
  public int add(final Item key, final InputInfo ii) throws QueryException {
    if(size == next.length) rehash();

    final int h = key.hash(ii);
    final int p = h & bucket.length - 1;
    for(int id = bucket[p]; id != 0; id = next[id]) {
      if(keys[id].equiv(ii, key)) return -id;
    }
    next[size] = bucket[p];
    hash[size] = h;
    keys[size] = key;
    bucket[p] = size;
    return size++;
  }

  /**
   * Returns the id of the specified key or -1 if key was not found.
   * @param key key to be found
   * @return id or 0 if nothing was found
   * @param ii input info
   * @throws QueryException query exception
   */
  public final int id(final Item key, final InputInfo ii) throws QueryException {
    final int h = key.hash(ii);
    final int p = h & bucket.length - 1;
    for(int id = bucket[p]; id != 0; id = next[id]) {
      if(keys[id].equiv(ii, key)) return id;
    }
    return 0;
  }

  /**
   * Resizes the hash table.
   */
  protected void rehash() {
    final int s = size << 1;
    final int[] b = new int[s];

    final int l = bucket.length;
    for(int i = 0; i < l; ++i) {
      int id = bucket[i];
      while(id != 0) {
        final int p = hash[id] & s - 1;
        final int nx = next[id];
        next[id] = b[p];
        b[p] = id;
        id = nx;
      }
    }
    bucket = b;
    next = Arrays.copyOf(next, s);
    hash = Arrays.copyOf(hash, s);
    final Item[] i = new Item[s];
    System.arraycopy(keys, 0, i, 0, size);
    keys = i;
  }

  /**
   * Returns the number of entries.
   * @return number of entries
   */
  public int size() {
    return size - 1;
  }

  @Override
  public Iterator<Item> iterator() {
    return new Iterator<Item>() {
      private int c = 1;
      @Override
      public boolean hasNext() { return c < size; }
      @Override
      public Item next() { return keys[c++]; }
      @Override
      public void remove() { Util.notexpected(); }
    };
  }
}
