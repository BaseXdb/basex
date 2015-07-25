package org.basex.query.value.node;

import java.util.*;

import org.basex.core.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Main-memory node fragment.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class FNode extends ANode {
  /**
   * Constructor.
   * @param type item type
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
  public final ANode deepCopy(final MainOptions options) {
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
    return parent;
  }

  @Override
  public final BasicNodeIter ancestor() {
    return new BasicNodeIter() {
      private ANode node = FNode.this;

      @Override
      public ANode next() {
        node = node.parent();
        return node;
      }
    };
  }

  @Override
  public final BasicNodeIter ancestorOrSelf() {
    return new BasicNodeIter() {
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
  public BasicNodeIter attributes() {
    return BasicNodeIter.EMPTY;
  }

  @Override
  public BasicNodeIter children() {
    return BasicNodeIter.EMPTY;
  }

  @Override
  public final FNode parent(final ANode par) {
    parent = par;
    return this;
  }

  @Override
  public boolean hasChildren() {
    return false;
  }

  @Override
  public final BasicNodeIter descendant() {
    return desc(false);
  }

  @Override
  public final BasicNodeIter descendantOrSelf() {
    return desc(true);
  }

  /**
   * Iterates all nodes of the specified iterator.
   * @param iter iterator
   * @return node iterator
   */
  static BasicNodeIter iter(final ANodeList iter) {
    return new BasicNodeIter() {
      int c;
      @Override
      public ANode next() { return iter != null && c != iter.size() ? iter.get(c++) : null; }
      @Override
      public ANode get(final long i) { return iter.get((int) i); }
      @Override
      public long size() { return iter.size(); }
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
  private BasicNodeIter desc(final boolean self) {
    return new BasicNodeIter() {
      private final Stack<BasicNodeIter> iters = new Stack<>();
      private ANode last;

      @Override
      public ANode next() {
        final BasicNodeIter iter = last != null ? last.children() : self ? self() : children();
        last = iter.next();
        if(last == null) {
          while(!iters.isEmpty()) {
            last = iters.peek().next();
            if(last != null) break;
            iters.pop();
          }
        } else {
          iters.add(iter);
        }
        return last;
      }
    };
  }

  @Override
  public final BasicNodeIter parentIter() {
    return new BasicNodeIter() {
      private boolean all;

      @Override
      public ANode next() {
        if(all) return null;
        all = true;
        return parent;
      }
    };
  }

  @Override
  public final BasicNodeIter followingSibling() {
    return new BasicNodeIter() {
      private BasicNodeIter iter;

      @Override
      public ANode next() {
        if(iter == null) {
          final ANode r = parent();
          if(r == null) return null;
          iter = r.children();
          for(ANode n; (n = iter.next()) != null && !n.is(FNode.this););
        }
        return iter.next();
      }
    };
  }

  @Override
  public final BasicNodeIter following() {
    return new BasicNodeIter() {
      private BasicNodeIter iter;

      @Override
      public ANode next() {
        if(iter == null) {
          final ANodeList list = new ANodeList();
          ANode node = FNode.this, par = node.parent();
          while(par != null) {
            final BasicNodeIter i = par.children();
            for(ANode n; node.type != NodeType.ATT && (n = i.next()) != null && !n.is(node););
            for(ANode n; (n = i.next()) != null;) {
              list.add(n.finish());
              addDesc(n.children(), list);
            }
            node = par;
            par = par.parent();
          }
          iter = list.iter();
        }
        return iter.next();
      }
    };
  }
}
