package org.basex.query.xquery.item;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.iter.NodIter;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.iter.NodeMore;
import org.basex.query.xquery.iter.NodeNext;

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
  public final NodeIter anc() {
    return new NodeIter() {
      /** Temporary node. */
      Node node = FNode.this;

      @Override
      public Node next() {
        node = node.par;
        return node;
      }
    };
  }

  @Override
  public final NodeIter ancOrSelf() {
    return new NodeIter() {
      /** Temporary node. */
      Node node = FNode.this;

      @Override
      public Node next() {
        if(node == null) return null;
        final Node n = node;
        node = node.par;
        return n;
      }
    };
  }

  @Override
  public final NodeIter attr() {
    return new NodeIter() {
      /** Child counter. */
      int c;

      @Override
      public Node next() {
        return FNode.this.atts != null && c != FNode.this.atts.size ?
            FNode.this.atts.list[c++] : null;
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
        return FNode.this.children != null && c != FNode.this.children.size;
      }

      @Override
      public Node next() {
        return more() ? FNode.this.children.list[c++] : null;
        /*while(more()) {
          Node node = FNode.this.children.list[c++];
          if(node.type == Type.DOC) {
            final Iter iter = node.child();
            if(iter.next() != null) nodes.add(node.copy());
            node = node.child().list[0];
          } else {
            return node;
          }
        }
        return null;
        */
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
          it[0] = FNode.this.child();
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
      private NodeNext it;
      /** First call. */
      private boolean more;

      @Override
      public Node next() throws XQException {
        if(!more) {
          final NodIter ch = new NodIter();
          ch.add(FNode.this);
          addDesc(FNode.this.child(), ch);
          it = new NodeNext(ch);
          more = true;
        }
        return it.next();
      }
    };
  }

  @Override
  public final NodeIter foll() {
    return new NodeIter() {
      /** Iterator. */
      private NodeNext it;
      /** First call. */
      private boolean more;

      @Override
      public Node next() throws XQException {
        if(!more) {
          final NodIter ch = new NodIter();
          Node nod = FNode.this;
          while(nod.par != null) {
            final NodeIter i = nod.par.child();
            Node n;
            while((n = i.next()) != null && !n.is(nod));
            while((n = i.next()) != null) {
              ch.add(n.finish());
              addDesc(n.child(), ch);
            }
            nod = nod.par;
          }
          it = new NodeNext(ch);
          more = true;
        }
        return it.next();
      }
    };
  }

  @Override
  public final NodeIter follSibl() {
    return new NodeIter() {
      /** Iterator. */
      private NodeIter it;
      /** First call. */
      private boolean more;

      @Override
      public Node next() throws XQException {
        if(!more) {
          final Node r = FNode.this.par;
          if(r == null) {
            it = NodeIter.NONE;
          } else {
            it = r.child();
            Node n;
            while((n = it.next()) != null && !n.is(FNode.this));
          }
          more = true;
        }
        return it.next();
      }
    };
  }

  @Override
  public final NodeIter par() {
    return new NodeIter() {
      /** Flag. */
      public boolean first;

      @Override
      public Node next() {
        first ^= true;
        return first ? FNode.this.par : null;
      }
    };
  }

  @Override
  public final NodeIter prec() {
    return new NodeIter() {
      /** Iterator. */
      private NodeIter it;
      /** First call. */
      private boolean more;

      @Override
      public Node next() throws XQException {
        if(!more) {
          final NodIter ch = new NodIter();
          Node nod = FNode.this;
          while(nod.par != null) {
            final NodIter tmp = new NodIter();
            final NodeIter itr = nod.par.child();
            Node n;
            while((n = itr.next()) != null) {
              if(!n.is(nod)) tmp.add(n.finish());
              else break;
            }
            int i = tmp.size;
            while(--i >= 0) {
              ch.add(tmp.list[i]);
              addDesc(tmp.list[i].child(), ch);
            }
            nod = nod.par;
          }
          it = new NodeNext(ch);
          more = true;
        }
        return it.next();
      }
    };
  }

  @Override
  public final NodeIter precSibl() {
    return new NodeIter() {
      /** Children nodes. */
      NodIter ch;
      /** Counter. */
      int c;
      /** First call. */
      private boolean more;

      @Override
      public Node next() throws XQException {
        if(!more) {
          final Node r = FNode.this.par;
          if(r == null) return null;

          ch = new NodIter();
          final NodeIter iter = r.child();
          Node n;
          while((n = iter.next()) != null) {
            if(!n.is(FNode.this)) ch.add(n.finish());
            else break;
          }
          c = ch.size;
          more = true;
        }
        return c > 0 ? ch.list[--c] : null;
      }
    };
  }

  @Override
  public final NodeIter self() {
    return new NodeIter() {
      /** First call. */
      private boolean first;
      
      @Override
      public Node next() {
        first ^= true;
        return first ? FNode.this : null;
      }
    };
  }
}
