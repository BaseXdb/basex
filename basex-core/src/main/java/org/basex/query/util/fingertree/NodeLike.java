package org.basex.query.util.fingertree;

/**
 * A possibly partial node of a finger tree.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 *
 * @param <N> node type
 * @param <E> element type
 */
public abstract class NodeLike<N, E> {
  /** Default constructor for package visibility. */
  NodeLike() {
  }

  /**
   * Concatenates this possibly partial node with the given one.
   * This method is allowed to re-use the result array from a recursive call for performance.
   * @param other the other node-like object
   * @return a two-element array containing the one or two resulting node-like objects
   */
  protected abstract NodeLike<N, E>[] concat(final NodeLike<N, E> other);

  /**
   * Appends this possibly partial node to the given buffer.
   * @param nodes the buffer
   * @param pos number of nodes in the buffer
   * @return new number of nodes
   */
  protected abstract int append(final NodeLike<N, E>[] nodes, final int pos);

  /**
   * Recursive helper method for {@link #toString()}.
   * @param sb string builder
   * @param indent indentation depth
   */
  protected abstract void toString(final StringBuilder sb, final int indent);

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    toString(sb, 0);
    return sb.toString();
  }
}
