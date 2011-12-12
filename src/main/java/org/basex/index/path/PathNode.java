package org.basex.index.path;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.core.Text;
import org.basex.data.Data;
import org.basex.index.Kind;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.io.serial.Serializer;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.list.IntList;
import org.basex.util.list.ObjList;
import org.basex.util.list.TokenList;

/**
 * This class represents a node of the path summary.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Andreas Weiler
 */
public final class PathNode {

  /** Tag/attribute name reference. */
  public final short name;
  /** Node kind, defined in the {@link Data} class. */
  public final byte kind;
  /** Parent. */
  public final PathNode par;
  /** Number of occurrences. */
  public int size;
  /** Values of node. */
  public TokenList values;
  /** Counts of nodes. */
  public IntList vasize;
  /** Children. */
  public PathNode[] ch;
  /** TypeKind of node. */
  public Kind tkind;
  /** Maximum text length. */
  private double len;
  /** Minimum value. */
  public double min;
  /** Maximum value. */
  public double max;
  /** Maximum cats. */
  private int maxcats;
  /** Checker boolean. */
  private boolean check;

  /**
   * Default constructor.
   * @param t tag
   * @param k node kind
   * @param p parent node
   * @param m maxcats
   */
  PathNode(final int t, final byte k, final PathNode p, final int m) {
    ch = new PathNode[0];
    size = 1;
    values = new TokenList();
    vasize = new IntList();
    name = (short) t;
    kind = k;
    par = p;
    max = Double.MIN_VALUE;
    min = Double.MAX_VALUE;
    tkind = Kind.INT;
    maxcats = m;
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
    size = in.readNum();
    ch = new PathNode[in.readNum()];
    if(in.readDouble() == 1) {
      tkind = Kind.values()[in.readNum()];
      if(tkind == Kind.INT || tkind == Kind.DBL) {
        min = in.readDouble();
        max = in.readDouble();
      }
      len = in.readDouble();
      if(tkind == Kind.CAT) {
        values = new TokenList(in.readTokens());
        vasize = new IntList(in.readNums());
      }
    }
    par = p;
    for(int i = 0; i < ch.length; ++i) ch[i] = new PathNode(in, this);
  }

  /**
   * Indexes the specified name along with its kind.
   * @param n name reference
   * @param k node kind
   * @param val value
   * @return node reference
   */
  PathNode index(final int n, final byte k, final byte[] val) {
    for(final PathNode c : ch) {
      if(c.kind == k && c.name == n) {
        if(val != null && c.values.size() <= maxcats + 1) {
          int pos = c.values.containsi(val);
          if (pos > -1) {
            int t = c.vasize.get(pos) + 1;
            c.vasize.set(pos, t);
          } else {
            c.values.add(val);
            c.vasize.add(1);
          }
        }
        if(val != null) {
          c.check(val);
        }
        c.size++;
        return c;
      }
    }
    final PathNode pn = new PathNode(n, k, this, maxcats);
    if(val != null) {
      pn.values.add(val);
      pn.vasize.add(1);
      pn.check(val);
    }
    final int cs = ch.length;
    final PathNode[] tmp = new PathNode[cs + 1];
    System.arraycopy(ch, 0, tmp, 0, cs);
    tmp[cs] = pn;
    ch = tmp;
    return pn;
  }

  /**
   * Adds a value. All values are first treated as integer values. If a value
   * can't be converted to an integer, it is treated as double value. If
   * conversion fails again, it is handled as string category. Next, all values
   * are cached. As soon as their number exceeds {@link #maxcats}, the cached
   * values are skipped, and contents are treated as arbitrary strings.
   * @param val value to be added
   */
  public void check(final byte[] val) {
    final int vl = val.length;
    if(vl > len) len = vl;

    if(vl == 0 || tkind == Kind.TEXT || ws(val)) return;

    if(check && values.size() <= maxcats) {
      if(val.length > MAXLEN) {
        tkind = Kind.TEXT;
        check = false;
      }
    }
    if(tkind == Kind.INT) {
      final long d = toLong(val);
      if(d == Long.MIN_VALUE) {
        tkind = Kind.DBL;
      } else {
        if(min > d) min = d;
        if(max < d) max = d;
      }
    }
    if(tkind == Kind.DBL) {
      final double d = toDouble(val);
      if(Double.isNaN(d)) {
        tkind = values.size() <= maxcats ? Kind.CAT : Kind.TEXT;
      } else {
        if(min > d) min = d;
        if(max < d) max = d;
      }
    } else if(tkind == Kind.CAT) {
      if(values.size() > maxcats) {
        tkind = Kind.TEXT;
        check = false;
      }
    }
  }

  /**
   * Writes the node to the specified output.
   * @param out output stream
   * @throws IOException I/O exception
   */
  void write(final DataOutput out) throws IOException {
    out.writeNum(name);
    out.write1(kind);
    out.writeNum(size);
    out.writeNum(ch.length);
    out.writeDouble(1);
    out.writeNum(tkind.ordinal());
    if(tkind == Kind.INT || tkind == Kind.DBL) {
      out.writeDouble(min);
      out.writeDouble(max);
    }
    out.writeDouble(len);
    if(tkind == Kind.CAT) {
      out.writeTokens(values.toArray());
      out.writeNums(vasize.toArray());
    }
    for(final PathNode c : ch) c.write(out);
  }

  /**
   * Recursively adds the node and its descendants to the specified list.
   * @param nodes node list
   */
  void addDesc(final ObjList<PathNode> nodes) {
    nodes.add(this);
    for(final PathNode n : ch) n.addDesc(nodes);
  }

  /**
   * Recursively adds the node and its descendants to the specified list
   * with the specified name.
   * @param nodes node list
   * @param n name reference
   * @param k node kind
   */
  void addDesc(final ObjList<PathNode> nodes, final int n, final int k) {
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
      case Data.COMM: return COMM;
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
   * Prints a path summary node.
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
      case Data.COMM: tb.add(COMM); break;
      case Data.PI:   tb.add(PI); break;
    }
    tb.add(" " + size + "x; ");
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
      ser.attribute(NAME, data.tagindex.key(name));
    } else if(kind == Data.ATTR) {
      ser.attribute(NAME, Token.concat(ATT, data.atnindex.key(name)));
    }
    ser.text(Token.token(size));
    for(final PathNode p : ch) p.plan(data, ser);
    ser.closeElement();
  }
}
