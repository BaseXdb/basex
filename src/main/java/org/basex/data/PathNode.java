package org.basex.data;

import static org.basex.data.DataText.*;
import java.io.IOException;
import java.util.ArrayList;
import org.basex.core.Text;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class represents a node of the path summary.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class PathNode {
  /** Tag/attribute name reference. */
  public final short name;
  /** Node kind, defined in the {@link Data} class. */
  public final byte kind;
  /** Parent. */
  public final PathNode par;
  /** Counter. */
  public int size;
  /** Children. */
  PathNode[] ch;

  /**
   * Default constructor.
   * @param t tag
   * @param k node kind
   * @param p parent node
   */
  PathNode(final int t, final byte k, final PathNode p) {
    ch = new PathNode[0];
    size = 1;
    name = (short) t;
    kind = k;
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
    size = in.readNum();
    ch = new PathNode[in.readNum()];
    in.readDouble();
    par = p;
    for(int i = 0; i < ch.length; i++) ch[i] = new PathNode(in, this);
  }

  /**
   * Returns a node reference for the specified tag.
   * @param t tag
   * @param k node kind
   * @return node reference
   */
  PathNode get(final int t, final byte k) {
    for(final PathNode c : ch) {
      if(c.kind == k && c.name == t) {
        c.size++;
        return c;
      }
    }
    final PathNode n = new PathNode(t, k, this);
    final int cs = ch.length;
    final PathNode[] tmp = new PathNode[cs + 1];
    System.arraycopy(ch, 0, tmp, 0, cs);
    tmp[cs] = n;
    ch = tmp;
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
    out.writeNum(size);
    out.writeNum(ch.length);
    out.writeDouble(0);
    for(final PathNode c : ch) c.finish(out);
  }

  /**
   * Recursively adds the node and its descendants to the specified list.
   * @param nodes node list
   */
  void addDesc(final ArrayList<PathNode> nodes) {
    // [CG] DATA: check for duplicates?
    // if(!nodes.contains(nodes)) nodes.add(this);
    nodes.add(this);
    for(final PathNode n : ch) n.addDesc(nodes);
  }

  /**
   * Returns a readable representation of this node.
   * @param data data reference
   * @return completions
   */
  public byte[] token(final Data data) {
    switch(kind) {
      case Data.ELEM: return data.tags.key(name);
      case Data.ATTR: return Token.concat(ATT, data.atts.key(name));
      case Data.TEXT: return TEXT;
      case Data.COMM: return COMM;
      case Data.PI:   return PI;
      default:        return Token.EMPTY;
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

  /**
   * Prints a path summary node.
   * @param data data reference
   * @param l level
   * @return string representation
   */
  byte[] info(final Data data, final int l) {
    final TokenBuilder tb = new TokenBuilder();
    if(l != 0) tb.add(Text.NL);
    for(int i = 0; i < l << 1; i++) tb.add(' ');
    switch(kind) {
      case Data.DOC:  tb.add(DOC); break;
      case Data.ELEM: tb.add(data.tags.key(name)); break;
      case Data.TEXT: tb.add(TEXT); break;
      case Data.ATTR: tb.add(ATT); tb.add(data.atts.key(name)); break;
      case Data.COMM: tb.add(COMM); break;
      case Data.PI:   tb.add(PI); break;
    }
    tb.add(" " + size + "x");
    for(final PathNode p : ch) tb.add(p.info(data, l + 1));
    return tb.finish();
  }

  /**
   * Serializes the path node.
   * @param data data reference
   * @param ser serializer
   * @throws IOException I/O exception
   */
  void plan(final Data data, final Serializer ser) throws IOException {
    ser.openElement(NODE, KIND, TABLEKINDS[kind]);
    if(kind == Data.ELEM) {
      ser.attribute(NAME, data.tags.key(name));
    } else if(kind == Data.ATTR) {
      ser.attribute(NAME, Token.concat(ATT, data.atts.key(name)));
    }
    ser.text(Token.token(size));
    for(final PathNode p : ch) p.plan(data, ser);
    ser.closeElement();
  }
}
