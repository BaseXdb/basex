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
 * This class stores database nodes in an ascending order.
 * Instances of this class are used in the {@link Context} class to
 * reference the currently used, marked, and copied nodes.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DBNodes implements Result {
  /** Pre values comprise all documents of the database. */
  public boolean all;
  /** Root node. */
  public final Data data;
  /** Pre values. */
  public int[] pres;
  /** Sorted pre values. */
  public int[] sorted;
  /** Full-text position data (for visualization). */
  private FTPosData ftpos;

  /**
   * Constructor, specifying a database and pre values.
   * @param data data reference
   * @param pres pre values
   */
  public DBNodes(final Data data, final int... pres) {
    this(data, null, pres);
  }

  /**
   * Constructor, specifying a database, pre values and full-text positions.
   * @param data data reference
   * @param ftpos ft position data
   * @param pres pre values
   */
  public DBNodes(final Data data, final FTPosData ftpos, final int... pres) {
    this.data = data;
    this.ftpos = ftpos;
    this.pres = pres;
  }

  @Override
  public long size() {
    return pres.length;
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof DBNodes)) return false;
    final DBNodes n = (DBNodes) obj;
    final int[] ps = pres, ps2 = n.pres;
    final int pl = ps.length;
    if(pl != ps2.length || data != n.data) return false;
    for(int c = 0; c < pl; ++c) if(ps2[c] != ps[c]) return false;
    return ftpos == null ? n.ftpos == null : ftpos.equals(n.ftpos);
  }

  /**
   * Returns {@code null} if the pre values comprise all documents of the database.
   * @return self reference or {@code null}
   */
  public DBNodes discardDocs() {
    if(all) return null;

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
   * Returns full-text position data.
   * @return position data
   */
  public FTPosData ftpos() {
    return ftpos;
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
    final IntList il = new IntList();
    int a = 0, b = 0;
    while(a != al && b != bl) {
      final int d = pres1[a] - pres2[b];
      il.add(d <= 0 ? pres1[a++] : pres2[b++]);
      if(d == 0) ++b;
    }
    while(a != al) il.add(pres1[a++]);
    while(b != bl) il.add(pres2[b++]);
    return il.finish();
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
    final IntList il = new IntList();
    int a = 0, b = 0;
    while(a != al && b != bl) {
      final int d = pres1[a] - pres2[b];
      if(d < 0) il.add(pres1[a]);
      else ++b;
      if(d <= 0) ++a;
    }
    while(a != al) il.add(pres1[a++]);
    return il.finish();
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
    final int pl = pres.length;
    for(int pre = 0; pre < pl && !ser.finished(); pre++) serialize(ser, pre);
  }

  @Override
  public void serialize(final Serializer ser, final int pre) throws IOException {
    ser.serialize(new FTPosNode(data, pres[pre], ftpos));
  }

  @Override
  public String serialize() throws IOException {
    final ArrayOutput ao = new ArrayOutput();
    serialize(Serializer.get(ao));
    return ao.toString();
  }

  @Override
  public String toString() {
    try {
      return serialize();
    } catch(final IOException ex) {
      throw Util.notExpected(ex);
    }
  }
}
