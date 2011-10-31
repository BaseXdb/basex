package org.basex.query.item;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;

import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.AxisMoreIter;
import org.basex.query.util.NSGlobal;
import org.basex.util.Atts;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.util.ft.Scoring;

/**
 * Disk-based Node item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class DBNode extends ANode {
  /** Data reference. */
  public final Data data;
  /** Pre value. */
  public int pre;

  /** Root node (constructor). */
  private ANode root;
  /** Namespaces. */
  Atts nsp;

  /**
   * Constructor.
   * @param d data reference
   * @param p pre value
   */
  public DBNode(final Data d, final int p) {
    this(d, p, d.kind(p));
  }

  /**
   * Constructor.
   * @param d data reference
   * @param p pre value
   * @param k node kind
   */
  public DBNode(final Data d, final int p, final int k) {
    this(d, p, null, type(k));
  }

  /**
   * Constructor.
   * @param d data reference
   * @param p pre value
   * @param r parent reference
   * @param t node type
   */
  protected DBNode(final Data d, final int p, final ANode r, final NodeType t) {
    super(t);
    data = d;
    pre = p;
    par = r;
  }

  /**
   * Constructor, specifying an XML input reference.
   * @param input input reference
   * @param prop database properties
   * @throws IOException I/O exception
   */
  public DBNode(final IO input, final Prop prop) throws IOException {
    this(Parser.xmlParser(input, prop), prop);
  }

  /**
   * Constructor, specifying a parser reference.
   * @param parser parser
   * @param prop database properties
   * @throws IOException I/O exception
   */
  public DBNode(final Parser parser, final Prop prop) throws IOException {
    this(MemBuilder.build("", parser, prop), 0);
  }

  /**
   * Sets the node type.
   * @param p pre value
   * @param k node kind
   */
  public final void set(final int p, final int k) {
    type = type(k);
    par = null;
    val = null;
    nsp = null;
    pre = p;
  }

  @Override
  public final Data data() {
    return data;
  }

  @Override
  public final byte[] atom() {
    if(val == null) val = data.atom(pre);
    return val;
  }

  @Override
  public final long itr(final InputInfo ii) throws QueryException {
    final boolean txt = type == NodeType.TXT || type == NodeType.COM;
    if(txt || type == NodeType.ATT) {
      final long l = data.textItr(pre, txt);
      if(l != Long.MIN_VALUE) return l;
    }
    return Itr.parse(data.atom(pre), ii);
  }

  @Override
  public final double dbl(final InputInfo ii) throws QueryException {
    final boolean txt = type == NodeType.TXT || type == NodeType.COM;
    if(txt || type == NodeType.ATT) {
      final double d = data.textDbl(pre, txt);
      if(!Double.isNaN(d)) return d;
    }
    return Dbl.parse(data.atom(pre), ii);
  }

  @Override
  public final void serialize(final Serializer ser) throws IOException {
    ser.node(data, pre);
  }

  @Override
  public final byte[] nname() {
    if(!(type instanceof NodeType)) return EMPTY;
    final NodeType t = ndType();
    switch(t) {
      case ELM: case ATT: case PI:
        return data.name(pre, kind(t));
      default:
        return EMPTY;
    }
  }

  @Override
  public final QNm qname() {
    return qname(new QNm());
  }

  @Override
  public final QNm qname(final QNm name) {
    final byte[] nm = nname();
    name.name(nm);
    name.uri(Uri.EMPTY);
    final boolean ns = name.ns();
    if(ns || data.ns.size() != 0) {
      final int n = ns ? data.ns.uri(nm, pre) : data.uri(pre, data.kind(pre));
      final byte[] uri = n > 0 ? data.ns.uri(n) : ns ?
          NSGlobal.uri(pref(nm)) : EMPTY;
      name.uri(uri.length == 0 ? Uri.EMPTY : new Uri(uri));
    }
    return name;
  }

  @Override
  public final Atts ns() {
    if(type == NodeType.ELM && nsp == null) nsp = data.ns(pre);
    return nsp;
  }

  @Override
  public final byte[] base() {
    if(type != NodeType.DOC) return EMPTY;
    final String dir = data.meta.original;
    final String name = string(data.text(pre, true));
    return token(dir.isEmpty() ? name : IO.get(dir).merge(name).url());
  }

  @Override
  public final boolean is(final ANode node) {
    if(node == this) return true;
    if(!(node instanceof DBNode)) return false;
    return data == node.data() && pre == ((DBNode) node).pre;
  }

  @Override
  public final int diff(final ANode node) {
    return !(node instanceof DBNode) || data != node.data() ?
      id - node.id : pre - ((DBNode) node).pre;
  }

  @Override
  public final DBNode copy() {
    final DBNode n = new DBNode(data, pre, par, ndType());
    n.root = root;
    n.score = score;
    return n;
  }

  @Override
  public final DBNode finish() {
    return copy();
  }

  @Override
  public final ANode parent() {
    if(par != null) return par;
    final int p = data.parent(pre, data.kind(pre));
    if(p == -1) return null;

    final DBNode node = copy();
    node.set(p, data.kind(p));
    node.score(Scoring.step(node.score()));
    return node;
  }

  @Override
  public DBNode parent(final ANode p) {
    root = p;
    par = p;
    return this;
  }

  @Override
  public final boolean hasChildren() {
    final int k = data.kind(pre);
    return data.attSize(pre, k) != data.size(pre, k);
  }

  @Override
  public final AxisIter anc() {
    return new AxisIter() {
      private final DBNode node = copy();
      int p = pre;
      int k = data.kind(p);
      final double sc = node.score();

      @Override
      public ANode next() {
        p = data.parent(p, k);
        if(p == -1) return null;
        k = data.kind(p);
        node.set(p, k);
        node.score(Scoring.step(sc));
        return node;
      }
    };
  }

  @Override
  public final AxisIter ancOrSelf() {
    return new AxisIter() {
      private final DBNode node = copy();
      int p = pre;
      int k = data.kind(p);
      final double sc = node.score();

      @Override
      public ANode next() {
        if(p == -1) return null;
        k = data.kind(p);
        node.set(p, k);
        node.score(Scoring.step(sc));
        p = data.parent(p, k);
        return node;
      }
    };
  }

  @Override
  public final AxisIter attributes() {
    return new AxisIter() {
      final DBNode node = copy();
      final int s = pre + data.attSize(pre, data.kind(pre));
      int p = pre + 1;

      @Override
      public ANode next() {
        if(p == s) return null;
        node.set(p++, Data.ATTR);
        return node;
      }
    };
  }

  @Override
  public final AxisMoreIter children() {
    return new AxisMoreIter() {
      int k = data.kind(pre);
      int p = pre + data.attSize(pre, k);
      final int s = pre + data.size(pre, k);
      final DBNode node = copy();
      final double sc = node.score();

      @Override
      public boolean more() {
        return p != s;
      }

      @Override
      public ANode next() {
        if(!more()) return null;
        k = data.kind(p);
        node.set(p, k);
        node.score(Scoring.step(sc));
        p += data.size(p, k);
        return node;
      }
    };
  }

  @Override
  public final AxisIter descendant() {
    return new AxisIter() {
      int k = data.kind(pre);
      int p = pre + data.attSize(pre, k);
      final int s = pre + data.size(pre, k);
      final DBNode node = copy();
      final double sc = node.score();

      @Override
      public DBNode next() {
        if(p == s) return null;
        k = data.kind(p);
        node.set(p, k);
        p += data.attSize(p, k);
        node.score(Scoring.step(sc));
        return node;
      }
    };
  }

  @Override
  public final AxisIter descOrSelf() {
    return new AxisIter() {
      final DBNode node = copy();
      final int s = pre + data.size(pre, data.kind(pre));
      int p = pre;

      @Override
      public ANode next() {
        if(p == s) return null;
        final int k = data.kind(p);
        node.set(p, k);
        p += data.attSize(p, k);
        return node;
      }
    };
  }

  @Override
  public final AxisIter foll() {
    return new AxisIter() {
      private final DBNode node = copy();
      final int s = data.meta.size;
      int k = data.kind(pre);
      int p = pre + data.size(pre, k);

      @Override
      public ANode next() {
        if(p == s) return null;
        k = data.kind(p);
        node.set(p, k);
        p += data.attSize(p, k);
        return node;
      }
    };
  }

  @Override
  public final AxisIter follSibl() {
    return new AxisIter() {
      private final DBNode node = copy();
      int k = data.kind(pre);
      private final int pp = data.parent(pre, k);
      final int s = pp == -1 ? 0 : pp + data.size(pp, data.kind(pp));
      int p = pp == -1 ? 0 : pre + data.size(pre, k);

      @Override
      public ANode next() {
        if(p == s) return null;
        k = data.kind(p);
        node.set(p, k);
        p += data.size(p, k);
        return node;
      }
    };
  }

  @Override
  public final AxisIter par() {
    return new AxisIter() {
      /** First call. */
      private boolean more;

      @Override
      public ANode next() {
        if(more) return null;
        more = true;
        return parent();
      }
    };
  }

  @Override
  public final boolean sameAs(final Expr cmp) {
    return cmp instanceof DBNode && data == ((DBNode) cmp).data &&
      pre == ((DBNode) cmp).pre;
  }

  @Override
  public final void plan(final Serializer ser) throws IOException {
    ser.openElement(token(Util.name(this)), NAM, token(data.meta.name));
    if(pre != 0) ser.attribute(PRE, token(pre));
    ser.closeElement();
  }

  @Override
  public String toString() {
    final NodeType nt = (NodeType) type;
    final TokenBuilder tb = new TokenBuilder(nt.nam()).add(' ');
    switch(nt) {
      case ATT:
      case PI:
        tb.add(nname()).add(" { \"").add(chop(atom(), 64)).add("\" }");
        break;
      case ELM:
        tb.add(nname()).add(" { ... }");
        break;
      case DOC:
        tb.add("{ \"").add(data.text(pre, true)).add("\" }");
        break;
      default:
        tb.add("{ \"").add(chop(atom(), 64)).add("\" }");
        break;
    }
    return tb.toString();
  }
}
