package org.basex.query.up;

import org.basex.query.QueryException;
import org.basex.query.up.primitives.PrimitiveType;
import org.basex.query.up.primitives.Primitive;

/**
 * Interface for a container that holds all update primitives for a specific
 * database node. The {@link Primitive} are ordered after their
 * {@link PrimitiveType}. There is at most one {@link Primitive} for each
 * type. Duplicate {@link Primitive} are merged.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
interface NodePrimitives extends Iterable<Primitive> {
  /**
   * Adds a primitive to the container.
   * @param p primitive to be added
   * @throws QueryException query exception
   */
  void add(final Primitive p) throws QueryException;

  /**
   * Finds a primitive with the given type. {@code null} is returned if there
   * is no primitive of type {@code t}.
   * @param t type of the primitive to find
   * @return primitive of type t or {@code null} if not found
   */
  Primitive find(final PrimitiveType t);

  /**
   * Optimizes accumulated update operations for the specific target node.
   * Unnecessary operations are deleted. I.e. if the corresponding target is
   * deleted, all other operations on this node have no effect at all.
   */
  void optimize();

  /**
   * Returns true if text node adjacency is possible as a result of the
   * aggregated updates. This is only the case if updates actually affect the
   * sibling axis of the target node and not the descendent-or-self axis.
   *
   * @return true if text node adjacency possible
   */
  boolean textAdjacency();
}
