package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Array.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
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
 * @author BaseX Team 2005-16, BSD License
 * @author Leo Woerteler
 */
public final class DynFuncCall extends FuncCall {
  /** Static context. */
  private final StaticContext sc;
  /** Updating flag. */
  private final boolean upd;

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
   * @param upd updating flag
   * @param ndt non-deterministic flag
   * @param expr function expression
   * @param arg arguments
   */
  public DynFuncCall(final InputInfo info, final StaticContext sc, final boolean upd,
      final boolean ndt, final Expr expr, final Expr... arg) {

    super(info, add(arg, expr));
    this.sc = sc;
    this.upd = upd;
    this.ndt = ndt;
    sc.dynFuncCall = true;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    if(body().has(Flag.NDT)) ndt = true;
    return super.compile(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    final Expr f = body();
    final Type t = f.seqType().type;

    final int last = exprs.length - 1;
    if(t instanceof FuncType) {
      final FuncType ft = (FuncType) t;
      if(ft.argTypes != null && ft.argTypes.length != last) {
        final Expr e = f instanceof FuncItem ? ((FuncItem) f).expr : f;
        throw INVARITY_X_X_X_X.get(info, e, last, last == 1 ? "" : "s", ft.argTypes.length);
      }
      if(ft.type != null) seqType = ft.type;
    }

    // maps and arrays can only contain fully evaluated values, so this is safe
    if((f instanceof Map || f instanceof Array) && allAreValues())
      return optPre(value(qc), qc);

    if(f instanceof XQFunctionExpr) {
      // try to inline the function
      final XQFunctionExpr fe = (XQFunctionExpr) f;
      if(!(f instanceof FuncItem && comesFrom((FuncItem) f))) {
        checkUpdating(fe);
        final Expr[] args = Arrays.copyOf(exprs, last);
        final Expr in = fe.inlineExpr(args, qc, scp, info);
        if(in != null) return in;
      }
    } else if(f instanceof Item && !(f instanceof FItem)) {
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
    inlinedFrom = inlinedFrom == null ? new int[] { hash } : add(inlinedFrom, hash);
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
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final Expr[] copy = copyAll(qc, scp, vs, exprs);
    final int last = copy.length - 1;
    final Expr[] args = Arrays.copyOf(copy, last);
    final DynFuncCall call = new DynFuncCall(info, sc, upd, ndt, copy[last], args);
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
    final int last = exprs.length - 1;
    if(f.arity() != last) {
      final Expr e = f instanceof FuncItem ? ((FuncItem) f).expr : f;
      throw INVARITY_X_X_X_X.get(info, e, last, last == 1 ? "" : "s", f.arity());
    }
    checkUpdating(f);
    return f;
  }

  /**
   * Checks if the function is updating or not.
   * @param item function expression
   * @throws QueryException query exception
   */
  private void checkUpdating(final XQFunctionExpr item) throws QueryException {
    if(!sc.mixUpdates && upd != item.annotations().contains(Annotation.UPDATING))
      throw (upd ? FUNCNOTUP : FUNCUP).get(info);
  }

  @Override
  Value[] evalArgs(final QueryContext qc) throws QueryException {
    final int last = exprs.length - 1;
    final Value[] args = new Value[last];
    for(int a = 0; a < last; a++) args[a] = qc.value(exprs[a]);
    return args;
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.UPD ? upd : flag == Flag.NDT ? ndt : super.has(flag);
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
