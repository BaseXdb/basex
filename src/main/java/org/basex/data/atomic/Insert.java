package org.basex.data.atomic;

import org.basex.data.*;

/**
 * Atomic insert that inserts a given insertion sequence data instance into a database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
class Insert extends BasicUpdate {
  /** Parent PRE of inserted nodes. */
  final int parent;
  /** Insertion sequence. */
  Data insseq;

  /**
   * Constructor.
   * @param l PRE value of the target node location
   * @param par parent PRE value for the inserted nodes
   * @param d insertion sequence data instance
   * @param s PRE value shifts introduced by update
   * @param f PRE value of the first node which distance has to be updated
   */
  Insert(final int l, final int s, final int f, final int par, final Data d) {
    super(l, s, f);
    parent = par;
    insseq = d;
  }

  @Override
  void apply(final Data d) {
    d.insert(location, parent, insseq);
  }

  @Override
  Data getInsertionData() {
    return insseq;
  }

  @Override
  int parent() {
    return parent;
  }

  @Override
  boolean destructive() {
    return false;
  }

  @Override
  public String toString() {
    return "Insert: " + location;
  }
}
