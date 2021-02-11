package org.basex.data;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class stores a single namespace node.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class NSNode {
  /** Child nodes. */
  private NSNode[] nodes;
  /** Number of children. */
  private int size;
  /** Parent node. */
  private NSNode parent;
  /** Dense array with ids of prefix/namespace uri pairs. */
  private int[] values;
  /** Pre value. */
  private int pre;

  /**
   * Default constructor.
   * @param pre pre value or {@code -1}
   */
  NSNode(final int pre) {
    this.pre = pre;
    values = new int[0];
    nodes = new NSNode[0];
  }

  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   * @param parent parent reference
   * @throws IOException I/O exception
   */
  NSNode(final DataInput in, final NSNode parent) throws IOException {
    this.parent = parent;
    pre = in.readNum();
    values = in.readNums();
    size = in.readNum();
    nodes = new NSNode[size];
    for(int n = 0; n < size; ++n) nodes[n] = new NSNode(in, this);
  }

  /**
   * Writes a single node to disk.
   * @param out output stream
   * @throws IOException I/O exception
   */
  void write(final DataOutput out) throws IOException {
    out.writeNum(pre);
    out.writeNums(values);
    out.writeNum(size);
    for(int c = 0; c < size; ++c) nodes[c].write(out);
  }

  /**
   * Returns the specified child.
   * @param i index
   * @return child
   */
  NSNode child(final int i) {
    return nodes[i];
  }

  /**
   * Returns the number of children.
   * @return number of children
   */
  int children() {
    return size;
  }

  /**
   * Returns the pre value.
   * @return pre value
   */
  int pre() {
    return pre;
  }

  /**
   * Returns the parent node.
   * @return parent node
   */
  NSNode parent() {
    return parent;
  }

  /**
   * Returns the ids of prefix/namespace uri pairs.
   * @return prefix/namespace uri pairs
   */
  int[] values() {
    return values;
  }

  // Requesting Namespaces ========================================================================

  /**
   * Finds the namespace node that is located closest to the specified pre value.
   * @param p pre value
   * @param data data reference
   * @return node
   */
  NSNode find(final int p, final Data data) {
    // return this node if the pre values of all children are greater than the searched value
    final int s = find(p);
    if(s == -1) return this;

    final NSNode ch = nodes[s];
    final int cp = ch.pre;
    // return exact hit
    if(cp == p) return ch;
    // found node is preceding sibling
    if(cp + data.size(cp, Data.ELEM) <= p) return this;
    // continue recursive search
    return nodes[s].find(p, data);
  }

  /**
   * Locates a child node with the specified pre value.
   * <ul>
   *   <li> If the value is found, the position of the child node is returned.</li>
   *   <li> Otherwise, the position of the last child with a smaller pre value is returned.</li>
   *   <li> -1 is returned if all children have greater pre values.</li>
   * </ul>
   * @param p pre value
   * @return position of the child node
   */
  int find(final int p) {
    int l = 0, h = size - 1;
    while(l <= h) { // binary search
      final int m = l + h >>> 1, v = nodes[m].pre;
      if(v == p) return m;
      if(v < p) l = m + 1;
      else h = m - 1;
    }
    return l - 1;
  }

  /**
   * Returns the id of the namespace uri for the specified prefix.
   * @param prefix prefix reference
   * @return if of the namespace uri, or {@code 0} if none is found
   */
  int uri(final int prefix) {
    final int[] vls = values;
    final int vl = vls.length;
    for(int v = 0; v < vl; v += 2) {
      if(vls[v] == prefix) return vls[v + 1];
    }
    return 0;
  }

  // Updating Namespaces ==========================================================================

  /**
   * Deletes nodes in the specified range (p .. p + s - 1) and updates the following pre values.
   * @param p pre value
   * @param s number of nodes to be deleted, or actually the size of the pre
   * value which is to be deleted
   */
  void delete(final int p, final int s) {
    final int sz = size;
    // find the node to deleted
    int i = find(p);
    // if the node is not directly contained as a child, either start at array index 0 or
    // proceed with the next node in the child array to search for descendants of pre
    if(i == -1 || nodes[i].pre != p) ++i;
    // first pre value which is not deleted
    final int upper = p + s;
    // number of nodes to be deleted
    int num = 0;
    // determine number of nodes to be deleted
    for(int n = i; n < sz && nodes[n].pre < upper; ++n, ++num);
    // new size of child array
    size -= num;

    if(size == 0) {
      // if all nodes are deleted, just create an empty array
      nodes = new NSNode[0];
    } else if(num > 0) {
      // otherwise remove nodes from the child array
      Array.remove(nodes, i, num, sz);
      for(int n = size; n < sz; n++) nodes[n] = null;
    }
  }

  /**
   * Adds the specified node into the child array, which is sorted by pre values.
   * @param node child node
   */
  void add(final NSNode node) {
    if(size == nodes.length) nodes = Array.copy(nodes, new NSNode[Array.newCapacity(size)]);

    // find inserting position
    int i = find(node.pre);
    if(i < 0 || node.pre != nodes[i].pre) i++;

    Array.insert(nodes, i, 1, size++, null);
    nodes[i] = node;
    node.parent = this;
  }

  /**
   * Adds the specified prefix and URI reference.
   * @param prefix prefix reference
   * @param uri uri reference
   */
  void add(final int prefix, final int uri) {
    final int v = values.length;
    values = Arrays.copyOf(values, v + 2);
    values[v] = prefix;
    values[v + 1] = uri;
  }

  /**
   * Recursively deletes the specified namespace URI reference.
   * @param uri namespace URI reference
   */
  void delete(final int uri) {
    for(int c = 0; c < size; ++c) nodes[c].delete(uri);

    final int vl = values.length;
    for(int v = 0; v < vl; v += 2) {
      if(values[v + 1] != uri) continue;
      final int[] vals = new int[vl - 2];
      Array.copy(values, v, vals);
      Array.copy(values, v + 2, vl - v - 2, vals, v);
      values = vals;
      break;
    }
  }

  /**
   * Recursive shifting of pre values after delete operations.
   * @param start update location
   * @param diff value to subtract from pre value
   */
  void decrementPre(final int start, final int diff) {
    if(pre >= start + diff) pre -= diff;
    for(int c = 0; c < size; c++) nodes[c].decrementPre(start, diff);
  }

  /**
   * Increments the pre value by the specified size.
   * @param diff value to add to pre value
   */
  void incrementPre(final int diff) {
    pre += diff;
  }

  // Printing Namespaces ==========================================================================

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

    if(pre >= start && pre <= end) {
      tb.add(NL);
      for(int i = 0; i < level; ++i) tb.add("  ");
      tb.add(this).add(' ');
      final int[] vls = values;
      final int vl = vls.length;
      for(int i = 0; i < vl; i += 2) {
        if(i != 0) tb.add(' ');
        tb.add("xmlns");
        final byte[] p = ns.prefix(vls[i]);
        if(p.length != 0) tb.add(':');
        tb.add(p).add("=\"").add(ns.uri(vls[i + 1])).add('"');
      }
    }
    for(int c = 0; c < size; ++c) nodes[c].print(tb, level + 1, ns, start, end);
  }

  /**
   * Adds the namespace structure of a node to the specified table.
   * @param table table
   * @param start first pre value
   * @param end last pre value
   * @param ns namespace reference
   */
  void table(final Table table, final int start, final int end, final Namespaces ns) {
    final int vl = values.length;
    for(int i = 0; i < vl; i += 2) {
      if(pre < start || pre > end) continue;
      final TokenList tl = new TokenList();
      tl.add(values[i + 1]);
      tl.add(pre);
      tl.add(pre - parent.pre);
      tl.add(ns.prefix(values[i]));
      tl.add(ns.uri(values[i + 1]));
      table.contents.add(tl);
    }
    for(int i = 0; i < size; i++) nodes[i].table(table, start, end, ns);
  }

  /**
   * Adds namespace information for the specified node to a map.
   * @param map namespace map
   * @param ns namespace reference
   */
  void info(final TokenObjMap<TokenList> map, final Namespaces ns) {
    final int vl = values.length;
    for(int v = 0; v < vl; v += 2) {
      final byte[] pref = ns.prefix(values[v]), uri = ns.uri(values[v + 1]);
      final TokenList prfs = map.computeIfAbsent(uri, () -> new TokenList(1));
      if(!prfs.contains(pref)) prfs.add(pref);
    }
    for(int c = 0; c < size; ++c) nodes[c].info(map, ns);
  }

  /**
   * Prints the node structure.
   * @param ns namespace reference
   * @param start start pre value
   * @param end end pre value
   * @return string
   */
  String toString(final Namespaces ns, final int start, final int end) {
    final TokenBuilder tb = new TokenBuilder();
    print(tb, 0, ns, start, end);
    return tb.toString();
  }

  @Override
  public String toString() {
    return "Pre[" + pre + ']';
  }
}
