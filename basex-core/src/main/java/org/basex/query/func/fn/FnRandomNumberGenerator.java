package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnRandomNumberGenerator extends StandardFunc {
  /** Type for permute function. */
  private static final FuncType PERMUTE_TYPE = FuncType.get(SeqType.ITEM_ZM, SeqType.ITEM_ZM);
  /** Type for next function. */
  private static final FuncType NEXT_TYPE =
      FuncType.get(MapType.get(AtomType.STR, SeqType.ITEM_O).seqType());

  /** Multiplier for the RNG (derived from Java's random class). */
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
    final Item item = exprs.length > 0 ? exprs[0].atomItem(qc, info) : null;
    final long seed = item != null ? item.hash(info) : qc.initDateTime().nano;
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
    // derived from Java's random class
    final long s1 = next(s0), s2 = next(s1);
    final Dbl number = Dbl.get(((s1 >>> 22 << 27) + (s2 >>> 21)) / (double) (1L << 53));
    final FItem next = nextFunc(s2), permute = permuteFunc(s1, qc);
    return Map.EMPTY.put(NUMBER, number, info).put(NEXT, next, info).put(PERMUTE, permute, info);
  }

  /**
   * Creates the permutation function initialized by the given seed.
   * @param seed initial seed
   * @param qctx query context
   * @return permutation function
   */
  private FuncItem permuteFunc(final long seed, final QueryContext qctx) {
    final Var var = new Var(new QNm("seq"), null, true, 0, qctx, sc, info);
    final VarRef ref = new VarRef(info, var);
    final StandardFunc sf = Function._RANDOM_SEEDED_PERMUTATION.get(sc, info, Int.get(seed), ref);
    return new FuncItem(sc, new AnnList(), null, new Var[] { var }, PERMUTE_TYPE, sf, 1);
  }

  /**
   * Creates the function returning the next random number generator.
   * @param seed initial seed
   * @return function returning the next random number generator
   */
  private FuncItem nextFunc(final long seed) {
    final StandardFunc sf = Function.RANDOM_NUMBER_GENERATOR.get(sc, info, Int.get(seed));
    return new FuncItem(sc, new AnnList(), null, new Var[0], NEXT_TYPE, sf, 0);
  }
}
