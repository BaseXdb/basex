package org.basex.query.value.node;

import java.util.concurrent.atomic.*;

import org.basex.api.dom.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class ANode extends Item {
  /** Node Types. */
  private static final NodeType[] TYPES = {
    NodeType.DOCUMENT_NODE, NodeType.ELEMENT, NodeType.TEXT, NodeType.ATTRIBUTE,
    NodeType.COMMENT, NodeType.PROCESSING_INSTRUCTION
  };
  /** Static node counter. */
  private static final AtomicInteger ID = new AtomicInteger();
  /** Unique node id. ID can get negative, as subtraction of ids is used for all comparisons. */
  public final int id = ID.incrementAndGet();

  /** Cached string value. */
  byte[] value;

  /**
   * Constructor.
   * @param type item type
   */
  ANode(final NodeType type) {
    super(type);
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
  public final boolean comparable(final Item item) {
    return item.type.isStringOrUntyped();
  }

  @Override
  public final boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return item.type.isUntyped() ? coll == null ? Token.eq(string(), item.string(ii)) :
      coll.compare(string(), item.string(ii)) == 0 : item.eq(this, coll, sc, ii);
  }

  @Override
  public boolean sameKey(final Item item, final InputInfo ii) throws QueryException {
    return item.type.isStringOrUntyped() && eq(item, null, null, ii);
  }

  @Override
  public final int diff(final Item item, final Collation coll, final InputInfo ii)
      throws QueryException {
    return item.type.isUntyped() ? coll == null ? Token.diff(string(), item.string(ii)) :
      coll.compare(string(), item.string(ii)) : -item.diff(this, coll, ii);
  }

  @Override
  public final Item atomValue(final QueryContext qc, final InputInfo ii) {
    return atomItem();
  }

  @Override
  public final Item atomItem(final QueryContext qc, final InputInfo ii) {
    return atomItem();
  }

  /**
   * Returns an atomic item.
   * @return item
   */
  public Item atomItem() {
    return type == NodeType.PROCESSING_INSTRUCTION || type == NodeType.COMMENT ? Str.get(string()) :
      new Atm(string());
  }

  @Override
  public boolean ddo() {
    return true;
  }

  @Override
  public abstract ANode materialize(QueryContext qc, boolean copy);

  /**
   * Creates a database node copy from this node.
   * @param qc query context
   * @return database node
   */
  public final DBNode copy(final QueryContext qc) {
    return copy(qc.context.options, qc);
  }

  /**
   * Creates a database node copy from this node.
   * @param options main options
   * @param qc query context (can be {@code null}; if supplied, allows interruption of process)
   * @return database node
   */
  public final DBNode copy(final MainOptions options, final QueryContext qc) {
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
   * Minimizes the memory consumption of the node.
   * @return self reference
   */
  public ANode optimize() {
    return this;
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
    } while(node != null && node.type == NodeType.ELEMENT);
    if(sc != null) sc.ns.inScope(ns);
    return ns;
  }

  /**
   * Recursively finds the uri for the specified prefix.
   * @param pref prefix
   * @return uri or {@code null}
   */
  public final byte[] uri(final byte[] pref) {
    final Atts at = namespaces();
    if(at != null) {
      final byte[] s = at.value(pref);
      if(s != null) return s;
      final ANode n = parent();
      if(n != null) return n.uri(pref);
    }
    return pref.length == 0 ? Token.EMPTY : null;
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
  public abstract int diff(ANode node);

  /**
   * Compares two nodes for their unique order.
   * @param node1 first node
   * @param node2 node to be compared
   * @return {@code 0} if the nodes are identical, or {@code 1}/{@code -1}
   * if the first node appears after/before the second
   */
  static int diff(final ANode node1, final ANode node2) {
    // cache parents of first node
    final ANodeList nl = new ANodeList();
    for(ANode n = node1; n != null; n = n.parent()) {
      if(n == node2) return 1;
      nl.add(n);
    }
    // find lowest common ancestor
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
  public byte[] attribute(final byte[] name) {
    return attribute(new QNm(name));
  }

  /**
   * Returns the value of the specified attribute.
   * @param name attribute to be found
   * @return attribute value or {@code null}
   */
  public byte[] attribute(final QNm name) {
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
            if(node.type != NodeType.ATTRIBUTE) {
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
            if(node.type != NodeType.ATTRIBUTE) {
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
          if(type == NodeType.ATTRIBUTE) return null;
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
  static void addDesc(final BasicNodeIter children, final ANodeList nodes) {
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
    return kind(nodeType());
  }

  /**
   * Returns a database kind for the specified node type.
   * @param type node type
   * @return node kind, or {@code -1} if no corresponding database kind exists
   */
  public static int kind(final NodeType type) {
    switch(type) {
      case DOCUMENT_NODE: return Data.DOC;
      case ELEMENT: return Data.ELEM;
      case TEXT: return Data.TEXT;
      case ATTRIBUTE: return Data.ATTR;
      case COMMENT: return Data.COMM;
      case PROCESSING_INSTRUCTION : return Data.PI;
      default : return -1;
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
  public abstract BXNode toJava();

  /**
   * Returns this Node's node type.
   * @return node type
   */
  public final NodeType nodeType() {
    return (NodeType) type;
  }
}
