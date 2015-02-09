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
 * This class represents a node of the path summary.
 *
 * @author BaseX Team 2005-15, BSD License
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

  /**
   * Empty constructor.
   */
  PathNode() {
    this(0, Data.DOC, null, 0);
  }

  /**
   * Default constructor.
   * @param name node name
   * @param kind node kind
   * @param parent parent node
   */
  private PathNode(final int name, final byte kind, final PathNode parent) {
    this(name, kind, parent, 1);
  }

  /**
   * Default constructor.
   * @param name node name
   * @param kind node kind
   * @param parent parent node
   * @param count counter
   */
  private PathNode(final int name, final byte kind, final PathNode parent, final int count) {
    children = new PathNode[0];
    this.name = (short) name;
    this.kind = kind;
    this.parent = parent;
    stats = new Stats();
    stats.count = count;
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
    final int count = in.readNum();
    final int cl = in.readNum();
    children = new PathNode[cl];
    if(in.readDouble() == 1) {
      // "1" indicates the format introduced with Version 7.1
      stats = new Stats(in);
    } else {
      // create old format
      stats = new Stats();
      stats.count = count;
    }
    parent = node;
    for(int c = 0; c < cl; ++c) children[c] = new PathNode(in, this);
  }

  /**
   * Indexes the specified name and its kind.
   * @param nm name id
   * @param knd node kind
   * @param value value
   * @param meta meta data
   * @return node reference
   */
  PathNode index(final int nm, final byte knd, final byte[] value, final MetaData meta) {
    for(final PathNode c : children) {
      if(c.kind == knd && c.name == nm) {
        if(value != null) c.stats.add(value, meta);
        c.stats.count++;
        return c;
      }
    }

    final PathNode node = new PathNode(nm, knd, this);
    if(value != null) node.stats.add(value, meta);

    final int cs = children.length;
    final PathNode[] nodes = new PathNode[cs + 1];
    System.arraycopy(children, 0, nodes, 0, cs);
    nodes[cs] = node;
    children = nodes;
    return node;
  }

  /**
   * Writes the node to the specified output stream.
   * @param out output stream
   * @throws IOException I/O exception
   */
  void write(final DataOutput out) throws IOException {
    out.writeNum(name);
    out.write1(kind);
    out.writeNum(0);
    out.writeNum(children.length);
    out.writeDouble(1);

    // update leaf flag
    boolean leaf = stats.isLeaf();
    for(final PathNode child : children) {
      leaf &= child.kind == Data.TEXT || child.kind == Data.ATTR;
    }
    stats.setLeaf(leaf);
    stats.write(out);
    for(final PathNode child : children) child.write(out);
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
   * Returns a string representation of a path summary node.
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
