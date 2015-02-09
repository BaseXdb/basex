package org.basex.query.up.primitives.db;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;

/**
 * Update that operates on a database but is not an update primitive. This task is carried out
 * after all updates on the database have been made effective in the order of the
 * {@link UpdateType}. Hence, changes made during a snapshot will be reflected by this task.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Lukas Kircher
 */
public abstract class DBUpdate extends DataUpdate implements Comparable<DBUpdate> {
  /**
   * Constructor.
   * @param type type of this operation
   * @param data target data reference
   * @param info input info
   */
  DBUpdate(final UpdateType type, final Data data, final InputInfo info) {
    super(type, data, info);
  }

  @Override
  public final int compareTo(final DBUpdate o) {
    return type.ordinal() - o.type.ordinal();
  }

  @Override
  public abstract void merge(final Update update) throws QueryException;

  /**
   * Applies this operation.
   * @throws QueryException exception
   */
  public abstract void apply() throws QueryException;

  /**
   * Prepares this operation.
   * @param tmp temporary mem data
   * @throws QueryException exception
   */
  public abstract void prepare(final MemData tmp) throws QueryException;
}
