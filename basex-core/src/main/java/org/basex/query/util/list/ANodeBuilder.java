package org.basex.query.util.list;

import java.util.*;

import org.basex.data.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.list.*;

/**
 * Resizable-array implementation for nodes. The stored nodes will be sorted and duplicates will
 * before removed before they are returned as value or via an iterator.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ANodeBuilder extends ObjectList<ANode, ANodeBuilder> {
  /** Distinct document order. */
  private boolean ddo = true;
  /** Shared database (can be {@code null}). */
  private Data data;

  /**
   * Constructor.
   */
  public ANodeBuilder() {
    super(new ANode[1]);
  }

  @Override
  public ANodeBuilder add(final ANode node) {
    if(isEmpty()) {
      // empty list: assign initial database reference (may be null)
      data = node.data();
    } else {
      // check if new node is in same database
      final Data dt = data;
      if(dt != null && dt != node.data()) data = null;

      // check if new node is identical to last one, or destroys distinct document order
      if(ddo) {
        final int d = node.diff(peek());
        if(d == 0) return this;
        if(d < 0) ddo = false;
      }
    }
    return super.add(node);
  }

  /**
   * Returns a value with the type of the given expression and invalidates the internal array.
   * Warning: the function must only be called if the builder is discarded afterwards.
   * @param expr expression
   * @return the iterator
   */
  public Value value(final Expr expr) {
    ddo();

    // return empty sequence or single node
    final int sz = size;
    if(sz == 0) return Empty.VALUE;
    if(sz == 1) return list[0];

    // check if compact sequence with single database instance can be created
    if(data != null) {
      final ANode parent = list[0].parent();
      if(parent == null || parent.data() == data) {
        final int[] pres = new int[sz];
        for(int l = 0; l < sz; l++) pres[l] = ((DBNode) list[l]).pre();
        return DBNodeSeq.get(pres, data, expr);
      }
    }

    // otherwise, create default sequence
    return ItemSeq.get(finish(), sz, NodeType.NODE.refine(expr));
  }

  /**
   * Sorts the nodes and removes distinct nodes.
   */
  public void ddo() {
    if(ddo) return;

    final int sz = size;
    if(sz > 1) {
      sort(0, sz);

      // remove duplicates
      int i = 1;
      final ANode[] nodes = list;
      for(int j = 1; j < sz; ++j) {
        while(j < sz && nodes[i - 1].is(nodes[j])) j++;
        if(j < sz) nodes[i++] = nodes[j];
      }
      size(i);
    }
    ddo = true;
  }

  /**
   * Returns the shared database.
   * @return database or {@code null}
   */
  public Data data() {
    return data;
  }

  @Override
  public boolean removeAll(final ANode node) {
    if(data != null && ddo && node instanceof DBNode) {
      final int p = binarySearch((DBNode) node, 0, size);
      if(p < 0) return false;
      remove(p);
      return true;
    }
    return super.removeAll(node);
  }

  @Override
  public boolean contains(final ANode node) {
    if(data != null && ddo) {
      return node instanceof DBNode && binarySearch((DBNode) node, 0, size) > -1;
    }
    return super.contains(node);
  }

  /**
   * Performs a binary search on the given range of this sequence iterator.
   * This works if {@link #data} is assigned.
   * @param node node to find
   * @param start start of the search interval
   * @param length length of the search interval
   * @return position of the item or {@code -insertPosition - 1} if not found
   */
  public int binarySearch(final DBNode node, final int start, final int length) {
    if(node.data() != data) return -start - 1;
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

  @Override
  public Iterator<ANode> iterator() {
    ddo();
    return super.iterator();
  }

  @Override
  protected ANode[] newArray(final int s) {
    return new ANode[s];
  }

  @Override
  public boolean equals(final ANode node1, final ANode node2) {
    return node1.is(node2);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj == this || obj instanceof ANodeBuilder && super.equals(obj);
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Recursively sorts the specified items via QuickSort (derived from Java's sort algorithms).
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
}
