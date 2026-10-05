package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Filter expression with an equality predicate, evaluated via a hash index.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class HashFilter extends Filter {
  /** Position of the predicate operand that depends on the context. */
  private final int key;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param root root expression
   * @param pred hashable predicate
   * @param key position of the context-dependent operand (see {@link #key(Expr, Expr[])})
   */
  HashFilter(final InputInfo info, final Expr root, final Expr pred, final int key) {
    super(info, root, pred);
    this.key = key;
  }

  /**
   * Returns the position of the context-dependent operand of a hashable filter predicate.
   * @param root root expression
   * @param preds predicates
   * @return position of the operand, or {@code -1} if the filter cannot be hashed
   */
  static int key(final Expr root, final Expr[] preds) {
    if(preds.length != 1 || root.seqType().zeroOrOne() || !(preds[0] instanceof final CmpG cmp) ||
        cmp.op != CmpOp.EQ || cmp.sc().collation != null) return -1;
    for(int k = 0; k < 2; k++) {
      final Expr kx = cmp.arg(k), px = cmp.arg(1 - k);
      if(kx.has(Flag.CTX) && !kx.has(Flag.POS, Flag.NDT) && !(px instanceof Value) &&
          !px.has(Flag.CTX, Flag.POS, Flag.NDT) &&
          CmpHashG.hashable(kx.seqType().type, px.seqType().type)) return k;
    }
    return -1;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = root.iter(qc);
    final Value hashed = hashed(iter, qc);
    return hashed != null ? hashed.iter() : filterIter(iter, qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter iter = root.iter(qc);
    final Value hashed = hashed(iter, qc);
    return hashed != null ? hashed : filterValue(iter, qc);
  }

  /**
   * Evaluates the filter via the hash index of a root value that has been filtered before.
   * @param iter iterator of the root expression
   * @param qc query context
   * @return result, or {@code null} if the filter must be evaluated sequentially
   * @throws QueryException query exception
   */
  private Value hashed(final Iter iter, final QueryContext qc) throws QueryException {
    final Value value = iter.eagerValue();
    if(value == null || value.size() < 2 || value.size() > Integer.MAX_VALUE) return null;

    // first evaluation with this root: remember it, evaluate sequentially
    final Cache cache = qc.threads.get(this).get();
    if(cache.root != value) {
      cache.root = value;
      cache.index = null;
      cache.family = null;
      return null;
    }

    // keys of different or unsupported families: evaluate sequentially
    if(cache.family == BasicType.ITEM) return null;
    final Value probes = exprs[0].arg(1 - key).atomValue(qc, info);
    if(probes.isEmpty()) return Empty.VALUE;
    if(cache.index == null) {
      cache.index = index(value, cache, qc);
      if(cache.index == null) return null;
    }
    if(cache.index.isEmpty()) return Empty.VALUE;
    // probe values must belong to the family of the keys
    for(final Item item : probes) {
      if(CmpHashG.family(item.type) != cache.family) return null;
    }

    IntList positions;
    if(probes.size() == 1) {
      positions = cache.index.get(probes.itemAt(0));
      if(positions == null) return Empty.VALUE;
    } else {
      positions = new IntList();
      for(final Item item : probes) {
        final IntList list = cache.index.get(item);
        if(list != null) positions.add(list.toArray());
      }
      positions.ddo();
    }

    final int ps = positions.size();
    if(ps == 1) return value.itemAt(positions.get(0));
    final ValueBuilder vb = new ValueBuilder(qc, ps);
    for(int p = 0; p < ps; p++) vb.add(value.itemAt(positions.get(p)));
    return vb.value(this);
  }

  /**
   * Creates a hash index from the values of the key operand to the positions of the items.
   * @param value root value
   * @param cache cache, in which the family of the keys is stored
   * @param qc query context
   * @return index, or {@code null} if the keys belong to different or unsupported families
   * @throws QueryException query exception
   */
  private ItemObjectMap<IntList> index(final Value value, final Cache cache,
      final QueryContext qc) throws QueryException {
    final Expr kx = exprs[0].arg(key);
    final ItemObjectMap<IntList> index = new ItemObjectMap<>(ItemSet.Mode.EQUAL, info);
    final QueryFocus qf = qc.focus;
    final Value qv = qf.value;
    try {
      final int vs = (int) value.size();
      for(int p = 0; p < vs; p++) {
        qc.checkStop();
        qf.value = value.itemAt(p);
        final Iter iter = kx.atomIter(qc, info);
        for(Item item; (item = qc.next(iter)) != null;) {
          cache.family = CmpHashG.family(cache.family, item);
          if(cache.family == BasicType.ITEM) return null;
          final IntList list = index.computeIfAbsent(item, () -> new IntList(1));
          if(list.isEmpty() || list.peek() != p) list.add(p);
        }
      }
    } finally {
      qf.value = qv;
    }
    return index;
  }

  @Override
  public HashFilter copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new HashFilter(info, root.copy(cc, vm), exprs[0].copy(cc, vm), key));
  }

  /**
   * Hash index of the last filtered root value.
   */
  public static final class Cache {
    /** Last filtered root value (compared by identity). */
    Value root;
    /** Index from keys to item positions (built on the second evaluation). */
    ItemObjectMap<IntList> index;
    /** Family of the keys ({@link BasicType#ITEM}: keys cannot be indexed). */
    Type family;
  }
}
