package org.basex.query.util;

import java.util.Arrays;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.util.InputInfo;

/**
 * Set for quickly indexing items.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ItemSet {
  /** Initial hash capacity. */
  private static final int CAP = 1 << 3;
  /** Hash keys. */
  private int[] keys = new int[CAP];
  /** Pointers to the next token. */
  private int[] next = new int[CAP];
  /** Hash table buckets. */
  private int[] bucket = new int[CAP];
  /** Items. */
  private Item[] values = new Item[CAP];
  /** Hash entries. Actual hash size is {@code size - 1}. */
  private int size = 1;

  /**
   * Indexes the specified item.
   * @param ii input info
   * @param i item
   * @return true if value is new
   * @throws QueryException query exception
   */
  public boolean index(final InputInfo ii, final Item i) throws QueryException {
    if(size == next.length) rehash();

    final int h = i.hash();
    final int p = h & bucket.length - 1;
    for(int id = bucket[p]; id != 0; id = next[id]) {
      if(values[id].equiv(ii, i)) return false;
    }

    next[size] = bucket[p];
    keys[size] = h;
    values[size] = i;
    bucket[p] = size++;
    return true;
  }

  /**
   * Resizes the hash table.
   */
  private void rehash() {
    final int s = size << 1;
    final int[] b = new int[s];

    final int l = bucket.length;
    for(int i = 0; i < l; ++i) {
      int id = bucket[i];
      while(id != 0) {
        final int p = keys[id] & s - 1;
        final int nx = next[id];
        next[id] = b[p];
        b[p] = id;
        id = nx;
      }
    }
    bucket = b;
    next = Arrays.copyOf(next, s);
    keys = Arrays.copyOf(keys, s);
    final Item[] i = new Item[s];
    System.arraycopy(values, 0, i, 0, size);
    values = i;
  }
}
