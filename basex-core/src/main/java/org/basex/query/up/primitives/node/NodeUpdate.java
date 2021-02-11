package org.basex.query.up.primitives.node;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.up.atomic.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Base class for all update primitives that operate on a specific node.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
public abstract class NodeUpdate extends DataUpdate {
  /** Pre value of target node. */
  public final int pre;

  /**
   * Constructor.
   * @param type update type
   * @param pre target node pre value
   * @param data target data reference
   * @param info input info
   */
  NodeUpdate(final UpdateType type, final int pre, final Data data, final InputInfo info) {
    super(type, data, info);
    this.pre = pre;
  }

  /**
   * Creates a {@link DBNode} instance from the target node information.
   * @return new node instance
   */
  public final DBNode node() {
    return new DBNode(data, pre);
  }

  /**
   * Prepares this update primitive before execution. This includes e.g. the
   * preparation of insertion sequences.
   * @param memData temporary data instance
   * @param qc query context
   * @throws QueryException query exception
   */
  public abstract void prepare(MemData memData, QueryContext qc) throws QueryException;

  /**
   * Updates the name pool, which is used to find duplicate attributes
   * and namespace conflicts.
   * @param pool name pool
   */
  public abstract void update(NamePool pool);

  /**
   * Adds the atomic update operations for this update primitive to the given list.
   * @param auc list of atomic updates
   */
  public abstract void addAtomics(AtomicUpdateCache auc);

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
  @SuppressWarnings("unused")
  public NodeUpdate[] substitute(final MemData tmp) {
    return new NodeUpdate[] { this };
  }
}
