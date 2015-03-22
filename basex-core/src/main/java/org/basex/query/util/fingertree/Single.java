package org.basex.query.util.fingertree;

import java.util.*;

/**
 * A tree consisting of a single value.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 *
 * @param <N> node type
 * @param <E> element type
 */
final class Single<N, E> extends FingerTree<N, E> {
  /** The element. */
  final Node<N, E> elem;

  /**
   * Constructor.
   * @param elem element
   */
  public Single(final Node<N, E> elem) {
    this.elem = elem;
  }

  @Override
  public Deep<N, E> cons(final Node<N, E> fst) {
    final long leftSize = fst.size();
    @SuppressWarnings("unchecked")
    final Node<N, E>[] left = new Node[] { fst }, right = new Node[] { elem };
    return Deep.get(left, leftSize, right, leftSize + elem.size());
  }

  @Override
  public Deep<N, E> snoc(final Node<N, E> lst) {
    final long leftSize = elem.size();
    @SuppressWarnings("unchecked")
    final Node<N, E>[] left = new Node[] { elem }, right = new Node[] { lst };
    return Deep.get(left, leftSize, right, leftSize + lst.size());
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
    return Empty.getInstance();
  }

  @Override
  public FingerTree<N, E> tail() {
    return Empty.getInstance();
  }

  @Override
  public long size() {
    return elem.size();
  }

  @Override
  public FingerTree<N, E> concat(final Node<N, E>[] mid, final FingerTree<N, E> other) {
    return other.isEmpty() ? addAll(mid, false) : other.addAll(mid, true).cons(elem);
  }

  @Override
  public FingerTree<N, E> reverse() {
    return new Single<>(elem.reverse());
  }

  @Override
  public FingerTree<N, E> insert(final long pos, final E val) {
    @SuppressWarnings("unchecked")
    final Node<N, E>[] siblings = new Node[4];
    if(!elem.insert(siblings, pos, val)) {
      // node was not split
      return new Single<>(siblings[1]);
    }

    final Node<N, E> l = siblings[1], r = siblings[2];
    @SuppressWarnings("unchecked")
    final Node<N, E>[] left = new Node[] { l }, right = new Node[] { r };
    return Deep.get(left, l.size(), right, elem.size() + 1);
  }

  @Override
  public TreeSlice<N, E> remove(final long pos) {
    NodeLike<N, E> removed = elem.remove(pos);
    return removed instanceof Node ? new TreeSlice<>(new Single<>((Node<N, E>) removed))
                                   : new TreeSlice<>((PartialNode<N, E>) removed);
  }

  @Override
  public TreeSlice<N, E> slice(final long pos, final long len) {
    if(pos == 0 && len == elem.size()) return new TreeSlice<>(this);
    final NodeLike<N, E> sub = elem.slice(pos, len);
    if(sub instanceof Node) return new TreeSlice<>(new Single<>((Node<N, E>) sub));
    return new TreeSlice<>((PartialNode<N, E>) sub);
  }

  @Override
  public ListIterator<E> listIterator(final boolean reverse) {
    return elem.listIterator(reverse);
  }

  @Override
  FingerTree<N, E> addAll(final Node<N, E>[] nodes, final boolean left) {
    if(nodes.length == 0) return this;
    if(nodes.length <= 4) {
      @SuppressWarnings("unchecked")
      final Node<N, E>[] arr = new Node[] { elem };
      return Deep.get(left ? nodes : arr, left ? arr : nodes);
    }

    final FingerTree<N, E> tree = buildTree(nodes, nodes.length, Deep.size(nodes));
    return left ? tree.snoc(elem) : tree.cons(elem);
  }

  @Override
  public FingerTree<N, E> replaceHead(final Node<N, E> head) {
    return new Single<>(head);
  }

  @Override
  public FingerTree<N, E> replaceLast(final Node<N, E> head) {
    return new Single<>(head);
  }

  @Override
  void toString(final StringBuilder sb, final int indent) {
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append("Single[\n");
    elem.toString(sb, indent + 1);
    sb.append("\n");
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append("]");
  }

  @Override
  public long checkInvariants() {
    return elem.checkInvariants();
  }

  @Override
  public long[] sizes(final int depth) {
    final long[] sizes = new long[depth + 1];
    sizes[depth] = elem.size();
    return sizes;
  }
}
