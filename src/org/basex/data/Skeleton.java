package org.basex.data;

import static org.basex.data.DataText.*;

import java.io.IOException;

import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.Array;
import org.basex.util.Token;

/**
 * This class stores the tree structure of a document.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Skeleton {
  /** Parent stack. */
  private Node[] stack;
  /** Root node. */
  private Node root;

  /**
   * Default Constructor.
   */
  public Skeleton() {
    stack = new Node[256];
  }

  /**
   * Constructor, specifying an input file.
   * @param db name of the database
   * @throws IOException I/O exception
   */
  public Skeleton(final String db) throws IOException {
    // ignore missing namespace input
    if(!IO.dbfile(db, DATASTR).exists()) return;

    final DataInput in = new DataInput(db, DATASTR);
    root = new Node(in);
    in.close();
  }

  /**
   * Opens an element.
   * @param n name reference
   * @param l current level
   * @param k node kind
   */
  public void add(final int n, final int l, final byte k) {
    if(root == null) {
      root = new Node(n, k);
      stack[0] = root;
    } else {
      stack[l] = stack[l - 1].get(n, k);
    }
  }

  /**
   * Finishes the structure.
   * @param db name of the database
   * @throws IOException I/O exception
   */
  public synchronized void finish(final String db) throws IOException {
    final DataOutput out = new DataOutput(db, DATASTR);
    root.finish(out);
    out.close();
  }

  /**
   * Removes the statistics.
   */
  public void noStats() {
    root = null;
  }

  /**
   * Dumps the tree structure.
   * @param d data reference
   */
  public void dump(final Data d) {
    final StringBuilder sb = new StringBuilder();
    root.dump(sb, 0, d);
    System.out.println(sb.toString());
  }

  /** Document node. */
  private static final class Node {
    /** Tag reference. */
    final int name;
    /** Node kind. */
    final byte kind;
    /** Tag counter. */
    int count;
    /** Children. */
    Node[] ch;

    /**
     * Default Constructor.
     * @param t tag
     * @param k node kind
     */
    Node(final int t, final byte k) {
      ch = new Node[0];
      count = 1;
      name = t;
      kind = k;
    }

    /**
     * Constructor, specifying an input stream.
     * @param in input stream
     */
    Node(final DataInput in) {
      name = in.readNum();
      kind = in.readByte();
      count = in.readNum();
      int cl = in.readNum();
      ch = new Node[cl];
      for(int i = 0; i < ch.length; i++) ch[i] = new Node(in);
    }

    /**
     * Returns a node reference for the specified tag.
     * @param t tag
     * @param k node kind
     * @return node reference
     */
    Node get(final int t, final byte k) {
      for(final Node c : ch) {
        if(c.name == t && c.kind == k) {
          c.count++;
          return c;
        }
      }
      final Node n = new Node(t, k);
      ch = Array.add(ch, n);
      return n;
    }

    /**
     * Finishes the tree structure.
     * @param out output stream
     * @throws IOException I/O exception
     */
    void finish(final DataOutput out) throws IOException {
      out.writeNum(name);
      out.write1(kind);
      out.writeNum(count);
      out.writeNum(ch.length);
      for(final Node c : ch) c.finish(out);
    }

    /**
     * Dumps the tree structure.
     * @param sb string builder
     * @param l level
     * @param d data reference
     */
    void dump(final StringBuilder sb, final int l, final Data d) {
      for(int i = 0; i < l; i++) sb.append("  ");
      String nm = "";
      switch(kind) {
        case Data.ELEM: nm = Token.string(d.tags.key(name)); break;
        case Data.ATTR: nm = "@" + Token.string(d.atts.key(name)); break;
        case Data.TEXT: nm = "TEXT"; break;
        case Data.COMM: nm = "COMM"; break;
        case Data.PI  : nm = "PI"; break;
      }
      sb.append(nm + ": " + count + "x\n");
      for(final Node c : ch) c.dump(sb, l + 1, d);
    }
  }
}
