package org.basex.index.path;

import static org.basex.data.DataText.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.stats.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;

/**
 * This class represents a node of the path index.
 *
 * @author BaseX Team 2005-21, BSD License
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
   * @param name id of node name
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
   * Constructor, specifying an input stream.
   * @param in input stream
   * @param node parent node
   * @throws IOException I/O exception
   */
  PathNode(final DataInput in, final PathNode node) throws IOException {
    name = (short) in.readNum();
    kind = (byte) in.read();
    in.readNum();
    final int cl = in.readNum();
    in.readDouble();
    children = new PathNode[cl];
    stats = new Stats(in);
    parent = node;
    for(int c = 0; c < cl; ++c) children[c] = new PathNode(in, this);
  }

  /**
   * Indexes the specified name and its kind.
   * @param id name id
   * @param knd node kind
   * @param value value (can be {@code null})
   * @param meta meta data
   * @return node reference
   */
  PathNode index(final int id, final byte knd, final byte[] value, final MetaData meta) {
    for(final PathNode child : children) {
      if(child.kind == knd && child.name == id) {
        child.index(value, meta);
        return child;
      }
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
   * Finalizes the index and writes the node to the specified output stream.
   * @param out output stream
   * @param meta meta data
   * @throws IOException I/O exception
   */
  void write(final DataOutput out, final MetaData meta) throws IOException {
    out.writeNum(name);
    out.write1(kind);
    // legacy (required before version 7.1)
    out.writeNum(0);
    out.writeNum(children.length);
    // legacy (required before version 7.1)
    out.writeDouble(1);

    // update leaf flag
    boolean leaf = stats.isLeaf();
    for(final PathNode child : children) {
      if(child.kind == Data.TEXT) {
        if(empty != 0) child.stats.add(Token.EMPTY, meta);
      } else if(child.kind != Data.ATTR) {
        leaf = false;
      }
    }

    stats.setLeaf(leaf);
    stats.write(out);
    for(final PathNode child : children) child.write(out, meta);
  }

  /**
   * Recursively adds the node and its descendants to the specified list.
   * @param nodes node list
   */
  void addDesc(final ArrayList<PathNode> nodes) {
    nodes.add(this);
    for(final PathNode child : children) child.addDesc(nodes);
  }

  /**
   * Recursively adds the node and its descendants to the specified list with the specified name.
   * @param nodes node list
   * @param nm name id
   */
  void addDesc(final ArrayList<PathNode> nodes, final int nm) {
    if(kind == Data.ELEM && nm == name) nodes.add(this);
    for(final PathNode child : children) child.addDesc(nodes, nm);
  }

  /**
   * Returns a readable representation of this node.
   * @param data data reference
   * @return completions
   */
  public byte[] token(final Data data) {
    switch(kind) {
      case Data.ELEM: return data.elemNames.key(name);
      case Data.ATTR: return Token.concat(ATT, data.attrNames.key(name));
      case Data.TEXT: return TEXT;
      case Data.COMM: return COMMENT;
      case Data.PI:   return PI;
      default:        return Token.EMPTY;
    }
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
    for(int i = 0; i < level << 1; ++i) tb.add(' ');
    switch(kind) {
      case Data.DOC:  tb.add(DOC); break;
      case Data.ELEM: tb.add(data.elemNames.key(name)); break;
      case Data.TEXT: tb.add(TEXT); break;
      case Data.ATTR: tb.add(ATT); tb.add(data.attrNames.key(name)); break;
      case Data.COMM: tb.add(COMMENT); break;
      case Data.PI:   tb.add(PI); break;
    }
    tb.add(": " + stats);
    for(final PathNode p : children) tb.add(p.info(data, level + 1));
    return tb.finish();
  }
}
