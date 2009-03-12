package org.basex.data;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.ArrayList;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.util.Array;

/**
 * This class represents a node of the path summary.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class PathNode {
  /** Tag/attribute name reference. */
  public final short name;
  /** Node kind, defined in the {@link Data} class. */
  public final byte kind;
  /** Tag counter. */
  public int count;
  /** Parent. */
  public PathNode par;
  /** Children. */
  public PathNode[] ch;
  /** Length of text. */
  public double tl;

  /**
   * Default Constructor.
   * @param t tag
   * @param k node kind
   * @param p parent node
   */
  PathNode(final int t, final byte k, final PathNode p) {
    this(t, k, 0, p);
  }

  /**
   * Default Constructor.
   * @param t tag
   * @param k node kind
   * @param l text length
   * @param p parent node
   */
  PathNode(final int t, final byte k, final int l, final PathNode p) {
    ch = new PathNode[0];
    count = 1;
    name = (short) t;
    kind = k;
    tl = l;
    par = p;
  }

  /**
   * Constructor, specifying an input stream.
   * @param in input stream
   * @param p parent node
   * @throws IOException I/O exception
   */
  PathNode(final DataInput in, final PathNode p) throws IOException {
    name = (short) in.readNum();
    kind = in.readByte();
    count = in.readNum();
    ch = new PathNode[in.readNum()];
    tl = in.readDouble();
    par = p;
    for(int i = 0; i < ch.length; i++) ch[i] = new PathNode(in, this);
  }

  /**
   * Returns a node reference for the specified tag.
   * @param t tag
   * @param k node kind
   * @param l text length
   * @return node reference
   */
  PathNode get(final int t, final byte k, final int l) {
    for(final PathNode c : ch) {
      if(c.kind == k && c.name == t) {
        c.count++;
        c.tl += l; 
        return c;
      }
    }
    final PathNode n = new PathNode(t, k, l, this);
    ch = Array.add(ch, n);
    return n;
  }

  /**
   * Finishes the tree structure.
   * @param out output stream
   * @throws IOException I/O exception
   */
  void finish(final DataOutput out) throws IOException {
    out.writeNum(name);
    out.write1(kind);
    out.writeNum(count);
    out.writeNum(ch.length);
    out.writeDouble(tl);
    for(final PathNode c : ch) c.finish(out);
  }

  /**
   * Recursively adds the node and its descendants to the specified list.
   * @param nodes node list
   */
  public void desc(final ArrayList<PathNode> nodes) {
    nodes.add(this);
    for(final PathNode n : ch) n.desc(nodes);
  }

  /**
   * Returns a readable representation of this node.
   * @param data data reference
   * @return completions
   */
  public byte[] token(final Data data) {
    switch(kind) {
      case Data.ELEM: return data.tags.key(name);
      case Data.ATTR: return concat(ATT, data.atts.key(name));
      case Data.TEXT: return TEXT;
      case Data.COMM: return COMM;
      case Data.PI:   return PI;
      default:        return EMPTY;
    }
  }

  /***
   * Returns the level of the path node.
   * @return level
   */
  public int level() {
    PathNode sn = par;
    int c = 0;
    while(sn != null) {
      sn = sn.par;
      c++;
    }
    return c;
  }
  
  @Override
  public String toString() {
    return "Node[" + kind + ", " + name + ", " + ch.length
      + " Children, " + tl + "]";
  }
}
