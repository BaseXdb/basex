package org.basex.query.xquery.util;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.CmpV;
import org.basex.query.xquery.item.Item;
import org.basex.util.Array;

/**
 * Scoring class, assembling all scoring calculations.
 * Current scoring is very simple and mainly used for testing.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class ItemSet {
  /** Initial hash capacity. */
  protected static final int CAP = 1 << 3;
  /** Hash keys. */
  private int[] keys = new int[CAP];
  /** Pointers to the next token. */
  private int[] next = new int[CAP];
  /** Hash table buckets. */
  private int[] bucket = new int[CAP];
  /** Items. */
  private Item[] values = new Item[CAP];
  /** Hash entries. Actual hash size is <code>size - 1</code>. */
  private int size = 1;

  /**
   * Indexes the specified item.
   * @param i item
   * @return true if value is new
   * @throws XQException evaluation exception
   */
  public boolean index(final Item i) throws XQException {
    if(size == next.length) rehash();

    final int h = i.hash();
    final int p = h & bucket.length - 1;
    final boolean nan = i.n() && Double.isNaN(i.dbl());
    for(int id = bucket[p]; id != 0; id = next[id]) {
      final Item c = values[id];
      // double value is NaN or values are equal..
      if(nan && c.n() && Double.isNaN(c.dbl()) || CmpV.valCheck(i, c) &&
          CmpV.COMP.EQ.e(i, c)) return false;
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
  protected void rehash() {
    final int s = size << 1;
    final int[] tmp = new int[s];

    final int l = bucket.length;
    for(int i = 0; i != l; i++) {
      int id = bucket[i];
      while(id != 0) {
        final int p = keys[id] & s - 1;
        final int nx = next[id];
        next[id] = tmp[p];
        tmp[p] = id;
        id = nx;
      }
    }
    bucket = tmp;
    next = Array.extend(next);
    keys = Array.extend(keys);
    values = Array.extend(values);
  }
}
