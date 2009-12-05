package org.basex.data;

import java.io.IOException;
import java.util.Arrays;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.util.TokenBuilder;

/**
 * This class stores a single namespace node.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
final class NSNode {
  /** Children. */
  NSNode[] ch;
  /** Parent node. */
  NSNode par;
  /** References to Prefix/URI pairs. */
  int[] vals;
  /** Pre value. */
  int pre;

  /** Default constructor. */
  NSNode() {
    vals = new int[] {};
    ch = new NSNode[0];
  }

  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   * @param p parent reference
   * @throws IOException I/O exception
   */
  NSNode(final DataInput in, final NSNode p) throws IOException {
    par = p;
    pre = in.readNum();
    vals = in.readNums();
    final int cl = in.readNum();
    ch = new NSNode[cl];
    for(int c = 0; c < cl; c++) ch[c] = new NSNode(in, this);
  }

  /**
   * Writes a single node to disk.
   * @param out output stream
   * @throws IOException I/O exception
   */
  void write(final DataOutput out) throws IOException {
    out.writeNum(pre);
    out.writeNums(vals);
    out.writeNum(ch.length);
    for(final NSNode c : ch) c.write(out);
  }

  /**
   * Adds the specified child.
   * @param n child node
   */
  void add(final NSNode n) {
    add(n, ch.length);
  }

  /**
   * Adds the specified prefix and URI reference.
   * @param p prefix reference
   * @param u uri reference
   */
  void add(final int p, final int u) {
    final int s = vals.length;
    vals = Arrays.copyOf(vals, s + 2);
    vals[s] = p;
    vals[s + 1] = u;
  }

  // Requesting Namespaces ====================================================

  /**
   * Finds the closest namespace node for the specified pre value.
   * @param p pre value
   * @return node
   */
  NSNode find(final int p) {
    final int s = fnd(p);
    return s == -1 ? this : ch[s].pre == p ? ch[s] : ch[s].find(p);
  }

  /**
   * Returns the namespace URI reference for the specified prefix.
   * @param p prefix reference
   * @return u uri reference or 0
   */
  int uri(final int p) {
    for(int v = 0; v < vals.length; v += 2) if(vals[v] == p) return vals[v + 1];
    return 0;
  }

  /**
   * Deletes nodes in the specified range (p .. p + nr) and updates the
   * following pre values
   * @param p pre value
   * @param nr number of nodes to be deleted
   */
  void delete(final int p, final int nr) {
    int s = fnd(p);
    if(s == -1 || ch[s].pre != p) s++;
    int e = s;
    while(e < ch.length && p + nr > ch[e].pre) e++;
    if(s != e) {
      final NSNode[] nd = new NSNode[ch.length - e + s];
      System.arraycopy(ch, 0, nd, 0, s);
      System.arraycopy(ch, e, nd, s, ch.length - e);
      ch = nd;
    }
    update(s, -nr);
  }

  /**
   * Recursively modified the pre values by the specified offset.
   * @param s start position
   * @param o offset
   */
  private void update(final int s, final int o) {
    for(int c = s; c < ch.length; c++) {
      ch[c].pre += o;
      ch[c].update(0, o);
    }
  }

  /**
   * Adds a child at the specified position.
   * @param p pre value
   * @return node
   */
  private int fnd(final int p) {
    int l = 0, h = ch.length - 1;
    while(l <= h) { // binary search
      final int m = l + h >>> 1;
      final int v = ch[m].pre;
      if(v == p) return m;
      if(v < p) l = m + 1;
      else h = m - 1;
    }
    return l - 1;
  }

  /**
   * Adds a child to the specified position.
   * @param nd child
   * @param s inserting position
   */
  private void add(final NSNode nd, final int s) {
    final NSNode[] tmp = new NSNode[ch.length + 1];
    System.arraycopy(ch, 0, tmp, 0, s);
    tmp[s] = nd;
    ch = tmp;
  }

  // Printing Namespaces ======================================================

  /**
   * Prints the node structure for debugging purposes.
   * @param ns namespace reference
   * @return string
   */
  String print(final Namespaces ns) {
    final TokenBuilder tb = new TokenBuilder();
    print(tb, 0, ns);
    return tb.toString();
  }

  /**
   * Prints the node structure for debugging purposes.
   * @param tb string builder
   * @param ns namespace reference
   * @param l level
   */
  void print(final TokenBuilder tb, final int l, final Namespaces ns) {
    tb.add('\n');
    for(int i = 0; i < l; i++) tb.add("  ");
    tb.add("Pre[" + pre + "] ");
    for(int i = 0; i < vals.length; i += 2) {
      tb.add("xmlns");
      final byte[] p = ns.pref(vals[i]);
      if(p.length != 0) tb.add(':');
      tb.add(p);
      tb.add("='");
      tb.add(ns.uri(vals[i + 1]));
      tb.add("' ");
    }
    for(final NSNode c : ch) c.print(tb, l + 1, ns);
  }

  @Override
  public String toString() {
    return "Pre[" + pre + "]";
    //return Main.name(this) + "[pre:" + pre + "]";
  }
}
