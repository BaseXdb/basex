package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Abstract superclass of all trie nodes.
 *
 * @author BaseX Team, BSD License
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
   * @return updated map, {@code this} otherwise
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
      update.add();
      ch[a] = new TrieLeaf(hs, update.key, update.value);
      ch[b] = this;
      used = 1 << a | 1 << b;
    }
    return new TrieBranch(ch, used, sz + 1);
  }

  /**
   * Builds a node for a range of entries.
   * @param keys keys
   * @param values values
   * @param hashes hash codes of the keys
   * @param indexes indexes of the entries in the range
   * @param tmp array with the same size, receiving the partitioned indexes
   * @param from first index position
   * @param to last index position (exclusive)
   * @param level level
   * @return node
   */
  static TrieNode build(final Item[] keys, final Value[] values, final int[] hashes,
      final int[] indexes, final int[] tmp, final int from, final int to, final int level) {
    final int first = indexes[from], hash = hashes[first];
    if(to - from == 1) return new TrieLeaf(hash, keys[first], values[first]);

    // identical hash codes: collision list
    int i = from + 1;
    while(i < to && hashes[indexes[i]] == hash) i++;
    if(i == to) {
      final int n = to - from;
      final Item[] ks = new Item[n];
      final Value[] vs = new Value[n];
      for(int j = 0; j < n; j++) {
        final int e = indexes[from + j];
        ks[j] = keys[e];
        vs[j] = values[e];
      }
      return new TrieList(hash, ks, vs);
    }

    // partition entries by the hash key of the current level, preserving their order
    final int[] ends = new int[KIDS];
    for(int j = from; j < to; j++) ends[hashKey(hashes[indexes[j]], level)]++;
    for(int k = 0, s = from; k < KIDS; k++) {
      final int c = ends[k];
      ends[k] = s;
      s += c;
    }
    for(int j = from; j < to; j++) {
      final int e = indexes[j];
      tmp[ends[hashKey(hashes[e], level)]++] = e;
    }

    final TrieNode[] kids = new TrieNode[KIDS];
    int used = 0;
    for(int k = 0, s = from; k < KIDS; k++) {
      final int e = ends[k];
      if(s < e) {
        kids[k] = build(keys, values, hashes, tmp, indexes, s, e, level + 1);
        used |= 1 << k;
      }
      s = e;
    }
    return new TrieBranch(kids, used, to - from);
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
   * Calculates the hash key for the given level.
   * @param hash hash value
   * @param level current level
   * @return hash key
   */
  static int hashKey(final int hash, final int level) {
    return hash >>> level * BITS & MASK;
  }

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
