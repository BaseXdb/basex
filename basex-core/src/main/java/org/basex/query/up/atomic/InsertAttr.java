package org.basex.query.up.atomic;

import org.basex.data.*;

/**
 * Atomic update operation that inserts an attribute into a database.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
final class InsertAttr extends StructuralUpdate {
  /** Insertion sequence. */
  private final DataClip clip;

  /**
   * Constructor.
   * @param location PRE value of the target node location
   * @param shifts row shifts
   * @param acc accumulated row shifts
   * @param first PRE value of the first node which distance has to be updated
   * @param parent parent PRE value for the inserted node
   * @param clip insert sequence data clip
   */
  private InsertAttr(final int location, final int shifts, final int acc, final int first,
      final int parent, final DataClip clip) {
    super(location, shifts, acc, first, parent);
    this.clip = clip;
  }

  /**
   * Factory.
   * @param pre target location PRE
   * @param par parent of new attribute
   * @param clip insertion sequence
   * @return instance
   */
  static InsertAttr getInstance(final int pre, final int par, final DataClip clip) {
    final int sh = clip.size();
    return new InsertAttr(pre, sh, sh, pre, par, clip);
  }

  @Override
  void apply(final Data data) {
    data.insertAttr(location, parent, clip);
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
  public String toString() {
    return "\nInsertAttr: " + super.toString();
  }
}
