package org.basex.query.expr;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Recursion step of an unfolded function body: returns the results preceding the recursive call
 * and binds the next state.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Recur extends Arr {
  /** State variables. */
  private final Var[] vars;
  /** Variable indicating that a recursion step was taken. */
  private final Var flag;
  /** Variable for the results that follow the recursive call (can be {@code null}). */
  private final Var suffix;
  /** Reference to the flag: the step depends on the enclosing iteration. */
  private final VarRef flagRef;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs results preceding the call, arguments of the call, results following the call
   *   (only if a suffix variable is supplied)
   * @param vars state variables
   * @param flag variable indicating that a recursion step was taken
   * @param suffix variable for the results that follow the recursive call (can be {@code null})
   */
  Recur(final InputInfo info, final Expr[] exprs, final Var[] vars, final Var flag,
      final Var suffix) {
    super(info, exprs[0].seqType(), exprs);
    this.vars = vars;
    this.flag = flag;
    this.suffix = suffix;
    flagRef = new VarRef(info, flag);
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    return adoptType(exprs[0]);
  }

  /**
   * Returns the results that follow the recursive call.
   * @return results (can be {@code null})
   */
  public Expr after() {
    return suffix != null ? exprs[exprs.length - 1] : null;
  }

  /**
   * Moves an enclosing expression into the preceding results of this step.
   * @param bound checks if an argument or the following results depend on the enclosing expression
   * @param wrap wraps the preceding results with the enclosing expression
   * @param cc compilation context
   * @return new step or {@code null}
   * @throws QueryException query exception
   */
  public Expr absorb(final Predicate<Expr> bound, final QueryFunction<Expr, Expr> wrap,
      final CompileContext cc) throws QueryException {
    final int el = exprs.length;
    for(int e = 1; e < el; e++) {
      if(bound.test(exprs[e])) return null;
    }
    final Expr[] ops = exprs.clone();
    ops[0] = wrap.apply(ops[0]);
    // let $x := E return recur(P, A) → recur(let $x := E return P, A)
    return new Recur(info, ops, vars, flag, suffix).optimize(cc);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // evaluate everything before the next state is bound
    final Value value = exprs[0].value(qc);
    if(suffix != null) qc.set(suffix, exprs[vars.length + 1].value(qc));
    Unfold.bind(vars, exprs, 1, qc);
    qc.set(flag, Bln.TRUE);
    return value;
  }

  @Override
  public boolean eager() {
    return true;
  }

  @Override
  public Recur copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    final int vl = vars.length;
    final Var[] vrs = new Var[vl];
    for(int v = 0; v < vl; v++) vrs[v] = var(vars[v], vm);
    return copyType(new Recur(info, copyAll(cc, vm, exprs), vrs, var(flag, vm),
        suffix != null ? var(suffix, vm) : null));
  }

  /**
   * Returns the copy of a variable.
   * @param var variable
   * @param vm variable mapping
   * @return copied or original variable
   */
  private static Var var(final Var var, final IntObjectMap<Var> vm) {
    final Var vr = vm.get(var.id);
    return vr != null ? vr : var;
  }

  @Override
  public VarUsage count(final Var var) {
    return super.count(var).plus(flagRef.count(var));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && visitor.used(flagRef);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final Recur recur && Arrays.equals(vars, recur.vars) &&
        flag.equals(recur.flag) && Objects.equals(suffix, recur.suffix) && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    final int vl = vars.length;
    final boolean prefix = exprs[0] != Empty.VALUE, list = prefix || suffix != null;
    if(list) qs.token("(");
    if(prefix) qs.token(exprs[0]).token(',');
    call(qs, info, flag, Arrays.copyOfRange(exprs, 1, vl + 1));
    if(suffix != null) qs.token(',').token(exprs[vl + 1]);
    if(list) qs.token(')');
  }

  /**
   * Adds a self-application of the recursive function to a query string.
   * @param qs query string
   * @param info input info (can be {@code null})
   * @param flag variable that represents the recursive function
   * @param args arguments
   */
  static void call(final QueryString qs, final InputInfo info, final Var flag,
      final Expr[] args) {
    final VarRef ref = new VarRef(info, flag);
    qs.token(ref).params(ExprList.concat(new Expr[] { ref }, args));
  }
}
