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
  public final FTPosData ftpos;
  /** Root flag (nodes represent all document nodes of the database). */
  public boolean root;
  /** Root node. */
  public Data data;
  /** Pre values container. */
  public int[] pres;
  /** Sorted pre values. */
  public int[] sorted;

  /**
   * Constructor, specifying a database instance.
   * @param d data reference
   */
  public Nodes(final Data d) {
    this(new int[0], d);
  }

  /**
   * Constructor, specifying a single node and a database instance.
   * @param n single node
   * @param d data reference
   */
  public Nodes(final int n, final Data d) {
    this(new int[] { n }, d);
  }

  /**
   * Constructor, specifying a node set and a database instance.
   * @param n node set
   * @param d data reference
   */
  public Nodes(final int[] n, final Data d) {
    this(n, d, Prop.gui ? new FTPosData() : null);
  }

  /**
   * Constructor, specifying a node set, a database instance, and full-text
   * positions.
   * @param n node set
   * @param d data reference
   * @param ft ft position data
   */
  public Nodes(final int[] n, final Data d, final FTPosData ft) {
    data = d;
    ftpos = ft;
    set(n);
    if(d == null) throw Util.notExpected("No data available");
  }

  /**
   * Constructor, which should only used by test classes.
   * No database reference is specified.
   * @param n node set
   */
  public Nodes(final int[] n) {
    pres = n;
    ftpos = null;
  }

  /**
   * Copy constructor.
   * @param nds nodes to copy
   */
  public Nodes(final Nodes nds) {
    this(nds.pres.clone(), nds.data, nds.ftpos == null ? null : nds.ftpos.copy());
    root = nds.root;
    if(nds.sorted != null) sorted = nds.sorted.clone();
  }

  @Override
  public long size() {
    return pres.length;
  }

  @Override
  public boolean sameAs(final Result r) {
    final int s = pres.length;
    if(!(r instanceof Nodes) || r.size() != s) return false;
    final Nodes n = (Nodes) r;
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
    final int[] n = { p };
    set(contains(p) ? except(pres, n) : union(pres, n));
  }

  /**
   * Merges the specified array with the existing pre nodes.
   * @param p pre value
   */
  public void union(final int[] p) {
    set(union(pres, p));
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
      if(d == 0) ++b;
    }
    while(a != al) c.add(ai[a++]);
    while(b != bl) c.add(bi[b++]);
    return c.toArray();
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
      else ++b;
      if(d <= 0) ++a;
    }
    while(a != al) c.add(ai[a++]);
    return c.toArray();
  }

  /**
   * Sets the specified nodes.
   * @param n values
   */
  private void set(final int[] n) {
    pres = n;
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
  public void serialize(final Serializer ser, final int n) throws IOException {
    ser.serialize(new FTPosNode(data, pres[n], ftpos));
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
