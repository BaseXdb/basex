package org.basex.data.atomic;

import org.basex.data.*;

/**
 * Atomic update operation that deletes a node.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
final class Delete extends BasicUpdate {
  /**
   * Constructor.
   * @param l target node location PRE
   * @param s PRE value shifts introduced by update
   * @param f PRE value of the first node which distance has to be updated
   */
  Delete(final int l, final int s, final int f) {
    super(l, s, f);
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
  int parent() {
    return -1;
  }

  @Override
  boolean destructive() {
    return true;
  }

  @Override
  public String toString() {
    return "Delete: " + location;
  }
}
