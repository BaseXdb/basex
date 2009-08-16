package org.basex.data;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.ArrayList;
import org.basex.core.Prop;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.Array;
import org.basex.util.TokenList;

/**
 * This class stores the path summary of a document.
 * It contains all unique location paths of the document.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class PathSummary {
  /** Parent stack. */
  private PathNode[] stack;
  /** Root node. */
  public PathNode root;

  /**
   * Default constructor.
   */
  public PathSummary() {
    init();
  }

  /**
   * Initializes the data structures. This method is called if new statistics
   * are created.
   */
  public void init() {
    stack = new PathNode[IO.MAXHEIGHT];
    root = null;
  }

  /**
   * Constructor, specifying an input file.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public PathSummary(final DataInput in) throws IOException {
    if(in.readBool()) root = new PathNode(in, null);
  }

  // Path Summary creation ====================================================

  /**
   * Opens an element.
   * @param n name reference
   * @param l current level
   * @param k node kind
   */
  public void add(final int n, final int l, final byte k) {
    if(root == null) {
      root = new PathNode(n, k, null);
      stack[0] = root;
    } else {
      stack[l] = stack[l - 1].get(n, k);
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

  // Path Summary traversal ===================================================

  /**
   * Returns all children or descendants of the specified nodes.
   * @param in input nodes
   * @param desc if false, return only children
   * @return descendant nodes
   */
  public ArrayList<PathNode> desc(final ArrayList<PathNode> in,
      final boolean desc) {

    final ArrayList<PathNode> out = new ArrayList<PathNode>();
    for(final PathNode n : in) {
      for(final PathNode c : n.ch) {
        if(desc) c.addDesc(out);
        else if(!out.contains(c)) out.add(c);
      }
    }
    return out;
  }

  /**
   * Returns all parents of the specified nodes.
   * @param in input nodes
   * @return parent nodes
   */
  public ArrayList<PathNode> parent(final ArrayList<PathNode> in) {
    final ArrayList<PathNode> out = new ArrayList<PathNode>();
    for(final PathNode n : in) if(!out.contains(n.par)) out.add(n.par);
    return out;
  }

  /**
   * Returns the root node.
   * @return root node
   */
  public ArrayList<PathNode> root() {
    final ArrayList<PathNode> out = new ArrayList<PathNode>();
    out.add(root);
    return out;
  }

  /**
   * Adds nodes to the hash set if they comply to the specified arguments.
   * @param in input node
   * @param out output nodes
   * @param t name reference
   * @param k node kind
   * @param desc if false, return only children
   */
  public void desc(final PathNode in, final ArrayList<PathNode> out,
      final int t, final int k, final boolean desc) {

    for(final PathNode n : in.ch) {
      if(desc) desc(n, out, t, k, desc);
      if(k == -1 && n.kind != Data.ATTR || k == n.kind &&
          (t == 0 || t == n.name)) {
        out.add(n);
      }
    }
  }

  /**
   * Returns descendant tags and attributes for the specified start key.
   * @param k input key
   * @param data data reference
   * @param d if false, return only children
   * @param o true/false: sort by occurrence/lexicographically
   * @return children
   */
  public TokenList desc(final byte[] k, final Data data, final boolean d,
      final boolean o) {
    final TokenList tl = new TokenList();
    if(k.length != 0) tl.add(k);
    return desc(tl, data, d, o);
  }

  /**
   * Returns descendant tags and attributes for the specified descendant path.
   * @param tl input steps
   * @param data data reference
   * @param d if false, return only children
   * @param o true/false: sort by occurrence/lexicographically
   * @return children
   */
  public TokenList desc(final TokenList tl, final Data data, final boolean d,
      final boolean o) {
    // follow the specified descendant/child steps
    ArrayList<PathNode> in = desc(root(), true);

    for(final byte[] i : tl) {
      final boolean att = startsWith(i, '@');
      final byte kind = att ? Data.ATTR : Data.ELEM;
      final int id = att ? data.attNameID(substring(i, 1)) : data.tagID(i);

      final ArrayList<PathNode> out = new ArrayList<PathNode>();
      for(final PathNode n : in) {
        if(n.name != id || n.kind != kind) continue;
        for(final PathNode c : n.ch) {
          if(d) c.addDesc(out);
          else out.add(c);
        }
      }
      in = out;
    }

    // sort by number of occurrences
    final double[] tmp = new double[in.size()];
    for(int i = 0; i < in.size(); i++) tmp[i] = in.get(i).count;
    final int[] occ = Array.createOrder(tmp, false);

    // remove non-text/attribute nodes
    final TokenList out = new TokenList();
    for(int i = 0; i < in.size(); i++) {
      final PathNode r = in.get(o ? occ[i] : i);
      final byte[] name = r.token(data);
      if(name.length != 0 && !out.contains(name) && !contains(name, '(')) {
        out.add(name);
      }
    }
    if(!o) out.sort(false);
    return out;
  }

  /**
   * Returns information on the path summary.
   * @param data data reference
   * @return info
   */
  public byte[] info(final Data data) {
    byte[] info = root.info(data, 0);
    if(!data.meta.prop.is(Prop.INDEXALL)) info = chop(info, 1 << 13);
    return info;
  }

  /**
   * Serializes the path node.
   * @param data data reference
   * @param ser serializer
   * @throws IOException I/O exception
   */
  public void plan(final Data data, final Serializer ser) throws IOException {
    ser.openElement(PATH);
    root.plan(data, ser);
    ser.closeElement();
  }
}
