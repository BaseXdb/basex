package org.basex.query.item.map;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.util.*;

/**
 * A single binding of a {@link Map}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
final class Leaf extends TrieNode {
  /** Hash code of the key, stored for performance. */
  final int hash;
  /** Key of this binding. */
  final Item key;
  /** Value of this binding. */
  final Value value;

  /**
   * Constructor.
   * @param h hash code of the key
   * @param k key
   * @param v value
   */
  Leaf(final int h, final Item k, final Value v) {
    super(1);
    hash = h;
    key = k;
    value = v;
    assert verify();
  }

  @Override
  TrieNode insert(final int h, final Item k, final Value v, final int l,
      final InputInfo ii) throws QueryException {
    // same hash, replace or merge
    if(h == hash) return eq(k, key, ii) ?
        new Leaf(h, k, v) : new List(hash, key, value, k, v);

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
    return new Branch(ch, used, 2);
  }

  @Override
  TrieNode delete(final int h, final Item k, final int l, final InputInfo ii)
      throws QueryException {
    return h == hash && eq(key, k, ii) ? null : this;
  }

  @Override
  Value get(final int h, final Item k, final int l, final InputInfo ii)
      throws QueryException {
    return h == hash && eq(key, k, ii) ? value : null;
  }

  @Override
  boolean contains(final int h, final Item k, final int l,
      final InputInfo ii) throws QueryException {
    return h == hash && eq(key, k, ii);
  }

  @Override
  StringBuilder toString(final StringBuilder sb, final String ind) {
    return sb.append(ind).append("`-- ").append(key).append(
        " => ").append(value).append('\n');
  }

  @Override
  TrieNode addAll(final TrieNode o, final int l, final InputInfo ii)
      throws QueryException {
    return o.add(this, l, ii);
  }

  @Override
  TrieNode add(final Leaf o, final int l, final InputInfo ii)
      throws QueryException {
    if(hash == o.hash) return eq(key, o.key, ii) ?
        this : new List(hash, key, value, o.key, o.value);

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

    return new Branch(ch, nu, 2);
  }

  @Override
  TrieNode add(final List o, final int l, final InputInfo ii)
      throws QueryException {

    // same hash? insert binding
    if(hash == o.hash) {
      for(int i = 0; i < o.size; i++) {
        if(eq(key, o.keys[i], ii)) {
          final Item[] ks = o.keys.clone();
          final Value[] vs = o.values.clone();
          ks[i] = key;
          vs[i] = value;
          return new List(hash, ks, vs);
        }
      }
      return new List(hash, Array.add(o.keys, key), Array.add(o.values, value));
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

    return new Branch(ch, nu, o.size + 1);
  }

  @Override
  TrieNode add(final Branch o, final int l, final InputInfo ii)
      throws QueryException {
    final int k = key(hash, l);
    final TrieNode[] ch = o.copyKids();
    final TrieNode old = ch[k];
    ch[k] = old == null ? this : old.addAll(this, l + 1, ii);
    return new Branch(ch, o.used | 1 << k,
        o.size + ch[k].size - (old != null ? old.size : 0));
  }

  @Override
  boolean verify() {
    try {
      return key.hash(null) == hash;
    } catch(final QueryException ex) {
      return false;
    }
  }

  @Override
  void keys(final ValueBuilder ks) {
    ks.add(key);
  }

  @Override
  boolean hasType(final AtomType kt, final SeqType vt) {
    return (kt == null || key.type.instanceOf(kt))
        && (vt == null || vt.instance(value));
  }

  @Override
  boolean deep(final InputInfo ii, final TrieNode o) throws QueryException {
    return o instanceof Leaf && eq(key, ((Leaf) o).key, ii)
        && deep(value, ((Leaf) o).value, ii);
  }

  @Override
  int hash(final InputInfo ii) throws QueryException {
    return 31 * hash + value.hash(ii);
  }

  @Override
  StringBuilder toString(final StringBuilder sb) {
    return sb.append(key).append(ASSIGN).append(value).append(", ");
  }
}