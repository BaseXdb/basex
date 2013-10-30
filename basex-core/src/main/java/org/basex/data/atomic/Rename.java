package org.basex.data.atomic;

import org.basex.data.*;
import org.basex.util.*;

/**
 * Atomic update operation that renames a node.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
final class Rename extends BasicUpdate {
  /** Kind of updated node. */
  private final int targetkind;
  /** The new name of the node. */
  private final byte[] name;
  /** Name URI. */
  private final byte[] uri;

  /**
   * Constructor.
   * @param l PRE value of target node location
   * @param k target node kind
   * @param n new name for the target node
   * @param u new name uri for the target node
   * @param p parent node PRE
   */
  private Rename(final int l, final int k, final byte[] n, final byte[] u, final int p) {
    super(l, p);
    if(n.length == 0) Util.notexpected("New name must not be empty.");
    targetkind = k;
    name = n;
    uri = u;
  }

  /**
   * Factory.
   * @param data data reference
   * @param pre target node PRE
   * @param n new name
   * @param u new uri
   * @return instance
   */
  static Rename getInstance(final Data data, final int pre, final byte[] n,
      final byte[] u) {
    return new Rename(pre, data.kind(pre), n, u, data.parent(pre, data.kind(pre)));
  }

  @Override
  void apply(final Data d) {
    d.update(location, targetkind, name, uri);
  }

  @Override
  DataClip getInsertionData() {
    Util.notexpected("No insertion sequence needed for atomic rename operation.");
    return null;
  }

  @Override
  boolean destructive() {
    return false;
  }

  @Override
  public String toString() {
    return "\n Rename: " + super.toString();
  }
}
