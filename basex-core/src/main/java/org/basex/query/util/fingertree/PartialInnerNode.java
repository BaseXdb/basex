package org.basex.query.util.fingertree;

/**
 * A partial node containing one potentially partial sub-node.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 *
 * @param <N> node type
 * @param <E> element type
 */
final class PartialInnerNode<N, E> extends PartialNode<Node<N, E>, E> {
  /** The sub-node. */
  final NodeLike<N, E> sub;

  /**
   * Constructor.
   * @param sub the sub-node
   */
  PartialInnerNode(final NodeLike<N, E> sub) {
    this.sub = sub;
  }

  @Override
  protected NodeLike<Node<N, E>, E>[] concat(final NodeLike<Node<N, E>, E> other) {
    if(other instanceof PartialInnerNode) {
      final PartialInnerNode<N, E> single = (PartialInnerNode<N, E>) other;
      final NodeLike<N, E>[] merged = sub.concat(single.sub);
      final NodeLike<N, E> a = merged[0], b = merged[1];

      @SuppressWarnings("unchecked")
      final NodeLike<Node<N, E>, E>[] out = (NodeLike<Node<N, E>, E>[]) merged;
      if(b == null) {
        out[0] = new PartialInnerNode<>(a);
      } else {
        @SuppressWarnings("unchecked")
        final Node<N, E>[] ch = new Node[] { (Node<N, E>) a, (Node<N, E>) b };
        out[0] = new InnerNode<>(ch);
        out[1] = null;
      }
      return out;
    }

    // merging with a full node cannot result in under-full nodes
    final InnerNode<N, E> inner = (InnerNode<N, E>) other;
    final NodeLike<N, E>[] merged = sub.concat(inner.getSub(0));
    final Node<N, E> a = (Node<N, E>) merged[0], b = (Node<N, E>) merged[1];

    @SuppressWarnings("unchecked")
    final NodeLike<Node<N, E>, E>[] out = (NodeLike<Node<N, E>, E>[]) merged;
    if(b == null) {
      // partial node was absorbed
      out[0] = inner.replaceFirst(a);
      return out;
    }

    inner.replaceFirst(out, a, b);
    return out;
  }

  @Override
  protected int append(final NodeLike<Node<N, E>, E>[] nodes, final int pos) {
    if(pos == 0) {
      nodes[pos] = this;
      return 1;
    }

    final NodeLike<Node<N, E>, E>[] joined = nodes[pos - 1].concat(this);
    nodes[pos - 1] = joined[0];

    if(joined[1] != null) {
      nodes[pos] = joined[1];
      return pos + 1;
    }

    return pos;
  }

  @Override
  protected void toString(final StringBuilder sb, final int indent) {
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append("OneSubnode[\n");
    sub.toString(sb, indent + 1);
    sb.append('\n');
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append(']');
  }
}
