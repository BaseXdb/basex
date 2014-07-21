package org.basex.data.atomic;

import org.basex.data.*;

/**
 * Abstract atomic update.
 * Atomic updates can only be initialized via {@link AtomicUpdateCache}.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public abstract class BasicUpdate {
  /** PRE value of the target location. */
  final int location;
  /** Parent PRE of nodes to insert. */
  final int parent;

  /**
   * Constructor.
   * @param location target node location PRE
   * @param parent parent node PRE value
   */
  BasicUpdate(final int location, final int parent) {
    this.location = location;
    this.parent = parent;
  }

  /**
   * Getter for accumulated shifts.
   * @return accumulated shifts, or zero if non-structural update.
   */
  int accumulatedShifts() {
    return 0;
  }

  /**
   * Applies the update to the given data instance.
   * @param data data instance on which to execute the update
   */
  abstract void apply(final Data data);

  /**
   * Returns the data to be inserted (for inserts,...).
   * @return Insertion sequence data instance
   */
  abstract DataClip getInsertionData();

  /**
   * Returns whether this updates destroys the target nodes identity. Used to determine
   * superfluous operations on the subtree of the target.
   * @return true, if target node identity destroyed
   */
  abstract boolean destructive();

  /**
   * Merges the given update and this update if possible.
   * @param data data reference
   * @param bu update to merge with
   * @return merged atomic update or null if merge not possible
   */
  public BasicUpdate merge(final Data data, final BasicUpdate bu) {
    return null;
  }

  @Override
  public String toString() {
    return "L" + location;
  }
}
