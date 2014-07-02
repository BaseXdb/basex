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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class PathSummary implements Index {
  /** Node stack for building the summary. */
  private final ArrayList<PathNode> stack = new ArrayList<>();
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
   * @param data data reference
   */
  public PathSummary(final Data data) {
    this();
    this.data = data;
  }

  /**
   * Constructor, specifying an input file.
   * @param data data reference
   * @param in input stream
   * @throws IOException I/O exception
   */
  public PathSummary(final Data data, final DataInput in) throws IOException {
    this.root = in.readBool() ? new PathNode(in, null) : new PathNode();
    this.data = data;
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
   * @param dt reference
   */
  public void data(final Data dt) {
    data = dt;
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
   * @param name name reference (0 for nodes other than element and attributes)
   * @param kind node kind
   * @param level current level
   */
  public void put(final int name, final byte kind, final int level) {
    put(name, kind, level, null, null);
  }

  /**
   * Adds an entry, including its value.
   * @param name name reference (0 for nodes other than element and attributes)
   * @param kind node kind
   * @param level current level
   * @param value value
   * @param meta meta data
   */
  public void put(final int name, final byte kind, final int level, final byte[] value,
      final MetaData meta) {

    if(level == 0) {
      if(value != null) root.stats.add(value, meta);
      root.stats.count++;
    } else {
      while(level >= stack.size()) stack.add(null);
      stack.set(level, stack.get(level - 1).index(name, kind, value, meta));
    }
  }

  // Traverse Index ===========================================================

  /**
   * Returns the root node.
   * @return root node
   */
  public ArrayList<PathNode> root() {
    final ArrayList<PathNode> out = new ArrayList<>();
    out.add(root);
    return out;
  }

  /**
   * Returns all parents of the specified nodes.
   * Used by the query optimizers.
   * @param nodes input nodes
   * @return parent nodes
   */
  public static ArrayList<PathNode> parent(final ArrayList<PathNode> nodes) {
    final ArrayList<PathNode> out = new ArrayList<>();
    for(final PathNode node : nodes) if(!out.contains(node.parent)) out.add(node.parent);
    return out;
  }

  /**
   * Returns all children or descendants of the specified nodes.
   * Called from the query parser and optimizer.
   * @param nodes input nodes
   * @param desc if false, return only children
   * @return descendant nodes
   */
  public static ArrayList<PathNode> desc(final ArrayList<PathNode> nodes, final boolean desc) {
    final ArrayList<PathNode> out = new ArrayList<>();
    for(final PathNode node : nodes) {
      for(final PathNode child : node.children) {
        if(desc) child.addDesc(out);
        else if(!out.contains(child)) out.add(child);
      }
    }
    return out;
  }

  /**
   * Returns all children or descendants of the specified nodes with the
   * specified tag or attribute value. Called from the query optimizer.
   * @param name name reference
   * @param kind node kind
   * @return descendant nodes
   */
  public ArrayList<PathNode> desc(final int name, final int kind) {
    final ArrayList<PathNode> nodes = new ArrayList<>();
    for(final PathNode child : root.children) child.addDesc(nodes, name, kind);
    return nodes;
  }

  /**
   * Returns descendant element and attribute names for the specified start key.
   * Used by the GUI.
   * @param name input key
   * @param desc if false, return only children
   * @param occ true/false: sort by occurrence/lexicographically
   * @return names
   */
  public TokenList desc(final byte[] name, final boolean desc, final boolean occ) {
    final TokenList names = new TokenList();
    if(name.length != 0) names.add(name);
    return desc(names, desc, occ);
  }

  /**
   * Returns descendant element and attribute names for the specified descendant path.
   * Used by the GUI.
   * @param names input steps
   * @param desc if false, return only children
   * @param occ true/false: sort by occurrence/lexicographically
   * @return children
   */
  public TokenList desc(final TokenList names, final boolean desc, final boolean occ) {
    // follow the specified descendant/child steps
    ArrayList<PathNode> nodes = desc(root(), true);

    for(final byte[] name : names) {
      final boolean attr = startsWith(name, '@');
      final byte kind = attr ? Data.ATTR : Data.ELEM;
      final int id = attr ? data.atnindex.id(substring(name, 1)) : data.tagindex.id(name);

      final ArrayList<PathNode> tmp = new ArrayList<>();
      for(final PathNode node : nodes) {
        if(node.name != id || node.kind != kind) continue;
        for(final PathNode child : node.children) {
          if(desc) child.addDesc(tmp);
          else tmp.add(child);
        }
      }
      nodes = tmp;
    }

    // sort by number of occurrences
    final int[] tmp = new int[nodes.size()];
    for(int i = 0; i < nodes.size(); ++i) tmp[i] = nodes.get(i).stats.count;
    final int[] occs = Array.createOrder(tmp, false);

    // remove non-text/attribute nodes
    final TokenList list = new TokenList();
    final int ns = nodes.size();
    for(int n = 0; n < ns; n++) {
      final PathNode node = nodes.get(occ ? occs[n] : n);
      final byte[] name = node.token(data);
      if(name.length != 0 && !list.contains(name) && !contains(name, '(')) {
        list.add(name);
      }
    }
    if(!occ) list.sort(false);
    return list;
  }

  // Info =====================================================================

  @Override
  public byte[] info() {
    return root != null ? chop(root.info(data, 0), 1 << 20) : EMPTY;
  }

  // Unsupported methods ======================================================

  @Override
  public IndexIterator iter(final IndexToken token) {
    throw Util.notExpected();
  }

  @Override
  public int costs(final IndexToken token) {
    throw Util.notExpected();
  }

  @Override
  public EntryIterator entries(final IndexEntries entries) {
    throw Util.notExpected();
  }

  @Override
  public String toString() {
    return string(info());
  }
}
