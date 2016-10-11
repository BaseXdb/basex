package org.basex.query.value.map;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * A single binding of a {@link Map}.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Leo Woerteler
 */
final class TrieLeaf extends TrieNode {
  /** Hash code of the key, stored for performance. */
  final int hash;
  /** Key of this binding. */
  final Item key;
  /** Value of this binding. */
  Value value;

  /**
   * Constructor.
   * @param hash hash code of the key
   * @param key key
   * @param value value
   */
  TrieLeaf(final int hash, final Item key, final Value value) {
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
    if(h == hash) return key.sameKey(k, ii) ? new TrieLeaf(h, k, v) :
      new TrieList(hash, key, value, k, v);

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
    return new TrieBranch(ch, used, 2);
  }

  @Override
  TrieNode delete(final int h, final Item k, final int l, final InputInfo ii)
      throws QueryException {
    return h == hash && key.sameKey(k, ii) ? null : this;
  }

  @Override
  Value get(final int h, final Item k, final int l, final InputInfo ii) throws QueryException {
    return h == hash && key.sameKey(k, ii) ? value : null;
  }

  @Override
  boolean contains(final int h, final Item k, final int l, final InputInfo ii)
      throws QueryException {
    return h == hash && key.sameKey(k, ii);
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
      if(!key.sameKey(o.key, ii)) return new TrieList(hash, key, value, o.key, o.value);
      switch(merge) {
        case USE_FIRST:
        case UNSPECIFIED:
          return o;
        case USE_LAST:
          return this;
        case COMBINE:
          return new TrieLeaf(hash, key, ValueBuilder.concat(o.value, value));
        default:
          throw MERGE_DUPLICATE_X.get(ii, key);
      }
    }

    final TrieNode[] ch = new TrieNode[KIDS];
    final int k = key(hash, l), ok = key(o.hash, l), nu;

    // same key? add recursively
    if(k == ok) {
      ch[k] = add(o, l + 1, merge, ii);
      nu = 1 << k;
    } else {
      ch[k] = this;
      ch[ok] = o;
      nu = 1 << k | 1 << ok;
    }
    return new TrieBranch(ch, nu, 2);
  }

  @Override
  TrieNode add(final TrieList o, final int l, final MergeDuplicates merge, final InputInfo ii)
      throws QueryException {

    // same hash? insert binding
    if(hash == o.hash) {
      for(int i = 0; i < o.size; i++) {
        if(key.sameKey(o.keys[i], ii)) {
          final Item[] ks = o.keys.clone();
          final Value[] vs = o.values.clone();
          ks[i] = key;
          vs[i] = value;
          return new TrieList(hash, ks, vs);
        }
      }
      return new TrieList(hash, Array.add(o.keys, key), Array.add(o.values, value));
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
    return new TrieBranch(ch, nu, o.size + 1);
  }

  @Override
  TrieNode add(final TrieBranch o, final int l, final MergeDuplicates merge, final InputInfo ii)
      throws QueryException {

    final int k = key(hash, l);
    final TrieNode[] ch = o.copyKids();
    final TrieNode old = ch[k];
    ch[k] = old == null ? this : old.addAll(this, l + 1, merge, ii);
    return new TrieBranch(ch, o.used | 1 << k, o.size + ch[k].size - (old != null ? old.size : 0));
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
  void materialize(final InputInfo ii) throws QueryException {
    key.materialize(ii);
    value.materialize(ii);
  }

  @Override
  void forEach(final ValueBuilder vb, final FItem func, final QueryContext qc, final InputInfo ii)
      throws QueryException {
    vb.add(func.invokeValue(qc, ii, key, value));
  }

  @Override
  boolean instanceOf(final AtomType kt, final SeqType vt) {
    return (kt == null || key.type.instanceOf(kt)) && (vt == null || vt.instance(value));
  }

  @Override
  boolean deep(final InputInfo ii, final TrieNode o, final Collation coll) throws QueryException {
    return o instanceof TrieLeaf && key.sameKey(((TrieLeaf) o).key, ii) &&
        deep(value, ((TrieLeaf) o).value, coll, ii);
  }

  @Override
  int hash(final InputInfo ii) throws QueryException {
    return 31 * hash + value.hash(ii);
  }

  @Override
  StringBuilder toString(final StringBuilder sb, final String ind) {
    return sb.append(ind).append("`-- ").append(key).append(" => ").append(value).append('\n');
  }

  @Override
  StringBuilder toString(final StringBuilder sb) {
    return sb.append(key).append(": ").append(value).append(", ");
  }
}
