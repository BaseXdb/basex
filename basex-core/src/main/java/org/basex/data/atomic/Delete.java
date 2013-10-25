package org.basex.data.atomic;

import org.basex.data.*;

/**
 * Atomic update operation that deletes a node.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
final class Delete extends StructuralUpdate {
  /**
   * Constructor.
   * @param l target node location PRE
   * @param s PRE value shifts introduced by update
   * @param a accumulated shifts
   * @param f PRE value of the first node which distance has to be updated
   * @param p parent
   */
  private Delete(final int l, final int s, final int a, final int f, final int p) {
    super(l, s, a, f, p);
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
  void apply(final Data d) {
    d.delete(location);
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
  public BasicUpdate merge(final Data data, final BasicUpdate u) {
    if(u != null && parent == u.parent && u instanceof Insert &&
        location - shifts == u.location && data.kind(location) != Data.ATTR) {
      final Insert ins = (Insert) u;
      final Replace rep = new Replace(location, shifts + ins.shifts,
          ins.accumulatedShifts, preOfAffectedNode, ins.insseq, parent);
      return rep;
    }
    return null;
  }
}
