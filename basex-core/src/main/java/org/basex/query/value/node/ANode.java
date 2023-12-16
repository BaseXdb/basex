package org.basex.query.value.node;

import static org.basex.query.util.DeepEqualOptions.*;
import static org.basex.query.value.type.NodeType.*;

import java.io.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.basex.api.dom.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.io.out.DataOutput;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract node type.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public abstract class ANode extends Item {
  /** QName: xml:base. */
  static final QNm XML_BASE = new QNm(QueryText.BASE, QueryText.XML_URI);
  /** Node Types. */
  private static final NodeType[] TYPES = {
    DOCUMENT_NODE, ELEMENT, TEXT, ATTRIBUTE, COMMENT, PROCESSING_INSTRUCTION
  };
  /** Static node counter. */
  private static final AtomicInteger ID = new AtomicInteger();
  /** Unique node id. ID can get negative, as subtraction of ids is used for all comparisons. */
  public final int id = ID.incrementAndGet();

  /**
   * Constructor.
   * @param type item type
   */
  ANode(final NodeType type) {
    super(type);
  }

  @Override
  public final void write(final DataOutput out) throws IOException {
    final ArrayOutput ao = new ArrayOutput();
    Serializer.get(ao).serialize(this);
    out.writeToken(ao.finish());
  }

  @Override
  public final boolean bool(final InputInfo ii) {
    return true;
  }

  @Override
  public final byte[] string(final InputInfo ii) {
    return string();
  }

  /**
   * Returns the string value.
   * @return string value
   */
  public abstract byte[] string();

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {
    Expr expr = this;
    if(mode == Simplify.STRING) {
      expr = Str.get(string());
    } else if(mode.oneOf(Simplify.DATA, Simplify.NUMBER)) {
      expr = Atm.get(string());
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public final boolean comparable(final Item item) {
    return item.type.isStringOrUntyped();
  }

  @Override
  public final boolean equal(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return item.type.isStringOrUntyped() ? Token.eq(string(), item.string(ii), coll) :
      item.equal(this, coll, sc, ii);
  }

  @Override
  public final int compare(final Item item, final Collation coll, final boolean transitive,
      final InputInfo ii) throws QueryException {
    return item.type.isStringOrUntyped() ? Token.compare(string(), item.string(ii), coll) :
      -item.compare(this, coll, transitive, ii);
  }

  @Override
  public final boolean deepEqual(final Item item, final DeepEqual deep) throws QueryException {
    final Type type1 = type, type2 = item.type;
    if(type1 != type2) return false;
    final ANode node1 = this, node2 = (ANode) item;
    if(node1.is(node2)) return true;

    QNm name1 = node1.qname(), name2 = node2.qname();
    if(type1 == NAMESPACE_NODE) return name1.eq(name2) && Token.eq(node1.string(), node2.string());

    // compare names
    final DeepEqualOptions options = deep.options;
    if(name1 != null && (!name1.eq(name2) ||
        options.get(NAMESPACE_PREFIXES) && !Token.eq(name1.prefix(), name2.prefix())
    )) return false;
    // compare values
    if(type1.oneOf(TEXT, COMMENT, PROCESSING_INSTRUCTION, ATTRIBUTE) &&
        !Token.eq(node1.string(), node2.string(), deep)) return false;
    // compare base URIs
    if(options.get(BASE_URI)) {
      if(deep.nested) return Token.eq(node1.baseURI(), node2.baseURI());
      final Uri uri1 = FnBaseUri.uri(node1, Uri.EMPTY, deep.info);
      final Uri uri2 = FnBaseUri.uri(node2, Uri.EMPTY, deep.info);
      if(uri1 == null ? uri2 != null : uri2 == null || !uri1.eq(uri2)) return false;
    }
    if(type1 == ELEMENT) {
      // compare attributes
      final BasicNodeIter iter1 = node1.attributeIter();
      BasicNodeIter iter2 = node2.attributeIter();
      if(iter1.size() != iter2.size()) return false;

      for(ANode attr1; (attr1 = iter1.next()) != null;) {
        name1 = attr1.qname();
        for(ANode attr2;;) {
          attr2 = iter2.next();
          if(attr2 == null) return false;
          name2 = attr2.qname();
          if(name1.eq(name2)) {
            if((!options.get(NAMESPACE_PREFIXES) || Token.eq(name1.prefix(), name2.prefix())) &&
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
    } else if(type1 != DOCUMENT_NODE) {
      return true;
    }

    final Function<ANode, ANodeList> children = node -> {
      final ANodeList nl = new ANodeList();
      for(final ANode child : node.childIter()) {
        if(deep.qc != null) deep.qc.checkStop();
        final Type tp = child.type;
        if(tp == COMMENT && !options.get(COMMENTS) ||
           tp == PROCESSING_INSTRUCTION && !options.get(PROCESSING_INSTRUCTIONS)) continue;
        if(tp == TEXT) {
          final byte[] string = child.string();
          if(Token.ws(string) && !options.get(PRESERVE_SPACE)) continue;
          if(!nl.isEmpty() && nl.peek().type == NodeType.TEXT && !options.get(TEXT_BOUNDARIES)) {
            nl.add(new FTxt(Token.concat(nl.pop().string(), string)));
            continue;
          }
        }
        nl.add(child.finish());
      }
      return nl;
    };

    final ANodeList list1 = children.apply(node1), list2 = children.apply(node2);
    final int size1 = list1.size();
    if(size1 != list2.size()) return false;
    deep.nested = true;

    // respect order
    if(name1 == null || !options.unordered(name1)) {
      for(final NodeIter iter1 = list1.iter(), iter2 = list2.iter();;) {
        if(deep.qc != null) deep.qc.checkStop();
        final ANode child1 = iter1.next();
        if(child1 == null) return true;
        if(!child1.deepEqual(iter2.next(), deep)) return false;
      }
    }

    // ignore order
    for(int l1 = size1 - 1; l1 >= 0; l1--) {
      boolean found = false;
      for(int l2 = list2.size() - 1; !found && l2 >= 0; l2--) {
        if(deep.qc != null) deep.qc.checkStop();
        if(list1.get(l1).deepEqual(list2.get(l2), deep)) {
          list2.remove(l2);
          found = true;
        }
      }
      if(!found) return false;
    }
    return true;
  }

  @Override
  public final Item atomValue(final QueryContext qc, final InputInfo ii) {
    return atomItem(qc, ii);
  }

  @Override
  public final Item atomItem(final QueryContext qc, final InputInfo ii) {
    return type.oneOf(PROCESSING_INSTRUCTION, COMMENT) ? Str.get(string()) : Atm.get(string());
  }

  @Override
  public abstract ANode materialize(Predicate<Data> test, InputInfo ii, QueryContext qc)
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
   * @param qc query context (can be {@code null}; if supplied, allows interruption of process)
   * @return database node
   * @throws QueryException query exception
   */
  public final DBNode copy(final MainOptions options, final QueryContext qc) throws QueryException {
    final MemData data = new MemData(options);
    new DataBuilder(data, qc).build(this);
    return new DBNode(data);
  }

  /**
   * Returns a finalized node instance. This method is called when iterating through node results:
   * If a single node instances is recycled, it needs to be duplicated in the final step.
   * @return node
   */
  public abstract ANode finish();

  /**
   * Returns the name (optional prefix, local name) of an attribute, element or
   * processing instruction. This function is possibly evaluated faster than {@link #qname()},
   * as no {@link QNm} instance may need to be created.
   * @return name, or {@code null} if node has no name
   */
  public byte[] name() {
    return null;
  }

  /**
   * Returns the QName (optional prefix, local name) of an attribute, element or
   * processing instruction.
   * @return name, or {@code null} if node has no QName
   */
  public QNm qname() {
    return null;
  }

  /**
   * Returns all namespaces defined for the nodes.
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
    ANode node = this;
    do {
      final Atts nsp = node.namespaces();
      if(nsp != null) {
        for(int a = nsp.size() - 1; a >= 0; a--) {
          final byte[] key = nsp.name(a);
          if(!ns.contains(key)) ns.add(key, nsp.value(a));
        }
      }
      node = node.parent();
    } while(node != null && node.type == ELEMENT);
    if(sc != null) sc.ns.inScope(ns);
    return ns;
  }

  /**
   * Recursively finds the uri for the specified prefix.
   * @param prefix prefix
   * @return uri or {@code null}
   */
  public final byte[] uri(final byte[] prefix) {
    final Atts ns = namespaces();
    if(ns != null) {
      final byte[] s = ns.value(prefix);
      if(s != null) return s;
      final ANode n = parent();
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
   * Checks if two nodes are identical.
   * @param node node to be compared
   * @return result of check
   */
  public abstract boolean is(ANode node);

  /**
   * Checks the document order of two nodes.
   * @param node node to be compared
   * @return {@code 0} if the nodes are identical, or {@code 1}/{@code -1}
   * if the node appears after/before the argument
   */
  public abstract int compare(ANode node);

  /**
   * Compares two nodes for their unique order.
   * @param node1 first node
   * @param node2 node to be compared
   * @return {@code 0} if the nodes are identical, or {@code 1}/{@code -1}
   * if the first node appears after/before the second
   */
  static int compare(final ANode node1, final ANode node2) {
    // cache parents of first node
    final ANodeList nl = new ANodeList();
    for(ANode n = node1; n != null; n = n.parent()) {
      if(n == node2) return 1;
      nl.add(n);
    }
    // find the lowest common ancestor
    ANode c2 = node2;
    LOOP:
    for(ANode n = node2; (n = n.parent()) != null;) {
      final int is = nl.size();
      for(int i = 1; i < is; i++) {
        if(n == node1) return -1;
        if(!nl.get(i).is(n)) continue;
        // check which node appears as first LCA child
        final ANode c1 = nl.get(i - 1);
        for(final ANode c : n.childIter()) {
          if(c.is(c1)) return -1;
          if(c.is(c2)) return 1;
        }
        break LOOP;
      }
      c2 = n;
    }
    return node1.id - node2.id;
  }

  /**
   * Returns the root of a node (the topmost ancestor without parent node).
   * @return root node
   */
  public final ANode root() {
    final ANode p = parent();
    return p == null ? this : p.root();
  }

  /**
   * Returns the parent node.
   * @return parent node or {@code null}
   */
  public abstract ANode parent();

  /**
   * Sets the parent node.
   * @param par parent node
   */
  public abstract void parent(FNode par);

  /**
   * Indicates if the node has children.
   * @return result of test
   */
  public abstract boolean hasChildren();

  /**
   * Returns the value of the specified attribute.
   * @param name attribute to be found
   * @return attribute value or {@code null}
   */
  public final byte[] attribute(final QNm name) {
    final BasicNodeIter iter = attributeIter();
    while(true) {
      final ANode node = iter.next();
      if(node == null) return null;
      if(node.qname().eq(name)) return node.string();
    }
  }

  /**
   * Returns a light-weight, low-level ancestor axis iterator.
   * Before nodes are added to the result, they must be finalized via {@link ANode#finish()}.
   * Overwritten by {@link DBNode#ancestorIter()}.
   * @return iterator
   */
  public BasicNodeIter ancestorIter() {
    return new BasicNodeIter() {
      private ANode node = ANode.this;

      @Override
      public ANode next() {
        node = node.parent();
        return node;
      }
    };
  }

  /**
   * Returns a light-weight ancestor-or-self axis iterator.
   * Before nodes are added to the result, they must be finalized via {@link ANode#finish()}.
   * Overwritten by {@link DBNode#ancestorOrSelfIter()}.
   * @return iterator
   */
  public BasicNodeIter ancestorOrSelfIter() {
    return new BasicNodeIter() {
      private ANode node = ANode.this;

      @Override
      public ANode next() {
        if(node == null) return null;
        final ANode n = node;
        node = n.parent();
        return n;
      }
    };
  }

  /**
   * Returns a light-weight, low-level attribute axis iterator with {@link Iter#size()} and
   * {@link Iter#get(long)} implemented.
   * Before nodes are added to the result, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public abstract BasicNodeIter attributeIter();

  /**
   * Returns a light-weight, low-level child axis iterator.
   * Before nodes are added to the result, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public abstract BasicNodeIter childIter();

  /**
   * Returns a light-weight, low-level descendant axis iterator.
   * Before nodes are added to the result, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public abstract BasicNodeIter descendantIter();

  /**
   * Returns a light-weight, low-level descendant-or-self axis iterator.
   * Before nodes are added to the result, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public abstract BasicNodeIter descendantOrSelfIter();

  /**
   * Returns a light-weight, low-level following axis iterator.
   * Before nodes are added to the result, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public BasicNodeIter followingIter() {
    return new BasicNodeIter() {
      private BasicNodeIter iter;

      @Override
      public ANode next() {
        if(iter == null) {
          final ANodeList list = new ANodeList();
          ANode node = ANode.this, root = node.parent();
          while(root != null) {
            final BasicNodeIter ir = root.childIter();
            if(node.type != ATTRIBUTE) {
              for(final ANode nd : ir) {
                if(nd.is(node)) break;
              }
            }
            for(final ANode nd : ir) {
              list.add(nd.finish());
              addDesc(nd.childIter(), list);
            }
            node = root;
            root = root.parent();
          }
          iter = list.iter();
        }
        return iter.next();
      }
    };
  }

  /**
   * Returns a light-weight, low-level following-sibling axis iterator.
   * Before nodes are added to the result, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public BasicNodeIter followingSiblingIter() {
    return new BasicNodeIter() {
      private BasicNodeIter iter;

      @Override
      public ANode next() {
        if(iter == null) {
          final ANode root = parent();
          if(root == null) return null;
          iter = root.childIter();
          for(ANode n; (n = iter.next()) != null && !n.is(ANode.this););
        }
        return iter.next();
      }
    };
  }

  /**
   * Returns a light-weight, low-level parent axis iterator.
   * Before nodes are added to the result, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public final BasicNodeIter parentIter() {
    return new BasicNodeIter() {
      private boolean called;

      @Override
      public ANode next() {
        if(called) return null;
        called = true;
        return parent();
      }
    };
  }

  /**
   * Returns a light-weight, low-level preceding axis iterator.
   * Before nodes are added to the result, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public final BasicNodeIter precedingIter() {
    return new BasicNodeIter() {
      private BasicNodeIter iter;

      @Override
      public ANode next() {
        if(iter == null) {
          final ANodeList list = new ANodeList();
          ANode node = ANode.this, root = node.parent();
          while(root != null) {
            if(node.type != ATTRIBUTE) {
              final ANodeList tmp = new ANodeList();
              for(final ANode c : root.childIter()) {
                if(c.is(node)) break;
                tmp.add(c.finish());
                addDesc(c.childIter(), tmp);
              }
              for(int t = tmp.size() - 1; t >= 0; t--) list.add(tmp.get(t));
            }
            node = root;
            root = root.parent();
          }
          iter = list.iter();
        }
        return iter.next();
      }
    };
  }

  /**
   * Returns a light-weight, low-level preceding-sibling axis iterator.
   * Before nodes are added to the result, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public final BasicNodeIter precedingSiblingIter() {
    return new BasicNodeIter() {
      private BasicNodeIter iter;
      private int i;

      @Override
      public ANode next() {
        if(iter == null) {
          if(type == ATTRIBUTE) return null;
          final ANode root = parent();
          if(root == null) return null;

          final ANodeList list = new ANodeList();
          for(final ANode node : root.childIter()) {
            if(node.is(ANode.this)) break;
            list.add(node.finish());
          }
          i = list.size();
          iter = list.iter();
        }
        return i > 0 ? iter.get(--i) : null;
      }
    };
  }

  /**
   * Returns a self axis iterator.
   * @return iterator
   */
  public final BasicNodeIter selfIter() {
    return new BasicNodeIter() {
      private boolean called;

      @Override
      public ANode next() {
        if(called) return null;
        called = true;
        return ANode.this;
      }
    };
  }

  /**
   * Adds children of a sub node.
   * @param children child nodes
   * @param nodes node cache
   */
  private static void addDesc(final BasicNodeIter children, final ANodeList nodes) {
    for(final ANode node : children) {
      nodes.add(node.finish());
      addDesc(node.childIter(), nodes);
    }
  }

  /**
   * Returns a database kind for the specified node type.
   * @return node kind
   */
  public int kind() {
    return kind((NodeType) type);
  }

  /**
   * Returns a database kind for the specified node type.
   * @param type node type
   * @return node kind, or {@code -1} if no corresponding database kind exists
   */
  public static int kind(final NodeType type) {
    switch(type) {
      case DOCUMENT_NODE:          return Data.DOC;
      case ELEMENT:                return Data.ELEM;
      case TEXT:                   return Data.TEXT;
      case ATTRIBUTE:              return Data.ATTR;
      case COMMENT:                return Data.COMM;
      case PROCESSING_INSTRUCTION: return Data.PI;
      default:                     return -1;
    }
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
