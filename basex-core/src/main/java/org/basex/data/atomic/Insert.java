package org.basex.data.atomic;

import org.basex.data.*;

/**
 * Atomic insert that inserts a given insertion sequence data instance into a database.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Lukas Kircher
 */
final class Insert extends StructuralUpdate {
  /** Insertion sequence. */
  final DataClip clip;

  /**
   * Constructor.
   * @param location PRE value of the target node location
   * @param shifts shifts
   * @param acc accumulated shifts
   * @param first PRE value of the first node which distance has to be updated
   * @param parent parent PRE value for the inserted nodes
   * @param clip insertion sequence data clip
   */
  private Insert(final int location, final int shifts, final int acc, final int first,
      final int parent, final DataClip clip) {
    super(location, shifts, acc, first, parent);
    this.clip = clip;
  }

  /**
   * Factory.
   * @param pre target location PRE
   * @param par parent of inserted node
   * @param clip insertion sequence
   * @return instance
   */
  static Insert getInstance(final int pre, final int par, final DataClip clip) {
    final int s = clip.size();
    return new Insert(pre, s, s, pre, par, clip);
  }

  @Override
  void apply(final Data data) {
    data.insert(location, parent, clip);
  }

  @Override
  DataClip getInsertionData() {
    return clip;
  }

  @Override
  boolean destructive() {
    return false;
  }

  @Override
  public BasicUpdate merge(final Data data, final BasicUpdate bu) {
    if(bu != null && parent == bu.parent && bu instanceof Delete && location == bu.location
        && data.kind(bu.location) != Data.ATTR) {
      final Delete del = (Delete) bu;
      return new Replace(location, shifts + del.shifts,
          del.accumulatedShifts, del.preOfAffectedNode, clip, parent);
    }
    return null;
  }

  @Override
  public String toString() {
    return "\n Insert: " + super.toString();
  }
}