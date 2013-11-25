package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
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
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public final class DynFuncCall extends FuncCall {
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
      if(ft.type != null) type = ft.type;
    }

    if(f instanceof XQFunction) {
      // maps can only contain fully evaluated Values, so this is safe
      if(allAreValues() && f instanceof Map) return optPre(value(ctx), ctx);

      final Expr[] args = Arrays.copyOf(expr, expr.length - 1);
      final Expr inl = ((XQFunction) f).inlineExpr(args, ctx, scp, info);
      if(inl != null) {
        // inline the function
        ctx.compInfo(OPTINLINEFN, f);
        return inl;
      }
    }
    return this;
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    final Expr[] copy = copyAll(ctx, scp, vs, expr);
    final int last = copy.length - 1;
    return copyType(new DynFuncCall(info, copy[last], Arrays.copyOf(copy, last)));
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
    if(!(it instanceof FItem)) throw INVCAST.get(info, it.type, "function item");
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
