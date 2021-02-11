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
 * @author BaseX Team 2005-21, BSD License
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
    TrieNode delete(final int hash, final Item key, final int level, final InputInfo ii) {
      return this; }
    @Override
    Value get(final int hash, final Item key, final int level, final InputInfo ii) {
      return null; }
    @Override
    boolean contains(final int hash, final Item key, final int level, final InputInfo ii) {
      return false; }
    @Override
    TrieNode addAll(final TrieNode node, final int level, final MergeDuplicates merge,
        final QueryContext qc, final InputInfo ii) { return node; }
    @Override
    TrieNode add(final TrieLeaf leaf, final int level, final MergeDuplicates merge,
        final QueryContext qc, final InputInfo ii) { return leaf; }
    @Override
    TrieNode add(final TrieList list, final int level, final MergeDuplicates merge,
        final QueryContext qc, final InputInfo ii) { return list; }
    @Override
    TrieNode add(final TrieBranch branch, final int level, final MergeDuplicates merge,
        final QueryContext qc, final InputInfo ii) { return branch; }
    @Override
    boolean verify() { return true; }
    @Override
    void keys(final ItemList ks) { }
    @Override
    void values(final ValueBuilder vs) { }
    @Override
    void cache(final boolean lazy, final InputInfo ii) { }
    @Override
    boolean materialized() { return true; }
    @Override
    boolean instanceOf(final AtomType kt, final SeqType dt) { return true; }
    @Override
    int hash(final InputInfo ii) { return 0; }
    @Override
    boolean deep(final TrieNode node, final Collation coll, final InputInfo ii) {
return this == node; }
    @Override
    public TrieNode put(final int hash, final Item key, final Value value, final int level,
        final InputInfo ii) { return new TrieLeaf(hash, key, value); }
    @Override
    void forEach(final ValueBuilder vb, final FItem func, final QueryContext qc,
        final InputInfo ii) { }
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
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode put(int hash, Item key, Value value, int level, InputInfo ii)
      throws QueryException;

  /**
   * Deletes a key from this map.
   * @param hash hash code of the key
   * @param key key to delete
   * @param level level
   * @param ii input info
   * @return updated map if changed, {@code null} if deleted, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode delete(int hash, Item key, int level, InputInfo ii) throws QueryException;

  /**
   * Looks up the value associated with the given key.
   * @param hash hash code
   * @param key key to look up
   * @param level level
   * @param ii input info
   * @return bound value if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  abstract Value get(int hash, Item key, int level, InputInfo ii) throws QueryException;

  /**
   * Checks if the given key exists in the map.
   * @param hash hash code
   * @param key key to look for
   * @param level level
   * @param ii input info
   * @return {@code true} if the key exists, {@code false} otherwise
   * @throws QueryException query exception
   */
  abstract boolean contains(int hash, Item key, int level, InputInfo ii) throws QueryException;

  /**
   * <p> Inserts all bindings from the given node into this one.
   * <p> This method is part of the <i>double dispatch</i> pattern and
   *     should be implemented as {@code return o.add(this, lvl, ii);}.
   * @param node other node
   * @param level level
   * @param merge merge duplicates
   * @param qc query context
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode addAll(TrieNode node, int level, MergeDuplicates merge, QueryContext qc,
      InputInfo ii) throws QueryException;

  /**
   * Add a leaf to this node if the key isn't already used.
   * @param leaf leaf to insert
   * @param level level
   * @param merge merge duplicates
   * @param qc query context
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode add(TrieLeaf leaf, int level, MergeDuplicates merge, QueryContext qc,
      InputInfo ii) throws QueryException;

  /**
   * Add an overflow list to this node if the key isn't already used.
   * @param list leaf to insert
   * @param level level
   * @param merge merge duplicates
   * @param qc query context
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode add(TrieList list, int level, MergeDuplicates merge, QueryContext qc,
      InputInfo ii) throws QueryException;

  /**
   * Add all bindings of the given branch to this node for which the key isn't
   * already used.
   * @param branch leaf to insert
   * @param level level
   * @param merge merge duplicates
   * @param qc query context
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode add(TrieBranch branch, int level, MergeDuplicates merge, QueryContext qc,
      InputInfo ii) throws QueryException;

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
   * @param ii input info
   * @throws QueryException query exception
   */
  abstract void cache(boolean lazy, InputInfo ii) throws QueryException;

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
   * @param ii input info
   * @throws QueryException query exception
   */
  abstract void forEach(ValueBuilder vb, FItem func, QueryContext qc, InputInfo ii)
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
   * @param ii input info
   * @return {@code true} if both values are deep equal, {@code false} otherwise
   * @throws QueryException query exception
   */
  static boolean deep(final Value value1, final Value value2, final Collation coll,
      final InputInfo ii) throws QueryException {
    return value1.size() == value2.size() && new DeepEqual(ii).collation(coll).
        equal(value1, value2);
  }

  /**
   * Calculates the hash code of this node.
   * @param ii input info
   * @return hash value
   * @throws QueryException query exception
   */
  abstract int hash(InputInfo ii) throws QueryException;

  /**
   * Checks if this node is indistinguishable from the given node.
   * @param node other node
   * @param coll collation
   * @param ii input info
   * @return result of check
   * @throws QueryException query exception
   */
  abstract boolean deep(TrieNode node, Collation coll, InputInfo ii) throws QueryException;

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
