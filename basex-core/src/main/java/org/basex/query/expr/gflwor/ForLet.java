package org.basex.query.expr.gflwor;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * FLWOR {@code for}/{@code let} clause.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
abstract class ForLet extends Clause {
  /** Item variable. */
  public final Var var;
  /** Bound expression. */
  public Expr expr;
  /** Scoring flag. */
  boolean scoring;

  /**
   * Constructor.
   * @param info input info
   * @param seqType sequence type
   * @param var variables
   * @param expr expression
   * @param vars variable
   * @param scoring scoring flag
   */
  ForLet(final InputInfo info, final SeqType seqType, final Var var, final Expr expr,
      final boolean scoring, final Var... vars) {
    super(info, seqType, vars);
    this.var = var;
    this.expr = expr;
    this.scoring = scoring;
  }

  @Override
  public final Clause compile(final CompileContext cc) throws QueryException {
    expr = expr.compile(cc);
    return optimize(cc);
  }

  @Override
  public final boolean has(final Flag... flags) {
    return expr.has(flags);
  }

  @Override
  public final boolean inlineable(final Var v) {
    return expr.inlineable(v);
  }

  @Override
  public final VarUsage count(final Var v) {
    return expr.count(v);
  }

  @Override
  public final Clause inline(final ExprInfo ei, final Expr ex, final CompileContext cc)
      throws QueryException {

    final Expr inlined = expr.inline(ei, ex, cc);
    if(inlined == null) return null;
    expr = inlined;
    return optimize(cc);
  }

  /**
   * Tries to add the given expression as a predicate.
   * Replaces variable references with a context expression
   * @param cc compilation context
   * @param ex expression to add as predicate
   * @return success flag
   * @throws QueryException query exception
   */
  boolean toPredicate(final CompileContext cc, final Expr ex) throws QueryException {
    if(scoring || !ex.uses(var) || !ex.inlineable(var)) return false;

    // reset context value (will not be accessible in predicate)
    Expr pred = cc.get(expr, () -> {
      // assign type of iterated items to context expression
      final Expr inlined = ex.inline(var, new ContextValue(info).optimize(cc), cc);
      return inlined != null ? inlined : ex;
    });

    // attach predicates to axis path or filter, or create a new filter
    if(pred.seqType().mayBeNumber()) {
      pred = cc.function(Function.BOOLEAN, info, pred);
    }

    addPredicate(cc, pred);
    return true;
  }

  /**
   * Adds a predicate to the looped expression.
   * @param cc compilation context
   * @param ex expression to add as predicate
   * @throws QueryException query exception
   */
  final void addPredicate(final CompileContext cc, final Expr ex) throws QueryException {
    if(expr instanceof AxisPath && !ex.has(Flag.POS)) {
      // add to last step of path, provided that predicate is not positional
      expr = ((AxisPath) expr).addPredicates(cc, ex);
    } else if(expr instanceof Filter) {
      // add to existing filter expression
      expr = ((Filter) expr).addPredicate(cc, ex);
    } else {
      // create new filter expression
      expr = Filter.get(cc, info, expr, ex);
    }
  }

  @Override
  public final void checkUp() throws QueryException {
    checkNoUp(expr);
  }

  @Override
  public final int exprSize() {
    return expr.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof ForLet)) return false;
    final ForLet fl = (ForLet) obj;
    return expr.equals(fl.expr) && var.equals(fl.var) && scoring == fl.scoring;
  }
}
