package org.basex.query.xquery.item;

import java.math.BigDecimal;
import org.basex.api.dom.BXAttr;
import org.basex.api.dom.BXComm;
import org.basex.api.dom.BXDoc;
import org.basex.api.dom.BXElem;
import org.basex.api.dom.BXNode;
import org.basex.api.dom.BXPI;
import org.basex.api.dom.BXText;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.iter.NodIter;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.iter.NodeMore;
import org.basex.util.Atts;
import org.basex.util.Token;

/**
 * Node Type.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Nod extends Item {
  /** Static node counter. */
  private static int idcounter;
  /** Parent node. */
  protected Nod par;
  /** Unique node id. */
  // [CG] XQuery/unique node id: might not be sufficient
  protected int id;

  /**
   * Constructor.
   * @param t data type
   */
  protected Nod(final Type t) {
    super(t);
    id = idcounter++;
  }

  @Override
  public final boolean bool() {
    return true;
  }

  @Override
  public final long itr() throws XQException {
    return Itr.parse(str());
  }

  @Override
  public final float flt() throws XQException {
    return Flt.parse(str());
  }

  @Override
  public BigDecimal dec() throws XQException {
    return Dec.parse(str());
  }

  @Override
  public final double dbl() throws XQException {
    return Dbl.parse(str());
  }

  @Override
  public final boolean eq(final Item i) throws XQException {
    return i.type == Type.BLN || i.n() ? i.eq(this) : Token.eq(str(), i.str());
  }

  @Override
  public final int diff(final Item i) throws XQException {
    return i.n() ? -i.diff(this) : Token.diff(str(), i.str());
  }

  /**
   * Creates a new copy (clone) of the node.
   * @return new copy
   */
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
   * Returns a temporary node name.
   * @param nm temporary qname
   * @return name
   * @throws XQException query exception
   */
  @SuppressWarnings("unused")
  public QNm qname(final QNm nm) throws XQException {
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
  public boolean is(final Nod nod) {
    return id == nod.id;
  }

  /**
   * Compares two nodes for their unique order.
   * @param nod node to be compared
   * @return result of check
   */
  public int diff(final Nod nod) {
    return id - nod.id;
  }

  /**
   * Returns a final node representation.
   * @return node
   */
  public Nod finish() {
    return this;
  }

  /**
   * Returns the parent node.
   * @return parent node
   */
  public Nod parent() {
    return par;
  }

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
  public final NodeIter anc() {
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
  public abstract NodeIter desc();

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
      private NodIter it;
      /** First call. */
      private boolean more;

      @Override
      public Nod next() throws XQException {
        if(!more) {
          it = new NodIter();
          Nod n = Nod.this;
          Nod p = n.parent();
          while(p != null) {
            final NodeIter i = p.child();
            Nod c;
            while((c = i.next()) != null && !c.is(n))
              ;
            while((c = i.next()) != null) {
              it.add(c.finish());
              addDesc(c.child(), it);
            }
            n = p;
            p = p.parent();
          }
          more = true;
        }

        return it.next();
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
      private NodeIter it;
      /** First call. */
      private boolean more;

      @Override
      public Nod next() throws XQException {
        if(!more) {
          final Nod r = parent();
          if(r == null) {
            it = NodeIter.NONE;
          } else {
            it = r.child();
            Nod n;
            while((n = it.next()) != null && !n.is(Nod.this))
              ;
          }
          more = true;
        }
        return it.next();
      }
    };
  }

  /**
   * Returns a parent axis iterator.
   * @return iterator
   */
  public NodeIter par() {
    return new NodeIter() {
      /** First call. */
      private boolean more;

      @Override
      public Nod next() {
        more ^= true;
        return more ? parent() : null;
      }
    };
  }

  /**
   * Returns a preceding axis iterator.
   * @return iterator
   */
  public final NodeIter prec() {
    return new NodeIter() {
      /** Iterator. */
      private NodIter it;
      /** First call. */
      private boolean more;

      @Override
      public Nod next() throws XQException {
        if(!more) {
          it = new NodIter();
          Nod n = Nod.this;
          Nod p = n.parent();
          while(p != null) {
            final NodIter tmp = new NodIter();
            final NodeIter i = p.child();
            Nod c;
            while((c = i.next()) != null && !c.is(n)) {
              tmp.add(c.finish());
              addDesc(c.child(), tmp);
            }
            for(int t = tmp.size - 1; t >= 0; t--)
              it.add(tmp.list[t]);
            n = p;
            p = p.parent();
          }
          more = true;
        }
        return it.next();
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
      private NodIter ch;
      /** Counter. */
      private int c;
      /** First call. */
      private boolean more;

      @Override
      public Nod next() throws XQException {
        if(!more) {
          final Nod r = parent();
          if(r == null) return null;

          ch = new NodIter();
          final NodeIter iter = r.child();
          Nod n;
          while((n = iter.next()) != null) {
            if(n.is(Nod.this)) break;
            ch.add(n.finish());
          }
          c = ch.size;
          more = true;
        }
        return c > 0 ? ch.list[--c] : null;
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
      private boolean first = true;

      @Override
      public boolean more() {
        return first;
      }
      @Override
      public Nod next() {
        final boolean f = first;
        first = false; 
        return f ? Nod.this : null;
      }
    };
  }

  /**
   * Adds children of a sub node.
   * @param children child nodes
   * @param nodes node builder
   * @throws XQException query exception
   */
  protected final void addDesc(final NodeIter children, final NodIter nodes)
      throws XQException {
    Nod ch;
    while((ch = children.next()) != null) {
      nodes.add(ch.finish());
      addDesc(ch.child(), nodes);
    }
  }

  @Override
  public final BXNode java() {
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
}
