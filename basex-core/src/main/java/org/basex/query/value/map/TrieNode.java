package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Abstract superclass of all trie nodes.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
abstract class TrieNode {
  /** Number of bits per level, maximum is 5 because {@code 1 << 5 == 32}. */
  static final int BITS = 5;
  /** Number of children on each level. */
  static final int KIDS = 1 << BITS;
  /** Mask for the bits used on the current level. */
  private static final int MASK = KIDS - 1;

  /** Size of this node. */
  final int size;

  /**
   * Constructor.
   * @param size size
   */
  TrieNode(final int size) {
    this.size = size;
  }

  /**
   * Puts the given value into this map and replaces existing keys.
   * @param hs hash code used as key
   * @param lv level
   * @param update update information
   * @return updated map
   * @throws QueryException query exception
   */
  abstract TrieNode put(int hs, int lv, TrieUpdate update) throws QueryException;

  /**
   * Creates a node branch.
   * @param hs hash code used as key
   * @param lv level
   * @param hash hash code of the existing key
   * @param sz old node size
   * @param update update information
   * @return branch
   * @throws QueryException query exception
   */
  final TrieBranch branch(final int hs, final int lv, final int hash, final int sz,
      final TrieUpdate update) throws QueryException {
    // different hash, branch
    final TrieNode[] ch = new TrieNode[KIDS];
    final int a = hashKey(hs, lv), b = hashKey(hash, lv), used;
    if(a == b) {
      ch[a] = put(hs, lv + 1, update);
      used = 1 << a;
    } else {
      ch[a] = new TrieLeaf(hs, update.key, update.value);
      ch[b] = this;
      used = 1 << a | 1 << b;
    }
    return new TrieBranch(ch, used, sz + 1);
  }

  /**
   * Removes a key from this map.
   * @param hs hash code of the key
   * @param lv level
   * @param update update information
   * @return updated map if changed, {@code null} if removed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode remove(int hs, int lv, TrieUpdate update) throws QueryException;

  /**
   * Looks up the value associated with the given key.
   * @param hs hash code
   * @param ky key to look up
   * @param lv level
   * @return bound value if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  abstract Value get(int hs, Item ky, int lv) throws QueryException;

  /**
   * Applies a function on all entries.
   * @param func function to apply on keys and values
   * @throws QueryException query exception
   */
  abstract void apply(QueryBiConsumer<Item, Value> func) throws QueryException;

  /**
   * Tests all entries.
   * @param func predicate function
   * @return result of check
   * @throws QueryException query exception
   */
  abstract boolean test(QueryBiPredicate<Item, Value> func) throws QueryException;

  /**
   * Calculates the hash key for the given level.
   * @param hash hash value
   * @param level current level
   * @return hash key
   */
  static int hashKey(final int hash, final int level) {
    return hash >>> level * BITS & MASK;
  }

  /**
   * Checks if this node is indistinguishable from the given node.
   * @param node other node
   * @param deep comparator
   * @return result of check
   * @throws QueryException query exception
   */
  abstract boolean equal(TrieNode node, DeepEqual deep) throws QueryException;

  /**
   * Recursive {@link #toString()} helper.
   * @param tb token builder
   * @param indent indentation string
   */
  abstract void add(TokenBuilder tb, String indent);

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    add(tb, "");
    return tb.toString();
  }
}
