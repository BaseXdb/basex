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
   * @param l target node location PRE
   * @param p parent node PRE value
   */
  BasicUpdate(final int l, final int p) {
    location = l;
    parent = p;
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
   * @param d data instance on which to execute the update
   */
  abstract void apply(final Data d);

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
   * @param u update to merge with
   * @return merged atomic update or null if merge not possible
   */
  @SuppressWarnings("unused")
  public BasicUpdate merge(final Data data, final BasicUpdate u) {
    return null;
  }

  @Override
  public String toString() {
    return "L" + location;
  }
}
