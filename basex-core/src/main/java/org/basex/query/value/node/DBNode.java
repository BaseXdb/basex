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
import org.basex.query.expr.path.*;
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
  public final byte[] id() {
    return Token.concat(Token.ID, data().dbid, 'd', pre());
  }

  @Override
  public final byte[] string() {
    return data.atom(pre);
  }

  @Override
  public final long itr(final InputInfo ii) throws QueryException {
    // try to directly retrieve inlined numeric value from XML storage
    long l = Long.MIN_VALUE;
    final Kind kind = kind();
    if(kind.oneOf(Kind.TEXT, Kind.ATTRIBUTE)) {
      l = data.textItr(pre, kind == Kind.TEXT);
    } else if(kind == Kind.ELEMENT) {
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
    final Kind kind = kind();
    if(kind.oneOf(Kind.TEXT, Kind.ATTRIBUTE)) {
      d = data.textDbl(pre, kind == Kind.TEXT);
    } else if(kind == Kind.ELEMENT) {
      final int as = data.attSize(pre, Data.ELEM);
      if(data.size(pre, Data.ELEM) - as == 1 && data.kind(pre + as) == Data.TEXT) {
        d = data.textDbl(pre + as, true);
      }
    }
    return Double.isNaN(d) ? Dbl.parse(string(), ii) : d;
  }

  @Override
  public final byte[] name() {
    final Kind kind = kind();
    return kind.oneOf(Kind.ELEMENT, Kind.ATTRIBUTE, Kind.PROCESSING_INSTRUCTION) ?
      data.name(pre, dbKind(kind)) : null;
  }

  @Override
  public final QNm qname() {
    if(kind().oneOf(Kind.ELEMENT, Kind.ATTRIBUTE, Kind.PROCESSING_INSTRUCTION)) {
      final byte[][] qname = data.qname(pre, dbKind());
      return new QNm(qname[0], qname[1]);
    }
    return null;
  }

  @Override
  public final Atts namespaces() {
    return kind() == Kind.ELEMENT ? data.namespaces(pre) : null;
  }

  @Override
  public final int attrType() {
    return kind() == Kind.ATTRIBUTE ? data.attrType(pre) : 0;
  }

  @Override
  public final byte[] baseURI() {
    if(kind() == Kind.DOCUMENT) {
      final String base = Token.string(data.text(pre, true)), name = data.meta.name;
      if(data.inMemory()) {
        final String path = data.meta.original;
        if(!path.isEmpty()) return Token.token(IO.get(path).merge(base).url());
        // documents without a database (e.g. created via fn:parse-xml) have no base URI
        if(name.isEmpty()) return Token.token(base);
      }
      return Token.concat('/', name, '/', base);
    }
    final byte[] base = attribute(XML_BASE);
    return base != null ? base : Token.EMPTY;
  }

  @Override
  public final byte[] documentURI() {
    // documents without a database (e.g. created via fn:parse-xml) have no document URI
    return kind() != Kind.DOCUMENT || data.meta.name.isEmpty() ? Token.EMPTY : baseURI();
  }

  @Override
  public final boolean is(final GNode node) {
    return this == node || data == node.data() && pre == ((DBNode) node).pre;
  }

  @Override
  public final int compare(final GNode node) {
    if(this == node) return 0;
    if(node instanceof final DBNode dbnode) {
      // comparison of database nodes: compare PRE values or database IDs
      final Data ndata = dbnode.data();
      return data == ndata ? Integer.signum(pre - dbnode.pre) :
        Integer.signum(data.dbid - ndata.dbid);
    }
    // comparison of database and fragment: find LCA
    if(node instanceof final XNode xnode) {
      return compare(this, xnode);
    }
    // comparison with JNode
    return -1;
  }

  @Override
  public final Value copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return new DBNode(data, pre, root, (NodeType) type);
  }

  @Override
  public final DBNode materialize(final Predicate<Data> test, final boolean funcs,
      final InputInfo ii, final QueryContext qc) throws QueryException {
    return materialized(test, funcs, ii) ? this : copy(qc);
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
  public final BasicNodeIter ancestorIter(final boolean self, final Test test) {
    if(root != null) return super.ancestorIter(self, test);

    final int first = self ? pre : data.parent(pre, dbKind());
    if(first == -1) return BasicNodeIter.EMPTY;

    return new AxisIter(test) {
      int curr = first;

      @Override
      public DBNode next() {
        while(curr != -1) {
          final int c = curr, k = data.kind(c);
          curr = data.parent(c, k);
          final DBNode node = node(c, k);
          if(node != null) return node;
        }
        return null;
      }
    };
  }

  @Override
  public final BasicNodeIter attributeIter(final Test test) {
    final int kind = dbKind(), first = pre + 1, last = pre + data.attSize(pre, kind);
    if(first == last) return BasicNodeIter.EMPTY;

    return new AxisIter(test) {
      int curr = first;

      @Override
      public DBNode next() {
        while(curr != last) {
          final DBNode node = node(curr++, Data.ATTR);
          if(node != null) return node;
        }
        return null;
      }
      @Override
      public XNode get(final long i) {
        return new DBNode(DBNode.this, first + (int) i, Data.ATTR);
      }
      @Override
      public long size() {
        return test == null ? last - first : -1;
      }
    };
  }

  @Override
  public final BasicNodeIter childIter(final Test test, final boolean descendant) {
    final int kind = dbKind();
    final int first = pre + data.attSize(pre, kind), last = pre + data.size(pre, kind);
    return siblings(first, last, descendant ? null : test);
  }

  @Override
  public final BasicNodeIter descendantIter(final boolean self, final Test test) {
    final int kind = dbKind();
    final int first = pre + (self ? 0 : data.attSize(pre, kind)), last = pre + data.size(pre, kind);
    if(first == last) return BasicNodeIter.EMPTY;

    return new AxisIter(test) {
      int curr = first;

      @Override
      public DBNode next() {
        while(curr != last) {
          final int c = curr, k = data.kind(c);
          curr = c + data.attSize(c, k);
          final DBNode node = node(c, k);
          if(node != null) return node;
        }
        return null;
      }
    };
  }

  @Override
  public final BasicNodeIter followingIter(final boolean self, final Test test) {
    if(root != null) return super.followingIter(self, test);

    return new AxisIter(test) {
      boolean slf = self;
      int curr = -1, size;

      @Override
      public DBNode next() {
        if(slf) {
          slf = false;
          final DBNode node = node(pre, dbKind());
          if(node != null) return node;
        }
        // initialize iterator: find last node that needs to be scanned
        if(curr == -1) {
          // attribute: continue after the attributes of the parent element
          final int kind = dbKind(), par = data.parent(pre, kind);
          curr = kind == Data.ATTR && par != -1 ? par + data.attSize(par, Data.ELEM) :
            pre + data.size(pre, kind);
          if(data.meta.ndocs > 1) {
            int p = pre, k = kind;
            for(int q; (q = data.parent(p, k)) != -1; p = q) k = data.kind(q);
            size = p + data.size(p, k);
          } else {
            size = data.nodes();
          }
        }
        while(curr != size) {
          final int c = curr, k = data.kind(c);
          curr = c + data.attSize(c, k);
          final DBNode node = node(c, k);
          if(node != null) return node;
        }
        return null;
      }
    };
  }

  @Override
  public final BasicNodeIter followingSiblingIter(final boolean self, final Test test) {
    final int kind = dbKind(), parent = data.parent(pre, kind);
    if(parent == -1 && root != null) return super.followingSiblingIter(self, test);
    if(parent == -1 || kind == Data.ATTR) return self ? selfIter(test) : BasicNodeIter.EMPTY;

    final int first = pre + (self ? 0 : data.size(pre, kind));
    final int last = parent + data.size(parent, data.kind(parent));
    return siblings(first, last, test);
  }

  @Override
  public final BasicNodeIter precedingIter(final boolean self, final Test test) {
    if(root != null) return super.precedingIter(self, test);

    return new AxisIter(test) {
      boolean slf = self;
      int curr = pre - 1, ancestor = data.parent(pre, dbKind());

      @Override
      public DBNode next() {
        if(slf) {
          slf = false;
          final DBNode node = node(pre, dbKind());
          if(node != null) return node;
        }
        // scan backwards, skip ancestors and attributes, stop after the topmost ancestor
        while(ancestor != -1) {
          final int c = curr--, k = data.kind(c);
          if(c == ancestor) {
            ancestor = data.parent(c, k);
          } else if(k != Data.ATTR) {
            final DBNode node = node(c, k);
            if(node != null) return node;
          }
        }
        return null;
      }
    };
  }

  @Override
  public final BasicNodeIter precedingSiblingIter(final boolean self, final Test test) {
    final int kind = dbKind(), parent = data.parent(pre, kind);
    if(parent == -1 && root != null) return super.precedingSiblingIter(self, test);
    if(parent == -1 || kind == Data.ATTR) return self ? selfIter(test) : BasicNodeIter.EMPTY;

    final int first = parent + data.attSize(parent, data.kind(parent));
    return new AxisIter(test) {
      int curr = self ? pre : pre - 1;

      @Override
      public DBNode next() {
        while(curr >= first) {
          // climb from the last descendant of the preceding sibling to the sibling
          int c = curr, k = data.kind(c);
          for(int p; (p = data.parent(c, k)) != parent; c = p) k = data.kind(p);
          curr = c - 1;
          final DBNode node = node(c, k);
          if(node != null) return node;
        }
        return null;
      }
    };
  }

  /**
   * Returns an iterator for the siblings in the specified range that match the specified test.
   * @param first PRE value of the first sibling
   * @param last PRE value after the last sibling
   * @param test node test (can be {@code null})
   * @return iterator
   */
  private BasicNodeIter siblings(final int first, final int last, final Test test) {
    if(first == last) return BasicNodeIter.EMPTY;

    return new AxisIter(test) {
      int curr = first;

      @Override
      public DBNode next() {
        while(curr != last) {
          final int c = curr, k = data.kind(c);
          curr = c + data.size(c, k);
          final DBNode node = node(c, k);
          if(node != null) return node;
        }
        return null;
      }
    };
  }

  /**
   * Axis iterator, returning the nodes that match a node test.
   */
  private abstract class AxisIter extends DBNodeIter {
    /** Filter for database nodes. */
    private final DBFilter filter;
    /** Node test that is not decided by the filter (can be {@code null}). */
    private final Test test;

    /**
     * Constructor.
     * @param test node test (can be {@code null})
     */
    AxisIter(final Test test) {
      super(DBNode.this.data);
      filter = test != null ? test.filter(data) : DBFilter.ALL;
      this.test = filter.exact() ? null : test;
    }

    /**
     * Returns the node with the specified PRE value if it matches the node test.
     * @param p PRE value
     * @param k database kind of the node
     * @return node or {@code null}
     */
    final DBNode node(final int p, final int k) {
      if(!filter.mayMatch(data, p, k)) return null;
      final DBNode node = p == pre ? DBNode.this : new DBNode(DBNode.this, p, k);
      return matches(node, test) ? node : null;
    }
  }

  @Override
  public final byte[] xdmInfo() {
    final ByteList bl = new ByteList().add(typeId().asByte());
    final Kind kind = kind();
    if(kind == Kind.DOCUMENT) bl.add(baseURI()).add(0);
    else if(kind == Kind.ATTRIBUTE) bl.add(qname().uri()).add(0);
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
      switch(kind()) {
        case ATTRIBUTE ->
          qs.token(QueryString.toAttribute(name(), string()));
        case PROCESSING_INSTRUCTION ->
          qs.concat(FPI.OPEN, name(), " ", QueryString.toValue(string()), FPI.CLOSE);
        case ELEMENT ->
          qs.concat("<", name(), hasChildren() || hasAttributes() ? DOTS : "", "/>");
        case DOCUMENT ->
          qs.token(DOCUMENT).brace(QueryString.toQuoted(baseURI()));
        case COMMENT ->
          qs.concat("<!--", QueryString.toValue(string()), "-->");
        default ->
          qs.quoted(string());
      }
    } else {
      qs.function(Function._DB_GET_PRE, data.meta.name, pre);
    }
  }
}
