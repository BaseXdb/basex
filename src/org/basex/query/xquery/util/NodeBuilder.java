package org.basex.query.xquery.util;

import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.iter.NodIter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.util.Array;

/**
 * Node Builder.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class NodeBuilder {
  /** Node array. */
  public Nod[] list = new Nod[1];
  /** Number of entries. */
  public int size;
  /** Sort flag. */
  private boolean sort;
  /** Ordered node addition. */
  private final boolean ordered;

  /**
   * Constructor.
   * @param ord if set to true, all nodes are supposed to be added in order.
   */
  public NodeBuilder(final boolean ord) {
    ordered = ord;
  }

  /**
   * Deletes a value at the specified position.
   * @param pos deletion position
   */
  public void del(final int pos) {
    Array.move(list, pos + 1, -1, --size - pos);
  }

  /**
   * Adds a single node.
   * @param n node to be added
   */
  public void add(final Nod n) {
    if(size == list.length) resize();
    if(!ordered && !sort) sort = size != 0 && list[size - 1].diff(n) > 0;
    list[size++] = n;
  }
  
  /**
   * Resizes the sequence array.
   */
  private void resize() {
    final Nod[] tmp = new Nod[size << 1];
    System.arraycopy(list, 0, tmp, 0, size);
    list = tmp;
  }

  /**
   * Returns a sequence or the atomic item if only one item exists.
   * @return result
   */
  public Item finish() {
    if(size < 2 || ordered) return Seq.get(list, size);
    
    if(sort) sort(0, size);
    final SeqIter iter = new SeqIter();
    iter.add(list[0]);
    for(int s = 1; s < size; s++) {
      if(!list[s].is((Nod) iter.item[iter.size - 1])) iter.add(list[s]);
    }
    return iter.finish();
  }

  /**
   * Returns a sequence or the atomic item if only one item exists.
   * @return result
   */
  public NodIter iter() {
    if(size < 2 || ordered) return new NodIter(list, size);
    
    if(sort) sort(0, size);
    final NodIter iter = new NodIter();
    iter.add(list[0]);
    for(int s = 1; s < size; s++) {
      if(!list[s].is(iter.list[iter.size - 1])) iter.add(list[s]);
    }
    return iter;
  }
  
  /**
   * Recursively sorts the specified items via QuickSort.
   * @param s start position
   * @param e end position
   */
  private void sort(final int s, final int e) {
    if(e < 7) {
      for(int i = s; i < e + s; i++)
        for(int j = i; j > s && list[j - 1].diff(list[j]) > 0; j--) s(j, j - 1);
      return;
    }
    
    int m = s + (e >> 1);
    if(e > 7) {
      int l = s;
      int n = s + e - 1;
      if(e > 40) {
        final int k = e >>> 3;
        l = m(l, l + k, l + (k << 1));
        m = m(m - k, m, m + k);
        n = m(n - (k << 1), n - k, n);
      }
      m = m(l, m, n);
    }
    final Nod v = list[m];

    int a = s, b = a, c = s + e - 1, d = c;
    while(true) {
      while(b <= c) {
        final int h = list[b].diff(v);
        if(h > 0) break;
        if(h == 0) s(a++, b);
        b++;
      }
      while(c >= b) {
        final int h = list[c].diff(v);
        if(h < 0) break;
        if(h == 0) s(c, d--);
        c--;
      }
      if(b > c) break;
      s(b++, c--);
    }

    int k;
    final int n = s + e;
    k = Math.min(a - s, b - a);
    s(s, b - k, k);
    k = Math.min(d - c, n - d - 1);
    s(b, n - k, k);

    if((k = b - a) > 1) sort(s, k);
    if((k = d - c) > 1) sort(n - k, k);
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   * @param a first offset
   * @param b second offset
   * @param n number of values
   */
  private void s(final int a, final int b, final int n) {
    for(int i = 0; i < n; i++) s(a + i, b + i);
  }

  /**
   * Returns the index of the median of the three indexed integers.
   * @param a first offset
   * @param b second offset
   * @param c thirst offset
   * @return median
   */
  private int m(final int a, final int b, final int c) {
    return list[a].diff(list[b]) < 0 ?
      (list[b].diff(list[c]) < 0 ? b : list[a].diff(list[c]) < 0 ? c : a) :
       list[b].diff(list[c]) > 0 ? b : list[a].diff(list[c]) > 0 ? c : a;
  }

  /**
   * Swaps two entries.
   * @param a first position
   * @param b second position
   */
  private void s(final int a, final int b) {
    final Nod tmp = list[a];
    list[a] = list[b];
    list[b] = tmp;
  }
}
