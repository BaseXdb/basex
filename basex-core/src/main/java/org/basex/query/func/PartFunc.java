package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Partial function application.
 *
 * @author BaseX Team 2005-21, BSD License
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

    super(info, SeqType.FUNCTION_O, ExprList.concat(args, expr));
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

    final Expr body = body();
    final FuncType ft = body.funcType();
    if(ft != null && ft != SeqType.FUNCTION) {
      final int nargs = exprs.length + holes.length - 1;
      if(ft.argTypes.length != nargs)
        throw INVARITY_X_X_X.get(info, arguments(nargs), ft.argTypes.length, body);
      final SeqType[] args = new SeqType[holes.length];
      final int hl = holes.length;
      for(int h = 0; h < hl; h++) args[h] = ft.argTypes[holes[h]];
      exprType.assign(FuncType.get(ft.declType, args).seqType());
    }

    return this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FItem func = toFunc(body(), qc);

    final int hl = holes.length, nargs = exprs.length + hl - 1;
    if(func.arity() != nargs) throw INVARITY_X_X_X.get(info, arguments(nargs), func.arity(), func);

    final FuncType ft = func.funcType();
    final Expr[] args = new Expr[nargs];
    final VarScope vs = new VarScope(sc);
    final Var[] params = new Var[hl];
    int a = -1;
    for(int h = 0; h < hl; h++) {
      while(++a < holes[h]) args[a] = exprs[a - h].value(qc);
      params[h] = vs.addNew(func.paramName(holes[h]), null, false, qc, info);
      args[a] = new VarRef(info, params[h]);
      final SeqType at = ft.argTypes[a];
      if(at != null) params[h].refineType(at, null);
    }
    final int al = args.length;
    while(++a < al) args[a] = exprs[a - hl].value(qc);

    final AnnList anns = func.annotations();
    final boolean updating = anns.contains(Annotation.UPDATING);
    final DynFuncCall expr = new DynFuncCall(info, sc, updating, false, func, args);

    final FuncType type = FuncType.get(anns, ft.declType, params);
    return new FuncItem(sc, anns, null, params, type, expr, qc.focus.copy(), vs.stackSize(), info);
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(Arrays.copyOf(exprs, exprs.length - 1));
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
  public void plan(final QueryString qs) {
    qs.token(body()).token('(');
    int p = -1;
    final int el = exprs.length, hs = holes.length;
    for(int i = 0; i < hs; i++) {
      while(++p < holes[i]) {
        if(p > 0) qs.token(SEP);
        qs.token(exprs[p - i]);
      }
      if(p > 0) qs.token(SEP);
      qs.token('?');
    }
    while(++p < el + hs - 1) qs.token(SEP).token(exprs[p - hs]);
    qs.token(')');
  }
}
