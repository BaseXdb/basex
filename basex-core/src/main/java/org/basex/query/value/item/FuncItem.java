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
   * @param cst cast flag
   * @param stSize stack-frame size
   */
  public FuncItem(final StaticContext sctx, final Ann annotations, final QNm n, final Var[] arg,
      final FuncType t, final Expr body, final boolean cst, final int stSize) {
    this(sctx, annotations, n, arg, t, body, cst, null, 0, 0, stSize);
  }

  /**
   * Constructor.
   * @param sctx static context
   * @param annotations function annotations
   * @param n function name
   * @param arg function arguments
   * @param t function type
   * @param body function body
   * @param cst cast flag
   * @param vl context value
   * @param ps context position
   * @param sz context size
   * @param stSize stack-frame size
   */
  public FuncItem(final StaticContext sctx, final Ann annotations, final QNm n, final Var[] arg,
      final FuncType t, final Expr body, final boolean cst, final Value vl,
      final long ps, final long sz, final int stSize) {

    super(t, annotations);
    name = n;
    vars = arg;
    expr = body;

    cast = cst && t.ret != null ? t.ret : null;
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
  public Value invValue(final QueryContext ctx, final InputInfo ii, final Value... args)
      throws QueryException {

    // bind variables and cache context
    final int fp = ctx.stack.enterFrame(stackSize);
    final Value cv = ctx.value;
    final long ps = ctx.pos, sz = ctx.size;
    try {
      ctx.value = ctxVal;
      ctx.pos = pos;
      ctx.size = size;
      for(int i = 0; i < vars.length; i++) ctx.set(vars[i], args[i], ii);
      final Value v = ctx.value(expr);
      // optionally cast return value to target type
      return cast != null ? cast.funcConvert(ctx, sc, ii, v, false) : v;
    } finally {
      ctx.value = cv;
      ctx.pos = ps;
      ctx.size = sz;
      ctx.stack.exitFrame(fp);
    }
  }

  @Override
  public Item invItem(final QueryContext ctx, final InputInfo ii, final Value... args)
      throws QueryException {

    // bind variables and cache context
    final int fp = ctx.stack.enterFrame(stackSize);
    final Value cv = ctx.value;
    final long ps = ctx.pos, sz = ctx.size;
    try {
      ctx.value = ctxVal;
      ctx.pos = pos;
      ctx.size = size;
      for(int i = 0; i < vars.length; i++) ctx.set(vars[i], args[i], ii);
      final Item it = expr.item(ctx, ii);
      final Value v = it == null ? Empty.SEQ : it;
      // optionally cast return value to target type
      return cast != null ? cast.funcConvert(ctx, sc, ii, v, false).item(ctx, ii) : it;
    } finally {
      ctx.value = cv;
      ctx.pos = ps;
      ctx.size = sz;
      ctx.stack.exitFrame(fp);
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
    final Expr e = new DynFuncCall(ii, this, refs);
    e.markTailCalls(null);
    return new FuncItem(sc, ann, name,
        vs, ft, opt ? e.optimize(ctx, vsc) : e, !tp.ret.instanceOf(ft.ret), vsc.stackSize());
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
    if(!inline(exprs, ctx)) return null;
    ctx.compInfo(OPTINLINEFN, this);
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
        !expr.has(Flag.CTX)) {
      // check if the function item does not introduce new function calls
      final ASTVisitor self = new ASTVisitor() {
        @Override
        public boolean dynFuncCall(final DynFuncCall call) {
          return false;
        }
      };
      if(expr.accept(self)) return true;

      // checks if the arguments don't contain this function item
      final ASTVisitor args = new ASTVisitor() {
        @Override
        public boolean funcItem(final FuncItem f) {
          return f != FuncItem.this && f.visit(this);
        }
        @Override
        public boolean inlineFunc(final Scope sub) {
          return sub.visit(this);
        }
      };
      return Expr.visitAll(args, as);
    }
    return false;
  }
}
