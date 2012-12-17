package org.basex.data.atomic;

import org.basex.data.*;
import org.basex.util.*;

/**
 * Atomic update operation that replaces the value of a single text, comment, pi or
 * attribute node.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
final class UpdateValue extends BasicUpdate {
  /** Target node kind. */
  final int targetkind;
  /** New value for target node. */
  final byte[] value;

  /**
   * Constructor.
   * @param l PRE value of the target node location
   * @param k node kind of the target node
   * @param v new value which is assigned to the target node
   */
  UpdateValue(final int l, final int k, final byte[] v) {
    super(l, 0, -1);
    targetkind = k;
    value = v;
  }

  @Override
  void apply(final Data d) {
    d.update(location, targetkind, value);
  }

  @Override
  DataClip getInsertionData() {
    Util.notexpected("No insertion sequence needed for atomic value update operation.");
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
    return "UpdateValue: " + location;
  }
}
