package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.QNm;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Function call for user-defined functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class UserFuncCall extends Arr {
  /**
   * A continuation that's thrown to free stack frames.
   * @author Leo Woerteler
   */
  public final class Continuation extends RuntimeException {
    /** Arguments. */
    private final Var[] args;

    /**
     * Constructor.
     * @param arg arguments
     */
    public Continuation(final Var[] arg) {
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
  public UserFuncCall(final InputInfo ii, final QNm nm, final Expr... arg) {
    super(ii, arg);
    name = nm;
  }

  /**
   * Initializes the function call after all functions have been declared.
   * @param f function reference
   */
  public void init(final UserFunc f) {
    func = f;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    // compile all arguments
    super.comp(ctx);

    // inline if result and arguments are all values.
    // currently, only functions with values as
    // return expressions are supported; otherwise, recursive functions
    // might not be correctly evaluated
    func.comp(ctx);
    if(func.expr.value() && values() && !func.uses(Use.NDT)) {
      // evaluate arguments to catch cast exceptions
      for(int a = 0; a < expr.length; ++a) func.args[a].bind(expr[a], ctx);
      ctx.compInfo(OPTINLINE, func.name.string());
      return func.value(ctx);
    }
    // user-defined functions are not pre-evaluated to avoid various issues
    // with recursive functions
    type = func.type();
    return this;
  }

  /**
   * Adds the given arguments to the variable stack.
   * @param ctx query context
   * @param vs variables to add
   * @return old stack size
   */
  int addArgs(final QueryContext ctx, final Var[] vs) {
    // move variables to stack
    final int s = ctx.vars.size();
    for(final Var v : vs) ctx.vars.add(v);
    return s;
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

  @Override
  public boolean uses(final Use u) {
    return u == Use.UPD ? func.updating : super.uses(u);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, NAM, Token.token(toString()));
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String desc() {
    return FUNC;
  }

  @Override
  public String toString() {
    return new TokenBuilder(name.string()).add(PAR1).add(
        toString(SEP)).add(PAR2).toString();
  }
}
