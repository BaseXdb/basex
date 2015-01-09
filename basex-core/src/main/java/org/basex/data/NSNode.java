package org.basex.data;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;

/**
 * This class stores a single namespace node.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class NSNode {
  /** Children. */
  NSNode[] children;
  /** Number of children. */
  int sz;
  /** Parent node. */
  NSNode parent;
  /** References to Prefix/URI pairs. */
  int[] values;
  /** Pre value. */
  int pr;

  /**
   * Default constructor.
   * @param pre pre value
   */
  NSNode(final int pre) {
    values = new int[0];
    children = new NSNode[0];
    pr = pre;
  }

  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   * @param parent parent reference
   * @throws IOException I/O exception
   */
  NSNode(final DataInput in, final NSNode parent) throws IOException {
    this.parent = parent;
    pr = in.readNum();
    values = in.readNums();
    sz = in.readNum();
    children = new NSNode[sz];
    for(int c = 0; c < sz; ++c) children[c] = new NSNode(in, this);
  }

  /**
   * Writes a single node to disk.
   * @param out output stream
   * @throws IOException I/O exception
   */
  void write(final DataOutput out) throws IOException {
    out.writeNum(pr);
    out.writeNums(values);
    out.writeNum(sz);
    for(int c = 0; c < sz; ++c) children[c].write(out);
  }

  /**
   * Adds the specified node into the child array, which is sorted by pre values.
   * @param node child node
   */
  void add(final NSNode node) {
    if(sz == children.length)
      children = Array.copy(children, new NSNode[Array.newSize(sz)]);

    // find inserting position
    int s = find(node.pr);
    if(s < 0 || node.pr != children[s].pr) s++;

    System.arraycopy(children, s, children, s + 1, sz++ - s);
    children[s] = node;
    node.parent = this;
  }

  /**
   * Adds the specified prefix and URI reference.
   * @param prefix prefix reference
   * @param uri uri reference
   */
  void add(final int prefix, final int uri) {
    final int s = values.length;
    values = Arrays.copyOf(values, s + 2);
    values[s] = prefix;
    values[s + 1] = uri;
  }

  /**
   * Recursively deletes the specified namespace URI reference.
   * @param uri namespace URI reference
   */
  void delete(final int uri) {
    for(int c = 0; c < sz; ++c) children[c].delete(uri);

    final int vl = values.length;
    for(int v = 0; v < vl; v += 2) {
      if(values[v + 1] != uri) continue;
      final int[] vals = new int[vl - 2];
      System.arraycopy(values, 0, vals, 0, v);
      System.arraycopy(values, v + 2, vals, v, vl - v - 2);
      values = vals;
      break;
    }
  }

  // Requesting Namespaces ====================================================

  /**
   * Finds the closest namespace node for the specified pre value.
   * @param pre pre value
   * @param data data reference
   * @return node
   */
  NSNode find(final int pre, final Data data) {
    final int s = find(pre);
    // no match found: return current node
    if(s == -1) return this;
    final NSNode ch = children[s];
    final int cp = ch.pr;
    // return exact hit
    if(cp == pre) return ch;
    // found node is preceding sibling
    if(cp + data.size(cp, Data.ELEM) <= pre) return this;
    // continue recursive search
    return children[s].find(pre, data);
  }

  /**
   * Finds a specific pre value in the child array utilizing binary search
   * and returns its position if it is contained.
   * If it is not contained, it returns the position of the biggest element in
   * the array that is still smaller than p. If all elements in the array are
   * bigger, it returns -1.
   * @param pre pre value
   * @return position of node in child array.
   */
  int find(final int pre) {
    int l = 0, h = sz - 1;
    while(l <= h) { // binary search
      final int m = l + h >>> 1;
      final int v = children[m].pr;
      if(v == pre) return m;
      if(v < pre) l = m + 1;
      else h = m - 1;
    }
    return l - 1;
  }

  /**
   * Returns the namespace URI reference for the specified prefix.
   * @param prefix prefix reference
   * @return uri reference or {@code 0}
   */
  int uri(final int prefix) {
    final int[] vls = values;
    final int vl = vls.length;
    for(int v = 0; v < vl; v += 2) if(vls[v] == prefix) return vls[v + 1];
    return 0;
  }

  /**
   * Deletes nodes in the specified range (p .. p + sz - 1) and updates the
   * following pre values
   * @param pre pre value
   * @param size number of nodes to be deleted, or actually the size of the pre
   * value which is to be deleted
   */
  void delete(final int pre, final int size) {
    // find the pre value which must be deleted
    int s = find(pre);
    /* if the node is not directly contained as a child, either start at array
     * index 0 or proceed with the next node in the child array to search for
     * descendants of pre
     */
    if(s == -1 || children[s].pr != pre) ++s;
    // first pre value which is not deleted
    final int upper = pre + size;
    // number of nodes to be deleted
    int num = 0;
    // determine number of nodes to be deleted
    for(int i = s; i < sz && children[i].pr < upper; ++i, ++num);
    // new size of child array
    sz -= num;

    // if all nodes are deleted, just create an empty array
    if(sz == 0) children = new NSNode[0];

    // otherwise remove nodes from the child array
    else if(num > 0) System.arraycopy(children, s + num, children, s, sz - s);
  }

  // Printing Namespaces ======================================================

  /**
   * Prints the node structure for debugging purposes.
   * @param ns namespace reference
   * @param start start pre value
   * @param end end pre value
   * @return string
   */
  String print(final Namespaces ns, final int start, final int end) {
    final TokenBuilder tb = new TokenBuilder();
    print(tb, 0, ns, start, end);
    return tb.toString();
  }

  /**
   * Prints the node structure for debugging purposes.
   * @param tb token builder
   * @param level level
   * @param ns namespace reference
   * @param start start pre value
   * @param end end pre value
   */
  private void print(final TokenBuilder tb, final int level, final Namespaces ns, final int start,
      final int end) {

    if(pr >= start && pr <= end) {
      tb.add(NL);
      for(int i = 0; i < level; ++i) tb.add("  ");
      tb.add(toString() + ' ');
      final int[] vls = values;
      final int vl = vls.length;
      for(int i = 0; i < vl; i += 2) {
        tb.add("xmlns");
        final byte[] p = ns.prefix(vls[i]);
        if(p.length != 0) tb.add(':');
        tb.add(p).add("=\"").add(ns.uri(vls[i + 1])).add("\" ");
      }
    }
    for(int c = 0; c < sz; ++c) children[c].print(tb, level + 1, ns, start, end);
  }

  @Override
  public String toString() {
    return "Pre[" + pr + ']';
  }
}
