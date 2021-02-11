package org.basex.query.util.fingertree;

import org.basex.query.*;

/**
 * A tree consisting of a single value.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 *
 * @param <N> node type
 * @param <E> element type
 */
final class SingletonTree<N, E> extends FingerTree<N, E> {
  /** The element. */
  final Node<N, E> elem;

  /**
   * Constructor.
   * @param elem element
   */
  SingletonTree(final Node<N, E> elem) {
    this.elem = elem;
  }

  @Override
  public DeepTree<N, E> cons(final Node<N, E> fst) {
    final long leftSize = fst.size();
    @SuppressWarnings("unchecked")
    final Node<N, E>[] left = new Node[] { fst }, right = new Node[] { elem };
    return DeepTree.get(left, leftSize, right, leftSize + elem.size());
  }

  @Override
  public DeepTree<N, E> snoc(final Node<N, E> lst) {
    final long leftSize = elem.size();
    @SuppressWarnings("unchecked")
    final Node<N, E>[] left = new Node[] { elem }, right = new Node[] { lst };
    return DeepTree.get(left, leftSize, right, leftSize + lst.size());
  }

  @Override
  public Node<N, E> head() {
    return elem;
  }

  @Override
  public Node<N, E> last() {
    return elem;
  }

  @Override
  public FingerTree<N, E> init() {
    return EmptyTree.getInstance();
  }

  @Override
  public FingerTree<N, E> tail() {
    return EmptyTree.getInstance();
  }

  @Override
  public long size() {
    return elem.size();
  }

  @Override
  public FingerTree<N, E> concat(final Node<N, E>[] mid, final long size,
      final FingerTree<N, E> other) {
    return other.isEmpty() ? addAll(mid, size, false) : other.addAll(mid, size, true).cons(elem);
  }

  @Override
  public FingerTree<N, E> reverse(final QueryContext qc) {
    return new SingletonTree<>(elem.reverse());
  }

  @Override
  public FingerTree<N, E> set(final long pos, final E val) {
    return new SingletonTree<>(elem.set(pos, val));
  }

  @Override
  public FingerTree<N, E> insert(final long pos, final E val, final QueryContext qc) {
    @SuppressWarnings("unchecked")
    final Node<N, E>[] siblings = new Node[4];
    if(!elem.insert(siblings, pos, val)) {
      // node was not split
      return new SingletonTree<>(siblings[1]);
    }

    final Node<N, E> l = siblings[1], r = siblings[2];
    @SuppressWarnings("unchecked")
    final Node<N, E>[] left = new Node[] { l }, right = new Node[] { r };
    return DeepTree.get(left, l.size(), right, elem.size() + 1);
  }

  @Override
  public TreeSlice<N, E> remove(final long pos, final QueryContext qc) {
    final NodeLike<N, E>[] removed = elem.remove(null, null, pos);
    return new TreeSlice<>(removed[1]);
  }

  @Override
  public TreeSlice<N, E> slice(final long pos, final long len) {
    if(pos == 0 && len == elem.size()) return new TreeSlice<>(this);
    return new TreeSlice<>(elem.slice(pos, len));
  }

  @Override
  FingerTree<N, E> addAll(final Node<N, E>[] nodes, final long size, final boolean left) {
    if(nodes.length == 0) return this;
    if(nodes.length <= MAX_DIGIT) {
      @SuppressWarnings("unchecked")
      final Node<N, E>[] arr = new Node[] { elem };
      return left ? DeepTree.get(nodes, arr) : DeepTree.get(arr, nodes);
    }

    final FingerTree<N, E> tree = buildTree(nodes, nodes.length, size);
    return left ? tree.snoc(elem) : tree.cons(elem);
  }

  @Override
  public FingerTree<N, E> replaceHead(final Node<N, E> head) {
    return new SingletonTree<>(head);
  }

  @Override
  public FingerTree<N, E> replaceLast(final Node<N, E> head) {
    return new SingletonTree<>(head);
  }

  @Override
  void toString(final StringBuilder sb, final int indent) {
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append("Single[\n");
    toString(elem, sb, indent + 1);
    sb.append('\n');
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append(']');
  }

  @Override
  public long checkInvariants() {
    return elem.checkInvariants();
  }
}
