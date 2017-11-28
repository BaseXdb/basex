package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Dynamic function call.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class DynFuncCall extends FuncCall {
  /** Static context. */
  private final StaticContext sc;

  /** Updating flag. */
  private final boolean updating;
  /** Non-deterministic flag. */
  private boolean ndt;
  /** Hash values of all function items that this call was copied from, possibly {@code null}. */
  private int[] inlinedFrom;

  /**
   * Function constructor.
   * @param info input info
   * @param sc static context
   * @param expr function expression
   * @param arg arguments
   */
  public DynFuncCall(final InputInfo info, final StaticContext sc, final Expr expr,
      final Expr... arg) {
    this(info, sc, false, false, expr, arg);
  }

  /**
   * Function constructor.
   * @param info input info
   * @param sc static context
   * @param updating updating flag
   * @param ndt non-deterministic flag
   * @param expr function expression
   * @param arg arguments
   */
  public DynFuncCall(final InputInfo info, final StaticContext sc, final boolean updating,
      final boolean ndt, final Expr expr, final Expr... arg) {

    super(info, ExprList.concat(arg, expr));
    this.sc = sc;
    this.updating = updating;
    this.ndt = ndt;
    sc.dynFuncCall = true;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    if(body().has(Flag.NDT)) ndt = true;
    return super.compile(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final Expr f = body();
    final Type tp = f.seqType().type;

    final int nargs = exprs.length - 1;
    if(tp instanceof FuncType) {
      final FuncType ft = (FuncType) tp;
      if(ft.argTypes != null && ft.argTypes.length != nargs)
        throw INVARITY_X_X_X.get(info, arguments(nargs), ft.argTypes.length, f.toErrorString());
      SeqType dt = ft.declType;
      if(tp instanceof MapType) dt = dt.with(dt.occ.union(Occ.ZERO));
      exprType.assign(dt);
    }

    // maps and arrays can only contain evaluated values, so this is safe
    if((f instanceof Map || f instanceof Array) && allAreValues(false)) return cc.preEval(this);

    if(f instanceof XQFunctionExpr) {
      // try to inline the function
      final XQFunctionExpr fe = (XQFunctionExpr) f;
      if(!(f instanceof FuncItem && comesFrom((FuncItem) f))) {
        checkUpdating(fe);
        final Expr[] args = Arrays.copyOf(exprs, nargs);
        final Expr in = fe.inlineExpr(args, cc, info);
        if(in != null) return in;
      }
    } else if(f instanceof Item) {
      throw INVFUNCITEM_X_X.get(info, ((Item) f).type, f);
    }
    return this;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(Arrays.copyOf(exprs, exprs.length - 1));
    body().checkUp();
  }

  /**
   * Marks this call after it was inlined from the given function item.
   * @param it the function item
   */
  public void markInlined(final FuncItem it) {
    final int hash = it.hashCode();
    inlinedFrom = inlinedFrom == null ? new int[] { hash } :
      org.basex.util.Array.add(inlinedFrom, hash);
  }

  /**
   * Checks if this call was inlined from the body of the given function item.
   * @param it function item
   * @return result of check
   */
  private boolean comesFrom(final FuncItem it) {
    if(inlinedFrom != null) {
      final int hash = it.hashCode();
      for(final int h : inlinedFrom) {
        if(hash == h) return true;
      }
    }
    return false;
  }

  /**
   * Returns the function body expression.
   * @return body
   */
  private Expr body() {
    return exprs[exprs.length - 1];
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Expr[] copy = copyAll(cc, vm, exprs);
    final int last = copy.length - 1;
    final Expr[] args = Arrays.copyOf(copy, last);
    final DynFuncCall call = new DynFuncCall(info, sc, updating, ndt, copy[last], args);
    if(inlinedFrom != null) call.inlinedFrom = inlinedFrom.clone();
    return copyType(call);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.dynFuncCall(this) && visitAll(visitor, exprs);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem(TCL, tailCall);
    addPlan(plan, el, body());
    final int last = exprs.length - 1;
    for(int e = 0; e < last; e++) exprs[e].plan(el);
  }

  @Override
  public String description() {
    return body().description() + "(...)";
  }

  @Override
  FItem evalFunc(final QueryContext qc) throws QueryException {
    final Item it = toItem(body(), qc);
    if(!(it instanceof FItem)) throw INVFUNCITEM_X_X.get(info, it.type, it);

    final FItem f = (FItem) it;
    final int nargs = exprs.length - 1;
    if(f.arity() != nargs) throw INVARITY_X_X_X.get(
        info, arguments(nargs), f.arity(), f.toErrorString());
    checkUpdating(f);
    return f;
  }

  /**
   * Checks if the function is updating or not.
   * @param expr function expression
   * @throws QueryException query exception
   */
  private void checkUpdating(final XQFunctionExpr expr) throws QueryException {
    // mix updates, correct annotation: all fine
    if(sc.mixUpdates || updating == expr.annotations().contains(Annotation.UPDATING)) return;

    if(!updating) {
      throw FUNCUP_X.get(info, expr);
    } else if(!expr.isVacuousBody()) {
      throw FUNCNOTUP_X.get(info, expr);
    }
  }

  @Override
  Value[] evalArgs(final QueryContext qc) throws QueryException {
    final int last = exprs.length - 1;
    final Value[] args = new Value[last];
    for(int a = 0; a < last; a++) args[a] = qc.value(exprs[a]);
    return args;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof DynFuncCall && updating == ((DynFuncCall) obj).updating &&
        super.equals(obj);
  }

  @Override
  public boolean has(final Flag... flags) {
    if(Flag.UPD.in(flags) && updating) return true;
    if(Flag.NDT.in(flags) && ndt) return true;
    final Flag[] flgs = Flag.NDT.remove(Flag.UPD.remove(flags));
    return flgs.length != 0 && super.has(flgs);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(body().toString()).add('(');
    final int last = exprs.length - 1;
    for(int e = 0; e < last; e++) {
      tb.add(exprs[e].toString());
      if(e < last - 1) tb.add(", ");
    }
    return tb.add(')').toString();
  }
}
