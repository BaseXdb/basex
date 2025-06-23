package org.basex.query.func.fn;

import java.util.function.LongUnaryOperator;

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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnRandomNumberGenerator extends StandardFunc {
  /** Type for permute function. */
  private static final FuncType PERMUTE_TYPE = FuncType.get(SeqType.ITEM_ZM, SeqType.ITEM_ZM);
  /** Type for next function. */
  private static final FuncType NEXT_TYPE =
      FuncType.get(MapType.get(AtomType.STRING, SeqType.ITEM_O).seqType());

  /** Number key. */
  private static final Str NUMBER = Str.get("number");
  /** Next key. */
  private static final Str NEXT = Str.get("next");
  /** Permute key. */
  private static final Str PERMUTE = Str.get("permute");

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item seed = arg(0).atomItem(qc, info);

    final LongUnaryOperator number = l -> l * 0x5DEECE66DL + 0xBL & (1L << 48) - 1;
    final long i1 = number.applyAsLong(seed.isEmpty() ? qc.dateTime().nano : seed.hashCode());
    final long i2 = number.applyAsLong(i1);
    return new MapBuilder().
      // derived from Java's random class
      put(NUMBER, Dbl.get(((i1 >>> 22 << 27) + (i2 >>> 21)) / (double) (1L << 53))).
      put(NEXT, nextFunc(i2)).
      put(PERMUTE, permuteFunc(i1, qc)).map();
  }

  /**
   * Creates the permutation function initialized by the given seed.
   * @param seed initial seed
   * @param qc query context
   * @return permutation function
   */
  private FuncItem permuteFunc(final long seed, final QueryContext qc) {
    final Var var = new Var(new QNm("seq"), null, qc, info, 0, null);
    final StandardFunc sf = Function._RANDOM_SEEDED_PERMUTATION.get(info, Itr.get(seed),
        new VarRef(info, var));
    return new FuncItem(info, sf, new Var[] { var }, AnnList.EMPTY, PERMUTE_TYPE, 1, null);
  }

  /**
   * Creates the function returning the next random number generator.
   * @param seed initial seed
   * @return function returning the next random number generator
   */
  private FuncItem nextFunc(final long seed) {
    final StandardFunc sf = Function.RANDOM_NUMBER_GENERATOR.get(info, Itr.get(seed));
    return new FuncItem(info, sf, new Var[0], AnnList.EMPTY, NEXT_TYPE, 0, null);
  }
}
