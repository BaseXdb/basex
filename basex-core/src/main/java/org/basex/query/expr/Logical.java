package org.basex.query.expr;

import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.index.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Logical expression, extended by {@link And} and {@link Or}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class Logical extends Arr {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs expressions
   */
  Logical(final InputInfo info, final Expr[] exprs) {
    super(info, Types.BOOLEAN_O, exprs);
  }

  @Override
  public final Expr compile(final CompileContext cc) throws QueryException {
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      exprs[e] = cc.compileOrError(exprs[e], e == 0);
    }
    return optimize(cc);
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    flatten(cc);
    exprs = simplifyAll(Simplify.EBV, cc);

    final boolean or = or();
    if(optimizeEbv(or, false, cc)) return cc.replaceWith(this, Bln.get(or));

    final int el = exprs.length;
    if(el == 0) return Bln.get(!or);
    if(el == 1) return cc.function(Function.BOOLEAN, info, exprs);
    return this;
  }

  @Override
  public final Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(test(qc, ii, 0));
  }

  @Override
  public final boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    final boolean or = or();
    for(final Expr expr : exprs) {
      if(expr.test(qc, info, 0) == or) return or;
    }
    return !or;
  }

  @Override
  public final boolean indexAccessible(final IndexInfo ii) throws QueryException {
    IndexCosts costs = IndexCosts.ZERO;
    final ExprList list = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      // check if expression can be rewritten, and if access is not sequential
      if(!expr.indexAccessible(ii)) return false;
      // skip expressions without results
      if(ii.costs.results() == 0) {
        if(or()) continue;
        return true;
      }
      // summarize costs
      costs = IndexCosts.add(costs, ii.costs);
      list.add(ii.expr);
    }
    // use summarized costs for estimation
    ii.costs = costs;
    // create union or intersection of all remaining requests
    ii.expr = list.size() == 1 ? list.get(0) :
      (or() ? new Union(info, list.finish()) : new Intersect(info, list.finish())).optimize(ii.cc);
    return true;
  }

  /**
   * Or/and comparison.
   * @return result of check
   */
  abstract boolean or();

  @Override
  public final void markTailCalls(final CompileContext cc) {
    // if the last expression returns a boolean for sure, we can jump to it
    final Expr last = exprs[exprs.length - 1];
    if(last.seqType().eq(Types.BOOLEAN_O)) last.markTailCalls(cc);
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    boolean changed = false;
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      try {
        final Expr inlined = exprs[e].inline(ic);
        if(inlined != null) {
          exprs[e] = inlined;
          changed = true;
        }
      } catch(final QueryException ex) {
        // first expression is evaluated eagerly
        if(e == 0) throw ex;

        // everything behind the error is dead anyway
        final Expr[] nw = new Expr[e + 1];
        Array.copy(exprs, e, nw);
        nw[e] = FnError.get(ex, exprs[e]);
        exprs = nw;
        changed = true;
        break;
      }
    }
    return changed ? optimize(ic.cc) : null;
  }
}
