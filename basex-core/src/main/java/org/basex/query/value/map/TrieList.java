package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Leaf that contains a collision list of keys with the same hash code.
 *
 * @author BaseX Team 2005-24, BSD License
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
    assert verify();
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
  TrieNode delete(final int hs, final Item ky, final int lv) throws QueryException {
    if(hs == hash) {
      for(int i = size; i-- > 0;) {
        // still collisions?
        if(ky.atomicEqual(keys[i])) {
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
  TrieNode put(final int hs, final Item ky, final Value vl, final int lv)
      throws QueryException {

    // different hash: create branch
    if(hs != hash) return branch(hs, ky, vl, lv, hash, size);

    // same hash, same value: replace
    for(int i = keys.length; i-- > 0;) {
      if(ky.atomicEqual(keys[i])) {
        final Value[] vs = values.clone();
        vs[i] = vl;
        return new TrieList(hs, keys, vs);
      }
    }
    // same hash, different value: add to list
    return new TrieList(hash, Array.add(keys, ky), Array.add(values, vl));
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
  boolean verify() {
    try {
      for(int i = 1; i < size; i++) {
        for(int j = i; j-- > 0;) {
          if(keys[i].atomicEqual(keys[j])) return false;
        }
      }
    } catch(final QueryException ex) {
      Util.debug(ex);
      return false;
    }
    return true;
  }

  @Override
  void apply(final QueryBiConsumer<Item, Value> func) throws QueryException {
    for(int i = 0; i < size; i++) func.accept(keys[i], values[i]);
  }

  @Override
  boolean test(final QueryBiPredicate<Item, Value> func) throws QueryException {
    for(int i = 0; i < size; i++) {
      if(!func.test(keys[i], values[i])) return false;
    }
    return true;
  }

  @Override
  boolean equal(final TrieNode node, final DeepEqual deep) throws QueryException {
    if(!(node instanceof TrieList) || size != node.size) return false;

    // do the evil nested-loop thing
    final TrieList ol = (TrieList) node;
    OUTER:
    for(int i = 0; i < size; i++) {
      if(deep != null && deep.qc != null) deep.qc.checkStop();
      final Item key = keys[i];
      final Value value = values[i];
      for(int j = 0; j < size; j++) {
        if(deep != null) {
          if(!key.atomicEqual(ol.keys[j])) continue;
          if(!deep.equal(value, ol.values[j])) return false;
        } else {
          if(!key.equals(ol.keys[j])) continue;
          if(!value.equals(ol.values[j])) return false;
        }
        continue OUTER;
      }
      // all keys of the other list were checked, none matched
      return false;
    }
    // all entries were found
    return true;
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
