package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnRandomNumberGenerator extends StandardFunc {
  /** Multiplier for the RNG. */
  private static final long MULT = 0x5DEECE66DL;
  /** Additive component for the RNG. */
  private static final long ADD = 0xBL;
  /** Mask for the RNG. */
  private static final long MASK = (1L << 48) - 1;

  /** Number key. */
  private static final Str NUMBER = Str.get("number");
  /** Next key. */
  private static final Str NEXT = Str.get("next");
  /** Permute key. */
  private static final Str PERMUTE = Str.get("permute");

  /**
   * Computes the next seed for the given one.
   * @param oldSeed old seed
   * @return new seed
   */
  private static long next(final long oldSeed) {
    return oldSeed * MULT + ADD & MASK;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final long seed;
    if(exprs.length > 0) {
      seed = toAtomItem(exprs[0], qc).hash(ii);
    } else {
      seed = qc.initDateTime().nano;
    }
    return result(seed, qc);
  }

  /**
   * Returns a new result.
   * @param s0 initial seed
   * @param qc query context
   * @return map
   * @throws QueryException query exception
   */
  private Map result(final long s0, final QueryContext qc) throws QueryException {
    final long s1 = next(s0), s2 = next(s1);
    final double dbl = ((s1 >>> 22 << 27) + (s2 >>> 21)) / (double) (1L << 53);
    final Dbl number = Dbl.get(dbl);
    final FItem next = nextFunc(s2, qc), permute = permuteFunc(s1, qc);
    return Map.EMPTY.put(NUMBER, number, info).put(NEXT, next, info).put(PERMUTE, permute, info);
  }

  /**
   * Creates the permutation function initialized by the given seed.
   * @param seed initial seed
   * @param qctx query context
   * @return permutation function
   */
  private FuncItem permuteFunc(final long seed, final QueryContext qctx) {
    return RuntimeExpr.funcItem(new RuntimeExpr(info) {
      @Override
      public Value value(final QueryContext qc) throws QueryException {
        final ItemList cache = qc.get(params[0]).cache();
        final int sz = cache.size();

        final Item[] items = cache.internal();
        long s = seed;
        for(int i = sz; --i >= 1;) {
          s = next(s);
          final int j = (int) ((s >>> 16) % (i + 1));
          if(i != j) {
            final Item item = items[i];
            items[i] = items[j];
            items[j] = item;
          }
        }
        return cache.value();
      }
    }, 1, sc, qctx);
  }

  /**
   * Creates the function returning the next random number generator.
   * @param seed initial seed
   * @param qctx query context
   * @return function returning the next random number generator
   */
  private FuncItem nextFunc(final long seed, final QueryContext qctx) {
    return RuntimeExpr.funcItem(new RuntimeExpr(info) {
      @Override
      public Map item(final QueryContext qc, final InputInfo ii) throws QueryException {
        return result(seed, qc);
      }

      @Override
      public Value value(final QueryContext qc) throws QueryException {
        return result(seed, qc);
      }
    }, 0, sc, qctx);
  }
}
