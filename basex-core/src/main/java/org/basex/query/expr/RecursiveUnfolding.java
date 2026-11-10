package org.basex.query.expr;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
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
public abstract class RecursiveUnfolding extends Unfolding {
  /** Recursive function. */
  final StaticFunc func;
  /** State variables. */
  private final Var[] vars;
  /** Variable indicating that a recursion step was taken. */
  private final Var flag;

  /** Indicates if the body has a branch without recursive call. */
  boolean based;
  /** Variable for the results that follow the recursive call (can be {@code null}). */
  private Var suffix;
  /** Type of the results that follow the recursive call (can be {@code null}). */
  private SeqType suffixType;

  /**
   * Constructor.
   * @param func recursive function
   * @param vars state variables
   * @param acc accumulator (can be {@code null})
   * @param supported supported operations
   * @param cc compilation context
   */
  RecursiveUnfolding(final StaticFunc func, final Var[] vars, final Var acc,
      final EnumSet<Op> supported, final CompileContext cc) {
    super(acc, supported, func.info);
    this.func = func;
    this.vars = vars;
    flag = cc.vs().addNew(new QNm("recur"), null, cc.qc, info);
  }

  /**
   * Rewrites a recursive function body to an iterative expression.
   * @param func recursive function
   * @param declType declared return type (can be {@code null})
   * @param cc compilation context
   * @return rewritten body (can be {@code null})
   * @throws QueryException query exception
   */
  public static Expr unfold(final StaticFunc func, final SeqType declType,
      final CompileContext cc) throws QueryException {
    final ArrayList<StaticFuncCall> calls = new ArrayList<>();
    if(!calls(func.expr, func, calls)) return null;

    // try parameters that accumulate values, then the plain recursion
    final Var[] params = func.params;
    final int pl = params.length;
    for(int p = 0; p < pl; p++) {
      final Var param = params[p];
      final int a = p;
      if(calls.stream().anyMatch(call -> {
        final Expr arg = call.arg(a);
        return candidate(arg) && arg.uses(param);
      })) {
        final Expr expr = unfold(func, declType, p, cc);
        if(expr != null) return expr;
      }
    }
    return unfold(func, declType, -1, cc);
  }

  /**
   * Rewrites a recursive function body to an iterative expression.
   * @param func recursive function
   * @param declType declared return type (can be {@code null})
   * @param accPos position of the accumulator in the parameter list ({@code -1}: none)
   * @param cc compilation context
   * @return rewritten body (can be {@code null})
   * @throws QueryException query exception
   */
  private static Expr unfold(final StaticFunc func, final SeqType declType, final int accPos,
      final CompileContext cc) throws QueryException {

    // replace parameters with state variables
    final IntObjectMap<Var> vm = new IntObjectMap<>();
    final ExprList init = new ExprList();
    final ArrayList<Var> list = new ArrayList<>();
    final InputInfo ii = func.info;
    Var acc = null;
    final int pl = func.params.length;
    for(int p = 0; p < pl; p++) {
      final Var param = func.params[p];
      final Var var = cc.vs().add(new Var(param.name, null, cc.qc, param.info, -1,
          new ExprType(param.seqType())));
      vm.put(param.id, var);
      if(p == accPos) {
        acc = var;
      } else {
        list.add(var);
        init.add(new VarRef(ii, param).optimize(cc));
      }
    }

    final Var[] vars = list.toArray(Var[]::new);
    final RecursiveUnfolding uf = acc != null ?
      new Accumulating(func, vars, acc, accPos, cc) : new Plain(func, vars, declType, cc);
    final Expr body = func.expr, step = body.copy(cc, vm).unfold(uf, cc);
    if(!uf.based || !uf.valid() || !uf.safe(step, body) || calls(step, func)) return null;

    if(uf.suffix != null) uf.suffix.refineType(uf.suffixType, cc);
    final Expr unfold = new Unfold(ii, init.finish(), vars, uf.flag, uf.suffix, step).optimize(cc);
    return uf.reduce(acc != null ? new VarRef(ii, func.params[accPos]).optimize(cc) : null,
        unfold, cc);
  }

  /**
   * Checks if the unfolded body can replace the recursive body.
   * @param step unfolded body
   * @param body original body
   * @return result of check
   */
  abstract boolean safe(Expr step, Expr body);

  /**
   * Collects the calls of the specified function.
   * @param expr expression
   * @param func function
   * @param calls list for the calls ({@code null}: stop at the first call)
   * @return {@code true} if the expression contains a call
   */
  private static boolean calls(final Expr expr, final StaticFunc func,
      final ArrayList<StaticFuncCall> calls) {
    final boolean[] found = { false };
    expr.accept(new ASTVisitor() {
      @Override
      public boolean staticFuncCall(final StaticFuncCall call) {
        if(call.func() != func) return true;
        found[0] = true;
        if(calls == null) return false;
        calls.add(call);
        return true;
      }
    });
    return found[0];
  }

  /**
   * Checks if the specified expression calls the specified function.
   * @param expr expression
   * @param func function
   * @return result of check
   */
  private static boolean calls(final Expr expr, final StaticFunc func) {
    return calls(expr, func, null);
  }

  /**
   * Checks if the specified expression is a recursive call.
   * @param expr expression
   * @return result of check
   */
  final boolean call(final Expr expr) {
    return expr instanceof final StaticFuncCall call && call.func() == func;
  }

  /**
   * Creates a recursion step.
   * @param prefix results preceding the call
   * @param args arguments of the call
   * @param sfx results following the call (can be {@code null})
   * @param cc compilation context
   * @return recursion step
   */
  final Expr recur(final Expr prefix, final Expr[] args, final Expr sfx,
      final CompileContext cc) {
    final ExprList list = new ExprList(args.length + 2).add(prefix).add(args);
    if(sfx != null) {
      if(suffix == null) suffix = cc.vs().addNew(new QNm("suffix"), null, cc.qc, info);
      suffixType = suffixType == null ? sfx.seqType() : suffixType.union(sfx.seqType());
      list.add(sfx);
    }
    return new Recur(info, list.finish(), vars, flag, sfx != null ? suffix : null).optimize(cc);
  }

  /**
   * Rewrites a recursive body whose recursive calls are operands of accumulating expressions:
   * {@code if(C) then E else (X, f($n + 1))}.
   */
  private static final class Plain extends RecursiveUnfolding {
    /** Declared return type, checked by the base expressions (can be {@code null}). */
    private final SeqType declType;
    /** Indicates if code is evaluated in a different order than in the recursive body. */
    private boolean reordered;

    /**
     * Constructor.
     * @param func recursive function
     * @param vars state variables
     * @param declType declared return type (can be {@code null})
     * @param cc compilation context
     */
    Plain(final StaticFunc func, final Var[] vars, final SeqType declType,
        final CompileContext cc) {
      super(func, vars, null, EnumSet.of(Op.LIST, Op.CONCAT, Op.ADD, Op.MULTIPLY), cc);
      this.declType = declType;
    }

    @Override
    boolean safe(final Expr step, final Expr body) {
      return typeSafe(step, body) && !(reordered && body.has(Flag.NDT, Flag.CNS));
    }

    /**
     * Checks if the unfolded body yields the declared type without the dropped per-level checks.
     * @param step unfolded body
     * @param body original body
     * @return result of check
     */
    private boolean typeSafe(final Expr step, final Expr body) {
      // sequences: the base expressions are checked, the outermost result is the largest
      final Op op = op();
      if(op == null || op == Op.LIST) return true;
      // results of the base case must be strings, as they are not converted by the original body
      if(op == Op.CONCAT) return body.seqType().instanceOf(Types.STRING_O) &&
          (declType == null || Types.STRING_O.instanceOf(declType));
      // exact numbers only: reassociation changes the rounding of floating-point numbers
      final SeqType st = step.seqType();
      return st.one() && st.type.instanceOf(BasicType.DECIMAL) &&
          (declType == null || st.instanceOf(declType));
    }

    @Override
    protected boolean hole(final Expr expr) {
      return call(expr);
    }

    @Override
    protected boolean contains(final Expr expr) {
      return calls(expr, func);
    }

    @Override
    protected Expr replace(final Expr[] ops, final int hole, final Op o, final CompileContext cc)
        throws QueryException {
      // operands following the call: suffix (ordered) or prefix (order-independent reduction)
      final int ol = ops.length;
      Expr sfx = null;
      if(hole < ol - 1) {
        reordered = true;
        if(o.ordered()) sfx = List.get(cc, info, Arrays.copyOfRange(ops, hole + 1, ol));
      }
      final Expr[] prefix = sfx != null ? Arrays.copyOf(ops, hole) : Array.remove(ops, hole);
      return recur(List.get(cc, info, prefix), ops[hole].args(), sfx, cc);
    }

    @Override
    public Expr base(final Expr expr, final CompileContext cc) throws QueryException {
      // tail call: no results
      if(call(expr)) return recur(Empty.VALUE, expr.args(), null, cc);
      based = true;
      // innermost result: checked by the original function, as it may be empty
      return declType == null || declType.occ.min == 0 ? expr :
        TypeCheck.get(info, expr, declType, null, cc);
    }
  }

  /**
   * Rewrites a recursive body with a parameter that accumulates the results:
   * {@code if(C) then $acc else f($n + 1, ($acc, X))}.
   */
  private static final class Accumulating extends RecursiveUnfolding {
    /** Position of the accumulator in the parameter list. */
    private final int pos;

    /**
     * Constructor.
     * @param func recursive function
     * @param vars state variables
     * @param acc accumulator
     * @param pos position of the accumulator in the parameter list
     * @param cc compilation context
     */
    Accumulating(final StaticFunc func, final Var[] vars, final Var acc, final int pos,
        final CompileContext cc) {
      super(func, vars, acc, supported(acc.seqType()), cc);
      this.pos = pos;
    }

    /**
     * Returns the operations supported for an accumulator.
     * @param type type of the accumulator
     * @return operations
     */
    private static EnumSet<Op> supported(final SeqType type) {
      // exact numbers only (empty sequences, rounding)
      final EnumSet<Op> supported = EnumSet.of(Op.LIST, Op.CONCAT);
      if(type.instanceOf(Types.DECIMAL_O)) Collections.addAll(supported, Op.ADD, Op.MULTIPLY);
      if(type.instanceOf(Types.MAP_O)) Collections.addAll(supported, Op.PUT, Op.REMOVE);
      return supported;
    }

    @Override
    boolean safe(final Expr step, final Expr body) {
      // concatenations that return the accumulator unchanged do not convert it to a string
      return (op() != Op.CONCAT || !unchanged() || acc.seqType().instanceOf(Types.STRING_O)) &&
          !step.uses(acc);
    }

    @Override
    protected Expr replace(final Expr[] ops, final int hole, final Op o, final CompileContext cc)
        throws QueryException {
      based = true;
      return super.replace(ops, hole, o, cc);
    }

    @Override
    public Expr base(final Expr expr, final CompileContext cc) throws QueryException {
      if(!call(expr)) {
        based = true;
        return super.base(expr, cc);
      }
      // tail call: the accumulated values precede or follow the call
      final Expr[] args = expr.args();
      final Expr arg = args[pos];
      Expr prefix = Empty.VALUE, sfx = null;
      if(!hole(arg)) {
        // nested rewrite: recursive calls in the argument are no accumulating expressions
        final Expr value = arg.unfold(new Unfolding(this), cc);
        final Op op = op();
        if(op != null && op.ordered() && !first()) sfx = value;
        else prefix = value;
      }
      return recur(prefix, Array.remove(args, pos), sfx, cc);
    }
  }
}
