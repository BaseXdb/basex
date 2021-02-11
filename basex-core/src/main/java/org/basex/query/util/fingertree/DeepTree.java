package org.basex.query.util.fingertree;

import org.basex.query.*;
import org.basex.util.*;

/**
 * A <i>deep</i> node containing elements in the left and right digit and a sub-tree in
 * the middle.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 *
 * @param <N> node type
 * @param <E> element type
 */
final class DeepTree<N, E> extends FingerTree<N, E> {
  /** Preferred size of an inner node. */
  private static final int NODE_SIZE = MAX_ARITY;
  /** Left digit. */
  final Node<N, E>[] left;
  /** Size of the left digit, cached for speeding up indexing. */
  final long leftSize;
  /** Middle tree. */
  final FingerTree<Node<N, E>, E> middle;
  /** Right digit. */
  final Node<N, E>[] right;

  /** Size of this tree. */
  final long size;

  /**
   * Constructor.
   * @param left left digit
   * @param leftSize size of the left digit
   * @param middle middle tree
   * @param right right digit
   * @param size size of this tree
   */
  DeepTree(final Node<N, E>[] left, final long leftSize,
      final FingerTree<Node<N, E>, E> middle, final Node<N, E>[] right, final long size) {
    this.left = left;
    this.leftSize = leftSize;
    this.middle = middle;
    this.right = right;
    this.size = size;
    assert left.length > 0 && right.length > 0
        && size == leftSize + middle.size() + size(right);
  }

  /**
   * Factory method calculating the size of the left digit.
   * @param <N> node type
   * @param <E> element type
   * @param left left digit
   * @param middle middle tree
   * @param right right digit
   * @param size size of this tree
   * @return the deep node
   */
  static <N, E> DeepTree<N, E> get(final Node<N, E>[] left, final FingerTree<Node<N, E>, E> middle,
      final Node<N, E>[] right, final long size) {
    return new DeepTree<>(left, size(left), middle, right, size);
  }

  /**
   * Factory method for deep nodes with an empty middle tree.
   * @param <N> node type
   * @param <E> element type
   * @param left left digit
   * @param leftSize size of the left sub-tree
   * @param right right digit
   * @param size size of this tree
   * @return the deep node
   */
  static <N, E> DeepTree<N, E> get(final Node<N, E>[] left, final long leftSize,
      final Node<N, E>[] right, final long size) {
    return new DeepTree<>(left, leftSize, EmptyTree.getInstance(), right, size);
  }

  /**
   * Factory method for deep nodes with an empty middle tree calculating the size of the left digit.
   * @param <N> node type
   * @param <E> element type
   * @param left left digit
   * @param right right digit
   * @param size size of this tree
   * @return the deep node
   */
  static <N, E> DeepTree<N, E> get(final Node<N, E>[] left, final Node<N, E>[] right,
      final long size) {
    return new DeepTree<>(left, size(left), EmptyTree.getInstance(), right, size);
  }

  /**
   * Factory method for deep nodes calculating all cached sizes.
   * @param <N> node type
   * @param <E> element type
   * @param left left digit
   * @param middle middle sub-tree
   * @param right right digit
   * @return the deep node
   */
  static <N, E> DeepTree<N, E> get(final Node<N, E>[] left,
      final FingerTree<Node<N, E>, E> middle, final Node<N, E>[] right) {
    final long l = size(left), m = middle.size(), r = size(right);
    return new DeepTree<>(left, l, middle, right, l + m + r);
  }

  /**
   * Factory method for deep nodes with empty middle tree calculating all cached sizes.
   * @param <N> node type
   * @param <E> element type
   * @param left left digit
   * @param right right digit
   * @return the deep node
   */
  static <N, E> DeepTree<N, E> get(final Node<N, E>[] left, final Node<N, E>[] right) {
    final long l = size(left), r = size(right);
    return new DeepTree<>(left, l, EmptyTree.getInstance(), right, l + r);
  }

  @Override
  public DeepTree<N, E> cons(final Node<N, E> fst) {
    final long sz = fst.size();
    if(left.length < MAX_DIGIT) {
      final Node<N, E>[] newLeft = slice(left, -1, left.length);
      newLeft[0] = fst;
      return new DeepTree<>(newLeft, leftSize + sz, middle, right, size + sz);
    }

    final int ll = left.length, m = ll - NODE_SIZE;
    final Node<N, E>[] newLeft = slice(left, -1, m), sub = slice(left, m, ll);
    newLeft[0] = fst;
    final FingerTree<Node<N, E>, E> mid = middle.cons(new InnerNode<>(sub));
    return get(newLeft, mid, right, size + sz);
  }

  @Override
  public DeepTree<N, E> snoc(final Node<N, E> lst) {
    if(right.length < MAX_DIGIT) {
      final Node<N, E>[] newRight = slice(right, 0, right.length + 1);
      newRight[right.length] = lst;
      return new DeepTree<>(left, leftSize, middle, newRight, size + lst.size());
    }

    final int rl = right.length, m = NODE_SIZE;
    final Node<N, E>[] sub = slice(right, 0, m), newRight = slice(right, m, rl + 1);
    newRight[rl - m] = lst;
    final FingerTree<Node<N, E>, E> mid = middle.snoc(new InnerNode<>(sub));
    return new DeepTree<>(left, leftSize, mid, newRight, size + lst.size());
  }

  @Override
  public Node<N, E> head() {
    return left[0];
  }

  @Override
  public Node<N, E> last() {
    return right[right.length - 1];
  }

  @Override
  public FingerTree<N, E> init() {
    final long newSize = size - right[right.length - 1].size();

    if(right.length > 1) {
      // right digit is safe, just shrink it
      return new DeepTree<>(left, leftSize, middle, slice(right, 0, right.length - 1), newSize);
    }

    if(middle.isEmpty()) {
      // middle tree empty, make a tree from the left list
      if(left.length == 1) return new SingletonTree<>(left[0]);

      final int mid = left.length / 2;
      return get(slice(left, 0, mid), slice(left, mid, left.length), newSize);
    }

    // extract values for the right digit from the middle
    final InnerNode<N, E> last = (InnerNode<N, E>) middle.last();
    return new DeepTree<>(left, leftSize, middle.init(), last.children, newSize);
  }

  @Override
  public FingerTree<N, E> tail() {
    final long fstSize = left[0].size(),  newSize = size - fstSize;

    if(left.length > 1) {
      // left digit is safe, just shrink it
      final Node<N, E>[] newLeft = slice(left, 1, left.length);
      return new DeepTree<>(newLeft, leftSize - fstSize, middle, right, newSize);
    }

    if(middle.isEmpty()) {
      // middle tree empty, make a tree from the right list
      if(right.length == 1) return new SingletonTree<>(right[0]);

      final int mid = right.length / 2;
      return get(slice(right, 0, mid), slice(right, mid, right.length), newSize);
    }

    // extract values for the left digit from the middle
    final InnerNode<N, E> head = (InnerNode<N, E>) middle.head();
    return new DeepTree<>(head.children, head.size(), middle.tail(), right, newSize);
  }

  @Override
  public long size() {
    return size;
  }

  @Override
  public DeepTree<N, E> concat(final Node<N, E>[] nodes, final long sz,
      final FingerTree<N, E> other) {
    final DeepTree<N, E> lft = (DeepTree<N, E>) addAll(nodes, sz, false);
    if(!(other instanceof DeepTree)) return other.isEmpty() ? lft : lft.snoc(other.head());

    final DeepTree<N, E> rght = (DeepTree<N, E>) other;
    final Node<N, E>[] as = lft.right, bs = rght.left;
    final int l = as.length, n = l + bs.length, k = (n + MAX_ARITY - 1) / MAX_ARITY;
    @SuppressWarnings("unchecked")
    final Node<Node<N, E>, E>[] out = new Node[k];
    for(int i = 0, p = 0; i < k; i++) {
      final int rem = k - i, curr = (n - p + rem - 1) / rem;
      @SuppressWarnings("unchecked")
      final Node<N, E>[] ch = new Node[curr];
      final int inL = l - p;
      if(curr <= inL) {
        Array.copyToStart(as, p, curr, ch);
      } else if(inL > 0) {
        Array.copyToStart(as, p, inL, ch);
        Array.copyFromStart(bs, curr - inL, ch, inL);
      } else {
        Array.copyToStart(bs, -inL, curr, ch);
      }
      out[i] = new InnerNode<>(ch);
      p += curr;
    }

    final long inMid = lft.rightSize() + rght.leftSize;
    final FingerTree<Node<N, E>, E> newMid = lft.middle.concat(out, inMid, rght.middle);
    final long newSize = lft.leftSize + newMid.size() + rght.rightSize();
    return new DeepTree<>(lft.left, lft.leftSize, newMid, rght.right, newSize);
  }

  @Override
  public FingerTree<N, E> reverse(final QueryContext qc) {
    qc.checkStop();
    final int l = left.length, r = right.length;
    @SuppressWarnings("unchecked")
    final Node<N, E>[] newLeft = new Node[r], newRight = new Node[l];
    for(int i = 0; i < r; i++) newLeft[i] = right[r - 1 - i].reverse();
    for(int i = 0; i < l; i++) newRight[i] = left[l - 1 - i].reverse();
    return new DeepTree<>(newLeft, rightSize(), middle.reverse(qc), newRight, size);
  }

  @Override
  public FingerTree<N, E> set(final long pos, final E val) {
    long off = pos;
    if(off < leftSize) {
      final Node<N, E>[] newLeft = left.clone();
      int i = 0;
      for(;; i++) {
        final long sub = newLeft[i].size();
        if(off < sub) break;
        off -= sub;
      }
      newLeft[i] = newLeft[i].set(off, val);
      return new DeepTree<>(newLeft, leftSize, middle, right, size);
    }
    off -= leftSize;

    final long mid = middle.size();
    if(off < mid) {
      return new DeepTree<>(left, leftSize, middle.set(off, val), right, size);
    }
    off -= mid;

    final Node<N, E>[] newRight = right.clone();
    int i = 0;
    for(;; i++) {
      final long sub = newRight[i].size();
      if(off < sub) break;
      off -= sub;
    }
    newRight[i] = newRight[i].set(off, val);
    return new DeepTree<>(left, leftSize, middle, newRight, size);
  }

  @Override
  public FingerTree<N, E> insert(final long pos, final E val, final QueryContext qc) {
    qc.checkStop();
    if(pos <= leftSize) {
      // insert into left digit
      int i = 0;
      long p = pos;
      for(;; i++) {
        final long sub = left[i].size();
        if(p <= sub) break;
        p -= sub;
      }

      final int ll = left.length;
      final Node<N, E> l = i > 0 ? left[i - 1] : null, r = i + 1 < ll ? left[i + 1] : null;
      @SuppressWarnings("unchecked")
      final Node<N, E>[] siblings = new Node[] { l, null, r, null };
      if(!left[i].insert(siblings, p, val)) {
        // no split
        final Node<N, E>[] newLeft = left.clone();
        if(i > 0) newLeft[i - 1] = siblings[0];
        newLeft[i] = siblings[1];
        if(i + 1 < ll) newLeft[i + 1] = siblings[2];
        return new DeepTree<>(newLeft, leftSize + 1, middle, right, size + 1);
      }

      // node was split
      @SuppressWarnings("unchecked")
      final Node<N, E>[] temp = new Node[ll + 1];
      if(i > 0) {
        Array.copy(left, i - 1, temp);
        temp[i - 1] = siblings[0];
      }
      temp[i] = siblings[1];
      temp[i + 1] = siblings[2];
      if(i + 1 < ll) {
        temp[i + 2] = siblings[3];
        Array.copy(left, i + 2, ll - i - 2, temp, i + 3);
      }
      if(ll < MAX_DIGIT) return new DeepTree<>(temp, leftSize + 1, middle, right, size + 1);

      // digit has to be split
      final int m = temp.length - NODE_SIZE;
      final Node<N, E>[] newLeft = slice(temp, 0, m), ch = slice(temp, m, temp.length);
      return get(newLeft, middle.cons(new InnerNode<>(ch)), right, size + 1);
    }

    long p = pos - leftSize;
    final long midSize = middle.size();
    if(p < midSize)
      return new DeepTree<>(left, leftSize, middle.insert(p, val, qc), right, size + 1);

    // insert into right digit
    p -= midSize;
    int i = 0;
    for(;; i++) {
      final long sub = right[i].size();
      if(p <= sub) break;
      p -= sub;
    }

    final int rl = right.length;
    final Node<N, E> l = i > 0 ? right[i - 1] : null, r = i + 1 < rl ? right[i + 1] : null;
    @SuppressWarnings("unchecked")
    final Node<N, E>[] siblings = new Node[] { l, null, r, null };
    if(!right[i].insert(siblings, p, val)) {
      // no split
      final Node<N, E>[] newRight = right.clone();
      if(i > 0) newRight[i - 1] = siblings[0];
      newRight[i] = siblings[1];
      if(i + 1 < rl) newRight[i + 1] = siblings[2];
      return new DeepTree<>(left, leftSize, middle, newRight, size + 1);
    }

    // node was split
    @SuppressWarnings("unchecked")
    final Node<N, E>[] temp = new Node[rl + 1];
    if(i > 0) {
      Array.copy(right, i - 1, temp);
      temp[i - 1] = siblings[0];
    }
    temp[i] = siblings[1];
    temp[i + 1] = siblings[2];
    if(i + 1 < rl) {
      temp[i + 2] = siblings[3];
      Array.copy(right, i + 2, rl - i - 2, temp, i + 3);
    }
    if(right.length < MAX_DIGIT) return new DeepTree<>(left, leftSize, middle, temp, size + 1);

    // digit has to be split
    final int m = NODE_SIZE;
    final Node<N, E>[] ch = slice(temp, 0, m), newRight = slice(temp, m, temp.length);
    return new DeepTree<>(left, leftSize, middle.snoc(new InnerNode<>(ch)), newRight, size + 1);
  }

  @Override
  public TreeSlice<N, E> remove(final long pos, final QueryContext qc) {
    qc.checkStop();
    if(pos < leftSize) return new TreeSlice<>(removeLeft(pos));
    final long rightStart = leftSize + middle.size();
    if(pos >= rightStart) return new TreeSlice<>(removeRight(pos - rightStart));

    final TreeSlice<Node<N, E>, E> slice = middle.remove(pos - leftSize, qc);
    if(slice.isTree()) {
      // no underflow
      final FingerTree<Node<N, E>, E> newMiddle = slice.getTree();
      return slice.setTree(new DeepTree<>(left, leftSize, newMiddle, right, size - 1));
    }

    // middle tree had an underflow, one sub-node left
    final Node<N, E> node = (Node<N, E>) ((PartialInnerNode<N, E>) slice.getPartial()).sub;

    // try to extend the smaller digit
    if(left.length < right.length) {
      // merge into left digit
      final Node<N, E>[] newLeft = slice(left, 0, left.length + 1);
      newLeft[left.length] = node;
      return slice.setTree(get(newLeft, leftSize + node.size(), right, size - 1));
    }

    if(right.length < MAX_DIGIT) {
      // merge into right digit
      final Node<N, E>[] newRight = slice(right, -1, right.length);
      newRight[0] = node;
      return slice.setTree(get(left, leftSize, newRight, size - 1));
    }

    // redistribute the nodes
    final int n = 2 * MAX_DIGIT + 1, ll = (n - NODE_SIZE) / 2;
    @SuppressWarnings("unchecked")
    final Node<N, E>[] newLeft = slice(left, 0, ll), ch = new Node[NODE_SIZE];
    final int inL = left.length - ll, inR = NODE_SIZE - inL - 1;
    Array.copyToStart(left, ll, inL, ch);
    ch[inL] = node;
    Array.copyFromStart(right, inR, ch, inL + 1);
    final Node<N, E>[] newRight = slice(right, inR, MAX_DIGIT);
    final Node<Node<N, E>, E> newMid = new InnerNode<>(ch);
    return slice.setTree(get(newLeft, new SingletonTree<>(newMid), newRight, size - 1));
  }

  /**
   * Remove an element from the left digit.
   * @param pos position inside the left digit
   * @return resulting tree
   */
  private FingerTree<N, E> removeLeft(final long pos) {
    if(left.length > 1) {
      // left digit cannot underflow, just delete the element
      return new DeepTree<>(remove(left, pos), leftSize - 1, middle, right, size - 1);
    }

    // singleton digit might underflow
    final Node<N, E> node = left[0];

    if(!middle.isEmpty()) {
      // next node for balancing is in middle tree
      final InnerNode<N, E> head = (InnerNode<N, E>) middle.head();
      final Node<N, E> first = head.getSub(0);
      final NodeLike<N, E>[] rem = node.remove(null, first, pos);
      final Node<N, E> newNode = (Node<N, E>) rem[1], newFirst = (Node<N, E>) rem[2];

      if(newNode == null) {
        // nodes were merged
        final Node<N, E>[] newLeft = head.children.clone();
        newLeft[0] = newFirst;
        return get(newLeft, middle.tail(), right, size - 1);
      }

      @SuppressWarnings("unchecked")
      final Node<N, E>[] newLeft = new Node[] { newNode };

      if(newFirst != first) {
        // nodes were balanced
        final FingerTree<Node<N, E>, E> newMid = middle.replaceHead(head.replaceFirst(newFirst));
        return new DeepTree<>(newLeft, newNode.size(), newMid, right, size - 1);
      }

      // no changes to this tree's structure
      return new DeepTree<>(newLeft, newNode.size(), middle, right, size - 1);
    }

    // potentially balance with right digit
    final NodeLike<N, E>[] rem = node.remove(null, right[0], pos);
    final Node<N, E> newNode = (Node<N, E>) rem[1], newFirstRight = (Node<N, E>) rem[2];

    if(newNode == null) {
      // nodes were merged
      if(right.length == 1) return new SingletonTree<>(newFirstRight);
      final int mid = right.length / 2;
      final Node<N, E>[] newLeft = slice(right, 0, mid);
      newLeft[0] = newFirstRight;
      return get(newLeft, middle, slice(right, mid, right.length), size - 1);
    }

    // structure does not change
    @SuppressWarnings("unchecked")
    final Node<N, E>[] newLeft = new Node[] { newNode };

    if(newFirstRight == right[0]) {
      // right digit stays the same
      return new DeepTree<>(newLeft, newLeft[0].size(), middle, right, size - 1);
    }

    // adapt the right digit
    final Node<N, E>[] newRight = right.clone();
    newRight[0] = newFirstRight;
    return new DeepTree<>(newLeft, newNode.size(), middle, newRight, size - 1);
  }

  /**
   * Remove an element from the right digit.
   * @param pos position inside the right digit
   * @return resulting tree
   */
  private FingerTree<N, E> removeRight(final long pos) {
    if(right.length > 1) {
      // right digit cannot underflow, just delete the element
      return new DeepTree<>(left, leftSize, middle, remove(right, pos), size - 1);
    }

    // singleton digit might underflow
    final Node<N, E> node = right[0];

    if(!middle.isEmpty()) {
      // potentially balance with middle tree
      final InnerNode<N, E> last = (InnerNode<N, E>) middle.last();
      final Node<N, E> lastSub = last.getSub(last.arity() - 1);
      final NodeLike<N, E>[] rem = node.remove(lastSub, null, pos);
      final Node<N, E> newLastSub = (Node<N, E>) rem[0], newNode = (Node<N, E>) rem[1];

      if(newNode == null) {
        // nodes were merged
        final Node<N, E>[] newRight = last.children.clone();
        newRight[newRight.length - 1] = newLastSub;
        return new DeepTree<>(left, leftSize, middle.init(), newRight, size - 1);
      }

      @SuppressWarnings("unchecked")
      final Node<N, E>[] newRight = new Node[] { newNode };

      // replace last node in middle tree
      final Node<Node<N, E>, E> newLast = last.replaceLast(newLastSub);
      return new DeepTree<>(left, leftSize, middle.replaceLast(newLast), newRight, size - 1);
    }

    // balance with left digit
    final Node<N, E> lastLeft = left[left.length - 1];
    final NodeLike<N, E>[] rem = node.remove(lastLeft, null, pos);
    final Node<N, E> newLastLeft = (Node<N, E>) rem[0], newNode = (Node<N, E>) rem[1];
    if(newNode == null) {
      // nodes were merged
      if(left.length == 1) {
        // only one node left
        return new SingletonTree<>(newLastLeft);
      }

      @SuppressWarnings("unchecked")
      final Node<N, E>[] newRight = new Node[] { newLastLeft };
      return get(slice(left, 0, left.length - 1), newRight, size - 1);
    }

    @SuppressWarnings("unchecked")
    final Node<N, E>[] newRight = new Node[] { newNode };

    if(newLastLeft == lastLeft) {
      // deletion could be absorbed
      return get(left, leftSize, newRight, size - 1);
    }

    // adapt the left digit
    final Node<N, E>[] newLeft = left.clone();
    newLeft[newLeft.length - 1] = newLastLeft;
    return get(newLeft, newRight, size - 1);
  }

  /**
   * Deletes an element from the given digit containing at least two nodes.
   * @param <N> node type
   * @param <E> element type
   * @param arr array of nodes
   * @param pos deletion position
   * @return new digit
   */
  private static <N, E> Node<N, E>[] remove(final Node<N, E>[] arr, final long pos) {
    int i = 0;
    long off = pos;
    Node<N, E> node;
    while(true) {
      node = arr[i];
      final long nodeSize = node.size();
      if(off < nodeSize) break;
      off -= nodeSize;
      i++;
    }

    final int n = arr.length;
    final NodeLike<N, E>[] res = arr[i].remove(
        i == 0 ? null : arr[i - 1], i == n - 1 ? null : arr[i + 1], off);
    final Node<N, E> l = (Node<N, E>) res[0], m = (Node<N, E>) res[1], r = (Node<N, E>) res[2];
    if(m != null) {
      // same number of nodes
      final Node<N, E>[] out = arr.clone();
      if(i > 0) out[i - 1] = l;
      out[i] = m;
      if(i < n - 1) out[i + 1] = r;
      return out;
    }

    // the node was merged
    @SuppressWarnings("unchecked")
    final Node<N, E>[] out = new Node[n - 1];
    if(i > 0) {
      // nodes to the left
      Array.copy(arr, i - 1, out);
      out[i - 1] = l;
    }

    if(i < n - 1) {
      // nodes to the right
      out[i] = r;
      Array.copy(arr, i + 2, n - i - 2, out, i + 1);
    }

    return out;
  }

  @Override
  public TreeSlice<N, E> slice(final long from, final long len) {
    if(from == 0 && len == size) return new TreeSlice<>(this);
    final long midSize = middle.size(), rightOff = leftSize + midSize;

    final long inLeft = from + len <= leftSize ? len : from < leftSize ? leftSize - from : 0;
    final long inRight = from >= rightOff ? len : from + len > rightOff ? from + len - rightOff : 0;

    @SuppressWarnings("unchecked")
    final NodeLike<N, E>[] buffer = new NodeLike[2 * MAX_DIGIT + 1];
    int inBuffer = splitDigit(left, from, inLeft, buffer, 0);
    if(inLeft == len) {
      if(inBuffer == 1) return new TreeSlice<>(buffer[0]);
      final int mid1 = inBuffer / 2;
      final Node<N, E>[] ls = slice(buffer, 0, mid1), rs = slice(buffer, mid1, inBuffer);
      return new TreeSlice<>(get(ls, rs, len));
    }

    final long inMiddle = len - inLeft - inRight;
    final FingerTree<Node<N, E>, E> mid;
    final TreeSlice<Node<N, E>, E> slice;
    if(inMiddle == 0) {
      mid = EmptyTree.getInstance();
      slice = new TreeSlice<>(mid);
    } else {
      final long midOff = from <= leftSize ? 0 : from - leftSize;
      slice = middle.slice(midOff, inMiddle);
      if(slice.isTree()) {
        mid = slice.getTree();
      } else {
        final NodeLike<N, E> sub = ((PartialInnerNode<N, E>) slice.getPartial()).sub;
        inBuffer = sub.append(buffer, inBuffer);
        mid = EmptyTree.getInstance();
      }
    }

    final long rightFrom = from < rightOff ? 0 : from - rightOff;
    if(mid.isEmpty()) {
      inBuffer = splitDigit(right, rightFrom, inRight, buffer, inBuffer);
      return slice.setNodes(buffer, inBuffer, len);
    }

    final FingerTree<Node<N, E>, E> mid2;
    if(inBuffer > 1 || buffer[0] instanceof Node) {
      mid2 = mid;
    } else {
      final InnerNode<N, E> head = (InnerNode<N, E>) mid.head();
      final int k = head.arity();
      inBuffer = head.getSub(0).append(buffer, inBuffer);
      for(int i = 1; i < k; i++) buffer[inBuffer++] = head.getSub(i);
      mid2 = mid.tail();
    }

    if(mid2.isEmpty()) {
      inBuffer = splitDigit(right, rightFrom, inRight, buffer, inBuffer);
      return slice.setNodes(buffer, inBuffer, len);
    }

    final Node<N, E>[] newLeft = slice(buffer, 0, inBuffer);
    inBuffer = splitDigit(right, rightFrom, inRight, buffer, 0);

    final FingerTree<Node<N, E>, E> mid3;
    final Node<N, E>[] newRight;
    if(inBuffer == 0) {
      mid3 = mid2.init();
      newRight = ((InnerNode<N, E>) mid2.last()).children;
    } else if(inBuffer > 1 || buffer[0] instanceof Node) {
      mid3 = mid2;
      newRight = slice(buffer, 0, inBuffer);
    } else {
      final NodeLike<N, E> partial = buffer[0];
      final InnerNode<N, E> last = (InnerNode<N, E>) mid2.last();
      final int k = last.arity();
      for(int i = 0; i < k; i++) buffer[i] = last.getSub(i);
      inBuffer = partial.append(buffer, k);
      mid3 = mid2.init();
      newRight = slice(buffer, 0, inBuffer);
    }

    return slice.setTree(get(newLeft, mid3, newRight, len));
  }

  /**
   * Creates a tree slice from a digit.
   * @param <N> node type
   * @param <E> element type
   * @param nodes the digit
   * @param from element offset
   * @param len number of elements
   * @param buffer buffer to insert the node slice into
   * @param inBuffer initial number of nodes in the buffer
   * @return the slice
   */
  private static <N, E> int splitDigit(final Node<N, E>[] nodes, final long from,
      final long len, final NodeLike<N, E>[] buffer, final int inBuffer) {
    if(len <= 0) return inBuffer;

    // find the first sub-node containing used elements
    int firstPos = 0;
    long firstOff = from;
    Node<N, E> first = nodes[0];
    long firstSize = first.size();
    while(firstOff >= firstSize) {
      firstOff -= firstSize;
      first = nodes[++firstPos];
      firstSize = first.size();
    }

    // firstOff < firstSize
    final long inFirst = firstSize - firstOff;
    if(inFirst >= len) {
      // everything in first sub-node
      final NodeLike<N, E> part = len == firstSize ? first : first.slice(firstOff, len);
      return part.append(buffer, inBuffer);
    }

    final NodeLike<N, E> firstSlice = firstOff == 0 ? first : first.slice(firstOff, inFirst);
    int numMerged = firstSlice.append(buffer, inBuffer);

    int pos = firstPos;
    long remaining = len - inFirst;
    while(remaining > 0) {
      final Node<N, E> curr = nodes[++pos];
      final long currSize = curr.size();
      final NodeLike<N, E> slice = remaining >= currSize ? curr : curr.slice(0, remaining);
      numMerged = slice.append(buffer, numMerged);
      remaining -= currSize;
    }

    return numMerged;
  }

  /**
   * Calculates the size of the right digit.
   * @return number of elements in the right digit
   */
  private long rightSize() {
    return size - leftSize - middle.size();
  }

  @Override
  FingerTree<N, E> addAll(final Node<N, E>[] nodes, final long sz, final boolean appendLeft) {
    final int k = nodes.length;
    if(k == 0) return this;
    if(k == 1) return appendLeft ? cons(nodes[0]) : snoc(nodes[0]);

    if(appendLeft) {
      int l = k + left.length;
      final Node<N, E>[] ls = slice(nodes, 0, l);
      Array.copyFromStart(left, left.length, ls, k);
      if(l <= MAX_DIGIT) return get(ls, middle, right);

      FingerTree<Node<N, E>, E> newMid = middle;
      for(int rem = (l + MAX_ARITY - 1) / MAX_ARITY; rem > 1; rem--) {
        final int curr = (l + rem - 1) / rem;
        newMid = newMid.cons(new InnerNode<>(slice(ls, l - curr, l)));
        l -= curr;
      }

      return get(slice(ls, 0, l), newMid, right);
    }

    final int r = right.length + k;
    final Node<N, E>[] rs = slice(right, 0, r);
    Array.copyFromStart(nodes, k, rs, right.length);
    if(k + right.length <= MAX_DIGIT) return get(left, middle, rs);

    int i = 0;
    FingerTree<Node<N, E>, E> newMid = middle;
    for(int rem = (r + MAX_ARITY - 1) / MAX_ARITY; rem > 1; rem--) {
      final int curr = (r - i + rem - 1) / rem;
      newMid = newMid.snoc(new InnerNode<>(slice(rs, i, i + curr)));
      i += curr;
    }

    return get(left, newMid, slice(rs, i, r));
  }

  @Override
  public FingerTree<N, E> replaceHead(final Node<N, E> head) {
    final long sizeDiff = head.size() - left[0].size();
    final Node<N, E>[] newLeft = left.clone();
    newLeft[0] = head;
    return new DeepTree<>(newLeft, leftSize + sizeDiff, middle, right, size + sizeDiff);
  }

  @Override
  public FingerTree<N, E> replaceLast(final Node<N, E> last) {
    final int lst = right.length - 1;
    final Node<N, E>[] newRight = right.clone();
    newRight[lst] = last;
    return new DeepTree<>(left, leftSize, middle, newRight, size + last.size()
        - right[lst].size());
  }

  @Override
  void toString(final StringBuilder sb, final int indent) {
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append("Deep(").append(size).append(")[\n");

    // left digit
    for(int i = 0; i < indent + 1; i++) sb.append("  ");
    sb.append("Left(").append(leftSize).append(")[\n");
    for(final Node<N, E> e : left) {
      toString(e, sb, indent + 2);
      sb.append('\n');
    }
    for(int i = 0; i < indent + 1; i++) sb.append("  ");
    sb.append("]\n");

    // middle tree
    middle.toString(sb, indent + 1);
    sb.append('\n');

    // right digit
    for(int i = 0; i < indent + 1; i++) sb.append("  ");
    sb.append("Right[\n");
    for(final Node<N, E> e : right) {
      toString(e, sb, indent + 2);
      sb.append('\n');
    }
    for(int i = 0; i < indent + 1; i++) sb.append("  ");
    sb.append("]\n");

    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append(']');
  }

  @Override
  public long checkInvariants() {
    if(left.length < 1 || left.length > MAX_DIGIT) throw new AssertionError(
        "Wrong left digit length: " + left.length);
    long sz = 0;
    for(final Node<N, E> nd : left)
      sz += nd.checkInvariants();
    if(sz != leftSize) throw new AssertionError("Wrong leftSize: " + leftSize + " vs. " + sz);
    sz += middle.checkInvariants();
    if(right.length < 1 || right.length > MAX_DIGIT) throw new AssertionError(
        "Wrong right digit length: " + right.length);
    for(final Node<N, E> nd : right)
      sz += nd.checkInvariants();
    if(sz != size) throw new AssertionError("Wrong size: " + size + " vs. " + sz);
    return sz;
  }

  /**
   * Calculates the size of a digit.
   * @param <N> node type
   * @param arr digit
   * @return size
   */
  static <N extends Node<?, ?>> long size(final N[] arr) {
    long size = 0;
    for(final N o : arr) size += o.size();
    return size;
  }

  /**
   * Returns an array containing the values at the indices {@code from} to {@code to - 1}
   * in the given array. Its length is always {@code to - from}. If {@code from} is
   * smaller than zero, the first {@code -from} entries in the resulting array are
   * {@code null}. If {@code to > arr.length} then the last {@code to - arr.length}
   * entries are {@code null}.
   * @param <N> node type
   * @param <E> element type
   * @param arr input array
   * @param from first index, inclusive (may be negative)
   * @param to last index, exclusive (may be greater than {@code arr.length})
   * @return resulting array
   */
  static <N, E> Node<N, E>[] slice(final NodeLike<N, E>[] arr, final int from, final int to) {
    @SuppressWarnings("unchecked")
    final Node<N, E>[] out = new Node[to - from];
    final int in0 = Math.max(0, from), in1 = Math.min(to, arr.length);
    final int out0 = Math.max(-from, 0);
    Array.copy(arr, in0, in1 - in0, out, out0);
    return out;
  }
}
