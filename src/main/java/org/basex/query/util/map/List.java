package org.basex.query.util.map;

import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Value;
import org.basex.query.iter.ItemCache;
import org.basex.util.Array;
import org.basex.util.InputInfo;

/**
 * Leaf that contains a collision list of keys with the same hash code.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
final class List extends TrieNode {
  /** Common hash value of all contained values. */
  final int hash;

  /** List of keys of this collision list. */
  final Item[] keys;
  /** List of values of this collision list. */
  final Value[] values;

  /**
   * Constructor.
   *
   * @param h hash value
   * @param ks key array
   * @param vs value array
   */
  List(final int h, final Item[] ks, final Value[] vs) {
    super(ks.length);
    keys = ks;
    values = vs;
    hash = h;
    assert verify();
  }

  /**
   * Constructor for creating a collision list from two bindings.
   * @param h hash value
   * @param k1 first key
   * @param v1 first value
   * @param k2 second key
   * @param v2 second value
   */
  List(final int h, final Item k1, final Value v1,
      final Item k2, final Value v2) {
    this(h, new Item[]{ k1, k2 }, new Value[]{ v1, v2 });
  }

  @Override
  TrieNode insert(final int h, final Item k, final Value v, final int l,
      final InputInfo ii) throws QueryException {
    // same hash, replace or merge
    if(h == hash) {
      for(int i = keys.length; i-- > 0;) {
        if(k.eq(ii, keys[i])) {
          // replace value
          final Value[] vs = values.clone();
          vs[i] = v;
          return new List(h, keys.clone(), vs);
        }
      }
      return new List(hash, Array.add(keys, k), Array.add(values, v));
    }

    // different hash, branch
    final TrieNode[] ch = new TrieNode[KIDS];
    final int a = key(h, l), b = key(hash, l);
    final int used;
    if(a != b) {
      ch[a] = new Leaf(h, k, v);
      ch[b] = this;
      used = 1 << a | 1 << b;
    } else {
      ch[a] = insert(h, k, v, l + 1, ii);
      used = 1 << a;
    }
    // we definitely inserted one value
    return new Branch(ch, used, size + 1);
  }

  @Override
  TrieNode delete(final int h, final Item k, final int l,
      final InputInfo ii) throws QueryException {
    if(h == hash) {
      for(int i = size; i-- > 0;) {
        if(k.eq(ii, keys[i])) {
          // found entry
          if(size == 2) {
            // single leaf remains
            final int o = i ^ 1;
            return new Leaf(h, keys[o], values[o]);
          }
          // still collisions
          return new List(h, Array.delete(keys, i), Array.delete(values, i));
        }
      }
    }
    return this;
  }

  @Override
  Value get(final int h, final Item k, final int l, final InputInfo ii)
      throws QueryException {
    if(h == hash) for(int i = keys.length; i-- != 0;)
      if(keys[i].eq(ii, k)) return values[i];
    return null;
  }

  @Override
  boolean contains(final int h, final Item k, final int u, final InputInfo ii)
      throws QueryException {
    if(h == hash) for(int i = keys.length; i-- != 0;)
      if(keys[i].eq(ii, k)) return true;
    return false;
  }

  @Override
  StringBuilder toString(final StringBuilder sb, final String ind) {
    sb.append(ind).append("`-- Collision (").append(
        Integer.toHexString(hash)).append("):\n");
    for(int i = 0; i < keys.length; i++) {
      sb.append(ind).append("      ").append(keys[i]).append(" => ").append(
          values[i]).append('\n');
    }
    return sb;
  }

  @Override
  TrieNode addAll(final TrieNode o, final int l, final InputInfo ii)
      throws QueryException {
    return o.add(this, l, ii);
  }

  @Override
  TrieNode add(final Leaf o, final int l, final InputInfo ii)
      throws QueryException {
    if(hash == o.hash) {
      for(final Item k : keys) if(k.eq(ii, o.key)) return this;
      return new List(hash, Array.add(keys, o.key), Array.add(values, o.value));
    }

    final TrieNode[] ch = new TrieNode[KIDS];
    final int k = key(hash, l), ok = key(o.hash, l);
    final int nu;

    // same key? add recursively
    if(k == ok) {
      ch[k] = add(o, l + 1, ii);
      nu = 1 << k;
    } else {
      ch[k] = this;
      ch[ok] = o;
      nu = 1 << k | 1 << ok;
    }

    return new Branch(ch, nu, size + 1);
  }

  @Override
  TrieNode add(final List o, final int l, final InputInfo ii)
      throws QueryException {
    if(hash == o.hash) {
      Item[] ks = keys;
      Value[] vs = values;

      outer: for(int i = 0; i < size; i++) {
        final Item ok = o.keys[i];
        // skip all entries that are overridden
        for(final Item k : keys) if(ok.eq(ii, k)) continue outer;
        // key is not in this list, add it
        ks = Array.add(ks, ok);
        vs = Array.add(vs, o.values[i]);
      }
      return ks == keys ? this : new List(hash, ks, vs);
    }

    final TrieNode[] ch = new TrieNode[KIDS];
    final int k = key(hash, l), ok = key(o.hash, l);
    final int nu;

    // same key? add recursively
    if(k == ok) {
      ch[k] = add(o, l + 1, ii);
      nu = 1 << k;
    } else {
      ch[k] = this;
      ch[ok] = o;
      nu = 1 << k | 1 << ok;
    }

    return new Branch(ch, nu, size + o.size);
  }

  @Override
  TrieNode add(final Branch o, final int l, final InputInfo ii)
      throws QueryException {
    final int k = key(hash, l);
    final TrieNode[] ch = o.kids.clone();
    final TrieNode old = ch[k];
    ch[k] = old == null ? this : old.addAll(this, l + 1, ii);
    return new Branch(ch, o.used | 1 << k,
        o.size + size - (old != null ? old.size : 0));
  }

  @Override
  boolean verify() {
    try {
      for(int i = 1; i < size; i++) {
        for(int j = i; j-- > 0;) {
          if(keys[i].eq(null, keys[j])) return false;
        }
      }
    } catch(final QueryException e) {
      return false;
    }
    return true;
  }

  @Override
  void keys(final ItemCache ks) {
    for(final Item k : keys) ks.add(k);
  }
}