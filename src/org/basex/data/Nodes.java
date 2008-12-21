package org.basex.data;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Context;
import org.basex.util.IntList;
import org.basex.util.TokenBuilder;

/**
 * This is a container for context nodes. Instances of this class are stored
 * in the {@link Context} class to reference the currently used nodes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Nodes implements Result {
  /** Pre values container. */
  public int[] nodes;
  /** Root Node. */
  public Data data;
  /** Number of stored nodes. */
  public int size;
  
  /**
   * Node Set constructor.
   * @param d data reference
   */
  public Nodes(final Data d) {
    this(new int[] { }, d);
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
    if(d == null) BaseX.notexpected("No data available");
    nodes = n;
    size = n.length;
    data = d;
  }
  
  /**
   * Returns the position of the specified node or the negative value - 1 of
   * the position where it should have been found.
   * @param p pre value
   * @return true if the node was found
   */
  public int find(final int p) {
    for(int s = 0; s < size; s++) {
      if(nodes[s] == p) return s;
      if(nodes[s] > p) return -s - 1;
    }
    return -size - 1;
  }

  /**
   * The specified pre value is added to or removed from the context set.
   * @param p pre value
   */
  public void toggle(final int p) {
    final int[] n = new int[] { p };
    nodes = find(p) < 0 ? union(nodes, n) : except(nodes, n);
    size = nodes.length;
  }
  
  /**
   * The specified nodes are merged.
   * @param p pre value
   */
  public void union(final int[] p) {
    nodes = union(nodes, p);
    size = nodes.length;
  }

  /**
   * Merges two integer arrays via union.
   * Note that the input arrays must be sorted.
   * @param ai first set
   * @param bi second set
   * @return resulting set
   */
  public static int[] union(final int[] ai, final int[] bi) {
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
   * Intersects two integer arrays via union.
   * Note that the input arrays must be sorted.
   * @param ai first set
   * @param bi second set
   * @return resulting set
   */
  public static int[] intersect(final int[] ai, final int[] bi) {
    final int al = ai.length, bl = bi.length;
    final IntList c = new IntList();
    for(int a = 0, b = 0; a != al && b != bl;) {
      final int d = ai[a] - bi[b];
      if(d == 0) c.add(ai[a]);
      if(d > 0) b++;
      else a++;
    }
    return c.finish();
  }

  /**
   * Subtracts the second from the first array.
   * Note that the input arrays must be sorted.
   * @param ai first set
   * @param bi second set
   * @return resulting set
   */
  public static int[] except(final int[] ai, final int[] bi) {
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

  public long size() {
    return size;
  }
  
  /**
   * Returns a copy of the node set.
   * @return copy
   */
  public Nodes copy() {
    return new Nodes(nodes, data);
  }
  
  public boolean same(final Result v) {
    if(!(v instanceof Nodes) || v.size() != size) return false;

    final Nodes n = (Nodes) v;
    if(data != n.data) return false;
    for(int c = 0; c < size; c++) if(n.nodes[c] != nodes[c]) return false;
    return true;
  }

  public void serialize(final Serializer ser) throws IOException {
    ser.open(size);
    for(int c = 0; c < size; c++) {
      if(ser.finished()) break;
      serialize(ser, c);
    }
    ser.close(size);
  }

  public void serialize(final Serializer ser, final int n) throws IOException {
    ser.openResult();
    ser.node(data, nodes[n]);
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
