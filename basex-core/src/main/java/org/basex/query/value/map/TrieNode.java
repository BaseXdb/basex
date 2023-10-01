package org.basex.query.value.map;

import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract superclass of all trie nodes.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Leo Woerteler
 */
abstract class TrieNode {
  /** Number of children on each level. */
  static final int KIDS = 1 << XQMap.BITS;
  /** Mask for the bits used on the current level. */
  private static final int MASK = KIDS - 1;

  /** The empty node. */
  static final TrieNode EMPTY = new TrieNode(0) {
    @Override
    TrieNode delete(final int hash, final Item key, final int level, final InputInfo info) {
      return this; }
    @Override
    Value get(final int hash, final Item key, final int level, final InputInfo info) {
      return null; }
    @Override
    boolean contains(final int hash, final Item key, final int level, final InputInfo info) {
      return false; }
    @Override
    TrieNode addAll(final TrieNode node, final int level, final MergeDuplicates merge,
        final QueryContext qc, final InputInfo info) { return node; }
    @Override
    TrieNode add(final TrieLeaf leaf, final int level, final MergeDuplicates merge,
        final QueryContext qc, final InputInfo info) { return leaf; }
    @Override
    TrieNode add(final TrieList list, final int level, final MergeDuplicates merge,
        final QueryContext qc, final InputInfo info) { return list; }
    @Override
    TrieNode add(final TrieBranch branch, final int level, final MergeDuplicates merge,
        final QueryContext qc, final InputInfo info) { return branch; }
    @Override
    boolean verify() { return true; }
    @Override
    void keys(final ItemList ks) { }
    @Override
    void values(final ValueBuilder vs) { }
    @Override
    void cache(final boolean lazy, final InputInfo info) { }
    @Override
    boolean materialized(final Predicate<Data> test, final InputInfo info) { return true; }
    @Override
    boolean instanceOf(final AtomType kt, final SeqType dt) { return true; }
    @Override
    int hash(final InputInfo info) { return 0; }
    @Override
    boolean equal(final TrieNode node, final DeepEqual deep) { return this == node; }
    @Override
    public TrieNode put(final int hash, final Item key, final Value value, final int level,
        final InputInfo info) { return new TrieLeaf(hash, key, value); }
    @Override
    void apply(final QueryBiConsumer<Item, Value> func) { }
    @Override
    void add(final TokenBuilder tb, final String indent) { tb.add("{ }"); }
    @Override
    void add(final TokenBuilder tb) { }
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
   * @param info input info (can be {@code null})
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode put(int hash, Item key, Value value, int level, InputInfo info)
      throws QueryException;

  /**
   * Deletes a key from this map.
   * @param hash hash code of the key
   * @param key key to delete
   * @param level level
   * @param info input info (can be {@code null})
   * @return updated map if changed, {@code null} if deleted, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode delete(int hash, Item key, int level, InputInfo info) throws QueryException;

  /**
   * Looks up the value associated with the given key.
   * @param hash hash code
   * @param key key to look up
   * @param level level
   * @param info input info (can be {@code null})
   * @return bound value if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  abstract Value get(int hash, Item key, int level, InputInfo info) throws QueryException;

  /**
   * Checks if the given key exists in the map.
   * @param hash hash code
   * @param key key to look for
   * @param level level
   * @param info input info (can be {@code null})
   * @return {@code true} if the key exists, {@code false} otherwise
   * @throws QueryException query exception
   */
  abstract boolean contains(int hash, Item key, int level, InputInfo info) throws QueryException;

  /**
   * <p> Inserts all bindings from the given node into this one.
   * <p> This method is part of the <i>double dispatch</i> pattern and
   *     should be implemented as {@code return o.add(this, lvl, info);}.
   * @param node other node
   * @param level level
   * @param merge merge duplicates
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode addAll(TrieNode node, int level, MergeDuplicates merge, QueryContext qc,
      InputInfo info) throws QueryException;

  /**
   * Add a leaf to this node if the key is not already used.
   * @param leaf leaf to insert
   * @param level level
   * @param merge merge duplicates
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode add(TrieLeaf leaf, int level, MergeDuplicates merge, QueryContext qc,
      InputInfo info) throws QueryException;

  /**
   * Add an overflow list to this node if the key is not already used.
   * @param list leaf to insert
   * @param level level
   * @param merge merge duplicates
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode add(TrieList list, int level, MergeDuplicates merge, QueryContext qc,
      InputInfo info) throws QueryException;

  /**
   * Add all bindings of the given branch to this node for which the key is not already used.
   * @param branch leaf to insert
   * @param level level
   * @param merge merge duplicates
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode add(TrieBranch branch, int level, MergeDuplicates merge, QueryContext qc,
      InputInfo info) throws QueryException;

  /**
   * Verifies the tree.
   * @return check result
   */
  abstract boolean verify();

  /**
   * Collects all keys in this subtree.
   * @param ks key cache
   */
  abstract void keys(ItemList ks);

  /**
   * Collects all values in this subtree.
   * @param vs value cache
   */
  abstract void values(ValueBuilder vs);

  /**
   * Caches all keys and values.
   * @param lazy lazy caching
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  abstract void cache(boolean lazy, InputInfo info) throws QueryException;

  /**
   * Checks if all value of this node are materialized.
   * @param test test for copying nodes
   * @param info input info (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  abstract boolean materialized(Predicate<Data> test, InputInfo info) throws QueryException;

  /**
   * Applies a function on all entries.
   * @param func function to apply on keys and values
   * @throws QueryException query exception
   */
  abstract void apply(QueryBiConsumer<Item, Value> func) throws QueryException;

  /**
   * Calculates the hash key for the given level.
   * @param hash hash value
   * @param level current level
   * @return hash key
   */
  static int key(final int hash, final int level) {
    return hash >>> level * XQMap.BITS & MASK;
  }

  /**
   * Checks if the map has the specified key and value type.
   * @param kt key type
   * @param dt declared type
   * @return {@code true} if the type fits, {@code false} otherwise
   */
  abstract boolean instanceOf(AtomType kt, SeqType dt);

  /**
   * Calculates the hash code of this node.
   * @param info input info (can be {@code null})
   * @return hash value
   * @throws QueryException query exception
   */
  abstract int hash(InputInfo info) throws QueryException;

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

  /**
   * Recursive helper for {@link XQMap#toString()}.
   * @param tb token builder
   */
  abstract void add(TokenBuilder tb);

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    add(tb, "");
    return tb.toString();
  }
}
