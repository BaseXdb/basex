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
  final TrieOrder oldOrder;
  /** Order of new map (initially {@code null}). */
  TrieOrder newOrder;

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
   * @param old key of entry to be replaced (only considered for additions to singleton maps)
   */
  void add(final Item old) {
    if(!oldOrder()) {
      newOrder = new TrieOrder();
      newOrder.add(old);
    }
    newOrder.add(key);
  }

  /**
   * Replaces an entry.
   * @param old key of entry to be replaced
   */
  void replace(final Item old) {
    if(oldOrder() && key.type != old.type) {
      // remove and add key if old and new keys are of different type
      newOrder.remove(old);
      newOrder.add(key);
    }
  }

  /**
   * Removes an entry.
   * @param old key of entry to be removed
   */
  void remove(final Item old) {
    if(oldOrder()) {
      newOrder.remove(old);
    }
  }

  /**
   * Indicates if an old order existed and could be prepared for updates.
   * @return result of check
   */
  private boolean oldOrder() {
    if(oldOrder == null) return false;
    newOrder = oldOrder.copy();
    return true;
  }

  @Override
  public String toString() {
    return key + "/" + value + ": " + oldOrder + " -> " + newOrder;
  }
}
