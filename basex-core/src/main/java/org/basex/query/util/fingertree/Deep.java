package org.basex.query.util.fingertree;

/**
 * A <i>deep</i> node containing elements in the left and right digit and a sub-tree in
 * the middle.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 *
 * @param <N> node type
 * @param <E> element type
 */
final class Deep<N, E> extends FingerTree<N, E> {
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
  Deep(final Node<N, E>[] left, final long leftSize,
      final FingerTree<Node<N, E>, E> middle, final Node<N, E>[] right, final long size) {
    this.left = left;
    this.leftSize = leftSize;
    this.middle = middle;
    this.right = right;
    this.size = size;
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
  static <N, E> Deep<N, E> get(final Node<N, E>[] left, final FingerTree<Node<N, E>, E> middle,
      final Node<N, E>[] right, final long size) {
    return new Deep<>(left, size(left), middle, right, size);
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
  static <N, E> Deep<N, E> get(final Node<N, E>[] left, final long leftSize,
      final Node<N, E>[] right, final long size) {
    return new Deep<>(left, leftSize, Empty.<Node<N, E>, E>getInstance(), right, size);
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
  static <N, E> Deep<N, E> get(final Node<N, E>[] left, final Node<N, E>[] right, final long size) {
    return new Deep<>(left, size(left), Empty.<Node<N, E>, E>getInstance(), right, size);
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
  static <N, E> Deep<N, E> get(final Node<N, E>[] left,
      final FingerTree<Node<N, E>, E> middle, final Node<N, E>[] right) {
    final long l = size(left), m = middle.size(), r = size(right);
    return new Deep<>(left, l, middle, right, l + m + r);
  }

  /**
   * Factory method for deep nodes with empty middle tree calculating all cached sizes.
   * @param <N> node type
   * @param <E> element type
   * @param left left digit
   * @param right right digit
   * @return the deep node
   */
  static <N, E> Deep<N, E> get(final Node<N, E>[] left, final Node<N, E>[] right) {
    final long l = size(left), r = size(right);
    return new Deep<>(left, l, Empty.<Node<N, E>, E>getInstance(), right, l + r);
  }

  @Override
  public Deep<N, E> cons(final Node<N, E> fst) {
    if(left.length < 4) {
      final Node<N, E>[] newLeft = slice(left, -1, left.length);
      newLeft[0] = fst;
      return new Deep<>(newLeft, leftSize + fst.size(), middle, right, size + fst.size());
    }

    final Node<N, E>[] newLeft = slice(left, -1, 1);
    newLeft[0] = fst;
    final FingerTree<Node<N, E>, E> mid = middle.cons(new InnerNode3<>(left[1], left[2], left[3]));
    return Deep.get(newLeft, mid, right, size + fst.size());
  }

  @Override
  public Deep<N, E> snoc(final Node<N, E> lst) {
    if(right.length < 4) {
      final Node<N, E>[] newRight = slice(right, 0, right.length + 1);
      newRight[right.length] = lst;
      return new Deep<>(left, leftSize, middle, newRight, size + lst.size());
    }

    final FingerTree<Node<N, E>, E> mid =
        middle.snoc(new InnerNode3<>(right[0], right[1], right[2]));
    final Node<N, E>[] newRight = slice(right, 3, 5);
    newRight[1] = lst;
    return new Deep<>(left, leftSize, mid, newRight, size + lst.size());
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
      return new Deep<>(left, leftSize, middle, slice(right, 0, right.length - 1), newSize);
    }

    if(middle.isEmpty()) {
      // middle tree empty, make a tree from the left list
      if(left.length == 1) return new Single<>(left[0]);

      final int mid = left.length / 2;
      final Node<N, E>[] newLeft = slice(left, 0, mid);
      return Deep.get(newLeft, slice(left, mid, left.length), newSize);
    }

    // extract values for the right digit from the middle
    final InnerNode<N, E> last = (InnerNode<N, E>) middle.last();
    return new Deep<>(left, leftSize, middle.init(), last.copyChildren(), newSize);
  }

  @Override
  public FingerTree<N, E> tail() {
    final long fstSize = left[0].size();
    final long newSize = size - fstSize;

    if(left.length > 1) {
      // left digit is safe, just shrink it
      return new Deep<>(slice(left, 1, left.length), leftSize - fstSize, middle, right, newSize);
    }

    if(middle.isEmpty()) {
      // middle tree empty, make a tree from the right list
      if(right.length == 1) return new Single<>(right[0]);

      final int mid = right.length / 2;
      return Deep.get(slice(right, 0, mid), slice(right, mid, right.length), newSize);
    }

    // extract values for the left digit from the middle
    final InnerNode<N, E> head = (InnerNode<N, E>) middle.head();
    return new Deep<>(head.copyChildren(), head.size(), middle.tail(), right, newSize);
  }

  @Override
  public long size() {
    return this.size;
  }

  @Override
  public Deep<N, E> concat(final Node<N, E>[] nodes, final FingerTree<N, E> other) {
    final Deep<N, E> lft = (Deep<N, E>) addAll(nodes, false);
    if(!(other instanceof Deep)) {
      return other instanceof Single ? lft.snoc(((Single<N, E>) other).elem) : lft;
    }

    final Deep<N, E> rght = (Deep<N, E>) other;
    final Node<N, E>[] as = lft.right, bs = rght.left;
    final int l = as.length, n = l + bs.length, k = (n + 2) / 3;
    @SuppressWarnings("unchecked")
    final Node<Node<N, E>, E>[] out = new Node[k];
    int p = 0;
    for(int i = 0; i < k; i++) {
      final int rest = n - p;
      final Node<N, E> x = p < l ? as[p] : bs[p - l];
      p++;
      final Node<N, E> y = p < l ? as[p] : bs[p - l];
      p++;
      if(rest > 4 || rest == 3) {
        final Node<N, E> z = p < l ? as[p] : bs[p - l];
        out[i] = new InnerNode3<>(x, y, z);
        p++;
      } else {
        out[i] = new InnerNode2<>(x, y);
      }
    }

    final FingerTree<Node<N, E>, E> newMid = lft.middle.concat(out, rght.middle);
    final long newSize = lft.leftSize + newMid.size() + rght.rightSize();
    return new Deep<>(lft.left, lft.leftSize, newMid, rght.right, newSize);
  }

  @Override
  public FingerTree<N, E> reverse() {
    final int l = left.length, r = right.length;
    @SuppressWarnings("unchecked")
    final Node<N, E>[] newLeft = new Node[r], newRight = new Node[l];
    for(int i = 0; i < r; i++) newLeft[i] = right[r - 1 - i].reverse();
    for(int i = 0; i < l; i++) newRight[i] = left[l - 1 - i].reverse();
    return new Deep<>(newLeft, rightSize(), middle.reverse(), newRight, size);
  }

  @Override
  public FingerTree<N, E> insert(final long pos, final E val) {
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
        return new Deep<>(newLeft, leftSize + 1, middle, right, size + 1);
      }

      // node was split
      @SuppressWarnings("unchecked")
      final Node<N, E>[] temp = new Node[ll + 1];
      if(i > 0) {
        System.arraycopy(left, 0, temp, 0, i - 1);
        temp[i - 1] = siblings[0];
      }
      temp[i] = siblings[1];
      temp[i + 1] = siblings[2];
      if(i + 1 < ll) {
        temp[i + 2] = siblings[3];
        System.arraycopy(left, i + 2, temp, i + 3, ll - i - 2);
      }
      if(ll < 4) return new Deep<>(temp, leftSize + 1, middle, right, size + 1);

      // digit has to be split
      final Node<N, E> a = temp[0], b = temp[1];
      @SuppressWarnings("unchecked")
      final Node<N, E>[] newLeft = new Node[] { a, b };
      final Node<Node<N, E>, E> sub = new InnerNode3<>(temp[2], temp[3], temp[4]);
      return new Deep<>(newLeft, a.size() + b.size(), middle.cons(sub), right, size + 1);
    }

    long p = pos - leftSize;
    final long midSize = middle.size();
    if(p < midSize) return new Deep<>(left, leftSize, middle.insert(p, val), right, size + 1);

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
      return new Deep<>(left, leftSize, middle, newRight, size + 1);
    }

    // node was split
    @SuppressWarnings("unchecked")
    final Node<N, E>[] temp = new Node[rl + 1];
    if(i > 0) {
      System.arraycopy(right, 0, temp, 0, i - 1);
      temp[i - 1] = siblings[0];
    }
    temp[i] = siblings[1];
    temp[i + 1] = siblings[2];
    if(i + 1 < rl) {
      temp[i + 2] = siblings[3];
      System.arraycopy(right, i + 2, temp, i + 3, rl - i - 2);
    }
    if(right.length < 4) return new Deep<>(left, leftSize, middle, temp, size + 1);

    // digit has to be split
    final Node<Node<N, E>, E> sub = new InnerNode3<>(temp[0], temp[1], temp[2]);
    final Node<N, E> a = temp[3], b = temp[4];
    @SuppressWarnings("unchecked")
    final Node<N, E>[] newRight = new Node[] { a, b};
    return new Deep<>(left, leftSize, middle.snoc(sub), newRight, size + 1);
  }

  @Override
  public TreeSlice<N, E> remove(final long pos) {
    if(pos < leftSize) return new TreeSlice<>(removeLeft(pos));
    final long rightStart = leftSize + middle.size();
    if(pos >= rightStart) return new TreeSlice<>(removeRight(pos - rightStart));

    final TreeSlice<Node<N, E>, E> slice = middle.remove(pos - leftSize);
    if(slice.isTree()) {
      // no underflow
      final FingerTree<Node<N, E>, E> newMiddle = slice.getTree();
      return slice.setTree(new Deep<>(left, leftSize, newMiddle, right, size - 1));
    }

    // middle tree had an underflow, one sub-node left
    final Node<N, E> node = (Node<N, E>) ((PartialInnerNode<N, E>) slice.getPartial()).sub;

    // try to extend the smaller digit
    if(left.length < right.length) {
      // merge into left digit
      final Node<N, E>[] newLeft = slice(left, 0, left.length + 1);
      newLeft[left.length] = node;
      return slice.setTree(Deep.get(newLeft, leftSize + node.size(), right, size - 1));
    }

    if(right.length < 4) {
      // merge into right digit
      @SuppressWarnings("unchecked")
      final Node<N, E>[] newRight = new Node[right.length + 1];
      newRight[0] = node;
      System.arraycopy(right, 0, newRight, 1, right.length);
      return slice.setTree(Deep.get(left, leftSize, newRight, size - 1));
    }

    // redistribute the 9 nodes
    final Node<N, E>[] newLeft = slice(left, 0, 3);
    final Node<Node<N, E>, E> newMid = new InnerNode3<>(left[3], node, right[0]);
    final Node<N, E>[] newRight = slice(right, 1, right.length);
    return slice.setTree(Deep.get(newLeft, new Single<>(newMid), newRight, size - 1));
  }

  /**
   * Remove an element from the left digit.
   * @param pos position inside the left digit
   * @return resulting tree
   */
  private FingerTree<N, E> removeLeft(final long pos) {
    if(left.length > 1) {
      // left digit cannot underflow, just delete the element
      return new Deep<>(remove(left, pos), leftSize - 1, middle, right, size - 1);
    }

    // singleton digit might underflow
    final Node<N, E> node = left[0];

    if(!middle.isEmpty()) {
      // next node for balancing is in middle tree
      final InnerNode<N, E> head = (InnerNode<N, E>) middle.head();
      final Node<N, E> first = head.getSub(0);
      final Node<N, E>[] rem = node.remove(null, first, pos);
      final Node<N, E> newNode = rem[1], newFirst = rem[2];

      if(newNode == null) {
        // nodes were merged
        final Node<N, E>[] newLeft = head.copyChildren();
        newLeft[0] = newFirst;
        return Deep.get(newLeft, middle.tail(), right, size - 1);
      }

      @SuppressWarnings("unchecked")
      final Node<N, E>[] newLeft = new Node[] { newNode };

      if(newFirst != first) {
        // nodes were balanced
        final FingerTree<Node<N, E>, E> newMid = middle.replaceHead(head.replaceFirst(newFirst));
        return new Deep<>(newLeft, newNode.size(), newMid, right, size - 1);
      }

      // no changes to this tree's structure
      return new Deep<>(newLeft, newNode.size(), middle, right, size - 1);
    }

    // potentially balance with right digit
    final Node<N, E>[] rem = node.remove(null, right[0], pos);
    final Node<N, E> newNode = rem[1], newFirstRight = rem[2];

    if(newNode == null) {
      // nodes were merged
      if(right.length == 1) return new Single<>(newFirstRight);
      final int mid = right.length / 2;
      final Node<N, E>[] newLeft = slice(right, 0, mid);
      newLeft[0] = newFirstRight;
      return Deep.get(newLeft, middle, slice(right, mid, right.length), size - 1);
    }

    // structure does not change
    @SuppressWarnings("unchecked")
    final Node<N, E>[] newLeft = new Node[] { newNode };

    if(newFirstRight == right[0]) {
      // right digit stays the same
      return new Deep<>(newLeft, newLeft[0].size(), middle, right, size - 1);
    }

    // adapt the right digit
    final Node<N, E>[] newRight = right.clone();
    newRight[0] = newFirstRight;
    return new Deep<>(newLeft, newNode.size(), middle, newRight, size - 1);
  }

  /**
   * Remove an element from the right digit.
   * @param pos position inside the right digit
   * @return resulting tree
   */
  private FingerTree<N, E> removeRight(final long pos) {
    if(right.length > 1) {
      // right digit cannot underflow, just delete the element
      return new Deep<>(left, leftSize, middle, remove(right, pos), size - 1);
    }

    // singleton digit might underflow
    final Node<N, E> node = right[0];

    if(!middle.isEmpty()) {
      // potentially balance with middle tree
      final InnerNode<N, E> last = (InnerNode<N, E>) middle.last();
      final Node<N, E> lastSub = last.getSub(last.arity() - 1);
      final Node<N, E>[] rem = node.remove(lastSub, null, pos);
      final Node<N, E> newLastSub = rem[0], newNode = rem[1];

      if(newNode == null) {
        // nodes were merged
        final Node<N, E>[] newRight = last.copyChildren();
        newRight[newRight.length - 1] = newLastSub;
        return new Deep<>(left, leftSize, middle.init(), newRight, size - 1);
      }

      @SuppressWarnings("unchecked")
      final Node<N, E>[] newRight = new Node[] { newNode };

      // replace last node in middle tree
      final Node<Node<N, E>, E> newLast = last.replaceLast(newLastSub);
      return new Deep<>(left, leftSize, middle.replaceLast(newLast), newRight, size - 1);
    }

    // balance with left digit
    final Node<N, E> lastLeft = left[left.length - 1];
    final Node<N, E>[] rem = node.remove(lastLeft, null, pos);
    final Node<N, E> newLastLeft = rem[0], newNode = rem[1];
    if(newNode == null) {
      // nodes were merged
      if(left.length == 1) {
        // only one node left
        return new Single<>(newLastLeft);
      }

      @SuppressWarnings("unchecked")
      final Node<N, E>[] newRight = new Node[] { newLastLeft };
      return Deep.get(slice(left, 0, left.length - 1), newRight, size - 1);
    }

    @SuppressWarnings("unchecked")
    final Node<N, E>[] newRight = new Node[] { newNode };

    if(newLastLeft == lastLeft) {
      // deletion could be absorbed
      return Deep.get(left, leftSize, newRight, size - 1);
    }

    // adapt the left digit
    final Node<N, E>[] newLeft = left.clone();
    newLeft[newLeft.length - 1] = newLastLeft;
    return Deep.get(newLeft, newRight, size - 1);
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
    for(;;) {
      node = arr[i];
      final long nodeSize = node.size();
      if(off < nodeSize) break;
      off -= nodeSize;
      i++;
    }

    final int n = arr.length;
    final Node<N, E>[] res = arr[i].remove(i == 0     ? null : arr[i - 1],
                                           i == n - 1 ? null : arr[i + 1], off);
    if(res[1] != null) {
      // same number of nodes
      final Node<N, E>[] out = arr.clone();
      if(i > 0) out[i - 1] = res[0];
      out[i] = res[1];
      if(i < n - 1) out[i + 1] = res[2];
      return out;
    }

    // the node was merged
    @SuppressWarnings("unchecked")
    final Node<N, E>[] out = new Node[n - 1];
    if(i > 0) {
      // nodes to the left
      System.arraycopy(arr, 0, out, 0, i - 1);
      out[i - 1] = res[0];
    }

    if(i < n - 1) {
      // nodes to the right
      out[i] = res[2];
      System.arraycopy(arr, i + 2, out, i + 1, n - i - 2);
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
    final NodeLike<N, E>[] buffer = new NodeLike[9];
    int inBuffer = splitDigit(left, from, inLeft, buffer, 0);
    if(inLeft == len) {
      final int n = inBuffer;
      if(n == 1) {
        if(buffer[0] instanceof PartialNode) return new TreeSlice<>((PartialNode<N, E>) buffer[0]);
        final Node<N, E> node = (Node<N, E>) buffer[0];
        return new TreeSlice<>(new Single<>(node));
      }

      final int mid1 = n / 2;
      final Node<N, E>[] ls = Deep.slice(buffer, 0, mid1), rs = Deep.slice(buffer, mid1, n);
      return new TreeSlice<>(Deep.get(ls, rs, len));
    }

    final long inMiddle = len - inLeft - inRight;
    final FingerTree<Node<N, E>, E> mid;
    final TreeSlice<Node<N, E>, E> slice;
    if(inMiddle == 0) {
      mid = Empty.getInstance();
      slice = new TreeSlice<>(mid);
    } else {
      final long midOff = from <= leftSize ? 0 : from - leftSize;
      slice = middle.slice(midOff, inMiddle);
      if(!slice.isTree()) {
        final NodeLike<N, E> sub = ((PartialInnerNode<N, E>) slice.getPartial()).sub;
        inBuffer = sub.append(buffer, inBuffer);
        mid = Empty.getInstance();
      } else {
        mid = slice.getTree();
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
      newRight = ((InnerNode<N, E>) mid2.last()).copyChildren();
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

    return slice.setTree(Deep.get(newLeft, mid3, newRight, len));
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
  FingerTree<N, E> addAll(final Node<N, E>[] nodes, final boolean appendLeft) {
    final int k = nodes.length;
    if(k == 0) return this;
    if(k == 1) return appendLeft ? cons(nodes[0]) : snoc(nodes[0]);

    if(appendLeft) {
      int l = k + left.length;
      final Node<N, E>[] ls = slice(nodes, 0, l);
      System.arraycopy(left, 0, ls, k, left.length);
      if(l <= 4) return Deep.get(ls, middle, right);
      FingerTree<Node<N, E>, E> newMid = middle;
      for(; l > 4; l -= 3) {
        final InnerNode<N, E> sub = new InnerNode3<>(ls[l - 3], ls[l - 2], ls[l - 1]);
        newMid = newMid.cons(sub);
      }
      return Deep.get(slice(ls, 0, l), newMid, right);
    }

    final int r = right.length + k;
    final Node<N, E>[] rs = slice(right, 0, r);
    System.arraycopy(nodes, 0, rs, right.length, k);
    if(k + right.length <= 4) return Deep.get(left, middle, rs);
    FingerTree<Node<N, E>, E> newMid = middle;
    int i = 0;
    for(; r - i > 4; i += 3) {
      final InnerNode<N, E> sub = new InnerNode3<>(rs[i], rs[i + 1], rs[i + 2]);
      newMid = newMid.snoc(sub);
    }
    return Deep.get(left, newMid, slice(rs, i, r));
  }

  @Override
  public FingerTree<N, E> replaceHead(final Node<N, E> head) {
    final long sizeDiff = head.size() - left[0].size();
    final Node<N, E>[] newLeft = left.clone();
    newLeft[0] = head;
    return new Deep<>(newLeft, leftSize + sizeDiff, middle, right, size + sizeDiff);
  }

  @Override
  public FingerTree<N, E> replaceLast(final Node<N, E> last) {
    final int lst = right.length - 1;
    final Node<N, E>[] newRight = right.clone();
    newRight[lst] = last;
    return new Deep<>(left, leftSize, middle, newRight, size + last.size()
        - right[lst].size());
  }

  @Override
  void toString(final StringBuilder sb, final int indent) {
    for(int i = 0; i < indent; i++)
      sb.append("  ");
    sb.append("Deep[\n");

    // left digit
    for(int i = 0; i < indent + 1; i++)
      sb.append("  ");
    sb.append("Left[\n");
    for(final Node<N, E> e : left) {
      e.toString(sb, indent + 2);
      sb.append('\n');
    }
    for(int i = 0; i < indent + 1; i++)
      sb.append("  ");
    sb.append("]\n");

    // middle tree
    middle.toString(sb, indent + 1);
    sb.append('\n');

    // right digit
    for(int i = 0; i < indent + 1; i++)
      sb.append("  ");
    sb.append("Right[\n");
    for(final Node<N, E> e : right) {
      e.toString(sb, indent + 2);
      sb.append("\n");
    }
    for(int i = 0; i < indent + 1; i++)
      sb.append("  ");
    sb.append("]\n");

    for(int i = 0; i < indent; i++)
      sb.append("  ");
    sb.append("]");
  }

  @Override
  public long checkInvariants() {
    if(left.length < 1 || left.length > 4) throw new AssertionError(
        "Wrong left digit length: " + left.length);
    long sz = 0;
    for(final Node<N, E> nd : left)
      sz += nd.checkInvariants();
    if(sz != leftSize) throw new AssertionError("Wrong leftSize: " + leftSize + " vs. "
        + sz);
    sz += middle.checkInvariants();
    if(right.length < 1 || right.length > 4) throw new AssertionError(
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
    for(final N o : arr)
      size += o.size();
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
    System.arraycopy(arr, in0, out, out0, in1 - in0);
    return out;
  }

  @Override
  public long[] sizes(final int depth) {
    final long[] sizes = middle.sizes(depth + 1);
    sizes[depth] = size - middle.size();
    return sizes;
  }
}
