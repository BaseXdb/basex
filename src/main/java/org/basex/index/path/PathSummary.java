package org.basex.index.path;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.data.Data;
import org.basex.index.Index;
import org.basex.index.IndexIterator;
import org.basex.index.IndexToken;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.io.serial.Serializer;
import org.basex.util.Array;
import org.basex.util.Util;
import org.basex.util.list.ObjList;
import org.basex.util.list.TokenList;

/**
 * This class stores the path summary of a database.
 * It contains all unique location paths.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class PathSummary implements Index {
  /** Node stack for building the summary. */
  private final ObjList<PathNode> stack = new ObjList<PathNode>();
  /** Data reference. */
  private Data data;
  /** Root node. */
  private PathNode root;

  /**
   * Constructor, specifying a data reference.
   * @param d data reference
   */
  public PathSummary(final Data d) {
    data = d;
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
   * Writes the path summary to the specified output.
   * @param out output stream
   * @throws IOException I/O exception
   */
  public void write(final DataOutput out) throws IOException {
    out.writeBool(root != null);
    if(root != null) root.write(out);
  }

  /**
   * Sets the data reference.
   * @param d reference
   */
  public void finish(final Data d) {
    data = d;
  }

  @Override
  public void close() {
    root = null;
  }

  // Traversal ================================================================

  /**
   * Adds an entry.
   * @param n name reference
   * @param k node kind
   * @param l current level
   */
  public void index(final int n, final byte k, final int l) {
    if(root == null) {
      root = new PathNode(n, k, null);
      stack.add(root);
    } else {
      stack.set(l, stack.get(l - 1).index(n, k));
    }
  }

  /**
   * Returns the root node.
   * @return root node
   */
  public ObjList<PathNode> root() {
    final ObjList<PathNode> out = new ObjList<PathNode>();
    out.add(root);
    return out;
  }

  /**
   * Returns all parents of the specified nodes.
   * @param in input nodes
   * @return parent nodes
   */
  public ObjList<PathNode> parent(final ObjList<PathNode> in) {
    final ObjList<PathNode> out = new ObjList<PathNode>();
    for(final PathNode n : in) if(!out.contains(n.par)) out.add(n.par);
    return out;
  }

  /**
   * Returns all children or descendants of the specified nodes.
   * @param in input nodes
   * @param desc if false, return only children
   * @return descendant nodes
   */
  public ObjList<PathNode> desc(final ObjList<PathNode> in,
      final boolean desc) {

    final ObjList<PathNode> out = new ObjList<PathNode>();
    for(final PathNode n : in) {
      for(final PathNode c : n.ch) {
        if(desc) c.addDesc(out);
        else if(!out.contains(c)) out.add(c);
      }
    }
    return out;
  }

  /**
   * Returns all children or descendants of the specified nodes with the
   * specified tag or attribute value.
   * @param n name reference
   * @param k node kind
   * @return descendant nodes
   */
  public ObjList<PathNode> desc(final int n, final int k) {
    final ObjList<PathNode> out = new ObjList<PathNode>();
    for(final PathNode c : root.ch) c.addDesc(out, n, k);
    return out;
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
   * @param tl input steps
   * @param d if false, return only children
   * @param o true/false: sort by occurrence/lexicographically
   * @return children
   */
  public TokenList desc(final TokenList tl, final boolean d, final boolean o) {
    // follow the specified descendant/child steps
    ObjList<PathNode> in = desc(root(), true);

    for(final byte[] i : tl) {
      final boolean att = startsWith(i, '@');
      final byte kind = att ? Data.ATTR : Data.ELEM;
      final int id = att ? data.atnindex.id(substring(i, 1)) :
        data.tagindex.id(i);

      final ObjList<PathNode> out = new ObjList<PathNode>();
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
    for(int i = 0; i < in.size(); ++i) tmp[i] = in.get(i).size;
    final int[] occ = Array.createOrder(tmp, false);

    // remove non-text/attribute nodes
    final TokenList out = new TokenList();
    for(int i = 0; i < in.size(); ++i) {
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
   * Adds nodes to the hash set if they comply to the specified arguments.
   * @param in input node
   * @param out output nodes
   * @param t name reference
   * @param k node kind
   * @param desc if false, return only children
   */
  public void add(final PathNode in, final ObjList<PathNode> out,
      final int t, final int k, final boolean desc) {

    for(final PathNode n : in.ch) {
      if(desc) add(n, out, t, k, desc);
      if(k == -1 && n.kind != Data.ATTR || k == n.kind &&
          (t == 0 || t == n.name)) {
        out.add(n);
      }
    }
  }

  // Info =====================================================================

  @Override
  public byte[] info() {
    return root != null ? chop(root.info(data, 0), 1 << 20) : EMPTY;
  }

  /**
   * Serializes the path node.
   * @param ser serializer
   * @throws IOException I/O exception
   */
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(PATH);
    root.plan(data, ser);
    ser.closeElement();
  }

  // Unsupported methods ======================================================

  @Override
  public IndexIterator iter(final IndexToken token) {
    throw Util.notexpected();
  }

  @Override
  public int count(final IndexToken token) {
    throw Util.notexpected();
  }
}
