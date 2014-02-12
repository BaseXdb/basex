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
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class PathNode {
  /** Tag/attribute name id. */
  public final short name;
  /** Node kind, defined in the {@link Data} class. */
  public final byte kind;
  /** Parent. */
  public final PathNode par;
  /** Children. */
  public PathNode[] ch;
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
   * @param n node name
   * @param k node kind
   * @param p parent node
   */
  private PathNode(final int n, final byte k, final PathNode p) {
    this(n, k, p, 1);
  }

  /**
   * Default constructor.
   * @param n node name
   * @param k node kind
   * @param p parent node
   * @param c counter
   */
  private PathNode(final int n, final byte k, final PathNode p, final int c) {
    ch = new PathNode[0];
    name = (short) n;
    kind = k;
    par = p;
    stats = new Stats();
    stats.count = c;
  }

  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   * @param p parent node
   * @throws IOException I/O exception
   */
  PathNode(final DataInput in, final PathNode p) throws IOException {
    name = (short) in.readNum();
    kind = (byte) in.read();
    final int s = in.readNum();
    ch = new PathNode[in.readNum()];
    if(in.readDouble() == 1) {
      // "1" indicates the format introduced with Version 7.1
      stats = new Stats(in);
    } else {
      // create old format
      stats = new Stats();
      stats.count = s;
    }
    par = p;
    for(int i = 0; i < ch.length; ++i) ch[i] = new PathNode(in, this);
  }

  /**
   * Indexes the specified name along with its kind.
   * @param n name id
   * @param k node kind
   * @param v value
   * @param md meta data
   * @return node reference
   */
  PathNode index(final int n, final byte k, final byte[] v, final MetaData md) {
    for(final PathNode c : ch) {
      if(c.kind == k && c.name == n) {
        if(v != null) c.stats.add(v, md);
        c.stats.count++;
        return c;
      }
    }

    final PathNode pn = new PathNode(n, k, this);
    if(v != null) pn.stats.add(v, md);

    final int cs = ch.length;
    final PathNode[] tmp = new PathNode[cs + 1];
    System.arraycopy(ch, 0, tmp, 0, cs);
    tmp[cs] = pn;
    ch = tmp;
    return pn;
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
    out.writeNum(ch.length);
    out.writeDouble(1);

    // update leaf flag
    boolean leaf = stats.isLeaf();
    for(final PathNode c : ch) {
      leaf &= c.kind == Data.TEXT || c.kind == Data.ATTR;
    }
    stats.setLeaf(leaf);
    stats.write(out);
    for(final PathNode c : ch) c.write(out);
  }

  /**
   * Recursively adds the node and its descendants to the specified list.
   * @param nodes node list
   */
  void addDesc(final ArrayList<PathNode> nodes) {
    nodes.add(this);
    for(final PathNode n : ch) n.addDesc(nodes);
  }

  /**
   * Recursively adds the node and its descendants to the specified list
   * with the specified name.
   * @param nodes node list
   * @param n name id
   * @param k node kind
   */
  void addDesc(final ArrayList<PathNode> nodes, final int n, final int k) {
    if(n == name && k == kind) nodes.add(this);
    for(final PathNode pn : ch) pn.addDesc(nodes, n, k);
  }

  /**
   * Returns a readable representation of this node.
   * @param data data reference
   * @return completions
   */
  public byte[] token(final Data data) {
    switch(kind) {
      case Data.ELEM: return data.tagindex.key(name);
      case Data.ATTR: return Token.concat(ATT, data.atnindex.key(name));
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
    PathNode pn = par;
    int c = 0;
    while(pn != null) {
      pn = pn.par;
      ++c;
    }
    return c;
  }

  /**
   * Returns a string representation of a path summary node.
   * @param data data reference
   * @param l level
   * @return string representation
   */
  byte[] info(final Data data, final int l) {
    final TokenBuilder tb = new TokenBuilder();
    if(l != 0) tb.add(Text.NL);
    for(int i = 0; i < l << 1; ++i) tb.add(' ');
    switch(kind) {
      case Data.DOC:  tb.add(DOC); break;
      case Data.ELEM: tb.add(data.tagindex.key(name)); break;
      case Data.TEXT: tb.add(TEXT); break;
      case Data.ATTR: tb.add(ATT); tb.add(data.atnindex.key(name)); break;
      case Data.COMM: tb.add(COMMENT); break;
      case Data.PI:   tb.add(PI); break;
    }
    tb.add(": " + stats);
    for(final PathNode p : ch) tb.add(p.info(data, l + 1));
    return tb.finish();
  }
}
