package org.basex.index.path;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class stores the path summary of a database.
 * It contains all unique location paths.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class PathSummary implements Index {
  /** Node stack for building the summary. */
  private final ArrayList<PathNode> stack = new ArrayList<PathNode>();
  /** Data reference. */
  private Data data;
  /** Root node. */
  private PathNode root;

  /**
   * Constructor.
   * The {@link Data} reference must be set in a second step via {@link #data(Data)}.
   */
  public PathSummary() {
    init();
  }

  /**
   * Constructor, specifying a data reference.
   * @param d data reference
   */
  public PathSummary(final Data d) {
    this();
    data = d;
  }

  /**
   * Constructor, specifying an input file.
   * @param d data reference
   * @param in input stream
   * @throws IOException I/O exception
   */
  public PathSummary(final Data d, final DataInput in) throws IOException {
    root = in.readBool() ? new PathNode(in, null) : new PathNode();
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
  public void data(final Data d) {
    data = d;
  }

  @Override
  public void init() {
    root = new PathNode();
    stack.clear();
    stack.add(root);
  }

  @Override
  public void close() { }

  // Build Index ==============================================================

  /**
   * Adds an entry.
   * @param n name reference (0 for nodes other than element and attributes)
   * @param k node kind
   * @param l current level
   */
  public void put(final int n, final byte k, final int l) {
    put(n, k, l, null, null);
  }

  /**
   * Adds an entry, including its value.
   * @param n name reference (0 for nodes other than element and attributes)
   * @param k node kind
   * @param l current level
   * @param v value
   * @param md meta data
   */
  public void put(final int n, final byte k, final int l, final byte[] v,
      final MetaData md) {

    if(l == 0) {
      if(v != null) root.stats.add(v, md);
      root.stats.count++;
    } else {
      while(l >= stack.size()) stack.add(null);
      stack.set(l, stack.get(l - 1).index(n, k, v, md));
    }
  }

  // Traverse Index ===========================================================

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
   * Returns all parents of the specified nodes.
   * Used by the query optimizers.
   * @param in input nodes
   * @return parent nodes
   */
  public static ArrayList<PathNode> parent(final ArrayList<PathNode> in) {
    final ArrayList<PathNode> out = new ArrayList<PathNode>();
    for(final PathNode n : in) if(!out.contains(n.par)) out.add(n.par);
    return out;
  }

  /**
   * Returns all children or descendants of the specified nodes.
   * Called from the query parser and optimizer.
   * @param in input nodes
   * @param desc if false, return only children
   * @return descendant nodes
   */
  public static ArrayList<PathNode> desc(final ArrayList<PathNode> in,
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
   * Returns all children or descendants of the specified nodes with the
   * specified tag or attribute value. Called from the query optimizer.
   * @param n name reference
   * @param k node kind
   * @return descendant nodes
   */
  public ArrayList<PathNode> desc(final int n, final int k) {
    final ArrayList<PathNode> out = new ArrayList<PathNode>();
    for(final PathNode c : root.ch) c.addDesc(out, n, k);
    return out;
  }

  /**
   * Returns descendant tags and attributes for the specified start key.
   * Used by the GUI.
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
   * Used by the GUI.
   * @param tl input steps
   * @param d if false, return only children
   * @param o true/false: sort by occurrence/lexicographically
   * @return children
   */
  public TokenList desc(final TokenList tl, final boolean d, final boolean o) {
    // follow the specified descendant/child steps
    ArrayList<PathNode> in = desc(root(), true);

    for(final byte[] i : tl) {
      final boolean att = startsWith(i, '@');
      final byte kind = att ? Data.ATTR : Data.ELEM;
      final int id = att ? data.atnindex.id(substring(i, 1)) : data.tagindex.id(i);

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
    final int[] tmp = new int[in.size()];
    for(int i = 0; i < in.size(); ++i) tmp[i] = in.get(i).stats.count;
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

  // Info =====================================================================

  @Override
  public byte[] info() {
    return root != null ? chop(root.info(data, 0), 1 << 20) : EMPTY;
  }

  // Unsupported methods ======================================================

  @Override
  public IndexIterator iter(final IndexToken token) {
    throw Util.notexpected();
  }

  @Override
  public int costs(final IndexToken token) {
    throw Util.notexpected();
  }

  @Override
  public EntryIterator entries(final IndexEntries entries) {
    throw Util.notexpected();
  }

  @Override
  public String toString() {
    return string(info());
  }
}
