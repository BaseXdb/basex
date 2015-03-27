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
        out[0] = new InnerNode2<>((Node<N, E>) a, (Node<N, E>) b);
        out[1] = null;
      }
      return out;
    }

    final InnerNode<N, E> deep0 = (InnerNode<N, E>) other;
    final NodeLike<N, E>[] merged = sub.concat(deep0.getSub(0));

    // merging with a full node cannot result in under-full nodes
    final Node<N, E> a = (Node<N, E>) merged[0], b = (Node<N, E>) merged[1];
    @SuppressWarnings("unchecked")
    final NodeLike<Node<N, E>, E>[] out = (NodeLike<Node<N, E>, E>[]) merged;

    if(other instanceof InnerNode2) {
      final InnerNode2<N, E> deep = (InnerNode2<N, E>) other;
      if(b == null) {
        out[0] = new InnerNode2<>(a, deep.child1);
      } else {
        out[0] = new InnerNode3<>(a, b, deep.child1);
        out[1] = null;
      }
    } else {
      final InnerNode3<N, E> deep = (InnerNode3<N, E>) other;
      if(b == null) {
        out[0] = new InnerNode3<>(a, deep.child1, deep.child2);
      } else {
        out[0] = new InnerNode2<>(a, b);
        out[1] = new InnerNode2<>(deep.child1, deep.child2);
      }
    }
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
