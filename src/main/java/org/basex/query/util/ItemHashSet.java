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
public class ItemHashSet implements ItemSet {
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

  @Override
  public int add(final Item key, final InputInfo ii) throws QueryException {
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
      if(keys[id].equiv(key, null, ii)) return id;
    }
    return 0;
  }

  @Override
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
