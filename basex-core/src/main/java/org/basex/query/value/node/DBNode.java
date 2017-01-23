package org.basex.query.value.node;

import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import java.io.*;

import org.basex.api.dom.*;
import org.basex.build.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.Type.ID;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Database node.
 *
 * @author BaseX Team 2005-17, BSD License
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
    return Int.parse(data.atom(pre), ii);
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
    return Double.isNaN(d) ? Dbl.parse(data.atom(pre), ii) : d;
  }

  @Override
  public final byte[] name() {
    return type == NodeType.ELM || type == NodeType.ATT || type == NodeType.PI ?
      data.name(pre, kind(nodeType())) : Token.EMPTY;
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
      return new TokenBuilder().add('/').add(data.meta.name).add('/').add(base).finish();
    }
    final byte[] base = attribute(QNm.XML_BASE);
    return base != null ? base : Token.EMPTY;
  }

  @Override
  public final boolean is(final ANode node) {
    return node == this || node instanceof DBNode &&
        data == node.data() && pre == ((DBNode) node).pre;
  }

  @Override
  public final int diff(final ANode node) {
    // compare fragment with database node; specify fragment first to save time
    if(node instanceof FNode) return -diff(node, this);
    // same database instance: compare pre values
    if(data == node.data()) {
      final int p = ((DBNode) node).pre;
      return pre > p ? 1 : pre < p ? -1 : 0;
    }
    // check order via lowest common ancestor
    return diff(this, node);
  }

  @Override
  public final Value copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return finish();
  }

  @Override
  public final DBNode dbNodeCopy(final MainOptions opts) {
    final MemData md = new MemData(opts);
    new DataBuilder(md).build(this);
    return new DBNode(md).parent(parent);
  }

  @Override
  public final DBNode deepCopy(final MainOptions options) {
    return dbNodeCopy(options);
  }

  @Override
  public final DBNode finish() {
    final DBNode n = new DBNode(data, pre, parent, nodeType());
    n.score = score;
    return n;
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
  protected final DBNode parent(final ANode par) {
    parent = par;
    return this;
  }

  @Override
  public final boolean hasChildren() {
    final int kind = data.kind(pre);
    return data.attSize(pre, kind) != data.size(pre, kind);
  }

  @Override
  public final DBNodeIter ancestor() {
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
  public final DBNodeIter ancestorOrSelf() {
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
  public final DBNodeIter attributes() {
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
  public final DBNodeIter children() {
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
  public final DBNodeIter descendant() {
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
  public final DBNodeIter descendantOrSelf() {
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
  public final DBNodeIter following() {
    return new DBNodeIter(data) {
      private final DBNode node = finish();
      int kind = data.kind(pre), curr = pre + data.size(pre, kind), size = -1;

      @Override
      public DBNode next() {
        // initialize iterator: find last node that needs to be scanned
        if(size == -1) {
          if(data.meta.ndocs > 1) {
            for(final ANode anc : ancestor()) size = ((DBNode) anc).pre;
          }
          size = size == -1 ? data.meta.size : data.size(size, data.kind(size));
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
  public final DBNodeIter followingSibling() {
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
  public final boolean sameAs(final Expr cmp) {
    return cmp instanceof DBNode && data == ((DBNode) cmp).data && pre == ((DBNode) cmp).pre;
  }

  @Override
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, data.meta.name, PRE, pre));
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
    ID t = type.id();
    if(type == NodeType.DOC) {
      final DBNodeIter ir = children();
      final ANode n = ir.next();
      if(n != null && n.type == NodeType.ELM && ir.next() == null) t = NodeType.DEL.id();
    }
    return t;
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
    if(func) return _DB_OPEN_PRE.args(data.meta.name, pre);

    final TokenBuilder tb = new TokenBuilder(type.string()).add(' ');
    switch((NodeType) type) {
      case ATT:
      case PI:
        tb.add(name()).add(" {\"").add(Token.chop(string(), 32)).add("\"}");
        break;
      case ELM:
        tb.add(name()).add(" {");
        if(hasChildren() || attributes().size() != 0) tb.add("...");
        tb.add('}');
        break;
      case DOC:
        tb.add("{\"").add(data.text(pre, true)).add("\"}");
        break;
      default:
        tb.add("{\"").add(Token.chop(string(), 32)).add("\"}");
        break;
    }
    return tb.toString();
  }

  @Override
  public final BXNode toJava() {
    return BXNode.get(deepCopy(new MainOptions()));
  }
}
