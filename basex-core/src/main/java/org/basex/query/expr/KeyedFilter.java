package org.basex.query.expr;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Filter expression with a predicate whose context-dependent operand is indexed.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param <C> cache type
 */
public abstract class KeyedFilter<C> extends Filter {
  /** Position of the predicate operand that depends on the context. */
  final int key;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param root root expression
   * @param pred predicate
   * @param key position of the context-dependent operand
   */
  KeyedFilter(final InputInfo info, final Expr root, final Expr pred, final int key) {
    super(info, root, pred);
    this.key = key;
  }

  /**
   * Returns the position of the context-dependent operand of a comparison that can be indexed.
   * @param root root expression
   * @param pred predicate
   * @param cmp check of the comparison
   * @param types check of the types of the context-dependent and the other operand
   * @return position of the operand, or {@code -1} if the predicate cannot be indexed
   */
  static int key(final Expr root, final Expr pred, final Predicate<CmpG> cmp,
      final BiPredicate<Type, Type> types) {
    if(root.seqType().zeroOrOne() || !(pred instanceof final CmpG cmpg) || !cmp.test(cmpg)) {
      return -1;
    }
    for(int k = 0; k < 2; k++) {
      final Expr kx = cmpg.arg(k), px = cmpg.arg(1 - k);
      if(kx.has(Flag.CTX) && !kx.has(Flag.POS, Flag.NDT) && !(px instanceof Value) &&
          !px.has(Flag.CTX, Flag.POS, Flag.NDT) &&
          types.test(kx.seqType().type, px.seqType().type) && !kx.hasFreeVars()) return k;
    }
    return -1;
  }

  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = root.iter(qc);
    final Value indexed = cached(iter, qc);
    return indexed != null ? indexed.iter() : filterIter(iter, qc);
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final Iter iter = root.iter(qc);
    final Value indexed = cached(iter, qc);
    return indexed != null ? indexed : filterValue(iter, qc);
  }

  /**
   * Evaluates the filter via the index of a root value that has been filtered before.
   * @param iter iterator of the root expression
   * @param qc query context
   * @return result, or {@code null} if the filter must be evaluated sequentially
   * @throws QueryException query exception
   */
  private Value cached(final Iter iter, final QueryContext qc) throws QueryException {
    final Value value = iter.eagerValue();
    if(value == null || value.size() < 2 || value.size() > Integer.MAX_VALUE) return null;

    // first evaluation with this root: remember it, evaluate sequentially
    final Entry<C> entry = qc.caches.get(this, Entry::new);
    if(entry.root != value) {
      entry.root = value;
      entry.cache = cache();
      return null;
    }
    return indexed(value, entry.cache, qc);
  }

  /**
   * Creates a new cache.
   * @return cache
   */
  abstract C cache();

  /**
   * Evaluates the filter via the index of the root value.
   * @param value root value
   * @param cache cache
   * @param qc query context
   * @return result, or {@code null} if the filter must be evaluated sequentially
   * @throws QueryException query exception
   */
  abstract Value indexed(Value value, C cache, QueryContext qc) throws QueryException;

  /**
   * Evaluates the context-dependent operand for each item of the root value.
   * @param value root value
   * @param qc query context
   * @param keys consumer of the item position and the atomized keys (returns {@code false} to stop)
   * @return {@code false} if the evaluation was stopped
   * @throws QueryException query exception
   */
  final boolean keys(final Value value, final QueryContext qc,
      final QueryBiPredicate<Integer, Iter> keys) throws QueryException {
    final Expr kx = exprs[0].arg(key);
    final QueryFocus qf = qc.focus;
    final Value qv = qf.value;
    try {
      int p = 0;
      for(final Item item : value) {
        qc.checkStop();
        qf.value = item;
        if(!keys.test(p++, kx.atomIter(qc, info))) return false;
      }
    } finally {
      qf.value = qv;
    }
    return true;
  }

  /**
   * Returns the items of the root value at the specified positions.
   * @param value root value
   * @param positions positions in ascending order
   * @param qc query context
   * @return items
   */
  final Value items(final Value value, final IntList positions, final QueryContext qc) {
    final int ps = positions.size();
    if(ps == 1) return value.itemAt(positions.get(0));
    final ValueBuilder vb = new ValueBuilder(qc, ps);
    for(int p = 0; p < ps; p++) vb.add(value.itemAt(positions.get(p)));
    return vb.value(this);
  }

  /**
   * Last filtered root value and its index.
   * @param <C> cache type
   */
  private static final class Entry<C> {
    /** Last filtered root value (compared by identity). */
    Value root;
    /** Index of the root value (built on the next evaluation). */
    C cache;
  }
}
