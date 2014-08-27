package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Inner node of a {@link Map}.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
final class Branch extends TrieNode {
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
  Branch(final TrieNode[] kids, final int used, final int size) {
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
    System.arraycopy(kids, 0, copy, 0, KIDS);
    return copy;
  }

  @Override
  TrieNode insert(final int h, final Item k, final Value v, final int l, final InputInfo ii)
      throws QueryException {
    final int key = key(h, l);
    final TrieNode sub = kids[key], nsub;
    final int bs, rem;
    if(sub != null) {
      nsub = sub.insert(h, k, v, l + 1, ii);
      if(nsub == sub) return this;
      bs = used;
      rem = sub.size;
    } else {
      nsub = new Leaf(h, k, v);
      bs = used | 1 << key;
      rem = 0;
    }
    final TrieNode[] ks = copyKids();
    ks[key] = nsub;
    return new Branch(ks, bs, size - rem + nsub.size);
  }

  @Override
  TrieNode delete(final int h, final Item k, final int l, final InputInfo ii)
      throws QueryException {
    final int key = key(h, l);
    final TrieNode sub = kids[key];
    if(sub == null) return this;
    final TrieNode nsub = sub.delete(h, k, l + 1, ii);
    if(nsub == sub) return this;

    final int nu;
    if(nsub == null) {
      nu = used ^ 1 << key;
      if(Integer.bitCount(nu) == 1) {
        final TrieNode single = kids[Integer.numberOfTrailingZeros(nu)];
        // check whether the child depends on the right offset
        if(!(single instanceof Branch)) return single;
      }
    } else nu = used;

    final TrieNode[] ks = copyKids();
    ks[key] = nsub;
    return new Branch(ks, nu, size - 1);
  }

  @Override
  Value get(final int h, final Item k, final int l, final InputInfo ii) throws QueryException {
    final int key = key(h, l);
    final TrieNode sub = kids[key];
    return sub == null ? null : sub.get(h, k, l + 1, ii);
  }

  @Override
  boolean contains(final int h, final Item k, final int l, final InputInfo ii)
      throws QueryException {
    final int key = key(h, l);
    final TrieNode sub = kids[key];
    return sub != null && sub.contains(h, k, l + 1, ii);
  }

  /** End strings. */
  private static final String[] ENDS = { "|-- ", "|   ", "`-- ", "    " };

  @Override
  StringBuilder toString(final StringBuilder sb, final String ind) {
    final int s = Integer.bitCount(used);
    for(int i = 0, j = 0; i < s; i++, j++) {
      while((used & 1 << j) == 0) j++;
      final int e = i == s - 1 ? 2 : 0;
      sb.append(ind).append(ENDS[e]).append(
          String.format("%x", j)).append('\n');
      kids[j].toString(sb, ind + ENDS[e + 1]);
    }
    return sb;
  }

  @Override
  TrieNode addAll(final TrieNode o, final int l, final InputInfo ii) throws QueryException {
    return o.add(this, l, ii);
  }

  @Override
  TrieNode add(final Leaf o, final int l, final InputInfo ii) throws QueryException {
    final int k = key(o.hash, l);
    final TrieNode ch = kids[k], nw;
    if(ch != null) {
      final TrieNode ins = ch.add(o, l + 1, ii);
      if(ins == ch) return this;
      nw = ins;
    } else nw = o;

    final TrieNode[] ks = copyKids();
    ks[k] = nw;

    // we don't replace here, so the size must increase
    return new Branch(ks, used | 1 << k, size + 1);
  }

  @Override
  TrieNode add(final List o, final int l, final InputInfo ii) throws QueryException {
    final int k = key(o.hash, l);
    final TrieNode ch = kids[k], nw;
    int n = o.size;
    if(ch != null) {
      final TrieNode ins = ch.add(o, l + 1, ii);
      if(ins == ch) return this;
      n = ins.size - ch.size;
      nw = ins;
    } else nw = o;

    final TrieNode[] ks = copyKids();
    ks[k] = nw;

    // we don't replace here, so the size must increase
    return new Branch(ks, used | 1 << k, size + n);
  }

  @Override
  TrieNode add(final Branch o, final int l, final InputInfo ii) throws QueryException {
    TrieNode[] ch = null;
    int nu = used, ns = size;
    for(int i = 0; i < kids.length; i++) {
      final TrieNode k = kids[i], ok = o.kids[i];
      if(ok != null) {
        final TrieNode nw = k == null ? ok : ok.addAll(k, l + 1, ii);
        if(nw != k) {
          if(ch == null) ch = copyKids();
          ch[i] = nw;
          nu |= 1 << i;
          ns += nw.size - (k == null ? 0 : k.size);
        }
      }
    }
    return ch == null ? this : new Branch(ch, nu, ns);
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
  void keys(final ValueBuilder ks) {
    for(final TrieNode nd : kids) if(nd != null) nd.keys(ks);
  }

  @Override
  void values(final ValueBuilder vs) {
    for(final TrieNode nd : kids) if(nd != null) nd.values(vs);
  }

  @Override
  void apply(final ValueBuilder vb, final FItem func, final QueryContext qc, final InputInfo ii)
      throws QueryException {
    for(final TrieNode nd : kids) if(nd != null) nd.apply(vb, func, qc, ii);
  }

  @Override
  boolean hasType(final AtomType kt, final SeqType vt) {
    for(final TrieNode k : kids)
      if(!(k == null || k.hasType(kt, vt))) return false;
    return true;
  }

  @Override
  int hash(final InputInfo ii) throws QueryException {
    int hash = 0;
    for(final TrieNode ch : kids) if(ch != null) hash = 31 * hash + ch.hash(ii);
    return hash;
  }

  @Override
  boolean deep(final InputInfo ii, final TrieNode o, final Collation coll) throws QueryException {
    if(!(o instanceof Branch)) return false;
    final Branch ob = (Branch) o;

    // check bin usage first
    if(used != ob.used) return false;

    // recursively compare children
    for(int i = 0; i < KIDS; i++)
      if(kids[i] != null && !kids[i].deep(ii, ob.kids[i], coll)) return false;

    // everything OK
    return true;
  }

  @Override
  StringBuilder toString(final StringBuilder sb) {
    for(int i = 0; i < KIDS; i++) if(kids[i] != null) kids[i].toString(sb);
    return sb;
  }
}
