package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Expr.Flag;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * A static user-defined function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class StaticFunc extends StaticDecl implements XQFunction {
  /** Arguments. */
  public final Var[] args;
  /** Updating flag. */
  public final boolean updating;
  /** Cast flag. */
  boolean cast;

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
  public StaticFunc(final Ann a, final QNm n, final Var[] v, final SeqType r,
      final Expr e, final StaticContext stc, final VarScope scp,
      final String xqdoc, final InputInfo ii) {

    super(stc, a, n, r, scp, xqdoc, ii);
    args = v;
    expr = e;
    cast = r != null;
    updating = ann.contains(Ann.Q_UPDATING);
  }

  @Override
  public void compile(final QueryContext ctx) throws QueryException {
    if(compiled) return;
    compiling = compiled = true;

    final Value cv = ctx.value;
    final StaticContext cs = ctx.sc;
    ctx.sc = sc;
    ctx.value = null;

    final int fp = scope.enter(ctx);
    try {
      expr = expr.compile(ctx, scope);
    } catch(final QueryException qe) {
      expr = FNInfo.error(qe);
    } finally {
      scope.cleanUp(this);
      scope.exit(ctx, fp);
      ctx.value = cv;
      ctx.sc = cs;
    }

    // convert all function calls in tail position to proper tail calls
    ctx.compInfo(OPTTCE, name);
    expr.markTailCalls();

    if(declType != null) {
      // remove redundant casts
      if((declType.type == AtomType.BLN || declType.type == AtomType.FLT ||
          declType.type == AtomType.DBL || declType.type == AtomType.QNM ||
          declType.type == AtomType.URI) && declType.eq(expr.type())) {
        ctx.compInfo(OPTCAST, declType);
        cast = false;
      }
    }
    compiling = false;
  }

  /**
   * Checks if this function can be inlined.
   * @param ctx query context
   * @return result of check
   */
  boolean inline(final QueryContext ctx) {
    return expr.isValue() || expr.exprSize() < ctx.context.options.num(MainOptions.INLINELIMIT)
        && !(compiling || has(Flag.NDT) || has(Flag.CTX) || selfRecursive());
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
  public boolean selfRecursive() {
    return !expr.accept(new ASTVisitor() {
      @Override
      public boolean funcCall(final StaticFuncCall call) {
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
  public QNm fName() {
    return name;
  }

  @Override
  public FuncType funcType() {
    return FuncType.get(ann, args, declType);
  }

  @Override
  public Item invItem(final QueryContext ctx, final InputInfo ii,
      final Value... arg) throws QueryException {

    // reset context and evaluate function
    final Value cv = ctx.value;
    final StaticContext cs = ctx.sc;
    ctx.sc = sc;
    ctx.value = null;
    final int fp = scope.enter(ctx);
    try {
      addArgs(ctx, ii, arg);
      final Item it = expr.item(ctx, ii);
      final Value v = it == null ? Empty.SEQ : it;
      // optionally promote return value to target type
      return cast ? declType.funcConvert(ctx, ii, v).item(ctx, ii) : it;
    } finally {
      scope.exit(ctx, fp);
      ctx.value = cv;
      ctx.sc = cs;
    }
  }

  @Override
  public Value invValue(final QueryContext ctx, final InputInfo ii,
      final Value... arg) throws QueryException {
    // reset context and evaluate function
    final Value cv = ctx.value;
    final StaticContext cs = ctx.sc;
    ctx.sc = sc;
    ctx.value = null;
    final int fp = scope.enter(ctx);
    try {
      addArgs(ctx, ii, arg);
      final Value v = ctx.value(expr);
      // optionally promote return value to target type
      return cast ? declType.funcConvert(ctx, info, v) : v;
    } finally {
      scope.exit(ctx, fp);
      ctx.value = cv;
      ctx.sc = cs;
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
   * Adds the given arguments to the variable stack.
   * @param ctx query context
   * @param ii input info
   * @param vals values to add
   * @throws QueryException if the arguments can't be bound
   */
  private void addArgs(final QueryContext ctx, final InputInfo ii, final Value[] vals)
      throws QueryException {
    // move variables to stack
    for(int i = 0; i < args.length; i++) ctx.set(args[i], vals[i], ii);
  }

  /**
   * Checks if all updating expressions in the function are correctly declared and placed.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    final boolean u = expr.has(Flag.UPD);
    if(u) expr.checkUp();
    if(updating) {
      // updating function
      if(declType != null) UPFUNCTYPE.thrw(info);
      if(!u && !expr.isVacuous()) UPEXPECTF.thrw(info);
    } else if(u) {
      // uses updates, but is not declared as such
      UPNOT.thrw(info, description());
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
}
