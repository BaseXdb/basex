package org.basex.query.item;

import java.util.Arrays;

import org.basex.query.QueryContext;
import org.basex.query.iter.NodeCache;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.AxisMoreIter;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Node type.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class FNode extends ANode {
  /**
   * Constructor.
   * @param t data type
   */
  protected FNode(final NodeType t) {
    super(t);
  }

  @Override
  public byte[] string() {
    if(val == null) val = Token.EMPTY;
    return val;
  }

  @Override
  public QNm update(final QNm nm) {
    return qname();
  }

  @Override
  public ANode copy(final QueryContext ctx) {
    return copy();
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
  public final AxisIter ancestor() {
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
  public final AxisIter ancestorOrSelf() {
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
  public AxisMoreIter attributes() {
    return AxisMoreIter.EMPTY;
  }

  @Override
  public AxisMoreIter children() {
    return AxisMoreIter.EMPTY;
  }

  @Override
  public final FNode parent(final ANode p) {
    par = p;
    return this;
  }

  @Override
  public boolean hasChildren() {
    return false;
  }

  @Override
  public final AxisIter descendant() {
    return desc(false);
  }

  @Override
  public final AxisIter descendantOrSelf() {
    return desc(true);
  }

  /**
   * Iterates all nodes of the specified iterator.
   * @param iter iterator
   * @return node iterator
   */
  protected final AxisMoreIter iter(final NodeCache iter) {
    return new AxisMoreIter() {
      /** Child counter. */ int c;
      @Override
      public boolean more() { return iter != null && c != iter.size(); }
      @Override
      public ANode next() { return more() ? iter.get(c++) : null; }
      @Override
      public ANode get(final long i) { return iter.get(i); }
      @Override
      public long size() { return iter.size(); }
      @Override
      public boolean reset() { c = 0; return true; }
      @Override
      public Value value() { return iter.value(); }
    };
  }

  /**
   * Returns the string value for the specified nodes.
   * @param iter iterator
   * @return node iterator
   */
  protected final byte[] string(final NodeCache iter) {
    if(val == null) {
      final TokenBuilder tb = new TokenBuilder();
      for(int c = 0; c < iter.size(); ++c) {
        final ANode nc = iter.get(c);
        if(nc.type == NodeType.ELM || nc.type == NodeType.TXT)
          tb.add(nc.string());
      }
      val = tb.finish();
    }
    return val;
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
  public final AxisIter parentIter() {
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
  public final AxisIter followingSibling() {
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
  public final AxisIter following() {
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
