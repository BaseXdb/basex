package org.basex.query.value.item;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.gflwor.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function item.
 *
 * @author BaseX Team 2005-14, BSD License
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

  /** Context value. */
  private final Value ctxVal;
  /** Context position. */
  private final long pos;
  /** Context length. */
  private final long size;

  /** Size of the stack frame needed for this function. */
  private final int stackSize;

  /**
   * Constructor.
   * @param sctx static context
   * @param annotations function annotations
   * @param n function name
   * @param arg function arguments
   * @param t function type
   * @param body function body
   * @param stSize stack-frame size
   */
  public FuncItem(final StaticContext sctx, final Ann annotations, final QNm n, final Var[] arg,
      final FuncType t, final Expr body, final int stSize) {
    this(sctx, annotations, n, arg, t, body, null, 0, 0, stSize);
  }

  /**
   * Constructor.
   * @param sctx static context
   * @param annotations function annotations
   * @param n function name
   * @param arg function arguments
   * @param t function type
   * @param body function body
   * @param vl context value
   * @param ps context position
   * @param sz context size
   * @param stSize stack-frame size
   */
  public FuncItem(final StaticContext sctx, final Ann annotations, final QNm n, final Var[] arg,
      final FuncType t, final Expr body, final Value vl,
      final long ps, final long sz, final int stSize) {

    super(t, annotations);
    name = n;
    vars = arg;
    expr = body;
    stackSize = stSize;
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
  public QNm funcName() {
    return name;
  }

  @Override
  public QNm argName(final int ps) {
    return vars[ps].name;
  }

  @Override
  public FuncType funcType() {
    return (FuncType) type;
  }

  @Override
  public int stackFrameSize() {
    return stackSize;
  }

  @Override
  public Value invValue(final QueryContext ctx, final InputInfo ii, final Value... args)
      throws QueryException {
    // bind variables and cache context
    final Value cv = ctx.value;
    final long ps = ctx.pos, sz = ctx.size;
    try {
      ctx.value = ctxVal;
      ctx.pos = pos;
      ctx.size = size;
      for(int i = 0; i < vars.length; i++) ctx.set(vars[i], args[i], ii);
      return ctx.value(expr);
    } finally {
      ctx.value = cv;
      ctx.pos = ps;
      ctx.size = sz;
    }
  }

  @Override
  public Item invItem(final QueryContext ctx, final InputInfo ii, final Value... args)
      throws QueryException {
    // bind variables and cache context
    final Value cv = ctx.value;
    final long ps = ctx.pos, sz = ctx.size;
    try {
      ctx.value = ctxVal;
      ctx.pos = pos;
      ctx.size = size;
      for(int i = 0; i < vars.length; i++) ctx.set(vars[i], args[i], ii);
      return expr.item(ctx, ii);
    } finally {
      ctx.value = cv;
      ctx.pos = ps;
      ctx.size = sz;
    }
  }

  @Override
  public FItem coerceTo(final FuncType ft, final QueryContext ctx, final InputInfo ii,
      final boolean opt) throws QueryException {
    if(vars.length != ft.args.length) throw Err.castError(ii, ft, this);
    final FuncType tp = funcType();
    if(tp.instanceOf(ft)) return this;

    final VarScope vsc = new VarScope(sc);
    final Var[] vs = new Var[vars.length];
    final Expr[] refs = new Expr[vs.length];
    for(int i = vs.length; i-- > 0;) {
      vs[i] = vsc.newLocal(ctx, vars[i].name, ft.args[i], true);
      refs[i] = new VarRef(ii, vs[i]);
    }

    final Expr e = new DynFuncCall(ii, this, refs),
        optimized = opt ? e.optimize(ctx, vsc) : e, checked;
    if(ft.ret == null || tp.ret != null && tp.ret.instanceOf(ft.ret)) {
      checked = optimized;
    } else {
      final TypeCheck tc = new TypeCheck(sc, ii, optimized, ft.ret, true);
      checked = opt ? tc.optimize(ctx, vsc) : tc;
    }
    checked.markTailCalls(null);
    return new FuncItem(sc, ann, name, vs, ft, checked, vsc.stackSize());
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
    tb.add(')').add(ft.ret != null ? " as " + ft.ret : "").add(" { ").addExt(expr).add(" }");
    return tb.toString();
  }

  @Override
  public Expr inlineExpr(final Expr[] exprs, final QueryContext ctx, final VarScope scp,
      final InputInfo ii) throws QueryException {
    if(!(expr.isValue() || expr.exprSize() < ctx.context.options.get(MainOptions.INLINELIMIT)
        && !expr.has(Flag.CTX))) return null;
    ctx.compInfo(OPTINLINE, this);
    // create let bindings for all variables
    final LinkedList<GFLWOR.Clause> cls =
        exprs.length == 0 ? null : new LinkedList<GFLWOR.Clause>();
    final IntObjMap<Var> vs = new IntObjMap<Var>();
    for(int i = 0; i < vars.length; i++) {
      final Var old = vars[i], v = scp.newCopyOf(ctx, old);
      vs.put(old.id, v);
      cls.add(new Let(v, exprs[i], false, ii).optimize(ctx, scp));
    }

    // copy the function body
    final Expr rt = expr.copy(ctx, scp, vs);

    rt.accept(new ASTVisitor() {
      @Override
      public boolean inlineFunc(final Scope sub) {
        return sub.visit(this);
      }

      @Override
      public boolean dynFuncCall(final DynFuncCall call) {
        call.markInlined(FuncItem.this);
        return true;
      }
    });

    return cls == null ? rt : new GFLWOR(ii, cls, rt).optimize(ctx, scp);
  }
}
