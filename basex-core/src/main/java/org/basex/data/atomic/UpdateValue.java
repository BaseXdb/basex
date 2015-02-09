package org.basex.data.atomic;

import org.basex.data.*;
import org.basex.util.*;

/**
 * Atomic update operation that replaces the value of a single text, comment, pi or
 * attribute node.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Lukas Kircher
 */
final class UpdateValue extends BasicUpdate {
  /** Target node kind. */
  private final int kind;
  /** New value for target node. */
  private final byte[] value;

  /**
   * Constructor.
   * @param location PRE value of the target node location
   * @param kind node kind of the target node
   * @param value new value which is assigned to the target node
   * @param parent parent of updated node
   */
  private UpdateValue(final int location, final int kind, final byte[] value, final int parent) {
    super(location, parent);
    this.kind = kind;
    this.value = value;
  }

  /**
   * Factory.
   * @param data data reference
   * @param pre PRE value of the target node location
   * @param value new value which is assigned to the target node
   * @return new instance
   */
  static UpdateValue getInstance(final Data data, final int pre, final byte[] value) {
    return new UpdateValue(pre, data.kind(pre), value, data.parent(pre, data.kind(pre)));
  }

  @Override
  void apply(final Data data) {
    data.update(location, kind, value);
  }

  @Override
  DataClip getInsertionData() {
    throw Util.notExpected("No insertion sequence needed for atomic value update operation.");
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
