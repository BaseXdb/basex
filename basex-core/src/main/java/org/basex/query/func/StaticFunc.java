package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Expr.Flag;
import org.basex.query.gflwor.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * A static user-defined function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class StaticFunc extends StaticDecl implements XQFunction {
  /** Arguments. */
  public final Var[] args;
  /** Updating flag. */
  public final boolean updating;

  /** Map with requested function properties. */
  private final EnumMap<Flag, Boolean> map = new EnumMap<Flag, Boolean>(Flag.class);
  /** Flag that is turned on during compilation and prevents premature inlining. */
  private boolean compiling;

  /**
   * Function constructor.
   * @param a annotations
   * @param n function name
   * @param v arguments
   * @param r return type
   * @param e function body
   * @param stc static context
   * @param scp variable scope
   * @param xqdoc current xqdoc cache
   * @param ii input info
   */
  public StaticFunc(final Ann a, final QNm n, final Var[] v, final SeqType r, final Expr e,
      final StaticContext stc, final VarScope scp, final String xqdoc, final InputInfo ii) {

    super(stc, a, n, r, scp, xqdoc, ii);
    args = v;
    expr = e;
    updating = ann.contains(Ann.Q_UPDATING);
  }

  @Override
  public void compile(final QueryContext ctx) {
    if(compiled) return;
    compiling = compiled = true;

    final Value cv = ctx.value;
    ctx.value = null;

    final int fp = scope.enter(ctx);
    try {
      expr = expr.compile(ctx, scope);

      if(declType != null) {
        // remove redundant casts
        if((declType.type == AtomType.BLN || declType.type == AtomType.FLT ||
            declType.type == AtomType.DBL || declType.type == AtomType.QNM ||
            declType.type == AtomType.URI) && declType.eq(expr.type())) {
          ctx.compInfo(OPTCAST, declType);
        } else {
          expr = new TypeCheck(sc, info, expr, declType, true).optimize(ctx, scope);
        }
      }
    } catch(final QueryException qe) {
      expr = FNInfo.error(qe, expr.type());
    } finally {
      scope.cleanUp(this);
      scope.exit(ctx, fp);
      ctx.value = cv;
    }

    // convert all function calls in tail position to proper tail calls
    expr.markTailCalls(ctx);

    compiling = false;
  }

  /**
   * Checks if this function can be inlined.
   * @param ctx query context
   * @return result of check
   */
  private boolean inline(final QueryContext ctx) {
    return expr.isValue() ||
        expr.exprSize() < ctx.context.options.get(MainOptions.INLINELIMIT) &&
        !(compiling || has(Flag.CTX) || selfRecursive());
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem(NAM, name.string());
    addPlan(plan, el, expr);
    for(int i = 0; i < args.length; ++i) {
      el.add(planAttr(ARG + i, args[i].name.string()));
    }
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(DECLARE).add(' ').addExt(ann);
    if(updating) tb.add(UPDATING).add(' ');
    tb.add(FUNCTION).add(' ').add(name.string());
    tb.add(PAR1).addSep(args, SEP).add(PAR2);
    if(declType != null) tb.add(' ' + AS + ' ' + declType);
    if(expr != null) tb.add(" { ").addExt(expr).add(" }; ");
    else tb.add(" external; ");
    return tb.toString();
  }

  /**
   * Checks if this function calls itself recursively.
   * @return result of check
   */
  private boolean selfRecursive() {
    return !expr.accept(new ASTVisitor() {
      @Override
      public boolean staticFuncCall(final StaticFuncCall call) {
        return call.func != StaticFunc.this;
      }

      @Override
      public boolean inlineFunc(final Scope sub) {
        return sub.visit(this);
      }
    });
  }

  @Override
  public int arity() {
    return args.length;
  }

  @Override
  public QNm funcName() {
    return name;
  }

  @Override
  public QNm argName(final int pos) {
    return args[pos].name;
  }

  @Override
  public FuncType funcType() {
    return FuncType.get(ann, args, declType);
  }

  @Override
  public int stackFrameSize() {
    return scope.stackSize();
  }

  @Override
  public Ann annotations() {
    return ann;
  }

  @Override
  public Item invItem(final QueryContext ctx, final InputInfo ii, final Value... arg)
      throws QueryException {

    // reset context and evaluate function
    final Value cv = ctx.value;
    ctx.value = null;
    try {
      for(int i = 0; i < args.length; i++) ctx.set(args[i], arg[i], ii);
      return expr.item(ctx, ii);
    } finally {
      ctx.value = cv;
    }
  }

  @Override
  public Value invValue(final QueryContext ctx, final InputInfo ii, final Value... arg)
      throws QueryException {

    // reset context and evaluate function
    final Value cv = ctx.value;
    ctx.value = null;
    try {
      for(int i = 0; i < args.length; i++) ctx.set(args[i], arg[i], ii);
      return ctx.value(expr);
    } finally {
      ctx.value = cv;
    }
  }

  @Override
  public Value invokeValue(final QueryContext ctx, final InputInfo ii, final Value... arg)
      throws QueryException {
    return FuncCall.value(this, arg, ctx, ii);
  }

  @Override
  public Item invokeItem(final QueryContext ctx, final InputInfo ii, final Value... arg)
      throws QueryException {
    return FuncCall.item(this, arg, ctx, ii);
  }

  /**
   * Checks if all updating expressions in the function are correctly declared and placed.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    final boolean u = expr.has(Flag.UPD);
    if(u) expr.checkUp();
    final InputInfo ii = expr instanceof ParseExpr ? ((ParseExpr) expr).info : info;
    if(updating) {
      // updating function
      if(declType != null) throw UPFUNCTYPE.get(info);
      if(!u && !expr.isVacuous()) throw UPEXPECTF.get(ii);
    } else if(u) {
      // uses updates, but is not declared as such
      throw UPNOT.get(ii, description());
    }
  }

  /**
   * Checks if this function returns vacuous results (see {@link Expr#isVacuous()}).
   * @return result of check
   */
  public boolean isVacuous() {
    return !has(Flag.UPD) && declType != null && declType.eq(SeqType.EMP);
  }

  /**
   * Indicates if an expression has the specified compiler property.
   * @param flag feature
   * @return result of check
   * @see Expr#has(Flag)
   */
  public boolean has(final Flag flag) {
    // handle recursive calls: set dummy value, eventually replace it with final value
    Boolean b = map.get(flag);
    if(b == null) {
      map.put(flag, false);
      b = expr == null || expr.has(flag);
      map.put(flag, b);
    }
    return b;
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    for(final Var v : args) if(!visitor.declared(v)) return false;
    return expr.accept(visitor);
  }

  @Override
  public byte[] id() {
    return StaticFuncs.sig(name, args.length);
  }

  @Override
  public Expr inlineExpr(final Expr[] exprs, final QueryContext ctx, final VarScope scp,
      final InputInfo ii) throws QueryException {
    if(!inline(ctx)) return null;
    ctx.compInfo(OPTINLINEFN, name);
    // create let bindings for all variables
    final LinkedList<GFLWOR.Clause> cls = exprs.length == 0 ? null :
      new LinkedList<GFLWOR.Clause>();
    final IntObjMap<Var> vs = new IntObjMap<Var>();
    for(int i = 0; i < args.length; i++) {
      final Var old = args[i], v = scp.newCopyOf(ctx, old);
      vs.put(old.id, v);
      cls.add(new Let(v, exprs[i], false, info).optimize(ctx, scp));
    }

    // copy the function body
    final Expr cpy = expr.copy(ctx, scp, vs);

    return cls == null ? cpy : new GFLWOR(info, cls, cpy).optimize(ctx, scp);
  }
}
