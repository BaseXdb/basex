package org.basex.data.atomic;

import org.basex.data.*;

/**
 * Abstract atomic update.
 * Atomic updates can only be initialized via {@link AtomicUpdateList}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public abstract class BasicUpdate {
  /** PRE value of the target node. */
  final int location;
  /** PRE value shifts introduced by this atomic update due to structural changes. */
  final int shifts;
  /** PRE value of the first node for which the distance must be updated due to PRE value
   * shifts introduced by this update. */
  final int preOfAffectedNode;

  /** Total/accumulated number of shifts introduced by all updates on the list up to this
   * updates (inclusive). The number of total shifts is used to calculate PRE values
   * before/after updates. */
  int accumulatedShifts;

  /**
   * Constructor.
   * @param l target node location PRE
   * @param s PRE value shifts introduced by update
   * @param f PRE value of the first node the distance of which has to be updated
   */
  BasicUpdate(final int l, final int s, final int f) {
    location = l;
    shifts = s;
    preOfAffectedNode = f;
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
   * Returns the parent of the update location, mostly important for inserts.
   * @return parent PRE value
   */
  abstract int parent();

  /**
   * Returns whether this updates destroys the target nodes identity. Used to determine
   * superfluous operations on the subtree of the target.
   * @return true, if target node identity destroyed
   */
  abstract boolean destructive();
}
