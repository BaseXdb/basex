package org.basex.query.value.node;

import static org.basex.query.QueryText.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.list.*;

/**
 * Database nodes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class DBNode extends ANode {
  /** Data reference. */
  public final Data data;
  /** Pre value. */
  public int pre;
  /** Namespaces. */
  private Atts nsp;

  /**
   * Constructor, creating a document node from the specified data reference.
   * @param d data reference
   */
  public DBNode(final Data d) {
    this(d, 0);
  }

  /**
   * Constructor, creating a node from the specified data reference.
   * @param d data reference
   * @param p pre value
   */
  public DBNode(final Data d, final int p) {
    this(d, p, d.kind(p));
  }

  /**
   * Constructor, specifying full node information.
   * @param d data reference
   * @param p pre value
   * @param k node kind
   */
  public DBNode(final Data d, final int p, final int k) {
    this(d, p, null, type(k));
  }

  /**
   * Constructor, specifying full node information.
   * @param d data reference
   * @param p pre value
   * @param r parent reference
   * @param t node type
   */
  DBNode(final Data d, final int p, final ANode r, final NodeType t) {
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
    this(Parser.xmlParser(input, prop));
  }

  /**
   * Constructor, specifying a parser reference.
   * @param parser parser
   * @throws IOException I/O exception
   */
  public DBNode(final Parser parser) throws IOException {
    this(MemBuilder.build("", parser));
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
  public final byte[] string() {
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
    return Int.parse(data.atom(pre), ii);
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
  public final byte[] name() {
    final NodeType t = nodeType();
    switch(t) {
      case ELM: case ATT: case PI:
        return data.name(pre, kind(t));
      default:
        return Token.EMPTY;
    }
  }

  @Override
  public final QNm qname() {
    return qname(new QNm());
  }

  @Override
  public final QNm qname(final QNm name) {
    // update the name and uri strings in the specified QName
    final byte[] nm = name();
    byte[] uri = Token.EMPTY;
    final boolean pref = Token.indexOf(nm, ':') != -1;
    if(pref || data.nspaces.size() != 0) {
      final int n = pref ? data.nspaces.uri(nm, pre) : data.uri(pre, data.kind(pre));
      final byte[] u = n > 0 ? data.nspaces.uri(n) : pref ?
          NSGlobal.uri(Token.prefix(nm)) : null;
      if(u != null) uri = u;
    }
    name.update(nm, uri);
    return name;
  }

  @Override
  public final Atts namespaces() {
    if(type == NodeType.ELM && nsp == null) nsp = data.ns(pre);
    return nsp;
  }

  @Override
  public final byte[] baseURI() {
    if(type == NodeType.DOC) {
      final byte[] base = data.text(pre, true);
      if(data.inMemory()) {
        final String dir = data.meta.original;
        final String bs = Token.string(base);
        return Token.token(dir.isEmpty() ? bs : IO.get(dir).merge(bs).url());
      }
      return new TokenBuilder(data.meta.name).add('/').add(base).finish();
    }
    final byte[] b = attribute(new QNm(BASE, XMLURI));
    return b != null ? b : Token.EMPTY;
  }

  @Override
  public final boolean is(final ANode node) {
    return node == this || node instanceof DBNode &&
        data == node.data() && pre == ((DBNode) node).pre;
  }

  @Override
  public final int diff(final ANode node) {
    return !(node instanceof DBNode) || data != node.data() ?
        id - node.id : pre - ((DBNode) node).pre;
  }

  @Override
  public final DBNode copy() {
    final DBNode n = new DBNode(data, pre, par, nodeType());
    n.score = score;
    return n;
  }

  @Override
  public final ANode deepCopy() {
    // adopt meta data and index structures if database is a main-memory instance
    final MemData md = data.inMemory() ? new MemData(data) : new MemData(data.meta.prop);
    new DataBuilder(md).build(this);
    return new DBNode(md);

    /*if(hasChildren()) {
      // adopt index structures if database is a main-memory instance
      final MemData md = data.inMemory() ?
          new MemData(data) : new MemData(data.meta.prop);
      new DataBuilder(md).build(this);
      return new DBNode(md, 0);
    }
    // create object-based fragments for nodes without children (faster)
    switch((NodeType) type) {
      case ATT:
        return new FAttr(qname(), string());
      case PI:
        return new FPI(qname(), string());
      case TXT:
        return new FTxt(string());
      case COM:
        return new FComm(string());
      case DOC:
        return new FDoc(new NodeCache(), baseURI());
      default:
        final NodeCache atts = new NodeCache();
        final AxisIter ai = attributes();
        for(ANode it; (it = ai.next()) != null;) atts.add(it.finish());
        return new FElem(qname(), null, atts, namespaces());
    }*/
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
    par = p;
    return this;
  }

  @Override
  public final boolean hasChildren() {
    final int k = data.kind(pre);
    return data.attSize(pre, k) != data.size(pre, k);
  }

  @Override
  public final AxisIter ancestor() {
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
  public final AxisIter ancestorOrSelf() {
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
  public final AxisMoreIter attributes() {
    return new AxisMoreIter() {
      final DBNode node = copy();
      final int s = pre + data.attSize(pre, data.kind(pre));
      int p = pre + 1;

      @Override
      public boolean more() {
        return p != s;
      }

      @Override
      public DBNode next() {
        if(!more()) return null;
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
  public final AxisIter descendantOrSelf() {
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
  public final AxisIter following() {
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
  public final AxisIter followingSibling() {
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
  public final AxisIter parentIter() {
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
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, data.meta.name, PRE, pre));
  }

  @Override
  public byte[] xdmInfo() {
    final ByteList bl = new ByteList().add(typeId());
    if(type == NodeType.DOC) bl.add(baseURI()).add(0);
    else if(type == NodeType.ATT) bl.add(qname().uri()).add(0);
    return bl.toArray();
  }

  @Override
  public int typeId() {
    // check if a document has a single element as child
    int t = type.id();
    if(type == NodeType.DOC) {
      final AxisMoreIter ai = children();
      if(ai.more() && ai.next().type == NodeType.ELM && !ai.more()) t = NodeType.DEL.id();
    }
    return t;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(type.string()).add(' ');
    switch((NodeType) type) {
      case ATT:
      case PI:
        tb.add(name()).add(" { \"").add(Token.chop(string(), 64)).add("\" }");
        break;
      case ELM:
        tb.add(name()).add(" { ... }");
        break;
      case DOC:
        tb.add("{ \"").add(data.text(pre, true)).add("\" }");
        break;
      default:
        tb.add("{ \"").add(Token.chop(string(), 64)).add("\" }");
        break;
    }
    return tb.toString();
  }
}
