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
 * A single binding of a {@link Map}.
 *
 * @author BaseX Team 2005-18, BSD License
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
  TrieNode put(final int hs, final Item ky, final Value vl, final int level, final InputInfo info)
      throws QueryException {

    // same hash, replace or merge
    if(hs == hash) return key.sameKey(ky, info) ? new TrieLeaf(hs, ky, vl) :
      new TrieList(hash, key, value, ky, vl);

    // different hash, branch
    final TrieNode[] ch = new TrieNode[KIDS];
    final int a = key(hs, level), b = key(hash, level);
    final int used;
    if(a == b) {
      ch[a] = put(hs, ky, vl, level + 1, info);
      used = 1 << a;
    } else {
      ch[a] = new TrieLeaf(hs, ky, vl);
      ch[b] = this;
      used = 1 << a | 1 << b;
    }
    return new TrieBranch(ch, used, 2);
  }

  @Override
  TrieNode delete(final int hs, final Item ky, final int level, final InputInfo info)
      throws QueryException {
    return hs == hash && key.sameKey(ky, info) ? null : this;
  }

  @Override
  Value get(final int hs, final Item ky, final int level, final InputInfo info)
      throws QueryException {
    return hs == hash && key.sameKey(ky, info) ? value : null;
  }

  @Override
  boolean contains(final int hs, final Item ky, final int level, final InputInfo info)
      throws QueryException {
    return hs == hash && key.sameKey(ky, info);
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
      if(!key.sameKey(leaf.key, info))
        return new TrieList(hash, key, value, leaf.key, leaf.value);

      switch(merge) {
        case USE_FIRST:
        case UNSPECIFIED:
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
      ch[k] = add(leaf, level + 1, merge, info, qc);
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
      final InputInfo info, final QueryContext qc) throws QueryException {

    // same hash? insert binding
    if(hash == list.hash) {
      for(int i = 0; i < list.size; i++) {
        if(key.sameKey(list.keys[i], info)) {
          final Item[] ks = list.keys.clone();
          final Value[] vs = list.values.clone();
          ks[i] = key;

          switch(merge) {
            case USE_FIRST:
            case UNSPECIFIED:
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
      ch[k] = add(list, level + 1, merge, info, qc);
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
      final InputInfo info, final QueryContext qc) throws QueryException {

    final int k = key(hash, level);
    final TrieNode[] ch = branch.copyKids();
    final TrieNode old = ch[k];
    ch[k] = old == null ? this : old.addAll(this, level + 1, merge, info, qc);
    return new TrieBranch(ch, branch.used | 1 << k,
        branch.size + ch[k].size - (old != null ? old.size : 0));
  }

  @Override
  boolean verify() {
    try {
      return key.hash(null) == hash;
    } catch(final QueryException ex) {
      Util.debug(ex);
      return false;
    }
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
  void materialize(final InputInfo info) throws QueryException {
    key.materialize(info);
    value.materialize(info);
  }

  @Override
  void forEach(final ValueBuilder vb, final FItem func, final QueryContext qc, final InputInfo info)
      throws QueryException {
    vb.add(func.invokeValue(qc, info, key, value));
  }

  @Override
  boolean instanceOf(final AtomType kt, final SeqType dt) {
    return (kt == null || key.type.instanceOf(kt)) && (dt == null || dt.instance(value));
  }

  @Override
  boolean deep(final InputInfo info, final TrieNode node, final Collation coll)
      throws QueryException {
    return node instanceof TrieLeaf && key.sameKey(((TrieLeaf) node).key, info) &&
        deep(value, ((TrieLeaf) node).value, coll, info);
  }

  @Override
  int hash(final InputInfo info) throws QueryException {
    return 31 * hash + value.hash(info);
  }

  @Override
  StringBuilder append(final StringBuilder sb, final String indent) {
    return sb.append(indent).append("`-- ").append(key).append(" => ").append(value).append('\n');
  }

  @Override
  StringBuilder append(final StringBuilder sb) {
    if(more(sb)) sb.append(key).append(MAPASG).append(value).append(SEP);
    return sb;
  }
}
