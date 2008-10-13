package org.basex.data;

import static org.basex.data.DataText.*;
import java.io.IOException;
import java.util.ArrayList;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.util.Array;
import org.basex.util.StringList;
import org.basex.util.Token;

/**
 * This class stores the tree structure of a document.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Skeleton {
  /** Data reference. */
  private Data data;
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
   * @param d data reference
   * @param in input stream
   * @throws IOException I/O exception
   */
  public Skeleton(final Data d, final DataInput in) throws IOException {
    if(in.readBool()) root = new Node(in);
    data = d;
  }

  /**
   * Returns the skeleton root.
   * @return root
   */
  public Node root() {
    return root;
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
   * @param out output stream
   * @throws IOException I/O exception
   */
  public synchronized void finish(final DataOutput out) throws IOException {
    out.writeBool(root != null);
    if(root != null) root.finish(out);
  }

  /**
   * Removes the statistics.
   */
  public void noStats() {
    stack = new Node[256];
    root = null;
  }
  
  /**
   * Return completions for the specified descendant steps.
   * based on the document tree structure.
   * Note that only basic completions will be recognized.
   * @param desc descendant steps
   * @return children
   */
  public String[] desc(final StringList desc) {
    final StringList sl = new StringList();
    if(root != null) {
      ArrayList<Node> n = new ArrayList<Node>();
      n.add(root);
      n = desc(n, "", true);
      for(int i = 0; i < desc.size; i++) n = desc(n, desc.list[i], true);
  
      for(final Node r : n) {
        final String name = Token.string(r.token(data));
        if(!sl.contains(name) && !name.contains("(")) sl.add(name);
      }
      sl.sort();
    }
    return sl.finish();
  }

  /**
   * Returns the child node for the specified name.
   * @param n node
   * @param name name
   * @param desc descendant flag
   * @return child node
   */
  public ArrayList<Node> desc(final ArrayList<Node> n, final String name,
      final boolean desc) {

    if(name.startsWith("@")) return new ArrayList<Node>();
    return desc(n, data.tagID(Token.token(name)), desc);
  }

  /**
   * Returns the descendant for the specified nodes.
   * @param n node
   * @param t name reference
   * @param desc if false, return only children
   * @return descendant nodes
   */
  public ArrayList<Node> desc(final ArrayList<Node> n, final int t,
      final boolean desc) {

    final ArrayList<Node> ch = new ArrayList<Node>();
    for(final Node r : n) {
      if(t != 0 && r.name != t) continue;
      for(final Node c : r.ch) {
        if(desc) c.desc(ch);
        else ch.add(c);
      }
    }
    return ch;
  }

  /** Document node. */
  public static final class Node {
    /** Tag reference. */
    public final short name;
    /** Node kind. */
    public final byte kind;
    /** Tag counter. */
    public int count;
    /** Children. */
    public Node[] ch;

    /**
     * Default Constructor.
     * @param t tag
     * @param k node kind
     */
    Node(final int t, final byte k) {
      ch = new Node[0];
      count = 1;
      name = (short) t;
      kind = k;
    }

    /**
     * Constructor, specifying an input stream.
     * @param in input stream
     * @throws IOException I/O exception
     */
    Node(final DataInput in) throws IOException {
      name = (short) in.readNum();
      kind = in.readByte();
      count = in.readNum();
      ch = new Node[in.readNum()];
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
        if(c.kind == k && c.name == t) {
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
     * Recursively adds the descendants to the specified node list.
     * @param nodes node list
     */
    public void desc(final ArrayList<Node> nodes) {
      nodes.add(this);
      for(final Node n : ch) n.desc(nodes);
    }

    /**
     * Returns a readable representation of this node.
     * @param data data reference
     * @return completions
     */
    public byte[] token(final Data data) {
      switch(kind) {
        case Data.ELEM: return data.tags.key(name);
        case Data.ATTR: return Token.concat(ATT, data.atts.key(name));
        case Data.TEXT: return TEXT;
        case Data.COMM: return COMM;
        case Data.PI:   return PI;
        default:        return Token.EMPTY;
      }
    }

    @Override
    public String toString() {
      return "Node[" + kind + ", " + name + ", " + ch.length + " Children]";
    }
  }
}
