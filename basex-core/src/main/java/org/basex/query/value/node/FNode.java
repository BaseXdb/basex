package org.basex.query.value.node;

import org.basex.api.dom.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * Main-memory node fragment.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class FNode extends XNode {
  /** Empty node array. */
  static final GNode[] NO_NODES = {};

  /** Parent node (can be {@code null}). */
  private XNode parent;

  /**
   * Constructor.
   * @param type item type
   */
  FNode(final NodeType type) {
    super(type);
  }

  /**
   * Constructor with a parent node.
   * @param type item type
   * @param parent parent node (can be {@code null})
   */
  FNode(final NodeType type, final XNode parent) {
    super(type);
    this.parent = parent;
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
  public boolean is(final GNode node) {
    return this == node;
  }

  @Override
  public int compare(final GNode node) {
    if(this == node) return 0;
    // namespace nodes are compared via their parent elements
    if(node instanceof final FNSpace nspace && nspace.parent() != null)
      return compareNSpace(this, nspace);
    // fragments: compare node IDs
    if(node instanceof final FNode fnode) {
      // attributes may be created after the descendants of their parent: compare the parents
      final FNode n1 = this instanceof FAttr && parent instanceof final FNode p ? p : this;
      final FNode n2 = fnode instanceof FAttr && fnode.parent instanceof final FNode p ? p : fnode;
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
  public final BasicNodeIter childIter(final Test test, final boolean descendant) {
    return iter(children(), descendant ? null : test);
  }

  @Override
  public BasicNodeIter followingSiblingIter(final boolean self, final Test test) {
    return siblingIter(self, true, test);
  }

  @Override
  public BasicNodeIter precedingSiblingIter(final boolean self, final Test test) {
    return siblingIter(self, false, test);
  }

  /**
   * Returns an iterator over the following or preceding siblings.
   * @param self include this node
   * @param following following or preceding siblings
   * @param test node test (can be {@code null})
   * @return iterator
   */
  private BasicNodeIter siblingIter(final boolean self, final boolean following,
      final Test test) {
    final GNode[] nodes = parent instanceof final FNode fnode && isChild() ? fnode.children() :
      NO_NODES;
    final int pos = position(nodes);
    if(pos == -1) {
      return following ? super.followingSiblingIter(self, test) :
        super.precedingSiblingIter(self, test);
    }
    final int step = following ? 1 : -1, start = self ? pos : pos + step,
        end = following ? nodes.length : -1;
    return new BasicNodeIter() {
      int s = start;

      @Override
      public GNode next() {
        while(s != end) {
          final GNode node = nodes[s];
          s += step;
          if(matches(node, test)) return node;
        }
        return null;
      }
    };
  }

  /**
   * Returns the position of this node in the specified sibling array.
   * @param nodes siblings
   * @return position, or {@code -1} if the node is not found
   */
  private int position(final GNode[] nodes) {
    // binary search by node ID, linear search if IDs are not in document order
    for(int l = 0, h = nodes.length - 1; l <= h;) {
      final int m = l + h >>> 1;
      final GNode node = nodes[m];
      if(node == this) return m;
      if(node.id - id < 0) l = m + 1;
      else h = m - 1;
    }
    for(int n = nodes.length - 1; n >= 0; n--) {
      if(nodes[n] == this) return n;
    }
    return -1;
  }

  /**
   * Returns the children.
   * @return children
   */
  GNode[] children() {
    return NO_NODES;
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
  public byte[] id() {
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
   * Copies an element, text, comment or processing instruction and its descendants to a fragment.
   * @param node node
   * @return copy
   */
  public static FNode fragment(final XNode node) {
    return switch(node.kind()) {
      case ELEMENT -> {
        final FBuilder elem = FElem.build(node.qname()).ns(node.namespaces());
        bind(elem, node.qname());
        for(final GNode attr : node.attributeIter()) {
          elem.node(new FAttr(attr.qname(), attr.string()));
          bind(elem, attr.qname());
        }
        for(final GNode child : node.childIter()) elem.node(fragment((XNode) child));
        yield elem.finish();
      }
      case TEXT -> new FTxt(node.string());
      case COMMENT -> new FComm(node.string());
      default -> new FPI(node.qname(), node.string());
    };
  }

  /**
   * Declares the namespace of a name if it may have been inherited from an ancestor.
   * @param elem element builder
   * @param name element or attribute name
   */
  private static void bind(final FBuilder elem, final QNm name) {
    final byte[] prefix = name.prefix(), uri = name.uri();
    if((prefix.length != 0 || uri.length != 0) &&
        (elem.namespaces == null || !elem.namespaces.contains(prefix))) elem.ns(prefix, uri);
  }

  /**
   * Adds the children of the specified DOM node.
   * @param node node
   * @param builder parent node
   * @param scope namespaces in scope of the parent
   */
  static void children(final Node node, final FBuilder builder, final Atts scope) {
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
          builder.node(FElem.build((Element) child, scope).finish());
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
}
