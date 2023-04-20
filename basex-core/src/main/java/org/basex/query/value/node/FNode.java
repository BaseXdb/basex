package org.basex.query.value.node;

import java.util.*;

import org.basex.api.dom.*;
import org.basex.query.iter.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.w3c.dom.*;

/**
 * Main-memory node fragment.
 *
 * @author BaseX Team 2005-23, BSD License
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
   * @param nodes nodes
   * @return string
   */
  static final byte[] string(final ANode[] nodes) {
    if(nodes.length == 0) return Token.EMPTY;

    final TokenBuilder tb = new TokenBuilder();
    for(final ANode node : nodes) {
      if(node.type == NodeType.ELEMENT || node.type == NodeType.TEXT) tb.add(node.string());
    }
    return tb.finish();
  }

  /**
   * Returns the children of the specified DOM node.
   * @param node node
   * @param builder parent node
   * @param nsMap namespace map
   */
  static void children(final Node node, final FBuilder builder, final TokenMap nsMap) {
    final NodeList ch = node.getChildNodes();
    final int cl = ch.getLength();
    for(int c = 0; c < cl; ++c) {
      final Node child = ch.item(c);

      switch(child.getNodeType()) {
        case Node.TEXT_NODE:
          builder.add(new FTxt((Text) child));
          break;
        case Node.COMMENT_NODE:
          builder.add(new FComm((Comment) child));
          break;
        case Node.PROCESSING_INSTRUCTION_NODE:
          builder.add(new FPI((ProcessingInstruction) child));
          break;
        case Node.ELEMENT_NODE:
          builder.add(FElem.build((Element) child, nsMap).finish());
          break;
        default:
          break;
      }
    }
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
    return type.eq(n.type) && parent == n.parent;
  }
}
