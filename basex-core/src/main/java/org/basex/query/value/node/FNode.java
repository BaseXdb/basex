package org.basex.query.value.node;

import org.basex.api.dom.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
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

  /**
   * Constructor with a pre-allocated node ID.
   * @param type item type
   * @param id node ID
   */
  FNode(final NodeType type, final int id) {
    super(type, id);
  }

  @Override
  public final boolean is(final GNode node) {
    return this == node;
  }

  @Override
  public final int compare(final GNode node) {
    if(this == node) return 0;
    // fragments: compare node IDs
    if(node instanceof final FNode fnode) {
      // attributes may be created after the descendants of their parent: compare the parents
      final FNode n1 = this instanceof FAttr && parent != null ? parent : this;
      final FNode n2 = fnode instanceof FAttr && fnode.parent != null ? fnode.parent : fnode;
      // an element precedes its attributes
      return n1 != n2 ? Integer.signum(n1.id - n2.id) : n1 == this ? -1 : n2 == fnode ? 1 :
        Integer.signum(id - fnode.id);
    }
    // find LCA
    if(node instanceof final DBNode dbnode) return compare(this, dbnode);
    // comparison with JNode
    return -1;
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
  public BasicNodeIter attributeIter(final Test test) {
    return BasicNodeIter.EMPTY;
  }

  @Override
  public BasicNodeIter childIter(final Test test, final boolean descendant) {
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

  @Override
  public final byte[] id() {
    return Token.concat(Token.ID, id);
  }

  /**
   * Returns the string value for the specified nodes.
   * @param nodes nodes
   * @return string
   */
  static byte[] string(final GNode[] nodes) {
    if(nodes.length == 0) return Token.EMPTY;

    final TokenBuilder tb = new TokenBuilder();
    for(final GNode node : nodes) {
      if(node.kind().oneOf(Kind.ELEMENT, Kind.TEXT)) tb.add(node.string());
    }
    return tb.finish();
  }

  /**
   * Adds the children of the specified DOM node.
   * @param node node
   * @param builder parent node
   * @param nsMap namespace map
   */
  static void children(final Node node, final FBuilder builder,
      final TokenObjectMap<byte[]> nsMap) {
    final NodeList ch = node.getChildNodes();
    final int cl = ch.getLength();
    for(int c = 0; c < cl; c++) {
      final Node child = ch.item(c);

      switch(child.getNodeType()) {
        case Node.TEXT_NODE ->
          builder.node(new FTxt((Text) child));
        case Node.COMMENT_NODE ->
          builder.node(new FComm((Comment) child));
        case Node.PROCESSING_INSTRUCTION_NODE ->
          builder.node(new FPI((ProcessingInstruction) child));
        case Node.ELEMENT_NODE ->
          builder.node(FElem.build((Element) child, nsMap).finish());
        default -> { }
      }
    }
  }

  /**
   * Returns an iterator for the nodes that match the specified test.
   * @param nodes nodes
   * @param test node test (can be {@code null})
   * @return iterator
   */
  static BasicNodeIter iter(final GNode[] nodes, final Test test) {
    final int nl = nodes.length;
    if(nl == 0) return BasicNodeIter.EMPTY;

    if(test != null) return new BasicNodeIter() {
      int pos;

      @Override
      public GNode next() {
        while(pos < nl) {
          final GNode node = nodes[pos++];
          if(test.matches(node)) return node;
        }
        return null;
      }
    };

    return new BasicNodeIter() {
      int pos;

      @Override
      public long size() {
        return nl;
      }
      @Override
      public GNode next() {
        return pos < nl ? nodes[pos++] : null;
      }
      @Override
      public GNode get(final long i) {
        return nodes[(int) i];
      }
      @Override
      public Value value(final QueryContext qc, final Expr expr) {
        return ItemSeq.get(nodes, nl, NodeType.NODE.refine(expr));
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
    return obj instanceof final FNode n && type.eq(n.type) && parent == n.parent;
  }
}
