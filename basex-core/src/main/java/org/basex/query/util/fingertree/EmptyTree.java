package org.basex.query.util.fingertree;

import org.basex.query.*;
import org.basex.util.*;

/**
 * An empty finger tree.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 *
 * @param <N> node type
 * @param <E> element type
 */
final class EmptyTree<N, E> extends FingerTree<N, E> {
  /** The empty finger tree. */
  static final EmptyTree<?, ?> INSTANCE = new EmptyTree<>();

  /**
   * Getter for the empty finger tree.
   * @param <N> node type
   * @param <E> element type
   * @return empty tree
   */
  @SuppressWarnings("unchecked")
  static <N, E> EmptyTree<N, E> getInstance() {
    return (EmptyTree<N, E>) INSTANCE;
  }

  /** Hidden constructor. */
  private EmptyTree() {
  }

  @Override
  public FingerTree<N, E> cons(final Node<N, E> fst) {
    return new SingletonTree<>(fst);
  }

  @Override
  public FingerTree<N, E> snoc(final Node<N, E> lst) {
    return new SingletonTree<>(lst);
  }

  @Override
  public Node<N, E> head() {
    throw Util.notExpected();
  }

  @Override
  public Node<N, E> last() {
    throw Util.notExpected();
  }

  @Override
  public FingerTree<N, E> init() {
    throw Util.notExpected();
  }

  @Override
  public FingerTree<N, E> tail() {
    throw Util.notExpected();
  }

  @Override
  public FingerTree<N, E> set(final long pos, final E val) {
    throw Util.notExpected();
  }

  @Override
  public long size() {
    return 0;
  }

  @Override
  public FingerTree<N, E> concat(final Node<N, E>[] nodes, final long size,
      final FingerTree<N, E> other) {
    return other.addAll(nodes, size, true);
  }

  @Override
  public TreeSlice<N, E> slice(final long pos, final long len) {
    return new TreeSlice<>(this);
  }

  @Override
  public FingerTree<N, E> reverse(final QueryContext qc) {
    return this;
  }

  @Override
  public FingerTree<N, E> insert(final long pos, final E val, final QueryContext qc) {
    throw Util.notExpected();
  }

  @Override
  public TreeSlice<N, E> remove(final long pos, final QueryContext qc) {
    throw Util.notExpected();
  }

  @Override
  public FingerTree<N, E> replaceHead(final Node<N, E> head) {
    throw Util.notExpected();
  }

  @Override
  public FingerTree<N, E> replaceLast(final Node<N, E> last) {
    throw Util.notExpected();
  }

  @Override
  void toString(final StringBuilder sb, final int indent) {
    for(int i = 0; i < indent; i++) sb.append("  ");
    sb.append("Empty[]");
  }

  @Override
  public long checkInvariants() {
    return 0;
  }

  @Override
  FingerTree<N, E> addAll(final Node<N, E>[] nodes, final long size, final boolean left) {
    return buildTree(nodes, nodes.length, size);
  }
}
