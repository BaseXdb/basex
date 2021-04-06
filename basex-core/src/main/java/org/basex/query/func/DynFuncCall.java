package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.XQArray;
import org.basex.query.value.item.*;
import org.basex.query.value.map.XQMap;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Dynamic function call.
 *
 * @author BaseX Team 2005-21, BSD License
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
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    if(body().has(Flag.NDT)) ndt = true;
    return super.compile(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final Expr func = body();

    final int nargs = exprs.length - 1;
    final FuncType ft = func.funcType();
    if(ft != null) {
      if(ft.argTypes != null && ft.argTypes.length != nargs) {
        throw INVARITY_X_X_X.get(info, arguments(nargs), ft.argTypes.length, func.toErrorString());
      }
      final SeqType dt = ft.declType;
      exprType.assign(ft instanceof MapType ? dt.union(Occ.ZERO) : dt);
    }

    // maps and arrays can only contain evaluated values, so this is safe
    if((func instanceof XQMap || func instanceof XQArray) && allAreValues(false)) {
      return cc.preEval(this);
    }

    if(func instanceof XQFunctionExpr) {
      // try to inline the function
      final XQFunctionExpr fe = (XQFunctionExpr) func;
      if(!(fe instanceof FuncItem && comesFrom((FuncItem) fe))) {
        checkUp(fe, updating, sc);
        final Expr inlined = fe.inline(Arrays.copyOf(exprs, nargs), cc);
        if(inlined != null) return inlined;
      }
    } else if(func instanceof Value) {
      // raise error (values tested at this stage are no functions)
      final Item item = toItem(func, cc.qc);
      throw INVFUNCITEM_X_X.get(info, item.type, func);
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
   * @param item the function item
   */
  public void markInlined(final FuncItem item) {
    final int hash = item.hashCode();
    inlinedFrom = inlinedFrom == null ? new int[] { hash } :
      org.basex.util.Array.add(inlinedFrom, hash);
  }

  /**
   * Checks if this call was inlined from the body of the given function item.
   * @param item function item
   * @return result of check
   */
  private boolean comesFrom(final FuncItem item) {
    if(inlinedFrom != null) {
      final int hash = item.hashCode();
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
  public Expr body() {
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
  FItem evalFunc(final QueryContext qc) throws QueryException {
    final Item item = toItem(body(), qc);
    if(!(item instanceof FItem)) throw INVFUNCITEM_X_X.get(info, item.type, item);

    final FItem func = checkUp((FItem) item, updating, sc);
    final int nargs = exprs.length - 1;
    if(func.arity() != nargs) throw INVARITY_X_X_X.get(
        info, arguments(nargs), func.arity(), func.toErrorString());
    return func;
  }

  @Override
  Value[] evalArgs(final QueryContext qc) throws QueryException {
    final int el = exprs.length - 1;
    final Value[] args = new Value[el];
    for(int e = 0; e < el; e++) args[e] = exprs[e].value(qc);
    return args;
  }

  @Override
  public boolean has(final Flag... flags) {
    final boolean upd = updating || sc.mixUpdates;
    if(Flag.UPD.in(flags) && upd) return true;
    if(Flag.NDT.in(flags) && (ndt || upd)) return true;
    final Flag[] flgs = Flag.NDT.remove(Flag.UPD.remove(flags));
    return flgs.length != 0 && super.has(flgs);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof DynFuncCall && updating == ((DynFuncCall) obj).updating &&
        super.equals(obj);
  }

  @Override
  public String description() {
    return body().description() + "(...)";
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, TAILCALL, tco), exprs);
  }

  @Override
  public void plan(final QueryString qs) {
    final int el = exprs.length - 1;
    qs.token(exprs[el]).token('(');
    for(int e = 0; e < el; e++) {
      if(e > 0) qs.token(SEP);
      qs.token(exprs[e]);
    }
    qs.token(')');
  }
}
