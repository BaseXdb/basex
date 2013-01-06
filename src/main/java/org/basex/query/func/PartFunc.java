package org.basex.query.func;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.InputInfo;
import org.basex.util.list.*;

/**
 * Partial function application.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class PartFunc extends InlineFunc {
  /**
   * Function constructor for static calls.
   * @param nm function name
   * @param ii input info
   * @param fun typed function expression
   * @param env environment
   * @param stc static context
   * @throws QueryException exception
   */
  public PartFunc(final QNm nm, final InputInfo ii, final TypedFunc fun, final Env env,
      final StaticContext stc)
      throws QueryException {
    super(ii, nm, fun.ret(), args(env, fun.type, ii), fun.fun, fun.ann, stc, env.scope);
  }

  /**
   * Function constructor for dynamic calls.
   * @param ii input info
   * @param func function expression
   * @param env environment
   * @param stc static context
   * @throws QueryException query exception
   */
  public PartFunc(final InputInfo ii, final Expr func, final Env env,
      final StaticContext stc) throws QueryException {
    // [LW] XQuery/HOF: dynamic type propagation, annotations
    super(ii, new QNm(), func.type(), args(env, null, ii), func,
        new Ann(), stc, env.scope);
  }

  /**
   * Gathers this partial function application's arguments and sets the types.
   * @param env variables to type
   * @param ft function type
   * @param ii input info
   * @return the variables for convenience
   * @throws QueryException exception
   */
  public static Var[] args(final Env env, final FuncType ft, final InputInfo ii)
      throws QueryException {
    final Var[] args = env.args.toArray(new Var[env.args.size()]);
    if(ft != null && ft != FuncType.ANY_FUN) {
      for(int i = 0; i < args.length; i++) {
        final int pos = env.poss.get(i);
        if(ft.args[pos] !=  null && ft.args[pos] != SeqType.ITEM_ZM)
          args[i].setRetType(ft.args[pos], ii);
      }
    }
    return args;
  }

  /**
   * Environment of a partial function application.
   *
   * @author BaseX Team 2005-12, BSD License
   * @author Leo Woerteler
   */
  public static final class Env {
    /** Position of the arguments. */
    final IntList poss = new IntList();
    /** Argument variables. */
    final ArrayList<Var> args = new ArrayList<Var>();
    /** Variable scope. */
    public final VarScope scope;

    /**
     * Constructor.
     * @param scp variable scope
     */
    public Env(final VarScope scp) {
      scope = scp;
    }

    /**
     * Adds a new argument to this environment.
     * @param pos argument position
     * @param var variable
     */
    public void add(final int pos, final Var var) {
      poss.add(pos);
      args.add(var);
    }
  }
}