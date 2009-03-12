package org.basex.data;

import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
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
public final class PathSummary {
  /** Data reference. */
  private Data data;
  /** Parent stack. */
  private PathNode[] stack;
  /** Root node. */
  public PathNode root;

  /**
   * Default Constructor.
   */
  public PathSummary() {
    init();
  }

  /**
   * Initializes the data structures.
   */
  public void init() {
    stack = new PathNode[IO.MAXHEIGHT];
    root = null;
  }

  /**
   * Constructor, specifying an input file.
   * @param d data reference
   * @param in input stream
   * @throws IOException I/O exception
   */
  public PathSummary(final Data d, final DataInput in) throws IOException {
    if(in.readBool()) root = new PathNode(in, null);
    data = d;
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
      root = new PathNode(n, k, null);
      stack[0] = root;
    } else {
      stack[l] = stack[l - 1].get(n, k, tl);
      root.tl += tl;
    }
  }
  
  /**
   * Writes the path summary to the specified output.
   * @param out output stream
   * @throws IOException I/O exception
   */
  public synchronized void write(final DataOutput out) throws IOException {
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
   * Returns descendant tags and attributes for the specified descendant path.
   * @param in input steps
   * @param d if false, return only children
   * @param o true/false: sort by occurrence/lexicographically
   * @return children
   */
  public TokenList desc(final TokenList in, final boolean d, final boolean o) {
    if(root == null) return new TokenList();
    // follow the specified descendant/child steps
    ArrayList<PathNode> n = new ArrayList<PathNode>();
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
    for(final PathNode r : n) tmp.add(token(r.count));
    final IntList occ = IntList.createOrder(tmp.finish(), true, false);

    // remove non-text/attribute nodes
    final TokenList out = new TokenList();
    for(int i = 0; i < n.size(); i++) {
      final PathNode r = n.get(o ? occ.list[i] : i);
      final byte[] name = r.token(data);
      if(name.length != 0 && !out.contains(name) && !contains(name, '(')) {
        out.add(name);
      }
    }
    if(!o) out.sort(false);
    return out;
  }

  /**
   * Returns the specified nodes.
   * @param in input node
   * @param out output nodes
   * @param t name reference
   * @param k node kind
   * @param desc if false, return only children
   */
  public void desc(final PathNode in, final HashSet<PathNode> out,
      final int t, final int k, final boolean desc) {

    for(final PathNode n : in.ch) {
      if(desc) desc(n, out, t, k, desc);
      if(k == -1 && n.kind != Data.ATTR || k == n.kind &&
          (t == 0 || t == n.name)) out.add(n);
    }
  }

  /**
   * Returns the descendants of the specified nodes.
   * @param in input nodes
   * @param t name reference
   * @param k node kind
   * @param desc if false, return only children
   * @return descendant nodes
   */
  public ArrayList<PathNode> desc(final ArrayList<PathNode> in, final int t,
      final int k, final boolean desc) {

    final ArrayList<PathNode> out = new ArrayList<PathNode>();
    for(final PathNode n : in) {
      if(t != 0 && (n.name != t || n.kind != k)) continue;
      for(final PathNode c : n.ch) {
        if(desc) c.desc(out);
        else out.add(c);
      }
    }
    return out;
  }
}
