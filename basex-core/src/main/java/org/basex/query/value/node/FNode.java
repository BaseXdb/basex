package org.basex.query.value.node;

import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Main memory based node fragments.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class FNode extends ANode {
  /**
   * Constructor.
   * @param type data type
   */
  FNode(final NodeType type) {
    super(type);
  }

  @Override
  public byte[] string() {
    if(value == null) value = Token.EMPTY;
    return value;
  }

  @Override
  public final QNm qname(final QNm nm) {
    return qname();
  }

  @Override
  public final ANode deepCopy() {
    return copy();
  }

  @Override
  public final boolean is(final ANode node) {
    return id == node.id;
  }

  @Override
  public final int diff(final ANode node) {
    // compare fragment with database node
    if(node instanceof DBNode) return diff(this, node);
    // compare fragments. subtraction is used instead of comparison to support overflow of node id
    final int i = id - node.id;
    return i > 0 ? 1 : i < 0 ? -1 : 0;
  }

  @Override
  public final ANode parent() {
    return par;
  }

  @Override
  public final AxisIter ancestor() {
    return new AxisIter() {
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
  static AxisMoreIter iter(final ANodeList iter) {
    return new AxisMoreIter() {
      /** Child counter. */ int c;
      @Override
      public boolean more() { return iter != null && c != iter.size(); }
      @Override
      public ANode next() { return more() ? iter.get(c++) : null; }
      @Override
      public ANode get(final long i) { return iter.get((int) i); }
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
  final byte[] string(final ANodeList iter) {
    if(value == null) {
      final TokenBuilder tb = new TokenBuilder();
      for(final ANode nc : iter) {
        if(nc.type == NodeType.ELM || nc.type == NodeType.TXT) tb.add(nc.string());
      }
      value = tb.finish();
    }
    return value;
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
            if(l + 1 == nm.length) nm = Array.copy(nm, new AxisMoreIter[l + 1 << 1]);
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
      private NodeSeqBuilder nc;

      @Override
      public ANode next() {
        if(nc == null) {
          nc = new NodeSeqBuilder();
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
