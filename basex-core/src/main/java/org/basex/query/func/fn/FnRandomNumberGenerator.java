package org.basex.query.func.fn;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnRandomNumberGenerator extends StandardFunc {
  /** Type for permute function. */
  private static final FuncType PERMUTE_TYPE = FuncType.get(Types.ITEM_ZM, Types.ITEM_ZM);
  /** Type for next function. */
  private static final FuncType NEXT_TYPE =
      FuncType.get(Records.RANDOM_NUMBER_GENERATOR.get().seqType());
  /** Type for take function. */
  private static final FuncType TAKE_TYPE =
      FuncType.get(Types.DOUBLE_ZM, BasicType.NON_NEGATIVE_INTEGER.seqType());
  /** Parameter name of permute function. */
  private static final QNm Q_SEQ = new QNm("seq");
  /** Parameter name of take function. */
  private static final QNm Q_COUNT = new QNm("count");
  /** State increment of {@link SplittableRandom} (one step per number). */
  private static final long GAMMA = 0x9E3779B97F4A7C15L;

  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    final Item seed = arg(0).atomItem(qc, info);
    return generator(seed.isEmpty() ? qc.dateTime().nano : seed.hashCode(), qc, info);
  }

  /**
   * Creates a random number generator.
   * @param state internal state
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return random number generator
   */
  private static XQMap generator(final long state, final QueryContext qc, final InputInfo info) {
    final SplittableRandom random = new SplittableRandom(state);
    final Next next = new Next(info, state + GAMMA);
    return XQMap.get(Records.RANDOM_NUMBER_GENERATOR.get(),
      Dbl.get(random.nextDouble()),
      new FuncItem(info, next, new Var[0], AnnList.EMPTY, NEXT_TYPE, 0, null),
      permuteFunc(random.nextLong(), qc, info),
      takeFunc(state, qc, info));
  }

  /**
   * Creates the permutation function initialized by the given seed.
   * @param seed initial seed
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return permutation function
   */
  private static FuncItem permuteFunc(final long seed, final QueryContext qc,
      final InputInfo info) {
    final Var var = new Var(Q_SEQ, null, qc, info, 0, null);
    final StandardFunc sf = Function._RANDOM_SEEDED_PERMUTATION.get(info, Itr.get(seed),
        new VarRef(info, var));
    return new FuncItem(info, sf, new Var[] { var }, AnnList.EMPTY, PERMUTE_TYPE, 1, null);
  }

  /**
   * Creates the function returning the next random numbers.
   * @param state internal state
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return function returning random numbers
   */
  private static FuncItem takeFunc(final long state, final QueryContext qc,
      final InputInfo info) {
    final Var var = new Var(Q_COUNT, null, qc, info, 0, null);
    final Take take = new Take(info, state, new VarRef(info, var));
    return new FuncItem(info, take, new Var[] { var }, AnnList.EMPTY, TAKE_TYPE, 1, null);
  }

  /**
   * Function returning the next random number generator.
   */
  private static final class Next extends FuncItemBody {
    /** Internal state. */
    private final long state;

    /**
     * Constructor.
     * @param info input info (can be {@code null})
     * @param state internal state
     */
    private Next(final InputInfo info, final long state) {
      super(info, Records.RANDOM_NUMBER_GENERATOR.get().seqType(),
          Function.RANDOM_NUMBER_GENERATOR);
      this.state = state;
    }

    @Override
    public XQMap value(final QueryContext qc) {
      return generator(state, qc, info);
    }

    @Override
    public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
      return copyType(new Next(info, state));
    }
  }

  /**
   * Function returning the next random numbers.
   */
  private static final class Take extends FuncItemBody {
    /** Internal state. */
    private final long state;

    /**
     * Constructor.
     * @param info input info (can be {@code null})
     * @param state internal state
     * @param args function arguments
     */
    private Take(final InputInfo info, final long state, final Expr... args) {
      super(info, Types.DOUBLE_ZM, Function.RANDOM_NUMBER_GENERATOR, args);
      this.state = state;
    }

    @Override
    public Iter iter(final QueryContext qc) throws QueryException {
      final long count = toLong(arg(0).atomItem(qc, info), 0);

      return new Iter() {
        // same sequence as ?number, ?next()?number, ...
        final SplittableRandom random = new SplittableRandom(state);
        long c;

        @Override
        public Item next() {
          return c++ == count ? null : Dbl.get(random.nextDouble());
        }
      };
    }

    @Override
    public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
      return copyType(new Take(info, state, copyAll(cc, vm, args())));
    }
  }
}
