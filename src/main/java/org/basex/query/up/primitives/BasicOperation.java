package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Basic task that operates on the database but is not an update primitive. This task
 * is carried out after all updates on the database have been made effective in the order
 * of the {@link TYPE}. Hence changes made during a snapshot will be reflected by this
 * task.
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public abstract class BasicOperation implements Comparable<BasicOperation>, Operation {
  /** Basic Operation types. Carried out in the given order. */
  public static enum TYPE {
    /** DBAdd.                 */ DBADD,
    /** DBStore.                 */ DBSTORE,
    /** DBRename.                */ DBRENAME,
    /** DBDelete.                */ DBDELETE,
    /** DBOptimize.              */ DBOPTIMIZE,
    /** DBFlush.                 */ DBFLUSH,
    /** FnPut.                 */ FNPUT,
  };
  /** Target data reference. */
  final Data data;
  /** Input info. */
  final InputInfo info;
  /** Type of this operation. */
  public final TYPE type;

  /**
   * Constructor.
   * @param t type of this operation
   * @param d target data reference
   * @param ii input info
   */
  public BasicOperation(final TYPE t, final Data d, final InputInfo ii) {
    type = t;
    info = ii;
    data = d;
  }

  @Override
  public final int compareTo(final BasicOperation o) {
    return this.type.ordinal() - o.type.ordinal();
  }

  @Override
  public DBNode getTargetNode() {
    return new DBNode(data, -1);
  }

  @Override
  public InputInfo getInfo() {
    return info;
  }

  @Override
  public Data getData() {
    return data;
  }

  /**
   * Merges this operation with the given one.
   * @param o operation to merge into this one
   * @throws QueryException exception
   */
  public abstract void merge(final BasicOperation o) throws QueryException;

  /**
   * Applies this operation.
   * @throws QueryException exception
   */
  public abstract void apply() throws QueryException;

  /**
   * Prepares this operation.
   * @throws QueryException exception
   */
  public abstract void prepare() throws QueryException;
}
