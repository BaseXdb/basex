package org.basex.query.xquery.item;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.iter.NodIter;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.iter.NodeMore;

/**
 * Node Type.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class FNode extends Node {
  /** Child nodes. */
  NodIter children;
  /** Attributes. */
  NodIter atts;

  /**
   * Constructor.
   * @param t data type
   */
  protected FNode(final Type t) {
    super(t);
  }
  
  @Override
  public final NodeIter attr() {
    return new NodeIter() {
      /** Child counter. */
      int c;

      @Override
      public Node next() {
        return atts != null && c != atts.size ? atts.list[c++] : null;
      }
    };
  }

  @Override
  public final NodeMore child() {
    return new NodeMore() {
      /** Child counter. */
      int c;

      @Override
      public boolean more() {
        return children != null && c != children.size;
      }

      @Override
      public Node next() {
        return more() ? children.list[c++] : null;
      }
    };
  }

  @Override
  public final NodeIter desc() {
    return new NodeIter() {
      /** Iterator. */
      private NodeMore[] it = new NodeMore[256];
      /** Iterator Level. */
      private int l;
      /** First call. */
      private boolean more;

      @Override
      public Node next() throws XQException {
        if(!more) {
          it[0] = child();
          more = true;
        }
        if(l < 0) return null;

        final Node node = it[l].next();
        if(node == null) return null;
        final NodeMore ch = node.child();
        if(ch.more()) {
          it[++l] = ch;
        } else {
          while(!it[l].more()) if(l-- <= 0) return node;
        }
        return node;
      }
    };
  }

  @Override
  public final NodeIter descOrSelf() {
    return new NodeIter() {
      /** Iterator. */
      private NodIter it;
      /** First call. */
      private boolean more;

      @Override
      public Node next() throws XQException {
        if(!more) {
          it = new NodIter();
          it.add(FNode.this);
          addDesc(child(), it);
          more = true;
        }
        return it.next();
      }
    };
  }
}
