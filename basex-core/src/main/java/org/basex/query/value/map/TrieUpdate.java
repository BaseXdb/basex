package org.basex.query.value.map;

import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Update information.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
final class TrieUpdate {
  /** Key. */
  final Item key;
  /** Value (can be {@code null}). */
  final Value value;

  /**
   * Constructor.
   * @param key key
   * @param value value ({@code null} for deletions)
   */
  TrieUpdate(final Item key, final Value value) {
    this.key = key;
    this.value = value;
  }

  @Override
  public String toString() {
    return key + "/" + value;
  }
}
