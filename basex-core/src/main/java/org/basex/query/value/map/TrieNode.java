package org.basex.query.value.map;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract superclass of all trie nodes.
 *
 * @author BaseX Team 2005-18, BSD License
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
        final InputInfo info, final QueryContext qc) { return node; }
    @Override
    TrieNode add(final TrieLeaf leaf, final int level, final MergeDuplicates merge,
        final InputInfo info, final QueryContext qc) { return leaf; }
    @Override
    TrieNode add(final TrieList list, final int level, final MergeDuplicates merge,
        final InputInfo info, final QueryContext qc) { return list; }
    @Override
    TrieNode add(final TrieBranch branch, final int level, final MergeDuplicates merge,
        final InputInfo info, final QueryContext qc) { return branch; }
    @Override
    boolean verify() { return true; }
    @Override
    void keys(final ItemList ks) { }
    @Override
    void values(final ValueBuilder vs) { }
    @Override
    void cache(final InputInfo info, final boolean lazy) { }
    @Override
    boolean materialized() { return true; }
    @Override
    boolean instanceOf(final AtomType kt, final SeqType dt) { return true; }
    @Override
    int hash(final InputInfo info) { return 0; }
    @Override
    boolean deep(final InputInfo info, final TrieNode node, final Collation coll) {
      return this == node; }
    @Override
    public TrieNode put(final int hash, final Item key, final Value value, final int level,
        final InputInfo info) { return new TrieLeaf(hash, key, value); }
    @Override
    void forEach(final ValueBuilder vb, final FItem func, final QueryContext qc,
        final InputInfo info) { }
    @Override
    StringBuilder append(final StringBuilder sb, final String indent) { return sb.append("{ }"); }
    @Override
    StringBuilder append(final StringBuilder sb) { return sb; }
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
   * @param info input info
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
   * @param info input info
   * @return updated map if changed, {@code null} if deleted, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode delete(int hash, Item key, int level, InputInfo info) throws QueryException;

  /**
   * Looks up the value associated with the given key.
   * @param hash hash code
   * @param key key to look up
   * @param level level
   * @param info input info
   * @return bound value if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  abstract Value get(int hash, Item key, int level, InputInfo info) throws QueryException;

  /**
   * Checks if the given key exists in the map.
   * @param hash hash code
   * @param key key to look for
   * @param level level
   * @param info input info
   * @return {@code true} if the key exists, {@code false} otherwise
   * @throws QueryException query exception
   */
  abstract boolean contains(int hash, Item key, int level, InputInfo info) throws QueryException;

  /**
   * <p> Inserts all bindings from the given node into this one.
   * <p> This method is part of the <i>double dispatch</i> pattern and
   *     should be implemented as {@code return o.add(this, lvl, ii);}.
   * @param node other node
   * @param level level
   * @param merge merge duplicates
   * @param info input info
   * @param qc query context
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode addAll(TrieNode node, int level, MergeDuplicates merge, InputInfo info,
      QueryContext qc) throws QueryException;

  /**
   * Add a leaf to this node if the key isn't already used.
   * @param leaf leaf to insert
   * @param level level
   * @param merge merge duplicates
   * @param info input info
   * @param qc query context
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode add(TrieLeaf leaf, int level, MergeDuplicates merge, InputInfo info,
      QueryContext qc) throws QueryException;

  /**
   * Add an overflow list to this node if the key isn't already used.
   * @param list leaf to insert
   * @param level level
   * @param merge merge duplicates
   * @param info input info
   * @param qc query context
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode add(TrieList list, int level, MergeDuplicates merge, InputInfo info,
      QueryContext qc) throws QueryException;

  /**
   * Add all bindings of the given branch to this node for which the key isn't
   * already used.
   * @param branch leaf to insert
   * @param level level
   * @param merge merge duplicates
   * @param info input info
   * @param qc query context
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode add(TrieBranch branch, int level, MergeDuplicates merge, InputInfo info,
      QueryContext qc) throws QueryException;

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
   * @param info input info
   * @param lazy lazy caching
   * @throws QueryException query exception
   */
  abstract void cache(InputInfo info, boolean lazy) throws QueryException;

  /**
   * Checks if all values are materialized.
   * @return result of check
   */
  abstract boolean materialized();

  /**
   * Applies a function on all entries.
   * @param vb value builder
   * @param func function to apply on keys and values
   * @param qc query context
   * @param info input info
   * @throws QueryException query exception
   */
  abstract void forEach(ValueBuilder vb, FItem func, QueryContext qc, InputInfo info)
      throws QueryException;

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
   * Compares two values.
   * @param value1 first value
   * @param value2 second value
   * @param coll collation (can be {@code null})
   * @param info input info
   * @return {@code true} if both values are deep equal, {@code false} otherwise
   * @throws QueryException query exception
   */
  static boolean deep(final Value value1, final Value value2, final Collation coll,
      final InputInfo info) throws QueryException {
    return value1.size() == value2.size() && new DeepEqual(info).collation(coll).
        equal(value1, value2);
  }

  /**
   * Calculates the hash code of this node.
   * @param info input info
   * @return hash value
   * @throws QueryException query exception
   */
  abstract int hash(InputInfo info) throws QueryException;

  /**
   * Checks if this node is indistinguishable from the given node.
   * @param info input info
   * @param node other node
   * @param coll collation
   * @return result of check
   * @throws QueryException query exception
   */
  abstract boolean deep(InputInfo info, TrieNode node, Collation coll) throws QueryException;

  /**
   * Recursive {@link #toString()} helper.
   * @param sb string builder
   * @param indent indentation string
   * @return string builder for convenience
   */
  abstract StringBuilder append(StringBuilder sb, String indent);

  /**
   * Recursive helper for {@link XQMap#toString()}.
   * @param sb string builder
   * @return reference to {@code sb}
   */
  abstract StringBuilder append(StringBuilder sb);

  @Override
  public String toString() {
    return append(new StringBuilder(), "").toString();
  }

  /**
   * Checks if string building should be continued.
   * @param sb string builder
   * @return result of check
   */
  static boolean more(final StringBuilder sb) {
    if(sb.length() <= 32) return true;
    if(!sb.substring(sb.length() - DOTS.length()).equals(DOTS)) sb.append(DOTS);
    return false;
  }
}
