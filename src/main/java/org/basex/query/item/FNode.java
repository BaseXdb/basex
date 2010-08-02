package org.basex.query.item;

import java.util.Arrays;
import org.basex.query.QueryException;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.query.iter.NodeMore;
import org.basex.util.TokenBuilder;

/**
 * Node type.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
  public final byte[] atom() {
    if(val == null) {
      final TokenBuilder tb = new TokenBuilder();
      for(int c = 0; c < children.size(); c++) {
        final Nod nc = children.get(c);
        if(nc.type == Type.ELM || nc.type == Type.TXT) tb.add(nc.atom());
      }
      val = tb.finish();
    }
    return val;
  }

  @Override
  public final boolean is(final Nod nod) {
    return id == nod.id;
  }

  @Override
  public final int diff(final Nod nod) {
    if(id != nod.id) {
      Nod n = this;
      while(n != null) {
        final Nod p = n.parent();
        if(p == nod) return 1;
        n = p;
      }
      n = nod;
      while(n != null) {
        final Nod p = n.parent();
        if(p == this) return -1;
        n = p;
      }
    }
    return id - nod.id;
  }

  @Override
  public final Nod parent() {
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
        return iter != null && c != iter.size();
      }

      @Override
      public Nod next() {
        return more() ? iter.get(c++) : null;
      }
    };
  }

  @Override
  public final NodeIter descendant() {
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
      private NodeMore[] nm = new NodeMore[1];
      /** Iterator Level. */
      private int l;

      @Override
      public Nod next() throws QueryException {
        if(nm[0] == null) nm[0] = self ? self() : child();
        if(l < 0) return null;

        final Nod node = nm[l].next();
        if(node != null) {
          final NodeMore ch = node.child();
          if(ch.more()) {
            if(l + 1 == nm.length) nm = Arrays.copyOf(nm, l + 1 << 1);
            nm[++l] = ch;
          } else {
            while(!nm[l].more()) if(l-- <= 0) break;
          }
        }
        return node;
      }
    };
  }

  @Override
  public final NodeIter par() {
    return new NodeIter() {
      /** First call. */
      private boolean more;

      @Override
      public Nod next() {
        return (more ^= true) ? par : null;
      }
    };
  }
}
