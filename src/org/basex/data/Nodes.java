package org.basex.data;

import java.io.IOException;
import java.util.Arrays;
import org.basex.BaseX;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.TokenBuilder;

/**
 * This class stores node references of a database in an ascending order.
 * Instances of this class are used in the {@link Context} class to
 * reference the currently used nodes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Nodes implements Result {
  /** Root Node. */
  public Data data;
  /** Full-text position data. */
  public FTPosData ftpos;
  /** Pre values container. */
  public int[] nodes;
  /** Sorted pre values. */
  public int[] sorted;
  /** Number of stored nodes. */
  private int size;

  /**
   * Node Set constructor.
   * @param d data reference
   */
  public Nodes(final Data d) {
    this(new int[] { }, d);
  }

  /**
   * Node Set constructor.
   * @param d data reference
   * @param ft ft position data
   */
  public Nodes(final Data d, final FTPosData ft) {
    this(new int[] { }, d, ft);
  }

  /**
   * Node Set constructor.
   * @param n single node
   * @param d data reference
   */
  public Nodes(final int n, final Data d) {
    this(new int[] { n }, d);
  }

  /**
   * Node Set constructor.
   * @param n node set
   * @param d data reference
   */
  public Nodes(final int[] n, final Data d) {
    this(n, d, Prop.gui ? new FTPosData() : null);
  }

  /**
   * Node Set constructor.
   * @param n node set
   * @param d data reference
   * @param ft ft position data
   */
  Nodes(final int[] n, final Data d, final FTPosData ft) {
    if(d == null) BaseX.notexpected("No data available");
    data = d;
    ftpos = ft;
    set(n);
  }

  /**
   * Sets the specified values.
   * @param n values
   */
  public void set(final int[] n) {
    nodes = n;
    size = n.length;
    sorted = null;
  }

  public int size() {
    return size;
  }

  public boolean same(final Result v) {
    if(!(v instanceof Nodes) || v.size() != size) return false;
    final Nodes n = (Nodes) v;
    if(data != n.data) return false;
    for(int c = 0; c < size; c++) if(n.nodes[c] != nodes[c]) return false;
    return ftpos == null || ftpos.same(n.ftpos);
  }

  /**
   * Checks if the specified node is contained in the array.
   * @param p pre value
   * @return true if the node was found
   */
  public boolean contains(final int p) {
    return find(p) >= 0;
  }

  /**
   * Returns the position of the specified node or the negative value - 1 of
   * the position where it should have been found.
   * @param p pre value
   * @return true if the node was found
   */
  public int find(final int p) {
    sort();
    return Arrays.binarySearch(sorted, p);
  }

  /**
   * Creates a sorted node array. If the original array is already sorted,
   * the same reference is used.
   */
  public void sort() {
    if(sorted != null) return;
    int i = Integer.MIN_VALUE;
    for(final int n : nodes) {
      if(i > n) {
        sorted = Array.finish(nodes, size);
        Arrays.sort(sorted);
        return;
      }
      i = n;
    }
    sorted = nodes;
  }

  /**
   * The specified pre value is added to or removed from the node array.
   * @param p pre value
   */
  public void toggle(final int p) {
    final int[] n = new int[] { p };
    set(contains(p) ? except(nodes, n) : union(nodes, n));
  }

  /**
   * The specified nodes are merged.
   * @param p pre value
   */
  public void union(final int[] p) {
    set(union(nodes, p));
  }

  /**
   * Merges two integer arrays via union.
   * Note that the input arrays must be sorted.
   * @param ai first set
   * @param bi second set
   * @return resulting set
   */
  private static int[] union(final int[] ai, final int[] bi) {
    final int al = ai.length, bl = bi.length;
    final IntList c = new IntList();
    int a = 0, b = 0;
    while(a != al && b != bl) {
      final int d = ai[a] - bi[b];
      c.add(d <= 0 ? ai[a++] : bi[b++]);
      if(d == 0) b++;
    }
    while(a != al) c.add(ai[a++]);
    while(b != bl) c.add(bi[b++]);
    return c.finish();
  }

  /**
   * Subtracts the second from the first array.
   * Note that the input arrays must be sorted.
   * @param ai first set
   * @param bi second set
   * @return resulting set
   */
  private static int[] except(final int[] ai, final int[] bi) {
    final int al = ai.length, bl = bi.length;
    final IntList c = new IntList();
    int a = 0, b = 0;
    while(a != al && b != bl) {
      final int d = ai[a] - bi[b];
      if(d < 0) c.add(ai[a]);
      else b++;
      if(d <= 0) a++;
    }
    while(a != al) c.add(ai[a++]);
    return c.finish();
  }

  public void serialize(final Serializer ser) throws IOException {
    for(int c = 0; c < size && !ser.finished(); c++) serialize(ser, c);
  }

  public void serialize(final Serializer ser, final int n) throws IOException {
    ser.openResult();
    ser.node(data, nodes[n], ftpos);
    ser.closeResult();
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(getClass().getSimpleName());
    tb.add('[');
    for(int i = 0; i < size; i++) {
      if(i > 0) tb.add(',');
      tb.add(nodes[i]);
    }
    tb.add(']');
    return tb.toString();
  }
}
