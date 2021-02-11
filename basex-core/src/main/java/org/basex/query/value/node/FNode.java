package org.basex.query.value.node;

import java.util.*;
import org.basex.api.dom.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Main-memory node fragment.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class FNode extends ANode {
  /** Parent node (can be {@code null}). */
  FNode parent;

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
  public final void parent(final FNode par) {
    parent = par;
  }

  @Override
  public BasicNodeIter attributeIter() {
    return BasicNodeIter.EMPTY;
  }

  @Override
  public BasicNodeIter childIter() {
    return BasicNodeIter.EMPTY;
  }

  @Override
  public boolean hasChildren() {
    return false;
  }

  @Override
  public final BasicNodeIter descendantIter() {
    return desc(false);
  }

  @Override
  public final BasicNodeIter descendantOrSelfIter() {
    return desc(true);
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
        if(nc.type == NodeType.ELEMENT || nc.type == NodeType.TEXT) tb.add(nc.string());
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
        final BasicNodeIter ir = last != null ? last.childIter() : self ? selfIter() : childIter();
        last = ir.next();
        if(last == null) {
          while(!iters.isEmpty()) {
            last = iters.peek().next();
            if(last != null) break;
            iters.pop();
          }
        } else {
          iters.add(ir);
        }
        return last;
      }
    };
  }

  @Override
  public final BXNode toJava() {
    return BXNode.get(this);
  }

  @Override
  public final int hashCode() {
    return id;
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof FNode)) return false;
    final FNode n = (FNode) obj;
    return type.eq(n.type) && Token.eq(value, n.value) && parent == n.parent;
  }
}
