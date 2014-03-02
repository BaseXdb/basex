package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
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
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class DynFuncCall extends FuncCall {
  /** Hash values of all function items that this call was copied from, possibly {@code null}. */
  private int[] inlinedFrom;
  /**
   * Function constructor.
   * @param ii input info
   * @param fun function expression
   * @param arg arguments
   */
  public DynFuncCall(final InputInfo ii, final Expr fun, final Expr... arg) {
    super(ii, Array.add(arg, fun));
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);
    return optimize(ctx, scp);
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    final int ar = expr.length - 1;
    final Expr f = expr[ar];
    final Type t = f.type().type;
    if(t instanceof FuncType) {
      final FuncType ft = (FuncType) t;
      if(ft.args != null && ft.args.length != ar) throw INVARITY.get(info, f, ar);
      if(ft.ret != null) type = ft.ret;
    }

    if(f instanceof XQFunctionExpr) {
      // maps can only contain fully evaluated Values, so this is safe
      if(allAreValues() && f instanceof Map) return optPre(value(ctx), ctx);

      // try to inline the function
      if(!(f instanceof FuncItem && comesFrom((FuncItem) f))) {
        final Expr[] args = Arrays.copyOf(expr, expr.length - 1);
        final Expr inl = ((XQFunctionExpr) f).inlineExpr(args, ctx, scp, info);
        if(inl != null) return inl;
      }
    }

    return this;
  }

  /**
   * Marks this call after it was inlined from the given function item.
   * @param it the function item
   */
  public void markInlined(final FuncItem it) {
    final int hash = it.hashCode();
    inlinedFrom = inlinedFrom == null ? new int[] { hash } : Array.add(inlinedFrom, hash);
  }

  /**
   * Checks if this call was inlined from the body of the given function item.
   * @param it function item
   * @return result of check
   */
  private boolean comesFrom(final FuncItem it) {
    if(inlinedFrom != null) {
      final int hash = it.hashCode();
      for(final int h : inlinedFrom) if(hash == h) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    final Expr[] copy = copyAll(ctx, scp, vs, expr);
    final int last = copy.length - 1;
    final DynFuncCall call = new DynFuncCall(info, copy[last], Arrays.copyOf(copy, last));
    if(inlinedFrom != null) call.inlinedFrom = inlinedFrom.clone();
    return copyType(call);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.dynFuncCall(this) && visitAll(visitor, expr);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem(TCL, tailCall);
    final int es = expr.length;
    addPlan(plan, el, expr[es - 1]);
    for(int e = 0; e < es - 1; e++) expr[e].plan(el);
  }

  @Override
  public String description() {
    return expr[expr.length - 1].description() + "(...)";
  }

  @Override
  public String toString() {
    final int es = expr.length;
    final TokenBuilder tb = new TokenBuilder(expr[es - 1].toString()).add('(');
    for(int e = 0; e < es - 1; e++) {
      tb.add(expr[e].toString());
      if(e < es - 2) tb.add(", ");
    }
    return tb.add(')').toString();
  }

  @Override
  FItem evalFunc(final QueryContext ctx) throws QueryException {
    final int ar = expr.length - 1;
    final Item it = checkItem(expr[ar], ctx);
    if(!(it instanceof FItem)) throw INVFUNCITEM.get(info, it.type);
    final FItem fit = (FItem) it;
    if(fit.arity() != ar) throw INVARITY.get(info, fit, ar);
    return fit;
  }

  @Override
  Value[] evalArgs(final QueryContext ctx) throws QueryException {
    final int al = expr.length - 1;
    final Value[] args = new Value[al];
    for(int a = 0; a < al; ++a) args[a] = ctx.value(expr[a]);
    return args;
  }
}
