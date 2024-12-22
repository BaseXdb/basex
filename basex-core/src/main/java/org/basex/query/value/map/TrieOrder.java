package org.basex.query.value.map;

import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Order of map entries.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
final class TrieOrder {
  /** Added keys. */
  private TrieKeys added = new TrieKeys();
  /** Removed keys (lazy instantiation). */
  private TrieKeys removed;

  /**
   * Returns a key iterator.
   * @return iterator
   */
  BasicIter<Item> keys() {
    cleanUp();
    return added.keys();
  }

  /**
   * Adds a key.
   * @param key key to be added
   */
  void add(final Item key) {
    added = added.add(key);
  }

  /**
   * Removes a key.
   * @param key key to be removed
   */
  void remove(final Item key) {
    removed = (removed != null ? removed : new TrieKeys()).add(key);
  }

  /**
   * Creates a copy of this data structure.
   * @return copy
   */
  protected TrieOrder copy() {
    final TrieOrder order = new TrieOrder();
    order.added = added;
    order.removed = removed;
    return order;
  }

  /**
   * Cleans up.
   */
  private void cleanUp() {
    if(removed != null) {
      added = added.remove(removed);
      removed = null;
    }
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + "Keys: " + added  +
        (removed != null ? ("; removed: " + removed) : "") + ']';
  }
}
