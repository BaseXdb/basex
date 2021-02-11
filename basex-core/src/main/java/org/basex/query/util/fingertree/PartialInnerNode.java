package org.basex.query.util.fingertree;

import org.basex.util.*;

/**
 * A partial node containing one potentially partial sub-node.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 *
 * @param <N> node type
 * @param <E> element type
 */
final class PartialInnerNode<N, E> implements NodeLike<Node<N, E>, E> {
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
  public int append(final NodeLike<Node<N, E>, E>[] out, final int pos) {
    if(pos == 0) {
      out[0] = this;
      return 1;
    }

    @SuppressWarnings("unchecked")
    final NodeLike<N, E>[] buffer = (NodeLike<N, E>[]) out;
    final NodeLike<Node<N, E>, E> left = out[pos - 1];
    if(left instanceof PartialInnerNode) {
      buffer[pos - 1] = ((PartialInnerNode<N, E>) left).sub;
      if(sub.append(buffer, pos) == pos) {
        out[pos - 1] = new PartialInnerNode<>(buffer[pos - 1]);
      } else {
        @SuppressWarnings("unchecked")
        final Node<N, E>[] ch = new Node[2];
        ch[0] = (Node<N, E>) buffer[pos - 1];
        ch[1] = (Node<N, E>) buffer[pos];
        out[pos - 1] = new InnerNode<>(ch);
        out[pos] = null;
      }
      return pos;
    }

    final Node<N, E>[] children = ((InnerNode<N, E>) left).children;
    final int n = children.length;
    final Node<N, E> a, b;
    if(sub instanceof Node) {
      a = children[n - 1];
      b = (Node<N, E>) sub;
    } else {
      buffer[pos - 1] = children[n - 1];
      if(sub.append(buffer, pos) == pos) {
        final Node<N, E>[] ch = children.clone();
        ch[n - 1] = (Node<N, E>) buffer[pos - 1];
        out[pos - 1] = new InnerNode<>(ch);
        return pos;
      }
      a = (Node<N, E>) buffer[pos - 1];
      b = (Node<N, E>) buffer[pos];
    }

    if(n < FingerTree.MAX_ARITY) {
      @SuppressWarnings("unchecked")
      final Node<N, E>[] ch = new Node[n + 1];
      Array.copy(children, n - 1, ch);
      ch[n - 1] = a;
      ch[n] = b;
      out[pos - 1] = new InnerNode<>(ch);
      out[pos] = null;
      return pos;
    }

    final int ll = (n + 1) / 2, rl = n + 1 - ll;
    @SuppressWarnings("unchecked")
    final Node<N, E>[] ls = new Node[ll], rs = new Node[rl];
    Array.copy(children, ll, ls);
    Array.copyToStart(children, ll, rl - 2, rs);
    rs[rl - 2] = a;
    rs[rl - 1] = b;
    out[pos - 1] = new InnerNode<>(ls);
    out[pos] = new InnerNode<>(rs);
    return pos + 1;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    toString(sb, 0);
    return sb.toString();
  }

  /**
   * Recursive helper for {@link #toString()}.
   * @param sb string builder
   * @param indent indentation level
   */
  void toString(final StringBuilder sb, final int indent) {
    for(int i = 0; i < indent; i++) sb.append(' ').append(' ');
    sb.append(Util.className(this)).append('[').append('\n');
    if(sub instanceof InnerNode) {
      ((InnerNode<?, ?>) sub).toString(sb, indent + 1);
    } else if(sub instanceof PartialInnerNode) {
      ((PartialInnerNode<?, ?>) sub).toString(sb, indent + 1);
    } else {
      for(final String line : sub.toString().split("\r\n?|\n")) {
        for(int i = 0; i <= indent; i++) sb.append(' ').append(' ');
        sb.append(line).append('\n');
      }
    }
    for(int i = 0; i < indent; i++) sb.append(' ').append(' ');
    sb.append(']');
  }
}
