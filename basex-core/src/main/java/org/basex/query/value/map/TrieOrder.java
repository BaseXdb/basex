package org.basex.query.value.map;

import org.basex.query.value.*;
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
  private final TrieKeys added;
  /** Removed keys ({@code null} if none are pending). */
  private final TrieKeys removed;
  /** Resolved keys, memoized. */
  private volatile TrieKeys resolved;

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
   * @param removed removed keys (can be {@code null})
   */
  private TrieOrder(final TrieKeys added, final TrieKeys removed) {
    this.added = added;
    this.removed = removed;
    if(removed == null) resolved = added;
  }

  /**
   * Returns all keys.
   * @return keys
   */
  Value keys() {
    return resolved().keys();
  }

  /**
   * Returns the key at the specified position.
   * @param index map index (starting with 0, must be valid)
   * @return key
   */
  Item keyAt(final int index) {
    return resolved().keyAt(index);
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
   * Removes a key, compacting the order when pending removals dominate the key list.
   * @param key key to be removed
   * @return new order
   */
  TrieOrder remove(final Item key) {
    final TrieKeys rem = removed != null ? removed.add(key) : new TrieKeys(key);
    return rem.size() * 2L > added.size() ? new TrieOrder(added.remove(rem), null) :
      new TrieOrder(added, rem);
  }

  /**
   * Returns the number of key references this order retains (for testing).
   * @return retained key reference count
   */
  int retained() {
    return added.size() + (removed != null ? removed.size() : 0);
  }

  /**
   * Resolves and memoizes the effective key list.
   * @return keys
   */
  private TrieKeys resolved() {
    TrieKeys r = resolved;
    if(r == null) {
      r = added.remove(removed);
      resolved = r;
    }
    return r;
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + "Keys: " + added +
        (removed != null ? "; removed: " + removed : "") + ']';
  }
}
