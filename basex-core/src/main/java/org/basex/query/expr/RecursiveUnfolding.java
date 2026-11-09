package org.basex.query.expr;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Rewrites a recursive function body to an iterative expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RecursiveUnfolding extends Unfolding {
  /** Supported operations. */
  private static final EnumSet<Op> SUPPORTED = EnumSet.of(Op.LIST, Op.CONCAT, Op.ADD, Op.MULTIPLY);

  /** Recursive function. */
  private final StaticFunc func;
  /** Input info (can be {@code null}). */
  private final InputInfo info;
  /** State variables. */
  private final Var[] vars;
  /** Variable indicating that a recursion step was taken. */
  private final Var flag;

  /** Variable for the results that follow the recursive call (can be {@code null}). */
  private Var suffix;
  /** Type of the results that follow the recursive call (can be {@code null}). */
  private SeqType suffixType;
  /** Indicates if code is evaluated in a different order than in the recursive body. */
  private boolean reordered;
  /** Indicates if the body has a branch without recursive call. */
  private boolean based;

  /**
   * Constructor.
   * @param func recursive function
   * @param vars state variables
   * @param cc compilation context
   */
  private RecursiveUnfolding(final StaticFunc func, final Var[] vars, final CompileContext cc) {
    super(SUPPORTED);
    this.func = func;
    this.vars = vars;
    info = func.info();
    flag = cc.vs().addNew(new QNm("recur"), null, cc.qc, info);
  }

  /**
   * Rewrites a function body with recursive calls in tail position modulo sequence construction,
   * addition, multiplication or string concatenation to an iterative expression.
   * @param func recursive function
   * @param declType declared return type (can be {@code null})
   * @param cc compilation context
   * @return rewritten body (can be {@code null})
   * @throws QueryException query exception
   */
  public static Expr unfold(final StaticFunc func, final SeqType declType,
      final CompileContext cc) throws QueryException {
    final Expr body = func.expr;
    if(!contains(body, func)) return null;

    // replace parameters with state variables
    final IntObjectMap<Var> vm = new IntObjectMap<>();
    final int pl = func.params.length;
    final Expr[] init = new Expr[pl];
    final Var[] vars = new Var[pl];
    final RecursiveUnfolding uf = new RecursiveUnfolding(func, vars, cc);
    final InputInfo ii = uf.info;
    for(int p = 0; p < pl; p++) {
      final Var param = func.params[p];
      init[p] = new VarRef(ii, param).optimize(cc);
      vars[p] = cc.vs().add(new Var(param.name, null, cc.qc, param.info, -1,
          new ExprType(param.seqType())));
      vm.put(param.id, vars[p]);
    }

    // replace recursive calls with recursion steps; reordered code must have no side effects
    final Expr step = body.copy(cc, vm).unfold(uf, cc);
    if(!uf.based || contains(step, func) || !uf.typeSafe(step.seqType(),
        body.seqType(), declType) || uf.reordered && body.has(Flag.NDT, Flag.CNS)) return null;

    if(uf.suffix != null) uf.suffix.refineType(uf.suffixType, cc);
    final Expr unfold = new Unfold(ii, init, vars, uf.flag, uf.suffix, step).optimize(cc);
    return switch(uf.op()) {
      case LIST -> unfold;
      case CONCAT -> cc.function(Function.STRING_JOIN, ii, unfold);
      case ADD -> cc.function(Function.SUM, ii, unfold);
      // products: keep the order of the recursive multiplications (intermediate overflows)
      case MULTIPLY -> cc.function(Function.FOLD_RIGHT, ii, unfold, Itr.ONE,
          cc.function(Function.OP, ii, Str.get(Calc.MULTIPLY.name)));
      default -> throw Util.notExpected();
    };
  }

  /**
   * Checks if the unfolded body yields the declared type without the dropped per-level checks.
   * @param st type of the step results
   * @param bt type of the original body
   * @param declType declared return type (can be {@code null})
   * @return result of check
   */
  private boolean typeSafe(final SeqType st, final SeqType bt, final SeqType declType) {
    final Op op = op();
    if(op == Op.LIST) return declType == null || declType.occ == Occ.ZERO_OR_MORE;
    // results of the base case must be strings, as they are not converted by the original body
    if(op == Op.CONCAT) return bt.instanceOf(Types.STRING_O) &&
        (declType == null || Types.STRING_O.instanceOf(declType));
    // exact numbers only: reassociation changes the rounding of floating-point numbers
    return st.one() && st.type.instanceOf(BasicType.DECIMAL) &&
        (declType == null || st.instanceOf(declType));
  }

  /**
   * Checks if the specified expression calls the specified function.
   * @param expr expression
   * @param func function
   * @return result of check
   */
  private static boolean contains(final Expr expr, final StaticFunc func) {
    return !expr.accept(new ASTVisitor() {
      @Override
      public boolean staticFuncCall(final StaticFuncCall call) {
        return call.func() != func;
      }
    });
  }

  @Override
  protected boolean hole(final Expr expr) {
    return expr instanceof final StaticFuncCall call && call.func() == func;
  }

  @Override
  protected Expr replace(final Expr[] ops, final int hole, final Op o, final CompileContext cc)
      throws QueryException {
    // operands following the call: suffix (ordered) or prefix (order-independent reduction)
    final int ol = ops.length;
    final ExprList list = new ExprList(ol + 1);
    Expr sfx = null;
    if(hole < ol - 1) {
      reordered = true;
      if(o.ordered()) {
        sfx = List.get(cc, info, Arrays.copyOfRange(ops, hole + 1, ol));
        if(suffix == null) suffix = cc.vs().addNew(new QNm("suffix"), null, cc.qc, info);
        suffixType = suffixType == null ? sfx.seqType() : suffixType.union(sfx.seqType());
      }
    }
    final Expr[] prefix = sfx != null ? Arrays.copyOf(ops, hole) : Array.remove(ops, hole);
    list.add(List.get(cc, info, prefix)).add(ops[hole].args());
    if(sfx != null) list.add(sfx);
    return new Recur(info, list.finish(), vars, flag, sfx != null ? suffix : null).optimize(cc);
  }

  @Override
  public Expr base(final Expr expr) {
    // without a branch that ends the recursion, the loop would never terminate
    based = true;
    return expr;
  }
}
