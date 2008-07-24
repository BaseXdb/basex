package org.basex.data;

import static org.basex.data.DataText.*;

import java.io.IOException;
import java.util.ArrayList;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
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
    if(in.length() != 0) root = new Node(in);
    in.close();
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
   * @param db name of the database
   * @throws IOException I/O exception
   */
  public synchronized void finish(final String db) throws IOException {
    final DataOutput out = new DataOutput(db, DATASTR);
    if(root != null) root.finish(out);
    out.close();
  }

  /**
   * Removes the statistics.
   */
  public void noStats() {
    root = null;
  }

  /**
   * Suggest completions for the specified xpath in a string list,
   * based on the document tree structure.
   * Note that only basic completions will be recognized.
   * @param data data reference
   * @param xpath input xpath
   * @return children
   */
  public StringList suggest(final Data data, final String xpath) {
    final StringList sl = new StringList();
    if(root == null) return sl;
    
    ArrayList<Node> n = new ArrayList<Node>();
    n.add(root);

    final String input = xpath.startsWith("/") ? xpath : "/" + xpath;
    final int il = input.length();
    int s = 0;
    for(int i = 0; i < il; i++) {
      final char c = input.charAt(i);
      if(c == '/' || c == '[') {
        final boolean desc = i + 1 < il && input.charAt(i + 1) == '/';
        if(desc && i + 2 < il && input.charAt(i + 2) == '/') return sl;
        n = child(n, data, input.substring(s, i), desc);
        if(n.size() == 0) return sl;
        if(desc) i++;
        s = i + 1;
      }
    }

    for(final Node r : n) {
      final String name = Token.string(r.token(data));
      if(name.length() != 0 && !sl.contains(name)) sl.add(name);
    }
    sl.sort();
    return sl;
  }

  /**
   * Returns the child node for the specified name.
   * @param n node
   * @param data data reference
   * @param name name
   * @param desc descendant flag
   * @return child node
   */
  public ArrayList<Node> child(final ArrayList<Node> n, final Data data,
      final String name, final boolean desc) {

    if(name.startsWith("@")) return new ArrayList<Node>();
    return child(n, data.tagID(Token.token(name)), desc);
  }

  /**
   * Returns the child node for the specified name.
   * @param n node
   * @param t name reference
   * @param desc descendant flag
   * @return child node
   */
  public ArrayList<Node> child(final ArrayList<Node> n, final int t,
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
     */
    Node(final DataInput in) {
      name = (short) in.readNum();
      kind = in.readByte();
      count = in.readNum();
      final int cl = in.readNum();
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
