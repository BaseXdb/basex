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
 * Convenience builder for rewriting an expression to a FLWOR expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FLWORBuilder {
  /** Compilation context. */
  final CompileContext cc;
  /** Input info. */
  final InputInfo info;
  /** Arity. */
  final int arity;

  /** Item variable. */
  public final Var item;
  /** Pos variable. */
  public final Var pos;

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
    item = cc.vs().addNew(new QNm("item"), Types.ITEM_ZM, cc.qc, info);
    pos = cc.vs().addNew(new QNm("pos"), Types.INTEGER_O, cc.qc, info);
  }

  /**
   * Returns an optimized variable reference.
   * @param var variable
   * @return expression
   */
  public Expr ref(final Var var) {
    return new VarRef(info, var).optimize(cc);
  }

  /**
   * Returns variable references as arguments.
   * @return arguments
   */
  public Expr[] refs() {
    final ExprList args = new ExprList(arity);
    if(arity > 0) args.add(ref(item));
    if(arity > 1) args.add(ref(pos));
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
    final Expr func = sf.coerceFunc(i, cc, Math.min(2, arity));
    return new DynFuncCall(info, updating, false, func, refs()).optimize(cc);
  }

  /**
   * Finalizes the GFLWOR expression.
   * @param input input expression
   * @param where where expression (can be empty)
   * @param rtrn return expression
   * @return expression
   * @throws QueryException query exception
   */
  public Expr finish(final Expr input, final Expr where, final Expr rtrn) throws QueryException {
    final LinkedList<Clause> clauses = new LinkedList<>();
    clauses.add(new For(item, pos, null, input, false).optimize(cc));
    if(where != null) clauses.add(new Where(where, info).optimize(cc));
    return new GFLWOR(info, clauses, rtrn).optimize(cc);
  }
}
