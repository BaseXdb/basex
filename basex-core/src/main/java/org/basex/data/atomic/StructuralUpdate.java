package org.basex.data.atomic;

/**
 * Base class for structural updates that add to/remove from the table and introduce
 * shifts.
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 *
 */
public abstract class StructuralUpdate extends BasicUpdate {
  /** PRE value shifts introduced by this atomic update due to structural changes. */
  final int shifts;
  /** PRE value of the first node for which the distance must be updated due to PRE value
   * shifts introduced by this update. */
  final int preOfAffectedNode;
  /** Total/accumulated number of shifts introduced by all updates on the list up to this
   * update (inclusive). The number of total shifts is used to calculate PRE values
   * before/after updates. */
  int accumulatedShifts;

  /**
   * Constructor.
   * @param location target node location PRE
   * @param shifts PRE value shifts introduced by update
   * @param acc accumulated shifts
   * @param first PRE value of the first node the distance of which has to be updated
   * @param parent parent node
   */
  StructuralUpdate(final int location, final int shifts, final int acc, final int first,
      final int parent) {
    super(location, parent);
    this.shifts = shifts;
    accumulatedShifts = acc;
    preOfAffectedNode = first;
  }

  @Override
  int accumulatedShifts() {
    return accumulatedShifts;
  }

  @Override
  public String toString() {
    return "L" + location +
        " PAR" + parent +
        " SHF" + shifts +
        " ASHF" + accumulatedShifts +
        " AFF" + preOfAffectedNode;
  }
}