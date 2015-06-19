package org.basex.query.util.list;

import java.util.*;

import org.basex.data.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Resizable-array implementation for nodes. If the {@link #check} function is called, the stored
 * nodes will be sorted and duplicates will before removed before they are returned as value or
 * via an iterator.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ANodeList extends ElementList implements Iterable<ANode> {
  /** Element container. */
  private ANode[] list;
  /** Sort flag. */
  private boolean sort;
  /** Check incoming nodes for potential duplicates and unsorted entries. */
  private boolean check;

  /**
   * Constructor.
   */
  public ANodeList() {
    this(1);
  }

  /**
   * Constructor, specifying an initial array capacity.
   * @param capacity array capacity
   */
  public ANodeList(final int capacity) {
    list = new ANode[capacity];
  }

  /**
   * Lightweight constructor, assigning the specified array.
   * @param elements initial array
   */
  public ANodeList(final ANode... elements) {
    list = elements;
    size = elements.length;
  }

  /**
   * Checks all nodes for potential duplicates and orderedness.
   * @return self reference
   */
  public ANodeList check() {
    check = true;
    return this;
  }

  /**
   * Adds an element to the array.
   * @param element element to be added
   */
  public void add(final ANode element) {
    ANode[] lst = list;
    final int s = size;
    if(s == lst.length) lst = copyOf(newSize());
    if(s != 0 && check && !sort) sort = lst[s - 1].diff(element) > 0;
    lst[s] = element;
    list = lst;
    size = s + 1;
  }

  /**
   * Sets an element at the specified index position.
   * @param index index
   * @param element element to be set
   */
  public void set(final int index, final ANode element) {
    ANode[] lst = list;
    if(index >= lst.length) lst = copyOf(newSize(index + 1));
    lst[index] = element;
    list = lst;
    size = Math.max(size, index + 1);
  }

  /**
   * Returns the specified element.
   * @param index index
   * @return element
   */
  public ANode get(final int index) {
    return list[index];
  }

  /**
   * Deletes the element at the specified position.
   * @param index index of the element to delete
   * @return deleted element
   */
  public ANode delete(final int index) {
    final ANode l = list[index];
    Array.move(list, index + 1, -1, --size - index);
    return l;
  }

  /**
   * Returns a {@link Value} representation of all items.
   * @return array
   */
  public Value value() {
    sort();
    return ValueBuilder.value(list, size, NodeType.NOD);
  }

  @Override
  public Iterator<ANode> iterator() {
    sort();
    return new ArrayIterator<>(list, size);
  }

  /**
   * Returns an iterator over the items in this list.
   * The list must not be modified after the iterator has been requested.
   * @return the iterator
   */
  public BasicNodeIter iter() {
    sort();

    return new BasicNodeIter() {
      int pos;

      @Override
      public Value value() {
        return ANodeList.this.value();
      }

      @Override
      public long size() {
        return size;
      }

      @Override
      public ANode next() {
        return pos < size ? list[pos++] : null;
      }

      @Override
      public ANode get(final long i) {
        return list[(int) i];
      }
    };
  }

  /**
   * Sorts the nodes and checks if binary search can be applied to this iterator.
   * This is the case if all nodes are {@link DBNode}s and refer to the same database.
   * @return result of check
   */
  public boolean dbnodes() {
    sort();

    final int s = size;
    final ANode[] lst = list;
    final Data data = s > 0 ? lst[0].data() : null;
    if(data == null) return false;
    for(int l = 1; l < s; ++l) if(data != lst[l].data()) return false;
    return true;
  }

  /**
   * Checks if the iterator contains a database node with the specified pre value.
   * @param node node to be found
   * @param db indicates if all nodes are sorted {@link DBNode}s
   * @return position, or {@code -1}
   */
  public int indexOf(final ANode node, final boolean db) {
    if(db) return node instanceof DBNode ? Math.max(binarySearch((DBNode) node, 0, size), -1) : -1;

    final long sz = size();
    final ANode[] lst = list;
    for(int s = 0; s < sz; ++s) if(lst[s].is(node)) return s;
    return -1;
  }

  /**
   * Performs a binary search on the given range of this sequence iterator,
   * assuming that all nodes are {@link DBNode}s from the same {@link Data}
   * instance (i.e., {@link #dbnodes()} returns {@code true}).
   * @param node node to find
   * @param start start of the search interval
   * @param length length of the search interval
   * @return position of the item or {@code -insertPosition - 1} if not found
   */
  public int binarySearch(final DBNode node, final int start, final int length) {
    if(size == 0 || node.data() != list[0].data()) return -start - 1;
    int l = start, r = start + length - 1;
    final ANode[] lst = list;
    while(l <= r) {
      final int m = l + r >>> 1, mpre = ((DBNode) lst[m]).pre(), npre = node.pre();
      if(mpre == npre) return m;
      if(mpre < npre) l = m + 1;
      else r = m - 1;
    }
    return -(l + 1);
  }

  /**
   * Creates a resized array.
   * @param s new size
   * @return new array
   */
  private ANode[] copyOf(final int s) {
    final ANode[] tmp = new ANode[s];
    System.arraycopy(list, 0, tmp, 0, size);
    return tmp;
  }

  /**
   * Sorts the nodes.
   */
  private void sort() {
    if(!check) return;

    final int s = size;
    if(s > 1) {
      if(sort) sort(0, s);

      // remove duplicates
      int i = 1;
      final ANode[] lst = list;
      for(int j = 1; j < s; ++j) {
        while(j < s && lst[i - 1].is(lst[j])) j++;
        if(j < s) lst[i++] = lst[j];
      }
      size = i;
    }
    check = false;
  }

  /**
   * Recursively sorts the specified items via QuickSort
   * (derived from Java's sort algorithms).
   * @param s start position
   * @param e end position
   */
  private void sort(final int s, final int e) {
    if(e < 7) {
      for(int i = s; i < e + s; ++i) {
        for(int j = i; j > s && list[j - 1].diff(list[j]) > 0; j--) s(j, j - 1);
      }
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
    final ANode v = list[m];

    int a = s, b = a, c = s + e - 1, d = c;
    while(true) {
      while(b <= c) {
        final int h = list[b].diff(v);
        if(h > 0) break;
        if(h == 0) s(a++, b);
        ++b;
      }
      while(c >= b) {
        final int h = list[c].diff(v);
        if(h < 0) break;
        if(h == 0) s(c, d--);
        --c;
      }
      if(b > c) break;
      s(b++, c--);
    }

    final int n = s + e;
    int k = Math.min(a - s, b - a);
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
    return list[a].diff(list[b]) < 0 ?
      list[b].diff(list[c]) < 0 ? b : list[a].diff(list[c]) < 0 ? c : a :
      list[b].diff(list[c]) > 0 ? b : list[a].diff(list[c]) > 0 ? c : a;
  }

  /**
   * Swaps two entries.
   * @param a first position
   * @param b second position
   */
  private void s(final int a, final int b) {
    final ANode tmp = list[a];
    list[a] = list[b];
    list[b] = tmp;
  }
}
