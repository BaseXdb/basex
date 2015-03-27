package org.basex.query.util.fingertree;

/**
 * An inner node containing nested sub-nodes.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 *
 * @param <N> node type
 * @param <E> element type
 */
abstract class InnerNode<N, E> extends Node<Node<N, E>, E> {
  /**
   * Returns a copy of this node's sub-nodes.
   * @return an array containing this node's children
   */
  abstract Node<N, E>[] copyChildren();
}
