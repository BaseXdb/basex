package org.basex.query.value.node;

import org.basex.api.dom.*;
import org.basex.query.iter.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.w3c.dom.*;

/**
 * Main-memory node fragment.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class FNode extends XNode {
  /** Parent node (can be {@code null}). */
  private FNode parent;

  /**
   * Constructor.
   * @param type item type
   */
  FNode(final NodeType type) {
    super(type);
  }

  @Override
  public final boolean is(final XNode node) {
    return this == node;
  }

  @Override
  public final int compare(final XNode node) {
    // fragments: compare node IDs. otherwise, find LCA
    return this == node ? 0 : node instanceof FNode ? Integer.signum(id - node.id) :
      compare(this, node);
  }

  @Override
  public final XNode parent() {
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
  public boolean hasAttributes() {
    return false;
  }

  /**
   * Returns the string value for the specified nodes.
   * @param nodes nodes
   * @return string
   */
  static byte[] string(final XNode[] nodes) {
    if(nodes.length == 0) return Token.EMPTY;

    final TokenBuilder tb = new TokenBuilder();
    for(final XNode node : nodes) {
      if(node.type.oneOf(NodeType.ELEMENT, NodeType.TEXT)) tb.add(node.string());
    }
    return tb.finish();
  }

  /**
   * Returns the children of the specified DOM node.
   * @param node node
   * @param builder parent node
   * @param nsMap namespace map
   */
  static void children(final Node node, final FBuilder builder,
      final TokenObjectMap<byte[]> nsMap) {
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
    return obj instanceof final FNode n && type.eq(n.type) && parent == n.parent;
  }
}
