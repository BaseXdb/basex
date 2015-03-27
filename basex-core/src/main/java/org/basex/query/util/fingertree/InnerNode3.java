package org.basex.query.util.fingertree;

/**
 * An inner node with three sub-nodes.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 *
 * @param <N> node type
 * @param <E> element type
 */
final class InnerNode3<N, E> extends InnerNode<N, E> {
  /** First sub-node. */
  final Node<N, E> child0;
  /** Second sub-node. */
  final Node<N, E> child1;
  /** Third sub-node. */
  final Node<N, E> child2;
  /** End of the first sub-node. */
  final long l;
  /** End of the second sub-node. */
  final long m;
  /** End of the third sub-node. */
  final long r;

  /**
   * Constructor for a trinary node.
   * @param a first sub-node
   * @param b second sub-node
   * @param c third sub-node
   */
  InnerNode3(final Node<N, E> a, final Node<N, E> b, final Node<N, E> c) {
    this.child0 = a;
    this.l = a.size();
    this.child1 = b;
    this.m = l + b.size();
    this.child2 = c;
    this.r = m + c.size();
  }

  @Override
  protected long size() {
    return r;
  }

  @Override
  protected InnerNode<N, E> reverse() {
    return new InnerNode3<>(child2.reverse(), child1.reverse(), child0.reverse());
  }

  @Override
  protected boolean insert(final Node<Node<N, E>, E>[] siblings, final long pos, final E val) {
    final InnerNode<N, E> left = (InnerNode<N, E>) siblings[0],
        right = (InnerNode<N, E>) siblings[2];
    @SuppressWarnings("unchecked")
    final Node<N, E>[] sub = (Node<N, E>[]) siblings;
    final Node<N, E> a, b, c, d;
    if(pos <= l) {
      // insert left
      sub[0] = null;
      sub[2] = child1;
      child0.insert(sub, pos, val);
      a = sub[1];
      b = sub[2];
      c = sub[3] == null ? child2 : sub[3];
      d = sub[3] == null ? null : child2;
    } else if(pos <= m) {
      // insert in the middle
      sub[0] = child0;
      sub[2] = child2;
      child1.insert(sub, pos - l, val);
      a = sub[0];
      b = sub[1];
      c = sub[2];
      d = sub[3];
    } else {
      // insert right
      sub[0] = child1;
      sub[2] = null;
      child2.insert(sub, pos - m, val);
      a = child0;
      b = sub[0];
      c = sub[1];
      d = sub[2];
    }

    if(d == null) {
      // no split
      siblings[0] = left;
      siblings[1] = new InnerNode3<>(a, b, c);
      siblings[2] = right;
      siblings[3] = null;
      return false;
    }

    if(left != null && left instanceof InnerNode2) {
      // merge with left sibling
      final InnerNode2<N, E> deep2 = (InnerNode2<N, E>) left;
      siblings[0] = new InnerNode3<>(deep2.child0, deep2.child1, a);
      siblings[1] = new InnerNode3<>(b, c, d);
      siblings[2] = right;
      siblings[3] = null;
      return false;
    }

    if(right != null && right instanceof InnerNode2) {
      // merge with left sibling
      final InnerNode2<N, E> deep2 = (InnerNode2<N, E>) right;
      siblings[0] = left;
      siblings[1] = new InnerNode3<>(a, b, c);
      siblings[2] = new InnerNode3<>(d, deep2.child0, deep2.child1);
      siblings[3] = null;
      return false;
    }

    // split the node
    siblings[0] = left;
    siblings[1] = new InnerNode2<>(a, b);
    siblings[2] = new InnerNode2<>(c, d);
    siblings[3] = right;
    return true;
  }

  @Override
  protected InnerNode<N, E> remove(final long pos) {
    if(pos < l) {
      final Node<N, E>[] res = child0.remove(null, child1, pos);
      if(res[1] == null) return new InnerNode2<>(res[2], child2);
      return new InnerNode3<>(res[1], res[2], child2);
    } else if(pos < m) {
      final Node<N, E>[] res = child1.remove(child0, child2, pos - l);
      if(res[1] == null) return new InnerNode2<>(res[0], res[2]);
      return new InnerNode3<>(res[0], res[1], res[2]);
    } else {
      final Node<N, E>[] res = child2.remove(child1, null, pos - m);
      if(res[1] == null) return new InnerNode2<>(child0, res[0]);
      return new InnerNode3<>(child0, res[0], res[1]);
    }
  }

  @Override
  protected Node<Node<N, E>, E>[] remove(final Node<Node<N, E>, E> left,
      final Node<Node<N, E>, E> right, final long pos) {
    final Node<N, E>[] res;
    final Node<Node<N, E>, E> node;
    if(pos < l) {
      res = child0.remove(null, child1, pos);
      node = res[1] == null ? new InnerNode2<>(res[2], child2)
                            : new InnerNode3<>(res[1], res[2], child2);
    } else if(pos < m) {
      res = child1.remove(child0, child2, pos - l);
      node = res[1] == null ? new InnerNode2<>(res[0], res[2])
                            : new InnerNode3<>(res[0], res[1], res[2]);
    } else {
      res = child2.remove(child1, null, pos - m);
      node = res[1] == null ? new InnerNode2<>(child0, res[0])
                            : new InnerNode3<>(child0, res[0], res[1]);
    }

    @SuppressWarnings("unchecked")
    final Node<Node<N, E>, E>[] out = (Node<Node<N, E>, E>[]) res;
    out[0] = left;
    out[1] = node;
    out[2] = right;
    return out;
  }

  @Override
  protected NodeLike<Node<N, E>, E> slice(final long start, final long len) {
    final long end = start + len;
    if(start < l) {
      // left node involved
      final long in0 = Math.min(l - start, len);
      final NodeLike<N, E> slice0 = in0 == l ? child0 : child0.slice(start, in0);
      if(end <= m) {
        // no right node
        if(end <= l) return new PartialInnerNode<>(slice0);
        final NodeLike<N, E> slice1 = end == m ? child1 : child1.slice(0, end - l);
        if(slice0 instanceof Node && slice1 instanceof Node)
          return new InnerNode2<>((Node<N, E>) slice0, (Node<N, E>) slice1);

        // partial nodes have to be merged
        final NodeLike<N, E>[] merged = slice0.concat(slice1);
        return merged[1] == null ? new PartialInnerNode<>(merged[0]) :
          new InnerNode2<>((Node<N, E>) merged[0], (Node<N, E>) merged[1]);
      }

      // middle is contained completely
      final long in2 = end - m;
      final NodeLike<N, E> slice2 = in2 == r - m ? child2 : child2.slice(0, in2);
      @SuppressWarnings("unchecked")
      final NodeLike<N, E>[] ns = new NodeLike[3];
      final int p0 = slice0.append(ns, 0), p1 = child1.append(ns, p0), p2 = slice2.append(ns, p1);
      return p2 == 1 ? new PartialInnerNode<>(ns[0])
           : p2 == 2 ? new InnerNode2<>((Node<N, E>) ns[0], (Node<N, E>) ns[1])
                     : new InnerNode3<>((Node<N, E>) ns[0], (Node<N, E>) ns[1], (Node<N, E>) ns[2]);
    }

    if(start < m) {
      // middle node included
      final long in1 = Math.min(m - start, len);
      final NodeLike<N, E> slice1 = in1 == m - l ? child1 : child1.slice(start - l, in1);
      if(end <= m) return new PartialInnerNode<>(slice1);

      // middle and last
      final long in2 = end - m;
      final NodeLike<N, E> slice2 = in2 == r - m ? child2 : child2.slice(0, in2);
      if(slice1 instanceof Node && slice2 instanceof Node)
        return new InnerNode2<>((Node<N, E>) slice1, (Node<N, E>) slice2);

      // partial nodes have to be merged
      final NodeLike<N, E>[] merged = slice1.concat(slice2);
      return merged[1] == null ? new PartialInnerNode<>(merged[0]) :
        new InnerNode2<>((Node<N, E>) merged[0], (Node<N, E>) merged[1]);
    }

    // only last node
    final NodeLike<N, E> slice2 = r - m == len ? child2 : child2.slice(start - m, len);
    return new PartialInnerNode<>(slice2);
  }

  @Override
  protected NodeLike<Node<N, E>, E>[] concat(final NodeLike<Node<N, E>, E> other) {
    if(other instanceof Node) {
      @SuppressWarnings("unchecked")
      final NodeLike<Node<N, E>, E>[] out = new NodeLike[] { this, other };
      return out;
    }

    final NodeLike<N, E> sub = ((PartialInnerNode<N, E>) other).sub;
    final NodeLike<N, E>[] merged = child2.concat(sub);
    final Node<N, E> a = (Node<N, E>) merged[0], b = (Node<N, E>) merged[1];
    @SuppressWarnings("unchecked")
    final NodeLike<Node<N, E>, E>[] out = (NodeLike<Node<N, E>, E>[]) merged;
    if(b == null) {
      out[0] = new InnerNode3<>(child0, child1, a);
    } else {
      out[0] = new InnerNode2<>(child0, child1);
      out[1] = new InnerNode2<>(a, b);
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
    child2.toString(sb, indent + 1);
    sb.append("\n");
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append("]");
  }

  @Override
  protected long checkInvariants() {
    final long lCheck = child0.checkInvariants();
    if(lCheck != l) throw new AssertionError("Wrong l: " + lCheck + " vs. " + l);
    final long mCheck = l + child1.checkInvariants();
    if(mCheck != m) throw new AssertionError("Wrong m: " + mCheck + " vs. " + m);
    final long rCheck = m + child2.checkInvariants();
    if(rCheck != r) throw new AssertionError("Wrong r: " + rCheck + " vs. " + r);
    return r;
  }

  @Override
  protected int arity() {
    return 3;
  }

  @Override
  protected Node<N, E> getSub(final int pos) {
    return pos == 0 ? child0 : pos == 1 ? child1 : child2;
  }

  @Override
  protected InnerNode<N, E> replaceFirst(final Node<N, E> newFirst) {
    return newFirst == child0 ? this : new InnerNode3<>(newFirst, child1, child2);
  }

  @Override
  protected InnerNode<N, E> replaceLast(final Node<N, E> newLast) {
    return newLast == child2 ? this : new InnerNode3<>(child0, child1, newLast);
  }

  @Override
  @SuppressWarnings("unchecked")
  Node<N, E>[] copyChildren() {
    return new Node[] { child0, child1, child2 };
  }
}
