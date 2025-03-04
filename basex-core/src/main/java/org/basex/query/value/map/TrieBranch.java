package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Inner node of a {@link XQMap}.
 *
 * @author BaseX Team, BSD License
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
  }

  @Override
  TrieNode put(final int hs, final int lv, final TrieUpdate update) throws QueryException {
    final int k = hashKey(hs, lv), bs, rem;
    final TrieNode sub = kids[k], nsub;
    if(sub != null) {
      final TrieNode sb = sub.put(hs, lv + 1, update);
      // nothing has change: return existing instance
      if(sb == sub) return this;
      nsub = sb;
      bs = used;
      rem = sub.size;
    } else {
      update.add();
      nsub = new TrieLeaf(hs, update.key, update.value);
      bs = used | 1 << k;
      rem = 0;
    }
    final TrieNode[] ks = kids.clone();
    ks[k] = nsub;
    return new TrieBranch(ks, bs, size - rem + nsub.size);
  }

  @Override
  TrieNode remove(final int hs, final int lv, final TrieUpdate update) throws QueryException {
    final int k = hashKey(hs, lv);
    final TrieNode sub = kids[k];
    if(sub == null) return this;
    final TrieNode nsub = sub.remove(hs, lv + 1, update);
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
