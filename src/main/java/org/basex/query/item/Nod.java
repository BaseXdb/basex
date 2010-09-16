package org.basex.query.item;

import org.basex.api.dom.BXAttr;
import org.basex.api.dom.BXComm;
import org.basex.api.dom.BXDoc;
import org.basex.api.dom.BXElem;
import org.basex.api.dom.BXNode;
import org.basex.api.dom.BXPI;
import org.basex.api.dom.BXText;
import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.query.iter.NodeMore;
import org.basex.util.Atts;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Node type.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Nod extends Item {
  /** Node Types. */
  private static final Type[] TYPES = {
    Type.DOC, Type.ELM, Type.TXT, Type.ATT, Type.COM, Type.PI
  };
  /** Static node counter. */
  // [CG] XQuery/ID: move to query context?
  private static int sid;

  /** Unique node id. */
  protected final int id = ++sid;
  /** Text value. */
  protected byte[] val;
  /** Parent node. */
  protected Nod par;

  /**
   * Constructor.
   * @param t data type
   */
  protected Nod(final Type t) {
    super(t);
  }

  @Override
  public final boolean bool(final InputInfo ii) {
    return true;
  }

  @Override
  public byte[] atom() {
    return val;
  }

  @Override
  public final boolean eq(final InputInfo ii, final Item it)
      throws QueryException {
    return !it.unt() ? it.eq(ii, this) : Token.eq(atom(), it.atom());
  }

  @Override
  public final int diff(final InputInfo ii, final Item it)
      throws QueryException {
    return !it.unt() ? -it.diff(ii, this) : Token.diff(atom(), it.atom());
  }

  @Override
  public abstract Nod copy();

  /**
   * Returns the node name.
   * @return name
   */
  public byte[] nname() {
    return null;
  }

  /**
   * Returns the node name.
   * @return name
   */
  public QNm qname() {
    return null;
  }

  /**
   * Returns the node id.
   * @return id
   */
  public final int id() {
    return id;
  }

  /**
   * Returns a temporary node name.
   * @param nm temporary qname
   * @return name
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public QNm qname(final QNm nm) throws QueryException {
    return qname();
  }

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
   */
  public final Atts nsScope() {
    final Atts ns = new Atts();
    Nod n = this;
    do {
      final Atts nns = n.ns();
      if(nns != null) {
        for(int a = nns.size - 1; a >= 0; a--) {
          final byte[] key = nns.key[a];
          if(!ns.contains(key)) ns.add(key, nns.val[a]);
        }
      }
      n = n.parent();
    } while(n != null && n.type == Type.ELM);
    return ns;
  }

  /**
   * Returns the uri for the specified prefix.
   * @param pref prefix
   * @param ctx query context
   * @return uri
   */
  public final byte[] uri(final byte[] pref, final QueryContext ctx) {
    final Atts at = ns();
    if(at != null) {
      final int i = at.get(pref);
      if(i != -1) return at.val[i];
      final Nod n = parent();
      if(n != null) return n.uri(pref, ctx);
    }
    return pref.length == 0 ? Token.EMPTY : null;
  }

  /**
   * Returns the database name.
   * @return database name
   */
  public byte[] base() {
    return Token.EMPTY;
  }

  /**
   * Compares two nodes for equality.
   * @param nod node to be compared
   * @return result of check
   */
  public abstract boolean is(final Nod nod);

  /**
   * Compares two nodes for their unique order.
   * @param nod node to be compared
   * @return 0 if the nodes are equal or a positive/negative value
   * if the node appears after/before the argument
   */
  public abstract int diff(final Nod nod);

  /**
   * Returns a final node representation. This method should be called as
   * soon as a node is passed on as result node.
   * @return node
   */
  public Nod finish() {
    return this;
  }

  /**
   * Returns the parent node.
   * @return parent node
   */
  public abstract Nod parent();

  /**
   * Sets the parent node.
   * @param p parent node
   */
  public void parent(final Nod p) {
    par = p;
  }

  /**
   * Returns an ancestor axis iterator.
   * @return iterator
   */
  public NodeIter anc() {
    return new NodeIter() {
      /** Temporary node. */
      private Nod node = Nod.this;

      @Override
      public Nod next() {
        node = node.parent();
        return node;
      }
    };
  }

  /**
   * Returns an ancestor-or-self axis iterator.
   * @return iterator
   */
  public final NodeIter ancOrSelf() {
    return new NodeIter() {
      /** Temporary node. */
      private Nod node = Nod.this;

      @Override
      public Nod next() {
        if(node == null) return null;
        final Nod n = node;
        node = n.parent();
        return n;
      }
    };
  }

  /**
   * Returns an attribute axis iterator.
   * @return iterator
   */
  public abstract NodeIter attr();

  /**
   * Returns a child axis iterator.
   * @return iterator
   */
  public abstract NodeMore child();

  /**
   * Returns a descendant axis iterator.
   * @return iterator
   */
  public abstract NodeIter descendant();

  /**
   * Returns a descendant-or-self axis iterator.
   * @return iterator
   */
  public abstract NodeIter descOrSelf();

  /**
   * Returns a following axis iterator.
   * @return iterator
   */
  public final NodeIter foll() {
    return new NodeIter() {
      /** Iterator. */
      private NodIter ir;

      @Override
      public Nod next() throws QueryException {
        if(ir == null) {
          ir = new NodIter();
          Nod n = Nod.this;
          Nod p = n.parent();
          while(p != null) {
            final NodeIter i = p.child();
            Nod c;
            while(n.type != Type.ATT && (c = i.next()) != null && !c.is(n));
            while((c = i.next()) != null) {
              ir.add(c.finish());
              addDesc(c.child(), ir);
            }
            n = p;
            p = p.parent();
          }
        }
        return ir.next();
      }
    };
  }

  /**
   * Returns a following-sibling axis iterator.
   * @return iterator
   */
  public final NodeIter follSibl() {
    return new NodeIter() {
      /** Iterator. */
      private NodeIter ir;

      @Override
      public Nod next() throws QueryException {
        if(ir == null) {
          final Nod r = parent();
          if(r == null) return null;
          ir = r.child();
          Nod n;
          while((n = ir.next()) != null && !n.is(Nod.this));
        }
        return ir.next();
      }
    };
  }

  /**
   * Returns a parent axis iterator.
   * @return iterator
   */
  public abstract NodeIter par();

  /**
   * Returns a preceding axis iterator.
   * @return iterator
   */
  public final NodeIter prec() {
    return new NodeIter() {
      /** Iterator. */
      private NodIter ir;

      @Override
      public Nod next() throws QueryException {
        if(ir == null) {
          ir = new NodIter();
          Nod n = Nod.this;
          Nod p = n.parent();
          while(p != null) {
            if(n.type != Type.ATT) {
              final NodIter tmp = new NodIter();
              final NodeIter i = p.child();
              Nod c;
              while((c = i.next()) != null && !c.is(n)) {
                tmp.add(c.finish());
                addDesc(c.child(), tmp);
              }
              for(long t = tmp.size() - 1; t >= 0; t--) ir.add(tmp.get(t));
            }
            n = p;
            p = p.parent();
          }
        }
        return ir.next();
      }
    };
  }

  /**
   * Returns a preceding-sibling axis iterator.
   * @return iterator
   */
  public final NodeIter precSibl() {
    return new NodeIter() {
      /** Children nodes. */
      private NodIter ir;
      /** Counter. */
      private long c;

      @Override
      public Nod next() throws QueryException {
        if(ir == null) {
          final Nod r = parent();
          if(r == null) return null;

          ir = new NodIter();
          final NodeIter iter = r.child();
          Nod n;
          while((n = iter.next()) != null) {
            if(n.is(Nod.this)) break;
            ir.add(n.finish());
          }
          c = ir.size();
        }
        return c > 0 ? ir.get(--c) : null;
      }
    };
  }

  /**
   * Returns an self axis iterator.
   * @return iterator
   */
  public final NodeMore self() {
    return new NodeMore() {
      /** First call. */
      private boolean more = true;

      @Override
      public boolean more() {
        return more;
      }
      @Override
      public Nod next() {
        return (more ^= true) ? null : Nod.this;
      }
    };
  }

  /**
   * Adds children of a sub node.
   * @param children child nodes
   * @param nodes node builder
   * @throws QueryException query exception
   */
  protected final void addDesc(final NodeIter children, final NodIter nodes)
      throws QueryException {
    Nod ch;
    while((ch = children.next()) != null) {
      nodes.add(ch.finish());
      addDesc(ch.child(), nodes);
    }
  }

  /**
   * Returns a database kind for the specified node type.
   * @param t node type
   * @return node kind
   */
  public static int kind(final Type t) {
    switch(t) {
      case DOC: return Data.DOC;
      case ELM: return Data.ELEM;
      case TXT: return Data.TEXT;
      case ATT: return Data.ATTR;
      case COM: return Data.COMM;
      case PI : return Data.PI;
      default : Util.notexpected(); return -1;
    }
  }

  /**
   * Returns a node type for the specified database kind.
   * @param k database kind
   * @return node type
   */
  public static Type type(final int k) {
    return TYPES[k];
  }

  @Override
  public final BXNode toJava() {
    switch(type) {
      case DOC: return new BXDoc(this);
      case ELM: return new BXElem(this);
      case TXT: return new BXText(this);
      case ATT: return new BXAttr(this);
      case COM: return new BXComm(this);
      case PI : return new BXPI(this);
      default : return null;
    }
  }

  @Override
  public final SeqType type() {
    return SeqType.NOD;
  }
}
