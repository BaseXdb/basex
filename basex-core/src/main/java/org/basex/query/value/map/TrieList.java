package org.basex.query.value.map;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
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
   *
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
  TrieNode delete(final int h, final Item k, final int l, final InputInfo ii)
      throws QueryException {

    if(h == hash) {
      for(int i = size; i-- > 0;) {
        // still collisions?
        if(k.sameKey(keys[i], ii)) {
          // found entry
          if(size == 2) {
            // single leaf remains
            final int o = i ^ 1;
            return new TrieLeaf(h, keys[o], values[o]);
          }
          // create new arrays (modified due to #1297; performance improved)
          final int s = size - 1;
          final Item[] ks = new Item[s];
          System.arraycopy(keys, 0, ks, 0, i);
          System.arraycopy(keys, i + 1, ks, i, s - i);
          final Value[] vs = new Value[s];
          System.arraycopy(values, 0, vs, 0, i);
          System.arraycopy(values, i + 1, vs, i, s - i);
          return new TrieList(h, ks, vs);
        }
      }
    }
    return this;
  }

  @Override
  TrieNode put(final int h, final Item k, final Value v, final int l, final InputInfo ii)
      throws QueryException {

    // same hash, replace or merge
    if(h == hash) {
      for(int i = keys.length; i-- > 0;) {
        if(k.sameKey(keys[i], ii)) {
          // replace value
          final Value[] vs = values.clone();
          vs[i] = v;
          return new TrieList(h, keys, vs);
        }
      }
      return new TrieList(hash, Array.add(keys, k), Array.add(values, v));
    }

    // different hash, branch
    final TrieNode[] ch = new TrieNode[KIDS];
    final int a = key(h, l), b = key(hash, l);
    final int used;
    if(a == b) {
      ch[a] = put(h, k, v, l + 1, ii);
      used = 1 << a;
    } else {
      ch[a] = new TrieLeaf(h, k, v);
      ch[b] = this;
      used = 1 << a | 1 << b;
    }
    // we definitely inserted one value
    return new TrieBranch(ch, used, size + 1);
  }

  @Override
  Value get(final int h, final Item k, final int l, final InputInfo ii) throws QueryException {
    if(h == hash) {
      for(int i = keys.length; i-- != 0;)
      if(k.sameKey(keys[i], ii)) return values[i];
    }
    return null;
  }

  @Override
  boolean contains(final int h, final Item k, final int u, final InputInfo ii)
      throws QueryException {
    if(h == hash) {
      for(int i = keys.length; i-- != 0;)
        if(k.sameKey(keys[i], ii)) return true;
    }
    return false;
  }

  @Override
  TrieNode addAll(final TrieNode o, final int l, final MergeDuplicates merge, final InputInfo ii)
      throws QueryException {
    return o.add(this, l, merge, ii);
  }

  @Override
  TrieNode add(final TrieLeaf o, final int l, final MergeDuplicates merge, final InputInfo ii)
      throws QueryException {

    if(hash == o.hash) {
      for(int i = keys.length; i-- > 0;) {
        if(o.key.sameKey(keys[i], ii)) {
          switch(merge) {
            case USE_FIRST:
            case UNSPECIFIED:
              final Value[] uf = values.clone();
              uf[i] = o.value;
              return new TrieList(hash, keys, uf);
            case USE_LAST:
              return this;
            case COMBINE:
              final Value[] cm = values.clone();
              cm[i] = ValueBuilder.concat(o.value, cm[i]);
              return new TrieList(hash, keys, cm);
            default:
              throw MERGE_DUPLICATE_X.get(ii, o.key);
          }
        }
      }
      return new TrieList(hash, Array.add(keys, o.key), Array.add(values, o.value));
    }

    final TrieNode[] ch = new TrieNode[KIDS];
    final int k = key(hash, l), ok = key(o.hash, l);
    final int nu;

    // same key? add recursively
    if(k == ok) {
      ch[k] = add(o, l + 1, merge, ii);
      nu = 1 << k;
    } else {
      ch[k] = this;
      ch[ok] = o;
      nu = 1 << k | 1 << ok;
    }

    return new TrieBranch(ch, nu, size + 1);
  }

  @Override
  TrieNode add(final TrieList o, final int l, final MergeDuplicates merge, final InputInfo ii)
      throws QueryException {

    if(hash == o.hash) {
      Item[] ks = keys;
      Value[] vs = values;

      outer: for(int i = 0; i < size; i++) {
        final Item ok = o.keys[i];
        // skip all entries that are overridden
        for(final Item k : keys) if(k.sameKey(ok, ii)) continue outer;
        // key is not in this list, add it
        ks = Array.add(ks, ok);
        vs = Array.add(vs, o.values[i]);
      }
      return ks == keys ? this : new TrieList(hash, ks, vs);
    }

    final TrieNode[] ch = new TrieNode[KIDS];
    final int k = key(hash, l), ok = key(o.hash, l);
    final int nu;

    // same key? add recursively
    if(k == ok) {
      ch[k] = add(o, l + 1, merge, ii);
      nu = 1 << k;
    } else {
      ch[k] = this;
      ch[ok] = o;
      nu = 1 << k | 1 << ok;
    }
    return new TrieBranch(ch, nu, size + o.size);
  }

  @Override
  TrieNode add(final TrieBranch o, final int l, final MergeDuplicates merge, final InputInfo ii)
      throws QueryException {

    final int k = key(hash, l);
    final TrieNode[] ch = o.copyKids();
    final TrieNode old = ch[k];
    ch[k] = old == null ? this : old.addAll(this, l + 1, merge, ii);
    return new TrieBranch(ch, o.used | 1 << k, o.size + size - (old != null ? old.size : 0));
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
      return false;
    }
    return true;
  }

  @Override
  void keys(final ValueBuilder ks) {
    for(final Item k : keys) ks.add(k);
  }

  @Override
  void values(final ValueBuilder vs) {
    for(final Value v : values) vs.add(v);
  }

  @Override
  void materialize(final InputInfo ii) throws QueryException {
    for(int i = 0; i < size; i++) {
      keys[i].materialize(ii);
      values[i].materialize(ii);
    }
  }

  @Override
  void forEach(final ValueBuilder vb, final FItem func, final QueryContext qc, final InputInfo ii)
      throws QueryException {
    for(int i = 0; i < size; i++) {
      vb.add(func.invokeValue(qc, ii, keys[i], values[i]));
    }
  }

  @Override
  boolean instanceOf(final AtomType kt, final SeqType vt) {
    if(kt != null)
      for(final Item k : keys) if(!k.type.instanceOf(kt)) return false;
    if(vt != null)
      for(final Value v : values) if(!vt.instance(v)) return false;
    return true;
  }

  @Override
  int hash(final InputInfo ii) throws QueryException {
    int h = hash;
    // order isn't important, operation has to be commutative
    for(int i = size; --i >= 0;) h ^= values[i].hash(ii);
    return h;
  }

  @Override
  boolean deep(final InputInfo ii, final TrieNode o, final Collation coll) throws QueryException {
    if(!(o instanceof TrieList) || size != o.size) return false;
    final TrieList ol = (TrieList) o;

    // do the evil nested-loop thing
    outer: for(int i = 0; i < size; i++) {
      final Item k = keys[i];
      for(int j = 0; j < size; j++) {
        if(k.sameKey(ol.keys[i], ii)) {
          // check bound value, too
          if(!deep(values[i], ol.values[j], coll, ii)) return false;
          // value matched, continue with next key
          continue outer;
        }
      }
      // all keys of the other list were checked, none matched
      return false;
    }
    // all entries were found
    return true;
  }

  @Override
  StringBuilder append(final StringBuilder sb, final String ind) {
    sb.append(ind).append("`-- Collision (").append(Integer.toHexString(hash)).append("):\n");
    final int kl = keys.length;
    for(int k = 0; k < kl; k++) {
      sb.append(ind).append("      ").append(keys[k]).append(" => ").append(values[k]).append('\n');
    }
    return sb;
  }

  @Override
  StringBuilder append(final StringBuilder sb) {
    for(int i = size; --i >= 0;)
      sb.append(keys[i]).append(ASSIGN).append(values[i]).append(", ");
    return sb;
  }
}