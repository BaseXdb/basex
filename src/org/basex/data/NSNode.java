package org.basex.data;

import java.io.IOException;
import org.basex.core.Main;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.util.Array;

/**
 * This class provides a single namespace node.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
final class NSNode {
  /** Children. */
  NSNode[] ch;
  /** Keys. */
  int[] key;
  /** Values. */
  int[] val;
  /** Parent node. */
  NSNode par;
  /** Pre value. */
  int pre;

  /** Default constructor. */
  NSNode() {
    key = Array.NOINTS;
    val = Array.NOINTS;
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
    key = in.readNums();
    val = in.readNums();
    final int cl = in.readNum();
    ch = new NSNode[cl];
    for(int c = 0; c < cl; c++) ch[c] = new NSNode(in, this);
  }

  /**
   * Finishes the tree structure.
   * @param out output stream
   * @throws IOException I/O exception
   */
  void finish(final DataOutput out) throws IOException {
    out.writeNum(pre);
    out.writeNums(key);
    out.writeNums(val);
    out.writeNum(ch.length);
    for(final NSNode c : ch) c.finish(out);
  }

  /**
   * Adds the specified key and value.
   * @param k key
   * @param v value
   */
  void add(final int k, final int v) {
    key = Array.add(key, k);
    val = Array.add(val, v);
  }

  /**
   * Adds the specified child.
   * @param c child
   */
  void add(final NSNode c) {
    ch = Array.add(ch, c);
  }

  /**
   * Returns the value reference for the specified key.
   * @param k key to be found
   * @return v value or 0
   */
  int get(final int k) {
    for(int i = 0; i < key.length; i++) if(key[i] == k) return val[i];
    return 0;
  }

  /**
   * Finds the closest namespace definitions for the specified pre value.
   * @param p pre value
   * @return node
   */
  NSNode find(final int p) {
    if(ch.length == 0) return this;
    int l = 0, m = 0, h = ch.length - 1;
    while(l <= h) { // binary search
      m = l + h >>> 1;
      final int v = ch[m].pre;
      if(v == p) return ch[m];
      if(v < p) l = m + 1;
      else h = m - 1;
    }
    return ch[m == 0 || ch[m].pre < p ? m : m - 1].find(p);
  }

  @Override
  public String toString() {
    return Main.name(this) + "[pre:" + pre + "]";
  }
}
