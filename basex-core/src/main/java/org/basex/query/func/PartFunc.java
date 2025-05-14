package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Partially applied function.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class PartFunc extends Arr {
  /** Number of placeholders. */
  private final int placeholders;
  /** Placeholder parameter permutation (can be {@code null}). */
  private final int[] placeholderPerm;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs expressions (arguments with optional placeholders, followed by body)
   * @param placeholders number of placeholders
   * @param placeholderPerm placeholder parameter permutation (can be {@code null})
   */
  public PartFunc(final InputInfo info, final Expr[] exprs, final int placeholders,
      final int[] placeholderPerm) {
    super(info, SeqType.FUNCTION_O, exprs);
    this.placeholders = placeholders;
    this.placeholderPerm = placeholderPerm;
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
    if(values(false, cc)) return cc.preEval(this);

    final Expr func = body();
    final FuncType ft = func.funcType();
    if(ft != null && ft != SeqType.FUNCTION) {
      final int nargs = exprs.length - 1, arity = ft.argTypes.length;
      if(nargs != arity) throw arityError(func, nargs, arity, false, info);

      final SeqType[] args = new SeqType[placeholders];
      for(int a = 0, e = 0; e < nargs; e++) if(placeholder(exprs[e])) {
        args[placeholderPerm == null ? a : placeholderPerm[a]] = ft.argTypes[e];
        ++a;
      }
      exprType.assign(FuncType.get(ft.declType, args).seqType());
    }
    return this;
  }

  @Override
  public FuncItem item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FItem func = toFunction(body(), qc);

    final int el = exprs.length - 1;
    final int nargs = el, arity = func.arity();
    if(nargs != arity) throw arityError(func, nargs, arity, false, info);

    final FuncType ft = func.funcType();
    final Expr[] args = new Expr[nargs];
    final VarScope vs = new VarScope();

    final Var[] params = new Var[placeholders];
    for(int p = 0, e = 0; e < el; e++) {
      final Expr expr = exprs[e];
      final SeqType at = ft.argTypes[e];
      if(placeholder(expr)) {
        final Var param = vs.addNew(func.paramName(e), at, qc, info);
        args[e] = new VarRef(info, param);
        params[placeholderPerm == null ? p : placeholderPerm[p]] = param;
        ++p;
      } else {
        args[e] = at.coerce(expr.value(qc), null, qc, null, ii);
      }
    }
    final AnnList anns = func.annotations();
    final boolean updating = anns.contains(Annotation.UPDATING);
    final DynFuncCall expr = new DynFuncCall(info, updating, false, func, args);

    final FuncType type = FuncType.get(anns, ft.declType, params);
    return new FuncItem(info, expr, params, anns, type, vs.stackSize(), null, qc.focus.copy());
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(Arrays.copyOf(exprs, exprs.length - 1));
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new PartFunc(info, copyAll(cc, vm, exprs), placeholders, placeholderPerm));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof PartFunc pf && placeholders == pf.placeholders &&
        Arrays.equals(placeholderPerm, pf.placeholderPerm) && super.equals(obj);
  }

  /**
   * Checks if an expression is a placeholder.
   * @param expr expression to be checked
   * @return result of check
   */
  static boolean placeholder(final Expr expr) {
    return expr == Empty.UNDEFINED;
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(body()).token('(');
    final int el = exprs.length - 1;
    for(int e = 0; e < el; e++) {
      if(e > 0) qs.token(SEP);
      final Expr expr = exprs[e];
      if(placeholder(expr)) {
        qs.token('?');
      } else {
        qs.token(exprs[e]);
      }
    }
    qs.token(')');
  }
}
