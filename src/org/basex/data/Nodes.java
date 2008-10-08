package org.basex.data;

import java.io.IOException;

import org.basex.BaseX;
import org.basex.core.Context;
import org.basex.util.Array;
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

  /* Fulltext data (pre values and positions).
  public int[][] ftpos;
  /** Fulltext pointer values, linking pre values and query strings.
  public int[] ftpoin;*/
  
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
   * Compares the node set with the specified node set.
   * @param n node set
   * @return result of check
   */
  public boolean sameAs(final Nodes n) {
    if(this == n) return true;
    if(data != n.data || size != n.size) return false;
    for(int s = 0; s != size; s++) if(nodes[s] != n.nodes[s]) return false;
    return true;
  }

  /**
   * The specified pre value is added to or removed from the context set.
   * @param p pre value
   * @return self reference
   */
  public Nodes toggle(final int p) {
    int i = find(p);
    if(i >= 0) {
      // remove pre value
      if(i < --size) Array.move(nodes, i + 1, -1, size - i);
    } else {
      // insert pre value
      if(size == 0) nodes = new int[1];
      else if(size == nodes.length) nodes = Array.extend(nodes);
      i = -i - 1;
      Array.move(nodes, i, 1, size++ - i);
      nodes[i] = p;
    }
    return this;
  }

  /**
   * Setter for FTData. Used for visualization purpose.
   * 
   * @param ftprepos pre values and position values
   * @param ftpointer pointer for pre values
  public void setFTData(final int[][] ftprepos, final int[] ftpointer) {
    ftpos = ftprepos;
    ftpoin = ftpointer;
  }
   */
  
  /**
   * The specified pre value is added to or removed from the context set.
   * @param p pre value
   * @return self reference
   */
  public Nodes add(final int p) {
    int i = find(p);
    if(i < 0) {
      // insert pre value
      if(size == 0) nodes = new int[1];
      else if(size == nodes.length) nodes = Array.extend(nodes);
      i = -i - 1;
      Array.move(nodes, i, 1, size++ - i);
      nodes[i] = p;
    }
    return this;
  }

  /** {@inheritDoc} */
  public int size() {
    return size;
  }
  
  /**
   * Returns a copy of the node set.
   * @return copy
   */
  public Nodes copy() {
    return new Nodes(Array.finish(nodes, size), data);
  }
  
  /** {@inheritDoc} */
  public boolean same(final Result v) {
    if(!(v instanceof Nodes) || v.size() != size) return false;

    final Nodes n = (Nodes) v;
    if(data != n.data) return false;
    for(int c = 0; c < size; c++) if(n.nodes[c] != nodes[c]) return false;
    return true;
  }

  /** {@inheritDoc} */
  public void serialize(final Serializer ser) throws IOException {
    ser.open(size);
    for(int c = 0; c < size; c++) {
      if(ser.finished()) break;
      serialize(ser, c);
    }
    ser.close(size);
  }

  /** {@inheritDoc} */
  public void serialize(final Serializer ser, final int n) throws IOException {
    ser.openResult();
    ser.node(data, nodes[n], 0);
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
