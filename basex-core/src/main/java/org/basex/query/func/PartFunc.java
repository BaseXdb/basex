package org.basex.query.func;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.util.list.*;
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
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class PartFunc extends Arr {
  /** Static context. */
  private final StaticContext sc;
  /** Positions of the placeholders. */
  private final int[] holes;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param expr function expression
   * @param args arguments
   * @param holes positions of the placeholders
   */
  public PartFunc(final StaticContext sc, final InputInfo info, final Expr expr, final Expr[] args,
      final int[] holes) {

    super(info, SeqType.FUN_O, ExprList.concat(args, expr));
    this.sc = sc;
    this.holes = holes;
  }

  /**
   * Returns the function body expression.
   * @return body
   */
  private Expr body() {
    return exprs[exprs.length - 1];
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    if(allAreValues(false)) return cc.preEval(this);

    final Expr f = body();
    final SeqType t = f.seqType();
    if(t.instanceOf(SeqType.FUN_O) && t.type != SeqType.ANY_FUN) {
      final FuncType ft = (FuncType) t.type;
      final int nargs = exprs.length + holes.length - 1;
      if(ft.argTypes.length != nargs)
        throw INVARITY_X_X_X.get(info, arguments(nargs), ft.argTypes.length, f);
      final SeqType[] args = new SeqType[holes.length];
      final int hl = holes.length;
      for(int h = 0; h < hl; h++) args[h] = ft.argTypes[holes[h]];
      exprType.assign(FuncType.get(ft.declType, args).seqType());
    }

    return this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FItem fun = toFunc(body(), qc);

    final int hl = holes.length, nargs = exprs.length + hl - 1;
    if(fun.arity() != nargs) throw INVARITY_X_X_X.get(info, arguments(nargs), fun.arity(), fun);

    final FuncType ft = fun.funcType();
    final Expr[] args = new Expr[nargs];
    final VarScope scp = new VarScope(sc);
    final Var[] vars = new Var[hl];
    int a = -1;
    for(int h = 0; h < hl; h++) {
      while(++a < holes[h]) args[a] = exprs[a - h].value(qc);
      vars[h] = scp.addNew(fun.paramName(holes[h]), null, false, qc, info);
      args[a] = new VarRef(info, vars[h]);
      final SeqType at = ft.argTypes[a];
      if(at != null) vars[h].refineType(at, null);
    }
    final int al = args.length;
    while(++a < al) args[a] = exprs[a - hl].value(qc);

    final AnnList anns = fun.annotations();
    final FuncType tp = FuncType.get(anns, ft.declType, vars);
    final DynFuncCall fc = new DynFuncCall(info, sc, anns.contains(Annotation.UPDATING),
        false, fun, args);

    return new FuncItem(sc, anns, null, vars, tp, fc, qc.focus, scp.stackSize());
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(Arrays.copyOf(exprs, exprs.length - 1));
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return item(qc, info);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new PartFunc(sc, info, body().copy(cc, vm),
        copyAll(cc, vm, Arrays.copyOf(exprs, exprs.length - 1)), holes.clone()));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof PartFunc && Arrays.equals(holes, ((PartFunc) obj).holes) &&
        super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    final int es = exprs.length, hs = holes.length;
    exprs[es - 1].plan(e);
    int p = -1;
    for(int h = 0; h < hs; h++) {
      while(++p < holes[h]) exprs[p - h].plan(e);
      final FElem a = new FElem(QueryText.ARG);
      e.add(a.add(planAttr(QueryText.POS, Token.token(h))));
    }
    while(++p < es + hs - 1) exprs[p - hs].plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(body().toString()).add('(');
    int p = -1;
    final int es = exprs.length, hs = holes.length;
    for(int i = 0; i < hs; i++) {
      while(++p < holes[i])
        tb.add(p > 0 ? QueryText.SEP : "").add(exprs[p - i].toString());
      tb.add(p > 0 ? QueryText.SEP : "").add('?');
    }
    while(++p < es + hs - 1) tb.add(QueryText.SEP).add(exprs[p - hs].toString());
    return tb.add(')').toString();
  }
}
