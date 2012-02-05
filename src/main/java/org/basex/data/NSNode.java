package org.basex.data;

import static org.basex.core.Text.*;
import java.io.IOException;
import java.util.Arrays;

import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.TokenBuilder;
import org.basex.util.list.IntList;

/**
 * This class stores a single namespace node.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class NSNode {
  /** Children. */
  NSNode[] ch;
  /** Number of children. */
  int size;
  /** Parent node. */
  NSNode par;
  /** References to Prefix/URI pairs. */
  int[] vals;
  /** Pre value. */
  int pre;

  /**
   * Default constructor.
   * @param p pre value
   */
  NSNode(final int p) {
    vals = new int[0];
    ch = new NSNode[0];
    pre = p;
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
    size = in.readNum();
    ch = new NSNode[size];
    for(int c = 0; c < size; ++c) ch[c] = new NSNode(in, this);
  }

  /**
   * Writes a single node to disk.
   * @param out output stream
   * @throws IOException I/O exception
   */
  void write(final DataOutput out) throws IOException {
    out.writeNum(pre);
    out.writeNums(vals);
    out.writeNum(size);
    for(int c = 0; c < size; ++c) ch[c].write(out);
  }

  /**
   * Sorts the specified node into the child array.
   * @param n child node
   * @return node
   */
  NSNode add(final NSNode n) {
    int s = size;
    if(s == ch.length) {
      final NSNode[] tmp = new NSNode[Math.max(s << 1, 1)];
      System.arraycopy(ch, 0, tmp, 0, s);
      ch = tmp;
    }
    while(--s >= 0 && n.pre - ch[s].pre <= 0);
    System.arraycopy(ch, ++s, ch, s + 1, size++ - s);
    ch[s] = n;
    n.par = this;
    return n;
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

  /**
   * Recursively deletes the specified namespace URI reference.
   * @param uri namespace URI reference
   */
  void delete(final int uri) {
    for(final NSNode n : ch) n.delete(uri);
    final IntList il = new IntList(vals.length);
    for(int v = 0; v < vals.length; v += 2) {
      if(vals[v + 1] != uri) {
        il.add(vals[v]);
        il.add(vals[v + 1]);
      }
    }
    if(il.size() != vals.length) vals = il.toArray();
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
   * Deletes nodes in the specified range (p .. p + sz - 1) and updates the
   * following pre values
   * @param p pre value
   * @param sz number of nodes to be deleted, or actually the size of the pre
   * value which is to be deleted
   */
  void delete(final int p, final int sz) {
    // find the pre value which must be deleted
    int s = fnd(p);
    /* if the node is not directly contained as a child, either start at array
     * index 0 or proceed with the next node in the child array to search for
     * descendants of pre
     */
    if(s == -1 || ch[s].pre != p) ++s;
    // first pre value which is not deleted
    final int upper = p + sz;
    // number of nodes to be deleted
    int num = 0;
    // determine number of nodes to be deleted
    for(int i = s; i < size && ch[i].pre < upper; ++i, ++num);
    // new size of child array
    size -= num;

    // if all nodes are deleted, just create an empty array
    if(size == 0) ch = new NSNode[0];

    // otherwise remove nodes from the child array
    else if(num > 0) System.arraycopy(ch, s + num, ch, s, size - s);
  }

  /**
   * Finds a specific pre value in the child array utilizing binary search
   * and returns its position.
   * @param p pre value
   * @return node
   */
  int fnd(final int p) {
    int l = 0, h = size - 1;
    while(l <= h) { // binary search
      final int m = l + h >>> 1;
      final int v = ch[m].pre;
      if(v == p) return m;
      if(v < p) l = m + 1;
      else h = m - 1;
    }
    return l - 1;
  }

  // Printing Namespaces ======================================================

  /**
   * Prints the node structure for debugging purposes.
   * @param ns namespace reference
   * @param s start pre value
   * @param e end pre value
   * @return string
   */
  String print(final Namespaces ns, final int s, final int e) {
    final TokenBuilder tb = new TokenBuilder();
    print(tb, 0, ns, s, e);
    return tb.toString();
  }

  /**
   * Prints the node structure for debugging purposes.
   * @param tb token builder
   * @param l level
   * @param ns namespace reference
   * @param s start pre value
   * @param e end pre value
   */
  private void print(final TokenBuilder tb, final int l, final Namespaces ns,
      final int s, final int e) {

    if(pre >= s && pre <= e) {
      tb.add(NL);
      for(int i = 0; i < l; ++i) tb.add("  ");
      tb.add(toString() + ' ');
      for(int i = 0; i < vals.length; i += 2) {
        tb.add("xmlns");
        final byte[] p = ns.prefix(vals[i]);
        if(p.length != 0) tb.add(':');
        tb.add(p).add("=\"").add(ns.uri(vals[i + 1])).add("\" ");
      }
    }
    for(int c = 0; c < size; ++c) ch[c].print(tb, l + 1, ns, s, e);
  }

  @Override
  public String toString() {
    return "Pre[" + pre + ']';
  }
}
