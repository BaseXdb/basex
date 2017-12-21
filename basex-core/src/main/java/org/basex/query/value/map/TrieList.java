package org.basex.query.value.map;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Leaf that contains a collision list of keys with the same hash code.
 *
 * @author BaseX Team 2005-17, BSD License
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
  TrieNode delete(final int hs, final Item key, final int level, final InputInfo info)
      throws QueryException {

    if(hs == hash) {
      for(int i = size; i-- > 0;) {
        // still collisions?
        if(key.sameKey(keys[i], info)) {
          // found entry
          if(size == 2) {
            // single leaf remains
            final int o = i ^ 1;
            return new TrieLeaf(hs, keys[o], values[o]);
          }
          // create new arrays (modified due to #1297; performance improved)
          final int s = size - 1;
          final Item[] ks = new Item[s];
          System.arraycopy(keys, 0, ks, 0, i);
          System.arraycopy(keys, i + 1, ks, i, s - i);
          final Value[] vs = new Value[s];
          System.arraycopy(values, 0, vs, 0, i);
          System.arraycopy(values, i + 1, vs, i, s - i);
          return new TrieList(hs, ks, vs);
        }
      }
    }
    return this;
  }

  @Override
  TrieNode put(final int hs, final Item key, final Value value, final int level,
      final InputInfo info) throws QueryException {

    // same hash, replace or merge
    if(hs == hash) {
      for(int i = keys.length; i-- > 0;) {
        if(key.sameKey(keys[i], info)) {
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
      ch[a] = put(hs, key, value, level + 1, info);
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
  Value get(final int hs, final Item key, final int level, final InputInfo info)
      throws QueryException {
    if(hs == hash) {
      for(int k = keys.length; k-- != 0;)
      if(key.sameKey(keys[k], info)) return values[k];
    }
    return null;
  }

  @Override
  boolean contains(final int hs, final Item key, final int level, final InputInfo info)
      throws QueryException {
    if(hs == hash) {
      for(int k = keys.length; k-- != 0;)
        if(key.sameKey(keys[k], info)) return true;
    }
    return false;
  }

  @Override
  TrieNode addAll(final TrieNode node, final int level, final MergeDuplicates merge,
      final InputInfo info, final QueryContext qc) throws QueryException {
    return node.add(this, level, merge, info, qc);
  }

  @Override
  TrieNode add(final TrieLeaf leaf, final int level, final MergeDuplicates merge,
      final InputInfo info, final QueryContext qc) throws QueryException {

    qc.checkStop();
    if(hash == leaf.hash) {
      for(int k = keys.length; k-- > 0;) {
        if(leaf.key.sameKey(keys[k], info)) {
          switch(merge) {
            case USE_FIRST:
            case UNSPECIFIED:
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
    final int k = key(hash, level), ok = key(leaf.hash, level);
    final int nu;

    // same key? add recursively
    if(k == ok) {
      ch[k] = add(leaf, level + 1, merge, info, qc);
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
      final InputInfo info, final QueryContext qc) throws QueryException {

    qc.checkStop();
    if(hash == list.hash) {
      Item[] ks = keys;
      Value[] vs = values;

      OUTER:
      for(int i = 0; i < size; i++) {
        final Item ok = list.keys[i];
        // skip all entries that are overridden
        for(final Item k : keys) if(k.sameKey(ok, info)) continue OUTER;
        // key is not in this list, add it
        ks = Array.add(ks, ok);
        vs = Array.add(vs, list.values[i]);
      }
      return ks == keys ? this : new TrieList(hash, ks, vs);
    }

    final TrieNode[] ch = new TrieNode[KIDS];
    final int k = key(hash, level), ok = key(list.hash, level);
    final int nu;

    // same key? add recursively
    if(k == ok) {
      ch[k] = add(list, level + 1, merge, info, qc);
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
      final InputInfo info, final QueryContext qc) throws QueryException {

    final int k = key(hash, level);
    final TrieNode[] ch = branch.copyKids();
    final TrieNode old = ch[k];
    ch[k] = old == null ? this : old.addAll(this, level + 1, merge, info, qc);
    return new TrieBranch(ch, branch.used | 1 << k,
        branch.size + size - (old != null ? old.size : 0));
  }

  @Override
  boolean verify() {
    try {
      for(int i = 1; i < size; i++) {
        for(int j = i; j-- > 0;) {
          if(keys[i].sameKey(keys[j], null)) return false;
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
  void materialize(final InputInfo info) throws QueryException {
    for(int i = 0; i < size; i++) {
      keys[i].materialize(info);
      values[i].materialize(info);
    }
  }

  @Override
  void forEach(final ValueBuilder vb, final FItem func, final QueryContext qc, final InputInfo info)
      throws QueryException {
    for(int i = 0; i < size; i++) vb.add(func.invokeValue(qc, info, keys[i], values[i]));
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
  int hash(final InputInfo info) throws QueryException {
    int h = hash;
    // order isn't important, operation has to be commutative
    for(int i = size; --i >= 0;) h ^= values[i].hash(info);
    return h;
  }

  @Override
  boolean deep(final InputInfo info, final TrieNode node, final Collation coll)
      throws QueryException {
    if(!(node instanceof TrieList) || size != node.size) return false;
    final TrieList ol = (TrieList) node;

    // do the evil nested-loop thing
    OUTER:
    for(int i = 0; i < size; i++) {
      final Item k = keys[i];
      for(int j = 0; j < size; j++) {
        if(k.sameKey(ol.keys[j], info)) {
          // check bound value, too
          if(!deep(values[i], ol.values[j], coll, info)) return false;
          // value matched, continue with next key
          continue OUTER;
        }
      }
      // all keys of the other list were checked, none matched
      return false;
    }
    // all entries were found
    return true;
  }

  @Override
  StringBuilder append(final StringBuilder sb, final String indent) {
    sb.append(indent).append("`-- Collision (").append(Integer.toHexString(hash)).append("):\n");
    final int kl = keys.length;
    for(int k = 0; k < kl; k++) {
      sb.append(indent).append("      ").append(keys[k]).append(" => ");
      sb.append(values[k]).append('\n');
    }
    return sb;
  }

  @Override
  StringBuilder append(final StringBuilder sb) {
    for(int i = size; --i >= 0 && more(sb);) {
      sb.append(keys[i]).append(MAPASG).append(values[i]).append(SEP);
    }
    return sb;
  }
}
