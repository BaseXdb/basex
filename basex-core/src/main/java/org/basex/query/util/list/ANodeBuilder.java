package org.basex.query.util.list;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.list.*;

/**
 * Resizable-array implementation for nodes. The stored nodes will be sorted and duplicates will
 * before removed before they are returned as value or via an iterator.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class ANodeBuilder extends ObjectList<ANode, ANodeBuilder> {
  /** State. */
  private enum State {
    /** Initial step.     */ BUILD,
    /** Sorting required. */ SORT,
    /** List is sorted.   */ SORTED,
  }
  /** Current state. */
  private State state = State.BUILD;
  /** Indicates if binary search can be used. */
  private boolean dbnodes;

  /**
   * Constructor.
   */
  public ANodeBuilder() {
    super(new ANode[1]);
  }

  @Override
  public ANodeBuilder add(final ANode element) {
    if(size != 0 && state != State.SORT) {
      final int d = element.diff(list[size - 1]);
      if(d == 0) return this;
      if(d < 0) {
        state = State.SORT;
        dbnodes = false;
      }
    }
    return super.add(element);
  }

  @Override
  public Iterator<ANode> iterator() {
    check();
    return super.iterator();
  }

  /**
   * Returns an iterator over the items in this list.
   * The list must not be modified after the iterator has been requested.
   * @return the iterator
   */
  public BasicNodeIter iter() {
    check();
    return new BasicNodeIter() {
      int pos;

      @Override
      public ANode next() {
        return pos < size ? list[pos++] : null;
      }

      @Override
      public ANode get(final long i) {
        return list[(int) i];
      }

      @Override
      public long size() {
        return size;
      }

      @Override
      public Value value(final QueryContext qc) {
        return ANodeBuilder.this.value();
      }
    };
  }

  /**
   * Returns all nodes as value.
   * The list must not be modified after the iterator has been requested.
   * @return the iterator
   */
  public Value value() {
    return ValueBuilder.value(list, size, NodeType.NOD);
  }

  /**
   * Sorts the nodes and finalizes the list.
   */
  public void check() {
    sort();
    final int s = size;
    final ANode[] nodes = list;
    final Data data = s > 0 ? nodes[0].data() : null;
    if(data == null) return;
    for(int l = 1; l < s; ++l) {
      if(data != nodes[l].data()) return;
    }
    dbnodes = true;
  }

  /**
   * Indicate if binary search can be used.
   * This is the case if all nodes are {@link DBNode}s and refer to the same database.
   * @return result of check
   */
  public boolean dbnodes() {
    check();
    return dbnodes;
  }

  @Override
  public boolean delete(final ANode node) {
    if(dbnodes) {
      final int p = binarySearch((DBNode) node, 0, size);
      if(p < 0) return false;
      remove(p);
      return true;
    }
    return super.delete(node);
  }

  @Override
  public boolean contains(final ANode node) {
    return dbnodes ? node instanceof DBNode && binarySearch((DBNode) node, 0, size) > -1 :
      super.contains(node);
  }

  @Override
  public boolean eq(final ANode node1, final ANode node2) {
    return node1.is(node2);
  }

  /**
   * Performs a binary search on the given range of this sequence iterator.
   * This works if {@link #dbnodes} is set to true.
   * @param node node to find
   * @param start start of the search interval
   * @param length length of the search interval
   * @return position of the item or {@code -insertPosition - 1} if not found
   */
  public int binarySearch(final DBNode node, final int start, final int length) {
    if(size == 0 || node.data() != list[0].data()) return -start - 1;
    int l = start, r = start + length - 1;
    final ANode[] nodes = list;
    while(l <= r) {
      final int m = l + r >>> 1, mpre = ((DBNode) nodes[m]).pre(), npre = node.pre();
      if(mpre == npre) return m;
      if(mpre < npre) l = m + 1;
      else r = m - 1;
    }
    return -(l + 1);
  }

  /**
   * Sorts the nodes.
   */
  private void sort() {
    if(state == State.SORTED) return;

    final int s = size;
    if(s > 1) {
      if(state == State.SORT) sort(0, s);

      // remove duplicates
      int i = 1;
      final ANode[] nodes = list;
      for(int j = 1; j < s; ++j) {
        while(j < s && nodes[i - 1].is(nodes[j])) j++;
        if(j < s) nodes[i++] = nodes[j];
      }
      size = i;
    }
    state = State.SORTED;
  }

  /**
   * Recursively sorts the specified items via QuickSort
   * (derived from Java's sort algorithms).
   * @param s start position
   * @param e end position
   */
  private void sort(final int s, final int e) {
    final ANode[] nodes = list;
    if(e < 7) {
      for(int i = s; i < e + s; ++i) {
        for(int j = i; j > s && nodes[j - 1].diff(nodes[j]) > 0; j--) s(j, j - 1);
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
    final ANode v = nodes[m];

    int a = s, b = a, c = s + e - 1, d = c;
    while(true) {
      while(b <= c) {
        final int h = nodes[b].diff(v);
        if(h > 0) break;
        if(h == 0) s(a++, b);
        ++b;
      }
      while(c >= b) {
        final int h = nodes[c].diff(v);
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
    final ANode[] nodes = list;
    final ANode nodeA = nodes[a], nodeB = nodes[b], nodeC = nodes[c];
    return nodeA.diff(nodeB) < 0 ?
      nodeB.diff(nodeC) < 0 ? b : nodeA.diff(nodeC) < 0 ? c : a :
      nodeB.diff(nodeC) > 0 ? b : nodeA.diff(nodeC) > 0 ? c : a;
  }

  /**
   * Swaps two entries.
   * @param a first position
   * @param b second position
   */
  private void s(final int a, final int b) {
    final ANode[] nodes = list;
    final ANode tmp = nodes[a];
    nodes[a] = nodes[b];
    nodes[b] = tmp;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj == this || obj instanceof ANodeBuilder && super.equals(obj);
  }

  @Override
  protected ANode[] newList(final int s) {
    return new ANode[s];
  }
}
