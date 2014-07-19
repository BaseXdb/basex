package org.basex.data;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class stores node references of a database in an ascending order.
 * Instances of this class are used in the {@link Context} class to
 * reference the currently used, marked, and copied nodes.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Nodes implements Result {
  /** Full-text position data (for visualization). */
  public FTPosData ftpos;
  /** Root flag (nodes represent all document nodes of the database). */
  public boolean root;
  /** Root node. */
  public Data data;
  /** Pre values. */
  public int[] pres;
  /** Sorted pre values. */
  public int[] sorted;

  /**
   * Constructor, specifying a database instance.
   * @param data data reference
   */
  public Nodes(final Data data) {
    this(new int[0], data);
  }

  /**
   * Constructor, specifying a single node and a database instance.
   * @param pre pre value
   * @param data data reference
   */
  public Nodes(final int pre, final Data data) {
    this(new int[] { pre }, data);
  }

  /**
   * Constructor, specifying a node set and a database instance.
   * @param pres pre values
   * @param data data reference
   */
  public Nodes(final int[] pres, final Data data) {
    this(pres, data, Prop.gui ? new FTPosData() : null);
  }

  /**
   * Constructor, specifying a node set, a database instance, and full-text
   * positions.
   * @param pres pre values
   * @param data data reference
   * @param ftpos ft position data
   */
  public Nodes(final int[] pres, final Data data, final FTPosData ftpos) {
    this.data = data;
    this.ftpos = ftpos;
    set(pres);
    if(data == null) throw Util.notExpected("No data available");
  }

  /**
   * Constructor, which should only used by test classes.
   * No database reference is specified.
   * @param pres pre values
   */
  public Nodes(final int[] pres) {
    this.pres = pres;
  }

  /**
   * Copy constructor.
   * @param nodes nodes to copy
   */
  public Nodes(final Nodes nodes) {
    this(nodes.pres.clone(), nodes.data, nodes.ftpos == null ? null : nodes.ftpos.copy());
    root = nodes.root;
    if(nodes.sorted != null) sorted = nodes.sorted.clone();
  }

  @Override
  public long size() {
    return pres.length;
  }

  @Override
  public boolean sameAs(final Result result) {
    final int s = pres.length;
    if(!(result instanceof Nodes) || result.size() != s) return false;
    final Nodes n = (Nodes) result;
    if(data != n.data) return false;
    for(int c = 0; c < s; ++c) if(n.pres[c] != pres[c]) return false;
    return ftpos == null || ftpos.sameAs(n.ftpos);
  }

  /**
   * Checks if the node set contains all root nodes of the data instance.
   * If yes, returns {@code null}.
   * @return self reference
   */
  public Nodes checkRoot() {
    final IntList docs = data.resources.docs();
    final int[] ps = pres;
    final int pl = ps.length;
    if(pl != docs.size()) return this;

    int c = -1;
    while(++c < pl && ps[c] == docs.get(c));
    return c < pl ? this : null;
  }

  /**
   * Checks if the specified node is contained in the array.
   * @param pre pre value
   * @return true if the node was found
   */
  public boolean contains(final int pre) {
    return find(pre) >= 0;
  }

  /**
   * Returns the position of the specified node or the negative value - 1 of
   * the position where it should have been found.
   * @param pre pre value
   * @return true if the node was found
   */
  public int find(final int pre) {
    sort();
    return Arrays.binarySearch(sorted, pre);
  }

  /**
   * Adds or removes the specified pre node.
   * @param pre pre value
   */
  public void toggle(final int pre) {
    final int[] n = { pre };
    set(contains(pre) ? except(pres, n) : union(pres, n));
  }

  /**
   * Merges the specified array with the existing pre nodes.
   * @param pre pre value
   */
  public void union(final int[] pre) {
    set(union(pres, pre));
  }

  /**
   * Merges two sorted integer arrays via union.
   * Note that the input arrays must be sorted.
   * @param pres1 first set
   * @param pres2 second set
   * @return resulting set
   */
  private static int[] union(final int[] pres1, final int[] pres2) {
    final int al = pres1.length, bl = pres2.length;
    final IntList c = new IntList();
    int a = 0, b = 0;
    while(a != al && b != bl) {
      final int d = pres1[a] - pres2[b];
      c.add(d <= 0 ? pres1[a++] : pres2[b++]);
      if(d == 0) ++b;
    }
    while(a != al) c.add(pres1[a++]);
    while(b != bl) c.add(pres2[b++]);
    return c.toArray();
  }

  /**
   * Subtracts the second from the first array.
   * Note that the input arrays must be sorted.
   * @param pres1 first set
   * @param pres2 second set
   * @return resulting set
   */
  private static int[] except(final int[] pres1, final int[] pres2) {
    final int al = pres1.length, bl = pres2.length;
    final IntList c = new IntList();
    int a = 0, b = 0;
    while(a != al && b != bl) {
      final int d = pres1[a] - pres2[b];
      if(d < 0) c.add(pres1[a]);
      else ++b;
      if(d <= 0) ++a;
    }
    while(a != al) c.add(pres1[a++]);
    return c.toArray();
  }

  /**
   * Sets the specified nodes.
   * @param nodes values
   */
  private void set(final int[] nodes) {
    pres = nodes;
    sorted = null;
  }

  /**
   * Creates a sorted node array. If the original array is already sorted,
   * the same reference is used.
   */
  private void sort() {
    if(sorted != null) return;
    int i = Integer.MIN_VALUE;
    for(final int n : pres) {
      if(i > n) {
        sorted = Arrays.copyOf(pres, pres.length);
        Arrays.sort(sorted);
        return;
      }
      i = n;
    }
    sorted = pres;
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    for(int c = 0; c < pres.length && !ser.finished(); ++c) serialize(ser, c);
  }

  @Override
  public void serialize(final Serializer ser, final int pre) throws IOException {
    ser.serialize(new FTPosNode(data, pres[pre], ftpos));
  }

  @Override
  public ArrayOutput serialize() throws IOException {
    final ArrayOutput ao = new ArrayOutput();
    serialize(Serializer.get(ao));
    return ao;
  }

  @Override
  public String toString() {
    try {
      return serialize().toString();
    } catch(final IOException ex) {
      throw Util.notExpected(ex);
    }
  }
}
