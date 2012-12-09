package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Base class for all update primitives.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public abstract class UpdatePrimitive implements Operation {
  /** Primitive Type. */
  public final PrimitiveType type;
  /** Target node pre value. */
  public final int targetPre;
  /** Target data reference. */
  public final Data data;
  /** Input info. */
  public final InputInfo info;

  /**
   * Constructor.
   * @param t primitive type
   * @param p target node pre value
   * @param d target data reference
   * @param ii input info
   */
  UpdatePrimitive(final PrimitiveType t, final int p, final Data d, final InputInfo ii) {
    targetPre = p;
    data = d;
    info = ii;
    type = t;
  }

  /**
   * Creates a {@link DBNode} instance from the target node information.
   * @return DBNode
   */
  @Override
  public final DBNode getTargetNode() {
    return new DBNode(data, targetPre);
  }

  @Override
  public Data getData() {
    return data;
  }

  @Override
  public InputInfo getInfo() {
    return info;
  }

  /**
   * Merges two update primitives, as they have the same target node.
   * @param p primitive to merge with
   * @throws QueryException exception
   */
  public abstract void merge(final UpdatePrimitive p) throws QueryException;

  /**
   * Updates the name pool, which is used to find duplicate attributes
   * and namespace conflicts.
   * @param pool name pool
   */
  public abstract void update(final NamePool pool);

  /**
   * Adds the atomic update operations for this update primitive to the given list.
   * @param l list of atomic updates
   */
  public abstract void addAtomics(final AtomicUpdateList l);

  /**
   * Substitutes the update primitive if necessary. For instance a 'Replace Value
   * of' primitive called on a target T with T being an element results in a 'Replace
   * Element Content' primitive with target T. As this is ugly to process it is
   * substituted by delete primitives for every child of T and an 'Insert into' primitive
   * if the length of the (optional!) text node is greater zero.
   *
   * When a primitive is substituted it is still added to the list itself to be able to
   * throw exceptions when necessary. I.e. for multiple replaces on the same target node.
   * These update primitives don't produce atomic updates, hence this won't affect the
   * database.
   *
   * @param tmp temporary mem data
   * @return An array that contains the substituting primitives or this update primitive
   * if no substitution is necessary.
   */
  public abstract UpdatePrimitive[] substitute(final MemData tmp);
}
