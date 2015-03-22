package org.basex.query.util.fingertree;

/**
 * An inner node with two sub-nodes.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 *
 * @param <N> node type
 * @param <E> element type
 */
final class InnerNode2<N, E> extends InnerNode<N, E> {
  /** First sub-node. */
  final Node<N, E> child0;
  /** Second sub-node. */
  final Node<N, E> child1;
  /** End of the first sub-node. */
  final long l;
  /** End of the second sub-node. */
  final long r;

  /**
   * Constructor for a binary node.
   * @param a first sub-node
   * @param b second sub-node
   */
  InnerNode2(final Node<N, E> a, final Node<N, E> b) {
    child0 = a;
    l = a.size();
    child1 = b;
    r = l + b.size();
  }

  @Override
  protected long size() {
    return r;
  }

  @Override
  protected InnerNode<N, E> reverse() {
    return new InnerNode2<>(child1.reverse(), child0.reverse());
  }

  @Override
  protected boolean insert(final Node<Node<N, E>, E>[] siblings, final long pos, final E val) {
    final InnerNode<N, E> left = (InnerNode<N, E>) siblings[0],
        right = (InnerNode<N, E>) siblings[2];
    @SuppressWarnings("unchecked")
    final Node<N, E>[] sub = (Node<N, E>[]) siblings;
    final Node<Node<N, E>, E> inserted;
    if(pos < l || pos == l && l <= r - l) {
      // insert into left sub-tree
      sub[0] = null;
      sub[2] = child1;
      child0.insert(sub, pos, val);
      final Node<N, E> a = sub[1], b = sub[2], c = sub[3];
      inserted = c == null ? new InnerNode2<>(a, b) : new InnerNode3<>(a, b, c);
    } else {
      // insert into right sub-tree
      sub[0] = child0;
      sub[2] = null;
      child1.insert(sub, pos - l, val);
      final Node<N, E> a = sub[0], b = sub[1], c = sub[2];
      inserted = c == null ? new InnerNode2<>(a, b) : new InnerNode3<>(a, b, c);
    }

    // restore the array
    siblings[0] = left;
    siblings[1] = inserted;
    siblings[2] = right;
    siblings[3] = null;
    return false;
  }

  @Override
  protected Node<Node<N, E>, E>[] remove(final Node<Node<N, E>, E> left,
      final Node<Node<N, E>, E> right, final long pos) {
    final Node<N, E>[] res;
    final Node<N, E> split0, split1;
    if(pos < l) {
      res = child0.remove(null, child1, pos);
      if(res[1] != null) {
        split0 = res[1];
        split1 = res[2];
      } else {
        split0 = res[2];
        split1 = null;
      }
    } else {
      res = child1.remove(child0, null, pos - l);
      split0 = res[0];
      split1 = res[1];
    }

    @SuppressWarnings("unchecked")
    final Node<Node<N, E>, E>[] out = (Node<Node<N, E>, E>[]) res;

    if(split1 != null) {
      // nodes were not merged
      out[0] = left;
      out[1] = new InnerNode2<>(split0, split1);
      out[2] = right;
      return out;
    }

    if(left != null && left instanceof InnerNode3) {
      // steal from the left
      final InnerNode3<N, E> steal = (InnerNode3<N, E>) left;
      out[0] = new InnerNode2<>(steal.child0, steal.child1);
      out[1] = new InnerNode2<>(steal.child2, split0);
      out[2] = right;
      return out;
    }

    if(right != null && right instanceof InnerNode3) {
      // steal from the right
      final InnerNode3<N, E> steal = (InnerNode3<N, E>) right;
      out[0] = left;
      out[1] = new InnerNode2<>(split0, steal.child0);
      out[2] = new InnerNode2<>(steal.child1, steal.child2);
      return out;
    }

    if(left != null && (right == null || left.size() < right.size())) {
      // merge with left neighbor
      final InnerNode2<N, E> merge = (InnerNode2<N, E>) left;
      out[0] = new InnerNode3<>(merge.child0, merge.child1, split0);
      out[1] = null;
      out[2] = right;
    } else {
      // merge with right neighbor
      final InnerNode2<N, E> merge = (InnerNode2<N, E>) right;
      out[0] = left;
      out[1] = null;
      out[2] = new InnerNode3<>(split0, merge.child0, merge.child1);
    }

    return out;
  }

  @Override
  protected NodeLike<Node<N, E>, E> remove(final long pos) {
    if(pos < l) {
      final Node<N, E>[] res = child0.remove(null, child1, pos);
      if(res[1] == null) return new PartialInnerNode<>(res[2]);
      return new InnerNode2<>(res[1], res[2]);
    }

    final Node<N, E>[] res = child1.remove(child0, null, pos - l);
    if(res[1] == null) return new PartialInnerNode<>(res[0]);
    return new InnerNode2<>(res[0], res[1]);
  }

  @Override
  protected NodeLike<Node<N, E>, E> slice(final long start, final long len) {
    if(start >= l) {
      // everything is in right sub-node
      return new PartialInnerNode<>(len == r - l ? child1 : child1.slice(start - l, len));
    }

    if(start + len <= l) {
      // everything is in left sub-node
      return new PartialInnerNode<>(len == l ? child0 : child0.slice(start, len));
    }

    // both nodes are involved
    final long inL = l - start, inR = len - inL;
    final NodeLike<N, E> left = inL == l ? child0 : child0.slice(start, inL);
    final NodeLike<N, E> right = inR == r - l ? child1 : child1.slice(0, inR);
    if(left instanceof Node && right instanceof Node)
      return new InnerNode2<>((Node<N, E>) left, (Node<N, E>) right);

    final NodeLike<N, E>[] res = left.concat(right);
    return res[1] == null ? new PartialInnerNode<>(res[0]) :
      new InnerNode2<>((Node<N, E>) res[0], (Node<N, E>) res[1]);
  }

  @Override
  protected NodeLike<Node<N, E>, E>[] concat(final NodeLike<Node<N, E>, E> other) {
    if(other instanceof Node) {
      @SuppressWarnings("unchecked")
      final NodeLike<Node<N, E>, E>[] out = new NodeLike[] { this, other };
      return out;
    }

    final NodeLike<N, E> sub = ((PartialInnerNode<N, E>) other).sub;
    final NodeLike<N, E>[] merged = child1.concat(sub);
    final Node<N, E> a = (Node<N, E>) merged[0], b = (Node<N, E>) merged[1];
    @SuppressWarnings("unchecked")
    final NodeLike<Node<N, E>, E>[] out = (NodeLike<Node<N, E>, E>[]) merged;
    if(b == null) {
      out[0] = new InnerNode2<>(child0, a);
    } else {
      out[0] = new InnerNode3<>(child0, a, b);
      out[1] = null;
    }
    return out;
  }

  @Override
  protected int append(final NodeLike<Node<N, E>, E>[] nodes, final int pos) {
    if(pos == 0) {
      nodes[0] = this;
      return 1;
    }

    final NodeLike<Node<N, E>, E> left = nodes[pos - 1];
    if(left instanceof Node) {
      nodes[pos] = this;
      return pos + 1;
    }

    final NodeLike<Node<N, E>, E>[] joined = left.concat(this);
    nodes[pos - 1] = joined[0];
    if(joined[1] == null) return pos;
    nodes[pos] = joined[1];
    return pos + 1;
  }

  @Override
  protected void toString(final StringBuilder sb, final int indent) {
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append("Node(").append(size()).append(")[\n");
    child0.toString(sb, indent + 1);
    sb.append("\n");
    child1.toString(sb, indent + 1);
    sb.append("\n");
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append("]");
  }

  @Override
  protected long checkInvariants() {
    final long lCheck = child0.checkInvariants();
    if(lCheck != l) throw new AssertionError("Wrong l: " + lCheck + " vs. " + l);
    final long rCheck = lCheck + child1.checkInvariants();
    if(rCheck != r) throw new AssertionError("Wrong r: " + rCheck + " vs. " + r);
    return rCheck;
  }

  @Override
  protected int arity() {
    return 2;
  }

  @Override
  protected Node<N, E> getSub(final int pos) {
    return pos == 0 ? child0 : child1;
  }

  @Override
  protected InnerNode<N, E> replaceFirst(final Node<N, E> newFirst) {
    return newFirst == child0 ? this : new InnerNode2<>(newFirst, child1);
  }

  @Override
  protected InnerNode<N, E> replaceLast(final Node<N, E> newLast) {
    return newLast == child1 ? this : new InnerNode2<>(child0, newLast);
  }

  @Override
  @SuppressWarnings("unchecked")
  Node<N, E>[] copyChildren() {
    return new Node[] { child0, child1 };
  }
}
