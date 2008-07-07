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
   * Suggest completions for the specified xpath in a string list,
   * based on the document tree structure.
   * Note that only basic completions will be recognized.
   * @param data data reference
   * @param xpath input xpath
   * @return children
   */
  public StringList suggest(final Data data, final String xpath) {
    final StringList sl = new StringList();
    
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
      String name = null;
      if(r.kind == Data.ATTR) {
        name = "@" + Token.string(data.atts.key(r.name));
      } else if(r.kind == Data.ELEM) {
        name = Token.string(data.tags.key(r.name));
        /*
      } else if(r.kind == Data.TEXT) {
        name = "text()";
      } else if(r.kind == Data.COMM) {
        name = "comment()";
      } else if(r.kind == Data.PI) {
        name = "processing-instruction()";
        */
      }
      if(name != null && !sl.contains(name)) sl.add(name);
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
  private ArrayList<Node> child(final ArrayList<Node> n, final Data data,
      final String name, final boolean desc) {

    final ArrayList<Node> ch = new ArrayList<Node>();
    if(name.startsWith("@")) return ch;
    final int t = data.tagID(Token.token(name));
    
    for(final Node r : n) {
      if(name.length() != 0 && r.name != t) continue;
      for(final Node c : r.ch) {
        if(desc) c.child(ch);
        else ch.add(c);
      }
    }
    return ch;
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
     * Recursively adds the children to this node.
     * @param nodes node array
     */
    public void child(final ArrayList<Node> nodes) {
      nodes.add(this);
      for(final Node n : ch) n.child(nodes);
    }

    @Override
    public String toString() {
      return "Node[" + kind + ", " + name + ", " + ch.length + " Children]";
    }
  }
}
