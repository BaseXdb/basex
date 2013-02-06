package org.basex.query.func;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function call for user-defined functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class UserFuncCall extends Arr {
  /** Function name. */
  final QNm name;
  /** Function reference. */
  UserFunc func;

  /**
   * Function constructor.
   * @param ii input info
   * @param nm function name
   * @param arg arguments
   */
  UserFuncCall(final InputInfo ii, final QNm nm, final Expr[] arg) {
    super(ii, arg);
    name = nm;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);

    // inline if result and arguments are all values.
    // currently, only functions with values as return expressions are inlined
    // otherwise, recursive functions might not be correctly evaluated
    func.compile(ctx, scp);
    if(func.expr.isValue() && allAreValues() && !func.uses(Use.NDT)) {
      // evaluate arguments to catch cast exceptions
      final Value[] sf = func.scope.enter(ctx);
      try {
        for(int a = 0; a < expr.length; ++a)
          ctx.set(func.args[a], (Value) expr[a], info);
      } finally {
        func.scope.exit(ctx, sf);
      }
      ctx.compInfo(OPTINLINE, func.name.string());
      return func.value(ctx);
    }
    type = func.type();
    return this;
  }

  @Override
  public final BaseFuncCall copy(final QueryContext ctx, final VarScope scp,
      final IntMap<Var> vs) {
    final Expr[] arg = new Expr[expr.length];
    for(int i = 0; i < arg.length; i++) arg[i] = expr[i].copy(ctx, scp, vs);
    final BaseFuncCall call = new BaseFuncCall(info, name, arg);
    call.func = func;
    call.type = type;
    call.size = size;
    return call;
  }

  /**
   * Adds the given arguments to the variable stack.
   * @param ctx query context
   * @param ii input info
   * @param scp variable scope
   * @param vars formal parameters
   * @param vals values to add
   * @return old stack frame
   * @throws QueryException if the arguments can't be bound
   */
  static Value[] addArgs(final QueryContext ctx, final InputInfo ii, final VarScope scp,
      final Var[] vars, final Value[] vals) throws QueryException {
    // move variables to stack
    final Value[] old = scp.enter(ctx);
    for(int i = 0; i < vars.length; i++) ctx.set(vars[i], vals[i], ii);
    return old;
  }

  /**
   * Evaluates all function arguments.
   * @param ctx query context
   * @return argument values
   * @throws QueryException query exception
   */
  Value[] args(final QueryContext ctx) throws QueryException {
    final int al = expr.length;
    final Value[] args = new Value[al];
    // evaluate arguments
    for(int a = 0; a < al; ++a) args[a] = expr[a].value(ctx);
    return args;
  }

  /**
   * Initializes the function call after all functions have been declared.
   * @param f function reference
   */
  public void init(final UserFunc f) {
    func = f;
  }

  /**
   * Getter for the called function.
   * @return user-defined function
   */
  final UserFunc func() {
    return func;
  }

  @Override
  public final boolean isVacuous() {
    return func != null && func.isVacuous();
  }

  @Override
  public boolean uses(final Use u) {
    // check arguments, which will be evaluated before running the function code
    if(super.uses(u)) return true;
    // function code: position or context references will have no effect on calling code
    if(u == Use.POS || u == Use.CTX) return false;
    // pass on check to function code
    return func == null || (u == Use.UPD ? func.updating : func.uses(u));
  }

  @Override
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, this), expr);
  }

  @Override
  public String description() {
    return FUNC;
  }

  @Override
  public String toString() {
    return new TokenBuilder(name.string()).add(PAR1).add(
        toString(SEP)).add(PAR2).toString();
  }

  /**
   * A continuation that's thrown to free stack frames.
   * @author Leo Woerteler
   */
  final class Continuation extends RuntimeException {
    /** Arguments. */
    private final Value[] args;

    /**
     * Constructor.
     * @param arg arguments
     */
    Continuation(final Value[] arg) {
      args = arg;
    }

    /**
     * Getter for the continuation function.
     * @return the next function to call
     */
    UserFunc getFunc() {
      return func;
    }

    /**
     * Getter for the function arguments.
     * @return the next function call's arguments
     */
    Value[] getArgs() {
      return args;
    }

    @Override
    public synchronized Continuation fillInStackTrace() {
      // ignore this for efficiency reasons
      return this;
    }
  }
}
