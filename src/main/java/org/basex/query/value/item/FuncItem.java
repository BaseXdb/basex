package org.basex.query.value.item;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.Map.Entry;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function item.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class FuncItem extends FItem implements Scope {
  /** Variables. */
  private final Var[] vars;
  /** Function expression. */
  private final Expr expr;
  /** Function name. */
  private final QNm name;
  /** Optional type to cast to. */
  private final SeqType cast;

  /** The closure of this function item. */
  private final Map<Var, Value> closure;
  /** Size of the stack frame needed for this function. */
  private final int stackSize;

  /**
   * Constructor.
   * @param n function name
   * @param arg function arguments
   * @param body function body
   * @param t function type
   * @param cst cast flag
   * @param cls closure
   * @param scp variable scope
   */
  public FuncItem(final QNm n, final Var[] arg, final Expr body, final FuncType t,
      final boolean cst, final Map<Var, Value> cls, final VarScope scp) {
    super(t);
    name = n;
    vars = arg;
    expr = body;
    cast = cst && t.ret != null ? t.ret : null;
    closure = cls != null ? cls : Collections.<Var, Value>emptyMap();
    stackSize = scp.stackSize();
  }

  /**
   * Constructor for anonymous functions.
   * @param arg function arguments
   * @param body function body
   * @param t function type
   * @param cl variables in the closure
   * @param cst cast flag
   * @param scp variable scope
   */
  public FuncItem(final Var[] arg, final Expr body, final FuncType t,
      final Map<Var, Value> cl, final boolean cst, final VarScope scp) {
    this(null, arg, body, t, cst, cl, scp);
  }

  @Override
  public int arity() {
    return vars.length;
  }

  @Override
  public QNm fName() {
    return name;
  }

  /**
   * Binds all variables to the context.
   * @param ctx query context
   * @param ii input info
   * @param arg argument values
   * @throws QueryException if the arguments can't be bound
   */
  private void bindVars(final QueryContext ctx, final InputInfo ii, final Value[] arg)
      throws QueryException {
    for(final Entry<Var, Value> e : closure.entrySet())
      ctx.set(e.getKey(), e.getValue(), ii);
    for(int v = vars.length; --v >= 0;) ctx.set(vars[v], arg[v], ii);
  }

  @Override
  public Value invValue(final QueryContext ctx, final InputInfo ii,
      final Value... args) throws QueryException {

    // bind variables and cache context
    final int fp = ctx.stack.enterFrame(stackSize);
    final Value cv = ctx.value;
    try {
      bindVars(ctx, ii, args);
      ctx.value = null;
      final Value v = ctx.value(expr);
      // optionally cast return value to target type
      return cast != null ? cast.funcConvert(ctx, ii, v) : v;
    } finally {
      ctx.value = cv;
      ctx.stack.exitFrame(fp);
    }
  }

  @Override
  public Iter invIter(final QueryContext ctx, final InputInfo ii,
      final Value... args) throws QueryException {
    return invValue(ctx, ii, args).iter();
  }

  @Override
  public Item invItem(final QueryContext ctx, final InputInfo ii,
      final Value... args) throws QueryException {

    // bind variables and cache context
    final int fp = ctx.stack.enterFrame(stackSize);
    final Value cv = ctx.value;
    try {
      bindVars(ctx, ii, args);
      ctx.value = null;
      final Item it = expr.item(ctx, ii);
      final Value v = it == null ? Empty.SEQ : it;
      // optionally cast return value to target type
      return cast != null ? cast.funcConvert(ctx, ii, v).item(ctx, ii) : it;
    } finally {
      ctx.value = cv;
      ctx.stack.exitFrame(fp);
    }
  }

  @Override
  public String toString() {
    final FuncType ft = (FuncType) type;
    final StringBuilder sb = new StringBuilder(FUNCTION).append('(');
    for(final Var v : vars)
      sb.append(v).append(v == vars[vars.length - 1] ? "" : ", ");
    return sb.append(')').append(ft.ret != null ? " as " + ft.ret :
      "").append(" { ").append(expr).append(" }").toString();
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 || expr.uses(u);
  }

  /**
   * Coerces a function item to the given type.
   * @param ctx query context
   * @param ii input info
   * @param fun function item to coerce
   * @param t type to coerce to
   * @return coerced function item
   */
  private static FuncItem coerce(final QueryContext ctx, final InputInfo ii,
      final FuncItem fun, final FuncType t) {
    final VarScope sc = new VarScope();
    final Var[] vars = new Var[fun.vars.length];
    final Expr[] refs = new Expr[vars.length];
    for(int i = vars.length; i-- > 0;) {
      vars[i] = sc.uniqueVar(ctx, t.args[i], true);
      refs[i] = new VarRef(ii, vars[i]);
    }
    return new FuncItem(fun.name, vars, new DynFuncCall(ii, fun, refs), t,
        fun.cast != null, null, sc);
  }

  @Override
  public FItem coerceTo(final FuncType ft, final QueryContext ctx,
      final InputInfo ii) throws QueryException {

    if(vars.length != ft.args.length) throw Err.cast(ii, ft, this);
    return type.instanceOf(ft) ? this : coerce(ctx, ii, this, ft);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(TYPE, type), vars, expr);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.funcItem(this);
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    for(final Var var : vars) if(!visitor.declared(var)) return false;
    for(final Var var : closure.keySet()) if(!visitor.declared(var)) return false;
    return expr.accept(visitor);
  }

  @Override
  public void compile(final QueryContext ctx) throws QueryException {
    // nothing to do here
  }

  @Override
  public boolean compiled() {
    return true;
  }
}
