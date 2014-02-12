package org.basex.data.atomic;

import org.basex.data.*;

/**
 * Atomic insert that inserts a given insertion sequence data instance into a database.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
final class Insert extends StructuralUpdate {
  /** Insertion sequence. */
  final DataClip insseq;

  /**
   * Constructor.
   * @param l PRE value of the target node location
   * @param s shifts
   * @param a accumulated shifts
   * @param f PRE value of the first node which distance has to be updated
   * @param p parent PRE value for the inserted nodes
   * @param c insertion sequence data clip
   */
  private Insert(final int l, final int s, final int a, final int f, final int p,
      final DataClip c) {
    super(l, s, a, f, p);
    insseq = c;
  }

  /**
   * Factory.
   * @param pre target location PRE
   * @param par parent of inserted node
   * @param clip insertion sequence
   * @return instance
   */
  static Insert getInstance(final int pre, final int par,
      final DataClip clip) {
    final int s = clip.size();
    return new Insert(pre, s, s, pre, par, clip);
  }

  @Override
  void apply(final Data d) {
    d.insert(location, parent, insseq);
  }

  @Override
  DataClip getInsertionData() {
    return insseq;
  }

  @Override
  boolean destructive() {
    return false;
  }

  @Override
  public String toString() {
    return "\n Insert: " + super.toString();
  }

  @Override
  public BasicUpdate merge(final Data data, final BasicUpdate u) {
    if(u != null && parent == u.parent && u instanceof Delete && location == u.location
        && data.kind(u.location) != Data.ATTR) {
      final Delete del = (Delete) u;
      return new Replace(location, shifts + del.shifts,
          del.accumulatedShifts, del.preOfAffectedNode, insseq, parent);
    }
    return null;
  }
}