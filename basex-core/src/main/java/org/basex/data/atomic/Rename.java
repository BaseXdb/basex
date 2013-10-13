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
  final int targetkind;
  /** The new name of the node. */
  final byte[] name;
  /** Name URI. */
  final byte[] uri;

  /**
   * Constructor.
   * @param l PRE value of target node location
   * @param k target node kind
   * @param n new name for the target node
   * @param u new name uri for the target node
   */
  Rename(final int l, final int k, final byte[] n, final byte[] u) {
    super(l, 0, -1);
    if(n.length == 0) Util.notexpected("New name must not be empty.");
    targetkind = k;
    name = n;
    uri = u;
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
  int parent() {
    return -1;
  }

  @Override
  boolean destructive() {
    return false;
  }

  @Override
  public String toString() {
    return "Rename: " + location;
  }
}
