package org.basex.query.util.map;

import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Value;
import org.basex.query.iter.ItemCache;
import org.basex.util.InputInfo;

/**
 * A persistent hash map based on a Trie.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public final class HashTrie {
  /** Number of bits used per level. */
  static final int BITS = 5;

  /** Empty map. */
  private static final HashTrie EMPTY = new HashTrie(TrieNode.EMPTY);

  /** Root node. */
  private final TrieNode root;

  /**
   * Constructor.
   * @param r root node
   */
  private HashTrie(final TrieNode r) {
    root = r;
  }

  /**
   * Inserts the given value into this map.
   * @param key key to insert
   * @param value value to insert
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public HashTrie insert(final Item key, final Value value, final InputInfo ii)
      throws QueryException {
    final TrieNode ins = root.insert(key.hash(ii), key, value, 0, ii);
    return ins == root ? this : new HashTrie(ins);
  }

  /**
   * Deletes a key from this map.
   * @param key key to delete
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public HashTrie delete(final Item key, final InputInfo ii)
      throws QueryException {
    final TrieNode del = root.delete(key.hash(ii), key, 0, ii);
    return del == root ? this : del != null ? new HashTrie(del) : EMPTY;
  }

  /**
   * Looks up the value associated with the given key.
   * @param key key to look up
   * @param ii input info
   * @return bound value if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  public Value get(final Item key, final InputInfo ii)
      throws QueryException {
    return root.get(key.hash(ii), key, 0, ii);
  }

  /**
   * Checks if the given key exists in the map.
   * @param key key to look for
   * @param ii input info
   * @return {@code true}, if the key exists, {@code false} otherwise
   * @throws QueryException query exception
   */
  public boolean contains(final Item key, final InputInfo ii)
      throws QueryException {
    return root.contains(key.hash(ii), key, 0, ii);
  }

  /**
   * Adds all bindings from the given map into {@code this}.
   * @param trie map to add
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public HashTrie addAll(final HashTrie trie, final InputInfo ii)
      throws QueryException {
    if(trie == EMPTY) return this;
    final TrieNode upd = root.addAll(trie.root, 0, ii);
    return upd == trie.root ? trie : new HashTrie(upd);
  }

  /**
   * Number of values contained in this map.
   * @return size
   */
  public int size() {
    return root.size;
  }

  /**
   * All keys defined in this map.
   * @return list of keys
   */
  public ItemCache keys() {
    final ItemCache res = new ItemCache(root.size);
    root.keys(res);
    return res;
  }

  /**
   * Verifies the data structure.
   * @return result of sanity checks
   */
  public boolean verify() {
    return root.verify();
  }

  /**
   * The empty map.
   * @return empty map
   */
  public static HashTrie empty() {
    return EMPTY;
  }

  @Override
  public String toString() {
    return root.toString();
  }
}
