package org.basex.query.up;

import org.basex.query.QueryException;
import org.basex.query.up.primitives.PrimitiveType;
import org.basex.query.up.primitives.UpdatePrimitive;

/**
 * Interface for a container that holds all update primitives for a specific 
 * database node. The {@link UpdatePrimitive} are ordered after their
 * {@link PrimitiveType}. There is at most one {@link UpdatePrimitive} for each
 * type. Duplicate {@link UpdatePrimitive} are merged.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
public interface NodePrimitives extends Iterable<UpdatePrimitive> {
  
  /**
   * Adds a primitive to the container.
   * @param p primitive to be added
   * @throws QueryException query exception
   */
  public void add(final UpdatePrimitive p) throws QueryException;
  
  /**
   * Finds a primitive with the given type. If there is no primitive of type t
   * the result equals null.
   * @param t type of the primitive to find
   * @return primitive of type t or null if not found
   */
  public UpdatePrimitive findSpecific(final PrimitiveType t);
}
