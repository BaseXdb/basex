package org.basex.query.expr.gflwor;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Builder for rewriting a higher-order function to a FLWOR expression that iterates over its input.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FLWORBuilder {
  /** Let clauses preceding the for clause. */
  private final ArrayList<Let> lets = new ArrayList<>();
  /** Compilation context. */
  private final CompileContext cc;
  /** Input info. */
  private final InputInfo info;
  /** Arity. */
  private final int arity;
  /** Item variable. */
  private final Var item;
  /** Positional variable (can be {@code null}). */
  private Var pos;

  /**
   * Constructor.
   * @param arity arity
   * @param cc compilation context
   * @param info input info
   */
  public FLWORBuilder(final int arity, final CompileContext cc, final InputInfo info) {
    this.arity = arity;
    this.cc = cc;
    this.info = info;
    item = cc.var("item", info);
  }

  /**
   * Returns an optimized reference to the item variable.
   * @return expression
   */
  public Expr item() {
    return VarRef.get(info, item);
  }

  /**
   * Returns an optimized reference to the positional variable.
   * @return expression
   */
  public Expr pos() {
    if(pos == null) pos = cc.var(new QNm("pos"), Types.INTEGER_O, info);
    return VarRef.get(info, pos);
  }

  /**
   * Returns variable references as arguments.
   * @return arguments
   */
  public Expr[] refs() {
    final ExprList args = new ExprList(arity);
    if(arity > 0) args.add(item());
    if(arity > 1) args.add(pos());
    return args.finish();
  }

  /**
   * Returns an optimized higher-order function call.
   * @param sf standard function
   * @param i index of higher-order function
   * @param updating updating flag
   * @return expression
   * @throws QueryException query exception
   */
  public Expr function(final StandardFunc sf, final int i, final boolean updating)
      throws QueryException {
    Expr func = sf.coerceFunc(i, cc, Math.min(2, arity));
    // coerce function once, not in each iteration
    if(!func.duplicable()) {
      // closure of the required type: coercion is redundant, inline the function body
      // some($e, fn($i) { $i = $x }) → (for $item in $e return $item = $x) = true()
      if(sf.arg(i) instanceof final Closure closure) {
        final FuncType ft = closure.funcType();
        if(FuncType.get(ft.anns, ft.refinedType, ft.argTypes).instanceOf(func.funcType())) {
          final Expr call = new DynFuncCall(info, updating, false, closure, refs()).optimize(cc);
          if(!(call instanceof DynFuncCall)) return call;
        }
      }
      final Var var = cc.var("action", info);
      lets.add(new Let(var, func).optimize(cc));
      func = VarRef.get(info, var);
    }
    return new DynFuncCall(info, updating, false, func, refs()).optimize(cc);
  }

  /**
   * Finalizes the GFLWOR expression with an optional where clause.
   * @param input input expression
   * @param where where expression (can be {@code null})
   * @param rtrn return expression
   * @return expression
   * @throws QueryException query exception
   */
  public Expr finish(final Expr input, final Expr where, final Expr rtrn) throws QueryException {
    return build(input, where == null ? null : new Where(where, info), rtrn);
  }

  /**
   * Finalizes the GFLWOR expression with a while clause.
   * @param input input expression
   * @param cond condition
   * @param rtrn return expression
   * @return expression
   * @throws QueryException query exception
   */
  public Expr finishWhile(final Expr input, final Expr cond, final Expr rtrn)
      throws QueryException {
    return build(input, new While(cond, info), rtrn);
  }

  /**
   * Builds the GFLWOR expression.
   * @param input input expression
   * @param clause clause after the for clause (can be {@code null})
   * @param rtrn return expression
   * @return expression
   * @throws QueryException query exception
   */
  private Expr build(final Expr input, final Clause clause, final Expr rtrn)
      throws QueryException {
    final ArrayList<Clause> clauses = new ArrayList<>(lets);
    clauses.add(new For(item, pos, null, input, false).optimize(cc));
    if(clause != null) clauses.add(clause.optimize(cc));
    return new GFLWOR(info, clauses, rtrn).optimize(cc);
  }
}
