package org.basex.data;

import java.io.*;
import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class stores a single namespace node.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class NSNode {
  /** Child nodes of leaf nodes. */
  private static final NSNode[] EMPTY_NODES = {};

  /** Child nodes. */
  private NSNode[] nodes;
  /** Number of children. */
  private int size;
  /** Parent node. */
  private NSNode parent;
  /** ID of the set with the prefix/namespace URI pairs. */
  private int setId;
  /** Pre value. */
  private int pre;

  /**
   * Default constructor.
   * @param pre PRE value or {@code -1}
   */
  NSNode(final int pre) {
    this(pre, 0);
  }

  /**
   * Constructor, specifying a set of prefix/namespace URI pairs.
   * @param pre PRE value or {@code -1}
   * @param setId ID of the set with the prefix/namespace URI pairs
   */
  NSNode(final int pre, final int setId) {
    this.pre = pre;
    this.setId = setId;
    nodes = EMPTY_NODES;
  }

  /**
   * Constructor for reading instances with an older storage version.
   * @param in input stream
   * @param parent parent reference
   * @param sets sets of prefix/namespace URI pairs
   * @throws IOException I/O exception
   */
  NSNode(final DataInput in, final NSNode parent, final NSSets sets) throws IOException {
    this.parent = parent;
    pre = in.readNum();
    setId = sets.put(in.readNums());
    size = in.readNum();
    nodes = new NSNode[size];
    for(int n = 0; n < size; n++) nodes[n] = new NSNode(in, this, sets);
  }

  /**
   * Writes a node and its descendants in the format of databases before version 13.
   * @param out output stream
   * @param sets sets of prefix/namespace URI pairs
   * @throws IOException I/O exception
   */
  void write(final DataOutput out, final NSSets sets) throws IOException {
    out.writeNum(pre);
    out.writeNums(sets.get(setId));
    out.writeNum(size);
    for(int c = 0; c < size; c++) nodes[c].write(out, sets);
  }

  /**
   * Counts the nodes of this subtree and stops if the specified limit is exceeded.
   * @param limit node limit
   * @return number of nodes (can be larger than the limit)
   */
  int count(final int limit) {
    int count = 1;
    // the remaining limit is passed on, so that the recursion depth is limited as well
    for(int c = 0; c < size && count <= limit; c++) count += nodes[c].count(limit - count);
    return count;
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
   * Returns the PRE value.
   * @return PRE value
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
   * Returns the ID of the set with the prefix/namespace URI pairs.
   * @return set ID
   */
  int setId() {
    return setId;
  }

  // Requesting Namespaces ========================================================================

  /**
   * Finds the namespace node that is located closest to the specified PRE value.
   * @param p PRE value
   * @param data data reference
   * @return node
   */
  NSNode find(final int p, final Data data) {
    // return this node if the PRE values of all children are greater than the searched value
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
   * Locates a child node with the specified PRE value.
   * <ul>
   *   <li> If the value is found, the position of the child node is returned.</li>
   *   <li> Otherwise, the position of the last child with a smaller PRE value is returned.</li>
   *   <li> -1 is returned if all children have greater PRE values.</li>
   * </ul>
   * @param p PRE value
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

  // Updating Namespaces ==========================================================================

  /**
   * Deletes nodes in the specified range (p .. p + s - 1) and updates the following PRE values.
   * @param p PRE value
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
    // first PRE value which is not deleted
    final int upper = p + s;
    // number of nodes to be deleted
    int num = 0;
    // determine number of nodes to be deleted
    for(int n = i; n < sz && nodes[n].pre < upper; n++, num++);
    // new size of child array
    size -= num;

    if(size == 0) {
      // if all nodes are deleted, just create an empty array
      nodes = EMPTY_NODES;
    } else if(num > 0) {
      // otherwise remove nodes from the child array
      Array.remove(nodes, i, num, sz);
      Arrays.fill(nodes, size, sz, null);
    }
  }

  /**
   * Adds the specified node into the child array, which is sorted by PRE values.
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
   * Assigns the children of this node.
   * @param children child nodes, sorted by their PRE values
   */
  void children(final NSNode[] children) {
    // the array has no spare capacity: it is resized by the next call of add()
    nodes = children;
    size = children.length;
    for(final NSNode child : children) child.parent = this;
  }

  /**
   * Adds the specified prefix and URI reference.
   * @param sets sets of prefix/namespace URI pairs
   * @param prefixId prefix reference
   * @param uriId URI reference
   */
  void add(final NSSets sets, final int prefixId, final int uriId) {
    setId = sets.add(setId, prefixId, uriId);
  }

  /**
   * Recursively deletes the specified namespace URI reference.
   * @param sets sets of prefix/namespace URI pairs
   * @param uriId namespace URI reference
   */
  void delete(final NSSets sets, final int uriId) {
    for(int c = 0; c < size; c++) nodes[c].delete(sets, uriId);
    setId = sets.delete(setId, uriId);
  }

  /**
   * Recursive shifting of PRE values after delete operations.
   * @param start update location
   * @param diff value to subtract from PRE value
   */
  void decrementPre(final int start, final int diff) {
    if(pre >= start + diff) pre -= diff;
    for(int c = 0; c < size; c++) nodes[c].decrementPre(start, diff);
  }

  /**
   * Increments the PRE value by the specified size.
   * @param diff value to add to PRE value
   */
  void incrementPre(final int diff) {
    pre += diff;
  }

  // Printing Namespaces ==========================================================================

  /**
   * Adds the namespace entries of a node and its descendants to a list.
   * @param list list with the PRE value, parent PRE value, level and set ID of each entry
   * @param level level of this node
   * @param start first PRE value
   * @param end last PRE value
   */
  void entries(final IntList list, final int level, final int start, final int end) {
    if(parent != null && pre >= start && pre <= end) {
      list.add(pre).add(parent.pre).add(level).add(setId);
    }
    for(int c = 0; c < size; c++) nodes[c].entries(list, level + 1, start, end);
  }

  @Override
  public String toString() {
    return "Pre[" + pre + ']';
  }
}
