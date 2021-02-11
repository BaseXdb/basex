package org.basex.query.util.fingertree;

/**
 * A node inside a digit.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 *
 * @param <N> node type
 * @param <E> element type
 */
public interface Node<N, E> extends NodeLike<N, E> {
  /**
   * Number of elements in this node.
   * @return number of elements
   */
  long size();

  /**
   * Number of children of this node.
   * @return number of children
   */
  int arity();

  /**
   * Returns the sub-node at the given position in this node.
   * @param pos index of the sub-node, must be between 0 and {@link #arity()} - 1
   * @return the sub-node
   */
  N getSub(int pos);

  /**
   * Creates a reversed version of this node.
   * @return a node with the reverse order of contained elements
   */
  Node<N, E> reverse();

  /**
   * Inserts the given element at the given position in this node.
   * The array {@code siblings} is used for input as well as output. It must contain the left and
   * right sibling of this node (if existing) at positions 0 and 2 when calling the method.
   * After the method returns, there are two cases to consider:
   * <ul>
   *   <li>
   *     If the method returned {@code true} (i.e. this node was split), the array contains the
   *     left sibling at position 0, the split node at position 1 and 2 and the right sibling at 3.
   *   </li>
   *   <li>
   *     Otherwise the array contains (possibly modified versions of) left sibling at position 0,
   *     this node at position 1 and the right sibling at position 2.
   *   </li>
   * </ul>
   * @param siblings sibling array for input and output
   * @param pos insertion position
   * @param val value to insert
   * @return {@code true} if the node was split, {@code false} otherwise
   */
  boolean insert(Node<N, E>[] siblings, long pos, E val);

  /**
   * Replaces the element at the given position in this node with the given element.
   * @param pos position
   * @param val new value
   * @return resulting node
   */
  Node<N, E> set(long pos, E val);

  /**
   * Removes the element at the given position in this node.
   * If this node is merged with one of its neighbors,
   * the middle element of the result array is {@code null}.
   * @param l left neighbor, possibly {@code null}
   * @param r right neighbor, possibly {@code null}
   * @param pos position of the element to delete
   * @return three-element array with the new left neighbor, node and right neighbor
   */
  NodeLike<N, E>[] remove(Node<N, E> l, Node<N, E> r, long pos);

  /**
   * Extracts a sub-tree containing the elements at positions {@code off .. off + len - 1}
   * from the tree rooted at this node.
   * This method is only called if {@code len < this.size()} holds.
   * @param off offset of first element
   * @param len number of elements
   * @return the sub-tree, possibly under-full
   */
  NodeLike<N, E> slice(long off, long len);

  /**
   * Checks that this node does not violate any invariants.
   * @return this node's size
   * @throws AssertionError if an invariant was violated
   */
  long checkInvariants();
}
