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
import org.basex.query.value.type.Type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Database node.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class DBNode extends ANode {
  /** Data reference. */
  private final Data data;
  /** Parent of the database instance  (can be {@code null}). */
  private FNode root;
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
   * @param data data reference (can be {@code null} if invoked by {@link FTNode})
   * @param pre pre value
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

  /**
   * Sets the node type.
   * @param p pre value
   * @param k node kind
   * @return self reference
   */
  private DBNode set(final int p, final int k) {
    type = type(k);
    pre = p;
    return this;
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
    return data.atom(pre);
  }

  @Override
  public final long itr(final InputInfo ii) throws QueryException {
    // try to directly retrieve inlined numeric value from XML storage
    long l = Long.MIN_VALUE;
    final boolean text = type == NodeType.TEXT;
    if(text || type == NodeType.ATTRIBUTE) {
      l = data.textItr(pre, text);
    } else if(type == NodeType.ELEMENT) {
      final int as = data.attSize(pre, Data.ELEM);
      if(data.size(pre, Data.ELEM) - as == 1 && data.kind(pre + as) == Data.TEXT) {
        l = data.textItr(pre + as, true);
      }
    }
    return l == Long.MIN_VALUE ? Int.parse(string(), ii) : l;
  }

  @Override
  public final double dbl(final InputInfo ii) throws QueryException {
    // try to directly retrieve inlined numeric value from XML storage
    double d = Double.NaN;
    final boolean text = type == NodeType.TEXT;
    if(text || type == NodeType.ATTRIBUTE) {
      d = data.textDbl(pre, text);
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
      data.name(pre, kind((NodeType) type)) : null;
  }

  @Override
  public final QNm qname() {
    if(type.oneOf(NodeType.ELEMENT, NodeType.ATTRIBUTE, NodeType.PROCESSING_INSTRUCTION)) {
      final byte[][] qname = data.qname(pre, kind());
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
    if(type == NodeType.DOCUMENT_NODE) {
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
  public final boolean is(final ANode node) {
    return this == node || data == node.data() && pre == ((DBNode) node).pre;
  }

  @Override
  public final int compare(final ANode node) {
    if(this == node) return 0;
    final Data ndata = node.data();
    return ndata != null ?
      // comparison of database nodes: compare pre values or database ids
      data == ndata ? Integer.signum(pre - ((DBNode) node).pre) :
        Integer.signum(data.dbid - ndata.dbid) :
      // comparison of database and fragment: find LCA
      compare(this, node);
  }

  @Override
  public final Value copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return finish();
  }

  @Override
  public final DBNode materialize(final Predicate<Data> test, final InputInfo ii,
      final QueryContext qc) throws QueryException {
    return materialized(test, ii) ? this : copy(qc);
  }

  @Override
  public final DBNode finish() {
    return new DBNode(data, pre, root, (NodeType) type);
  }

  @Override
  public final ANode parent() {
    final int par = data.parent(pre, kind());
    return par == -1 ? root : finish().set(par, data.kind(par));
  }

  @Override
  public final void parent(final FNode par) {
    // supplied parent node will be set as parent of the database instance
    root = par;
  }

  @Override
  public final boolean hasChildren() {
    final int kind = kind();
    return data.attSize(pre, kind) != data.size(pre, kind);
  }

  @Override
  public final boolean hasAttributes() {
    return data.attSize(pre, kind()) > 0;
  }

  @Override
  public final BasicNodeIter ancestorIter() {
    return root != null ? super.ancestorIter() : ancestorIter(data.parent(pre, kind()));
  }

  @Override
  public final BasicNodeIter ancestorOrSelfIter() {
    return root != null ? super.ancestorOrSelfIter() : ancestorIter(pre);
  }

  @Override
  public final BasicNodeIter attributeIter() {
    final int k = kind(), first = pre + 1, last = pre + data.attSize(pre, k);
    return first == last ? BasicNodeIter.EMPTY : new DBNodeIter(data) {
      final DBNode node = finish();
      int curr = first;

      @Override
      public DBNode next() {
        return curr == last ? null : node.set(curr++, Data.ATTR);
      }
      @Override
      public ANode get(final long i) {
        return node.set(pre + 1 + (int) i, Data.ATTR);
      }
      @Override
      public long size() {
        return last - first;
      }
    };
  }

  @Override
  public final BasicNodeIter childIter() {
    final int k = kind(), first = pre + data.attSize(pre, k), last = pre + data.size(pre, k);
    return first == last ? BasicNodeIter.EMPTY : new DBNodeIter(data) {
      final DBNode node = finish();
      int curr = first;

      @Override
      public DBNode next() {
        if(curr == last) return null;
        final int kind = data.kind(curr);
        node.set(curr, kind);
        curr += data.size(curr, kind);
        return node;
      }
    };
  }

  @Override
  public final BasicNodeIter descendantIter() {
    final int k = kind(), first = pre + data.attSize(pre, k), last = pre + data.size(pre, k);
    return descendantIter(first, last);
  }

  @Override
  public final BasicNodeIter descendantOrSelfIter() {
    final int k = kind(), first = pre, last = pre + data.size(pre, k);
    return descendantIter(first, last);
  }

  @Override
  public final BasicNodeIter followingIter() {
    if(root != null) return super.followingIter();

    return new DBNodeIter(data) {
      private final DBNode node = finish();
      int kind = kind(), curr = pre + data.size(pre, kind), size = -1;

      @Override
      public DBNode next() {
        // initialize iterator: find last node that needs to be scanned
        if(size == -1) {
          if(data.meta.ndocs > 1) {
            int p = pre;
            for(final ANode nd : ancestorIter()) p = ((DBNode) nd).pre;
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
  public final BasicNodeIter followingSiblingIter() {
    final int k = kind(), parent = data.parent(pre, k);
    if(parent == -1) return root != null ? super.followingSiblingIter() : BasicNodeIter.EMPTY;

    return new DBNodeIter(data) {
      final DBNode node = finish();
      final int last = parent + data.size(parent, data.kind(parent));
      int curr = pre + data.size(pre, k);

      @Override
      public DBNode next() {
        if(curr == last) return null;
        final int kind = data.kind(curr);
        node.set(curr, kind);
        curr += data.size(curr, kind);
        return node;
      }
    };
  }

  /**
   * Returns an iterator for the ancestor axes.
   * @param first pre value to start from
   * @return iterator
   */
  private BasicNodeIter ancestorIter(final int first) {
    return first == -1 ? BasicNodeIter.EMPTY : new DBNodeIter(data) {
      final DBNode node = finish();
      int curr = first;

      @Override
      public DBNode next() {
        if(curr == -1) return null;
        final int kind = data.kind(curr);
        node.set(curr, kind);
        curr = data.parent(curr, kind);
        return node;
      }
    };
  }

  /**
   * Returns an iterator for the ancestor axes.
   * @param first pre value to start from
   * @param last last pre value
   * @return iterator
   */
  private BasicNodeIter descendantIter(final int first, final int last) {
    return first == last ? BasicNodeIter.EMPTY : new DBNodeIter(data) {
      final DBNode node = finish();
      int curr = first;

      @Override
      public DBNode next() {
        if(curr == last) return null;
        final int kind = data.kind(curr);
        node.set(curr, kind);
        curr += data.attSize(curr, kind);
        return node;
      }
    };
  }

  @Override
  public final byte[] xdmInfo() {
    final ByteList bl = new ByteList().add(typeId().asByte());
    if(type == NodeType.DOCUMENT_NODE) bl.add(baseURI()).add(0);
    else if(type == NodeType.ATTRIBUTE) bl.add(qname().uri()).add(0);
    return bl.finish();
  }

  @Override
  public final ID typeId() {
    // check if a document has a single element as child
    ID i = type.id();
    if(type == NodeType.DOCUMENT_NODE) {
      final BasicNodeIter iter = childIter();
      final ANode n = iter.next();
      if(n != null && n.type == NodeType.ELEMENT && iter.next() == null) {
        i = NodeType.DOCUMENT_NODE_ELEMENT.id();
      }
    }
    return i;
  }

  @Override
  public final BXNode toJava() throws QueryException {
    return BXNode.get(copy(new MainOptions(), null));
  }

  @Override
  public final boolean equals(final Object obj) {
    return obj instanceof DBNode && is((DBNode) obj);
  }

  @Override
  public final void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, PRE, pre));
  }

  @Override
  public String toErrorString() {
    final QueryString qs = new QueryString();
    toString(qs, true);
    return qs.toString();
  }

  @Override
  public void toString(final QueryString qs) {
    toString(qs, false);
  }

  /**
   * Returns a string representation of the sequence.
   * @param qs query string builder
   * @param error error representation
   */
  private void toString(final QueryString qs, final boolean error) {
    if(error || data.inMemory()) {
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
        case DOCUMENT_NODE:
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
