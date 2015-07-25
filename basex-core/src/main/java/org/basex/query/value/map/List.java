package org.basex.query.value.map;

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
 * @author BaseX Team 2005-15, BSD License
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
   * @param hash hash value
   * @param keys key array
   * @param values value array
   */
  List(final int hash, final Item[] keys, final Value[] values) {
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
  List(final int hash, final Item key1, final Value value1, final Item key2, final Value value2) {
    this(hash, new Item[]{ key1, key2 }, new Value[]{ value1, value2 });
  }

  @Override
  TrieNode delete(final int h, final Item k, final int l, final InputInfo ii)
      throws QueryException {

    if(h == hash) {
      for(int i = size; i-- > 0;) {
        if(eq(k, keys[i], ii)) {
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
  TrieNode put(final int h, final Item k, final Value v, final int l, final InputInfo ii)
      throws QueryException {

    // same hash, replace or merge
    if(h == hash) {
      for(int i = keys.length; i-- > 0;) {
        if(eq(k, keys[i], ii)) {
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
    if(a == b) {
      ch[a] = put(h, k, v, l + 1, ii);
      used = 1 << a;
    } else {
      ch[a] = new Leaf(h, k, v);
      ch[b] = this;
      used = 1 << a | 1 << b;
    }
    // we definitely inserted one value
    return new Branch(ch, used, size + 1);
  }

  @Override
  Value get(final int h, final Item k, final int l, final InputInfo ii) throws QueryException {
    if(h == hash) for(int i = keys.length; i-- != 0;)
      if(eq(k, keys[i], ii)) return values[i];
    return null;
  }

  @Override
  boolean contains(final int h, final Item k, final int u, final InputInfo ii)
      throws QueryException {
    if(h == hash) for(int i = keys.length; i-- != 0;)
      if(eq(k, keys[i], ii)) return true;
    return false;
  }

  @Override
  StringBuilder toString(final StringBuilder sb, final String ind) {
    sb.append(ind).append("`-- Collision (").append(Integer.toHexString(hash)).append("):\n");
    final int kl = keys.length;
    for(int k = 0; k < kl; k++) {
      sb.append(ind).append("      ").append(keys[k]).append(" => ").append(values[k]).append('\n');
    }
    return sb;
  }

  @Override
  TrieNode addAll(final TrieNode o, final int l, final InputInfo ii) throws QueryException {
    return o.add(this, l, ii);
  }

  @Override
  TrieNode add(final Leaf o, final int l, final InputInfo ii) throws QueryException {
    if(hash == o.hash) {
      for(final Item k : keys) if(eq(k, o.key, ii)) return this;
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
  TrieNode add(final List o, final int l, final InputInfo ii) throws QueryException {
    if(hash == o.hash) {
      Item[] ks = keys;
      Value[] vs = values;

      outer: for(int i = 0; i < size; i++) {
        final Item ok = o.keys[i];
        // skip all entries that are overridden
        for(final Item k : keys) if(eq(k, ok, ii)) continue outer;
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
  TrieNode add(final Branch o, final int l, final InputInfo ii) throws QueryException {
    final int k = key(hash, l);
    final TrieNode[] ch = o.copyKids();
    final TrieNode old = ch[k];
    ch[k] = old == null ? this : old.addAll(this, l + 1, ii);
    return new Branch(ch, o.used | 1 << k, o.size + size - (old != null ? old.size : 0));
  }

  @Override
  boolean verify() {
    try {
      for(int i = 1; i < size; i++) {
        for(int j = i; j-- > 0;) {
          if(eq(keys[i], keys[j], null)) return false;
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
  void apply(final ValueBuilder vb, final FItem func, final QueryContext qc, final InputInfo ii)
      throws QueryException {
    for(int i = 0; i < size; i++) {
      vb.add(func.invokeValue(qc, ii, keys[i], values[i]));
    }
  }

  @Override
  boolean hasType(final AtomType kt, final SeqType vt) {
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
    if(!(o instanceof List) || size != o.size) return false;
    final List ol = (List) o;

    // do the evil nested-loop thing
    outer: for(int i = 0; i < size; i++) {
      final Item k = keys[i];
      for(int j = 0; j < size; j++) {
        if(eq(k, ol.keys[i], ii)) {
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
  StringBuilder toString(final StringBuilder sb) {
    for(int i = size; --i >= 0;)
      sb.append(keys[i]).append(ASSIGN).append(values[i]).append(", ");
    return sb;
  }
}