package org.basex.query.iter;

import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Seq;
import org.basex.util.Array;

/**
 * Simple node Iterator, ignoring duplicates.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class NodIter extends NodeIter {
  /** Items. */
  public Nod[] list;
  /** Size. */
  public int size;

  /** Iterator. */
  private int pos = -1;
  /** Sort flag. */
  private boolean sort;
  /** Ordered node addition. */
  private boolean ordered;

  /**
   * Constructor.
   */
  public NodIter() {
    this(true);
  }

  /**
   * Constructor.
   * @param ord if set to true, all nodes are supposed to be added in order.
   */
  public NodIter(final boolean ord) {
    list = new Nod[1];
    ordered = ord;
  }

  /**
   * Constructor.
   * @param l node list
   * @param s number of nodes
   */
  public NodIter(final Nod[] l, final int s) {
    this(true);
    list = l;
    size = s;
  }
  
  @Override
  public boolean ordered() {
    return ordered;
  }

  /**
   * Deletes a value at the specified position.
   * @param p deletion position
   */
  public void delete(final int p) {
    Array.move(list, p + 1, -1, --size - p);
  }

  /**
   * Adds a node.
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

  @Override
  public boolean reset() {
    pos = -1;
    return true;
  }

  @Override
  public Nod next() {
    if(!ordered) sort(sort);
    return ++pos < size ? list[pos] : null;
  }

  @Override
  public Item get(final long i) {
    return i < size ? list[(int) i] : null;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public Item finish() {
    if(!ordered) sort(sort);
    return Seq.get(list, size);
  }

  /**
   * Sorts the nodes.
   * @param force force sort
   */
  public void sort(final boolean force) {
    ordered = true;
    if(size > 1) {
      // sort arrays and remove duplicates
      if(force) sort(0, size);
      int i = 1;
      for(int j = 1; j < size; j++) {
        if(!list[i - 1].is(list[j])) list[i++] = list[j];
      }
      size = i;
    }
  }
  
  /**
   * Recursively sorts the specified items via QuickSort
   * (derived from Java's sort algorithms).
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

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("(");
    for(int v = 0; v != size; v++) {
      sb.append((v != 0 ? ", " : "") + list[v]);
      if(sb.length() > 15 && v + 1 != size) {
        sb.append(", ...");
        break;
      }
    }
    return sb.append(")").toString();
  }
}
