package org.basex.query.value.item;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.Map.Entry;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
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
  /** Static context. */
  private final StaticContext sc;
  /** Variables. */
  private final Var[] vars;
  /** Function expression. */
  private final Expr expr;
  /** Function name. */
  private final QNm name;
  /** Optional type to cast to. */
  private final SeqType cast;

  /** Context value. */
  private final Value ctxVal;
  /** Context position. */
  private final long pos;
  /** Context length. */
  private final long size;

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
   * @param scp variable scope
   * @param sctx static context
   */
  public FuncItem(final QNm n, final Var[] arg, final Expr body, final FuncType t,
      final VarScope scp, final StaticContext sctx) {
    this(n, arg, body, t, false, null, 0, 0, null, scp, sctx);
  }

  /**
   * Constructor.
   * @param n function name
   * @param arg function arguments
   * @param body function body
   * @param t function type
   * @param cst cast flag
   * @param cls closure
   * @param scp variable scope
   * @param sctx static context
   */
  public FuncItem(final QNm n, final Var[] arg, final Expr body, final FuncType t,
      final boolean cst, final Map<Var, Value> cls, final VarScope scp,
      final StaticContext sctx) {
    this(n, arg, body, t, cst, null, 0, 0, cls, scp, sctx);
  }

  /**
   * Constructor for anonymous functions.
   * @param arg function arguments
   * @param body function body
   * @param t function type
   * @param cl variables in the closure
   * @param cst cast flag
   * @param scp variable scope
   * @param sctx static context
   */
  public FuncItem(final Var[] arg, final Expr body, final FuncType t,
      final Map<Var, Value> cl, final boolean cst, final VarScope scp,
      final StaticContext sctx) {
    this(null, arg, body, t, cst, cl, scp, sctx);
  }

  /**
   * Constructor.
   * @param n function name
   * @param arg function arguments
   * @param body function body
   * @param t function type
   * @param cst cast flag
   * @param vl context value
   * @param ps context position
   * @param sz context size
   * @param cls closure
   * @param scp variable scope
   * @param sctx static context
   */
  public FuncItem(final QNm n, final Var[] arg, final Expr body, final FuncType t,
      final boolean cst, final Value vl, final long ps, final long sz,
      final Map<Var, Value> cls, final VarScope scp, final StaticContext sctx) {
    super(t);
    name = n;
    vars = arg;
    expr = body;
    cast = cst && t.ret != null ? t.ret : null;
    closure = cls != null ? cls : Collections.<Var, Value>emptyMap();
    stackSize = scp.stackSize();
    sc = sctx;

    ctxVal = vl;
    pos = ps;
    size = sz;
  }

  @Override
  public int arity() {
    return vars.length;
  }

  @Override
  public QNm fName() {
    return name;
  }

  @Override
  public FuncType funcType() {
    return (FuncType) type;
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
  public Value invValue(final QueryContext ctx, final InputInfo ii, final Value... args)
      throws QueryException {

    // bind variables and cache context
    final StaticContext cs = ctx.sc;
    ctx.sc = sc;
    final int fp = ctx.stack.enterFrame(stackSize);
    final Value cv = ctx.value;
    final long ps = ctx.pos, sz = ctx.size;
    try {
      bindVars(ctx, ii, args);
      ctx.value = ctxVal;
      ctx.pos = pos;
      ctx.size = size;
      final Value v = ctx.value(expr);
      // optionally cast return value to target type
      return cast != null ? cast.funcConvert(ctx, ii, v) : v;
    } finally {
      ctx.value = cv;
      ctx.pos = ps;
      ctx.size = sz;
      ctx.stack.exitFrame(fp);
      ctx.sc = cs;
    }
  }

  @Override
  public Item invItem(final QueryContext ctx, final InputInfo ii, final Value... args)
      throws QueryException {

    // bind variables and cache context
    final StaticContext cs = ctx.sc;
    ctx.sc = sc;
    final int fp = ctx.stack.enterFrame(stackSize);
    final Value cv = ctx.value;
    final long ps = ctx.pos, sz = ctx.size;
    try {
      bindVars(ctx, ii, args);
      ctx.value = ctxVal;
      ctx.pos = pos;
      ctx.size = size;
      final Item it = expr.item(ctx, ii);
      final Value v = it == null ? Empty.SEQ : it;
      // optionally cast return value to target type
      return cast != null ? cast.funcConvert(ctx, ii, v).item(ctx, ii) : it;
    } finally {
      ctx.value = cv;
      ctx.pos = ps;
      ctx.size = sz;
      ctx.stack.exitFrame(fp);
      ctx.sc = cs;
    }
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
        fun.cast != null, null, sc, ctx.sc);
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

  @Override
  public String toString() {
    final FuncType ft = (FuncType) type;
    final TokenBuilder tb = new TokenBuilder(FUNCTION).add('(');
    for(final Var v : vars) tb.addExt(v).add(v == vars[vars.length - 1] ? "" : ", ");
    return tb.add(')').add(ft.ret != null ? " as " + ft.ret : "").add(" { ").
        addExt(expr).add(" }").toString();
  }
}
