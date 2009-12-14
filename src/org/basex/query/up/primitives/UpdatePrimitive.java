package org.basex.query.up.primitives;

import org.basex.query.QueryException;
import org.basex.query.item.Nod;
import org.basex.query.up.NamePool;

/**
 * Abstract XQuery Update Primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public abstract class UpdatePrimitive {
  /** Target node of update expression. */
  public final Nod node;

  /**
   * Constructor.
   * @param n DBNode reference
   */
  protected UpdatePrimitive(final Nod n) {
    node = n;
  }

  /**
   * Returns the type of the update primitive.
   * @return type
   */
  public abstract PrimitiveType type();

  /**
   * Applies the update operation represented by this primitive to the
   * database. If an 'insert before' primitive is applied to a target node t,
   * the pre value of t changes. Thus the number of inserted nodes is added to
   * the pre value of t for all following update operations.
   * @param add size to add
   * @throws QueryException query exception
   */
  public abstract void apply(final int add) throws QueryException;

  /**
   * Prepares the update.
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public void prepare() throws QueryException { }

  /**
   * Merges if possible two update primitives of the same type if they have the
   * same target node.
   * @param p primitive to be merged with
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public void merge(final UpdatePrimitive p) throws QueryException { }

  /**
   * Updates the name pool, which is used for finding duplicate attributes
   * and namespace conflicts.
   * @param pool name pool
   */
  @SuppressWarnings("unused")
  public void update(final NamePool pool) { };
}
