package org.basex.data.atomic;

import org.basex.data.*;

/**
 * Atomic update operation that deletes a node.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Lukas Kircher
 */
final class Delete extends StructuralUpdate {
  /**
   * Constructor.
   * @param location target node location PRE
   * @param shifts PRE value shifts introduced by update
   * @param acc accumulated shifts
   * @param first PRE value of the first node which distance has to be updated
   * @param parent parent
   */
  private Delete(final int location, final int shifts, final int acc, final int first,
      final int parent) {
    super(location, shifts, acc, first, parent);
  }

  /**
   * Factory.
   * @param data data reference
   * @param pre location pre value
   * @return atomic delete
   */
  static Delete getInstance(final Data data, final int pre) {
    final int k = data.kind(pre);
    final int s = data.size(pre, k);
    return new Delete(pre, -s, -s, pre + s, data.parent(pre, k));
  }

  @Override
  void apply(final Data data) {
    data.delete(location);
  }

  @Override
  DataClip getInsertionData() {
    return null;
  }

  @Override
  boolean destructive() {
    return true;
  }

  @Override
  public String toString() {
    return "\n Delete: " + super.toString();
  }

  @Override
  public BasicUpdate merge(final Data data, final BasicUpdate bu) {
    if(bu != null && parent == bu.parent && bu instanceof Insert &&
        location - shifts == bu.location && data.kind(location) != Data.ATTR) {
      final Insert ins = (Insert) bu;
      return new Replace(location, shifts + ins.shifts,
          ins.accumulatedShifts, preOfAffectedNode, ins.clip, parent);
    }
    return null;
  }
}
