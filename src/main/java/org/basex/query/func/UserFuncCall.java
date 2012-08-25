package org.basex.query.func;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

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
  public Expr compile(final QueryContext ctx) throws QueryException {
    super.compile(ctx);

    // inline if result and arguments are all values.
    // currently, only functions with values as return expressions are inlined
    // otherwise, recursive functions might not be correctly evaluated
    func.compile(ctx);
    if(func.expr.isValue() && allAreValues() && !func.uses(Use.NDT)) {
      // evaluate arguments to catch cast exceptions
      for(int a = 0; a < expr.length; ++a) func.args[a].bind(expr[a], ctx);
      ctx.compInfo(OPTINLINE, func.name.string());
      return func.value(ctx);
    }
    type = func.type();
    return this;
  }

  /**
   * Adds the given arguments to the variable stack.
   * @param ctx query context
   * @param vs variables to add
   * @return old stack size
   */
  static VarStack addArgs(final QueryContext ctx, final Var[] vs) {
    // move variables to stack
    final VarStack vl = ctx.vars.cache(vs.length);
    for(final Var v : vs) ctx.vars.add(v);
    return vl;
  }

  /**
   * Evaluates all function arguments.
   * @param ctx query context
   * @return argument values
   * @throws QueryException query exception
   */
  Var[] args(final QueryContext ctx) throws QueryException {
    final int al = expr.length;
    final Var[] args = new Var[al];
    // evaluate arguments
    for(int a = 0; a < al; ++a)
      args[a] = func.args[a].bind(expr[a].value(ctx), ctx).copy();
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
  public boolean uses(final Use u) {
    return u == Use.UPD ? func.updating : func.uses(u) || super.uses(u);
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
    private final Var[] args;

    /**
     * Constructor.
     * @param arg arguments
     */
    Continuation(final Var[] arg) {
      args = arg;
    }

    /**
     * Getter for the continuation function.
     * @return the next function to call
     */
    Expr getFunc() {
      return func;
    }

    /**
     * Getter for the function arguments.
     * @return the next function call's arguments
     */
    Var[] getArgs() {
      return args;
    }

    @Override
    public synchronized Continuation fillInStackTrace() {
      // ignore this for efficiency reasons
      return this;
    }
  }
}
