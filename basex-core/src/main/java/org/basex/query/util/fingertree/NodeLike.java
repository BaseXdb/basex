package org.basex.query.util.fingertree;

/**
 * A possibly partial node of a finger tree.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 *
 * @param <N> node type
 * @param <E> element type
 */
public interface NodeLike<N, E> {
  /**
   * Appends this possibly partial node to the given buffer.
   * @param nodes the buffer
   * @param pos number of nodes in the buffer
   * @return new number of nodes
   */
  int append(NodeLike<N, E>[] nodes, int pos);
}
