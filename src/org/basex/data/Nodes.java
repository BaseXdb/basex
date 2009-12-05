package org.basex.data;

import java.io.IOException;
import java.util.Arrays;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Prop;
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
  /** Doc flag (all nodes refer to documents). */
  public boolean doc;
  /** Root Node. */
  public Data data;
  /** Full-text position data (for visualization). */
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
    this(new int[] {}, d);
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
  public Nodes(final int[] n, final Data d, final FTPosData ft) {
    data = d;
    ftpos = ft;
    set(n);
    if(d == null) Main.notexpected("No data available");
  }

  /**
   * Node Set constructor (used by test classes).
   * No data reference is defined by this constructor.
   * @param n node set
   */
  public Nodes(final int[] n) {
    nodes = n;
    size = nodes.length;
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
   * Adds or removes the specified pre node.
   * @param p pre value
   */
  public void toggle(final int p) {
    final int[] n = new int[] { p };
    set(contains(p) ? except(nodes, n) : union(nodes, n));
  }

  /**
   * Merges the specified array with the existing pre nodes.
   * @param p pre value
   */
  public void union(final int[] p) {
    set(union(nodes, p));
  }

  /**
   * Merges two sorted integer arrays via union.
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

  /**
   * Sets the specified nodes.
   * @param n values
   */
  private void set(final int[] n) {
    nodes = n;
    size = n.length;
    sorted = null;
  }

  /**
   * Creates a sorted node array. If the original array is already sorted,
   * the same reference is used.
   */
  private void sort() {
    if(sorted != null) return;
    int i = Integer.MIN_VALUE;
    for(final int n : nodes) {
      if(i > n) {
        sorted = Arrays.copyOf(nodes, size);
        Arrays.sort(sorted);
        return;
      }
      i = n;
    }
    sorted = nodes;
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
    final TokenBuilder tb = new TokenBuilder(Main.name(this) + '[');
    for(int i = 0; i < size; i++) {
      if(i > 0) tb.add(',');
      tb.add(nodes[i]);
    }
    tb.add(']');
    return tb.toString();
  }
}
