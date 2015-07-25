package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A single binding of a {@link Map}.
 *
 * @author BaseX Team 2005-15, BSD License
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
   * @param hash hash code of the key
   * @param key key
   * @param value value
   */
  Leaf(final int hash, final Item key, final Value value) {
    super(1);
    this.hash = hash;
    this.key = key;
    this.value = value;
    assert verify();
  }

  @Override
  TrieNode put(final int h, final Item k, final Value v, final int l, final InputInfo ii)
      throws QueryException {
    // same hash, replace or merge
    if(h == hash) return eq(k, key, ii) ? new Leaf(h, k, v) : new List(hash, key, value, k, v);

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
    return new Branch(ch, used, 2);
  }

  @Override
  TrieNode delete(final int h, final Item k, final int l, final InputInfo ii)
      throws QueryException {
    return h == hash && eq(key, k, ii) ? null : this;
  }

  @Override
  Value get(final int h, final Item k, final int l, final InputInfo ii) throws QueryException {
    return h == hash && eq(key, k, ii) ? value : null;
  }

  @Override
  boolean contains(final int h, final Item k, final int l, final InputInfo ii)
      throws QueryException {
    return h == hash && eq(key, k, ii);
  }

  @Override
  StringBuilder toString(final StringBuilder sb, final String ind) {
    return sb.append(ind).append("`-- ").append(key).append(" => ").append(value).append('\n');
  }

  @Override
  TrieNode addAll(final TrieNode o, final int l, final InputInfo ii) throws QueryException {
    return o.add(this, l, ii);
  }

  @Override
  TrieNode add(final Leaf o, final int l, final InputInfo ii) throws QueryException {
    if(hash == o.hash) return eq(key, o.key, ii) ?
        this : new List(hash, key, value, o.key, o.value);

    final TrieNode[] ch = new TrieNode[KIDS];
    final int k = key(hash, l), ok = key(o.hash, l), nu;

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
  TrieNode add(final List o, final int l, final InputInfo ii) throws QueryException {
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
  TrieNode add(final Branch o, final int l, final InputInfo ii) throws QueryException {
    final int k = key(hash, l);
    final TrieNode[] ch = o.copyKids();
    final TrieNode old = ch[k];
    ch[k] = old == null ? this : old.addAll(this, l + 1, ii);
    return new Branch(ch, o.used | 1 << k, o.size + ch[k].size - (old != null ? old.size : 0));
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
  void values(final ValueBuilder vs) {
    vs.add(value);
  }

  @Override
  void apply(final ValueBuilder vb, final FItem func, final QueryContext qc, final InputInfo ii)
      throws QueryException {
    vb.add(func.invokeValue(qc, ii, key, value));
  }

  @Override
  boolean hasType(final AtomType kt, final SeqType vt) {
    return (kt == null || key.type.instanceOf(kt)) && (vt == null || vt.instance(value));
  }

  @Override
  boolean deep(final InputInfo ii, final TrieNode o, final Collation coll) throws QueryException {
    return o instanceof Leaf && eq(key, ((Leaf) o).key, ii) &&
        deep(value, ((Leaf) o).value, coll, ii);
  }

  @Override
  int hash(final InputInfo ii) throws QueryException {
    return 31 * hash + value.hash(ii);
  }

  @Override
  StringBuilder toString(final StringBuilder sb) {
    return sb.append(key).append(": ").append(value).append(", ");
  }
}