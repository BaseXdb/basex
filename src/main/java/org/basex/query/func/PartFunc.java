package org.basex.query.func;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.InputInfo;
import org.basex.util.hash.*;
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
   */
  public PartFunc(final QNm nm, final InputInfo ii, final TypedFunc fun, final Env env,
      final StaticContext stc) {
    super(ii, nm, fun.ret(), args(env, fun.type), fun.fun, fun.ann, stc, env.scope);
  }

  /**
   * Function constructor for dynamic calls.
   * @param ii input info
   * @param func function expression
   * @param env environment
   * @param stc static context
   */
  public PartFunc(final InputInfo ii, final Expr func, final Env env,
      final StaticContext stc) {
    // [LW] XQuery/HOF: dynamic type propagation, annotations
    super(ii, new QNm(), func.type(), args(env, null), func,
        new Ann(), stc, env.scope);
  }

  /**
   * Copy constructor.
   * @param ii input info
   * @param nm name
   * @param r return type
   * @param a argument variables
   * @param f function body
   * @param an annotations
   * @param s static context
   * @param scp variable scope
   */
  private PartFunc(final InputInfo ii, final QNm nm, final SeqType r, final Var[] a,
      final Expr f, final Ann an, final StaticContext s, final VarScope scp) {
    super(ii, nm, r, a, f, an, s, scp);
  }

  /**
   * Gathers this partial function application's arguments and sets the types.
   * @param env variables to type
   * @param ft function type
   * @return the variables for convenience
   */
  public static Var[] args(final Env env, final FuncType ft) {
    final Var[] args = env.args.toArray(new Var[env.args.size()]);
    if(ft != null && ft != FuncType.ANY_FUN) {
      for(int i = 0; i < args.length; i++) {
        final int pos = env.poss.get(i);
        if(ft.args[pos] !=  null && ft.args[pos] != SeqType.ITEM_ZM)
          args[i].setDeclaredType(ft.args[pos]);
      }
    }
    return args;
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    final VarScope v = scope.copy(ctx, scp, vs);
    final Var[] a = args.clone();
    for(int i = 0; i < a.length; i++) a[i] = vs.get(a[i].id);
    return copyType(new PartFunc(info, name, ret, a, expr.copy(ctx, v, vs), ann, sc, v));
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