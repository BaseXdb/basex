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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class ForLet extends Clause {
  /** Item variable. */
  public Var var;
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
  public final boolean inlineable(final InlineContext v) {
    return expr.inlineable(v);
  }

  @Override
  public final VarUsage count(final Var v) {
    return expr.count(v);
  }

  @Override
  public final Clause inline(final InlineContext ic) throws QueryException {
    final Expr inlined = expr.inline(ic);
    if(inlined == null) return null;
    expr = inlined;
    return optimize(ic.cc);
  }

  /**
   * Tries to return an expression that is appropriate for inlining.
   * @param cc compilation context
   * @return inlineable expression or {@code null}
   * @throws QueryException query exception
   */
  abstract Expr inlineExpr(CompileContext cc) throws QueryException;

  /**
   * Tries to add the given expression as a predicate.
   * Replaces variable references with a context expression
   * @param cc compilation context
   * @param ex expression to add as predicate
   * @return success flag
   * @throws QueryException query exception
   */
  final boolean toPredicate(final CompileContext cc, final Expr ex) throws QueryException {
    // do not rewrite:
    //   for $a at $p in (1,2) where $a = 2 return $p
    //   let score $s := <a/> where not($s) return $s
    //   let $a as element(a) := <a/> where $a instance of element(b) return $a
    //   let $a := (<a/>, <b/>) where $a/self::a return $a
    //   for $a allowing empty in 0 where $a return count($a)
    if(vars.length != 1 || scoring || var.checksType() || size() != 1 || !ex.uses(var))
      return false;

    final InlineContext ic = new InlineContext(var, new ContextValue(info), cc);
    if(!ic.inlineable(ex)) return false;

    // reset context value (will not be accessible in predicate)
    Expr pred = cc.get(expr, () -> ic.inline(ex));

    // attach predicates to axis path or filter, or create a new filter
    // for $i in 1 where $i  ->  for $i in 1[boolean(.)]
    if(pred.seqType().mayBeNumber()) pred = cc.function(Function.BOOLEAN, info, pred);

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
