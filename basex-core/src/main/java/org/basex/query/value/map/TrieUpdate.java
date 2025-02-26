package org.basex.query.value.map;

import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Update information.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class TrieUpdate {
  /** Key. */
  final Item key;
  /** Value (can be {@code null}). */
  final Value value;
  /** Order of old map ({@code null} for empty and singleton maps). */
  private final TrieOrder oldOrder;
  /** Order of new map (initially {@code null}). */
  private TrieOrder newOrder;

  /**
   * Constructor.
   * @param key key
   * @param value value ({@code null} for deletions)
   * @param oldOrder old order ({@code null} for empty and singleton maps)
   */
  TrieUpdate(final Item key, final Value value, final TrieOrder oldOrder) {
    this.key = key;
    this.value = value;
    this.oldOrder = oldOrder;
  }

  /**
   * Adds an entry.
   */
  void add() {
    if(oldOrder != null) newOrder = oldOrder.add(key);
  }

  /**
   * Removes an entry.
   * @param old key of entry to be removed
   */
  void remove(final Item old) {
    if(oldOrder != null) newOrder = oldOrder.remove(old);
  }

  /**
   * Returns the current map order.
   * @return order (can be {@code null})
   */
  TrieOrder order() {
    return newOrder != null ? newOrder : oldOrder;
  }

  @Override
  public String toString() {
    return key + "/" + value + ": " + oldOrder + " -> " + newOrder;
  }
}
