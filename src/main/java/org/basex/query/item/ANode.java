package org.basex.query.item;

import java.io.IOException;

import org.basex.api.dom.BXAttr;
import org.basex.api.dom.BXComm;
import org.basex.api.dom.BXDoc;
import org.basex.api.dom.BXElem;
import org.basex.api.dom.BXNode;
import org.basex.api.dom.BXPI;
import org.basex.api.dom.BXText;
import org.basex.data.Data;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.iter.NodeCache;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.AxisMoreIter;
import org.basex.util.Atts;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Abstract node type.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class ANode extends Item {
  /** Node Types. */
  private static final NodeType[] TYPES = {
    NodeType.DOC, NodeType.ELM, NodeType.TXT, NodeType.ATT,
    NodeType.COM, NodeType.PI
  };
  /** Static node counter. */
  // [CG] XQuery/ID: move to query context to reduce chance of overflow
  private static int sid;
  /** Unique node id. */
  public final int id = ++sid;

  /** Cached string value. */
  protected byte[] val;
  /** Parent node. */
  protected ANode par;

  /**
   * Constructor.
   * @param t data type
   */
  protected ANode(final NodeType t) {
    super(t);
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
  public final boolean eq(final InputInfo ii, final Item it)
      throws QueryException {
    return !it.isUntyped() ? it.eq(ii, this) :
      Token.eq(string(), it.string(ii));
  }

  @Override
  public final int diff(final InputInfo ii, final Item it)
      throws QueryException {
    return !it.isUntyped() ? -it.diff(ii, this) :
      Token.diff(string(), it.string(ii));
  }

  /**
   * Returns a copy of the node.
   * @return copy
   */
  public abstract ANode copy();

  /**
   * Returns the name of the node, composed of an optional prefix
   * and the local name.
   * This function must only be called for element and attribute nodes.
   * It is more efficient than calling {@link #qname}, as no {@link QNm}
   * instance is created.
   * @return name
   */
  public byte[] nname() {
    return null;
  }

  /**
   * Returns the QName of the node.
   * This function must only be called for elements, attributes and pi's.
   * @return name
   */
  public QNm qname() {
    return null;
  }

  /**
   * Updates the specified with the information of the current node.
   * This is more efficient than calling {@link #qname}, as an existing
   * {@link QNm} instance is reused.
   * This function must only be called for elements, attributes and pi's.
   * @param nm temporary qname
   * @return name
   */
  public abstract QNm update(final QNm nm);

  /**
   * Returns a namespace array.
   * @return namespace array
   */
  public Atts ns() {
    return null;
  }

  /**
   * Returns the namespace hierarchy.
   * @return namespaces
   * [LW][LK] Namespaces: this isn't enough
   */
  public final Atts nsScope() {
    return nsScope(true);
  }

  /**
   * Returns the namespace hierarchy.
   * @param nsInherit copy-namespaces inherit
   * @return namespaces
   * [LW][LK] Namespaces: this isn't enough
   */
  public final Atts nsScope(final boolean nsInherit) {
    final Atts ns = new Atts();
    ANode n = this;
    do {
      final Atts nns = n.ns();
      if(!nsInherit) return nns;
      if(nns != null) {
        for(int a = nns.size - 1; a >= 0; a--) {
          final byte[] key = nns.key[a];
          if(!ns.contains(key)) ns.add(key, nns.val[a]);
        }
      }
      n = n.parent();
    } while(n != null && n.type == NodeType.ELM);
    return ns;
  }

  /**
   * Recursively finds the uri for the specified prefix.
   * @param pref prefix
   * @param ctx query context
   * @return uri
   */
  public final byte[] uri(final byte[] pref, final QueryContext ctx) {
    final Atts at = ns();
    if(at != null) {
      final int i = at.get(pref);
      if(i != -1) return at.val[i];
      final ANode n = parent();
      if(n != null) return n.uri(pref, ctx);
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

  @Override
  public boolean isNode() {
    return true;
  }

  @Override
  public boolean isUntyped() {
    return true;
  }

  @Override
  public abstract void serialize(final Serializer ser) throws IOException;

  /**
   * Compares the identity of two nodes.
   * @param node node to be compared
   * @return result of check
   */
  public abstract boolean is(final ANode node);

  /**
   * Compares two nodes for their unique order.
   * @param node node to be compared
   * @return 0 if the nodes are equal or a positive/negative value
   * if the node appears after/before the argument
   */
  public abstract int diff(final ANode node);

  /**
   * Returns a final node representation. This method is called by the
   * step expressions, before it is passed on as result.
   * @return node
   */
  public ANode finish() {
    return this;
  }

  /**
   * Returns the parent node.
   * @return parent node
   */
  public abstract ANode parent();

  /**
   * Sets the parent node.
   * @param p parent node
   * @return self reference
   */
  public abstract ANode parent(final ANode p);

  /**
   * Returns true if the node has children.
   * @return result of test
   */
  public abstract boolean hasChildren();

  /**
   * Returns the value of the specified attribute, or {@code null}.
   * @param name attribute to be found
   * @return attribute value
   */
  public byte[] attribute(final QNm name) {
    final AxisIter ai = attributes();
    while(true) {
      final ANode node = ai.next();
      if(node == null) return null;
      if(node.qname().eq(name)) return node.string();
    }
  }

  /**
   * Returns an ancestor axis iterator.
   * @return iterator
   */
  public abstract AxisIter anc();

  /**
   * Returns an ancestor-or-self axis iterator.
   * @return iterator
   */
  public abstract AxisIter ancOrSelf();

  /**
   * Returns an attribute axis iterator.
   * @return iterator
   */
  public abstract AxisIter attributes();

  /**
   * Returns a child axis iterator.
   * @return iterator
   */
  public abstract AxisMoreIter children();

  /**
   * Returns a descendant axis iterator.
   * @return iterator
   */
  public abstract AxisIter descendant();

  /**
   * Returns a descendant-or-self axis iterator.
   * @return iterator
   */
  public abstract AxisIter descOrSelf();

  /**
   * Returns a following axis iterator.
   * @return iterator
   */
  public abstract AxisIter foll();

  /**
   * Returns a following-sibling axis iterator.
   * @return iterator
   */
  public abstract AxisIter follSibl();

  /**
   * Returns a parent axis iterator.
   * @return iterator
   */
  public abstract AxisIter par();

  /**
   * Returns a preceding axis iterator.
   * @return iterator
   */
  public final AxisIter prec() {
    return new AxisIter() {
      /** Iterator. */
      private NodeCache nc;

      @Override
      public ANode next() {
        if(nc == null) {
          nc = new NodeCache();
          ANode n = ANode.this;
          ANode p = n.parent();
          while(p != null) {
            if(n.type != NodeType.ATT) {
              final NodeCache tmp = new NodeCache();
              final AxisIter ai = p.children();
              for(ANode c; (c = ai.next()) != null && !c.is(n);) {
                tmp.add(c.finish());
                addDesc(c.children(), tmp);
              }
              for(long t = tmp.size() - 1; t >= 0; t--) nc.add(tmp.get(t));
            }
            n = p;
            p = p.parent();
          }
        }
        return nc.next();
      }
    };
  }

  /**
   * Returns a preceding-sibling axis iterator.
   * @return iterator
   */
  public final AxisIter precSibl() {
    return new AxisIter() {
      /** Child nodes. */
      private NodeCache nc;
      /** Counter. */
      private long c;

      @Override
      public ANode next() {
        if(nc == null) {
          final ANode r = parent();
          if(r == null) return null;

          nc = new NodeCache();
          final AxisIter ai = r.children();
          for(ANode n; (n = ai.next()) != null && !n.is(ANode.this);) {
            nc.add(n.finish());
          }
          c = nc.size();
        }
        return c > 0 ? nc.get(--c) : null;
      }
    };
  }

  /**
   * Returns an self axis iterator.
   * @return iterator
   */
  public final AxisMoreIter self() {
    return new AxisMoreIter() {
      /** First call. */
      private boolean more = true;

      @Override
      public boolean more() {
        return more;
      }
      @Override
      public ANode next() {
        return (more ^= true) ? null : ANode.this;
      }
    };
  }

  /**
   * Adds children of a sub node.
   * @param ch child nodes
   * @param nc node cache
   */
  protected final void addDesc(final AxisMoreIter ch, final NodeCache nc) {
    for(ANode n; (n = ch.next()) != null;) {
      nc.add(n.finish());
      addDesc(n.children(), nc);
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
      default : throw Util.notexpected();
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
  public final BXNode toJava() {
    switch(nodeType()) {
      case DOC: return new BXDoc(this);
      case ELM: return new BXElem(this);
      case TXT: return new BXText(this);
      case ATT: return new BXAttr(this);
      case COM: return new BXComm(this);
      case PI : return new BXPI(this);
      default : return null;
    }
  }

  /**
   * Returns this Node's node type.
   * @return node type
   */
  public final NodeType nodeType() {
    return (NodeType) type;
  }
}
