package org.basex.query.up.primitives;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Update that operates on a data reference.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Lukas Kircher
 */
public abstract class DataUpdate extends Update {
  /** Target data reference. */
  protected final Data data;

  /**
   * Constructor.
   * @param type type of this operation
   * @param data target data reference
   * @param info input info
   */
  protected DataUpdate(final UpdateType type, final Data data, final InputInfo info) {
    super(type, info);
    this.data = data;
  }

  /**
   * Returns the target data reference.
   * @return data
   */
  public final Data data() {
    return data;
  }

  /**
   * Checks if the node limit is exceeded.
   * @param size nodes to be added
   * @throws QueryException query exception
   */
  public final void checkLimit(final long size) throws QueryException {
    if(data.meta.size + size >= Integer.MAX_VALUE) {
      throw UPDBERROR_X.get(null, "Update would exceed database node limit.");
    }
  }
}
