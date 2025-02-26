package org.basex.query.value.map;

import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Order of map entries.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class TrieOrder {
  /** Added keys. */
  private TrieKeys added;
  /** Removed keys (lazy instantiation). */
  private TrieKeys removed;

  /**
   * Constructor.
   * @param keys added keys
   */
  TrieOrder(final Item... keys) {
    this(new TrieKeys(keys), null);
  }

  /**
   * Constructor.
   * @param added added keys
   * @param removed removed keys
   */
  private TrieOrder(final TrieKeys added, final TrieKeys removed) {
    this.added = added;
    this.removed = removed;
  }

  /**
   * Returns a key iterator.
   * @return iterator
   */
  BasicIter<Item> keys() {
    if(removed != null) {
      added = added.remove(removed);
      removed = null;
    }
    return added.keys();
  }

  /**
   * Adds a key.
   * @param key key to be added
   * @return new order
   */
  TrieOrder add(final Item key) {
    return new TrieOrder(added.add(key), removed);
  }

  /**
   * Removes a key.
   * @param key key to be removed
   * @return new order
   */
  TrieOrder remove(final Item key) {
    return new TrieOrder(added, removed != null ? removed.add(key) : new TrieKeys(key));
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + "Keys: " + added  +
        (removed != null ? "; removed: " + removed : "") + ']';
  }
}
