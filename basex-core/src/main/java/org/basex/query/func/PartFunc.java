package org.basex.query.func;

import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Partial function application.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public final class PartFunc extends Arr {
  /** Static context. */
  private final StaticContext sc;
  /** Positions of the placeholders. */
  private final int[] holes;

  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param fn function expression
   * @param arg argument expressions
   * @param hl positions of the placeholders
   */
  public PartFunc(final StaticContext sctx, final InputInfo ii, final Expr fn,
      final Expr[] arg, final int[] hl) {
    super(ii, Array.add(arg, fn));
    sc = sctx;
    holes = hl;
    type = SeqType.FUN_O;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);
    return optimize(ctx, scp);
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    final Expr f = expr[expr.length - 1];
    if(allAreValues()) return preEval(ctx);

    final SeqType t = f.type();
    if(t.instanceOf(SeqType.FUN_O) && t.type != FuncType.ANY_FUN) {
      final FuncType ft = (FuncType) t.type;
      final int arity = expr.length + holes.length - 1;
      if(ft.args.length != arity) throw INVARITY.get(info, f, arity);
      final SeqType[] ar = new SeqType[holes.length];
      for(int i = 0; i < holes.length; i++) ar[i] = ft.args[holes[i]];
      type = FuncType.get(ft.ret, ar).seqType();
    }

    return this;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Expr fn = expr[expr.length - 1];
    final FItem f = (FItem) checkType(fn.item(ctx, ii), FuncType.ANY_FUN);
    final FuncType ft = f.funcType();

    final int arity = expr.length + holes.length - 1;
    if(f.arity() != arity) throw INVARITY.get(ii, f, arity);
    final Expr[] args = new Expr[arity];

    final VarScope scp = new VarScope(sc);
    final Var[] vars = new Var[holes.length];
    int p = -1;
    for(int i = 0; i < holes.length; i++) {
      while(++p < holes[i]) args[p] = expr[p - i].value(ctx);
      vars[i] = scp.newLocal(ctx, f.argName(holes[i]), ft.args[p], true);
      args[p] = new VarRef(info, vars[i]);
    }
    while(++p < args.length) args[p] = expr[p - holes.length].value(ctx);

    final Expr call = new DynFuncCall(info, f, args).optimize(ctx, scp);
    final InlineFunc func = new InlineFunc(
        info, ft.ret, vars, call, f.annotations(), sc, scp);
    return func.optimize(ctx, null).item(ctx, ii);
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    return item(ctx, info);
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new PartFunc(sc, info, expr[expr.length - 1].copy(ctx, scp, vs),
        copyAll(ctx, scp, vs, Arrays.copyOf(expr, expr.length - 1)), holes.clone());
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    final int es = expr.length, hs = holes.length;
    expr[es - 1].plan(e);
    int p = -1;
    for(int i = 0; i < hs; i++) {
      while(++p < holes[i]) expr[p - i].plan(e);
      final FElem a = new FElem(QueryText.ARG);
      e.add(a.add(planAttr(QueryText.POS, Token.token(i))));
    }
    while(++p < es + hs - 1) expr[p - hs].plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(expr[expr.length - 1].toString()).add('(');
    int p = -1;
    final int es = expr.length, hs = holes.length;
    for(int i = 0; i < hs; i++) {
      while(++p < holes[i])
        tb.add(p > 0 ? QueryText.SEP : "").add(expr[p - i].toString());
      tb.add(p > 0 ? QueryText.SEP : "").add('?');
    }
    while(++p < es + hs - 1) tb.add(QueryText.SEP).add(expr[p - hs].toString());
    return tb.add(')').toString();
  }
}
