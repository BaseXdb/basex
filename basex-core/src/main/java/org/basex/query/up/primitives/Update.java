package org.basex.query.up.primitives;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Abstract class for an update operations.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
public abstract class Update {
  /** Type of update primitive. */
  public final UpdateType type;
  /** Input info. */
  protected final InputInfo info;

  /**
   * Constructor.
   * @param type update primitive
   * @param info input info
   */
  protected Update(final UpdateType type, final InputInfo info) {
    this.type = type;
    this.info = info;
  }

  /**
   * Merges two update operations pointing to the same target.
   * @param update operation to merge with
   * @throws QueryException exception
   */
  public abstract void merge(Update update) throws QueryException;

  /**
   * Returns the number of update operations.
   * @return number of updates
   */
  public abstract int size();

  /**
   * Input info.
   * @return input info
   */
  public final InputInfo info() {
    return info;
  }
}
