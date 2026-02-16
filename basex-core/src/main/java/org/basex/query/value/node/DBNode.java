package org.basex.query.value.node;

import static org.basex.query.QueryText.*;

import java.io.*;
import java.util.function.*;

import org.basex.api.dom.*;
import org.basex.build.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.Function;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Database node.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class DBNode extends XNode {
  /** Data reference. */
  private final Data data;
  /** Pre value. */
  private final int pre;
  /** Parent of the database instance (can be {@code null}). */
  private FNode root;

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
   * @param pre PRE value
   */
  public DBNode(final Data data, final int pre) {
    this(data, pre, data.kind(pre));
  }

  /**
   * Constructor, specifying full node information.
   * @param data data reference
   * @param pre PRE value
   * @param kind node kind
   */
  public DBNode(final Data data, final int pre, final int kind) {
    this(data, pre, null, type(kind));
  }

  /**
   * Constructor, specifying full node information.
   * @param node original node
   * @param pre new PRE value
   * @param kind node kind
   */
  private DBNode(final DBNode node, final int pre, final int kind) {
    this(node.data, pre, node.root, type(kind));
  }

  /**
   * Constructor, specifying full node information.
   * @param data data reference (can be {@code null} if invoked by {@link FTNode})
   * @param pre PRE value
   * @param rt root reference (can be {@code null})
   * @param type node type
   */
  DBNode(final Data data, final int pre, final FNode rt, final NodeType type) {
    super(type);
    this.data = data;
    this.pre = pre;
    root = rt;
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

  @Override
  public final Data data() {
    return data;
  }

  /**
   * Returns the PRE value.
   * @return PRE value
   */
  public final int pre() {
    return pre;
  }

  @Override
  public final byte[] string() {
    return data.atom(pre);
  }

  @Override
  public final long itr(final InputInfo ii) throws QueryException {
    // try to directly retrieve inlined numeric value from XML storage
    long l = Long.MIN_VALUE;
    if(type.oneOf(NodeType.TEXT, NodeType.ATTRIBUTE)) {
      l = data.textItr(pre, type == NodeType.TEXT);
    } else if(type == NodeType.ELEMENT) {
      final int as = data.attSize(pre, Data.ELEM);
      if(data.size(pre, Data.ELEM) - as == 1 && data.kind(pre + as) == Data.TEXT) {
        l = data.textItr(pre + as, true);
      }
    }
    return l == Long.MIN_VALUE ? Itr.parse(string(), ii) : l;
  }

  @Override
  public final double dbl(final InputInfo ii) throws QueryException {
    // try to directly retrieve inlined numeric value from XML storage
    double d = Double.NaN;
    if(type.oneOf(NodeType.TEXT, NodeType.ATTRIBUTE)) {
      d = data.textDbl(pre, type == NodeType.TEXT);
    } else if(type == NodeType.ELEMENT) {
      final int as = data.attSize(pre, Data.ELEM);
      if(data.size(pre, Data.ELEM) - as == 1 && data.kind(pre + as) == Data.TEXT) {
        d = data.textDbl(pre + as, true);
      }
    }
    return Double.isNaN(d) ? Dbl.parse(string(), ii) : d;
  }

  @Override
  public final byte[] name() {
    return type.oneOf(NodeType.ELEMENT, NodeType.ATTRIBUTE, NodeType.PROCESSING_INSTRUCTION) ?
      data.name(pre, dbKind((NodeType) type)) : null;
  }

  @Override
  public final QNm qname() {
    if(type.oneOf(NodeType.ELEMENT, NodeType.ATTRIBUTE, NodeType.PROCESSING_INSTRUCTION)) {
      final byte[][] qname = data.qname(pre, dbKind());
      return new QNm(qname[0], qname[1]);
    }
    return null;
  }

  @Override
  public final Atts namespaces() {
    return type == NodeType.ELEMENT ? data.namespaces(pre) : null;
  }

  @Override
  public final byte[] baseURI() {
    if(type == NodeType.DOCUMENT) {
      final String base = Token.string(data.text(pre, true));
      if(data.inMemory()) {
        final String path = data.meta.original;
        return Token.token(path.isEmpty() ? base : IO.get(path).merge(base).url());
      }
      return Token.concat('/', data.meta.name, '/', base);
    }
    final byte[] base = attribute(XML_BASE);
    return base != null ? base : Token.EMPTY;
  }

  @Override
  public final boolean is(final XNode node) {
    return this == node || data == node.data() && pre == ((DBNode) node).pre;
  }

  @Override
  public final int compare(final XNode node) {
    if(this == node) return 0;
    final Data ndata = node.data();
    return ndata != null ?
      // comparison of database nodes: compare PRE values or database IDs
      data == ndata ? Integer.signum(pre - ((DBNode) node).pre) :
        Integer.signum(data.dbid - ndata.dbid) :
      // comparison of database and fragment: find LCA
      compare(this, node);
  }

  @Override
  public final Value copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return new DBNode(data, pre, root, (NodeType) type);
  }

  @Override
  public final DBNode materialize(final Predicate<Data> test, final InputInfo ii,
      final QueryContext qc) throws QueryException {
    return materialized(test, ii) ? this : copy(qc);
  }

  @Override
  public final XNode parent() {
    final int parent = data.parent(pre, dbKind());
    return parent == -1 ? root : new DBNode(this, parent, data.kind(parent));
  }

  @Override
  public final void parent(final FNode par) {
    // supplied parent node will be set as parent of the database instance
    root = par;
  }

  @Override
  public final boolean hasChildren() {
    final int kind = dbKind();
    return data.attSize(pre, kind) != data.size(pre, kind);
  }

  @Override
  public final boolean hasAttributes() {
    return data.attSize(pre, dbKind()) > 1;
  }

  @Override
  public final BasicNodeIter ancestorIter(final boolean self) {
    if(root != null) return super.ancestorIter(self);

    final int first = self ? pre : data.parent(pre, dbKind());
    return first == -1 ? BasicNodeIter.EMPTY : new DBNodeIter(data) {
      int curr = first;

      @Override
      public DBNode next() {
        if(curr == -1) return null;
        final int c = curr, k = data.kind(c);
        curr = data.parent(c, k);
        return new DBNode(DBNode.this, c, k);
      }
    };
  }

  @Override
  public final BasicNodeIter attributeIter() {
    final int kind = dbKind(), first = pre + 1, last = pre + data.attSize(pre, kind);
    return first == last ? BasicNodeIter.EMPTY : new DBNodeIter(data) {
      int curr = first;

      @Override
      public DBNode next() {
        return curr == last ? null : new DBNode(DBNode.this, curr++, Data.ATTR);
      }
      @Override
      public XNode get(final long i) {
        return new DBNode(DBNode.this, pre + 1 + (int) i, Data.ATTR);
      }
      @Override
      public long size() {
        return last - first;
      }
    };
  }

  @Override
  public final BasicNodeIter childIter() {
    final int kind = dbKind();
    final int first = pre + data.attSize(pre, kind), last = pre + data.size(pre, kind);
    return first == last ? BasicNodeIter.EMPTY : new DBNodeIter(data) {
      int curr = first;

      @Override
      public DBNode next() {
        if(curr == last) return null;
        final int c = curr, k = data.kind(c);
        curr = c + data.size(c, k);
        return new DBNode(DBNode.this, c, k);
      }
    };
  }

  @Override
  public final BasicNodeIter descendantIter(final boolean self) {
    final int kind = dbKind();
    final int first = pre + (self ? 0 : data.attSize(pre, kind)), last = pre + data.size(pre, kind);
    return first == last ? BasicNodeIter.EMPTY : new DBNodeIter(data) {
      int curr = first;

      @Override
      public DBNode next() {
        if(curr == last) return null;
        final int c = curr, k = data.kind(c);
        curr = c + data.attSize(c, k);
        return new DBNode(DBNode.this, c, k);
      }
    };
  }

  @Override
  public final BasicNodeIter followingIter(final boolean self) {
    if(root != null) return super.followingIter(self);

    return new DBNodeIter(data) {
      DBNode node = DBNode.this;
      int curr = self ? -2 : -1, size;

      @Override
      public DBNode next() {
        // initialize iterator: find last node that needs to be scanned
        if(curr == -2) {
          ++curr;
        } else {
          if(curr == -1) {
            curr = pre + data.size(pre, dbKind());
            if(data.meta.ndocs > 1) {
              int p = pre;
              for(final XNode nd : ancestorIter(false)) p = ((DBNode) nd).pre;
              size = p + data.size(p, data.kind(p));
            } else {
              size = data.meta.size;
            }
          }
          if(curr == size) return null;

          final int c = curr, k = data.kind(c);
          curr = c + data.attSize(c, k);
          node = new DBNode(DBNode.this, c, k);
        }
        return node;
      }
    };
  }

  @Override
  public final BasicNodeIter followingSiblingIter(final boolean self) {
    final int parent = data.parent(pre, dbKind());
    if(parent == -1) return root != null ? super.followingSiblingIter(self) :
      self ? selfIter() : BasicNodeIter.EMPTY;

    return new DBNodeIter(data) {
      final int last = parent + data.size(parent, data.kind(parent));
      int curr = pre + (self ? 0 : data.size(pre, dbKind()));

      @Override
      public DBNode next() {
        if(curr == last) return null;
        final int c = curr, k = data.kind(c);
        curr = c + data.size(c, k);
        return new DBNode(DBNode.this, c, k);
      }
    };
  }

  @Override
  public final byte[] xdmInfo() {
    final ByteList bl = new ByteList().add(typeId().asByte());
    if(type == NodeType.DOCUMENT) bl.add(baseURI()).add(0);
    else if(type == NodeType.ATTRIBUTE) bl.add(qname().uri()).add(0);
    return bl.finish();
  }

  @Override
  public final BXNode toJava() throws QueryException {
    return BXNode.get(copy(new MainOptions(), null));
  }

  @Override
  public final boolean equals(final Object obj) {
    return obj instanceof final DBNode dbnode && is(dbnode);
  }

  @Override
  public final void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, PRE, pre));
  }

  @Override
  public void toString(final QueryString qs) {
    if(qs.error() || data.inMemory()) {
      switch((NodeType) type) {
        case ATTRIBUTE:
          qs.concat(name(), "=", QueryString.toQuoted(string()));
          break;
        case PROCESSING_INSTRUCTION:
          qs.concat(FPI.OPEN, name(), " ", QueryString.toValue(string()), FPI.CLOSE);
          break;
        case ELEMENT:
          qs.concat("<", name(), hasChildren() || hasAttributes() ? DOTS : "", "/>");
          break;
        case DOCUMENT:
          qs.token(DOCUMENT).brace(QueryString.toQuoted(baseURI()));
          break;
        case COMMENT:
          qs.concat("<!--", QueryString.toValue(string()), "-->");
          break;
        default:
          qs.quoted(string());
          break;
      }
    } else {
      qs.function(Function._DB_GET_PRE, data.meta.name, pre);
    }
  }
}
