package org.basex.query.util;

import java.util.*;

import org.basex.query.*;

/**
 * Depth-first traversal of a graph that rejects nodes that depend on themselves.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param <T> node type
 */
public abstract class Cycles<T> {
  /** States of the visited nodes ({@code false}: being visited, {@code true}: finished). */
  private final HashMap<T, Boolean> states = new HashMap<>();

  /**
   * Visits a node and all nodes reachable from it.
   * @param node node
   * @throws QueryException query exception
   */
  public final void visit(final T node) throws QueryException {
    final Boolean state = states.putIfAbsent(node, Boolean.FALSE);
    if(state == Boolean.TRUE) return;
    if(state == Boolean.FALSE) throw error(node);
    for(final T next : next(node)) visit(next);
    finish(node);
    states.put(node, Boolean.TRUE);
  }

  /**
   * Returns the nodes that a node directly depends on.
   * @param node node
   * @return nodes
   * @throws QueryException query exception
   */
  protected abstract Iterable<T> next(T node) throws QueryException;

  /**
   * Finishes a node after all nodes it depends on have been finished.
   * @param node node
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  protected void finish(final T node) throws QueryException { }

  /**
   * Returns the error for a node that depends on itself.
   * @param node node
   * @return query exception
   */
  protected abstract QueryException error(T node);
}
