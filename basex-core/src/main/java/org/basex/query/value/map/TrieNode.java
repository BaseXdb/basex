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

  /** The empty node. */
  static final TrieNode EMPTY = new TrieNode(0) {
    @Override
    TrieNode delete(final int hash, final Item key, final int level) { return this; }
    @Override
    Value get(final int hash, final Item key, final int level) { return null; }
    @Override
    boolean verify() { return true; }
    @Override
    boolean equal(final TrieNode node, final DeepEqual deep) { return this == node; }
    @Override
    public TrieNode put(final int hash, final Item key, final Value value, final int level) {
      return new TrieLeaf(hash, key, value); }
    @Override
    void apply(final QueryBiConsumer<Item, Value> func) { }
    @Override
    boolean test(final QueryBiPredicate<Item, Value> func) { return true; }
    @Override
    void add(final TokenBuilder tb, final String indent) { tb.add("{ }"); }
  };

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
   * @param hash hash code used as key
   * @param key key to insert
   * @param value value to insert
   * @param level level
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode put(int hash, Item key, Value value, int level) throws QueryException;

  /**
   * Deletes a key from this map.
   * @param hash hash code of the key
   * @param key key to delete
   * @param level level
   * @return updated map if changed, {@code null} if deleted, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode delete(int hash, Item key, int level) throws QueryException;

  /**
   * Looks up the value associated with the given key.
   * @param hash hash code
   * @param key key to look up
   * @param level level
   * @return bound value if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  abstract Value get(int hash, Item key, int level) throws QueryException;

  /**
   * Verifies the tree.
   * @return check result
   */
  abstract boolean verify();

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
  static int key(final int hash, final int level) {
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
