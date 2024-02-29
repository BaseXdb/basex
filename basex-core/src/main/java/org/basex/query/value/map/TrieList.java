package org.basex.query.value.map;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;
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
  TrieNode delete(final int hs, final Item key, final int level)
      throws QueryException {

    if(hs == hash) {
      for(int i = size; i-- > 0;) {
        // still collisions?
        if(key.atomicEqual(keys[i])) {
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
  TrieNode put(final int hs, final Item key, final Value value, final int level)
      throws QueryException {

    // same hash, replace or merge
    if(hs == hash) {
      for(int i = keys.length; i-- > 0;) {
        if(key.atomicEqual(keys[i])) {
          // replace value
          final Value[] vs = values.clone();
          vs[i] = value;
          return new TrieList(hs, keys, vs);
        }
      }
      return new TrieList(hash, Array.add(keys, key), Array.add(values, value));
    }

    // different hash, branch
    final TrieNode[] ch = new TrieNode[KIDS];
    final int a = key(hs, level), b = key(hash, level);
    final int used;
    if(a == b) {
      ch[a] = put(hs, key, value, level + 1);
      used = 1 << a;
    } else {
      ch[a] = new TrieLeaf(hs, key, value);
      ch[b] = this;
      used = 1 << a | 1 << b;
    }
    // we definitely inserted one value
    return new TrieBranch(ch, used, size + 1);
  }

  @Override
  Value get(final int hs, final Item key, final int level)
      throws QueryException {
    if(hs == hash) {
      for(int k = keys.length; k-- != 0;) {
        if(key.atomicEqual(keys[k])) return values[k];
      }
    }
    return null;
  }

  @Override
  boolean contains(final int hs, final Item key, final int level)
      throws QueryException {
    if(hs == hash) {
      for(int k = keys.length; k-- != 0;)
        if(key.atomicEqual(keys[k])) return true;
    }
    return false;
  }

  @Override
  TrieNode addAll(final TrieNode node, final int level, final MergeDuplicates merge,
      final QueryContext qc, final InputInfo info) throws QueryException {
    return node.add(this, level, merge, qc, info);
  }

  @Override
  TrieNode add(final TrieLeaf leaf, final int level, final MergeDuplicates merge,
      final QueryContext qc, final InputInfo info) throws QueryException {

    qc.checkStop();
    if(hash == leaf.hash) {
      for(int k = keys.length; k-- > 0;) {
        if(leaf.key.atomicEqual(keys[k])) {
          switch(merge) {
            case USE_FIRST:
            case USE_ANY:
              final Value[] uf = values.clone();
              uf[k] = leaf.value;
              return new TrieList(hash, keys, uf);
            case USE_LAST:
              return this;
            case COMBINE:
              final Value[] cm = values.clone();
              cm[k] = ValueBuilder.concat(leaf.value, cm[k], qc);
              return new TrieList(hash, keys, cm);
            default:
              throw MERGE_DUPLICATE_X.get(info, leaf.key);
          }
        }
      }
      return new TrieList(hash, Array.add(keys, leaf.key), Array.add(values, leaf.value));
    }

    final TrieNode[] ch = new TrieNode[KIDS];
    final int k = key(hash, level), ok = key(leaf.hash, level), nu;

    // same key? add recursively
    if(k == ok) {
      ch[k] = add(leaf, level + 1, merge, qc, info);
      nu = 1 << k;
    } else {
      ch[k] = this;
      ch[ok] = leaf;
      nu = 1 << k | 1 << ok;
    }

    return new TrieBranch(ch, nu, size + 1);
  }

  @Override
  TrieNode add(final TrieList list, final int level, final MergeDuplicates merge,
      final QueryContext qc, final InputInfo info) throws QueryException {

    qc.checkStop();
    if(hash == list.hash) {
      final Value[] vs0 = list.values;
      Item[] ks = list.keys;
      Value[] vs = vs0;
      final BitSet unmatched = new BitSet(list.size);
      unmatched.set(0, list.size);

      OUTER:
      for(int i = 0; i < size; i++) {
        final Item ok = keys[i];
        // check if the key is already in the list
        for(int j = unmatched.nextSetBit(0); j >= 0; j = unmatched.nextSetBit(j + 1)) {
          final Item k = list.keys[j];
          if(k.atomicEqual(ok)) {
            unmatched.clear(j);
            switch(merge) {
              case USE_FIRST:
              case USE_ANY:
                // no change, skip the entry
                break;
              case USE_LAST:
                // left value is overwritten
                if(vs == vs0) vs = vs0.clone();
                vs[j] = values[i];
                break;
              case COMBINE:
                // right value is appended
                if(vs == vs0) vs = vs0.clone();
                vs[j] = ValueBuilder.concat(vs[j], values[i], qc);
                break;
              default:
                throw MERGE_DUPLICATE_X.get(info, k);
            }
            continue OUTER;
          }
        }
        // key is not in this list, add it
        ks = Array.add(ks, ok);
        vs = Array.add(vs, values[i]);
      }
      return vs == vs0 ? list : new TrieList(hash, ks, vs);
    }

    final TrieNode[] ch = new TrieNode[KIDS];
    final int k = key(hash, level), ok = key(list.hash, level), nu;

    // same key? add recursively
    if(k == ok) {
      ch[k] = add(list, level + 1, merge, qc, info);
      nu = 1 << k;
    } else {
      ch[k] = this;
      ch[ok] = list;
      nu = 1 << k | 1 << ok;
    }
    return new TrieBranch(ch, nu, size + list.size);
  }

  @Override
  TrieNode add(final TrieBranch branch, final int level, final MergeDuplicates merge,
      final QueryContext qc, final InputInfo info) throws QueryException {

    final int k = key(hash, level);
    final TrieNode[] ch = branch.copyKids();
    final TrieNode old = ch[k];
    ch[k] = old == null ? this : old.addAll(this, level + 1, merge, qc, info);
    return new TrieBranch(ch, branch.used | 1 << k,
        branch.size + size - (old != null ? old.size : 0));
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
  void keys(final ItemList ks) {
    for(final Item key : keys) ks.add(key);
  }

  @Override
  void values(final ValueBuilder vs) {
    for(final Value value : values) vs.add(value);
  }

  @Override
  void cache(final boolean lazy, final InputInfo info) throws QueryException {
    for(int i = 0; i < size; i++) {
      keys[i].cache(lazy, info);
      values[i].cache(lazy, info);
    }
  }

  @Override
  public boolean materialized(final Predicate<Data> test, final InputInfo info)
      throws QueryException {
    for(final Value value : values)  {
      if(!value.materialized(test, info)) return false;
    }
    return true;
  }

  @Override
  void apply(final QueryBiConsumer<Item, Value> func) throws QueryException {
    for(int i = 0; i < size; i++) func.accept(keys[i], values[i]);
  }

  @Override
  boolean instanceOf(final AtomType kt, final SeqType dt) {
    if(kt != null) {
      for(final Item key : keys) {
        if(!key.type.instanceOf(kt)) return false;
      }
    }
    if(dt != null) {
      for(final Value value : values) {
        if(!dt.instance(value)) return false;
      }
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

  @Override
  void add(final TokenBuilder tb) {
    final int kl = keys.length;
    for(int k = 0; k < kl && tb.moreInfo(); k++) {
      tb.add(keys[k]).add(MAPASG).add(values[k]).add(SEP);
    }
  }
}
