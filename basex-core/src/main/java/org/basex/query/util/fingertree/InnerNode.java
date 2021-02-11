package org.basex.query.util.fingertree;

import org.basex.util.*;

/**
 * An inner node containing nested sub-nodes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 *
 * @param <N> node type
 * @param <E> element type
 */
final class InnerNode<N, E> implements Node<Node<N, E>, E> {
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
    bounds = new long[n];
    long off = 0;
    for(int i = 0; i < n; i++) {
      off += children[i].size();
      bounds[i] = off;
    }
    assert 2 <= n && n <= FingerTree.MAX_ARITY;
  }

  @Override
  public long size() {
    return bounds[bounds.length - 1];
  }

  @Override
  public int arity() {
    return bounds.length;
  }

  @Override
  public Node<N, E> getSub(final int pos) {
    return children[pos];
  }

  @Override
  public InnerNode<N, E> reverse() {
    final int n = children.length;
    @SuppressWarnings("unchecked")
    final Node<N, E>[] newChildren = new Node[n];
    for(int i = 0; i < n; i++) newChildren[i] = children[n - 1 - i].reverse();
    return new InnerNode<>(newChildren);
  }

  @Override
  public InnerNode<N, E> set(final long pos, final E val) {
    int i = 0;
    while(pos >= bounds[i]) i++;
    final long p = i == 0 ? pos : pos - bounds[i - 1];
    final Node<N, E>[] ch = children.clone();
    ch[i] = children[i].set(p, val);
    return new InnerNode<>(ch);
  }

  @Override
  public boolean insert(final Node<Node<N, E>, E>[] siblings, final long index, final E val) {
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
      Array.copy(subs, i == 0 ? 1 : 0, r - l + 1, out, l);
      siblings[0] = left;
      siblings[1] = new InnerNode<>(out);
      siblings[2] = right;
      return false;
    }

    @SuppressWarnings("unchecked")
    final Node<N, E>[] temp = new Node[n + 1];
    if(i == 0) {
      Array.copyToStart(subs, 1, 3, temp);
      Array.copy(children, 2, n - 2, temp, 3);
    } else if(i < n - 1) {
      Array.copy(children, l, temp);
      Array.copyFromStart(subs, 4, temp, l);
      Array.copy(children, r + 1, n - l - 3, temp, l + 4);
    } else {
      Array.copy(children, n - 2, temp);
      Array.copyFromStart(subs, 3, temp, n - 2);
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
        Array.copy(ch, la, ls);
        Array.copyFromStart(temp, move, ls, la);
        Array.copyToStart(temp, move, rs.length, rs);
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
        Array.copy(temp, ls.length, ls);
        Array.copyToStart(temp, ls.length, move, rs);
        Array.copyFromStart(ch, ra, rs, move);
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
      Array.copy(ch, ll, ls);
      Array.copyToStart(ch, ll, inL, mid1);
      Array.copyFromStart(temp, ml - inL, mid1, inL);
      Array.copyToStart(temp, ml - inL, ml, mid2);
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
      Array.copy(temp, ml, mid1);
      Array.copyToStart(temp, ml, ml - inR, mid2);
      Array.copyFromStart(ch, inR, mid2, ml - inR);
      Array.copyToStart(ch, inR, rl, rs);
      siblings[0] = null;
      siblings[1] = new InnerNode<>(mid1);
      siblings[2] = new InnerNode<>(mid2);
      siblings[3] = inR == 0 ? right : new InnerNode<>(rs);
      return true;
    }

    // split the node
    final int ll = (n + 1) / 2, rl = n + 1 - ll;
    @SuppressWarnings("unchecked")
    final Node<N, E>[] ls = new Node[ll], rs = new Node[rl];
    Array.copy(temp, ll, ls);
    Array.copyToStart(temp, ll, rl, rs);
    siblings[0] = null;
    siblings[1] = new InnerNode<>(ls);
    siblings[2] = new InnerNode<>(rs);
    siblings[3] = null;
    return true;
  }

  @Override
  public NodeLike<Node<N, E>, E>[] remove(final Node<Node<N, E>, E> left,
      final Node<Node<N, E>, E> right, final long pos) {
    int i = 0;
    final int n = bounds.length;
    while(pos >= bounds[i]) i++;
    final long off = i == 0 ? pos : pos - bounds[i - 1];

    final NodeLike<N, E>[] res = children[i].remove(
        i ==     0 ? null : children[i - 1],
        i == n - 1 ? null : children[i + 1], off);

    @SuppressWarnings("unchecked")
    final NodeLike<Node<N, E>, E>[] out = (NodeLike<Node<N, E>, E>[]) res;
    final Node<N, E> l = (Node<N, E>) res[0], m = (Node<N, E>) res[1], r = (Node<N, E>) res[2];

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
        Array.copy(children, i - 1, ch);
        ch[i - 1] = l;
      }
      if(i < n - 1) {
        ch[i] = r;
        Array.copy(children, i + 2, n - i - 2, ch, i + 1);
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
      Array.copy(ch, a - move, ls);
      Array.copyToStart(ch, a - move, move, ms);
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
      Array.copyFromStart(ch, move, ms, 1);
      Array.copyToStart(ch, move, rs.length, rs);
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
      Array.copy(ch, a, ls);
      ls[a] = single;
      out[0] = new InnerNode<>(ls);
      out[2] = right;
      return out;
    }

    if(right != null) {
      // merge with right sibling
      final Node<N, E>[] ch = ((InnerNode<N, E>) right).children;
      final int a = ch.length;
      @SuppressWarnings("unchecked")
      final Node<N, E>[] rs = new Node[a + 1];
      rs[0] = single;
      Array.copyFromStart(ch, a, rs, 1);
      out[0] = null;
      out[2] = new InnerNode<>(rs);
      return out;
    }

    // underflow
    out[0] = null;
    out[1] = new PartialInnerNode<>(single);
    out[2] = null;
    return out;
  }

  @Override
  @SuppressWarnings("unchecked")
  public NodeLike<Node<N, E>, E> slice(final long start, final long len) {
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
    Array.copy(buffer, inBuffer, subs);
    return new InnerNode<>(subs);
  }

  @Override
  public long checkInvariants() {
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
  public int append(final NodeLike<Node<N, E>, E>[] nodes, final int pos) {
    if(pos == 0 || nodes[pos - 1] instanceof InnerNode) {
      nodes[pos] = this;
      return pos + 1;
    }

    final NodeLike<N, E> sub = ((PartialInnerNode<N, E>) nodes[pos - 1]).sub;
    final int n = children.length;
    final Node<N, E> a, b;
    if(sub instanceof Node) {
      a = (Node<N, E>) sub;
      b = children[0];
    } else {
      @SuppressWarnings("unchecked")
      final NodeLike<N, E>[] buffer = (NodeLike<N, E>[]) nodes;
      buffer[pos - 1] = sub;
      if(children[0].append(buffer, pos) == pos) {
        nodes[pos - 1] = replaceFirst((Node<N, E>) buffer[pos - 1]);
        return pos;
      }
      a = (Node<N, E>) buffer[pos - 1];
      b = (Node<N, E>) buffer[pos];
    }

    if(n < FingerTree.MAX_ARITY) {
      @SuppressWarnings("unchecked")
      final Node<N, E>[] ch = new Node[n + 1];
      Array.copy(children, 1, n - 1, ch, 2);
      ch[0] = a;
      ch[1] = b;
      nodes[pos - 1] = new InnerNode<>(ch);
      nodes[pos] = null;
      return pos;
    }

    final int rl = (n + 1) / 2, ll = n + 1 - rl;
    @SuppressWarnings("unchecked")
    final Node<N, E>[] ls = new Node[ll], rs = new Node[rl];
    Array.copy(children, 1, ll - 2, ls, 2);
    ls[0] = a;
    ls[1] = b;
    Array.copyToStart(children, ll - 1, rl, rs);
    nodes[pos - 1] = new InnerNode<>(ls);
    nodes[pos] = new InnerNode<>(rs);
    return pos + 1;
  }

  /**
   * Recursive helper method for {@link #toString()}.
   * @param sb string builder
   * @param indent indentation depth
   */
  public void toString(final StringBuilder sb, final int indent) {
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append("Node(").append(size()).append(")[\n");
    for(final Node<N, E> sub : children) {
      FingerTree.toString(sub, sb, indent + 1);
      sb.append('\n');
    }
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append(']');
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    toString(sb, 0);
    return sb.toString();
  }

  /**
   * Returns a version of this node where the first sub-node is the given one.
   * @param newFirst new first sub-node
   * @return resulting node
   */
  InnerNode<N, E> replaceFirst(final Node<N, E> newFirst) {
    final Node<N, E>[] copy = children.clone();
    copy[0] = newFirst;
    return new InnerNode<>(copy);
  }

  /**
   * Returns a version of this node where the last sub-node is the given one.
   * @param newLast new last sub-node
   * @return resulting node
   */
  InnerNode<N, E> replaceLast(final Node<N, E> newLast) {
    final Node<N, E>[] copy = children.clone();
    copy[copy.length - 1] = newLast;
    return new InnerNode<>(copy);
  }
}
