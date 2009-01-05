package org.basex.data;

import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.ArrayList;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
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
  private SkelNode[] stack;
  /** Root node. */
  private SkelNode root;

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
    stack = new SkelNode[IO.MAXHEIGHT];
    root = null;
  }

  /**
   * Constructor, specifying an input file.
   * @param d data reference
   * @param in input stream
   * @throws IOException I/O exception
   */
  public Skeleton(final Data d, final DataInput in) throws IOException {
    if(in.readBool()) root = new SkelNode(in);
    data = d;
  }

  /**
   * Returns the skeleton root.
   * @return root
   */
  public SkelNode root() {
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
   * @param tl length of text in bytes (0 for non-text nodes)
   */
  public void add(final int n, final int l, final byte k, final int tl) {
    if(root == null) {
      root = new SkelNode(n, k);
      stack[0] = root;
    } else {
      stack[l] = stack[l - 1].get(n, k, tl);
      root.tl += tl;
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
   * Returns descendant tags and attributes for the specified start key.
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
   * Returns average text length of a text node in bytes.
   * @param k tag element
   * @return average text length of a text node in bytes
   */
  public double[] tl(final byte[] k) {
    if(k == null) return new double[] { root.tl, root.count };
    
    // follow the specified descendant/child steps
    ArrayList<SkelNode> n = new ArrayList<SkelNode>();
    n.add(root);
    n = desc(n, 0, Data.DOC, true);
    
    final int id = data.tagID(k);
    n = desc(n, id, Data.ELEM, false);
    double sum = 0;
    double c = 0;
    for(final SkelNode r : n) if (r.kind == Data.TEXT) {
      sum += r.tl; 
      c += r.count;
    }
    return new double[] { sum, c };
  }
  
  /**
   * Returns descendant tags and attributes for the specified descendant path.
   * @param in input steps
   * @param d if false, return only children
   * @param o true/false: sort by occurrence/lexicographically
   * @return children
   */
  public TokenList desc(final TokenList in, final boolean d, final boolean o) {
    if(root == null) return new TokenList();
    // follow the specified descendant/child steps
    ArrayList<SkelNode> n = new ArrayList<SkelNode>();
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
    for(final SkelNode r : n) tmp.add(token(r.count));
    final IntList occ = IntList.createOrder(tmp.finish(), true, false);

    // remove non-text/attribute nodes
    final TokenList out = new TokenList();
    for(int i = 0; i < n.size(); i++) {
      final SkelNode r = n.get(o ? occ.list[i] : i);
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
  public ArrayList<SkelNode> desc(final ArrayList<SkelNode> in, final int t,
      final int k, final boolean desc) {

    final ArrayList<SkelNode> out = new ArrayList<SkelNode>();
    for(final SkelNode n : in) {
      if(t != 0 && (n.name != t || n.kind != k)) continue;
      for(final SkelNode c : n.ch) {
        if(desc) c.desc(out);
        else out.add(c);
      }
    }
    return out;
  }
}
