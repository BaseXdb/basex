package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Inner node of a {@link XQMap}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
final class TrieBranch extends TrieNode {
  /** Child array. */
  private final TrieNode[] kids;
  /** Bit array with a bit set for every used slot. */
  final int used;

  /**
   * Constructor taking children array and the size of this map.
   * @param kids children
   * @param used bit array
   * @param size size of this node
   */
  TrieBranch(final TrieNode[] kids, final int used, final int size) {
    super(size);
    this.kids = kids;
    this.used = used;
    assert verify();
  }

  /**
   * Copies the children array.
   * This is faster than {@code kids.clone()} according to
   * <a href="http://www.javaspecialists.eu/archive/Issue124.html">Heinz M. Kabutz</a>.
   * @return copy of the child array
   */
  TrieNode[] copyKids() {
    final TrieNode[] copy = new TrieNode[KIDS];
    Array.copy(kids, KIDS, copy);
    return copy;
  }

  @Override
  TrieNode put(final int hs, final Item key, final Value value, final int level,
      final InputInfo ii) throws QueryException {
    final int k = key(hs, level);
    final TrieNode sub = kids[k], nsub;
    final int bs, rem;
    if(sub != null) {
      nsub = sub.put(hs, key, value, level + 1, ii);
      if(nsub == sub) return this;
      bs = used;
      rem = sub.size;
    } else {
      nsub = new TrieLeaf(hs, key, value);
      bs = used | 1 << k;
      rem = 0;
    }
    final TrieNode[] ks = copyKids();
    ks[k] = nsub;
    return new TrieBranch(ks, bs, size - rem + nsub.size);
  }

  @Override
  TrieNode delete(final int hash, final Item key, final int level, final InputInfo ii)
      throws QueryException {
    final int k = key(hash, level);
    final TrieNode sub = kids[k];
    if(sub == null) return this;
    final TrieNode nsub = sub.delete(hash, key, level + 1, ii);
    if(nsub == sub) return this;

    final int nu;
    if(nsub == null) {
      nu = used ^ 1 << k;
      if(Integer.bitCount(nu) == 1) {
        final TrieNode single = kids[Integer.numberOfTrailingZeros(nu)];
        // check whether the child depends on the right offset
        if(!(single instanceof TrieBranch)) return single;
      }
    } else {
      nu = used;
    }

    final TrieNode[] ks = copyKids();
    ks[k] = nsub;
    return new TrieBranch(ks, nu, size - 1);
  }

  @Override
  Value get(final int hash, final Item key, final int level, final InputInfo ii)
      throws QueryException {
    final int k = key(hash, level);
    final TrieNode sub = kids[k];
    return sub == null ? null : sub.get(hash, key, level + 1, ii);
  }

  @Override
  boolean contains(final int hash, final Item key, final int level, final InputInfo ii)
      throws QueryException {
    final int k = key(hash, level);
    final TrieNode sub = kids[k];
    return sub != null && sub.contains(hash, key, level + 1, ii);
  }

  /** End strings. */
  private static final String[] ENDS = { "|-- ", "|   ", "`-- ", "    " };

  @Override
  TrieNode addAll(final TrieNode node, final int level, final MergeDuplicates merge,
      final QueryContext qc, final InputInfo ii) throws QueryException {
    return node.add(this, level, merge, qc, ii);
  }

  @Override
  TrieNode add(final TrieLeaf leaf, final int level, final MergeDuplicates merge,
      final QueryContext qc, final InputInfo ii) throws QueryException {

    qc.checkStop();
    final int k = key(leaf.hash, level);
    final TrieNode ch = kids[k], kid;
    int n = 1;
    if(ch != null) {
      final TrieNode ins = ch.add(leaf, level + 1, merge, qc, ii);
      if(ins == ch) return this;
      n = ins.size - ch.size;
      kid = ins;
    } else {
      kid = leaf;
    }

    final TrieNode[] ks = copyKids();
    ks[k] = kid;
    return new TrieBranch(ks, used | 1 << k, size + n);
  }

  @Override
  TrieNode add(final TrieList list, final int level, final MergeDuplicates merge,
      final QueryContext qc, final InputInfo ii) throws QueryException {

    qc.checkStop();
    final int k = key(list.hash, level);
    final TrieNode ch = kids[k], kid;
    int n = list.size;
    if(ch != null) {
      final TrieNode ins = ch.add(list, level + 1, merge, qc, ii);
      if(ins == ch) return this;
      n = ins.size - ch.size;
      kid = ins;
    } else {
      kid = list;
    }

    final TrieNode[] ks = copyKids();
    ks[k] = kid;
    return new TrieBranch(ks, used | 1 << k, size + n);
  }

  @Override
  TrieNode add(final TrieBranch branch, final int level, final MergeDuplicates merge,
      final QueryContext qc, final InputInfo ii) throws QueryException {

    TrieNode[] ch = null;
    int nu = used, ns = size;
    final int kl = kids.length;
    for(int k = 0; k < kl; k++) {
      final TrieNode n = kids[k], ok = branch.kids[k];
      if(ok != null) {
        final TrieNode kid = n == null ? ok : ok.addAll(n, level + 1, merge, qc, ii);
        if(kid != n) {
          if(ch == null) ch = copyKids();
          ch[k] = kid;
          nu |= 1 << k;
          ns += kid.size - (n == null ? 0 : n.size);
        }
      }
    }
    return ch == null ? this : new TrieBranch(ch, nu, ns);
  }

  @Override
  boolean verify() {
    int c = 0;
    for(int i = 0; i < KIDS; i++) {
      final boolean bit = (used & 1 << i) != 0, act = kids[i] != null;
      if(bit ^ act) return false;
      if(act) c += kids[i].size;
    }
    return c == size;
  }

  @Override
  void keys(final ItemList ks) {
    for(final TrieNode nd : kids) {
      if(nd != null) nd.keys(ks);
    }
  }

  @Override
  void values(final ValueBuilder vs) {
    for(final TrieNode nd : kids) {
      if(nd != null) nd.values(vs);
    }
  }

  @Override
  void cache(final boolean lazy, final InputInfo ii) throws QueryException {
    for(final TrieNode nd : kids) {
      if(nd != null) nd.cache(lazy, ii);
    }
  }

  @Override
  boolean materialized() {
    for(final TrieNode nd : kids) {
      if(nd != null && !nd.materialized()) return false;
    }
    return true;
  }

  @Override
  void forEach(final ValueBuilder vb, final FItem func, final QueryContext qc, final InputInfo ii)
      throws QueryException {
    for(final TrieNode nd : kids) {
      if(nd != null) nd.forEach(vb, func, qc, ii);
    }
  }

  @Override
  boolean instanceOf(final AtomType kt, final SeqType dt) {
    for(final TrieNode nd : kids) {
      if(nd != null && !nd.instanceOf(kt, dt)) return false;
    }
    return true;
  }

  @Override
  int hash(final InputInfo ii) throws QueryException {
    int hash = 0;
    for(final TrieNode nd : kids) {
      if(nd != null) hash = (hash << 5) - hash + nd.hash(ii);
    }
    return hash;
  }

  @Override
  boolean deep(final TrieNode node, final Collation coll, final InputInfo ii)
      throws QueryException {
    if(!(node instanceof TrieBranch)) return false;
    final TrieBranch ob = (TrieBranch) node;

    // check bin usage first
    if(used != ob.used) return false;

    // recursively compare children
    for(int i = 0; i < KIDS; i++)
      if(kids[i] != null && !kids[i].deep(ob.kids[i], coll, ii)) return false;

    // everything OK
    return true;
  }

  @Override
  StringBuilder append(final StringBuilder sb, final String indent) {
    final int s = Integer.bitCount(used);
    for(int i = 0, j = 0; i < s; i++, j++) {
      while((used & 1 << j) == 0) j++;
      final int e = i == s - 1 ? 2 : 0;
      sb.append(indent).append(ENDS[e]).append(String.format("%x", j)).append('\n');
      kids[j].append(sb, indent + ENDS[e + 1]);
    }
    return sb;
  }

  @Override
  StringBuilder append(final StringBuilder sb) {
    for(int i = 0; i < KIDS && more(sb); i++) {
      if(kids[i] != null) kids[i].append(sb);
    }
    return sb;
  }
}
