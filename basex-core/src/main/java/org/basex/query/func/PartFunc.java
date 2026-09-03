package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
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
    super(info, Types.FUNCTION_ZM, exprs);
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
    // create function items at compile time: the resulting function calls can be inlined
    if(values(false, cc)) return cc.replaceWith(this, value(cc.qc, cc));

    final Expr func = body();
    if(func.size() == 1) {
      final FuncType ft = func.funcType();
      if(ft != null && ft != Types.FUNCTION) {
        final int nargs = exprs.length - 1, arity = ft.argTypes.length;
        if(nargs != arity) throw arityError(func, nargs, arity, false, info);

        // all arguments are placeholders in the original order: return original function
        if(placeholders == nargs && placeholderPerm == null) return cc.replaceWith(this, func);

        // FUNC(?, ARG) → fn($param) { FUNC($param, ARG) }: makes the call inlineable.
        // supplied arguments must already match the parameter types: a closure would coerce them
        // when the function is called, but coercion errors must be raised by the application
        boolean rewrite = true;
        for(int e = 0; e < nargs && rewrite; e++) {
          rewrite = placeholder(exprs[e]) || exprs[e].seqType().instanceOf(ft.argTypes[e]);
        }
        if(rewrite) return cc.replaceWith(this, closure(func, ft, nargs, cc));

        final SeqType[] args = new SeqType[placeholders];
        for(int a = 0, e = 0; e < nargs; e++) if(placeholder(exprs[e])) {
          args[placeholderPerm == null ? a : placeholderPerm[a]] = ft.argTypes[e];
          ++a;
        }
        exprType.assign(FuncType.get(ft.declType, args).withRefinedType(ft.refinedType).seqType());
      }
    }
    return this;
  }

  /**
   * Rewrites this expression to a closure with one parameter per placeholder.
   * @param func function expression
   * @param ft type of the function expression
   * @param nargs number of arguments
   * @param cc compilation context
   * @return closure
   * @throws QueryException query exception
   */
  private Expr closure(final Expr func, final FuncType ft, final int nargs,
      final CompileContext cc) throws QueryException {

    final XQFunctionExpr xqf = func instanceof final XQFunctionExpr fe ? fe : null;
    final AnnList anns = ft.anns;
    final boolean updating = anns.contains(Annotation.UPDATING);

    final Closure closure;
    final VarScope vs = new VarScope();
    cc.pushScope(vs);
    try {
      // the function and the supplied arguments are evaluated once, when the closure is created
      final HashMap<Var, Expr> global = new HashMap<>();
      final Var fn = vs.addNew(new QNm(FUNCTION), func.seqType(), cc.qc, info);
      global.put(fn, func);

      final Var[] params = new Var[placeholders];
      final Expr[] args = new Expr[nargs];
      for(int p = 0, e = 0; e < nargs; e++) {
        final QNm name = xqf != null ? xqf.paramName(e) : new QNm(ARG + (e + 1));
        final Var var = vs.addNew(name, ft.argTypes[e], cc.qc, info);
        if(placeholder(exprs[e])) {
          params[placeholderPerm == null ? p : placeholderPerm[p]] = var;
          ++p;
        } else {
          global.put(var, exprs[e]);
        }
        args[e] = new VarRef(info, var).optimize(cc);
      }
      final Expr call = new DynFuncCall(info, updating, false,
          new VarRef(info, fn).optimize(cc), args).optimize(cc);
      closure = new Closure(info, call, params, anns, vs, global, ft.declType, null, false);
    } finally {
      cc.removeScope();
    }
    return closure.optimize(cc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return value(qc, null);
  }

  /**
   * Creates the function items of this partially applied function.
   * @param qc query context
   * @param cc compilation context ({@code null} during runtime)
   * @return function items
   * @throws QueryException query exception
   */
  private Value value(final QueryContext qc, final CompileContext cc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Item item : body().value(qc)) vb.add(funcItem(toFunction(item, qc), qc, cc));
    return vb.value(this);
  }

  /**
   * Creates a function item that calls the given function item with the arguments and placeholders
   * of this partially applied function.
   * @param func function item to be called
   * @param qc query context
   * @param cc compilation context ({@code null} during runtime)
   * @return function item
   * @throws QueryException query exception
   */
  private FItem funcItem(final FItem func, final QueryContext qc, final CompileContext cc)
      throws QueryException {
    final int nargs = exprs.length - 1, arity = func.arity();
    if(nargs != arity) throw arityError(func, nargs, arity, false, info);

    // all arguments are placeholders in the original order: return original function
    if(placeholders == nargs && placeholderPerm == null) return func;

    final FuncType ft = func.funcType();
    final Expr[] args = new Expr[nargs];
    for(int e = 0; e < nargs; e++) {
      final Expr expr = exprs[e];
      args[e] = placeholder(expr) ? Empty.UNDEFINED :
        ft.argTypes[e].coerce(expr.value(qc), qc, info);
    }
    return func.partial(args, placeholderPerm, qc, cc, info);
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
    return this == obj || obj instanceof final PartFunc pf && placeholders == pf.placeholders &&
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
