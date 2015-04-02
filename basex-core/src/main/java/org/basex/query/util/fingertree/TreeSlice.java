package org.basex.query.util.fingertree;

/**
 * A slice of a finger tree, used as internal representation
 * for {@link FingerTree#slice(long, long)}.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 *
 * @param <N> node type
 * @param <E> element type
 */
public final class TreeSlice<N, E> {
  /** A full sub-tree. */
  private FingerTree<N, E> tree;
  /** A partial node. */
  private PartialNode<N, E> partial;

  /**
   * Constructor for whole trees.
   * @param tree the tree
   */
  TreeSlice(final FingerTree<N, E> tree) {
    this.tree = tree;
    this.partial = null;
  }

  /**
   * Constructor for partial nodes.
   * @param partial partial node
   */
  TreeSlice(final PartialNode<N, E> partial) {
    this.tree = null;
    this.partial = partial;
  }

  /**
   * Checks if this slice contains a full finger tree.
   * @return {@code true} if this slice contains a full tree,
   *     {@code false} if it contains a partial node
   */
  public boolean isTree() {
    return tree != null;
  }

  /**
   * Getter for a contained full tree, should only be called if {@link #isTree()}
   * returns {@code true}.
   * @return the contained tree
   */
  public FingerTree<N, E> getTree() {
    return tree;
  }

  /**
   * Getter for a contained partial node, should only be called if {@link #isTree()}
   * returns {@code false}.
   * @return the contained partial node
   */
  public PartialNode<N, E> getPartial() {
    return partial;
  }

  /**
   * Sets the contents of this slice to the given tree and returns it with the correct type.
   * The value with the current type is invalid afterwards and should <i>not</i> be used.
   * @param <M> new node type
   * @param newTree the new contents
   * @return type-cast version of this slice
   */
  <M> TreeSlice<M, E> setTree(final FingerTree<M, E> newTree) {
    @SuppressWarnings("unchecked")
    final TreeSlice<M, E> out = (TreeSlice<M, E>) this;
    out.partial = null;
    out.tree = newTree;
    return out;
  }

  /**
   * Sets the contents of this slice to the given partial node and returns it with the correct type.
   * The value with the current type is invalid afterwards and should <i>not</i> be used.
   * @param <M> new node type
   * @param newPartial new contents
   * @return type-cast version of this slice
   */
  <M> TreeSlice<M, E> setPartial(final PartialNode<M, E> newPartial) {
    @SuppressWarnings("unchecked")
    final TreeSlice<M, E> out = (TreeSlice<M, E>) this;
    out.partial = newPartial;
    out.tree = null;
    return out;
  }

  /**
   * Populates this slice with the contents of the given node buffer.
   * @param <M> node type
   * @param arr node buffer
   * @param n number of nodes in the buffer
   * @param size number of elements in the buffer
   * @return type-cast version of this slice
   */
  <M> TreeSlice<M, E> setNodes(final NodeLike<M, E>[] arr, final int n, final long size) {
    if(n == 1) {
      if(arr[0] instanceof PartialNode) return setPartial((PartialNode<M, E>) arr[0]);
      final Node<M, E> node = (Node<M, E>) arr[0];
      return setTree(new SingletonTree<>(node));
    }

    final int mid = n / 2;
    final Node<M, E>[] left = DeepTree.slice(arr, 0, mid), right = DeepTree.slice(arr, mid, n);
    return setTree(DeepTree.get(left, right, size));
  }
}
