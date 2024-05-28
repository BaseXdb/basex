package org.basex.query.value.map;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

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
 * A single binding of a {@link XQMap}.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
final class TrieLeaf extends TrieNode {
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
  TrieLeaf(final int hash, final Item key, final Value value) {
    super(1);
    this.hash = hash;
    this.key = key;
    this.value = value;
    assert verify();
  }

  @Override
  TrieNode put(final int hs, final Item ky, final Value vl, final int level) throws QueryException {
    // same hash, replace or merge
    if(hs == hash) return key.atomicEqual(ky) ? new TrieLeaf(hs, ky, vl) :
      new TrieList(hash, key, value, ky, vl);

    // different hash, branch
    final TrieNode[] ch = new TrieNode[KIDS];
    final int a = key(hs, level), b = key(hash, level);
    final int used;
    if(a == b) {
      ch[a] = put(hs, ky, vl, level + 1);
      used = 1 << a;
    } else {
      ch[a] = new TrieLeaf(hs, ky, vl);
      ch[b] = this;
      used = 1 << a | 1 << b;
    }
    return new TrieBranch(ch, used, 2);
  }

  @Override
  TrieNode delete(final int hs, final Item ky, final int level) throws QueryException {
    return hs == hash && key.atomicEqual(ky) ? null : this;
  }

  @Override
  Value get(final int hs, final Item ky, final int level) throws QueryException {
    return hs == hash && key.atomicEqual(ky) ? value : null;
  }

  @Override
  boolean contains(final int hs, final Item ky, final int level) throws QueryException {
    return hs == hash && key.atomicEqual(ky);
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
      if(!key.atomicEqual(leaf.key))
        return new TrieList(hash, key, value, leaf.key, leaf.value);

      switch(merge) {
        case USE_FIRST:
        case USE_ANY:
          return leaf;
        case USE_LAST:
          return this;
        case COMBINE:
          return new TrieLeaf(hash, key, ValueBuilder.concat(leaf.value, value, qc));
        default:
          throw MERGE_DUPLICATE_X.get(info, key);
      }
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
    return new TrieBranch(ch, nu, 2);
  }

  @Override
  TrieNode add(final TrieList list, final int level, final MergeDuplicates merge,
      final QueryContext qc, final InputInfo info) throws QueryException {

    // same hash? insert binding
    if(hash == list.hash) {
      for(int i = 0; i < list.size; i++) {
        if(key.atomicEqual(list.keys[i])) {
          final Item[] ks = list.keys.clone();
          final Value[] vs = list.values.clone();
          ks[i] = key;

          switch(merge) {
            case USE_FIRST:
            case USE_ANY:
              break;
            case USE_LAST:
              vs[i] = value;
              break;
            case COMBINE:
              vs[i] = ValueBuilder.concat(list.values[i], value, qc);
              break;
            default:
              throw MERGE_DUPLICATE_X.get(info, key);
          }
          return new TrieList(hash, ks, vs);
        }
      }
      return new TrieList(hash, Array.add(list.keys, key), Array.add(list.values, value));
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
    return new TrieBranch(ch, nu, list.size + 1);
  }

  @Override
  TrieNode add(final TrieBranch branch, final int level, final MergeDuplicates merge,
      final QueryContext qc, final InputInfo info) throws QueryException {

    final int k = key(hash, level);
    final TrieNode[] ch = branch.copyKids();
    final TrieNode old = ch[k];
    ch[k] = old == null ? this : old.addAll(this, level + 1, merge, qc, info);
    return new TrieBranch(ch, branch.used | 1 << k,
        branch.size + ch[k].size - (old != null ? old.size : 0));
  }

  @Override
  boolean verify() {
    return key.hash() == hash;
  }

  @Override
  void keys(final ItemList keys) {
    keys.add(key);
  }

  @Override
  void values(final ValueBuilder vs) {
    vs.add(value);
  }

  @Override
  void cache(final boolean lazy, final InputInfo info) throws QueryException {
    key.cache(lazy, info);
    value.cache(lazy, info);
  }

  @Override
  boolean materialized(final Predicate<Data> test, final InputInfo info) throws QueryException {
    return value.materialized(test, info);
  }

  @Override
  void apply(final QueryBiConsumer<Item, Value> func) throws QueryException {
    func.accept(key, value);
  }

  @Override
  boolean instanceOf(final Type kt, final SeqType dt) {
    return (kt == null || key.type.instanceOf(kt)) && (dt == null || dt.instance(value));
  }

  @Override
  boolean equal(final TrieNode node, final DeepEqual deep) throws QueryException {
    if(node instanceof TrieLeaf) {
      final TrieLeaf leaf = (TrieLeaf) node;
      return deep != null ? key.atomicEqual(leaf.key) && deep.equal(value, leaf.value) :
        key.equals(leaf.key) && value.equals(leaf.value);
    }
    return false;
  }

  @Override
  void add(final TokenBuilder tb, final String indent) {
    tb.add(indent).add("`-- ").add(key).add(" => ").add(value).add('\n');
  }

  @Override
  void add(final TokenBuilder tb) {
    if(tb.moreInfo()) tb.add(key).add(MAPASG).add(value).add(SEP);
  }
}
