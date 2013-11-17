package org.basex.query.value.item;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.gflwor.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function item.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public final class FuncItem extends FItem implements Scope {
  /** Original function. */
  public final StaticFunc func;

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
   * @param sf original function
   */
  public FuncItem(final QNm n, final Var[] arg, final Expr body, final FuncType t,
      final VarScope scp, final StaticContext sctx, final StaticFunc sf) {
    this(n, arg, body, t, false, null, 0, 0, null, scp, sctx, sf);
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
   * @param sf original function
   */
  public FuncItem(final QNm n, final Var[] arg, final Expr body, final FuncType t,
      final boolean cst, final Map<Var, Value> cls, final VarScope scp,
      final StaticContext sctx, final StaticFunc sf) {
    this(n, arg, body, t, cst, null, 0, 0, cls, scp, sctx, sf);
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
    this(null, arg, body, t, cst, cl, scp, sctx, null);
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
   * @param sf original function
   */
  public FuncItem(final QNm n, final Var[] arg, final Expr body, final FuncType t,
      final boolean cst, final Value vl, final long ps, final long sz,
      final Map<Var, Value> cls, final VarScope scp, final StaticContext sctx,
      final StaticFunc sf) {

    super(t);
    name = n;
    vars = arg;
    expr = body;
    cast = cst && t.type != null ? t.type : null;
    closure = cls != null ? cls : Collections.<Var, Value>emptyMap();
    stackSize = scp.stackSize();
    sc = sctx;

    ctxVal = vl;
    pos = ps;
    size = sz;
    func = sf;
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
  public Value invValue(final QueryContext ctx, final InputInfo ii,
      final Value... args) throws QueryException {

    // bind variables and cache context
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
      return cast != null ? cast.funcConvert(ctx, sc, ii, v) : v;
    } finally {
      ctx.value = cv;
      ctx.pos = ps;
      ctx.size = sz;
      ctx.stack.exitFrame(fp);
    }
  }

  @Override
  public Item invItem(final QueryContext ctx, final InputInfo ii,
      final Value... args) throws QueryException {

    // bind variables and cache context
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
      return cast != null ? cast.funcConvert(ctx, sc, ii, v).item(ctx, ii) : it;
    } finally {
      ctx.value = cv;
      ctx.pos = ps;
      ctx.size = sz;
      ctx.stack.exitFrame(fp);
    }
  }

  /**
   * Coerces a function item to the given type.
   * @param ctx query context
   * @param sc static context
   * @param ii input info
   * @param fun function item to coerce
   * @param t type to coerce to
   * @return coerced function item
   */
  private static FuncItem coerce(final QueryContext ctx, final StaticContext sc,
      final InputInfo ii, final FuncItem fun, final FuncType t) {
    final VarScope vsc = new VarScope(sc);
    final Var[] vs = new Var[fun.vars.length];
    final Expr[] refs = new Expr[vs.length];
    for(int i = vs.length; i-- > 0;) {
      vs[i] = vsc.uniqueVar(ctx, t.args[i], true);
      refs[i] = new VarRef(ii, vs[i]);
    }
    final Expr e = new DynFuncCall(ii, fun, refs);
    e.markTailCalls();
    return new FuncItem(fun.name, vs, e, t, fun.cast != null, null, vsc, sc, fun.func);
  }

  @Override
  public FItem coerceTo(final FuncType ft, final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    if(vars.length != ft.args.length) throw Err.castError(ii, ft, this);
    return type.instanceOf(ft) ? this : coerce(ctx, sc, ii, this, ft);
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
    for(final Entry<Var, Value> e : closure.entrySet())
      if(!(visitor.declared(e.getKey()) && e.getValue().accept(visitor))) return false;
    return expr.accept(visitor);
  }

  @Override
  public void compile(final QueryContext ctx) {
    // nothing to do here
  }

  @Override
  public boolean compiled() {
    return true;
  }

  @Override
  public Object toJava() {
    throw Util.notExpected();
  }

  @Override
  public String toString() {
    final FuncType ft = (FuncType) type;
    final TokenBuilder tb = new TokenBuilder(FUNCTION).add('(');
    for(final Var v : vars) tb.addExt(v).add(v == vars[vars.length - 1] ? "" : ", ");
    return tb.add(')').add(ft.type != null ? " as " + ft.type : "").add(" { ").
        addExt(expr).add(" }").toString();
  }

  @Override
  public Expr inlineExpr(final Expr[] exprs, final QueryContext ctx, final VarScope scp,
      final InputInfo ii)
      throws QueryException {
    if(!inline(exprs, ctx)) return null;
    // create let bindings for all variables
    final LinkedList<GFLWOR.Clause> cls = exprs.length == 0 ? null :
      new LinkedList<GFLWOR.Clause>();
    final IntObjMap<Var> vs = new IntObjMap<Var>();
    for(int i = 0; i < vars.length; i++) {
      final Var old = vars[i], v = scp.newCopyOf(ctx, old);
      vs.put(old.id, v);
      cls.add(new Let(v, exprs[i], false, ii).optimize(ctx, scp));
    }

    for(final Entry<Var, Value> e : closure.entrySet()) {
      final Var old = e.getKey(), v = scp.newCopyOf(ctx, old);
      vs.put(old.id, v);
      cls.add(new Let(v, e.getValue(), false, ii).optimize(ctx, scp));
    }

    // copy the function body
    final Expr cpy = expr.copy(ctx, scp, vs), rt = cast == null ? cpy :
      new TypeCheck(sc, ii, cpy, cast, true).optimize(ctx, scp);

    return cls == null ? rt : new GFLWOR(ii, cls, rt).optimize(ctx, scp);
  }

  /**
   * Checks if this function item should be inlined.
   * @param as argument expressions
   * @param ctx query context
   * @return result of check
   */
  private boolean inline(final Expr[] as, final QueryContext ctx) {
    if(expr.isValue() || expr.exprSize() < ctx.context.options.get(MainOptions.INLINELIMIT) &&
        !(expr.has(Flag.NDT) || expr.has(Flag.CTX))) {
      final ASTVisitor visitor = new ASTVisitor() {
        @Override
        public boolean funcItem(final FuncItem f) {
          return f != FuncItem.this && f.visit(this);
        }
        @Override
        public boolean inlineFunc(final Scope sub) {
          return sub.visit(this);
        }
      };
      return Expr.visitAll(visitor, as);
    }
    return false;
  }
}
