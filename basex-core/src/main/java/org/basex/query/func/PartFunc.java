package org.basex.query.func;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
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
 * @author BaseX Team 2005-15, BSD License
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
   * @param args argument expressions
   * @param holes positions of the placeholders
   */
  public PartFunc(final StaticContext sc, final InputInfo info, final Expr expr, final Expr[] args,
      final int[] holes) {

    super(info, Array.add(args, expr));
    this.sc = sc;
    this.holes = holes;
    seqType = SeqType.FUN_O;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    final Expr f = exprs[exprs.length - 1];
    if(allAreValues()) return preEval(qc);

    final SeqType t = f.seqType();
    if(t.instanceOf(SeqType.FUN_O) && t.type != FuncType.ANY_FUN) {
      final FuncType ft = (FuncType) t.type;
      final int ar = exprs.length + holes.length - 1;
      if(ft.argTypes.length != ar)
        throw INVARITY_X_X_X_X.get(info, f, ar, ar == 1 ? "" : "s", ft.argTypes.length);
      final SeqType[] args = new SeqType[holes.length];
      final int hl = holes.length;
      for(int h = 0; h < hl; h++) args[h] = ft.argTypes[holes[h]];
      seqType = FuncType.get(ft.retType, args).seqType();
    }

    return this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FItem f = toFunc(exprs[exprs.length - 1], qc);

    final int hl = holes.length;
    final int ar = exprs.length + hl - 1;
    if(f.arity() != ar) throw INVARITY_X_X_X_X.get(info, f, ar, ar == 1 ? "" : "s", f.arity());

    final FuncType ft = f.funcType();
    final Expr[] args = new Expr[ar];
    final VarScope scp = new VarScope(sc);
    final Var[] vars = new Var[hl];
    int a = -1;
    for(int h = 0; h < hl; h++) {
      while(++a < holes[h]) args[a] = exprs[a - h].value(qc);
      vars[h] = scp.newLocal(qc, f.argName(holes[h]), null, false);
      args[a] = new VarRef(info, vars[h]);
      vars[h].refineType(ft.argTypes[a], qc, ii);
    }
    final int al = args.length;
    while(++a < al) args[a] = exprs[a - hl].value(qc);

    final Ann ann = f.annotations();
    final FuncType tp = FuncType.get(ann, vars, ft.retType);
    final DynFuncCall fc = new DynFuncCall(info, sc, ann.contains(Ann.Q_UPDATING), f, args);
    return new FuncItem(sc, ann, null, vars, tp, fc, qc.value, qc.pos, qc.size, scp.stackSize());
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
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new PartFunc(sc, info, exprs[exprs.length - 1].copy(qc, scp, vs),
        copyAll(qc, scp, vs, Arrays.copyOf(exprs, exprs.length - 1)), holes.clone());
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    final int es = exprs.length, hs = holes.length;
    exprs[es - 1].plan(e);
    int p = -1;
    for(int i = 0; i < hs; i++) {
      while(++p < holes[i]) exprs[p - i].plan(e);
      final FElem a = new FElem(QueryText.ARG);
      e.add(a.add(planAttr(QueryText.POS, Token.token(i))));
    }
    while(++p < es + hs - 1) exprs[p - hs].plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(exprs[exprs.length - 1].toString()).add('(');
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

  /**
   * Returns the function annotations.
   * @return annotations
   */
  public Ann annotations() {
    final Expr fn = exprs[exprs.length - 1];
    if(!(fn instanceof FItem)) return null;
    return ((FItem) fn).annotations();
  }
}
