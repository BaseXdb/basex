package org.basex.query.func;

import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
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
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class DynFuncCall extends Arr {
  /**
   * Function constructor.
   * @param ii input info
   * @param fun function expression
   * @param arg arguments
   */
  public DynFuncCall(final InputInfo ii, final Expr fun, final Expr[] arg) {
    super(ii, Array.add(arg, fun));
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);
    final int ar = expr.length - 1;
    final Expr f = expr[ar];
    final Type t = f.type().type;
    if(t instanceof FuncType) {
      final FuncType ft = (FuncType) t;
      if(ft.args != null && ft.args.length != ar)
        throw INVARITY.thrw(info, f, ar);
      if(ft.ret != null) type = ft.ret;
    }
    // maps can only contain fully evaluated Values, so this is safe
    return allAreValues() && f instanceof Map ? optPre(value(ctx), ctx) : this;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return getFun(ctx).invItem(ctx, ii, argv(ctx));
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    return getFun(ctx).invValue(ctx, info, argv(ctx));
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    final Expr[] copy = copyAll(ctx, scp, vs, expr);
    final int last = copy.length - 1;
    return copyType(new DynFuncCall(info, copy[last], Arrays.copyOf(copy, last)));
  }

  /**
   * Evaluates all arguments.
   * @param ctx query context
   * @return array of argument values
   * @throws QueryException query exception
   */
  private Value[] argv(final QueryContext ctx) throws QueryException {
    final Value[] argv = new Value[expr.length - 1];
    for(int i = argv.length; --i >= 0;) argv[i] = ctx.value(expr[i]);
    return argv;
  }

  /**
   * Evaluates and checks the function item.
   * @param ctx query context
   * @return function item
   * @throws QueryException query exception
   */
  private FItem getFun(final QueryContext ctx) throws QueryException {
    final int ar = expr.length - 1;
    final Item it = checkItem(expr[ar], ctx);
    if(!(it instanceof FItem)) NOCAST.thrw(info, it.type, "function item");
    final FItem fit = (FItem) it;
    if(fit.arity() != ar) throw INVARITY.thrw(info, fit, ar);
    return fit;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem();
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
}
