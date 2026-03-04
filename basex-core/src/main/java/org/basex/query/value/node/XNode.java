package org.basex.query.value.node;

import static org.basex.query.QueryError.*;
import static org.basex.query.util.DeepEqualOptions.*;
import static org.basex.query.value.type.Kind.*;

import java.io.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.basex.api.dom.*;
import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.io.out.DataOutput;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * XML node.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class XNode extends GNode {
  /** QName: xml:base. */
  static final QNm XML_BASE = new QNm(QueryText.BASE, QueryText.XML_URI);
  /** Node Types. */
  private static final NodeType[] TYPES = {
    NodeType.DOCUMENT, NodeType.ELEMENT, NodeType.TEXT, NodeType.ATTRIBUTE,
    NodeType.COMMENT, NodeType.PROCESSING_INSTRUCTION
  };
  /** Static node counter. */
  private static final AtomicInteger ID = new AtomicInteger();
  /** Unique node ID. ID can get negative, as subtraction of IDs is used for all comparisons. */
  public final int id = ID.incrementAndGet();

  /**
   * Constructor.
   * @param type item type
   */
  XNode(final NodeType type) {
    super(type);
  }

  @Override
  public final void write(final DataOutput out) throws IOException {
    final ArrayOutput ao = new ArrayOutput();
    Serializer.get(ao).serialize(this);
    out.writeToken(ao.finish());
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos) {
    return true;
  }

  @Override
  public final boolean bool(final InputInfo ii) {
    return true;
  }

  @Override
  public final byte[] string(final InputInfo ii) {
    return string();
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {
    Expr expr = this;
    if(mode == Simplify.STRING) {
      // boolean(<a>A</a>) → boolean('A')
      expr = Str.get(string());
    } else if(mode.oneOf(Simplify.DATA, Simplify.NUMBER)) {
      // data(<a>A</a>) → data(xs:untypedAtomic('A'))
      expr = atomItem(cc.qc, null);
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public final boolean comparable(final Item item) {
    return item.type.isStringOrUntyped();
  }

  @Override
  public final int compare(final Item item, final Collation coll, final boolean transitive,
      final InputInfo ii) throws QueryException {
    return item.type.isStringOrUntyped() ?
      Token.compare(string(), item.string(ii), Collation.get(coll, ii)) :
      -item.compare(this, coll, transitive, ii);
  }

  @Override
  public final boolean deepEqual(final Item item, final DeepEqual deep) throws QueryException {
    if(!(item instanceof final XNode node2)) return false;

    final Kind kind1 = kind(), kind2 = node2.kind();
    if(kind1 != kind2) return false;
    final XNode node1 = this;
    if(node1.is(node2)) return true;

    QNm name1 = node1.qname(), name2 = node2.qname();
    if(kind1 == NAMESPACE) return name1.eq(name2) && Token.eq(node1.string(), node2.string());

    // compare names
    final DeepEqualOptions options = deep.options;
    if(name1 != null && (!name1.eq(name2) ||
        options.get(NAMESPACE_PREFIXES) && !Token.eq(name1.prefix(), name2.prefix())
    )) return false;
    // compare values
    if(kind1.oneOf(TEXT, COMMENT, PROCESSING_INSTRUCTION, ATTRIBUTE) &&
        !Token.eq(node1.string(), node2.string(), deep)) return false;
    // compare base URIs
    if(options.get(BASE_URI)) {
      if(deep.nested) return Token.eq(node1.baseURI(), node2.baseURI());
      final Uri uri1 = node1.baseURI(Uri.EMPTY, true, deep.info);
      final Uri uri2 = node2.baseURI(Uri.EMPTY, true, deep.info);
      if(!uri1.eq(uri2)) return false;
    }
    if(kind1 == ELEMENT) {
      // compare attributes
      final BasicNodeIter iter1 = node1.attributeIter();
      BasicNodeIter iter2 = node2.attributeIter();
      if(iter1.size() != iter2.size()) return false;

      for(GNode attr1; (attr1 = iter1.next()) != null;) {
        name1 = attr1.qname();
        for(GNode attr2;;) {
          attr2 = iter2.next();
          if(attr2 == null) return false;
          name2 = attr2.qname();
          if(name1.eq(name2)) {
            final Bln eq = deep.itemsEqual(attr1, attr2);
            if(eq == Bln.TRUE || eq == null &&
                (!options.get(NAMESPACE_PREFIXES) || Token.eq(name1.prefix(), name2.prefix())) &&
                Token.eq(attr1.string(), attr2.string(), deep)) break;
            return false;
          }
        }
        iter2 = node2.attributeIter();
      }

      // compare namespaces
      if(options.get(IN_SCOPE_NAMESPACES)) {
        final Atts atts1 = deep.nested ? node1.namespaces() : node1.nsScope(null);
        final Atts atts2 = deep.nested ? node2.namespaces() : node2.nsScope(null);
        if(!atts1.equals(atts2)) return false;
      }
    } else if(kind1 != DOCUMENT) {
      return true;
    }

    final Function<XNode, GNodeList> children = node -> {
      final GNodeList nl = new GNodeList();
      for(final GNode child : node.childIter()) {
        if(deep.qc != null) deep.qc.checkStop();
        final Kind kind = child.kind();
        if((kind != PROCESSING_INSTRUCTION || options.get(PROCESSING_INSTRUCTIONS)) &&
            (kind != COMMENT || options.get(COMMENTS))) {
          nl.add(kind != TEXT || nl.isEmpty() || nl.peek().kind() != TEXT ? child :
            new FTxt(Token.concat(nl.pop().string(), child.string())));
        }
      }
      if(options.get(WHITESPACE) != Whitespace.PRESERVE && !preserve()) {
        for(int n = nl.size() - 1; n >= 0; n--) {
          final GNode child = nl.get(n);
          if(child.kind() == TEXT && Token.ws(child.string())) nl.remove(n);
        }
      }
      return nl;
    };

    final GNodeList list1 = children.apply(node1), list2 = children.apply(node2);
    final int size1 = list1.size();
    if(size1 != list2.size()) return false;
    deep.nested = true;

    // respect order
    if(name1 == null || !options.unordered(name1)) {
      for(final NodeIter iter1 = list1.iter(), iter2 = list2.iter();;) {
        if(deep.qc != null) deep.qc.checkStop();
        final GNode child1 = iter1.next();
        if(child1 == null) return true;
        if(!deep.equal(child1, iter2.next())) return false;
      }
    }

    // ignore order
    for(int l1 = size1 - 1; l1 >= 0; l1--) {
      boolean found = false;
      for(int l2 = list2.size() - 1; !found && l2 >= 0; l2--) {
        if(deep.qc != null) deep.qc.checkStop();
        if(deep.equal(list1.get(l1), list2.get(l2))) {
          list2.remove(l2);
          found = true;
        }
      }
      if(!found) return false;
    }
    return true;
  }

  /**
   * Returns if whitespace needs to be preserved.
   * @return result of check
   */
  private boolean preserve() {
    final QNm xs = new QNm(XMLToken.XML_SPACE, QueryText.XML_URI);
    for(XNode node = this; node != null; node = node.parent()) {
      if(node.kind() == ELEMENT) {
        final byte[] v = node.attribute(xs);
        if(v != null) return Token.eq(v, XMLToken.PRESERVE);
      }
    }
    return false;
  }

  @Override
  public final Item atomValue(final QueryContext qc, final InputInfo ii) {
    return atomItem(qc, ii);
  }

  @Override
  public final Item atomItem(final QueryContext qc, final InputInfo ii) {
    return kind().oneOf(PROCESSING_INSTRUCTION, COMMENT) ? Str.get(string()) : Atm.get(string());
  }

  @Override
  public abstract XNode materialize(Predicate<Data> test, InputInfo ii, QueryContext qc)
      throws QueryException;

  @Override
  public final boolean materialized(final Predicate<Data> test, final InputInfo ii) {
    return test.test(data());
  }

  /**
   * Creates a database node copy from this node.
   * @param qc query context
   * @return database node
   * @throws QueryException query exception
   */
  public final DBNode copy(final QueryContext qc) throws QueryException {
    return copy(qc.context.options, qc);
  }

  /**
   * Creates a database node copy from this node.
   * @param options main options
   * @param job interruptible job (can be {@code null})
   * @return database node
   * @throws QueryException query exception
   */
  public final DBNode copy(final MainOptions options, final Job job) throws QueryException {
    final MemData data = new MemData(options);
    new DataBuilder(data, job).build(this);
    return new DBNode(data);
  }

  @Override
  public byte[] name() {
    return null;
  }

  @Override
  public QNm qname() {
    return null;
  }

  /**
   * Returns all namespaces defined for the node.
   * Overwritten by {@link FElem} and {@link DBNode}.
   * @return namespace array or {@code null}
   */
  public Atts namespaces() {
    return null;
  }

  /**
   * Returns a copy of the namespace hierarchy.
   * @param sc static context (can be {@code null})
   * @return namespaces
   */
  public final Atts nsScope(final StaticContext sc) {
    final Atts ns = new Atts();
    for(XNode node = this; node != null; node = node.parent()) {
      final Atts nsp = node.namespaces();
      if(nsp != null) {
        for(int a = nsp.size() - 1; a >= 0; a--) {
          final byte[] name = nsp.name(a);
          if(!ns.contains(name)) ns.add(name, nsp.value(a));
        }
      }
    }
    if(sc != null) sc.ns.inScope(ns);
    return ns;
  }

  /**
   * Recursively finds the URI for the specified prefix.
   * @param prefix prefix
   * @return URI or {@code null}
   */
  public final byte[] uri(final byte[] prefix) {
    final Atts ns = namespaces();
    if(ns != null) {
      final byte[] s = ns.value(prefix);
      if(s != null) return s;
      final XNode n = parent();
      if(n != null) return n.uri(prefix);
    }
    return prefix.length == 0 ? Token.EMPTY : null;
  }

  /**
   * Returns the base URI of the node.
   * @return base URI
   */
  public byte[] baseURI() {
    return Token.EMPTY;
  }

  /**
   * Returns the static base URI of a node.
   * @param base static base URI
   * @param empty return empty URI if a node has no base URI, or {@code null} otherwise
   * @param info input info (can be {@code null})
   * @return base URI or {@code null}
   * @throws QueryException query exception
   */
  public final Uri baseURI(final Uri base, final boolean empty, final InputInfo info)
      throws QueryException {

    if(!kind().oneOf(ELEMENT, DOCUMENT) && parent() == null) {
      return empty ? Uri.EMPTY : null;
    }
    Uri uri = Uri.EMPTY;
    XNode nd = this;
    do {
      final Uri bu = Uri.get(nd.baseURI(), false);
      if(!bu.isValid()) throw INVURI_X.get(info, nd.baseURI());
      uri = bu.resolve(uri, info);
      if(nd.kind() == DOCUMENT && nd instanceof DBNode) break;
      nd = nd.parent();
    } while(!uri.isAbsolute() && nd != null);
    return nd == null || uri == Uri.EMPTY ? base.resolve(uri, info) : uri;
  }

  /**
   * Compares two nodes for their unique order.
   * @param node1 first node
   * @param node2 node to be compared
   * @return result of comparison (-1, 0, 1)
   */
  static int compare(final XNode node1, final XNode node2) {
    // cache parents of first node
    final GNodeList nl = new GNodeList();
    for(XNode node = node1; node != null; node = node.parent()) {
      if(node == node2) return 1;
      nl.add(node);
    }
    // find the lowest common ancestor
    GNode c2 = node2;
    LOOP:
    for(GNode node = node2; (node = node.parent()) != null;) {
      final int is = nl.size();
      for(int i = 1; i < is; i++) {
        if(node == node1) return -1;
        if(!nl.get(i).is(node)) continue;
        // check which node appears as first LCA child
        final GNode c1 = nl.get(i - 1);
        for(final GNode c : node.childIter()) {
          if(c.is(c1)) return -1;
          if(c.is(c2)) return 1;
        }
        break LOOP;
      }
      c2 = node;
    }
    return Integer.signum(node1.id - node2.id);
  }

  @Override
  public final XNode root() {
    final XNode p = parent();
    return p == null ? this : p.root();
  }

  @Override
  public abstract XNode parent();

  /**
   * Sets the parent node.
   * @param par parent node
   */
  public abstract void parent(FNode par);

  /**
   * Indicates if the node has attributes.
   * @return result of test
   */
  public abstract boolean hasAttributes();

  /**
   * Returns the value of the specified attribute.
   * @param name attribute to be found
   * @return attribute value or {@code null}
   */
  public final byte[] attribute(final QNm name) {
    final BasicNodeIter iter = attributeIter();
    while(true) {
      final GNode node = iter.next();
      if(node == null) return null;
      if(node.qname().eq(name)) return node.string();
    }
  }

  /**
   * Returns the numeric database node kind.
   * @return node kind
   */
  public int dbKind() {
    return dbKind(kind());
  }

  /**
   * Returns the numeric database node kind for a node kind.
   * @param kind node kind
   * @return node kind, or {@code -1} if no corresponding database kind exists
   */
  public static int dbKind(final Kind kind) {
    return switch(kind) {
      case DOCUMENT -> Data.DOC;
      case ELEMENT -> Data.ELEM;
      case TEXT -> Data.TEXT;
      case ATTRIBUTE -> Data.ATTR;
      case COMMENT -> Data.COMM;
      case PROCESSING_INSTRUCTION -> Data.PI;
      default -> -1;
    };
  }

  /**
   * Returns a node type for the specified database kind.
   * @param k database kind
   * @return node type
   */
  public static NodeType type(final int k) {
    return TYPES[k];
  }

  @Override
  public abstract BXNode toJava() throws QueryException;
}
