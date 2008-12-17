package org.basex.data;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.ArrayList;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.TokenList;

/**
 * This class stores the tree structure of a document.
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
    init();
  }

  /**
   * Initializes the data structures.
   */
  public void init() {
    stack = new Node[IO.MAXHEIGHT];
    root = null;
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
    add(n, l, k, 0);
  }

  /**
   * Opens an element.
   * @param n name reference
   * @param l current level
   * @param k node kind
   * @param tl length of text in bytes (0) for nontext nodes
   */
  public void add(final int n, final int l, final byte k, final int tl) {
    if(root == null) {
      root = new Node(n, k);
      stack[0] = root;
    } else {
      stack[l] = stack[l - 1].get(n, k, tl);
      if (k == Data.TEXT) {
        root.tl += tl;
      }
      
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
   * Return descendant tags and attributes for the specified start key.
   * @param k input key
   * @param d if false, return only children
   * @param o true/false: sort by occurrence/lexicographically
   * @return children
   */
  public TokenList desc(final byte[] k, final boolean d, final boolean o) {
    final TokenList tl = new TokenList();
    if(k.length != 0) tl.add(k);
    return desc(tl, d, o);
  }

  /**
   * Returns avg. textlength of a textnode in bytes.
   * @param k tag element
   * @return avg. textlength of a textnode in bytes
   */
  public double tl(final byte[] k) {
    // return tl of document
    if (k == null) return root.tl / root.count;
    // follow the specified descendant/child steps
    ArrayList<Node> n = new ArrayList<Node>();
    n.add(root);
    n = desc(n, 0, Data.DOC, true);
    
    final int id = data.tagID(k);
    n = desc(n, id, Data.ELEM, false);
    double avg = 0;
    int c = 0;
    for(final Node r : n) if (r.kind == Data.TEXT) {
      avg += r.tl / r.count;
      c++;
    }
    return avg / c;
  }
  
  /**
   * Return descendant tags and attributes for the specified descendant path.
   * @param in input steps
   * @param d if false, return only children
   * @param o true/false: sort by occurrence/lexicographically
   * @return children
   */
  public TokenList desc(final TokenList in, final boolean d, final boolean o) {
    if(root == null) return new TokenList();
    // follow the specified descendant/child steps
    ArrayList<Node> n = new ArrayList<Node>();
    n.add(root);
    n = desc(n, 0, Data.DOC, true);

    for(final byte[] i : in) {
      final boolean att = startsWith(i, '@');
      final byte kind = att ? Data.ATTR : Data.ELEM;
      final int id = att ? data.attNameID(substring(i, 1)) : data.tagID(i);
      n = desc(n, id, kind, d);
    }

    // sort by number of occurrences
    final TokenList tmp = new TokenList();
    for(final Node r : n) tmp.add(token(r.count));
    final IntList occ = IntList.createOrder(tmp.finish(), true, false);

    // remove non-text/attribute nodes
    final TokenList out = new TokenList();
    for(int i = 0; i < n.size(); i++) {
      final Node r = n.get(o ? occ.list[i] : i);
      final byte[] name = r.token(data);
      if(name.length != 0 && !out.contains(name) && !contains(name, '(')) {
        out.add(name);
      }
    }
    if(!o) out.sort(false);
    return out;
  }

  /**
   * Returns the descendant for the specified nodes.
   * @param in input nodes
   * @param t name reference
   * @param k node kind
   * @param desc if false, return only children
   * @return descendant nodes
   */
  public ArrayList<Node> desc(final ArrayList<Node> in, final int t,
      final int k, final boolean desc) {

    final ArrayList<Node> out = new ArrayList<Node>();
    for(final Node n : in) {
      if(t != 0 && (n.name != t || n.kind != k)) continue;
      for(final Node c : n.ch) {
        if(desc) c.desc(out);
        else out.add(c);
      }
    }
    return out;
  }

  /** Skeleton node. */
  public static final class Node {
    /** Tag/attribute name reference. */
    public final short name;
    /** Node kind. */
    public final byte kind;
    /** Tag counter. */
    public int count;
    /** Children. */
    public Node[] ch;
    /** Length of text. */
    public int tl;

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
     * Default Constructor.
     * @param t tag
     * @param k node kind
     * @param l text length
     */
    Node(final int t, final byte k, final int l) {
      ch = new Node[0];
      count = 1;
      name = (short) t;
      kind = k;
      tl = l;
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
      tl = in.readNum();
      for(int i = 0; i < ch.length; i++) ch[i] = new Node(in);
    }

    /**
     * Returns a node reference for the specified tag.
     * @param t tag
     * @param k node kind
     * @return node reference
     */
    Node get(final int t, final byte k) {
      return get(t, k, 0);
    }

    /**
     * Returns a node reference for the specified tag.
     * @param t tag
     * @param k node kind
     * @param l text length
     * @return node reference
     */
    Node get(final int t, final byte k, final int l) {
      for(final Node c : ch) {
        if(c.kind == k && c.name == t) {
          c.count++;
          c.tl += l; 
          return c;
        }
      }
      final Node n = new Node(t, k, l);
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
      out.writeNum(tl);
      
      for(final Node c : ch) c.finish(out);
    }

    /**
     * Recursively adds the node and its descendants to the specified list.
     * @param nodes node list
     */
    public void desc(final ArrayList<Node> nodes) {
      nodes.add(this);
      for(final Node n : ch)
        n.desc(nodes);
    }

    /**
     * Returns a readable representation of this node.
     * @param data data reference
     * @return completions
     */
    public byte[] token(final Data data) {
      switch(kind) {
        case Data.ELEM: return data.tags.key(name);
        case Data.ATTR: return concat(ATT, data.atts.key(name));
        case Data.TEXT: return TEXT;
        case Data.COMM: return COMM;
        case Data.PI:   return PI;
        default:        return EMPTY;
      }
    }

    @Override
    public String toString() {
      return "Node[" + kind + ", " + name + ", " + ch.length 
        + " Children, " + tl + "]";
    }
  }
}
