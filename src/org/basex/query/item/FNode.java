package org.basex.query.item;

import org.basex.query.QueryException;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.query.iter.NodeMore;
import org.basex.util.Array;

/**
 * Node Type.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class FNode extends Nod {
  /** Child nodes. */
  protected NodIter children;
  /** Attributes. */
  protected NodIter atts;

  /**
   * Constructor.
   * @param t data type
   */
  protected FNode(final Type t) {
    super(t);
  }

  @Override
  public boolean is(final Nod nod) {
    return id == nod.id;
  }

  @Override
  public final int diff(final Nod nod) {
    // [CG] XQuery/ID: check all cases
    
    if(id != nod.id) {
      Nod n = this;
      while(n != null) {
        Nod p = n.parent();
        if(p == nod) return 1;
        n = p;
      }
      n = nod;
      while(n != null) {
        Nod p = n.parent();
        if(p == this) return -1;
        n = p;
      }
    }
    return id - nod.id;
  }

  @Override
  public Nod parent() {
    return par;
  }
  
  @Override
  public final NodeIter attr() {
    return iter(atts);
  }

  @Override
  public final NodeMore child() {
    return iter(children);
  }
  
  /**
   * Iterates all nodes of the specified iterator.
   * @param iter iterator
   * @return node iterator
   */
  private NodeMore iter(final NodIter iter) {
    return new NodeMore() {
      /** Child counter. */
      int c;

      @Override
      public boolean more() {
        return iter != null && c != iter.size;
      }

      @Override
      public Nod next() {
        return more() ? iter.list[c++] : null;
      }
    };
  }

  @Override
  public final NodeIter desc() {
    return desc(false);
  }

  @Override
  public final NodeIter descOrSelf() {
    return desc(true);
  }

  /**
   * Returns an iterator for all descendant nodes.
   * @param self include self node
   * @return node iterator
   */
  private NodeIter desc(final boolean self) {
    return new NodeIter() {
      /** Iterator. */
      private NodeMore[] it = new NodeMore[1];
      /** Iterator Level. */
      private int l;
      /** First call. */
      private boolean more;

      @Override
      public Nod next() throws QueryException {
        if(!more) {
          it[0] = self ? self() : child();
          more = true;
        }
        if(l < 0) return null;

        final Nod node = it[l].next();
        if(node != null) {
          final NodeMore ch = node.child();
          if(ch.more()) {
            if(l + 1 == it.length) it = Array.extend(it);
            it[++l] = ch;
          } else {
            while(!it[l].more()) if(l-- <= 0) break;
          }
        }
        return node;
      }
    };
  }
}
