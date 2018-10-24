package org.basex.query.value.node;

import java.util.*;
import org.basex.api.dom.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Main-memory node fragment.
 *
 * @author BaseX Team 2005-18, BSD License
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
  public FNode finish() {
    return this;
  }

  @Override
  public final boolean is(final ANode node) {
    return this == node;
  }

  @Override
  public final int diff(final ANode node) {
    // fragments: compare node ids. otherwise, find LCA
    return this == node ? 0 : node instanceof FNode ? id - node.id : diff(this, node);
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
      @Override
      public Value value(final QueryContext qc) { return value(); }
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
            final BasicNodeIter ir = par.children();
            if(node.type != NodeType.ATT) {
              for(final ANode nd : ir) {
                if(nd.is(node)) break;
              }
            }
            for(final ANode nd : ir) {
              list.add(nd.finish());
              addDesc(nd.children(), list);
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

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof FNode)) return false;
    final FNode n = (FNode) obj;
    return type.eq(n.type) && Token.eq(value, n.value) && parent == n.parent;
  }

  @Override
  public final BXNode toJava() {
    return BXNode.get(this);
  }
}
