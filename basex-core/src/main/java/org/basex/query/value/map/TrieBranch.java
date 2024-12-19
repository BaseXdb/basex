package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Inner node of a {@link XQMap}.
 *
 * @author BaseX Team 2005-24, BSD License
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

  @Override
  TrieNode put(final int hs, final Item ky, final Value vl, final int lv)
      throws QueryException {
    final int k = hashKey(hs, lv), bs, rem;
    final TrieNode sub = kids[k], nsub;
    if(sub != null) {
      nsub = sub.put(hs, ky, vl, lv + 1);
      if(nsub == sub) return this;
      bs = used;
      rem = sub.size;
    } else {
      nsub = new TrieLeaf(hs, ky, vl);
      bs = used | 1 << k;
      rem = 0;
    }
    final TrieNode[] ks = kids.clone();
    ks[k] = nsub;
    return new TrieBranch(ks, bs, size - rem + nsub.size);
  }

  @Override
  TrieNode delete(final int hs, final Item ky, final int lv) throws QueryException {
    final int k = hashKey(hs, lv);
    final TrieNode sub = kids[k];
    if(sub == null) return this;
    final TrieNode nsub = sub.delete(hs, ky, lv + 1);
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
    final TrieNode[] ks = kids.clone();
    ks[k] = nsub;
    return new TrieBranch(ks, nu, size - 1);
  }

  @Override
  Value get(final int hs, final Item ky, final int lv) throws QueryException {
    final int k = hashKey(hs, lv);
    final TrieNode sub = kids[k];
    return sub == null ? null : sub.get(hs, ky, lv + 1);
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
  void apply(final QueryBiConsumer<Item, Value> func) throws QueryException {
    for(final TrieNode nd : kids) {
      if(nd != null) nd.apply(func);
    }
  }

  @Override
  boolean test(final QueryBiPredicate<Item, Value> func) throws QueryException {
    for(final TrieNode nd : kids) {
      if(nd != null && !nd.test(func)) return false;
    }
    return true;
  }

  @Override
  boolean equal(final TrieNode node, final DeepEqual deep) throws QueryException {
    if(!(node instanceof TrieBranch)) return false;

    // check bit usage first
    final TrieBranch ob = (TrieBranch) node;
    if(used != ob.used) return false;
    // recursively compare children
    if(deep != null && deep.qc != null) deep.qc.checkStop();
    for(int k = 0; k < KIDS; k++) {
      if(kids[k] != null && !kids[k].equal(ob.kids[k], deep)) return false;
    }
    // everything OK
    return true;
  }

  /** End strings. */
  private static final String[] ENDS = { "|-- ", "|   ", "`-- ", "    " };

  @Override
  void add(final TokenBuilder tb, final String indent) {
    final int s = Integer.bitCount(used);
    for(int i = 0, j = 0; i < s; i++, j++) {
      while((used & 1 << j) == 0) j++;
      final int e = i == s - 1 ? 2 : 0;
      tb.add(indent).add(ENDS[e]).add(String.format("%x", j)).add('\n');
      kids[j].add(tb, indent + ENDS[e + 1]);
    }
  }
}
