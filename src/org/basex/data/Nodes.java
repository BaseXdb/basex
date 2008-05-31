package org.basex.data;

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
  /** Root Node. */
  public Data data;
  /** Pre values container. */
  public int[] pre;
  /** Number of stored nodes. */
  public int size;
  /** Fulltext data (pre values and positions). */
  public int[][] ftpos;
  /** Fulltext pointer values, linking pre values and query strings. */
  public int[] ftpoin;
  /** Fulltext search string from query. **/
  public byte[][] ftss;
  
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
    if(d == null) throw new RuntimeException("No data available.");
    pre = n;
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
      if(pre[s] == p) return s;
      if(pre[s] > p) return -s - 1;
    }
    return -size - 1;
  }

  /**
   * Compares the node set with the specified node set.
   * @param n node set
   * @return result of check
   */
  public boolean sameAs(final Nodes n) {
    if(data != n.data) return false;
    if(size != n.size) return false;
    for(int s = 0; s < size; s++) {
      if(pre[s] != n.pre[s]) return false;
    }
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
      if(i < --size) Array.move(pre, i + 1, -1, size - i);
    } else {
      // insert pre value
      if(size == 0) pre = new int[1];
      else if(size == pre.length) pre = Array.extend(pre);
      i = -i - 1;
      Array.move(pre, i, 1, size++ - i);
      pre[i] = p;
    }
    return this;
  }

  /**
   * Setter for FTData. Used for visualization purpose.
   * 
   * @param ftprepos pre values and position values
   * @param ftpointer pointer for pre values
   */
  public void setFTData(final int[][] ftprepos, final int[] ftpointer) {
    ftpos = ftprepos;
    ftpoin = ftpointer;
  }
  
  /**
   * The specified pre value is added to or removed from the context set.
   * @param p pre value
   * @return self reference
   */
  public Nodes add(final int p) {
    int i = find(p);
    if(i < 0) {
      // insert pre value
      if(size == 0) pre = new int[1];
      else if(size == pre.length) pre = Array.extend(pre);
      i = -i - 1;
      Array.move(pre, i, 1, size++ - i);
      pre[i] = p;
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
    return new Nodes(Array.finish(pre, size), data);
  }
  
  /** {@inheritDoc} */
  public boolean same(final Result v) {
    if(!(v instanceof Nodes) || v.size() != size) return false;

    final Nodes n = (Nodes) v;
    if(data != n.data) return false;
    for(int c = 0; c < size; c++) if(n.pre[c] != pre[c]) return false;
    return true;
  }

  /** {@inheritDoc} */
  public void serialize(final Serializer ser) throws Exception {
    ser.open(size);
    for(int c = 0; c < size; c++) {
      if(ser.finished()) break;
      ser.openResult();
      ser.xml(data, pre[c]);
      ser.closeResult();
    }
    ser.close(size);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(getClass().getSimpleName());
    tb.add('[');
    for(int i = 0; i < size; i++) {
      if(i > 0) tb.add(',');
      tb.add(pre[i]);
    }
    tb.add(']');
    return tb.toString();
  }
}
