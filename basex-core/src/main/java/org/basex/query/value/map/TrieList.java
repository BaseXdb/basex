package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Leaf that contains a collision list of keys with the same hash code.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
final class TrieList extends TrieNode {
  /** Common hash value of all contained values. */
  final int hash;
  /** List of keys of this collision list. */
  final Item[] keys;
  /** List of values of this collision list. */
  final Value[] values;

  /**
   * Constructor.
   * @param hash hash value
   * @param keys key array
   * @param values value array
   */
  TrieList(final int hash, final Item[] keys, final Value[] values) {
    super(keys.length);
    this.keys = keys;
    this.values = values;
    this.hash = hash;
  }

  /**
   * Constructor for creating a collision list from two bindings.
   * @param hash hash value
   * @param key1 first key
   * @param value1 first value
   * @param key2 second key
   * @param value2 second value
   */
  TrieList(final int hash, final Item key1, final Value value1, final Item key2,
      final Value value2) {
    this(hash, new Item[]{ key1, key2 }, new Value[]{ value1, value2 });
  }

  @Override
  TrieNode put(final int hs, final int lv, final TrieUpdate update) throws QueryException {
    // different hash: proceed recursively
    if(hs != hash) return branch(hs, lv, hash, size, update);
    // same hash...
    for(int i = keys.length; i-- > 0;) {
      final Item key = keys[i];
      if(key.atomicEqual(update.key)) {
        // same key...
        if(values[i] == update.value) {
          // same value: return existing instance
          return this;
        }
        // different value: replace value
        final Value[] vs = values.clone();
        vs[i] = update.value;
        return new TrieList(hs, keys, vs);
      }
    }
    // different key: add key and value
    update.add();
    return new TrieList(hash, Array.add(keys, update.key), Array.add(values, update.value));
  }

  @Override
  TrieNode remove(final int hs, final int lv, final TrieUpdate update) throws QueryException {
    if(hs == hash) {
      for(int i = size; i-- > 0;) {
        // still collisions?
        if(keys[i].atomicEqual(update.key)) {
          update.remove(keys[i]);
          // found entry
          if(size == 2) {
            // single leaf remains
            final int o = i ^ 1;
            return new TrieLeaf(hs, keys[o], values[o]);
          }
          // create new arrays (modified due to #1297; performance improved)
          final int s = size - 1;
          final Item[] ks = new Item[s];
          Array.copy(keys, i, ks);
          Array.copy(keys, i + 1, s - i, ks, i);
          final Value[] vs = new Value[s];
          Array.copy(values, i, vs);
          Array.copy(values, i + 1, s - i, vs, i);
          return new TrieList(hs, ks, vs);
        }
      }
    }
    return this;
  }

  @Override
  Value get(final int hs, final Item ky, final int lv) throws QueryException {
    if(hs == hash) {
      for(int k = keys.length; k-- != 0;) {
        if(ky.atomicEqual(keys[k])) return values[k];
      }
    }
    return null;
  }

  @Override
  void add(final TokenBuilder tb, final String indent) {
    tb.add(indent).add("`-- Collision (").add(Integer.toHexString(hash)).add("):\n");
    final int kl = keys.length;
    for(int k = 0; k < kl; k++) {
      tb.add(indent).add("      ").add(keys[k]).add(" => ").add(values[k]).add('\n');
    }
  }
}
