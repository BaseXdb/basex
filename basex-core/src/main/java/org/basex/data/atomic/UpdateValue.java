package org.basex.data.atomic;

import org.basex.data.*;
import org.basex.util.*;

/**
 * Atomic update operation that replaces the value of a single text, comment, pi or
 * attribute node.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
final class UpdateValue extends BasicUpdate {
  /** Target node kind. */
  private final int targetkind;
  /** New value for target node. */
  private final byte[] value;

  /**
   * Constructor.
   * @param l PRE value of the target node location
   * @param k node kind of the target node
   * @param v new value which is assigned to the target node
   * @param p parent of updated node
   */
  private UpdateValue(final int l, final int k, final byte[] v, final int p) {
    super(l, p);
    targetkind = k;
    value = v;
  }

  /**
   * Factory.
   * @param data data reference
   * @param pre PRE value of the target node location
   * @param v new value which is assigned to the target node
   * @return new instance
   */
  static UpdateValue getInstance(final Data data, final int pre, final byte[] v) {
    return new UpdateValue(pre, data.kind(pre), v, data.parent(pre, data.kind(pre)));
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
  boolean destructive() {
    return false;
  }

  @Override
  public String toString() {
    return "\nUpdateValue: " + super.toString();
  }
}
