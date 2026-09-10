package org.basex.index.path;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.name.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class stores the path summary of a database.
 * It contains all unique location paths.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class PathIndex implements Index {
  /** Node stack for building the summary. */
  private final ArrayList<PathNode> stack = new ArrayList<>();
  /** Data reference. */
  private Data data;
  /** Root node (can be {@code null}). */
  private PathNode root;

  /**
   * Constructor.
   * The {@link Data} reference must be set in a second step via {@link #data(Data)}.
   */
  public PathIndex() {
    init();
  }

  /**
   * Constructor, specifying a data reference.
   * @param data data reference
   */
  public PathIndex(final Data data) {
    this();
    this.data = data;
  }

  /**
   * Constructor, specifying an input file.
   * @param data data reference
   * @param in input stream
   * @throws IOException I/O exception
   */
  public PathIndex(final Data data, final DataInput in) throws IOException {
    root = in.readBool() ? PathNode.read(in) : new PathNode();
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
   * Finalizes the index: assigns leaf flags and the string values of empty elements.
   * @param meta meta data
   * @param elemNames element names
   */
  public void finish(final MetaData meta, final Names elemNames) {
    root.finish(meta, elemNames);
  }

  /**
   * Sets the data reference.
   * @param dt reference
   */
  public void data(final Data dt) {
    data = dt;
  }

  /**
   * Initializes the index.
   */
  public void init() {
    root = new PathNode();
    stack.clear();
    stack.add(root);
  }

  @Override
  public void close() { }

  // Build Index ==================================================================================

  /**
   * Adds an element or document node.
   * @param name name ID ({@code 0} for nodes other than elements and attributes)
   * @param kind node kind
   * @param level current level
   */
  public void index(final int name, final byte kind, final int level) {
    index(name, kind, level, null, null);
  }

  /**
   * Adds an entry, including its value.
   * @param name name ID ({@code 0} for nodes other than elements and attributes)
   * @param kind node kind
   * @param level current level
   * @param value value ({@code null} for element or document nodes)
   * @param meta meta data (ignored if value is {@code null})
   */
  public void index(final int name, final byte kind, final int level, final byte[] value,
      final MetaData meta) {

    if(level == 0) {
      final Stats stats = root.stats;
      if(value != null) stats.add(value, meta);
      stats.count++;
    } else {
      while(level >= stack.size()) stack.add(null);
      stack.set(level, stack.get(level - 1).index(name, kind, value, meta));
    }
  }

  /**
   * Adds an entry to a known parent node.
   * @param node parent node ({@code null} for document nodes)
   * @param name name ID ({@code 0} for nodes other than elements and attributes)
   * @param kind node kind
   * @param value value ({@code null} for element and document nodes)
   * @return path node of the new entry
   */
  public PathNode index(final PathNode node, final int name, final byte kind, final byte[] value) {
    if(node == null) {
      root.stats.count++;
      return root;
    }
    // a new text node proves that earlier elements of this path had no text node child
    final boolean empty = kind == Data.TEXT && node.kind == Data.ELEM &&
        node.stats.count > 1 && child(node, name, kind) == null;
    final PathNode child = node.index(name, kind, value, data.meta);
    if(empty) child.stats.add(Token.EMPTY, data.meta);
    return child;
  }

  /**
   * Returns the path node that represents the specified database node.
   * @param pre PRE value
   * @return path node, or {@code null} if the node is not indexed
   */
  public PathNode node(final int pre) {
    // collect ancestor-or-self nodes
    final IntList pres = new IntList();
    for(int p = pre; p >= 0; p = data.parent(p, data.kind(p))) pres.add(p);
    // the path index is rooted in the document node
    if(pres.isEmpty() || data.kind(pres.peek()) != Data.DOC) return null;

    PathNode node = root;
    for(int p = pres.size() - 2; p >= 0 && node != null; p--) {
      final int curr = pres.get(p), kind = data.kind(curr);
      final int name = kind == Data.ELEM || kind == Data.ATTR ? data.nameId(curr) : 0;
      node = child(node, name, kind);
    }
    return node;
  }

  /**
   * Returns the child of a path node with the specified name and kind.
   * @param node path node
   * @param name name ID
   * @param kind node kind
   * @return child node, or {@code null} if no child exists
   */
  private static PathNode child(final PathNode node, final int name, final int kind) {
    for(final PathNode child : node.children) {
      if(child.kind == kind && child.name == name) return child;
    }
    return null;
  }

  // Traverse Index ===============================================================================

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
   * Called by the query optimizer.
   * @param nodes input nodes
   * @return parent nodes
   */
  public static ArrayList<PathNode> parent(final ArrayList<PathNode> nodes) {
    final ArrayList<PathNode> out = new ArrayList<>();
    for(final PathNode node : nodes) {
      if(!out.contains(node.parent)) out.add(node.parent);
    }
    return out;
  }

  /**
   * Returns all children or descendants of the specified nodes.
   * Called by the query parser and optimizer.
   * @param nodes input nodes
   * @param desc if false, return only children
   * @return descendant nodes
   */
  public static ArrayList<PathNode> desc(final ArrayList<PathNode> nodes, final boolean desc) {
    final ArrayList<PathNode> list = new ArrayList<>();
    for(final PathNode node : nodes) {
      for(final PathNode child : node.children) {
        if(desc) child.addDesc(list);
        else if(!list.contains(child)) list.add(child);
      }
    }
    return list;
  }

  /**
   * Returns all descendants with the specified element name.
   * Called by the query optimizer.
   * @param name local name
   * @return descendant nodes
   */
  public ArrayList<PathNode> desc(final byte[] name) {
    final int id = data.elemNames.index(name);
    final ArrayList<PathNode> list = new ArrayList<>();
    for(final PathNode child : root.children) child.addDesc(list, id);
    return list;
  }

  /**
   * Returns descendant element and attribute names for the specified start key.
   * Called by the GUI.
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
   * Called by the GUI.
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
      final int id = attr ? data.attrNames.index(substring(name, 1)) : data.elemNames.index(name);

      final ArrayList<PathNode> list = new ArrayList<>();
      for(final PathNode node : nodes) {
        if(node.name != id || node.kind != kind) continue;
        for(final PathNode child : node.children) {
          if(desc) child.addDesc(list);
          else list.add(child);
        }
      }
      nodes = list;
    }

    // sort by number of occurrences
    final int ns = nodes.size();
    final int[] tmp = new int[ns];
    for(int i = 0; i < ns; i++) tmp[i] = nodes.get(i).stats.count;
    final int[] occs = Array.createOrder(tmp, false);

    // remove non-text/attribute nodes
    final TokenList tl = new TokenList();
    for(int n = 0; n < ns; n++) {
      final PathNode node = nodes.get(occ ? occs[n] : n);
      final byte[] name = node.token(data);
      if(name.length != 0 && !tl.contains(name) && !contains(name, '(')) tl.add(name);
    }
    if(!occ) tl.sort(false);
    return tl;
  }

  // Info =========================================================================================

  @Override
  public byte[] info(final MainOptions options) {
    return root != null ? chop(root.info(data, 0), 1 << 20) : EMPTY;
  }

  // Unsupported methods ==========================================================================

  @Override
  public boolean drop() {
    throw Util.notExpected();
  }

  @Override
  public IndexIterator iter(final IndexSearch search) {
    throw Util.notExpected();
  }

  @Override
  public IndexCosts costs(final IndexSearch search) {
    throw Util.notExpected();
  }

  @Override
  public EntryIterator entries(final IndexEntries entries) {
    throw Util.notExpected();
  }

  @Override
  public String toString() {
    return string(info(null));
  }
}
