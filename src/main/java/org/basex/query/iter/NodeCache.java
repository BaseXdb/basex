package org.basex.query.iter;

import java.util.*;

import org.basex.data.*;
import org.basex.query.item.*;
import org.basex.util.*;

/**
 * Caching node iterator, returning sorted nodes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class NodeCache extends AxisIter {
  /** Node container. */
  public ANode[] item;
  /** Number of nodes. */
  private int size;
  /** Current iterator position. */
  private int pos = -1;
  /** Sort flag. */
  private boolean sort;
  /** Flag for potential duplicates and unsorted entries. */
  private boolean random;

  /**
   * Constructor.
   */
  public NodeCache() {
    item = new ANode[1];
  }

  /**
   * Constructor, specifying an initial array of sorted nodes.
   * @param it node array
   * @param s size
   */
  public NodeCache(final ANode[] it, final int s) {
    item = it;
    size = s;
  }

  /**
   * Sets the internal duplicate flag, which indicates that duplicate and
   * unordered nodes might be added to this iterator.
   * @return self reference
   */
  public NodeCache random() {
    random = true;
    return this;
  }

  /**
   * Returns the specified node.
   * @param i node offset
   * @return node
   */
  public ANode get(final int i) {
    return item[i];
  }

  /**
   * Deletes a value at the specified position.
   * @param p deletion position
   */
  public void delete(final int p) {
    Array.move(item, p + 1, -1, --size - p);
  }

  /**
   * Adds a node.
   * @param n node to be added
   */
  public void add(final ANode n) {
    if(size == item.length) {
      final ANode[] tmp = new ANode[Array.newSize(size)];
      System.arraycopy(item, 0, tmp, 0, size);
      item = tmp;
    }
    if(random && !sort && size != 0) sort = item[size - 1].diff(n) > 0;
    item[size++] = n;
  }

  @Override
  public boolean reset() {
    pos = -1;
    return true;
  }

  @Override
  public ANode next() {
    if(random) sort(sort);
    return ++pos < size ? item[pos] : null;
  }

  @Override
  public ANode get(final long i) {
    return i < size ? item[(int) i] : null;
  }

  @Override
  public long size() {
    return size;
  }

  /**
   * Sets a new item size.
   * @param s size
   */
  public void size(final int s) {
    size = s;
  }

  @Override
  public Value value() {
    if(random) sort(sort);
    return Seq.get(item, size);
  }

  /**
   * Checks if binary search can be applied to this iterator, i.e.
   * if all nodes are {@link DBNode} references and refer to the same database.
   * @return result of check
   */
  public boolean dbnodes() {
    if(random) sort(sort);

    final Data data = size > 0 ? item[0].data() : null;
    if(data == null) return false;
    for(int s = 1; s < size; ++s) if(data != item[s].data()) return false;
    return true;
  }

  /**
   * Checks if the iterator contains a database node with the specified pre value.
   * @param node node to be found
   * @param db indicates if all nodes are sorted {@link DBNode} references
   * @return position, or {@code -1}
   */
  public int indexOf(final ANode node, final boolean db) {
    if(db) return node instanceof DBNode ?
        Math.max(binarySearch((DBNode) node, 0, size), -1) : -1;
    for(int s = 0; s < size(); ++s) if(item[s].is(node)) return s;
    return -1;
  }

  /**
   * Performs a binary search on the given range of this sequence iterator,
   * assuming that all nodes are {@link DBNode}s from the same {@link Data}
   * instance (i.e., {@link #dbnodes()} returns {@code true}).
   * @param nd node to find
   * @param start start of the search interval
   * @param length length of the search interval
   * @return position of the item or {@code -insertPosition - 1} if not found
   */
  public int binarySearch(final DBNode nd, final int start, final int length) {
    if(size == 0 || nd.data != item[0].data()) return -start - 1;
    int l = start, r = start + length - 1;
    while(l <= r) {
      final int m = l + r >>> 1;
      final int npre = ((DBNode) item[m]).pre;
      if(npre == nd.pre) return m;
      if(npre < nd.pre) l = m + 1;
      else r = m - 1;
    }
    return -(l + 1);
  }

  /**
   * Sorts the nodes, if necessary.
   * @return self reference
   */
  public NodeCache sort() {
    if(random) sort(sort);
    return this;
  }

  /**
   * Sorts the nodes.
   * @param force force sort
   */
  private void sort(final boolean force) {
    random = false;
    if(size > 1) {
      // sort arrays and remove duplicates
      if(force) sort(0, size);

      // remove duplicates and merge scores
      int i = 1;
      for(int j = 1; j < size; ++j) {
        while(j < size && item[i - 1].is(item[j])) {
          item[i - 1].score(Math.max(item[j++].score(), item[i - 1].score()));
        }
        if(j == size) break;
        item[i++] = item[j];
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
      for(int i = s; i < e + s; ++i)
        for(int j = i; j > s && item[j - 1].diff(item[j]) > 0; j--) s(j, j - 1);
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
    final ANode v = item[m];

    int a = s, b = a, c = s + e - 1, d = c;
    while(true) {
      while(b <= c) {
        final int h = item[b].diff(v);
        if(h > 0) break;
        if(h == 0) s(a++, b);
        ++b;
      }
      while(c >= b) {
        final int h = item[c].diff(v);
        if(h < 0) break;
        if(h == 0) s(c, d--);
        --c;
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
    for(int i = 0; i < n; ++i) s(a + i, b + i);
  }

  /**
   * Returns the index of the median of the three indexed integers.
   * @param a first offset
   * @param b second offset
   * @param c thirst offset
   * @return median
   */
  private int m(final int a, final int b, final int c) {
    return item[a].diff(item[b]) < 0 ?
      item[b].diff(item[c]) < 0 ? b : item[a].diff(item[c]) < 0 ? c : a :
      item[b].diff(item[c]) > 0 ? b : item[a].diff(item[c]) > 0 ? c : a;
  }

  /**
   * Swaps two entries.
   * @param a first position
   * @param b second position
   */
  private void s(final int a, final int b) {
    final ANode tmp = item[a];
    item[a] = item[b];
    item[b] = tmp;
  }

  @Override
  public String toString() {
    return Util.name(this) + Arrays.toString(Arrays.copyOf(item, size));
  }
}
