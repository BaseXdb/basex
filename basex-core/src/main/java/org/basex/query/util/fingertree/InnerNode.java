package org.basex.query.util.fingertree;

/**
 * An inner node containing nested sub-nodes.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 *
 * @param <N> node type
 * @param <E> element type
 */
final class InnerNode<N, E> extends Node<Node<N, E>, E> {
  /** Child nodes. */
  final Node<N, E>[] children;
  /** Right bound for the elements' index in each sub-node. */
  final long[] bounds;

  /**
   * Constructor.
   * @param children children array
   */
  InnerNode(final Node<N, E>[] children) {
    final int n = children.length;
    this.children = children;
    this.bounds = new long[n];
    long off = 0;
    for(int i = 0; i < n; i++) {
      off += children[i].size();
      bounds[i] = off;
    }
    assert 2 <= n && n <= FingerTree.MAX_ARITY;
  }

  @Override
  protected long size() {
    return bounds[bounds.length - 1];
  }

  @Override
  protected int arity() {
    return bounds.length;
  }

  @Override
  protected Node<N, E> getSub(final int pos) {
    return children[pos];
  }

  @Override
  protected Node<Node<N, E>, E> reverse() {
    final int n = children.length;
    @SuppressWarnings("unchecked")
    final Node<N, E>[] newChildren = new Node[n];
    for(int i = 0; i < n; i++) newChildren[i] = children[n - 1 - i].reverse();
    return new InnerNode<>(newChildren);
  }

  @Override
  protected boolean insert(final Node<Node<N, E>, E>[] siblings, final long index, final E val) {
    final Node<Node<N, E>, E> left = siblings[0], right = siblings[2];

    int i = 0;
    final int n = bounds.length;
    while(index > bounds[i]) i++;
    final long off = i == 0 ? index : index - bounds[i - 1];

    @SuppressWarnings("unchecked")
    final Node<N, E>[] subs = (Node<N, E>[]) siblings;
    subs[0] = i ==     0 ? null : children[i - 1];
    subs[2] = i == n - 1 ? null : children[i + 1];

    final int l = Math.max(0, i - 1), r = Math.min(i + 1, n - 1);
    if(!children[i].insert(subs, off, val)) {
      // no split
      final Node<N, E>[] out = children.clone();
      System.arraycopy(subs, i == 0 ? 1 : 0, out, l, r - l + 1);
      siblings[0] = left;
      siblings[1] = new InnerNode<>(out);
      siblings[2] = right;
      return false;
    }

    @SuppressWarnings("unchecked")
    final Node<N, E>[] temp = new Node[n + 1];
    if(i == 0) {
      System.arraycopy(subs, 1, temp, 0, 3);
      System.arraycopy(children, 2, temp, 3, n - 2);
    } else if(i < n - 1) {
      System.arraycopy(children, 0, temp, 0, l);
      System.arraycopy(subs, 0, temp, l, 4);
      System.arraycopy(children, r + 1, temp, l + 4, n - l - 3);
    } else {
      System.arraycopy(children, 0, temp, 0, n - 2);
      System.arraycopy(subs, 0, temp, n - 2, 3);
    }

    if(n < FingerTree.MAX_ARITY) {
      // still small enough
      siblings[0] = left;
      siblings[1] = new InnerNode<>(temp);
      siblings[2] = right;
      return false;
    }

    if(left != null) {
      final int la = left.arity(), move = (FingerTree.MAX_ARITY - la + 1) / 2;
      if(move > 0) {
        // left node has capacity
        final Node<N, E>[] ch = ((InnerNode<N, E>) left).children;
        @SuppressWarnings("unchecked")
        final Node<N, E>[] ls = new Node[la + move], rs = new Node[n + 1 - move];
        System.arraycopy(ch, 0, ls, 0, la);
        System.arraycopy(temp, 0, ls, la, move);
        System.arraycopy(temp, move, rs, 0, rs.length);
        siblings[0] = new InnerNode<>(ls);
        siblings[1] = new InnerNode<>(rs);
        siblings[2] = right;
        return false;
      }
    }

    if(right != null) {
      final int ra = right.arity(), move = (FingerTree.MAX_ARITY - ra + 1) / 2;
      if(move > 0) {
        // right node has capacity
        final Node<N, E>[] ch = ((InnerNode<N, E>) right).children;
        @SuppressWarnings("unchecked")
        final Node<N, E>[] ls = new Node[n + 1 - move], rs = new Node[ra + move];
        System.arraycopy(temp, 0, ls, 0, ls.length);
        System.arraycopy(temp, ls.length, rs, 0, move);
        System.arraycopy(ch, 0, rs, move, ra);
        siblings[0] = left;
        siblings[1] = new InnerNode<>(ls);
        siblings[2] = new InnerNode<>(rs);
        return false;
      }
    }

    if(left != null) {
      // merge with left neighbor
      final Node<N, E>[] ch = ((InnerNode<N, E>) left).children;
      final int la = ch.length, k = la + n + 1, ml = k / 3, ll = k - 2 * ml, inL = la - ll;
      @SuppressWarnings("unchecked")
      final Node<N, E>[] ls = new Node[ll], mid1 = new Node[ml], mid2 = new Node[ml];
      System.arraycopy(ch, 0, ls, 0, ll);
      System.arraycopy(ch, ll, mid1, 0, inL);
      System.arraycopy(temp, 0, mid1, inL, ml - inL);
      System.arraycopy(temp, ml - inL, mid2, 0, ml);
      siblings[0] = inL == 0 ? left : new InnerNode<>(ls);
      siblings[1] = new InnerNode<>(mid1);
      siblings[2] = new InnerNode<>(mid2);
      siblings[3] = right;
      return true;
    }

    if(right != null) {
      // merge with right neighbor
      final Node<N, E>[] ch = ((InnerNode<N, E>) right).children;
      final int ra = ch.length, k = n + 1 + ra, ml = k / 3, rl = k - 2 * ml, inR = ra - rl;
      @SuppressWarnings("unchecked")
      final Node<N, E>[] mid1 = new Node[ml], mid2 = new Node[ml], rs = new Node[rl];
      System.arraycopy(temp, 0, mid1, 0, ml);
      System.arraycopy(temp, ml, mid2, 0, ml - inR);
      System.arraycopy(ch, 0, mid2, ml - inR, inR);
      System.arraycopy(ch, inR, rs, 0, rl);
      siblings[0] = left;
      siblings[1] = new InnerNode<>(mid1);
      siblings[2] = new InnerNode<>(mid2);
      siblings[3] = inR == 0 ? right : new InnerNode<>(rs);
      return true;
    }

    // split the node
    final int ll = (n + 1) / 2, rl = n + 1 - ll;
    @SuppressWarnings("unchecked")
    final Node<N, E>[] ls = new Node[ll], rs = new Node[rl];
    System.arraycopy(temp, 0, ls, 0, ll);
    System.arraycopy(temp, ll, rs, 0, rl);
    siblings[0] = null;
    siblings[1] = new InnerNode<>(ls);
    siblings[2] = new InnerNode<>(rs);
    siblings[3] = null;
    return true;
  }

  @Override
  protected NodeLike<Node<N, E>, E> remove(final long pos) {
    int i = 0;
    final int n = bounds.length;
    while(pos >= bounds[i]) i++;
    final long off = i == 0 ? pos : pos - bounds[i - 1];

    final Node<N, E> left = i == 0 ? null : children[i - 1],
                     right = i == n - 1 ? null : children[i + 1];
    final Node<N, E>[] res = children[i].remove(left, right, off);

    if(res[1] != null) {
      // no underflow
      final Node<N, E>[] out = children.clone();
      final int l = Math.max(0, i - 1), r = Math.min(i + 1, n - 1);
      System.arraycopy(res, i == 0 ? 1 : 0, out, l, r - l + 1);
      return new InnerNode<>(out);
    }

    if(n == 2) {
      // this node underflowed
      return new PartialInnerNode<>(res[i == 0 ? 2 : 0]);
    }

    // still big enough
    @SuppressWarnings("unchecked")
    final Node<N, E>[] out = new Node[n - 1];
    if(i > 0) {
      System.arraycopy(children, 0, out, 0, i - 1);
      out[i - 1] = res[0];
    }
    if(i < n - 1) {
      out[i] = res[2];
      System.arraycopy(children, i + 2, out, i + 1, n - i - 2);
    }
    return new InnerNode<>(out);
  }

  @Override
  protected Node<Node<N, E>, E>[] remove(final Node<Node<N, E>, E> left,
      final Node<Node<N, E>, E> right, final long pos) {
    int i = 0;
    final int n = bounds.length;
    while(pos >= bounds[i]) i++;
    final long off = i == 0 ? pos : pos - bounds[i - 1];

    final Node<N, E>[] res = children[i].remove(
        i ==     0 ? null : children[i - 1],
        i == n - 1 ? null : children[i + 1], off);

    @SuppressWarnings("unchecked")
    final Node<Node<N, E>, E>[] out = (Node<Node<N, E>, E>[]) res;
    final Node<N, E> l = res[0], m = res[1], r = res[2];

    if(m != null) {
      // no underflow
      final Node<N, E>[] ch = children.clone();
      if(i > 0) ch[i - 1] = l;
      ch[i] = m;
      if(i < n - 1) ch[i + 1] = r;
      out[0] = left;
      out[1] = new InnerNode<>(ch);
      out[2] = right;
      return out;
    }

    if(n > 2) {
      // still big enough
      @SuppressWarnings("unchecked")
      final Node<N, E>[] ch = new Node[n - 1];
      if(i > 0) {
        System.arraycopy(children, 0, ch, 0, i - 1);
        ch[i - 1] = l;
      }
      if(i < n - 1) {
        ch[i] = r;
        System.arraycopy(children, i + 2, ch, i + 1, n - i - 2);
      }
      out[0] = left;
      out[1] = new InnerNode<>(ch);
      out[2] = right;
      return out;
    }

    // only one sub-node left
    final Node<N, E> single = i == 0 ? r : l;

    if(left != null && left.arity() > 2) {
      // refill from left sibling
      final Node<N, E>[] ch = ((InnerNode<N, E>) left).children;
      final int a = ch.length, move = (a - 1) / 2;
      @SuppressWarnings("unchecked")
      final Node<N, E>[] ls = new Node[a - move], ms = new Node[move + 1];
      System.arraycopy(ch, 0, ls, 0, a - move);
      System.arraycopy(ch, a - move, ms, 0, move);
      ms[move] = single;
      out[0] = new InnerNode<>(ls);
      out[1] = new InnerNode<>(ms);
      out[2] = right;
      return out;
    }

    if(right != null && right.arity() > 2) {
      // refill from right sibling
      final Node<N, E>[] ch = ((InnerNode<N, E>) right).children;
      final int a = ch.length, move = (a - 1) / 2;
      @SuppressWarnings("unchecked")
      final Node<N, E>[] ms = new Node[move + 1], rs = new Node[a - move];
      ms[0] = single;
      System.arraycopy(ch, 0, ms, 1, move);
      System.arraycopy(ch, move, rs, 0, rs.length);
      out[0] = left;
      out[1] = new InnerNode<>(ms);
      out[2] = new InnerNode<>(rs);
      return out;
    }

    if(left != null) {
      // merge with left sibling
      final Node<N, E>[] ch = ((InnerNode<N, E>) left).children;
      final int a = ch.length;
      @SuppressWarnings("unchecked")
      final Node<N, E>[] ls = new Node[a + 1];
      System.arraycopy(ch, 0, ls, 0, a);
      ls[a] = single;
      out[0] = new InnerNode<>(ls);
      out[2] = right;
    } else {
      // merge with right sibling
      final Node<N, E>[] ch = ((InnerNode<N, E>) right).children;
      final int a = ch.length;
      @SuppressWarnings("unchecked")
      final Node<N, E>[] rs = new Node[a + 1];
      rs[0] = single;
      System.arraycopy(ch, 0, rs, 1, a);
      out[0] = left;
      out[2] = new InnerNode<>(rs);
    }
    return out;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected NodeLike<Node<N, E>, E> slice(final long start, final long len) {
    // find the range of affected sub-nodes
    int p = 0;
    while(start >= bounds[p]) p++;
    final long off = p == 0 ? start : start - bounds[p - 1], end = start + len - 1;
    final int l = p;
    while(end >= bounds[p]) p++;
    final int r = p;

    // first node can be partial
    final Node<N, E> first = children[l];
    final long inFst = Math.min(bounds[l] - start, len);
    final NodeLike<N, E> fst = inFst == first.size() ? first : first.slice(off, inFst);
    if(l == r) return new PartialInnerNode<>(fst);

    // more than one node affected
    final NodeLike<N, E>[] buffer = new NodeLike[r - l + 1];
    buffer[0] = fst;
    int inBuffer = 1;
    for(int i = l + 1; i < r; i++) inBuffer = children[i].append(buffer, inBuffer);
    final Node<N, E> last = children[r];
    final long inLst = start + len - bounds[r - 1];
    final NodeLike<N, E> lst = inLst == last.size() ? last : last.slice(0, inLst);
    inBuffer = lst.append(buffer, inBuffer);

    if(inBuffer == 1) {
      // merged into a single sub-node
      return new PartialInnerNode<>(buffer[0]);
    }

    // enough children for a full node
    final Node<N, E>[] subs = new Node[inBuffer];
    System.arraycopy(buffer, 0, subs, 0, inBuffer);
    return new InnerNode<>(subs);
  }

  @Override
  protected long checkInvariants() {
    final int a = children.length;
    if(a < 2 || a > FingerTree.MAX_ARITY) throw new AssertionError("Wrong arity: " + a);
    long b = 0;
    for(int i = 0; i < a; i++) {
      final Node<N, E> ch = children[i];
      b += ch.checkInvariants();
      if(b != bounds[i]) throw new AssertionError("Wrong boundary: " + b);
    }
    return b;
  }

  @Override
  protected NodeLike<Node<N, E>, E>[] concat(final NodeLike<Node<N, E>, E> other) {
    if(other instanceof Node) {
      // nothing to do
      @SuppressWarnings("unchecked")
      final NodeLike<Node<N, E>, E>[] out = new NodeLike[] { this, other };
      return out;
    }

    final int n = children.length;
    final NodeLike<N, E> single = ((PartialInnerNode<N, E>) other).sub;
    final NodeLike<N, E>[] res = children[n - 1].concat(single);
    @SuppressWarnings("unchecked")
    final NodeLike<Node<N, E>, E>[] out = (NodeLike<Node<N, E>, E>[]) res;

    if(res[1] == null) {
      // partial node was absorbed
      final Node<N, E>[] ch = children.clone();
      ch[n - 1] = (Node<N, E>) res[0];
      out[0] = new InnerNode<>(ch);
      return out;
    }

    if(n < FingerTree.MAX_ARITY) {
      // new sub-node can be absorbed
      @SuppressWarnings("unchecked")
      final Node<N, E>[] ch = new Node[n + 1];
      System.arraycopy(children, 0, ch, 0, n - 1);
      ch[n - 1] = (Node<N, E>) res[0];
      ch[n] = (Node<N, E>) res[1];
      out[0] = new InnerNode<>(ch);
      out[1] = null;
      return out;
    }

    // split this node
    final int ll = (n + 1) / 2, rl = n + 1 - ll;
    @SuppressWarnings("unchecked")
    final Node<N, E>[] ls = new Node[ll], rs = new Node[rl];
    System.arraycopy(children, 0, ls, 0, ll);
    System.arraycopy(children, ll, rs, 0, rl - 2);
    rs[rl - 2] = (Node<N, E>) res[0];
    rs[rl - 1] = (Node<N, E>) res[1];
    out[0] = new InnerNode<>(ls);
    out[1] = new InnerNode<>(rs);
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
    for(final Node<N, E> sub : children) {
      sub.toString(sb, indent + 1);
      sb.append("\n");
    }
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append("]");
  }

  /**
   * Returns a version of this node where the first sub-node is the given one.
   * @param newFirst new first sub-node
   * @return resulting node
   */
  Node<Node<N, E>, E> replaceFirst(final Node<N, E> newFirst) {
    final Node<N, E>[] copy = children.clone();
    copy[0] = newFirst;
    return new InnerNode<>(copy);
  }

  /**
   * Replaces the first sub-node with the two given ones and writes the results to the given array.
   * @param out output array
   * @param a first node
   * @param b second node
   */
  void replaceFirst(final NodeLike<Node<N, E>, E>[] out, final Node<N, E> a, final Node<N, E> b) {
    final int n = children.length;
    if(n == 2) {
      @SuppressWarnings("unchecked")
      final Node<N, E>[] subs = new Node[] { a, b, children[1] };
      out[0] = new InnerNode<>(subs);
      out[1] = null;
    } else {
      final int ll = (n + 1) / 2, rl = n + 1 - ll;
      @SuppressWarnings("unchecked")
      final Node<N, E>[] ls = new Node[ll], rs = new Node[rl];
      ls[0] = a;
      ls[1] = b;
      System.arraycopy(children, 1, ls, 2, ll - 2);
      System.arraycopy(children, ll - 1, rs, 0, rl);
      out[0] = new InnerNode<>(ls);
      out[1] = new InnerNode<>(rs);
    }
  }

  /**
   * Returns a version of this node where the last sub-node is the given one.
   * @param newLast new last sub-node
   * @return resulting node
   */
  Node<Node<N, E>, E> replaceLast(final Node<N, E> newLast) {
    final Node<N, E>[] copy = children.clone();
    copy[copy.length - 1] = newLast;
    return new InnerNode<>(copy);
  }
}
