package org.basex.query.expr;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Filter expression with a range predicate, evaluated via a sorted index.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RangeFilter extends KeyedFilter<RangeFilter.Cache> {
  /** Key is compared with {@code <} or {@code <=} (otherwise {@code >} or {@code >=}). */
  private final boolean lower;
  /** Keys up to and including the probe are skipped by binary search ({@code <=}, {@code >}). */
  private final boolean upto;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param root root expression
   * @param pred range predicate
   * @param key position of the context-dependent operand (see {@link #key(Expr, Expr)})
   */
  RangeFilter(final InputInfo info, final Expr root, final Expr pred, final int key) {
    super(info, root, pred, key);
    final CmpOp cmp = ((CmpG) pred).op, op = key == 0 ? cmp : cmp.swap();
    lower = op.oneOf(CmpOp.LT, CmpOp.LE);
    upto = op.oneOf(CmpOp.LE, CmpOp.GT);
  }

  /**
   * Returns the position of the context-dependent operand of a numeric range predicate.
   * @param root root expression
   * @param pred predicate
   * @return position of the operand, or {@code -1} if the predicate cannot be indexed
   */
  static int key(final Expr root, final Expr pred) {
    return key(root, pred, cmp -> !cmp.op.oneOf(CmpOp.EQ, CmpOp.NE),
      (kt, pt) -> floating(kt) ? floating(pt) || pt.isUntyped() : kt.isUntyped() && floating(pt));
  }

  /**
   * Checks if values of the specified type are compared as doubles.
   * @param type type
   * @return result of check
   */
  private static boolean floating(final Type type) {
    return type.oneOf(BasicType.DOUBLE, BasicType.FLOAT);
  }

  @Override
  Cache cache() {
    return new Cache();
  }

  @Override
  Value indexed(final Value value, final Cache cache, final QueryContext qc)
      throws QueryException {
    if(cache.invalid) return null;
    // greatest probe for key < probe, smallest probe for key > probe
    final double probe = extreme(exprs[0].arg(1 - key).atomIter(qc, info), lower, qc);
    if(Double.isNaN(probe)) return Empty.VALUE;

    if(cache.keys == null && !index(value, cache, qc)) {
      cache.invalid = true;
      return null;
    }

    // keys are sorted in ascending order: find the end of the keys that are (not) matched
    final double[] keys = cache.keys;
    int l = 0, h = keys.length;
    while(l < h) {
      final int m = l + h >>> 1;
      if(upto ? keys[m] <= probe : keys[m] < probe) l = m + 1;
      else h = m;
    }
    final int[] positions = cache.positions;
    final int from = lower ? 0 : l, to = lower ? l : positions.length;
    if(from == to) return Empty.VALUE;
    if(to - from == value.size()) return value;
    return items(value, new IntList(Arrays.copyOfRange(positions, from, to)).sort(), qc);
  }

  /**
   * Returns the smallest or greatest non-NaN value of the specified items.
   * @param iter items
   * @param max greatest value
   * @param qc query context
   * @return value, or {@code NaN} if no value was found
   * @throws QueryException query exception
   */
  private double extreme(final Iter iter, final boolean max, final QueryContext qc)
      throws QueryException {
    double ext = Double.NaN;
    for(Item item; (item = qc.next(iter)) != null;) {
      final double d = item.dbl(info);
      if(!Double.isNaN(d)) ext = Double.isNaN(ext) ? d : max ? Math.max(ext, d) : Math.min(ext, d);
    }
    return ext;
  }

  /**
   * Creates an index from the smallest or greatest key of each item, sorted in ascending order.
   * @param value root value
   * @param cache cache
   * @param qc query context
   * @return {@code false} if a key cannot be converted to a double
   */
  private boolean index(final Value value, final Cache cache, final QueryContext qc) {
    final DoubleList keys = new DoubleList();
    final IntList positions = new IntList();
    try {
      keys(value, qc, (p, iter) -> {
        final double k = extreme(iter, !lower, qc);
        if(!Double.isNaN(k)) {
          keys.add(k);
          positions.add(p);
        }
        return true;
      });
    } catch(final QueryException ex) {
      // the error will be raised by the sequential evaluation
      Util.debug(ex);
      return false;
    }
    final double[] ks = keys.finish();
    positions.sort(ks, true);
    cache.keys = ks;
    cache.positions = positions.finish();
    return true;
  }

  @Override
  public RangeFilter copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new RangeFilter(info, root.copy(cc, vm), exprs[0].copy(cc, vm), key));
  }

  /**
   * Sorted index of the last filtered root value.
   */
  static final class Cache {
    /** Smallest or greatest key of each item, sorted in ascending order. */
    double[] keys;
    /** Positions of the items with the sorted keys. */
    int[] positions;
    /** Indicates if the keys cannot be indexed. */
    boolean invalid;
  }
}
