package org.basex.index.path;

import static org.basex.data.DataText.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.name.*;
import org.basex.index.stats.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class represents a node of the path index.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class PathNode {
  /** Element/attribute name. */
  public final short name;
  /** Node kind, defined in the {@link Data} class. */
  public final byte kind;
  /** Parent. */
  public final PathNode parent;
  /** Children. */
  public PathNode[] children;
  /** Node kind. */
  public final Stats stats;

  /** Empty element flag,assigned during index construction.
   *  0: no empty elements;
   *  1: test flag;
   *  2: element can be empty. */
  private byte empty;

  /**
   * Empty constructor.
   */
  PathNode() {
    this(0, Data.DOC, null);
  }

  /**
   * Default constructor.
   * @param name ID of node name
   * @param kind node kind
   * @param parent parent node
   */
  private PathNode(final int name, final byte kind, final PathNode parent) {
    children = new PathNode[0];
    this.name = (short) name;
    this.kind = kind;
    this.parent = parent;
    stats = new Stats();
  }

  /**
   * Constructor, specifying an input stream. The children of the node are not read yet.
   * @param in input stream
   * @param node parent node (can be {@code null})
   * @throws IOException I/O exception
   */
  private PathNode(final DataInput in, final PathNode node) throws IOException {
    name = (short) in.readNum();
    kind = (byte) in.read();
    in.readNum();
    children = new PathNode[in.readNum()];
    in.readDouble();
    stats = new Stats(in);
    parent = node;
  }

  /**
   * Reads a node and its descendants from the specified input stream.
   * @param in input stream
   * @return node
   * @throws IOException I/O exception
   */
  static PathNode read(final DataInput in) throws IOException {
    // iterative traversal: the depth of the path index is not limited
    final PathNode root = new PathNode(in, null);
    final ArrayList<PathNode> stack = new ArrayList<>();
    final IntList indexes = new IntList();
    PathNode node = root;
    for(int i = 0;;) {
      if(i < node.children.length) {
        stack.add(node);
        indexes.push(i + 1);
        final PathNode child = new PathNode(in, node);
        node.children[i] = child;
        node = child;
        i = 0;
      } else {
        if(stack.isEmpty()) return root;
        node = stack.remove(stack.size() - 1);
        i = indexes.pop();
      }
    }
  }

  /**
   * Indexes the specified name and its kind.
   * @param id name ID
   * @param knd node kind
   * @param value value (can be {@code null})
   * @param meta meta data
   * @return node reference
   */
  PathNode index(final int id, final byte knd, final byte[] value, final MetaData meta) {
    final PathNode found = child(id, knd);
    if(found != null) {
      found.index(value, meta);
      return found;
    }

    final PathNode child = new PathNode(id, knd, this);
    child.index(value, meta);

    final int cl = children.length;
    final PathNode[] nodes = new PathNode[cl + 1];
    Array.copy(children, cl, nodes);
    nodes[cl] = child;
    children = nodes;
    return child;
  }

  /**
   * Indexes a value.
   * @param value value (can be {@code null})
   * @param meta meta data
   */
  private void index(final byte[] value, final MetaData meta) {
    if(value == null) {
      // opening element
      if(kind == Data.ELEM) {
        // check if this is an empty element: set test flag
        if(empty == 0) empty = 1;
        // confirm that this element can be empty
        else if(empty == 1) empty = 2;
      }
    } else {
      stats.add(value, meta);
      // text node: invalidate test flag of parent node
      if(kind == Data.TEXT && parent.empty == 1) parent.empty = 0;
    }
    stats.count++;
  }

  /**
   * Finalizes the node: assigns the leaf flag and the string value of empty elements.
   * @param meta meta data
   * @param elemNames element names
   */
  void finish(final MetaData meta, final Names elemNames) {
    final ArrayList<PathNode> nodes = new ArrayList<>();
    addDesc(nodes, -1);
    for(final PathNode node : nodes) {
      boolean leaf = node.stats.isLeaf();
      for(final PathNode child : node.children) {
        if(child.kind == Data.TEXT) {
          if(node.empty != 0) child.stats.add(Token.EMPTY, meta);
        } else if(child.kind != Data.ATTR) {
          leaf = false;
        }
      }
      // an element without a text node child has an empty string value
      if(node.empty != 0 && node.kind == Data.ELEM) {
        elemNames.createStats(node.name).add(Token.EMPTY, meta);
      }
      // reset flag: empty values are only to be added once
      node.empty = 0;

      node.stats.setLeaf(leaf);
    }
  }

  /**
   * Writes the node to the specified output stream.
   * @param out output stream
   * @throws IOException I/O exception
   */
  void write(final DataOutput out) throws IOException {
    // nodes are written in document order
    final ArrayList<PathNode> nodes = new ArrayList<>();
    addDesc(nodes, -1);
    for(final PathNode node : nodes) {
      out.writeNum(node.name);
      out.write1(node.kind);
      // legacy (required before version 7.1)
      out.writeNum(0);
      out.writeNum(node.children.length);
      // legacy (required before version 7.1)
      out.writeDouble(1);

      node.stats.write(out);
    }
  }

  /**
   * Returns the child with the specified name and kind.
   * @param nm name ID
   * @param knd node kind
   * @return child, or {@code null} if it does not exist
   */
  PathNode child(final int nm, final int knd) {
    for(final PathNode child : children) {
      if(child.kind == knd && child.name == nm) return child;
    }
    return null;
  }

  /**
   * Adds the node and its descendants, or the elements with the specified name, to a list.
   * @param nodes node list
   * @param nm name ID of the elements ({@code -1}: all nodes)
   */
  void addDesc(final ArrayList<PathNode> nodes, final int nm) {
    // iterative traversal: the depth of the path index is not limited
    final ArrayList<PathNode> stack = new ArrayList<>();
    stack.add(this);
    while(!stack.isEmpty()) {
      final PathNode node = stack.remove(stack.size() - 1);
      if(nm == -1 || node.kind == Data.ELEM && nm == node.name) nodes.add(node);
      // add the children in reverse order: the nodes are returned in document order
      for(int c = node.children.length - 1; c >= 0; c--) stack.add(node.children[c]);
    }
  }

  /**
   * Returns a readable representation of this node.
   * @param data data reference
   * @return completions
   */
  public byte[] token(final Data data) {
    return switch(kind) {
      case Data.ELEM -> data.elemNames.key(name);
      case Data.ATTR -> Token.concat(XMLToken.AT, data.attrNames.key(name));
      case Data.TEXT -> TEXT;
      case Data.COMM -> COMMENT;
      case Data.PI   -> PI;
      default        -> Token.EMPTY;
    };
  }

  /**
   * Returns the level of the path node.
   * @return level
   */
  public int level() {
    PathNode pn = parent;
    int c = 0;
    while(pn != null) {
      pn = pn.parent;
      ++c;
    }
    return c;
  }

  /**
   * Returns a string representation of a path index node.
   * @param data data reference
   * @param level level
   * @return string representation
   */
  byte[] info(final Data data, final int level) {
    final TokenBuilder tb = new TokenBuilder();
    if(level != 0) tb.add(Text.NL);
    for(int i = 0; i < level * 2; i++) tb.add(' ');
    tb.add(kind == Data.DOC ? DOC : token(data)).add(": " + stats);
    for(final PathNode p : children) tb.add(p.info(data, level + 1));
    return tb.finish();
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + name + ':' + kind + ']';
  }
}
