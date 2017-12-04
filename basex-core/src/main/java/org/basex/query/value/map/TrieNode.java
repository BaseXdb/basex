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
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
abstract class TrieNode {
  /** Number of children on each level. */
  static final int KIDS = 1 << Map.BITS;
  /** Mask for the bits used on the current level. */
  private static final int MASK = KIDS - 1;

  /** The empty node. */
  static final TrieNode EMPTY = new TrieNode(0) {
    @Override
    TrieNode delete(final int h, final Item k, final int l, final InputInfo i) { return this; }
    @Override
    Value get(final int h, final Item k, final int l, final InputInfo i) { return null; }
    @Override
    boolean contains(final int h, final Item k, final int l, final InputInfo ii) { return false; }
    @Override
    TrieNode addAll(final TrieNode o, final int l, final MergeDuplicates merge,
        final InputInfo ii, final QueryContext qc) { return o; }
    @Override
    TrieNode add(final TrieLeaf o, final int l, final MergeDuplicates merge,
        final InputInfo ii, final QueryContext qc) { return o; }
    @Override
    TrieNode add(final TrieList o, final int l, final MergeDuplicates merge,
        final InputInfo ii, final QueryContext qc) { return o; }
    @Override
    TrieNode add(final TrieBranch o, final int l, final MergeDuplicates merge,
        final InputInfo ii, final QueryContext qc) { return o; }
    @Override
    boolean verify() { return true; }
    @Override
    void keys(final ItemList ks) { }
    @Override
    void values(final ValueBuilder vs) { }
    @Override
    void materialize(final InputInfo ii) { }
    @Override
    boolean instanceOf(final AtomType kt, final SeqType dt) { return true; }
    @Override
    int hash(final InputInfo ii) { return 0; }
    @Override
    boolean deep(final InputInfo ii, final TrieNode o, final Collation coll) { return this == o; }
    @Override
    public TrieNode put(final int h, final Item k, final Value v, final int l,
        final InputInfo i) { return new TrieLeaf(h, k, v); }
    @Override
    void forEach(final ValueBuilder vb, final FItem func, final QueryContext qc,
        final InputInfo ii) { }
    @Override
    StringBuilder append(final StringBuilder sb, final String ind) { return sb.append("{ }"); }
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
   * @param val value to insert
   * @param lvl level
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode put(int hash, Item key, Value val, int lvl, InputInfo ii) throws QueryException;

  /**
   * Deletes a key from this map.
   * @param hash hash code of the key
   * @param key key to delete
   * @param lvl level
   * @param ii input info
   * @return updated map if changed, {@code null} if deleted, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode delete(int hash, Item key, int lvl, InputInfo ii) throws QueryException;

  /**
   * Looks up the value associated with the given key.
   * @param hash hash code
   * @param key key to look up
   * @param lvl level
   * @param ii input info
   * @return bound value if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  abstract Value get(int hash, Item key, int lvl, InputInfo ii) throws QueryException;

  /**
   * Checks if the given key exists in the map.
   * @param hash hash code
   * @param key key to look for
   * @param lvl level
   * @param ii input info
   * @return {@code true} if the key exists, {@code false} otherwise
   * @throws QueryException query exception
   */
  abstract boolean contains(int hash, Item key, int lvl, InputInfo ii) throws QueryException;

  /**
   * <p> Inserts all bindings from the given node into this one.
   * <p> This method is part of the <i>double dispatch</i> pattern and
   *     should be implemented as {@code return o.add(this, lvl, ii);}.
   * @param o other node
   * @param lvl level
   * @param merge merge duplicates
   * @param ii input info
   * @param qc query context
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode addAll(TrieNode o, int lvl, MergeDuplicates merge, InputInfo ii,
      QueryContext qc) throws QueryException;

  /**
   * Add a leaf to this node if the key isn't already used.
   * @param o leaf to insert
   * @param lvl level
   * @param merge merge duplicates
   * @param ii input info
   * @param qc query context
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode add(TrieLeaf o, int lvl, MergeDuplicates merge, InputInfo ii, QueryContext qc)
      throws QueryException;

  /**
   * Add an overflow list to this node if the key isn't already used.
   * @param o leaf to insert
   * @param lvl level
   * @param merge merge duplicates
   * @param ii input info
   * @param qc query context
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode add(TrieList o, int lvl, MergeDuplicates merge, InputInfo ii,
      QueryContext qc) throws QueryException;

  /**
   * Add all bindings of the given branch to this node for which the key isn't
   * already used.
   * @param o leaf to insert
   * @param lvl level
   * @param merge merge duplicates
   * @param ii input info
   * @param qc query context
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  abstract TrieNode add(TrieBranch o, int lvl, MergeDuplicates merge, InputInfo ii,
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
   * Materializes all keys and values.
   * @param ii input info
   * @throws QueryException query exception
   */
  abstract void materialize(InputInfo ii) throws QueryException;

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
   * @param lvl current level
   * @return hash key
   */
  static int key(final int hash, final int lvl) {
    return hash >>> lvl * Map.BITS & MASK;
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
   * @param a first value
   * @param b second value
   * @param coll collation (can be {@code null})
   * @param ii input info
   * @return {@code true} if both values are deep equal, {@code false} otherwise
   * @throws QueryException query exception
   */
  static boolean deep(final Value a, final Value b, final Collation coll, final InputInfo ii)
      throws QueryException {
    return a.size() == b.size() && new DeepEqual(ii).collation(coll).equal(a, b);
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
   * @param ii input info
   * @param o other node
   * @param coll collation
   * @return result of check
   * @throws QueryException query exception
   */
  abstract boolean deep(InputInfo ii, TrieNode o, Collation coll) throws QueryException;

  /**
   * Recursive {@link #toString()} helper.
   * @param sb string builder
   * @param ind indentation string
   * @return string builder for convenience
   */
  abstract StringBuilder append(StringBuilder sb, String ind);

  /**
   * Recursive helper for {@link Map#toString()}.
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
