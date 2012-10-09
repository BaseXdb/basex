package org.basex.data.atomic;

import org.basex.data.*;

/**
 * Replaces a node in the database with an insertion sequence.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
class Replace extends BasicUpdate {
  /** Insertion sequence. */
  final Data insseq;

  /**
   * Constructor.
   * @param l PRE value of the target node location
   * @param d insertion sequence data instance
   * @param s PRE value shifts introduced by update
   * @param f PRE value of the first node which distance has to be updated
   */
  Replace(final int l, final int s, final int f, final Data d) {
    super(l, s, f);
    insseq = d;
  }

  @Override
  void apply(final Data d) {
    if(fastReplace(d))
      d.replace(location, insseq);
    else {
      final int targetKind = d.kind(location);
      final int targetParent = d.parent(location, targetKind);
      // delete first - otherwise insert must be at location+1
      d.delete(location);
      if(targetKind == Data.ATTR)
        d.insertAttr(location, targetParent, insseq);
      else
        d.insert(location, targetParent, insseq);
    }
  }

  /**
   * Checks whether an optimization for a faster replace can be leveraged. Only available
   * if no {@link Namespaces} exist in both the target and the source database.
   * @param d target database
   * @return true if fast replace possible
   */
  private boolean fastReplace(final Data d) {
    return d.nspaces.size() == 0 && insseq.nspaces.size() == 0 &&
        d.kind(location) != Data.ATTR;
  }

  @Override
  Data getInsertionData() {
    return insseq;
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
    return "Replace: " + location;
  }
}
