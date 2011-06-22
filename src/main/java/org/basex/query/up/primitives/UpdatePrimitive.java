package org.basex.query.up.primitives;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.up.NamePool;
import org.basex.util.InputInfo;

/**
 * Base class for all update primitives.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public abstract class UpdatePrimitive implements Comparable<UpdatePrimitive> {
  /** Primitive Type. */
  public final PrimitiveType type;
  /** Target pre value. */
  public final int pre;
  /** Target data reference. */
  public final Data data;
  /** Input info. */
  public final InputInfo input;

  /**
   * Constructor.
   * @param t primitive type
   * @param p target pre value
   * @param d target data reference
   * @param info input info
   */
  public UpdatePrimitive(final PrimitiveType t, final int p, final Data d,
      final InputInfo info) {
    pre = p;
    data = d;
    input = info;
    type = t;
  }

  /**
   * Creates a {@link DBNode} instance from the target node information.
   * @return DBNode
   */
  public DBNode getTargetDBNode() {
    return new DBNode(data, pre);
  }

  @Override
  public int compareTo(final UpdatePrimitive p) {
    return this.type.ordinal() - p.type.ordinal();
  }

  /**
   * Merges two update primitives, as they have the same target node.
   * @param p primitive to merge with
   * @throws QueryException exception
   */
  public abstract void merge(final UpdatePrimitive p) throws QueryException;

  /**
   * Applies this update primitive to the corresponding database.
   * @throws QueryException exception
   */
  public abstract void apply() throws QueryException;

  /**
   * Updates the name pool, which is used to find duplicate attributes
   * and namespace conflicts.
   * @param pool name pool
   */
  @SuppressWarnings("unused")
  public void update(final NamePool pool) { }
}