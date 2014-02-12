package org.basex.data.atomic;

import org.basex.data.*;

/**
 * Atomic update operation that inserts an attribute into a database.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
final class InsertAttr extends StructuralUpdate {
  /** Insertion sequence. */
  private final DataClip insseq;

  /**
   * Constructor.
   * @param l PRE value of the target node location
   * @param s row shifts
   * @param a accumulated row shifts
   * @param f PRE value of the first node which distance has to be updated
   * @param par parent PRE value for the inserted node
   * @param c insert sequence data clip
   */
  private InsertAttr(final int l, final int s, final int a, final int f, final int par,
      final DataClip c) {
    super(l, s, a, f, par);
    insseq = c;
  }

  /**
   * Factory.
   * @param pre target location PRE
   * @param par parent of new attribute
   * @param clip insertion sequence
   * @return instance
   */
  static InsertAttr getInstance(final int pre, final int par,
      final DataClip clip) {
    final int sh = clip.size();
    return new InsertAttr(pre, sh, sh, pre, par, clip);
  }

  @Override
  void apply(final Data d) {
    d.insertAttr(location, parent, insseq);
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
    return "\nInsertAttr: " + super.toString();
  }
}
