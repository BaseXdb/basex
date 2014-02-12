package org.basex.data.atomic;


/**
 * Base class for structural updates that add to/remove from the table and introduce
 * shifts.
 * @author BaseX Team 2005-13, BSD License
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
   * @param l target node location PRE
   * @param s PRE value shifts introduced by update
   * @param a accumulated shifts
   * @param f PRE value of the first node the distance of which has to be updated
   * @param p parent node
   */
  StructuralUpdate(final int l, final int s, final int a, final int f, final int p) {
    super(l, p);
    shifts = s;
    accumulatedShifts = a;
    preOfAffectedNode = f;
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