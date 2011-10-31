package org.basex.query.item;

import java.util.Arrays;
import org.basex.query.iter.NodeCache;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.AxisMoreIter;
import org.basex.util.TokenBuilder;

/**
 * Node type.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class FNode extends ANode {
  /** Child nodes. */
  protected NodeCache children;
  /** Attributes. */
  protected NodeCache atts;

  /**
   * Constructor.
   * @param t data type
   */
  protected FNode(final NodeType t) {
    super(t);
  }

  /**
   * Adds a node to this node.
   * @param node node to be added
   * @return self reference
   */
  public FNode add(final ANode node) {
    (node.type == NodeType.ATT ? atts : children).add(node);
    node.parent(this);
    return this;
  }

  @Override
  public final byte[] atom() {
    if(val == null) {
      final TokenBuilder tb = new TokenBuilder();
      for(int c = 0; c < children.size(); ++c) {
        final ANode nc = children.get(c);
        if(nc.type == NodeType.ELM || nc.type == NodeType.TXT)
          tb.add(nc.atom());
      }
      val = tb.finish();
    }
    return val;
  }

  @Override
  public final boolean is(final ANode node) {
    return id == node.id;
  }

  @Override
  public final int diff(final ANode node) {
    if(id != node.id) {
      ANode n = this;
      while(n != null) {
        final ANode p = n.parent();
        if(p == node) return 1;
        n = p;
      }
      n = node;
      while(n != null) {
        final ANode p = n.parent();
        if(p == this) return -1;
        n = p;
      }
    }
    return id - node.id;
  }

  @Override
  public final ANode parent() {
    return par;
  }

  @Override
  public final AxisIter anc() {
    return new AxisIter() {
      /** Temporary node. */
      private ANode node = FNode.this;

      @Override
      public ANode next() {
        node = node.parent();
        return node;
      }
    };
  }

  @Override
  public final AxisIter ancOrSelf() {
    return new AxisIter() {
      /** Temporary node. */
      private ANode node = FNode.this;

      @Override
      public ANode next() {
        if(node == null) return null;
        final ANode n = node;
        node = n.parent();
        return n;
      }
    };
  }

  @Override
  public final AxisIter attributes() {
    return iter(atts);
  }

  @Override
  public final AxisMoreIter children() {
    return iter(children);
  }

  @Override
  public FNode parent(final ANode p) {
    par = p;
    return this;
  }

  @Override
  public final boolean hasChildren() {
    return children.size() != 0;
  }

  /**
   * Iterates all nodes of the specified iterator.
   * @param iter iterator
   * @return node iterator
   */
  private AxisMoreIter iter(final NodeCache iter) {
    return new AxisMoreIter() {
      /** Child counter. */
      int c;

      @Override
      public boolean more() {
        return iter != null && c != iter.size();
      }

      @Override
      public ANode next() {
        return more() ? iter.get(c++) : null;
      }
    };
  }

  @Override
  public final AxisIter descendant() {
    return desc(false);
  }

  @Override
  public final AxisIter descOrSelf() {
    return desc(true);
  }

  /**
   * Returns an iterator for all descendant nodes.
   * @param self include self node
   * @return node iterator
   */
  private AxisIter desc(final boolean self) {
    return new AxisIter() {
      /** Iterator. */
      private AxisMoreIter[] nm = new AxisMoreIter[1];
      /** Iterator Level. */
      private int l;

      @Override
      public ANode next() {
        if(nm[0] == null) nm[0] = self ? self() : children();
        if(l < 0) return null;

        final ANode node = nm[l].next();
        if(node != null) {
          final AxisMoreIter ch = node.children();
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
  public final AxisIter par() {
    return new AxisIter() {
      /** First call. */
      private boolean more;

      @Override
      public ANode next() {
        return (more ^= true) ? par : null;
      }
    };
  }

  @Override
  public final AxisIter follSibl() {
    return new AxisIter() {
      /** Iterator. */
      private AxisIter ai;

      @Override
      public ANode next() {
        if(ai == null) {
          final ANode r = parent();
          if(r == null) return null;
          ai = r.children();
          for(ANode n; (n = ai.next()) != null && !n.is(FNode.this););
        }
        return ai.next();
      }
    };
  }

  @Override
  public final AxisIter foll() {
    return new AxisIter() {
      /** Iterator. */
      private NodeCache nc;

      @Override
      public ANode next() {
        if(nc == null) {
          nc = new NodeCache();
          ANode n = FNode.this;
          ANode p = n.parent();
          while(p != null) {
            final AxisIter i = p.children();
            for(ANode c; n.type != NodeType.ATT &&
              (c = i.next()) != null && !c.is(n););
            for(ANode c; (c = i.next()) != null;) {
              nc.add(c.finish());
              addDesc(c.children(), nc);
            }
            n = p;
            p = p.parent();
          }
        }
        return nc.next();
      }
    };
  }
}
