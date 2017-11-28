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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class ANode extends Item {
  /** Node Types. */
  private static final NodeType[] TYPES = {
    NodeType.DOC, NodeType.ELM, NodeType.TXT, NodeType.ATT, NodeType.COM, NodeType.PI
  };
  /** Static node counter. */
  private static final AtomicInteger ID = new AtomicInteger();
  /** Unique node id. ID can get negative, as subtraction of ids is used for all comparisons. */
  public final int id = ID.incrementAndGet();

  /** Cached string value. */
  byte[] value;
  /** Parent node (can be {@code null}). */
  ANode parent;

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
  public final boolean eq(final Item it, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return it.type.isUntyped() ? coll == null ? Token.eq(string(), it.string(ii)) :
      coll.compare(string(), it.string(ii)) == 0 : it.eq(this, coll, sc, ii);
  }

  @Override
  public boolean sameKey(final Item it, final InputInfo ii) throws QueryException {
    return it.type.isStringOrUntyped() && eq(it, null, null, ii);
  }

  @Override
  public final int diff(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    return it.type.isUntyped() ? coll == null ? Token.diff(string(), it.string(ii)) :
      coll.compare(string(), it.string(ii)) : -it.diff(this, coll, ii);
  }

  @Override
  public final Item atomItem(final InputInfo ii) {
    return type == NodeType.PI || type == NodeType.COM ? Str.get(string()) : new Atm(string());
  }

  /**
   * Returns a deep copy of the node.
   * @param opts main options
   * @return node copy
   */
  public abstract ANode deepCopy(MainOptions opts);

  /**
   * Returns a final node representation. This method is called when iterating through node
   * results: Iterated node instances may be reused and may need to be copied in the final step.
   * @return node
   */
  public abstract ANode finish();

  /**
   * Returns a database node representation of the node.
   * @param opts main options
   * @return database node
   */
  public DBNode dbNodeCopy(final MainOptions opts) {
    final MemData md = new MemData(opts);
    new DataBuilder(md).build(this);
    return new DBNode(md);
  }

  /**
   * Returns the name of the node, composed of an optional prefix and the local name.
   * It is more efficient than calling {@link #qname}, as no {@link QNm} instance must be created.
   * @return name, or {@code null} if node has no name
   */
  public byte[] name() {
    return null;
  }

  /**
   * Returns the QName of the node.
   * @return name, or {@code null} if node has no name
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
    } while(node != null && node.type == NodeType.ELM);
    if(sc != null) sc.ns.inScope(ns);
    return ns;
  }

  /**
   * Recursively finds the uri for the specified prefix.
   * @param pref prefix
   * @return uri, or {@code null}
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
        for(final ANode c : n.children()) {
          if(c.is(c1)) return -1;
          if(c.is(c2)) return 1;
        }
        break LOOP;
      }
      c2 = n;
    }
    return node1.id - node2.id < 0 ? -1 : 1;
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
   * @return self reference
   */
  protected abstract ANode parent(ANode par);

  /**
   * Returns true if the node has children.
   * @return result of test
   */
  public abstract boolean hasChildren();

  /**
   * Returns the value of the specified attribute or {@code null}.
   * @param name attribute to be found
   * @return attribute value
   */
  public byte[] attribute(final byte[] name) {
    return attribute(new QNm(name));
  }

  /**
   * Returns the value of the specified attribute or {@code null}.
   * @param name attribute to be found
   * @return attribute value or {@code null}
   */
  public byte[] attribute(final QNm name) {
    final BasicNodeIter iter = attributes();
    while(true) {
      final ANode node = iter.next();
      if(node == null) return null;
      if(node.qname().eq(name)) return node.string();
    }
  }

  /**
   * Returns a lightweight, low-level ancestor axis iterator.
   * If nodes returned are to be further used, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public abstract BasicNodeIter ancestor();

  /**
   * Returns a light-weight ancestor-or-self axis iterator.
   * If nodes returned are to be further used, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public abstract BasicNodeIter ancestorOrSelf();

  /**
   * Returns a lightweight, low-level attribute axis iterator with {@link Iter#size()} and
   * {@link Iter#get(long)} implemented.
   * If nodes returned are to be further used, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public abstract BasicNodeIter attributes();

  /**
   * Returns a lightweight, low-level child axis iterator.
   * If nodes returned are to be further used, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public abstract BasicNodeIter children();

  /**
   * Returns a lightweight, low-level descendant axis iterator.
   * If nodes returned are to be further used, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public abstract BasicNodeIter descendant();

  /**
   * Returns a lightweight, low-level descendant-or-self axis iterator.
   * If nodes returned are to be further used, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public abstract BasicNodeIter descendantOrSelf();

  /**
   * Returns a lightweight, low-level following axis iterator.
   * If nodes returned are to be further used, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public abstract BasicNodeIter following();

  /**
   * Returns a lightweight, low-level following-sibling axis iterator.
   * If nodes returned are to be further used, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public abstract BasicNodeIter followingSibling();

  /**
   * Returns a lightweight, low-level parent axis iterator.
   * If nodes returned are to be further used, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public final BasicNodeIter parentIter() {
    return new BasicNodeIter() {
      private boolean all;
      @Override
      public ANode next() {
        if(all) return null;
        all = true;
        return parent();
      }
      @Override
      public ANode get(final long i) {
        return parent();
      }
      @Override
      public ANode value(final QueryContext qc) {
        return parent();
      }
      @Override
      public long size() {
        return 1;
      }
    };
  }

  /**
   * Returns a lightweight, low-level preceding axis iterator.
   * If nodes returned are to be further used, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public final BasicNodeIter preceding() {
    return new BasicNodeIter() {
      /** Iterator. */
      private BasicNodeIter iter;

      @Override
      public ANode next() {
        if(iter == null) {
          final ANodeList list = new ANodeList();
          ANode n = ANode.this;
          ANode p = n.parent();
          while(p != null) {
            if(n.type != NodeType.ATT) {
              final ANodeList tmp = new ANodeList();
              for(final ANode c : p.children()) {
                if(c.is(n)) break;
                tmp.add(c.finish());
                addDesc(c.children(), tmp);
              }
              for(int t = tmp.size() - 1; t >= 0; t--) list.add(tmp.get(t));
            }
            n = p;
            p = p.parent();
          }
          iter = list.iter();
        }
        return iter.next();
      }
    };
  }

  /**
   * Returns a lightweight, low-level preceding-sibling axis iterator.
   * If nodes returned are to be further used, they must be finalized via {@link ANode#finish()}.
   * @return iterator
   */
  public final BasicNodeIter precedingSibling() {
    return new BasicNodeIter() {
      /** Child nodes. */
      private BasicNodeIter iter;
      /** Counter. */
      private int i;

      @Override
      public ANode next() {
        if(iter == null) {
          if(type == NodeType.ATT) return null;
          final ANode r = parent();
          if(r == null) return null;

          final ANodeList list = new ANodeList();
          for(final ANode n : r.children()) {
            if(n.is(ANode.this)) break;
            list.add(n.finish());
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
  public final BasicNodeIter self() {
    return new BasicNodeIter() {
      /** Flag. */
      private boolean all;

      @Override
      public ANode next() {
        if(all) return null;
        all = true;
        return ANode.this;
      }
    };
  }

  /**
   * Adds children of a sub node.
   * @param ch child nodes
   * @param nb node cache
   */
  static void addDesc(final BasicNodeIter ch, final ANodeList nb) {
    for(final ANode n : ch) {
      nb.add(n.finish());
      addDesc(n.children(), nb);
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
   * @param t node type
   * @return node kind
   */
  public static int kind(final NodeType t) {
    switch(t) {
      case DOC: return Data.DOC;
      case ELM: return Data.ELEM;
      case TXT: return Data.TEXT;
      case ATT: return Data.ATTR;
      case COM: return Data.COMM;
      case PI : return Data.PI;
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
