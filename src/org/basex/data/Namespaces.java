package org.basex.data;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.PrintOutput;
import org.basex.util.Array;
import org.basex.util.Set;

/**
 * This class organizes the namespaces of a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Namespaces extends Set {

  // [CG] add support for updates/disallow updates for documents with namespaces

  /** Root node. */
  private Node root;
  /** Current node. */
  private Node tmp;

  /**
   * Default Constructor.
   */
  public Namespaces() {
    root = new Node();
  }

  /**
   * Constructor, specifying an input file.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public Namespaces(final DataInput in) throws IOException {
    keys = in.readBytesArray();
    next = in.readNums();
    bucket = in.readNums();
    size = in.readNum();
    root = new Node(in, null);
  }

  /**
   * Writes the namespaces to disk.
   * @param out output stream
   * @throws IOException I/O exception
   */
  public synchronized void finish(final DataOutput out) throws IOException {
    out.writeBytesArray(keys);
    out.writeNums(next);
    out.writeNums(bucket);
    out.writeNum(size);
    root.finish(out);
  }

  /**
   * Opens a node.
   * @param p current pre value
   * @return true if namespaces have been registered
   */
  public boolean open(final int p) {
    if(tmp == null) return false;
    tmp.par = root;
    tmp.pre = p;
    root.add(tmp);
    root = tmp;
    tmp = null;
    return true;
  }

  /**
   * Closes a node.
   * @param p current pre value
   */
  public void close(final int p) {
    while(root.pre >= p) root = root.par;
  }

  /**
   * Adds the specified namespace.
   * @param p namespace prefix
   * @param u namespace uri
   */
  public void add(final byte[] p, final byte[] u) {
    if(tmp == null) tmp = new Node();
    tmp.add(Math.abs(add(p)), Math.abs(add(u)));
  }

  /**
   * Returns the namespace for the specified qname and pre value.
   * @param n tag/attribute name
   * @param p pre value
   * @return namespace reference or 0 if no namespace was found
   */
  public int get(final byte[] n, final int p) {
    return ns(pre(n), root.find(p));
  }

  /**
   * Returns the namespace keys and values for the specified pre value.
   * @param p pre value
   * @return namespace reference or 0 if no namespace was found
   */
  public int[] get(final int p) {
    final Node node = root.find(p);
    final int[] ns = new int[node.key.length << 1];
    for(int n = 0; n < ns.length; n += 2) {
      ns[n] = node.key[n >> 1];
      ns[n + 1] = node.val[n >> 1];
    }
    return ns;
  }

  /**
   * Returns the namespace for the specified qname.
   * @param n tag/attribute name
   * @return namespace
   */
  public int get(final byte[] n) {
    final byte[] pre = pre(n);
    return pre.length == 0 ? 0 : ns(pre, root);
  }

  /**
   * Returns the namespace for the specified qname.
   * @param p prefix
   * @param node node to start with
   * @return namespace
   */
  private int ns(final byte[] p, final Node node) {
    if(eq(XML, p)) return 0;

    Node nd = node;
    final int k = id(p);
    if(k == 0) return 0;
    
    while(nd != null) {
      final int i = nd.get(k);
      if(i != 0) return i;
      nd = nd.par;
    }
    return 0;
  }

  /**
   * Prints the namespace structure to the specified output stream.
   * @param out output stream
   * @param s space for pre value
   * @throws IOException I/O exception
   */
  public void print(final PrintOutput out, final int s) throws IOException {
    if(root.ch.length == 0) return;
    out.print(s, token(TABLEPRE));
    out.print(s + 1, token(TABLEDIST));
    out.print(' ');
    out.print(token(TABLEPREF), 11);
    out.println(token(TABLEURI));
    root.print(out, s);
  }

  /** Document node. */
  final class Node {
    /** Children. */
    Node[] ch;
    /** Keys. */
    int[] key;
    /** Values. */
    int[] val;
    /** Parent node. */
    Node par;
    /** Pre value. */
    int pre;

    /** Default constructor. */
    Node() {
      key = Array.NOINTS;
      val = Array.NOINTS;
      ch = new Node[0];
    }

    /**
     * Constructor, specifying an input stream.
     * @param in input stream
     * @param p parent reference
     * @throws IOException I/O exception
     */
    Node(final DataInput in, final Node p) throws IOException {
      par = p;
      pre = in.readNum();
      key = in.readNums();
      val = in.readNums();
      final int cl = in.readNum();
      ch = new Node[cl];
      for(int c = 0; c < cl; c++) ch[c] = new Node(in, this);
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
      for(final Node c : ch) c.finish(out);
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
    void add(final Node c) {
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
    Node find(final int p) {
      if(ch.length == 0) return this;
      int l = 0, m = 0, h = ch.length - 1;
      while(l <= h) { //binary search
        m = (l + h) >>> 1;
        final int v = ch[m].pre;
        if(v < p) l = m + 1;
        else if(v > p) h = m - 1;
        else return ch[m];
      }
      return ch[m == 0 || ch[m].pre < p ? m : m - 1].find(p);
    }

    /**
     * Prints information on the node and its descendants.
     * @param out output stream
     * @param s space for pre value
     * @throws IOException I/O exception
     */
    void print(final PrintOutput out, final int s) throws IOException {
      final byte[] quote = { '"' };
      for(int i = 0; i < key.length; i++) {
        out.print(s, token(pre));
        out.print(s + 1, token(pre - par.pre));
        out.print(' ');
        out.print(concat(quote, key(key[i]), quote), 11);
        out.print(key(val[i]));
        out.println(" (" + val[i] + ")");
      }
      for(final Node c : ch) c.print(out, s);
    }
  }
}
