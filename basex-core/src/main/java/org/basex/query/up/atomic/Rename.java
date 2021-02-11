package org.basex.query.up.atomic;

import org.basex.data.*;
import org.basex.util.*;

/**
 * Atomic update operation that renames a node.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
final class Rename extends BasicUpdate {
  /** Kind of updated node. */
  private final int kind;
  /** The new name of the node. */
  private final byte[] name;
  /** Name URI. */
  private final byte[] uri;

  /**
   * Constructor.
   * @param location PRE value of target node location
   * @param kind target node kind
   * @param name new name for the target node
   * @param uri new name uri for the target node
   * @param parent parent node PRE
   */
  private Rename(final int location, final int kind, final byte[] name, final byte[] uri,
      final int parent) {
    super(location, parent);
    if(name.length == 0) throw Util.notExpected("New name must not be empty.");
    this.kind = kind;
    this.name = name;
    this.uri = uri;
  }

  /**
   * Factory.
   * @param data data reference
   * @param pre target node PRE
   * @param name new name
   * @param uri new uri
   * @return instance
   */
  static Rename getInstance(final Data data, final int pre, final byte[] name, final byte[] uri) {
    return new Rename(pre, data.kind(pre), name, uri, data.parent(pre, data.kind(pre)));
  }

  @Override
  void apply(final Data data) {
    data.update(location, kind, name, uri);
  }

  @Override
  DataClip getInsertionData() {
    throw Util.notExpected("No insertion sequence needed for atomic rename operation.");
  }

  @Override
  boolean destructive() {
    return false;
  }

  @Override
  public String toString() {
    return "\nRename: " + super.toString();
  }
}
