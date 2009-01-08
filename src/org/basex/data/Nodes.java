package org.basex.data;

import java.io.IOException;
import java.util.Arrays;
import org.basex.BaseX;
import org.basex.core.Context;
import org.basex.util.Array;
import org.basex.util.IntArrayList;
import org.basex.util.IntList;
import org.basex.util.TokenBuilder;

/**
 * This class stores node references of a database in an ascending order.
 * Instances of this class are used in the {@link Context} class to
 * reference the currently used nodes.
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
  /** Fulltext position values. */
  public int[][] pos;
  /** Fulltext position pointer. */
  public int[][] poi;
  
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
    this (n, d, null, null);
  }

  /**
   * Node Set constructor.
   * @param n node set
   * @param d data reference
   * @param po ftpos values
   * @param pi fulltext position pointer
   */
  public Nodes(final int[] n, final Data d, final int[][] po, 
      final int[][] pi) {
    if(d == null) BaseX.notexpected("No data available");
    nodes = n;
    size = n.length;
    data = d;
    pos = po;
    poi = pi;
  }

  
  /**
   * Returns the position of the specified node or the negative value - 1 of
   * the position where it should have been found.
   * @param p pre value
   * @return true if the node was found
   */
  public boolean contains(final int p) {
    return Arrays.binarySearch(nodes, p) >= 0;
  }

  /**
   * The specified pre value is added to or removed from the context set.
   * @param p pre value
   */
  public void toggle(final int p) {
    final int[] n = new int[] { p };
    nodes = contains(p) ? except(nodes, n) : union(nodes, n);
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
   * Get all marked nodes out of marked, which are in the current nodes.
   * 
   * @param marked marked nodes
   * @return all marked nodes from this object
   */
  public Nodes getAllMarked(final Nodes marked) {
    if (marked == null) return null;
    if (nodes == null || nodes.length == 0) return marked;
    final IntList pre = new IntList();
    final IntArrayList posi = new IntArrayList();
    final IntArrayList poin = new IntArrayList();
    int n0 = 0, n1 = 0;
    while (n0 < nodes.length && n1 < marked.nodes.length) {
      if (nodes[n0] < marked.nodes[n1]) {
        n0++;
      } else if (nodes[n0] > marked.nodes[n1]) {
        n1++;
      } else {
        pre.add(marked.nodes[n1]);
        posi.add(marked.pos[n1]);
        poin.add(marked.poi[n1++]);
        n0++;
      }
    }
    
    return new Nodes(pre.finish(), marked.data, posi.finish(), poin.finish());
    
    
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
  
  public boolean same(final Result v) {
    if(!(v instanceof Nodes) || v.size() != size) 
      return false;

    final Nodes n = (Nodes) v;
    if(data != n.data) 
      return false;
    final boolean ftd1 = n.poi != null && n.pos != null;
    final boolean ftd2 = pos != null && poi != null;
    for(int c = 0; c < size; c++) 
      //if(n.nodes[c] != nodes[c]) return false; 
      if(n.nodes[c] != nodes[c] || ftd1 != ftd2 
          || (ftd1 && !Array.eq(n.poi[c], poi[c]) 
          && !Array.eq(n.pos[c], pos[c]))) 
        return false;
    return true;
  }

  public void serialize(final Serializer ser) throws IOException {
    for(int c = 0; c < size && !ser.finished(); c++) serialize(ser, c);
  }

  public void serialize(final Serializer ser, final int n) throws IOException {
    ser.openResult();
    ser.node(data, nodes[n], pos != null ? pos[n] : null, 
        poi != null ? poi[n] : null);
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
