package org.basex.query.value.map;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Leaf that contains a collision list of keys with the same hash code.
 *
 * @author BaseX Team 2005-12, BSD License
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
  List(final int h, final Item k1, final Value v1, final Item k2, final Value v2) {
    this(h, new Item[]{ k1, k2 }, new Value[]{ v1, v2 });
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
  TrieNode insert(final int h, final Item k, final Value v, final int l,
      final InputInfo ii) throws QueryException {
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
  Value get(final int h, final Item k, final int l, final InputInfo ii)
      throws QueryException {
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
  boolean deep(final InputInfo ii, final TrieNode o) throws QueryException {
    if(!(o instanceof List) || size != o.size) return false;
    final List ol = (List) o;

    // do the evil nested-loop thing
    outer: for(int i = 0; i < size; i++) {
      final Item k = keys[i];
      for(int j = 0; j < size; j++) {
        if(eq(k, ol.keys[i], ii)) {
          // check bound value, too
          if(!deep(values[i], ol.values[j], ii)) return false;
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