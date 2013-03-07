package org.basex.query.iter;

import java.util.*;

import org.basex.data.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * This class can be used to build new node sequences.
 * At the same time, it serves as an iterator.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class NodeSeqBuilder extends AxisIter {
  /** Node container. */
  public ANode[] nodes;
  /** Number of nodes. */
  private int size;
  /** Current iterator position. */
  private int pos = -1;
  /** Sort flag. */
  private boolean sort;
  /** Check incoming nodes for potential duplicates and unsorted entries. */
  private boolean check;

  /**
   * Constructor.
   */
  public NodeSeqBuilder() {
    nodes = new ANode[1];
  }

  /**
   * Lightweight constructor, assigning the specified array of sorted nodes.
   * @param it node array
   * @param s size
   */
  public NodeSeqBuilder(final ANode[] it, final int s) {
    nodes = it;
    size = s;
  }

  /**
   * Checks all nodes for potential duplicates and their orderedness.
   * @return self reference
   */
  public NodeSeqBuilder check() {
    check = true;
    return this;
  }

  /**
   * Returns the specified node.
   * @param i node offset
   * @return node
   */
  public ANode get(final int i) {
    return nodes[i];
  }

  /**
   * Deletes a value at the specified position.
   * @param p deletion position
   */
  public void delete(final int p) {
    Array.move(nodes, p + 1, -1, --size - p);
  }

  /**
   * Adds a node.
   * @param n node to be added
   */
  public void add(final ANode n) {
    if(size == nodes.length) {
      final ANode[] tmp = new ANode[Array.newSize(size)];
      System.arraycopy(nodes, 0, tmp, 0, size);
      nodes = tmp;
    }
    if(check && !sort && size != 0) sort = nodes[size - 1].diff(n) > 0;
    nodes[size++] = n;
  }

  @Override
  public boolean reset() {
    pos = -1;
    return true;
  }

  @Override
  public ANode next() {
    if(check) sort(sort);
    return ++pos < size ? nodes[pos] : null;
  }

  @Override
  public ANode get(final long i) {
    return i < size ? nodes[(int) i] : null;
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
    if(check) sort(sort);
    return Seq.get(nodes, size, NodeType.NOD);
  }

  /**
   * Checks if binary search can be applied to this iterator, i.e.
   * if all nodes are {@link DBNode} references and refer to the same database.
   * @return result of check
   */
  public boolean dbnodes() {
    if(check) sort(sort);

    final Data data = size > 0 ? nodes[0].data() : null;
    if(data == null) return false;
    for(int s = 1; s < size; ++s) if(data != nodes[s].data()) return false;
    return true;
  }

  /**
   * Checks if the iterator contains a database node with the specified pre value.
   * @param n node to be found
   * @param db indicates if all nodes are sorted {@link DBNode} references
   * @return position, or {@code -1}
   */
  public int indexOf(final ANode n, final boolean db) {
    if(db) return n instanceof DBNode ?
        Math.max(binarySearch((DBNode) n, 0, size), -1) : -1;
    for(int s = 0; s < size(); ++s) if(nodes[s].is(n)) return s;
    return -1;
  }

  /**
   * Performs a binary search on the given range of this sequence iterator,
   * assuming that all nodes are {@link DBNode}s from the same {@link Data}
   * instance (i.e., {@link #dbnodes()} returns {@code true}).
   * @param n node to find
   * @param start start of the search interval
   * @param length length of the search interval
   * @return position of the item or {@code -insertPosition - 1} if not found
   */
  public int binarySearch(final DBNode n, final int start, final int length) {
    if(size == 0 || n.data != nodes[0].data()) return -start - 1;
    int l = start, r = start + length - 1;
    while(l <= r) {
      final int m = l + r >>> 1;
      final int npre = ((DBNode) nodes[m]).pre;
      if(npre == n.pre) return m;
      if(npre < n.pre) l = m + 1;
      else r = m - 1;
    }
    return -(l + 1);
  }

  /**
   * Sorts the nodes, if necessary.
   * @return self reference
   */
  public NodeSeqBuilder sort() {
    if(check) sort(sort);
    return this;
  }

  /**
   * Sorts the nodes.
   * @param force force sort
   */
  private void sort(final boolean force) {
    check = false;
    if(size > 1) {
      // sort arrays and remove duplicates
      if(force) sort(0, size);

      // remove duplicates and merge scores
      int i = 1;
      for(int j = 1; j < size; ++j) {
        while(j < size && nodes[i - 1].is(nodes[j])) {
          nodes[i - 1].score(Math.max(nodes[j++].score(), nodes[i - 1].score()));
        }
        if(j == size) break;
        nodes[i++] = nodes[j];
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
        for(int j = i; j > s && nodes[j - 1].diff(nodes[j]) > 0; j--) s(j, j - 1);
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
    return nodes[a].diff(nodes[b]) < 0 ?
      nodes[b].diff(nodes[c]) < 0 ? b : nodes[a].diff(nodes[c]) < 0 ? c : a :
      nodes[b].diff(nodes[c]) > 0 ? b : nodes[a].diff(nodes[c]) > 0 ? c : a;
  }

  /**
   * Swaps two entries.
   * @param a first position
   * @param b second position
   */
  private void s(final int a, final int b) {
    final ANode tmp = nodes[a];
    nodes[a] = nodes[b];
    nodes[b] = tmp;
  }

  @Override
  public String toString() {
    return Util.name(this) + Arrays.toString(Arrays.copyOf(nodes, size));
  }

  /**
   * Creates a copy of this sequence builder.
   * @return copy
   */
  public NodeSeqBuilder copy() {
    final NodeSeqBuilder b = new NodeSeqBuilder(nodes.clone(), size);
    b.pos = pos;
    b.sort = sort;
    b.check = check;
    return b;
  }
}
