package org.basex.query.value.node;

import static org.basex.query.QueryText.*;

import java.io.*;

import org.basex.api.dom.*;
import org.basex.build.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.Type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Database node.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public class DBNode extends ANode {
  /** Data reference. */
  private final Data data;
  /** Pre value. */
  private int pre;

  /**
   * Constructor, creating a document node from the specified data reference.
   * @param data data reference
   */
  public DBNode(final Data data) {
    this(data, 0);
  }

  /**
   * Constructor, creating a node from the specified data reference.
   * @param data data reference
   * @param pre pre value
   */
  public DBNode(final Data data, final int pre) {
    this(data, pre, data.kind(pre));
  }

  /**
   * Constructor, specifying full node information.
   * @param data data reference
   * @param pre pre value
   * @param kind node kind
   */
  public DBNode(final Data data, final int pre, final int kind) {
    this(data, pre, null, type(kind));
  }

  /**
   * Constructor, specifying full node information.
   * @param data data reference
   * @param pre pre value
   * @param par parent reference
   * @param type node type
   */
  DBNode(final Data data, final int pre, final ANode par, final NodeType type) {
    super(type);
    this.data = data;
    this.pre = pre;
    parent = par;
  }

  /**
   * Constructor, specifying an XML input reference.
   * @param input input reference
   * @throws IOException I/O exception
   */
  public DBNode(final IO input) throws IOException {
    this(MemBuilder.build(input));
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
  private void set(final int p, final int k) {
    type = type(k);
    parent = null;
    value = null;
    pre = p;
  }

  @Override
  public final Data data() {
    return data;
  }

  /**
   * Assigns a pre value.
   * @param p pre value
   */
  public final void pre(final int p) {
    pre = p;
  }

  /**
   * Returns the pre value.
   * @return pre value
   */
  public final int pre() {
    return pre;
  }

  @Override
  public final byte[] string() {
    if(value == null) value = data.atom(pre);
    return value;
  }

  @Override
  public final long itr(final InputInfo ii) throws QueryException {
    if(type == NodeType.ELM) {
      final int as = data.attSize(pre, Data.ELEM);
      if(data.size(pre, Data.ELEM) - as == 1 && data.kind(pre + as) == Data.TEXT) {
        final long l = data.textItr(pre + as, true);
        if(l != Long.MIN_VALUE) return l;
      }
    } else if(type == NodeType.TXT || type == NodeType.ATT) {
      final long l = data.textItr(pre, type == NodeType.TXT);
      if(l != Long.MIN_VALUE) return l;
    }
    return Int.parse(this, ii);
  }

  @Override
  public final double dbl(final InputInfo ii) throws QueryException {
    // try to directly retrieve inlined numeric value from XML storage
    double d = Double.NaN;
    if(type == NodeType.ELM) {
      final int as = data.attSize(pre, Data.ELEM);
      if(data.size(pre, Data.ELEM) - as == 1 && data.kind(pre + as) == Data.TEXT) {
        d = data.textDbl(pre + as, true);
      }
    } else if(type == NodeType.TXT || type == NodeType.ATT) {
      d = data.textDbl(pre, type == NodeType.TXT);
    }
    // GH-1206: parse invalid values again
    return Double.isNaN(d) ? Dbl.parse(string(), ii) : d;
  }

  @Override
  public final byte[] name() {
    return type == NodeType.ELM || type == NodeType.ATT || type == NodeType.PI ?
      data.name(pre, kind(nodeType())) : null;
  }

  @Override
  public final QNm qname() {
    if(type == NodeType.ELM || type == NodeType.ATT || type == NodeType.PI) {
      final byte[][] qname = data.qname(pre, kind());
      return new QNm(qname[0], qname[1]);
    }
    return null;
  }

  @Override
  public final Atts namespaces() {
    return data.namespaces(pre);
  }

  @Override
  public final byte[] baseURI() {
    if(type == NodeType.DOC) {
      final String base = Token.string(data.text(pre, true));
      if(data.inMemory()) {
        final String path = data.meta.original;
        return Token.token(path.isEmpty() ? base : IO.get(path).merge(base).url());
      }
      return Token.concat('/', data.meta.name, '/', base);
    }
    final byte[] base = attribute(QNm.XML_BASE);
    return base != null ? base : Token.EMPTY;
  }

  @Override
  public final boolean is(final ANode node) {
    return this == node || data == node.data() && pre == ((DBNode) node).pre;
  }

  @Override
  public final int diff(final ANode node) {
    if(this == node) return 0;
    final Data ndata = node.data();
    return ndata != null ?
      // comparison of two databases: compare pre values or database ids
      data == ndata ? pre - ((DBNode) node).pre : data.dbid - ndata.dbid :
      // comparison of database and fragment: find LCA
      diff(this, node);
  }

  @Override
  public final Value copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return finish();
  }

  @Override
  public final DBNode materialize(final QueryContext qc, final boolean copy) {
    return copy ? copy(qc) : this;
  }

  @Override
  public final DBNode finish() {
    final DBNode node = new DBNode(data, pre, parent, nodeType());
    node.score = score;
    return node;
  }

  @Override
  public final ANode parent() {
    if(parent != null) return parent;
    final int p = data.parent(pre, data.kind(pre));
    if(p == -1) return null;

    final DBNode node = finish();
    node.set(p, data.kind(p));
    return node;
  }

  @Override
  public final boolean hasChildren() {
    final int kind = data.kind(pre);
    return data.attSize(pre, kind) != data.size(pre, kind);
  }

  @Override
  public final DBNodeIter ancestorIter() {
    return new DBNodeIter(data) {
      private final DBNode node = finish();
      int curr = pre, kind = data.kind(curr);

      @Override
      public DBNode next() {
        curr = data.parent(curr, kind);
        if(curr == -1) return null;
        kind = data.kind(curr);
        node.set(curr, kind);
        return node;
      }
    };
  }

  @Override
  public final DBNodeIter ancestorOrSelfIter() {
    return new DBNodeIter(data) {
      private final DBNode node = finish();
      int curr = pre, kind = data.kind(curr);

      @Override
      public DBNode next() {
        if(curr == -1) return null;
        kind = data.kind(curr);
        node.set(curr, kind);
        curr = data.parent(curr, kind);
        return node;
      }
    };
  }

  @Override
  public final DBNodeIter attributeIter() {
    return new DBNodeIter(data) {
      final DBNode node = finish();
      final int size = data.attSize(pre, data.kind(pre));
      int curr = pre + 1;

      @Override
      public DBNode next() {
        if(curr == pre + size) return null;
        final DBNode n = node;
        n.set(curr++, Data.ATTR);
        return n;
      }
      @Override
      public ANode get(final long i) {
        final DBNode n = node;
        n.set(pre + 1 + (int) i, Data.ATTR);
        return n;
      }
      @Override
      public long size() { return size - 1; }
    };
  }

  @Override
  public final DBNodeIter childIter() {
    return new DBNodeIter(data) {
      int kind = data.kind(pre), curr = pre + data.attSize(pre, kind);
      final int last = pre + data.size(pre, kind);
      final DBNode node = finish();

      @Override
      public DBNode next() {
        if(curr == last) return null;
        final Data d = data;
        kind = d.kind(curr);
        node.set(curr, kind);
        curr += d.size(curr, kind);
        return node;
      }
    };
  }

  @Override
  public final DBNodeIter descendantIter() {
    return new DBNodeIter(data) {
      int kind = data.kind(pre), curr = pre + data.attSize(pre, kind);
      final int last = pre + data.size(pre, kind);
      final DBNode node = finish();

      @Override
      public DBNode next() {
        if(curr == last) return null;
        kind = data.kind(curr);
        node.set(curr, kind);
        curr += data.attSize(curr, kind);
        return node;
      }
    };
  }

  @Override
  public final DBNodeIter descendantOrSelfIter() {
    return new DBNodeIter(data) {
      final DBNode node = finish();
      final int last = pre + data.size(pre, data.kind(pre));
      int curr = pre;

      @Override
      public DBNode next() {
        if(curr == last) return null;
        final int k = data.kind(curr);
        node.set(curr, k);
        curr += data.attSize(curr, k);
        return node;
      }
    };
  }

  @Override
  public final DBNodeIter followingIter() {
    return new DBNodeIter(data) {
      private final DBNode node = finish();
      int kind = data.kind(pre), curr = pre + data.size(pre, kind), size = -1;

      @Override
      public DBNode next() {
        // initialize iterator: find last node that needs to be scanned
        if(size == -1) {
          if(data.meta.ndocs > 1) {
            int p = pre;
            for(final ANode n : ancestorIter()) p = ((DBNode) n).pre;
            size = p + data.size(p, data.kind(p));
          } else {
            size = data.meta.size;
          }
        }

        if(curr == size) return null;
        kind = data.kind(curr);
        node.set(curr, kind);
        curr += data.attSize(curr, kind);
        return node;
      }
    };
  }

  @Override
  public final DBNodeIter followingSiblingIter() {
    return new DBNodeIter(data) {
      private final DBNode node = finish();
      int kind = data.kind(pre);
      private final int pp = data.parent(pre, kind);
      final int sz = pp == -1 ? 0 : pp + data.size(pp, data.kind(pp));
      int curr = pp == -1 ? 0 : pre + data.size(pre, kind);

      @Override
      public DBNode next() {
        if(curr == sz) return null;
        kind = data.kind(curr);
        node.set(curr, kind);
        curr += data.size(curr, kind);
        return node;
      }
    };
  }

  @Override
  public final byte[] xdmInfo() {
    final ByteList bl = new ByteList().add(typeId().asByte());
    if(type == NodeType.DOC) bl.add(baseURI()).add(0);
    else if(type == NodeType.ATT) bl.add(qname().uri()).add(0);
    return bl.finish();
  }

  @Override
  public final ID typeId() {
    // check if a document has a single element as child
    ID i = type.id();
    if(type == NodeType.DOC) {
      final DBNodeIter iter = childIter();
      final ANode n = iter.next();
      if(n != null && n.type == NodeType.ELM && iter.next() == null) i = NodeType.DEL.id();
    }
    return i;
  }

  @Override
  public final BXNode toJava() {
    return BXNode.get(copy(new MainOptions(), null));
  }

  @Override
  public final boolean equals(final Object obj) {
    return obj instanceof DBNode && is((DBNode) obj);
  }

  @Override
  public final void plan(final QueryPlan plan) {
    plan.add(plan.create(this, PRE, pre));
  }

  @Override
  public String toErrorString() {
    return toString(false);
  }

  @Override
  public String toString() {
    return toString(!data.inMemory());
  }

  /**
   * Returns a string representation of the sequence.
   * @param func display function representation
   * @return string
   */
  private String toString(final boolean func) {
    if(func) return Function._DB_OPEN_PRE.args(data.meta.name, pre).substring(1);

    final TokenBuilder tb = new TokenBuilder().add(type.string()).add(' ');
    switch((NodeType) type) {
      case ATT:
      case PI:
        tb.add(name()).add(" {").add(toQuotedToken(string())).add('}');
        break;
      case ELM:
        tb.add(name()).add(" {");
        if(hasChildren() || attributeIter().size() != 0) tb.add(Text.DOTS);
        tb.add('}');
        break;
      case DOC:
        tb.add('{').add(toQuotedToken(data.text(pre, true))).add('}');
        break;
      default:
        tb.add('{').add(toQuotedToken(string())).add('}');
        break;
    }
    return tb.toString();
  }
}
